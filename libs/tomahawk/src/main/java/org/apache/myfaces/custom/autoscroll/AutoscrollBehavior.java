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

import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.behavior.ClientBehaviorBase;
import jakarta.faces.component.behavior.ClientBehaviorContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFBehavior;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.FormInfo;
/**
 * Adds to a command link or button a javascript that enable automatic scrolling 
 * behavior after the event is invoked.
 * 
 * @since 1.1.10
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
@JSFBehavior(name="t:autoScroll",bodyContent="empty")
@ResourceDependency(library="org.apache.myfaces", name="oamSubmit.js")
public class AutoscrollBehavior extends ClientBehaviorBase
{

    public static final String BEHAVIOR_ID = "org.apache.myfaces.custom.autoscroll.AutoscrollBehavior";
    
    @Override
    public String getScript(ClientBehaviorContext behaviorContext)
    {
        FormInfo nestedFormInfo = RendererUtils.findNestingForm(behaviorContext.getComponent(), behaviorContext.getFacesContext());
        StringBuilder script = new StringBuilder();
        HtmlRendererUtils.appendAutoScrollAssignment(behaviorContext.getFacesContext(), script, nestedFormInfo.getFormName());
        return script.toString();
    }
    
    /**
     * The event that this client behavior should be attached.
     * 
     * @return
     */
    @JSFProperty
    private String getEvent()
    {
        return null;
    }
}
