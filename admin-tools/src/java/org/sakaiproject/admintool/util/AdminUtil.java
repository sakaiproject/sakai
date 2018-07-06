/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.admintool.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.time.api.UserTimeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminUtil {
	

 
	/**
	 * @param date
	 * @param locale
	 * @return
	 */
	public static String  dateFormatLong(Date date, Locale locale) {
		log.debug("dateFormat: " + date.toString() + ", " + locale.toString());
		//Users date
		UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
  		DateFormat dsf = DateFormat.getDateInstance(DateFormat.LONG, locale);
  		dsf.setTimeZone(userTimeService.getLocalTimeZone());
  		String d = dsf.format(date);
  		log.debug("date: " + d);  
  		return d;
	}
	
	/**
	 * @param date
	 * @param locale
	 * @return
	 */
	public static String  dateTimeFormatLong(Date date, Locale locale) {
		log.debug("dateFormat: " + date.toString() + ", " + locale.toString());
		//Users date
		UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
  		DateFormat dsf = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
  		dsf.setTimeZone(userTimeService.getLocalTimeZone());
  		String d = dsf.format(date);
  		log.debug("date: " + d);  
  		return d;
	}



}
