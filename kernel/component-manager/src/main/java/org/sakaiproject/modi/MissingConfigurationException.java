package org.sakaiproject.modi;

import java.nio.file.Path;

/**
 * Thrown to indicate that a required configuration file is missing. This should halt Sakai startup because missing
 * defaults or core customizations could cause startup that creates or modifies content in unexpected ways.
 */
public class MissingConfigurationException extends RuntimeException {
    public MissingConfigurationException(Path path) {
        super(String.format("Cannot boot Sakai; missing supplied startup configuration file: %s", path));
    }
}
