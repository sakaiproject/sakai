/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.entity.api;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Provide entity access via http for use in the access servlet.
 * </p>
 */
public interface HttpAccess
{
	/**
	 * Handle an HTTP request for access. The request and response objects are provider.<br />
	 * The access is for the referenced entity.<br />
	 * Use the response object to send the headers, length, content type, and bytes of the response in whatever manner needed.<br />
	 * Make the response ONLY if it is permitted and exists and otherwise valid. Use the exceptions for any error handling.
	 * 
	 * @param req
	 *        The request object.
	 * @param res
	 *        The response object.
	 * @param ref
	 *        The entity reference
	 * @param copyrightAcceptedRefs
	 *        The collection (entity reference String) of entities that the end user in this session have already accepted the copyright for.
	 * @throws EntityPermissionException
	 *         Throw this if the current user does not have permission for the access.
	 * @throws EntityNotDefinedException
	 *         Throw this if the ref is not supported or the entity is not available for access in any way.
	 * @throws EntityAccessOverloadException
	 *         Throw this if you are rejecting an otherwise valid request because of some sort of server resource shortage or limit.
	 * @throws EntityCopyrightException
	 *         Throw this if you are rejecting an otherwise valid request because the user needs to agree to the copyright and has not yet done so.
	 */
	void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection<String> copyrightAcceptedRefs)
			throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException;
}
