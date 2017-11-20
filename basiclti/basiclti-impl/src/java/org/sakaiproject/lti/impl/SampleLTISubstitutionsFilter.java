package org.sakaiproject.lti.impl;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.LTISubstitutionsFilter;
import org.sakaiproject.site.api.Site;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * This is an example of the LTI Subsitutions filter. It just add a random number from 0 to 9 to the substitions
 * that are available. It puts this under the key of <code>random</code>. This can then be substituted by a custom
 * property value such as <code>lucky=$random</code>.
 */
public class SampleLTISubstitutionsFilter implements LTISubstitutionsFilter {

    public final static String RANDOM = "random";

    private LTIService ltiService;
    private Random random;

    public void setLtiService(LTIService ltiService) {
        this.ltiService = ltiService;
    }

    public void init() {
        ltiService.registerPropertiesFilter(this);
        random = new SecureRandom();
    }

    public void destroy() {
        ltiService.removePropertiesFilter(this);
    }

    @Override
    public void filterCustomSubstitutions(Properties properties, Map<String, Object> tool, Site site) {
        if (properties.getProperty(RANDOM) == null) {
            properties.setProperty(RANDOM, String.valueOf(random.nextInt(10)));
        }
    }
}
