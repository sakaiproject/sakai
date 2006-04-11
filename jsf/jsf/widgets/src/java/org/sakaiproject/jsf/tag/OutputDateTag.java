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

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import org.sakaiproject.jsf.util.TagUtil;


/**
 * <p>Title: Sakai JSF</p>
 * <p>Description: output date tag</p>
 * <p>Copyright: Copyright (c) 2005 Sakai Project</p>
 * <p>: </p>
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
