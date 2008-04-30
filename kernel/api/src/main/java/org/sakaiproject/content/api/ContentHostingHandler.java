/**
 * 
 */
package org.sakaiproject.content.api;

import java.io.InputStream;
import java.util.List;

import org.sakaiproject.content.api.exception.ServerOverloadException;

/**
 * @author ieb
 */
public interface ContentHostingHandler
{



	/**
	 * get a list of collections contained within the supplied collection
	 * 
	 * @param collection
	 * @return
	 */
	List<ContentCollection> getCollections(ContentCollection collection);

	/**
	 * get a ContentCollectionEdit for the ID, creating it if necessary, this should not persist until commit is invoked
	 * 
	 * @param id
	 * @return
	 */
	ContentCollection getContentCollection(String id);

	/**
	 * get a content resource edit for the supplied ID, creating it if necesary. This sould not persist until commit is invoked
	 * 
	 * @param id
	 * @return
	 */
	ContentResource getContentResource(String id);

	/**
	 * get a list of string ids of all resources below this point
	 * 
	 * @param ce
	 * @return
	 */
	List getFlatResources(ContentEntity ce);


	/**
	 * get a list of resource ids as strings within the collection
	 * 
	 * @param collection
	 * @return
	 */
	List getResources(ContentCollection collection);

	/**
	 * Convert the passed-in ContentEntity into a virtual Content Entity. The implementation should check that the passed in entity is managed by this content handler before performing the translation. Additionally it must register the content handler
	 * with the newly proxied ContentEntity so that subsequent invocations are routed back to the correct ContentHostingHandler implementation
	 * 
	 * @param edit
	 * @return
	 */
	ContentEntity getVirtualContentEntity(ContentEntity edit, String finalId);

	/**
	 * perform a wastebasket operation on the names id, if the implementation supports the operation otherwise its safe to ignore.
	 * 
	 * @param id
	 * @param uuid
	 * @param userId
	 * @return
	 */
	ContentResource putDeleteResource(String id, String uuid, String userId);

	/**
	 * remove the supplied collection
	 * 
	 * @param edit
	 */
	void removeCollection(ContentCollection edit);

	/**
	 * remove the resource
	 * 
	 * @param edit
	 */
	void removeResource(ContentResource edit);

	/**
	 * stream the body of the resource
	 * 
	 * @param resource
	 * @return
	 * @throws ServerOverloadException
	 */
	InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException;

	/**
	 * get the number of members
	 * @param ce
	 * @return
	 */
	int getMemberCount(ContentEntity ce);

	/**
	 * @param ce
	 * @return
	 */
	List<String> getMemberCollectionIds(ContentEntity ce);

	/**
	 * @param ce
	 * @return
	 */
	List<String> getMemberResourceIds(ContentEntity ce);

	/**
	 * @param thisResource
	 * @param new_id
	 * @return
	 */
	String moveResource(ContentResource thisResource, String new_id);

	/**
	 * @param thisCollection
	 * @param new_folder_id
	 * @return
	 */
	String moveCollection(ContentCollection thisCollection, String new_folder_id);

	/**
	 * @param resourceId
	 * @param uuid
	 * @return
	 */
	 void setResourceUuid(String resourceId, String uuid);

	/**
	 * @param id
	 */
	void getUuid(String id);

}
