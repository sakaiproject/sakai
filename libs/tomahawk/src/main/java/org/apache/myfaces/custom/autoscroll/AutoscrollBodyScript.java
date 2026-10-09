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
package org.apache.myfaces.custom.autoscroll;

import jakarta.faces.component.UIOutput;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

/**
 * This component is used internally to render the script that comes before
 * render &lt;/body&gt;  
 * 
 * @since 1.1.10
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
@JSFComponent
public class AutoscrollBodyScript extends UIOutput
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.custom.autoscroll.AutoscrollBodyScript";
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.custom.autoscroll.AutoscrollBodyScript";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.custom.autoscroll.AutoscrollBodyScript";

    public AutoscrollBodyScript()
    {
        this.getAttributes().put(JSFAttr.TARGET_ATTR, TomahawkResourceUtils.BODY_LOCATION);
    }
    
    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

}
