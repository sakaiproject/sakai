/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.messageforums;

import java.util.Comparator;

import org.sakaiproject.site.api.Group;

public class GroupComparator implements Comparator {

	private String m_property = null;

	private boolean m_ascending = true;

	/**
	 * Construct.
	 * 
	 * @param property
	 *            The property name used for the sort.
	 * @param asc
	 *            true if the sort is to be ascending (false for descending).
	 */
	public GroupComparator(String property, boolean ascending) {
		m_property = property;
		m_ascending = ascending;

	} // GroupsComparator

	public int compare(Object o1, Object o2) {
		int rv = 0;

		String t1 = ((Group) o1).getTitle();
		String t2 = ((Group) o2).getTitle();
			
		rv = t1.compareTo(t2);

		if (!m_ascending)
			rv = -rv;

		return rv;
	}
}
