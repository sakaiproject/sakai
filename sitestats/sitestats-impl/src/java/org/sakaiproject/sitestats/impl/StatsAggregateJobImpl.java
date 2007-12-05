package org.sakaiproject.sitestats.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.StatsUpdateManager;


public class StatsAggregateJobImpl implements Job {
	private Log					LOG					= LogFactory.getLog(StatsAggregateJobImpl.class);

	// Spring fields
	private int					maxEventsPerRun		= 0;
	private long				startEventId		= -1;
	private long 				lastEventIdInTable	= -1;

	private String				driverClassName		= null;
	private String				url					= null;
	private String				username			= null;
	private String				password			= null;
	
	// Relevant job fields
	private JobRun				jobRun				= null;
	private Object				extDbdriver			= null;
	private String				sqlGetEvent			= null;
	private final static int	SQL_BLOCK_SIZE		= 1000;
	private boolean				isOracle 			= false;
	private final static String MYSQL_GET_EVENT		= "select EVENT_ID as EVENT_ID,EVENT_DATE as EVENT_DATE,EVENT as EVENT,REF as REF,SESSION_USER as SESSION_USER,e.SESSION_ID as SESSION_ID " +
														"from SAKAI_EVENT e join SAKAI_SESSION s on e.SESSION_ID=s.SESSION_ID " +
														"where EVENT_ID >= ? " +
														"order by EVENT_ID asc " +
														"limit ? offset ?";
	private final static String ORACLE_GET_EVENT	= "SELECT * FROM ( " +
														"SELECT " +
															" ROW_NUMBER() OVER (ORDER BY EVENT_ID ASC) AS rn," +
															" EVENT_ID,EVENT_DATE,EVENT,REF,SESSION_USER,e.SESSION_ID SESSION_ID " +
														"from SAKAI_EVENT e join SAKAI_SESSION s on e.SESSION_ID=s.SESSION_ID " +
														"where EVENT_ID >= ? " +
														") " +
														"WHERE rn <= ? AND rn > ?";
	private final static String LAST_EVENT_ID		= "select max(EVENT_ID) LAST_ID from SAKAI_EVENT";
	
	// Services
	private StatsUpdateManager	statsUpdateManager	= null;
	private SqlService			sqlService			= null;

	public void init(){
		LOG.info("StatsAggregateJobImpl.init()");
		doInitialCheck();		
	}
	
	private void doInitialCheck() {
		JobRun lastJobRun = null;
		try{
			lastJobRun = getLastJobRun();
		}catch(Exception e){
			LOG.error("Make sure SST_JOB_RUN table is created before running the StatsAggregateJob job.");
		}
		if(lastJobRun == null){
			if(getStartEventId() < 0){
				LOG.warn("First StatsAggregateJob job run will use last SAKAI_EVENT.EVENT_ID (id = "+getLastEventIdInTable()+"). To override this, please specify a new eventId in sakai.properties (property: startEventId@org.sakaiproject.sitestats.api.StatsAggregateJob=n, where n>=0). This value is for the first job run only.");
			}else{
				LOG.warn("First jStatsAggregateJob job run will use 'startEventId' ("+getStartEventId()+") specified in sakai.properties. This value is for the first job run only.");
			}
		}
	}
	
	// ################################################################
	// Job related methods
	// ################################################################
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String result = null;
		String jobName = context.getJobDetail().getFullName();
		LOG.info("Starting job: " + jobName);

		// configure job
		JobRun lastJobRun;
		try{
			lastJobRun = getLastJobRun();
		}catch(Exception e){
			LOG.error("Error accessing SST_JOB_RUN table. Does this table exists? Aborting job...");
			return;
		}
		jobRun = new JobRunImpl();
		jobRun.setJobStartDate(new Date(System.currentTimeMillis()));
		if(lastJobRun != null){
			jobRun.setStartEventId(lastJobRun.getEndEventId() + 1);
		}else if(getStartEventId() >= 0){
			LOG.warn("First job run: using 'startEventId' ("+getStartEventId()+") specified in sakai.properties. This value is for the first job run only.");
			jobRun.setStartEventId(getStartEventId());
		}else{
			long lastEventIdInTable = getLastEventIdInTable();
			LOG.warn("First job run: no 'startEventId' specified in sakai.properties; using last SAKAI_EVENT.EVENT_ID (id = "+lastEventIdInTable+"). This value is for the first job run only.");
			jobRun.setStartEventId(lastEventIdInTable);
		}

