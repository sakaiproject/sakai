/******************************************************************************
Copyright (c) 2022 Apereo Foundation

Licensed under the Educational Community License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
******************************************************************************/
package org.sakaiproject.webapi.beans;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import lombok.Data;
import lombok.NonNull;

@Data
public class RosterRestBean {

    private String id;
    private String title;
    private RosterType type;

    public static enum RosterType {
        CMPROVIDED, CMREQUESTED, MANREQUESTED
    }

    public RosterRestBean(@NonNull String id, @NonNull RosterType type) {
        Objects.requireNonNull(StringUtils.trimToNull(id));
        this.id = id;
        this.title = id;
        this.type = type;
    }
}
