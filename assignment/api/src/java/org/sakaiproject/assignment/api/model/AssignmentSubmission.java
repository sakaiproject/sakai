/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.assignment.api.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * AssignmentSubmission represents a student submission for an assignment.
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "ASN_SUBMISSION")
@Data
@NoArgsConstructor
@ToString(exclude = {"assignment", "submitters", "attachments", "feedbackAttachments", "properties"})
@EqualsAndHashCode(of = "id")
public class AssignmentSubmission {

    @Id
    @Column(name = "SUBMISSION_ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "ASSIGNMENT_ID")
    private Assignment assignment;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AssignmentSubmissionSubmitter> submitters = new HashSet<>();

    //private List submissionLog;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "SUBMITTED_DATE")
    private Instant dateSubmitted;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "RETURNED_DATE")
    private Instant dateReturned;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "CREATED_DATE")
    private Instant dateCreated;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "MODIFIED_DATE")
    private Instant dateModified;

    @ElementCollection
    @Column(name = "ATTACHMENT")
    @CollectionTable(name = "ASN_SUBMISSION_ATTACHMENTS", joinColumns = @JoinColumn(name = "SUBMISSION_ID"))
    private Set<String> attachments = new HashSet<>();

    // TODO combine attachments and feedbackAttachements into a single table
    @ElementCollection
    @Column(name = "FEEDBACK_ATTACHMENT")
    @CollectionTable(name = "ASN_SUBMISSION_FEEDBACK_ATTACH", joinColumns = @JoinColumn(name = "SUBMISSION_ID"))
    private Set<String> feedbackAttachments = new HashSet<>();

    @Lob
    @Column(name = "TEXT")
    private String submittedText;

    @Lob
    @Column(name = "FEEDBACK_COMMENT")
    private String feedbackComment;

    @Lob
    @Column(name = "FEEDBACK_TEXT")
    private String feedbackText;

    @Column(name = "GRADE")
    private String grade;

    @Column(name = "FACTOR")
    private Integer factor;

    @Column(name = "SUBMITTED")
    private Boolean submitted = Boolean.FALSE;

    @Column(name = "RETURNED")
    private Boolean returned = Boolean.FALSE;

    @Column(name = "GRADED")
    private Boolean graded = Boolean.FALSE;

    @Column(name = "GRADED_BY")
    private String gradedBy;

    @Column(name = "GRADE_RELEASED")
    private Boolean gradeReleased = Boolean.FALSE;

    @Column(name = "HONOR_PLEDGE")
    private Boolean honorPledge = Boolean.FALSE;

    @Column(name = "ANONYMOUS_SUBMISSION_ID")
    private String anonymousSubmissionId;

    @Column(name = "HIDDEN_DUE_DATE")
    private Boolean hiddenDueDate = Boolean.FALSE;

    @Column(name = "USER_SUBMISSION")
    private Boolean userSubmission = Boolean.FALSE;

    @Column(name = "GROUP_ID")
    private String groupId;

    @ElementCollection
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUE")
    @CollectionTable(name = "ASN_SUBMISSION_PROPERTIES", joinColumns = @JoinColumn(name = "SUBMISSION_ID"))
    private Map<String, String> properties = new HashMap<>();

    // TODO this data should come from a ReviewableSubmissionEntity and not be part of the Submission (SOLID)
    // for this data will be stored in the submissions properties
    // private Integer reviewScore;
    // private String reviewReport;
    // private String reviewStatus;
    // private String reviewIconUrl;
    // private String reviewError;
}
