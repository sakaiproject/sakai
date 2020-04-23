/**********************************************************************************
Copyright (c) 2018 Apereo Foundation

Licensed under the Educational Community License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************************/


package org.sakaiproject.jsf2.tag;

import javax.faces.webapp.UIComponentTag;
import javax.faces.component.UIComponent;

import lombok.Data;

import org.sakaiproject.jsf2.util.TagUtil;

@Data
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
}
