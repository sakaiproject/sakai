package org.sakaiproject.profile2.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;

/**
 * This is the Kudos calculation job.
 * 
 * <p>Certain items/events have weightings, these are calculated and summed to give a score.
 * <br />That score is then divided by the total number of possible items/weightings and converted to a percentage
 * to give the total kudos ranking for the user.
 * </p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class KudosJob implements StatefulJob {

	private static final Logger log = Logger.getLogger(KudosJob.class);
	
	private final String BEAN_ID = "org.sakaiproject.profile2.job.KudosJob";
	
	
	/**
	 * setup the rule map
	 */
	private final HashMap<String,BigDecimal> RULES = new HashMap<String,BigDecimal>() {
		{
			//points for profile completeness
			put("email", new BigDecimal(1));
			put("nickname", new BigDecimal(1));
			put("birthday", new BigDecimal(0.5));
			
			put("homePage", new BigDecimal(1));
			put("workPhone", new BigDecimal(1));
			put("homePhone", new BigDecimal(1));
			put("mobilePhone", new BigDecimal(1));
			
			put("position", new BigDecimal(0.5));
			put("department", new BigDecimal(0.5));
			put("school", new BigDecimal(0.5));
			put("room", new BigDecimal(0.5));
			put("degree", new BigDecimal(0.5));
			put("subjects", new BigDecimal(0.5));

			put("favouriteBooks", new BigDecimal(0.25));
			put("favouriteTvShows", new BigDecimal(0.25));
			put("favouriteMovies", new BigDecimal(0.25));
			put("favouriteQuotes", new BigDecimal(0.25));
			put("other", new BigDecimal(2));

			//points for usage - more points for the heavier usage
			put("hasImage", new BigDecimal(5));
			put("hasOneConnection", new BigDecimal(2));
			put("hasMoreThanTenConnections", new BigDecimal(3));

			put("hasOneSentMessage", new BigDecimal(2));
			put("hasMoreThanTenSentMessages", new BigDecimal(3));
			
			put("hasOneStatusUpdate", new BigDecimal(0.25));
			put("hasMoreThanTenStatusUpdates", new BigDecimal(1));
			put("hasMoreThanOneHundredStatusUpdates", new BigDecimal(2));

			put("twitterEnabled", new BigDecimal(2));

			put("hasOnePicture", new BigDecimal(0.25));
			put("hasMoreThanTenPictures", new BigDecimal(1));

			//points for openness in privacy
			put("connectionsProfileImage", new BigDecimal(0.05));
			put("allProfileImage", new BigDecimal(0.10));
			put("connectionsBasicInfo", new BigDecimal(0.05));
			put("allBasicInfo", new BigDecimal(0.10));
			put("connectionsContactInfo", new BigDecimal(0.05));
			put("allContactInfo", new BigDecimal(0.10));
			put("connectionsPersonalInfo", new BigDecimal(0.05));
			put("allPersonalInfo", new BigDecimal(0.10));
			put("connectionsStaffInfo", new BigDecimal(0.05));
			put("allStaffInfo", new BigDecimal(0.10));
			put("connectionsStudentInfo", new BigDecimal(0.05));
			put("allStudentInfo", new BigDecimal(0.10));
			put("connectionsSearch", new BigDecimal(0.05));
			put("allSearch", new BigDecimal(0.10));
			put("connectionsViewConnections", new BigDecimal(0.05));
			put("allViewConnections", new BigDecimal(0.10));
			put("connectionsViewStatus", new BigDecimal(0.05));
			put("allViewStatus", new BigDecimal(0.10));
			put("connectionsViewPictures", new BigDecimal(0.05));
			put("allViewPictures", new BigDecimal(0.10));

			put("showBirthYear", new BigDecimal(0.1));

			//points for others viewing their profile
			//put("hasMoreThanOneVisitor", new BigDecimal(0.05));
			//put("hasMoreThanTenUniqueVisitors", new BigDecimal(2));
			//put("hasMoreThanOneHundredUniqueVisitors", new BigDecimal(3));


		}
	};
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//abort if already running on THIS server node (cannot check other nodes)
		try {
			while(isJobCurrentlyRunning(context)) {
				String beanId = context.getJobDetail().getJobDataMap().getString(BEAN_ID);
				log.warn("Another instance of "+beanId+" is currently running - Execution aborted.");
				return;
			}
		} catch(SchedulerException e){
			log.error("Aborting job execution due to " +e.toString(), e);
			return;
		}
		
		log.info("KudosJob run");
		
		//start a session for admin so we can get full profiles
		Session session = sessionManager.startSession();
        sessionManager.setCurrentSession(session);
        session.setUserEid("admin");
        session.setUserId("admin");
				
		//get total possible score
		BigDecimal total = getTotal();
		
		//get total number of records
		List<String> profileUuids = profileLogic.getAllSakaiPersonIds();
		
		//iterate over list getting a chunk of profiles at a time
		for(String userUuid: profileUuids) {
		
			Person person = profileLogic.getPerson(userUuid);
			if(person == null){
				continue;
			}
			
			log.info("Processing user: " + userUuid + ", " + person.getDisplayName());
				
				
			//get score for user	
			BigDecimal score = getScoreAsPercentage(getScore(person), total);

			//save it
			if(profileLogic.updateKudos(userUuid, score)) {
				log.info("Kudos updated for user: " + userUuid + ", score: " + score);
			}
			
			
		}
		
		session.setUserId(null);
		session.setUserEid(null);
		
		log.info("KudosJob finished");

		
	}
	
	/**
	 * Are multiples of this job currently running?
	 * @param context
	 * @return
	 * @throws SchedulerException
	 */
	private boolean isJobCurrentlyRunning(JobExecutionContext context) throws SchedulerException {
		String beanId = context.getJobDetail().getJobDataMap().getString(BEAN_ID);
		List<JobExecutionContext> jobsRunning = context.getScheduler().getCurrentlyExecutingJobs();
		
		int jobsCount = 0;
		for(JobExecutionContext j : jobsRunning)
			if(StringUtils.equals(beanId, j.getJobDetail().getJobDataMap().getString(BEAN_ID))) {
				jobsCount++; //this job=1, any more and they are multiples.
			}
		if(jobsCount > 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Calculate the score for this person
	 * @param person	Person object
	 * @return
	 */
	private BigDecimal getScore(Person person) {
		return new BigDecimal(25);
	}
	
	/**
	 * Helper to perform an addition
	 * @param total		number that is to be added to
	 * @param addend	number that is to be added
	 * @return
	 */
	private BigDecimal add(BigDecimal total, BigDecimal addend) {
		return total.add(addend);
	}
	
	/**
	 * Gets the total of all BigDecimals in the RULES map
	 * @param map
	 * @return
	 */
	private BigDecimal getTotal() {
		
		BigDecimal total = new BigDecimal(0);
		
		if(RULES != null) {
			for(Map.Entry<String,BigDecimal> entry : RULES.entrySet()) {
				total = total.add(entry.getValue());
			}
		}
		return total;
	}
	
	/**
	 * Gets the score as a percentage, two decimal precision
	 * @param map
	 * @return
	 */
	private BigDecimal getScoreAsPercentage(BigDecimal score, BigDecimal total) {
		return score.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).stripTrailingZeros();
	}
	
	
	public void init(){
		log.info("KudosJob.init()");		
	}
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
}
