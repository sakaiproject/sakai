/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.cheftool;

import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuField;
import org.sakaiproject.courier.api.ObservingCourier;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.SessionState;


/**
 * <p>
 * PagedResourceAction is a base class that handles paged display of lists of Resourecs.
 * </p>
 */
@Slf4j
public abstract class NewPagedResourceAction extends VelocityPortletPaneledAction
{

	private static final long serialVersionUID = 1L;

	/** The default number of items per page. */
	protected static final int DEFAULT_PAGE_SIZE = 15;

	/** portlet configuration parameter names. */
	protected static final String PARAM_PAGESIZE = "pagesize";

	/** state attribute names. */
	protected static final String STATE_VIEW_ID = "view-id";

	protected static final String STATE_TOP_PAGE_ITEM = "item-top";

	protected static final String STATE_PAGESIZE = "page-size";

	protected static final String STATE_TOTAL_PAGENUMBER = "total_page_number";

	protected static final String STATE_NUM_ITEMS = "num-items";

	protected static final String STATE_NEXT_PAGE_EXISTS = "item-next-page";

	protected static final String STATE_PREV_PAGE_EXISTS = "item-prev-page";

	protected static final String STATE_GO_NEXT_PAGE = "item-go-next-page";

	protected static final String STATE_GO_PREV_PAGE = "item-go-prev-page";

	protected static final String STATE_GO_NEXT = "item-go-next";

	protected static final String STATE_GO_PREV = "item-go-prev";

	protected static final String STATE_NEXT_EXISTS = "item-next";

	protected static final String STATE_PREV_EXISTS = "item-prev";

	protected static final String STATE_GO_FIRST_PAGE = "item-go-first-page";

	protected static final String STATE_GO_LAST_PAGE = "item-go-last-page";

	protected static final String STATE_SEARCH = "search";

	protected static final String STATE_MANUAL_REFRESH = "manual";

	protected static final String STATE_GOTO_PAGE = "goto-page";

	protected static final String STATE_CURRENT_PAGE = "current-page";

	protected static final String STATE_SELECTED_VIEW = "selected_view";

	protected static final String STATE_PAGING = "paging";

	/** Form fields. */
	protected static final String FORM_SEARCH = "search";

	protected static final String FORM_PAGE_NUMBER = "page_number";

	/**
	 * Implement this to return alist of all the resources that there are to page. Sort them as appropriate, and apply search criteria.
	 */
	protected abstract List readAllResources(SessionState state);

	/**
	 * Return the total page number
	 */
	protected int totalPageNumber(SessionState state)
	{
		return ((Integer) state.getAttribute(STATE_TOTAL_PAGENUMBER)).intValue();

	} // totalPageNumber

	/**
	 * Add the menus for search.
	 */
	protected void addSearchMenus(Menu bar, SessionState state)
	{
		bar.add(new MenuField(FORM_SEARCH, "2ndToolbarForm", "doSearch", (String) state.getAttribute(STATE_SEARCH)));
		bar.add(new MenuEntry("Search", null, true, MenuItem.CHECKED_NA, "doSearch", "2ndToolbarForm"));
		if (state.getAttribute(STATE_SEARCH) != null)
		{
			bar.add(new MenuEntry("Clear Search", "doSearch_clear"));
		}

	} // addSearchMenus

