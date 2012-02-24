/**********************************************************************************
 * $URL$
 * $Id$
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