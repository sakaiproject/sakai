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
public class MySignupEventsProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful,
		RequestStorable, RedirectDefinable{

	protected SakaiFacade sakaiFacade;

	protected SignupMeetingService signupMeetingService;

	protected EventProcessHandler eventProcessHandler;

	public static String PREFIX = "mySignup";

	public String getEntityPrefix() {
		return PREFIX;
	}
	
	public TemplateMap[] defineURLMappings() {
		// all sign-up events in a site
		return new TemplateMap[] { new TemplateMap("/{prefix}/user/{userId}", "{prefix}{dot-extension}")};
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
	 * Note that details is the only optional field
	 */
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		SignupEvent event = (SignupEvent) entity;
		// TODO now do-nothing
		throw new IllegalArgumentException("Method: 'New' is not supported.");
	}

	public Object getSampleEntity() {
		return new SignupEvent();
	}

	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		String id = ref.getId();
		//Do Nothing
		throw new IllegalArgumentException("Metho: 'updae' is not supported.");
	}

	public Object getEntity(EntityReference ref) {
		String id = ref.getId();
		//Do Nothing
		throw new IllegalArgumentException("Metho: 'get' is not supported.");
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		String id = ref.getId();
		//Do Nothing
		throw new IllegalArgumentException("Metho: 'Delete' is not supported.");
	}

	/**
	 * Example for accessing multiple events's Json objects URL: 
	 * http://localhost:8080/direct/mySignup/user/50c08b39-0232-4072-b5fc-7c5bc2e3e965.json?viewNextDays=30&rnd=32421
	 * where '50c08b39-0232-4072-b5fc-7c5bc2e3e965' is userId and 'rnd' is any random numbers
	 * <b>Important Note</b>: the userId is only used when you log in as admin. Otherwise it is not used and you
	 * can use any random number for this userId in url instead.
	 * */
	public List<?> getEntities(EntityReference ref, Search search) {				
		// get the user (if set)
		Restriction userRes = search.getRestrictionByProperty(CollectionResolvable.SEARCH_USER_REFERENCE);
		String userId = null;
		if (userRes != null) {
			String currentUser = developerHelperService.getCurrentUserReference();
			String userReference = userRes.getStringValue();//retrieved from url path info
			if (userReference == null) {
				throw new IllegalArgumentException("Invalid request: Cannot view event by user when the value is null");
			}
						
			if(developerHelperService.isUserAdmin(currentUser)){
					userId = developerHelperService.getUserIdFromRef(userReference);
					if (getSakaiFacade().getUser(userId) ==null ){
						throw new IllegalArgumentException("Invalid User Id");
					}
				
			} else {
				//only display current user signed up info no matter what
				userId = developerHelperService.getCurrentUserId();
				if (userId == null) {
					throw new SecurityException("User is not logged in, so no event data can be retrieved");
				}
				
			}
		}
		else {
			//display current user signed up info no matter what case!
			userId = developerHelperService.getCurrentUserId();
			if (userId == null) {
				throw new SecurityException("User is not logged in, so no event data can be retrieved");
			}
			
		}

		/* user's view range request if any*/
		String viewRange = (String) requestStorage.getStoredValueAsType(String.class,
				EventProcessHandler.VIEW_SIGNUP_EVENTS_RANGE);
		List<SignupEvent> events = getEventProcessHandler().getMySignupEvents(userId, viewRange);

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
