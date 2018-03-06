/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.portal.chat.entity;

import junit.framework.TestCase;

public class PCServiceEntityProviderTest extends TestCase {

	// Sample formats
	public String [] formats = {"stun:user@um.es:pwd@stun.example.net",
			"turn:user:pwd@host_name.domain.com:1234",
			"stun:host.hola.com:1902",
			"turn:turn.mydomain.com",
			"stun:user:pwd@127.0.0.1",
			"turn:127.0.0.1:1234"};
	// Hosts
	public String [] hosts = {"stun.example.net",
			"host_name.domain.com:1234",
			"host.hola.com:1902",
			"turn.mydomain.com",
			"127.0.0.1",
			"127.0.0.1:1234"};
	// Credentials
	public String [] credentials = {"pwd","pwd","","","pwd",""};
	// Hosts
	public String [] users = {"user@um.es","user","","","user",""};
	
	public void testWebRTCFormat() {
		boolean verify = true;
		PCServiceEntityProvider entity = new PCServiceEntityProvider();
		for (int k=0; k<formats.length && verify; k++) {
			PCServiceEntityProvider.PortalVideoServer pv = entity.new PortalVideoServer(formats[k]);
			verify = verify 
					&& ((k%2==0)?"stun":"turn").equals(pv.protocol)
					&& hosts[k].equals(pv.host) 
					&& credentials[k].equals(pv.credential) 
					&& users[k].equals(pv.username);
		}
		assertTrue(verify);
	}
	
}
