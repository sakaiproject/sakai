/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-tool/src/java/org/sakaiproject/jsf/help/HelpFrameSetTag.java,v 1.3 2005/06/09 16:39:39 jlannan.iupui.edu Exp $
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
 * help frameset tag
 * @version $Id: HelpFrameSetTag.java,v 1.3 2005/06/09 16:39:39 jlannan.iupui.edu Exp $
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