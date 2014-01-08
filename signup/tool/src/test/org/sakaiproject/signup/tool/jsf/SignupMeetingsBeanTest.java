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
		_signupMeetings.getAllCategories();

		Assert.assertFalse(_signupMeetings.getAllLocations().isEmpty());
	}

}
