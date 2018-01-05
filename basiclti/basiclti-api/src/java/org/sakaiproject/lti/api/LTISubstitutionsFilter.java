package org.sakaiproject.lti.api;

import org.sakaiproject.site.api.Site;

import java.util.Map;
import java.util.Properties;

/**
 * This allows additional LTI custom substitution filtering to happen that is deployment specific.
 */
public interface LTISubstitutionsFilter {

    /**
     * This is called on the custom substitution properties to perform custom filtering.
     * @param properties The custom properties ready to be substituted. The filter should directly change this
     *                   object to pass changes back to the caller.
     * @param tool The LTI tool.
     * @param site The site in which the launch is happening.
     */
    void filterCustomSubstitutions(Properties properties, Map<String, Object> tool, Site site);

}
