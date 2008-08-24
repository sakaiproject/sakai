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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Mar 6, 2007
 * Time: 7:09:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class DateWidgetFormat {

   private Map<String, DateFormat> acceptableFormats;
   private ResourceLoader loader = new ResourceLoader();
   private Date testDate;

   public static DateFormat MM_DD_YYYY()
	{
		return new SimpleDateFormat("MM/dd/yyyy");
	}

	public static DateFormat DD_MM_YYYY()
	{
		return new SimpleDateFormat("dd-MM-yyyy");
	}

	public static DateFormat MM_DD_YYYY_short()
	{
		return new SimpleDateFormat("M/d/yy");
	}

	public static DateFormat DD_MM_YYYY_short()
	{
		return new SimpleDateFormat("d/M/yy");
	}

   public DateWidgetFormat() {
      try {
         testDate = MM_DD_YYYY().parse("12/31/1999");
      } catch (ParseException e) {

      }
      acceptableFormats = new Hashtable<String, DateFormat>();
      acceptableFormats.put(MM_DD_YYYY_short().format(testDate), MM_DD_YYYY());
      acceptableFormats.put(DD_MM_YYYY_short().format(testDate), DD_MM_YYYY());
   }

   public DateFormat getLocaleDateFormat() {
      DateFormat returned = DateFormat.getDateInstance(DateFormat.SHORT, loader.getLocale());
      String testDateString = returned.format(testDate);
      if (acceptableFormats.containsKey(testDateString)) {
         return acceptableFormats.get(testDateString);
      }
      else {
         return getDefaultDateFormat();
      }
   }

   public DateFormat getDefaultDateFormat() {
      String defaultFormat = ServerConfigurationService.getString("dateWidget.defaultFormat");
      if ("dd/MM/yyyy".equals(defaultFormat)) {
         return DD_MM_YYYY();
      }
      return MM_DD_YYYY();
   }

}
