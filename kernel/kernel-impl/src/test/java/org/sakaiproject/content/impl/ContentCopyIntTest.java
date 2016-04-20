package org.sakaiproject.content.impl;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentCopy;
import org.sakaiproject.content.api.ContentCopyContext;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class ContentCopyIntTest extends SakaiKernelTestBase {

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(ContentCopyIntTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}

			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}

	public void testSimpleCopy() throws Exception {
		ContentHostingService chs = (ContentHostingService)ComponentManager.get(ContentHostingService.class);
		SessionManager sessionManager = (SessionManager)ComponentManager.get(SessionManager.class);

		// set the user information into the current session
		Session sakaiSession = sessionManager.getCurrentSession();
		sakaiSession.setUserEid("admin");
		sakaiSession.setUserId("admin");
		
		String collectionId = chs.getSiteCollection("original");
		ContentCollectionEdit collection = chs.addCollection(collectionId);
		chs.commitCollection(collection);
		
		String resourceId = addHtmlFile(chs, collectionId+ "index.html", "<html><body><h1>Hello World</h1><a href='other.html'>Other File</a></body></html>");
		addHtmlFile(chs, collectionId+ "other.html", "<html><body><h1>Other File</h1></body></html>");
		
		ContentCopy contentCopy = (ContentCopy)ComponentManager.get(ContentCopy.class);
		ContentCopyContext context = contentCopy.createCopyContext("original", "new", true);
		context.addResource(resourceId);
		contentCopy.copyReferences(context);
		
		try {
			chs.getResource(chs.getSiteCollection("new")+"index.html");
			chs.getResource(chs.getSiteCollection("new")+"other.html");
		} catch (Exception e) {
			fail("Should be present now.");
		}
	}
	
	public void testCopySiteContent() throws Exception {
		ContentHostingService chs = (ContentHostingService)ComponentManager.get(ContentHostingService.class);
		SessionManager sessionManager = (SessionManager)ComponentManager.get(SessionManager.class);
		
		// set the user information into the current session
		Session sakaiSession = sessionManager.getCurrentSession();
		sakaiSession.setUserEid("admin");
		sakaiSession.setUserId("admin");
		
		String collectionId = chs.getSiteCollection("source");
		ContentCollectionEdit collection = chs.addCollection(collectionId);
		chs.commitCollection(collection);
		
		String destId = chs.getSiteCollection("dest");
		
		addHtmlFile(chs, collectionId+ "index.html", "<html><body><h1>Hello World</h1><a href='other.html'>Other File</a></body></html>");
		addHtmlFile(chs, collectionId+ "other.html", "<html><body><h1>Other File</h1></body></html>");
		
		((EntityTransferrer)chs).transferCopyEntities(collectionId, destId, null);
		
		try {
			chs.getResource(destId+"index.html");
			chs.getResource(destId+"other.html");
		} catch (Exception e) {
			fail("Should be present now.");
		}
	}

	private String addHtmlFile(ContentHostingService chs, String resourceId, String content)
			throws PermissionException, IdUsedException, IdInvalidException,
			InconsistentException, ServerOverloadException, OverQuotaException {
		ContentResourceEdit resource = chs.addResource(resourceId);
		resource.setContentType("text/html");
		resource.setContent(content.getBytes());
		chs.commitResource(resource);
		return resourceId;
	}

}
