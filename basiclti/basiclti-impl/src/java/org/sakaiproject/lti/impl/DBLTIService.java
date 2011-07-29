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
 *       http://www.osedu.org/licenses/ECL-2.0
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
      // if we are auto-creating our schema, check and create
      if (m_autoDdl) {
        // Use very carefully - for testing table creation
        boolean doReset = false;
        if (doReset)
          M_log.error("DO NOT RUN IN PRODUCTION WITH doReset TRUE");

        String[] sqls = foorm.formSqlTable("lti_mapping", LTIService.MAPPING_MODEL,
            m_sql.getVendor(), doReset);
        for (String sql : sqls)
          if (m_sql.dbWriteFailQuiet(null, sql, null))
            M_log.info(sql);
        sqls = foorm.formSqlTable("lti_content", LTIService.CONTENT_MODEL,
            m_sql.getVendor(), doReset);
        for (String sql : sqls)
          if (m_sql.dbWriteFailQuiet(null, sql, null))
            M_log.info(sql);
        sqls = foorm.formSqlTable("lti_tools", LTIService.TOOL_MODEL, m_sql.getVendor(),
            doReset);
        for (String sql : sqls)
          if (m_sql.dbWriteFailQuiet(null, sql, null))
            M_log.info(sql);

        // Keep to add indexes (maybe)
        // m_sql.ddl(this.getClass().getClassLoader(), "sakai_lti");

      }

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
    return insertThing("lti_mapping", LTIService.MAPPING_MODEL, null, newProps);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getMapping(java.lang.Long)
   */
  public Map<String, Object> getMapping(Long key) {
    return getThing("lti_mapping", LTIService.MAPPING_MODEL, key);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#deleteMapping(java.lang.Long)
   */
  public boolean deleteMapping(Long key) {
    return deleteThing("lti_mapping", LTIService.MAPPING_MODEL, key);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.impl.BaseLTIService#updateMapping(java.lang.Long,
   *      java.lang.Object)
   */
  public Object updateMapping(Long key, Object newProps) {
    return updateThing("lti_mapping", LTIService.MAPPING_MODEL, null, key, newProps);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getMappings(java.lang.String,
   *      java.lang.String, int, int)
   */
  public List<Map<String, Object>> getMappings(String search, String order, int first,
      int last) {
    return getThings("lti_mapping", LTIService.MAPPING_MODEL, search, order, first, last);
  }

  // TODO: Actually check mappings
  /**
   * 
   */
  public String checkMapping(String url) {
    return url;
  }

  /* Tool Methods */
  /**
   * 
   */
  public Object insertTool(Properties newProps) {
    return insertThing("lti_tools", LTIService.TOOL_MODEL, null, newProps);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getTool(java.lang.Long)
   */
  public Map<String, Object> getTool(Long key) {
    return getThing("lti_tools", LTIService.TOOL_MODEL, key);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getToolNoAuthz(java.lang.Long)
   */
  public Map<String, Object> getToolNoAuthz(Long key) {
    Map<String, Object> retval = getThingNoAuthz("lti_tools", LTIService.TOOL_MODEL, key);
    if (retval == null)
      return retval;
    String launch_url = (String) retval.get("launch");
    if (launch_url != null) {
      String newLaunch = checkMapping(launch_url);
      if (!newLaunch.equals(launch_url)) {
        retval.put("x_launch", launch_url);
        retval.put("launch", newLaunch);
      }
    }
    return retval;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getTool(java.lang.String)
   */
  public Map<String, Object> getTool(String url) {
    return null;
  }

  /**
   * 
   * @param urlorkey
   * @param retval
   * @return
   */
  private boolean getTool(Object urlorkey, Map<String, Object> retval) {
    return false;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#deleteTool(java.lang.Long)
   */
  public boolean deleteTool(Long key) {
    return deleteThing("lti_tools", LTIService.TOOL_MODEL, key);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.impl.BaseLTIService#updateTool(java.lang.Long,
   *      java.lang.Object)
   */
  public Object updateTool(Long key, Object newProps) {
    return updateThing("lti_tools", LTIService.TOOL_MODEL, null, key, newProps);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getTools(java.lang.String, java.lang.String,
   *      int, int)
   */
  public List<Map<String, Object>> getTools(String search, String order, int first,
      int last) {
    return getThings("lti_tools", LTIService.TOOL_MODEL, search, order, first, last);
  }

  /* Content Methods */
  /**
   * 
   */
  public Object insertContent(Properties newProps) {
    String toolId = newProps.getProperty("tool_id");
    if (toolId == null)
      return rb.getString("error.missing.toolid");
    Long toolKey = null;
    try {
      toolKey = new Long(toolId);
    } catch (Exception e) {
      return rb.getString("error.invalid.toolid");
    }
    String[] contentModel = getContentModel(toolKey);
    if (contentModel == null)
      return rb.getString("error.invalid.toolid");
    return insertThing("lti_content", contentModel, LTIService.CONTENT_MODEL, newProps);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getContent(java.lang.Long)
   */
  public Map<String, Object> getContent(Long key) {
    Map<String, Object> retval = getThing("lti_content", LTIService.CONTENT_MODEL, key);
    if (retval == null)
      return retval;
    retval.put("launch_url", getContentLaunch(retval));
    return retval;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getContentNoAuthz(java.lang.Long)
   */
  public Map<String, Object> getContentNoAuthz(Long key) {
    return getThingNoAuthz("lti_content", LTIService.CONTENT_MODEL, key);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#deleteContent(java.lang.Long)
   */
  public boolean deleteContent(Long key) {
    return deleteThing("lti_content", LTIService.CONTENT_MODEL, key);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.impl.BaseLTIService#updateContent(java.lang.Long,
   *      java.lang.Object)
   */
  public Object updateContent(Long key, Object newProps) {
    // Make sure we like the proposed tool_id
    String toolId = (String) foorm.getField(newProps, "tool_id");
    if (toolId == null)
      return rb.getString("error.missing.toolid");
    Long toolKey = null;
    try {
      toolKey = new Long(toolId);
    } catch (Exception e) {
      return rb.getString("error.invalid.toolid");
    }
    String[] contentModel = getContentModel(toolKey);
    if (contentModel == null)
      return rb.getString("error.invalid.toolid");

    return updateThing("lti_content", contentModel, LTIService.CONTENT_MODEL, key,
        newProps);
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.lti.api.LTIService#getContents(java.lang.String,
   *      java.lang.String, int, int)
   */
  public List<Map<String, Object>> getContents(String search, String order, int first,
      int last) {
    List<Map<String, Object>> contents = getThings("lti_content",
        LTIService.CONTENT_MODEL, search, order, first, last);
    for (Map<String, Object> content : contents) {
      content.put("launch_url", getContentLaunch(content));
    }
    return contents;
  }

  // Returns String (falure) or Long (key on success)
  /**
   * 
   */
  public Object insertThing(String table, String[] formModel, String[] fullModel,
      Properties newProps) {
    if (table == null || formModel == null || newProps == null) {
      throw new IllegalArgumentException(
          "table, model, and newProps must all be non-null");
    }
    if (!isMaintain())
      return null;

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
    if ((Arrays.asList(columns).indexOf("SITE_ID") >= 0)) {
      if (!isAdmin() && newMapping.get("SITE_ID") == null) {
        newMapping.put("SITE_ID", getContext());
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
    /* Long retval = m_sql.dbInsert(null, sql, fields, "id"); */

    // In this version we don't get the key back for HSQL - not ideal - but works without
    // KNL-767

    /* Workaround */
    Long retval = new Long(-1);
    // HSQL does not support getGeneratedKeys() - Yikes
    if ("hsqldb".equals(m_sql.getVendor())) {
      try {
        retval = m_sql.dbInsert(null, sql, fields, "id");
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
          PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
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
   * @return
   */
  public Map<String, Object> getThing(String table, String[] model, Long key) {
    return getThing(table, model, key, true);
  }

  /**
   * 
   * @param table
   * @param model
   * @param key
   * @return
   */
  public Map<String, Object> getThingNoAuthz(String table, String[] model, Long key) {
    return getThing(table, model, key, false);
  }

  /**
   * 
   * @param table
   * @param model
   * @param key
   * @param doAuthz
   * @return
   */
  private Map<String, Object> getThing(String table, String[] model, Long key,
      boolean doAuthz) {
    if (table == null || model == null || key == null) {
      throw new IllegalArgumentException("table, model, and key must all be non-null");
    }
    String statement = "SELECT " + foorm.formSelect(model) + " from " + table
        + " WHERE id = ?";
    Object fields[] = null;
    String[] columns = foorm.getFields(model);

    if (doAuthz && Arrays.asList(columns).indexOf("SITE_ID") >= 0 && !isAdmin()) {
      statement += " AND SITE_ID = ? OR SITE_ID IS NULL";
      fields = new Object[2];
      fields[0] = key;
      fields[1] = getContext();
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
   * @return
   */
  public List<Map<String, Object>> getThings(String table, String[] model, String search,
      String order, int first, int last) {
    if (table == null || model == null) {
      throw new IllegalArgumentException("table and model must be non-null");
    }
    String statement = "SELECT " + foorm.formSelect(model) + " FROM " + table;
    String[] columns = foorm.getFields(model);

    Object fields[] = null;
    if (Arrays.asList(columns).indexOf("SITE_ID") >= 0) {
      if (!isAdmin()) {
        statement += " WHERE SITE_ID = ? OR SITE_ID IS NULL";
        fields = new Object[1];
        fields[0] = getContext();
      }
    }

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
   * @return
   */
  public boolean deleteThing(String table, String[] model, Long key) {
    if (table == null || model == null || key == null) {
      throw new IllegalArgumentException("table, model, and key must all be non-null");
    }
    String statement = "DELETE FROM " + table + " WHERE id = ?";
    Object fields[] = null;
    String[] columns = foorm.getFields(model);

    // Hack to insure that We *Can* delete this since SqlService cannot tell us if updates
    // work
    if (!isAdmin()) {
      Object thing = getThing(table, model, key);
      if (thing == null || !(thing instanceof Map)) {
        return false;
      }

      String siteId = (String) foorm.getField(thing, "SITE_ID");

      if (siteId == null || !siteId.equals(getContext())) {
        return false;
      }
    }

    if (Arrays.asList(columns).indexOf("SITE_ID") >= 0 && !isAdmin()) {
      if (!isMaintain()) {
        M_log.info("Non-maintain attemped delete on " + table);
        return false;
      }
      statement += " AND SITE_ID = ?";
      fields = new Object[2];
      fields[0] = key;
      fields[1] = getContext();
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
   * @return
   */
  public Object updateThing(String table, String[] formModel, String[] fullModel,
      Long key, Object newProps) {
    if (table == null || formModel == null || key == null || newProps == null) {
      throw new IllegalArgumentException(
          "table, model, key, and newProps must all be non-null");
    }

    if (!isMaintain())
      return null;

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

    if ((Arrays.asList(columns).indexOf("SITE_ID") >= 0)) {
      if (!isAdmin() && newMapping.get("SITE_ID") == null) {
        newMapping.put("SITE_ID", getContext());
      }
    }

    String sql = "UPDATE " + table + " SET " + foorm.updateForm(newMapping)
        + " WHERE id=" + key.toString();

    if (isMaintain() && !isAdmin() && (Arrays.asList(columns).indexOf("SITE_ID") >= 0)) {
      sql += " AND SITE_ID = '" + getContext() + "'";
      foorm.setField(newMapping, "SITE_ID", getContext());
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
