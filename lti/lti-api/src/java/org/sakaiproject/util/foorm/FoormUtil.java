/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.util.foorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.Properties;

/**
 *  FoormUtil - A series of simple helper methods that save a lot of 
 *  type checking, error checking and try/except blocks when extracting 
 *  data from record sets, maps, or properties as is often done in Foorm.
 */
public class FoormUtil {

	/**
	 * getLong Convert various types to a Long
	 *
	 * @param o 
	 * @return Long - if there is something wrong, return -1
	 */
	public static Long getLong(Object o) {
		Long retval = getLongNull(o);
		if (retval != null)
			return retval;
		return new Long(-1);
	}

	/**
	 * getLong Convert various types to a Long, returning null on error
	 *
	 * @param o Can be a Number or a String
	 * @return Long - if there is something wrong, return null
	 */
	public static Long getLongNull(Object o) {
		if (o == null)
			return null;
		if (o instanceof Number)
			return new Long(((Number) o).longValue());
		if (o instanceof String) {
			try {
				return new Long((String) o);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * getInt - Convert an object to an integer
	 * @param o Can be a Number or a String
	 * @return an integer value or -1 for error
	 */
	public static int getInt(Object o) {
		if (o instanceof String) {
			try {
				return (new Integer((String) o)).intValue();
			} catch (Exception e) {
				return -1;
			}
		}
		if (o instanceof Number)
			return ((Number) o).intValue();
		return -1;
	}

}
