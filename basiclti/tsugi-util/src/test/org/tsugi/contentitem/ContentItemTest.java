package org.tsugi.contentitem;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.mock.web.MockHttpServletRequest;
import org.tsugi.basiclti.ContentItem;
import org.tsugi.jackson.JacksonUtil;

import org.tsugi.contentitem.objects.Icon;
import org.tsugi.contentitem.objects.PlacementAdvice;
import org.tsugi.contentitem.objects.LtiLinkItem;
import org.tsugi.contentitem.objects.ContentItemResponse;

@Slf4j
public class ContentItemTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testBuildObjects() {
		Icon icon = new Icon("https://www.apereo.org/sites/all/themes/apereo/images/apereo-logo-white-bg.png");
		icon.setHeight(64);
		icon.setWidth(64);

		String output = icon.prettyPrintLog();
		assertNotNull(output);

		assertTrue(output.contains("apereo-logo-white"));
		assertTrue(output.contains("64"));

		PlacementAdvice placementAdvice = new PlacementAdvice();
		output = placementAdvice.prettyPrintLog();
		assertNotNull(output);

		LtiLinkItem item = new LtiLinkItem("sakai.announcements", placementAdvice, icon);
		item.setTitle("A cool tool hosted in the Sakai environment.");
		item.setText("For more information on how to build and host powerful LTI-based Tools quickly, see www.tsugi.org");
		item.setUrl("http://www.tsugi.org");
		output = item.prettyPrintLog();
		assertNotNull(output);
		assertTrue(output.contains("cool"));

		ContentItemResponse resp = new ContentItemResponse();
		resp.addGraph(item);
		output = resp.prettyPrintLog();
		assertNotNull(output);
		assertTrue(output.contains("@graph"));
		assertTrue(output.contains("apereo-logo-white"));
		assertTrue(output.contains("64"));
		assertTrue(output.contains("cool"));

		log.debug("output={}", output);		
	}

	@Test
	public void testContentItemThrowsNoContentItems() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("data", "{\"eggs\": \"chips\"}");
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage(ContentItem.NO_CONTENT_ITEMS);
		ContentItem contentItem = new ContentItem(req);
	}

	@Test
	public void testContentItemThrowsBadContentItems() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("data", "{\"eggs\": \"chips\"}");
		req.addParameter(ContentItem.CONTENT_ITEMS, "[\"bad\"]");
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage(ContentItem.BAD_CONTENT_MESSAGE);
		ContentItem contentItem = new ContentItem(req);
	}

	@Test
	public void testContentItemThrowsNoData() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage(ContentItem.NO_DATA_MESSAGE);
		ContentItem contentItem = new ContentItem(req);
	}

	@Test
	public void testContentItemThrowsBadData() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
		req.addParameter("data", "[\"eggs\"]");
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage(ContentItem.BAD_DATA_MESSAGE);
		ContentItem contentItem = new ContentItem(req);
	}

	@Test
	public void testContentItemThrowsNoGraph() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(ContentItem.CONTENT_ITEMS, "{\"no\": \"graph\"}");
		req.addParameter("data", "{\"eggs\": \"chips\"}");
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage(ContentItem.NO_GRAPH_MESSAGE);
		ContentItem contentItem = new ContentItem(req);
	}

	@Test
	public void testContentItem() {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
		req.addParameter("data", "{\"eggs\": \"chips\"}");
		ContentItem contentItem = new ContentItem(req);
		assertTrue(contentItem.getDataProperties().getProperty("eggs").equals("chips"));
	}

	@Test
	public void testContentItemEscapedData() {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
		req.addParameter("data", "{\\\"eggs\\\": \\\"chips\\\"}");
		ContentItem contentItem = new ContentItem(req);
		assertTrue(contentItem.getDataProperties().getProperty("eggs").equals("chips"));
	}

	@Test
	public void testContentItemGraphParsing() {

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": [{\"@type\": \"" + ContentItem.TYPE_LTILINKITEM + "\"}]}");
		req.addParameter("data", "{\"eggs\": \"chips\"}");
		ContentItem contentItem = new ContentItem(req);
		assertTrue(contentItem.getItemOfType(ContentItem.TYPE_LTILINKITEM) != null);
	}
}

