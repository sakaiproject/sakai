package org.sakaiproject.modi;

import java.nio.file.Path;

/**
 * Thrown if a component directory is attempted to be loaded, but does not conform to the expectations.
 * <p>
 * See {@link TraditionalComponent} for the expectations for traditional, on-disk components. There are no other
 * component types defined at this time.
 */
public class MalformedComponentException extends Exception {
    public MalformedComponentException(Path path, String problem) {
        super(String.format("Malformed component at: %s -- %s", path, problem));
    }
}
