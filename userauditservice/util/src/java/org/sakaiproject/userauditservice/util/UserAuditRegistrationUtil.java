/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
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

package org.sakaiproject.userauditservice.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.ResourceLoader;

/**
 * UserAuditUtil
 */
@Slf4j
public class UserAuditRegistrationUtil implements UserAuditRegistration
{

	// Services needed
	protected SqlService sqlService;
	protected UserAuditService userAuditService;
	
	// Other variables
	private String bundleLocation = "";
	private ResourceLoader rl = null;
	private String databaseSourceKey = "";
	private String sourceText = "";
	
	// flag for telling the UI there's parameters to consider
	public boolean hasParameters = false;
	
	/** UserAuditService init() */
	public void init()
	{
		ResourceLoader loader = getLocalResourceLoader();
		if (loader != null) {
			this.sourceText = loader.getString(getDatabaseSourceKey());
		}
		getUserAuditService().register(this);
	}
	
	public SqlService getSqlService() {
		return sqlService;
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}
	
	public UserAuditService getUserAuditService() {
		return userAuditService;
	}

	public void setUserAuditService(UserAuditService userAuditService) {
		this.userAuditService = userAuditService;
	}
	
	/**
	 * Get the fully qualified package of where the message bundle is located.
	 * @return
	 */
	public String getBundleLocation() {
		return bundleLocation;
	}
	
	/**
	 * Setter
	 * @param bundleLocation
	 */
	public void setBundleLocation(String bundleLocation) {
		this.bundleLocation = bundleLocation;
	}
	
	public ResourceLoader getResourceLoader(String location) {
		return new ResourceLoader(location);
	}
	
	/**
	 * Gets the ResourceLoader specified by the bundleLocation.
	 * @return
	 */
	private ResourceLoader getLocalResourceLoader() {
		if (rl == null) {
			rl = (ResourceLoader)getResourceLoader(getBundleLocation());
		}
		return rl;
	}
	
	public String getDatabaseSourceKey() {
		return databaseSourceKey;
	}

	public void setDatabaseSourceKey(String databaseSourceKey) {
		this.databaseSourceKey = databaseSourceKey;
	}

	public String getSourceText(String[] parameter) {
		if (isHasParameters())
		{
			return rl.getFormattedMessage(getDatabaseSourceKey(), parameter);
		}
		
		return sourceText;
	}

	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}
	
	public boolean isHasParameters() {
		return hasParameters;
	}

	public void setHasParameters(boolean hasParameters) {
		this.hasParameters = hasParameters;
	}
	
	/** Pass in a List of String[] and this method will process them and write to the database */
	public void addToUserAuditing(List<String[]> userAuditList)
	{
		// determine which flavor of database we're using
		String sqlVendor = sqlService.getVendor();
		
		for (String[] auditStrings : userAuditList)
		{
			String siteId = auditStrings[0];
			String username = auditStrings[1];
			String roleName = auditStrings[2];
			String actionTaken = auditStrings[3];
			String source = auditStrings[4];
			String actionUserId = auditStrings[5];
			String sql = null;
			Object fields[] = new Object[7];
			if ("oracle".equals(sqlVendor))
			{
				sql = "insert into user_audits_log (id,site_id,user_id,role_name,action_taken,audit_stamp,source,action_user_id) values (user_audits_log_seq.nextval,?,?,?,?,to_date(?,'YYYY-MM-DD HH24:MI:SS'),?,?)";
			}
			else
			{
				sql = "insert into user_audits_log (site_id,user_id,role_name,action_taken,audit_stamp,source,action_user_id) values (?,?,?,?,?,?,?)";
			}
			fields[0] = siteId;
			fields[1] = username;
			fields[2] = roleName;
			fields[3] = actionTaken;
			Date currentDate = new Date();
			DateFormat actionDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String actionDate = actionDateFormat.format(currentDate);
			fields[4] = actionDate;
			fields[5] = source;
			fields[6] = actionUserId;
			sqlService.dbWrite(sql, fields);
		}
	}
	
	/** Method to delete all the user auditing log entries for a site.  Used when deleting a site */
	public void deleteUserAuditingFromSite(String siteId)
	{
		String sql = "delete from user_audits_log where site_id = ?";

		Object fields[] = new Object[1];

		fields[0] = siteId;
		sqlService.dbWrite(sql, fields);
	}
}
