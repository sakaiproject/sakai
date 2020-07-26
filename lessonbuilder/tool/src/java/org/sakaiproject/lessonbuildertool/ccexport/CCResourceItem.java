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

package org.sakaiproject.lessonbuildertool.ccexport;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class CCResourceItem {

    private String sakaiId;
    private String resourceId;
    private String location;
    private String use;
    private String title;
    private String url;
    private boolean link = false;
    private boolean bank = false;
    private Set<String> dependencies = new HashSet<>();

    /**
     * Common Cartridge Resource
     *
     * @param sakaiId
     * @param resourceId
     * @param location
     * @param use
     * @param title
     * @param url
     */
    public CCResourceItem(String sakaiId, String resourceId, String location, String use, String title, String url) {
        this.sakaiId = sakaiId;
        this.resourceId = resourceId;
        this.location = location;
        this.use = use;
        this. title = title;
        this.url = url;
    }
}
