/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2003, 2004, 2005, 2006 Sakai Foundation
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

package org.sakaiproject.chat.api;

import org.sakaiproject.message.api.MessageHeaderEdit;

/**
 * <p>
 * ChatMessageHeader is the Interface for an editable Sakai Chat Message header.
 * </p>
 */
public interface ChatMessageHeaderEdit extends ChatMessageHeader, MessageHeaderEdit
{
	/** ChatMessageHeader has no additional fields over MessageHeader */
}
