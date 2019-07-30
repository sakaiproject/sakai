package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Test;

import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.ResourceLink;
import org.tsugi.lti13.objects.Context;
import org.tsugi.lti13.objects.ToolPlatform;
import org.tsugi.lti13.objects.LaunchLIS;
import org.tsugi.lti13.objects.BasicOutcome;
import org.tsugi.lti13.objects.Endpoint;
import org.tsugi.lti13.objects.LTI11Transition;

import org.tsugi.jackson.JacksonUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class LTI13ObjectTest {

	Pattern base64url_pattern = Pattern.compile("^[A-Za-z0-9.\\-_]+$");

	// http://javadox.com/io.jsonwebtoken/jjwt/0.4/io/jsonwebtoken/package-summary.html
	@Test
	public void testOne() throws com.fasterxml.jackson.core.JsonProcessingException {

		LaunchJWT lj = new LaunchJWT();
		lj.launch_presentation.width = 42;
		lj.issuer = "https://www.sakailms.org/";
		lj.audience = "42_34989754987548";  // Client Id
		lj.deployment_id = "42_this_field_sucks";  // Client Id
		lj.subject = "142";  // formerly user_id in LTI 1.1
		lj.name = "Fred";
		lj.email = "Zippy@zippy.com";
		lj.issued = System.currentTimeMillis() / 1000L;
		lj.expires = lj.issued + 600L;
		lj.roles.add(LaunchJWT.ROLE_INSTRUCTOR);

		lj.resource_link = new ResourceLink();
		lj.resource_link.id = "23098439084309809854";

		lj.context = new Context();
		lj.context.id = "83934984398";
		lj.context.type.add(Context.COURSE_OFFERING);

		lj.tool_platform = new ToolPlatform();
		lj.tool_platform.name = "Sakai";
		lj.tool_platform.url = "https://www.sakailms.org/";

		lj.lti11_transition = new LTI11Transition();
		lj.lti11_transition.user_id = "142";
		lj.lti11_transition.oauth_consumer_key = "12345";
		// Actual signature check is done in LTI13UtilTest.java
		lj.lti11_transition.oauth_consumer_key_sign = "computeme";

		LaunchLIS lis = new LaunchLIS();
		lis.person_sourcedid = "person:12345:chuck";
		lj.lis = lis;

		Endpoint ep = new Endpoint();
		ep.lineitem = "https://www.tsugicloud.org/lineitems/1234/999/";
		lj.endpoint = ep;

		BasicOutcome outcome = new BasicOutcome();
		outcome.lis_result_sourcedid = "58489";
		outcome.lis_outcome_service_url = "http://call.me.back";
		lj.basicoutcome = outcome;

		Properties ltiProps = new Properties();
		ltiProps.setProperty("normal_x","42");
		ltiProps.setProperty("custom_x","42");
		ltiProps.setProperty("custom_y","142");

		lj.custom = new TreeMap<String, String>();
		for (Map.Entry<Object, Object> entry : ltiProps.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
			String custom_key = (String) entry.getKey();
			String custom_val = (String) entry.getValue();
			if (!custom_key.startsWith("custom_")) {
				continue;
			}
			custom_key = custom_key.substring(7);
			lj.custom.put(custom_key, custom_val);
		}

		String ljs = JacksonUtil.toString(lj);
		boolean good = ljs.contains("call.me.back");
		if (!good) {
			System.out.println("Bad Payload: " + ljs);
		}
		assertTrue(good);

		String ljsp = JacksonUtil.prettyPrint(lj);
		good = ljsp.contains("call.me.back");
		if (!good) {
			System.out.println("Bad pretty payload: " + ljsp);
		}
		assertTrue(good);

		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		// http://javadox.com/io.jsonwebtoken/jjwt/0.4/io/jsonwebtoken/JwtBuilder.html
		String jws = Jwts.builder()
				.setHeaderParam("kid", "42_42_42")
				.setPayload(ljs)
				.signWith(key)
				.compact();

		assertEquals(2174, jws.length());
		Matcher m = base64url_pattern.matcher(jws);
		good = m.find();
		if (!good) {
			System.out.println("Bad JWS:\n" + jws);
		}
		assertTrue(good);

		String body = LTI13JwtUtil.getBodyAsString(jws, key);
		good = body.contains("call.me.back");
		if (!good) {
			System.out.println("Bad body: " + body);
		}
		assertTrue(good);

		String header = LTI13JwtUtil.getHeaderAsString(jws, key);
		good = header.contains("42_42_42");
		if (!good) {
			System.out.println("Bad header: " + header);
		}
		assertTrue(good);
	}

}
