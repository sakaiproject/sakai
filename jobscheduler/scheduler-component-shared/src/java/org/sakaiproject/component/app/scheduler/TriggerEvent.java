/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.component.app.scheduler;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TriggerEvent
{
  private String jobName;
  private Date time;
  private String eventType;

  protected static String TRIGGER_FIRED = "Fired";
  protected static String TRIGGER_COMPLETE = "Complete";

  private static final Log LOG = LogFactory.getLog(TriggerEvent.class);

  /**
   * @return Returns the eventType.
   */
  public String getEventType()
  {
    return eventType;
  }

  /**
   * @param eventType The eventType to set.
   */
  public void setEventType(String eventType)
  {
    this.eventType = eventType;
  }

  /**
   * @return Returns the time.
   */
  public Date getTime()
  {
    return time;
  }

  /**
   * @param time The time to set.
   */
  public void setTime(Date time)
  {
    this.time = time;
  }

  /**
   * @return Returns the jobName.
   */
  public String getJobName()
  {
    return jobName;
  }

  /**
   * @param jobName The jobName to set.
   */
  public void setJobName(String jobName)
  {
    this.jobName = jobName;
  }
}