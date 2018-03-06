/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.impl;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * This is used to handle option dependencies in spring XML.
 * This keeps the spring dependencies out of the class that actually needs the dependency.
 *
 * @see <a href="http://stackoverflow.com/questions/3957430/optional-spring-bean-references">
 * http://stackoverflow.com/questions/3957430/optional-spring-bean-references</a>
 */
public class OptionalFactoryBean extends AbstractFactoryBean<Object> implements BeanNameAware {

    private String beanName;

    @Override
    public void setBeanName(String beanName) {
        this.beanName = BeanFactoryUtils.originalBeanName(beanName);

    }

    @Override
    protected Object createInstance() throws Exception {
        if (getBeanFactory().containsBean(beanName)) {
            return getBeanFactory().getBean(beanName);
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }
}
