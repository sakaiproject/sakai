/**
 * $Id$
 * $URL$
 * EntityBrokerException.java - entity-broker - Aug 18, 2008 2:44:50 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.exception;


/**
 * The base class for all exceptions out of the entity broker system
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityBrokerException extends RuntimeException {

    /**
     * The unique reference for the entity or entity space related to this failure
     */
    public String entityReference;

    public EntityBrokerException(String message, String entityReference) {
        super(message);
        this.entityReference = entityReference;
    }

    public EntityBrokerException(String message, String entityReference, Throwable cause) {
        super(message, cause);
        this.entityReference = entityReference;
    }

}
