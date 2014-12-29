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
