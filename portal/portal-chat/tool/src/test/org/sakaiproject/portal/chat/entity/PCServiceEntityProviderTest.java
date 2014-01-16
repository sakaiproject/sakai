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
