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
package org.sakaiproject.lessonbuildertool.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.io.FileSystemResource;

// Reads the Lessons context the webapp deploys, without starting it, as starting it needs all of Sakai
public class LessonsSearchWiringTest {

    private static final String LESSONS_ACCESS = "org.sakaiproject.lessonbuildertool.service.LessonsAccess";

    private final DefaultListableBeanFactory context = new DefaultListableBeanFactory();

    @Before
    public void loadTheDeployedContext() {
        new XmlBeanDefinitionReader(context).loadBeanDefinitions(new FileSystemResource("src/webapp/WEB-INF/applicationContext.xml"));
    }

    @Test
    public void annotationConfigIsThereForTheBeansLessonsAccessBuilds() {
        assertTrue(context.containsBeanDefinition(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
    }

    @Test
    public void lessonsAccessIsDefined() {
        assertEquals(LessonsAccess.class.getName(), context.getBeanDefinition(LESSONS_ACCESS).getBeanClassName());
    }

    @Test
    public void everythingThatBuildsOrChecksABeanGoesThroughLessonsAccess() {
        Map.of(
                "lessonsSearchAdapter", LessonsEntityContentProducer.class,
                "org.sakaiproject.lessonbuildertool.service.LessonBuilderAccessService", LessonBuilderAccessService.class,
                "org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer", LessonBuilderEntityProducer.class
        ).forEach((name, type) -> {
            assertEquals(LESSONS_ACCESS, reference(name, "lessonsAccess"));
            assertTrue(type.getName() + " has no lessonsAccess setter", new BeanWrapperImpl(type).isWritableProperty("lessonsAccess"));
        });
    }

    @Test
    public void searchProducerCanQueueReindexing() {
        assertEquals("org.sakaiproject.event.api.EventTrackingService", reference("lessonsSearchAdapter", "eventTrackingService"));
        assertTrue(new BeanWrapperImpl(LessonsEntityContentProducer.class).isWritableProperty("eventTrackingService"));
    }

    private String reference(String beanName, String property) {
        BeanDefinition definition = context.getBeanDefinition(beanName);
        return ((RuntimeBeanReference) definition.getPropertyValues().get(property)).getBeanName();
    }
}
