/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.scheduler.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;

@Slf4j
public class SakaiEventArchiveJob implements Job {

	   private static final String DEFAULT_ARCHIVE_LENGTH = "86400000";
	   
		public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
			
			
			long archiveLength = Long.parseLong(
					ServerConfigurationService.getString("scheduler.event.archive.length", DEFAULT_ARCHIVE_LENGTH));
			
			Connection sakaiConnection = null;
			PreparedStatement sakaiStatement = null;
			PreparedStatement sakaiStatement2 = null;
			String sql;
		
			Timestamp	archiveDate = new Timestamp(System.currentTimeMillis()- archiveLength);
			
			log.info("archiveDate="+archiveDate.toString());
			
			// TODO: checkToSeeIfArchiveTablesExist();
			// Make separate statements for HSQL, MySQL, Oracle
			
			try {
			
				sakaiConnection = SqlService.borrowConnection(); 
				sakaiConnection.setAutoCommit(false);
				
				// move session entries older than <date> to archive table
			    sql = "INSERT INTO SAKAI_SESSION_ARCHIVE (SELECT * FROM SAKAI_SESSION WHERE SESSION_END IS NOT NULL AND SESSION_END < ?)";
		    	log.info("sql="+sql);
		    	
		    	sakaiStatement = sakaiConnection.prepareStatement(sql);
		    	sakaiStatement.setTimestamp(1, archiveDate);
		    	sakaiStatement.execute(sql);
		    	
		    	sql = "DELETE FROM SAKAI_SESSION WHERE SESSION_END IS NOT NULL AND SESSION_END < ?";   	
			    log.info("sql="+sql);
			    	
			    //sakaiStatement = sakaiConnection.prepareStatement(sql);
		    	//sakaiStatement.setTimestamp(1, archiveDate);
		    	//sakaiStatement.execute(sql);
			    
			    sakaiConnection.commit();
			    
			    // move events older than <date> to archive table
			    sql = "INSERT INTO SAKAI_EVENT_ARCHIVE (SELECT * FROM SAKAI_EVENT WHERE EVENT_DATE < ?)";
		    	log.info("sql="+sql);
		    	
		    	sakaiStatement2 = sakaiConnection.prepareStatement(sql);
		    	sakaiStatement2.setTimestamp(1, archiveDate);
		    	sakaiStatement2.execute(sql);
		    	
		    	sql = "DELETE FROM SAKAI_EVENT WHERE EVENT_DATE < ?";   	
			    log.info("sql="+sql);
			    	
			    //sakaiStatement = sakaiConnection.prepareStatement(sql);
		    	//sakaiStatement.setTimestamp(1, archiveDate);
		    	//sakaiStatement.execute(sql);
			    
			    sakaiConnection.commit();
				
			} catch (SQLException e) {
				log.error("SQLException: " +e);
			} finally {
                try {
                    if(sakaiStatement != null) sakaiStatement.close();
                } catch (SQLException e) {
                    log.error("SQLException in finally block: " +e);
                }
                try {
                    if(sakaiStatement2 != null) sakaiStatement2.close();
                } catch (SQLException e) {
                    log.error("SQLException in finally block: " +e);
                }
                if(sakaiConnection != null) SqlService.returnConnection(sakaiConnection);
			}
			
		}
		
}
