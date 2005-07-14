/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.component.app.syllabus;

// imports
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.sakaiproject.service.framework.log.cover.Logger;
import org.sakaiproject.service.legacy.resource.ResourceProperties;
import org.sakaiproject.service.legacy.resource.ResourcePropertiesEdit;
import org.w3c.dom.Element;

/**
* <p>BaseResourceProperties is the base class for ResourcePropertiesEdit implementations.</p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision: 20 $
* @see org.chefproject.core.ResourceProperties
*/
public class BaseResourcePropertiesEdit
	extends BaseResourceProperties
	implements ResourcePropertiesEdit
{
	/**
	* Construct.
	*/
	public BaseResourcePropertiesEdit()
	{
		super();

	}   // BaseResourcePropertiesEdit

	/**
	* Construct from XML.
	* @param el The XML DOM element.
	*/
	public BaseResourcePropertiesEdit(Element el)
	{
		super(el);

	}	// BaseResourcePropertiesEdit

	/**
	 * {@inheritDoc}
	 */
	public void setLazy(boolean lazy)
	{
		m_lazy = lazy;
	}

	/**
	* Add a single valued property.
	* @param name The property name.
	* @param value The property value.
	*/
	public void addProperty(String name, String value)
	{
		// protect against a null put
		if (value == null) value = "";

		m_props.put(name, value);

	}   // addProperty

	/**
	* Add a value to a multi-valued property.
	* @param name The property name.
	* @param value The property value.
	*/
	public void addPropertyToList(String name, String value)
	{
		// protect against a null put
		if (value == null) value = "";

		// accumulate multiple values in a list
		Object current = m_props.get(name);
		
		// if we don't have a value yet, make a list to hold this one
		if (current == null)
		{
			List values = new Vector();
			m_props.put(name, values);
			values.add(value);
		}
		
		// if we do and it's a list, add this one
		else if (current instanceof List)
		{
			((List) current).add(value);
		}
		
		// if it's not a list, it's wrong!
		else
		{
			Logger.warn(this + "addPropertyToList() value set not a list: " + name);
		}

	}	// addPropertyToList

	/**
	* Add all the properties from the other ResourceProperties object.
	* @param other The ResourceProperties to add.
	*/
	public void addAll(ResourceProperties other)
	{
		// if there's a list, it must be deep copied
		for (Iterator iNames = other.getPropertyNames(); iNames.hasNext();)
		{
			String name = (String) iNames.next();
			Object value = ((BaseResourceProperties) other).m_props.get(name);
			if (value instanceof List)
			{
				List list = new Vector();
				list.addAll((List) value);
				m_props.put(name, list);
			}
			else
			{
				m_props.put(name, value);
			}
		}

	}	// addAll

	/**
	* Add all the properties from the Properties object.
	* @param props The Properties to add.
	*/
	public void addAll(Properties props)
	{
		// if there's a list, it must be deep copied
		for (Enumeration e = props.propertyNames(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			Object value = props.get(name);
			if (value instanceof List)
			{
				List list = new Vector();
				list.addAll((List) value);
				m_props.put(name, list);
			}
			else
			{
				m_props.put(name, value);
			}
		}

	}	// addAll

	/**
	* Remove all properties.
	*/
	public void clear()
	{
		m_props.clear();

	}	// clear

	/**
	* Remove a property.
	* @param name The property name.
	*/
	public void removeProperty(String name)
	{
		m_props.remove(name);

	}   // removeProperty

	/**
	* Take all values from this object.
	* @param user The ResourceProperties object to take values from.
	*/
	public void set(ResourceProperties props)
	{
		clear();
		addAll(props);

	}	// set

}   // BaseResourcePropertiesEdit



