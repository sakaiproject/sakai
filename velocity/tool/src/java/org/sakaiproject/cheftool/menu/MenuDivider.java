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


import org.sakaiproject.cheftool.api.MenuItem;

/**
 * <p>
 * MenuDivider is a menu item that makes a visible divider in the menu.
 * </p>
 */
public class MenuDivider implements MenuItem
{
	/**
	 * Construct a menu divider.
	 */
	public MenuDivider()
	{
	} // MenuDivider

	/**
	 * Does this item act as a container for other items?
	 * 
	 * @return true if this MenuItem is a container for other items, false if not.
	 */
	public boolean getIsContainer()
	{
		return false;

	} // getIsContainer

	/**
	 * Is this item a divider ?
	 * 
	 * @return true if this MenuItem is a divider, false if not.
	 */
	public boolean getIsDivider()
	{
		return true;

	} // getIsDivider

	/**
	 * Access the display title for the item.
	 * 
	 * @return The display title for the item.
	 */
	public String getTitle()
	{
		return "-";

	} // getTitle

	/**
	 * Access the icon name for the item (or null if no icon).
	 * 
	 * @return The icon name for the item (or null if no icon).
	 */
	public String getIcon()
	{
		return null;

	} // getIcon

	/**
	 * Access the enabled flag for the item.
	 * 
	 * @return True if the item is enabled, false if not.
	 */
	public boolean getIsEnabled()
	{
		return true;

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
		return null;

	} // getForm

	/**
	 * Access the sub-items of the item. Note: if !isContainer(), there will be no sub-items (will return EmptyIterator).
	 * 
	 * @return The sub-items of the item.
	 */
	public List<MenuItem> getItems()
	{
		return new ArrayList<>();

	} // getItems

	/**
	 * Access one sub-items of the item. Note: if !isContainer(), there will be no sub-items (will return null).
	 * 
	 * @param index
	 *        The index position (0 based) for the sub-item to get.
	 * @return The sub-item of the item.
	 */
	public MenuItem getItem(int index)
	{
		return null;

	} // getItem

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
	 * Count the sub-items of the item. Note: if !isContainer(), the count is 0.
	 * 
	 * @return The count of sub-items of the item.
	 */
	public int size()
	{
		return 0;

	} // size

	/**
	 * Check if there are any sub-items. Note: if !isContainer(), this is empty.
	 * 
	 * @return true of there are no sub-items, false if there are.
	 */
	public boolean isEmpty()
	{
		return true;

	} // isEmpty

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
	
	@Override
	public String getAccessibilityLabel() {
		//not currently used in this class
		return null;
	}

} // MenuDivider

