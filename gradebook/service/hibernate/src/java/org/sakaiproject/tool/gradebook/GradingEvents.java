/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006 Sakai Foundation, the MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the grading events for a group of students in a particular gradebook
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradingEvents implements Serializable {
    protected Map studentsToEventsMap;

    public GradingEvents() {
        studentsToEventsMap = new HashMap();
    }

    /**
     * Returns a list of grading events, which may be empty if none exist.
     *
     * @param studentId
     * @return
     */
    public List getEvents(String studentId) {
        List gradingEvents = (List)studentsToEventsMap.get(studentId);
        if(gradingEvents == null) {
            return new ArrayList();
        } else {
            return gradingEvents;
        }
    }

    public void addEvent(GradingEvent event) {
        String studentId = event.getStudentId();
        List list = (List)studentsToEventsMap.get(studentId);
        if(list == null) {
            list = new ArrayList();
            studentsToEventsMap.put(studentId, list);
        }
        list.add(event);
    }

}




