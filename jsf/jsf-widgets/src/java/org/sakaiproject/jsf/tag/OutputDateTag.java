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

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import org.sakaiproject.jsf.util.TagUtil;


/**
 * <p>Title: Sakai JSF</p>
 * <p>Description: output date tag</p>
 * @author Ed Smiley
 * @version 2.0
 */
public class OutputDateTag extends UIComponentTag
{
  private String showTime;
  private String showDate;
  private String showSeconds;
  private String value;
  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.OutputDate";
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    TagUtil.setString(component, "showTime", showTime);
    TagUtil.setString(component, "showDate", showDate);
    TagUtil.setString(component, "showSeconds", showSeconds);

  }
  public void setShowTime(String showTime)
  {
    this.showTime = showTime;
  }
  public void setShowDate(String showDate)
  {
    this.showDate = showDate;
  }
  public void setShowSeconds(String showSeconds)
  {
    this.showSeconds = showSeconds;
  }
  public void setValue(String value) {
    this.value = value;
  }
}
