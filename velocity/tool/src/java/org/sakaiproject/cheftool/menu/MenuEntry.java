/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

// package
package org.sakaiproject.cheftool.menu;

// imports
import java.util.List;
import java.util.Vector;

import org.sakaiproject.cheftool.api.MenuItem;

/**
* <p>MenuEntry is a clickable entry in a menu. </p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
* @see org.chefproject.util.MenuItem
*/
public class MenuEntry implements MenuItem
{
	/** The display title for the entry. */
	protected String m_title = null;

	/** The icon name for the entry. */
	protected String m_icon = null;

	/** The enabled flag for the entry. */
	protected boolean m_enabled = true;

	/** The action string for the entry. */
	protected String m_action = null;

	/** The full URL string for the entry. */
	protected String m_url = null;

	/** The form name string for the entry. */
	protected String m_form = null;

	/** The checked status (@see MenuItem for values). */
	protected int m_checked = CHECKED_NA;

	/**
	* Construct a menu.
	*/
	public MenuEntry(
		String title,
		String icon,
		boolean enabled,
		int checked,
		String action,
		String form)
	{
		m_title = title;
		m_icon = icon;
		m_enabled = enabled;
		m_checked = checked;
		m_action = action;
		m_form = form;

	} // MenuEntry

	/**
	* Construct a menu.
	*/
	public MenuEntry(
		String title,
		String icon,
		boolean enabled,
		int checked,
		String action)
	{
		m_title = title;
		m_icon = icon;
		m_enabled = enabled;
		m_checked = checked;
		m_action = action;

	} // MenuEntry

	/**
	* Construct a menu.
	*/
	public MenuEntry(String title, boolean enabled, String action)
	{
		m_title = title;
		m_enabled = enabled;
		m_action = action;

	} // MenuEntry

	/**
	* Construct a menu.
	*/
	public MenuEntry(String title, String action)
	{
		m_title = title;
		m_action = action;

	} // MenuEntry

	/**
	* Set the full URL of the entry.  To create an entry with a URL, create one first with a "" action,
	* then call this.
	* @param url The full URL for the entry.
	* @return This, for convenience.
	*/
	public MenuEntry setUrl(String url)
	{
		m_url = url;
		return this;

	} // setUrl

	/**
	* Does this item act as a container for other items?
	* @return true if this MenuItem is a container for other items, false if not.
	*/
	public boolean getIsContainer()
	{
		return false;

	} // getIsContainer

	/**
	* Is this item a divider ?
	* @return true if this MenuItem is a divider, false if not.
	*/
	public boolean getIsDivider()
	{
		return false;

	} // getIsDivider

	/**
	* Access the display title for the item.
	* @return The display title for the item.
	*/
	public String getTitle()
	{
		return ((m_title == null) ? "" : m_title);

	} // getTitle

	/**
	* Access the icon name for the item (or null if no icon).
	* @return The icon name for the item (or null if no icon).
	*/
	public String getIcon()
	{
		return m_icon;

	} // getIcon

	/**
	* Access the enabled flag for the item.
	* @return True if the item is enabled, false if not.
	*/
	public boolean getIsEnabled()
	{
		return m_enabled;

	} // getIsEnabled

	/**
	* Access the action string for this item; what to do when the user clicks.
	* Note: if getIsMenu(), there will not be an action string (will return "").
	* Note: if the entry is not enabled, this will return "".
	* @return The action string for this item.
	*/
	public String getAction()
	{
		return (((m_action == null) || (!m_enabled)) ? "" : m_action);

	} // getAction

	/**
	* Access the full URL string for this item; what to do when the user clicks.
	* Note: this if defined overrides getAction() which should be "".
	* Note: if getIsMenu(), there will not be a  URL string (will return "").
	* @return The full URL string for this item.
	*/
	public String getUrl()
	{
		return (((m_url == null) || (!m_enabled)) ? "" : m_url);

	} // getUrl

	/**
	* Access the form name whose values will be used when this item is selected.
	* @return The form name whose values will be used when this item is selected.
	*/
	public String getForm()
	{
		return m_form;

	} // getForm

	/**
	* Access the sub-items of the item.
	* Note: if !isContainer(), there will be no sub-items (will return EmptyIterator).
	* @return The sub-items of the item.
	*/
	public List getItems()
	{
		return new Vector();

	} // getItems

	/**
	* Access one sub-items of the item.
	* Note: if !isContainer(), there will be no sub-items (will return null).
	* @param index The index position (0 based) for the sub-item to get.
	* @return The sub-item of the item.
	*/
	public MenuItem getItem(int index)
	{
		return null;

	} // getItem

	/**
	* Access the checked status of this item.
	* Possible values: @see MenuItem
	* @return The checked status of this item.
	*/
	public int getChecked()
	{
		return m_checked;

	} // getChecked

	/**
	* Access the checked status of this item.
	* @return True if item is checked, false otherwise.
	*/
	public boolean getIschecked()
	{
		return m_checked == CHECKED_TRUE;

	} // getIsChecked

	/**
	* Count the sub-items of the item.
	* Note: if !isContainer(), the count is 0.
	* @return The count of sub-items of the item.
	*/
	public int size()
	{
		return 0;

	} // size

	/**
	* Check if there are any sub-items.
	* Note: if !isContainer(), this is empty.
	* @return true of there are no sub-items, false if there are.
	*/
	public boolean isEmpty()
	{
		return true;

	} // isEmpty

	/**
	* Access the is-field (not a button) flag.
	* @return True if the item is a field, false if not.
	*/
	public boolean getIsField()
	{
		return false;

	} // getIsField

} // MenuEntry



