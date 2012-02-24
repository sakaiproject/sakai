/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.help;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * help set default action component
 * @version $Id$
 */
public class HelpSetDefaultActionComponent extends UIOutput
{

  /** 
   * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
   */
  public void encodeBegin(FacesContext context) throws IOException
  {
    return;
  }

  public void decode(FacesContext context)
  {
    return;
  }

  /** 
   * @see javax.faces.component.UIComponent#encodeEnd(javax.faces.context.FacesContext)
   */
  public void encodeEnd(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    UIComponent actionComponent = super.getParent();
    String acionElement = actionComponent.getClientId(context);
    UIForm form = getForm(actionComponent);
    if (form != null)
    {

      writer.startElement("script", null);
      writer.writeAttribute("type", "text/javascript", null);

      String functionCode = "if (document.layers) \n"
          + "document.captureEvents(Event.KEYDOWN); \n"
          + "document.onkeydown =" + "function (evt) \n {"
          + " var keyCode = evt ? (evt.which ? evt.which : evt.keyCode) : "
          + "event.keyCode; \n"
          + "var eventTarget = evt ? evt.target : event.srcElement;  \n"
          + "var textField = eventTarget.type == 'text';  \n"
          + "if (keyCode == 13 && textField) { \n "
          + "document.getElementById('" + acionElement
          + "').click();return false; }  \n" + "else  return true; }";

      writer.write(functionCode);

      writer.endElement("script");
    }
  }

  /**
   * get form
   * @param component
   * @return ui form
   */
  private UIForm getForm(UIComponent component)
  {
    while (component != null)
    {
      if (component instanceof UIForm)
      {
        break;
      }
      component = component.getParent();
    }
    return (UIForm) component;
  }

}
