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

public interface DavManager 
{
	/**
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
	 *            if overwrite is false and the id is already in use.
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
	 * @param newId
	 * @param content
	 * @param contenttype
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to create this entity.
	 * @throws IdUsedException
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
			throws PermissionException, InconsistentException, IdLengthException, 
					InUseException, ServerOverloadException;
	
	/**
	 * Delete a collection or resource.
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
	 */
	public String davGetProperty(String entityId, String propertyName) 
			throws PermissionException, IdUnusedException;
	
	/**
	 * 
	 * @param oldId
	 * @param newId
	 * @param overwrite
	 * 
	 * @throws PermissionException
	 *            if the user does not have permission to delete 
	 *            the existing entity or create the new entity.
	 * @throws IdUsedException
	 *            if overwrite is false and the id is already in use.
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
	 *            (May be avoidable if rename does not actually require moving
	 *            the bytes around).
	 */
	public void davRename(String oldId, String newId, boolean overwrite) 
			throws PermissionException, InconsistentException, IdLengthException, 
					InUseException, ServerOverloadException;
	
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
	
	/*
create(newid, contentstream, contenttype)
   error if newid exists and is collection
   if newid exists and is resource, ideally you just update its    contents, retaining
properities; however delete followed by create   is  OK as long as you preserve the  old
properties
   error if file name is too long
   ancestors must exist, or error
   If resource did not exist, use specified content type, if  any.  If contenttype is
null, look at the extension. If no extension or  unknown, I'd probably call it
text/plain,  though  if you feel  strongly about application/binary I'll accept  that as 
long as you  don't add .bin

The DAV operation will often get a MIME type from the client. If it's  specified, you
should use it.

If you'd prefer for your code not to know about extensions and mime  types, you can ask
me to pass the contenttype in all cases.

rename(oldid, newid, boolean overwrite)
    works for resources or collections
    may change name, move to new location, or both
    error if file name is too long
    if newid exists, delete it if overwrite set, else error
    ancestors must exist, or error
    all properties should be retained, where possible.
       there's some question about mime type. According to the  spec,  renaming foo.pdf
to foo.xls should leave it as mime type  pdf. That  seems highly counterintuitive. I
would recommend  resetting mime type,  with missing or unrecognized extensions  using
text/plain.

copy(oldid, newid, boolean overwrite, boolean recursive)
    works for resources or collections
    if newid exists, delete it if overwrite set, else error
    error if file name is too long
    ancestors must exist, or error
    all properties should be retained, where possible.
       there's some question about mime type. see rename
    if old is a collection, if recursive is set, copy all members   recursively with
infinite depth. otherwise copy only the  collection  and any properties

	 */
	
	
}
