/**********************************************************************************
* $URL$
* $Id$
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


package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.util.TagUtil;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>popup</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PopupTag extends UIComponentTag
{

  private String id = null;

  private String title;
  private String url;
  private String target;
  private String toolbar;
  private String menubar;
  private String personalbar;
  private String scrollbars;
  private String resizable;
  private String useButton;
  private String width;
  private String height;


  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }


  public String getRendererType()
  {
    return "org.sakaiproject.Popup";
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);

    FacesContext context = getFacesContext();

    TagUtil.setString(component, "title", title);
    TagUtil.setString(component, "url", url);
    TagUtil.setString(component, "target", target);
    TagUtil.setString(component, "toolbar", toolbar);
    TagUtil.setString(component, "menubar", menubar);
    TagUtil.setString(component, "personalbar", personalbar);
    TagUtil.setString(component, "scrollbars", scrollbars);
    TagUtil.setString(component, "resizable", resizable);
    TagUtil.setString(component, "useButton", useButton);
    TagUtil.setInteger(component, "height", height);
    TagUtil.setInteger(component, "width", width);
  }


  public String getComponentType() {
    return ("javax.faces.Output");
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getTarget() {
    return target;
  }
  public void setTarget(String target) {
    this.target = target;
  }
  public String getToolbar() {
    return toolbar;
  }
  public void setToolbar(String toolbar) {
    this.toolbar = toolbar;
  }
  public String getMenubar() {
    return menubar;
  }
  public void setMenubar(String menubar) {
    this.menubar = menubar;
  }
  public String getPersonalbar() {
    return personalbar;
  }
  public void setPersonalbar(String personalbar) {
    this.personalbar = personalbar;
  }
  public String getWidth() {
    return width;
  }
  public void setWidth(String width) {
    this.width = width;
  }
  public String getHeight() {
    return height;
  }
  public void setHeight(String height) {
    this.height = height;
  }
  public String getScrollbars() {
    return scrollbars;
  }
  public void setScrollbars(String scrollbars) {
    this.scrollbars = scrollbars;
  }
  public String getResizable() {
    return resizable;
  }
  public void setResizable(String resizable) {
    this.resizable = resizable;
  }
  public String getUseButton() {
    return useButton;
  }
  public void setUseButton(String useButton) {
    this.useButton = useButton;
  }


}
