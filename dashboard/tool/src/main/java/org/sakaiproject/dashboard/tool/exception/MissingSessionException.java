package org.sakaiproject.dashboard.tool.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Missing Sakai Session")
public class MissingSessionException extends RuntimeException {
    public MissingSessionException() {
        super();
    }

    public MissingSessionException(Throwable cause) {
        super(cause);
    }

    public MissingSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingSessionException(String message) {
        super(message);
    }
}
