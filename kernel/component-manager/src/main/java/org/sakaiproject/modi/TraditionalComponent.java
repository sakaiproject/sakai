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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A TraditionalComponent is a Sakai component (implementation bundle) intended to be loaded for use with the
 * ComponentManager / Spring injection. The layout on disk is that the effective component name is taken from the
 * directory, there is a {@code WEB-INF/} directory, and a Spring {@code components.xml} file inside.
 */
@Slf4j
public class TraditionalComponent implements BeanDefinitionSource {
    /** This component's name; by convention, it is the name of the containing directory. */
    @Getter private final String name;

    /** The directory that contains this component. */
    @Getter private final Path path;

    // Convenient references to the constituent parts, set and final upon construction.
    protected final Path webInf;
    protected final Path classes;
    protected final Path lib;
    protected final Path componentsXml;
    protected final Path demoComponentsXml;

    /**
     * Create a TraditionalComponent from a path on disk. It will not yet be loaded, but the basic structure will be
     * validated. Any classes will be loaded in their own ClassLoader, as a child of the current thread.
     * <p>
     * At a minimum, it must have a WEB-INF/ directory containing a components.xml file. Optionally, it may have:
     * <p>
     * - a classes/ directory to put on the classpath - a lib/ directory with .jar files to put on the classpath - a
     * components-demo.xml file for special beans or properties in "demo mode" (when sakai.demo is true)
     *
     * @param path absolute path on disk to the component directory
     * @throws MalformedComponentException if the component is not well-formed
     */
    public TraditionalComponent(Path path) throws MalformedComponentException {
        this.path = path;
        this.name = path.getFileName().toString();
        this.webInf = path.resolve("WEB-INF");
        this.classes = webInf.resolve("classes");
        this.lib = webInf.resolve("lib");
        this.componentsXml = webInf.resolve("components.xml");
        this.demoComponentsXml = webInf.resolve("components-demo.xml");
        validate();
    }

    /**
     * Create a TraditionalComponent from a path on disk, but log and return an empty Optional, rather than throw if
     * there is a problem in validation.
     *
     * @param path absolute path on disk to the component directory
     * @return a validated {@link TraditionalComponent} or an empty Optional
     */
    public static Optional<TraditionalComponent> fromDisk(Path path) {
        try {
            return Optional.of(new TraditionalComponent(path));
        } catch (MalformedComponentException e) {
            log.warn(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Set up a ClassLoader for this components packaged classes/jars, and register the bean definitions from
     * components.xml (and components-demo.xml, if in demo mode) with the Spring context.
     *
     * @param registry the bean registry for the active application context
     */
    @Override
    public void registerBeans(BeanDefinitionRegistry registry) {
        withPackageLoader(loader -> {
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
            reader.setBeanClassLoader(loader);
            reader.loadBeanDefinitions(componentBeans());
            log.info("Loaded bean definitions for {} from: {}", getName(), componentsXml);
            demoBeans().ifPresent(beans -> {
                reader.loadBeanDefinitions(beans);
                log.info("Loaded DEMO bean definitions for {} from: {}", getName(), demoComponentsXml);
            });
        });
    }

    /**
     * Wrap a lambda in a new class loader, activated for loading this component/package.
     * <p>
     * Used so the context can use any referenced classes when instantiating beans, and so this component's dependencies
     * do not pollute or conflict with a broader ClassLoader.
     * <p>
     * This creates a new ClassLoader with the existing one for the current thread as the parent, sets it as current for
     * the thread, then delivers it to the supplied function, and resets to the parent when the function completes.
     *
     * @param function a function that takes the new ClassLoader
     */
    protected void withPackageLoader(Consumer<ClassLoader> function) {
        ClassLoader parent = getClassLoader();
        try {
            ClassLoader packageLoader = newClassLoader(parent);
            setClassLoader(packageLoader);
            function.accept(packageLoader);
        } finally {
            setClassLoader(parent);
        }
    }

    /**
     * Create a ClassLoader with our conventions for classpath, which are typical: add the {@code classes/} directory
     * and all .jar files under {@code lib/}.
     *
     * @param parent the ClassLoader to set as the parent of the new one
     * @return a newly constructed ClassLoader with all classes and jars on the classpath
     */
    protected ClassLoader newClassLoader(ClassLoader parent) {
        // We stream through both classes/ and lib/*.jar, safely converting to URL.
        Stream<Path> classDir = Stream.of(classes).filter(Files::isDirectory);
        URL[] entries = Stream.concat(classDir, jarFiles())
                .map(Path::toUri)
                .map(this::toURL)
                .flatMap(Optional::stream)
                .toArray(URL[]::new);
        return new URLClassLoader(name, entries, parent);
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private void setClassLoader(ClassLoader loader) {
        Thread.currentThread().setContextClassLoader(loader);
    }

    protected Stream<Path> jarFiles() {
        if (!Files.isDirectory(lib)) return Stream.empty();

        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(lib, "*.jar")) {
            jars.forEach(files::add);
            Collections.sort(files);
            return files.stream();
        } catch (IOException e) {
            log.warn("Unable to read jar files in: {}", lib);
            throw new InitializationException(
                    "Problems loading JAR dependencies for component " + getName() + ".\n"
                            + "  Filesystem became unavailable while reading; startup canceled.\n"
                            + "  The directory affected was: " + lib);
        }
    }

    protected Optional<URL> toURL(URI uri) {
        try {
            return Optional.of(uri.toURL());
        } catch (MalformedURLException e) {
            log.debug("Problem building URL for classloader from URI: {}", uri);
            return Optional.empty();
        }
    }

    protected Resource componentBeans() {
        return new FileSystemResource(componentsXml);
    }

    protected Optional<Resource> demoBeans() {
        return isDemoMode() && hasDemoComponents()
                ? Optional.of(new FileSystemResource(demoComponentsXml))
                : Optional.empty();
    }

    protected boolean isDemoMode() {
        return "true".equalsIgnoreCase(System.getProperty("sakai.demo"));
    }

    protected boolean hasDemoComponents() {
        return Files.isRegularFile(demoComponentsXml);
    }

    private void validate() throws MalformedComponentException {
        if (!Files.isDirectory(path))
            throw new MalformedComponentException(path, "is not a directory");
        if (!Files.isDirectory(webInf))
            throw new MalformedComponentException(path, "does not contain a WEB-INF/ directory");
        if (!Files.isRegularFile(componentsXml))
            throw new MalformedComponentException(path, "does not contain WEB-INF/components.xml");
    }
}
