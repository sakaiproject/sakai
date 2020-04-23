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

@Data
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
}
