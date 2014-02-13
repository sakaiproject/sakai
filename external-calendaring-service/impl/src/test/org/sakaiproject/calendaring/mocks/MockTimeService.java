/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.calendaring.mocks;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;

public class MockTimeService implements TimeService {

	@Override
	public Time newTime(long millis) {
		return new org.sakaiproject.mock.domain.Time(new Date(millis));
	}
	
	@Override
	public TimeRange newTimeRange(Time start, Time end, boolean startIncluded, boolean endIncluded) {
		return new MockTimeRange(start, end, startIncluded, endIncluded);
	}
	
	
	/* anything below here is not used at this stage */

	
	@Override
	public boolean clearLocalTimeZone(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean different(Time arg0, Time arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GregorianCalendar getCalendar(TimeZone arg0, int arg1, int arg2,
			int arg3, int arg4, int arg5, int arg6, int arg7) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeZone getLocalTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time newTime() {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public Time newTime(GregorianCalendar arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeBreakdown newTimeBreakdown(int arg0, int arg1, int arg2,
			int arg3, int arg4, int arg5, int arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time newTimeGmt(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time newTimeGmt(TimeBreakdown arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time newTimeGmt(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5, int arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time newTimeLocal(TimeBreakdown arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Time newTimeLocal(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5, int arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeRange newTimeRange(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeRange newTimeRange(Time arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeRange newTimeRange(long arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeRange newTimeRange(Time arg0, Time arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
