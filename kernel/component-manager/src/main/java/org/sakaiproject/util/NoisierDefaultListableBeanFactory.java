/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * In Spring 1.2.6 and 1.2.8, when there's a problem pre-instantiating any
 * singleton bean, all existing singleton beans are immediately destroyed.
 * Unfortunately for developers, this destruction occurs without any
 * explanation: neither the problematic bean's name nor the error stack is
 * logged.
 * 
 * This subclasses the default bean factory to log the failing bean's name and a
 * stack trace of the exception.
 */
public class NoisierDefaultListableBeanFactory extends
		DefaultListableBeanFactory {

	public static boolean noisyClose = true;

	public void preInstantiateSingletons() throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Pre-instantiating singletons in factory [" + this
					+ "]");
		}

		// The superclass's variable by this name is declared private.
		String[] beanDefinitionNames = getBeanDefinitionNames();
		String beanName = null; // Remember in case of an exception
		try {
			// for (Iterator it = this.beanDefinitionNames.iterator();
			// it.hasNext();) {
			for (int i = 0; i < beanDefinitionNames.length; i++) {
				beanName = beanDefinitionNames[i];
				if (!containsSingleton(beanName)
						&& containsBeanDefinition(beanName)) {
					RootBeanDefinition bd = (RootBeanDefinition)getMergedBeanDefinition(beanName);
					if (!bd.isAbstract() && bd.isSingleton()
							&& !bd.isLazyInit()) {
						if (bd.hasBeanClass()
								&& FactoryBean.class.isAssignableFrom(bd
										.getBeanClass())) {
							FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX
									+ beanName);
							if (factory.isSingleton()) {
								getBean(beanName);
							}
						} else {
							getBean(beanName);
						}
					}
				}
			}
		} catch (BeansException ex) {
			// Destroy already created singletons to avoid dangling resources.
			logger.error("Failed to preinstantiate the singleton named "
					+ beanName + ". Destroying all Spring beans.", ex);
			try {
				destroySingletons();
			} catch (Exception ex2) {
				logger.error("Pre-instantiating singletons failed, and couldn't destroy already created singletons: "+ex2, ex2);
			}
			throw ex;
		}
	}

	@Override
	public void destroySingletons() {
		if ( noisyClose  ) {
		  Exception e = new Exception("traceback");
		  e.printStackTrace();
		}
		super.destroySingletons();
	}

	@Override
	protected void afterSingletonCreation(String beanName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Finished Creating " + beanName);
		}
		super.afterSingletonCreation(beanName);
	}

	@Override
	public Object getBean(String name, Class requiredType, Object[] args)
			throws BeansException {
		if (!logger.isDebugEnabled()) {
			return super.getBean(name, requiredType, args);
		} else {
			logger.debug("Getting Name " + name);
			try {
				return super.getBean(name, requiredType, args);
			} catch (BeansException e) {
				logger.error("Failed to get bean ", e);
				throw e;
			} finally {
				logger.debug("End getting " + name);
			}
		}
	}
}
