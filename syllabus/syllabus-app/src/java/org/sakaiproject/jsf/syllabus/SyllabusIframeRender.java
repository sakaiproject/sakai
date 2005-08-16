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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

public class SyllabusIframeRender extends Renderer
{
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.syllabus.SyllabusIframeComponent);
  }
  
  public void encodeBegin(FacesContext context, UIComponent component)
  throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    
    String widthIn = (String) component.getAttributes().get("width");
    String heightIn = (String) component.getAttributes().get("height");
    String redirectUrl = (String) component.getAttributes().get("redirectUrl");
    
    if (widthIn == null) 
    {
      widthIn = new Integer(450).toString();
    }
    
    if (heightIn == null) 
    {
      heightIn = new Integer(80).toString();
    }
    
    if((redirectUrl != null) && (!redirectUrl.equals("")))
    {
/*      if(!redirectUrl.startsWith("http://"))
      {
        if((!redirectUrl.toLowerCase().endsWith(".doc")) && (!redirectUrl.toLowerCase().endsWith(".pdf"))
            && (!redirectUrl.toLowerCase().startsWith("https://")))
        {
          redirectUrl = "http://" + redirectUrl;
        }
      }*/
      if(redirectUrl.toLowerCase().startsWith("www."))
      {
        redirectUrl = "http://" + redirectUrl;
      }
      writer.write("<iframe src=\"" + redirectUrl + "\"");
      writer.write(" width=\"" + widthIn + "\"");
      writer.write(" height=\"" + heightIn + "\"");
      writer.write("></iframe>");
    }
  }
}



