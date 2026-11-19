/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool;

// Plain, cacheable stand-in for a ContentResource. ContentResource's concrete runtime
// type (BaseContentService.BaseResourceEdit) is a non-static inner class carrying an
// implicit reference to the enclosing BaseContentService, so it can never be cached
// directly - only these three fields are ever read off a cached CSS resource.
public class CssResource {
    public String id;
    public String url;
    public String displayName;

    public CssResource(String id, String url, String displayName) {
        this.id = id;
        this.url = url;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }
}
