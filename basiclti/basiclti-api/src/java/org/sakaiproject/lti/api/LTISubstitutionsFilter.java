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
