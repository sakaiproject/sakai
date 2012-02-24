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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

/**
 * Verifies (a subset of) extensions to {@link LDAPConnection} behaviors.
 * 
 * @author Dan McCallum (dmccallum@unicon.net)
 *
 */
public class PooledLDAPConnectionTest extends MockObjectTestCase {

    private PooledLDAPConnection conn;
    private LdapConnectionManager connMgr;
    private Mock mockConnMgr;
    
    protected void setUp() throws Exception {
        conn = new PooledLDAPConnection();
        mockConnMgr = new Mock(LdapConnectionManager.class);
        connMgr = (LdapConnectionManager)mockConnMgr.proxy();
        conn.setConnectionManager(connMgr);
        super.setUp();
    }
    
    /**
     * Verifies that active {@link PooledLDAPConnection}s are returned to the
     * assigned {@link LdapConnectionManager}.
     *
     * @see #testFinalizeDoesNotReturnInactiveConnectionToTheConnectionManager() for handling of
     *   inactive connections
     * @throws LDAPException test error
     */
    public void testFinalizeReturnsActiveConnectionToTheConnectionManager() throws LDAPException {
        conn.setActive(true);
        mockConnMgr.expects(once()).method("returnConnection").with(same(conn));
        conn.finalize();
    }
    
    /**
     * Verifies the inverse of {@link #testFinalizeReturnsActiveConnectionToTheConnectionManager()}
     * 
     * @throws LDAPException test error
     */
    public void testFinalizeDoesNotReturnInactiveConnectionToTheConnectionManager() throws LDAPException {
        conn.setActive(false); // just to be sure
        conn.finalize();
        // rely on jMock to refuse any calls to the connection mgr
    }
    

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
