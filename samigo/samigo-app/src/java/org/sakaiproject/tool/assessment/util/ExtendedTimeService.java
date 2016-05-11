package org.sakaiproject.tool.assessment.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.time.cover.TimeService;

/**
 * This class will instantiate with all the proper values for the current user's
 * extended time values for the given published assessment.
 * 
 * @author pdagnall1
 *
 */
public class ExtendedTimeService {

	private static String EXTENDED_TIME_KEY = "extendedTime";
	private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private String siteId;
	private AuthzGroupService authzGroupService;

	private boolean hasExtendedTime;
	private Integer timeLimit;
	private Date startDate;
	private Date dueDate;
	private Date retractDate;
	private String metaString; // holds the extended time info for the current
								// user

	public ExtendedTimeService(PublishedAssessmentFacade publishedAssessment) {
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		PublishedAssessmentFacade metaPublishedAssessment = assessmentService
				.getPublishedAssessmentQuick(publishedAssessment.getPublishedAssessmentId().toString());
		if (!assessmentInitialized(publishedAssessment)) {
			publishedAssessment = metaPublishedAssessment;
		}
	    authzGroupService = ComponentManager.get(AuthzGroupService.class);

		// Grab the site id from the publishedAssessment because the user may
		// not be in a site
		// if they're taking the test via url.
		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		String pubId = publishedAssessment.getPublishedAssessmentId().toString();
		siteId = publishedAssessmentService.getPublishedAssessmentSiteId(pubId);

		this.metaString = extractMetaString(metaPublishedAssessment);
		this.hasExtendedTime = (metaString != null);
		if (this.hasExtendedTime) {
			this.timeLimit = extractExtendedTime();
			this.startDate = determineDate(1, publishedAssessment.getStartDate());
			this.dueDate = determineDate(2, publishedAssessment.getDueDate());
			this.retractDate = determineDate(3, publishedAssessment.getRetractDate());
		} else {
			this.timeLimit = 0;
			this.startDate = publishedAssessment.getStartDate();
			this.dueDate = publishedAssessment.getDueDate();
			this.retractDate = publishedAssessment.getRetractDate();
		}
	}

	// Depending on the scope the assessment info sometimes is not initialized.
	private boolean assessmentInitialized(PublishedAssessmentFacade publishedAssessment) {
		if (publishedAssessment == null)
			return false;
		if (publishedAssessment.getStartDate() != null)
			return true;
		if (publishedAssessment.getDueDate() != null)
			return true;
		if (publishedAssessment.getRetractDate() != null)
			return true;
		if (publishedAssessment.getTimeLimit() != null)
			return true;
		return false;
	}

	// This sets the metString that holds the extended time info for the user
	private String extractMetaString(PublishedAssessmentFacade publishedAssessment) {
		short itemNum = 1;
		String meta = null;
		String extendedTimeData = publishedAssessment.getAssessmentMetaDataByLabel(EXTENDED_TIME_KEY + itemNum);
		while ((extendedTimeData != null) && (!extendedTimeData.equals(""))) {

			String[] extendedTimeItems = extendedTimeData.split("[|]");

			// Get target user/group value
			String target = extendedTimeItems[0];

			// If it's a group determine if user is a member
			boolean isMember = isUserInGroup(target);

			String userId = AgentFacade.getAgentString();
			if (target.equals(userId) || isMember) {
				meta = extendedTimeData;
			}
			itemNum++;
			extendedTimeData = publishedAssessment.getAssessmentMetaDataByLabel(EXTENDED_TIME_KEY + itemNum);
		}
		return meta;
	}

	/**
	 * If this user has been assigned an extended time then we'll return the
	 * time value. Otherwise we'll return null.
	 * 
	 * @param delivery
	 * @param publishedAssessment
	 * @return
	 */
	private int extractExtendedTime() {
		int extendedTime = 0;
		String[] extendedTimeItems = metaString.split("[|]");
		extendedTime = Integer.parseInt(extendedTimeItems[1]);
		return extendedTime;
	}

	/**
	 * Return the default date unless there are extended time dates we should
	 * use instead.
	 * 
	 * @param dateType
	 *            - 1: Start Date, 2. Due Date 3. Retract Date
	 * @param defaultDate
	 * @return
	 */
	private Date determineDate(int dateType, Date defaultDate) {
		Date xtDate = defaultDate;

		String[] extendedTimeItems = metaString.split("\\|", -1);

		// no entry means user has chosen not to override, use default
		if (extendedTimeItems.length < dateType + 2) { // check for no entry
			return defaultDate;
		}

		// blank entry means user wants no limit, except start always has to be defined
		String dateString = extendedTimeItems[dateType + 1];
		if (dateString == null || dateString.equals("")) { // check for blanks
			if (dateType == 1) // start
				return defaultDate;
			else
				return null;
		}

		return parseDate(dateString, xtDate);
	}

	private Date parseDate(String dateString, Date xtDate) {
		try {
			//xtDate = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.ENGLISH).parse(dateString);
			xtDate = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(dateString);
			this.hasExtendedTime = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return xtDate;
	}

	private boolean isUserInGroup(String groupId) {
		String realmId = "/site/" + siteId + "/group/" + groupId;
		boolean isMember = false;
		try {
			AuthzGroup group = authzGroupService.getAuthzGroup(realmId);
			if (group.getUserRole(AgentFacade.getAgentString()) != null)
				isMember = true;
		} catch (Exception e) {
			return false; // this isn't a group
		}
		return isMember;
	}

	// convert extended time string between two time zones

	public static String convertZones(String times, TimeZone fromZone, TimeZone toZone) {
	    if (times == null || times.equals("")) {
		return times;
	    }
	    String[] values = times.split("\\|", -1);
	    String ret = values[0] + "|" + values[1];
	    // if < 5, this is version with no dates, so nothing to convert
	    if (values.length != 5) {
		return times;
	    }
	    for (int i = 2; i < 5; i++) {
		String timeString = values[i];
		if (!timeString.equals("")) {
		    try {
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
			df.setTimeZone(fromZone);
			Date timeDate = df.parse(timeString);
			df.setTimeZone(toZone);
			timeString = df.format(timeDate);
		    } catch (Exception e) {
			// leaves string alone
		    }
		}
		ret += "|" + timeString;
	    }
	    return ret;
	}

	public Integer getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(Integer timeLimit) {
		this.timeLimit = timeLimit;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getRetractDate() {
		return retractDate;
	}

	public void setRetractDate(Date retractDate) {
		this.retractDate = retractDate;
	}

	public boolean hasExtendedTime() {
		return hasExtendedTime;
	}

	public void setHasExtendedTime(boolean hasExtendedTime) {
		this.hasExtendedTime = hasExtendedTime;
	}
}