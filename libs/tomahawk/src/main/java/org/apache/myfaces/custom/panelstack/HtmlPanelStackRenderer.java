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
package org.apache.myfaces.custom.panelstack;

import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.io.IOException;


/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "jakarta.faces.Panel"
 *   type = "org.apache.myfaces.PanelStack"
 * 
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 990487 $ $Date: 2010-08-28 23:25:48 -0500 (Sat, 28 Aug 2010) $
 */
public class HtmlPanelStackRenderer extends HtmlRenderer
{


    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
    }


    public boolean getRendersChildren()
    {
        return true;
    }


    public void encodeChildren(FacesContext facescontext, UIComponent uicomponent) throws IOException
    {
    }


    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlPanelStack.class);

        HtmlPanelStack panelStack = (HtmlPanelStack) uiComponent;
        String selectedPanel = panelStack.getSelectedPanel();
        UIComponent childToRender = null;

        if (selectedPanel != null && selectedPanel.length() > 0)
        {
            // render the selected child
            childToRender = panelStack.findComponent(selectedPanel);
            if (childToRender == null)
            {
                // if not found, render the first child
                if (panelStack.getChildCount() > 0) {
                    childToRender = (UIComponent) panelStack.getChildren().get(0);
                }
            }
        }
        else
        {
            // render the first child
            if (panelStack.getChildCount() > 0) {
                childToRender = (UIComponent) panelStack.getChildren().get(0);
            }
        }

        if (childToRender != null)
        {
            RendererUtils.renderChild(facesContext, childToRender);
        }
    }

}
