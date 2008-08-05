/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

package org.sakaiproject.email.api;

import java.util.List;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;

/**
 * <p>
 * The DigestService collects sets of messages for different users, and sends
 * them out periodically.
 * </p>
 */
public interface DigestService {
	/** This string can be used to find the service in the service manager. */
	static final String SERVICE_NAME = DigestService.class.getName();

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/digest";

	public static enum Permission implements Entity.Permission {

		/** Securiy / Event for adding a digest. */
		SECURE_ADD_DIGEST("digest.add"),
		/** Securiy / Event for updating a digest. */
		SECURE_EDIT_DIGEST("digest.upd"),

		/** Securiy / Event for removing a digest. */
		SECURE_REMOVE_DIGEST("digest.del");

		private final String permission;

		private Permission(String permission) {
			this.permission = permission;
		}

		@Override
		public String toString() {
			return permission;
		}
	}

	/**
	 * Access a digest associated with this id.
	 * 
	 * @param id
	 *            The digest id.
	 * @return The Digest object.
	 * @exception IdUnusedException
	 *                if there is not digest object with this id.
	 */
	Digest getDigest(String id) throws IdUnusedException;

	/**
	 * Access all digest objects.
	 * 
	 * @return A List (Digest) of all defined digests.
	 */
	List getDigests();

	/**
	 * Add a new message to a digest, creating one if needed. This returns right
	 * away; the digest will be added as soon as possible.
	 * 
	 * @param message
	 *            The message to digest.
	 * @param subject
	 *            The to (user id) of the message.
	 * @param subject
	 *            The subject of the message.
	 * @param body
	 *            The subject of the message.
	 */
	void digest(String to, String Subject, String body);

	/**
	 * Add a new digest with this id. Must commit(), remove() or cancel() when
	 * done.
	 * 
	 * @param id
	 *            The digest id.
	 * @return A new DigestEdit object for editing.
	 * @exception IdUsedException
	 *                if these digest already exist.
	 */
	Digest add(String id) throws IdUsedException;

	/**
	 * Remove this DigestEdit - it must be locked from edit(). The DigestEdit is
	 * disabled, and not to be used after this call.
	 * 
	 * @param user
	 *            The DigestEdit object to remove.
	 */
	void remove(Digest edit);
}
