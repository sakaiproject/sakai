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

import java.util.*;
import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

/**
 * Assignment represents a specific assignment for a specific section or class.
 * <p>
 * <p>Important notes about Java java.util.Date timezone persistence</p>
 * <pre>
 * * MySQL DATETIME vs TIMESTAMP
 *   - DATETIME is a date and time and has a much wider range than TIMESTAMP. MySQL's date operations
 *     can be used to perform date calculations. Probably the most notable trait is that the value that
 *     is stored will be the same when retrieved regardless of timezone info. This shifts the
 *     responsibility to the application to handling timezone info. Changing the server/jvm timezone
 *     would result in different times if not handled.
 *     It's best canonicalized with DATETIME is not affected by timezones.
 *   - TIMESTAMP is a specific point in time (uses 4 bytes making it more efficient in mysql).
 *     Values are converted from the current timezone to UTC for storage and then converted back
 *     from UTC to the current timezone. The current timezone is usually the mysql servers timezone.
 *     Changing the server/jvm timezone wouldn't matter.
 *   - Connector/J useLegacyDatetimeCode is used for backwards compatibility, handling timezone info
 *     the way it always has. Newer versions will probably change this to false. The general rule of thumb
 *     is to use whatever your database was running with (i.e. true), and new databases with new data to use false.
 *   - Connector/J 5.1.x useLegacyDatetimeCode=true and > 5.1 is false
 *   - MariaDB Connector/J 1.1.7+ useLegacyDatetimeCode=true
 * * Hibernate
 *   - JPA @Temporal creates a TIMESTAMP type in MYSQL by default
 *   - java 8 time is supported natively in Hibernate 5
 *     - where @Temporal is implicit and therefore not needed
 *     - LocalDate implicitly @Temporal(TemporalType.Date)
 *     - LocalTime implicitly @Temporal(TemporalType.Time)
 *     - LocalDateTime implicitly @Temporal(TemporalType.Timestamp)
 *   - java 8 time is not supported in Hibernate < 5
 *     - So we use java.util.Date and specify the TemporalType to use
 *     - @Temporal(TemporalType.Date) maps to SQL (JDBC) DATE
 *     - @Temporal(TemporalType.Time) maps to SQL (JDBC) TIME
 *     - @Temporal(TemporalType.Timestamp) maps to SQL (JDBC) TIMESTAMP
 * </pre>
 */

@Entity
@Table(name = "ASN_ASSIGNMENT")
@Data
@NoArgsConstructor
@ToString(exclude = {"authors", "submissions", "groups", "properties", "attachments"})
@EqualsAndHashCode(of = "id")
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

    @Column(name = "CONTEXT", nullable = false)
    private String context;

    @Column(name = "SECTION")
    private String section;

    @Column(name = "CREATED_DATE", nullable = false)
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

    @Column(name = "MODIFIER")
    private String modifier;

    @Column(name = "AUTHOR")
    private String author;

    @Column(name = "DRAFT", nullable = false)
    private Boolean draft = Boolean.FALSE;

    @Column(name = "DELETED")
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "HIDE_DUE_DATE")
    private Boolean hideDueDate = Boolean.FALSE;

    @Column(name = "IS_GROUP")
    private Boolean isGroup = Boolean.FALSE;

    @Column(name = "POSITION")
    private Integer position;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AssignmentSubmission> submissions = new HashSet<>();

    @ElementCollection
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUE")
    @CollectionTable(name = "ASN_ASSIGNMENT_PROPERTIES", joinColumns = @JoinColumn(name = "ASSIGNMENT_ID"))
    private Map<String, String> properties = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "ASN_ASSIGNMENT_GROUPS", joinColumns = @JoinColumn(name = "ASSIGNMENT_ID"))
    @Column(name = "GROUP_ID")
    private Set<String> groups = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "ASN_ASSIGNMENT_ATTACHMENTS", joinColumns = @JoinColumn(name = "ASSIGNMENT_ID"))
    @Column(name = "ATTACHMENT")
    private Set<String> attachments = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    @Column(name = "ACCESS", nullable = false)
    private Access access = Access.SITE;

    @Column(name = "HONOR_PLEDGE")
    private Boolean honorPledge = Boolean.FALSE;

    @Enumerated
    @Column(name = "SUBMISSION_TYPE")
    private SubmissionType typeOfSubmission = SubmissionType.ASSIGNMENT_SUBMISSION_TYPE_NONE;

    @Enumerated
    @Column(name = "GRADE_TYPE")
    private GradeType typeOfGrade = GradeType.GRADE_TYPE_NONE;

    @Column(name = "MAX_GRADE_POINT")
    private Integer maxGradePoint;

    @Column(name = "SCALE_FACTOR")
    private Integer scaleFactor;

    @Column(name = "INDIVIDUALLY_GRADED")
    private Boolean individuallyGraded;

    @Column(name = "RELEASE_GRADES")
    private Boolean releaseGrades = Boolean.FALSE;

    @Column(name = "ALLOW_ATTACHMENTS")
    private Boolean allowAttachments;

    @Column(name = "ALLOW_PEER_ASSESSMENT")
    private Boolean allowPeerAssessment = Boolean.FALSE;

    @Column(name = "PEER_ASSESSMENT_PERIOD_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date peerAssessmentPeriodDate;

    @Column(name = "PEER_ASSESSMENT_ANON_EVAL")
    private Boolean peerAssessmentAnonEval;

    @Column(name = "PEER_ASSESSMENT_STUDENT_REVIEW")
    private Boolean peerAssessmentStudentReview = Boolean.FALSE;

    @Column(name = "PEER_ASSESSMENT_NUMBER_REVIEW")
    private Integer peerAssessmentNumberReviews;

    @Column(name = "PEER_ASSESSMENT_INSTRUCTIONS")
    private String peerAssessmentInstructions;

    @Column(name = "CONTENT_REVIEW")
    private Boolean contentReview;

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

    // TODO this data should come from a ReviewableAssignmentEntity and not be part of the Assignment (SOLID)
    // for now this is stored in the assignments properties
    // private String generateOriginalityReport;
    // private Boolean allowStudentViewReport;
    // private String submitReviewRepo;
    // private boolean checkTurnitin = true;
    // private boolean checkInternet = true;
    // private boolean checkPublications = true;
    // private boolean checkInstitution = true;
    // private boolean excludeBibliographic = true;
    // private boolean excludeQuoted = true;
    // private boolean excludeSelfPlag = true;
    // private boolean storeInstIndex = true;
    // private int excludeType = 0;
    // private int excludeValue = 1;
}

