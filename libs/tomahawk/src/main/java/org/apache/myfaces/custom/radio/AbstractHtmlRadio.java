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
package org.apache.myfaces.custom.radio;

import jakarta.faces.component.UIComponentBase;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.component.AccesskeyProperty;
import org.apache.myfaces.component.AlignProperty;
import org.apache.myfaces.component.AltProperty;
import org.apache.myfaces.component.ChangeSelectProperties;
import org.apache.myfaces.component.DisabledClassEnabledClassProperties;
import org.apache.myfaces.component.EventAware;
import org.apache.myfaces.component.FocusBlurProperties;
import org.apache.myfaces.component.StyleAware;
import org.apache.myfaces.component.TabindexProperty;
import org.apache.myfaces.component.UniversalProperties;
import org.apache.myfaces.component.UserRoleAware;

/**
 * This tag is used in conjunction with the extended selectOneRadio 
 * tag when the "spread" layout is selected. It specifies the 
 * position within the document that the radio button corresponding 
 * to a specific SelectItem should be rendered. All HTML pass-through 
 * attributes for this input are taken from the associated 
 * selectOneRadio. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:radio"
 *   class = "org.apache.myfaces.custom.radio.HtmlRadio"
 *   tagClass = "org.apache.myfaces.custom.radio.HtmlRadioTag"
 * @since 1.1.7
 * @author Thomas Spiegl (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public abstract class AbstractHtmlRadio
    extends UIComponentBase implements UserRoleAware, 
    FocusBlurProperties, ChangeSelectProperties, 
    UniversalProperties, EventAware, AltProperty, 
    AlignProperty, StyleAware, AccesskeyProperty, TabindexProperty, DisabledClassEnabledClassProperties
{
    //private static final Log log = LogFactory.getLog(HtmlRadio.class);

    public static final String FOR_ATTR = "for".intern();
    public static final String INDEX_ATTR = "index".intern();


    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlRadio";
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.Radio";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Radio";

    /**
     * The id of the referenced extended selectOneRadio component. 
     * This value is resolved to the particular component using 
     * the standard UIComponent.findComponent() searching algorithm.
     * 
     * @JSFProperty
     *   required="true"
     */
    public abstract String getFor();
    
    /**
     * The index of the corresponding SelectItem, where 0 represents the first SelectItem.
     * 
     * @JSFProperty
     *   defaultValue = "Integer.MIN_VALUE"
     *   required="true"
     */
    public abstract int getIndex();
    
    /**
     * If this property is set to true, the id generated for the input html markup
     * will be the logical id composed from the clientId of the associated 
     * selectOneRadio and the index of this component (for example 'myComp:2').
     * 
     * <p>
     * NOTE: This is provided only for backward compatibility with tomahawk 1.2.
     * Activate this behavior will make client behaviors added to 
     * t:selectOneRadio like f:ajax or others fail, because the logical id 
     * has no counterpart in the component tree.
     * </p>
     * 
     * @return
     */
    @JSFProperty(defaultValue="false")
    public abstract boolean isRenderLogicalId();
    

}
