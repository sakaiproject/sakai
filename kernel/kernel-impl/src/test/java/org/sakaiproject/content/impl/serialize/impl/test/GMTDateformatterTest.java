/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.serialize.impl.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import org.sakaiproject.content.impl.util.GMTDateformatter;

@Slf4j
public class GMTDateformatterTest
{
	@Test
	public void testPad()
	{
		Assert.assertEquals("10", pad(10, 2));
		Assert.assertEquals("010", pad(10, 3));
		Assert.assertEquals("01", pad(1, 2));
		Assert.assertEquals("001", pad(1, 3));
		Assert.assertEquals("1", pad(1, 1));
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.content.impl.util.GMTDateformatter#parse(java.lang.String)}.
	 */
	@Test
	public final void testParse()
	{
		// String format = "yyyyMMddHHmmssSSS";
		String[] testPattern = new String[20000];
		Date[] result = new Date[testPattern.length];
		String[] formatPattern = new String[20000];
		for (int i = 0; i < testPattern.length; i++)
		{
			int year = 1980 + (i % 200);
			int month = 1 + (i % 12);
			int day = 1 + (i % 28);
			int hour = (i % 24);
			int minute = (i % 60);
			int second = 59 - (i % 60);
			int millis = (i % 1000);
			StringBuilder sb = new StringBuilder();
			sb.append(pad(year, 4));
			sb.append(pad(month, 2));
			sb.append(pad(day, 2));
			sb.append(pad(hour, 2));
			sb.append(pad(minute, 2));
			sb.append(pad(second, 2));
			sb.append(pad(millis, 3));

			testPattern[i] = sb.toString();
		}
		{
			long start = System.currentTimeMillis();
			for (int i = 0; i < testPattern.length; i++)
			{
				result[i] = GMTDateformatter.parse(testPattern[i]);
			}
			long end = System.currentTimeMillis();
			double ms = 1.0 * (end - start) / (1.0 * testPattern.length);
			log.info(" Took " + (end - start) + " ms " + ms + " per parse");
		}
		{
			long start = System.currentTimeMillis();
			for (int i = 0; i < testPattern.length; i++)
			{
				formatPattern[i] = GMTDateformatter.format(result[i]);
			}
			long end = System.currentTimeMillis();
			double ms = 1.0 * (end - start) / (1.0 * testPattern.length);
			log.info(" Took " + (end - start) + " ms " + ms + " per format");
		}

		for (int i = 0; i < testPattern.length; i++)
		{
			int year = 1980 + (i % 200);
			int month = 1 + (i % 12);
			int day = 1 + (i % 28);
			int hour = (i % 24);
			int minute = (i % 60);
			int second = 59 - (i % 60);
			int millis = (i % 1000);
			GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

			gc.setTime(result[i]);
			Assert.assertEquals(testPattern[i] + " year", year, gc.get(Calendar.YEAR));
			Assert.assertEquals(testPattern[i] + " month", month, gc.get(Calendar.MONTH) + 1);
			Assert.assertEquals(testPattern[i] + " day", day, gc.get(Calendar.DATE));
			Assert.assertEquals(testPattern[i] + " hour", hour, gc.get(Calendar.HOUR_OF_DAY));
			Assert.assertEquals(testPattern[i] + " minute", minute, gc.get(Calendar.MINUTE));
			Assert.assertEquals(testPattern[i] + " second", second, gc.get(Calendar.SECOND));
			Assert.assertEquals(testPattern[i] + " millis", millis, gc.get(Calendar.MILLISECOND));
			Assert.assertEquals("Format Check ", testPattern[i], GMTDateformatter
					.format(result[i]));
		}
	}

	/**
	 * @param year
	 * @param i
	 * @return
	 */
	private CharSequence pad(int value, int pad)
	{
		char[] c = new char[pad];
		char[] v = String.valueOf(value).toCharArray();
		for (int i = 0; i < pad; i++)
		{
			if (i < pad - v.length)
			{
				c[i] = '0';
			}
			else
			{
				c[i] = v[i - (pad - v.length)];
			}
		}
		return new String(c);
	}
}
