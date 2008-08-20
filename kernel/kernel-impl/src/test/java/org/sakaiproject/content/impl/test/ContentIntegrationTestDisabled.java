package org.sakaiproject.content.impl.test;

import java.nio.ByteBuffer;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;

public class ContentIntegrationTestDisabled extends SakaiKernelTestBase
{
	private static final Log log = LogFactory.getLog(ContentIntegrationTestDisabled.class);
	
	private AuthzGroupService authzGroupService;
	private SiteService siteService;
	protected ContentHostingService contentService;
	protected ResourceTypeRegistry resourceTypeRegistry;
	
	protected String collectionId;
	protected String resourceId; 
	
	public static final int CONTENT_SIZE = 2 * (1 + Byte.MAX_VALUE - Byte.MIN_VALUE);// Math.round(Math.pow(2.0, Byte.SIZE)); 
	protected String TEST_TEXT = "<ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789>";
	protected byte[] byteArray = new byte[4096];


	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(ContentIntegrationTestDisabled.class))
		{
			protected void setUp() throws Exception 
			{
				log.debug("starting oneTimeSetup");
				oneTimeSetup(null);
				log.debug("finished oneTimeSetup");
			}
			protected void tearDown() throws Exception 
			{
				log.debug("starting tearDown");
				oneTimeTearDown();
				log.debug("finished tearDown");
			}
		};
		return setup;
	}
	
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
