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
