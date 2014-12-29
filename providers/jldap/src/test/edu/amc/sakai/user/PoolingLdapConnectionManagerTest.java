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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import org.apache.commons.pool.ObjectPool;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class PoolingLdapConnectionManagerTest extends MockObjectTestCase {

	private Mock mockPool;
	private ObjectPool pool;
	private Mock mockConfig;
	private LdapConnectionManagerConfig config;
	private PoolingLdapConnectionManager poolingConnMgr;
	
	
	protected void setUp() {
		mockPool = new Mock(ObjectPool.class);
		pool = (ObjectPool) mockPool.proxy();
		mockConfig = new Mock(LdapConnectionManagerConfig.class);
		config = (LdapConnectionManagerConfig) mockConfig.proxy();
		poolingConnMgr = new PoolingLdapConnectionManager();
		poolingConnMgr.setConfig(config);
		poolingConnMgr.setPool(pool);
		mockConfig.expects(once()).method("isSecureConnection").will(returnValue(false)); // some white box awkwardness
		poolingConnMgr.init();
	}
	
	public void testDoesNotReturnNullReferencesToPool() {
		poolingConnMgr.returnConnection(null); // mockPool will throw a fit if any method called	
	}
	
}
