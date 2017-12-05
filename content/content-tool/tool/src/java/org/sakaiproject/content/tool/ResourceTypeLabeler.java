/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.content.tool;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;

@Slf4j
public class ResourceTypeLabeler
{
	public String getLabel(ResourceToolAction action)
	{
		String label = null;
		if(action == null)
		{
			log.info("Null action passed to labeler ");
			label = "";
		}
		else
		{
			label = action.getLabel();
		}
		if(label == null)
		{
			switch(action.getActionType())
			{
				case NEW_UPLOAD:
					label = ResourcesAction.trb.getString("create.uploads");
					break;
				case NEW_FOLDER:
					label = ResourcesAction.trb.getString("create.folder");
					break;
				case NEW_URLS:
					label = ResourcesAction.trb.getString("create.urls");
					break;
				case CREATE:
					ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
					ResourceType typedef = registry.getType(action.getTypeId());
					String[] args = { typedef.getLabel() };
					label = ResourcesAction.trb.getFormattedMessage("create.unknown", args);
					break;
				case COPY:
					label = ResourcesAction.trb.getString("action.copy");
					break;
				case DUPLICATE:
					label = ResourcesAction.trb.getString("action.duplicate");
					break;
				case DELETE:
					label = ResourcesAction.trb.getString("action.delete");
					break;
				case MOVE:
					label = ResourcesAction.trb.getString("action.move");
					break;
				case VIEW_METADATA:
					label = ResourcesAction.trb.getString("action.info");
					break;
				case REVISE_METADATA:
					label = ResourcesAction.trb.getString("action.props");
					break;
				case VIEW_CONTENT:
					label = ResourcesAction.trb.getString("action.access");
					break;
				case REVISE_CONTENT:
					label = ResourcesAction.trb.getString("action.revise");
					break;
				case REPLACE_CONTENT:
					label = ResourcesAction.trb.getString("action.replace");
					break;
				case PASTE_COPIED:
					label = ResourcesAction.trb.getString("action.pastecopy");
					break;
				case PASTE_MOVED:
					label = ResourcesAction.trb.getString("action.pastemove");
					break;
				case PRINT_FILE:
					label = ResourcesAction.trb.getString("action.printfile");
					break;
				case MAKE_SITE_PAGE:
					label = ResourcesAction.trb.getString("action.makesitepage");
					break;
				default:
					log.info("No label provided for ResourceToolAction: " + action.getTypeId() + ResourceToolAction.ACTION_DELIMITER + action.getId());
					label = action.getId();
					break;
			}
		}
		return label;
	}
}
