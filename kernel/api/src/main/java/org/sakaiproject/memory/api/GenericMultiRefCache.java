/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/memory/api/MultiRefCache.java $
 * $Id: MultiRefCache.java 66305 2009-09-06 10:14:27Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.memory.api;

import java.util.Collection;

/**
 * <p>
 * GenericMultiRefCache is a cache that holds objects and a set of references that the cached entry are dependent on - if any change, the entry is invalidated.
 * </p>
 * @deprecated since Sakai 2.9 - Do NOT use this anymore, it is not cluster safe or JSR-107 compatible,
 *             if you need this functionality then it should be handled with 2 caches in your service
 */
public interface GenericMultiRefCache extends Cache
{
	/**
	 * Cache an object that depends on more than just the key. This doesn't just work for
	 * authz but anything.
	 * @param key The key with which to find the payload.
	 * @param payload The cached object.
	 * @param dependRefs All the references that this cached object depends on.
	 */
	void put(String key, Object payload, String ref, Collection<String> dependRefs);
}
