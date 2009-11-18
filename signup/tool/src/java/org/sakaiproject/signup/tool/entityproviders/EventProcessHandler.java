/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.signup.tool.entityproviders;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.restful.SignupEvent;
import org.sakaiproject.signup.tool.jsf.organizer.action.AddAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.AddWaiter;
import org.sakaiproject.signup.tool.jsf.organizer.action.CancelAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.RemoveWaiter;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.signup.util.SignupDateFormat;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>
 * This class will provides methods for processing attendee's actions via
 * RESTful links.
 * </P>
 * 
 * @author Peter Liu
 */
public class EventProcessHandler implements SignupBeanConstants {
	/* name of user request parameter for view range */
	public static final String VIEW_SIGNUP_EVENTS_RANGE = "viewNextDays";

	protected SakaiFacade sakaiFacade;

	protected SignupMeetingService signupMeetingService;

	protected SignupRESTfulSessionManager signupRESTfulSessionManager;

	protected Log logger = LogFactoryImpl.getLog(getClass());

	public SignupEvent getSignupEvent(Long eventId, String siteId, String userId, boolean mustAccessDB) {
		SignupEvent event = null;

		event = signupRESTfulSessionManager.getExistedSignupEventInCache(siteId, eventId);
		if (event == null || event.getSignupSiteItems() == null || mustAccessDB) {
			event = SignupObjectConverter.convertToSignupEventObj(getSignupMeetingService().loadSignupMeeting(eventId,
					userId, siteId), userId, siteId, true, this.sakaiFacade);
			signupRESTfulSessionManager.updateSiteEventsCache(siteId, event);
		}

		return event;
	}

	public List<SignupEvent> getSignupEvents(String siteId, String userId, String viewRange) {
		viewRange = validateViewRange(viewRange);
		List<SignupEvent> events = null;
		if (signupRESTfulSessionManager.isUpTodateDataAvailable(siteId)
				&& signupRESTfulSessionManager.isSameViewRange(siteId, viewRange))
			events = signupRESTfulSessionManager.getSiteEventsCache(siteId).getEvents();
		else {
			List<SignupMeeting> sMeetings = null;
			if (VIEW_ALL.equals(viewRange)) {// view all
				sMeetings = getSignupMeetingService().getAllSignupMeetings(siteId, userId);
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				/* including today's day for search */
				int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
				int currentMinutes = calendar.get(Calendar.MINUTE);
				calendar.add(Calendar.HOUR, -1 * currentHour);
				calendar.add(Calendar.MINUTE, -1 * currentMinutes);
				String searchDateStr = viewRange;

				sMeetings = signupMeetingService.getSignupMeetings(siteId, userId, calendar.getTime(), Utilities
						.getUserDefinedDate(Integer.parseInt(searchDateStr)));

			}

			events = new ArrayList<SignupEvent>();
			if (sMeetings != null && !sMeetings.isEmpty()) {
				for (SignupMeeting sMeeting : sMeetings) {
					events.add(SignupObjectConverter.convertToSignupEventObj(sMeeting, userId, siteId, false,
							this.sakaiFacade));
				}
			}

			signupRESTfulSessionManager.StoreSiteEventsData(siteId, events, viewRange);
		}

		return events;
	}

	public void updateAttendStatus(SignupEvent event) {
		String userAction = event.getUserActionType();
		SignupEvent updatedEvent = null;
		if (SignupEvent.USER_SIGNUP.equals(userAction))
			updatedEvent = userSignup(event);
		else if (SignupEvent.USER_CANCEL_SIGNUP.equals(userAction))
			updatedEvent = userCancelSignup(event);
		else if (SignupEvent.USER_ADD_TO_WAITLIST.equals(userAction))
			updatedEvent = userAddToWaitList(event);
		else if (SignupEvent.USER_REMOVE_FROM_WAITLIST.equals(userAction))
			updatedEvent = userRemoveFromWaitList(event);
		else {
			logger.warn("The userAction:" + userAction + " is not defined!");
		}

		signupRESTfulSessionManager.updateSiteEventsCache(event.getSiteId(), updatedEvent);

	}