	/**
	 * Populate the state object, if needed, concerning paging
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		if (state.getAttribute(STATE_PAGESIZE) == null)
		{
			PortletConfig config = portlet.getPortletConfig();

			try
			{
				Integer size = new Integer(config.getInitParameter(PARAM_PAGESIZE));
				if (size.intValue() <= 0)
				{
					size = new Integer(DEFAULT_PAGE_SIZE);
					log.debug("size parameter invalid 1: {}", config.getInitParameter(PARAM_PAGESIZE));
				}
				state.setAttribute(STATE_PAGESIZE, size);
			}
			catch (Exception any)
			{
				log.debug("size parameter invalid 2: {}", any.toString());
				state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
			}
		}

		if (state.getAttribute(STATE_CURRENT_PAGE) == null)
		{
			state.setAttribute(STATE_CURRENT_PAGE, new Integer(1));
		}

		if (state.getAttribute(STATE_PAGING) == null)
		{
			state.setAttribute(STATE_PAGING, Boolean.FALSE);
		}

		if (state.getAttribute(STATE_TOTAL_PAGENUMBER) == null)
		{
			state.setAttribute(STATE_TOTAL_PAGENUMBER, new Integer(1));
		}
	} // initState

	/**
	 * Prepare the current page of items to display.
	 * 
	 * @return List of items to display on this page.
	 */
	protected List prepPage(SessionState state)
	{
		List rv = new Vector();

		// read all items
		List allItems = readAllResources(state);

		if (allItems == null)
		{
			return rv;
		}

		// access the page size
		int pageSize = ((Integer) state.getAttribute(STATE_PAGESIZE)).intValue();

		// set the total page number
		int totalPageNumber = 1;
		int listSize = allItems.size();
		if ((listSize % pageSize) > 0)
		{
			totalPageNumber = listSize / pageSize + 1;
		}
		else
		{
			totalPageNumber = listSize / pageSize;
		}
		state.setAttribute(STATE_TOTAL_PAGENUMBER, new Integer(totalPageNumber));

		boolean paged = ((Boolean) state.getAttribute(STATE_PAGING)).booleanValue();
		if (!paged)
		{
			// no paging, return all items
			// if the total page is greater than 1, set the STATE_NEXT_PAGE_EXISTS
			if (totalPageNumber > 1)
			{
				state.setAttribute(STATE_NEXT_PAGE_EXISTS, "");
			}
			else
			{
				state.removeAttribute(STATE_NEXT_PAGE_EXISTS);
			}
			state.removeAttribute(STATE_PREV_PAGE_EXISTS);
			return allItems;
		}
		else
		{

			// cleanup prior prep
			state.removeAttribute(STATE_NUM_ITEMS);

			// are we going next or prev, first or last page?
			boolean goNextPage = state.getAttribute(STATE_GO_NEXT_PAGE) != null;
			boolean goPrevPage = state.getAttribute(STATE_GO_PREV_PAGE) != null;
			boolean goFirstPage = state.getAttribute(STATE_GO_FIRST_PAGE) != null;
			boolean goLastPage = state.getAttribute(STATE_GO_LAST_PAGE) != null;
			state.removeAttribute(STATE_GO_NEXT_PAGE);
			state.removeAttribute(STATE_GO_PREV_PAGE);
			state.removeAttribute(STATE_GO_FIRST_PAGE);
			state.removeAttribute(STATE_GO_LAST_PAGE);

			// are we going next or prev item?
			boolean goNext = state.getAttribute(STATE_GO_NEXT) != null;
			boolean goPrev = state.getAttribute(STATE_GO_PREV) != null;
			state.removeAttribute(STATE_GO_NEXT);
			state.removeAttribute(STATE_GO_PREV);

			boolean goViewPage = state.getAttribute(STATE_GOTO_PAGE) != null;

			// if we have no prev page and do have a top item, then we will stay "pined" to the top
			boolean pinToTop = ((state.getAttribute(STATE_TOP_PAGE_ITEM) != null)
					&& (state.getAttribute(STATE_PREV_PAGE_EXISTS) == null) && !goNextPage && !goPrevPage && !goNext && !goPrev
					&& !goFirstPage && !goLastPage && !goViewPage);

			// if we have no next page and do have a top item, then we will stay "pined" to the bottom
			boolean pinToBottom = ((state.getAttribute(STATE_TOP_PAGE_ITEM) != null)
					&& (state.getAttribute(STATE_NEXT_PAGE_EXISTS) == null) && !goNextPage && !goPrevPage && !goNext && !goPrev
					&& !goFirstPage && !goLastPage && !goViewPage);

			// how many items, total
			int numItems = allItems.size();

			if (numItems == 0)
			{
				return rv;
			}

			// save the number of messges
			state.setAttribute(STATE_NUM_ITEMS, new Integer(numItems));

			// find the position of the item that is the top first on the page
			int posStart = 0;
			String itemIdAtTheTopOfThePage = (String) state.getAttribute(STATE_TOP_PAGE_ITEM);
			if (itemIdAtTheTopOfThePage != null)
			{
				// find the next page
				posStart = findResourceInList(allItems, itemIdAtTheTopOfThePage);

				// if missing, start at the top
				if (posStart == -1)
				{
					posStart = 0;
				}
			}

			// if going to the next page, adjust
			if (state.getAttribute(STATE_GOTO_PAGE) != null)
			{
				int gotoPage = ((Integer) state.getAttribute(STATE_GOTO_PAGE)).intValue();
				int currentPage = ((Integer) state.getAttribute(STATE_CURRENT_PAGE)).intValue();
				posStart += pageSize * (gotoPage - currentPage);
			}
			else if (goNextPage)
			{
				posStart += pageSize;
			}

			// if going to the prev page, adjust
			else if (goPrevPage)
			{
				posStart -= pageSize;
				if (posStart < 0) posStart = 0;
			}

			// if going to the first page, adjust
			else if (goFirstPage)
			{
				posStart = 0;
			}

			// if going to the last page, adjust
			else if (goLastPage)
			{
				posStart = numItems - pageSize;
				if (posStart < 0) posStart = 0;
			}

			// pinning
			if (pinToTop)
			{
				posStart = 0;
			}
			else if (pinToBottom)
			{
				posStart = numItems - pageSize;
				if (posStart < 0) posStart = 0;
			}

			// get the last page fully displayed
			/*
			 * if (posStart + pageSize > numItems) { posStart = numItems - pageSize; if (posStart < 0) posStart = 0; }
			 */

			// compute the end to a page size, adjusted for the number of items available
			int posEnd = posStart + (pageSize - 1);
			if (posEnd >= numItems) posEnd = numItems - 1;
			int numItemsOnThisPage = (posEnd - posStart) + 1;

			// select the items on this page
			for (int i = posStart; i <= posEnd; i++)
			{
				rv.add(allItems.get(i));
			}

			// save which item is at the top of the page
			Entity itemAtTheTopOfThePage = (Entity) allItems.get(posStart);
			state.setAttribute(STATE_TOP_PAGE_ITEM, itemAtTheTopOfThePage.getId());

			// which item starts the next page (if any)
			int next = posStart + pageSize;
			if (next < numItems)
			{
				state.setAttribute(STATE_NEXT_PAGE_EXISTS, "");
			}
			else
			{
				state.removeAttribute(STATE_NEXT_PAGE_EXISTS);
			}

			// which item ends the prior page (if any)
			int prev = posStart - 1;
			if (prev >= 0)
			{
				state.setAttribute(STATE_PREV_PAGE_EXISTS, "");
			}
			else
			{
				state.removeAttribute(STATE_PREV_PAGE_EXISTS);
			}

			if (state.getAttribute(STATE_VIEW_ID) != null)
			{
				int viewPos = findResourceInList(allItems, (String) state.getAttribute(STATE_VIEW_ID));

				// are we moving to the next item
				if (goNext)
				{
					// advance
					viewPos++;
					if (viewPos >= numItems) viewPos = numItems - 1;
				}

				// are we moving to the prev item
				if (goPrev)
				{
					// retreat
					viewPos--;
					if (viewPos < 0) viewPos = 0;
				}

				// update the view item
				state.setAttribute(STATE_VIEW_ID, ((Entity) allItems.get(viewPos)).getId());

				// if the view item is no longer on the current page, adjust the page
				// Note: next time through this will get processed
				if (viewPos < posStart)
				{
					state.setAttribute(STATE_GO_PREV_PAGE, "");
				}
				else if (viewPos > posEnd)
				{
					state.setAttribute(STATE_GO_NEXT_PAGE, "");
				}

				if (viewPos > 0)
				{
					state.setAttribute(STATE_PREV_EXISTS, "");
				}
				else
				{
					state.removeAttribute(STATE_PREV_EXISTS);
				}

				if (viewPos < numItems - 1)
				{
					state.setAttribute(STATE_NEXT_EXISTS, "");
				}
				else
				{
					state.removeAttribute(STATE_NEXT_EXISTS);
				}
			}

			if (state.getAttribute(STATE_GOTO_PAGE) != null)
			{
				state.setAttribute(STATE_CURRENT_PAGE, state.getAttribute(STATE_GOTO_PAGE));
				state.removeAttribute(STATE_GOTO_PAGE);
			}
			return rv;
		}

	} // prepPage

