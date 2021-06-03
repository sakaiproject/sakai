/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tasks.api;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "TASKS", indexes = {
        @Index(name = "IDX_TASKS_REF_TYPE", columnList = "REFERENCE")
})
public class Task implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "task_id_sequence")
    @SequenceGenerator(name = "task_id_sequence", sequenceName = "TASKS_S")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "SITE_ID", length = 99)
    private String siteId;

    @Column(name = "REFERENCE", length = 255, nullable = false)
    private String reference;

    @Column(name = "DESCRIPTION", length = 255, nullable = false)
    private String description;

    @Column(name = "SYSTEM", nullable = false)
    private Boolean system;

    @Column(name = "STARTS")
    @Type(type = "org.hibernate.type.InstantType")
    private Instant starts;

    @Column(name = "DUE")
    @Type(type = "org.hibernate.type.InstantType")
    private Instant due;
}
