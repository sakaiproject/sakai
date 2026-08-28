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
package org.apache.myfaces.custom.tabbedpane;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PhaseId;

/**
 * @author Manfred Geiler (latest modification by $Author: grantsmith $)
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public class TabChangeEvent
        extends FacesEvent
{
    private static final long serialVersionUID = -7249763750612129099L;
    //private static final Log log = LogFactory.getLog(TabChangeEvent.class);
    private int _oldTabIndex;
    private int _newTabIndex;

    public TabChangeEvent(UIComponent component, int oldTabIndex, int newTabIndex)
    {
        super(component);
        _oldTabIndex = oldTabIndex;
        _newTabIndex = newTabIndex;
        setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
    }

    public int getOldTabIndex()
    {
        return _oldTabIndex;
    }

    public int getNewTabIndex()
    {
        return _newTabIndex;
    }

    public boolean isAppropriateListener(FacesListener listener)
    {
        return listener instanceof TabChangeListener;
    }

    public void processListener(FacesListener listener)
    {
        ((TabChangeListener)listener).processTabChange(this);
    }

}
