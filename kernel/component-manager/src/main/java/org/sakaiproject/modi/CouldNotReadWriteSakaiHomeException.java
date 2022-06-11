package org.sakaiproject.modi;

import java.nio.file.Path;

/**
 * Thrown when sakai.home, as configured or computed by default, exists but is not a directory,
 * or is not readable and writable. This is fatal as there are many components that expect the value
 * to be present, and be a writable directory.
 */
public class CouldNotReadWriteSakaiHomeException extends InitializationException {
    public CouldNotReadWriteSakaiHomeException(Path path) {
        super("Could not read Sakai home directory (sakai.home) at: " + path);
    }
}