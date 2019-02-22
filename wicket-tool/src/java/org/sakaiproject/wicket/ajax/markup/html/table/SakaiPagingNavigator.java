/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.wicket.ajax.markup.html.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

public class SakaiPagingNavigator extends AjaxPagingNavigator
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param id
     *            See Component
     * @param pageable
     *            The pageable component the page links are referring to.
     */
    public SakaiPagingNavigator(final String id, final IPageable pageable)
    {
        this(id, pageable, null);
    }

    /**
     * Constructor.
     *
     * @param id
     *            See Component
     * @param pageable
     *            The pageable component the page links are referring to.
     * @param labelProvider
     *            The label provider for the link text.
     */
    public SakaiPagingNavigator(final String id, final IPageable pageable, final IPagingLabelProvider labelProvider)
    {
        super(id, pageable, labelProvider);
    }

    @Override
    protected void onBeforeRender()
    {
        if (get("pageSizeSelector") == null)
        {
            setDefaultModel(new CompoundPropertyModel(this));

            // Get the row number selector
            add(newPageSizeSelector(getPageable()));

            // Add additional page links
            replace(newPagingNavigationLink("first", getPageable(), 0));
            replace(newPagingNavigationIncrementLink("prev", getPageable(), -1));
            replace(newPagingNavigationIncrementLink("next", getPageable(), 1));
            replace(newPagingNavigationLink("last", getPageable(), -1));
        }
        super.onBeforeRender();
    }

    /**
     * Create a new increment link. May be subclassed to make use of specialized links, e.g. Ajaxian
     * links.
     *
     * @param id
     *            the link id
     * @param pageable
     *            the pageable to control
     * @param increment
     *            the increment
     * @return the increment link
     */
    protected Link newPagingNavigationIncrementLink(String id, IPageable pageable, int increment)
    {
        return new AjaxPagingNavigationIncrementLink(id, pageable, increment);
    }

    /**
     * Create a new pagenumber link. May be subclassed to make use of specialized links, e.g.
     * Ajaxian links.
     *
     * @param id
     *            the link id
     * @param pageable
     *            the pageable to control
     * @param pageNumber
     *            the page to jump to
     * @return the pagenumber link
     */
    protected Link newPagingNavigationLink(String id, IPageable pageable, int pageNumber)
    {
        return new AjaxPagingNavigationLink(id, pageable, pageNumber);
    }

    protected DropDownChoice newPageSizeSelector(final IPageable pageable)
    {
        List<String> choices = new ArrayList<String>();
        choices.add("5");
        choices.add("10");
        choices.add("20");
        choices.add("50");
        choices.add("100");
        choices.add("200");
        DropDownChoice pageSizeSelector = new DropDownChoice("pageSizeSelector", choices, new IChoiceRenderer()
        {
            @Override
            public Object getDisplayValue(Object object)
            {
                StringResourceModel model = new StringResourceModel("pager_textPageSize", SakaiPagingNavigator.this);
                model.setParameters(object);
                return model.getString();
            }

            @Override
            public String getIdValue(Object object, int index)
            {
                return (String) object;
            }

            @Override
            public Object getObject(String id, IModel choices)
            {
                return id;
            }
        });

        pageSizeSelector.add(new FormComponentUpdatingBehavior()
        {
            @Override
            protected void onUpdate()
            {
                // Tell the PageableListView which page to print next
                pageable.setCurrentPage(0);

                // We do need to redirect, else refresh refresh will go to next, next
                //setRedirect(true);

                // Return the current page.
                setResponsePage(getPage());
                super.onUpdate();
            }

            @Override
            protected void onError(RuntimeException ex)
            {
                super.onError(ex);
            }
        });

        return pageSizeSelector;
    }

    public String getPageSizeSelector()
    {
        return String.valueOf(((DataTable) getPageable()).getItemsPerPage());
    }

    public void setPageSizeSelector(String value)
    {
        ((DataTable) getPageable()).setItemsPerPage(Integer.valueOf(value));
    }

    /**
     * Create a new PagingNavigation. May be subclassed to make us of specialized PagingNavigation.
     *
     * @param pageable
     *            the pageable component
     * @param labelProvider
     *            The label provider for the link text.
     * @return the navigation object
     */
    @Override
    protected PagingNavigation newNavigation(final String id, final IPageable pageable, final IPagingLabelProvider labelProvider)
    {
        PagingNavigation nav = new PagingNavigation("navigation", pageable, labelProvider)
        {
            @Override
            public boolean isVisible()
            {
                // hide the numbered navigation bar e.g. 1 | 2 | 3 etc.
                return false;
            }
        };

        nav.setVisible( false );
        nav.setVisibilityAllowed( false );
        return nav;
    }
}
