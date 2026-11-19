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

import jakarta.faces.component.UIPanel;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 * This component is used to render the body section in a t:dataTable, when
 * ajaxBodyRender="true" is set. In other words, if that property is set to true,
 * a special component is added on a facet with name "tbody_element"
 * and with id="tbody_element" that can be used as a target for f:ajax or similar
 * components to render the body.
 * 
 * @author Leonardo Uribe
 */
@JSFComponent
public class HtmlTableBodyElem extends UIPanel
{
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.HtmlTableBody"; 
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlTableBody";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.HtmlTableBody";
    
    public static final String DEFAULT_ID="tbody_element";

    public HtmlTableBodyElem()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
