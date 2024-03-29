/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.core.*;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract bean factory superclass that implements default bean creation,
 * with the full capabilities specified by the {@link RootBeanDefinition} class.
 * Implements the {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * interface in addition to AbstractBeanFactory's {@link #createBean} method.
 * <p>
 * <p>Provides bean creation (with constructor resolution), property population,
 * wiring (including autowiring), and initialization. Handles runtime bean
 * references, resolves managed collections, calls initialization methods, etc.
 * Supports autowiring constructors, properties by name, and properties by type.
 * <p>
 * <p>The main template method to be implemented by subclasses is
 * {@link #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)},
 * used for autowiring by type. In case of a factory which is capable of searching
 * its bean definitions, matching beans will typically be implemented through such
 * a search. For other factory styles, simplified matching algorithms can be implemented.
 * <p>
 * <p>Note that this class does <i>not</i> assume or implement bean definition
 * registry capabilities. See {@link DefaultListableBeanFactory} for an implementation
 * of the {@link org.springframework.beans.factory.ListableBeanFactory} and
 * {@link BeanDefinitionRegistry} interfaces, which represent the API and SPI
 * view of such a factory, respectively.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @see RootBeanDefinition
 * @see DefaultListableBeanFactory
 * @see BeanDefinitionRegistry
 * @since 13.02.2004
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
        implements AutowireCapableBeanFactory {

    /**
     * Strategy for creating bean instances
     */
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

    /**
     * Resolver strategy for method parameter names
     */
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * Whether to automatically try to resolve circular references between beans
     */
    private boolean allowCircularReferences = true;

    /**
     * Whether to resort to injecting a raw bean instance in case of circular reference,
     * even if the injected bean eventually got wrapped.
     */
    private boolean allowRawInjectionDespiteWrapping = false;

    /**
     * Dependency types to ignore on dependency check and autowire, as Set of
     * Class objects: for example, String. Default is none.
     */
    private final Set<Class<?>> ignoredDependencyTypes = new HashSet<Class<?>>();

    /**
     * Dependency interfaces to ignore on dependency check and autowire, as Set of
     * Class objects. By default, only the BeanFactory interface is ignored.
     */
    private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<Class<?>>();

    /**
     * Cache of unfinished FactoryBean instances: FactoryBean name --> BeanWrapper
     */
    private final Map<String, BeanWrapper> factoryBeanInstanceCache =
            new ConcurrentHashMap<String, BeanWrapper>(16);

    /**
     * Cache of filtered PropertyDescriptors: bean Class -> PropertyDescriptor array
     */
    private final ConcurrentMap<Class<?>, PropertyDescriptor[]> filteredPropertyDescriptorsCache =
            new ConcurrentHashMap<Class<?>, PropertyDescriptor[]>(256);


    /**
     * Create a new AbstractAutowireCapableBeanFactory.
     */
    public AbstractAutowireCapableBeanFactory() {
        super();
        ignoreDependencyInterface(BeanNameAware.class);
        ignoreDependencyInterface(BeanFactoryAware.class);
        ignoreDependencyInterface(BeanClassLoaderAware.class);
    }

    /**
     * Create a new AbstractAutowireCapableBeanFactory with the given parent.
     *
     * @param parentBeanFactory parent bean factory, or {@code null} if none
     */
    public AbstractAutowireCapableBeanFactory(BeanFactory parentBeanFactory) {
        this();
        setParentBeanFactory(parentBeanFactory);
    }


    /**
     * Set the instantiation strategy to use for creating bean instances.
     * Default is CglibSubclassingInstantiationStrategy.
     *
     * @see CglibSubclassingInstantiationStrategy
     */
    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }

    /**
     * Return the instantiation strategy to use for creating bean instances.
     */
    protected InstantiationStrategy getInstantiationStrategy() {
        return this.instantiationStrategy;
    }

    /**
     * Set the ParameterNameDiscoverer to use for resolving method parameter
     * names if needed (e.g. for constructor names).
     * <p>Default is a {@link DefaultParameterNameDiscoverer}.
     */
    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    /**
     * Return the ParameterNameDiscoverer to use for resolving method parameter
     * names if needed.
     */
    protected ParameterNameDiscoverer getParameterNameDiscoverer() {
        return this.parameterNameDiscoverer;
    }

    /**
     * Set whether to allow circular references between beans - and automatically
     * try to resolve them.
     * <p>Note that circular reference resolution means that one of the involved beans
     * will receive a reference to another bean that is not fully initialized yet.
     * This can lead to subtle and not-so-subtle side effects on initialization;
     * it does work fine for many scenarios, though.
     * <p>Default is "true". Turn this off to throw an exception when encountering
     * a circular reference, disallowing them completely.
     * <p><b>NOTE:</b> It is generally recommended to not rely on circular references
     * between your beans. Refactor your application logic to have the two beans
     * involved delegate to a third bean that encapsulates their common logic.
     */
    public void setAllowCircularReferences(boolean allowCircularReferences) {
        this.allowCircularReferences = allowCircularReferences;
    }

    /**
     * Set whether to allow the raw injection of a bean instance into some other
     * bean's property, despite the injected bean eventually getting wrapped
     * (for example, through AOP auto-proxying).
     * <p>This will only be used as a last resort in case of a circular reference
     * that cannot be resolved otherwise: essentially, preferring a raw instance
     * getting injected over a failure of the entire bean wiring process.
     * <p>Default is "false", as of Spring 2.0. Turn this on to allow for non-wrapped
     * raw beans injected into some of your references, which was Spring 1.2's
     * (arguably unclean) default behavior.
     * <p><b>NOTE:</b> It is generally recommended to not rely on circular references
     * between your beans, in particular with auto-proxying involved.
     *
     * @see #setAllowCircularReferences
     */
    public void setAllowRawInjectionDespiteWrapping(boolean allowRawInjectionDespiteWrapping) {
        this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
    }

    /**
     * Ignore the given dependency type for autowiring:
     * for example, String. Default is none.
     */
    public void ignoreDependencyType(Class<?> type) {
        this.ignoredDependencyTypes.add(type);
    }

    /**
     * Ignore the given dependency interface for autowiring.
     * <p>This will typically be used by application contexts to register
     * dependencies that are resolved in other ways, like BeanFactory through
     * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
     * <p>By default, only the BeanFactoryAware interface is ignored.
     * For further types to ignore, invoke this method for each type.
     *
     * @see org.springframework.beans.factory.BeanFactoryAware
     * @see org.springframework.context.ApplicationContextAware
     */
    public void ignoreDependencyInterface(Class<?> ifc) {
        this.ignoredDependencyInterfaces.add(ifc);
    }

    @Override
    public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
        super.copyConfigurationFrom(otherFactory);
        if (otherFactory instanceof AbstractAutowireCapableBeanFactory) {
            AbstractAutowireCapableBeanFactory otherAutowireFactory =
                    (AbstractAutowireCapableBeanFactory) otherFactory;
            this.instantiationStrategy = otherAutowireFactory.instantiationStrategy;
            this.allowCircularReferences = otherAutowireFactory.allowCircularReferences;
            this.ignoredDependencyTypes.addAll(otherAutowireFactory.ignoredDependencyTypes);
            this.ignoredDependencyInterfaces.addAll(otherAutowireFactory.ignoredDependencyInterfaces);
        }
    }


    //-------------------------------------------------------------------------
    // Typical methods for creating and populating external bean instances
    //-------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createBean(Class<T> beanClass) throws BeansException {
        // Use prototype bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd = new RootBeanDefinition(beanClass);
        bd.setScope(SCOPE_PROTOTYPE);
        bd.allowCaching = ClassUtils.isCacheSafe(beanClass, getBeanClassLoader());
        return (T) createBean(beanClass.getName(), bd, null);
    }

    @Override
    public void autowireBean(Object existingBean) {
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        bd.allowCaching = ClassUtils.isCacheSafe(bd.getBeanClass(), getBeanClassLoader());
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(bd.getBeanClass().getName(), bd, bw);
    }

    @Override
    public Object configureBean(Object existingBean, String beanName) throws BeansException {
        markBeanAsCreated(beanName);
        BeanDefinition mbd = getMergedBeanDefinition(beanName);
        RootBeanDefinition bd = null;
        if (mbd instanceof RootBeanDefinition) {
            RootBeanDefinition rbd = (RootBeanDefinition) mbd;
            bd = (rbd.isPrototype() ? rbd : rbd.cloneBeanDefinition());
        }
        if (!mbd.isPrototype()) {
            if (bd == null) {
                bd = new RootBeanDefinition(mbd);
            }
            bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            bd.allowCaching = ClassUtils.isCacheSafe(ClassUtils.getUserClass(existingBean), getBeanClassLoader());
        }
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(beanName, bd, bw);
        return initializeBean(beanName, existingBean, bd);
    }

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String beanName) throws BeansException {
        return resolveDependency(descriptor, beanName, null, null);
    }


    //-------------------------------------------------------------------------
    // Specialized methods for fine-grained control over the bean lifecycle
    //-------------------------------------------------------------------------

    @Override
    public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        return createBean(beanClass.getName(), bd, null);
    }

    @Override
    public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        final RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
            return autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
        } else {
            Object bean;
            final BeanFactory parent = this;
            if (System.getSecurityManager() != null) {
                bean = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        return getInstantiationStrategy().instantiate(bd, null, parent);
                    }
                }, getAccessControlContext());
            } else {
                bean = getInstantiationStrategy().instantiate(bd, null, parent);
            }
            populateBean(beanClass.getName(), bd, new BeanWrapperImpl(bean));
            return bean;
        }
    }

    @Override
    public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
            throws BeansException {

        if (autowireMode == AUTOWIRE_CONSTRUCTOR) {
            throw new IllegalArgumentException("AUTOWIRE_CONSTRUCTOR not supported for existing bean instance");
        }
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd =
                new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode, dependencyCheck);
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        populateBean(bd.getBeanClass().getName(), bd, bw);
    }

    @Override
    public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
        markBeanAsCreated(beanName);
        BeanDefinition bd = getMergedBeanDefinition(beanName);
        BeanWrapper bw = new BeanWrapperImpl(existingBean);
        initBeanWrapper(bw);
        applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) {
        return initializeBean(beanName, existingBean, null);
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException {

        Object result = existingBean;
        List<BeanPostProcessor> beanPostProcessorList = getBeanPostProcessors();
        for (BeanPostProcessor beanProcessor : beanPostProcessorList) {
            result = beanProcessor.postProcessBeforeInitialization(result, beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException {

        Object result = existingBean;
        List<BeanPostProcessor> beanPostProcessorList = getBeanPostProcessors();
        for (BeanPostProcessor beanProcessor : beanPostProcessorList) {
            result = beanProcessor.postProcessAfterInitialization(result, beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }

    @Override
    public void destroyBean(Object existingBean) {
        new DisposableBeanAdapter(existingBean, getBeanPostProcessors(), getAccessControlContext()).destroy();
    }


    //---------------------------------------------------------------------
    // Implementation of relevant AbstractBeanFactory template methods
    //---------------------------------------------------------------------

    /**
     * Central method of this class: creates a bean instance,
     * populates the bean instance, applies post-processors, etc.
     *
     * @see #doCreateBean
     */
    @Override
    protected Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) throws BeanCreationException {
        logger.info("6. 创建bean实例 '" + beanName + "'");

        RootBeanDefinition mbdToUse = mbd;

        // Make sure bean class is actually resolved at this point, and
        // clone the bean definition in case of a dynamically resolved Class
        // which cannot be stored in the shared merged bean definition.
        // todo 在这里构建load class...
        Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
        if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            mbdToUse = new RootBeanDefinition(mbd);
            mbdToUse.setBeanClass(resolvedClass);
        }

        // Prepare method overrides.
        try {
            mbdToUse.prepareMethodOverrides();
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
                    beanName, "Validation of method overrides failed", ex);
        }

        try {
            // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
            if (bean != null) {
                return bean;
            }
        } catch (Throwable ex) {
            throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                    "BeanPostProcessor before instantiation of bean failed", ex);
        }

        Object beanInstance = doCreateBean(beanName, mbdToUse, args);
        if (logger.isDebugEnabled()) {
            logger.debug("Finished creating instance of bean '" + beanName + "'");
        }
        return beanInstance;
    }

    /**
     * Actually create the specified bean. Pre-creation processing has already happened
     * at this point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
     * <p>Differentiates between default bean instantiation, use of a
     * factory method, and autowiring a constructor.
     *
     * @param beanName the name of the bean
     * @param mbd      the merged bean definition for the bean
     * @param args     explicit arguments to use for constructor or factory method invocation
     * @return a new instance of the bean
     * @throws BeanCreationException if the bean could not be created
     * @see #instantiateBean
     * @see #instantiateUsingFactoryMethod
     * @see #autowireConstructor
     */
    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {
        // Instantiate the bean.
        BeanWrapper instanceWrapper = null;
        if (mbd.isSingleton()) {
            // 如果是单例的，则FactoryBean只创建一次？
            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        if (instanceWrapper == null) {
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
        final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
        Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);

        // Allow post-processors to modify the merged bean definition.
        synchronized (mbd.postProcessingLock) {
            if (!mbd.postProcessed) {
                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
                mbd.postProcessed = true;
            }
        }

        // Eagerly cache singletons to be able to resolve circular references
        // even when triggered by lifecycle interfaces like BeanFactoryAware.
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            if (logger.isInfoEnabled()) {
                logger.info("8. 先将 '" + beanName + "' 曝光到缓存中，以允许循环依赖");
            }
            addSingletonFactory(beanName, new ObjectFactory<Object>() {
                @Override
                public Object getObject() throws BeansException {
                    return getEarlyBeanReference(beanName, mbd, bean);
                }
            });
        }

        // Initialize the bean instance.
        Object exposedObject = bean;
        try {
            logger.info(beanName + "......................9. " + beanName + "属性注入开始......................" + instanceWrapper.getWrappedInstance());
            // 创建实例后为BeanWrapper，setter方法在BeanWrapper中注入
            populateBean(beanName, mbd, instanceWrapper);
            logger.info(beanName + "......................17. 属性注入结束......................" + instanceWrapper.getWrappedInstance());
            if (exposedObject != null) {
                exposedObject = initializeBean(beanName, exposedObject, mbd);
            }
        } catch (Throwable ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            } else {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
            }
        }

        if (earlySingletonExposure) {
            Object earlySingletonReference = getSingleton(beanName, false);
            if (earlySingletonReference != null) {
                if (exposedObject == bean) {
                    exposedObject = earlySingletonReference;
                } else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                    String[] dependentBeans = getDependentBeans(beanName);
                    Set<String> actualDependentBeans = new LinkedHashSet<String>(dependentBeans.length);
                    for (String dependentBean : dependentBeans) {
                        if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                            actualDependentBeans.add(dependentBean);
                        }
                    }
                    if (!actualDependentBeans.isEmpty()) {
                        throw new BeanCurrentlyInCreationException(beanName,
                                "Bean with name '" + beanName + "' has been injected into other beans [" +
                                        StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                                        "] in its raw version as part of a circular reference, but has eventually been " +
                                        "wrapped. This means that said other beans do not use the final version of the " +
                                        "bean. This is often the result of over-eager type matching - consider using " +
                                        "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
                    }
                }
            }
        }

        // Register bean as disposable.
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }

        return exposedObject;
    }

    /**
     * 	 * todo 这里对于@Bean，通过方法签名的返回值类型，猜测返回的bean type
     * 	 * 				对于FactoryBean，返回的也是对应的FactoryBean子类，
     * 	 * 				例如ReferenceBean<UserService>，返回的是ReferenceBean，而不是UserService
     * 	                注意，@Component中的@Bean，也是返回ReferenceBean
     * @param beanName the name of the bean
     * @param mbd the merged bean definition to determine the type for
     * @param typesToMatch the types to match in case of internal type matching purposes
     * (also signals that the returned {@code Class} will never be exposed to application code)
     * @return
     */
    @Override
    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);

        // Apply SmartInstantiationAwareBeanPostProcessors to predict the
        // eventual type after a before-instantiation shortcut.
        if (targetType != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                    Class<?> predicted = ibp.predictBeanType(targetType, beanName);
                    if (predicted != null && (typesToMatch.length != 1 || FactoryBean.class != typesToMatch[0] ||
                            FactoryBean.class.isAssignableFrom(predicted))) {
                        return predicted;
                    }
                }
            }
        }
        return targetType;
    }

    /**
     * Determine the target type for the given bean definition.
     *
     * @param beanName     the name of the bean (for error handling purposes)
     * @param mbd          the merged bean definition for the bean
     * @param typesToMatch the types to match in case of internal type matching purposes
     *                     (also signals that the returned {@code Class} will never be exposed to application code)
     * @return the type for the bean if determinable, or {@code null} otherwise
     */
    protected Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        // todo 这个方法，仅仅是获取到@Bean方法返回值类型，不会包含其泛型信息，会load bean class
        //      所以，对于接口类型为FactoryBean的，例如ReferenceBean<UserService>
        //      返回的是ReferenceBean
        //      所以还需要进一步判断，如果是FactoryBean，则还要看#getObjectType()，见getTypeForFactoryBean
        //      resolveBeanClass可能得到@Component注册的FactoryBean，其有beanClassName字段
        Class<?> targetType = mbd.getTargetType();
        if (targetType == null) {
            targetType = (mbd.getFactoryMethodName() != null ? getTypeForFactoryMethod(beanName, mbd, typesToMatch) :
                    resolveBeanClass(mbd, beanName, typesToMatch));
            if (ObjectUtils.isEmpty(typesToMatch) || getTempClassLoader() == null) {
                mbd.setTargetType(targetType);
            }
        }
        return targetType;
    }

    /**
     * todo 这个方法，仅仅是获取到@Bean方法返回值类型，不会包含其泛型信息，
     *      所以，对于接口类型为FactoryBean的，目标类型拿不到的。要获取目标类型信息，可以通过下一个方法getTypeForFactoryBean
     *      通过反射Method签名 返回值类型等信息获得。
     * Determine the target type for the given bean definition which is based on
     * a factory method. Only called if there is no singleton instance registered
     * for the target bean already.
     * <p>This implementation determines the type matching {@link #createBean}'s
     * different creation strategies. As far as possible, we'll perform static
     * type checking to avoid creation of the target bean.
     *
     * @param beanName     the name of the bean (for error handling purposes)
     * @param mbd          the merged bean definition for the bean
     * @param typesToMatch the types to match in case of internal type matching purposes
     *                     (also signals that the returned {@code Class} will never be exposed to application code)
     * @return the type for the bean if determinable, or {@code null} otherwise
     * @see #createBean
     */
    protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> preResolved = mbd.resolvedFactoryMethodReturnType;
        if (preResolved != null) {
            return preResolved;
        }

        Class<?> factoryClass;
        boolean isStatic = true;

        String factoryBeanName = mbd.getFactoryBeanName();
        if (factoryBeanName != null) {
            if (factoryBeanName.equals(beanName)) {
                throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
                        "factory-bean reference points back to the same bean definition");
            }
            // Check declared factory method return type on factory class.
            factoryClass = getType(factoryBeanName);
            isStatic = false;
        } else {
            // Check declared factory method return type on bean class.
            // todo 静态类型时 @Bean，beanClassName为其所在类
            factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
        }

        if (factoryClass == null) {
            return null;
        }

        // If all factory methods have the same return type, return that type.
        // Can't clearly figure out exact method due to type converting / autowiring!
        Class<?> commonType = null;
        boolean cache = false;
        int minNrOfArgs = mbd.getConstructorArgumentValues().getArgumentCount();
        Method[] candidates = ReflectionUtils.getUniqueDeclaredMethods(factoryClass);
        for (Method factoryMethod : candidates) {
            if (Modifier.isStatic(factoryMethod.getModifiers()) == isStatic &&
                    factoryMethod.getName().equals(mbd.getFactoryMethodName()) &&
                    factoryMethod.getParameterTypes().length >= minNrOfArgs) {
                // Declared type variables to inspect?
                if (factoryMethod.getTypeParameters().length > 0) {
                    try {
                        // Fully resolve parameter names and argument values.
                        Class<?>[] paramTypes = factoryMethod.getParameterTypes();
                        String[] paramNames = null;
                        ParameterNameDiscoverer pnd = getParameterNameDiscoverer();
                        if (pnd != null) {
                            paramNames = pnd.getParameterNames(factoryMethod);
                        }
                        ConstructorArgumentValues cav = mbd.getConstructorArgumentValues();
                        Set<ConstructorArgumentValues.ValueHolder> usedValueHolders =
                                new HashSet<ConstructorArgumentValues.ValueHolder>(paramTypes.length);
                        Object[] args = new Object[paramTypes.length];
                        for (int i = 0; i < args.length; i++) {
                            ConstructorArgumentValues.ValueHolder valueHolder = cav.getArgumentValue(
                                    i, paramTypes[i], (paramNames != null ? paramNames[i] : null), usedValueHolders);
                            if (valueHolder == null) {
                                valueHolder = cav.getGenericArgumentValue(null, null, usedValueHolders);
                            }
                            if (valueHolder != null) {
                                args[i] = valueHolder.getValue();
                                usedValueHolders.add(valueHolder);
                            }
                        }
                        Class<?> returnType = AutowireUtils.resolveReturnTypeForFactoryMethod(
                                factoryMethod, args, getBeanClassLoader());
                        if (returnType != null) {
                            cache = true;
                            commonType = ClassUtils.determineCommonAncestor(returnType, commonType);
                        }
                    } catch (Throwable ex) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Failed to resolve generic return type for factory method: " + ex);
                        }
                    }
                } else {
                    // todo 最近公共祖先
                    commonType = ClassUtils.determineCommonAncestor(factoryMethod.getReturnType(), commonType);
                }
            }
        }

        if (commonType != null) {
            // Clear return type found: all factory methods return same type.
            if (cache) {
                mbd.resolvedFactoryMethodReturnType = commonType;
            }
            return commonType;
        } else {
            // Ambiguous return types found: return null to indicate "not determinable".
            return null;
        }
    }

    /**
     * This implementation attempts to query the FactoryBean's generic parameter metadata
     * if present to determine the object type. If not present, i.e. the FactoryBean is
     * declared as a raw type, checks the FactoryBean's {@code getObjectType} method
     * on a plain instance of the FactoryBean, without bean properties applied yet.
     * If this doesn't return a type yet, a full creation of the FactoryBean is
     * used as fallback (through delegation to the superclass's implementation).
     * <p>The shortcut check for a FactoryBean is only applied in case of a singleton
     * FactoryBean. If the FactoryBean instance itself is not kept as singleton,
     * it will be fully created to check the type of its exposed object.
     */
    @Override
    protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
        class Holder {
            Class<?> value = null;
        }
        final Holder objectType = new Holder();
        String factoryBeanName = mbd.getFactoryBeanName();
        final String factoryMethodName = mbd.getFactoryMethodName();

        if (factoryBeanName != null) {
            if (factoryMethodName != null) {
                // Try to obtain the FactoryBean's object type without instantiating it at all.
                BeanDefinition fbDef = getBeanDefinition(factoryBeanName);
                if (fbDef instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) fbDef).hasBeanClass()) {
                    // CGLIB subclass methods hide generic parameters; look at the original user class.
                    Class<?> fbClass = ClassUtils.getUserClass(((AbstractBeanDefinition) fbDef).getBeanClass());
                    // Find the given factory method, taking into account that in the case of
                    // @Bean methods, there may be parameters present.
                    ReflectionUtils.doWithMethods(fbClass,
                            new ReflectionUtils.MethodCallback() {
                                @Override
                                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                                    if (method.getName().equals(factoryMethodName) &&
                                            FactoryBean.class.isAssignableFrom(method.getReturnType())) {
                                        objectType.value = GenericTypeResolver.resolveReturnTypeArgument(method, FactoryBean.class);
                                    }
                                }
                            });
                    if (objectType.value != null && Object.class != objectType.value) {
                        return objectType.value;
                    }
                }
            }
            // todo 可能获取不到，只是尽可能，例如@Component中，@Bean方法，由于factoryBean不是@Configuration，
            //       这个时候还没有resolve BeanClass，所以，上面的分支不会走，这个开关直接返回
            // If not resolvable above and the referenced factory bean doesn't exist yet,
            // exit here - we don't want to force the creation of another bean just to
            // obtain a FactoryBean's object type...
            // todo 如果当前factoryBeanName在创建中，则继续往下，否则直接返回了
            if (!isBeanEligibleForMetadataCaching(factoryBeanName)) {
                return null;
            }
        }

        // todo 对于静态方法，直接构建实例，调用getObjectType()，像static @Bean，@DubboReference，factoryBeanName=null，
        //        返回的是null，因为构造函数中此时objectType()不一定有值...
        //        同时，对于@Component类型的FactoryBean，factoryBeanName=null和factoryMethodName=null，也是通过这里构造的
        FactoryBean<?> fb = (mbd.isSingleton() ?
                getSingletonFactoryBeanForTypeCheck(beanName, mbd) :
                getNonSingletonFactoryBeanForTypeCheck(beanName, mbd));

        if (fb != null) {
            // Try to obtain the FactoryBean's object type from this early stage of the instance.
            objectType.value = getTypeForFactoryBean(fb);
            if (objectType.value != null) {
                return objectType.value;
            }
        }

        // No type found - fall back to full creation of the FactoryBean instance.
        return super.getTypeForFactoryBean(beanName, mbd);
    }

    /**
     * Obtain a reference for early access to the specified bean,
     * typically for the purpose of resolving a circular reference.
     *
     * @param beanName the name of the bean (for error handling purposes)
     * @param mbd      the merged bean definition for the bean
     * @param bean     the raw bean instance
     * @return the object to expose as bean reference
     */
    protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
        if (bean != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                    exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
                    if (exposedObject == null) {
                        return exposedObject;
                    }
                }
            }
        }
        return exposedObject;
    }


    //---------------------------------------------------------------------
    // Implementation methods
    //---------------------------------------------------------------------

    /**
     * Obtain a "shortcut" singleton FactoryBean instance to use for a
     * {@code getObjectType()} call, without full initialization of the FactoryBean.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @return the FactoryBean instance, or {@code null} to indicate
     * that we couldn't obtain a shortcut FactoryBean instance
     */
    private FactoryBean<?> getSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
        synchronized (getSingletonMutex()) {
            BeanWrapper bw = this.factoryBeanInstanceCache.get(beanName);
            if (bw != null) {
                return (FactoryBean<?>) bw.getWrappedInstance();
            }
            if (isSingletonCurrentlyInCreation(beanName) ||
                    (mbd.getFactoryBeanName() != null && isSingletonCurrentlyInCreation(mbd.getFactoryBeanName()))) {
                return null;
            }
            Object instance = null;
            try {
                // Mark this bean as currently in creation, even if just partially.
                beforeSingletonCreation(beanName);
                // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
                instance = resolveBeforeInstantiation(beanName, mbd);
                if (instance == null) {
                    bw = createBeanInstance(beanName, mbd, null);
                    instance = bw.getWrappedInstance();
                }
            } finally {
                // Finished partial creation of this bean.
                afterSingletonCreation(beanName);
            }
            FactoryBean<?> fb = getFactoryBean(beanName, instance);
            if (bw != null) {
                this.factoryBeanInstanceCache.put(beanName, bw);
            }
            return fb;
        }
    }

    /**
     * Obtain a "shortcut" non-singleton FactoryBean instance to use for a
     * {@code getObjectType()} call, without full initialization of the FactoryBean.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @return the FactoryBean instance, or {@code null} to indicate
     * that we couldn't obtain a shortcut FactoryBean instance
     */
    private FactoryBean<?> getNonSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
        if (isPrototypeCurrentlyInCreation(beanName)) {
            return null;
        }
        Object instance = null;
        try {
            // Mark this bean as currently in creation, even if just partially.
            beforePrototypeCreation(beanName);
            // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            instance = resolveBeforeInstantiation(beanName, mbd);
            if (instance == null) {
                BeanWrapper bw = createBeanInstance(beanName, mbd, null);
                instance = bw.getWrappedInstance();
            }
        } catch (BeanCreationException ex) {
            // Can only happen when getting a FactoryBean.
            if (logger.isDebugEnabled()) {
                logger.debug("Bean creation exception on non-singleton FactoryBean type check: " + ex);
            }
            onSuppressedException(ex);
            return null;
        } finally {
            // Finished partial creation of this bean.
            afterPrototypeCreation(beanName);
        }
        return getFactoryBean(beanName, instance);
    }

    /**
     * Apply MergedBeanDefinitionPostProcessors to the specified bean definition,
     * invoking their {@code postProcessMergedBeanDefinition} methods.
     *
     * @param mbd      the merged bean definition for the bean
     * @param beanType the actual type of the managed bean instance
     * @param beanName the name of the bean
     * @throws BeansException if any post-processing failed
     * @see MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition
     */
    protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName)
            throws BeansException {

        try {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof MergedBeanDefinitionPostProcessor) {
                    MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
                    bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
                }
            }
        } catch (Exception ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Post-processing failed of bean type [" + beanType + "] failed", ex);
        }
    }

    /**
     * Apply before-instantiation post-processors, resolving whether there is a
     * before-instantiation shortcut for the specified bean.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @return the shortcut-determined bean instance, or {@code null} if none
     */
    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
            logger.info("实例化前的解析，beanName = " + beanName);
            // Make sure bean class is actually resolved at this point.
            if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
                Class<?> targetType = determineTargetType(beanName, mbd);
                if (targetType != null) {
                    bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                    if (bean != null) {
                        bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                    }
                }
            }
            // 如果上面bean为null，则下一次不会进来
            mbd.beforeInstantiationResolved = (bean != null);
        }
        return bean;
    }

    /**
     * Apply InstantiationAwareBeanPostProcessors to the specified bean definition
     * (by class and name), invoking their {@code postProcessBeforeInstantiation} methods.
     * <p>Any returned object will be used as the bean instead of actually instantiating
     * the target bean. A {@code null} return value from the post-processor will
     * result in the target bean being instantiated.
     *
     * @param beanClass the class of the bean to be instantiated
     * @param beanName  the name of the bean
     * @return the bean object to use instead of a default instance of the target bean, or {@code null}
     * @throws BeansException if any post-processing failed
     * @see InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation
     */
    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName)
            throws BeansException {

        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                logger.info("调用bean" + beanName + "后处理器的实例化前方法：" + ibp.getClass().getSimpleName());
                Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Create a new instance for the specified bean, using an appropriate instantiation strategy:
     * factory method, constructor autowiring, or simple instantiation.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @param args     explicit arguments to use for constructor or factory method invocation
     * @return BeanWrapper for the new instance
     * @see #instantiateUsingFactoryMethod
     * @see #autowireConstructor
     * @see #instantiateBean
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
        logger.info("7. 实例化beanName=" + beanName);
        // Make sure bean class is actually resolved at this point.
        Class<?> beanClass = resolveBeanClass(mbd, beanName);

        if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
        }

        if (mbd.getFactoryMethodName() != null) {
            logger.info("使用工厂方法实例化beanName=" + beanName);
            return instantiateUsingFactoryMethod(beanName, mbd, args);
        }

        // Shortcut when re-creating the same bean...
        boolean resolved = false;
        boolean autowireNecessary = false;
        if (args == null) {
            synchronized (mbd.constructorArgumentLock) {
                if (mbd.resolvedConstructorOrFactoryMethod != null) {
                    resolved = true;
                    autowireNecessary = mbd.constructorArgumentsResolved;
                }
            }
        }
        // 解析过？
        if (resolved) {
            if (autowireNecessary) {
                return autowireConstructor(beanName, mbd, null, null);
            } else {
                // 直接实例化
                return instantiateBean(beanName, mbd);
            }
        }

        // Need to determine the constructor...
        Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
        if (ctors != null ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
                mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
            // 参数非空，确定有参数构造函数或者工厂方法
            return autowireConstructor(beanName, mbd, ctors, args);
        }
        // 无参构造函数
        // No special handling: simply use no-arg constructor.
        return instantiateBean(beanName, mbd);
    }

    /**
     * Determine candidate constructors to use for the given bean, checking all registered
     * {@link SmartInstantiationAwareBeanPostProcessor SmartInstantiationAwareBeanPostProcessors}.
     *
     * @param beanClass the raw class of the bean
     * @param beanName  the name of the bean
     * @return the candidate constructors, or {@code null} if none specified
     * @throws org.springframework.beans.BeansException in case of errors
     * @see org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors
     */
    protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(Class<?> beanClass, String beanName)
            throws BeansException {

        if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                    Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);
                    if (ctors != null) {
                        return ctors;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Instantiate the given bean using its default constructor.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @return BeanWrapper for the new instance
     */
    protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
        try {
            Object beanInstance;
            final BeanFactory parent = this;
            if (System.getSecurityManager() != null) {
                beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        return getInstantiationStrategy().instantiate(mbd, beanName, parent);
                    }
                }, getAccessControlContext());
            } else {
                beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
            }
            BeanWrapper bw = new BeanWrapperImpl(beanInstance);
            // 注入cs/customEditor
            initBeanWrapper(bw);
            return bw;
        } catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
        }
    }

    /**
     * Instantiate the bean using a named factory method. The method may be static, if the
     * mbd parameter specifies a class, rather than a factoryBean, or an instance variable
     * on a factory object itself configured using Dependency Injection.
     *
     * @param beanName     the name of the bean
     * @param mbd          the bean definition for the bean
     * @param explicitArgs argument values passed in programmatically via the getBean method,
     *                     or {@code null} if none (-> use constructor argument values from bean definition)
     * @return BeanWrapper for the new instance
     * @see #getBean(String, Object[])
     */
    protected BeanWrapper instantiateUsingFactoryMethod(
            String beanName, RootBeanDefinition mbd, Object[] explicitArgs) {

        return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
    }

    /**
     * "autowire constructor" (with constructor arguments by type) behavior.
     * Also applied if explicit constructor argument values are specified,
     * matching all remaining arguments with beans from the bean factory.
     * <p>This corresponds to constructor injection: In this mode, a Spring
     * bean factory is able to host components that expect constructor-based
     * dependency resolution.
     *
     * @param beanName     the name of the bean
     * @param mbd          the bean definition for the bean
     * @param ctors        the chosen candidate constructors
     * @param explicitArgs argument values passed in programmatically via the getBean method,
     *                     or {@code null} if none (-> use constructor argument values from bean definition)
     * @return BeanWrapper for the new instance
     */
    protected BeanWrapper autowireConstructor(
            String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors, Object[] explicitArgs) {

        return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
    }

    /**
     * Populate the bean instance in the given BeanWrapper with the property values
     * from the bean definition.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @param bw       BeanWrapper with bean instance
     */
    protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
        PropertyValues pvs = mbd.getPropertyValues();
        logger.info("10. " + beanName + "已经含有属性：" + pvs);
        if (bw == null) {
            if (!pvs.isEmpty()) {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
            } else {
                // Skip property population phase for null instance.
                return;
            }
        }

        // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
        // state of the bean before properties are set. This can be used, for example,
        // to support styles of field injection.
        boolean continueWithPropertyPopulation = true;

        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof InstantiationAwareBeanPostProcessor) {
                    InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;

                    if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                        logger.info("11. 调用实例化bean[" + beanName + "]后处理器的后实例化方法，" + ibp + "，不初始化值了");
                        continueWithPropertyPopulation = false;
                        break;
                    }
                }
            }
        }

        if (!continueWithPropertyPopulation) {
            return;
        }

        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
            MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

            // Add property values based on autowire by name if applicable.
            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mbd, bw, newPvs);
            }

            // Add property values based on autowire by type if applicable.
            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mbd, bw, newPvs);
            }

            pvs = newPvs;
        }
        logger.info("12. " + beanName + "获得所有属性值" + pvs);
        boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
        boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);

        if (hasInstAwareBpps || needsDepCheck) {
            PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
            if (hasInstAwareBpps) {
                for (BeanPostProcessor bp : getBeanPostProcessors()) {
                    if (bp instanceof InstantiationAwareBeanPostProcessor) {
                        InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                        logger.info("13. " + beanName + "属性注入前，调用实例化后处理器的postProcessPropertyValues方法，" + ibp);
                        pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
                        if (pvs == null) {
                            return;
                        }
                    }
                }
            }
            if (needsDepCheck) {
                checkDependencies(beanName, mbd, filteredPds, pvs);
            }
        }
        logger.info("14. 调用" + beanName + "，开始设置属性值，属性值是否为空：" + pvs.isEmpty());
        applyPropertyValues(beanName, mbd, bw, pvs);
    }

    /**
     * Fill in any missing property values with references to
     * other beans in this factory if autowire is set to "byName".
     *
     * @param beanName the name of the bean we're wiring up.
     *                 Useful for debugging messages; not used functionally.
     * @param mbd      bean definition to update through autowiring
     * @param bw       BeanWrapper from which we can obtain information about the bean
     * @param pvs      the PropertyValues to register wired objects with
     */
    protected void autowireByName(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
        logger.info("按照属性名注入" + mbd);
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        for (String propertyName : propertyNames) {
            if (containsBean(propertyName)) {
                Object bean = getBean(propertyName);
                pvs.add(propertyName, bean);
                registerDependentBean(propertyName, beanName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added autowiring by name from bean name '" + beanName +
                            "' via property '" + propertyName + "' to bean named '" + propertyName + "'");
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
                            "' by name: no matching bean found");
                }
            }
        }
    }

    /**
     * Abstract method defining "autowire by type" (bean properties by type) behavior.
     * <p>This is like PicoContainer default, in which there must be exactly one bean
     * of the property type in the bean factory. This makes bean factories simple to
     * configure for small namespaces, but doesn't work as well as standard Spring
     * behavior for bigger applications.
     *
     * @param beanName the name of the bean to autowire by type
     * @param mbd      the merged bean definition to update through autowiring
     * @param bw       BeanWrapper from which we can obtain information about the bean
     * @param pvs      the PropertyValues to register wired objects with
     */
    protected void autowireByType(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
        logger.info("按照类型注入" + mbd);
        TypeConverter converter = getCustomTypeConverter();
        if (converter == null) {
            converter = bw;
        }

        Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        for (String propertyName : propertyNames) {
            try {
                PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
                // Don't try autowiring by type for type Object: never makes sense,
                // even if it technically is a unsatisfied, non-simple property.
                if (Object.class != pd.getPropertyType()) {
                    MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
                    // Do not allow eager init for type matching in case of a prioritized post-processor.
                    boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());
                    DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
                    Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
                    if (autowiredArgument != null) {
                        pvs.add(propertyName, autowiredArgument);
                    }
                    for (String autowiredBeanName : autowiredBeanNames) {
                        registerDependentBean(autowiredBeanName, beanName);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Autowiring by type from bean name '" + beanName + "' via property '" +
                                    propertyName + "' to bean named '" + autowiredBeanName + "'");
                        }
                    }
                    autowiredBeanNames.clear();
                }
            } catch (BeansException ex) {
                throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
            }
        }
    }


    /**
     * Return an array of non-simple bean properties that are unsatisfied.
     * These are probably unsatisfied references to other beans in the
     * factory. Does not include simple properties like primitives or Strings.
     *
     * @param mbd the merged bean definition the bean was created with
     * @param bw  the BeanWrapper the bean was created with
     * @return an array of bean property names
     * @see org.springframework.beans.BeanUtils#isSimpleProperty
     */
    protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
        Set<String> result = new TreeSet<String>();
        PropertyValues pvs = mbd.getPropertyValues();
        // TODO 自动装配必须存在getter方法，且非简单类型，property条目没有配置到xml中。
        PropertyDescriptor[] pds = bw.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName()) &&
                    !BeanUtils.isSimpleProperty(pd.getPropertyType())) {
                result.add(pd.getName());
            }
        }
        return StringUtils.toStringArray(result);
    }

    /**
     * Extract a filtered set of PropertyDescriptors from the given BeanWrapper,
     * excluding ignored dependency types or properties defined on ignored dependency interfaces.
     *
     * @param bw    the BeanWrapper the bean was created with
     * @param cache whether to cache filtered PropertyDescriptors for the given bean Class
     * @return the filtered PropertyDescriptors
     * @see #isExcludedFromDependencyCheck
     * @see #filterPropertyDescriptorsForDependencyCheck(org.springframework.beans.BeanWrapper)
     */
    protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw, boolean cache) {
        PropertyDescriptor[] filtered = this.filteredPropertyDescriptorsCache.get(bw.getWrappedClass());
        if (filtered == null) {
            filtered = filterPropertyDescriptorsForDependencyCheck(bw);
            if (cache) {
                PropertyDescriptor[] existing =
                        this.filteredPropertyDescriptorsCache.putIfAbsent(bw.getWrappedClass(), filtered);
                if (existing != null) {
                    filtered = existing;
                }
            }
        }
        return filtered;
    }

    /**
     * Extract a filtered set of PropertyDescriptors from the given BeanWrapper,
     * excluding ignored dependency types or properties defined on ignored dependency interfaces.
     *
     * @param bw the BeanWrapper the bean was created with
     * @return the filtered PropertyDescriptors
     * @see #isExcludedFromDependencyCheck
     */
    protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw) {
        List<PropertyDescriptor> pds =
                new LinkedList<PropertyDescriptor>(Arrays.asList(bw.getPropertyDescriptors()));
        for (Iterator<PropertyDescriptor> it = pds.iterator(); it.hasNext(); ) {
            PropertyDescriptor pd = it.next();
            if (isExcludedFromDependencyCheck(pd)) {
                it.remove();
            }
        }
        return pds.toArray(new PropertyDescriptor[pds.size()]);
    }

    /**
     * Determine whether the given bean property is excluded from dependency checks.
     * <p>This implementation excludes properties defined by CGLIB and
     * properties whose type matches an ignored dependency type or which
     * are defined by an ignored dependency interface.
     *
     * @param pd the PropertyDescriptor of the bean property
     * @return whether the bean property is excluded
     * @see #ignoreDependencyType(Class)
     * @see #ignoreDependencyInterface(Class)
     */
    protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
        return (AutowireUtils.isExcludedFromDependencyCheck(pd) ||
                this.ignoredDependencyTypes.contains(pd.getPropertyType()) ||
                AutowireUtils.isSetterDefinedInInterface(pd, this.ignoredDependencyInterfaces));
    }

    /**
     * Perform a dependency check that all properties exposed have been set,
     * if desired. Dependency checks can be objects (collaborating beans),
     * simple (primitives and String), or all (both).
     *
     * @param beanName the name of the bean
     * @param mbd      the merged bean definition the bean was created with
     * @param pds      the relevant property descriptors for the target bean
     * @param pvs      the property values to be applied to the bean
     * @see #isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
     */
    protected void checkDependencies(
            String beanName, AbstractBeanDefinition mbd, PropertyDescriptor[] pds, PropertyValues pvs)
            throws UnsatisfiedDependencyException {

        int dependencyCheck = mbd.getDependencyCheck();
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null && !pvs.contains(pd.getName())) {
                boolean isSimple = BeanUtils.isSimpleProperty(pd.getPropertyType());
                boolean unsatisfied = (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_ALL) ||
                        (isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
                        (!isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
                if (unsatisfied) {
                    throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, pd.getName(),
                            "Set this property value or disable dependency checking for this bean.");
                }
            }
        }
    }

    /**
     * Apply the given property values, resolving any runtime references
     * to other beans in this bean factory. Must use deep copy, so we
     * don't permanently modify this property.
     *
     * @param beanName the bean name passed for better exception information
     * @param mbd      the merged bean definition
     * @param bw       the BeanWrapper wrapping the target object
     * @param pvs      the new property values
     */
    protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
        if (pvs == null || pvs.isEmpty()) {
            return;
        }

        MutablePropertyValues mpvs = null;
        List<PropertyValue> original;

        if (System.getSecurityManager() != null) {
            if (bw instanceof BeanWrapperImpl) {
                ((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
            }
        }

        if (pvs instanceof MutablePropertyValues) {
            mpvs = (MutablePropertyValues) pvs;
            if (mpvs.isConverted()) {
                // Shortcut: use the pre-converted values as-is.
                try {
                    bw.setPropertyValues(mpvs);
                    return;
                } catch (BeansException ex) {
                    throw new BeanCreationException(
                            mbd.getResourceDescription(), beanName, "Error setting property values", ex);
                }
            }
            original = mpvs.getPropertyValueList();
        } else {
            original = Arrays.asList(pvs.getPropertyValues());
        }

        TypeConverter converter = getCustomTypeConverter();
        if (converter == null) {
            converter = bw;
        }
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

        // Create a deep copy, resolving any references for values.
        List<PropertyValue> deepCopy = new ArrayList<PropertyValue>(original.size());
        boolean resolveNecessary = false;
        for (PropertyValue pv : original) {
            logger.info("15. " + beanName + "属性引用转化 : " + pv);
            if (pv.isConverted()) {
                deepCopy.add(pv);
            } else {
                String propertyName = pv.getName();
                Object originalValue = pv.getValue();
                Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
                Object convertedValue = resolvedValue;
                boolean convertible = bw.isWritableProperty(propertyName) &&
                        !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
                if (convertible) {
                    convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
                }
                // Possibly store converted value in merged bean definition,
                // in order to avoid re-conversion for every created bean instance.
                // TODO 如果非嵌入式注入，含有.或者[]，则转换值成功，接下来就不用转换了
                if (resolvedValue == originalValue) {
                    if (convertible) {
                        pv.setConvertedValue(convertedValue);
                    }
                    deepCopy.add(pv);
                } else if (convertible && originalValue instanceof TypedStringValue &&
                        !((TypedStringValue) originalValue).isDynamic() &&
                        !(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
                    pv.setConvertedValue(convertedValue);
                    deepCopy.add(pv);
                } else {
                    resolveNecessary = true;
                    deepCopy.add(new PropertyValue(pv, convertedValue));
                }
            }
        }
        if (mpvs != null && !resolveNecessary) {
            mpvs.setConverted();
        }

        // Set our (possibly massaged) deep copy.
        try {
            bw.setPropertyValues(new MutablePropertyValues(deepCopy));
        } catch (BeansException ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Error setting property values", ex);
        }
        logger.info("16. " + beanName + "设置完属性值了：" + bw.getWrappedInstance());
    }

    /**
     * Convert the given value for the specified target property.
     */
    private Object convertForProperty(Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {
        if (converter instanceof BeanWrapperImpl) {
            return ((BeanWrapperImpl) converter).convertForProperty(value, propertyName);
        } else {
            PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
            MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
            return converter.convertIfNecessary(value, pd.getPropertyType(), methodParam);
        }
    }


    /**
     * Initialize the given bean instance, applying factory callbacks
     * as well as init methods and bean post processors.
     * <p>Called from {@link #createBean} for traditionally defined beans,
     * and from {@link #initializeBean} for existing bean instances.
     *
     * @param beanName the bean name in the factory (for debugging purposes)
     * @param bean     the new bean instance we may need to initialize
     * @param mbd      the bean definition that the bean was created with
     *                 (can also be {@code null}, if given an existing bean instance)
     * @return the initialized bean instance (potentially wrapped)
     * @see BeanNameAware
     * @see BeanClassLoaderAware
     * @see BeanFactoryAware
     * @see #applyBeanPostProcessorsBeforeInitialization
     * @see #invokeInitMethods
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    invokeAwareMethods(beanName, bean);
                    return null;
                }
            }, getAccessControlContext());
        } else {
            logger.info("18. " + beanName + "调用aware方法");
            invokeAwareMethods(beanName, bean);
        }

        Object wrappedBean = bean;
        if (mbd == null || !mbd.isSynthetic()) {
            logger.info("19. " + beanName + "调用后处理器before init 方法。。");
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }

        try {
            invokeInitMethods(beanName, wrappedBean, mbd);
        } catch (Throwable ex) {
            throw new BeanCreationException(
                    (mbd != null ? mbd.getResourceDescription() : null),
                    beanName, "Invocation of init method failed", ex);
        }

        if (mbd == null || !mbd.isSynthetic()) {
            logger.info("21. " + beanName + "调用后处理器after init 方法");
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }
        return wrappedBean;
    }

    private void invokeAwareMethods(final String beanName, final Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            if (bean instanceof BeanClassLoaderAware) {
                ((BeanClassLoaderAware) bean).setBeanClassLoader(getBeanClassLoader());
            }
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
            }
        }
    }

    /**
     * Give a bean a chance to react now all its properties are set,
     * and a chance to know about its owning bean factory (this object).
     * This means checking whether the bean implements InitializingBean or defines
     * a custom init method, and invoking the necessary callback(s) if it does.
     *
     * @param beanName the bean name in the factory (for debugging purposes)
     * @param bean     the new bean instance we may need to initialize
     * @param mbd      the merged bean definition that the bean was created with
     *                 (can also be {@code null}, if given an existing bean instance)
     * @throws Throwable if thrown by init methods or by the invocation process
     * @see #invokeCustomInitMethod
     */
    protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd)
            throws Throwable {

        boolean isInitializingBean = (bean instanceof InitializingBean);
        if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
            }
            if (System.getSecurityManager() != null) {
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws Exception {
                            ((InitializingBean) bean).afterPropertiesSet();
                            return null;
                        }
                    }, getAccessControlContext());
                } catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            } else {
                logger.info("20. 针对：" + beanName + "调用afterProperties方法");
                ((InitializingBean) bean).afterPropertiesSet();

            }
        }

        if (mbd != null) {
            String initMethodName = mbd.getInitMethodName();
            if (initMethodName != null && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
                    !mbd.isExternallyManagedInitMethod(initMethodName)) {
                logger.info("20. 针对：" + beanName + "调用自定义init方法");
                invokeCustomInitMethod(beanName, bean, mbd);
            }
        }
    }

    /**
     * Invoke the specified custom init method on the given bean.
     * Called by invokeInitMethods.
     * <p>Can be overridden in subclasses for custom resolution of init
     * methods with arguments.
     *
     * @see #invokeInitMethods
     */
    protected void invokeCustomInitMethod(String beanName, final Object bean, RootBeanDefinition mbd) throws Throwable {
        String initMethodName = mbd.getInitMethodName();
        final Method initMethod = (mbd.isNonPublicAccessAllowed() ?
                BeanUtils.findMethod(bean.getClass(), initMethodName) :
                ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName));
        if (initMethod == null) {
            if (mbd.isEnforceInitMethod()) {
                throw new BeanDefinitionValidationException("Couldn't find an init method named '" +
                        initMethodName + "' on bean with name '" + beanName + "'");
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No default init method named '" + initMethodName +
                            "' found on bean with name '" + beanName + "'");
                }
                // Ignore non-existent default lifecycle methods.
                return;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Invoking init method  '" + initMethodName + "' on bean with name '" + beanName + "'");
        }

        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    ReflectionUtils.makeAccessible(initMethod);
                    return null;
                }
            });
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws Exception {
                        initMethod.invoke(bean);
                        return null;
                    }
                }, getAccessControlContext());
            } catch (PrivilegedActionException pae) {
                InvocationTargetException ex = (InvocationTargetException) pae.getException();
                throw ex.getTargetException();
            }
        } else {
            try {
                ReflectionUtils.makeAccessible(initMethod);
                initMethod.invoke(bean);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


    /**
     * Applies the {@code postProcessAfterInitialization} callback of all
     * registered BeanPostProcessors, giving them a chance to post-process the
     * object obtained from FactoryBeans (for example, to auto-proxy them).
     *
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    @Override
    protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
        return applyBeanPostProcessorsAfterInitialization(object, beanName);
    }

    /**
     * Overridden to clear FactoryBean instance cache as well.
     */
    @Override
    protected void removeSingleton(String beanName) {
        super.removeSingleton(beanName);
        this.factoryBeanInstanceCache.remove(beanName);
    }


    /**
     * Special DependencyDescriptor variant for Spring's good old autowire="byType" mode.
     * Always optional; never considering the parameter name for choosing a primary candidate.
     */
    @SuppressWarnings("serial")
    private static class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {

        public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
            super(methodParameter, false, eager);
        }

        @Override
        public String getDependencyName() {
            return null;
        }
    }

}
