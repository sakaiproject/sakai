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
 * means that they are expanded on disk with a {@code WEB-INF} directory, with a {@code lib} directory and
 * {@code components.xml} inside. The {@code components} directory is almost always within Tomcat's main directory
 * (CATALINA_HOME / CATALINA_BASE), a sibling to {@code webapps}.
 */
@Slf4j
public class TraditionalComponents {
    protected final Path componentsRoot;
    protected final Path overridePath;

    protected final List<TraditionalComponent> components;

    /**
     * Construct a set of traditional components from a root directory and a directory override files.
     * @param componentsRoot the root directory for components (one per directory); may not be null
     * @param overridePath a directory with override files; each matching a component directory name
     *                       with .xml extension; may be null
     */
    public TraditionalComponents(@NonNull Path componentsRoot, Path overridePath) {
        this.componentsRoot = componentsRoot;
        this.overridePath = overridePath;
        components = findComponents(componentsRoot);
    }

    /**
     * Signal that the container has started and components should load.
     */
    public void start() {
        log.info("Starting traditional components in: {}", componentsRoot);
        if (overridePath != null)
            log.info("Will apply overrides from: {}", overridePath);
        components.forEach(component -> {
            log.debug("Found component: " + component.getPath());
            component.registerBeans(null);
        });
        System.exit(-1);
    }

    /**
     * Signal that the container has stopped and components should shut down. Components are typically stateless,
     * so there is usually very little to do, but this unwinds the application context.
     */
    public void stop() {
        log.info("Stopping traditional components in: {}", componentsRoot);
    }

    protected List<TraditionalComponent> findComponents(Path componentsRoot) {
        try (Stream<Path> stream = Files.list(componentsRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .sorted()
                    .map(TraditionalComponent::fromDisk)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Error locating components in: {}", componentsRoot, e);
            return List.of();
        }
    }
}