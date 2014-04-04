/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakai.memory.impl.test;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.impl.BasicMemoryService;

import java.util.Random;

/**
 * @author ieb
 *
 */
public class MemoryServiceTest extends TestCase
{

	private static Log log = LogFactory.getLog(MemoryServiceTest.class);
	private EventTrackingService eventTrackingService;
	private SecurityService securityService;
	private AuthzGroupService authzGroupService;
	private BasicMemoryService basicMemoryService;
	private CacheManager cacheManager;
	private ServerConfigurationService serverConfigurationService;

	/**
	 * @param name
	 */
	public MemoryServiceTest(String name)
	{
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		eventTrackingService = new MockEventTrackingService();
		securityService = new MockSecurityService();
		serverConfigurationService = new MockServerConfigurationService();
		authzGroupService = new MockAuthzGroupService();
		basicMemoryService = new MockBasicMemoryService(eventTrackingService, securityService, authzGroupService, serverConfigurationService );
		cacheManager = new CacheManager(this.getClass().getResourceAsStream("ehcache-test.xml"));
		basicMemoryService.setCacheManager(cacheManager);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testCreate() {
		Cache cache = basicMemoryService.newCache("org.sakaiproject.alias.api.AliasService.callCache","");
		Random r = new Random();
		int hit = 0;
		int miss = 0;
		for ( int i = 0; i < 10000; i++) {
			int k = r.nextInt(1000);
			if ( cache.containsKey(String.valueOf(k))) {
				Object k2 = cache.get(String.valueOf(k));
				hit++;
				assertNotNull(k2);
			} else {
				cache.put(String.valueOf(k),k);
				miss++;
			}
		}
		log.info("Hits ="+hit+" Misses="+miss);
	}
	public void xtestGetLong() {
		Cache cache = basicMemoryService.newCache("org.sakaiproject.alias.api.AliasService.callCache","");
		for ( int i = 0; i < 100; i++) {
			cache.put(String.valueOf(i), i);
		}
		long endTime = System.currentTimeMillis() + 10*60*1000;
		long ncount = 0;
		long errors = 0;
		while(errors < 100 && System.currentTimeMillis() < endTime ) {
		for ( int i = 0; i < 100; i++) {
			ncount++;
			if ( cache.containsKey(String.valueOf(i)) ) {
				if ( cache.get(String.valueOf(i)) == null ) {
					log.info("Found Null Key for "+i+" after "+ncount+" attempts ");
					errors++;
				}
			} else {
				log.info("Key missing ");
				cache.put(String.valueOf(i), i);
			}
		}
		}
		log.info("All Ok");
	}

}
