package org.sakaiproject.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;

/**
 * Test that the Resource class loads things correctly a second time.
 * It doesn't test the cross classloader functionality of Resource.
 * @author buckett
 *
 */
public class ResourceTest {

	@Before
	public void setUp() throws Exception {
		ComponentManager.testingMode = true;
		ComponentManager.loadComponent(ContentHostingService.class, new Object());

	}

	@Test
	public void test() {
		// KNL-888 Check that multiple calls to getLoader() return the correct loader.
		Resource resource = new Resource();
		ResourceLoader loader1 = resource.getLoader(ContentHostingService.class.getName(), "bundle1");
		ResourceLoader loader2 = resource.getLoader(ContentHostingService.class.getName(), "bundle2");
		assertEquals("Bundle 1", loader1.getString("example"));
		assertEquals("Bundle 2", loader2.getString("example"));
	}

}
