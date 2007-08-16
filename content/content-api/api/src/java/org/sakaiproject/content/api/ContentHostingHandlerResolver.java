/**
 * 
 */
package org.sakaiproject.content.api;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.exception.ServerOverloadException;

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
	Edit newCollectionEdit(String id);

	/**
	 * create a new Resource Edit to allow the CHH implementation to deliver
	 * Resources
	 * 
	 * @param id
	 * @return
	 */
	Edit newResourceEdit(String id);

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
	ContentCollectionEdit putCollection( String id);

	/**
	 * @param id
	 * @return
	 */
	ContentCollectionEdit editCollection( String id);

	/**
	 * @param edit
	 */
	void cancelResource( ContentResourceEdit edit);

	/**
	 * @param edit
	 */
	void commitCollection( ContentCollectionEdit edit);

	/**
	 * @param edit
	 */
	void cancelCollection( ContentCollectionEdit edit);

	/**
	 * @param edit
	 */
	void removeCollection( ContentCollectionEdit edit);

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
	ContentResourceEdit putResource( String id);

	/**
	 * @param id
	 * @return
	 */
	ContentResourceEdit editResource( String id);

	/**
	 * @param edit
	 * @throws ServerOverloadException 
	 */
	void commitResource( ContentResourceEdit edit) throws ServerOverloadException;

	/**
	 * @param id
	 * @param uuid
	 * @param userId
	 * @return
	 */
	ContentResourceEdit putDeleteResource(String id, String uuid, String userId);

	/**
	 * @param edit
	 * @param uuid
	 */
	void commitDeleteResource(ContentResourceEdit edit, String uuid);

	/**
	 * @param edit
	 */
	void removeResource(ContentResourceEdit edit);

	/**
	 * @param resource
	 * @return
	 * @throws ServerOverloadException 
	 */
	byte[] getResourceBody(ContentResource resource) throws ServerOverloadException;

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
	Collection<String> getMemberCollectionIds(String collectionId);

	/**
	 * @param collectionId
	 * @return
	 */
	Collection<String> getMemberResourceIds(String collectionId);

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
	String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id) throws OperationDelegationException;

	/**
	 * @param thisResource
	 * @param new_id
	 * @return
	 * @throws OperationDelegationException if the caller should be implementing the operation
	 */
	String moveResource(ContentResourceEdit thisResource, String new_id) throws OperationDelegationException;

	/**
	 * @param id
	 * @return
	 */
	String getUuid(String id) throws OperationDelegationException;

}
