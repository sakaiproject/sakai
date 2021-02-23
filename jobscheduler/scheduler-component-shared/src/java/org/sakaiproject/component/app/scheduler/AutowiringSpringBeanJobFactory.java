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
package org.sakaiproject.component.app.scheduler;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import lombok.Setter;

/**
 * This JobFactory autowires automatically the created quartz bean with spring @Autowired dependencies.
 *
 * @author jelies (thanks to Brian Matthews)
 * @link http://webcache.googleusercontent.com/search?q=cache:FH-N1i--sDgJ:blog.btmatthews.com/2011/09/24/inject-application-context-dependencies-in-quartz-job-beans/+&cd=7&hl=en&ct=clnk&gl=es)
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    @Setter private ApplicationContext applicationContext;

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
    	AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
    	
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        // This is set to disable dependency checking, otherwise some bean properties throw an error here
        beanFactory.autowireBeanProperties(job, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        return job;
    }
}