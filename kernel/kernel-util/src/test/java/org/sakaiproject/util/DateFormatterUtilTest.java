/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2017 Sakai Foundation
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

package org.sakaiproject.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Testing the DateFormatterUtil class
 * 
 */
public class DateFormatterUtilTest {

    LocalDateTime dateTime = LocalDateTime.of(2016, 6, 11, 7, 45);

    @Test
    public void testIsValidISODate() {
        Assert.assertTrue(DateFormatterUtil.isValidISODate("2017-12-03T10:15:30+01:00[Europe/Paris]"));
        Assert.assertFalse(DateFormatterUtil.isValidISODate(null));
        Assert.assertFalse(DateFormatterUtil.isValidISODate("2017-12-31 23:34:00"));
        Assert.assertFalse(DateFormatterUtil.isValidISODate("2017-12-03T10:15:30+01:00[Mordor/Paris]"));
        Assert.assertFalse(DateFormatterUtil.isValidISODate("2017-13-03T10:15:30+01:00[Europe/Paris]"));
        Assert.assertFalse(DateFormatterUtil.isValidISODate("2017-12-33T10:15:30+01:00[Europe/Paris]"));
    }

    @Test
    public void testParseISODate(){
        Assert.assertTrue(DateFormatterUtil.parseISODate("2017-12-03T10:15:30+01:00") instanceof Date);
        Assert.assertTrue(DateFormatterUtil.parseISODate("2017-12-03T10:15:30+01:00[Europe/Paris]") instanceof Date);
        Assert.assertNull(DateFormatterUtil.parseISODate(null));
        Assert.assertNull(DateFormatterUtil.parseISODate("2017-12-31 23:34:00"));
        Assert.assertNull(DateFormatterUtil.parseISODate("2017-12-03T10:15:30+01:00[Mordor/Paris]"));
        Assert.assertNull(DateFormatterUtil.parseISODate("2017-13-03T10:15:30+01:00[Europe/Paris]"));
        Assert.assertNull(DateFormatterUtil.parseISODate("2017-12-33T10:15:30+01:00[Europe/Paris]"));
    }

    @Test
    public void testFormat() {
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        Assert.assertEquals(DateFormatterUtil.format(date, "MM/dd/yyyy", Locale.US), "06/11/2016");
        Assert.assertEquals(DateFormatterUtil.format(date, "dd/MM/yyyy", Locale.UK), "11/06/2016");
        Assert.assertEquals(DateFormatterUtil.format(date, "HH:mm:ss", Locale.US), "07:45:00");
        Assert.assertEquals(DateFormatterUtil.format(date, "hh:mm:ss a", Locale.US), "07:45:00 AM");
        Assert.assertEquals(DateFormatterUtil.format(date, "dd/MM/yyyy HH:mm:ss", Locale.UK), "11/06/2016 07:45:00");
        Assert.assertEquals(DateFormatterUtil.format(date, "some_invented_format", Locale.US), "6/11/16 7:45 AM");
        Assert.assertEquals(DateFormatterUtil.format(date, "some_invented_format", null), "6/11/16 7:45 AM");
        Assert.assertEquals(DateFormatterUtil.format(date, "dd/MM/yyyy HH:mm:ss", null), "6/11/16 7:45 AM");
        Assert.assertNull(DateFormatterUtil.format(null, "dd/MM/yyyy HH:mm:ss", Locale.US));
    }
}
