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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

public class PortletStateTest extends TestCase
{

	public void testSerialization() throws IOException, ClassNotFoundException
	{
		PortletState state = new PortletState("id");
		state.setPortletMode(PortletMode.VIEW);
		state.setWindowState(WindowState.MAXIMIZED);

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bao);

		out.writeObject(state);

		ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bai);
		PortletState alter = (PortletState) in.readObject();

		assertEquals(state, alter);

	}

}
