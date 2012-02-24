/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

import java.util.List;

/**
 * <p>
 * AttachmentContainer is a mutable AttachmentContainer.
 * </p>
 */
public interface AttachmentContainerEdit extends AttachmentContainer
{
	/**
	 * Add an attachment.
	 * 
	 * @param ref
	 *        The attachment Reference.
	 */
	void addAttachment(Reference ref);

	/**
	 * Remove an attachment.
	 * 
	 * @param ref
	 *        The attachment Reference to remove (the one removed will equal this, they need not be ==).
	 */
	void removeAttachment(Reference ref);

	/**
	 * Replace the attachment set.
	 * 
	 * @param attachments
	 *        A ReferenceVector that will become the new set of attachments.
	 */
	void replaceAttachments(List<Reference> attachments);

	/**
	 * Clear all attachments.
	 */
	void clearAttachments();
}
