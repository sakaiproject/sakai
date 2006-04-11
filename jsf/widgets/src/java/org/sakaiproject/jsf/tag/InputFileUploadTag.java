/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees
 * of Indiana University, Board of Trustees of the Leland Stanford, Jr.,
 * University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you
 * have read, understand, and will comply with the terms and conditions of the
 * Educational Community License. You may obtain a copy of the License at:
 * 
 * http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *  
 ******************************************************************************/

package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import org.sakaiproject.jsf.util.TagUtil;

public class InputFileUploadTag extends UIComponentTag
{
    private String value;
    private String directory;
 
    private String valueChangeListener;
    private String style;
    private String styleClass;
    private String immediate;
    private String required;
    private String validator;
 
    private String accept;
    private String align;
    private String accesskey;
    private String maxlength;
    private String size;
    private String disabled;
    private String readonly;
    private String tabindex;
    

    public void setProperties(UIComponent component)
    {
        super.setProperties(component);
        TagUtil.setString(component, "value", value);
        TagUtil.setString(component, "directory", directory);
        TagUtil.setValueChangeListener(component, valueChangeListener);
        TagUtil.setString(component, "style", style);
        TagUtil.setString(component, "styleClass", styleClass);
        TagUtil.setBoolean(component, "immediate", immediate);
        TagUtil.setBoolean(component, "required", required);
        TagUtil.setValidator(component, validator);
        TagUtil.setString(component, "accept", accept);
        TagUtil.setString(component, "align", align);
        TagUtil.setString(component, "accesskey", accesskey);
        TagUtil.setString(component, "maxlength", maxlength);
        TagUtil.setString(component, "size", size);
        TagUtil.setString(component, "disabled", disabled);
        TagUtil.setString(component, "readonly", readonly);
        TagUtil.setString(component, "tabindex", tabindex);
    }

    public void release()
    {
        super.release();
        value = null;
        directory = null;
        valueChangeListener = null;
        style = null;
        styleClass = null;
        immediate = null;
        required = null;
        validator = null;
       accept = null;
        align = null;
        accesskey = null;
        maxlength = null;
        size = null;
        disabled = null;
        readonly = null;
        tabindex = null;
    }

    public String getRendererType()
    {
        return "org.sakaiproject.InputFileUpload";
    }

    public String getComponentType()
    {
        return "javax.faces.Input";
    }
    
    // getters and setters
    
    public String getAccept()
    {
        return accept;
    }
    public void setAccept(String accept)
    {
        this.accept = accept;
    }
    public String getAccesskey()
    {
        return accesskey;
    }
    public void setAccesskey(String accesskey)
    {
        this.accesskey = accesskey;
    }
    public String getAlign()
    {
        return align;
    }
    public void setAlign(String align)
    {
        this.align = align;
    }
    public String getDirectory()
    {
        return directory;
    }
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }
    public String getDisabled()
    {
        return disabled;
    }
    public void setDisabled(String disabled)
    {
        this.disabled = disabled;
    }
    public String getImmediate()
    {
        return immediate;
    }
    public void setImmediate(String immediate)
    {
        this.immediate = immediate;
    }
    public String getMaxlength()
    {
        return maxlength;
    }
    public void setMaxlength(String maxlength)
    {
        this.maxlength = maxlength;
    }
    public String getReadonly()
    {
        return readonly;
    }
    public void setReadonly(String readonly)
    {
        this.readonly = readonly;
    }
    public String getRequired()
    {
        return required;
    }
    public void setRequired(String required)
    {
        this.required = required;
    }
    public String getSize()
    {
        return size;
    }
    public void setSize(String size)
    {
        this.size = size;
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
    public String getValidator()
    {
        return validator;
    }
    public void setValidator(String validator)
    {
        this.validator = validator;
    }
    public String getValue()
    {
        return value;
    }
    public void setValue(String value)
    {
        this.value = value;
    }
    public String getValueChangeListener()
    {
        return valueChangeListener;
    }
    public void setValueChangeListener(String valueChangeListener)
    {
        this.valueChangeListener = valueChangeListener;
    }
}
