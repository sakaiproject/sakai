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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.metadata.logic.MetadataService;
import org.sakaiproject.content.metadata.model.MetadataType;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ToolManager;

/**
 * @author Colin Hebert
 */
@Slf4j
public class MetadataServiceFromContent implements MetadataService
{

	protected final ContentHostingService contentHostingService;
	protected final SecurityService securityService;
	protected final ToolManager toolManager;
	protected final MetadataParser parser;
	
	private String localMetadataConfigFile = "metadata/metadata.json";
	private String globalMetadataConfigFile = "/metadata/metadata.json";

	public MetadataServiceFromContent(ContentHostingService contentHostingService, SecurityService securityService, ToolManager toolManager, MetadataParser parser)
	{
		this.contentHostingService = contentHostingService;
		this.securityService = securityService;
		this.toolManager = toolManager;
		this.parser = parser;
	}

	public void setLocalMetadataConfigFile(String localMetadataConfigFile)
	{
		this.localMetadataConfigFile = localMetadataConfigFile;
	}

	public void setGlobalMetadataConfigFile(String globalMetadataConfigFile)
	{
		this.globalMetadataConfigFile = globalMetadataConfigFile;
	}

	public List<MetadataType> getMetadataAvailable(String resourceType)
	{
		try
		{
			InputStream is = forceAccessResource(getGlobalMetaContent()).streamContent();
			//TODO find a way to filter based on resourceType (should be in an AbstractMetadataService?)
			return parser.parse(is);
		}
		catch (IdUnusedException e)
		{
			log.debug("The metadata configuration file doesn't exist", e);
			return Collections.emptyList();
		}
		catch (TypeException e)
		{
			log.debug("The metadata configuration file doesn't exist", e);
			return Collections.emptyList();
		}
		catch (Exception e)
		{
			//No exception coming from the file loading shall get out of this API !
			log.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	public List<MetadataType> getMetadataAvailable(String siteId, String resourceType)
	{
		List<MetadataType> metadataTypes = new ArrayList<MetadataType>();
		metadataTypes.addAll(getMetadataAvailable(resourceType));

		try
		{
			InputStream is = forceAccessResource(getSiteMetadataConfigFile(siteId)).streamContent();
			//TODO find a way to filter based on resourceType (should be in an AbstractMetadataService?)

			metadataTypes.addAll(parser.parse(is));
		}
		catch (IdUnusedException e)
		{
			log.debug("The metadata configuration file doesn't exist", e);
		}
		catch (TypeException e)
		{
			log.debug("The metadata configuration file doesn't exist", e);
		}
		catch (Exception e)
		{
			//No exception coming from the file loading shall get out of this API !
			log.error(e.getMessage(), e);
		}

		return metadataTypes;
	}

	private ContentResource forceAccessResource(String contentId) throws IdUnusedException, TypeException, PermissionException
	{
		SecurityAdvisor securityAdvisor = tempReadOnlyAdvisor(contentHostingService.getReference(contentId));
		securityService.pushAdvisor(securityAdvisor);
		try
		{
			return contentHostingService.getResource(contentId);
		}
		finally
		{
			securityService.popAdvisor(securityAdvisor);
		}
	}

	/**
	 * Generate a temporary advisor to enable reading on a specific content
	 * <p/>
	 * Any use of this advisor should be temporary as it could create a security breach
	 *
	 * @param contentReference Content to unlock
	 * @return an advisor allowing read access on the specified content
	 */
	private SecurityAdvisor tempReadOnlyAdvisor(final String contentReference)
	{
		return new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				//TODO Check userId too ?
				if (ContentHostingService.AUTH_RESOURCE_READ.equals(function) && contentReference.equals(reference))
					return SecurityAdvice.ALLOWED;
				else
					return SecurityAdvice.PASS;
			}
		};
	}

	protected String getSiteMetadataConfigFile(String siteId)
	{
		String siteRoot = contentHostingService.getSiteCollection(siteId);
		return siteRoot + localMetadataConfigFile;
	}

	protected String getGlobalMetaContent()
	{
		return globalMetadataConfigFile;
	}
}
