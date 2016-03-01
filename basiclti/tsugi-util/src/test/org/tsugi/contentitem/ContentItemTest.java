package org.tsugi.contentitem;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import org.tsugi.jackson.JacksonUtil;

import org.tsugi.contentitem.objects.Icon;
import org.tsugi.contentitem.objects.PlacementAdvice;
import org.tsugi.contentitem.objects.LtiLinkItem;
import org.tsugi.contentitem.objects.ContentItemResponse;

public class ContentItemTest {

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

System.out.println("output="+output);
		
	}

}

