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

package org.sakaiproject.content.metadata.logic;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.content.metadata.logic.MetadataService;
import org.sakaiproject.content.metadata.model.MetadataType;

/**
 * Created by IntelliJ IDEA.
 * User: oucs0164
 * Date: 01/02/2012
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
public class MetadataServiceAggregator implements MetadataService
{
	private final List<MetadataService> metadataServices;

	public MetadataServiceAggregator(List<MetadataService> metadataServices) {this.metadataServices = metadataServices;}

	public List<MetadataType> getMetadataAvailable(String resourceType)
	{
		List<MetadataType> metadataTypes = new ArrayList<MetadataType>();
		for (MetadataService metadataService : metadataServices)
		{
			metadataTypes.addAll(metadataService.getMetadataAvailable(resourceType));
		}
		return metadataTypes;
	}

	public List<MetadataType> getMetadataAvailable(String siteId, String resourceType)
	{
		List<MetadataType> metadataTypes = new ArrayList<MetadataType>();
		for (MetadataService metadataService : metadataServices)
		{
			metadataTypes.addAll(metadataService.getMetadataAvailable(siteId, resourceType));
		}
		return metadataTypes;
	}
}
