/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.util;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Hook some Sakai-specific operations into the normal ApplicationContext
 * refresh cycle: read component manager configuration files, give creators of
 * PostProcessor objects (e.g., SakaiProperties) a chance to do their work,
 * and load a few central components before the rest.
 */
public class SakaiApplicationContext extends GenericApplicationContext {
	private String[] initialSingletonNames;
	private String[] configLocations;

	public SakaiApplicationContext() {
		super(new DefaultListableBeanFactory());
	}
	
	/**
	 * Load component manager configurations. A more normal hook for this is
	 * the "refreshBeanFactory" method, but it's declared final by GenericApplicationContext.
	 */
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.prepareBeanFactory(beanFactory);
		try {
			loadBeanDefinitions(beanFactory);
		} catch (IOException e) {
			throw new ApplicationContextException("I/O error parsing XML document for application context [" + getDisplayName() + "]", e);
		}
	}

	protected void loadBeanDefinitions(ConfigurableListableBeanFactory beanFactory) throws IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader((BeanDefinitionRegistry)beanFactory);
		beanDefinitionReader.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		if (configLocations != null) {
			beanDefinitionReader.loadBeanDefinitions(configLocations);
		}
	}
	
	/**
	 * Before post-processing, load beans which have declared that they want to add post-processors
	 * dynamically.
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		invokePostProcessorCreators(beanFactory);
		super.postProcessBeanFactory(beanFactory);
	}
	
	/**
	 * Load initial beans before going through the default logic.
	 */
	protected void onRefresh() throws BeansException {
		if (initialSingletonNames != null) {
			for (int i = 0; i < initialSingletonNames.length; i++)	{
				getBean(initialSingletonNames[i]);
			}
		}
		super.onRefresh();
	}

	/**
	 * Add bean-created post processors.
	 * @param beanFactory
	 */
	public void invokePostProcessorCreators(ConfigurableListableBeanFactory beanFactory) {
		String[] postProcessorCreatorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessorCreator.class, false, false);
		for (int i = 0; i < postProcessorCreatorNames.length; i++) {
			BeanFactoryPostProcessorCreator postProcessorCreator = (BeanFactoryPostProcessorCreator)beanFactory.getBean(postProcessorCreatorNames[i]);
			for (BeanFactoryPostProcessor beanFactoryPostProcessor : postProcessorCreator.getBeanFactoryPostProcessors()) {
				addBeanFactoryPostProcessor(beanFactoryPostProcessor);
			}
		}
	}

	/**
	 * @param initialSingletonNames initial configuration beans to load, behaving
	 * as universal "dependsOn" targets
	 */
	public void setInitialSingletonNames(String[] initialSingletonNames) {
		this.initialSingletonNames = initialSingletonNames;
	}

	/**
	 * @param configLocations file paths of XML bean definition files to load before refresh
	 */
	public void setConfigLocations(String[] configLocations) {
		this.configLocations = configLocations;
	}
}
