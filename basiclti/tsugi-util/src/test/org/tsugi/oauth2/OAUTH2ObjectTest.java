package org.tsugi.oauth2;

import static org.junit.Assert.*;

import org.junit.Test;

import org.tsugi.jackson.JacksonUtil;
import org.tsugi.oauth2.objects.AccessToken;

public class OAUTH2ObjectTest {

	@Test
	public void testOne() throws com.fasterxml.jackson.core.JsonProcessingException {

		AccessToken at = new AccessToken();
		at.access_token = "42";
		at.expires_in = new Long(3600);
		at.token_type = AccessToken.BEARER;

		String atsp = JacksonUtil.prettyPrint(at);
		boolean good = atsp.contains("3600");
		if (!good) {
			System.out.println("Bad pretty payload: " + atsp);
		}
		assertTrue(good);

		String ats = JacksonUtil.toString(at);
		good = ats.contains("3600");
		if (!good) {
			System.out.println("Bad Payload: " + ats);
		}
		assertTrue(good);

		at.refresh_token = "43";
		atsp = JacksonUtil.prettyPrint(at);
		good = atsp.contains("refresh_token");
		if (!good) {
			System.out.println("Bad refresh_token: " + atsp);
		}
		assertTrue(good);
	}
}
