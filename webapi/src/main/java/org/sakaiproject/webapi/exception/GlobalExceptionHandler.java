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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
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
        return createErrorResponse(HttpStatus.FORBIDDEN, "Missing or invalid Sakai session", ex, request);
    }

    @ExceptionHandler(UnknownSiteException.class)
    public ResponseEntity<Object> handleUnknownSiteException(UnknownSiteException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Unknown Sakai site", ex, request);
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<Object> handleForbiddenAccessException(ForbiddenAccessException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access forbidden", ex, request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Object> handleSecurityException(SecurityException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access forbidden", ex, request);
    }

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<Object> handlePermissionException(PermissionException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Permission denied", ex, request);
    }

    @ExceptionHandler(IdUnusedException.class)
    public ResponseEntity<Object> handleIdUnusedException(IdUnusedException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", ex, request);
    }

    @ExceptionHandler(InUseException.class)
    public ResponseEntity<Object> handleInUseException(InUseException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.CONFLICT, "Resource is currently in use", ex, request);
    }

    @ExceptionHandler(TypeException.class)
    public ResponseEntity<Object> handleTypeException(TypeException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid resource type", ex, request);
    }

    @ExceptionHandler(ServerOverloadException.class)
    public ResponseEntity<Object> handleServerOverloadException(ServerOverloadException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Server is currently overloaded", ex, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        return ResponseEntity.status(ex.getStatus()).build();
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Object> handleClientDisconnect(AsyncRequestNotUsableException ex, WebRequest request) {
        // The client closed the connection before the response was fully written (e.g. a browser cancelling
        // an <img> request for a profile image). This is harmless: the connection is already gone, so there
        // is nothing to send back and no need to log it as an error.
        log.debug("Client disconnected before the response could be written: {}", ex.getMessage());
        return null;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Uncaught exception", ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex, request);
    }

    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String message, Exception ex, WebRequest request) {
        // The handler method that threw may have declared a non-JSON `produces` (e.g. octet-stream for image
        // endpoints). That media type is "preset" on the request and would otherwise cause Spring to fail to
        // serialise this JSON error body ("No converter for ... with preset Content-Type 'application/octet-stream'").
        // Clear it so the error body is always written as JSON.
        request.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

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
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
} 
