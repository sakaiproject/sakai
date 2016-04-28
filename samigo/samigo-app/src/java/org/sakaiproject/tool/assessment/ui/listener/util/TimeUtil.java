/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.ui.listener.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.util.StringUtils;
/**
 * <p>Description: Time conversion utility class</p>
 */

public class TimeUtil 
{

  private static Logger log = LoggerFactory.getLogger(TimeUtil.class);
  private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
  private static DateTimeFormatter dtf = DateTimeFormat.forPattern(ISO_8601_DATE_FORMAT);

  private TimeZone m_client_timezone= null;
  private TimeZone m_server_timezone= null;

  public TimeUtil() {
    m_client_timezone= TimeService.getLocalTimeZone();
    m_server_timezone= TimeZone.getDefault();
  }


  /**
  * Convert a String representation of date and time in the client TimeZone 
  * to Date representation of date and time  in server TimeZone
  * before saving to DB.
  * tz1 is the client timezone,  tz2 is the server timezone
  */

  public Date convertFromTimeZone1StringToServerDate
        (SimpleDateFormat ndf, String tz1string, TimeZone tz1){
    Date serverDate= null;
    try {
      ndf.setTimeZone(tz1);
      serverDate= ndf.parse(tz1string);
    }
    catch(Exception e){
      e.printStackTrace();
    }

     return serverDate;

  }


  /**
  * Convert a Date representation of date and time in the server TimeZone 
  * to String representation of date and time  in client TimeZone
  * used for display. 
  * tz1 is the client timezone,  tz2 is the server timezone
  */

  public String convertFromServerDateToTimeZone2String 
    (SimpleDateFormat ndf, Date tz2Date, TimeZone tz1){
    // for display
    Calendar cal1= new GregorianCalendar(tz1);
    ndf.setCalendar(cal1);
    String clientStr= ndf.format(tz2Date);

    return clientStr;
  }

  /**
  * Convert a String representation of date and time to Date on the server timezone 
  */

  public Date getServerDateTime(SimpleDateFormat ndf, String clientString){
    Date serverDate = null;
    try {
      if ((m_client_timezone !=null) && (m_server_timezone!=null) 
	&& (!m_client_timezone.hasSameRules(m_server_timezone))) {

        serverDate =convertFromTimeZone1StringToServerDate 
                        (ndf, clientString, m_client_timezone);
      }
      else {
        serverDate= ndf.parse(clientString);
      }
    }
    catch (ParseException e) {
    	log.warn("can not parse the string, " + clientString + ", into a Date using format: " + ndf.toPattern());
    	if (log.isDebugEnabled()) {
    		e.printStackTrace();
    	}
	}
    return serverDate;
  }


  /**
  * Convert a Date representation of date and time to String in the client timezone for display
  */

  public String getDisplayDateTime(SimpleDateFormat ndf, Date serverDate ){
    String displayDate = "";
     //we can't format a null date
    if (serverDate == null) {
    	return displayDate;
    }
    
    try {
      if ((m_client_timezone !=null) && (m_server_timezone!=null) 
	&& (!m_client_timezone.hasSameRules(m_server_timezone))) {

        displayDate = convertFromServerDateToTimeZone2String
			(ndf, serverDate, m_client_timezone);
      }
      else {
        displayDate= ndf.format(serverDate);
      }
    }
    catch (RuntimeException e){
      log.warn("can not format the Date to a string", e);
    }
    return displayDate;
  }

  /*
   * SAM-2323: this is useful for simple sorting by jQuery tablesorter plugin
   * In USA, I expect a date like 2015-02-15 04:00pm
   * In Sweden, I expect a date like 2015-02-15 16:00
   */
  public String getIsoDateWithLocalTime(Date dateToConvert) {
      DateTime dt = new DateTime(dateToConvert);
      DateTimeFormatter fmt = ISODateTimeFormat.yearMonthDay();
      DateTimeFormatter localFmt = fmt.withLocale(new ResourceLoader().getLocale());
      DateTimeFormatter fmtTime = DateTimeFormat.shortTime();
      DateTimeFormatter localFmtTime = fmtTime.withLocale(new ResourceLoader().getLocale());
      return dt.toString(localFmt) + " " + dt.toString(localFmtTime);
  }
  
  /*
   * SAM-2323: the jquery-ui datepicker provides a hidden field with ISO-8601 date/time
   * This method will convert that date string into a Java Date
   */
  public Date parseISO8601String(String dateString) {
	  if (StringUtils.isEmpty(dateString)) {
		  return null;
	  }

	  try {
		DateTime dt = dtf.parseDateTime(dateString);
		return dt.toDate();
	  } catch (Exception e) {
		log.error("parseISO8601String could not parse: " + dateString);
	  }
	  
	  return null;
  }


}
