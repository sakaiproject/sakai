/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
