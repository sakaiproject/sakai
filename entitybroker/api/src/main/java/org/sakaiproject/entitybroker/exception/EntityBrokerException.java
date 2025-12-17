/**
 * $Id$
 * $URL$
 * EntityBrokerException.java - entity-broker - Aug 18, 2008 2:44:50 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
