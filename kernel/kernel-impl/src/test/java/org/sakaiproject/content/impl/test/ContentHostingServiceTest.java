package org.sakaiproject.content.impl.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class ContentHostingServiceTest extends SakaiKernelTestBase {

	private static final String SIMPLE_FOLDER1 = "/admin/folder1/";
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
	
	
	public void testSaveRetriveFolder() {
		ContentHostingService ch = org.sakaiproject.content.cover.ContentHostingService.getInstance();
		SessionManager sm = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		
		try {
			ContentCollectionEdit ce = ch.addCollection(SIMPLE_FOLDER1);
			ch.commitCollection(ce);
			log.info("commited folder:" + ce.getId());
		} catch (IdUsedException e) {
			e.printStackTrace();
			fail("Got an id Used exception!");
		} catch (IdInvalidException e) {
			e.printStackTrace();
			fail("That id is invalid!");
		} catch (PermissionException e) {
			e.printStackTrace();
			fail();
		} catch (InconsistentException e) {
			e.printStackTrace();
			fail();
		}
		
		
		//now try retrieve the folder
		try {
			ContentCollection cc = ch.getCollection(SIMPLE_FOLDER1);
			assertNotNull(cc);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//lets test saving a utf8
		String utf8Folder = String.valueOf("\u6c92\u6709\u5df2\u9078\u8981\u522a\u9664\u7684\u9644\u4ef6");
		String utfId = "/admin/" + utf8Folder + "/";
		try {
			ContentCollectionEdit cce = ch.addCollection(utfId);
			ch.commitCollection(cce);
			log.info("commited folder:" + cce.getId());
		} catch (IdUsedException e) {
			e.printStackTrace();
		} catch (IdInvalidException e) {
			e.printStackTrace();
			fail();
		} catch (PermissionException e) {
			e.printStackTrace();
			fail();
		} catch (InconsistentException e) {
			e.printStackTrace();
			fail();
		}
		
		//now try retrieve the folder
		try {
			ContentCollection cc = ch.getCollection(utfId);
			assertNotNull(cc);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
	}
	
	/**
	 * See SAK-17308 test for cases if resources saved in tf8 folders
	 */
	public void testUtfFolders() {
		//lets test saving a utf8
		ContentHostingService ch = org.sakaiproject.content.cover.ContentHostingService.getInstance();
		SessionManager sm = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");

		String utf8Folder = String.valueOf("\u6c92\u6709\u5df2\u9078\u8981\u522a\u9664\u7684\u9644\u4ef6");
		String utfId = "/admin/" + utf8Folder + "/";
		String resId = null;
		String fileName = "someFile";
		String fileExtension = ".txt";
		try {
			ContentResourceEdit cre = ch.addResource(utfId, fileName, fileExtension, 10);
			ch.commitResource(cre);
			resId = cre.getId();
			log.info("saved: " + cre.getId());
			log.info("url is: " + cre.getUrl());
			log.info("relative url:" + cre.getUrl(true));
			String urlDecode = URLDecoder.decode(cre.getUrl(true), "utf8");
			log.info("decoded url: " + urlDecode);
			String url = "/access/content" + utfId + fileName + fileExtension;
			assertEquals(url, urlDecode);
		} catch (PermissionException e) {
			e.printStackTrace();
			fail();
		} catch (IdUniquenessException e) {
			e.printStackTrace();
			fail();
		} catch (IdLengthException e) {
			e.printStackTrace();
			fail();
		} catch (IdInvalidException e) {
			e.printStackTrace();
			fail();
		} catch (IdUnusedException e) {
			e.printStackTrace();
			fail();
		} catch (OverQuotaException e) {
			e.printStackTrace();
			fail();
		} catch (ServerOverloadException e) {
			e.printStackTrace();
			fail();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//now lets try retrieve it
		try {
			ContentResource res = ch.getResource(resId);
			assertNotNull(res);
		} catch (PermissionException e) {
			e.printStackTrace();
			fail();
		} catch (IdUnusedException e) {
			e.printStackTrace();
			fail();
		} catch (TypeException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
	public void testDeleteResource() {
		ContentHostingService ch = org.sakaiproject.content.cover.ContentHostingService.getInstance();
		SessionManager sm = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		try {
			ch.removeResource("noSuchResource");
			fail();
		} catch (PermissionException e) {
			e.printStackTrace();
			fail();
		} catch (IdUnusedException e) {
			e.printStackTrace();
		} catch (TypeException e) {
			e.printStackTrace();
			fail();
		} catch (InUseException e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
}
