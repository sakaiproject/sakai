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

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

public class SyllabusIfRender extends Renderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.syllabus.SyllabusIfComponent);
  }
  
  public void encodeBegin(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String test = (String) component.getAttributes().get("test");
    if(test!=null)
      test = test.trim();
    
    if((test==null) || (test.equals("")))
    {
      writer.write("<div>");
    }
  }

  public void encodeEnd(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String test = (String) component.getAttributes().get("test");
    if(test!=null)
      test = test.trim();

    if((test==null) || (test.equals("")))
    {
      writer.write("</div>");
    }  
  }
  
  public void encodeChildren(FacesContext context, UIComponent component)
  	throws IOException 
  {
    if (context == null || component == null) 
    {
      throw new NullPointerException();
    }

    String test = (String) component.getAttributes().get("test");
    if(test!=null)
      test = test.trim();
    
    if((test==null) || (test.equals("")))
    {
      Iterator kids = component.getChildren().iterator();
      
      while (kids.hasNext()) 
      {
        UIComponent kid = (UIComponent) kids.next();
        kid.encodeBegin(context);
        if (kid.getRendersChildren()) {
          kid.encodeChildren(context);
        }
        kid.encodeEnd(context);
      }
    }
    else
    {
    }
  }
}



