/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
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

import java.sql.Time;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.section.jsf.JsfUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TimeConversionTest extends TestCase {
	private static final Log log = LogFactory.getLog(TimeConversionTest.class);

	private class TimeObject {
		TimeObject(String startTime, boolean startTimeAm, String endTime, boolean endTimeAm) {
			this.startTime = startTime;
			this.startTimeAm = startTimeAm;
			this.endTime = endTime;
			this.endTimeAm = endTimeAm;
		}
		String startTime;
		boolean startTimeAm;
		String endTime;
		boolean endTimeAm;
	}

	public void testConvertStringToTime() throws Exception {
		// Is midnight before 1am?
		TimeObject time = new TimeObject("12", true, "1", true);
		checkBefore(time, true);
		
		time = new TimeObject("1", true, "2", true);
		checkBefore(time, true);

		time = new TimeObject("2", true, "1", true);
		checkBefore(time, false);

		time = new TimeObject("9", false, "10", false);
		checkBefore(time, true);

		time = new TimeObject("10", false, "11", false);
		checkBefore(time, true);

		// Is 11am before noon?
		time = new TimeObject("11", true, "12", false);
		checkBefore(time, true);

		// Is 1pm before noon?
		time = new TimeObject("1", false, "12", false);
		checkBefore(time, false);
	}

	private void checkBefore(TimeObject timeObj, boolean shouldPass) {
		Time startTime = JsfUtil.convertStringToTime(timeObj.startTime, timeObj.startTimeAm);
		Time endTime = JsfUtil.convertStringToTime(timeObj.endTime, timeObj.endTimeAm);

		boolean before = startTime.before(endTime);
		if(shouldPass) {
			Assert.assertTrue(before);
		} else {
			Assert.assertTrue(!before);
		}
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
