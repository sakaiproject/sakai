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
	 *          The SqlService.
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
	 *          the auto ddl value.
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

			foorm.autoDDL("lti_mapping", LTIService.MAPPING_MODEL, m_sql, m_autoDdl, doReset, M_log);
			foorm.autoDDL("lti_content", LTIService.CONTENT_MODEL, m_sql, m_autoDdl, doReset, M_log);
			foorm.autoDDL("lti_tools", LTIService.TOOL_MODEL, m_sql, m_autoDdl, doReset, M_log);
			super.init();
		} catch (Exception t) {
			M_log.warn("init(): ", t);
		}
	}

	/* Mapping methods */

	/**
	 * 
	 */
	public Object insertMapping(Properties newProps) {
		return insertThingDao("lti_mapping", LTIService.MAPPING_MODEL, null, 
			newProps, getContext(), isMaintain(getContext()));
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getMapping(java.lang.Long)
	 */
	public Map<String, Object> getMapping(Long key) {
		return getThingDao("lti_mapping", LTIService.MAPPING_MODEL, key, getContext(), isMaintain(getContext()));
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#deleteMapping(java.lang.Long)
	 */
	public boolean deleteMapping(Long key) {
		return deleteThingDao("lti_mapping", LTIService.MAPPING_MODEL, key, getContext(), isMaintain(getContext()));
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateMapping(java.lang.Long,
	 *      java.lang.Object)
	 */
	public Object updateMapping(Long key, Object newProps) {
		return updateThingDao("lti_mapping", LTIService.MAPPING_MODEL, null, 
			key, newProps, getContext(), isMaintain(getContext()));
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getMappings(java.lang.String,
	 *      java.lang.String, int, int)
	 */
	public List<Map<String, Object>> getMappings(String search, String order, int first, int last) {
		return getThingsDao("lti_mapping", LTIService.MAPPING_MODEL, search, order, 
			first, last, getContext(), isMaintain(getContext()));
	}

	// TODO: Actually check mappings
	/**
	 * 
	 */
	public String checkMapping(String url) {
		return url;
	}

	/**
	 * 
	 */
	public Object insertToolDao(Properties newProps, String siteId, boolean isMaintainRole) {
		return insertThingDao("lti_tools", LTIService.TOOL_MODEL, null, newProps, siteId, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getToolDao(java.lang.Long, java.lang.String, boolean)
	 */
	protected Map<String, Object> getToolDao(Long key, String siteId, boolean isMaintainRole) 
	{
		Map<String, Object> retval = getThingDao("lti_tools", LTIService.TOOL_MODEL, key, siteId, isMaintainRole);
		if (retval == null)
			return retval;
		String launch_url = (String) retval.get(LTIService.LTI_LAUNCH);
		if (launch_url != null) {
			String newLaunch = checkMapping(launch_url);
			if (!newLaunch.equals(launch_url)) {
				retval.put("x_launch", launch_url);
				retval.put(LTIService.LTI_LAUNCH, newLaunch);
			}
		}
		return retval;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#deleteToolDao(java.lang.Long, java.lang.String, boolean)
	 */
	public boolean deleteToolDao(Long key, String siteId, boolean isMaintainRole) {
		return deleteThingDao("lti_tools", LTIService.TOOL_MODEL, key, siteId, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateToolDao(java.lang.Long,
	 *      java.lang.Object, java.lang.String, boolean)
	 */
	public Object updateToolDao(Long key, Object newProps, String siteId, boolean isMaintainRole) {
		return updateThingDao("lti_tools", LTIService.TOOL_MODEL, null, key, (Object) newProps, siteId, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getToolsDao(java.lang.String, java.lang.String,
	 *      int, int, java.lang.String, boolean)
	 */
	public List<Map<String, Object>> getToolsDao(String search, String order, int first,
			int last, String siteId, boolean isMaintainRole) {
		return getThingsDao("lti_tools", LTIService.TOOL_MODEL, search, order, first, last, siteId, isMaintainRole);
	}

	/**
	 * 
	 */
	protected Object insertContentDao(Properties newProps, String siteId, boolean isMaintainRole) {
		String toolId = newProps.getProperty(LTIService.LTI_TOOL_ID);
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
		tool = getToolDao(toolKey, siteId, isMaintainRole);
			
		if ( tool == null ) {
			return rb.getString("error.invalid.toolid");
		}

		Long visible = foorm.getLongNull(tool.get(LTIService.LTI_VISIBLE));
		if ( visible == null ) visible = new Long(0);
		if ( ! isAdmin(siteId, isMaintainRole) ) {
			if ( visible == 1 ) {
				return rb.getString("error.invalid.toolid");
			}
		}

		String[] contentModel = getContentModelDao(tool, siteId, isMaintainRole);
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
		
		if (contentModel == null)
			return rb.getString("error.invalid.toolid");
		return insertThingDao("lti_content", contentModel, LTIService.CONTENT_MODEL, newProps, siteId, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getContentDao(java.lang.Long, java.lang.String, boolean)
	 */
	public Map<String, Object> getContentDao(Long key, String siteId, boolean isMaintainRole) {
		Map<String, Object> retval = getThingDao("lti_content", LTIService.CONTENT_MODEL, key, siteId, isMaintainRole);
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
	public boolean deleteContentDao(Long key, String siteId, boolean isMaintainRole) {
		// remove the content link first
		deleteContentLink(key);
		return deleteThingDao("lti_content", LTIService.CONTENT_MODEL, key, siteId, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateContentDao(java.lang.Long, 
	 *      java.lang.Object, java.lang.String, boolean)
	 */
	public Object updateContentDao(Long key, Object newProps, String siteId, boolean isMaintainRole) {
		// Load the content item
		Map<String,Object> content = getContentDao(key, siteId, isMaintainRole);
		if (  content == null ) {
			return rb.getString("error.content.not.found");
		}
		Long oldToolKey = foorm.getLongNull(content.get(LTIService.LTI_TOOL_ID));

		Object oToolId = (Object) foorm.getField(newProps, LTIService.LTI_TOOL_ID);
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
		Map<String, Object> tool = getToolDao(newToolKey, siteId, isMaintainRole);
		if ( tool == null ) {
			return rb.getString("error.invalid.toolid");
		}

		// If the user is not an admin, they cannot switch to 
		// a tool that is stealthed
		Long visible = foorm.getLongNull(tool.get(LTIService.LTI_VISIBLE));
		if ( visible == null ) visible = new Long(0);
		if ( ( !isAdmin(siteId, isMaintainRole) ) && ( ! oldToolKey.equals(newToolKey) )  ) {
			if ( visible == 1 ) {
				return rb.getString("error.invalid.toolid");
			}
		}

		String[] contentModel = getContentModelDao(tool, siteId, isMaintainRole);
		if (contentModel == null)
			return rb.getString("error.invalid.toolid");

		return updateThingDao("lti_content", contentModel, LTIService.CONTENT_MODEL, 
			key, newProps, siteId, isMaintainRole);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getContentsDao(java.lang.String,
	 *      java.lang.String, int, int)
	 */
	public List<Map<String, Object>> getContentsDao(String search, String order, int first,
			int last, String siteId, boolean isMaintainRole) {
		List<Map<String, Object>> contents = getThingsDao("lti_content",
				LTIService.CONTENT_MODEL, search, order, first, last, siteId, isMaintainRole);
		for (Map<String, Object> content : contents) {
			content.put("launch_url", getContentLaunch(content));
		}
		return contents;
	}

	// Returns String (falure) or Long (key on success)
	/**
	 * 
	 */
	public Object insertThingDao(String table, String[] formModel, String[] fullModel,
			Properties newProps, String siteId, boolean isMaintainRole) {

		if (table == null || formModel == null || newProps == null || siteId == null) {
			throw new IllegalArgumentException(
					"siteId, table, model, and newProps must all be non-null");
		}

		if (!isMaintainRole) return null;

		HashMap<String, Object> newMapping = new HashMap<String, Object>();

		String errors = foorm.formExtract(newProps, formModel, rb, true, newMapping, null);
		if (errors != null)
			return errors;

		String[] columns = null;
		String theKey = null;
		if (fullModel == null) {
			columns = foorm.getFields(formModel);
			theKey = foorm.formSqlKey(formModel);
		} else {
			columns = foorm.getFields(fullModel);
			theKey = foorm.formSqlKey(fullModel);
		}
		if ((Arrays.asList(columns).indexOf(LTIService.LTI_SITE_ID) >= 0)) {
			if (!isAdmin(siteId, isMaintainRole) && newMapping.get(LTIService.LTI_SITE_ID) == null) {
				newMapping.put(LTIService.LTI_SITE_ID, siteId);
			}
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

		// Requires KNL-767
		/* Long retval = m_sql.dbInsert(null, sql, fields, LTIService.LTI_ID); */

		// In this version we don't get the key back for HSQL - not ideal - but works without
		// KNL-767

		/* Workaround */
		Long retval = new Long(-1);
		// HSQL does not support getGeneratedKeys() - Yikes
		if ("hsqldb".equals(m_sql.getVendor())) {
			try {
				retval = m_sql.dbInsert(null, sql, fields, LTIService.LTI_ID);
			} catch (Exception e) { // KNL-767 is not fixed
				M_log.warn("Falling back to jdbcTemplate.update because KNL-767 is not fixed.");
				M_log
					.warn("The previous traceback was not-fatal and will go away when KNL-767 is fixed.");
				M_log
					.warn("Spring JDBC cannot get a key back from an HSQL insert, but at least the insert works.");
				jdbcTemplate.update(sql, fields); // At least insert the data
			}
		} else {

			KeyHolder keyHolder = new GeneratedKeyHolder();

			jdbcTemplate.update(new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection)
					throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sql, new String[] { LTIService.LTI_ID });
					for (int i = 0; i < fields.length; i++) {
					ps.setObject(i + 1, fields[i]);
					}
					return ps;
					}
					}, keyHolder);
			retval = foorm.getLong(keyHolder.getKey());
		}
		/* end of workaround */

		// System.out.println("Insert="+retval);
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
			String siteId, boolean isMaintainRole)
	{
		if (table == null || model == null || key == null) {
			throw new IllegalArgumentException("table, model, and key must all be non-null");
		}
		String statement = "SELECT " + foorm.formSelect(model) + " from " + table
			+ " WHERE id = ?";
		Object fields[] = null;
		String[] columns = foorm.getFields(model);

		if (siteId != null && Arrays.asList(columns).indexOf(LTIService.LTI_SITE_ID) >= 0 && !isAdmin(siteId, isMaintainRole)) {
			statement += " AND (SITE_ID = ? OR SITE_ID IS NULL)";
			fields = new Object[2];
			fields[0] = key;
			fields[1] = siteId;
		} else {
			fields = new Object[1];
			fields[0] = key;
		}

		List rv = getResultSet(statement, fields, columns);

		if ((rv != null) && (rv.size() > 0)) {
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
	public List<Map<String, Object>> getThingsDao(String table, String[] model, String search,
			String order, int first, int last, String siteId, boolean isMaintainRole) {
		if (table == null || model == null ) {
			throw new IllegalArgumentException("table and model must be non-null");
		}
		String statement = "SELECT " + foorm.formSelect(model) + " FROM " + table;
		String[] columns = foorm.getFields(model);
		String whereClause = "";

		Object fields[] = null;
		if ( ! isAdmin(siteId, isMaintainRole) ) {
			if (Arrays.asList(columns).indexOf(LTIService.LTI_VISIBLE) >= 0 && 
				Arrays.asList(columns).indexOf(LTIService.LTI_SITE_ID) >= 0 ) {
				whereClause = " ("+LTIService.LTI_SITE_ID+" = ? OR "+
					"("+LTIService.LTI_SITE_ID+" IS NULL AND "+LTIService.LTI_VISIBLE+" != 1 ) ) ";
				fields = new Object[1];
				fields[0] = siteId;
			} else if (Arrays.asList(columns).indexOf(LTIService.LTI_SITE_ID) >= 0) {
				whereClause = " ("+LTIService.LTI_SITE_ID+" = ? OR "+LTIService.LTI_SITE_ID+" IS NULL)";
				fields = new Object[1];
				fields[0] = siteId;
			}
		}
		if ( whereClause.length() > 0 ) statement += " WHERE " + whereClause;

		if (last != 0) {
			String pagedStatement = foorm.getPagedSelect(statement, first, last,
					m_sql.getVendor());
			if (pagedStatement != null)
				statement = pagedStatement;
		}
		// System.out.println("statement = " + statement);
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
	public boolean deleteThingDao(String table, String[] model, Long key, String siteId, boolean isMaintainRole) {
		if (table == null || model == null || key == null) {
			throw new IllegalArgumentException("table, model, and key must all be non-null");
		}
		String statement = "DELETE FROM " + table + " WHERE id = ?";
		Object fields[] = null;
		String[] columns = foorm.getFields(model);

		// Hack to insure that We *Can* delete this since SqlService cannot tell us if updates
		// work
		if (!isAdmin(siteId, isMaintainRole)) {
			Object thing = getThingDao(table, model, key, siteId, isMaintainRole);
			if (thing == null || !(thing instanceof Map)) {
				return false;
			}

			String thingSite = (String) foorm.getField(thing, LTIService.LTI_SITE_ID);

			if (thingSite == null || !thingSite.equals(siteId)) {
				return false;
			}
		}

		if (Arrays.asList(columns).indexOf(LTIService.LTI_SITE_ID) >= 0 && !isAdmin(siteId, isMaintainRole) ) {
			if (!isMaintainRole) {
				M_log.info("Non-maintain attemped delete on " + table);
				return false;
			}
			statement += " AND SITE_ID = ?";
			fields = new Object[2];
			fields[0] = key;
			fields[1] = siteId;
		} else {
			fields = new Object[1];
			fields[0] = key;
		}

		// TODO: Once KNL-775 is safely in
		// return m_sql.dbWriteCount(statement, fields, null, null, false) == 1;

		return jdbcTemplate.update(statement, fields) == 1;
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
		return updateThingDao(table, formModel, fullModel, key, newProps, siteId, isMaintain(siteId));
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
			Long key, Object newProps, String siteId, boolean isMaintainRole) {
		if (table == null || formModel == null || key == null || newProps == null) {
			throw new IllegalArgumentException(
					"table, model, key, and newProps must all be non-null");
		}

		if (!isMaintainRole) return null;

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

		if ( (Arrays.asList(columns).indexOf(LTIService.LTI_SITE_ID) >= 0)) {
			if ( !isAdmin(siteId, isMaintainRole) && newMapping.get(LTIService.LTI_SITE_ID) == null) {
				newMapping.put(LTIService.LTI_SITE_ID, siteId);
			}
		}

		String sql = "UPDATE " + table + " SET " + foorm.updateForm(newMapping)
			+ " WHERE id=" + key.toString();

		if ( isMaintainRole && !isAdmin(siteId, isMaintainRole) && (Arrays.asList(columns).indexOf(LTIService.LTI_SITE_ID) >= 0)) {
			sql += " AND SITE_ID = '" + siteId + "'";
			foorm.setField(newMapping, LTIService.LTI_SITE_ID, siteId);
		}

		// System.out.println("Upate="+sql);
		Object[] fields = foorm.getUpdateObjects(newMapping);
		// System.out.println("Fields="+Arrays.toString(fields));

		// TODO: Once KNL-775 is safely in
		// int count = m_sql.dbWriteCount(sql, fields, null, null, false);

		int count = jdbcTemplate.update(sql, fields);

		return count == 1;
	}

	// Utility to return a resultset
	/**
	 * 
	 */
	public List<Map<String, Object>> getResultSet(String statement, Object[] fields,
			final String[] columns) {
		// System.out.println("getResultSet sql="+statement+" fields="+fields);
		List rv = jdbcTemplate.query(statement, fields, new ColumnMapRowMapper());
		// System.out.println("getResultSet size="+rv.size()+" sql="+statement);
		return (List<Map<String, Object>>) rv;
	}

}
