/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.jsf2.tag;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.webapp.UIComponentTag;

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
  private String valueChangeListener;
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
    if (converter != null) {
      if (isValueReference(converter)) {
        TagUtil.setValueBinding(component, "converter", converter);
      } else {
        ((ValueHolder) component).setConverter(FacesContext.getCurrentInstance().getApplication().createConverter(converter));
      }
    }
    TagUtil.setBoolean(component, "immediate", immediate);
    TagUtil.setBoolean(component, "required", required);
    TagUtil.setValidator(component, validator);
    TagUtil.setValueChangeListener(component, valueChangeListener);
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
    valueChangeListener = null;
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
