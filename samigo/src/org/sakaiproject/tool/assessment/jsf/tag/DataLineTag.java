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
import javax.faces.context.FacesContext;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>script</code>
 * custom tag.</p>
 * <p>Based on example code by Sun Microsystems. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class DataLineTag extends UIComponentTag
{
  private TagUtil util;

  private String first;
  private String rows;
  private String value;
  private String var;
  private String separator;

  /**
   *
   * @return javax.faces.Output
   */
  public String getComponentType()
  {
    return ("javax.faces.Data");
  }

  /**
   *
   * @return "DataLine"
   */
  public String getRendererType()
  {
    return "DataLine";
  }

  /**
   * Set the properties.  analogous to a dataTable.
   * @param component
   */
  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    FacesContext context = getFacesContext();
    util.setInteger(component, "first", first);
    util.setInteger(component, "rows", rows);
    util.setString(component, "value", value);
    util.setString(component, "var", var);
    util.setString(component, "separator", separator);
  }
  /**
   *
   * @param first
   */
  public void setFirst(String first)
  {
    this.first = first;
  }

  /**
   *
   * @param rows
   */
  public void setRows(String rows)
  {
    this.rows = rows;
  }
  /**
   *
   * @param value
   */
  public void setValue(String value)
  {
    this.value = value;
  }
  /**
   *
   * @param var
   */
  public void setVar(String var)
  {
    this.var = var;
  }

  /**
   *
   * @param separator
   */
  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

}
