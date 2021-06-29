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

package org.sakaiproject.content.api.persistence;

import java.time.Instant;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "FILE_CONVERSION_QUEUE", indexes = {
        @Index(name = "IDX_FCI_STATUS", columnList = "STATUS")
})
public class FileConversionQueueItem implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "REFERENCE", length = 255, nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 16, nullable = false)
    private Status status;

    @Column(name = "ATTEMPTS", nullable = false)
    private Integer attempts;

    @Column(name = "LAST_ATTEMPT_STARTED")
    @Type(type = "org.hibernate.type.InstantType")
    private Instant lastAttemptStarted;

    /**
     * NOT_STARTED = first time around, or a retry
     * FAILED = max attemptes reached, give up.
     * IN_PROGRESS = A worker is in the process of sending this to the converter
     */
    public enum Status {
        NOT_STARTED,
        FAILED,
        IN_PROGRESS;
    }
}
