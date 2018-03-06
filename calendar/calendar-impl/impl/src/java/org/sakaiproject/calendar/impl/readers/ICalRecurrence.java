/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.calendar.impl.readers;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.property.RRule;

import org.sakaiproject.calendar.impl.DailyRecurrenceRule;
import org.sakaiproject.calendar.impl.MWFRecurrenceRule;
import org.sakaiproject.calendar.impl.MonthlyRecurrenceRule;
import org.sakaiproject.calendar.impl.TThRecurrenceRule;
import org.sakaiproject.calendar.impl.WeeklyRecurrenceRule;
import org.sakaiproject.calendar.impl.YearlyRecurrenceRule;
import org.sakaiproject.exception.ImportException;

@Slf4j
public class ICalRecurrence  
{
	private String rrule_text = "";
	private RRule rrule = null;
	private Recur recur = null;
	private static final Map<String,String> frequencyLookup;
	private static List moWeFr = Arrays.asList("MO","WE","FR");
	
	static {
		
		Map<String, String>m = new HashMap<String,String>();
		
		m.put("DAILY",new DailyRecurrenceRule().getFrequency());
		m.put("MONTHLY",new MonthlyRecurrenceRule().getFrequency());
		m.put("WEEKLY", new WeeklyRecurrenceRule().getFrequency());
		m.put("YEARLY",new YearlyRecurrenceRule().getFrequency());

		// These don't have corresponding explicit frequency
		// designators in iCal.  The corresponding Sakai
		// frequency string will be set below.
		
		m.put(new MWFRecurrenceRule().getFrequency(),new MWFRecurrenceRule().getFrequency());
		m.put(new TThRecurrenceRule().getFrequency(),new TThRecurrenceRule().getFrequency());

		
		// iCal has additional frequencies for hour, minute, day.
		// They are not handled at all.
		
		frequencyLookup = Collections.unmodifiableMap(m);
	}

	public ICalRecurrence(String rrule_text) throws ImportException {
		
		if (rrule_text == null) {
			return;	
		}
		
		try {
			this.rrule_text = rrule_text;
			this.rrule = new RRule(rrule_text);
		} catch (ParseException e) {
			log.warn("Parse exception for iCal recurrence rule: "+rrule_text);
			throw new ImportException(e);
		}
		
		// The RRule has been successfully created, now use it.
		recur = rrule.getRecur();

		// Make sure the rule makes sense.
		isValidateRRule();
	}
	
	// Get declared event frequency, e.g weekly, monthly, daily
	public String getFrequency() {
		if (recur == null) {
			return null;
		}

		String extendedFrequency = recur.getFrequency();
		WeekDayList dayList = recur.getDayList();
		String dayListAsString = dayList.toString();
		
		if (! dayList.isEmpty()) {
			if ("MO,WE,FR".equals(dayListAsString)) {
				extendedFrequency = new MWFRecurrenceRule().getFrequency();
			}
			if ("TU,TH".equals(dayListAsString)) {
				extendedFrequency = new TThRecurrenceRule().getFrequency();
			}
//			if ("MO,TU,WE,TH,FR".equals(dayListAsString)) {
//				extendedFrequency = new TThRecurrenceRule().getFrequency();
//			}
		}
		
		String sakaiFrequency = frequencyLookup.get(extendedFrequency);

		return sakaiFrequency;
	}
	
	// No instances past this time are created.
	public Date getEND_TIME() {
		if (recur == null) {
			return null;
		}
		Date d = recur.getUntil();
		return d;
	}

	// How frequently should the event be scheduled?
	// Every other year?  Every week?
	// Default to scheduling every possible time.
	public Integer getINTERVAL() {
		if (recur == null) {
			return null;
		}
		
		Integer i = recur.getInterval();
		if (i.equals(-1)) {
			i= new Integer(1);
		}
		return i;
	}

	// Count of how many times this event should be repeated.
	// same as count?  This many occurrences. 0 means no limit.
	public Integer getREPEAT() {
		if (recur == null) {
			return null;
		}
		Integer c = recur.getCount();
		if (c.equals(-1)) {
			return null;
		}
		return c;
	}
	
	

	/*
	 * Take a recurrence and validate the settings to ensure
	 * they make sense.  Currently this will only log the problem 
	 * and return a flag indicating whether or not the the rule is valid.
	 * Calling code can determine what to do with the invalid recurrence.
	 * The order of the tests is important.  These tests are also
	 * applied in GenericCalendarImporter.
	 */
	
	public Boolean isValidateRRule() {
		
		Boolean valid = Boolean.valueOf(true);
		String reason = "";
		
		// It is ok to specify no modifiers
		if (getEND_TIME() == null && getREPEAT() == null && getINTERVAL() == null) {
			valid = Boolean.valueOf(true);
			return valid;
		}
	
		// can't specify both end time and repeat
		if (valid && getEND_TIME() != null && getREPEAT() != null) {
			reason = "specifies both ending time and repeat count";
			valid = Boolean.valueOf(false);
		}
		
		// must have an interval
		if (valid && getINTERVAL() == null) {
			reason = "specifies ending time or repeat but not interval";
			valid = Boolean.valueOf(false);
		}
		
		if (valid && getINTERVAL() == null && getREPEAT() == null && getEND_TIME() != null) {
			reason = "specifies only end time which Sakai does not support";
			valid = Boolean.valueOf(false);
		}
		
		if (!valid) {
			log.warn("iCal recurrence was not valid: "+reason+" ["+rrule_text+"]");
		}
		
		return valid;

	}

}
