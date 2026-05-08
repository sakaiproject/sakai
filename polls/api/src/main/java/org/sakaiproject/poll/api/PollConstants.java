/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.poll.api;

import org.sakaiproject.entity.api.Entity;

public class PollConstants {

    public static final String APPLICATION_ID = "sakai:poll";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "poll";

    public static final String PERMISSION_PREFIX = "poll";
    public static final String PERMISSION_VOTE = "poll.vote";
    public static final String PERMISSION_ADD = "poll.add";
    public static final String PERMISSION_DELETE_OWN = "poll.deleteOwn";
    public static final String PERMISSION_DELETE_ANY = "poll.deleteAny";
    public static final String PERMISSION_EDIT_ANY = "poll.editAny";
    public static final String PERMISSION_EDIT_OWN = "poll.editOwn";

    public static final String REF_POLL_TYPE = "poll";

    private PollConstants() {
        throw new IllegalStateException("do not instantiate this class");
    }
}
