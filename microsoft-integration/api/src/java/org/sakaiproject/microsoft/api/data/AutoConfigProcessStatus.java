/*
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.api.data;

import lombok.Getter;

@Getter
public enum AutoConfigProcessStatus {

    START_RUNNING(0),
    CREATING_TEAM(1),
    BINDING_TEAM(2),
    END_RUNNING(3);

    private Integer code;

    private AutoConfigProcessStatus(Integer code) {
        this.code = code;
    }

    public static AutoConfigProcessStatus fromCode(Integer code) {
        for (AutoConfigProcessStatus v : AutoConfigProcessStatus.values()) {
            if (v.code == code) {
                return v;
            }
        }
        return null;
    }
}
