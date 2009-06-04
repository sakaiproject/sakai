/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.api;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
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
	/** Status code (201) indicating the request succeeded and created a new resource on the server. */
	public final static Integer STATUS_CREATED = Integer.valueOf(HttpServletResponse.SC_CREATED);
	
	/** Status reporting an operation succeeded without creation of a new resource */
	public final static Integer STATUS_NO_CONTENT = Integer.valueOf(HttpServletResponse.SC_NO_CONTENT);
	
	/** Status code (401) indicating that the request requires HTTP authentication. */
	public final static Integer STATUS_UNAUTHORIZED = Integer.valueOf(HttpServletResponse.SC_UNAUTHORIZED);
	
	/** Status code (403) indicating the server understood the request but refused to fulfill it. */
	public final static Integer STATUS_FORBIDDEN = Integer.valueOf(HttpServletResponse.SC_FORBIDDEN);
	
	/** Status code (409) indicating requested action cannot be executed on an existing resource. */
	public final static Integer STATUS_METHOD_NOT_ALLOWED = Integer.valueOf(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	
	/** Status code (409) indicating a resource cannot be created at the destination until one or more intermediate collections have been created. */
	public final static Integer STATUS_CONFLICT = Integer.valueOf(HttpServletResponse.SC_CONFLICT);
	
	
	/*
	 * 
412 (Precondition Failed) - The server was unable to maintain the liveness of the properties listed in the propertybehavior XML element or the Overwrite header is "F" and the state of the destination resource is non-null.
412 (Precondition Failed) - The server was unable to maintain the liveness of the properties listed in the propertybehavior XML element or the Overwrite header is "F" and the state of the destination resource is non-null.

415 (Unsupported Media Type)- The server does not support the request type of the body. (MKCOL)

423 (Locked) - The destination resource was locked.
423 (Locked) - The source or the destination resource was locked.

502 (Bad Gateway) - This may occur when the destination is on another server and the destination server refuses to accept the resource.
502 (Bad Gateway) - This may occur when the destination is on another server and the destination server refuses to accept the resource.

507 (Insufficient Storage) - The destination resource does not have sufficient space to record the state of the resource after the execution of this method.
507 (Insufficient Storage) - The resource does not have sufficient space to record the state of the resource after the execution of this method.

	 */
	
	
    /* 
     * TODO: Locks
     * General question on locks:
	 * DAV has a locking scheme. Currently it is implemented entirely within DavServlet.
	 * Since Sakai also has locking, we end up with two locking systems that
	 * don't talk. If we do things like copying collections in Content, it also
	 * makes it nearly impossible to do what the protocol suggests and copy
	 * all but the locked items, returning a list of what was and was not
	 * copied. At the moment we simply give an error for the whole operation.
	 * I believe that is legal. I am also skeptical about the ability of clients
	 * to cope with partial success. However to produce a maximally compliant DAV,
	 * we would probably need to move DAV locking into Content.
	 * (hedrick at rutgers dot edu)
	 */

    /* 
     * General comment on error handling:
	 * DAV has the possibility to return an XML structure listing items and error codes. 
	 * If an operation on a collection fails for some items in the collection, the spec requires
	 * us to return a list of all items indicating where it succeeded and where it failed.
	 * Operations here must either succeed in their entirety, fail and back out of all 
	 * changes, or return a list of items and status. It would clearly be easier not to
	 * return such a list. If that is the intent, care must be taken to avoid partial
	 * failure. E.g. in copying collections where an existing one will be deleted, it
	 * would be prudent to copy to a new name, verify that the copy worked, and then
	 * delete the old one and rename the new one.
	 * (hedrick at rutgers dot edu)
	 */

    /* 
     * General comment on return values:
	 * These operations all return boolean. The reason is that DAV has two different
	 * success codes: 201 Created and 204 No content. We must distinguish between
	 * cases where new content was created and where it was not. E.g. the spec for
	 * copy says that 201 is returned if a new resource is created, while
	 * 204 is returned if the resource existed. Operations are expected to
	 * return true for Created and false for No Content. Some operations will
	 * always return the same value. This is noted in the descriptions.
	 * At the HTTP level, 201 returns a URI for the new resources. This allows for
	 * the possibility that we could use a different name than the requested one.
	 * If we wanted to do that, then these functions would return null or a
	 * String. However because clients will typically use these functions to
	 * model a Unix file system, I strongly recommend against doing this.
	 * (hedrick at rutgers dot edu)
	 * 
	 * Maybe the return value should be a List of id's indicating entities for which 
	 * the operation succeeded/failed?  Or a Map (hashtable) with keys for each
	 * item involved in the operation and values indicating whether the operation
	 * succeeded or failed?
	 * (jimeng at umich dot edu)
	 * 
	 * Sure, if you want to do that. it would map nicely into what DAV is supposed to 
	 * return. Note that the DAV protocol requires minimization  of exceptions. So if 
	 * a whole tree fails in the same way, you mention  only the top of the tree. While 
	 * it isn't clear in the text, it  appears that if there is partial success, you 
	 * return a multistatus  XML structure that lists only the things that failed. And 
	 * for them  you list only the top of any tree for which all elements failed with  
	 * the same error code.
	 * 
	 * As I read it, most operations return either 201 or 204. I believe the  only ones 
	 * where partial success is possible are ones where a whole  tree is manipulated at 
	 * once.
	 * 
	 * Copy certainly has this issue, since one could run into a lock or a  quota problem 
	 * during the recursive copy.
	 * 
	 * Delete can succeed partially if elements are locked. Whether other  types of 
	 * failure are possible depends upon the implementation. I'm  guessing that a failure 
	 * to delete an item other than because of a  lock would indicate a serious problem 
	 * with the db or file system.
	 * 
	 * Move can have partial success due to system failures. Is anything  else possible? 
	 * I'm not so sure. If overwrite is on, the implied  delete could partially fail, 
	 * but in that case the move can't occur at  all, so optimally you'd not do the delete 
	 * and fail the whole operation. In theory if you're copying to a different file 
	 * system you  could get a quota problem, but in Sakai I suspect quotas are on a  
	 * sakai-wide basis so that wouldn't happen.
	 * 
	 * Lock can have partial success, but I'm not currently dealing with the  details of 
	 * locking unless you tell me you're sure you can implement  DAV compatibility. 
	 * Otherwise I'd rather maintain the locks myself,  and checking for locks before 
	 * calling you.
	 * (hedrick at rutgers dot edu)
	 */

	/**
	 * Copies a resource or collection along with all attributes and properties.  
	 * If the parameter overwrite is true, delete existing entity (or entities).  
	 * Otherwise update them. For collections, preexisting items from newId that
	 * do not have corresponding items in oldId will remain.
	 * If the parameter oldId identifies a collection and the parameter recursive
	 * is true, copy the entire hierarchy of collections and resources rooted at
	 * oldId. If the parameter oldId identifies a collection and the parameter 
	 * recursive is false, copy only the entity oldId. The copy succeeds unless 
	 * one of these errors occurs:
	 * <ul>
	 *  <li>no entity exists with the identifier oldId</li>
	 *  <li>the user does not have permission to access the entity oldId</li>
	 *  <li>the user does not have permission to create the entity newId</li>
	 *  <li>the entity newId exists, and the user does not have permission to modify/delete oldId</li>
	 *  <li>the entity newId exists, and the entity newId (or one of its members) is in use by another user</li>
	 *  <li>overwrite is false and one of newId, oldId is a resource and the other a collection</li>
	 *  <li>the containing collection for newId does not exist</li>
	 *  <li>newId is too long to be used as a resource-id or collection-id</li>
	 *  <li>the server is configured to write the resource body to the filesystem and an attempt to save the resource body fails</li>
	 *  <li>the source and destination objects are the same
	 *  <li>writing the new resource puts the user over quota
	 * </ul>
	 * If overwrite is set, the nature of any existing destination does not matter. It will be deleted.
	 * Return: true if a new collection or resource was created, i.e. newId did not exist previously
	 * 
	 * @param oldId
	 * @param newId
	 * @param overwrite
	 * @param recursive
	 * 
	 * @return true if a new collection or resource was created, i.e. newId did not exist previously
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to access the old entity, delete 
	 *            the existing entity or create the new entity.
	 * @throws IdUnusedException
	 *            if oldIdd does not exist.
	 * @throws InconsistentException
	 *            if the containing collection (for newId) does not exist.
     * @throws TypeException
     *            if overwrite is false, and 
	 *            newId and oldId do not identify the same kind of entities 
	 *            (must both be identifiers for collections OR must both
     *            be identifiers for collections)
	 * @throws IdLengthException
	 *            if the newId is too long.
	 * @throws InUseException
	 *            if the entity specified by oldId is currently in use 
	 *            by another user or, if overwrite is true, the entity 
	 *            identified by newId exists and is in use by another user.
	 * @throws ServerOverloadException
	 *            if the server is configured to write the resource body to the 
	 *            filesystem and an attempt to save the resource body fails. 
	 * @throws OverQuotaException
	 *            if writing the new resource puts the user over quota
	 */
	public boolean copy(String oldId, String newId, boolean overwrite, boolean recursive)  
			throws PermissionException, InconsistentException, IdLengthException, 
					InUseException, ServerOverloadException;
	
	/**
	 * 
	 * Make a new collection.
	 * The new collection is empty.
     * The operation succeeds unless one of the following occurs:
	 * <ul>
	 *  <li>the user does not have permission to create the entity newId</li>
	 *  <li>an entity newId exists
	 *  <li>newId is too long to be used as a resource-id or collection-id</li>
	 *  <li>the server is configured to write the resource body to the filesystem and an attempt to save the resource body fails</li>
	 *  <li>writing the new collection puts the user over quota
	 * </ul>
	 * If successful, always returns true, because a new collection is always created
	 *
	 * @param newId
	 * 
	 * @return true if a new collection is created (which is always true unless an exception is thrown).
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
	 * @throws ServerOverloadException
	 *            if the server is configured to write the collection to the
	 *            filesystem and it fails
	 * @throws OverQuotaException
	 *            if writing the new collection puts the user over quota
	 */
	public boolean createCollection(String newId) 
			throws PermissionException, IdUsedException, InconsistentException, 
					IdLengthException, IdInvalidException;
	
	/**
	 * Create the resource or update it if it exists.
	 * if newid exists and is resource, update its contents, retaining properities; 
	 * however delete followed by create is  OK as long as it preserves the  old
	 * properties.
	 * If newid did not exist, use specified content type, if any. 
	 * If contenttype is null, look at the extension. If no extension or unknown, use text/plain
	 * The operation succeeds unless one of the following is true:
	 * <ul>
	 *  <li>newid exists and is a collection
	 *  <li>user does not have permission to create or write the new object
	 *  <li>the containing collection does not exist
	 *  <li>newId is too long to be used as a resource-id or collection-id</li>
	 *  <li>the server is configured to write the resource body to the filesystem and an attempt to save the resource body fails</li>
	 *  <li>writing the new resource puts the user over quota
	 * </ul>
	 * Returns true if a new resource is created, i.e. if newId did not exist
	 * 
	 * @param newId
	 * @param content
	 * @param contenttype
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to create or write to this entity.
	 * @throws TypeException
	 *            if the id is already in use and identifies a collection. 
	 *            TODO: Is this the right exception in that case?
	 *            [I don't care what exception you use, as long as it is documented.]
	 * @throws InconsistentException
	 *            if the containing collection does not exist.
	 * @throws IdLengthException
	 *            if the newId is too long.
	 * @throws InUseException
	 *            if the entity exists and is currently in use by another user.
	 * @throws OverQuotaException
	 *            if the entity cannot be added without exceeding the quota.
	 * @throws ServerOverloadException
	 *            if the server is configured to write the resource body to the 
	 *            filesystem and an attempt to save the resource body fails.
	 * @throws OverQuotaException
	 *            if writing the new collection puts the user over quota
	 */
	public boolean createResource(String newId, InputStream content, String contentType) 
			throws PermissionException, TypeException, InconsistentException, IdLengthException, 
					InUseException, OverQuotaException, ServerOverloadException;
	
	/**
	 * Delete a collection or resource. If entity is a non-empty collection,
	 * remove it and everything it contains.
	 * Fails if
	 * <ul>
	 *  <li>no entity exists with the identifier entityId</li>
	 *  <li>the user does not have permission to delete the entity</li>
	 *  <li>the server is configured to write the resource body to the filesystem and an attempt to delete it fails</li>
	 * </ul>
	 * Always returns false.
	 * 
	 * @param entityId
	 * @throws PermissionException
	 *            if the user does not have permission to delete this entity.
	 * @throws InUseException
	 *            if the entity (or one of its members) is currently in use
	 *            by another user.
	 * @throws IdUnusedException
	 *            if entityId does not exist.
	 * @throws ServerOverloadException
	 *            if the server is configured to write the resource body to the 
	 *            filesystem and an attempt to delete it fails. 
	 */
	public boolean delete(String entityId) 
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
	public ResourceProperties getProperties(String entityId) 
			throws PermissionException, IdUnusedException;
	
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
	 * @throws TypeException
	 *            if the entity specified by the id exists and is a collection.
	 */
	public String getContentType(String entityId) 
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
	public String getProperty(String entityId, String propertyName) 
			throws PermissionException, IdUnusedException;

	/**
	 * Moves a resource or collection along with all attributes and properties.
	 * No properties are taken from any existing entity.
	 * If the parameter overwrite is true, delete existing entities before
	 *  the move; otherwise fail if the new entity exists.
	 * If the parameter oldId identifies a collection, move the entire hierarchy 
	 * of collections and resources rooted at oldId. The copy succeeds unless 
	 * one of these errors occurs:
	 * <ul>
	 *  <li>no entity exists with the identifier oldId</li>
	 *  <li>the user does not have permission to access the entity oldId</li>
	 *  <li>the user does not have permission to create the entity newId</li>
	 *  <li>the user does not have permission to delete oldId</li>
	 *  <li>overwrite is true, the entity newId exists, and the user does not have permission to delete newId</li>
	 *  <li>overwrite is true, the entity newId exists, and the entity newId (or one of its members) is in use by another user</li>
	 *  <li>overwrite is false and the entity newId already exists</li>
	 *  <li>the containing collection for newId does not exist</li>
	 *  <li>newId is too long to be used as a resource-id or collection-id</li>
	 *  <li>the server is configured to write the resource body to the filesystem and an attempt to save the resource body fails</li>
	 *  <li>the source and destination objects are the same
	 *  <li>writing the new resource puts the user over quota
	 * </ul>
	 * returns true if a new entity is created, i.e. newId did not exist
	 * 
	 * @param oldId
	 * @param newId
	 * @param overwrite
	 * 
	 * @return true if a new entity is created, i.e. newId did not exist
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to delete 
	 *            the existing entity, or delete/create the new entity.
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
	 * @throws ServerOverloadException
	 *            if the server is configured to write the resource body to the 
	 *            filesystem and an attempt to save the resource body fails. 
	 * @throws OverQuotaException
	 *            if writing the new resource puts the user over quota
	 */
	public boolean rename(String oldId, String newId, boolean overwrite) 
			throws PermissionException, IdUsedException, InconsistentException, IdLengthException, 
					InUseException, TypeException, ServerOverloadException;
	
	/**
	 * 
	 * @param entityId
	 * @param contentType
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
	public String setContentType(String entityId, String contentType) 
			throws PermissionException, IdUnusedException, TypeException;
	
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
	public void setProperties(String entityId, ResourceProperties properties) 
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
	public void setProperty(String entityId, String propertyName, String propertyValue) 
			throws PermissionException, IdUnusedException;

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
	public OutputStream streamContent(String entityId) throws PermissionException, IdUnusedException;
	
}
