package org.tsugi.lti13;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.tsugi.lti13.LTI13Constants;
import org.tsugi.lti13.LTI13JwtUtil;
import org.tsugi.lti13.LTI13JacksonUtil;

import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.Context;
import org.tsugi.lti13.objects.ToolPlatform;
import org.tsugi.lti13.objects.LaunchLIS;
import org.tsugi.lti13.objects.BasicOutcome;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class LTI13ObjectTest {

	@Before
	public void setUp() throws Exception {
	}

	// http://javadox.com/io.jsonwebtoken/jjwt/0.4/io/jsonwebtoken/package-summary.html
	@Test
	public void testOne() throws com.fasterxml.jackson.core.JsonProcessingException {

		LaunchJWT lj = new LaunchJWT();
		lj.launch_presentation.width = 42;
		lj.issuer = "https://www.sakaiproject.org/";
		lj.subject = "Bob";
		lj.name = "Fred";
		lj.email = "Zippy@zippy.com";
		lj.issued = new Long(System.currentTimeMillis() / 1000L);
		lj.expires = lj.issued + 600L;
		lj.roles.add(LaunchJWT.ROLE_INSTRUCTOR);
		lj.resource_link.id = "23098439084309809854";
		lj.context.id = "83934984398";
		lj.context.type.add(Context.COURSE_OFFERING);
		lj.tool_platform.name = "Sakai";
		lj.tool_platform.url = "https://www.sakaiproject.org";
		LaunchLIS lis = new LaunchLIS();
		lis.person_sourcedid = "person:12345:chuck";
		lj.lis = lis;

		BasicOutcome outcome = new BasicOutcome();
		outcome.lis_result_sourcedid = "58489";
		outcome.lis_outcome_service_url = "http://call.me.back";
		lj.basic_outcome = outcome;

		String ljs = LTI13JacksonUtil.toString(lj);
System.out.println("jls="+ljs);
		String ljsp = LTI13JacksonUtil.toStringPretty(lj);
System.out.println("jlsp="+ljsp);

		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		// http://javadox.com/io.jsonwebtoken/jjwt/0.4/io/jsonwebtoken/JwtBuilder.html#setPayload-java.lang.String-
		String jws = Jwts.builder()
			.setPayload(ljs)
                        .signWith(key)
                        .compact();

System.out.println("jws="+jws);
                String body = LTI13JwtUtil.getBodyAsString(jws, key);
System.out.println("body "+body);
                String header = LTI13JwtUtil.getHeaderAsString(jws, key);
System.out.println("header "+header);

		// LaunchPresentation lp = new LaunchPresentation(

	}
}

