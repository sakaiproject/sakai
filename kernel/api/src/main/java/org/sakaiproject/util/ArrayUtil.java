/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;

/**
 * <p>
 * ArrayUtil collects together some Array utility methods.
 * </p>
 */
public class ArrayUtil
{
	/**
	 * Search for an object in an array.
	 * 
	 * @param target
	 *        The target array
	 * @param search
	 *        The object to search for.
	 * @return true if search is "in" (equal to any object in) the target, false if not
	 */
	public static boolean contains(Object[] target, Object search)
	{
		if ((target == null) || (search == null)) return false;
		if (target.length == 0) return false;
		try
		{
			for (int i = 0; i < target.length; i++)
			{
				if (search.equals(target[i])) return true;
			}
		}
		catch (Exception e)
		{
			return false;
		}

		return false;
	}
}
