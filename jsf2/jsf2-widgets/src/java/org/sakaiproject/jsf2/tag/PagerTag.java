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
public class PagerTag extends UIComponentTag {

    private String totalItems;
    private String firstItem;
    private String pageSize;
    private String value;
    private String valueChangeListener;
    private String pageSizes;
    private String accesskeys;
    private String renderFirst;
    private String renderPrev;
    private String renderNext;
    private String renderLast;
    private String renderPageSize;
    private String textFirst;
    private String textPrev;
    private String textNext;
    private String textLast;
    private String textPageSize;
    private String textStatus;
    private String textItem;
    private String immediate;

    public String getComponentType() {
        return ("org.sakaiproject.Pager");
    }

    public String getRendererType() {
        return ("org.sakaiproject.Pager");
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        TagUtil.setInteger(component, "totalItems", totalItems);
        TagUtil.setInteger(component, "firstItem", firstItem);
        TagUtil.setInteger(component, "pageSize", pageSize);
        TagUtil.setString(component, "value", value);
        TagUtil.setValueChangeListener(component, valueChangeListener);
        TagUtil.setString(component, "pageSizes", pageSizes);
        TagUtil.setBoolean(component, "accesskeys", accesskeys);
        TagUtil.setBoolean(component, "renderFirst", renderFirst);
        TagUtil.setBoolean(component, "renderPrev", renderPrev);
        TagUtil.setBoolean(component, "renderNext", renderNext);
        TagUtil.setBoolean(component, "renderLast", renderLast);
        TagUtil.setBoolean(component, "renderPageSize", renderPageSize);
        TagUtil.setString(component, "textFirst", textFirst);
        TagUtil.setString(component, "textPrev", textPrev);
        TagUtil.setString(component, "textNext", textNext);
        TagUtil.setString(component, "textLast", textLast);
        TagUtil.setString(component, "textPageSize", textPageSize);
        TagUtil.setString(component, "textStatus", textStatus);
        TagUtil.setString(component, "textItem", textItem);
        TagUtil.setBoolean(component, "immediate", immediate);
    }

    public void release() {
        super.release();
        totalItems = null;
        firstItem = null;
        pageSize = null;
        value = null;
        valueChangeListener = null;
        pageSizes = null;
        accesskeys = null;
        renderFirst = null;
        renderPrev = null;
        renderNext = null;
        renderLast = null;
        renderPageSize = null;
        textFirst = null;
        textPrev = null;
        textNext = null;
        textLast = null;
        textPageSize = null;
        textStatus = null;
        textItem = null;
        immediate = null;
    }
}
