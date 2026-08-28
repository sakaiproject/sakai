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
package org.apache.myfaces.custom.jslistener;

import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIOutput;

/**
 * Value change listener on client side. 
 * <p>
 * This component replicates the 'Value Change Listener' functionality on the client side. It can be used
 * when the user would like a change in the value of one control to trigger off changes in the states of 
 * other controls. One or more Javascript Listeners can be nested within the source control (a control 
 * belonging to the 'jakarta.faces.Input' family). When the value of the source control is modified, the 
 * listeners are triggered and the states of the target controls modified.
 * </p>
 * <p>
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * </p>
 * 
 * @JSFComponent
 *   name = "t:jsValueChangeListener"
 *   class = "org.apache.myfaces.custom.jslistener.JsValueChangeListener"
 *   tagClass = "org.apache.myfaces.custom.jslistener.JsValueChangeListenerTag"
 * 
 * @JSFJspProperty name = "rendered" returnType = "boolean" tagExcluded = "true"
 * @JSFJspProperty name = "binding" returnType = "java.lang.String" tagExcluded = "true"
 * @JSFJspProperty name = "id" returnType = "java.lang.String" tagExcluded = "true"
 * @since 1.1.7
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractJsValueChangeListener extends UIComponentBase
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.JsValueChangeListener";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Output";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.JsValueChangeListener";

    /**
     * for - the id of the target control
     * 
     * @JSFProperty
     */
    public abstract String getFor();

    /**
     * the javascript expression to evaluate. The keyword '$srcElem' resolves to 
     * the source control and the keyword '$destElem' resolves to the target control
     * 
     * @JSFProperty
     *   required="true"
     */
    public abstract String getExpressionValue();

    /**
     * The result of the evaluated expression is assigned to the specified property 
     * of the target control
     * 
     * @JSFProperty
     */
    public abstract String getProperty();

    /**
     * Events are triggered by the 'onchange' event of the source control. Here, 
     * an additional event can be specified (onload?).
     * 
     * If specified this JavaScript event will be inserted in the 
     * body tag. JavaScript code will be the same like it is 
     * rendered in the parent component.
     * 
     * @JSFProperty
     */
    public abstract String getBodyTagEvent();

}
