/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBHelper extends HibernateDaoSupport {
	private boolean		autoDdl					= false;
	private String		dbVendor				= null;
	private boolean		notifiedIndexesUpdate	= false;
	

	// ################################################################
	// Spring bean methods
	// ################################################################

	public void init() {
		dbVendor = getDbVendor();
		autoDdl = getAutoDdl();
		
		if(autoDdl) {
			// update db indexes, if needed
			//updateIndexes();
		
			// preload default reports, if needed
			//preloadDefaultReports();
		}
	}
	
	public void preloadDefaultReports() {
		HibernateCallback hcb = session -> {
            InputStreamReader isr = null;
            BufferedReader br = null;
            try{
                ClassPathResource defaultReports = new ClassPathResource(dbVendor + "/default_reports.sql");
                log.info("init(): - preloading sitestats default reports");
                isr = new InputStreamReader(defaultReports.getInputStream());
                br = new BufferedReader(isr);

                session.getSessionFactory().openSession();
                session.beginTransaction();
                String sqlLine = null;
                while((sqlLine = br.readLine()) != null) {
                    sqlLine = sqlLine.trim();
                    if(!sqlLine.equals("") && !sqlLine.startsWith("--")) {
                        if(sqlLine.endsWith(";")) {
                            sqlLine = sqlLine.substring(0, sqlLine.indexOf(";"));
                        }
                        try{
                            session.createSQLQuery(sqlLine).executeUpdate();
                            session.flush();
                        }catch(Exception e){
                            log.warn("Failed to preload default report: " + sqlLine, e);
                        }
                    }
                }
                session.getTransaction().commit();
            }catch(Exception e){
                log.error("Error while preloading default reports", e);
            }finally{
                if(session != null) {
                    session.close();
                }
            }
            return null;
        };
		getHibernateTemplate().execute(hcb);
	}

	public void updateIndexes() {
		if(!dbVendor.equals("mysql") && !dbVendor.equals("oracle"))
			return;
		notifiedIndexesUpdate = false;
		HibernateCallback hcb = session -> {
            session.doWork(c -> {
                try{
                    List<String> sstEventsIxs = listIndexes(c, "SST_EVENTS");
                    List<String> sstResourcesIxs = listIndexes(c, "SST_RESOURCES");
                    List<String> sstSiteActivityIxs = listIndexes(c, "SST_SITEACTIVITY");
                    List<String> sstSiteVisitsIxs = listIndexes(c, "SST_SITEVISITS");
                    List<String> sstReportsIxs = listIndexes(c, "SST_REPORTS");

                    // SST_EVENTS
                    if(sstEventsIxs.contains("SITE_ID_IX")) renameIndex(c, "SITE_ID_IX", "SST_EVENTS_SITE_ID_IX", "SITE_ID", "SST_EVENTS");
                    else if(!sstEventsIxs.contains("SST_EVENTS_SITE_ID_IX")) createIndex(c, "SST_EVENTS_SITE_ID_IX", "SITE_ID", "SST_EVENTS");
                    if(sstEventsIxs.contains("EVENT_ID_IX")) renameIndex(c, "EVENT_ID_IX", "SST_EVENTS_EVENT_ID_IX", "EVENT_ID", "SST_EVENTS");
                    else if(!sstEventsIxs.contains("SST_EVENTS_EVENT_ID_IX")) createIndex(c, "SST_EVENTS_EVENT_ID_IX", "EVENT_ID", "SST_EVENTS");
                    if(sstEventsIxs.contains("DATE_ID_IX")) renameIndex(c, "DATE_ID_IX", "SST_EVENTS_DATE_ID_IX", "EVENT_DATE", "SST_EVENTS");
                    else if(!sstEventsIxs.contains("SST_EVENTS_DATE_ID_IX")) createIndex(c, "SST_EVENTS_DATE_ID_IX", "EVENT_DATE", "SST_EVENTS");

                    // SST_RESOURCES
                    if(sstResourcesIxs.contains("SITE_ID_IX")) renameIndex(c, "SITE_ID_IX", "SST_RESOURCES_SITE_ID_IX", "SITE_ID", "SST_RESOURCES");
                    else if(!sstResourcesIxs.contains("SST_RESOURCES_SITE_ID_IX")) createIndex(c, "SST_RESOURCES_SITE_ID_IX", "SITE_ID", "SST_RESOURCES");
                    if(sstResourcesIxs.contains("USER_ID_IX")) renameIndex(c, "USER_ID_IX", "SST_RESOURCES_USER_ID_IX", "USER_ID", "SST_RESOURCES");
                    else if(!sstResourcesIxs.contains("SST_RESOURCES_USER_ID_IX")) createIndex(c, "SST_RESOURCES_USER_ID_IX", "USER_ID", "SST_RESOURCES");
                    if(sstResourcesIxs.contains("RES_ACT_IDX")) renameIndex(c, "RES_ACT_IDX", "SST_RESOURCES_RES_ACT_IDX", "RESOURCE_ACTION", "SST_RESOURCES");
                    else if(!sstResourcesIxs.contains("SST_RESOURCES_RES_ACT_IDX")) createIndex(c, "SST_RESOURCES_RES_ACT_IDX", "RESOURCE_ACTION", "SST_RESOURCES");
                    if(sstResourcesIxs.contains("DATE_ID_IX")) renameIndex(c, "DATE_ID_IX", "SST_RESOURCES_DATE_ID_IX", "RESOURCE_DATE", "SST_RESOURCES");
                    else if(!sstResourcesIxs.contains("SST_RESOURCES_DATE_ID_IX")) createIndex(c, "SST_RESOURCES_DATE_ID_IX", "RESOURCE_DATE", "SST_RESOURCES");

                    // SST_SITEACTIVITY
                    if(sstSiteActivityIxs.contains("SITE_ID_IX")) renameIndex(c, "SITE_ID_IX", "SST_SITEACTIVITY_SITE_ID_IX", "SITE_ID", "SST_SITEACTIVITY");
                    else if(!sstSiteActivityIxs.contains("SST_SITEACTIVITY_SITE_ID_IX")) createIndex(c, "SST_SITEACTIVITY_SITE_ID_IX", "SITE_ID", "SST_SITEACTIVITY");
                    if(sstSiteActivityIxs.contains("EVENT_ID_IX")) renameIndex(c, "EVENT_ID_IX", "SST_SITEACTIVITY_EVENT_ID_IX", "EVENT_ID", "SST_SITEACTIVITY");
                    else if(!sstSiteActivityIxs.contains("SST_SITEACTIVITY_EVENT_ID_IX")) createIndex(c, "SST_SITEACTIVITY_EVENT_ID_IX", "EVENT_ID", "SST_SITEACTIVITY");
                    if(sstSiteActivityIxs.contains("DATE_ID_IX")) renameIndex(c, "DATE_ID_IX", "SST_SITEACTIVITY_DATE_ID_IX", "ACTIVITY_DATE", "SST_SITEACTIVITY");
                    else if(!sstSiteActivityIxs.contains("SST_SITEACTIVITY_DATE_ID_IX")) createIndex(c, "SST_SITEACTIVITY_DATE_ID_IX", "ACTIVITY_DATE", "SST_SITEACTIVITY");

                    // SST_SITEVISITS
                    if(sstSiteVisitsIxs.contains("SITE_ID_IX")) renameIndex(c, "SITE_ID_IX", "SST_SITEVISITS_SITE_ID_IX", "SITE_ID", "SST_SITEVISITS");
                    else if(!sstSiteVisitsIxs.contains("SST_SITEVISITS_SITE_ID_IX")) createIndex(c, "SST_SITEVISITS_SITE_ID_IX", "SITE_ID", "SST_SITEVISITS");
                    if(sstSiteVisitsIxs.contains("DATE_ID_IX")) renameIndex(c, "DATE_ID_IX", "SST_SITEVISITS_DATE_ID_IX", "VISITS_DATE", "SST_SITEVISITS");
                    else if(!sstSiteVisitsIxs.contains("SST_SITEVISITS_DATE_ID_IX")) createIndex(c, "SST_SITEVISITS_DATE_ID_IX", "VISITS_DATE", "SST_SITEVISITS");

                    // SST_REPORTS
                    if(!sstReportsIxs.contains("SST_REPORTS_SITE_ID_IX")) createIndex(c, "SST_REPORTS_SITE_ID_IX", "SITE_ID", "SST_REPORTS");

                }catch(Exception e){
                    log.error("Error while updating indexes", e);
                }
            });
            return null;
        };
		getHibernateTemplate().execute(hcb);
	}

	private void notifyIndexesUpdate(){
		if(!notifiedIndexesUpdate)
			log.info("init(): updating indexes on SiteStats tables...");
		notifiedIndexesUpdate = true;
	}
	
	private List<String> listIndexes(Connection c, String table) throws SQLException {
		List<String> indexes = new ArrayList<String>();
		String sql = null;
		int pos = 1;
		if(dbVendor.equals("mysql")){
			sql = "show indexes from " + table;
			pos = 3;
		}else if(dbVendor.equals("oracle")){
			sql = "select * from all_indexes where table_name = '" + table + "'";
			pos = 2;
		}
		Statement st = null;
		ResultSet rs = null;
		try{
			st = c.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()){
				String ixName = rs.getString(pos);
				indexes.add(ixName);
			}
		}catch(SQLException e){
			log.warn("Failed to execute sql: " + sql, e);
		}finally{
			try {
                if (rs != null)
                    rs.close();
            }
            finally {
                if (st != null)
                	st.close();
            }
		}
		return indexes;
	}

	private void createIndex(Connection c, String index, String field, String table) throws SQLException {
		notifyIndexesUpdate();
		String sql = "create index " + index + " on " + table + "(" + field + ")";
		Statement st = null;
		try{
			st = c.createStatement();
			st.execute(sql);
		}catch(SQLException e){
			log.warn("Failed to execute sql: " + sql, e);
		}finally{
			if (st != null)
                st.close();
		}
	}

	private void renameIndex(Connection c, String oldIndex, String newIndex, String field, String table) throws SQLException {
		String sql = null;
		notifyIndexesUpdate();
		if(dbVendor.equals("mysql")) sql = "ALTER TABLE " + table + " DROP INDEX " + oldIndex + ", ADD INDEX " + newIndex + " USING BTREE(" + field + ")";
		else if(dbVendor.equals("oracle")) sql = "ALTER INDEX " + oldIndex + " RENAME TO " + newIndex;
		Statement st = null;
		try{
			st = c.createStatement();
			st.execute(sql);
		}catch(SQLException e){
			log.warn("Failed to execute sql: " + sql, e);
		}finally{
			if (st != null)
                st.close();
		}
	}
	
	private String getDbVendor() {
		String dialectStr = null;
		if(ServerConfigurationService.getString("sitestats.db", "internal").equals("internal")) {
			dialectStr = ServerConfigurationService.getString("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		}else{
			dialectStr = ServerConfigurationService.getString("sitestats.externalDb.hibernate.dialect","org.hibernate.dialect.HSQLDialect");
		}
		if(dialectStr.toLowerCase().contains("mysql")) {
			return "mysql";
		}else if(dialectStr.toLowerCase().contains("oracle")) {
			return "oracle";
		}else{
			return "hsql";
		}
	}
	
	public boolean getAutoDdl() {
		boolean autoDdl = false;
		if(ServerConfigurationService.getString("sitestats.db", "internal").equals("internal")) {
			autoDdl = ServerConfigurationService.getBoolean("auto.ddl", true);
		}else{
			autoDdl = ServerConfigurationService.getBoolean("sitestats.externalDb.auto.ddl", true);
		}
		return autoDdl;
	}
}
