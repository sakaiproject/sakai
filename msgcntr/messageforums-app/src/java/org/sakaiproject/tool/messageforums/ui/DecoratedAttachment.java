/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DecoratedAttachment.java $
 * $Id: DecoratedAttachment.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.ui;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;

public class DecoratedAttachment
{
	private Attachment attachment;
	private String url;


	public DecoratedAttachment(Attachment attachment)
	{
		this.attachment = attachment;
	}
	
	public Attachment getAttachment()
	{
		return attachment;
	}

	public void setAttachment(Attachment attachment)
	{
		this.attachment = attachment;
	}

	public String getUrl()
	{
		MessageForumsMessageManager messageManager = (MessageForumsMessageManager)ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");
		if(attachment != null)
		{
		    return messageManager.getAttachmentRelativeUrl(attachment.getAttachmentId());
		}
		else
			return "";
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
}
