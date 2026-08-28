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

import jakarta.faces.component.UIComponentBase;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * Base class to handle the document family
 * 
 * @JSFComponent
 *   tagClass = "org.apache.myfaces.custom.document.AbstractDocumentTag"
 *   configExcluded = "true"
 *   
 * @author Mario Ivankovits (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (jue, 03 jul 2008) $
 */
@JSFComponent(
   tagClass = "org.apache.myfaces.custom.document.AbstractDocumentTag",
   configExcluded = true)
public class AbstractDocument extends UIComponentBase
{
    public static final String COMPONENT_FAMILY = "jakarta.faces.Data";

    public AbstractDocument(String renderType)
    {
        setRendererType(renderType);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void setState(String state)
    {
        getStateHelper().put(PropertyKeys.state, state );
    }

    /**
     * state="start|end". Used to demarkate the document boundaries
     * 
     */
    @JSFProperty(literalOnly = true)
    public String getState()
    {
        return (String)getStateHelper().get(PropertyKeys.state);
    }

    public boolean hasState()
    {
        return isStartState() || isEndState();
    }

    public boolean isStartState()
    {
        return "start".equals(getState());
    }

    public boolean isEndState()
    {
        return "end".equals(getState());
    }
    
    protected enum PropertyKeys
    {
        state
    }
}