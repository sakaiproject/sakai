/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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


/**********************************************************************************
 * $Id$
 *********************************************************************************/
