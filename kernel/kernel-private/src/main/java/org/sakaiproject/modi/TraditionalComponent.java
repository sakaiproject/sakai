package org.sakaiproject.modi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A TraditionalComponent is a Sakai component (implementation bundle) intended to be loaded for use with the
 * ComponentManager / Spring injection. The layout on disk is that the effective component name is taken from the
 * directory, there is a {@code WEB-INF/} directory, and a Spring {@code components.xml} file inside.
 */
@Slf4j
public class TraditionalComponent {
    @Getter
    private final Path path;

    /**
     * Create a TraditionalComponent from a path on disk. It will not yet be loaded, but the basic structure will be
     * validated.
     *
     * @param path absolute path on disk to the component directory
     * @throws MalformedComponentException if the component is not well-formed
     */
    public TraditionalComponent(Path path) throws MalformedComponentException {
        this.path = path;
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

    private void validate() throws MalformedComponentException {
        if (!Files.isDirectory(path))
            throw new MalformedComponentException(path, "is not a directory");
        if (!Files.isDirectory(path.resolve("WEB-INF")))
            throw new MalformedComponentException(path, "does not contain WEB-INF/ directory");
        if (!Files.isRegularFile(path.resolve("WEB-INF/components.xml")))
            throw new MalformedComponentException(path, "does not contain WEB-INF/components.xml");
    }
}