/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import junit.framework.TestCase;

/**
 * 
 * @author dmccallum@unicon.net
 *
 */
public class JLDAPDirectoryProviderTest extends TestCase {

	private JLDAPDirectoryProvider provider;
	
	protected void setUp() {
		provider = new JLDAPDirectoryProvider();
		provider.init();
	}
	
	public void testSupportsCaseSensitiveCacheKeys() {
		// some "local constants"
		final String EID = "some-eid";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(true);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(EID);
		
		assertSame("Should have failed to find cached objecct", cachedObject, 
				retrievedObject);
	
	}
	
	
	public void testSupportsCaseSensitiveCacheKeys_Negative() {
		
		// some "local constants"
		final String CACHED_EID = "some-eid";
		final String MIXED_CASE_EID = "sOmE-eId";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(true);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(CACHED_EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(MIXED_CASE_EID);
		
		assertNull("Should have failed to find cached objecct", retrievedObject);
	
	}
	
	public void testSupportsCaseInsensitiveCacheKeys() {
		
		// some "local constants"
		final String CACHED_EID = "some-eid";
		final String MIXED_CASE_EID = "sOmE-eId";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(false);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(CACHED_EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(MIXED_CASE_EID);
		
		assertSame("Should have ignored cache key case", 
				cachedObject, retrievedObject);
		
	}
	
	public void testSupportsCaseInsensitiveCacheKeys_Negative() {
		
		// some "local constants"
		final String CACHED_EID = "some-eid";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(false);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(CACHED_EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(CACHED_EID + "X");
		
		assertNull("Should have ignored cache key case", retrievedObject);
		
	}
		
}
