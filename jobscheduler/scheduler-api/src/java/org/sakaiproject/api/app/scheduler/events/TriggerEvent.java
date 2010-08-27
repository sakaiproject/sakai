package org.sakaiproject.api.app.scheduler.events;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 3:39:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TriggerEvent
{
    public static enum TRIGGER_EVENT_TYPE
    {
        FIRED, COMPLETE, INFO, DEBUG, ERROR
    }

    public TRIGGER_EVENT_TYPE getEventType();

    public String getJobName();

    public String getTriggerName();

    public Date getTime();

    public String getMessage();
}
