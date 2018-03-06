/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.entityproviders;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.restful.SignupEvent;

/**
 * Handles the signupEvent entity
 * 
 * @author Peter Liu
 */
public class SignupEventEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful,
		RequestStorable, RedirectDefinable {

	protected SakaiFacade sakaiFacade;

	protected SignupMeetingService signupMeetingService;

	protected EventProcessHandler eventProcessHandler;

	public static String PREFIX = "signupEvent";

	public String getEntityPrefix() {
		return PREFIX;
	}

	public TemplateMap[] defineURLMappings() {
		// all sign-up events in a site
		return new TemplateMap[] { new TemplateMap("/{prefix}/site/{siteId}", "{prefix}{dot-extension}")};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	public boolean entityExists(String id) {
		if (id == null) {
			return false;
		}
		if ("".equals(id)) {
			return true;
		}

		Long eventId;
		try {
			eventId = new Long(id);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid event id (" + id + "), the id must be a number");
		}
		return getSignupMeetingService().isEventExisted(eventId);
	}

	/**
	 * @param id
	 * @return
	 */
	private SignupEvent getEventById(String id, boolean mustAccessDB) {
		Long eventId;
		try {
			eventId = new Long(id);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid event id (" + id + "), the id must be a number");
		}

		String siteId = developerHelperService.getCurrentLocationId();
		if (siteId == null || siteId.trim().length() < 1) {
			siteId = (String) requestStorage.getStoredValueAsType(String.class, SignupEvent.SITE_ID_FIELD_NAME);
			if (siteId == null || siteId.trim().length() < 1) {
				//throw new IllegalArgumentException("Missing current site id: (" + id + "), it is required.");
				//now siteId is an optional parameter and it will find the siteId with the highest permission level for user
				siteId = null;
			}
		}

		SignupEvent event = getEventProcessHandler().getSignupEvent(eventId, siteId, sakaiFacade.getCurrentUserId(),
				mustAccessDB);
		event.setSiteId(siteId);// keep tracking current site
		return event;
	}

	/**
	 * Note that details is the only optional field
	 */
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		SignupEvent event = (SignupEvent) entity;
		// TODO now do-nothing
		throw new IllegalArgumentException("Method: 'New' is not supported currently.");
	}

	public Object getSampleEntity() {
		return new SignupEvent();
	}

	/**
	 * Example for updating event via json object:
	 * http://localhost:8080/direct/signupEvent/128/edit
	 * where '128 is eventId and with corresponding POST parameters.
	 */
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException("The reference must include an id for updates (id is currently null)");
		}
		String userReference = developerHelperService.getCurrentUserReference();
		if (userReference == null) {
			throw new SecurityException("anonymous user cannot update event: " + ref);
		}

		SignupEvent event = (SignupEvent) entity;

		String siteId = developerHelperService.getCurrentLocationId();
		if (siteId == null || siteId.trim().length() < 1) {
			siteId = event != null ? event.getSiteId() : null;
			if (siteId == null || siteId.trim().length() < 1) {
				siteId = (String) requestStorage.getStoredValueAsType(String.class, SignupEvent.SITE_ID_FIELD_NAME);
				if (siteId == null || siteId.trim().length() < 1) {
					throw new IllegalArgumentException("Missing current site id: (" + id + "), it is required.");
				}
			}
		}

		String userGoToTSid = event != null ? event.getAllocToTSid() : null;
		if (userGoToTSid == null || userGoToTSid.trim().length() < 1) {
			userGoToTSid = (String) requestStorage.getStoredValueAsType(String.class, SignupEvent.ALLOCATE_TO_TS_ID_FIELD_NAME);
			if (userGoToTSid == null || userGoToTSid.trim().length() < 1) {
				throw new IllegalArgumentException("Missing allocToTSid, it is required.");
			}
		}

		String userAction = event != null ? event.getUserActionType() : null;
		if (userAction == null || userAction.trim().length() < 1) {
			userAction = (String) requestStorage.getStoredValueAsType(String.class, SignupEvent.USER_ACTION_TYPE_FIELD_NAME);
			if (userAction == null || userAction.trim().length() < 1) {
				throw new IllegalArgumentException("Missing userActionType, it is required.");
			}
		}
		SignupEvent current = getEventById(id, true);
		if (current == null) {
			throw new IllegalArgumentException("No event found to update for the given reference: " + ref);
		}

		if (!(current.getPermission().isAttend() || current.getPermission().isUpdate()))
			throw new SecurityException("Current user (" + userReference + ") cannot attend event in location ("
					+ siteId + ")");

		current.setAllocToTSid(userGoToTSid);
		current.setSiteId(siteId);
		current.setUserActionType(userAction);
		getEventProcessHandler().updateAttendStatus(current);
		// TODO update the event only for attend purpose here
	}

	/**
	 * Example for accessing one event's Json object URL: 
	 * http://localhost:8080/direct/signupEvent/128.json?siteId=91ab88b6-f687-46e2-8645-f8cc1d26959c&rnd=32421
	 * where '128' is eventId, and 'rnd' is any random numbers.
	 * <b>Important Note</b>: the siteId is an optional parameter. If it is not supplied, it will return a target site, which
	 * the user has the highest permission level on the event.
	 * */
	public Object getEntity(EntityReference ref) {
		String id = ref.getId();
		if (id == null) {
			// TODO should return a new SignupEvent?
			return new SignupEvent();
		}

		String currentUserId = developerHelperService.getCurrentUserId();
		if (currentUserId == null) {
			throw new SecurityException("User must be logged in in order to access event data: " + ref);
		}

		SignupEvent event = getEventById(id, false);
		if (event == null) {
			throw new IllegalArgumentException("No event found for the given reference: " + ref);
		}

		String siteId = developerHelperService.getCurrentLocationId();
		if (siteId == null || siteId.trim().length() < 1) {
			siteId = (String) requestStorage.getStoredValueAsType(String.class, SignupEvent.SITE_ID_FIELD_NAME);
		}
		boolean allowedView = false;
		if (!developerHelperService.isEntityRequestInternal(ref + "")) {
			// not an internal request so we require user to be logged in
			String userReference = developerHelperService.getCurrentUserReference();
			if (!(event.getPermission().isAttend() || event.getPermission().isUpdate())) {
				throw new SecurityException("User (" + userReference + ") not allowed to access event data: " + ref);
			}
		}
		
		return event;
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		String id = ref.getId();
		//Do Nothing
		throw new IllegalArgumentException("Metho: 'Delete' is not supported currently.");
	}

	/**
	 * Example for accessing multiple events's Json objects URL: 
	 * http://localhost:8080/direct/signupEvent/site/91ab88b6-f687-46e2-8645-f8cc1d26959c.json?viewNextDays=30&rnd=32421
	 * where '91ab88b6-f687-46e2-8645-f8cc1d26959c' is siteId and 'rnd' is any random numbers
	 * */
	public List<?> getEntities(EntityReference ref, Search search) {
		// get the location (if set)
		Restriction locRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_LOCATION_REFERENCE); // requestStorage.getStoredValueAsType(String.class,
		// "siteId");
		String siteId = null;
		if (locRes != null) {
			siteId = developerHelperService.getLocationIdFromRef(locRes.getStringValue());
		}
		
		//if siteId still null, reutrn 400 as we are missing info
		if(siteId == null) {
			throw new IllegalArgumentException("Missing site id, cannot retrieve signup events");
		}
		
		// get the user (if set)
		Restriction userRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_USER_REFERENCE);
		String userId = null;
		if (userRes != null) {
			String currentUser = developerHelperService.getCurrentUserReference();
			String userReference = userRes.getStringValue();
			if (userReference == null) {
				throw new IllegalArgumentException("Invalid request: Cannot limit event by user when the value is null");
			}
			if (userReference.equals(currentUser) || developerHelperService.isUserAdmin(currentUser)) {
				userId = developerHelperService.getUserIdFromRef(userReference); // requestStorage.getStoredValueAsType(String.class,
				// "userId");
			} else {
				throw new SecurityException("Only the admin can get event for other users, you requested event for: "
						+ userReference);
			}
		} else {
			userId = developerHelperService.getCurrentUserId();
			if (userId == null) {
				throw new SecurityException("No user is currently logged in so no event data can be retrieved");
			}
		}

		/* user's view range request if any*/
		String viewRange = (String) requestStorage.getStoredValueAsType(String.class,
				EventProcessHandler.VIEW_SIGNUP_EVENTS_RANGE);
		List<SignupEvent> events = getEventProcessHandler().getSignupEvents(siteId, userId, viewRange);

		return events;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML };
	}

	RequestStorage requestStorage = null;

	public void setRequestStorage(RequestStorage requestStorage) {
		this.requestStorage = requestStorage;
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

	public EventProcessHandler getEventProcessHandler() {
		return eventProcessHandler;
	}

	public void setEventProcessHandler(EventProcessHandler eventProcessHandler) {
		this.eventProcessHandler = eventProcessHandler;
	}

}
