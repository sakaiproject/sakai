/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.business.entity.assessment.model;

import java.io.Serializable;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * This contains assessment access data per group.  A collection of these in
 * AssessmentImpl makes up the list of groups that have access, and lists what
 * access they each have.
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 */
public class AccessGroup
  implements Serializable
{
  private long id;
  private String name;
  private String releaseType;
  private String releaseWhen;
  private String releaseScore;
  private String retractType;
  private Calendar releaseDate;
  private String releaseDay;
  private String releaseMonth;
  private String releaseYear;
  private String releaseHour;
  private String releaseMinute;
  private String releaseAmPm;
  private Calendar retractDate;
  private String retractDay;
  private String retractMonth;
  private String retractYear;
  private String retractHour;
  private String retractMinute;
  private String retractAmPm;
  private String dueDateModel;
  private Calendar dueDate;
  private String dueDay;
  private String dueMonth;
  private String dueYear;
  private String dueHour;
  private String dueMinute;
  private String dueAmPm;
  private boolean retryAllowed;
  private boolean timedAssessment;
  private String minutes;
  private String ipAccess;
  private IPMaskData ipMaskData;
  private boolean passwordAccess;
  private String password;
  private FeedbackModel feedbackModel; // This is in the assessment for now
  private boolean isActive;
  static Logger logger = Logger.getLogger(AccessGroup.class.getName());

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public long getId()
  {
    return id;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pid DOCUMENTATION PENDING
   */
  public void setId(long pid)
  {
    id = pid;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getName()
  {
    return name;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pname DOCUMENTATION PENDING
   */
  public void setName(String pname)
  {
    name = pname;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseType()
  {
    return releaseType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pType DOCUMENTATION PENDING
   */
  public void setReleaseType(String pType)
  {
    releaseType = pType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseMonth()
  {
    return this.releaseMonth;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param releaseMonth DOCUMENTATION PENDING
   */
  public void setReleaseMonth(String releaseMonth)
  {
    try
    {
      this.releaseMonth = releaseMonth;
      if(releaseMonth.equals("--"))
      {
        releaseDate = null;
      }
      else
      {
        trySetReleaseDate();
      }

      if(releaseDate != null)
      {
        int month = Integer.parseInt(releaseMonth);
        releaseDate.set(Calendar.MONTH, month - 1);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseDay()
  {
    return this.releaseDay;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param releaseDay DOCUMENTATION PENDING
   */
  public void setReleaseDay(String releaseDay)
  {
    try
    {
      this.releaseDay = releaseDay;
      if(releaseDay.equals("--"))
      {
        releaseDate = null;
      }
      else
      {
        trySetReleaseDate();
      }

      if(releaseDate != null)
      {
        int day = Integer.parseInt(releaseDay);
        releaseDate.set(Calendar.DAY_OF_MONTH, day);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseYear()
  {
    return this.releaseYear;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param releaseYear DOCUMENTATION PENDING
   */
  public void setReleaseYear(String releaseYear)
  {
    try
    {
      this.releaseYear = releaseYear;
      if(releaseYear.equals("--"))
      {
        releaseDate = null;
      }
      else
      {
        trySetReleaseDate();
      }

      if(releaseDate != null)
      {
        int year = Integer.parseInt(releaseYear);
        releaseDate.set(Calendar.YEAR, year);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseHour()
  {
    if(releaseHour.equals("00"))
    {
      return "12";
    }
    else
    {
      return this.releaseHour;
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param preleaseHour DOCUMENTATION PENDING
   */
  public void setReleaseHour(String preleaseHour)
  {
    try
    {
      if(preleaseHour.equals("12"))
      {
        preleaseHour = "00";
      }

      if(preleaseHour.length() == 1)
      {
        preleaseHour = "0" + preleaseHour;
      }

      releaseHour = preleaseHour;
      trySetReleaseDate();
      if(releaseDate != null)
      {
        int hour = Integer.parseInt(releaseHour);
        releaseDate.set(Calendar.HOUR, hour);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseMinute()
  {
    return this.releaseMinute;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param preleaseMinute DOCUMENTATION PENDING
   */
  public void setReleaseMinute(String preleaseMinute)
  {
    try
    {
      if(preleaseMinute.length() == 1)
      {
        preleaseMinute = "0" + preleaseMinute;
      }

      releaseMinute = preleaseMinute;
      trySetReleaseDate();
      if(releaseDate != null)
      {
        int minute = Integer.parseInt(releaseMinute);
        releaseDate.set(Calendar.MINUTE, minute);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseAmPm()
  {
    return this.releaseAmPm;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param releaseAmPm DOCUMENTATION PENDING
   */
  public void setReleaseAmPm(String releaseAmPm)
  {
    try
    {
      this.releaseAmPm = releaseAmPm;
      trySetReleaseDate();
      if(releaseDate != null)
      {
        releaseDate.set(
          Calendar.AM_PM,
          ((releaseAmPm).compareTo("AM") == 0) ? Calendar.AM : Calendar.PM);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getReleaseDate()
  {
    if(releaseDate == null)
    {
      //setReleaseDate(null); // Will initialize to a year from today.
      return null;
    }

    return releaseDate.getTime();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param preleaseDate DOCUMENTATION PENDING
   */
  public void setReleaseDate(Date preleaseDate)
  {
    if(preleaseDate == null)
    {
      releaseDate = null;
      setReleaseDay("--");
      setReleaseMonth("--");
      setReleaseYear("--");
      setReleaseHour("08");
      setReleaseMinute("00");
      setReleaseAmPm("AM");

      return;
    }

    if(releaseDate == null)
    {
      releaseDate = Calendar.getInstance();
    }

    if(preleaseDate == null)
    {
      releaseDate.setTime(new Date());
      releaseDate.add(Calendar.YEAR, 1); // Release a year from today.
    }
    else
    {
      releaseDate.setTime(preleaseDate);
    }

    releaseDate.set(Calendar.SECOND, 0);
    int tempYear = releaseDate.get(Calendar.YEAR);
    setReleaseDay(
      new Integer(releaseDate.get(Calendar.DAY_OF_MONTH)).toString());
    setReleaseMonth(
      new Integer(releaseDate.get(Calendar.MONTH) + 1).toString());
    setReleaseYear(new Integer(releaseDate.get(Calendar.YEAR)).toString());
    setReleaseHour(new Integer(releaseDate.get(Calendar.HOUR)).toString());
    setReleaseMinute(new Integer(releaseDate.get(Calendar.MINUTE)).toString());
    int tempAmPm = releaseDate.get(Calendar.AM_PM);
    setReleaseAmPm((tempAmPm == Calendar.AM) ? "AM" : "PM");
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseWhen()
  {
    return releaseWhen;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param preleaseWhen DOCUMENTATION PENDING
   */
  public void setReleaseWhen(String preleaseWhen)
  {
    releaseWhen = preleaseWhen;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getReleaseScore()
  {
    return releaseScore;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param preleaseScore DOCUMENTATION PENDING
   */
  public void setReleaseScore(String preleaseScore)
  {
    releaseScore = preleaseScore;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRetractType()
  {
    return retractType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pType DOCUMENTATION PENDING
   */
  public void setRetractType(String pType)
  {
    retractType = pType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRetractMonth()
  {
    return this.retractMonth;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param retractMonth DOCUMENTATION PENDING
   */
  public void setRetractMonth(String retractMonth)
  {
    try
    {
      this.retractMonth = retractMonth;
      if(retractMonth.equals("--"))
      {
        retractDate = null;
      }
      else
      {
        trySetRetractDate();
      }

      if(retractDate != null)
      {
        int month = Integer.parseInt(retractMonth);
        retractDate.set(Calendar.MONTH, month - 1);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRetractDay()
  {
    return this.retractDay;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param retractDay DOCUMENTATION PENDING
   */
  public void setRetractDay(String retractDay)
  {
    try
    {
      this.retractDay = retractDay;
      if(retractDay.equals("--"))
      {
        retractDate = null;
      }
      else
      {
        trySetRetractDate();
      }

      if(retractDate != null)
      {
        int day = Integer.parseInt(retractDay);
        retractDate.set(Calendar.DAY_OF_MONTH, day);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRetractYear()
  {
    return this.retractYear;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param retractYear DOCUMENTATION PENDING
   */
  public void setRetractYear(String retractYear)
  {
    try
    {
      this.retractYear = retractYear;
      if(retractYear.equals("--"))
      {
        retractDate = null;
      }
      else
      {
        trySetRetractDate();
      }

      if(retractDate != null)
      {
        int year = Integer.parseInt(retractYear);
        retractDate.set(Calendar.YEAR, year);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRetractHour()
  {
    if(retractHour.compareTo("00") == 0)
    {
      return "12";
    }
    else
    {
      return this.retractHour;
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param retractHour DOCUMENTATION PENDING
   */
  public void setRetractHour(String retractHour)
  {
    try
    {
      if(retractHour.compareTo("12") == 0)
      {
        retractHour = "00";
      }

      if(retractHour.length() == 1)
      {
        retractHour = "0" + retractHour;
      }

      this.retractHour = retractHour;
      if(retractDate != null)
      {
        int hour = Integer.parseInt(retractHour);
        retractDate.set(Calendar.HOUR, hour);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRetractMinute()
  {
    return this.retractMinute;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param retractMinute DOCUMENTATION PENDING
   */
  public void setRetractMinute(String retractMinute)
  {
    try
    {
      if(retractMinute.length() == 1)
      {
        retractMinute = "0" + retractMinute;
      }

      this.retractMinute = retractMinute;
      if(retractDate != null)
      {
        int minute = Integer.parseInt(retractMinute);
        retractDate.set(Calendar.MINUTE, minute);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRetractAmPm()
  {
    return this.retractAmPm;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param retractAmPm DOCUMENTATION PENDING
   */
  public void setRetractAmPm(String retractAmPm)
  {
    try
    {
      this.retractAmPm = retractAmPm;
      if(retractDate != null)
      {
        retractDate.set(
          Calendar.AM_PM,
          ((retractAmPm).compareTo("AM") == 0) ? Calendar.AM : Calendar.PM);
      }
    }
    catch(Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getRetractDate()
  {
    if(retractDate == null)
    {
      return null;
    }

    return retractDate.getTime();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pretractDate DOCUMENTATION PENDING
   */
  public void setRetractDate(Date pretractDate)
  {
    if(pretractDate == null)
    {
      retractDate = null;
      setRetractDay("--");
      setRetractMonth("--");
      setRetractYear("--");
      setRetractHour("08");
      setRetractMinute("00");
      setRetractAmPm("AM");

      return;
    }

    if(retractDate == null)
    {
      retractDate = Calendar.getInstance();
    }

    if(pretractDate == null)
    {
      retractDate.setTime(new Date());
      retractDate.add(Calendar.YEAR, 1); // Retract a year from today.
    }
    else
    {
      retractDate.setTime(pretractDate);
    }

    retractDate.set(Calendar.SECOND, 0);

    int tempYear = retractDate.get(Calendar.YEAR);
    setRetractDay(
      new Integer(retractDate.get(Calendar.DAY_OF_MONTH)).toString());
    setRetractMonth(
      new Integer(retractDate.get(Calendar.MONTH) + 1).toString());
    setRetractYear(new Integer(retractDate.get(Calendar.YEAR)).toString());
    setRetractHour(new Integer(retractDate.get(Calendar.HOUR)).toString());
    setRetractMinute(new Integer(retractDate.get(Calendar.MINUTE)).toString());
    int tempAmPm = retractDate.get(Calendar.AM_PM);
    setRetractAmPm((tempAmPm == Calendar.AM) ? "AM" : "PM");
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDueDateModel()
  {
    return dueDateModel;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newDueDateModel DOCUMENTATION PENDING
   */
  public void setDueDateModel(String newDueDateModel)
  {
    dueDateModel = newDueDateModel;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDueMonth()
  {
    return this.dueMonth;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param dueMonth DOCUMENTATION PENDING
   */
  public void setDueMonth(String dueMonth)
  {
    try
    {
      this.dueMonth = dueMonth;
      if(dueMonth.equals("--"))
      {
        dueDate = null;
      }
      else
      {
        trySetDueDate();
      }

      if(dueDate != null)
      {
        int month = Integer.parseInt(dueMonth);
        dueDate.set(Calendar.MONTH, month - 1);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDueDay()
  {
    return this.dueDay;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param dueDay DOCUMENTATION PENDING
   */
  public void setDueDay(String dueDay)
  {
    try
    {
      this.dueDay = dueDay;
      if(dueDay.equals("--"))
      {
        dueDate = null;
      }
      else
      {
        trySetDueDate();
      }

      if(dueDate != null)
      {
        int day = Integer.parseInt(dueDay);
        dueDate.set(Calendar.DAY_OF_MONTH, day);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDueYear()
  {
    return this.dueYear;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param dueYear DOCUMENTATION PENDING
   */
  public void setDueYear(String dueYear)
  {
    try
    {
      this.dueYear = dueYear;
      if(dueYear.equals("--"))
      {
        dueDate = null;
      }
      else
      {
        trySetDueDate();
      }

      if(dueDate != null)
      {
        int year = Integer.parseInt(dueYear);
        dueDate.set(Calendar.YEAR, year);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDueHour()
  {
    if(this.dueHour.compareTo("00") == 0)
    {
      return "12";
    }
    else
    {
      return this.dueHour;
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param dueHour DOCUMENTATION PENDING
   */
  public void setDueHour(String dueHour)
  {
    try
    {
      if(dueHour.compareTo("12") == 0)
      {
        dueHour = "00";
      }

      if(dueHour.length() == 1)
      {
        dueHour = "0" + dueHour;
      }

      this.dueHour = dueHour;
      if(dueDate != null)
      {
        int hour = Integer.parseInt(dueHour);
        dueDate.set(Calendar.HOUR, hour);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDueMinute()
  {
    return this.dueMinute;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param dueMinute DOCUMENTATION PENDING
   */
  public void setDueMinute(String dueMinute)
  {
    try
    {
      if(dueMinute.length() == 1)
      {
        dueMinute = "0" + dueMinute;
      }

      this.dueMinute = dueMinute;
      if(dueDate != null)
      {
        int minute = Integer.parseInt(dueMinute);
        dueDate.set(Calendar.MINUTE, minute);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDueAmPm()
  {
    return this.dueAmPm;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param dueAmPm DOCUMENTATION PENDING
   */
  public void setDueAmPm(String dueAmPm)
  {
    try
    {
      this.dueAmPm = dueAmPm;
      if(dueDate != null)
      {
        dueDate.set(
          Calendar.AM_PM,
          (dueAmPm.compareTo("AM") == 0) ? Calendar.AM : Calendar.PM);
      }
    }
    catch(NumberFormatException e)
    {
      logger.error(e.getMessage());
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getDueDate()
  {
    if(dueDate == null)
    {
      //setDueDate(null); // Will initialize to a year from today.
      return null;
    }

    return dueDate.getTime();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pdueDate DOCUMENTATION PENDING
   */
  public void setDueDate(Date pdueDate)
  {
    if(pdueDate == null)
    {
      dueDate = null;
      setDueDay("--");
      setDueMonth("--");
      setDueYear("--");
      setDueHour("08");
      setDueMinute("00");
      setDueAmPm("AM");

      return;
    }

    if(dueDate == null)
    {
      dueDate = Calendar.getInstance();
    }

    if(pdueDate == null)
    {
      dueDate.setTime(new Date());
      dueDate.add(Calendar.YEAR, 1); // Due a year from today.
    }
    else
    {
      dueDate.setTime(pdueDate);
    }

    dueDate.set(Calendar.SECOND, 0);
    int tempYear = dueDate.get(Calendar.YEAR);
    setDueDay(new Integer(dueDate.get(Calendar.DAY_OF_MONTH)).toString());
    setDueMonth(new Integer(dueDate.get(Calendar.MONTH) + 1).toString());
    setDueYear(new Integer(dueDate.get(Calendar.YEAR)).toString());
    setDueHour(new Integer(dueDate.get(Calendar.HOUR)).toString());
    setDueMinute(new Integer(dueDate.get(Calendar.MINUTE)).toString());
    int tempAmPm = dueDate.get(Calendar.AM_PM);
    setDueAmPm((tempAmPm == Calendar.AM) ? "AM" : "PM");
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean getRetryAllowed()
  {
    return retryAllowed;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pretryAllowed DOCUMENTATION PENDING
   */
  public void setRetryAllowed(boolean pretryAllowed)
  {
    retryAllowed = pretryAllowed;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean getTimedAssessment()
  {
    return timedAssessment;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ptimedAssessment DOCUMENTATION PENDING
   */
  public void setTimedAssessment(boolean ptimedAssessment)
  {
    timedAssessment = ptimedAssessment;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getMinutes()
  {
    return minutes;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pminutes DOCUMENTATION PENDING
   */
  public void setMinutes(String pminutes)
  {
    minutes = pminutes;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getIpAccess()
  {
    return ipAccess;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pipAccess DOCUMENTATION PENDING
   */
  public void setIpAccess(String pipAccess)
  {
    ipAccess = pipAccess;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public IPMaskData getIPMaskData()
  {
    return ipMaskData;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pipMaskData DOCUMENTATION PENDING
   */
  public void setIPMaskData(IPMaskData pipMaskData)
  {
    ipMaskData = pipMaskData;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean getPasswordAccess()
  {
    return passwordAccess;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pAccess DOCUMENTATION PENDING
   */
  public void setPasswordAccess(boolean pAccess)
  {
    passwordAccess = pAccess;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ppassword DOCUMENTATION PENDING
   */
  public void setPassword(String ppassword)
  {
    password = ppassword;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public FeedbackModel getFeedbackModel()
  {
    return feedbackModel;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pfeedbackModel DOCUMENTATION PENDING
   */
  public void setFeedbackModel(FeedbackModel pfeedbackModel)
  {
    feedbackModel = pfeedbackModel;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean getIsActive()
  {
    return isActive;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pisActive DOCUMENTATION PENDING
   */
  public void setIsActive(boolean pisActive)
  {
    isActive = pisActive;
  }

  /**
   * DOCUMENTATION PENDING
   */
  public void trySetReleaseDate()
  {
    if(
      (releaseDate == null) && ! releaseDay.equals("--") &&
        ! releaseMonth.equals("--") && ! releaseYear.equals("--"))
    {
      releaseDate = Calendar.getInstance();
      setReleaseDay(releaseDay);
      setReleaseMonth(releaseMonth);
      setReleaseYear(releaseYear);
      setReleaseHour(releaseHour);
      setReleaseMinute(releaseMinute);
      setReleaseAmPm(releaseAmPm);
    }
  }

  /**
   * DOCUMENTATION PENDING
   */
  public void trySetRetractDate()
  {
    if(
      (retractDate == null) && ! retractDay.equals("--") &&
        ! retractMonth.equals("--") && ! retractYear.equals("--"))
    {
      retractDate = Calendar.getInstance();
      setRetractDay(retractDay);
      setRetractMonth(retractMonth);
      setRetractYear(retractYear);
      setRetractHour(retractHour);
      setRetractMinute(retractMinute);
      setRetractAmPm(retractAmPm);
    }
  }

  /**
   * DOCUMENTATION PENDING
   */
  public void trySetDueDate()
  {
    if(
      (dueDate == null) && ! dueDay.equals("--") && ! dueMonth.equals("--") &&
        ! dueYear.equals("--"))
    {
      dueDate = Calendar.getInstance();
      setDueDay(dueDay);
      setDueMonth(dueMonth);
      setDueYear(dueYear);
      setDueHour(dueHour);
      setDueMinute(dueMinute);
      setDueAmPm(dueAmPm);
    }
  }
}
