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
    return "HideDivision";
  }

  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    /* SAK-7299
    if (id != null)
    {
      component.getAttributes().put("id", id);
    }
    */
    
    if (title != null)
    {
      if ( title.indexOf("#{") > -1)
      {
        component.setValueBinding("title",
          getFacesContext().getApplication().createValueBinding(
          getTitle()));
      }
      else
      {
        component.getAttributes().put("title", title);
      }
    }

  }

}
