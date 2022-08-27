package org.tsugi.lti13;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.tsugi.jackson.JacksonUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tsugi.deeplink.objects.*;
import org.tsugi.deeplink.objects.DeepLinkResponse;
import org.tsugi.shared.objects.*;

public class DeepLinkResponseObjectTest {

	String sampleResponse = null;
	String sampleLTILinkItem = null;

	@Before
	public void setUp() throws Exception {
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("deeplink/sample_response.json");
		sampleResponse = IOUtils.toString(resourceAsStream, "UTF-8");
		resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("deeplink/sample_ltiresourcelink.json");
		sampleLTILinkItem = IOUtils.toString(resourceAsStream, "UTF-8");
	}

	@Test
	public void testParse() throws JsonProcessingException {
        assertNotNull(sampleResponse);

        ObjectMapper mapper = new ObjectMapper();
        DeepLinkResponse dlr = mapper.readValue(sampleResponse, DeepLinkResponse.class);
        assertNotNull(dlr);
		assertEquals(dlr.audience, "https://platform.example.org");
		assertEquals(dlr.deployment_id, "07940580-b309-415e-a37c-914d387c1150");
		assertEquals(dlr.message_type, "LtiDeepLinkingResponse");
		assertEquals(dlr.data, "csrftoken:c7fbba78-7b75-46e3-9201-11e6d5f36f53");
		assertEquals(dlr.content_items.size(), 7);
		assertEquals(dlr.content_items.get(4).type, "ltiResourceLink");

        assertNotNull(sampleLTILinkItem);
        LtiResourceLink rl = mapper.readValue(sampleLTILinkItem, LtiResourceLink.class);
        assertNotNull(rl);
	}

	@Test
	public void testOne() throws JsonProcessingException {
		LtiResourceLink ltiResourceLink = new LtiResourceLink();
		assertEquals(ltiResourceLink.type, "ltiResourceLink");
		ltiResourceLink.title = "A title";
		ltiResourceLink.url = "https://www.dj4e.com/mod/tdiscuss/";
		SizedUrl thumbnail = new SizedUrl();
		thumbnail.url = "http://www.sakailms.org/thumbnail.png";
		thumbnail.height = 142;
		thumbnail.width = 142;
		ltiResourceLink.thumbnail = thumbnail;
		SizedUrl icon = new SizedUrl();
		icon.url = "http://www.sakailms.org/icon.png";
		icon.height = 42;
		icon.width = 42;
		ltiResourceLink.icon = icon;
		String out = JacksonUtil.prettyPrint(ltiResourceLink);
		assertFalse(out.contains("TARGETNAME"));
	}

	@Test
	public void testResponse() throws JsonProcessingException {
		org.tsugi.deeplink.objects.DeepLinkResponse dlr = new org.tsugi.deeplink.objects.DeepLinkResponse();
		assertNotNull(dlr.issued);
		assertNotNull(dlr.expires);
		assertNotNull(dlr.nonce);

		LtiResourceLink ltiResourceLink = new LtiResourceLink();
		assertEquals(ltiResourceLink.type, "ltiResourceLink");
		ltiResourceLink.title = "A title";
		ltiResourceLink.url = "https://www.dj4e.com/mod/tdiscuss/";
		ltiResourceLink.setWindowTarget("_blank");
		dlr.content_items.add(ltiResourceLink);

		String out = JacksonUtil.prettyPrint(dlr);
		assertTrue(out.contains("nonce"));
		assertTrue(out.contains("\"https://purl.imsglobal.org/spec/lti-dl/claim/content_items\" : [ {"));
		assertTrue(out.contains("targetName"));
		assertTrue(out.contains("window"));
		assertTrue(out.contains("_blank"));
// System.out.println("out\n"+out);
	}
}
