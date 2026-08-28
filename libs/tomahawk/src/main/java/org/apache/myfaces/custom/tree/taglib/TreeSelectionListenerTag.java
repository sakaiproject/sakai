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
package org.apache.myfaces.custom.tree.taglib;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.webapp.UIComponentTag;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.myfaces.custom.tree.HtmlTree;
import org.apache.myfaces.custom.tree.event.TreeSelectionListener;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;


/**
 * Tag to add a tree selection listeners to a {@link HtmlTree}
 *
 * @JSFJspTag
 *   name="t:treeSelectionListener"
 *   bodyContent="empty"
 *   tagHandler="org.apache.myfaces.custom.tree.taglib.TreeSelectionListenerTagHandler"
 *   
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 666398 $ $Date: 2008-06-10 19:13:17 -0500 (Tue, 10 Jun 2008) $
 */
public class TreeSelectionListenerTag extends TagSupport
{
    private static final long serialVersionUID = 7322612767746076403L;
    private String type = null;


    public TreeSelectionListenerTag()
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
            throw new JspException("TreeSelectionListenerTag has no UIComponentTag ancestor");
        }

        if (componentTag.getCreated())
        {
            //Component was just created, so we add the Listener
            UIComponent component = componentTag.getComponentInstance();
            if (component instanceof HtmlTree)
            {
                String className;
                if (UIComponentTag.isValueReference(type))
                {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    ValueBinding valueBinding = facesContext.getApplication().createValueBinding(type);
                    className = (String) valueBinding.getValue(facesContext);
                } else
                {
                    className = type;
                }
                TreeSelectionListener listener = (TreeSelectionListener) ClassUtils.newInstance(className);
                ((HtmlTree) component).addTreeSelectionListener(listener);
            } else
            {
                throw new JspException("Component " + component.getId() + " is no HtmlTree");
            }
        }

        return Tag.SKIP_BODY;
    }
}
