/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
