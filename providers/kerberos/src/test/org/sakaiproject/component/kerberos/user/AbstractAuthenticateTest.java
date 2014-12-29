package org.sakaiproject.component.kerberos.user;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

public abstract class AbstractAuthenticateTest extends TestCase {

	
	protected String servicePrincipal;
	
	protected String goodUser;
	protected String goodPass;
	
	protected String badUser;
	protected String badPass;
	
	protected void setUp() throws Exception {
		super.setUp();
		Properties props = new Properties();
		try {
			props.load(getClass().getResourceAsStream("/test.properties"));
		} catch (IOException e) {
			throw new IllegalStateException("Can't load users file.", e);
		}
		
		System.setProperty("java.security.auth.login.config", getClass().getResource("/").getFile()+"sakai-jaas.conf");
		
		servicePrincipal = props.getProperty("service.principal");
		
		goodUser = props.getProperty("user.good.username");
		goodPass = props.getProperty("user.good.password");
		badUser = props.getProperty("user.bad.username");
		badPass = props.getProperty("user.bad.password");
	}
}
