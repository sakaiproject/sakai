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
package org.sakaiproject.authz.api;

import lombok.Builder;
import org.sakaiproject.entity.api.Entity;

public class AuthzGroupReferenceBuilder {

    @Builder
    public static String buildReference(String site, String group) {

        String ref = "";
        if (site != null) {
            ref += Entity.SEPARATOR + "site" + Entity.SEPARATOR + site;
        }

        if (group != null) {
            ref += Entity.SEPARATOR + "group" + Entity.SEPARATOR + group;
        }

        return ref;
    }
}
