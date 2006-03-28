/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Locale;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

/*
import java.text.NumberFormat;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
*/
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.springframework.web.context.WebApplicationContext;

import java.util.TimeZone;
// need to move this out to support standalone,  using spring injection 
import org.sakaiproject.service.legacy.preference.PreferencesService;

import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.preference.PreferencesEdit;
import org.sakaiproject.service.legacy.preference.PreferencesService;
import org.sakaiproject.service.legacy.time.cover.TimeService;


/**
 * <p>Description: Time conversion utility class</p>
 */

public class TimeUtil 
{

  private static Log log = LogFactory.getLog(TimeUtil.class);

  private TimeZone m_client_timezone= null;
  private TimeZone m_server_timezone= null;

  public TimeUtil() {
    m_client_timezone= TimeService.getLocalTimeZone();
    m_server_timezone= TimeZone.getDefault();
    System.out.println("timeutil() : m_client_timezone =" + m_client_timezone.getID());
    System.out.println("timeutil() : m_server_timezone=" + m_server_timezone.getID());
  }


  /**
  * Convert a String reprepsentation of date and time in the client TimeZone 
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
  * Convert a Date reprepsentation of date and time in the server TimeZone 
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
  * Convert a String reprepsentation of date and time to Date on the server timezone 
  */

  public Date getServerDateTime(SimpleDateFormat ndf, String clientString){
System.out.println(" timeutil.getServerDateTime() : clientString = " + clientString);
    Date serverDate = null;
    try {
      if ((m_client_timezone !=null) && (m_server_timezone!=null) 
	&& (!m_client_timezone.hasSameRules(m_server_timezone))) {

    System.out.println("timeutil() : m_client_timezone =" + m_client_timezone.getID());
    System.out.println("timeutil() : m_server_timezone=" + m_server_timezone.getID());
        serverDate =convertFromTimeZone1StringToServerDate 
                        (ndf, clientString, m_client_timezone);
      }
      else {
System.out.println(" do not convert " );
        serverDate= ndf.parse(clientString);
      }
System.out.println(" timeutil.getServerDateTime : serverDAte = " + serverDate);
    }
    catch (Exception e){
      log.warn("can not parse the string into a Date");
    }
    return serverDate;
  }


  /**
  * Convert a Date reprepsentation of date and time to String in the client timezone for display
  */

  public String getDisplayDateTime(SimpleDateFormat ndf, Date serverDate ){
    String displayDate = "";
System.out.println(" timeutil.getDisplayDateTime() : serverDate = " + serverDate);
    try {
      if ((m_client_timezone !=null) && (m_server_timezone!=null) 
	&& (!m_client_timezone.hasSameRules(m_server_timezone))) {

    System.out.println("timeutil() : m_client_timezone =" + m_client_timezone.getID());
    System.out.println("timeutil() : m_server_timezone=" + m_server_timezone.getID());
        displayDate = convertFromServerDateToTimeZone2String
			(ndf, serverDate, m_client_timezone);
      }
      else {
System.out.println(" do not convert " );
        displayDate= ndf.format(serverDate);
      }
System.out.println(" timeutil.getDisplayDateTime() : displayDate = " + displayDate);
    }
    catch (Exception e){
      log.warn("can not format the Date to a string");
    }
    return displayDate;
  }



}
