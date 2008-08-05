/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.util.Comparator;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * ContentHostingComparator can be used to sort stuff (collections, resources) from the content hosting service.
 * </p>
 */
public class ContentHostingComparator implements Comparator
{
	/** The property name used for the sort. */
	String m_property = null;

	/** true if the sort is to be ascending (false for descending). */
	boolean m_ascending = true;

	/**
	 * Construct.
	 * 
	 * @param property
	 *        The property name used for the sort.
	 * @param asc
	 *        true if the sort is to be ascending (false for descending).
	 */
	public ContentHostingComparator(String property, boolean ascending)
	{
		m_property = property;
		m_ascending = ascending;

	} // ContentHostingComparator

	/**
	 * Compare these objects based on my property and ascending settings. Collections sort lower than Resources.
	 * 
	 * @param o1
	 *        The first object, ContentCollection or ContentResource
	 * @param o2
	 *        The second object, ContentCollection or ContentResource
	 * @return The compare result: -1 if o1 < o2, 0 if they are equal, and 1 if o1 > o2
	 */
	public int compare(Object o1, Object o2)
	{
		String property = m_property;
		
		// PROP_CONTENT_PRIORITY is special because it allows 
		// intermixing folders and files. 
		if(property.equals(ResourceProperties.PROP_CONTENT_PRIORITY))
		{
			Entity entity1 = (Entity) o1;
			Entity entity2 = (Entity) o2;
			String str1 = entity1.getProperties().getProperty(property);
			String str2 = entity2.getProperties().getProperty(property);

			if(str1 == null || str2 == null)
			{
				// ignore -- default to a different sort
			}
			else
			{
				try
				{
					Integer rank1 = new Integer(str1);
					Integer rank2 = new Integer(str2);
					return m_ascending ? rank1.compareTo(rank2) : rank2.compareTo(rank1) ;
				}
				catch(NumberFormatException e)
				{
					// ignore -- default to a different sort
				}
			}
			// if unable to do a priority sort, sort by title
			property = ResourceProperties.PROP_DISPLAY_NAME;
		}
		
		// collections sort lower than resources
		if ((o1 instanceof ContentCollection) && (o2 instanceof ContentResource))
		{
			return (m_ascending ? -1 : 1);
		}
		if ((o1 instanceof ContentResource) && (o2 instanceof ContentCollection))
		{
			return (m_ascending ? 1 : -1);
		}
		
		if(property.equals(ResourceProperties.PROP_CONTENT_LENGTH) && o1 instanceof ContentCollection)
		{
			int size1 = ((ContentCollection) o1).getMemberCount();
			int size2 = ((ContentCollection) o2).getMemberCount();
			int rv = ((size1 < size2) ? -1 : ((size1 > size2) ? 1 : 0));
			if (!m_ascending) rv = -rv;
			return rv;
		}

		// ok, they are both the same: resources or collections

		// try a numeric interpretation
		try
		{
			long l1 = ((Entity) o1).getProperties().getLongProperty(property);
			long l2 = ((Entity) o2).getProperties().getLongProperty(property);
			int rv = ((l1 < l2) ? -1 : ((l1 > l2) ? 1 : 0));
			if (!m_ascending) rv = -rv;
			return rv;
		}
		catch (Exception ignore)
		{
		}

		// try a Time interpretation
		try
		{
			Time t1 = ((Entity) o1).getProperties().getTimeProperty(property);
			Time t2 = ((Entity) o2).getProperties().getTimeProperty(property);
			int rv = t1.compareTo(t2);
			if (!m_ascending) rv = -rv;
			return rv;
		}
		catch (Exception ignore)
		{
		}

		// do a formatted interpretation - case insensitive
		if (o1 == null) return -1;
		if (o2 == null) return +1;
		String s1 = ((Entity) o1).getProperties().getPropertyFormatted(property);
		String s2 = ((Entity) o2).getProperties().getPropertyFormatted(property);
		int rv = s1.compareToIgnoreCase(s2);
		if (!m_ascending) rv = -rv;
		return rv;

	} // compare
	
	public String toString()
	{
		return this.getClass().getName() + ": property(" + m_property  + ") ascending(" + m_ascending + ")";
		
	}

} // ClassResourcesComparator