	private SignupEvent userRemoveFromWaitList(SignupEvent event) {
		String userActionWarningMsg = null;
		SignupMeeting meeting = null;
		try {
			meeting = getSignupMeeting(event);
			RemoveWaiter removeWaiter = new RemoveWaiter(signupMeetingService, getSakaiFacade().getCurrentUserId(),
					event.getSiteId(), ON_BOTTOM_LIST, false);
			SignupAttendee newWaiter = new SignupAttendee(getSakaiFacade().getCurrentUserId(), event.getSiteId());
			SignupTimeslot timeSlot = getSignupTimeSlot(meeting, event.getAllocToTSid());
			meeting = removeWaiter.removeFromWaitingList(meeting, timeSlot, newWaiter);

			/* No double recording */
			if (ToolManager.getCurrentPlacement() == null)
				Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_REMOVE_ATTENDEE_WL_S, event.getSiteId()
						+ " meetingId:" + meeting.getId() + " -removed from wlist on TS:"
						+ SignupDateFormat.format_date_h_mm_a(timeSlot.getStartTime()));
		} catch (SignupUserActionException ue) {
			userActionWarningMsg = ue.getMessage();
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			userActionWarningMsg = Utilities.rb.getString("error.occurred_try_again");
		}

		SignupEvent evt = SignupObjectConverter.convertToSignupEventObj(meeting, getSakaiFacade().getCurrentUserId(),
				event.getSiteId(), true, this.sakaiFacade);
		evt.setUserActionWarningMsg(userActionWarningMsg);
		return evt;

	}

	private SignupEvent userAddToWaitList(SignupEvent event) {
		String userActionWarningMsg = null;
		SignupMeeting meeting = null;
									
		try {
			meeting = getSignupMeeting(event);
			/*check if it's allowed to add to the waitlist*/
			if(meeting.isAllowWaitList()){
				AddWaiter addWaiter = new AddWaiter(signupMeetingService, getSakaiFacade().getCurrentUserId(), event
						.getSiteId(), ON_BOTTOM_LIST, false);
				SignupAttendee newWaiter = new SignupAttendee(sakaiFacade.getCurrentUserId(), event.getSiteId());
				SignupTimeslot timeSlot = getSignupTimeSlot(meeting, event.getAllocToTSid());
				meeting = addWaiter.addToWaitingList(meeting, timeSlot, newWaiter);
	
				if (ToolManager.getCurrentPlacement() == null)
					Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_ADD_ATTENDEE_WL_S, event.getSiteId()
							+ " meetingId:" + meeting.getId() + " -added to wlist on TS:"
							+ SignupDateFormat.format_date_h_mm_a(timeSlot.getStartTime()));
			}
			else{
				userActionWarningMsg = Utilities.rb.getString("user.not.allowed.to.waitlist");
			}
		} catch (SignupUserActionException ue) {
			userActionWarningMsg = ue.getMessage();
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			userActionWarningMsg = Utilities.rb.getString("error.occurred_try_again");
		}

		SignupEvent evt = SignupObjectConverter.convertToSignupEventObj(meeting, getSakaiFacade().getCurrentUserId(),
				event.getSiteId(), true, this.sakaiFacade);
		evt.setUserActionWarningMsg(userActionWarningMsg);
		return evt;

	}

	private SignupEvent userCancelSignup(SignupEvent event) {
		String userActionWarningMsg = null;
		SignupMeeting meeting = null;
		try {
			meeting = getSignupMeeting(event);
			CancelAttendee signup = new CancelAttendee(signupMeetingService, getSakaiFacade().getCurrentUserId(), event
					.getSiteId(), false);
			SignupAttendee removedAttendee = new SignupAttendee(getSakaiFacade().getCurrentUserId(), event.getSiteId());
			meeting = signup.cancelSignup(meeting, getSignupTimeSlot(meeting, event.getAllocToTSid()), removedAttendee);
			if (ToolManager.getCurrentPlacement() == null)
				Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_REMOVE_ATTENDEE_S, event.getSiteId()
						+ " meetingId:" + meeting.getId()
						+ signup.getSignupEventTrackingInfo().getAllAttendeeTransferLogInfo());
			/* send notification to organizer and possible promoted participants */
			try {
				/* Pass current siteId for email */
				signup.getSignupEventTrackingInfo().getMeeting().setCurrentSiteId(event.getSiteId());
				signupMeetingService.sendCancellationEmail(signup.getSignupEventTrackingInfo());
			} catch (Exception e) {
				logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
				// Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
			}

		} catch (SignupUserActionException ue) {
			// Utilities.addErrorMessage(ue.getMessage());
			userActionWarningMsg = ue.getMessage();
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			userActionWarningMsg = Utilities.rb.getString("error.occurred_try_again");
		}

		SignupEvent evt = SignupObjectConverter.convertToSignupEventObj(meeting, getSakaiFacade().getCurrentUserId(),
				event.getSiteId(), true, this.sakaiFacade);
		evt.setUserActionWarningMsg(userActionWarningMsg);
		return evt;
	}

	private SignupEvent userSignup(SignupEvent event) {
		String userActionWarningMsg = null;
		SignupMeeting meeting = null;
		try {
			meeting = getSignupMeeting(event);
			AddAttendee signup = new AddAttendee(signupMeetingService, getSakaiFacade().getCurrentUserId(), event
					.getSiteId(), false);// not organizer
			SignupAttendee signupAttendee = new SignupAttendee(getSakaiFacade().getCurrentUserId(), event.getSiteId());
			meeting = signup.signup(meeting, getSignupTimeSlot(meeting, event.getAllocToTSid()), signupAttendee);
			if (ToolManager.getCurrentPlacement() == null)
				Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_ADD_ATTENDEE_S, event.getSiteId()
						+ " meetingId:" + meeting.getId()
						+ signup.getSignupEventTrackingInfo().getAllAttendeeTransferLogInfo());

			/* send notification to organizer */
			if (meeting.isReceiveEmailByOwner()) {
				try {
					/* Pass current siteId for email */
					signup.getSignupEventTrackingInfo().getMeeting().setCurrentSiteId(event.getSiteId());
					signupMeetingService.sendEmailToOrganizer(signup.getSignupEventTrackingInfo());
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					// Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
		} catch (SignupUserActionException ue) {
			userActionWarningMsg = ue.getMessage();
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			userActionWarningMsg = Utilities.rb.getString("error.occurred_try_again");
		}

		SignupEvent evt = SignupObjectConverter.convertToSignupEventObj(meeting, getSakaiFacade().getCurrentUserId(),
				event.getSiteId(), true, this.sakaiFacade);
		evt.setUserActionWarningMsg(userActionWarningMsg);
		return evt;
	}

	/* need more efficiency, not load again */
	private SignupMeeting getSignupMeeting(SignupEvent event) {
		SignupMeeting meeting = signupMeetingService.loadSignupMeeting(event.getEventId(), getSakaiFacade()
				.getCurrentUserId(), event.getSiteId());
		return meeting;
	}

	private SignupTimeslot getSignupTimeSlot(SignupMeeting sMeeting, String tsId) {
		List<SignupTimeslot> tsList = sMeeting.getSignupTimeSlots();
		if (tsList != null) {
			for (SignupTimeslot ts : tsList) {
				if (ts.getId().toString().equals(tsId))
					return ts;
			}
		}

		return null;
	}

	private String validateViewRange(String viewRange) {
		String vRange = VIEW_ALL;
		if (viewRange != null) {
			try {
				Integer.parseInt(viewRange);
				vRange = viewRange;
			} catch (Exception e) {
				// do nothing
			}
		}
		return vRange;
	}

	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	public SignupMeetingService getSignupMeetingService() {
		return signupMeetingService;
	}

	public void setSignupMeetingService(SignupMeetingService signupMeetingService) {
		this.signupMeetingService = signupMeetingService;
	}

	public SignupRESTfulSessionManager getSignupRESTfulSessionManager() {
		return signupRESTfulSessionManager;
	}

	public void setSignupRESTfulSessionManager(SignupRESTfulSessionManager signupRESTfulSessionManager) {
		this.signupRESTfulSessionManager = signupRESTfulSessionManager;
	}

}
