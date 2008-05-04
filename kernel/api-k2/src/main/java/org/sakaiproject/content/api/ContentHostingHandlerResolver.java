/**
 * 
 */
package org.sakaiproject.content.api;

import java.io.InputStream;
import java.util.List;

import org.sakaiproject.content.api.exception.OperationDelegationException;
import org.sakaiproject.content.api.exception.ServerOverloadException;

/**
 * @author ieb
 */
public interface ContentHostingHandlerResolver
{

	public static final String CHH_BEAN_NAME = "sakai:handler-bean-id";

	/**
	 * create a new Collection Edit to allow the CHH implementation to deliver
	 * Collections
	 * 
	 * @param id
	 * @return
	 */
	ContentCollection newCollection(String id);

	/**
	 * create a new Resource Edit to allow the CHH implementation to deliver
	 * Resources
	 * 
	 * @param id
	 * @return
	 */
	ContentResource newResource(String id);

	/**
	 * @param id
	 * @return
	 */
	boolean checkCollection( String id);

	/**
	 * @param id
	 * @return
	 */
	ContentCollection getCollection( String id);

	/**
	 * @param collection
	 * @return
	 */
	List getCollections( ContentCollection collection);

	/**
	 * @param id
	 * @return
	 */
	ContentCollection putCollection( String id);


	/**
	 * @param edit
	 */
	void removeCollection( ContentCollection edit);

	/**
	 * @param id
	 * @return
	 */
	boolean checkResource( String id);

	/**
	 * @param id
	 * @return
	 */
	ContentResource getResource( String id);

	/**
	 * @param collection
	 * @return
	 */
	List getResources( ContentCollection collection);

	/**
	 * @param collectionId
	 * @return
	 */
	List getFlatResources( String collectionId);

	/**
	 * @param id
	 * @return
	 */
	ContentResource putResource( String id);


	/**
	 * @param id
	 * @param uuid
	 * @param userId
	 * @return
	 */
	ContentResource putDeleteResource(String id, String uuid, String userId);


	/**
	 * @param edit
	 */
	void removeResource(ContentResource edit);

	/**
	 * @param resource
	 * @return
	 * @throws ServerOverloadException 
	 */
	InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException;

	/**
	 * @param collectionId
	 * @return
	 */
	int getMemberCount(String collectionId);

	/**
	 * @param collectionId
	 * @return
	 */
	List<String> getMemberCollectionIds(String collectionId);

	/**
	 * @param collectionId
	 * @return
	 */
	List<String> getMemberResourceIds(String collectionId);

	/**
	 * @param resourceId
	 * @param uuid 
	 * @throws OperationDelegationException 
	 */
	void setResourceUuid(String resourceId, String uuid) throws OperationDelegationException;

	/**
	 * @param thisCollection
	 * @param new_folder_id
	 * @return
	 * @throws OperationDelegationException if the caller should be implementing the operation
	 */
	String moveCollection(ContentCollection thisCollection, String new_folder_id) throws OperationDelegationException;

	/**
	 * @param thisResource
	 * @param new_id
	 * @return
	 * @throws OperationDelegationException if the caller should be implementing the operation
	 */
	String moveResource(ContentResource thisResource, String new_id) throws OperationDelegationException;

	/**
	 * @param id
	 * @return
	 */
	String getUuid(String id) throws OperationDelegationException;

}
