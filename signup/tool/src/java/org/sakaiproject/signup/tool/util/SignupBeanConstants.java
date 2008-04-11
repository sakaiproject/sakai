/**********************************************************************************
 * $URL: https://sakai21-dev.its.yale.edu/svn/signup/branches/2-5/tool/src/java/org/sakaiproject/signup/tool/util/SignupBeanConstants.java $
 * $Id: SignupBeanConstants.java 2975 2008-04-11 16:01:29Z gl256 $
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool.util;

public interface SignupBeanConstants {
	
	public static final String MOVE_ACTION = "moveAction";

	public static final String REPLACE_ACTION = "replaceAction";

	public static final String SWAP_ACTION = "swapAction";
	
	public static final String ON_TOP_LIST="toTopOfList";
	
	public static final String ON_BOTTOM_LIST="toBbottomOfList";
	
	public static final String ATTENDEE_USER_ID = "attendeeUserId";
	
	public static final String THIRTY_DAYS = "30";
	
	public static final String NINTY_DAYS ="90";
	
	public static final String HALF_YEAR = "180";
	
	public static final String ALL_FUTURE =	"10000";//over 20 years
	
	public static final String OLD_DAYS =	"-1";
	
	public static final String VIEW_ALL ="all";
	
	public static final long dataRefreshInterval= 5*60*1000;//5 minutes
	
	public final static int MINUTE_IN_MILLISEC = 1000 * 60;// milli-seconds
	
	public static int DAY_IN_MINUTES=24*60;
	
	public static int Hour_In_MINUTES=60;
	
	public static final String DAYS ="days";
	
	public static final String HOURS ="hours";
	
	public static final String MINUTES ="minutes";
	
	public static final int MAX_NUMBER_OF_RETRY = 20;
	
	
	/** define the JSF action outcomes constants*/
	public static final int MAX_NUM_PARTICIPANTS_FOR_DROPDOWN_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE= 600;//1000;
	
	public static final String MAIN_EVENTS_LIST_PAGE_URL="listMeetings";
	
	public static final String ORGANIZER_MEETING_PAGE_URL = "organizerMeeting";

	public static final String VIEW_COMMENT_PAGE_URL = "viewComment";

	public static final String COPTY_MEETING_PAGE_URL = "copyMeeting";

	public static final String MODIFY_MEETING_PAGE_URL = "modifyMeeting";
	
	public static final String ATTENDEE_MEETING_PAGE_URL="meeting";
	
	public static final String ATTENDEE_ADD_COMMENT_PAGE_URL="addSignupAttendee";
	
	public static final String ADD_MEETING_PAGE_URL="addMeeting";
	
	public static final String CANCEL_ADD_MEETING_PAGE_URL="cancelAddMeeting";
	
	public static final String PRE_ASSIGN_ATTENDEE_PAGE_URL="assignStudents";
	
	public static final String ADD_MEETING_STEP1_PAGE_URL="addMeetingStep1";
	
	public static final String ADD_MEETING_STEP2_PAGE_URL="addMeetingStep2";
	
	public static final String  ADD_MEETING_STEP3_PAGE_URL="addMeetingStep3";
	
	public static final String ADD_MEETING_STEP4_PAGE_URL="addMeetingStep4";
	
	public static final String ADD_MEETING_STEP5_PAGE_URL="addMeetingStep5";

}
