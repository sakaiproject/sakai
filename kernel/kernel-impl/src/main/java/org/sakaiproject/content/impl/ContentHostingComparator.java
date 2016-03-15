/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.math.BigInteger;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.sakaiproject.content.api.ContentCollection;
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
	 * true if we're using a sort which accounts for "mixed mode" names, like
	 * 'slide1', 'slide2, ..., 'slide10', 'slide11', which would not sort as
	 * expected under a strict lexicographical sort.
	 */
	boolean m_smart_sort = true;
	
	/**
	 * That object is used to allow locale-sensitive ordering. 
	 */
	private static final Collator collator = Collator.getInstance();
	
	
	
	public ContentHostingComparator(String property, boolean  ascending) {
		this(property, ascending, true);
	}
	/**
	 * Construct.
	 * 
	 * @param property
	 *        The property name used for the sort.
	 * @param ascending
	 *        true if the sort is to be ascending (false for descending).
	 */
	public ContentHostingComparator(String property, boolean ascending, boolean is_smart) {
		m_property = property;
		m_ascending = ascending;
		m_smart_sort = is_smart;
		
		//sets the collator properties
		 collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		 collator.setStrength(Collator.PRIMARY);

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
			Integer rank1 = null;
			Integer rank2 = null;

			try {
				rank1 = new Integer(entity1.getProperties().getProperty(property));
			} catch (NumberFormatException e) { /* ignore, rank1 will be null */ }

			try {
				rank2 = new Integer(entity2.getProperties().getProperty(property));
			} catch (NumberFormatException e) { /* ignore, rank2 will be null */ }

			// Priorities can be null if a resource or collection was created before priorities were
			// introduced. Sort null priorities to the bottom.
			if (rank1 != null && rank2 != null) {
				return m_ascending ? rank1.compareTo(rank2) : rank2.compareTo(rank1);
			}

			if (rank1 != null) { return m_ascending ? -1 : 1; }
			if (rank2 != null) { return m_ascending ? 1 : -1; }
			
			// If priorities are null for both items, fall back to display name sort.
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
		int rv = 0;
		if (m_smart_sort) {
			rv = compareLikeMacFinder( 
					((Entity) o1).getProperties().getPropertyFormatted(property),
					((Entity) o2).getProperties().getPropertyFormatted(property));
		}
		else {
			rv = ((Entity) o1).getProperties().getPropertyFormatted(property).compareTo
				(((Entity) o2).getProperties().getPropertyFormatted(property));
		}
		return m_ascending ? rv : -rv;
	} // compare
	
	
	public int comparerLocalSensitive(String s1, String s2) {
		Collator c = Collator.getInstance();
		c.setStrength(Collator.PRIMARY);
		return c.compare(s1, s2);
	}
	
	/**  
	 * this is public to enable testing??
	 * @param s1
	 * @param s2
	 * @return
	 */
	public int compareLikeMacFinder(String s1, String s2) {
		if (! (containsDigits(s1) || containsDigits(s2))) {
			return collator.compare(s1,s2);
		}
		
		/*
		 * the strategy here is simple, but certainly not the fastest approach.  Each string is
		 * split into a number of components, each component being either a String or a
		 * BigInteger.  Then we iterate over the components.  As we do this, we must be careful
		 * to call compareTo only on "compatible" types, which here is defined in the simplest
		 * way: identity.  If the types aren't compatible, we toString() the components and
		 * compare as Strings.
		 * 
		 */
		Comparable[] c1 = makeGroups(s1);
		Comparable[] c2 = makeGroups(s2);
	
		int i = 0;
		while (i < c1.length) {
			if (i >= c2.length) {
				return 1;
			}
			int v = 0;
			if (c1[i].getClass().getName().equals("java.lang.String") && c2[i].getClass().getName().equals("java.lang.String")) {
				v = collator.compare(c1[i].toString(),c2[i].toString());
			} else if (c1[i].getClass().equals(c2[i].getClass())) {
				v = c1[i].compareTo(c2[i]);
			}
			else {
				v = collator.compare(c1[i].toString(),c2[i].toString());
			}
			if (v != 0) {
				return v;
			}
			i++;
		}
		// here, c2 must be at least as long as c1
		return (c2.length > c1.length) ? -1 : 0;
	}
	
	private boolean containsDigits(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (Character.isDigit(s.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	private Comparable[] makeGroups(String s) {
		List<Comparable> l = new ArrayList<Comparable>();
		boolean isNumber = false;
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (sb.length() > 0) {
				char last = sb.charAt(sb.length()-1);
				if ((Character.isDigit(c) && Character.isDigit(last)) ||
						(!Character.isDigit(c) && !Character.isDigit(last))) {
				}
				else {
					if (isNumber) {
						l.add(new BigInteger(sb.toString()));
					}
					else {
						l.add(sb.toString());
					}
					sb.setLength(0);
				}
			}
			sb.append(c);
			isNumber = Character.isDigit(c);
		}
		if (sb.length() > 0) {
			if (isNumber) {
				l.add(new BigInteger(sb.toString()));
			}
			else {
				l.add(sb.toString());
			}
		}
		Comparable[] cs = new Comparable[l.size()];
		return l.toArray(cs);
	}
	
	public String toString()
	{
		return this.getClass().getName() + ": property(" + m_property  + ") ascending(" + m_ascending + ")";
		
	}

} // ClassResourcesComparator

