/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.tabbedpane;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.webapp.UIComponentTag;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.myfaces.shared_tomahawk.util.ClassUtils;


/**
 * Tag to add a tab change listeners to a {@link org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane}
 *
 * @JSFJspTag
 *   name="t:tabChangeListener"
 *   bodyContent="empty"
 *   tagHandler = "org.apache.myfaces.custom.tabbedpane.TabChangeListenerTagHandler"
 *   
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 692184 $ $Date: 2008-09-04 13:21:52 -0500 (Thu, 04 Sep 2008) $
 */
public class TabChangeListenerTag extends TagSupport
{
    private static final long serialVersionUID = -6903749011638483023L;
    private String type = null;


    public TabChangeListenerTag()
    {
    }

    /**
     * @JSFJspAttribute
     *   required = "true"
     */
    public void setType(String type)
    {
        this.type = type;
    }


    public int doStartTag() throws JspException
    {
        if (type == null)
        {
            throw new JspException("type attribute not set");
        }

        //Find parent UIComponentTag
        UIComponentTag componentTag = UIComponentTag.getParentUIComponentTag(pageContext);
        if (componentTag == null)
        {
            throw new JspException("TabChangeListenerTag has no UIComponentTag ancestor");
        }

        if (componentTag.getCreated())
        {
            //Component was just created, so we add the Listener
            UIComponent component = componentTag.getComponentInstance();
            if (component instanceof HtmlPanelTabbedPane)
            {
                Object listenerRef = type;
                if (UIComponentTag.isValueReference(type))
                {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    ValueBinding valueBinding = facesContext.getApplication().createValueBinding(type);
                    listenerRef = valueBinding.getValue(facesContext);
                }

                if(listenerRef instanceof String)
                {
                    String className = (String) listenerRef;
                    TabChangeListener listener = (TabChangeListener) ClassUtils.newInstance(className);
                    ((HtmlPanelTabbedPane) component).addTabChangeListener(listener);
                }
                else if(listenerRef instanceof TabChangeListener)
                {
                    TabChangeListener listener = (TabChangeListener) listenerRef;
                    ((HtmlPanelTabbedPane) component).addTabChangeListener(listener);

                }
                else
                    throw new JspException("type is neither a 'String' nor a value-binding to a 'String' or a 'TabChangeListener' instance.");
            }
            else
            {
                throw new JspException("Component " + component.getId() + " is no HtmlPanelTabbedPane");
            }
        }

        return Tag.SKIP_BODY;
    }
}
