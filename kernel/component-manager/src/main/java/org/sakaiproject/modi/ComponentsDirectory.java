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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of "traditional" components. These are the typical impl jars packaged with the "component" type, which
 * means that they are expanded on disk with a {@code WEB-INF} directory, with a {@code components.xml} and {@code lib}
 * directory for jars inside. The {@code components} directory is almost always within Tomcat's main directory
 * (CATALINA_HOME / CATALINA_BASE), a sibling to {@code webapps}.
 * <p>
 * This class is designed as immutable and essentially context independent, with all fields being set at construction.
 * It does not read any system properties or access the Environment.
 */
@Slf4j
public class ComponentsDirectory {
    /**
     * Construct a set of traditional components from a root directory and a directory of override files.
     *
     * @param rootPath     the root directory for components (one per directory); may not be null
     * @param overridePath a directory with override files; each matching a component directory name with .xml
     *                     extension; may be null
     */
    public ComponentsDirectory(@NonNull Path rootPath, Path overridePath) {
        this.rootPath = rootPath;
        this.overridePath = overridePath;
        components = findComponents();
        overrides = findOverrides();
    }

    /** The root directory for these components. */
    protected final Path rootPath;

    /** The directory where there may be override files in the form of Spring bean XML. */
    protected final Path overridePath;

    /** The ordered list of components within this directory. */
    protected final List<TraditionalComponent> components;

    /**
     * The overrides for these components.
     * <p>
     * There is some debate about whether fields should ever be optional. This is marked final and set at construction,
     * and used in exactly one place, when starting. I (@botimer) found the design clarity and syntactic consistency of
     * an Optional better than a null check in this specific case. This class will never be serialized, so some of the
     * general concerns about Optional fields do not apply here.
     */
    protected final Optional<ComponentOverrides> overrides;

    /**
     * Signal that the container has started and components should load.
     */
    public void starting(SharedApplicationContext context) {
        log.info("Starting traditional components in: {}", rootPath);
        components.forEach(context::registerBeanSource);
        overrides.ifPresent(context::registerBeanSource);
    }

    /**
     * Signal that the container is stopping and components should shut down. Components are typically stateless, so
     * there is usually very little to do, but this event indicates that the application context is about to be stopped.
     * There is nothing else for us to do here.
     * <p>
     * At this point (and likely permanently, with Spring's rich lifecycle), there is no meaningful entrypoint for the
     * components themselves, so there aren't any events to send here. A new type of component packaging could listen
     * for the Spring events directly (and this loader would not apply, in any case). This method is primarily for
     * symmetry and linearity of logging.
     */
    public void stopping() {
        log.info("Stopping traditional components in: {}", rootPath);
    }

    /**
     * Find all of the directories that are valid components and construct those objects.
     * <p>
     * This does not register or instantiate them, but holds onto them until we start.
     *
     * @return all of the components within this directory, sorted in directory name order
     */
    protected List<TraditionalComponent> findComponents() {
        try (Stream<Path> stream = Files.list(rootPath)) {
            return stream
                    .filter(Files::isDirectory)
                    .sorted()
                    .map(TraditionalComponent::fromDisk)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Error locating components in: {}", rootPath, e);
            return List.of();
        }
    }

    /**
     * Look for overrides matching the components we have located.
     * <p>
     * It isn't strictly necessary to have overrides, so we use the Optional API.
     *
     * @return any overrides for our components in the override path supplied; may be empty
     */
    protected Optional<ComponentOverrides> findOverrides() {
        return overridePath != null && Files.isDirectory(overridePath)
                ? Optional.of(new ComponentOverrides(components, overridePath))
                : Optional.empty();
    }
}
