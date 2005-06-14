/**********************************************************************************
*
* $Header: /cvs/sakai2/syllabus/syllabus-app/src/java/org/sakaiproject/jsf/syllabus/SyllabusShowAreaTag.java,v 1.1 2005/05/06 14:42:28 cwen.iupui.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.jsf.syllabus;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

public class SyllabusShowAreaTag extends UIComponentTag
{
  private String value;
  
  public void setvalue(String value)
  {
    this.value = value;
  }
  
  public String getvalue()
  {
    return value;
  }
  
  public String getComponentType()
  {
    return "SyllabusShowArea";
  }
  
  public String getRendererType()
  {
    return "SyllabusShowAreaRender";
  }
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    setString(component, "value", value);
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

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/syllabus/syllabus-app/src/java/org/sakaiproject/jsf/syllabus/SyllabusShowAreaTag.java,v 1.1 2005/05/06 14:42:28 cwen.iupui.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
