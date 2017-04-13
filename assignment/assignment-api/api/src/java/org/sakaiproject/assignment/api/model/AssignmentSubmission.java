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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AssignmentSubmission represents a student submission for an assignment.
 */
@Entity
@Table(name = "ASN_SUBMISSION")

@Data
@NoArgsConstructor
public class AssignmentSubmission {

	@Id
	@Column(name = "SUBMISSION_ID", length = 36, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@ManyToOne
	@JoinColumn(name = "ASSIGNMENT_ID")
	private Assignment assignment;

	@OneToMany(mappedBy = "submission")
	private Set<AssignmentSubmissionSubmitter> submitters = new HashSet<>();

	//private List submissionLog;

	@Column(name = "SUBMITTED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateSubmitted;

	@Column(name = "RETURNED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateReturned;

	@Column(name = "MODIFIED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModified;

	@ElementCollection
	@Column(name = "ATTACHMENT")
	@CollectionTable(name = "ASN_SUBMISSION_ATTACHMENTS", joinColumns = @JoinColumn(name = "SUBMISSION_ID"))
	private Set<String> submittedAttachments;

	// TODO combine submitted and feedback attachements into a single table
	@ElementCollection
	@Column(name = "FEEDBACK_ATTACHMENT")
	@CollectionTable(name = "ASN_SUBMISSION_FEEDBACK_ATTACHMENTS", joinColumns = @JoinColumn(name = "SUBMISSION_ID"))
	private Set<String> feedbackAttachments;

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
	private Boolean submitted;

	@Column(name = "RETURNED")
	private Boolean returned;

	@Column(name = "GRADED")
	private Boolean graded;

	@Column(name = "GRADED_BY")
	private String gradedBy;

	@Column(name = "GRADE_RELEASED")
	private Boolean gradeReleased;

	@Column(name = "HONOR_PLEDGE")
	private Boolean honorPledge;

	@Column(name = "ANONYMOUS_SUBMISSION_ID")
	private String anonymousSubmissionId;

	@Column(name = "HIDDEN_DUE_DATE")
	private Boolean hiddenDueDate;

	@Column(name = "USER_SUBMISSION")
	private Boolean userSubmission;

	@ElementCollection
	@MapKeyColumn(name = "NAME")
	@Column(name = "VALUE")
	@CollectionTable(name = "ASN_SUBMISSION_PROPERTIES", joinColumns = @JoinColumn(name = "SUBMISSION_ID"))
	private Map<String, String> properties = new HashMap<>();

	// should get this data from the content review service
	// private Integer reviewScore;
	// private String reviewReport;
	// private String reviewStatus;
	// private String reviewIconUrl;
	// private String reviewError;
}
