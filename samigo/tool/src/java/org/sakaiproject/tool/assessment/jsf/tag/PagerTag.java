/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 *
 * <p>Description:  modified from an example in the core jsf book</p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p>Standard tag and mutator methods.</p>
 * @author Lydia Li
 * @author Ed Smiley
 * @version $Id$
 */

public class PagerTag
  extends UIComponentTag
{
  private String showpages;
  private String dataTableId;
  private String styleClass;
  private String selectedStyleClass;
  private String controlId;
  private String showLinks;

  public void setShowpages(String newValue)
  {
    showpages = newValue;
  }

  public void setDataTableId(String newValue)
  {
    dataTableId = newValue;
  }

  public void setStyleClass(String newValue)
  {
    styleClass = newValue;
  }

  public void setSelectedStyleClass(String newValue)
  {
    selectedStyleClass = newValue;
  }

  public void setControlId(String newId)
  {
    controlId = newId;
  }


  public void setShowLinks(String newValue)
  {
    showLinks = newValue;
  }

  public void setProperties(UIComponent component)
  {
    super.setProperties(component);
    if (component == null)
    {
      return;
    }
    TagUtil.setInteger(component, "showpages", showpages);
    TagUtil.setString(component, "dataTableId",
      dataTableId);
    TagUtil.setString(component, "controlId", controlId);
    TagUtil.setString(component, "styleClass",
      styleClass);
    TagUtil.setString(component, "selectedStyleClass",
      selectedStyleClass);
    TagUtil.setBoolean(component, "showLinks",
      showLinks);
  }

  public void release()
  {
    super.release();
    showpages = null;
    dataTableId = null;
    styleClass = null;
    selectedStyleClass = null;
  }

  public String getRendererType()
  {
    return "Pager";
  }

  public String getComponentType()
  {
    return "javax.faces.Output";
  }
}
