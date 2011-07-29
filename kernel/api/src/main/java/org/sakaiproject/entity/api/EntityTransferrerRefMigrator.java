package org.sakaiproject.entity.api;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Services which implement EntityTransferrer declare themselves as willing and able to transfer/copy their entities from one context to another.
 * </p>
 * <p>
 * This interface has two parts:  
 * <br/>
 * 1) After transferring the entities, it expects you to return a map of (Old Site Entity Refs -> New Site Entity Refs)
 * </br>
 * 2) Accept a map of (Old Site Entity Refs -> New Site Entity Refs).  You can implement the conversion however you want within your tool
 * 
 * </p>
 * @author bryan holladay
 *
 */

public interface EntityTransferrerRefMigrator {

	/**
	 * Takes a map of ref's (fromContextRef -> toContextRef) and replaces any reference to them
	 * 
	 * @param toContext
	 * @param transversalMap
	 */
	void updateEntityReferences(String toContext, Map<String, String> transversalMap);

	/**
	 * {@link EntityTransferrer#transferCopyEntities(String, String, List)}
	 * 
	 * @return
	 */
	Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids);
	
	/**
	 * {@link EntityTransferrer#transferCopyEntities(String, String, List, boolean)}
	 * 
	 * @return
	 */
	Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids, boolean cleanup);

}
