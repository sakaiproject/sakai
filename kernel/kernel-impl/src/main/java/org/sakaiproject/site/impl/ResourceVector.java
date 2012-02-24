/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.site.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>
 * ResourceVector is a Vector of Identifiables....
 * </p>
 */
public class ResourceVector extends Vector
{
	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public ResourceVector(int initialCapacity, int capacityIncrement)
	{
		super(initialCapacity, capacityIncrement);
	}

	/**
	 * Constructor.
	 */
	public ResourceVector(int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Constructor.
	 */
	public ResourceVector(Collection c)
	{
		super(c);
	}

	/**
	 * Constructor.
	 */
	public ResourceVector()
	{
		super();
	}

	/**
	 * Find the first item with this Resource id.
	 * 
	 * @param id
	 *        The resource id.
	 * @return the Resource that has this id, first in the list.
	 */
	public Identifiable getById(String id)
	{
		for (Iterator i = iterator(); i.hasNext();)
		{
			Identifiable r = (Identifiable) i.next();
			if (r.getId().equals(id)) return r;
		}

		return null;
	}

	/**
	 * Move an entry one up towards the start of the list.
	 * 
	 * @param entry
	 *        The resource to move.
	 */
	public void moveUp(Identifiable entry)
	{
		int pos = indexOf(entry);
		if (pos == -1) return;
		if (pos == 0) return;
		remove(entry);
		add(pos - 1, entry);
	}

	/**
	 * Move an entry one down towards the end of the list.
	 * 
	 * @param entry
	 *        The resource to move.
	 */
	public void moveDown(Identifiable entry)
	{
		int pos = indexOf(entry);
		if (pos == -1) return;
		if (pos == size() - 1) return;
		remove(entry);
		add(pos + 1, entry);
	}

	/**
	 * Move an entry to a specific (0 based) index.
	 * 
	 * @param entry
	 *        The resource to move.
	 */
	public void moveTo(Identifiable entry, int newPos)
	{
		remove(entry);
		add(newPos, entry);
	}
}
