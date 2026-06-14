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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.property.RRule;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.calendar.impl.DailyRecurrenceRule;
import org.sakaiproject.calendar.impl.ExclusionRecurrenceRule;
import org.sakaiproject.calendar.impl.ExclusionSeqRecurrenceRule;
import org.sakaiproject.calendar.impl.MWRecurrenceRule;
import org.sakaiproject.calendar.impl.MWFRecurrenceRule;
import org.sakaiproject.calendar.impl.MonthlyRecurrenceRule;
import org.sakaiproject.calendar.impl.RecurrenceInstance;
import org.sakaiproject.calendar.impl.SMTWRecurrenceRule;
import org.sakaiproject.calendar.impl.SMWRecurrenceRule;
import org.sakaiproject.calendar.impl.STTRecurrenceRule;
import org.sakaiproject.calendar.impl.TThRecurrenceRule;
import org.sakaiproject.calendar.impl.WeeklyRecurrenceRule;
import org.sakaiproject.calendar.impl.YearlyRecurrenceRule;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.ExDate;

@Slf4j
public class ICalRecurrence  
{
	private String rrule_text = "";
	private RRule rrule = null;
	private Recur recur = null;
	private static final Map<String,String> frequencyLookup;
	private static final Map<String,String> dayListToSakaiFrequency;

