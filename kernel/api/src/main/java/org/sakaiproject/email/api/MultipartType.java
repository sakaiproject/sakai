/**
 * Copyright (c) 2003-2008 The Apereo Foundation
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
package org.sakaiproject.email.api;

public interface MultipartType
{
	/**
	 * Used mainly for envelope of message to specify differing body parts
	 */
	String MULTI_MIXED = "multipart/mixed";

	/**
	 * Defer mime detection to client
	 */
	String MULTI_ALT = "multipart/alternative";

	/**
	 * For parts intended to be viewed simultaneously
	 */
	String MULTI_PARALLEL = "multipart/parallel";

	/**
	 * A digest of multiple messages
	 */
	String MULTI_DIGEST = "multipart/digest";

	/**
	 * Used for digests as well as for E-mail forwarding.
	 */
	String MULTI_MESSAGE = "multipart/message";

	/**
	 * Used to indicate that message parts should not be considered individually but rather as parts
	 * of an aggregate whole.
	 */
	String MULTI_RELATED = "multipart/related";

	/**
	 * Used to attach a digital signature to a message.
	 */
	String MULTI_SIGNED = "multipart/signed";
}
