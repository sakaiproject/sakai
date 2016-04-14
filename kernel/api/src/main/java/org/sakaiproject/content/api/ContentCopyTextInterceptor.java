package org.sakaiproject.content.api;

import java.util.Map;

/**
 * Text interceptor to run when a site is duplicated
 *
 * @author Colin Hebert
 */
public interface ContentCopyTextInterceptor {
    String runTextInterceptor(String interceptedText, Map<String, String> replacements);
}
