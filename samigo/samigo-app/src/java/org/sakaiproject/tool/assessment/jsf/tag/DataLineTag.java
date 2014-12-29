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
  //private TagUtil util;

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

    //FacesContext context = getFacesContext();
    TagUtil.setInteger(component, "first", first);
    TagUtil.setInteger(component, "rows", rows);
    TagUtil.setString(component, "value", value);
    TagUtil.setString(component, "var", var);
    TagUtil.setString(component, "separator", separator);
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
