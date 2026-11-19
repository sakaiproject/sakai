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
package org.apache.myfaces.component.html.ext;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;

public class HtmlDataTableTagHandler extends ComponentHandler
{
    private static final String ADD_SPECIAL_COMPONENTS_POPULATE = "oam.tomahawk.table.ADD_SPCP";
    
    /**
     * Special myfaces core marker to indicate the component is handled by a facelet tag handler,
     * so its creation is not handled by user programatically and PSS remove listener should
     * not register it when a remove happens.
     */
    public final static String COMPONENT_ADDED_BY_HANDLER_MARKER = "oam.vf.addedByHandler";
    
    public HtmlDataTableTagHandler(ComponentConfig config)
    {
        super(config);
    }
    
    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c,
            UIComponent parent)
    {
        // Indicate the next onComponentPopulated should be used to check and 
        // add some special facets according to some properties.
        c.getAttributes().put(ADD_SPECIAL_COMPONENTS_POPULATE, Boolean.TRUE);
    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c,
            UIComponent parent)
    {
        if (c.getAttributes().containsKey(ADD_SPECIAL_COMPONENTS_POPULATE))
        {
            HtmlDataTable dataTable = (HtmlDataTable) c;
            
            UIComponent detailStamp = c.getFacet(AbstractHtmlDataTable.DETAIL_STAMP_FACET_NAME);
            if (detailStamp != null)
            {
                // Add a detailStampRow component, so detailStamp rendering will be delegated
                // to a component that can be used in ajax.
                UIComponent detailStampRow = ctx.getFacesContext().
                    getApplication().createComponent(ctx.getFacesContext(),
                        HtmlDetailStampRow.COMPONENT_TYPE, HtmlDetailStampRow.DEFAULT_RENDERER_TYPE);
                detailStampRow.setId(HtmlDetailStampRow.DEFAULT_ID);
                detailStampRow.getAttributes().put(COMPONENT_ADDED_BY_HANDLER_MARKER, Boolean.TRUE);
                c.getFacets().put(AbstractHtmlDataTable.DETAIL_STAMP_ROW_FACET_NAME, detailStampRow);
            }
            if (dataTable.isAjaxRowRender())
            {
                // Add a special component that is used to render a row on an ajax operation.
                UIComponent row = ctx.getFacesContext().
                    getApplication().createComponent(ctx.getFacesContext(),
                        HtmlTableRow.COMPONENT_TYPE, HtmlTableRow.DEFAULT_RENDERER_TYPE);
                row.setId(HtmlTableRow.DEFAULT_ID);
                row.getAttributes().put(COMPONENT_ADDED_BY_HANDLER_MARKER, Boolean.TRUE);
                c.getFacets().put(AbstractHtmlDataTable.TABLE_ROW_FACET_NAME, row);
            }
            if (dataTable.isAjaxBodyRender())
            {
                // Add a special component that is used to render the body part on an ajax operation.
                UIComponent bodyElem = ctx.getFacesContext().
                    getApplication().createComponent(ctx.getFacesContext(),
                        HtmlTableBodyElem.COMPONENT_TYPE, HtmlTableBodyElem.DEFAULT_RENDERER_TYPE);
                bodyElem.setId(HtmlTableBodyElem.DEFAULT_ID);
                bodyElem.getAttributes().put(COMPONENT_ADDED_BY_HANDLER_MARKER, Boolean.TRUE);
                c.getFacets().put(AbstractHtmlDataTable.TABLE_BODY_FACET_NAME, bodyElem);
            }
            c.getAttributes().remove(ADD_SPECIAL_COMPONENTS_POPULATE);
        }
    }

    protected MetaRuleset createMetaRuleset(Class type)
    {
        return super.createMetaRuleset(type).alias("class", "styleClass");
    }
}
