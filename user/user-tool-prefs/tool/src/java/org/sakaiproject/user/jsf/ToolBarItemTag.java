/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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

package org.sakaiproject.user.jsf;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.jsf2.util.JSFDepends;
import org.sakaiproject.jsf2.util.TagUtil;

import javax.faces.component.UIComponent;

@Getter
@Setter
public class ToolBarItemTag extends JSFDepends.CommandButtonTag
{

    /**
     * Indicates if the tool bar item is "current" - i.e. represents the current view being rendered
     * In other words, if this is current then it is the tool bar item indicating the current page the user is viewing
     */
    String current = null; // NOTE: MUST be a string to work right

    String _action = null;

    String _value = null;

    String _rendered = null;

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        if (current != null) {
            TagUtil.setBoolean(component, "current", current);
        }
        if (_action != null) {
            TagUtil.setAction(component, _action);
        }

        if (_rendered != null) {
            TagUtil.setBoolean(component, "rendered", _rendered);
        }

        if (_value != null) {
            TagUtil.setValueBinding(component, "value", _value);
        }
    }

    public String getRendererType()
    {
        return "org.sakaiproject.user.jsf.ToolBarItem";
    }

//    public String getComponentType() {
//        return "javax.faces.Command";
//    }

}



