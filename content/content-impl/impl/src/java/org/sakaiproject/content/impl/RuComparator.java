/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;

public class RuComparator implements Comparator
{

	public RuComparator()
	{
	}

	public int compare(Object o1, Object o2)
	{

		ResourceProperties p1 = ((Entity) o1).getProperties();
		ResourceProperties p2 = ((Entity) o2).getProperties();

		String so1 = p1.getProperty("RU:resourceorder");
		String so2 = p2.getProperty("RU:resourceorder");

		int t1;
		int t2;

		// first sort on type, so that those with specified order
		// come first

		if (so1 != null && !so1.equals(""))
			t1 = 1;
		else if (o1 instanceof ContentCollection)
			t1 = 2;
		else
			t1 = 3;

		if (so2 != null && !so2.equals(""))
			t2 = 1;
		else if (o2 instanceof ContentCollection)
			t2 = 2;
		else
			t2 = 3;

		if (t1 < t2)
			return -1;
		else if (t1 > t2) return 1;

		// now, they are the same type, so compare within type
		// if order properties, use them

		if (so1 != null && !so1.equals("") && so2 != null && !so2.equals(""))
		{

			int i1 = Integer.parseInt(so1);
			int i2 = Integer.parseInt(so2);
			if (i1 == i2)
				return 0;
			else if (i1 < i2)
				return -1;
			else
				return 1;
		}

		// else do a formatted interpretation - case insensitive
		String s1 = p1.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		String s2 = p2.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		return s1.compareToIgnoreCase(s2);
	}
}
