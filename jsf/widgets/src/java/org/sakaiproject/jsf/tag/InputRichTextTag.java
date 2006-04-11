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
import javax.faces.webapp.UIComponentTag;
import org.sakaiproject.jsf.util.TagUtil;

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
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * @author cwen@iu.edu
 * @author Ed Smiley esmiley@stanford.edu (modifications)
 * @version $Id$
 */

public class InputRichTextTag
  extends UIComponentTag
{
  private String value;
  private String columns;
  private String rows;
  private String justArea;
  private String cols;
  private String width;
  private String height;
  private String textareaOnly;
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

  public void setValue(String newValue)
  {
    value = newValue;
  }

  public String getValue()
  {
    return value;
  }

  public void setColumns(String newC)
  {
    columns = newC;
  }

  public String getColumns()
  {
    return columns;
  }

  public void setRows(String newRows)
  {
    rows = newRows;
  }

  public String getRows()
  {
    return rows;
  }

  public void setJustArea(String newJ)
  {
    justArea = newJ;
  }

  public String getJustArea()
  {
    return justArea;
  }

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
    TagUtil.setInteger(component, "columns", columns);
    TagUtil.setInteger(component, "rows", rows);
    TagUtil.setInteger(component, "width", width);
    TagUtil.setInteger(component, "height", height);
    TagUtil.setString(component, "textareaOnly", textareaOnly);
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
  }

  public void release()
  {
    super.release();

    value = null;
    columns = null;
    rows = null;
    justArea = null;
    cols = null;
    width = null;
    height = null;
    textareaOnly = null;
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
  }


  public String getCols()
  {
    return cols;
  }

  public void setCols(String cols)
  {
    this.cols = cols;
  }

  public String getWidth()
  {
    return width;
  }

  public void setWidth(String width)
  {
    this.width = width;
  }

  public String getHeight()
  {
    return height;
  }

  public void setHeight(String height)
  {
    this.height = height;
  }

  public String getTextareaOnly()
  {
    return textareaOnly;
  }

  public void setTextareaOnly(String textareaOnly)
  {
    this.textareaOnly = textareaOnly;
  }

  public String getButtonSet()
  {
    return buttonSet;
  }

  public void setButtonSet(String buttonSet)
  {
    this.buttonSet = buttonSet;
  }

  public String getButtonList()
  {
    return buttonList;
  }

  public void setButtonList(String buttonList)
  {
    this.buttonList = buttonList;
  }

  public String getJavascriptLibraryURL()
  {
    return javascriptLibraryURL;
  }

  public void setJavascriptLibraryURL(String javascriptLibraryURL)
  {
    this.javascriptLibraryURL = javascriptLibraryURL;
  }

  public String getJavascriptLibraryExtensionURL()
  {
    return javascriptLibraryExtensionURL;
  }

  public void setJavascriptLibraryExtensionURL(String
    javascriptLibraryExtensionURL)
  {
    this.javascriptLibraryExtensionURL = javascriptLibraryExtensionURL;
  }

  public String getShowXPath()
  {
    return showXPath;
  }

  public void setShowXPath(String showXPath)
  {
    this.showXPath = showXPath;
  }

  public String getHideAble()
  {
    return hideAble;
  }

  public void setHideAble(String hideAble)
  {
    this.hideAble = hideAble;
  }

  public String getAutoConfig()
  {
    return autoConfig;
  }

  public void setAutoConfig(String autoConfig)
  {
    this.autoConfig = autoConfig;
  }

  public String getConverter()
  {
    return converter;
  }

  public void setConverter(String converter)
  {
    this.converter = converter;
  }

  public String getImmediate()
  {
    return immediate;
  }

  public void setImmediate(String immediate)
  {
    this.immediate = immediate;
  }

  public String getRequired()
  {
    return required;
  }

  public void setRequired(String required)
  {
    this.required = required;
  }

  public String getValidator()
  {
    return validator;
  }

  public void setValidator(String validator)
  {
    this.validator = validator;
  }

  public String getValueChangedListener()
  {
    return valueChangedListener;
  }

  public void setValueChangedListener(String valueChangedListener)
  {
    this.valueChangedListener = valueChangedListener;
  }

  public String getAccesskey()
  {
    return accesskey;
  }

  public void setAccesskey(String accesskey)
  {
    this.accesskey = accesskey;
  }

  public String getDir()
  {
    return dir;
  }

  public void setDir(String dir)
  {
    this.dir = dir;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getTabindex()
  {
    return tabindex;
  }

  public void setTabindex(String tabindex)
  {
    this.tabindex = tabindex;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getReadonly()
  {
    return readonly;
  }

  public void setReadonly(String readonly)
  {
    this.readonly = readonly;
  }

  public String getLang()
  {
    return lang;
  }

  public void setLang(String lang)
  {
    this.lang = lang;
  }

   public String getAttachedFiles()
   {
      return attachedFiles;
   }

   public void setAttachedFiles(String attachedFiles)
   {
      this.attachedFiles = attachedFiles;
   }
}
