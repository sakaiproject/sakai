package org.sakaiproject.delegatedaccess.dao.impl;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.log4j.Logger;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.delegatedaccess.dao.DelegatedAccessDao;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.dao.DataAccessException;

public class DelegatedAccessDaoImpl extends JdbcDaoSupport implements DelegatedAccessDao {

	private static final Logger log = Logger.getLogger(DelegatedAccessDaoImpl.class);
	private PropertiesConfiguration statements;
	
	/**
	 * init
	 */
	public void init() {
		log.info("init()");
		
		//setup the vendor
		String vendor = ServerConfigurationService.getInstance().getString("vendor@org.sakaiproject.db.api.SqlService", null);
		
		//initialise the statements
		initStatements(vendor);
	}
	
	/**
	 * Loads our SQL statements from the appropriate properties file
	 
	 * @param vendor	DB vendor string. Must be one of mysql, oracle, hsqldb
	 */
	private void initStatements(String vendor) {
		
		URL url = getClass().getClassLoader().getResource(vendor + ".properties"); 
		
		try {
			statements = new PropertiesConfiguration(); //must use blank constructor so it doesn't parse just yet (as it will split)
			statements.setReloadingStrategy(new InvariantReloadingStrategy());	//don't watch for reloads
			statements.setThrowExceptionOnMissing(true);	//throw exception if no prop
			statements.setDelimiterParsingDisabled(true); //don't split properties
			statements.load(url); //now load our file
		} catch (ConfigurationException e) {
			log.error(e.getClass() + ": " + e.getMessage());
			return;
		}
	}
	
	/**
	 * Get an SQL statement for the appropriate vendor from the bundle
	
	 * @param key
	 * @return statement or null if none found. 
	 */
	private String getStatement(String key) {
		try {
			return statements.getString(key);
		} catch (NoSuchElementException e) {
			log.error("Statement: '" + key + "' could not be found in: " + statements.getFileName());
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getDistinctSiteTerms(String termField) {
		try{
			return getJdbcTemplate().query(getStatement("select.distinctTerms"), new String[]{termField}, new RowMapper() {
			      public Object mapRow(ResultSet resultSet, int i) throws SQLException {
			          return resultSet.getString(1);
			        }
			      });
		}catch (DataAccessException ex) {
			log.error("Error executing query: " + ex.getClass() + ":" + ex.getMessage(), ex);
           return null;
		}
	}

	public String getSiteProperty(String propertyName, String siteId){
		try{
			return (String) getJdbcTemplate().queryForObject(getStatement("select.siteProperty"), new Object[]{propertyName, siteId}, new RowMapper() {
				
				 public Object mapRow(ResultSet resultSet, int i) throws SQLException {
					return resultSet.getString("VALUE");
				}
			});
		}catch (DataAccessException ex) {
			return null;
		}
	}
	
	public List<String> getNodesBySiteRef(String siteRef, String hierarchyId){
		try{
			return (List<String>) getJdbcTemplate().query(getStatement("select.hierarchyNode"), new Object[]{siteRef, hierarchyId}, new RowMapper() {
				
				 public Object mapRow(ResultSet resultSet, int i) throws SQLException {
					return resultSet.getString("ID");
				}
			});
		}catch (DataAccessException ex) {
			return null;
		}
	}
	
	public void addSiteProperty(String siteId, String propertyName, String propertyValue){
		try {
			getJdbcTemplate().update(getStatement("insert.siteProperty"),
				new Object[]{siteId, propertyName, propertyValue}
			);
		} catch (DataAccessException ex) {
           log.error("Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
	}
	
	public void updateSiteProperty(String siteId, String propertyName, String propertyValue){
		try {
			getJdbcTemplate().update(getStatement("update.siteProperty"),
				new Object[]{propertyValue, propertyName, siteId}
			);
		} catch (DataAccessException ex) {
           log.error("Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
	}
	
	public void removeSiteProperty(String siteId, String propertyName){
		try {
			getJdbcTemplate().update(getStatement("delete.siteProperty"),
				new Object[]{propertyName, siteId}
			);
		} catch (DataAccessException ex) {
           log.error("Error executing query: " + ex.getClass() + ":" + ex.getMessage());
		}
	}
	
}