	/**
	 * Handle a view indecated page request
	 */
	public void doView_page(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// set the flag to go to the next item on the next view
		String page = runData.getParameters().getString(FORM_PAGE_NUMBER);
		state.setAttribute(STATE_GOTO_PAGE, new Integer(page));

	} // doView_page

	/**
	 * Handle a next-item (view) request.
	 */
	public void doView_next(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// set the flag to go to the next item on the next view
		state.setAttribute(STATE_GO_NEXT, "");

		// set the page number
		int page = ((Integer) state.getAttribute(STATE_CURRENT_PAGE)).intValue();
		state.setAttribute(STATE_CURRENT_PAGE, new Integer(page + 1));

	} // doView_next

	/**
	 * Handle a first-item page (list) request.
	 */
	public void doList_first(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// set the flag to go to the next item on the next view
		state.setAttribute(STATE_GO_FIRST_PAGE, "");

		// set the page number
		state.setAttribute(STATE_CURRENT_PAGE, new Integer(1));

	} // doList_first

	/**
	 * Handle a last-item page (list) request.
	 */
	public void doList_last(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// set the flag to go to the next item on the next view
		state.setAttribute(STATE_GO_LAST_PAGE, "");

		// set the page number
		state.setAttribute(STATE_CURRENT_PAGE, new Integer(totalPageNumber(state)));

	} // doList_last

