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
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.util.TagUtil;

public class RichTextAreaTag extends UIComponentTag
{
    private String value;
    private String width;
    private String height;
    private String toolbarButtonRows;
    private String javascriptLibrary;
    private String autoConfig;
    private String columns;
    private String rows;
    private String justArea;

    public String getComponentType()
    {
        return "org.sakaiproject.RichTextArea";
    }

    public String getRendererType()
    {
        return "org.sakaiproject.RichTextArea";
    }

    // getters and setters for component properties

    public void setValue(String newValue) { value = newValue; }
    public String getValue() { return value; }
    public void setWidth(String newWidth) { width = newWidth; }
    public String getWidth() { return width; }
    public void setHeight(String newHeight) { height = newHeight; }
    public String getHeight() { return height; }
    public void setToolbarButtonRows(String str) { toolbarButtonRows = str; }
    public String getToolbarButtonRows() { return toolbarButtonRows; }
    public void setJavascriptLibrary(String str) { javascriptLibrary = str; }
    public String getJavascriptLibrary() { return javascriptLibrary; }
    public void setAutoConfig(String str) { autoConfig = str; }
    public String getAutoConfig() { return autoConfig; }
    public void setColumns(String newC) { columns = newC; }
    public String getColumns() { return columns; }
    public void setRows(String newRows) { rows = newRows; }
    public String getRows() { return rows; }
    public void setJustArea(String newJ) { justArea = newJ; }
    public String getJustArea() { return justArea; }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);
        TagUtil.setString(component, "value", value);
        TagUtil.setString(component, "width", width);
        TagUtil.setString(component, "height", height);
        TagUtil.setString(component, "toolbarButtonRows", toolbarButtonRows);
        TagUtil.setString(component, "javascriptLibrary", javascriptLibrary);
        TagUtil.setString(component, "autoConfig", autoConfig);
        TagUtil.setString(component, "columns", columns);
        TagUtil.setString(component, "rows", rows);
        TagUtil.setString(component, "justArea", justArea);
    }

    public void release()
    {
        super.release();
        
        value = null;
        width = null;
        height = null;
        toolbarButtonRows = null;
        javascriptLibrary = null;
        autoConfig = null;
        columns = null;
        rows = null;
        justArea = null;      
    }
}



