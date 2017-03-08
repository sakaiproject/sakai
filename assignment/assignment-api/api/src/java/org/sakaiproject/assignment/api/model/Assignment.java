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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Assignment represents a specific assignment for a specific section or class.
 */
@Entity
@Table(name = "ASN_ASSIGNMENT")

@Data
@NoArgsConstructor
public class Assignment {

    @Id
    @Column(name = "ASSIGNMENT_ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

    @Column(name = "TITLE")
	private String title;

    @Lob
    @Column(name = "INSTRUCTIONS")
    private String instructions;

    @Column(name = "CONTEXT")
	private String context;

    @Column(name = "SECTION")
	private String section;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "MODIFIED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModified;

    @Column(name = "VISIBLE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date visibleDate;

    @Column(name = "OPEN_DATE")
    @Temporal(TemporalType.TIMESTAMP)
	private Date openDate;

    @Column(name = "DUE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
	private Date dueDate;

    @Column(name = "CLOSE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
	private Date closeDate;

    @Column(name = "DROP_DEAD_DATE")
    @Temporal(TemporalType.TIMESTAMP)
	private Date dropDeadDate;

    @ElementCollection
    @CollectionTable(name = "ASN_ASSIGNMENT_AUTHORS", joinColumns = @JoinColumn(name = "ASSIGNMENT_ID"))
    @Column(name = "AUTHOR")
	private Set<String> authors = new HashSet<>();

    @Column(name = "DRAFT")
	private Boolean draft;

    @Column(name = "HIDE_DUE_DATE")
	private Boolean hideDueDate;

    @Column(name = "IS_GROUP")
	private Boolean isGroup;

    @Column(name = "POSITION")
	private Integer position;

    @OneToMany(mappedBy = "assignment")
    private Set<AssignmentSubmission> submissions = new HashSet<>();

    @ElementCollection
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUE")
    @CollectionTable(name = "ASN_ASSIGNMENT_PROPERTIES", joinColumns = @JoinColumn(name = "ASSIGNMENT_ID"))
	private Map<String, String> properties = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "ASN_ASSIGNMENT_GROUPS", joinColumns = @JoinColumn(name = "ASSIGNMENT_ID"))
    @Column(name = "GROUP")
	private Set<String> groups = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "ASN_ASSIGNMENT_ATTACHMENTS", joinColumns = @JoinColumn(name = "ASSIGNMENT_ID"))
    @Column(name = "ATTACHMENT")
    private Set<String> attachments = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    @Column(name = "ACCESS")
	private Access access = Access.SITE;

    @Column(name = "HONOR_PLEDGE")
    private Boolean honorPledge;

    @Enumerated
    @Column(name = "SUBMISSION_TYPE")
    private SubmissionType typeOfSubmission;

    @Enumerated
    @Column(name = "GRADE_TYPE")
    private GradeType typeOfGrade;

    @Column(name = "MAX_GRADE_POINT")
    private Integer maxGradePoint;

    @Column(name = "SCALE_FACTOR")
    private Integer scaleFactor;

    @Column(name = "GROUP_PROJECT")
    private Boolean groupProject;

    @Column(name = "INDIVIDUALLY_GRADED")
    private Boolean individuallyGraded;

    @Column(name = "RELEASE_GRADES")
    private Boolean releaseGrades;

    @Column(name = "ALLOW_ATTACHMENTS")
    private Boolean allowAttachments;

    @Column(name = "ALLOW_PEER_ASSESSMENT")
	private Boolean allowPeerAssessment;

    @Column(name = "PEER_ASSESSMENT_PERIOD_DATE")
    @Temporal(TemporalType.TIMESTAMP)
	private Date peerAssessmentPeriodDate;

    @Column(name = "PEER_ASSESSMENT_ANON_EVAL")
	private Boolean peerAssessmentAnonEval;

    @Column(name = "PEER_ASSESSMENT_STUDENT_VIEW_REVIEW")
	private Boolean peerAssessmentStudentViewReview;

    @Column(name = "PEER_ASSESSMENT_NUMBER_REVIEW")
	private Integer peerAssessmentNumberReviews;

    @Column(name = "PEER_ASSESSMENT_INSTRUCTIONS")
	private String peerAssessmentInstructions;

    public enum Access {
        SITE,
        GROUPED
    }

    public enum SubmissionType {
        ASSIGNMENT_SUBMISSION_TYPE_NONE,           // 0
        TEXT_ONLY_ASSIGNMENT_SUBMISSION,           // 1
        ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION,     // 2
        TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION, // 3
        NON_ELECTRONIC_ASSIGNMENT_SUBMISSION,      // 4
        SINGLE_ATTACHMENT_SUBMISSION               // 5
    }

    public enum GradeType {
        GRADE_TYPE_NONE,      // 0
        UNGRADED_GRADE_TYPE,  // 1
        LETTER_GRADE_TYPE,    // 2
        SCORE_GRADE_TYPE,     // 3
        PASS_FAIL_GRADE_TYPE, // 4
        CHECK_GRADE_TYPE      // 5
    }

    // CONTENT REVIEW use separate table?
//    private Boolean allowReview;
//    private Boolean allowStudentViewReport;
//    private String submitReviewRepo;
//    private String generateOriginalityReport;
//    private boolean checkTurnitin = true;
//    private boolean checkInternet = true;
//    private boolean checkPublications = true;
//    private boolean checkInstitution = true;
//    private boolean excludeBibliographic = true;
//    private boolean excludeQuoted = true;
//    private boolean excludeSelfPlag = true;
//    private boolean storeInstIndex = true;
//    private int excludeType = 0;
//    private int excludeValue = 1;
}

