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

import java.io.IOException;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import org.apache.myfaces.custom.tree.HtmlTree;
import org.apache.myfaces.custom.tree.IconProvider;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

/**
 * 
 * @since 1.1.7
 */
public class IconProviderTagHandler extends TagHandler
{

    private final TagAttribute typeAttr;
    
    public IconProviderTagHandler(TagConfig config)
    {
        super(config);
        typeAttr = getRequiredAttribute("type");
    }

    public void apply(FaceletContext faceletContext, UIComponent parent)
            throws IOException, FacesException, FaceletException, ELException
    {
        if (parent.getParent() == null)
        {
            if (parent instanceof HtmlTree)
            {
                String className;
                if (!typeAttr.isLiteral())
                {
                    className = typeAttr.getValue();
                }
                else
                {
                    className = typeAttr.getValue(faceletContext);
                }
                IconProvider provider = (IconProvider) ClassUtils.newInstance(className);
                ((HtmlTree) parent).setIconProvider(provider);                
            }
            else
            {
                throw new FacesException(
                        "Component is not HtmlTree children");
            }            
        }
    }
}
