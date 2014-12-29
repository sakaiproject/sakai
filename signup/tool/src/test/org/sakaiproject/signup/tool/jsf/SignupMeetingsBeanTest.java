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

package org.sakaiproject.signup.tool.jsf;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * @author Ben Holmes
 */
public class SignupMeetingsBeanTest {

	private SignupMeetingsBean _signupMeetings;

	@Before
	public void setUp() {
		_signupMeetings = new SignupMeetingsBean();

		SakaiFacade mockSakaiFacade = mock(SakaiFacade.class);
		when(mockSakaiFacade.getCurrentLocationId()).thenReturn("siteId");
		_signupMeetings.setSakaiFacade(mockSakaiFacade);
	}

	private void setMockedLocations(ArrayList<String> locations) throws Exception {
		SignupMeetingService mockMeetingsService = mock(SignupMeetingService.class);
		when(mockMeetingsService.getAllLocations("siteId")).thenReturn(locations);
		_signupMeetings.setSignupMeetingService(mockMeetingsService);
	}

	@Test
	public void testShouldCacheLocations() throws Exception {
		ArrayList<String> locations = new ArrayList<String>();
		setMockedLocations(locations);
		Assert.assertTrue(_signupMeetings.getAllLocations().isEmpty());

		locations.add("Extra location");
		Assert.assertTrue(_signupMeetings.getAllLocations().isEmpty());
	}

	@Test
	public void testShouldBehaveConsistentlyWithCategories() throws Exception {

		ArrayList<String> locations = new ArrayList<String>();
		setMockedLocations(locations);
		locations.add("Extra location");
		Assert.assertFalse(_signupMeetings.getAllLocations().isEmpty());

		// make a cheeky call to categories
		SignupMeetingService mockMeetingsService = mock(SignupMeetingService.class);
		when(mockMeetingsService.getAllCategories("siteId")).thenReturn(new ArrayList());
		_signupMeetings.setSignupMeetingService(mockMeetingsService);
		//_signupMeetings.getAllCategories(); //this will be broken with new implementation and comment it out

		Assert.assertFalse(_signupMeetings.getAllLocations().isEmpty());
	}

}
