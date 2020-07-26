/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import lombok.Getter;
import lombok.Setter;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

public class RichTextEditArea extends UIComponentTag
{
  @Setter @Getter 
  private String identity;
  @Setter @Getter 
  private String value;
  @Setter @Getter 
  private String columns;
  @Setter @Getter 
  private String rows;
  @Setter @Getter 
  private String justArea;
  @Setter @Getter 
  private String hasToggle;
  @Setter @Getter 
  private String mode;
  @Setter @Getter 
  private String reset;
  @Setter @Getter 
  private String maxCharCount;

  public String getComponentType()
	{
		return "SakaiRichTextEditArea";
	}

	public String getRendererType()
	{
		return "SakaiRichTextEditArea";
	}

	protected void setProperties(UIComponent component)
	{
		super.setProperties(component);
	setString(component, "identity", identity);
    setString(component, "value", value);
    setString(component, "columns", columns);
    setString(component, "rows", rows);
    setString(component, "justArea", justArea);
    setString(component, "hasToggle", hasToggle);
    setString(component, "mode", mode);
    setString(component, "reset", reset);
    setString(component, "maxCharCount", maxCharCount);
	}

	public void release()
	{
    super.release();
    identity = null;
    value = null;
    columns = null;
    rows = null;
    justArea = null;
    hasToggle = null;
    mode = null;
    reset = null;
    maxCharCount = null;
  }

  public static void setString(UIComponent component, String attributeName,
          String attributeValue)
  {
    if(attributeValue == null)
      return;
    if(UIComponentTag.isValueReference(attributeValue))
      setValueBinding(component, attributeName, attributeValue);
    else
      component.getAttributes().put(attributeName, attributeValue);
  }

  public static void setValueBinding(UIComponent component, String attributeName,
          String attributeValue)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding vb = app.createValueBinding(attributeValue);
    component.setValueBinding(attributeName, vb);
  }
}
