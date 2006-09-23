package org.sakaiproject.content.api;

import java.io.InputStream;
import java.io.OutputStream;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

/**
 * 
 * 
 *
 */
public interface DavManager 
{
	/**
	 * Copies a resource or collection along with all attributes and properties.  
	 * If the parameter overwrite is true, replace existing entity (or entities).  
	 * If the parameter oldId identifies a collection and the parameter recursive
	 * is true, copy the entire hierarchy of collections and resources rooted at
	 * oldId. If the parameter oldId identifies a collection and the parameter 
	 * recursive is false, copy only the entity oldId. The copy succeeds unless 
	 * one of these errors occurs:
	 * <ul>
	 *  <li>no entity exists with the identifier oldId</li>
	 *  <li>the user does not have permission to access the entity oldId</li>
	 *  <li>the user does not have permission to create the entity newId</li>
	 *  <li>overwrite is true, the entity newId exists, and the user does not have permission to modify/delete oldId</li>
	 *  <li>overwrite is true, the entity newId exists, and the entity newId (or one of its members) is in use by another user</li>
	 *  <li>overwrite is false and the entity newId already exists</li>
	 *  <li>the containing collection for newId does not exist</li>
	 *  <li>newId is too long to be used as a resource-id or collection-id</li>
	 *  <li>either oldId or newId identifies a collection and the other identifies a resource</li>
	 *  <li>the server is configured to write the resource body to the filesystem and an attempt to save the resource body fails</li>
	 * </ul>
	 * TODO: if overwrite is true, recursive is false and newId exists, is it an error if newId is not empty? 
	 * what should happen to items contained by newId if recursive is false or if there are not entities with 
	 * corresponding id's in oldId?
	 * TODO: when replacing an existing entity, does user need permission to 'modify' or 'delete' the existing entity?
	 * 
	 * @param oldId
	 * @param newId
	 * @param overwrite
	 * @param recursive
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to delete 
	 *            the existing entity or create the new entity.
	 * @throws IdUsedException
	 *            if overwrite is false and the newId is already in use.
	 * @throws IdUnusedException
	 *            if oldIdd does not exist.
	 * @throws InconsistentException
	 *            if the containing collection (for newId) does not exist.
	 * @throws IdLengthException
	 *            if the newId is too long.
	 * @throws InUseException
	 *            if the entity specified by oldId is currently in use 
	 *            by another user or, if overwrite is true, the entity 
	 *            identified by newId exists and is in use by another user.
	 * @throws TypeException
	 *            if newId and oldId do not identify the same kind of entities 
	 *            (must both be identifiers for collections OR must both
	 *            be identifiers for collections)
	 * @throws ServerOverloadException
	 *            if the server is configured to write the resource body to the 
	 *            filesystem and an attempt to save the resource body fails. 
	 */
	public void davCopy(String oldId, String newId, boolean overwrite, boolean recursive)  
			throws PermissionException, InconsistentException, IdLengthException, 
					InUseException, ServerOverloadException;
	
	/**
	 * 
	 * @param newId
	 *            
	 * @throws PermissionException
	 *            if the user does not have permission to add this entity.
	 * @throws IdUsedException
	 *            if the id is already in use.
	 * @throws InconsistentException
	 *            if the containing collection does not exist.
	 * @throws IdLengthException
	 *            if the newId is too long.
	 * @throws IdInvalidException
	 *            if the resource id is invalid.
	 */
	public void davCreateCollection(String newId) 
			throws PermissionException, IdUsedException, InconsistentException, 
					IdLengthException, IdInvalidException;
	
	/**
	 * Create the entity or update it if it exists.
	 * if newid exists and is resource, update its contents, retaining properities; 
	 * however delete followed by create is  OK as long as it preserves the  old
	 * properties
	 * 
	 * @param newId
	 * @param content
	 * @param contenttype
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to create this entity.
	 * @throws TypeException
	 *            if the id is already in use and identifies a collection. 
	 *            TODO: Is this the right exception in that case?
	 * @throws InconsistentException
	 *            if the containing collection does not exist.
	 * @throws IdLengthException
	 *            if the newId is too long.
	 * @throws InUseException
	 *            if the entity exists and is currently in use by another user.
	 * @throws ServerOverloadException
	 *            if the server is configured to write the resource body to the 
	 *            filesystem and an attempt to save the resource body fails.
	 */
	public void davCreateResource(String newId, InputStream content, String contentType) 
			throws PermissionException, TypeException, InconsistentException, IdLengthException, 
					InUseException, ServerOverloadException;
	
	/**
	 * Delete a collection or resource. If entity is a non-empty collection,
	 * remove it and everything it contains.
	 * 
	 * @param entityId
	 * @throws PermissionException
	 *            if the user does not have permission to delete this entity.
	 * @throws InUseException
	 *            if the entity (or one of its members) is currently in use
	 *            by another user.
	 */
	public void davDelete(String entityId) 
			throws PermissionException, IdUnusedException, InUseException;
	
