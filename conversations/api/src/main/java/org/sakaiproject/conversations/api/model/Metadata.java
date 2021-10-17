/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Metadata {

    @Column(name = "CREATOR", length = 99)
    private String creator;

    @Column(name = "CREATED", nullable = false)
    private Instant created;

    @Column(name = "MODIFIER", length = 99)
    private String modifier;

    @Column(name = "MODIFIED")
    private Instant modified;
}
