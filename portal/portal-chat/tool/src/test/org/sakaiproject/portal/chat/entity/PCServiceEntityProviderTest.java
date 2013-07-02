package org.sakaiproject.portal.chat.entity;

import junit.framework.TestCase;

public class PCServiceEntityProviderTest extends TestCase {

	// Sample formats
	public String [] formats = {"stun:user:pwd@stun.example.net",
			"turn:user:pwd@host_name.domain.com:1234",
			"stun:host.hola.com:1902",
			"turn:turn.mydomain.com"};
	// Urls
	public String [][] urls = {
			// Chrome
			{"stun:user@stun.example.net",
			"turn:user@host_name.domain.com:1234",
			"stun:host.hola.com:1902",
			"turn:turn.mydomain.com"},
			// Firefox
			{"stun:stun.example.net",
			"turn:host_name.domain.com:1234",
			"stun:host.hola.com:1902",
			"turn:turn.mydomain.com"}};
	// Credentials
	public String [] credentials = {"pwd",
			"pwd",
			"",
			""};
	
	public void testWebRTCFormat() {
		boolean verify = true;
		PCServiceEntityProvider entity = new PCServiceEntityProvider();
		for (int j=0; j<2; j++) {
			for (int k=0; k<formats.length && verify; k++) {
				PCServiceEntityProvider.PortalVideoServer pv = entity.new PortalVideoServer(formats[k],j==0?"chrome":"firefox");
				verify = verify && urls[j][k].equals(pv.url) && credentials[k].equals(pv.credential);
			}
		}
		assertTrue(verify);
	}
	
}
