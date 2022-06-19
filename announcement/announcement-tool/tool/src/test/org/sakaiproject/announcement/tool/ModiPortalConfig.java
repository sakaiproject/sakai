package org.sakaiproject.announcement.tool;

import org.sakaiproject.portal.util.ToolPortalServlet;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration that provides convenience in locating and injecting a ToolPortalServlet (tool portal), so tests can
 * make servlet requests through the full request filter and tool portal dispatch easily. We mention the specific class
 * here, but the entire package is scanned for @Component, @Resource, and @Named annotations.
 */
@Configuration
@ComponentScan(basePackageClasses = ToolPortalServlet.class)
public class ModiPortalConfig {
}
