/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/master/trunk/header.java $
 * $Id: header.java 9220 2006-05-09 23:09:28Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.sakaiproject.springframework.orm.hibernate.impl;

import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.cover.VendorHbmTransformer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class AdditionalHibernateMappingsImpl implements AdditionalHibernateMappings, Comparable
{
	protected final transient Log logger = LogFactory.getLog(getClass());

	private Resource[] mappingLocations;

	private Integer sortOrder = Integer.valueOf(Integer.MAX_VALUE);

    public void setMappingResources(String[] mappingResources)
	{
		this.mappingLocations = new Resource[mappingResources.length];
		for (int i = 0; i < mappingResources.length; i++)
		{
			this.mappingLocations[i] = new ClassPathResource(mappingResources[i].trim());
		}
	}

	public Resource[] getMappingLocations()
	{
		return mappingLocations;
	}

	public void processConfig(Configuration config) throws IOException, MappingException
	{
		for (int i = 0; i < this.mappingLocations.length; i++)
		{
			try {
            logger.info("Loading hbm: " + mappingLocations[i]);
            config.addInputStream(VendorHbmTransformer.getTransformedMapping(this.mappingLocations[i].getInputStream()));
			} catch (MappingException me) {
				throw new MappingException("Failed to load "+ this.mappingLocations[i], me);
			}
		}
	}

	public int compareTo(Object o)
	{
		return getSortOrder().compareTo(((AdditionalHibernateMappingsImpl) o).getSortOrder());
	}

	public Integer getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder)
	{
		this.sortOrder = sortOrder;
	}

}
