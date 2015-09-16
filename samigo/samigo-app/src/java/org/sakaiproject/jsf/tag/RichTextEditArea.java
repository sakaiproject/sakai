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
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

public class RichTextEditArea extends UIComponentTag
{
  private String identity;
  private String value;
  private String columns;
  private String rows;
  private String justArea;
  private String hasToggle;
  private String mode;
  private String reset;

  public void setIdentity(String newIdentity)
  {
	  identity = newIdentity;
  }

  public String getIdentity()
  {
    return identity;
  }
  
  public void setValue(String newValue)
  {
    value = newValue;
  }

  public String getValue()
  {
    return value;
  }

  public void setColumns(String newC)
  {
    columns = newC;
  }

  public String getColumns()
  {
    return columns;
  }

  public void setRows(String newRows)
  {
    rows = newRows;
  }

  public String getRows()
  {
    return rows;
  }

  public void setJustArea(String newJ)
  {
    justArea = newJ;
  }

  public String getJustArea()
  {
    return justArea;
  }

  public void setHasToggle(String hasT)
  {
	  hasToggle = hasT;
  }
   
  public String getHasToggle()
  {
	  return hasToggle;
  }

  public void setMode(String mode)
  {
	  this.mode = mode;
  }
   
  public String getMode()
  {
	  return mode;
  }

  
  public void setReset(String newR)
  {
	  reset = newR;
  }
   
  public String getReset()
  {
	  return reset;
  }
  
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
