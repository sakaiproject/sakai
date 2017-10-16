/**
 * Copyright (c) 2003-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
