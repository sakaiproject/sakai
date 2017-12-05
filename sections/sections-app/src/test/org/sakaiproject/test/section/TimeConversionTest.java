/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.test.section;

import java.sql.Time;

import lombok.extern.slf4j.Slf4j;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.sakaiproject.tool.section.jsf.JsfUtil;

@Slf4j
public class TimeConversionTest extends TestCase {

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
