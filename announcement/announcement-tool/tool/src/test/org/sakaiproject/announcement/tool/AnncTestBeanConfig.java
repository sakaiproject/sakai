package org.sakaiproject.announcement.tool;

import org.springframework.context.annotation.ComponentScan;

/**
 * This is here only to set up component scanning... so we can declare test beans with the @Named annotation anywhere in
 * this package, then we can inject them into a given test. It has an intentionally bad name for right now to be very
 * specific and clear, not as a pattern to follow.
 */
@ComponentScan(basePackages = "org.sakaiproject.announcement.tool")
public class AnncTestBeanConfig {
}
