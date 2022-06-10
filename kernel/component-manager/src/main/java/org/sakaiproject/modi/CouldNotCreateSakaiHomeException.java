package org.sakaiproject.modi;

import java.nio.file.Path;

/**
 * Thrown when the sakai.home directory, as configured or computed by default, did not exist
 * and could not be created. This is fatal as there are many components that expect the value
 * to be present, and be a writable directory.
 */
public class CouldNotCreateSakaiHomeException extends RuntimeException {
    public CouldNotCreateSakaiHomeException(Path path) {
        super("Could not create Sakai home directory (sakai.home) at: " + path);
    }
}