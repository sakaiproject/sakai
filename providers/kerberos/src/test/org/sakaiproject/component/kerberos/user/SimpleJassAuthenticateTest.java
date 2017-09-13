/**
 * Copyright (c) 2005-2009 The Apereo Foundation
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
package org.sakaiproject.component.kerberos.user;


/**
 * A couple of tests where we don't verify the service ticket.
 * @author Matthew Buckett
 *
 */
public class SimpleJassAuthenticateTest extends AbstractAuthenticateTest {
	
	protected JassAuthenticate jass;

	public void setUp() throws Exception {
		super.setUp();
		jass = new JassAuthenticate("KerberosAuthentication");
	}
	
	public void testGood() {
		assertTrue(jass.attemptAuthentication(goodUser, goodPass));
	}
	
	public void testBad() {
		assertFalse(jass.attemptAuthentication(badUser, badPass));
	}
}
