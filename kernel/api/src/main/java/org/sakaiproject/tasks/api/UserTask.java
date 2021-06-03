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

import javax.persistence.*;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "USER_TASKS", indexes = {
    @Index(name = "IDX_USER_TASK", columnList = "USER_ID, TASK_ID")
})
public class UserTask implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_task_id_sequence")
    @SequenceGenerator(name = "user_task_id_sequence", sequenceName = "USER_TASKS_S")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "TASK_ID", nullable = false)
    private Task task;

    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    @Lob
    @Column(name = "NOTES")
    private String notes;

    @Column(name = "COMPLETE")
    private Boolean complete;

    @Column(name = "SOFT_DELETED")
    private Boolean softDeleted;
}
