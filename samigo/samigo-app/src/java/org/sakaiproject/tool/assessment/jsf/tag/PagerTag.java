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
