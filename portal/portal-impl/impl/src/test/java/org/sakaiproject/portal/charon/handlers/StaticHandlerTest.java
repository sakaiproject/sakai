/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-impl/impl/src/java/org/sakaiproject/portal/charon/CharonPortal.java $
 * $Id: CharonPortal.java 122221 2013-04-04 21:24:12Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.handlers;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.component.cover.ComponentManager;
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
		assertEquals("text/javascript", handler.getContentType(new File("myfile.js").getName()));
		assertEquals("text/javascript", handler.getContentType(new File("/somepath/to/myfile.js").getName()));
		assertEquals("text/javascript", handler.getContentType(new File("another/path/myfile.js").getName()));
		// Check trailing don't don't break things.
		assertEquals("application/octet-stream", handler.getContentType(new File("file.that.ends.with.dot.").getName()));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ComponentManager.testingMode = true;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ComponentManager.shutdown();
	}
}
