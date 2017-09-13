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
package org.sakaiproject.scheduler.events.hibernate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Just maps a context ID, component ID to a quartz trigger UUID.
 */
@Entity(name = "context_mapping")
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"componentId", "contextId"})})
@EqualsAndHashCode(exclude = {"uuid"})
@ToString
public class ContextMapping {

    /**
     *This is the ID of the quartz trigger
     */
    @Id
    @Getter @Setter
    private String uuid;

    /**
     * This is the context ID (opaque ID) passed when the job was created.
     */
    @Getter @Setter
    private String contextId;

    /**
     * This is the component ID, we have this as a separate column so that overlapping context IDs (opaque IDs) aren't
     * a problem.
     */
    @Getter @Setter
    private String componentId;

}
