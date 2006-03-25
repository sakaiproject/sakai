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
* <p>MenuField is a text entry field in a menu / toolbar.</p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class MenuField implements MenuItem
{
	/** The form field name. */
	protected String m_name = null;

	/** The form name string for the entry. */
	protected String m_form = null;

	/** The value to display in the field. */
	protected String m_value = null;

	/** The action to perform. */
	protected String m_action = null;

	/**
	* Construct.
	*/
	public MenuField(String name, String form, String action, String value)
	{
		m_name = name;
		m_form = form;
		m_action = action;
		m_value = value;

	} // MenuField

	/**
	* Set the full URL of the entry.  To create an entry with a URL, create one first with a "" action,
	* then call this.
	* @param url The full URL for the entry.
	* @return This, for convenience.
	*/
	public MenuField setUrl(String url)
	{
		return this;
	}

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
	* Access the display title for the item - this is really the form field name.
	* @return The display title for the item - but really, the form field name.
	*/
	public String getTitle()
	{
		return ((m_name == null) ? "" : m_name);

	} // getTitle

	/**
	* Access the icon name for the item (or null if no icon).
	* @return The icon name for the item (or null if no icon).
	*/
	public String getIcon()
	{
		return null;
	}

	/**
	* Access the enabled flag for the item.
	* @return True if the item is enabled, false if not.
	*/
	public boolean getIsEnabled()
	{
		return true;
	}

	/**
	* Access the value string for this item - the initial value.
	* @return The value string for this item - the initial value.
	*/
	public String getValue()
	{
		return ((m_value == null) ? "" : m_value);

	} // getValue

	/**
	* Access the action string for this item; what to do when the user clicks.
	* Note: if getIsMenu(), there will not be an action string (will return "").
	* Note: if the entry is not enabled, this will return "".
	* @return The action string for this item.
	*/
	public String getAction()
	{
		return ((m_action == null) ? "" : m_action);

	} // getAction

	/**
	* Access the full URL string for this item; what to do when the user clicks.
	* Note: this if defined overrides getAction() which should be "".
	* Note: if getIsMenu(), there will not be a  URL string (will return "").
	* @return The full URL string for this item.
	*/
	public String getUrl()
	{
		return "";
	}

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
	* @return The the checked status of this item.
	*/
	public int getChecked()
	{
		return CHECKED_NA;

	} // getChecked

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
		return true;

	} // getIsField

} // MenuField



