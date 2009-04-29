package org.sakaiproject.sitestats.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;


public class ConvOldPrefsJobImpl implements StatefulJob {
	// Log
	private Log							LOG							= LogFactory.getLog(ConvOldPrefsJobImpl.class);

	// Sakai Services
	private StatsManager				statsManager				= null;
	public void setStatsManager(StatsManager statsManager) {
		this.statsManager = statsManager;
	}
	private SqlService					sqlService					= null;
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}
	private EventRegistryService eventRegistryService				= null;
	public void setEventRegistryService(EventRegistryService eventRegistryService) {
		this.eventRegistryService = eventRegistryService;
	}
	

	/** Convert old SST_PREFS to new SST_PREFERENCES. */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.info("Old SiteStats Preferences table conversion started...");
		if(areBothTablesPresent()){
			convertPrefs();
		}
		LOG.info("Old SiteStats Preferences table conversion finished.");
	}

	private boolean convertPrefs() {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		List<String> siteIds = getSitesInSSTPrefs();
		final String sitePrefSql = "select EVENT_ID from SST_PREFS where SITE_ID=? and PAGE=0 ORDER BY EVENT_ID";
		
		try{
			c = sqlService.borrowConnection();
			ps = c.prepareStatement(sitePrefSql);
			
			// Get preferences from SST_PREFS
			for(String siteId : siteIds) {
				try{
					// get list of events for current site
					List<String> eventIds = new ArrayList<String>();
					ps.clearParameters();
					ps.setString(1, siteId);
					rs = ps.executeQuery();
					while(rs.next()) {
						String eventId = rs.getString(1);
						eventIds.add(eventId);
						//LOG.info("Site '"+siteId+"' has selected: "+eventId);
					}
					rs.close();
					
					// build a new preferences object
					PrefsData prefs = new PrefsData();
					prefs.setToolEventsDef(eventRegistryService.getEventRegistry());
					List<ToolInfo> allTools = prefs.getToolEventsDef();
					for(ToolInfo ti : allTools) {
						boolean toolSelected = false;
						for(EventInfo ei : ti.getEvents()) {
							if(eventIds.contains(ei.getEventId())) {
								ei.setSelected(true);
								toolSelected = true;
							}else{
								ei.setSelected(false);
							}
						}
						ti.setSelected(toolSelected);
					}					
					
					// persist it
					statsManager.setPreferences(siteId, prefs);
					
					// check it
//					PrefsData prefsRead = statsManager.getPreferences(siteId, false);
//					for(ToolInfo ti2 : prefsRead.getToolEventsDef()) {
//						if(ti2.isSelected()) {
//							for(EventInfo ei2 : ti2.getEvents()) {
//								if(ei2.isSelected() && !eventIds.contains(ei2.getEventId())) {
//									LOG.warn("Check failed: event '"+ei2.getEventId()+"' selected in new preferences but unselected in old preferences.");
//								}
//								if(!ei2.isSelected() && eventIds.contains(ei2.getEventId())) {
//									LOG.warn("Check failed: event '"+ei2.getEventId()+"' unselected in new preferences but selected in old preferences.");
//								}
//							}
//						}
//					}
				}catch(SQLException e){
					LOG.error("An SQL error occurred while converting SST_PREFS data for site: "+siteId, e);
					return false;
				}					
			}
			
		}catch(SQLException e){
			LOG.error("An SQL error occurred while converting SST_PREFS data to SST_PREFERENCES table.", e);
		}finally{
			if(rs != null){
				try{ rs.close(); }catch(SQLException e){ /* ignore */ }
			}
			if(ps != null){
				try{ ps.close(); }catch(SQLException e){ /* ignore */ }
			}
			if(c != null){
				sqlService.returnConnection(c);
			}
		}

		return true;
	}

	private List<String> getSitesInSSTPrefs() {
		List<String> siteIds = new ArrayList<String>();
		Connection c = null;
		Statement s = null;
		ResultSet rs = null;
		try{
			c = sqlService.borrowConnection();
			// Get sites
			try{
				s = c.createStatement();
				rs = s.executeQuery("select distinct SITE_ID from SST_PREFS");
				while(rs.next()) {
					siteIds.add(rs.getString(1));
				}
			}catch(SQLException e){
				LOG.error("Unable to get list of sites from SST_PREFS.", e);
			}finally{
				if(rs != null){
					rs.close();
				}
				if(s != null){
					s.close();
				}
			}
		}catch(SQLException e){
			LOG.error("An SQL error occurred while getting list of sites from SST_PREFS.", e);
		}finally{
			if(c != null){
				sqlService.returnConnection(c);
			}
		}

		return siteIds;
	}

	private boolean areBothTablesPresent() {
		boolean present = false;
		Connection c = null;
		Statement s = null;
		ResultSet rs = null;
		try{
			c = sqlService.borrowConnection();
			// Check for SST_PREFS
			try{
				s = c.createStatement();
				rs = s.executeQuery("select count(*) from SST_PREFS");
				present = true;
			}catch(SQLException e){
				LOG.error("Table SST_PREFS doesn't exist! Create it before running this conversion script.", e);
				present = false;
			}finally{
				if(rs != null){
					rs.close();
				}
				if(s != null){
					s.close();
				}
			}
			// Check for SST_PREFERENCES
			try{
				s = c.createStatement();
				rs = s.executeQuery("select count(*) from SST_PREFERENCES");
				if(present) {
					present = true;
				}
			}catch(SQLException e){
				LOG.error("Table SST_PREFERENCES doesn't exist! Create it before running this conversion script.", e);
				present = false;
			}finally{
				if(rs != null){
					rs.close();
				}
				if(s != null){
					s.close();
				}
			}
		}catch(SQLException e){
			LOG.error("An SQL error occurred while checking for SST_PREFS and SST_PREFERENCES existence.", e);
		}finally{
			if(c != null){
				sqlService.returnConnection(c);
			}
		}

		return present;
	}

}
