package org.sakaiproject.component.app.scheduler.jobs;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;

public class SakaiEventArchiveJob implements Job {
	
	   private static final Log LOG = LogFactory.getLog(SakaiEventArchiveJob.class);

	   // default is to move any events older than 24 hours
	   private static final boolean ARCHIVE_ENABLED = true;
	   private static final String DEFAULT_ARCHIVE_LENGTH = "86400000";
	   
		public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
			boolean archiveEnabled = ServerConfigurationService.getBoolean("scheduler.event.archive.enabled", ARCHIVE_ENABLED);
			
			long archiveLength = Long.parseLong(
					ServerConfigurationService.getString("scheduler.event.archive.length", DEFAULT_ARCHIVE_LENGTH));
			
			Connection sakaiConnection = null;
			PreparedStatement sakaiStatement = null;
			String sql;
		
			Timestamp	archiveDate = new Timestamp(System.currentTimeMillis()- archiveLength);
			
			LOG.info("archiveDate="+archiveDate.toString());
			
			// TODO: checkToSeeIfArchiveTablesExist();
			// Make separate statements for HSQL, MySQL, Oracle
			
			try {
			
				sakaiConnection = SqlService.borrowConnection(); 
				sakaiConnection.setAutoCommit(false);
				
				// move session entries older than <date> to archive table
			    sql = "INSERT INTO SAKAI_SESSION_ARCHIVE (SELECT * FROM SAKAI_SESSION WHERE SESSION_END IS NOT NULL AND SESSION_END < ?)";
		    	LOG.info("sql="+sql);
		    	
		    	sakaiStatement = sakaiConnection.prepareStatement(sql);
		    	sakaiStatement.setTimestamp(1, archiveDate);
		    	sakaiStatement.execute(sql);
		    	
		    	sql = "DELETE FROM SAKAI_SESSION WHERE SESSION_END IS NOT NULL AND SESSION_END < ?";   	
			    LOG.info("sql="+sql);
			    	
			    //sakaiStatement = sakaiConnection.prepareStatement(sql);
		    	//sakaiStatement.setTimestamp(1, archiveDate);
		    	//sakaiStatement.execute(sql);
			    
			    sakaiConnection.commit();
			    
			    // move events older than <date> to archive table
			    sql = "INSERT INTO SAKAI_EVENT_ARCHIVE (SELECT * FROM SAKAI_EVENT WHERE EVENT_DATE < ?)";
		    	LOG.info("sql="+sql);
		    	
		    	sakaiStatement = sakaiConnection.prepareStatement(sql);
		    	sakaiStatement.setTimestamp(1, archiveDate);
		    	sakaiStatement.execute(sql);
		    	
		    	sql = "DELETE FROM SAKAI_EVENT WHERE EVENT_DATE < ?";   	
			    LOG.info("sql="+sql);
			    	
			    //sakaiStatement = sakaiConnection.prepareStatement(sql);
		    	//sakaiStatement.setTimestamp(1, archiveDate);
		    	//sakaiStatement.execute(sql);
			    
			    sakaiConnection.commit();
				
			} catch (SQLException e) {
				LOG.error("SQLException: " +e);
			} finally {
                try {
                    if(sakaiStatement != null) sakaiStatement.close();
                } catch (SQLException e) {
                    LOG.error("SQLException in finally block: " +e);
                }
                if(sakaiConnection != null) SqlService.returnConnection(sakaiConnection);
			}
			
		}
		
}
