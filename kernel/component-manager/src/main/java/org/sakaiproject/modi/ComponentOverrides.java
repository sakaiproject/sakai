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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Bean definition source of overrides (i.e., additional config files) for the supplied set of components, within a
 * given directory on disk.
 * <p>
 * The convention is that the directory of the component is its name, and the override file for that component would be
 * the name plus a .xml extension. This implies that these overrides are in Spring bean XML format.
 * <p>
 * Overrides of another format or convention could be applied as a factory post-processor. There is no direct support
 * for that in the {@link Launcher}, but any component can register post-processor beans. If using the
 * {@link SharedApplicationContext} directly, you can simply register another source.
 */
@Slf4j
class ComponentOverrides implements BeanDefinitionSource {

    private final List<TraditionalComponent> components;
    private final Path overridesFolder;

    /**
     * Create an override list for the given components from the given directory.
     * <p>
     * Files that do not apply to these components will not be sourced.
     *
     * @param components      the list of components to use as a filter/selector for override files
     * @param overridesFolder the directory to scan for override files
     */
    public ComponentOverrides(@NonNull List<TraditionalComponent> components, @NonNull Path overridesFolder) {
        this.overridesFolder = overridesFolder;
        this.components = components;
    }

    @Override
    public String getName() {
        return "Local Component Overrides";
    }

    @Override
    public void registerBeans(BeanDefinitionRegistry registry) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        getOverrideFiles().forEach(file -> {
            FileSystemResource override = new FileSystemResource(file);
            reader.loadBeanDefinitions(override);
            log.info("Loaded component overrides from: {}", file);
        });
    }

    private Stream<Path> getOverrideFiles() {
        return components.stream()
                .map(c -> c.getName() + ".xml")
                .map(overridesFolder::resolve);
    }
}
