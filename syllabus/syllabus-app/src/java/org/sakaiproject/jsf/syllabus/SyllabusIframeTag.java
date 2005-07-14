/**********************************************************************************
* $URL$
* $Id$
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

public class SyllabusIframeTag extends UIComponentTag
{
  private String redirectUrl;
  
  private String width;
  
  private String height;
  
  public void setRedirectUrl(String redirectUrl)
  {
    this.redirectUrl = redirectUrl;
  }
  
  public String getedirectUrl()
  {
    return redirectUrl;
  }
  
  public void setWidth(String width)
  {
    this.width = width;
  }
  
  public String getWidth()
  {
    return width;
  }
  
  public void setHeight(String height)
  {
    this.height = height;
  }
  
  public String getHeight()
  {
    return height;
  }
  
  public String getComponentType()
  {
    return "SakaiSyllabusIframe";
  }
  
  public String getRendererType()
  {
    return "SakaiSyllabusIframeRender";
  }
  
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    setString(component, "redirectUrl", redirectUrl);
    setString(component, "width", width);
    setString(component, "height", height);
  }
  
  public void release()
  {
    super.release();
    
    redirectUrl = null;
    width = null;
    height = null;
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



