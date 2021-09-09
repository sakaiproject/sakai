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
package org.sakaiproject.assignment.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

/**
 * Defines a relation between a submission and the submission's submitters.
 * <br/> - A submitter can have its own grade separate from the grade of the submission,
 * useful in providing user with different grades in group submissions.
 * <br/> - A submitter can have its own feedback separate from the feedback of the submission,
 * useful when different feedback is needed in group submissions
 * <p>
 * <b>Constraints</b>
 * <br/>- submission and submitter are unique,
 * meaning a user can't be a submitter more than once on a submission.
 * Notice that equals and hashcode also reflect this relationship.
 */
@Entity
@Table(name = "ASN_TIMESHEET")
@Data
@NoArgsConstructor
@ToString(exclude = {"submitter"})
@EqualsAndHashCode(of = "id")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AssignmentTimeSheet {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_id_asn_timesheet")
    @SequenceGenerator(name = "seq_id_asn_timesheet", sequenceName = "SEQ_ID_ASN_TIMESHEET_S")
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBMITTER_ID", nullable = false)
    @JsonBackReference
    private AssignmentSubmissionSubmitter submitter;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "REG_DATE", nullable = false)
    private Instant regDate;

    @Column(name = "REG_TIME", length = 255)
    private String duration;

    @Column(name = "ASN_COMMENT", length = 255)
    private String asnComment;
}
