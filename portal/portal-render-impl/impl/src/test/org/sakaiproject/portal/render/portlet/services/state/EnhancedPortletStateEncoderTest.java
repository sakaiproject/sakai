/**
 * Copyright (c) 2003-2007 The Apereo Foundation
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
package org.sakaiproject.portal.render.portlet.services.state;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import lombok.extern.slf4j.Slf4j;
import junit.framework.TestCase;

@Slf4j
public class EnhancedPortletStateEncoderTest extends TestCase
{

	private EnhancedPortletStateEncoder encoder;

	@Override
	public void setUp()
	{
		encoder = new EnhancedPortletStateEncoder();
		encoder.setUrlSafeEncoder(new BasicWebRecoder());
	}

	public void testEncodeDecode()
	{
		Map parms = new HashMap();
		parms.put("one", "oneValue");
		parms.put("two", "twoValue");
		parms.put("three", new String[] { "threeOne", "threeTwo", null });

		PortletState state = new PortletState("id");
		state.setAction(true);
		state.setSecure(true);
		state.setWindowState(WindowState.MAXIMIZED);
		state.setPortletMode(PortletMode.EDIT);
		state.setParameters(parms);

		String uriSafe = encoder.encode(state);
		
		log.info("URI is "+uriSafe);
		
		assertNotNull(uriSafe);
		assertEquals(-1, uriSafe.indexOf(" "));
		assertEquals(-1, uriSafe.indexOf("/"));
		assertEquals(-1, uriSafe.indexOf(":"));
		assertEquals(-1, uriSafe.indexOf("="));
		assertEquals(-1, uriSafe.indexOf("?"));
		assertEquals(-1, uriSafe.indexOf("&"));

		PortletState read = encoder.decode(uriSafe);

		assertEquals(state.getId(), read.getId());
		assertEquals(state.isAction(), read.isAction());
		assertEquals(state.isSecure(), read.isSecure());
		assertEquals(state.getWindowState(), read.getWindowState());
		assertEquals(state.getPortletMode(), read.getPortletMode());
		assertEquals(3, read.getParameters().size());

		String[] one = (String[]) read.getParameters().get("one");
		assertEquals(1, one.length);
		assertEquals("oneValue", one[0]);

		String[] two = (String[]) read.getParameters().get("two");
		assertEquals(1, two.length);
		assertEquals("twoValue", two[0]);

		String[] three = (String[]) read.getParameters().get("three");
		assertEquals(2, three.length);
		assertEquals("threeOne", three[0]);
		assertEquals("threeTwo", three[1]);

	}
}
