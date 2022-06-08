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
    @Getter private final Path path;
    @Getter private final String name;

    protected final Path webInf;
    protected final Path classes;
    protected final Path lib;
    protected final Path componentsXml;
    protected final Path demoComponentsXml;

    /**
     * Create a TraditionalComponent from a path on disk. It will not yet be loaded, but the basic structure will be
     * validated.
     *
     * @param path absolute path on disk to the component directory
     * @throws MalformedComponentException if the component is not well-formed
     */
    public TraditionalComponent(Path path) throws MalformedComponentException {
        this.path = path;
        this.name = path.getFileName().toString();
        this.webInf            = path.resolve("WEB-INF");
        this.classes           = webInf.resolve("classes");
        this.lib               = webInf.resolve("lib");
        this.componentsXml     = webInf.resolve("components.xml");
        this.demoComponentsXml = webInf.resolve("demo-components.xml");
        validate();
    }

    /**
     * Create a TraditionalComponent from a path on disk, but log and return an empty Optional, rather than throw
     * if there is a problem in validation.
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

    @Override
    public void registerBeans(BeanDefinitionRegistry registry) {
        withPackageLoader(loader -> {
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
            reader.setBeanClassLoader(loader);
            reader.loadBeanDefinitions(componentBeans());
            if (isDemoMode())
                demoBeans().ifPresent(reader::loadBeanDefinitions);
        });
    }

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
            return Stream.empty();
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
        return hasDemoComponents()
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