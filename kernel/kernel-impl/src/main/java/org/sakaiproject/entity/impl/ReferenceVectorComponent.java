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

package org.sakaiproject.entity.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;

/**
 * <p>
 * ReferenceVectorComponent implements the ReferenceVector API.
 * </p>
 */
public class ReferenceVectorComponent extends Vector
{
	/**
	 * Constructor.
	 */
	public ReferenceVectorComponent(int initialCapacity, int capacityIncrement)
	{
		super(initialCapacity, capacityIncrement);
	}

	/**
	 * Constructor.
	 */
	public ReferenceVectorComponent(int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Constructor.
	 */
	public ReferenceVectorComponent(Collection c)
	{
		super(c);
	}

	/**
	 * Constructor.
	 */
	public ReferenceVectorComponent()
	{
		super();
	}

	/**
	 * Is this resource referred to in any of the references in the vector? Accept any Resource, or a String assumed to be a resource reference.
	 * 
	 * @param o
	 *        The Resource (or resource reference string) to check for presence in the references in the vector.
	 * @return true if the resource referred to in any of the references in the Vector, false if not.
	 */
	public boolean contains(Object o)
	{
		if ((o instanceof Entity) || (o instanceof String) || (o instanceof Reference))
		{
			String ref = null;
			if (o instanceof Entity)
			{
				ref = ((Entity) o).getReference();
			}
			else if (o instanceof String)
			{
				ref = (String) o;
			}
			else
			{
				ref = ((Reference) o).getReference();
			}

			Iterator it = iterator();
			while (it.hasNext())
			{
				Reference de = (Reference) it.next();
				if (de.getReference().equals(ref)) return true;
			}

			return false;
		}

		else
			return super.contains(o);
	}

	/**
	 * Removes the first occurrence of the specified element in this Vector. If the element is a String, treat it as a resource reference, else it's a Reference object.
	 * 
	 * @return true if the element was found, false if not.
	 */
	public boolean remove(Object o)
	{
		// if a string, treat as a resource reference
		if (o instanceof String)
		{
			String ref = (String) o;
			Iterator it = iterator();
			while (it.hasNext())
			{
				Reference de = (Reference) it.next();
				if (de.getReference().equals(ref))
				{
					return super.remove(de);
				}
			}

			return false;
		}

		return super.remove(o);
	}
}
