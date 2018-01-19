/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.springframework.orm.hibernate;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

/**
 * When the kernel starts up it will ask the component manager for all instances of this interface and then
 * it will allow them all to add to the central Hibernate session factory configuration.
 *
 * @see AddableSessionFactoryBean
 */
public interface AdditionalHibernateMappings extends Comparable<AdditionalHibernateMappings>
{
	Integer getSortOrder();
	void processAdditionalMappings(LocalSessionFactoryBuilder localSessionFactoryBuilder) throws IOException;
	void setAnnotatedClasses(Class<?>... annotatedClasses);
	void setAnnotatedPackages(String... annotatedPackages);
	void setCacheableMappingLocations(Resource... mappingLocations);
	void setMappingDirectoryLocations(Resource... mappingLocations);
	void setMappingJarLocations(Resource... mappingLocations);
	void setMappingLocations(Resource... mappingLocations);
	void setMappingResources(String... mappingResources);
	void setPackagesToScan(String... packagesToScan);
	void setSortOrder(Integer sortOrder);

}
