package org.sakaiproject.component.kerberos.user;


public class VerifyJassAuthenticateTest extends AbstractAuthenticateTest {
	
	protected JassAuthenticate jass;

	public void setUp() throws Exception {
		super.setUp();
		jass = new JassAuthenticate(servicePrincipal, "ServiceKerberosAuthentication", "KerberosAuthentication");
	}
	
	public void testGood() {
		assertTrue(jass.attemptAuthentication(goodUser, goodPass));
	}
	
	public void testBad() {
		assertFalse(jass.attemptAuthentication(badUser, badPass));
	}
}
