/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.util.List;
import java.util.ArrayList;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceTypeRegistry;

/**
 * BasicMultiFileUploadPipe
 *
 */
public class BasicMultiFileUploadPipe extends BasicResourceToolActionPipe
	implements MultiFileUploadPipe
{
	/*  */
	protected List<ResourceToolActionPipe> pipes = new ArrayList<ResourceToolActionPipe>();
	
	/*  */
	protected ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");

	/**
	 * @param interactionId
	 * @param action
	 */
	public BasicMultiFileUploadPipe(String interactionId, ResourceToolAction action)
	{
		super(interactionId, action);
		pipes.add(this);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.MultiFileUploadPipe#addFile()
	 */
	public ResourceToolActionPipe addFile()
	{
		ResourceToolActionPipe newPipe = registry.newPipe(initializationId, action);
		pipes.add(newPipe);
		return newPipe;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.MultiFileUploadPipe#getPipes()
	 */
	public List<ResourceToolActionPipe> getPipes()
	{
		return new ArrayList(pipes);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.MultiFileUploadPipe#setFileCount(int)
	 */
	public void setFileCount(int count)
	{
		while(pipes.size() < count)
		{
			ResourceToolActionPipe newPipe = registry.newPipe(initializationId, action);
			pipes.add(newPipe);
		}
		
		while(pipes.size() > count)
		{
			pipes.remove(pipes.size() - 1);
		}
	}

}
