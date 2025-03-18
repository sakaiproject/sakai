/******************************************************************************
 * Copyright 2024 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.exception.ServerOverloadException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingSessionException.class)
    public ResponseEntity<Object> handleMissingSessionException(MissingSessionException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Missing or invalid Sakai session", ex);
    }

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<Object> handlePermissionException(PermissionException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Permission denied", ex);
    }

    @ExceptionHandler(IdUnusedException.class)
    public ResponseEntity<Object> handleIdUnusedException(IdUnusedException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", ex);
    }

    @ExceptionHandler(InUseException.class)
    public ResponseEntity<Object> handleInUseException(InUseException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.CONFLICT, "Resource is currently in use", ex);
    }

    @ExceptionHandler(TypeException.class)
    public ResponseEntity<Object> handleTypeException(TypeException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid resource type", ex);
    }

    @ExceptionHandler(ServerOverloadException.class)
    public ResponseEntity<Object> handleServerOverloadException(ServerOverloadException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Server is currently overloaded", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Uncaught exception", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex);
    }

    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String message, Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        
        if (ex.getMessage() != null) {
            body.put("detail", ex.getMessage());
        }
        
        log.error("WEBAPI {}: {}", message, ex.toString());
        log.debug("WEBAPI full exception", ex);
        return new ResponseEntity<>(body, status);
    }
} 
