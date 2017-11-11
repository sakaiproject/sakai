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

package org.sakaiproject.cheftool.menu;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;

/**
 * <p>
 * Menu is an ordered list of MenuItems.
 * </p>
 */
public class MenuImpl implements Menu
{
	/** The menu's title. */
	protected String m_title = null;

	/** The menu's icon. */
	protected String m_icon = null;

	/** The menu's enabled flag. */
	protected boolean m_enabled = true;

	/** The MenuItems, in order. */
	protected List<MenuItem> m_items = new ArrayList<>();

	/** The form name string for the entry. */
	protected String m_form = null;

	/** The base url for any action taken after clicking on the menu. */
	protected String m_linkBaseUrl = null;

	/** The base url for any resources (images, etc.) required by the menu. */
	protected String m_resourceBaseUrl = null;

	/** The menu's flag for whether to display disabled entries */
	protected boolean m_showDisabled = false;
	
	// CHEF 1.x support
	public final static String CONTEXT_ACTION = "action";

	public final static String CONTEXT_MENU = "menu";

	public final static String STATE_MENU = "menu";

	public MenuImpl(VelocityPortlet p, RunData r, String action)
	{
		super();
	}

	// CHEF 1.x support

	/**
	 * Construct a menu (good for sub-menus).
	 */
	public MenuImpl(String title, String icon, boolean enabled)
	{
		m_title = title;
		m_icon = icon;
		m_enabled = enabled;

	} // Menu

	/**
	 * Construct a menu (good for sub-menus).
	 */
	public MenuImpl(String title, String icon, boolean enabled, String form)
	{
		m_title = title;
		m_icon = icon;
		m_enabled = enabled;
		m_form = form;

	} // Menu

	/**
	 * Construct a menu (good for a menu bar).
	 */
	public MenuImpl()
	{
	} // Menu

	/**
	 * Add a menu item to the bar.
	 * 
	 * @param entry
	 *        The menu item to add.
	 * @return the item.
	 */
	public MenuItem add(MenuItem item)
	{
		m_items.add(item);

		return item;

	} // add

	/**
	 * Clear the menu of all items.
	 */
	public void clear()
	{
		m_items.clear();

	} // clear

	/**
	 * Does this item act as a container for other items?
	 * 
	 * @return true if this MenuItem is a container for other items, false if not.
	 */
	public boolean getIsContainer()
	{
		return true;

	} // getIsContainer

	/**
	 * Is this item a divider ?
	 * 
	 * @return true if this MenuItem is a divider, false if not.
	 */
	public boolean getIsDivider()
	{
		return false;

	} // getIsDivider

	/**
	 * Access the display title for the item.
	 * 
	 * @return The display title for the item.
	 */
	public String getTitle()
	{
		return ((m_title == null) ? "" : m_title);

	} // getTitle

	/**
	 * Access the icon name for the item (or null if no icon).
	 * 
	 * @return The icon name for the item (or null if no icon).
	 */
	public String getIcon()
	{
		return m_icon;

	} // getIcon

	/**
	 * Access the enabled flag for the item.
	 * 
	 * @return True if the item is enabled, false if not.
	 */
	public boolean getIsEnabled()
	{
		return m_enabled;

	} // getIsEnabled

	/**
	 * Access the action string for this item; what to do when the user clicks. Note: if getIsMenu(), there will not be an action string (will return "").
	 * 
	 * @return The action string for this item.
	 */
	public String getAction()
	{
		return "";

	} // getAction

	/**
	 * Access the full URL string for this item; what to do when the user clicks. Note: this if defined overrides getAction() which should be "". Note: if getIsMenu(), there will not be a URL string (will return "").
	 * 
	 * @return The full URL string for this item.
	 */
	public String getUrl()
	{
		return "";

	} // getUrl

	/**
	 * Access the form name whose values will be used when this item is selected.
	 * 
	 * @return The form name whose values will be used when this item is selected.
	 */
	public String getForm()
	{
		return m_form;

	} // getForm

	/**
	 * Access the sub-items of the item. Note: if !isContainer(), there will be no sub-items (will return EmptyIterator).
	 * 
	 * @return The sub-items of the item.
	 */
	public List<MenuItem> getItems()
	{
		return m_items;

	} // getItems

	/**
	 * Count the sub-items of the item. Note: if !isContainer(), the count is 0.
	 * 
	 * @return The count of sub-items of the item.
	 */
	public int size()
	{
		return m_items.size();

	} // size

	/**
	 * Check if there are any sub-items. Note: if !isContainer(), this is empty.
	 * 
	 * @return true of there are no sub-items, false if there are.
	 */
	public boolean isEmpty()
	{
		return m_items.isEmpty();

	} // isEmpty

	/**
	 * Access one sub-items of the item. Note: if !isContainer(), there will be no sub-items (will return null).
	 * 
	 * @param index
	 *        The index position (0 based) for the sub-item to get.
	 * @return The sub-item of the item.
	 */
	public MenuItem getItem(int index)
	{
		try
		{
			return (MenuItem) m_items.get(index);
		}
		catch (Exception e)
		{
			return null;
		}

	} // getItems

	/**
	 * Access the checked status of this item. Possible values:
	 * 
	 * @see MenuItem
	 * @return The the checked status of this item.
	 */
	public int getChecked()
	{
		return CHECKED_NA;

	} // getChecked

	/**
	 * Access the is-field (not a button) flag.
	 * 
	 * @return True if the item is a field, false if not.
	 */
	public boolean getIsField()
	{
		return false;

	} // getIsField

	public boolean getIsCurrent()
	{
		return false;
	}

	/**
	 * Adjust by removing any dividers at the start or end.
	 */
	public void adjustDividers()
	{
		// trim leading dividers
		while ((m_items.size() > 0) && (m_items.get(0) instanceof MenuDivider))
		{
			m_items.remove(0);
		}

		// trim trailing dividers
		while ((m_items.size() > 0) && (m_items.get(m_items.size() - 1) instanceof MenuDivider))
		{
			m_items.remove(m_items.size() - 1);
		}

	} // adjustDividers

	/**
	 * Set whether disabled items in this menu should be shown.
	 * 
	 * @param value
	 *        True to show disabled items, False otherwise.
	 * @return This, for convenience.
	 */
	public Menu setShowdisabled(boolean value)
	{
		m_showDisabled = value;
		return this;

	} // setShowdisabled

	/**
	 * Access whether disabled items in this menu should be shown.
	 * 
	 * @return Current setting for show-disabled status (true to show disabled items, false to NOT show disabled items).
	 */
	public boolean getShowdisabled()
	{
		return m_showDisabled;

	} // getShowdisabled
	
	@Override
	public String getAccessibilityLabel() {
		//not currently used in this class
		return null;
	}

} // class Menu

