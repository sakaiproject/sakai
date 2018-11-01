package org.sakaiproject.widget.api;

public class WidgetServiceException extends RuntimeException {

    public WidgetServiceException(String msg) {
        super(msg);
    }

    public WidgetServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
