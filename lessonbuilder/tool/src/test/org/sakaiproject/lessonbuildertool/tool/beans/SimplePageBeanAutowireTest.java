/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.tool.beans;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

// Beans built outside the tool, for search, /access and gradebook, are autowired from the Lessons context
public class SimplePageBeanAutowireTest {

    @Test
    public void qualifierPicksUserTimeServiceOverTimeService() {
        GenericApplicationContext context = lessonsContext(true);
        UserTimeService userTimeService = (UserTimeService) context.getBean("org.sakaiproject.time.api.UserTimeService");

        SimplePageBean bean = new SimplePageBean(context.getAutowireCapableBeanFactory());

        assertSame(userTimeService, ReflectionTestUtils.getField(bean, "userTimeService"));
    }

    @Test(expected = IllegalStateException.class)
    public void contextWithoutAnnotationConfigIsRejected() {
        new SimplePageBean(lessonsContext(false).getAutowireCapableBeanFactory());
    }

    // a context holding every bean SimplePageBean autowires, named as Sakai names them, with TimeService alongside
    private GenericApplicationContext lessonsContext(boolean annotationConfig) {
        GenericApplicationContext context = new GenericApplicationContext();
        if (annotationConfig) {
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        }
        for (Field field : SimplePageBean.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Qualifier qualifier = field.getAnnotation(Qualifier.class);
                String name = qualifier != null ? qualifier.value() : field.getType().getName();
                // init() reads settings, so they answer with their defaults
                Object bean = field.getType() == ServerConfigurationService.class
                        ? mock(ServerConfigurationService.class, invocation -> invocation.getArguments().length == 2 ? invocation.getArgument(1) : null)
                        : mock(field.getType());
                context.getBeanFactory().registerSingleton(name, bean);
            }
        }
        context.getBeanFactory().registerSingleton(TimeService.class.getName(), mock(TimeService.class));
        context.refresh();
        return context;
    }
}
