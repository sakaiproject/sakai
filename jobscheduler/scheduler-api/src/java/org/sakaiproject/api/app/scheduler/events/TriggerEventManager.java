package org.sakaiproject.api.app.scheduler.events;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 3:40:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TriggerEventManager
{
    public TriggerEvent createTriggerEvent (TriggerEvent.TRIGGER_EVENT_TYPE type, String jobName, String triggerName, Date time, String message);

    public List<TriggerEvent> getTriggerEvents ();

    public List<TriggerEvent> getTriggerEvents (Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types);

    public void purgeEvents (Date before);
}
