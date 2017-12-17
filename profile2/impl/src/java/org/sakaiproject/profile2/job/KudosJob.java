/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileExternalIntegrationLogic;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfileKudosLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.ProfileStatusLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

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
@Slf4j
 public class KudosJob implements StatefulJob {

	private final String BEAN_ID = "org.sakaiproject.profile2.job.KudosJob";
	
	
	/**
	 * setup the rule map
	 */
	private final HashMap<String,BigDecimal> RULES = new HashMap<String,BigDecimal>() {
		private static final long serialVersionUID = 1L;

		{
			//points for profile completeness
			put("nickname", new BigDecimal("1"));
			put("birthday", new BigDecimal("0.5"));
			
			put("email", new BigDecimal("1"));
			put("homePage", new BigDecimal("1"));
			put("workPhone", new BigDecimal("1"));
			put("homePhone", new BigDecimal("1"));
			put("mobilePhone", new BigDecimal("1"));
			
			put("position", new BigDecimal("0.5"));
			put("department", new BigDecimal("0.5"));
			put("school", new BigDecimal("0.5"));
			put("room", new BigDecimal("0.5"));
			put("course", new BigDecimal("0.5"));
			put("subjects", new BigDecimal("0.5"));

			put("favouriteBooks", new BigDecimal("0.25"));
			put("favouriteTvShows", new BigDecimal("0.25"));
			put("favouriteMovies", new BigDecimal("0.25"));
			put("favouriteQuotes", new BigDecimal("0.25"));
			put("personalSummary", new BigDecimal("2"));

			//points for openness in privacy
			put("profileImageShared", new BigDecimal("0.05"));
			put("profileImageBonus", new BigDecimal("0.05"));
			put("basicInfoShared", new BigDecimal("0.05"));
			put("basicInfoBonus", new BigDecimal("0.05"));
			put("contactInfoShared", new BigDecimal("0.05"));
			put("contactInfoBonus", new BigDecimal("0.05"));
			put("personalInfoShared", new BigDecimal("0.05"));
			put("personalInfoBonus", new BigDecimal("0.05"));
			put("staffInfoShared", new BigDecimal("0.05"));
			put("staffInfoBonus", new BigDecimal("0.05"));
			put("studentInfoShared", new BigDecimal("0.05"));
			put("studentInfoBonus", new BigDecimal("0.05"));
			put("viewConnectionsShared", new BigDecimal("0.05"));
			put("viewConnectionsBonus", new BigDecimal("0.05"));
			put("viewStatusShared", new BigDecimal("0.05"));
			put("viewStatusBonus", new BigDecimal("0.05"));
			put("viewPicturesShared", new BigDecimal("0.05"));
			put("viewPicturesBonus", new BigDecimal("0.05"));

			put("showBirthYear", new BigDecimal("0.1"));
			
			//points for usage - more points for the heavier usage
			put("hasImage", new BigDecimal("5"));

			put("hasOneConnection", new BigDecimal("2"));
			put("hasMoreThanTenConnections", new BigDecimal("3"));

			put("hasOneSentMessage", new BigDecimal("2"));
			put("hasMoreThanTenSentMessages", new BigDecimal("3"));
			
			put("hasOneStatusUpdate", new BigDecimal("0.25"));
			
			// add when PRFL-191 is added
			//put("hasMoreThanTenStatusUpdates", new BigDecimal(1));
			//put("hasMoreThanOneHundredStatusUpdates", new BigDecimal(2));

			put("twitterEnabled", new BigDecimal("2"));

			put("hasOneGalleryPicture", new BigDecimal("0.25"));
			put("hasMoreThanTenGalleryPictures", new BigDecimal("1"));

			//points for others viewing their profile, not yet implemented
			//put("hasMoreThanOneVisitor", new BigDecimal(0.05));
			//put("hasMoreThanTenUniqueVisitors", new BigDecimal(2));
			//put("hasMoreThanOneHundredUniqueVisitors", new BigDecimal(3));


		}
	};
	
	
	/**
	 * Calculate the score for this person
	 * @param person	Person object
	 * @return
	 */
	private BigDecimal getScore(Person person) {
		
		BigDecimal score = new BigDecimal(0);
		
		//profile
		UserProfile profile = person.getProfile();
		if(profile != null){
			//basic
			if(nb(profile.getNickname())){
				score = score.add(val("nickname"));
			}
			if(nb(profile.getBirthday())){
				score = score.add(val("birthday"));
			}
			
			//contact
			if(nb(profile.getEmail())){
				score = score.add(val("email"));
			}
			if(nb(profile.getHomepage())){
				score = score.add(val("homePage"));
			}
			if(nb(profile.getWorkphone())){
				score = score.add(val("workPhone"));
			}
			if(nb(profile.getHomephone())){
				score = score.add(val("homePhone"));
			}
			if(nb(profile.getMobilephone())){
				score = score.add(val("mobilePhone"));
			}
			
			//staff/student
			if(nb(profile.getPosition())){
				score = score.add(val("position"));
			}
			if(nb(profile.getDepartment())){
				score = score.add(val("department"));
			}
			if(nb(profile.getSchool())){
				score = score.add(val("school"));
			}
			if(nb(profile.getRoom())){
				score = score.add(val("room"));
			}
			if(nb(profile.getCourse())){
				score = score.add(val("course"));
			}
			if(nb(profile.getSubjects())){
				score = score.add(val("subjects"));
			}
			
			//personal
			if(nb(profile.getFavouriteBooks())){
				score = score.add(val("favouriteBooks"));
			}
			if(nb(profile.getFavouriteTvShows())){
				score = score.add(val("favouriteTvShows"));
			}
			if(nb(profile.getFavouriteMovies())){
				score = score.add(val("favouriteMovies"));
			}
			if(nb(profile.getFavouriteQuotes())){
				score = score.add(val("favouriteQuotes"));
			}
			if(nb(profile.getPersonalSummary())){
				score = score.add(val("personalSummary"));
			}
		}
		
		ProfilePrivacy privacy = person.getPrivacy();
		if(privacy != null){
			
			//profile image
			switch(privacy.getProfileImage()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("profileImageShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("profileImageShared"));
					score = score.add(val("profileImageBonus"));
				break;
			}
			
			//basic info
			switch(privacy.getBasicInfo()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("basicInfoShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("basicInfoShared"));
					score = score.add(val("basicInfoBonus"));
				break;
			}
			
			//contact info
			switch(privacy.getContactInfo()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("contactInfoShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("contactInfoShared"));
					score = score.add(val("contactInfoBonus"));
				break;
			}
			
			//personal info
			switch(privacy.getPersonalInfo()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("personalInfoShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("personalInfoShared"));
					score = score.add(val("personalInfoBonus"));
				break;
			}
			
			//staff info
			switch(privacy.getStaffInfo()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("staffInfoShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("staffInfoShared"));
					score = score.add(val("staffInfoBonus"));
				break;
			}
			
			//student info
			switch(privacy.getStudentInfo()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("studentInfoShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("studentInfoShared"));
					score = score.add(val("studentInfoBonus"));
				break;
			}
			
			//view connections
			switch(privacy.getMyFriends()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("viewConnectionsShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("viewConnectionsShared"));
					score = score.add(val("viewConnectionsBonus"));
				break;
			}
			
			//view status
			switch(privacy.getMyStatus()) {
				case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
					score = score.add(val("viewStatusShared"));
				break;
				case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
					score = score.add(val("viewStatusShared"));
					score = score.add(val("viewStatusBonus"));
				break;
			}
			
			//view pictures. if it's disabled, assign full points
			if(sakaiProxy.isProfileGalleryEnabledGlobally()) {
				switch(privacy.getMyPictures()) {
					case (ProfileConstants.PRIVACY_OPTION_ONLYFRIENDS) :
						score = score.add(val("viewPicturesShared"));
					break;
					case (ProfileConstants.PRIVACY_OPTION_EVERYONE) :
						score = score.add(val("viewPicturesShared"));
						score = score.add(val("viewPicturesBonus"));
					break;
				}
			} else {
				score = score.add(val("viewPicturesShared"));
				score = score.add(val("viewPicturesBonus"));
			}
			
			//birth year visible
			if(privacy.isShowBirthYear()){
				score = score.add(val("showBirthYear"));
			}
			
		}
		
		//points for image that isn't the default
		ProfileImage image = imageLogic.getProfileImage(person, ProfileConstants.PROFILE_IMAGE_MAIN);
		if(image != null){
			if(image.getBinary() != null) {
				score = score.add(val("hasImage"));
			}
			if(!StringUtils.equals(image.getUrl(), imageLogic.getUnavailableImageURL())) {
				score = score.add(val("hasImage"));
			}
		}
		
		//number of connections
		int numConnections = connectionsLogic.getConnectionsForUserCount(person.getUuid());
		if(numConnections >= 1){
			score = score.add(val("hasOneConnection"));
		}
		if(numConnections > 10){
			score = score.add(val("hasMoreThanTenConnections"));
		}
		
		//number of sent messages
		int numSentMessages = messagingLogic.getSentMessagesCount(person.getUuid());
		if(numSentMessages >= 1){
			score = score.add(val("hasOneSentMessage"));
		}
		if(numSentMessages > 10){
			score = score.add(val("hasMoreThanTenSentMessages"));
		}
		
		//number of status updates
		int numStatusUpdates = statusLogic.getStatusUpdatesCount(person.getUuid());
		if(numStatusUpdates >= 1) {
			score = score.add(val("hasOneStatusUpdate"));
		}
		/* enable for PRFL-191 as well as entries in map above.
		if(numStatusUpdates > 10) {
			score = score.add(val("hasMoreThanTenStatusUpdates"));
		}
		if(numStatusUpdates > 100) {
			score = score.add(val("hasMoreThanOneHundredStatusUpdates"));
		}
		*/
		
		/*
		ProfilePreferences prefs = person.getPreferences();
		if(prefs != null){
			//is twitter enabled?
			if(prefs.isTwitterEnabled()) {
				score = score.add(val("twitterEnabled"));
			}
		}
		*/
		ExternalIntegrationInfo externalIntegrationInfo = externalIntegrationLogic.getExternalIntegrationInfo(person.getUuid());
		if(externalIntegrationInfo != null){
			if(externalIntegrationInfo.isTwitterAlreadyConfigured()) {
				score = score.add(val("twitterEnabled"));
			}
		}
		
		//if gallery enabled, number of gallery pictures
		if(sakaiProxy.isProfileGalleryEnabledGlobally()){
			int numGalleryPictures = imageLogic.getGalleryImagesCount(person.getUuid());
			if(numGalleryPictures >= 1) {
				score = score.add(val("hasOneGalleryPicture"));
			}
			if(numGalleryPictures > 10){
				score = score.add(val("hasMoreThanTenGalleryPictures"));
			}
		}
		
		
		return score;
		
	}
	
	@Override
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
		log.info("Total score possible: " + total.setScale(2, RoundingMode.HALF_UP));
		
		//get total number of records
		List<String> profileUuids = profileLogic.getAllSakaiPersonIds();
		
		//iterate over list getting a chunk of profiles at a time
		for(String userUuid: profileUuids) {
		
			Person person = profileLogic.getPerson(userUuid);
			if(person == null){
				continue;
			}
			
			log.info("Processing user: " + userUuid + " (" + person.getDisplayName() + ")");
				
			//get score for user
			BigDecimal score = getScore(person);
			BigDecimal percentage = getScoreAsPercentage(score, total);
			int adjustedScore = getScoreOutOfTen(score, total);

			//save it
			if(kudosLogic.updateKudos(userUuid, adjustedScore, percentage)) {
				log.info("Kudos updated for user: " + userUuid + ", score: " + score.setScale(2, RoundingMode.HALF_UP) + ", percentage: " + percentage + ", adjustedScore: " + adjustedScore);
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
	 * Helper for StringUtils.isNotBlank
	 * @param s1	String to check
	 * @return
	 */
	private boolean nb(String s1){
		return StringUtils.isNotBlank(s1);
	}
	
	/**
	 * Helper to get the value of the key from the RULES map
	 * @param key	key to getvalue for
	 * @return		BigDecimal value
	 */
	private BigDecimal val(String key){
		return RULES.get(key);
	}
	
	/**
	 * Gets the total of all BigDecimals in the RULES map
	 * @param map
	 * @return
	 */
	private BigDecimal getTotal() {
		
		BigDecimal total = new BigDecimal("0");
		
		if(RULES != null) {
			for(Map.Entry<String,BigDecimal> entry : RULES.entrySet()) {
				total = total.add(entry.getValue());
			}
		}
		return total;
	}
	
	/**
	 * Gets the score as a percentage, two decimal precision
	 * @param score		score for user
	 * @param total		total possible score
	 * @return
	 */
	private BigDecimal getScoreAsPercentage(BigDecimal score, BigDecimal total) {
		return score.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).stripTrailingZeros();
	}
	
	/**
	 * Gets the score out of ten as an int, and rounded up
	 * @param score		score for user
	 * @param total		total possible score
	 * @return
	 */
	private static int getScoreOutOfTen(BigDecimal score, BigDecimal total) {
		return score.divide(total, 1, RoundingMode.HALF_UP).multiply(new BigDecimal("10")).intValue();
	}
	
	
	public void init(){
		log.info("KudosJob.init()");		
	}
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileLogic profileLogic;
	
	@Setter
	private ProfileKudosLogic kudosLogic;
	
	@Setter
	private ProfileImageLogic imageLogic;
	
	@Setter
	private ProfileConnectionsLogic connectionsLogic;
	
	@Setter
	private ProfileMessagingLogic messagingLogic;
	
	@Setter
	private ProfileStatusLogic statusLogic;
	
	@Setter
	private ProfileExternalIntegrationLogic externalIntegrationLogic;
	
	@Setter
	private SessionManager sessionManager;
	
	
}
