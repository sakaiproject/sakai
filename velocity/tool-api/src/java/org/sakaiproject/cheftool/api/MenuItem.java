/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 2006, 2006, 2008 The Sakai Foundation
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

import java.util.List;

/**
 * <p>
 * MenuItem is the interface for all the objects that can live on a menu.
 * </p>
 */
public interface MenuItem
{
	/** Checked status values. */
	final static int CHECKED_NA = 0;

	final static int CHECKED_FALSE = 1;

	final static int CHECKED_TRUE = 2;

	final static String STATE_MENU = "menu";

	/**
	 * Does this item act as a container for other items?
	 * 
	 * @return true if this MenuItem is a container for other items, false if not.
	 */
	boolean getIsContainer();

	/**
	 * Is this item a divider ?
	 * 
	 * @return true if this MenuItem is a divider, false if not.
	 */
	boolean getIsDivider();

	/**
	 * Access the display title for the item.
	 * 
	 * @return The display title for the item.
	 */
	String getTitle();

	/**
	 * Access the icon name for the item (or null if no icon).
	 * 
	 * @return The icon name for the item (or null if no icon).
	 */
	String getIcon();

	/**
	 * Access the enabled flag for the item.
	 * 
	 * @return True if the item is enabled, false if not.
	 */
	boolean getIsEnabled();

	/**
	 * Access the action string for this item; what to do when the user clicks. Note: if getIsMenu(), there will not be an action string (will return "").
	 * 
	 * @return The action string for this item.
	 */
	String getAction();

	/**
	 * Access the full URL string for this item; what to do when the user clicks. Note: this if defined overrides getAction() which should be "". Note: if getIsMenu(), there will not be a URL string (will return "").
	 * 
	 * @return The full URL string for this item.
	 */
	String getUrl();

	/**
	 * Access the form name whose values will be used when this item is selected.
	 * 
	 * @return The form name whose values will be used when this item is selected, or null if there is none.
	 */
	String getForm();

	/**
	 * Access the checked status of this item. Possible values are (see above) CHECKED_NA, CHECKED_FALSE, CHECKED_TRUE
	 * 
	 * @return The the checked status of this item.
	 */
	int getChecked();

	/**
	 * Access the sub-items of the item. Note: if !isContainer(), there will be no sub-items (will return EmptyIterator).
	 * 
	 * @return The sub-items of the item.
	 */
	List<MenuItem> getItems();

	/**
	 * Count the sub-items of the item. Note: if !isContainer(), the count is 0.
	 * 
	 * @return The count of sub-items of the item.
	 */
	int size();

	/**
	 * Check if there are any sub-items. Note: if !isContainer(), this is empty.
	 * 
	 * @return true of there are no sub-items, false if there are.
	 */
	boolean isEmpty();

	/**
	 * Access one sub-items of the item. Note: if !isContainer(), there will be no sub-items (will return null).
	 * 
	 * @param index
	 *        The index position (0 based) for the sub-item to get.
	 * @return The sub-item of the item.
	 */
	MenuItem getItem(int index);

	/**
	 * Access the is-field (not a button) flag.
	 * 
	 * @return True if the item is a field, false if not.
	 */
	boolean getIsField();

	/**
	 * Access the is-current flag.
	 * 
	 * @return True if the item is the currently selected one, false if not.
	 */
	boolean getIsCurrent();
	
	/**
	 * Get the optional label that may be set by components to provide accessiblity information, eg alt tags, title attributes etc
	 * @return the value
	 */
	String getAccessibilityLabel();
}
