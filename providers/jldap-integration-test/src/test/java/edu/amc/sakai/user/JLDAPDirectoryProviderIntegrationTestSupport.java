/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

/**
 * Collects the not-insignificant quantity utility methods related to
 * configuring {@link JLDAPDirectoryProviderIntegrationTest}. Is not
 * itself a test case, but is set up and torn down in the usual JUnit
 * fashion. 
 * 
 * @author dmccallum@unicon.net
 *
 */
class JLDAPDirectoryProviderIntegrationTestSupport {

	public static final String TEST_CONFIG_ACCESSOR_BEAN_NAME = "propertiesConfigurer";
	public static final String[] TEST_CONTEXT_CONFIG_FILE_NAMES = 
		{ "jldap-test-context.xml", "jldap-test-context-local.xml" };
	ConfigurableApplicationContext testContext;
	PropertyResolver testConfigAccessor;
	JLDAPDirectoryProvider udp;
	
	
	/**
     * Creates and caches an {@link ApplicationContext}, loading bean defs from
     * "test-context.xml" on the classpath. Still TBD as to whether or not this 
     * should be a one-time operation per test classloader. Is potentially an 
     * expensive operation.
     * 
     * <p>Assumes the "parent" Sakai {@link ApplicationContext} can be
     * retrieved from {@link ComponentManager}.</p>
     *
     * @see #getSakaiApplicationContext()
     * @see #newTestApplicationContext()
     */
    void setUpTestApplicationContext() {
    	ApplicationContext sakaiAC = getSakaiApplicationContext();
        testContext = newTestApplicationContext(sakaiAC);
    }
    
    
	ApplicationContext getSakaiApplicationContext() {
		// TODO Auto-generated method stub
		return ((SpringCompMgr)ComponentManager.getInstance()).getApplicationContext();
	}
	
	ConfigurableApplicationContext newTestApplicationContext(ApplicationContext parent) {
		List existingFiles = new ArrayList();
		for ( String fileName : TEST_CONTEXT_CONFIG_FILE_NAMES ) {
			try {
				ResourceUtils.getFile(ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + fileName));
				existingFiles.add(fileName);
			} catch ( FileNotFoundException e ) {
				//
			}
		}
		String[] existingFilesArray = new String[existingFiles.size()];
		return new ClassPathXmlApplicationContext(
				(String[])existingFiles.toArray(existingFilesArray), parent);
	}

	/**
     * Caches the bean named {@link #TEST_CONFIG_ACCESSOR_BEAN_NAME}. This
     * bean is used to resolve test configuration properties.
     */
    void setUpTestConfiguration() {
    	testConfigAccessor = (PropertyResolver)testContext.getBean("propertiesConfigurer");
    }
    
    /**
     * Caches the actual object under test and any collaborators and
     * data preconditions. Currently, this is limited to looking up
     * a {@link #JLDAPDirectoryProvider} in the current test context.
     * 
     * @throws Exception
     */
    void setUpTestFixture() throws Exception {
    	// obtain the directory provider
    	udp = 
			(JLDAPDirectoryProvider)
			testContext.getBean("org.sakaiproject.user.api.UserDirectoryProvider");
		if (udp == null) {
			throw new Exception("unable to get JLDAPDirectoryProvider");
		}
    }
	
    /**
     * Controller method which invokes the following in this order:
     * 
     * <ol>
     *   <li>{@link #setUpTestApplicationContext()}</li>
     *   <li>{@link #setUpTestConfiguration()}</li>
     *   <li>{@link #setUpTestFixture()}</li>
     * </ol>
     * 
     * @throws Exception
     */
	void setUp() throws Exception {
        setUpTestApplicationContext();
        setUpTestConfiguration();
        setUpTestFixture();
	}
	
	/**
	 * Controller method which invokes the following in this order
	 * (reverse of {@link #setUp()}:
	 * 
	 * <ol>
	 *   <li>{@link #tearDownTestFixture()}</li>
	 *   <li>{@link #tearDownTestConfiguration()}</li>
	 *   <li>{@link #tearDownTestApplicationContext()}</li>
	 * </ol>
	 * 
	 * @throws Exception
	 */
	void tearDown() throws Exception {
		tearDownTestFixture();
		tearDownTestConfiguration();
		tearDownTestApplicationContext();
	}
	
	/**
	 * Implemented to clear the current {@link JLDAPDirectoryProvider}'s
	 * cache. This is necessary because while our test context is
	 * set up and torn down for each test method, we are typically
	 * retrieving the actual UDP from the test context's parent context,
	 * which is typically persistent for the lifetime of the current
	 * <code>TestSuite</code>
	 */
	void tearDownTestFixture() {
		if ( udp == null ) {
			return;
		}
		udp.clearCache();
		udp = null; // paranoia
	}

	/**
	 * Simply clears the current reference to the test config
	 * {@link PropertyResolver}.
	 */
	void tearDownTestConfiguration() {
		testConfigAccessor = null; // paranoia
	}
	
	/**
	 * Invokes {@link ConfigurableApplicationContext#close()} on the
	 * currently cached test context.
	 */
	void tearDownTestApplicationContext() {
    	if ( testContext == null ) {
    		return;
    	}
    	
    	try {
    		testContext.close();
    	} finally {
    		testContext = null; // paranoia
    	}
    }
	
	/**
	 * Access a configured property from the current test context.
	 * This allows properties to be defined with interpolated values
	 * (e.g. name=${other.property.value}).
	 * 
	 * @see PropertyResolver#getStringValue(String)
	 * @param key name of the property to resolve
	 * @return resolved property name
	 */
	String getConfiguredValue(String key) {
    	return testConfigAccessor.getStringValue(key);
    }
	
	/**
	 * Instantiate a {@link UserEditStub} and assign the given
	 * "enterprise ID" (i.e. "eid") value. Object is otherwise
	 * not initialized.
	 * 
	 * @see UserEditStub#SimpleUserEdit()
	 * @param eid the enterprise ID to assign to the new object.
	 * @return
	 */
	UserEditStub newUserEditStub(String eid) {
		UserEditStub user =	new UserEditStub();
		user.setEid(eid);
		return user;
	}
	
	/**
	 * Retrieve a {@link UserEditStub} from the current test context.
	 * The underlying bean definition should be a prototype such that
	 * this call always returns a new {@link UserEditStub} object.
	 * 
	 * @param beanName
	 * @return
	 */
	UserEditStub getConfiguredUserEditStub(String beanName) {
		return (UserEditStub) testContext.getBean(beanName);
	}
    
}