		// start job
		result = startJob();
		LOG.info("Summary: " + result);

		// persist job info
		saveJobRun(jobRun);
		LOG.info("Finishing job: " + jobName);
	}

	private long getLastEventIdInTable() {
		if(lastEventIdInTable == -1) {
			Connection connection = getEventDbConnection();
			try{
				Statement st = connection.createStatement();
				ResultSet rs = st.executeQuery(LAST_EVENT_ID);
				if(rs.next()){
					lastEventIdInTable = rs.getLong("LAST_ID");
				}
				rs.close();			
				st.close();
			}catch(SQLException e){
				LOG.error("Unable to retrieve events", e); 
			}
			closeEventDbConnection(connection);
		}
		return lastEventIdInTable;
	}

	private String startJob() {
		List<Event> eventsQueue = new ArrayList<Event>();
		long counter = 0;
		long offset = 0;
		long lastProcessedEventId = 0;
		long lastProcessedEventIdWithSuccess = 0;
		long firstEventIdProcessed = -1;
		long firstEventIdProcessedInBlock = -1;
		Date lastEventDate = null;
		Date lastEventDateWithSuccess = null;
		boolean abortIteration = false;
		long start = System.currentTimeMillis();
		
		Connection connection = getEventDbConnection();
		long eventIdLowerLimit = getEventIdLowerLimit();
		try{
			PreparedStatement st = connection.prepareStatement(sqlGetEvent);
			ResultSet rs = null;
			
			while(!abortIteration) {
				abortIteration = true;
				st.clearParameters();
				st.setLong(1, eventIdLowerLimit);			// lower limit			
				if(!isOracle){
					st.setLong(2, SQL_BLOCK_SIZE);			// MySQL limit	
					st.setLong(3, offset);					// MySQL offset
				}else{
					st.setLong(2, SQL_BLOCK_SIZE + offset);	// Oracle limit	
					st.setLong(3, offset);					// Oracle offset
				}
				rs = st.executeQuery();
				
				while(rs.next()){
					abortIteration = false;
					Date date = new Date(rs.getDate("EVENT_DATE").getTime());
					String event = rs.getString("EVENT");
					String ref = rs.getString("REF");
					String sessionUser = rs.getString("SESSION_USER");
					String sessionId = rs.getString("SESSION_ID");
					
					eventsQueue.add( statsUpdateManager.buildEvent(date, event, ref, sessionUser, sessionId) );
					
					counter++;					
					lastProcessedEventId = rs.getInt("EVENT_ID");
					lastEventDate = date;
					if(firstEventIdProcessed == -1)
						firstEventIdProcessed = lastProcessedEventId;
					if(firstEventIdProcessedInBlock == -1)
						firstEventIdProcessedInBlock = lastProcessedEventId;					
				}
				rs.close();
				
				if(!abortIteration){
					// process events
					boolean processedOk = statsUpdateManager.collectEvents(eventsQueue);
					eventsQueue.clear();
					if(processedOk){
						lastProcessedEventIdWithSuccess = lastProcessedEventId;
						lastEventDateWithSuccess = lastEventDate;
						jobRun.setStartEventId(firstEventIdProcessed);
						jobRun.setEndEventId(lastProcessedEventIdWithSuccess);
						jobRun.setLastEventDate(lastEventDateWithSuccess);
						jobRun.setJobEndDate(new Date(System.currentTimeMillis()));
						saveJobRun(jobRun);
						firstEventIdProcessedInBlock = -1;
						if(counter >= getMaxEventsPerRun()){
							abortIteration = true;
						}else if(counter + SQL_BLOCK_SIZE < getMaxEventsPerRun()){
							offset += SQL_BLOCK_SIZE;	
						}else{
							offset += getMaxEventsPerRun() - counter;
						}
					}else{
						st.close();
						closeEventDbConnection(connection);
						abortIteration = true;
						String msg = "An error occurred while processing/persisting events to db. Please check your logs, fix possible problems and re-run this job (will start after last successful processed event).";
						LOG.error(msg);
						return msg; 
					}
				}
			}
			
			st.close();
		}catch(SQLException e){
			closeEventDbConnection(connection);
			LOG.error("Unable to retrieve events", e);
			return "Unable to retrieve events due to: " + e.getMessage(); 
		}
		
		if(firstEventIdProcessed == -1){
			LOG.warn("No events were returned - nothing to do.");
			// no data was processed
			long eventId = jobRun != null? jobRun.getEndEventId(): 0;
			firstEventIdProcessed = eventId;
			lastProcessedEventIdWithSuccess = eventId;
			jobRun.setStartEventId(eventId);
			jobRun.setEndEventId(eventId);
			jobRun.setLastEventDate(null);
			jobRun.setJobEndDate(new Date(System.currentTimeMillis()));
			saveJobRun(jobRun);
		}
		
		closeEventDbConnection(connection);
		long end = System.currentTimeMillis();
		return counter + " events processed (ids: "+firstEventIdProcessed+" - "+lastProcessedEventIdWithSuccess+") in "+((end-start)/1000)+"s";
	}

	private long getEventIdLowerLimit() {
		long start = getStartEventId();
		long nextEventId = jobRun.getStartEventId();
		if(nextEventId > start)
			start = nextEventId;
		return start;
	}
	
	private JobRun getLastJobRun() throws Exception {
		return statsUpdateManager.getLatestJobRun();
	}
	
	private boolean saveJobRun(JobRun jobRun) {
		boolean ok = false;
		try{
			ok = statsUpdateManager.saveJobRun(jobRun);
		}catch(Exception e){
			LOG.error("Unable to persist last job information to db.", e);
		}
		return ok;
	}


	// ################################################################
	// Util methods
	// ################################################################
	private Connection getEventDbConnection() {
		Connection connection = null;
		if(getUrl() == null){
			// SAKAI_EVENT and SAKAI_SESSION are on the same database
			try{
				connection = sqlService.borrowConnection();
				if(sqlService.getVendor().equals("oracle")){
					isOracle = true;
					sqlGetEvent = ORACLE_GET_EVENT;
				}else{
					isOracle = false;
					sqlGetEvent = MYSQL_GET_EVENT;
				}
			}catch(SQLException e){
				LOG.error("Unable to connect Sakai Db", e);
				return null;
			}catch(Exception e){
				LOG.error("Unable to connect to Sakai Db", e);
				return null;
			}
		}else{
			// SAKAI_EVENT and SAKAI_SESSION are on different database
			try{
				if(extDbdriver == null){
					extDbdriver = Class.forName(getDriverClassName()).newInstance();
					if(getDriverClassName().equals("oracle.jdbc.driver.OracleDriver")){
						isOracle = true;
						sqlGetEvent = ORACLE_GET_EVENT;
					}else{
						isOracle = false;
						sqlGetEvent = MYSQL_GET_EVENT;
					}
				}
				connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
			}catch(SQLException e){
				LOG.error("Unable to connect to " + getUrl(), e);
				return null;
			}catch(Exception e){
				LOG.error("Unable to connect to " + getUrl(), e);
				return null;
			}
		}
		return connection;
	}

	private void closeEventDbConnection(Connection connection) {
		if(getUrl() == null){
			if(connection != null){
				sqlService.returnConnection(connection);
			}
		}else{
			try{
				if(connection != null && !connection.isClosed()){
					connection.close();
				}
			}catch(SQLException e){
				LOG.error("Unable to close connection " + getUrl(), e);
			}
		}
	}

	// ################################################################
	// Spring related methods
	// ################################################################
	public int getMaxEventsPerRun() {
		return maxEventsPerRun;
	}

	public void setMaxEventsPerRun(int maxEventsPerRun) {
		this.maxEventsPerRun = maxEventsPerRun;
	}

	public long getStartEventId() {
		return startEventId;
	}

	public void setStartEventId(long startEventId) {
		this.startEventId = startEventId;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public StatsUpdateManager getStatsUpdateManager() {
		return statsUpdateManager;
	}

	public void setStatsUpdateManager(StatsUpdateManager statsUpdateManager) {
		this.statsUpdateManager = statsUpdateManager;
	}

	public SqlService getSqlService() {
		return sqlService;
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

}
