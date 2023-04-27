/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.impl;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PlusTestConfiguration.class})
public class PlusServiceImplTests extends AbstractTransactionalJUnit4SpringContextTests {

	@Before
	public void setup() {
	}

	@Test
	public void testSplit() {
		String normal =  "gradebook.updateItemScore@/gradebookng/7/8/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0/70/OK/instructor";
		String twodelim = "gradebook.updateItemScore@/gradebookng/7/8/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0//OK/instructor";
		String[] parts = normal.split("/");
		assertEquals(parts.length, 8);
		parts =  twodelim.split("/");
		assertEquals(parts.length, 8);

		// StringUtils.split() treats two successive delimiters as one - Sheesh
		// Don't use it :)
		parts = StringUtils.split(normal, '/');
		assertEquals(parts.length, 8);
		parts = StringUtils.split(twodelim, '/');
		assertEquals(parts.length, 7);
	}

	// We do this to track when the deprecation actually happens (not likely)
	@Test
	public void testCalendar() {
		Date date = new Date("Wed Jan 18 00:00:00 UTC 2023");
		int hours = date.getHours();
		int seconds = date.getSeconds();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hours2 = cal.get(Calendar.HOUR_OF_DAY);
		int seconds2 = cal.get(Calendar.SECOND);
		assertEquals(hours, hours2);
		assertEquals(seconds, seconds2);

		// https://stackoverflow.com/questions/5050170/how-do-i-get-a-date-without-time-in-java
		cal = Calendar.getInstance();
		date = new Date();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date dateWithoutTime = cal.getTime();
		String dstr = dateWithoutTime.toString();
		assertTrue(dstr.contains("00:00:00"));

		cal = Calendar.getInstance();
		date = new Date();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		dateWithoutTime = cal.getTime();
		dstr = dateWithoutTime.toString();
		assertTrue(dstr.contains("23:59:00"));
	}

	@Test
	public void testTweakDueDate() {
		Date tweakNull = PlusServiceImpl.tweakDueDate(null);
		assertNull(tweakNull);

		Date dueDate = new Date();
		Date tweak = PlusServiceImpl.tweakDueDate(dueDate);
		assertTrue(tweak.toString().contains("23:59:00"));

		// Doing this without timezone - in herit the current timezone
		dueDate = new Date("Wed Jan 18 00:00:01 2023");
		Date good = new Date("Wed Jan 18 23:59:00 2023");
		// One second after midnight
		tweak = PlusServiceImpl.tweakDueDate(dueDate);
		assertTrue(tweak.toString().contains("23:59:00"));
		assertEquals(tweak, good);

		// One hour after midnight
		dueDate = new Date("Wed Jan 18 00:01:00 2023");
		tweak = PlusServiceImpl.tweakDueDate(dueDate);
		assertTrue(tweak.toString().contains("23:59:00"));
		assertEquals(tweak, good);

		// Noon :)
		dueDate = new Date("Wed Jan 18 12:00:00 2023");
		tweak = PlusServiceImpl.tweakDueDate(dueDate);
		assertTrue(tweak.toString().contains("23:59:00"));
		assertEquals(tweak, good);

	}

	@Test
	public void testNumberUtils() {
		int i = NumberUtils.toInt("42", -1);
		assertEquals(i, 42);
		assertEquals(NumberUtils.toInt("", -1), -1);
		assertEquals(NumberUtils.toInt(null, -1), -1);
		assertEquals(NumberUtils.toInt("fred", -1), -1);
	}
}
