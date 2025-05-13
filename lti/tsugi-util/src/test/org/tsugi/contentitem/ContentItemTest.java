package org.tsugi.contentitem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.mock.web.MockHttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.lti.ContentItem;
import org.tsugi.contentitem.objects.Icon;
import org.tsugi.contentitem.objects.PlacementAdvice;
import org.tsugi.contentitem.objects.LtiLinkItem;
import org.tsugi.contentitem.objects.ContentItemResponse;

/**
 * Test class for Content Item Message processing.
 * Tests the functionality of Content Item objects according to IMS Content Item Message specification.
 * @see <a href="https://www.imsglobal.org/specs/lticiv1p0/specification">IMS Content-Item Message Specification</a>
 */
@Slf4j
public class ContentItemTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private MockHttpServletRequest request;
    private Icon testIcon;
    private PlacementAdvice testPlacementAdvice;
    private LtiLinkItem testLtiLinkItem;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        
        // Initialize test icon
        testIcon = new Icon("https://www.apereo.org/sites/all/themes/apereo/images/apereo-logo-white-bg.png");
        testIcon.setHeight(64);
        testIcon.setWidth(64);
        
        // Initialize placement advice
        testPlacementAdvice = new PlacementAdvice();
        
        // Initialize LTI link item
        testLtiLinkItem = new LtiLinkItem("sakai.announcements", testPlacementAdvice, testIcon);
        testLtiLinkItem.setTitle("A cool tool hosted in the Sakai environment.");
        testLtiLinkItem.setText("For more information on how to build and host powerful LTI-based Tools quickly, see www.tsugi.org");
        testLtiLinkItem.setUrl("http://www.tsugi.org");
    }

    /**
     * Tests the creation and validation of Content Item objects
     */
    @Test
    public void testBuildObjects() {
        // Test Icon object
        String output = testIcon.prettyPrintLog();
        assertNotNull("Icon pretty print should not be null", output);
        assertTrue("Icon output should contain image path", output.contains("apereo-logo-white"));
        assertTrue("Icon output should contain dimensions", output.contains("64"));

        // Test PlacementAdvice object
        output = testPlacementAdvice.prettyPrintLog();
        assertNotNull("PlacementAdvice pretty print should not be null", output);

        // Test LtiLinkItem object
        output = testLtiLinkItem.prettyPrintLog();
        assertNotNull("LtiLinkItem pretty print should not be null", output);
        assertTrue("LtiLinkItem output should contain title", output.contains("cool"));

        // Test ContentItemResponse object
        ContentItemResponse resp = new ContentItemResponse();
        resp.addGraph(testLtiLinkItem);
        output = resp.prettyPrintLog();
        assertNotNull("ContentItemResponse pretty print should not be null", output);
        assertTrue("Response should contain @graph", output.contains("@graph"));
        assertTrue("Response should contain icon details", output.contains("apereo-logo-white"));
        assertTrue("Response should contain dimensions", output.contains("64"));
        assertTrue("Response should contain title", output.contains("cool"));

        log.debug("ContentItemResponse output: {}", output);
    }

    /**
     * Tests validation when no content items are present
     */
    @Test
    public void testContentItemThrowsNoContentItems() {
        request.addParameter("data", "{\"eggs\": \"chips\"}");
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage(ContentItem.NO_CONTENT_ITEMS);
        new ContentItem(request);
    }

    /**
     * Tests validation of malformed content items
     */
    @Test
    public void testContentItemThrowsBadContentItems() {
        request.addParameter("data", "{\"eggs\": \"chips\"}");
        request.addParameter(ContentItem.CONTENT_ITEMS, "[\"bad\"]");
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage(ContentItem.BAD_CONTENT_MESSAGE);
        new ContentItem(request);
    }

    /**
     * Tests validation when data parameter is missing
     */
    @Test
    public void testContentItemThrowsNoData() {
        request.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage(ContentItem.NO_DATA_MESSAGE);
        new ContentItem(request);
    }

    /**
     * Tests validation of malformed data parameter
     */
    @Test
    public void testContentItemThrowsBadData() {
        request.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
        request.addParameter("data", "[\"eggs\"]");
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage(ContentItem.BAD_DATA_MESSAGE);
        new ContentItem(request);
    }

    /**
     * Tests validation when @graph is missing
     */
    @Test
    public void testContentItemThrowsNoGraph() {
        request.addParameter(ContentItem.CONTENT_ITEMS, "{\"no\": \"graph\"}");
        request.addParameter("data", "{\"eggs\": \"chips\"}");
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage(ContentItem.NO_GRAPH_MESSAGE);
        new ContentItem(request);
    }

    /**
     * Tests successful content item creation and data property access
     */
    @Test
    public void testContentItem() {
        request.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
        request.addParameter("data", "{\"eggs\": \"chips\"}");
        ContentItem contentItem = new ContentItem(request);
        assertEquals("Data property should be accessible", "chips", 
            contentItem.getDataProperties().getProperty("eggs"));
    }

    /**
     * Tests handling of escaped JSON data
     */
    @Test
    public void testContentItemEscapedData() {
        request.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": \"ene\"}");
        request.addParameter("data", "{\\\"eggs\\\": \\\"chips\\\"}");
        ContentItem contentItem = new ContentItem(request);
        assertEquals("Escaped JSON should be properly parsed", "chips", 
            contentItem.getDataProperties().getProperty("eggs"));
    }

    /**
     * Tests parsing of graph content and type validation
     */
    @Test
    public void testContentItemGraphParsing() {
        request.addParameter(ContentItem.CONTENT_ITEMS, 
            "{\"@graph\": [{\"@type\": \"" + ContentItem.TYPE_LTILINKITEM + "\"}]}");
        request.addParameter("data", "{\"eggs\": \"chips\"}");
        ContentItem contentItem = new ContentItem(request);
        assertNotNull("Should find item of correct type", 
            contentItem.getItemOfType(ContentItem.TYPE_LTILINKITEM));
    }

    /**
     * Tests handling of empty content items
     */
    @Test
    public void testEmptyContentItems() {
        request.addParameter(ContentItem.CONTENT_ITEMS, "{\"@graph\": []}");
        request.addParameter("data", "{}");
        ContentItem contentItem = new ContentItem(request);
        assertNull("Should return null for non-existent type", 
            contentItem.getItemOfType("non-existent-type"));
    }

    /**
     * Tests content item with multiple graph items
     */
    @Test
    public void testMultipleGraphItems() {
        String multipleItems = "{\"@graph\": [" +
            "{\"@type\": \"" + ContentItem.TYPE_LTILINKITEM + "\", \"title\": \"First Item\"}," +
            "{\"@type\": \"" + ContentItem.TYPE_LTILINKITEM + "\", \"title\": \"Second Item\"}" +
            "]}";
        request.addParameter(ContentItem.CONTENT_ITEMS, multipleItems);
        request.addParameter("data", "{}");
        ContentItem contentItem = new ContentItem(request);
        assertNotNull("Should find first item of type", 
            contentItem.getItemOfType(ContentItem.TYPE_LTILINKITEM));
    }
}

