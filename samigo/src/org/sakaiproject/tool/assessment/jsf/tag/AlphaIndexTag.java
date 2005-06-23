/**********************************************************************************
* $HeadURL$
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
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>alphaIndex</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */


public class AlphaIndexTag
    extends UIComponentTag
{

  private String initials = null;

  public String getInitials()
  {
    return initials;
  }

  public void setInitials(String initials)
  {
    this.initials = initials;
  }


  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "AlphaIndex";
  }

  /**
   * set the component properties
   * @param component
   */
  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    if (initials != null)
    {
      if (initials.startsWith("#"))
      {
        component.setValueBinding("initials",
          getFacesContext().getApplication().createValueBinding(getInitials()));
      }
      else
      {
        component.getAttributes().put("initials", initials);
      }
    }
  }

}
