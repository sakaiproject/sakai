package org.tsugi.oauth2;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.oauth2.objects.ClientAssertion;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import org.tsugi.jackson.JacksonUtil;

// https://www.imsglobal.org/spec/security/v1p0/

public class OAUTH2ObjectTest {

    String sampleToken = null;

    @Before
    public void setUp() throws Exception {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("oauth2/sample_access_token.json");
        sampleToken = IOUtils.toString(resourceAsStream, "UTF-8");
	}

	@Test
	public void testOne() throws com.fasterxml.jackson.core.JsonProcessingException {

		AccessToken at = new AccessToken();
		at.access_token = "42";
		at.expires_in = new Long(3600);
		at.token_type = AccessToken.TOKEN_TYPE_BEARER;
		at.scope = "yada scope";

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

	@Test
	public void testTwo() throws com.fasterxml.jackson.core.JsonProcessingException {
		assertNotNull(sampleToken);
        ObjectMapper mapper = new ObjectMapper();
        AccessToken accessToken = mapper.readValue(sampleToken, AccessToken.class);
        assertNotNull(accessToken);
	}

	@Test
	public void testThree() throws JsonProcessingException {
		ClientAssertion ca = new ClientAssertion();
		ca.issuer = "testissuer";
		ca.audience = "why-viktor-why";
		ca.deployment_id = "thanks-eric-nice-feature";
	}
}

/*
{
    "access_token" : "dkj4985kjaIAJDJ89kl8rkn5",
    "token_type" : "bearer",
    "expires_in" : 3600,
    "scope" : "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem https://purl.imsglobal.org/spec/lti-ags/scope/result/read"
}
*/
