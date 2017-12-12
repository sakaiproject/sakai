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

import com.novell.ldap.LDAPConnection;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author dmccallum
 *
 */
@Slf4j
public class MaxLifetimeLdapConnectionLivenessValidator implements LdapConnectionLivenessValidator {

	public static final long DEFAULT_MAX_TTL = -1L;
	
	/**
	 * Max connection life in millis
	 */
	private long maxTtl;

	/**
	 * Tests if the allowable lifetime of the given connection has already
	 * elapsed. Edge cases:
	 * 
	 * <ol>
	 *   <li>Non-{@link PooledLDAPConnection} - returns <code>true</code></li>
	 *   <li><code>maxTtl</code> &lt;= 0 - returns <code>true</code></li>
	 *   <li>Connection birthdate in the future - returns <code>true</code></li>
	 * </ol>
	 * 
	 * 
	 */
	public boolean isConnectionAlive(LDAPConnection connectionToTest) {
		if ( !(connectionToTest instanceof PooledLDAPConnection) ) {
			if (log.isDebugEnabled()) {
    			log.debug("isConnectionAlive(): connection not of expected type [" + 
    					(connectionToTest == null ? "null" : connectionToTest.getClass().getName()) + 
    					"], returning true");
    		}
			return true;
		}
		if ( maxTtl <= 0 ) {
			if ( log.isDebugEnabled() ) {
				log.debug("isConnectionAlive(): maxTtl set to infinite [" + maxTtl + "], returning true");
			}
			return true;
		}
		long now = System.currentTimeMillis();
		long then = ((PooledLDAPConnection)connectionToTest).getBirthdate();
		long elapsed = now - then;
		boolean isAlive = elapsed <= maxTtl;
		if ( log.isDebugEnabled() ) {
			log.debug("isConnectionAlive(): [now = " + now + 
					"][then = " + then + "][elapsed = " + elapsed + 
					"][max TTL = " + maxTtl + 
					"][isAlive = " + isAlive + "]");
		}
		return isAlive;
	}

	/**
	 * Get the max connection lifetime, in millis. Values
	 * less than or equals to zero are considered infinite, i.e.
	 * no TTL.
	 * 
	 * @return
	 */
	public long getMaxTtl() {
		return maxTtl;
	}

	/**
	 * Assign the max connection lifetime, in millis. Values
	 * less than or equal to zero are considered infinite, i.e.
	 * no TTL.
	 * 
	 * @param maxTtl
	 */
	public void setMaxTtl(long maxTtl) {
		this.maxTtl = maxTtl;
	}

	
	
}
