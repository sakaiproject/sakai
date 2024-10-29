package org.tsugi.lti13;

import java.util.Map;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Base64;

import org.tsugi.oauth2.objects.ClientAssertion;
import org.tsugi.lti13.LTI13ConstantsUtil;

import io.jsonwebtoken.Jwts;

public class LTI13AccessTokenUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testOne() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPair keyPair = LTI13Util.generateKeyPair();
		String clientId = "client-was-here";
		String deploymentId = "deployment-id-42";
		String tokenAudience = null;
		StringBuffer dbs = new StringBuffer();
		Map retval = LTI13AccessTokenUtil.getClientAssertion(
				new String[] {
					LTI13ConstantsUtil.SCOPE_RESULT_READONLY,
					LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY,
					LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES
				},
			keyPair, clientId, deploymentId, tokenAudience, dbs);
		assertNotNull(retval);
		assertEquals(retval.get(ClientAssertion.GRANT_TYPE), ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS);
		assertEquals(retval.get(ClientAssertion.CLIENT_ASSERTION_TYPE), ClientAssertion.CLIENT_ASSERTION_TYPE_JWT);
		assertNotNull(retval.get(ClientAssertion.CLIENT_ASSERTION));

		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"));
		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly"));
		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"));

		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/score"));

		String debugStr = dbs.toString();
		assertTrue(debugStr.contains("kid="));
	}

	@Test
	public void testTwo() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPair keyPair = LTI13Util.generateKeyPair();
		String clientId = "client-was-here";
		String deploymentId = "deployment-id-42";
		String tokenAudience = null;
		StringBuffer dbs = new StringBuffer();

		Map retval = LTI13AccessTokenUtil.getScoreAssertion(keyPair, clientId, deploymentId, tokenAudience, dbs);
		assertNotNull(retval);
		assertEquals(retval.get(ClientAssertion.GRANT_TYPE), ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS);
		assertEquals(retval.get(ClientAssertion.CLIENT_ASSERTION_TYPE), ClientAssertion.CLIENT_ASSERTION_TYPE_JWT);
		assertNotNull(retval.get(ClientAssertion.CLIENT_ASSERTION));
		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"));
		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/score"));
		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"));

		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly"));
		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"));

		String debugStr = dbs.toString();
		assertTrue(debugStr.contains("kid="));
		assertTrue(debugStr.contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"));

		dbs = new StringBuffer();
		retval = LTI13AccessTokenUtil.getNRPSAssertion(keyPair, clientId, deploymentId, tokenAudience, dbs);
		assertNotNull(retval);
		assertEquals(retval.get(ClientAssertion.GRANT_TYPE), ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS);
		assertEquals(retval.get(ClientAssertion.CLIENT_ASSERTION_TYPE), ClientAssertion.CLIENT_ASSERTION_TYPE_JWT);
		assertNotNull(retval.get(ClientAssertion.CLIENT_ASSERTION));
		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"));

		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"));
		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/score"));
		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"));
		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly"));

		debugStr = dbs.toString();
		assertTrue(debugStr.contains("kid="));
		assertTrue(debugStr.contains("https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"));

		dbs = new StringBuffer();
		retval = LTI13AccessTokenUtil.getLineItemsAssertion(keyPair, clientId, deploymentId, tokenAudience, dbs);
		assertNotNull(retval);
		assertEquals(retval.get(ClientAssertion.GRANT_TYPE), ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS);
		assertEquals(retval.get(ClientAssertion.CLIENT_ASSERTION_TYPE), ClientAssertion.CLIENT_ASSERTION_TYPE_JWT);
		assertNotNull(retval.get(ClientAssertion.CLIENT_ASSERTION));
		assertTrue(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"));

		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"));
		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/score"));
		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly"));
		assertFalse(((String)retval.get(ClientAssertion.SCOPE)).contains("https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"));

		debugStr = dbs.toString();
		assertTrue(debugStr.contains("kid="));
		assertTrue(debugStr.contains("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"));
	}

}
