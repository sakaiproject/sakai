/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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




package org.sakaiproject.tool.podcasts.jsf.tag;

import javax.faces.webapp.UIComponentTag;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler custom for color picker control</p>
 * <p>Based on example code by Sun Microsystems. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class DatePickerTag extends UIComponentTag
{
  private static final String DATE_START_STRING = "MM/DD/YYYY HH:MM AM/PM";

  private TagUtil util;

  private String size;
  private String value;

  /**
   * set size
   * @param size
   */
  public void setSize(String size)
  {
    this.size = size;
  }

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
    return "DatePicker";
  }

  /**
   * Set the properties.
   * @param component
   */
  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    FacesContext context = getFacesContext();
    util.setString(component, "value", value);
    util.setString(component, "size", size);
  }

  /**
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

}
