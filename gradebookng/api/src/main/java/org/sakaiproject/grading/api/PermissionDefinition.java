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
package org.sakaiproject.grading.api;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *  DTO for the {@link org.sakaiproject.gradebook.tool.Permission} to pass to external services. Not persisted.
 */
@EqualsAndHashCode
@Getter @Setter
public class PermissionDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Exclude
    private Long id;
    private String userId;
    private String functionName;
    private Long categoryId;
    private String groupReference;
}
