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
package org.apache.myfaces.custom.document;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.component.html.util.StreamingAddResource;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * Document to enclose the document head. If not otherwise possible you can use
 * state="start|end" to demarkate the document boundaries
 * 
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC"
 *   family = "jakarta.faces.Data"
 *   type = "org.apache.myfaces.DocumentHead"
 * 
 * @author Mario Ivankovits (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (jue, 03 jul 2008) $
 */
public class DocumentHeadRenderer extends AbstractDocumentRenderer
{
    public static final String RENDERER_TYPE = "org.apache.myfaces.DocumentHead";

    protected String getHtmlTag()
    {
        return "head";
    }

    protected Class getDocumentClass()
    {
        return DocumentHead.class;
    }

    protected void writeBeforeEnd(FacesContext facesContext) throws IOException
    {
        super.writeBeforeEnd(facesContext);
        
        AddResource addResource = AddResourceFactory.getInstance(facesContext);
        if (addResource instanceof StreamingAddResource)
        {
            ((StreamingAddResource) addResource).addStyleLoaderHere(facesContext, DocumentHead.class);
        }
        
        UIViewRoot root = facesContext.getViewRoot();
        for (UIComponent child : root.getComponentResources(facesContext,
                TomahawkResourceUtils.HEAD_LOCATION))
        {
            child.encodeAll(facesContext);
        }
    }
}