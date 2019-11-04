/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 Apereo Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.event.api;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;

/**
 * Allows Event object to be serializable for distribution
 * KNL-1184
 */
@Getter @Setter
public class SimpleEvent implements Event {

    static final long serialVersionUID = 1L;

    /**
     * Constructor
     * 
     * Converts an Event object into a SimpleEvent object
     * in order to be serializable for distribution
     * 
     * @param event the event object
     * @param serverId the server id this event originated from
     */
    public SimpleEvent(Event event, String serverId) {
        if (event == null) {
            throw new IllegalArgumentException("The SimpleEvent event cannot be null.");
        }
        if (serverId == null) {
            throw new IllegalArgumentException("The SimpleEvent serverId cannot be null.");
        }

        setEvent(event.getEvent());
        setResource(event.getResource());
        setSessionId(event.getSessionId());
        setUserId(event.getUserId());
        setContext(event.getContext());
        setModify(event.getModify());
        setPriority(event.getPriority());
        setEventTime(event.getEventTime());
        setServerId(serverId);
        setLRSStatement(event.getLrsStatement());
    }

    /** The Event's sequence number. */
    protected long seq = 0;

    /** The Event's id string. */
    protected String id = "";

    /** The Event's resource reference string. */
    protected String resource = "";

    /** The Event's lrs statement */
    protected LRS_Statement lrsStatement = null;

    /** The Event's context. May be null. */
    protected String context = null;
    
    /** The Event's session id string. May be null. */
    protected String sessionId = null;

    /** The Event's user id string. May be null. */
    protected String userId = null;

    /** The Event's modify flag (true if the event caused a resource modification). */
    protected boolean modify = false;

    /** The Event's notification priority. */
    protected int priority = NotificationService.NOTI_OPTIONAL;

    /** Event creation time. */
    protected Date eventTime = null;

    /** Event server ID */
    protected String serverId = null;

    /** Do we store this event? */
    protected boolean isTransient = false;

    /**
     * Access the event id string
     * 
     * @return The event id string.
     */
    public String getEvent() {
        return id;
    }

    /**
     * Set the event id.
     * 
     * @param id
     *        The event id string.
     */
    public void setEvent(String id) {
        this.id = (id != null) ? id : "";
    }

    /**
     * Set the resource id.
     * 
     * @param id
     *        The resource id string.
     */
    public void setResource(String id) {
        resource = (id != null) ? id : "";
    }

    public boolean getModify() {
        return this.modify;
    }

    /**
     * Set the resource lrsStatement.
     * 
     * @param id
     *        The resource LRS Statement.
     */
    public void setLRSStatement(LRS_Statement lrsStatement) {
        this.lrsStatement = lrsStatement; 
    }

    /**
     * Set the session id.
     * 
     * @param id
     *        The session id string.
     */
    public void setSessionId(String id) {
        sessionId = ((id != null) && (id.length() > 0)) ? id : null;
    }

    /**
     * Set the user id.
     * 
     * @param id
     *        The user id string.
     */
    public void setUserId(String id) {
        userId = ((id != null) && (id.length() > 0)) ? id : null;
    }

    /**
     * @return A representation of this event's values as a string.
     */
    public String toString() {
        return seq + ":" + getEvent() + "@" + getResource() + "[" + (getModify() ? "m" : "a") + ", " + getPriority() + "]";
    }

}
