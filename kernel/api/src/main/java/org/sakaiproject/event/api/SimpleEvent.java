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

import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;

/**
 * Allows Event object to be serializable for distribution
 * KNL-1184
 */
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
    protected String session = null;

    /** The Event's user id string. May be null. */
    protected String user = null;

    /** The Event's modify flag (true if the event caused a resource modification). */
    protected boolean modify = false;

    /** The Event's notification priority. */
    protected int priority = NotificationService.NOTI_OPTIONAL;

    /** Event creation time. */
    protected Date time = null;

    /** Event server ID */
    protected String serverId = null;

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
     * Access the resource reference.
     * 
     * @return The resource reference string.
     */
    public String getResource() {
        return resource;
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
    
    /**
     * Access the resource metadata.
     * 
     * @return The resource metadata string.
     */
    public LRS_Statement getLrsStatement() {
        return lrsStatement;
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
     * Access the resource reference.
     * 
     * @return The resource reference string.
     */
    public String getContext() {
        return context;
    }

    /**
     * Set the resource reference
     * 
     * @param context resource reference
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Access the UsageSession id. If null, check for a User id.
     * 
     * @return The UsageSession id string.
     */
    public String getSessionId() {
        return session;
    }

    /**
     * Set the session id.
     * 
     * @param id
     *        The session id string.
     */
    public void setSessionId(String id) {
        session = ((id != null) && (id.length() > 0)) ? id : null;
    }

    /**
     * Access the User id. If null, check for a session id.
     * 
     * @return The User id string.
     */
    public String getUserId() {
        return user;
    }

    /**
     * Set the user id.
     * 
     * @param id
     *        The user id string.
     */
    public void setUserId(String id) {
        user = ((id != null) && (id.length() > 0)) ? id : null;
    }

    /**
     * Is this event one that caused a modify to the resource, or just an access.
     * 
     * @return true if the event caused a modify to the resource, false if it was just an access.
     */
    public boolean getModify() {
        return modify;
    }

    /**
     * Set resource modify or access
     * 
     * @param modify modified event
     */
    public void setModify(boolean modify) {
        this.modify = modify;
    }

    /**
     * Access the event's notification priority.
     * 
     * @return The event's notification priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the event's notification priority
     * 
     * @param priority level
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Get the server's ID
     * 
     * @return the server ID string
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Set the server ID string
     * 
     * @param serverId id string
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Get the event's time
     * 
     * @return the event date
     */
    public Date getEventTime() {
        return time;
    }

    /**
     * Set the event's date
     * 
     * @param time date event occurred
     */
    public void setEventTime(Date time) {
        this.time = time;
    }

    /**
     * @return A representation of this event's values as a string.
     */
    public String toString() {
        return seq + ":" + getEvent() + "@" + getResource() + "[" + (getModify() ? "m" : "a") + ", " + getPriority() + "]";
    }

}
