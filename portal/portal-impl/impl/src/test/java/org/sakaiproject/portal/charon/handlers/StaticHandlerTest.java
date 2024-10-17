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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.charon.PortalTestConfiguration;
import org.sakaiproject.tool.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collections;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PortalTestConfiguration.class})
public class StaticHandlerTest {

	public static boolean setupOnceCompleted = false;

	@Autowired private ApplicationContext applicationContext;

	private StaticHandler staticHandler;

	@Before
	public void setUp() {
		if (!setupOnceCompleted) {
			// Setup a fake webapp so spring injection will happen
			ServletContext servletContext = mock(ServletContext.class);
			Mockito.when(servletContext.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
			Mockito.when(servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());

			WebApplicationContext webApplicationContext = new GenericWebApplicationContext(servletContext);
			((AbstractApplicationContext) webApplicationContext).setParent(applicationContext);
			ContextLoader contextLoader = new ContextLoader(webApplicationContext);
			contextLoader.initWebApplicationContext(servletContext);

			setupOnceCompleted = true;
		}

		this.staticHandler = new StaticHandler() {
			@Override
			public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session) throws PortalHandlerException {
				return 0;
			}
		};
	}

	@Test
	public void testGetContentType() {
		// Check we get this correct.
		Assert.assertEquals("text/javascript", staticHandler.getContentType(new File("myfile.js").getName()));
		Assert.assertEquals("text/javascript", staticHandler.getContentType(new File("/somepath/to/myfile.js").getName()));
		Assert.assertEquals("text/javascript", staticHandler.getContentType(new File("another/path/myfile.js").getName()));
		// Check trailing don't don't break things.
		Assert.assertEquals("application/octet-stream", staticHandler.getContentType(new File("file.that.ends.with.dot.").getName()));
	}
}
