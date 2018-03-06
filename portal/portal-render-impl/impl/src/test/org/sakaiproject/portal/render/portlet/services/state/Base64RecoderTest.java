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

import junit.framework.TestCase;

public class Base64RecoderTest extends TestCase
{

	private Base64Recoder encoder;

	@Override
	public void setUp()
	{
		encoder = new Base64Recoder();
	}

	public void testEncodeDecode()
	{
		String testString = "abcdefg, :!?+_-^%$";

		String uriSafe = encoder.encode(testString.getBytes());
		assertNotNull(uriSafe);
		assertEquals(-1, uriSafe.indexOf(" "));
		assertEquals(-1, uriSafe.indexOf("/"));
		assertEquals(-1, uriSafe.indexOf(":"));
		assertEquals(-1, uriSafe.indexOf("+"));
		assertEquals(-1, uriSafe.indexOf("="));
		assertEquals(-1, uriSafe.indexOf("?"));
		assertEquals(-1, uriSafe.indexOf("&"));
		byte[] bits = encoder.decode(uriSafe);

		assertEquals(testString, new String(bits));
	}

}