	static {

		Map<String, String>m = new HashMap<String,String>();

		m.put("DAILY",new DailyRecurrenceRule().getFrequency());
		m.put("MONTHLY",new MonthlyRecurrenceRule().getFrequency());
		m.put("WEEKLY", new WeeklyRecurrenceRule().getFrequency());
		m.put("YEARLY",new YearlyRecurrenceRule().getFrequency());

		m.put(new MWFRecurrenceRule().getFrequency(),new MWFRecurrenceRule().getFrequency());
		m.put(new TThRecurrenceRule().getFrequency(),new TThRecurrenceRule().getFrequency());
		m.put(new MWRecurrenceRule().getFrequency(),new MWRecurrenceRule().getFrequency());
		m.put(new SMWRecurrenceRule().getFrequency(),new SMWRecurrenceRule().getFrequency());
		m.put(new SMTWRecurrenceRule().getFrequency(),new SMTWRecurrenceRule().getFrequency());
		m.put(new STTRecurrenceRule().getFrequency(),new STTRecurrenceRule().getFrequency());

		frequencyLookup = Collections.unmodifiableMap(m);

		Map<String, String> dayLists = new HashMap<String,String>();
		dayLists.put("MO,WE,FR", MWFRecurrenceRule.FREQ);
		dayLists.put("TU,TH", TThRecurrenceRule.FREQ);
		dayLists.put("MO,WE", MWRecurrenceRule.FREQ);
		dayLists.put("SU,MO,WE", SMWRecurrenceRule.FREQ);
		dayLists.put("SU,MO,TU,WE", SMTWRecurrenceRule.FREQ);
		dayLists.put("SU,TU,TH", STTRecurrenceRule.FREQ);
		dayListToSakaiFrequency = Collections.unmodifiableMap(dayLists);
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

		String extendedFrequency = recur.getFrequency().toString();
		WeekDayList dayList = recur.getDayList();
		String dayListAsString = dayList.toString();
		
		if (! dayList.isEmpty()) {
			String sakaiFromDayList = dayListToSakaiFrequency.get(dayListAsString);
			if (sakaiFromDayList != null) {
				extendedFrequency = sakaiFromDayList;
			}
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

	/**
	 * Converts a Sakai recurrence rule to an iCal4j RRULE property.
	 *
	 * @param rule Sakai recurrence rule
	 * @param timeZone timezone applied to UNTIL so it matches DTSTART TZID handling
	 * @return RRULE property, or null when the Sakai frequency has no iCal equivalent
	 */
	public static RRule toRRule(RecurrenceRule rule, TimeZone timeZone)
	{
		if (rule == null || rule.getFrequency() == null)
		{
			return null;
		}

		Recur.Builder builder;
		switch (rule.getFrequency())
		{
			case DailyRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.DAILY);
				break;
			case WeeklyRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.WEEKLY);
				break;
			case MonthlyRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.MONTHLY);
				break;
			case YearlyRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.YEARLY);
				break;
			case MWFRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.WEEKLY)
						.dayList(weekDayList(WeekDay.MO, WeekDay.WE, WeekDay.FR));
				break;
			case TThRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.WEEKLY)
						.dayList(weekDayList(WeekDay.TU, WeekDay.TH));
				break;
			case MWRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.WEEKLY)
						.dayList(weekDayList(WeekDay.MO, WeekDay.WE));
				break;
			case SMWRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.WEEKLY)
						.dayList(weekDayList(WeekDay.SU, WeekDay.MO, WeekDay.WE));
				break;
			case SMTWRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.WEEKLY)
						.dayList(weekDayList(WeekDay.SU, WeekDay.MO, WeekDay.TU, WeekDay.WE));
				break;
			case STTRecurrenceRule.FREQ:
				builder = new Recur.Builder().frequency(Recur.Frequency.WEEKLY)
						.dayList(weekDayList(WeekDay.SU, WeekDay.TU, WeekDay.TH));
				break;
			default:
				log.warn("No iCal RRULE mapping for Sakai recurrence frequency [{}]", rule.getFrequency());
				return null;
		}

		if (rule.getInterval() > 1)
		{
			builder.interval(rule.getInterval());
		}
		if (rule.getCount() > 0)
		{
			builder.count(rule.getCount());
		}
		else if (rule.getUntil() != null)
		{
			DateTime until = new DateTime(rule.getUntil().getTime());
			net.fortuna.ical4j.model.TimeZone icalTimeZone = icalTimeZone(timeZone);
			if (icalTimeZone != null)
			{
				until.setTimeZone(icalTimeZone);
			}
			builder.until(until);
		}

		return new RRule(builder.build());
	}

	/**
	 * Builds EXDATE properties for excluded occurrences on a recurring event.
	 */
	public static List<ExDate> toExDates(CalendarEvent event, TimeService timeService)
	{
		List<ExDate> exDates = new ArrayList<>();
		RecurrenceRule exclusionRule = event.getExclusionRule();
		if (exclusionRule == null)
		{
			return exDates;
		}

		TimeZone timeZone = resolveEventTimeZone(event, timeService);
		DateList dateList = new DateList();
		dateList.setUtc(false);

		if (exclusionRule instanceof ExclusionRecurrenceRule)
		{
			ExclusionRecurrenceRule rangeExclusions = (ExclusionRecurrenceRule) exclusionRule;
			for (Object rangeObj : rangeExclusions.getRanges())
			{
				TimeRange excludedRange = (TimeRange) rangeObj;
				dateList.add(dateTimeForExclusion(excludedRange.firstTime(), timeZone));
			}
		}
		else if (exclusionRule instanceof ExclusionSeqRecurrenceRule)
		{
			RecurrenceRule recurrenceRule = event.getRecurrenceRule();
			if (recurrenceRule == null)
			{
				return exDates;
			}

			ExclusionSeqRecurrenceRule seqExclusions = (ExclusionSeqRecurrenceRule) exclusionRule;
			List<Integer> excludedSequences = seqExclusions.getExclusions();
			if (excludedSequences.isEmpty())
			{
				return exDates;
			}

			TimeRange generationRange = recurrenceGenerationRange(recurrenceRule, event.getRange(), timeService);
			List instances = recurrenceRule.generateInstances(event.getRange(), generationRange, timeZone);
			for (Object instanceObj : instances)
			{
				RecurrenceInstance instance = (RecurrenceInstance) instanceObj;
				if (excludedSequences.contains(instance.getSequence()))
				{
					dateList.add(dateTimeForExclusion(instance.getRange().firstTime(), timeZone));
				}
			}
		}

		if (!dateList.isEmpty())
		{
			ExDate exDate = new ExDate(dateList);
			exDate.getParameters().add(new TzId(timeZone.getID()));
			exDates.add(exDate);
		}
		return exDates;
	}

	private static TimeRange recurrenceGenerationRange(RecurrenceRule recurrenceRule, TimeRange prototype, TimeService timeService)
	{
		Time until = recurrenceRule.getUntil();
		if (until != null)
		{
			return timeService.newTimeRange(prototype.firstTime(), until, true, true);
		}

		int count = recurrenceRule.getCount();
		if (count > 0)
		{
			long estimatedEnd = prototype.firstTime().getTime()
					+ (count * 366L * 24L * 60L * 60L * 1000L);
			return timeService.newTimeRange(prototype.firstTime(), timeService.newTime(estimatedEnd), true, true);
		}

		Time farFuture = timeService.newTime(prototype.firstTime().getTime() + (100L * 365L * 24L * 60L * 60L * 1000L));
		return timeService.newTimeRange(prototype.firstTime(), farFuture, true, true);
	}

	private static TimeZone resolveEventTimeZone(CalendarEvent event, TimeService timeService)
	{
		String timeZoneId = event.getField("createdInTimeZone");
		if (timeZoneId == null || timeZoneId.isEmpty())
		{
			return timeService.getLocalTimeZone();
		}
		return TimeZone.getTimeZone(timeZoneId);
	}

	private static DateTime dateTimeForExclusion(Time time, TimeZone timeZone)
	{
		DateTime dateTime = new DateTime(time.getTime());
		net.fortuna.ical4j.model.TimeZone icalTimeZone = icalTimeZone(timeZone);
		if (icalTimeZone != null)
		{
			dateTime.setTimeZone(icalTimeZone);
		}
		return dateTime;
	}

	private static net.fortuna.ical4j.model.TimeZone icalTimeZone(TimeZone timeZone)
	{
		if (timeZone == null)
		{
			return null;
		}
		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		return registry.getTimeZone(timeZone.getID());
	}

	private static WeekDayList weekDayList(WeekDay... days)
	{
		WeekDayList list = new WeekDayList();
		for (WeekDay day : days)
		{
			list.add(day);
		}
		return list;
	}

}
