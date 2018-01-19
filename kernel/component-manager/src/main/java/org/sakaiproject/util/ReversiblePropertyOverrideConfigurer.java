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

    /* NOTE: this was meant to fix https://jira.sakaiproject.org/browse/KNL-791
     * but it fails because factory.getType will return interfaces sometimes instead of the underlying
     * bean class type and this causes the method to fail to identify that the setter exists (since it
     * often does not on the interface). Notably, this fails for ehcache Cache beans since spring reports
     * the class type as EhCache and not Cache.
     * 
	@Override
	protected void applyPropertyValue(ConfigurableListableBeanFactory factory, String beanName, String property, String value) {
	    // check if the bean has this property which can be set
	    Class<?> beanClass = factory.getType(beanName);
	    boolean exists = ReflectionUtils.findField(beanClass, property) != null;
	    if (!exists) {
	        String setProperty = "set"+Character.toUpperCase(property.charAt(0))+(property.length() > 1 ? property.substring(1) : "");
	        Method[] methods = ReflectionUtils.getAllDeclaredMethods(beanClass);
	        ArrayList<String> l = new ArrayList<String>();
	        for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                l.add(method.getName());
                if (method.getName().equals(setProperty) && method.getParameterTypes().length == 1) {
                    exists = true;
                    break;
                }
            }
            log.warn("KNL-791 ("+property+") to bean ("+beanName+") class ("+beanClass.getCanonicalName()+"): "+l);
	    }
	    if (exists) {
	        log.info("Applying property ("+property+"="+value+") to bean ("+beanName+")");
	        super.applyPropertyValue(factory, beanName, property, value);
	    } else {
            log.warn("Skipping configured bean property ("+property+"="+value+") for bean ("+beanName+"), no property with name ("+property+") could be found");
	    }
	}
	*/

	public boolean isBeanNameAtEnd() {
		return beanNameAtEnd;
	}
	public void setBeanNameAtEnd(boolean beanNameAtEnd) {
		this.beanNameAtEnd = beanNameAtEnd;
	}

}
