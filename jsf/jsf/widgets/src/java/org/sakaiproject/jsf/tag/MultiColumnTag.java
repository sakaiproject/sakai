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
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

//import org.sakaiproject.jsf.util.JSFDepends;
import org.sakaiproject.jsf.util.TagUtil;


public class MultiColumnTag
  extends  UIComponentTag //JSFDepends.ColumnTag
{
  private String first;
  private String var;
  private String value;
  private String rows;

  /**
   * @return "org.sakaiproject.MultiColumn"
   */
  public String getComponentType()
  {
    return ("org.sakaiproject.MultiColumn");
  }


  /**
   *
   * @return org.sakaiproject.MultiColumn
   */
  public String getRendererType()
  {
    return "org.sakaiproject.MultiColumn";
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
   * @param var
   */
  public void setVar(String var)
  {
    this.var = var;
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
   *  The rows property refers to rows in the mini-model, NOT table rows.
   *  In point of fact, they will be columns, instead.
   *  @param rows
   */

  public void setRows(String rows)
  {
    this.rows = rows;
  }

    /**
     * Set the properties.  Analogous to a dataTable.
     * However, acts as one or more columns.
     *
     * @param component
     */
    protected void setProperties(UIComponent component)
    {

      super.setProperties(component);

      FacesContext context = getFacesContext();
      TagUtil.setInteger(component, "first", first);
      TagUtil.setInteger(component, "rows", rows);
      TagUtil.setString(component, "value", value);
      TagUtil.setString(component, "var", var);
    }

//    public void release()
//    {
//    	first = null;
//    	var = null;
//    	value = null;
//    	rows = null;
//    }


}
