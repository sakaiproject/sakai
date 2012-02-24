/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.cheftool.api;

/**
 * <p>
 * Menu is an interface for an ordered list of MenuItems.
 * </p>
 */
public interface Menu extends MenuItem
{
	// CHEF 1.x support
	public final static String CONTEXT_ACTION = "action";

	public final static String CONTEXT_MENU = "menu";

	public final static String STATE_MENU = "menu";

	/**
	 * Add a menu item to the bar.
	 * 
	 * @param entry
	 *        The menu item to add.
	 * @return the item.
	 */
	MenuItem add(MenuItem item);

	/**
	 * Clear the menu of all items.
	 */
	void clear();

	/**
	 * Adjust by removing any dividers at the start or end.
	 */
	void adjustDividers();

	/**
	 * Set whether disabled items in this menu should be shown.
	 * 
	 * @param value
	 *        True to show disabled items, False otherwise.
	 * @return This, for convenience.
	 */
	Menu setShowdisabled(boolean value);

	/**
	 * Access whether disabled items in this menu should be shown.
	 * 
	 * @return Current setting for show-disabled status (true to show disabled items, false to NOT show disabled items).
	 */
	boolean getShowdisabled();
}
