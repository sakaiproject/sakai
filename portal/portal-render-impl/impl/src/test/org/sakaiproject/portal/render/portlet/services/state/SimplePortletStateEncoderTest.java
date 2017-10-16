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

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

public class SimplePortletStateEncoderTest extends TestCase
{

	private SimplePortletStateEncoder encoder;

	@Override
	public void setUp()
	{
		encoder = new SimplePortletStateEncoder();
		encoder.setUrlSafeEncoder(new Base64Recoder());
	}

	public void testEncodeDecode()
	{
		PortletState state = new PortletState("id");
		state.setAction(true);
		state.setSecure(true);
		state.setWindowState(WindowState.MAXIMIZED);
		state.setPortletMode(PortletMode.EDIT);

		String uriSafe = encoder.encode(state);
		PortletState read = encoder.decode(uriSafe);
		assertEquals(state.getId(), read.getId());
		assertEquals(state.isAction(), read.isAction());
		assertEquals(state.isSecure(), read.isSecure());
		assertEquals(state.getWindowState(), read.getWindowState());
		assertEquals(state.getPortletMode(), read.getPortletMode());
	}

}
