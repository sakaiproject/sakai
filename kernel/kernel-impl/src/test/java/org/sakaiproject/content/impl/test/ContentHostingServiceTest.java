package org.sakaiproject.content.impl.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class ContentHostingServiceTest extends SakaiKernelTestBase {

	private static final Log log = LogFactory.getLog(ContentHostingServiceTest.class);
	
	
	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(ContentHostingServiceTest.class))
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
	
	
	/**
	 * Checks the resources of zero bytes are handled correctly.
	 */
	public void testEmptyResources() throws Exception {
		ContentHostingService ch = org.sakaiproject.content.cover.ContentHostingService.getInstance();
		SessionManager sm = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		ContentResourceEdit cr;
		cr = ch.addResource("/emptyFileStreamed");
		cr.setContent(new ByteArrayInputStream(new byte[0]));
		ch.commitResource(cr);
		
		cr = ch.addResource("/emptyFileArray");
		cr.setContent(new byte[0]);
		ch.commitResource(cr);
		
		ContentResource resource;
		InputStream stream;
		resource = ch.getResource("/emptyFileStreamed");
		stream = resource.streamContent();
		assertEquals(0, stream.available());
		assertEquals(0, resource.getContentLength());
		assertEquals(0, resource.getContent().length);
		
		resource = ch.getResource("/emptyFileArray");
		stream = resource.streamContent();
		assertEquals(0, stream.available());
		assertEquals(0, resource.getContentLength());
		assertEquals(0, resource.getContent().length);
		
		
	}
}
