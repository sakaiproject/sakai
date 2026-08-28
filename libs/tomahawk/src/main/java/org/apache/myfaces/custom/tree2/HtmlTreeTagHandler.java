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
package org.apache.myfaces.custom.tree2;

import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIGraphic;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;

/**
 * 
 * @since 1.1.10
 * @author Leonardo Uribe
 */
public class HtmlTreeTagHandler extends ComponentHandler
{
    private static final String IMAGE_PREFIX = "t2";
    private static final String NODE_STATE_EXPANDED = "x";
    private static final String NODE_STATE_CLOSED = "c";
    
    public HtmlTreeTagHandler(ComponentConfig config)
    {
        super(config);
    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c,
            UIComponent parent)
    {
        if (ComponentHandler.isNew(c))
        {
            // t:tree2 requires to force id rendering of UIGraphic images on "expand" and "collapse" facets for
            // a nodeTypeFacet. Long time ago, this was done on HtmlTreeRenderer.encodeNavigation, but that
            // hack will not work well with jsf 2 PSS, because it requires ids to be generated in a "stable" way,
            // and that hack used a counter to generate it.
            for (Map.Entry<String, UIComponent> entry : c.getFacets().entrySet())
            {
                UIComponent nodeTypeFacet = entry.getValue();
                UIComponent expandFacet = nodeTypeFacet.getFacet("expand");
                if (expandFacet != null)
                {
                    //if (! (expandFacet instanceof UIGraphic))
                    //{
                    //    expandFacet = expandFacet.getChildren().get(0);
                    //    nodeTypeFacet.getFacets().put("expand", expandFacet);
                    //}
                    UIGraphic expandImg = (UIGraphic)expandFacet;
                    String id = expandImg.getId();
                    if (id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
                    {
                        expandImg.setId(IMAGE_PREFIX + id.substring(UIViewRoot.UNIQUE_ID_PREFIX.length()) + NODE_STATE_EXPANDED);
                    }
                    
                }
                
                UIComponent collapseFacet = nodeTypeFacet.getFacet("collapse");
                if (collapseFacet != null)
                {
                    //if (! (collapseFacet instanceof UIGraphic))
                    //{
                    //    collapseFacet = collapseFacet.getChildren().get(0);
                    //    nodeTypeFacet.getFacets().put("collapse", collapseFacet);
                    //}
                    UIGraphic collapseImg = (UIGraphic)collapseFacet;
                    String id = collapseImg.getId();
                    if (id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
                    {
                        collapseImg.setId(IMAGE_PREFIX + id.substring(UIViewRoot.UNIQUE_ID_PREFIX.length()) + NODE_STATE_CLOSED);
                    }
                }
            }
        }
    }
}
