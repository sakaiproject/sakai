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
