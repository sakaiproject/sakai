package org.sakaiproject.portal.charon.handlers;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.tool.api.Session;

public class StaticHandlerTest extends TestCase {

	public void testGetContentType() {
		StaticHandler handler = new StaticHandler() {
			
			@Override
			public int doGet(String[] parts, HttpServletRequest req,
					HttpServletResponse res, Session session)
					throws PortalHandlerException {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		// Check we get this correct.
		assertEquals("text/javascript", handler.getContentType(new File("myfile.js")));
		assertEquals("text/javascript", handler.getContentType(new File("/somepath/to/myfile.js")));
		assertEquals("text/javascript", handler.getContentType(new File("another/path/myfile.js")));
		// Check trailing don't don't break things.
		assertEquals("application/octet-stream", handler.getContentType(new File("file.that.ends.with.dot.")));
	}
}