	/**
	 * 
	 * @param entityId
	 * 
	 * @return
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to access this entity.
	 * @throws IdUnusedException
	 *            if the entity specified by the id does not exist.
	 */
	public ResourceProperties davGetProperties(String entityId) 
			throws PermissionException, IdUnusedException;
	
	/**
	 * 
	 * @param entityId
	 * @param propertyName
	 * 
	 * @return
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to access this entity.
	 * @throws IdUnusedException
	 *            if the entity specified by the id does not exist.
	 * @throws TypeException
	 *            if the entity specified by the id exists and is a collection.
	 */
	public String davGetContentType(String entityId) 
			throws PermissionException, IdUnusedException, TypeException;
	
	/**
	 * 
	 * @param entityId
	 * @param propertyName
	 * 
	 * @return
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to access this entity.
	 * @throws IdUnusedException
	 *            if the entity specified by the id does not exist.
	 */
	public String davGetProperty(String entityId, String propertyName) 
			throws PermissionException, IdUnusedException;

	/**
	 * Moves a resource or collection along with all attributes and properties,
	 * unless the entity identified by newId exists, in which case certain 
	 * properties and attributes are retained for existing entities that are 
	 * being replaced.   
	 * If the parameter overwrite is true, replace existing entity (or entities).  
	 * If the parameter oldId identifies a collection, move the entire hierarchy 
	 * of collections and resources rooted at oldId. The copy succeeds unless 
	 * one of these errors occurs:
	 * <ul>
	 *  <li>no entity exists with the identifier oldId</li>
	 *  <li>the user does not have permission to access the entity oldId</li>
	 *  <li>the user does not have permission to create the entity newId</li>
	 *  <li>overwrite is true, the entity newId exists, and the user does not have permission to modify/delete oldId</li>
	 *  <li>overwrite is true, the entity newId exists, and the entity newId (or one of its members) is in use by another user</li>
	 *  <li>overwrite is false and the entity newId already exists</li>
	 *  <li>the containing collection for newId does not exist</li>
	 *  <li>newId is too long to be used as a resource-id or collection-id</li>
	 *  <li>either oldId or newId identifies a collection and the other identifies a resource</li>
	 *  <li>the server is configured to write the resource body to the filesystem and an attempt to save the resource body fails</li>
	 * </ul>
	 * TODO: if overwrite is true and newId exists, is it an error if newId is not empty? 
	 * what should happen to items contained by newId if there are not entities with 
	 * corresponding id's in oldId?
	 * TODO: when replacing an existing entity, does user need permission to 'modify' or 'delete' the existing entity?
	 * 
	 * @param oldId
	 * @param newId
	 * @param overwrite
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to delete 
	 *            the existing entity or create the new entity.
	 * @throws IdUsedException
	 *            if overwrite is false and the newId is already in use.
	 * @throws IdUnusedException
	 *            if oldIdd does not exist.
	 * @throws InconsistentException
	 *            if the containing collection (for newId) does not exist.
	 * @throws IdLengthException
	 *            if the newId is too long.
	 * @throws InUseException
	 *            if the entity specified by oldId is currently in use 
	 *            by another user or, if overwrite is true, the entity 
	 *            identified by newId exists and is in use by another user.
	 * @throws TypeException
	 *            if newId and oldId do not identify the same kind of entities 
	 *            (must both be identifiers for collections OR must both
	 *            be identifiers for collections)
	 * @throws ServerOverloadException
	 *            if the server is configured to write the resource body to the 
	 *            filesystem and an attempt to save the resource body fails. 
	 */
	public void davRename(String oldId, String newId, boolean overwrite) 
			throws PermissionException, IdUsedException, InconsistentException, IdLengthException, 
					InUseException, TypeException, ServerOverloadException;
	
	/**
	 * 
	 * @param entityId
	 * @param properties
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to modify this entity.
	 * @throws IdUnusedException
	 *            if the entity specified by the id does not exist.
	 */
	public void davSetProperties(String entityId, ResourceProperties properties) 
			throws PermissionException, IdUnusedException;
	
	
	/**
	 * 
	 * @param entityId
	 * @param propertyName
	 * @param propertyValue
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to modify this entity.
	 * @throws IdUnusedException
	 *            if the entity specified by the id does not exist.
	 */
	public void davSetProperty(String entityId, String propertyName, String propertyValue) 
			throws PermissionException, IdUnusedException;

	/**
	 * 
	 * @param entityId
	 * @return
	 * @throws PermissionException
	 *            if the user does not have permission to access this entity.
	 * @throws IdUnusedException
	 *            if the entity specified by the id does not exist.
	 */
	public OutputStream davStreamContent(String entityId) throws PermissionException, IdUnusedException;
	
}
