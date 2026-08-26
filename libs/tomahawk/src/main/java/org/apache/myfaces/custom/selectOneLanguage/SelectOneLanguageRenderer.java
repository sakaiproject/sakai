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
package org.apache.myfaces.custom.selectOneLanguage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;

import org.apache.myfaces.renderkit.html.ext.HtmlMenuRenderer;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.ResourceUtils;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.SelectOne"
 *   type = "org.apache.myfaces.SelectOneLanguageRenderer"
 * 
 * @author Sylvain Vieujot (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2005-05-11 12:14:23 -0400 (Wed, 11 May 2005) $
 */
public class SelectOneLanguageRenderer extends HtmlMenuRenderer {
    
    public void encodeEnd(FacesContext facesContext, UIComponent component)
    throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, null);

        SelectOneLanguage selectOneLanguage = (SelectOneLanguage) component;
        ResponseWriter writer = facesContext.getResponseWriter();

        Map<String, List<ClientBehavior>> behaviors = null;
        behaviors = selectOneLanguage.getClientBehaviors();
        if (!behaviors.isEmpty())
        {
            ResourceUtils.renderDefaultJsfJsInlineIfNecessary(
                    facesContext, facesContext.getResponseWriter());
        }
        
        if(HtmlRendererUtils.isDisplayValueOnly(component))
        {
            //HtmlRendererUtils.renderDisplayValueOnlyForSelects(facesContext, component);
            writer.startElement(HTML.SPAN_ELEM, selectOneLanguage);
            if (!behaviors.isEmpty())
            {
                writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext),null);
            }
            else
            {
                HtmlRendererUtils.writeIdIfNecessary(writer, selectOneLanguage, facesContext);
            }
        
            String[] supportedAttributes = {HTML.STYLE_CLASS_ATTR, HTML.STYLE_ATTR};
            HtmlRendererUtils.renderHTMLAttributes(writer, selectOneLanguage, supportedAttributes);
            
            String languageCode = selectOneLanguage.getValue().toString();
            String languageName = new Locale(languageCode).getDisplayLanguage( facesContext.getViewRoot().getLocale() );
            
            writer.write( languageName );
            
            writer.endElement(HTML.SPAN_ELEM);
            return;
        }
        
        writer.startElement(HTML.SELECT_ELEM, component);
        if (!behaviors.isEmpty())
        {
            writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext),null);
        }
        else
        {
            HtmlRendererUtils.writeIdIfNecessary(writer, selectOneLanguage, facesContext);
        }
        writer.writeAttribute(HTML.NAME_ATTR, selectOneLanguage.getClientId(facesContext), null);

        List selectItemList = selectOneLanguage.getLanguagesChoicesAsSelectItemList();
        Converter converter = HtmlRendererUtils.findUIOutputConverterFailSafe(facesContext, selectOneLanguage);

        writer.writeAttribute(HTML.SIZE_ATTR, "1", null);

        if (!behaviors.isEmpty())
        {
            HtmlRendererUtils.renderBehaviorizedOnchangeEventHandler(facesContext, writer, selectOneLanguage, behaviors);
            HtmlRendererUtils.renderBehaviorizedEventHandlers(facesContext, writer, selectOneLanguage, behaviors);
            HtmlRendererUtils.renderBehaviorizedFieldEventHandlersWithoutOnchange(facesContext, writer, selectOneLanguage, behaviors);
            HtmlRendererUtils.renderHTMLAttributes(writer, selectOneLanguage, HTML.SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
        }
        else
        {
            HtmlRendererUtils.renderHTMLAttributes(writer, selectOneLanguage, HTML.SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
        }
        
        if ( isDisabled(facesContext, selectOneLanguage) ) {
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);
        }

        Set lookupSet = HtmlRendererUtils.getSubmittedOrSelectedValuesAsSet(false, selectOneLanguage, facesContext, converter);

        HtmlRendererUtils.renderSelectOptions(facesContext, selectOneLanguage, converter, lookupSet, selectItemList);
        // bug #970747: force separate end tag
        writer.writeText("", null);
        writer.endElement(HTML.SELECT_ELEM);
    }
}
