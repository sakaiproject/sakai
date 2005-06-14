/*
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
*/

package org.sakaiproject.tool.assessment.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
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
  private TagUtil util;

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

    FacesContext context = getFacesContext();
    //System.out.println("value="+value);
    util.setString(component, "value", value);
    //System.out.println("value="+value);
    util.setString(component, "size", size);
  }

  /**
   *
   * @return String value
   */
  public String getValue() {
    return value;
  }

}
