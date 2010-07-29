/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/SignupEventTypes.java $
 * $Id: SignupEventTypes.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic;

/**
 * <P>
 * This interface defines the constants of event types
 * </P>
 */
public class SignupEventTypes {

	public static String EVENT_SIGNUP_MTNG_ADD = "signup.mtngadd";	
	public static String EVENT_SIGNUP_MTNG_REMOVE = "signup.mtngdelete";
	public static String EVENT_SIGNUP_MTNG_MODIFY = "signup.mtngchanged";
	public static String EVENT_SIGNUP_MTNG_TS_LOCK = "signup.mtnglockts";
	public static String EVENT_SIGNUP_MTNG_TS_UNLOCK = "signup.mtngunlockts";
	public static String EVENT_SIGNUP_MTNG_TS_CANCEL = "signup.mtngcancelts";
	public static String EVENT_SIGNUP_MTNG_TS_UNCANCEL = "signup.mtnguncancelts";
	public static String EVENT_SIGNUP_ADD_ATTENDEE_L = "signup.attendeeadd";
	public static String EVENT_SIGNUP_REMOVE_ATTENDEE_L = "signup.attendeeremove";
	public static String EVENT_SIGNUP_REPLACE_ATTENDEE_L = "signup.attendeereplace";
	public static String EVENT_SIGNUP_MOVE_ATTENDEE_L = "signup.attendeemove";
	public static String EVENT_SIGNUP_SWAP_ATTENDEE_L = "signup.attendeeswapl ";
	public static String EVENT_SIGNUP_ADD_ATTENDEE_WL_L = "signup.attendeeaddwl";
	public static String EVENT_SIGNUP_REMOVE_ATTENDEE_WL_L = "signup.attendeeremovewl ";
	public static String EVENT_SIGNUP_ADD_ATTENDEE_S = "signup.studentsignup";
	public static String EVENT_SIGNUP_REMOVE_ATTENDEE_S = "signup.studentcancel";
	public static String EVENT_SIGNUP_ADD_ATTENDEE_WL_S = "signup.studentsignupwl";
	public static String EVENT_SIGNUP_REMOVE_ATTENDEE_WL_S = "signup.studentcancelwl ";

}
