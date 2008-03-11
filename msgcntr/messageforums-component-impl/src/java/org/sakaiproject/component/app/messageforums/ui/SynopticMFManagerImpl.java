/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation.
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
package org.sakaiproject.component.app.messageforums.ui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.ui.SynopticMFManager;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.user.cover.UserDirectoryService;

public class SynopticMFManagerImpl implements SynopticMFManager {
	private static final Log LOG = LogFactory.getLog(SynopticMFManagerImpl.class);
	
	private SqlService sqlService;

	/** To inject sqlService into this Manager */
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	/**
	 * @see org.sakaiproject.api.app.messageforums.ui.SynopticMFManager#fillToolsInSites(List)
	 */
    public Map<String, String> fillToolsInSites(List siteList)
    {
  	  	StringBuilder siteListString = new StringBuilder();
  	  
  	  	for (Iterator iter = siteList.iterator(); iter.hasNext();)
  	  	{
  	  		String siteId = (String) iter.next();
  		  
  	  		if (siteListString.length() == 0)
  	  		{
  	  			siteListString.append("'" + siteId);
  	  		}
  	  		else
  	  		{
  	  			siteListString.append("','" + siteId);
  	  		}
  	  	}
  	  	siteListString.append("'");
  	  
  	  	// SQL Statement to return for each site, a row that contains the tool id, tool name
  	  	//   (sakai.whatever), and site id if a tool with that tool name exists in the site
  	  	final String statement = "SELECT tool_id, registration, site_id FROM SAKAI_SITE_TOOL " + 
  	  			"WHERE registration IN ('sakai.forums','sakai.messages','sakai.messagecenter') " +
  	  			"AND site_id IN (" + siteListString.toString() + ") ORDER BY site_id ASC";
  	  
  		final Map<String, String> siteToolMap = new HashMap<String, String>();
  		sqlService.dbRead(statement, null, new SqlReader()
  		{
  			public Object readSqlResultRecord(ResultSet result)
  			{
  				try
  				{
  					siteToolMap.put(result.getString(3) + ":" + result.getString(2), result.getString(1));

  					return null;
  				}
  				catch (SQLException e)
  				{
  					LOG.error("fillToolsInSites: " + e, e);
  					return null;
  				}
  			}
  		});

  		if (siteToolMap.size() < 1) return null;
  		
  		return siteToolMap;	  
    }    

	/**
	 * @see org.sakaiproject.api.app.messageforums.ui.SynopticMFManager#getGroupMembershipsForSites(List<String>)
	 */
	public Map<String, String> getGroupMembershipsForSites(List<String> siteList)
	{
		// Construct a string of site ids for SQL IN clause
		StringBuilder siteListString = new StringBuilder();
		for (Iterator<String> iter = siteList.iterator(); iter.hasNext();)
		{
			String siteId = iter.next();
			  
			if (siteListString.length() == 0)
			{
				siteListString.append("'" + siteId);
			}
			else
			{
				siteListString.append("','" + siteId);
			}
		}
		siteListString.append("'");
		  
		// Get site id and group name (title) for all groups user is a member of in any of their sites
		final String sql = "select ssg.SITE_ID, ssg.TITLE from " +
					"(select sakai_realm.realm_key, user_id, realm_id from SAKAI_REALM_RL_GR join SAKAI_REALM " + 
					"on SAKAI_REALM_RL_GR.realm_key=SAKAI_REALM.realm_key where USER_ID=?) t, sakai_site_group ssg " +
					"where ssg.SITE_ID in (" + siteListString.toString() + ") and " +
					"t.REALM_ID=CONCAT('/site/',CONCAT(SITE_ID,CONCAT('/group/',GROUP_ID))) order by ssg.SITE_ID";
		  
		Object [] fields = new Object[1];
		fields[0] = UserDirectoryService.getCurrentUser().getId();
			
		final Map<String, String> groupMembershipMap = new HashMap<String, String>();
		sqlService.dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					groupMembershipMap.put(result.getString(1), result.getString(2));

					return null;
				}
				catch (SQLException e)
				{
					LOG.error("fillToolsInSites: " + e, e);
					return null;
				}
			}
		});

		if (groupMembershipMap.size() < 1) return null;
			
		return groupMembershipMap;
	}
	  
	/**
	 * @see org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager#getCurrentUserMemberships()
	 */
	public Map<String, String> getUserRoleForAllSites()
	{
	  	// Move this to a helper to get the correct version?
	  	final String statement = "select userSiteInfo.siteId, roleTable.roleName, rrg.role_key from SAKAI_REALM_RL_GR rrg " +
	      "join (select site.SITE_ID siteId, myrealm.realm_key realm_key, userinfo.userId userId" + 
	      "        from sakai_site site" +
	      "        join (select realm_id realm_id, realm_key realm_key from sakai_realm) myrealm" +
	      "        on myrealm.realm_id = '/site/'|| site.site_id" +
	      "        join (select idmap.USER_ID userId, siteUser.SITE_ID siteId from sakai_user_id_map idmap, SAKAI_SITE_USER siteUser" +
	      "         where idmap.eid = ? and idmap.USER_ID = siteUser.USER_ID) userinfo" +
	      "        on userinfo.siteId = site.site_id) userSiteInfo " +
	      "on rrg.REALM_KEY = userSiteInfo.realm_key " +
	      "and rrg.user_id = userSiteInfo.userId " +
	      "join (select role_name  roleName, role_key roleKey from  SAKAI_REALM_ROLE) roleTable " +
	      "on rrg.role_key = roleTable.roleKey";
			
		Object fields[] = new Object[1];
		fields[0] = UserDirectoryService.getCurrentUser().getEid();
			
		final Map<String, String> siteRoleNameMap = new HashMap<String, String>();
		sqlService.dbRead(statement, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					siteRoleNameMap.put(result.getString(1), result.getString(2));

					return null;
				}
				catch (SQLException e)
				{
					LOG.error("getUserRoleForAllSites: " + e, e);
					return null;
				}
			}
		});

		if (siteRoleNameMap.size() < 1) return null;
		
		return siteRoleNameMap;
	}	  
}
