/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.util.Web;

/**
 * <p>
 * MapUtil collects together some string utility classes.
 * </p>
 */
public class MapUtil
{
	/**
	 * Copies elements from a map to another map (similar to putall)
	 * 
	 * @param destMap
	 *        The Map to add the key/value pairs to.
	 * @param sourceMap
	 *        The Map to add the key/value pairs from
	 * @return true if there were keys copied
	 */
	public static boolean copy(Map destMap, Map sourceMap)
	{
	    if ( destMap == null || sourceMap == null || sourceMap.isEmpty() ) return false;
	    Set s = sourceMap.keySet();
	    if ( s == null ) return false;
	    Iterator iSet = s.iterator();

	    boolean retval = false;
            while (iSet.hasNext()) {
                Object item  = iSet.next();
		destMap.put(item,sourceMap.get(item));
		retval = true;
	    }
	    return retval;
	}

	/**
	 * Assigns a value from one map to another
         *
         * destMap[destKey] = sourceMap[sourceKey];
	 * 
	 * @param destMap
	 *        The Map to add the key/value pair to.
	 * @param destKey
	 *        The key to use in the destination map.
	 * @param sourceMap
	 *        The Map to add the key/value pairs from
	 * @param sourceKey
	 *        The key to use from the source  map.
	 * @return true if there were keys copied
	 */
	public static boolean copy(Map destMap, String destKey, Map sourceMap, String sourceKey)
	{
	    if ( destMap == null || sourceMap == null || sourceMap.isEmpty() ) return false;
	    Object o = sourceMap.get(sourceKey);
	    if ( o == null ) return false;
	    destMap.put(destKey,o);
	    return true;
	}

	/**
	 * Assigns a value from one map to another - applying an HMTL filter to the value
         *
         * destMap[destKey] = sourceMap[sourceKey];
	 * 
	 * @param destMap
	 *        The Map to add the key/value pair to.
	 * @param destKey
	 *        The key to use in the destination map.
	 * @param sourceMap
	 *        The Map to add the key/value pairs from
	 * @param sourceKey
	 *        The key to use from the source  map.
	 * @return true if there were keys copied
	 */
	public static boolean copyHtml(Map destMap, String destKey, Map sourceMap, String sourceKey)
	{
	    if ( destMap == null || sourceMap == null || sourceMap.isEmpty() ) return false;
            try {
	    	String s = (String) sourceMap.get(sourceKey);
	    	if ( s == null ) return false;
	    	destMap.put(destKey,Web.escapeHtml(s));
	    	return true;
	    } catch (Exception t) { // Not a string
		return false;
	    }
	}
}
