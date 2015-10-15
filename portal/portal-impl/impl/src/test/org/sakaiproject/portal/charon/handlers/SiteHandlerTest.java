/**********************************************************************************
 * $URL: https://github.com/sakaiproject/sakai/tree/master/portal/portal-impl/impl/src/java/org/sakaiproject/portal/charon/handlers/SiteHandler.java $
 * $Id: SiteHandlerTest.java 2015-10-15 12:44:00Z proyectos@seduerey.com, jjmerono@um.es $
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

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import junit.framework.TestCase;
import org.sakaiproject.component.cover.ComponentManager;

public class SiteHandlerTest extends TestCase {

	private static Map<String,String> useCases = new HashMap<String,String>();

	public void testRemoveHeaderTitle() {
		SiteHandler sh = new SiteHandler();
		for (Entry<String,String> entry:useCases.entrySet()) {
			assertEquals(entry.getValue(), sh.removeTitleFromHeader(entry.getKey()));	
		}
		
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ComponentManager.testingMode = true;

		useCases.put("<html><head>Before title<title>This is the title</title>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<ttle>This is the title</title>after title</head></html>",
					"<html><head>Before title<ttle>This is the title</title>after title</head></html>");

		useCases.put("<html><head>Before title<Title>This is the title</Title>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<TITLE>This is the title</TITLE>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<title lang=\"es-ES\">This is the title</title>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<title>This is the title after title</head></html>",
					"<html><head>Before title<title>This is the title after title</head></html>");

		useCases.put("<html><head>Before title<TITLE>This is the title</title>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<title>This is<title> the</title> title</title>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<title>This is the</title> title</title>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<title>This<title> is the title</title>after title</head></html>",
					"<html><head>Before titleafter title</head></html>");

		useCases.put("<html><head>Before title<title>This is\nthe title</title>after\ntitle</head></html>",
					"<html><head>Before titleafter\ntitle</head></html>");

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ComponentManager.shutdown();
	}
}
