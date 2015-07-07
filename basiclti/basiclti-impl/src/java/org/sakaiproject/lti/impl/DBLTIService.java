
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

package org.sakaiproject.lti.impl;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.lti.api.LTIService;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.sakaiproject.lti.impl.FoormMapRowMapper;

/**
 * <p>
 * DBLTIService extends the BaseLTIService.
 * </p>
 */
public class DBLTIService extends BaseLTIService implements LTIService {
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(DBLTIService.class);

	/**
	 * 
	 */
	private PropertiesConfiguration statements;

	/** Dependency: SqlService */
	protected SqlService m_sql = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *	  The SqlService.
	 */
	public void setSqlService(SqlService service) {
		m_sql = service;
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *	  the auto ddl value.
	 */
	public void setAutoDdl(String value) {
		m_autoDdl = Boolean.valueOf(value);
	}

	/**
	 * 
	 */
	private javax.sql.DataSource dataSource = null;
	/**
	 * 
	 */
	private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init() {
		if (m_sql == null)
			m_sql = (SqlService) ComponentManager.get("org.sakaiproject.db.api.SqlService");
		if (dataSource == null)
			dataSource = (DataSource) ComponentManager.get("javax.sql.DataSource");
		if (jdbcTemplate == null && dataSource != null)
			jdbcTemplate = new JdbcTemplate(dataSource);

		try {
			boolean doReset = false;
			if (doReset) M_log.error("DO NOT RUN IN PRODUCTION WITH doReset TRUE");

			foorm.autoDDL("lti_content", LTIService.CONTENT_MODEL, m_sql, m_autoDdl, doReset, M_log);
			foorm.autoDDL("lti_tools", LTIService.TOOL_MODEL, m_sql, m_autoDdl, doReset, M_log);
			foorm.autoDDL("lti_deploy", LTIService.DEPLOY_MODEL, m_sql, m_autoDdl, doReset, M_log);
			foorm.autoDDL("lti_binding", LTIService.BINDING_MODEL, m_sql, m_autoDdl, doReset, M_log);
			foorm.autoDDL("lti_memberships_jobs", LTIService.MEMBERSHIPS_JOBS_MODEL, m_sql, m_autoDdl, doReset, M_log);
			super.init();
		} catch (Exception t) {
			M_log.warn("init(): ", t);
		}
	}

    /**
     *
     */
    public Object insertMembershipsJobDao(String siteId, String membershipsId, String membershipsUrl, String consumerKey, String ltiVersion) {

	if (M_log.isDebugEnabled()) {
	    M_log.debug("insertMembershipsJobDao(" + siteId + "," + membershipsId + "," + membershipsUrl + "," + consumerKey + "," + ltiVersion + ")");
	}

	// First, check if there is already a job for this site.
	if (getMembershipsJobDao(siteId) == null) {
	    Map<String, Object> props = new HashMap<String, Object>();
	    props.put(LTI_SITE_ID, siteId);
	    props.put("memberships_id", membershipsId);
	    props.put("memberships_url", membershipsUrl);
	    props.put("consumerkey", consumerKey);
	    props.put("lti_version", ltiVersion);
	    return insertThingDao("lti_memberships_jobs", LTIService.MEMBERSHIPS_JOBS_MODEL, null, props, siteId, false, true);
	} else {
	    return "SITE_ALREADY_JOBBED";
	}
    }

	public  List<Map<String, Object>> getMembershipsJobsDao() {

	M_log.debug("getMembershipsJobDao()");

	return getThingsDao("lti_memberships_jobs", LTIService.MEMBERSHIPS_JOBS_MODEL, null, null, null, null, null, 0, 0, null, true);
    }

	public Map<String, Object> getMembershipsJobDao(String siteId) {

	if (M_log.isDebugEnabled()) {
	    M_log.debug("getMembershipsJobDao(" + siteId + ")");
	}

	List<Map<String, Object>> rows
	    = getThingsDao("lti_memberships_jobs", LTIService.MEMBERSHIPS_JOBS_MODEL, null, null, "SITE_ID = '" + siteId + "'", null, null, 0, 0, siteId, true);

	int size = rows.size();

	if (size == 1) {
	    return rows.get(0);
	} else if (size > 1) {
	    M_log.warn("Mutiple memberships jobs found for site '" + siteId + "'. Returning first ...");
	    return rows.get(0);
	} else {
	    return null;
	}
    }

	/**
	 * 
	 */
	public Object insertToolDao(Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		return insertThingDao("lti_tools", LTIService.TOOL_MODEL, null, newProps, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getToolDao(java.lang.Long, java.lang.String, boolean)
	 */
	public Map<String, Object> getToolDao(Long key, String siteId, boolean isAdminRole) 
	{
		return getThingDao("lti_tools", LTIService.TOOL_MODEL, key, siteId, isAdminRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#deleteToolDao(java.lang.Long, java.lang.String, boolean)
	 */
	public boolean deleteToolDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		return deleteThingDao("lti_tools", LTIService.TOOL_MODEL, key, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateToolDao(java.lang.Long,
	 *      java.lang.Object, java.lang.String, boolean)
	 */
	public Object updateToolDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		return updateThingDao("lti_tools", LTIService.TOOL_MODEL, null, key, (Object) newProps, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getToolsDao(java.lang.String, java.lang.String,
	 *      int, int, java.lang.String, boolean)
	 */
	public List<Map<String, Object>> getToolsDao(String search, String order, int first,
			int last, String siteId, boolean isAdminRole) {

		String extraSelect = null;
		String joinClause = null;
		String groupBy = null;

		if ( order != null ) {
			order = foorm.orderCheck(order, "lti_tools", LTIService.TOOL_MODEL);
			if ( order == null ) {
				throw new IllegalArgumentException("order must be [table.]field [asc|desc]");
			}
		} else {
			extraSelect = "COUNT(DISTINCT lti_content.id) AS lti_content_count, COUNT(DISTINCT lti_content.SITE_ID) AS lti_site_count";
			joinClause = "LEFT OUTER JOIN lti_content ON lti_content.tool_id = lti_tools.id";
			groupBy = "lti_tools.id";
			order = "lti_tools.id";
		}

		// Oracle needs all the selected values in the GROUP_BY
		if ("mysql".equals(m_sql.getVendor())) {
			return getThingsDao("lti_tools", LTIService.TOOL_MODEL, extraSelect, joinClause, search, groupBy, order, first, last, siteId, isAdminRole);
		} else {
			List<Map<String, Object>> mainList = getThingsDao("lti_tools", LTIService.TOOL_MODEL, null, null, search, null, order, first, last, siteId, isAdminRole);
			String[] id_model = { "id:key", "visible:radio", "SITE_ID:text" } ; 
			groupBy = "lti_tools.id, lti_tools.visible, lti_tools.SITE_ID";
			List<Map<String, Object>> countList = getThingsDao("lti_tools", id_model, extraSelect, joinClause, search, groupBy, order, first, last, siteId, isAdminRole);

			// Merge the lists...
			Map<Object, Map<String, Object>> countMap = new HashMap<Object, Map<String, Object>> ();
			for (Map<String, Object> count : countList) {
				Object id = count.get("id");
				countMap.put(id, count);
			}

			for (Map<String, Object> row : mainList) {
				Object id = row.get("id");
				if ( id == null ) continue;
				Map<String, Object> count = countMap.get(id);
				if ( count == null ) continue;
				Object contentCount = count.get("LTI_CONTENT_COUNT");
				row.put("lti_content_count", contentCount);
				Object siteCount = count.get("LTI_SITE_COUNT");
				row.put("lti_site_count", siteCount);
			}
			return mainList;
		}

	}

	/**
	 * @return Returns String (falure) or Long (key on success)
	 */
	public Object insertContentDao(Properties newProps, String siteId, 
		boolean isAdminRole, boolean isMaintainRole) 
	{
		if ( newProps == null ) {
			throw new IllegalArgumentException(
					"newProps must be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		if (!isMaintainRole) return null;

		String toolId = newProps.getProperty(LTI_TOOL_ID);
		if (toolId == null)
			return rb.getString("error.missing.toolid");
		Long toolKey = null;
		try {
			toolKey = new Long(toolId);
		} catch (Exception e) {
			return rb.getString("error.invalid.toolid");
		}

		// Load the tool we are aiming for Using DAO
		Map<String, Object> tool = null;
		tool = getToolDao(toolKey, siteId, isAdminRole);
			
		if ( tool == null ) {
			return rb.getString("error.invalid.toolid");
		}

		Long visible = foorm.getLongNull(tool.get(LTI_VISIBLE));
		if ( visible == null ) visible = new Long(0);
		if ( ! isAdminRole ) {
			if ( visible == 1 ) {
				return rb.getString("error.invalid.toolid");
			}
		}

		String[] contentModel = getContentModelDao(tool, isAdminRole);
		String[] columns = foorm.getFields(contentModel);
		
		// Since page title and title are both required and dynamically hideable, 
		// They may not be in the model.  If they are not there, add them for the purpose
		// of the insert, and then copy the values from the tool.
		List<String> contentModelList = new ArrayList<String>(Arrays.asList(contentModel));
		List<String> contentModelColumns = new ArrayList<String>(Arrays.asList(columns));
		if (!contentModelColumns.contains(LTI_TITLE) || !contentModelColumns.contains(LTI_PAGETITLE))
		{
			String toolTitle = (String) tool.get(LTI_TITLE);
			if ( toolTitle == null ) toolTitle = "...";  // should not happen
			if (!contentModelColumns.contains(LTI_TITLE))
			{
				contentModelList.add(LTI_TITLE + ":text");
				newProps.put(LTI_TITLE, toolTitle);
			}

			if (!contentModelColumns.contains(LTI_PAGETITLE))
			{
				// May happen for old / upgraded tool items
				String pageTitle = (String) tool.get(LTI_PAGETITLE);
				if ( pageTitle == null ) pageTitle = toolTitle;
				contentModelList.add(LTI_PAGETITLE + ":text");
				newProps.put(LTI_PAGETITLE, pageTitle);
			}
			contentModel = contentModelList.toArray(new String[contentModelList.size()]);
		}

		// Copy to fa_icon across
		String fa_icon = (String) tool.get(LTI_FA_ICON);
		if ( fa_icon != null && fa_icon.length() > 0 ) {
			newProps.put(LTI_FA_ICON, fa_icon);
		}

		// If resource_handler is not in content and is in the tool, copy it
		if ( newProps.getProperty(LTI_RESOURCE_HANDLER) == null && tool.get(LTI_RESOURCE_HANDLER) != null ) {
			newProps.put(LTI_RESOURCE_HANDLER, (String) tool.get(LTI_RESOURCE_HANDLER));
			contentModelList.add(LTI_RESOURCE_HANDLER + ":text");
			contentModel = contentModelList.toArray(new String[contentModelList.size()]);
		}
		
		if (contentModel == null)
			return rb.getString("error.invalid.toolid");
		return insertThingDao("lti_content", contentModel, LTIService.CONTENT_MODEL, newProps, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getContentDao(java.lang.Long, java.lang.String, boolean)
	 */
	public Map<String, Object> getContentDao(Long key, String siteId, boolean isAdminRole) {
		Map<String, Object> retval = getThingDao("lti_content", LTIService.CONTENT_MODEL, key, siteId, isAdminRole);
		if (retval == null) return retval;
		retval.put("launch_url", getContentLaunch(retval));
		return retval;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#deleteContent(java.lang.Long, java.lang.String, boolean)
	 */
	public boolean deleteContentDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		deleteContentLinkDao(key, siteId, isAdminRole, isMaintainRole);
		return deleteThingDao("lti_content", LTIService.CONTENT_MODEL, key, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateContentDao(java.lang.Long, 
	 *      java.lang.Object, java.lang.String, boolean)
	 */
	public Object updateContentDao(Long key, Object newProps, String siteId, 
		boolean isAdminRole, boolean isMaintainRole) 
	{
		if ( key == null || newProps == null ) {
			throw new IllegalArgumentException(
					"both key and newProps must be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		// Load the content item
		Map<String,Object> content = getContentDao(key, siteId, isAdminRole);
		if (  content == null ) {
			return rb.getString("error.content.not.found");
		}
		Long oldToolKey = foorm.getLongNull(content.get(LTI_TOOL_ID));

		Object oToolId = (Object) foorm.getField(newProps, LTI_TOOL_ID);
		Long newToolKey = null;
		if ( oToolId != null && oToolId instanceof Number ) {
			newToolKey = new Long( ((Number) oToolId).longValue());
		} else if ( oToolId != null ) {
			try {
				newToolKey = new Long((String) oToolId);
			} catch (Exception e) {
				return rb.getString("error.invalid.toolid");
			}
		}
		if ( newToolKey == null || newToolKey < 0 ) newToolKey = oldToolKey;

		// Load the tool we are aiming for
		Map<String, Object> tool = getToolDao(newToolKey, siteId, isAdminRole);
		if ( tool == null ) {
			return rb.getString("error.invalid.toolid");
		}

		// If the user is not an admin, they cannot switch to 
		// a tool that is stealthed
		Long visible = foorm.getLongNull(tool.get(LTI_VISIBLE));
		if ( visible == null ) visible = new Long(0);
		if ( ( !isAdminRole ) && ( ! oldToolKey.equals(newToolKey) )  ) {
			if ( visible == 1 ) {
				return rb.getString("error.invalid.toolid");
			}
		}

		String[] contentModel = getContentModelDao(tool, isAdminRole);
		if (contentModel == null)
			return rb.getString("error.invalid.toolid");

		return updateThingDao("lti_content", contentModel, LTIService.CONTENT_MODEL, 
			key, newProps, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getContentsDao(java.lang.String,
	 *      java.lang.String, int, int)
	 */
	public List<Map<String, Object>> getContentsDao(String search, String order, int first,
			int last, String siteId, boolean isAdminRole) {

		if ( order != null ) {
			order = foorm.orderCheck(order, "lti_content", LTIService.CONTENT_MODEL);
			if ( order == null ) {
				throw new IllegalArgumentException("order must be [table.]field [asc|desc]");
			}
		}

		List<Map<String, Object>> contents = getThingsDao("lti_content",
				LTIService.CONTENT_MODEL, null, null, search, null, order, first, last, siteId, isAdminRole);
		for (Map<String, Object> content : contents) {
			content.put("launch_url", getContentLaunch(content));
		}
		return contents;
	}

	/**
	 * 
	 */
	public Object insertDeployDao(Properties newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		if ( ! isAdminRole ) throw new IllegalArgumentException("Currently we support admins/Dao access");
		return insertThingDao("lti_deploy", LTIService.DEPLOY_MODEL, null, newProps, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#deleteDeployDao(java.lang.Long, java.lang.String, boolean)
	 */
	public boolean deleteDeployDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		if ( ! isAdminRole ) throw new IllegalArgumentException("Currently we support admins/Dao access");
		return deleteThingDao("lti_deploy", LTIService.DEPLOY_MODEL, key, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateDeployDao(java.lang.Long,
	 *      java.lang.Object, java.lang.String, boolean)
	 */
	public Object updateDeployDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		if ( ! isAdminRole ) throw new IllegalArgumentException("Currently we support admins/Dao access");
		return updateThingDao("lti_deploy", LTIService.DEPLOY_MODEL, null, key, newProps, siteId, isAdminRole, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getDeployDao(java.lang.Long, java.lang.String, boolean)
	 */
	public Map<String, Object> getDeployDao(Long key, String siteId, boolean isAdminRole) 
	{
		if ( ! isAdminRole ) throw new IllegalArgumentException("Currently we support admins/Dao access");
		return getThingDao("lti_deploy", LTIService.DEPLOY_MODEL, key, siteId, isAdminRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getDeploysDao(java.lang.String, java.lang.String,
	 *      int, int, java.lang.String, boolean)
	 */
	public List<Map<String, Object>> getDeploysDao(String search, String order, int first,
			int last, String siteId, boolean isAdminRole) {
		if ( ! isAdminRole ) throw new IllegalArgumentException("Currently we support admins/Dao access");

		String extraSelect = null;
		String joinClause = null;
		String groupBy = null;

		if ( order != null ) {
			order = foorm.orderCheck(order, "lti_deploy", LTIService.DEPLOY_MODEL);
			if ( order == null ) {
				throw new IllegalArgumentException("order must be [table.]field [asc|desc]");
			}
		} else {
			extraSelect = "COUNT(DISTINCT lti_tools.id) AS lti_tool_count, COUNT(DISTINCT lti_content.SITE_ID) AS lti_site_count, COUNT(DISTINCT lti_content.id) AS lti_content_count";
			joinClause = "LEFT OUTER JOIN lti_tools ON lti_tools.deployment_id = lti_deploy.id LEFT OUTER JOIN lti_content ON lti_content.tool_id = lti_tools.id";
			groupBy = "lti_deploy.id";
                        order = "lti_deploy.id";
                }

		// Oracle needs all the selected values in the GROUP_BY
		if ("mysql".equals(m_sql.getVendor())) {
			return getThingsDao("lti_deploy", LTIService.DEPLOY_MODEL, extraSelect, joinClause, search, groupBy, order, first, last, siteId, isAdminRole);
		} else {
			List<Map<String, Object>> mainList = getThingsDao("lti_deploy",LTIService.DEPLOY_MODEL, null, null, search, null, order, first, last, siteId, isAdminRole);
			String[] id_model = { "id:key", "visible:radio" } ; 
			groupBy = "lti_tools.id, lti_tools.visible";
			List<Map<String, Object>> countList = getThingsDao("lti_deploy", id_model, extraSelect, joinClause, search, groupBy, order, first, last, siteId, isAdminRole);

			// Merge the lists...
			Map<Object, Map<String, Object>> countMap = new HashMap<Object, Map<String, Object>> ();
			for (Map<String, Object> count : countList) {
				Object id = count.get("id");
				countMap.put(id, count);
			}

			for (Map<String, Object> row : mainList) {
				Object id = row.get("id");
				if ( id == null ) continue;
				Map<String, Object> count = countMap.get(id);
				if ( count == null ) continue;
				Object contentCount = count.get("LTI_CONTENT_COUNT");
				row.put("lti_content_count", contentCount);
				Object toolCount = count.get("LTI_TOOL_COUNT");
				row.put("lti_tool_count", toolCount);
				Object siteCount = count.get("LTI_SITE_COUNT");
				row.put("lti_site_count", siteCount);
			}
			return mainList;
		}
	}

	public Object insertProxyBindingDao(Properties newProps) {
		return insertThingDao("lti_binding", LTIService.BINDING_MODEL, null, newProps, null, true, true);
	}

	public Object updateProxyBindingDao(Long key, Object newProps) {
		return updateThingDao("lti_binding", LTIService.BINDING_MODEL, null, key, newProps, null, true, true);
	}
	public boolean deleteProxyBindingDao(Long key) {
		return deleteThingDao("lti_binding", LTIService.BINDING_MODEL, key, null, true, true);
	}
	public Map<String, Object> getProxyBindingDao(Long key) {
		return getThingDao("lti_binding", LTIService.BINDING_MODEL, key, null, true);
	}

	public Map<String, Object> getProxyBindingDao(Long tool_id, String siteId) {
		if (tool_id == null || siteId == null) {
			throw new IllegalArgumentException("tool_id and siteId must be non-null");
		}

		String[] model = LTIService.BINDING_MODEL;
		String[] columns = foorm.getFields(model);

		String statement = "SELECT " + foorm.formSelect(model) + " FROM lti_binding WHERE " + 
			LTI_SITE_ID + " = ? AND " + LTI_TOOL_ID + " = ?";

		Object [] fields = new Object[2];
		fields[0] = siteId;
		fields[1] = tool_id;

		M_log.debug(statement);
		List rv = getResultSet(statement, fields, columns);

		if ((rv != null) && (rv.size() > 0)) {
			if ( rv.size() > 1 ) {
				M_log.warn("Warning more than one row returned: "+statement);
			}
			return (Map<String, Object>) rv.get(0);
		}
		return null;
	}


	/**
	 * @param table
	 *		The name of the table to use
	 * @param formModel
	 *		The filtered model(required)
	 * @param fullModel
	 *		The full model (or null)
	 * @param newProps
	 *		The key/value pairs for this object.
	 * @param siteId
	 *		The siteId that this item is being inserted into.  If isAdmin is true,
	 *		this should be null and the siteId is expected be in the newProps variable.
	 * @param isAdminRole
	 *		This is true if we are doing this as an administrator (i.e. we can bypass
	 *		rules about SITE_ID being null in the inserted object.
	 * @param isMaintainRole
	 *		This is true if we are doing this as a site maintainer.  This will return
	 *		null if we are not the site maintainer.
	 * @return Returns String (failure) or Long (key on success)
	 */
	public Object insertThingDao(String table, String[] formModel, String[] fullModel,
			Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {

		if (table == null || formModel == null || newProps == null ) {
			throw new IllegalArgumentException(
					"table, model, and newProps must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		if ( ! (newProps instanceof Properties || newProps instanceof Map )  ) {
			throw new IllegalArgumentException("newProps must Properties or Map<String, Object>");
		}

		// TODO: Remove this as a parameter
		if (!isMaintainRole) {
	    M_log.debug("Not in maintain role. Nothing will be inserted. Returning null ...");
	    return null;
	}

		HashMap<String, Object> newMapping = new HashMap<String, Object>();

		String[] columns = null;
		String theKey = null;
		if (fullModel == null) {
			columns = foorm.getFields(formModel);
			theKey = foorm.formSqlKey(formModel);
		} else {
			columns = foorm.getFields(fullModel);
			theKey = foorm.formSqlKey(fullModel);
		}

		// Insert the SITE_ID if it is not present in case it is required
		if (!isAdminRole && (Arrays.asList(columns).contains(LTI_SITE_ID))) {
			((Map) newProps).put(LTI_SITE_ID, siteId);
		}

		// Check to see if this insert has all required fields in the proper format
		String errors = foorm.formExtract(newProps, formModel, rb, true, newMapping, null);
		if (errors != null)
			return errors;

		// Only admins can insert things into sites other than the current site
		if (!isAdminRole && (Arrays.asList(columns).contains(LTI_SITE_ID))) {
			newMapping.put(LTI_SITE_ID, siteId);
		}
		String seqName = foorm.getSqlSequence(table, theKey, m_sql.getVendor());

		String[] insertInfo = foorm.insertForm(newMapping);
		String makeSql = "INSERT INTO " + table + " ( " + insertInfo[0] + " ) VALUES ( "
			+ insertInfo[1] + " )";
		if ("oracle".equals(m_sql.getVendor()) && theKey != null && seqName != null) {

			makeSql = "INSERT INTO " + table + " ( " + theKey + ", " + insertInfo[0]
				+ " ) VALUES ( " + seqName + ".NextVal, " + insertInfo[1] + " )";
		}

		final String sql = makeSql;

		// System.out.println("Insert SQL="+sql);
		final Object[] fields = foorm.getInsertObjects(newMapping);

		Long retval = m_sql.dbInsert(null, sql, fields, LTI_ID);

		M_log.debug("Count="+retval+" Insert="+sql);
		return retval;
	}

	/**
	 * 
	 * @param table
	 * @param model
	 * @param key
	 * @param siteId - This is allowed to be null
	 * @param isMaintainRole
	 * @return
	 */
	private Map<String, Object> getThingDao(String table, String[] model, Long key,
			String siteId, boolean isAdminRole)
	{
		if (table == null || model == null || key == null) {
			throw new IllegalArgumentException("table, model, and key must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}
		String statement = "SELECT " + foorm.formSelect(model) + " from " + table
			+ " WHERE id = ?";
		Object fields[] = null;
		String[] columns = foorm.getFields(model);

		// Non-admins only see global (SITE_ID IS NULL) or in their site
		if (!isAdminRole && Arrays.asList(columns).indexOf(LTI_SITE_ID) >= 0 ) {
			statement += " AND (SITE_ID = ? OR SITE_ID IS NULL)";
			fields = new Object[2];
			fields[0] = key;
			fields[1] = siteId;
		} else {
			fields = new Object[1];
			fields[0] = key;
		}

		M_log.debug(statement);
		List rv = getResultSet(statement, fields, columns);

		if ((rv != null) && (rv.size() > 0)) {
			if ( rv.size() > 1 ) {
				M_log.warn("Warning more than one row returned: "+statement);
			}
			return (Map<String, Object>) rv.get(0);
		}
		return null;
	}

	/**
	 * 
	 * @param table
	 * @param model
	 * @param search
	 * @param order
	 * @param first
	 * @param last
	 * @param siteId
	 * @param isMaintainRole
	 * @return
	 */
	public List<Map<String, Object>> getThingsDao(String table, String[] model, 
		String extraSelect, String joinClause, String search, String groupBy, String order, 
		int first, int last, String siteId, boolean isAdminRole) 
	{
		if (table == null || model == null ) {
			throw new IllegalArgumentException("table and model must be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		String statement = "SELECT " + foorm.formSelect(table, model, true);
		if ( extraSelect != null ) {
			statement += ", " + extraSelect;
		}
		statement += " FROM " + table;
		if ( joinClause != null ) {
			statement += " " + joinClause;
		}
		String[] columns = foorm.getFields(model);
		String whereClause = "";

		// Only admins can see invisible items and items from any site
		Object fields[] = null;
		if ( ! isAdminRole ) {
			if (Arrays.asList(columns).indexOf(LTI_VISIBLE) >= 0 && 
				Arrays.asList(columns).indexOf(LTI_SITE_ID) >= 0 ) {
				whereClause = " ("+table+'.'+LTI_SITE_ID+" = ? OR "+
					"("+table+'.'+LTI_SITE_ID+" IS NULL AND "+table+'.'+LTI_VISIBLE+" != 1 ) ) ";
				fields = new Object[1];
				fields[0] = siteId;
			} else if (Arrays.asList(columns).indexOf(LTI_SITE_ID) >= 0) {
				whereClause = " ("+table+'.'+LTI_SITE_ID+" = ? OR "+table+'.'+LTI_SITE_ID+" IS NULL)";
				fields = new Object[1];
				fields[0] = siteId;
			}
		}

		if ( search != null && search.length() > 0 ) {
			if ( whereClause.length() > 0 ) {
				whereClause += " AND (" + search + ") ";
			} else { 
				whereClause += " (" + search + ") ";
			}
		}

		if ( whereClause.length() > 0 ) statement += " WHERE " + whereClause;

		if ( groupBy != null ) {
			statement += " GROUP BY ";
			if ("oracle".equals(m_sql.getVendor()) ) {
				statement += foorm.formSelect(table, model, false);
			} else {
				statement += groupBy;
			}
		}

		if ( order != null ) {
			statement += " ORDER BY " + order;
		}

		if (last != 0) {
			String pagedStatement = foorm.getPagedSelect(statement, first, last,
					m_sql.getVendor());
			if (pagedStatement != null)
				statement = pagedStatement;
		}
		M_log.debug(statement);
		return getResultSet(statement, fields, columns);
	}

	/**
	 * 
	 * @param table
	 * @param model
	 * @param key
	 * @param siteId
	 * @param isMaintainRole
	 * @return
	 */
	public boolean deleteThingDao(String table, String[] model, Long key, String siteId, 
		boolean isAdminRole, boolean isMaintainRole) 
	{
		if (table == null || model == null || key == null) {
			throw new IllegalArgumentException("table, model, and key must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		if (!isMaintainRole) return false;

		String statement = "DELETE FROM " + table + " WHERE id = ?";
		Object fields[] = null;
		String[] columns = foorm.getFields(model);

		// Only admins can delete by id irrespective of the current site
		if (!isAdminRole && Arrays.asList(columns).indexOf(LTI_SITE_ID) >= 0 ) {
			statement += " AND SITE_ID = ?";
			fields = new Object[2];
			fields[0] = key;
			fields[1] = siteId;
		} else {
			fields = new Object[1];
			fields[0] = key;
		}

		int count = m_sql.dbWriteCount(statement, fields, null, null, false);
		M_log.debug("Count="+count+" Delete="+statement);
		return count == 1;
	}

	/**
	 * 
	 * @param table
	 * @param formModel
	 * @param fullModel
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object updateThingDao(String table, String[] formModel, String[] fullModel, Long key, Object newProps, String siteId) {
		return updateThingDao(table, formModel, fullModel, key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	/**
	 * 
	 * @param table
	 * @param formModel
	 * @param fullModel
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @param isMaintainRole
	 * @return
	 */
	public Object updateThingDao(String table, String[] formModel, String[] fullModel,
			Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) 
	{
		if (table == null || formModel == null || key == null || newProps == null) {
			throw new IllegalArgumentException(
					"table, model, key, and newProps must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}
	
		if ( ! (newProps instanceof Properties || newProps instanceof Map)  ) {
			throw new IllegalArgumentException("newProps must Properties or Map<String, Object>");
		}

		if (!isMaintainRole) return false;

		HashMap<String, Object> newMapping = new HashMap<String, Object>();

		String errors = foorm.formExtract(newProps, formModel, rb, false, newMapping, null);
		if (errors != null)
			return errors;

		String[] columns = null;
		if (fullModel == null) {
			columns = foorm.getFields(formModel);
		} else {
			columns = foorm.getFields(fullModel);
		}

		// Only admins can update *into* a site
		if ( !isAdminRole && (Arrays.asList(columns).indexOf(LTI_SITE_ID) >= 0)) {
			newMapping.put(LTI_SITE_ID, siteId);
		}

		String sql = "UPDATE " + table + " SET " + foorm.updateForm(newMapping)
			+ " WHERE id=" + key.toString();

		if ( isMaintainRole && !isAdminRole && (Arrays.asList(columns).indexOf(LTI_SITE_ID) >= 0)) {
			sql += " AND SITE_ID = '" + siteId + "'";
			foorm.setField(newMapping, LTI_SITE_ID, siteId);
		}

		// System.out.println("Upate="+sql);
		Object[] fields = foorm.getUpdateObjects(newMapping);
		// System.out.println("Fields="+Arrays.toString(fields));

		int count = m_sql.dbWriteCount(sql, fields, null, null, false);

		M_log.debug("Count="+count+" Update="+sql);
		return count == 1;
	}

	/*-- Straight-up API methods ------------------------*/

	public Map<String, Object> getToolForResourceHandlerDao(String resourceType)
	{
		if (resourceType == null ) {
			throw new IllegalArgumentException("resourceType must be non-null");
		}

		String[] model = LTIService.TOOL_MODEL;
		String[] columns = foorm.getFields(model);
		String statement = "SELECT " + foorm.formSelect(model) + " FROM lti_tools WHERE " + 
			LTI_RESOURCE_HANDLER + " = ? ";

		Object [] fields = new Object[1];
		fields[0] = resourceType;

		M_log.debug(statement);
		List rv = getResultSet(statement, fields, columns);

		if ((rv != null) && (rv.size() > 0)) {
			if ( rv.size() > 1 ) {
				M_log.warn("Warning more than one row returned: "+statement);
			}
			return (Map<String, Object>) rv.get(0);
		}
		return null;
	}

	public Map<String, Object> getDeployForConsumerKeyDao(String consumerKey)
	{
		if (consumerKey == null ) {
			throw new IllegalArgumentException("consumerKey must be non-null");
		}

		String[] model = LTIService.DEPLOY_MODEL;
		String[] columns = foorm.getFields(model);
		String statement = "SELECT " + foorm.formSelect(model) + " FROM lti_deploy WHERE " + 
			LTI_CONSUMERKEY + " = ? ";

		Object [] fields = new Object[1];
		fields[0] = consumerKey;

		M_log.debug(statement);
		List rv = getResultSet(statement, fields, columns);

		if ((rv != null) && (rv.size() > 0)) {
			if ( rv.size() > 1 ) {
				M_log.warn("Warning more than one row returned: "+statement);
			}
			return (Map<String, Object>) rv.get(0);
		}
		return null;
	}


	// Utility to return a resultset
	public List<Map<String, Object>> getResultSet(String statement, Object[] fields,
			final String[] columns) {
		// System.out.println("getResultSet sql="+statement+" fields="+fields);
		List rv = jdbcTemplate.query(statement, fields, new FoormMapRowMapper(columns));
		// System.out.println("getResultSet size="+rv.size()+" sql="+statement);

		return (List<Map<String, Object>>) rv;
	}



}
