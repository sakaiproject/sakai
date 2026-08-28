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
package org.apache.myfaces.custom.tree.event;

import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.component.UIComponent;

import org.apache.myfaces.custom.tree.model.TreePath;


/**
 * Event fired by {@link org.apache.myfaces.custom.tree.HtmlTree} on selection changes.
 *
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public class TreeSelectionEvent extends FacesEvent
{
    private static final long serialVersionUID = -361206105634892091L;
    private TreePath oldSelectionPath;
    private TreePath newSelectionPath;


    /**
     * Construct an event.
     *
     * @param uiComponent      event source
     * @param oldSelectionPath path of the old selection, null if no node was selected before
     * @param newSelectionPath path of the current selection
     */
    public TreeSelectionEvent(UIComponent uiComponent, TreePath oldSelectionPath, TreePath newSelectionPath)
    {
        super(uiComponent);
        this.oldSelectionPath = oldSelectionPath;
        this.newSelectionPath = newSelectionPath;
    }


    /**
     * Answer the path of the old selection.
     *
     * @return path of previous (old) selection, null if no node was selected before
     */
    public TreePath getOldSelectionPath()
    {
        return oldSelectionPath;
    }


    /**
     * Answer the path of the current (new) selection.
     *
     * @return path of the new selected node
     */
    public TreePath getNewSelectionPath()
    {
        return newSelectionPath;
    }


    public boolean isAppropriateListener(FacesListener faceslistener)
    {
        return faceslistener instanceof TreeSelectionListener;
    }


    public void processListener(FacesListener faceslistener)
    {
        ((TreeSelectionListener) faceslistener).valueChanged(this);
    }
}
