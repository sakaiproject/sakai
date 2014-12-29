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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/




package org.sakaiproject.tool.assessment.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;


/**
 * @todo make default alt tag come from resource
 *
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

public class ColorPickerTag extends UIComponentTag
{
  //private TagUtil util;

//  private String type = "text";//later on we may want to allow hidden
//  private String cursorStyle = "cursor:pointer;";
//  private String height = "13";
//  private String width = "15";
//  private String clickAlt = "Click Here to Pick Color";
  private String size = "8";
  private String value = "";

//  public void setType(String type)
//  {
//    this.type = type;
//  }

  public void setSize(String size)
  {
    this.size = size;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

//  public void setCursorStyle(String cursorStyle)
//  {
//    this.cursorStyle = cursorStyle;
//  }
//
//  public void setWidth(String width)
//  {
//    this.width = width;
//  }
//
//  public void setHeight(String height)
//  {
//    this.height = height;
//  }
//
//  public void setclickAlt(String clickAlt)
//  {
//    this.clickAlt = clickAlt;
//  }

  public String getComponentType()
  {
    return ("javax.faces.Input");
  }

  public String getRendererType()
  {
    return "ColorPicker";
  }

  /**
   * Set the properties.
   * @param component
   */
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);

    //FacesContext context = getFacesContext();
    TagUtil.setString(component, "value", value);
    TagUtil.setString(component, "size", size);
  }

  /**
   *
   * @return String value
   */
  public String getValue() {
    return value;
  }

}
