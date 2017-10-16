/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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
/**
 * 
 */
package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.util.List;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.impl.BaseContentService.Storage;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.util.StorageUser;

/**
 * <p>
 * The ContentHostingHandlerResolver performs operations on the ContentHostingService storage area to resolve operations in the storage area to the correct location. This can either be the default storage implementation or the ContentHostingHandler
 * associated with nodes in the path to the ContentEntity in question. i.e. traditional ContentHosting resources are dealt with as they always have been, storage commands applied to virtual entities are passed to the ContentHostingHandler which represents
 * them in CHH (johnf@caret.cam.ac.uk)
 * </p>
 * <p>
 * Implementors should be aware that there may be heavy access to this component on a per- request basis, so they might want to consider a caching mechanism if the resolution of ContentEntities is expensive.
 * </p>
 * 
 * @author ieb
 */
public interface BaseContentHostingHandlerResolver extends ContentHostingHandlerResolver
{
	/**
	 * @param storage
	 * @param edit
	 */
	void cancelCollection(Storage storage, ContentCollectionEdit edit);

	/**
	 * @param storage
	 * @param edit
	 */
	void cancelResource(Storage storage, ContentResourceEdit edit);

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	boolean checkCollection(Storage storage, String id);

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	boolean checkResource(Storage storage, String id);

	/**
	 * @param storage
	 * @param edit
	 */
	void commitCollection(Storage storage, ContentCollectionEdit edit);

	/**
	 * @param storage
	 * @param edit
	 * @param uuid
	 */
	void commitDeleteResource(Storage storage, ContentResourceEdit edit, String uuid);

	/**
	 * @param storage
	 * @param edit
	 * @throws ServerOverloadException
	 */
	void commitResource(Storage storage, ContentResourceEdit edit) throws ServerOverloadException;

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	ContentCollectionEdit editCollection(Storage storage, String id);

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	ContentResourceEdit editResource(Storage storage, String id);

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	ContentCollection getCollection(Storage storage, String id);

	/**
	 * @param storage
	 * @param collection
	 * @return
	 */
	List getCollections(Storage storage, ContentCollection collection);

	/**
	 * @param storage
	 * @param collectionId
	 * @return
	 */
	List getFlatResources(Storage storage, String collectionId);

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	ContentResource getResource(Storage storage, String id);

	/**
	 * @param storage
	 * @param resource
	 * @return
	 * @throws ServerOverloadException
	 */
	byte[] getResourceBody(Storage storage, ContentResource resource) throws ServerOverloadException;

	/**
	 * @param storage
	 * @param collection
	 * @return
	 */
	List getResources(Storage storage, ContentCollection collection);

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	ContentCollectionEdit putCollection(Storage storage, String id);

	/**
	 * @param storage
	 * @param uuid
	 * @param userId
	 * @param object
	 * @return
	 */
	ContentResourceEdit putDeleteResource(Storage storage, String id, String uuid, String userId);

	/**
	 * @param storage
	 * @param id
	 * @return
	 */
	ContentResourceEdit putResource(Storage storage, String id);

	/**
	 * @param storage
	 * @param edit
	 */
	void removeCollection(Storage storage, ContentCollectionEdit edit);

	/**
	 * @param storage
	 * @param edit
	 */
	void removeResource(Storage storage, ContentResourceEdit edit);

	/**
	 * @param storage
	 * @param resource
	 * @return
	 * @throws ServerOverloadException
	 */
	InputStream streamResourceBody(Storage storage, ContentResource resource) throws ServerOverloadException;


	void setResourceUser(StorageUser rsu);

	void setCollectionUser(StorageUser csu);
	
	int getMemberCount(Storage storage, String collectionId);


}
