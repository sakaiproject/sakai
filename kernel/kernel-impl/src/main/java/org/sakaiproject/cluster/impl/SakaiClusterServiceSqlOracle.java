/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/cluster/cluster-impl/impl/src/java/org/sakaiproject/cluster/impl/SakaiClusterServiceSqlOracle.java $
 * $Id: SakaiClusterServiceSqlOracle.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.cluster.impl;

/**
 * methods for accessing cluster data in an oracle database.
 */
public class SakaiClusterServiceSqlOracle extends SakaiClusterServiceSqlDefault
{
	/**
	 * returns the sql statement for obtaining a list of expired sakai servers from the sakai_cluster table. <br/>br/>
	 * 
	 * @param timeout
	 *        how long (in seconds) we give an app server to respond before it is considered lost.
	 */
	public String getListExpiredServers(long timeout)
	{
		return "select SERVER_ID_INSTANCE from SAKAI_CLUSTER where SERVER_ID_INSTANCE != ? and UPDATE_TIME < (CURRENT_TIMESTAMP - "
				+ ((float) timeout / (float) (60 * 60 * 24)) + " )";
	}
}
