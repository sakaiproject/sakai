/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/ShowAreaTag.java $
 * $Id: ShowAreaTag.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

/**
 * @author Chen Wen
 * @version $Id$
 * 
 */
public class ShowAreaTag extends UIComponentTag
{
  private String value;
  private String hideBorder;
  private String showInputTextArea;
  
  public void setvalue(String value)
  {
    this.value = value;
  }
  
  public String getvalue()
  {
    return value;
  }
    
  /**
   * @return Returns the hideBorder.
   */
  public String gethideBorder()
  {
    return hideBorder;
  }

  /**
   * @param hideBorder The hideBorder to set.
   */
  public void sethideBorder(String hideBorder)
  {
    this.hideBorder = hideBorder;
  }

  
  /**
   * @return Returns the showInputTextArea.
   */
  public String getShowInputTextArea()
  {
    return showInputTextArea;
  }

  /**
   * @param showInputTextArea The showInputTextArea to set.
   */
  public void setShowInputTextArea(String showInputTextArea)
  {
    this.showInputTextArea = showInputTextArea;
  }

  public String getComponentType()
  {
    return "ShowArea";
  }
  
  public String getRendererType()
  {
    return "ShowAreaRender";
  }
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    setString(component, "value", value);
    setString(component, "hideBorder", hideBorder);
    setString(component, "showInputTextArea", showInputTextArea);
  }
  
  public void release()
  {
    super.release();
    
    value = null;
  }
  
  public static void setString(UIComponent component, String attributeName,
      String attributeValue)
  {
    if (attributeValue == null) return;
    if (UIComponentTag.isValueReference(attributeValue)) setValueBinding(
        component, attributeName, attributeValue);
    else
      component.getAttributes().put(attributeName, attributeValue);
  }

  public static void setValueBinding(UIComponent component,
      String attributeName, String attributeValue)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding vb = app.createValueBinding(attributeValue);
    component.setValueBinding(attributeName, vb);
  }
}



