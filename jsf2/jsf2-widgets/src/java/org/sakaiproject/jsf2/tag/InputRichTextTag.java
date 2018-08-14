/**********************************************************************************
Copyright (c) 2018 Apereo Foundation

Licensed under the Educational Community License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************************/

package org.sakaiproject.jsf2.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

import lombok.Data;

import org.sakaiproject.jsf2.util.TagUtil;

/**
 * <p>Formerly RichTextEditArea.java</p>
 *  * <p>Renders a rich text editor and toolbar within an HTML "textarea" element.</p>
    <p>The textarea is decorated using the HTMLArea JavaScript library.</p>
    <p>
      HTMLArea is a free, customizable online editor.  It works inside your
      browser.  It uses a non-standard feature implemented in Internet
      Explorer 5.5 or better for Windows and Mozilla 1.3 or better (any
      platform), therefore it will only work in one of these browsers.
    </p>

    <p>
      HTMLArea is copyright <a
      href="http://interactivetools.com">InteractiveTools.com</a> and
      released under a BSD-style license.  HTMLArea is created and developed
      upto version 2.03 by InteractiveTools.com.  Version 3.0 developed by
      <a href="http://students.infoiasi.ro/~mishoo/">Mihai Bazon</a> for
      InteractiveTools.  It contains code sponsored by other companies as
      well.
    </p>
 */
@Data
public class InputRichTextTag
  extends UIComponentTag
{
  private String value;
  private String rows;
  private String justArea;
  private String cols;
  private String width;
  private String height;
  private String textareaOnly;
  private String enableFullPage;
  private String buttonSet;
  private String buttonList;
  private String javascriptLibraryURL;
  private String javascriptLibraryExtensionURL;
  private String showXPath;
  private String hideAble;
  private String autoConfig; //????
  private String converter;
  private String immediate;
  private String required;
  private String validator;
  private String valueChangedListener;
  private String accesskey;
  private String dir;
  private String style;
  private String styleClass;
  private String tabindex;
  private String title;
  private String readonly;
  private String lang;
  private String attachedFiles;
  private String collectionBase;

  public String getComponentType()
  {
    return "org.sakaiproject.InputRichText";
  }

  public String getRendererType()
  {
    return "org.sakaiproject.InputRichText";
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    TagUtil.setInteger(component, "cols", cols);
    TagUtil.setInteger(component, "rows", rows);
    TagUtil.setInteger(component, "width", width);
    TagUtil.setInteger(component, "height", height);
    TagUtil.setString(component, "textareaOnly", textareaOnly);
    TagUtil.setString(component, "enableFullPage", enableFullPage);
    TagUtil.setString(component, "buttonSet", buttonSet);
    TagUtil.setString(component, "buttonList", buttonList);
    TagUtil.setString(component, "javascriptLibraryURL", javascriptLibraryURL);
    TagUtil.setString(component, "javascriptLibraryExtensionURL", javascriptLibraryExtensionURL);
    TagUtil.setString(component, "showXPath", showXPath);
    TagUtil.setString(component, "hideAble", hideAble);
    TagUtil.setString(component, "autoConfig", autoConfig); //????
    TagUtil.setString(component, "converter", converter);
    TagUtil.setString(component, "immediate", immediate);
    TagUtil.setString(component, "required", required);
    TagUtil.setString(component, "validator", validator);
    TagUtil.setString(component, "valueChangedListener", valueChangedListener);
    TagUtil.setString(component, "accesskey", accesskey);
    TagUtil.setString(component, "dir", dir);
    TagUtil.setString(component, "style", style);
    TagUtil.setString(component, "styleClass", styleClass);
    TagUtil.setString(component, "tabindex", tabindex);
    TagUtil.setString(component, "title", title);
    TagUtil.setString(component, "readonly", readonly);
    TagUtil.setString(component, "lang", lang);
    TagUtil.setString(component, "value", value);
    TagUtil.setString(component, "justArea", justArea);
    TagUtil.setString(component, "attachedFiles", attachedFiles);
    TagUtil.setString(component, "collectionBase", collectionBase);
  }

  public void release()
  {
    super.release();

    value = null;
    rows = null;
    justArea = null;
    cols = null;
    width = null;
    height = null;
    textareaOnly = null;
    enableFullPage = null;
    buttonSet = null;
    buttonList = null;
    javascriptLibraryURL = null;
    javascriptLibraryExtensionURL = null;
    showXPath = null;
    hideAble = null;
    autoConfig = null; //????
    converter = null;
    immediate = null;
    required = null;
    validator = null;
    valueChangedListener = null;
    accesskey = null;
    dir = null;
    style = null;
    styleClass = null;
    tabindex = null;
    title = null;
    readonly = null;
    lang = null;
    attachedFiles = null;
    collectionBase = null;
  }

}
