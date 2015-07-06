/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.cover;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.metadata.logic.MetadataService;

/**
 * Static cover for MetadataService
 *
 * @author Colin Hebert
 */
@Deprecated
public final class ContentMetadataService
{
	private static MetadataService instance;

	/**
	 * Access the component instance: special cover only method.
	 * <p/>
	 * Automatically caches the instance if required.
	 *
	 * @return the component instance.
	 */
	public static MetadataService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (instance == null)
				instance = (MetadataService) ComponentManager.get(MetadataService.class);
			return instance;
		} else
		{
			return (MetadataService) ComponentManager.get(MetadataService.class);
		}
	}
}
