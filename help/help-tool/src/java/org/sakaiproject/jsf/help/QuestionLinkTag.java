/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/jsf/help/QuestionLinkTag.java,v 1.1 2005/05/15 23:03:52 jlannan.iupui.edu Exp $
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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

/**
 * question link tag
 * @version $Id$
 */
public class QuestionLinkTag extends UIComponentTag
{
  String URL = null;
  String message = null;
  String showLink = "false";

  /** 
   * @see javax.faces.webapp.UIComponentTag#getComponentType()
   */
  public String getComponentType()
  {
    return "javax.faces.Data";
  }

  /** 
   * @see javax.faces.webapp.UIComponentTag#getRendererType()
   */
  public String getRendererType()
  {
    return "QuestionLink";
  }

  /** 
   * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
   */
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    FacesContext context = getFacesContext();

    if (URL != null)
    {
      if (isValueReference(URL))
      {
        component.setValueBinding("URL", context.getApplication()
            .createValueBinding(URL));
      }
      else
      {
        component.getAttributes().put("URL", URL);
      }
    }
    if (message != null)
    {
      if (isValueReference(message))
      {
        component.setValueBinding("message", context.getApplication()
            .createValueBinding(message));
      }
      else
      {
        component.getAttributes().put("message", message);
      }
    }
    if (showLink != null)
    {
      if (isValueReference(showLink))
      {
        component.setValueBinding("showLink", context.getApplication()
            .createValueBinding(showLink));
      }
      else
      {
        component.getAttributes().put("showLink", showLink);
      }
    }
  }

  /**
   * get message
   * @return Returns the message.
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * set message
   * @param message The message to set.
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  /**
   * get show link
   * @return Returns the showLink.
   */
  public String getShowLink()
  {
    return showLink;
  }

  /**
   * set show link
   * @param showLink The showLink to set.
   */
  public void setShowLink(String showLink)
  {
    this.showLink = showLink;
  }

  /**
   * get URL
   * @return Returns the uRL.
   */
  public String getURL()
  {
    return URL;
  }

  /**
   * set URL
   * @param url The uRL to set.
   */
  public void setURL(String url)
  {
    URL = url;
  }
}