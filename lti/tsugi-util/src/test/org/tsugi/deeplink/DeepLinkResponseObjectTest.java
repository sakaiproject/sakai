package org.tsugi.deeplink;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.tsugi.jackson.JacksonUtil;
import org.tsugi.deeplink.objects.*;
import org.tsugi.shared.objects.*;

public class DeepLinkResponseObjectTest {

	private String sampleResponse;
	private String sampleLTILinkItem;
	private ObjectMapper mapper;
	private DeepLinkResponse deepLinkResponse;
	private LtiResourceLink ltiResourceLink;

	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
		loadTestResources();
		setupTestObjects();
	}

	private void loadTestResources() throws IOException {
		try (InputStream responseStream = getClass().getClassLoader().getResourceAsStream("deeplink/sample_response.json");
			 InputStream linkStream = getClass().getClassLoader().getResourceAsStream("deeplink/sample_ltiresourcelink.json")) {
			
			if (responseStream == null || linkStream == null) {
				throw new IOException("Required test resources not found");
			}
			
			sampleResponse = IOUtils.toString(responseStream, "UTF-8");
			sampleLTILinkItem = IOUtils.toString(linkStream, "UTF-8");
		}
	}

	private void setupTestObjects() {
		deepLinkResponse = new DeepLinkResponse();
		ltiResourceLink = new LtiResourceLink();
	}

	@Test
	public void testSampleResponseParsing() throws JsonProcessingException {
		assertNotNull("Sample response should not be null", sampleResponse);

		DeepLinkResponse dlr = mapper.readValue(sampleResponse, DeepLinkResponse.class);
		
		// Test basic properties
		assertNotNull("Parsed response should not be null", dlr);
		assertEquals("Audience should match", "https://platform.example.org", dlr.audience);
		assertEquals("Deployment ID should match", "07940580-b309-415e-a37c-914d387c1150", dlr.deployment_id);
		assertEquals("Message type should match", "LtiDeepLinkingResponse", dlr.message_type);
		assertEquals("Data should match", "csrftoken:c7fbba78-7b75-46e3-9201-11e6d5f36f53", dlr.data);
		
		// Test content items
		assertNotNull("Content items should not be null", dlr.content_items);
		assertEquals("Should have correct number of content items", 7, dlr.content_items.size());
		assertEquals("Fifth item should be ltiResourceLink", "ltiResourceLink", dlr.content_items.get(4).type);
	}

	@Test
	public void testSampleLTILinkItemParsing() throws JsonProcessingException {
		assertNotNull("Sample LTI link item should not be null", sampleLTILinkItem);
		
		LtiResourceLink link = mapper.readValue(sampleLTILinkItem, LtiResourceLink.class);
		assertNotNull("Parsed LTI link should not be null", link);
	}

	@Test
	public void testLtiResourceLinkCreation() throws JsonProcessingException {
		// Test default values
		assertEquals("Default type should be ltiResourceLink", "ltiResourceLink", ltiResourceLink.type);
		
		// Test setting basic properties
		ltiResourceLink.title = "A title";
		ltiResourceLink.url = "https://www.dj4e.com/mod/tdiscuss/";
		
		// Test thumbnail
		SizedUrl thumbnail = new SizedUrl();
		thumbnail.url = "http://www.sakailms.org/thumbnail.png";
		thumbnail.height = 142;
		thumbnail.width = 142;
		ltiResourceLink.thumbnail = thumbnail;
		
		// Test icon
		SizedUrl icon = new SizedUrl();
		icon.url = "http://www.sakailms.org/icon.png";
		icon.height = 42;
		icon.width = 42;
		ltiResourceLink.icon = icon;
		
		// Test serialization
		String output = JacksonUtil.prettyPrint(ltiResourceLink);
		assertNotNull("Serialized output should not be null", output);
		assertFalse("Output should not contain TARGETNAME", output.contains("TARGETNAME"));
		assertTrue("Output should contain thumbnail URL", output.contains("thumbnail.png"));
		assertTrue("Output should contain icon URL", output.contains("icon.png"));
	}

	@Test
	public void testDeepLinkResponseCreation() throws JsonProcessingException {
		// Test automatic field initialization
		assertNotNull("Issued timestamp should be initialized", deepLinkResponse.issued);
		assertNotNull("Expiry timestamp should be initialized", deepLinkResponse.expires);
		assertNotNull("Nonce should be initialized", deepLinkResponse.nonce);
		assertNotNull("Content items list should be initialized", deepLinkResponse.content_items);

		// Test adding content item
		LtiResourceLink link = new LtiResourceLink();
		link.title = "Test Resource";
		link.url = "https://test.com/resource";
		link.setWindowTarget("_blank");
		deepLinkResponse.content_items.add(link);

		// Test serialization
		String output = JacksonUtil.prettyPrint(deepLinkResponse);
		assertTrue("Output should contain nonce", output.contains("nonce"));
		assertTrue("Output should contain content_items claim", 
			output.contains("\"https://purl.imsglobal.org/spec/lti-dl/claim/content_items\""));
		assertTrue("Output should contain window target", output.contains("targetName"));
		assertTrue("Output should contain _blank target", output.contains("_blank"));
	}

	@Test
	public void testContentItemsManipulation() {
		// Test adding multiple items
		List<LtiResourceLink> links = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			LtiResourceLink link = new LtiResourceLink();
			link.title = "Resource " + i;
			link.url = "https://test.com/resource/" + i;
			links.add(link);
			deepLinkResponse.content_items.add(link);
		}

		assertEquals("Should have correct number of items", 3, deepLinkResponse.content_items.size());
		assertEquals("First item should have correct title", "Resource 0", 
			((LtiResourceLink)deepLinkResponse.content_items.get(0)).title);
	}

	@Test(expected = IOException.class)
	public void testInvalidJsonParsing() throws IOException {
		mapper.readValue("{invalid json}", DeepLinkResponse.class);
	}
}