	/**
	 * Handle a next-page (list) request.
	 */
	public void doList_next(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// set the flag to go to the next page on the next list
		state.setAttribute(STATE_GO_NEXT_PAGE, "");

		// set the page number
		int page = ((Integer) state.getAttribute(STATE_CURRENT_PAGE)).intValue();
		state.setAttribute(STATE_CURRENT_PAGE, new Integer(page + 1));

		// %%% ?? doList(runData, context);

	} // doList_next

	/**
	 * Handle a prev-item (view) request.
	 */
	public void doView_prev(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// set the flag to go to the prev item on the next view
		state.setAttribute(STATE_GO_PREV, "");

		// set the page number
		int page = ((Integer) state.getAttribute(STATE_CURRENT_PAGE)).intValue();
		state.setAttribute(STATE_CURRENT_PAGE, new Integer(page - 1));

	} // doView_prev

	/**
	 * Handle a prev-page (list) request.
	 */
	public void doList_prev(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// set the flag to go to the prev page on the next list
		state.setAttribute(STATE_GO_PREV_PAGE, "");

		// set the page number
		int page = ((Integer) state.getAttribute(STATE_CURRENT_PAGE)).intValue();
		state.setAttribute(STATE_CURRENT_PAGE, new Integer(page - 1));

	} // doList_prev

	/**
	 * Handle a Search request.
	 */
	public void doSearch(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// read the search form field into the state object
		String search = StringUtils.trimToNull(runData.getParameters().getString(FORM_SEARCH));

		// set the flag to go to the prev page on the next list
		if (search == null)
		{
			state.removeAttribute(STATE_SEARCH);
		}
		else
		{
			state.setAttribute(STATE_SEARCH, search);
		}

		// start paging again from the top of the list
		resetPaging(state);

		// if we are searching, turn off auto refresh
		if (search != null)
		{
			ObservingCourier observer = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
			if (observer != null)
			{
				observer.disable();
			}
		}

		// else turn it back on
		else
		{
			enableObserver(state);
		}

	} // doSearch

	/**
	 * Handle a Search Clear request.
	 */
	public void doSearch_clear(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// clear the search
		state.removeAttribute(STATE_SEARCH);

		// start paging again from the top of the list
		resetPaging(state);

		// turn on auto refresh
		enableObserver(state);

	} // doSearch_clear

	/**
	 * Reset to the first page
	 */
	protected void resetPaging(SessionState state)
	{
		// we are changing the sort, so start from the first page again
		state.removeAttribute(STATE_TOP_PAGE_ITEM);
		state.setAttribute(STATE_CURRENT_PAGE, new Integer(1));

	} // resetPaging

	/**
	 * Find the resource with this id in the list.
	 * 
	 * @param items
	 *        The list of items.
	 * @param id
	 *        The item id.
	 * @return The index position in the list of the item with this id, or -1 if not found.
	 */
	protected int findResourceInList(List resources, String id)
	{
		for (int i = 0; i < resources.size(); i++)
		{
			// if this is the one, return this index
			if (((Entity) (resources.get(i))).getId().equals(id)) return i;
		}

		// not found
		return -1;

	} // findResourceInList

	/**
	 * Toggle auto-update
	 */
	public void doAuto(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// get the observer
		ObservingCourier observer = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		if (observer != null)
		{
			boolean enabled = observer.getEnabled();
			if (enabled)
			{
				observer.disable();
				state.setAttribute(STATE_MANUAL_REFRESH, "manual");
			}
			else
			{
				observer.enable();
				state.removeAttribute(STATE_MANUAL_REFRESH);
			}
		}

	} // doAuto

	/**
	 * The action for when the user want's an update
	 */
	public void doRefresh(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

	} // doRefresh

	/**
	 * Enable the observer, unless we are in search mode, where we want it disabled.
	 */
	public void enableObserver(SessionState state)
	{
		// get the observer
		ObservingCourier observer = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		if (observer != null)
		{
			// we leave it disabled if we are searching, or if the user has last selected to be manual
			if ((state.getAttribute(STATE_SEARCH) != null) || (state.getAttribute(STATE_MANUAL_REFRESH) != null))
			{
				observer.disable();
			}
			else
			{
				observer.enable();
			}
		}

	} // enableObserver

	/**
	 * The action for toggling paging status: show all(no paging) or paging
	 */
	public void doToggle_paging(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		Boolean paging_status = (Boolean) state.getAttribute(STATE_PAGING);
		state.setAttribute(STATE_PAGING, new Boolean(!(paging_status.booleanValue())));
		if (((Boolean) state.getAttribute(STATE_PAGING)).booleanValue())
		{
			resetPaging(state);
		}

	} // doToggle_paging

} // PagedResourceAction

