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
 * help frameset tag
 * @version $Id$
 */
public class HelpFrameSetTag extends UIComponentTag
{
  private String helpWindowTitle;
  private String searchToolUrl;
  private String tocToolUrl;
  private String helpUrl;

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
    return "HelpFrameSet";
  }

  /** 
   * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
   */
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    FacesContext context = getFacesContext();

    if (searchToolUrl != null)
    {
      if (isValueReference(searchToolUrl))
      {
        component.setValueBinding("searchToolUrl", context.getApplication()
            .createValueBinding(searchToolUrl));
      }
      else
      {
        component.getAttributes().put("searchToolUrl", searchToolUrl);
      }
    }
    if (tocToolUrl != null)
    {
      if (isValueReference(tocToolUrl))
      {
        component.setValueBinding("tocToolUrl", context.getApplication()
            .createValueBinding(tocToolUrl));
      }
      else
      {
        component.getAttributes().put("tocToolUrl", tocToolUrl);
      }
    }
    if (helpWindowTitle != null)
    {
      if (isValueReference(helpWindowTitle))
      {
        component.setValueBinding("helpWindowTitle", context.getApplication()
            .createValueBinding(helpWindowTitle));
      }
      else
      {
        component.getAttributes().put("helpWindowTitle", helpWindowTitle);
      }
    }
    if (helpUrl != null)
    {
      if (isValueReference(helpUrl))
      {
        component.setValueBinding("helpUrl", context.getApplication()
            .createValueBinding(helpUrl));
      }
      else
      {
        component.getAttributes().put("helpUrl", helpUrl);
      }
    }
  }
  
  /**
   * get search tool url
   * @return Returns the searchTooolUrl.
   */
  public String getSearchToolUrl()
  {
    return searchToolUrl;
  }

  /**
   * set search tool url
   * @param searchTooolUrl The searchTooolUrl to set.
   */
  public void setSearchToolUrl(String searchToolUrl)
  {
    this.searchToolUrl = searchToolUrl;
  }

  /**
   * get toc tool url
   * @return Returns the tocToolUrl.
   */
  public String getTocToolUrl()
  {
    return tocToolUrl;
  }

  /**
   * @param tocToolUrl The tocToolUrl to set.
   */
  public void setTocToolUrl(String tocToolUrl)
  {
    this.tocToolUrl = tocToolUrl;
  }

  /**
   * get help window title
   * @return Returns the helpWindowTitle. 
   */
  public String getHelpWindowTitle()
  {
    return helpWindowTitle;
  }

  /**
   * set help window title
   * @param helpWindowTitle The helpWindowTitle to set.
   */
  public void setHelpWindowTitle(String helpWindowTitle)
  {
    this.helpWindowTitle = helpWindowTitle;
  }

  /**
   * get help URL
   * @return help URL
   */
  public String getHelpUrl()
  {
    return helpUrl;
  }
  
  /**
   * set help URL
   * @param helpUrl
   */
  public void setHelpUrl(String helpUrl)
  {
    this.helpUrl = helpUrl;
  }
}