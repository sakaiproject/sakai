/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.types;

import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.springframework.orm.hibernate.EnumUserType;

/**
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author Duffy Gillman
 * Modeled after org.sakaiproject.scheduler.events.hibernate.TriggerEventEnumUserType.java
 */
public class StatusUserType extends EnumUserType<Status> {
    public StatusUserType(){
        super(Status.class);
    }
}
