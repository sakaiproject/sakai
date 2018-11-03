package org.sakaiproject.progress.api;

public class ProgressServiceException extends RuntimeException {

    public ProgressServiceException(String msg) {
        super(msg);
    }

    public ProgressServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
