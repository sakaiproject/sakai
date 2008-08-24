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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;

/**
 * This is just a version of Spring's PropertyOverrideConfigurer that lets the
 * bean name be specified Sakai-style before the property name instead of after.
 * (For example "myBean@the.property" instead of "the.property@myBean".)
 */
public class ReversiblePropertyOverrideConfigurer extends PropertyOverrideConfigurer {
	private boolean beanNameAtEnd = true;
	private String beanNameSeparator;	// Private in the superclass, so we need to stash our own copy.

	protected void processKey(ConfigurableListableBeanFactory factory, String key, String value)
	throws BeansException {
		if (this.beanNameAtEnd) {
			int separatorIndex = key.indexOf(this.beanNameSeparator);
			if (separatorIndex >= 0) {
				String beanProperty = key.substring(0, separatorIndex);
				String beanName = key.substring(separatorIndex+1);
				key = beanName + this.beanNameSeparator + beanProperty;
			}
			super.processKey(factory, key, value);
		}
	}

	public void setBeanNameSeparator(String beanNameSeparator) {
		this.beanNameSeparator = beanNameSeparator;
		super.setBeanNameSeparator(beanNameSeparator);
	}

	public boolean isBeanNameAtEnd() {
		return beanNameAtEnd;
	}
	public void setBeanNameAtEnd(boolean beanNameAtEnd) {
		this.beanNameAtEnd = beanNameAtEnd;
	}

}
