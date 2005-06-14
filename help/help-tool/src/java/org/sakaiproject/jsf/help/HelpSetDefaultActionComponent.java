/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/jsf/help/HelpSetDefaultActionComponent.java,v 1.1 2005/05/15 23:03:52 jlannan.iupui.edu Exp $
 *
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

package org.sakaiproject.jsf.help;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * help set default action component
 * @version $Id: HelpSetDefaultActionComponent.java,v 1.1 2005/05/15 23:03:52 jlannan.iupui.edu Exp $
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

      String formId = form.getClientId(context);

      writer.startElement("script", null);
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