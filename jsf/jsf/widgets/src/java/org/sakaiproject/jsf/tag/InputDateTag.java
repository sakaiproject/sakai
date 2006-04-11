/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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


package org.sakaiproject.jsf.tag;

import javax.faces.webapp.UIComponentTag;
import javax.faces.component.UIComponent;
import org.sakaiproject.jsf.util.TagUtil;

public class InputDateTag extends UIComponentTag
{
  private String showDate;
  private String showTime;
  private String showYear;
  private String showSecond;
  private String calendarTitle;
  private String value;
  private String accesskey;
  private String disabled;

  /**
   * set size
   * @param size
   */

  /**
   * set the value
   * @param value
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * get component type
   * @return component type
   */
  public String getComponentType()
  {
    return ("javax.faces.Input");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.InputDate";
  }

  /**
   * Set the properties.
   * @param component
   */
  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    TagUtil.setString(component, "showDate", showDate);
    TagUtil.setString(component, "showTime", showTime);
    TagUtil.setString(component, "showYear", showYear);
    TagUtil.setString(component, "showSecond", showSecond);
    TagUtil.setString(component, "calendarTitle", calendarTitle);
    TagUtil.setString(component, "value", value);
    TagUtil.setString(component, "accesskey", accesskey);
    TagUtil.setString(component, "disabled", disabled);
    TagUtil.setString(component, "value", value);
  }

  /**
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  public String getShowDate()
  {
    return showDate;
  }
  public void setShowDate(String showDate)
  {
    this.showDate = showDate;
  }
  public String getShowTime()
  {
    return showTime;
  }
  public void setShowTime(String showTime)
  {
    this.showTime = showTime;
  }
  public String getShowYear()
  {
    return showYear;
  }
  public void setShowYear(String showYear)
  {
    this.showYear = showYear;
  }
  public String getShowSecond()
  {
    return showSecond;
  }
  public void setShowSecond(String showSecond)
  {
    this.showSecond = showSecond;
  }
  public String getCalendarTitle()
  {
    return calendarTitle;
  }
  public void setCalendarTitle(String calendarTitle)
  {
    this.calendarTitle = calendarTitle;
  }
  public String getAccesskey()
  {
    return accesskey;
  }
  public void setAccesskey(String accesskey)
  {
    this.accesskey = accesskey;
  }

}
