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
package org.apache.myfaces.custom.datascroller;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.ActionEvent;

/**
 * An event representing a click on some scroller control to
 * change the currently displayed table rows.
 * 
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ScrollerActionEvent extends ActionEvent
{
    private static final long serialVersionUID = -5692343289423906802L;

    private final String mScrollerfacet;

    private final int mPageIndex;

    /**
     * An event representing a user's choice of navigation option
     * <i>except</i> jumping to a specific page.
     * <o>
     * Param scrollerFacet contains the name of the operation performed,
     * which matches one of the public HtmlDataScroller.FACET_* constants.
     */
    public ScrollerActionEvent(UIComponent component, String scrollerfacet)
    {
        super(component);
        mScrollerfacet = scrollerfacet;
        mPageIndex = -1;
    }

    /**
     * An event representing a user's choice to jump straight to page
     * #pageIndex of the available pages of data.
     * 
     * @param component is the DataScroller component 
     * @param pageIndex is in the range 0..(nPages-1), where nPages
     * is (rowsOfDataAvailable/rowsPerPage).
     */
    public ScrollerActionEvent(UIComponent component, int pageIndex)
    {
        super(component);
        if (pageIndex < 0)
        {
            throw new IllegalArgumentException("wrong pageindex");
        }
        mPageIndex = pageIndex;
        mScrollerfacet = null;
    }

    /**
     * Returns a string which matches one of the HtmlDataScroller.FACET_*
     * public constants, or null if the user chose a page# navigation option.
     */
    public String getScrollerfacet()
    {
        return mScrollerfacet;
    }

    /**
     * Return the page of data the user wants to see, or -1 if the
     * user didn't choose a page# navigation option. 
     */
    public int getPageIndex()
    {
        return mPageIndex;
    }
}
