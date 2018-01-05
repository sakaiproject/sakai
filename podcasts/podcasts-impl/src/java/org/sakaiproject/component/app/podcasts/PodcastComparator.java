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


package org.sakaiproject.component.app.podcasts;

import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.time.api.Time;

@Slf4j
public class PodcastComparator implements Comparator {

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
	public PodcastComparator(String property, boolean ascending) {
		m_property = property;
		m_ascending = ascending;

	} // PodcastComparator

	/**
	 * First check if release dates set. May not be due to old podcasts
	 * or if hidden property set. if release date not set, get the
	 * custom DISPLAY_DATE property. 
	 */
	public int compare(Object o1, Object o2) {
		int rv = 0;

		try {
			Time t1 = ((ContentResource) o1).getReleaseDate();
			if (t1 == null)	 t1 = ((ContentResource) o1).getProperties().getTimeProperty(m_property);
			
			Time t2 = ((ContentResource) o2).getReleaseDate();
			if (t2 == null)  t2 = ((ContentResource) o2).getProperties().getTimeProperty(m_property);
			
			rv = t1.compareTo(t2);

			if (!m_ascending)
				rv = -rv;
			
		} 
		catch (EntityPropertyTypeException ignore) {
			log.warn("EntityPropertyTypeException while comparing podcast dates. "
							+ ignore.getMessage(), ignore);
		}
		catch (EntityPropertyNotDefinedException ignore) {
			log.warn("EntityPropertyNotDefinedException while comparing podcast dates. "
							+ ignore.getMessage(), ignore);
		}

		return rv;
	}
}
