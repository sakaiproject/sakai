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
package org.apache.myfaces.custom.htmlTag;

import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.component.ForceIdAware;
import org.apache.myfaces.component.StyleAware;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.util.HtmlComponentUtils;

/**
 * Creates an arbitrary HTML tag which encloses any child components. 
 * The value attribute specifies the name of the generated tag.
 * <br/>
 * If value is an empty string then no tag will be generated, but 
 * the child components will be rendered. This differs from setting 
 * rendered=false, which prevents child components from being 
 * rendered at all.
 * <br/>
 * You can specify some attribute to be added to the component 
 * using f:param like this:
 * <br/>
 * <t:htmlTag value="span">
 *       <f:param name="title" value="Hello world!"/>
 * </t:htmlTag>
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @since 1.1.7
 * @author bdudney (latest modification by $Author: lu4242 $)
 * @version $Revision: 691871 $ $Date: 2008-09-03 23:32:08 -0500 (Wed, 03 Sep 2008) $
 */
@JSFComponent(
        name = "t:htmlTag",
        clazz = "org.apache.myfaces.custom.htmlTag.HtmlTag",
        tagClass = "org.apache.myfaces.custom.htmlTag.HtmlTagTag")
@JSFJspProperty(
        name = "converter",
        returnType = "jakarta.faces.convert.Converter",
        tagExcluded = true)
public abstract class AbstractHtmlTag extends UIOutput 
    implements UserRoleAware, StyleAware, ForceIdAware
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTag";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Output";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlTagRenderer";

    public String getClientId(FacesContext context)
    {
        String clientId = HtmlComponentUtils.getClientId(this,
                getRenderer(context), context);
        if (clientId == null)
        {
            clientId = super.getClientId(context);
        }

        return clientId;
    }
    
    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }

}