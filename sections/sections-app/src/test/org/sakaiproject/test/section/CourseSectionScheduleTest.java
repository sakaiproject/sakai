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
package org.sakaiproject.test.section;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.section.CourseSectionScheduleImpl;

/**
 * Tests the day/time parsing of CourseSectionScheduleImpl.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseSectionScheduleTest extends TestCase {
	private static final Log log = LogFactory.getLog(CourseSectionScheduleTest.class);
	

    public void testParseMeetingTimes() throws Exception {
    	String meetingTimes = "MON,WED,FRI@9:00am,5:00pm";
    	CourseSectionScheduleImpl schedule = new CourseSectionScheduleImpl(meetingTimes);

    	Assert.assertTrue(schedule.isMonday());
    	Assert.assertTrue(schedule.isWednesday());
    	Assert.assertTrue(schedule.isFriday());

    	Assert.assertTrue( ! schedule.isTuesday());
    	Assert.assertTrue( ! schedule.isThursday());
    	Assert.assertTrue( ! schedule.isSaturday());
    	Assert.assertTrue( ! schedule.isSunday());
    	
    	Assert.assertTrue(schedule.getStartTime().equals("9:00"));
    	Assert.assertTrue(schedule.getEndTime().equals("5:00"));
    	
    	Assert.assertTrue(schedule.isStartTimeAm());
    	Assert.assertTrue( ! schedule.isEndTimeAm());
    }
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
