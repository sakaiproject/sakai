/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.impl.test;

import java.nio.ByteBuffer;

import lombok.extern.slf4j.Slf4j;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;

@Slf4j
public class ContentIntegrationTestDisabled extends SakaiKernelTestBase
{
	private AuthzGroupService authzGroupService;
	private SiteService siteService;
	protected ContentHostingService contentService;
	protected ResourceTypeRegistry resourceTypeRegistry;
	
	protected String collectionId;
	protected String resourceId; 
	
	public static final int CONTENT_SIZE = 2 * (1 + Byte.MAX_VALUE - Byte.MIN_VALUE);// Math.round(Math.pow(2.0, Byte.SIZE)); 
	protected String TEST_TEXT = "<ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789>";
	protected byte[] byteArray = new byte[4096];


	@BeforeClass
	public static void beforeClass() {
		try {
            log.debug("starting oneTimeSetup");
			oneTimeSetup();
            log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	@Before
	public void setUp() throws Exception 
	{
		log.debug("Setting up a ContentIntegrationTest test");
		
		// Connect to the required services
//		authzGroupService = (AuthzGroupService) getService(AuthzGroupService.class.getName());
//		siteService = (SiteService) getService(SiteService.class.getName());
//		contentService = (ContentHostingService) getService(ContentHostingService.class.getName());
		
		ByteBuffer buf = ByteBuffer.allocate(CONTENT_SIZE);
		
		for(byte b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++)
		{
			buf.put(b, b);
		}
		
		for(int i = 0; i < CONTENT_SIZE/2; i++)
		{
			buf.put(CONTENT_SIZE - i, buf.get(i));
		}
		
		
		
		log.debug("Done setting up a ContentIntegrationTest test");
	}
	
	@After
	public void tearDown() throws Exception 
	{
		log.debug("Tearing down a ContentIntegrationTest test");
		log.debug("Done tearing down a ContentIntegrationTest test");

	}
	
	/*
	protected InputStream getInputStream(int contentLength)
	{
		
	}
	*/

	/**
     * @return the authzGroupService
     */
    public AuthzGroupService getAuthzGroupService()
    {
    	return authzGroupService;
    }

	/**
     * @param authzGroupService the authzGroupService to set
     */
    public void setAuthzGroupService(AuthzGroupService authzGroupService)
    {
    	this.authzGroupService = authzGroupService;
    }

	/**
     * @return the siteService
     */
    public SiteService getSiteService()
    {
    	return siteService;
    }

	/**
     * @param siteService the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
    	this.siteService = siteService;
    }

	/**
     * @return the contentService
     */
    public ContentHostingService getContentService()
    {
    	return contentService;
    }

	/**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentHostingService contentService)
    {
    	this.contentService = contentService;
    }

	/**
     * @return the resourceTypeRegistry
     */
    public ResourceTypeRegistry getResourceTypeRegistry()
    {
    	return resourceTypeRegistry;
    }

	/**
     * @param resourceTypeRegistry the resourceTypeRegistry to set
     */
    public void setResourceTypeRegistry(ResourceTypeRegistry resourceTypeRegistry)
    {
    	this.resourceTypeRegistry = resourceTypeRegistry;
    }

}
