/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.entity.api;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Services which implement EntityTransferrer declare themselves as willing and able to transfer/copy their entities
 * from one context to another.
 * </p>
 * <p>
 * This interface has two parts:  
 * <ol>
 * <li>
 * After transferring the entities, it expects you to return a map of (Old Site Entity Refs -> New Site Entity Refs)
 * <br>
 * {@link #transferCopyEntitiesRefMigrator(String, String, List)}
 * <br>
 * {@link #transferCopyEntitiesRefMigrator(String, String, List, boolean)}
 *  </li>
 *  <li>
 *  Accept a map of (Old Site Entity Refs -> New Site Entity Refs).
 *  You can implement the conversion however you want within your tool
 *  <br>
 *  {@link #updateEntityReferences(String, Map)}
 *  </li>
 *  </ol>
 *
 * The traversal map can contain multiple entries for the same piece of content. For example content in ContentHosting
 * might return both the ID (/access/content/group/site/myfile and http://server/access/content/group/site/myfile) for
 * the same file.
 * </p>
 * @author bryan holladay
 *
 */

public interface EntityTransferrerRefMigrator {

	/**
	 * Takes a map of ref's (fromContextRef -> toContextRef) and replaces any reference to them
	 * 
	 * @param toContext The destination context
	 * @param transversalMap All the refs that can be updated.
	 */
	void updateEntityReferences(String toContext, Map<String, String> transversalMap);

	/**
	 * transfer a copy of Entites from the source context into the destination context
	 *
	 * @param fromContext The source context
	 * @param toContext The destination context
	 * @param ids when null, all entities will be imported; otherwise, only entities with those ids will be imported
	 * @return A map of old entity references to new entity references.
	 * @see EntityTransferrer#transferCopyEntities(String, String, List)
	 */
	Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids);
	
	/**
	 * transfer a copy of Entites from the source context into the destination context
	 *
	 * @param fromContext The source context
	 * @param toContext destination context
	 * @param ids when null, all entities will be imported; otherwise, only entities with those ids will be imported
	 * @param cleanup if true then remove everything in destination first.
	 * @return A map of old entity references to new entity references.
	 * @see EntityTransferrer#transferCopyEntities(String, String, List, boolean)
	 */
	Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids, boolean cleanup);

}
