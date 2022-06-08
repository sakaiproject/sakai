package org.sakaiproject.modi;

import java.nio.file.Path;

public class MalformedComponentException extends Exception {
    public MalformedComponentException(Path path, String problem) {
        super(String.format("Malformed component at: %s -- %s", path, problem));
    }
}