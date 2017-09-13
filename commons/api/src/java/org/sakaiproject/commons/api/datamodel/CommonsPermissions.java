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
package org.sakaiproject.commons.api.datamodel;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommonsPermissions {

    private String role;
    private boolean postCreate = false;
    private boolean postReadAny = false;
    private boolean postReadOwn = false;
    private boolean postUpdateAny = false;
    private boolean postUpdateOwn = false;
    private boolean postDeleteAny = false;
    private boolean postDeleteOwn = false;
    private boolean commentCreate = false;
    private boolean commentReadAny = false;
    private boolean commentReadOwn = false;
    private boolean commentUpdateAny = false;
    private boolean commentUpdateOwn = false;
    private boolean commentDeleteAny = false;
    private boolean commentDeleteOwn = false;
}
