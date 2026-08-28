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
package org.apache.myfaces.custom.jsvalueset;

import jakarta.faces.component.UIOutput;

import org.apache.myfaces.component.UserRoleUtils;

/**
 * Setting a value from the model in java-script so that it can be 
 * used (e.g. by the value change listener) afterwards. 
 * 
 * Unless otherwise specified, all attributes accept static values 
 * or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:jsValueSet"
 *   class = "org.apache.myfaces.custom.jsvalueset.HtmlJsValueSet"
 *   tagClass = "org.apache.myfaces.custom.jsvalueset.HtmlJsValueSetTag"
 * @JSFJspProperty name = "id" tagExcluded = "true"
 * @JSFJspProperty name = "binding" tagExcluded = "true"
 * @JSFJspProperty name = "rendered" tagExcluded = "true"
 * @JSFJspProperty name = "converter" tagExcluded = "true"
 * @since 1.1.7
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractHtmlJsValueSet extends UIOutput
{

    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlJsValueSet";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Output";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.JsValueSet";

    /**
     * javascript variable to be set.
     * 
     * @JSFProperty
     *   required="true"
     */
    public abstract String getName();

    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }
    
    /**
     * value to be set in the variable.
     * 
     * @JSFProperty
     *   required="true"
     */
    public Object getValue()
    {
        return super.getValue();
    }

}


