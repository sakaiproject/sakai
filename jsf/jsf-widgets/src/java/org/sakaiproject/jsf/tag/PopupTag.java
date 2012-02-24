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
