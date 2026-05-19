/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.order.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolOrderPage {

    private String id;
    private String title;
    private String toolId;
    private String toolIconClass;
    private String webContentUrl;
    private boolean visible;
    private boolean enabled;
    private boolean hidden;
    private boolean locked;
    private boolean allowsHide;
    private boolean allowsLock;
    private boolean allowsEdit;
    private boolean deletable;
    private boolean webContent;
    private boolean first;
    private boolean last;
}
