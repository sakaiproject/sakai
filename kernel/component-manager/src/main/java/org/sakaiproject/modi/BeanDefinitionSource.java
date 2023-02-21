/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.modi;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Interface used to load bean definitions into a {@link SharedApplicationContext}.
 * <p>
 * When the context is starting, it will request bean definitions from all of its registered sources. The name here is
 * strictly informational.
 * <p>
 * This interface is used to separate the knowledge of where and how the beans are defined from the loading, enabling
 * components to be packaged differently than the traditional structure, with no requirement that the beans defined in
 * any given format. For example, it is now possible to define a component with annotations or entirely in code.
 * <p>
 * This design is used to implement the traditional launch, with base configuration, each component, and then collective
 * overrides serving as the three sources. A new component could be structured as any number of sources and, indeed, the
 * notion of a component is now largely immaterial. The beans have always been loaded into the shared context with no
 * isolation or metadata about which "component" they belong to.
 * <p>
 * For example, service beans that should be available to all webapps could now be located by annotation and registered
 * from plain jars in Tomcat's lib/ directory.
 * <p>
 * {@link SharedApplicationContext#registerBeanSource(BeanDefinitionSource)}
 */
public interface BeanDefinitionSource {
    /**
     * The name of this source of beans.
     */
    public String getName();

    /**
     * A request from a {@link SharedApplicationContext} to register this source's beans .
     */
    public void registerBeans(BeanDefinitionRegistry registry);
}
