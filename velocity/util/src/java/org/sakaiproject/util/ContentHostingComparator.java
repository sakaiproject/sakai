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
package org.sakaiproject.util;

// imports
import java.util.Comparator;

import org.sakaiproject.service.legacy.content.ContentCollection;
import org.sakaiproject.service.legacy.content.ContentResource;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.time.Time;

/**
* <p>ContentHostingComparator can be used to sort stuff (collections, resources) from the content hosting service.</p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class ContentHostingComparator
	implements Comparator
{
	/** The property name used for the sort. */
	String m_property = null;
	
	/** true if the sort is to be ascending (false for descending). */
	boolean m_ascending = true;
	
	/**
	 * Construct.
	 * @param property The property name used for the sort.
	 * @param asc true if the sort is to be ascending (false for descending).
	 */
	public ContentHostingComparator (String property, boolean ascending)
	{
		m_property = property;
		m_ascending = ascending;
		
	}	// ContentHostingComparator
	
	/**
	* Compare these objects based on my property and ascending settings.
	* Collections sort lower than Resources.
	* @param o1 The first object, ContentCollection or ContentResource
	* @param o2 The second object,  ContentCollection or ContentResource
	* @return The compare result: -1 if o1 < o2, 0 if they are equal, and 1 if o1 > o2
	*/
	public int compare(Object o1, Object o2)
	{
		// collections sort lower than resources
		if ((o1 instanceof ContentCollection) && (o2 instanceof ContentResource))
		{
			return (m_ascending ? -1 : 1);
		}
		if ((o1 instanceof ContentResource) && (o2 instanceof ContentCollection))
		{
			return (m_ascending ? 1 : -1);
		}

		// ok, they are both the same: resources or collections

		// try a numeric interpretation
		try
		{
			long l1 = ((Entity)o1).getProperties().getLongProperty(m_property);
			long l2 = ((Entity)o2).getProperties().getLongProperty(m_property);
			int rv = ((l1 < l2) ? -1 : ((l1 > l2) ? 1 : 0));
			if (!m_ascending) rv = -rv;
			return rv;
		}
		catch (Exception ignore) {}

		// try a Time interpretation
		try
		{
			Time t1 = ((Entity)o1).getProperties().getTimeProperty(m_property);
			Time t2 = ((Entity)o2).getProperties().getTimeProperty(m_property);
			int rv = t1.compareTo(t2);
			if (!m_ascending) rv = -rv;
			return rv;
		}
		catch (Exception ignore) {}

		// do a formatted interpretation - case insensitive
		String s1 = ((Entity)o1).getProperties().getPropertyFormatted(m_property);
		String s2 = ((Entity)o2).getProperties().getPropertyFormatted(m_property);
		int rv = s1.compareToIgnoreCase(s2);
		if (!m_ascending) rv = -rv;
		return rv;
		
	}	// compare
	
}	// ClassResourcesComparator



