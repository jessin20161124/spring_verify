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

package org.springframework.web.bind.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;


/**
 * Create a {@link WebRequestDataBinder} instance and initialize it with a
 * {@link WebBindingInitializer}.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class DefaultDataBinderFactory implements WebDataBinderFactory {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private final WebBindingInitializer initializer;


	/**
	 * Create a new {@code DefaultDataBinderFactory} instance.
	 * @param initializer for global data binder initialization
	 * (or {@code null} if none)
	 */
	public DefaultDataBinderFactory(WebBindingInitializer initializer) {
		this.initializer = initializer;
	}


	/**
	 * Create a new {@link WebDataBinder} for the given target object and
	 * initialize it through a {@link WebBindingInitializer}.
	 * TODO 返回ExtendedServletRequestDataBinder
	 * @throws Exception in case of invalid state or arguments
	 */
	@Override
	public final WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
			throws Exception {

		logger.info("为objectName:{}，target:{}，创建WebDataBinder", objectName, target);
		// ToDo 返回WebRequestDataBinder，在ServletRequestDataBinderFactory会被替换为ExtendedServletRequestDataBinder
		WebDataBinder dataBinder = createBinderInstance(target, objectName, webRequest);
		if (this.initializer != null) {
			this.initializer.initBinder(dataBinder, webRequest);
		}
		// 调用@InitBinder方法
		initBinder(dataBinder, webRequest);
		return dataBinder;
	}

	/**
	 * Extension point to create the WebDataBinder instance.
	 * By default this is {@code WebRequestDataBinder}.
	 * @param target the binding target or {@code null} for type conversion only
	 * @param objectName the binding target object name
	 * @param webRequest the current request
	 * @throws Exception in case of invalid state or arguments
	 */
	protected WebDataBinder createBinderInstance(Object target, String objectName, NativeWebRequest webRequest)
			throws Exception {

		return new WebRequestDataBinder(target, objectName);
	}

	/**
	 * Extension point to further initialize the created data binder instance
	 * (e.g. with {@code @InitBinder} methods) after "global" initializaton
	 * via {@link WebBindingInitializer}.
	 * @param dataBinder the data binder instance to customize
	 * @param webRequest the current request
	 * @throws Exception if initialization fails
	 */
	protected void initBinder(WebDataBinder dataBinder, NativeWebRequest webRequest) throws Exception {
	}

}
