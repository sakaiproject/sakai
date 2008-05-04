package org.sakaiproject.content.api;

import org.sakaiproject.content.api.exception.InconsistentException;
import org.sakaiproject.content.api.exception.OverQuotaException;
import org.sakaiproject.content.api.exception.ServerOverloadException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;

public interface AttachmentContentService {
	/**
	 * check permissions for addAttachmentResource().
	 * 
	 * @return true if the user is allowed to addAttachmentResource(), false if
	 *         not.
	 */
	public boolean allowAddAttachmentResource();

	/**
	 * Check whether a resource id or collection id references an entity in the
	 * attachments collection. This method makes no guarantees that a resource
	 * actually exists with this id.
	 * 
	 * @param id
	 *            Assumed to be a valid resource id or collection id.
	 * @return true if the id (assuming it is a valid id for an existing
	 *         resource or collection) references an entity in the hidden
	 *         attachments area created through one of this class's
	 *         addAttachmentResource methods.
	 */
	public boolean isAttachmentResource(String id);

	/**
	 * Create a new resource as an attachment to some other resource in the
	 * system. The new resource will be placed into a newly created collecion in
	 * the attachment collection, with an auto-generated id, and given the
	 * specified resource name within this collection.
	 * 
	 * @param name
	 *            The name of the new resource, i.e. a partial id relative to
	 *            the collection where it will live.
	 * @param type
	 *            The mime type string of the resource.
	 * @param content
	 *            An array containing the bytes of the resource's content.
	 * @param properties
	 *            A ResourceProperties object with the properties to add to the
	 *            new resource.
	 * @exception IdUsedException
	 *                if the resource name is already in use (not likely, as the
	 *                containing collection is auto-generated!)
	 * @exception IdInvalidException
	 *                if the resource name is invalid.
	 * @exception InconsistentException
	 *                if the containing collection (or it's containing
	 *                collection...) does not exist.
	 * @exception PermissionException
	 *                if the user does not have permission to add a collection,
	 *                or add a member to a collection.
	 * @exception OverQuotaException
	 *                if this would result in being over quota.
	 * @exception ServerOverloadException
	 *                if the server is configured to write the resource body to
	 *                the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addAttachmentResource(String name, String type,
			byte[] content, ResourceProperties properties)
			throws IdInvalidException, InconsistentException, IdUsedException,
			PermissionException, OverQuotaException, ServerOverloadException;

	/**
	 * Create a new resource as an attachment to some other resource in the
	 * system. The new resource will be placed into a newly created collection
	 * in the site collection within the attachment collection. The new
	 * collection will have an auto-generated id, and it will be given the
	 * specified resource name within the site collection.
	 * 
	 * @param name
	 *            The name of the new resource, i.e. a partial id relative to
	 *            the collection where it will live.
	 * @param site
	 *            The string identifier for the site where the attachment is
	 *            being added within the attachments collection.
	 * @param tool
	 *            The display-name for the tool through which the attachment is
	 *            being added within the site's attachments collection.
	 * @param type
	 *            The mime type string of the resource.
	 * @param content
	 *            An array containing the bytes of the resource's content.
	 * @param properties
	 *            A ResourceProperties object with the properties to add to the
	 *            new resource.
	 * @exception IdUsedException
	 *                if the resource name is already in use (not likely, as the
	 *                containing collection is auto-generated!)
	 * @exception IdInvalidException
	 *                if the resource name is invalid.
	 * @exception InconsistentException
	 *                if the containing collection (or it's containing
	 *                collection...) does not exist.
	 * @exception PermissionException
	 *                if the user does not have permission to add a collection,
	 *                or add a member to a collection.
	 * @exception OverQuotaException
	 *                if this would result in being over quota.
	 * @exception ServerOverloadException
	 *                if the server is configured to write the resource body to
	 *                the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addAttachmentResource(String name, String site,
			String tool, String type, byte[] content,
			ResourceProperties properties) throws IdInvalidException,
			InconsistentException, IdUsedException, PermissionException,
			OverQuotaException, ServerOverloadException;

	/**
	 * Create a new resource as an attachment to some other resource in the
	 * system, locked for update. Must commitResource() to make official, or
	 * cancelResource() when done! The new resource will be placed into a newly
	 * created collecion in the attachment collection, with an auto-generated
	 * id, and given the specified resource name within this collection.
	 * 
	 * @param name
	 *            The name of the new resource, i.e. a partial id relative to
	 *            the collection where it will live.
	 * @exception IdUsedException
	 *                if the resource name is already in use (not likely, as the
	 *                containing collection is auto-generated!)
	 * @exception IdInvalidException
	 *                if the resource name is invalid.
	 * @exception InconsistentException
	 *                if the containing collection (or it's containing
	 *                collection...) does not exist.
	 * @exception PermissionException
	 *                if the user does not have permission to add a collection,
	 *                or add a member to a collection.
	 * @exception ServerOverloadException
	 *                if the server is configured to write the resource body to
	 *                the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addAttachmentResource(String name)
			throws IdInvalidException, InconsistentException, IdUsedException,
			PermissionException, ServerOverloadException;

}
