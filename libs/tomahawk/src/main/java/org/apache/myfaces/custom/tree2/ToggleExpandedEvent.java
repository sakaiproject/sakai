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
package org.apache.myfaces.custom.tree2;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PhaseId;

/**
 * @author Mathias Broekelmann
 *
 */
public class ToggleExpandedEvent extends FacesEvent
{
    private final String mNodeId;

    /**
     * @param uiComponent
     * @param nodeId 
     */
    public ToggleExpandedEvent(UIComponent uiComponent, String nodeId)
    {
        super(uiComponent);
        mNodeId = nodeId;
        setPhaseId(PhaseId.INVOKE_APPLICATION);
    }
    
    /**
     * @return Returns the nodeId.
     */
    public String getNodeId()
    {
        return mNodeId;
    }

    /**
     * @see jakarta.faces.event.FacesEvent#isAppropriateListener(jakarta.faces.event.FacesListener)
     */
    public boolean isAppropriateListener(FacesListener faceslistener)
    {
        return false;
    }

    /**
     * @see jakarta.faces.event.FacesEvent#processListener(jakarta.faces.event.FacesListener)
     */
    public void processListener(FacesListener faceslistener)
    {
        throw new UnsupportedOperationException();
    }
}
