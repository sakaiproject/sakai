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
package org.apache.myfaces.custom.tree.renderkit.html;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.custom.tree.HtmlTreeNode;
import org.apache.myfaces.renderkit.html.jsf.ExtendedHtmlLinkRenderer;
import org.apache.myfaces.renderkit.html.util.DummyFormUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Command"
 *   type = "org.apache.myfaces.HtmlTreeNode"
 * 
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (Sat, 24 May 2008) $
 */
public class HtmlTreeNodeRenderer
    extends ExtendedHtmlLinkRenderer {

    public void decode(FacesContext facesContext, UIComponent component) {
        super.decode(facesContext, component);
        String clientId = component.getClientId(facesContext);
        String reqValue = (String) facesContext
            .getExternalContext()
            .getRequestParameterMap()
            .get(HtmlRendererUtils
                .getHiddenCommandLinkFieldName(DummyFormUtils
                .findNestingForm(component, facesContext)));
        if (reqValue != null && reqValue.equals(clientId)) {
            HtmlTreeNode node = (HtmlTreeNode) component;

            node.setSelected(true);
        }
    }
}
