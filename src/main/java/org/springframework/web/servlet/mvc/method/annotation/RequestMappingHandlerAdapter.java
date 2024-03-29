/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.mvc.method.annotation;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.*;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.*;
import org.springframework.web.method.support.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;
import org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link AbstractHandlerMethodAdapter} that supports {@link HandlerMethod}s
 * with their method argument and return type signature, as defined via
 * {@code @RequestMapping}.
 * <p>
 * <p>Support for custom argument and return value types can be added via
 * {@link #setCustomArgumentResolvers} and {@link #setCustomReturnValueHandlers}.
 * Or alternatively, to re-configure all argument and return value types,
 * use {@link #setArgumentResolvers} and {@link #setReturnValueHandlers}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @see HandlerMethodArgumentResolver
 * @see HandlerMethodReturnValueHandler
 * @since 3.1
 */
public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter
        implements BeanFactoryAware, InitializingBean {

    private static final boolean completionStagePresent = ClassUtils.isPresent(
            "java.util.concurrent.CompletionStage", RequestMappingHandlerAdapter.class.getClassLoader());


    private List<HandlerMethodArgumentResolver> customArgumentResolvers;

    private HandlerMethodArgumentResolverComposite argumentResolvers;

    private HandlerMethodArgumentResolverComposite initBinderArgumentResolvers;

    private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    private List<ModelAndViewResolver> modelAndViewResolvers;

    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    private List<HttpMessageConverter<?>> messageConverters;

    private List<Object> requestResponseBodyAdvice = new ArrayList<Object>();

    private WebBindingInitializer webBindingInitializer;

    private AsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("HTTP异步请求线程池_");

    private Long asyncRequestTimeout;

    private CallableProcessingInterceptor[] callableInterceptors = new CallableProcessingInterceptor[0];

    private DeferredResultProcessingInterceptor[] deferredResultInterceptors = new DeferredResultProcessingInterceptor[0];

    private boolean ignoreDefaultModelOnRedirect = false;

    private int cacheSecondsForSessionAttributeHandlers = 0;

    private boolean synchronizeOnSession = false;

    private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private ConfigurableBeanFactory beanFactory;

    private final Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache =
            new ConcurrentHashMap<Class<?>, SessionAttributesHandler>(64);

    private final Map<Class<?>, Set<Method>> initBinderCache = new ConcurrentHashMap<Class<?>, Set<Method>>(64);

    private final Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache =
            new LinkedHashMap<ControllerAdviceBean, Set<Method>>();

    private final Map<Class<?>, Set<Method>> modelAttributeCache = new ConcurrentHashMap<Class<?>, Set<Method>>(64);

    private final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache =
            new LinkedHashMap<ControllerAdviceBean, Set<Method>>();


    public RequestMappingHandlerAdapter() {
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false);  // see SPR-7316

        this.messageConverters = new ArrayList<HttpMessageConverter<?>>(4);
        this.messageConverters.add(new ByteArrayHttpMessageConverter());
        this.messageConverters.add(stringHttpMessageConverter);
        this.messageConverters.add(new SourceHttpMessageConverter<Source>());
        this.messageConverters.add(new AllEncompassingFormHttpMessageConverter());
    }


    /**
     * Provide resolvers for custom argument types. Custom resolvers are ordered
     * after built-in ones. To override the built-in support for argument
     * resolution use {@link #setArgumentResolvers} instead.
     */
    public void setCustomArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.customArgumentResolvers = argumentResolvers;
    }

    /**
     * Return the custom argument resolvers, or {@code null}.
     */
    public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
        return this.customArgumentResolvers;
    }

    /**
     * Configure the complete list of supported argument types thus overriding
     * the resolvers that would otherwise be configured by default.
     */
    public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.argumentResolvers = null;
        } else {
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.argumentResolvers.addResolvers(argumentResolvers);
        }
    }

    /**
     * Return the configured argument resolvers, or possibly {@code null} if
     * not initialized yet via {@link #afterPropertiesSet()}.
     */
    public List<HandlerMethodArgumentResolver> getArgumentResolvers() {
        return (this.argumentResolvers != null) ? this.argumentResolvers.getResolvers() : null;
    }

    /**
     * Configure the supported argument types in {@code @InitBinder} methods.
     */
    public void setInitBinderArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.initBinderArgumentResolvers = null;
        } else {
            this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.initBinderArgumentResolvers.addResolvers(argumentResolvers);
        }
    }

    /**
     * Return the argument resolvers for {@code @InitBinder} methods, or possibly
     * {@code null} if not initialized yet via {@link #afterPropertiesSet()}.
     */
    public List<HandlerMethodArgumentResolver> getInitBinderArgumentResolvers() {
        return (this.initBinderArgumentResolvers != null) ? this.initBinderArgumentResolvers.getResolvers() : null;
    }

    /**
     * Provide handlers for custom return value types. Custom handlers are
     * ordered after built-in ones. To override the built-in support for
     * return value handling use {@link #setReturnValueHandlers}.
     */
    public void setCustomReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        this.customReturnValueHandlers = returnValueHandlers;
    }

    /**
     * Return the custom return value handlers, or {@code null}.
     */
    public List<HandlerMethodReturnValueHandler> getCustomReturnValueHandlers() {
        return this.customReturnValueHandlers;
    }

    /**
     * Configure the complete list of supported return value types thus
     * overriding handlers that would otherwise be configured by default.
     */
    public void setReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers == null) {
            this.returnValueHandlers = null;
        } else {
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlers.addHandlers(returnValueHandlers);
        }
    }

    /**
     * Return the configured handlers, or possibly {@code null} if not
     * initialized yet via {@link #afterPropertiesSet()}.
     */
    public List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
        return this.returnValueHandlers.getHandlers();
    }

    /**
     * Provide custom {@link ModelAndViewResolver}s.
     * <p><strong>Note:</strong> This method is available for backwards
     * compatibility only. However, it is recommended to re-write a
     * {@code ModelAndViewResolver} as {@link HandlerMethodReturnValueHandler}.
     * An adapter between the two interfaces is not possible since the
     * {@link HandlerMethodReturnValueHandler#supportsReturnType} method
     * cannot be implemented. Hence {@code ModelAndViewResolver}s are limited
     * to always being invoked at the end after all other return value
     * handlers have been given a chance.
     * <p>A {@code HandlerMethodReturnValueHandler} provides better access to
     * the return type and controller method information and can be ordered
     * freely relative to other return value handlers.
     */
    public void setModelAndViewResolvers(List<ModelAndViewResolver> modelAndViewResolvers) {
        this.modelAndViewResolvers = modelAndViewResolvers;
    }

    /**
     * Return the configured {@link ModelAndViewResolver}s, or {@code null}.
     */
    public List<ModelAndViewResolver> getModelAndViewResolvers() {
        return modelAndViewResolvers;
    }

    /**
     * Set the {@link ContentNegotiationManager} to use to determine requested media types.
     * If not set, the default constructor is used.
     */
    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager;
    }

    /**
     *
     *
     * [org.springframework.http.converter.json.MappingJackson2HttpMessageConverter@6a816369,
     * org.springframework.http.converter.ByteArrayHttpMessageConverter@43b5c521,
     * org.springframework.http.converter.StringHttpMessageConverter@65123405,
     * org.springframework.http.converter.ResourceHttpMessageConverter@54a11e84,
     * org.springframework.http.converter.xml.SourceHttpMessageConverter@35dab552,
     * org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter@582be205,
     * org.springframework.http.converter.feed.AtomFeedHttpMessageConverter@2c6d4719,
     * org.springframework.http.converter.feed.RssChannelHttpMessageConverter@70e4392e,
     * org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter@51db49fb,
     * org.springframework.http.converter.json.MappingJackson2HttpMessageConverter@6065e8e7]

     */
    /**
     * Provide the converters to use in argument resolvers and return value
     * handlers that support reading and/or writing to the body of the
     * request and response.
     */
    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        logger.info("设置消息转换器" + messageConverters);
        this.messageConverters = messageConverters;
    }

    /**
     * Return the configured message body converters.
     */
    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }

    /**
     * Add one or more {@code RequestBodyAdvice} instances to intercept the
     * request before it is read and converted for {@code @RequestBody} and
     * {@code HttpEntity} method arguments.
     */
    public void setRequestBodyAdvice(List<RequestBodyAdvice> requestBodyAdvice) {
        if (requestBodyAdvice != null) {
            this.requestResponseBodyAdvice.addAll(requestBodyAdvice);
        }
    }

    /**
     * Add one or more {@code ResponseBodyAdvice} instances to intercept the
     * response before {@code @ResponseBody} or {@code ResponseEntity} return
     * values are written to the response body.
     */
    public void setResponseBodyAdvice(List<ResponseBodyAdvice<?>> responseBodyAdvice) {
        if (responseBodyAdvice != null) {
            this.requestResponseBodyAdvice.addAll(responseBodyAdvice);
        }
    }

    /**
     * Provide a WebBindingInitializer with "global" initialization to apply
     * to every DataBinder instance.
     */
    public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
        this.webBindingInitializer = webBindingInitializer;
    }

    /**
     * Return the configured WebBindingInitializer, or {@code null} if none.
     */
    public WebBindingInitializer getWebBindingInitializer() {
        return this.webBindingInitializer;
    }

    /**
     * Set the default {@link AsyncTaskExecutor} to use when a controller method
     * return a {@link Callable}. Controller methods can override this default on
     * a per-request basis by returning an {@link WebAsyncTask}.
     * <p>By default a {@link SimpleAsyncTaskExecutor} instance is used.
     * It's recommended to change that default in production as the simple executor
     * does not re-use threads.
     */
    public void setTaskExecutor(AsyncTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Specify the amount of time, in milliseconds, before concurrent handling
     * should time out. In Servlet 3, the timeout begins after the main request
     * processing thread has exited and ends when the request is dispatched again
     * for further processing of the concurrently produced result.
     * <p>If this value is not set, the default timeout of the underlying
     * implementation is used, e.g. 10 seconds on Tomcat with Servlet 3.
     *
     * @param timeout the timeout value in milliseconds
     */
    public void setAsyncRequestTimeout(long timeout) {
        this.asyncRequestTimeout = timeout;
    }

    /**
     * Configure {@code CallableProcessingInterceptor}'s to register on async requests.
     *
     * @param interceptors the interceptors to register
     */
    public void setCallableInterceptors(List<CallableProcessingInterceptor> interceptors) {
        Assert.notNull(interceptors);
        this.callableInterceptors = interceptors.toArray(new CallableProcessingInterceptor[interceptors.size()]);
    }

    /**
     * Configure {@code DeferredResultProcessingInterceptor}'s to register on async requests.
     *
     * @param interceptors the interceptors to register
     */
    public void setDeferredResultInterceptors(List<DeferredResultProcessingInterceptor> interceptors) {
        Assert.notNull(interceptors);
        this.deferredResultInterceptors = interceptors.toArray(new DeferredResultProcessingInterceptor[interceptors.size()]);
    }

    /**
     * By default the content of the "default" model is used both during
     * rendering and redirect scenarios. Alternatively a controller method
     * can declare a {@link RedirectAttributes} argument and use it to provide
     * attributes for a redirect.
     * <p>Setting this flag to {@code true} guarantees the "default" model is
     * never used in a redirect scenario even if a RedirectAttributes argument
     * is not declared. Setting it to {@code false} means the "default" model
     * may be used in a redirect if the controller method doesn't declare a
     * RedirectAttributes argument.
     * <p>The default setting is {@code false} but new applications should
     * consider setting it to {@code true}.
     *
     * @see RedirectAttributes
     */
    public void setIgnoreDefaultModelOnRedirect(boolean ignoreDefaultModelOnRedirect) {
        this.ignoreDefaultModelOnRedirect = ignoreDefaultModelOnRedirect;
    }

    /**
     * Specify the strategy to store session attributes with. The default is
     * {@link org.springframework.web.bind.support.DefaultSessionAttributeStore},
     * storing session attributes in the HttpSession with the same attribute
     * name as in the model.
     */
    public void setSessionAttributeStore(SessionAttributeStore sessionAttributeStore) {
        this.sessionAttributeStore = sessionAttributeStore;
    }

    /**
     * Cache content produced by {@code @SessionAttributes} annotated handlers
     * for the given number of seconds.
     * <p>Possible values are:
     * <ul>
     * <li>-1: no generation of cache-related headers</li>
     * <li>0 (default value): "Cache-Control: no-store" will prevent caching</li>
     * <li>1 or higher: "Cache-Control: max-age=seconds" will ask to cache content;
     * not advised when dealing with session attributes</li>
     * </ul>
     * <p>In contrast to the "cacheSeconds" property which will apply to all general
     * handlers (but not to {@code @SessionAttributes} annotated handlers),
     * this setting will apply to {@code @SessionAttributes} handlers only.
     *
     * @see #setCacheSeconds
     * @see org.springframework.web.bind.annotation.SessionAttributes
     */
    public void setCacheSecondsForSessionAttributeHandlers(int cacheSecondsForSessionAttributeHandlers) {
        this.cacheSecondsForSessionAttributeHandlers = cacheSecondsForSessionAttributeHandlers;
    }

    /**
     * Set if controller execution should be synchronized on the session,
     * to serialize parallel invocations from the same client.
     * <p>More specifically, the execution of the {@code handleRequestInternal}
     * method will get synchronized if this flag is "true". The best available
     * session mutex will be used for the synchronization; ideally, this will
     * be a mutex exposed by HttpSessionMutexListener.
     * <p>The session mutex is guaranteed to be the same object during
     * the entire lifetime of the session, available under the key defined
     * by the {@code SESSION_MUTEX_ATTRIBUTE} constant. It serves as a
     * safe reference to synchronize on for locking on the current session.
     * <p>In many cases, the HttpSession reference itself is a safe mutex
     * as well, since it will always be the same object reference for the
     * same active logical session. However, this is not guaranteed across
     * different servlet containers; the only 100% safe way is a session mutex.
     *
     * @see org.springframework.web.util.HttpSessionMutexListener
     * @see org.springframework.web.util.WebUtils#getSessionMutex(javax.servlet.http.HttpSession)
     */
    public void setSynchronizeOnSession(boolean synchronizeOnSession) {
        this.synchronizeOnSession = synchronizeOnSession;
    }

    /**
     * Set the ParameterNameDiscoverer to use for resolving method parameter names if needed
     * (e.g. for default attribute names).
     * <p>Default is a {@link org.springframework.core.DefaultParameterNameDiscoverer}.
     */
    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    /**
     * A {@link ConfigurableBeanFactory} is expected for resolving expressions
     * in method argument default values.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    /**
     * Return the owning factory of this bean instance, or {@code null} if none.
     */
    protected ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }


    @Override
    public void afterPropertiesSet() {
        // Do this first, it may add ResponseBody advice beans
        initControllerAdviceCache();

        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
            logger.info("参数解析器为空");
        }
        if (this.initBinderArgumentResolvers == null) {
            logger.info("InitBinder解析器为空");
            // 这种情况设置Custom相关属性，会排在默认值后面
            List<HandlerMethodArgumentResolver> resolvers = getDefaultInitBinderArgumentResolvers();
            this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
        if (this.returnValueHandlers == null) {
            logger.info("返回值处理器为空");
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }
    }

    private void initControllerAdviceCache() {
        if (getApplicationContext() == null) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("寻找标有@ControllerAdvice注解的bean " + getApplicationContext());
        }

        List<ControllerAdviceBean> beans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
        AnnotationAwareOrderComparator.sort(beans);

        List<Object> requestResponseBodyAdviceBeans = new ArrayList<Object>();

        // 检测＠ControllerAdvice类上的方法：@ModelAttribute/@InitBinder/或者该类实现RequestBodyAdvice ResponseBodyAdvice
        for (ControllerAdviceBean bean : beans) {
            Set<Method> attrMethods = MethodIntrospector.selectMethods(bean.getBeanType(), MODEL_ATTRIBUTE_METHODS);
            if (!attrMethods.isEmpty()) {
                this.modelAttributeAdviceCache.put(bean, attrMethods);
                if (logger.isInfoEnabled()) {
                    logger.info("Detected @ModelAttribute methods in " + bean);
                }
            }
            Set<Method> binderMethods = MethodIntrospector.selectMethods(bean.getBeanType(), INIT_BINDER_METHODS);
            if (!binderMethods.isEmpty()) {
                this.initBinderAdviceCache.put(bean, binderMethods);
                if (logger.isInfoEnabled()) {
                    logger.info("Detected @InitBinder methods in " + bean);
                }
            }
            if (RequestBodyAdvice.class.isAssignableFrom(bean.getBeanType())) {
                requestResponseBodyAdviceBeans.add(bean);
                if (logger.isInfoEnabled()) {
                    logger.info("Detected RequestBodyAdvice bean in " + bean);
                }
            }
            if (ResponseBodyAdvice.class.isAssignableFrom(bean.getBeanType())) {
                requestResponseBodyAdviceBeans.add(bean);
                if (logger.isInfoEnabled()) {
                    logger.info("Detected ResponseBodyAdvice bean in " + bean);
                }
            }
        }

        if (!requestResponseBodyAdviceBeans.isEmpty()) {
            this.requestResponseBodyAdvice.addAll(0, requestResponseBodyAdviceBeans);
        }
    }

    /**
     * Return the list of argument resolvers to use including built-in resolvers
     * and custom resolvers provided via {@link #setCustomArgumentResolvers}.
     */
    private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>();

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
        resolvers.add(new RequestParamMapMethodArgumentResolver());
        resolvers.add(new PathVariableMethodArgumentResolver());
        resolvers.add(new PathVariableMapMethodArgumentResolver());
        resolvers.add(new MatrixVariableMethodArgumentResolver());
        resolvers.add(new MatrixVariableMapMethodArgumentResolver());
        resolvers.add(new ServletModelAttributeMethodProcessor(false));
        resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
        resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));
        resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
        resolvers.add(new RequestHeaderMapMethodArgumentResolver());
        resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
        resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());
        resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
        resolvers.add(new RedirectAttributesMethodArgumentResolver());
        resolvers.add(new ModelMethodProcessor());
        resolvers.add(new MapMethodProcessor());
        resolvers.add(new ErrorsMethodArgumentResolver());
        resolvers.add(new SessionStatusMethodArgumentResolver());
        resolvers.add(new UriComponentsBuilderMethodArgumentResolver());

        // TODO Custom arguments，自定义的放在后面
        if (getCustomArgumentResolvers() != null) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        // Catch-all
        // 普通类型注入
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
        // TODO 兜底的那一个
        resolvers.add(new ServletModelAttributeMethodProcessor(true));

        return resolvers;
    }

    /**
     * Return the list of argument resolvers to use for {@code @InitBinder}
     * methods including built-in and custom resolvers.
     */
    private List<HandlerMethodArgumentResolver> getDefaultInitBinderArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>();

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
        resolvers.add(new RequestParamMapMethodArgumentResolver());
        resolvers.add(new PathVariableMethodArgumentResolver());
        resolvers.add(new PathVariableMapMethodArgumentResolver());
        resolvers.add(new MatrixVariableMethodArgumentResolver());
        resolvers.add(new MatrixVariableMapMethodArgumentResolver());
        resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());

        // Custom arguments
        if (getCustomArgumentResolvers() != null) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        // Catch-all
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));

        return resolvers;
    }

    /**
     * Return the list of return value handlers to use including built-in and
     * custom handlers provided via {@link #setReturnValueHandlers}.
     */
    private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<HandlerMethodReturnValueHandler>();

        // Single-purpose return value types
        handlers.add(new ModelAndViewMethodReturnValueHandler());
        handlers.add(new ModelMethodProcessor());
        handlers.add(new ViewMethodReturnValueHandler());
        handlers.add(new ResponseBodyEmitterReturnValueHandler(getMessageConverters()));
        handlers.add(new StreamingResponseBodyReturnValueHandler());
        handlers.add(new HttpEntityMethodProcessor(getMessageConverters(),
                this.contentNegotiationManager, this.requestResponseBodyAdvice));
        handlers.add(new HttpHeadersReturnValueHandler());
        handlers.add(new CallableMethodReturnValueHandler());
        handlers.add(new DeferredResultMethodReturnValueHandler());
        handlers.add(new AsyncTaskMethodReturnValueHandler(this.beanFactory));
        handlers.add(new ListenableFutureReturnValueHandler());
        if (completionStagePresent) {
            handlers.add(new CompletionStageReturnValueHandler());
        }

        // Annotation-based return value types
        handlers.add(new ModelAttributeMethodProcessor(false));
        // TODO 处理@ResponseBody返回值
        handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(),
                this.contentNegotiationManager, this.requestResponseBodyAdvice));

        // Multi-purpose return value types
        handlers.add(new ViewNameMethodReturnValueHandler());
        handlers.add(new MapMethodProcessor());

        // Custom return value types
        if (getCustomReturnValueHandlers() != null) {
            handlers.addAll(getCustomReturnValueHandlers());
        }

        // Catch-all
        if (!CollectionUtils.isEmpty(getModelAndViewResolvers())) {
            handlers.add(new ModelAndViewResolverMethodReturnValueHandler(getModelAndViewResolvers()));
        } else {
            handlers.add(new ModelAttributeMethodProcessor(true));
        }

        return handlers;
    }


    /**
     * Always return {@code true} since any method argument and return value
     * type will be processed in some way. A method argument not recognized
     * by any HandlerMethodArgumentResolver is interpreted as a request parameter
     * if it is a simple type, or as a model attribute otherwise. A return value
     * not recognized by any HandlerMethodReturnValueHandler will be interpreted
     * as a model attribute.
     */
    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod) {
        return true;
    }

    @Override
    protected ModelAndView handleInternal(HttpServletRequest request,
                                          HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        ModelAndView mav;
        checkRequest(request);

        // Execute invokeHandlerMethod in synchronized block if required.
        if (this.synchronizeOnSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object mutex = WebUtils.getSessionMutex(session);
                synchronized (mutex) {
                    mav = invokeHandlerMethod(request, response, handlerMethod);
                }
            } else {
                // No HttpSession available -> no mutex necessary
                mav = invokeHandlerMethod(request, response, handlerMethod);
            }
        } else {
            // No synchronization on session demanded at all...
            mav = invokeHandlerMethod(request, response, handlerMethod);
        }

        if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
            if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
                applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
            } else {
                prepareResponse(response);
            }
        }

        return mav;
    }

    /**
     * This implementation always returns -1. An {@code @RequestMapping} method can
     * calculate the lastModified value, call {@link WebRequest#checkNotModified(long)},
     * and return {@code null} if the result of that call is {@code true}.
     */
    @Override
    protected long getLastModifiedInternal(HttpServletRequest request, HandlerMethod handlerMethod) {
        return -1;
    }


    /**
     * Return the {@link SessionAttributesHandler} instance for the given handler type
     * (never {@code null}).
     */
    private SessionAttributesHandler getSessionAttributesHandler(HandlerMethod handlerMethod) {
        Class<?> handlerType = handlerMethod.getBeanType();
        SessionAttributesHandler sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
        if (sessionAttrHandler == null) {
            synchronized (this.sessionAttributesHandlerCache) {
                sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
                if (sessionAttrHandler == null) {
                    sessionAttrHandler = new SessionAttributesHandler(handlerType, sessionAttributeStore);
                    this.sessionAttributesHandlerCache.put(handlerType, sessionAttrHandler);
                }
            }
        }
        return sessionAttrHandler;
    }

    /**
     * Invoke the {@link RequestMapping} handler method preparing a {@link ModelAndView}
     * if view resolution is required.
     *
     * @see #createInvocableHandlerMethod(HandlerMethod)
     * @since 4.2
     */
    protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                               HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        ServletWebRequest webRequest = new ServletWebRequest(request, response);

        logger.info("[Spring MVC] 4. 调用invokeHandlerMethod处理request：" + request.getRequestURI());

        // 封装了所有的@InitBinder的注解方法
        // TODO 每次都新建一下
        // 先放全局@Controller中@InitBinder方法，再添加当前Handler类中的@InitBinder方法
        WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
        ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

        // HandlerMethod的封装
        ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
        invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
        // TODO binderFactory可能需要传递给argumentResolvers进行参数转换。
        invocableMethod.setDataBinderFactory(binderFactory);
        invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
        modelFactory.initModel(webRequest, mavContainer, invocableMethod);
        mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

        // TODO 实际是StandardServletAsyncWebRequest，这个类封装了request/response，以便处理完成时转发
        AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
        // TODO 看看人家怎么用的
        asyncWebRequest.setTimeout(this.asyncRequestTimeout);

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        asyncManager.setTaskExecutor(this.taskExecutor);
        asyncManager.setAsyncWebRequest(asyncWebRequest);
        asyncManager.registerCallableInterceptors(this.callableInterceptors);
        asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

        // 异步请求重新分发时，会有并发结果，直接返回callable中的result
        if (asyncManager.hasConcurrentResult()) {
            // TODO 获取并发处理结果
            Object result = asyncManager.getConcurrentResult();
            // 当时存储的MavContainer
            mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
            asyncManager.clearConcurrentResult();
            logger.info("发现并发结果： [" + result + "]");
            invocableMethod = invocableMethod.wrapConcurrentResult(result);
        }

        // TODO 先使用参数解析器获取到所有参数值，再反射调用method，最后调用返回值处理器处理返回值，
        // TODO 返回值没有返回，可能在返回值处理器中被处理了，也可能放在mavContainer中
        invocableMethod.invokeAndHandle(webRequest, mavContainer);
        // 如果是callable返回值，在CallableMethodReturnValueHandler中会开启处理该WebAsyncRequest，这里会返回null
        if (asyncManager.isConcurrentHandlingStarted()) {
            logger.info("并发请求开始了，直接结束当前请求，返回null，不处理response");
            return null;
        }

        return getModelAndView(mavContainer, modelFactory, webRequest);
    }

    /**
     * Create a {@link ServletInvocableHandlerMethod} from the given {@link HandlerMethod} definition.
     *
     * @param handlerMethod the {@link HandlerMethod} definition
     * @return the corresponding {@link ServletInvocableHandlerMethod} (or custom subclass thereof)
     * @since 4.2
     */
    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new ServletInvocableHandlerMethod(handlerMethod);
    }

    private ModelFactory getModelFactory(HandlerMethod handlerMethod, WebDataBinderFactory binderFactory) {
        SessionAttributesHandler sessionAttrHandler = getSessionAttributesHandler(handlerMethod);
        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.modelAttributeCache.get(handlerType);
        if (methods == null) {
            // 含有@RequestMapping/@ModelAttribute注解的方法
            methods = MethodIntrospector.selectMethods(handlerType, MODEL_ATTRIBUTE_METHODS);
            this.modelAttributeCache.put(handlerType, methods);
        }
        List<InvocableHandlerMethod> attrMethods = new ArrayList<InvocableHandlerMethod>();
        // Global methods first
        for (Entry<ControllerAdviceBean, Set<Method>> entry : this.modelAttributeAdviceCache.entrySet()) {
            if (entry.getKey().isApplicableToBeanType(handlerType)) {
                Object bean = entry.getKey().resolveBean();
                for (Method method : entry.getValue()) {
                    attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
                }
            }
        }
        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
        }
        return new ModelFactory(attrMethods, binderFactory, sessionAttrHandler);
    }

    private InvocableHandlerMethod createModelAttributeMethod(WebDataBinderFactory factory, Object bean, Method method) {
        InvocableHandlerMethod attrMethod = new InvocableHandlerMethod(bean, method);
        attrMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        attrMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        attrMethod.setDataBinderFactory(factory);
        return attrMethod;
    }

    /**
     * TODO 先放全局@ControllerAdvice中@InitBinder方法，再添加当前Handler类中的@InitBinder方法
     *
     * @param handlerMethod
     * @return
     * @throws Exception
     */
    private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.initBinderCache.get(handlerType);
        // TODO 寻找当前HandlerMethod所在类的@InitBinder注解
        if (methods == null) {
            methods = MethodIntrospector.selectMethods(handlerType, INIT_BINDER_METHODS);
            this.initBinderCache.put(handlerType, methods);
        }
        List<InvocableHandlerMethod> initBinderMethods = new ArrayList<InvocableHandlerMethod>();
        // Global methods first
        // TODO @ControllerAdvice中的@InitBinder??
        for (Entry<ControllerAdviceBean, Set<Method>> entry : this.initBinderAdviceCache.entrySet()) {
            if (entry.getKey().isApplicableToBeanType(handlerType)) {
                Object bean = entry.getKey().resolveBean();
                for (Method method : entry.getValue()) {
                    initBinderMethods.add(createInitBinderMethod(bean, method));
                }
            }
        }
        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            initBinderMethods.add(createInitBinderMethod(bean, method));
        }
        return createDataBinderFactory(initBinderMethods);
    }

    /**
     * TODO 封装每个@InitBinder方法，还是使用InvocableHandlerMethod
     *
     * @param bean
     * @param method
     * @return
     */
    private InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {
        InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);
        // 参数解析为initBinderArgumentResolvers
        binderMethod.setHandlerMethodArgumentResolvers(this.initBinderArgumentResolvers);
        // todo 引进binder全局初始化器
        binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(this.webBindingInitializer));
        binderMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        return binderMethod;
    }

    /**
     * Template method to create a new InitBinderDataBinderFactory instance.
     * <p>The default implementation creates a ServletRequestDataBinderFactory.
     * This can be overridden for custom ServletRequestDataBinder subclasses.
     *
     * @param binderMethods {@code @InitBinder} methods
     * @return the InitBinderDataBinderFactory instance to use
     * @throws Exception in case of invalid state or arguments
     */
    protected InitBinderDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods)
            throws Exception {

        // todo 数据绑定工厂
        return new ServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer());
    }

    private ModelAndView getModelAndView(ModelAndViewContainer mavContainer,
                                         ModelFactory modelFactory, NativeWebRequest webRequest) throws Exception {

        modelFactory.updateModel(webRequest, mavContainer);
        // TODO 如果请求已经被处理，则返回modelAndView为null
        if (mavContainer.isRequestHandled()) {
            return null;
        }
        // 将mavContainer转化为modelAndView
        ModelMap model = mavContainer.getModel();
        ModelAndView mav = new ModelAndView(mavContainer.getViewName(), model);
        if (!mavContainer.isViewReference()) {
            mav.setView((View) mavContainer.getView());
        }
        if (model instanceof RedirectAttributes) {
            Map<String, ?> flashAttributes = ((RedirectAttributes) model).getFlashAttributes();
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            RequestContextUtils.getOutputFlashMap(request).putAll(flashAttributes);
        }
        return mav;
    }


    /**
     * MethodFilter that matches {@link InitBinder @InitBinder} methods.
     */
    public static final MethodFilter INIT_BINDER_METHODS = new MethodFilter() {
        @Override
        public boolean matches(Method method) {
            return AnnotationUtils.findAnnotation(method, InitBinder.class) != null;
        }
    };

    /**
     * MethodFilter that matches {@link ModelAttribute @ModelAttribute} methods.
     */
    public static final MethodFilter MODEL_ATTRIBUTE_METHODS = new MethodFilter() {
        @Override
        public boolean matches(Method method) {
            return ((AnnotationUtils.findAnnotation(method, RequestMapping.class) == null) &&
                    (AnnotationUtils.findAnnotation(method, ModelAttribute.class) != null));
        }
    };

}
