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
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.util.TagUtil;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>hideDivision</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class HideDivisionTag
  extends UIComponentTag
{
  private String title = null;
  private String id = null;
  private String first;
  private String rows;
  private String value;
  private String var;
  private String separator;
  private String onclick;
  private String ondblclick;
  private String onkeydown;
  private String onkeypress;
  private String onkeyup;
  private String onmousedown;
  private String onmousemove;
  private String onmouseout;
  private String onmouseover;
  private String onmouseup;


  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return title;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.HideDivision";
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
   *  The rows property refers to rows in the mini-model, NOT table rows.
   *  @param rows
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
  public void setOnclick(String onclick)
  {
    this.onclick = onclick;
  }
  public void setOndblclick(String ondblclick)
  {
    this.ondblclick = ondblclick;
  }
  public void setOnkeydown(String onkeydown)
  {
    this.onkeydown = onkeydown;
  }
  public void setOnkeypress(String onkeypress)
  {
    this.onkeypress = onkeypress;
  }
  public void setOnkeyup(String onkeyup)
  {
    this.onkeyup = onkeyup;
  }
  public void setOnmousedown(String onmousedown)
  {
    this.onmousedown = onmousedown;
  }
  public void setOnmousemove(String onmousemove)
  {
    this.onmousemove = onmousemove;
  }
  public void setOnmouseout(String onmouseout)
  {
    this.onmouseout = onmouseout;
  }
  public void setOnmouseover(String onmouseover)
  {
    this.onmouseover = onmouseover;
  }
  public void setOnmouseup(String onmouseup)
  {
    this.onmouseup = onmouseup;
  }

  protected void setProperties(UIComponent component)
  {
     super.setProperties(component);
     TagUtil.setString(component, "title", title);
     TagUtil.setString(component, "ondblclick", ondblclick);
     TagUtil.setString(component, "onclick", onclick);
     TagUtil.setString(component, "onkeydown", onkeydown);
     TagUtil.setString(component, "onkeypress", onkeypress);
     TagUtil.setString(component, "onkeyup", onkeyup);
     TagUtil.setString(component, "onmousedown", onmousedown);
     TagUtil.setString(component, "onmousemove", onmousemove);
     TagUtil.setString(component, "onmouseout", onmouseout);
     TagUtil.setString(component, "onmouseover", onmouseover);
     TagUtil.setString(component, "onmouseup", onmouseup);
  }

}
