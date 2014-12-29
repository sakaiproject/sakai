package org.sakaiproject.scheduler.events.hibernate;

import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.springframework.orm.hibernate.EnumUserType;

/**
 * This custom user type was created based on the EnumUserType class modelled at:
 *      http://community.jboss.org/wiki/UserTypeforpersistinganEnumwithaVARCHARcolumn
 * 
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 5:01:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TriggerEventEnumUserType extends EnumUserType<TriggerEvent.TRIGGER_EVENT_TYPE>
{
    public TriggerEventEnumUserType()
    {
        super(TriggerEvent.TRIGGER_EVENT_TYPE.class);
    }
}
