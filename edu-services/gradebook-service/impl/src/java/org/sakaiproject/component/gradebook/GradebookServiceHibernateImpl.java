/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.component.gradebook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

/**
 * A Hibernate implementation of GradebookService.
 */
public class GradebookServiceHibernateImpl extends BaseHibernateManager implements GradebookService {
    private static final Log log = LogFactory.getLog(GradebookServiceHibernateImpl.class);

    private GradebookFrameworkService frameworkService;
    private GradebookExternalAssessmentService externalAssessmentService;
    private Authz authz;
    private GradebookPermissionService gradebookPermissionService;
    
	public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName)
        throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to check for assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
        Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
        return (assignment != null);
    }

	private boolean isUserAbleToViewAssignments(String gradebookUid) {
		Authz authz = getAuthz();
		return (authz.isUserAbleToEditAssessments(gradebookUid) || authz.isUserAbleToGrade(gradebookUid));
	}

	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().isUserAbleToGradeItemForStudent(gradebookUid, itemId, studentUid);
	}
	
	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String itemName, String studentUid) {
		
		if (itemName == null || studentUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in GradebookServiceHibernateImpl.isUserAbleToGradeItemForStudent");
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assignment = getAssignment(gradebookUid, itemName);
		if (assignment != null) {
			return isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid);
		}
		
		return false;

	}
	
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().isUserAbleToViewItemForStudent(gradebookUid, itemId, studentUid);
	}
	
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, String itemName, String studentUid) {
		
		if (itemName == null || studentUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in GradebookServiceHibernateImpl.isUserAbleToGradeItemForStudent");
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assignment = getAssignment(gradebookUid, itemName);
		if (assignment != null) {
			return isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid);
		}
		
		return false;

	}
	
	public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().getGradeViewFunctionForUserForStudentForItem(gradebookUid, itemId, studentUid);
	}
	
	public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String itemName, String studentUid) {
		if (itemName == null || studentUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in G.isUserAbleToGradeItemForStudent");
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assignment = getAssignment(gradebookUid, itemName);
		if (assignment != null) {
			return getGradeViewFunctionForUserForStudentForItem(gradebookUid, assignment.getId(), studentUid);
		}
		
		return null;
	}

	public List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(String gradebookUid)
		throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignments list");
			throw new SecurityException("You do not have permission to perform this operation");
		}

		final Long gradebookId = getGradebook(gradebookUid).getId();

        List internalAssignments = (List)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                return getAssignments(gradebookId, session);
            }
        });

		List<org.sakaiproject.service.gradebook.shared.Assignment> assignments = new ArrayList<org.sakaiproject.service.gradebook.shared.Assignment>();
		for (Iterator iter = internalAssignments.iterator(); iter.hasNext(); ) {
			Assignment assignment = (Assignment)iter.next();
			assignments.add(getAssignmentDefinition(assignment));
		}
		return assignments;
	}

	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final String assignmentName) throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
		if (assignment != null) {
			return getAssignmentDefinition(assignment);
		} else {
			return null;
		}
	}
	
	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final Long gbItemId) throws AssessmentNotFoundException {
		if (gbItemId == null || gradebookUid == null) {
			throw new IllegalArgumentException("null gbItemId passed to getAssignment");
		}
		if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get gb item with id " + gbItemId);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, gbItemId, session);
			}
		});
		
		if (assignment == null) {
			throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + gbItemId);
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assnDef;
		if (assignment != null) {
			assnDef =  getAssignmentDefinition(assignment);
		} else {
			assnDef = null;
		}
		
		return assnDef;
	}
		
	private org.sakaiproject.service.gradebook.shared.Assignment getAssignmentDefinition(Assignment internalAssignment) {
		org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition = new org.sakaiproject.service.gradebook.shared.Assignment();
    	assignmentDefinition.setName(internalAssignment.getName());
    	assignmentDefinition.setPoints(internalAssignment.getPointsPossible());
    	assignmentDefinition.setDueDate(internalAssignment.getDueDate());
    	assignmentDefinition.setCounted(internalAssignment.isCounted());
    	assignmentDefinition.setExternallyMaintained(internalAssignment.isExternallyMaintained());
    	assignmentDefinition.setExternalAppName(internalAssignment.getExternalAppName());
    	assignmentDefinition.setExternalId(internalAssignment.getExternalId());
    	assignmentDefinition.setReleased(internalAssignment.isReleased());
    	assignmentDefinition.setId(internalAssignment.getId());
    	if(internalAssignment.getCategory() != null) {
    		assignmentDefinition.setCategoryName(internalAssignment.getCategory().getName());
    		assignmentDefinition.setWeight(internalAssignment.getCategory().getWeight());
    	}
    	assignmentDefinition.setUngraded(internalAssignment.getUngraded());
    	return assignmentDefinition;
    }   

	public Double getAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);

		Double assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				
				if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}
				
				// If this is the student, then the assignment needs to have
				// been released.
				if (studentRequestingOwnScore && !assignment.isReleased()) {
					log.error("AUTHORIZATION FAILURE: Student " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve score for unreleased assignment " + assignment.getName());
					throw new SecurityException("You do not have permission to perform this operation");					
				}
				
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
				if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
				if (gradeRecord == null) {
					return null;
				} else {
					return gradeRecord.getPointsEarned();
				}
			}
		});
		if (log.isDebugEnabled()) log.debug("returning " + assignmentScore);
		return assignmentScore;
	}
	
	public Double getAssignmentScore(final String gradebookUid, final Long gbItemId, final String studentUid) {
		if (gradebookUid == null || gbItemId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScore");
		}
		
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, gbItemId, session);
			}
		});
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with the gbItemId " + gbItemId);
		}
		
		return getAssignmentScore(gradebookUid, assignment.getName(), studentUid);
	}
	
	public GradeDefinition getGradeDefinitionForStudentForItem(final String gradebookUid,
			final Long gbItemId, final String studentUid) {
		
		if (gradebookUid == null || gbItemId == null || studentUid == null) {
			throw new IllegalArgumentException("Null gradebookUid or gbItemId or studentUid" +
					" passed to getGradeDefinitionForStudentForItem");	
		}
		
		final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);

		GradeDefinition gradeDef = (GradeDefinition)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, gbItemId, session);
	
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment with the gbItemId " 
							+ gbItemId + " in gradebook " + gradebookUid);
				}
				
				if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + gbItemId);
					throw new SecurityException("You do not have permission to perform this operation");
				}
				
				Gradebook gradebook = assignment.getGradebook();
				
				GradeDefinition gradeDef = new GradeDefinition();
				gradeDef.setStudentUid(studentUid);
				gradeDef.setGradeEntryType(gradebook.getGrade_type());
				gradeDef.setGradeReleased(assignment.isReleased());
				
				// If this is the student, then the assignment needs to have
				// been released. Return null score information if not released
				if (studentRequestingOwnScore && !assignment.isReleased()) {
					gradeDef.setDateRecorded(null);
					gradeDef.setGrade(null);
					gradeDef.setGraderUid(null);
					gradeDef.setGradeComment(null);
					log.debug("Student " + getUserUid() + " in gradebook " + gradebookUid + " retrieving score for unreleased assignment " + assignment.getName());		
				
				} else {
				
					AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
					CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, gbItemId, studentUid);
					String commentText = gradeComment != null ? gradeComment.getCommentText() : null;
					if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
					
					if (gradeRecord == null) {
						gradeDef.setDateRecorded(null);
						gradeDef.setGrade(null);
						gradeDef.setGraderUid(null);
						gradeDef.setGradeComment(commentText);
					} else {
						gradeDef.setDateRecorded(gradeRecord.getDateRecorded());
						gradeDef.setGraderUid(gradeRecord.getGraderId());
						gradeDef.setGradeComment(commentText);
						
						if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
							List<AssignmentGradeRecord> gradeList = new ArrayList<AssignmentGradeRecord>();
							gradeList.add(gradeRecord);
							convertPointsToLetterGrade(gradebook, gradeList);
							AssignmentGradeRecord gradeRec = (AssignmentGradeRecord)gradeList.get(0);
							if (gradeRec != null) {
								gradeDef.setGrade(gradeRec.getLetterEarned());
							}
						} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
							Double percent = calculateEquivalentPercent(assignment.getPointsPossible(), gradeRecord.getPointsEarned());
							if (percent != null) {
								gradeDef.setGrade(percent.toString());
							}
						} else {
							if (gradeRecord.getPointsEarned() != null) {
								gradeDef.setGrade(gradeRecord.getPointsEarned().toString());
							}
						}
					}
				}
				
				return gradeDef;
			}
		});
		if (log.isDebugEnabled()) log.debug("returning grade def for " + studentUid);
		return gradeDef;
	}

	public void setAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid, final Double score, final String clientServiceDescription)
		throws GradebookNotFoundException, AssessmentNotFoundException {


		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade externally maintained assignment " + assignmentName + " from " + clientServiceDescription);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				if (!isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade student " + studentUid + " from " + clientServiceDescription + " for item " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				Date now = new Date();
				String graderId = getAuthn().getUserUid();
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
				if (gradeRecord == null) {
					// Creating a new grade record.
					gradeRecord = new AssignmentGradeRecord(assignment, studentUid, score);
				} else {
					gradeRecord.setPointsEarned(score);
				}
				gradeRecord.setGraderId(graderId);
				gradeRecord.setDateRecorded(now);
				session.saveOrUpdate(gradeRecord);
				
				session.save(new GradingEvent(assignment, graderId, studentUid, score));
				
				// Sync database.
				session.flush();
				session.clear();
				return null;
			}
		});

		if (log.isInfoEnabled()) log.info("Score updated in gradebookUid=" + gradebookUid + ", assignmentName=" + assignmentName + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
	}
	
	private Comment getInternalComment(String gradebookUid, String assignmentName, String studentUid, Session session) {
		Query q = session.createQuery(
		"from Comment as c where c.studentId=:studentId and c.gradableObject.gradebook.uid=:gradebookUid and c.gradableObject.name=:assignmentName");
		q.setParameter("studentId", studentUid);
		q.setParameter("gradebookUid", gradebookUid);
		q.setParameter("assignmentName", assignmentName);
		return (Comment)q.uniqueResult();		
	}

	public CommentDefinition getAssignmentScoreComment(final String gradebookUid, final String assignmentName, final String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
		CommentDefinition commentDefinition = null;
        Comment comment = (Comment)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
            	return getInternalComment(gradebookUid, assignmentName, studentUid, session);
            }
        });
        if (comment != null) {
        	commentDefinition = new CommentDefinition();
        	commentDefinition.setAssignmentName(assignmentName);
        	commentDefinition.setCommentText(comment.getCommentText());
        	commentDefinition.setDateRecorded(comment.getDateRecorded());
        	commentDefinition.setGraderUid(comment.getGraderId());
        	commentDefinition.setStudentUid(comment.getStudentId());
        }
		return commentDefinition;
	}
	
	public CommentDefinition getAssignmentScoreComment(final String gradebookUid, final Long gbItemId, final String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
		if (gradebookUid == null || gbItemId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScoreComment");
		}
		
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, gbItemId, session);
			}
		});
		
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with the gbItemId " + gbItemId);
		}
		
		return getAssignmentScoreComment(gradebookUid, assignment.getName(), studentUid);
	}

	public void setAssignmentScoreComment(final String gradebookUid, final String assignmentName, final String studentUid, final String commentText) throws GradebookNotFoundException, AssessmentNotFoundException {
		getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
        		Comment comment = getInternalComment(gradebookUid, assignmentName, studentUid, session);
        		if (comment == null) {
        			comment = new Comment(studentUid, commentText, getAssignmentWithoutStats(gradebookUid, assignmentName, session));
        		} else {
        			comment.setCommentText(commentText);
        		}
				comment.setGraderId(authn.getUserUid());
				comment.setDateRecorded(new Date());
				session.saveOrUpdate(comment);
            	return null;
            }
		});
	}
	
	public String getGradebookDefinitionXml(String gradebookUid) {		
		Long gradebookId = getGradebook(gradebookUid).getId();
		Gradebook gradebook = getGradebook(gradebookUid);
		
		GradebookDefinition gradebookDefinition = new GradebookDefinition();
		GradeMapping selectedGradeMapping = gradebook.getSelectedGradeMapping();
		gradebookDefinition.setSelectedGradingScaleUid(selectedGradeMapping.getGradingScale().getUid());
		gradebookDefinition.setSelectedGradingScaleBottomPercents(new HashMap<String,Double>(selectedGradeMapping.getGradeMap()));
		gradebookDefinition.setAssignments(getAssignments(gradebookUid));
		
		gradebookDefinition.setGradeType(gradebook.getGrade_type());
		gradebookDefinition.setCategoryType(gradebook.getCategory_type());	
		gradebookDefinition.setCategory(getCategories(gradebookId));
		
		return VersionedExternalizable.toXml(gradebookDefinition);
	}
	
	public void transferGradebookDefinitionXml(String fromGradebookUid, String toGradebookUid, String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		final Gradebook fromGradebook = getGradebook(fromGradebookUid);
		
		GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);
		
		gradebook.setCategory_type(gradebookDefinition.getCategoryType());
		gradebook.setGrade_type(gradebookDefinition.getGradeType());
		
		updateGradebook(gradebook);
		
		List category = getCategories(fromGradebook.getId());
	
		int assignmentsAddedCount = 0;
		Long catId = null;
		int undefined_nb = 0;
		
		List catList = gradebookDefinition.getCategory();
		List<Category> catList_tempt = new ArrayList<Category>();
				
		if(category.size() !=0) {
			//deal with category with assignments
			for(Iterator iter = category.iterator(); iter.hasNext();) {
			
				int categoryCount = 0;
				String catName = ((Category)iter.next()).getName();
		
				for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
					org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
				
					boolean newCategory = false;
					// Externally managed assessments should not be included.
					if (assignmentDef.isExternallyMaintained()) {
						continue;
					}
					
					if(catName.equals(assignmentDef.getCategoryName())) {
						newCategory = true;
						categoryCount++;
					}
		
					if (assignmentDef.getCategoryName() != null) {
						if(!newCategory) {}
						else if(newCategory && categoryCount == 1) {
							catId = createCategory(gradebook.getId(), assignmentDef.getCategoryName(), assignmentDef.getWeight(), 0);
							Category catTempt = getCategory(catId);
							
							catList_tempt.add(catTempt);
							createAssignmentForCategory(gradebook.getId(), catId, assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), !assignmentDef.isCounted(), false);
							assignmentsAddedCount++;
						}
						else{
							createAssignmentForCategory(gradebook.getId(), catId, assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), !assignmentDef.isCounted(), false);
							assignmentsAddedCount++;
						}
					
					}
					//deal with assignments in undefined.
					else {
						if (undefined_nb == 0) {
							createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), !assignmentDef.isCounted(), false);
							assignmentsAddedCount++;
							
						}
					}
				}
				undefined_nb++;
			}
			
			//deal with Category without assignments inside
			Iterator it_tempt = catList_tempt.iterator();
			Iterator it = catList.iterator();
			
			Category cat_cat ;
			Category cat_tempt;
		
			while(it_tempt.hasNext()) {
				cat_tempt = (Category) it_tempt.next();
				while(it.hasNext()) {
					cat_cat = (Category) it.next();
					if(cat_tempt.getName().equals(cat_cat.getName())) {
						catList.remove(cat_cat);
						it = catList.iterator();
					}	
				}
				it = catList.iterator();
			}
						
			Iterator itUpdate = catList.iterator();
			while(itUpdate.hasNext()){
				Category catObj = (Category)itUpdate.next();
				createCategory(gradebook.getId(), catObj.getName(), catObj.getWeight(), 0);				
			}
		}
		//deal with no categories
		else {
			for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
				org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
				
				// Externally managed assessments should not be included.
				if (assignmentDef.isExternallyMaintained()) {
					continue;
				}
				
				// All assignments should be unreleased even if they were released in the original.
				createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), !assignmentDef.isCounted(), false);
				assignmentsAddedCount++;
			}	
			
		}
				
		if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		
			// Carry over the old gradebook's selected grading scheme if possible.
		String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
		for (GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					Set<String> gradeCodes = (Set<String>)inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));							
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
					} else {
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
					}
					break MERGE_GRADE_MAPPING;
				}
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
		}
	}
	
	public void mergeGradebookDefinitionXml(String toGradebookUid, String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);

		List<String> assignmentNames = (List<String>)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(final Session session) throws HibernateException {
            	return session.createQuery(
            		"select asn.name from Assignment as asn where asn.gradebook.id=? and asn.removed=false").
            		setLong(0, gradebook.getId().longValue()).
            		list();
            }
        });
		
		// Add any non-externally-managed assignments with non-duplicate names.
		int assignmentsAddedCount = 0;
		for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
			org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
			
			// Externally managed assessments should not be included.
			if (assignmentDef.isExternallyMaintained()) {
				continue;
			}

			// Skip any input assignments with duplicate names.
			if (assignmentNames.contains(assignmentDef.getName())) {
				if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped duplicate assignment named " + assignmentDef.getName());
				continue;				
			}
			
			// All assignments should be unreleased even if they were released in the original.
			createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), !assignmentDef.isCounted(), false);
			assignmentsAddedCount++;
		}
		if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		
		// Carry over the old gradebook's selected grading scheme if possible.
		String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
			for (GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					Set<String> gradeCodes = (Set<String>)inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));							
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
					} else {
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
					}
					break MERGE_GRADE_MAPPING;
				}			
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
		}
	}
	
	public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {
    	
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Assignment asn = (Assignment)session.load(Assignment.class, assignmentId);
                Gradebook gradebook = asn.getGradebook();
                asn.setRemoved(true);
                session.update(asn);
                
                if(log.isInfoEnabled()) log.info("Assignment " + asn.getName() + " has been removed from " + gradebook);
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
        
    }
	
	public void addAssignment(String gradebookUid, org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to add an assignment");
			throw new SecurityException("You do not have permission to perform this operation");
		}

        // Ensure that points is > zero.
		Double points = assignmentDefinition.getPoints();
        if ((points == null) || (points.doubleValue() <= 0)) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

		Gradebook gradebook = getGradebook(gradebookUid);
		createAssignment(gradebook.getId(), assignmentDefinition.getName(), points, assignmentDefinition.getDueDate(), !assignmentDefinition.isCounted(), assignmentDefinition.isReleased());
	}

	public void updateAssignment(final String gradebookUid, final String assignmentName, final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {		
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to change the definition of assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		// This method is for Gradebook-managed assignments only.
		if (assignmentDefinition.isExternallyMaintained()) {
			log.error("User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to set assignment " + assignmentName + " to be externally maintained");
			throw new SecurityException("You do not have permission to perform this operation");
		}

		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to change the definition of externally maintained assignment " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}
				assignment.setCounted(assignmentDefinition.isCounted());
				assignment.setDueDate(assignmentDefinition.getDueDate());
				assignment.setName(assignmentDefinition.getName().trim());
				assignment.setPointsPossible(assignmentDefinition.getPoints());
				assignment.setReleased(assignmentDefinition.isReleased());
				updateAssignment(assignment, session);
				return null;
			}
		});
	}

    public Authz getAuthz() {
        return authz;
    }
    public void setAuthz(Authz authz) {
        this.authz = authz;
    }
    
    public GradebookPermissionService getGradebookPermissionService() {
    	return gradebookPermissionService;
    }
    public void setGradebookPermissionService(GradebookPermissionService gradebookPermissionService) {
    	this.gradebookPermissionService = gradebookPermissionService;
    }

    // Deprecated calls to new framework-specific interface.

	public void addGradebook(String uid, String name) {
		frameworkService.addGradebook(uid, name);
	}
	public void setAvailableGradingScales(Collection gradingScaleDefinitions) {
		frameworkService.setAvailableGradingScales(gradingScaleDefinitions);
	}
	public void setDefaultGradingScale(String uid) {
		frameworkService.setDefaultGradingScale(uid);
	}
	public void deleteGradebook( String uid)
		throws GradebookNotFoundException {
		frameworkService.deleteGradebook(uid);
	}
    public boolean isGradebookDefined(String gradebookUid) {
        return frameworkService.isGradebookDefined(gradebookUid);
    }

	public GradebookFrameworkService getFrameworkService() {
		return frameworkService;
	}
	public void setFrameworkService(GradebookFrameworkService frameworkService) {
		this.frameworkService = frameworkService;
	}

	// Deprecated calls to new interface for external assessment engines.

	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, double points, Date dueDate, String externalServiceDescription)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {
		externalAssessmentService.addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription);
	}
	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, Double points, Date dueDate, String externalServiceDescription, Boolean ungraded)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {
		externalAssessmentService.addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription, ungraded);
	}
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
                                         String title, double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException,AssignmentHasIllegalPointsException {
    	externalAssessmentService.updateExternalAssessment(gradebookUid, externalId, externalUrl, title, Double.valueOf(points), dueDate, Boolean.valueOf(false));
	}
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
        String title, Double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException,AssignmentHasIllegalPointsException {
    	externalAssessmentService.updateExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, Boolean.valueOf(false));
    }
	public void removeExternalAssessment(String gradebookUid,
            String externalId) throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.removeExternalAssessment(gradebookUid, externalId);
	}
	public void updateExternalAssessmentScore(String gradebookUid, String externalId,
			String studentUid, Double points) throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.updateExternalAssessmentScore(gradebookUid, externalId, studentUid, points.toString());
	}
	public void updateExternalAssessmentScores(String gradebookUid, String externalId, Map studentUidsToScores)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.updateExternalAssessmentScores(gradebookUid, externalId, studentUidsToScores);
	}
	public boolean isExternalAssignmentDefined(String gradebookUid, String externalId) throws GradebookNotFoundException {
		return externalAssessmentService.isExternalAssignmentDefined(gradebookUid, externalId);
	}

	public GradebookExternalAssessmentService getExternalAssessmentService() {
		return externalAssessmentService;
	}
	public void setExternalAssessmentService(
			GradebookExternalAssessmentService externalAssessmentService) {
		this.externalAssessmentService = externalAssessmentService;
	}

	public Map getImportCourseGrade(String gradebookUid)
	{
		HashMap returnMap = new HashMap();

		try
		{
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map enrollmentMap;
			String userUid = authn.getUserUid();
			
			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			enrollmentMap = new HashMap();

			Map enrollmentMapUid = new HashMap();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet());
			ArrayList grades = new ArrayList();
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					if(gradeRecord.getEnteredGrade() != null && !gradeRecord.getEnteredGrade().equalsIgnoreCase(""))
					{
						returnMap.put(enr.getUser().getDisplayId(), gradeRecord.getEnteredGrade());
					}
					else
					{
						if(!nonAssignment)
							returnMap.put(enr.getUser().getDisplayId(), (String)gradeMap.getGrade(gradeRecord.getNonNullAutoCalculatedGrade()));
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnMap;
	}

	public CourseGrade getCourseGrade(Long gradebookId) {
		return (CourseGrade)getHibernateTemplate().find(
				"from CourseGrade as cg where cg.gradebook.id=?",
				gradebookId).get(0);
	}

	public List getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				if(studentUids == null || studentUids.size() == 0) {
					if(log.isInfoEnabled()) log.info("Returning no grade records for an empty collection of student UIDs");
					return new ArrayList();
				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

				Long gradebookId = courseGrade.getGradebook().getId();
				Gradebook gradebook = getGradebook(gradebookId);
				List cates = getCategories(gradebookId);
				//double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
				//if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				for(Iterator iter = records.iterator(); iter.hasNext();) {
					CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
					//double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
					List totalEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session, gradebook, cates);
					double totalPointsEarned = ((Double)totalEarned.get(0)).doubleValue();
					double literalTotalPointsEarned = ((Double)totalEarned.get(1)).doubleValue();
					double totalPointsPossible = getTotalPointsInternal(gradebookId, session, gradebook, cates, cgr.getStudentId());
					cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
					if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
				}

				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}


	private List filterAndPopulateCourseGradeRecordsByStudents(CourseGrade courseGrade, Collection gradeRecords, Collection studentUids) {
		List filteredRecords = new ArrayList();
		Set missingStudents = new HashSet(studentUids);
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
			if (studentUids.contains(cgr.getStudentId())) {
				filteredRecords.add(cgr);
				missingStudents.remove(cgr.getStudentId());
			}
		}
		for (Iterator iter = missingStudents.iterator(); iter.hasNext(); ) {
			String studentUid = (String)iter.next();
			CourseGradeRecord cgr = new CourseGradeRecord(courseGrade, studentUid);
			filteredRecords.add(cgr);
		}
		return filteredRecords;
	}

	private double getTotalPointsInternal(final Long gradebookId, Session session, final Gradebook gradebook, final List categories, final String studentId)
	{
  	double totalPointsPossible = 0;
  	List assgnsList = session.createQuery(
  			"select asn from Assignment asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
  			setParameter("gbid", gradebookId).
  			list();

  	Iterator scoresIter = session.createQuery(
  	"select agr.pointsEarned, asn from AssignmentGradeRecord agr, Assignment asn where agr.gradableObject=asn and agr.studentId=:student and asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
  	setParameter("student", studentId).
  	setParameter("gbid", gradebookId).
  	list().iterator();

  	Set assignmentsTaken = new HashSet();
  	Set categoryTaken = new HashSet();
  	while (scoresIter.hasNext()) {
  		Object[] returned = (Object[])scoresIter.next();
  		Double pointsEarned = (Double)returned[0];
  		Assignment go = (Assignment) returned[1];
  		if (pointsEarned != null) {
  			if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
  			{
  				assignmentsTaken.add(go.getId());
  			}
  			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
  			{
  				assignmentsTaken.add(go.getId());
  			}
  			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
  			{
  				for(int i=0; i<categories.size(); i++)
  				{
  					Category cate = (Category) categories.get(i);
  					if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
  					{
  						assignmentsTaken.add(go.getId());
  						categoryTaken.add(cate.getId());
  						break;
  					}
  				}
  			}
  		}
  	}

  	if(!assignmentsTaken.isEmpty())
  	{
  		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
  		{
    		for(int i=0; i<categories.size(); i++)
    		{
    			Category cate = (Category) categories.get(i);
    			if(cate != null && !cate.isRemoved() && categoryTaken.contains(cate.getId()) )
    			{
    				totalPointsPossible += cate.getWeight().doubleValue();
    			}
    		}
    		return totalPointsPossible;
  		}
  		Iterator assignmentIter = assgnsList.iterator();
  		while (assignmentIter.hasNext()) {
  			Assignment asn = (Assignment) assignmentIter.next();
  			if(asn != null)
  			{
  				Double pointsPossible = asn.getPointsPossible();

  				if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(asn.getId()))
  				{
  					totalPointsPossible += pointsPossible.doubleValue();
  				}
  				else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(asn.getId()))
  				{
  					totalPointsPossible += pointsPossible.doubleValue();
  				}
  			}
  		}
  	}
  	else
  		totalPointsPossible = -1;

  	return totalPointsPossible;
	}

	private List getTotalPointsEarnedInternal(final Long gradebookId, final String studentId, final Session session, final Gradebook gradebook, final List categories) 
	{
  	double totalPointsEarned = 0;
  	double literalTotalPointsEarned = 0;
  	Iterator scoresIter = session.createQuery(
  			"select agr.pointsEarned, asn from AssignmentGradeRecord agr, Assignment asn where agr.gradableObject=asn and agr.studentId=:student and asn.gradebook.id=:gbid and asn.removed=false and asn.pointsPossible > 0").
  			setParameter("student", studentId).
  			setParameter("gbid", gradebookId).
  			list().iterator();

  	List assgnsList = session.createQuery(
  	"from Assignment as asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
  	setParameter("gbid", gradebookId).
  	list();

  	Map cateScoreMap = new HashMap();
  	Map cateTotalScoreMap = new HashMap();

  	Set assignmentsTaken = new HashSet();
  	while (scoresIter.hasNext()) {
  		Object[] returned = (Object[])scoresIter.next();
  		Double pointsEarned = (Double)returned[0];
  		Assignment go = (Assignment) returned[1];
  		if (go.isCounted() && pointsEarned != null) {
  			if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
  			{
  				totalPointsEarned += pointsEarned.doubleValue();
  				literalTotalPointsEarned += pointsEarned.doubleValue();
  				assignmentsTaken.add(go.getId());
  			}
  			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
  			{
  				totalPointsEarned += pointsEarned.doubleValue();
  				literalTotalPointsEarned += pointsEarned.doubleValue();
  				assignmentsTaken.add(go.getId());
  			}
  			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
  			{
  				for(int i=0; i<categories.size(); i++)
  				{
  					Category cate = (Category) categories.get(i);
  					if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
  					{
  						assignmentsTaken.add(go.getId());
  						literalTotalPointsEarned += pointsEarned.doubleValue();
  						if(cateScoreMap.get(cate.getId()) != null)
  						{
  							cateScoreMap.put(cate.getId(), Double.valueOf(((Double)cateScoreMap.get(cate.getId())).doubleValue() + pointsEarned.doubleValue()));
  						}
  						else
  						{
  							cateScoreMap.put(cate.getId(), Double.valueOf(pointsEarned));
  						}
  						break;
  					}
  				}
  			}
  		}
  	}

  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
  	{
  		Iterator assgnsIter = assgnsList.iterator();
  		while (assgnsIter.hasNext()) 
  		{
  			Assignment asgn = (Assignment)assgnsIter.next();
  			if(assignmentsTaken.contains(asgn.getId()))
  			{
  				for(int i=0; i<categories.size(); i++)
  				{
  					Category cate = (Category) categories.get(i);
  					if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()))
  					{
  						if(cateTotalScoreMap.get(cate.getId()) == null)
  						{
  							cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
  						}
  						else
  						{
  							cateTotalScoreMap.put(cate.getId(), Double.valueOf(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));
  						}
  					}
  				}
  			}
  		}
  	}

  	if(assignmentsTaken.isEmpty())
  		totalPointsEarned = -1;

  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
  	{
  		for(int i=0; i<categories.size(); i++)
  		{
  			Category cate = (Category) categories.get(i);
  			if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
  			{
  				totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
  			}
  		}
  	}

  	if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
  	List returnList = new ArrayList();
  	returnList.add(Double.valueOf(totalPointsEarned));
  	returnList.add(Double.valueOf(literalTotalPointsEarned));
  	return returnList;
	}

	public Gradebook getGradebook(Long id) {
		return (Gradebook)getHibernateTemplate().load(Gradebook.class, id);
	}

	protected List getAssignmentsCounted(final Long gradebookId) throws HibernateException 
	{
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List assignments = session.createQuery(
						"from Assignment as asn where asn.gradebook.id=? and asn.removed=false and asn.notCounted=false").
						setLong(0, gradebookId.longValue()).
						list();
				return assignments;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}
	
  public boolean checkStuendsNotSubmitted(String gradebookUid)
  {
  	Gradebook gradebook = getGradebook(gradebookUid);
  	Set studentUids = getAllStudentUids(getGradebookUid(gradebook.getId()));
  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY || gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
  	{
  		List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
  		List assigns = getAssignments(gradebook.getId(), Assignment.DEFAULT_SORT, true);
  		List filteredAssigns = new ArrayList();
  		for(Iterator iter = assigns.iterator(); iter.hasNext();)
  		{
  			Assignment assignment = (Assignment)iter.next();
  			if(assignment.isCounted() && !assignment.getUngraded())
  				filteredAssigns.add(assignment);
  		}
  		List filteredRecords = new ArrayList();
  		for(Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(!agr.isCourseGradeRecord() && agr.getAssignment().isCounted() && !agr.getAssignment().getUngraded())
  			{
  				if(agr.getPointsEarned() == null)
  					return true;
  				filteredRecords.add(agr);
  			}
  		}

  		if(filteredRecords.size() < (filteredAssigns.size() * studentUids.size()))
  			return true;
  		
  		return false;
  	}
  	else
  	{
    	List assigns = getAssignments(gradebook.getId(), Assignment.DEFAULT_SORT, true);
    	List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
    	Set filteredAssigns = new HashSet();
    	for (Iterator iter = assigns.iterator(); iter.hasNext(); )
    	{
    		Assignment assign = (Assignment) iter.next();
    		if(assign != null && assign.isCounted() && !assign.getUngraded())
    		{
    			if(assign.getCategory() != null && !assign.getCategory().isRemoved())
    			{
    				filteredAssigns.add(assign.getId());
    			}
    		}
    	}
    	
  		List filteredRecords = new ArrayList();
  		for(Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(filteredAssigns.contains(agr.getAssignment().getId()) && !agr.isCourseGradeRecord())
  			{
  				if(agr.getPointsEarned() == null)
  					return true;
  				filteredRecords.add(agr);
  			}
  		}

  		if(filteredRecords.size() < filteredAssigns.size() * studentUids.size())
  			return true;

  		return false;
  	}
  }

  public List getAllAssignmentGradeRecords(final Long gradebookId, final Collection studentUids) {
  	HibernateCallback hc = new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			if(studentUids.size() == 0) {
  				// If there are no enrollments, no need to execute the query.
  				if(log.isInfoEnabled()) log.info("No enrollments were specified.  Returning an empty List of grade records");
  				return new ArrayList();
  			} else {
  				Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
  				"agr.gradableObject.gradebook.id=:gradebookId order by agr.pointsEarned");
  				q.setLong("gradebookId", gradebookId.longValue());
  				return filterGradeRecordsByStudents(q.list(), studentUids);
  			}
  		}
  	};
  	return (List)getHibernateTemplate().execute(hc);
  }
  
  private List getAllAssignmentGradeRecordsForGbItem(final Long gradableObjectId, 
		  final Collection studentUids) {
	  	HibernateCallback hc = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException {
	  			if(studentUids.size() == 0) {
	  				// If there are no enrollments, no need to execute the query.
	  				if(log.isInfoEnabled()) log.info("No enrollments were specified.  Returning an empty List of grade records");
	  				return new ArrayList();
	  			} else {
	  				Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
	  				"agr.gradableObject.id=:gradableObjectId order by agr.pointsEarned");
	  				q.setLong("gradableObjectId", gradableObjectId.longValue());
	  				return filterGradeRecordsByStudents(q.list(), studentUids);
	  			}
	  		}
	  	};
	  	return (List)getHibernateTemplate().execute(hc);
	  }

  public List getAssignments(final Long gradebookId, final String sortBy, final boolean ascending) {
  	return (List)getHibernateTemplate().execute(new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			List assignments = getAssignments(gradebookId, session);

  			sortAssignments(assignments, sortBy, ascending);
  			return assignments;
  		}
  	});
  }
  private void sortAssignments(List assignments, String sortBy, boolean ascending) {
    // WARNING: AZ - this method is duplicated in GradebookManagerHibernateImpl
  	Comparator comp;
    if (Assignment.SORT_BY_NAME.equals(sortBy)) {
        comp = GradableObject.nameComparator;
    } else if(Assignment.SORT_BY_DATE.equals(sortBy)){
        comp = GradableObject.dateComparator;
    } else if(Assignment.SORT_BY_MEAN.equals(sortBy)) {
        comp = GradableObject.meanComparator;
    } else if(Assignment.SORT_BY_POINTS.equals(sortBy)) {
        comp = Assignment.pointsComparator;
    } else if(Assignment.SORT_BY_RELEASED.equals(sortBy)){
        comp = Assignment.releasedComparator;
    } else if(Assignment.SORT_BY_COUNTED.equals(sortBy)){
        comp = Assignment.countedComparator;
    } else if(Assignment.SORT_BY_EDITOR.equals(sortBy)){
        comp = Assignment.gradeEditorComparator;
    } else if (Assignment.SORT_BY_SORTING.equals(sortBy)) {
        comp = GradableObject.sortingComparator;
    } else {
        comp = GradableObject.defaultComparator;
    }
  	Collections.sort(assignments, comp);
  	if(!ascending) {
  		Collections.reverse(assignments);
  	}
  	if (log.isDebugEnabled()) {
  	    log.debug("sortAssignments: ordering by "+sortBy+" ("+comp+"), ascending="+ascending);
  	}
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.service.gradebook.shared.GradebookService#getViewableAssignmentsForCurrentUser(java.lang.String)
   */
  public List<org.sakaiproject.service.gradebook.shared.Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid)
  throws GradebookNotFoundException {

	  List<Assignment> viewableAssignments = new ArrayList();
	  List<org.sakaiproject.service.gradebook.shared.Assignment> assignmentsToReturn = new ArrayList();

	  Gradebook gradebook = getGradebook(gradebookUid);

	  // will send back all assignments if user can grade all
	  if (getAuthz().isUserAbleToGradeAll(gradebookUid)) {
		  viewableAssignments = getAssignments(gradebook.getId(), null, true);
	  } else if (getAuthz().isUserAbleToGrade(gradebookUid)) {
		  // if user can grade and doesn't have grader perm restrictions, they
		  // may view all assigns
		  if (!getAuthz().isUserHasGraderPermissions(gradebookUid)) {
			  viewableAssignments = getAssignments(gradebook.getId(), null, true);
		  } else {
			  // this user has grader perms, so we need to filter the items returned
			  // if this gradebook has categories enabled, we need to check for category-specific restrictions

			  if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY) {
				  assignmentsToReturn = getAssignments(gradebookUid);
			  } else {

				  String userUid = getUserUid();
				  if (getGradebookPermissionService().getPermissionForUserForAllAssignment(gradebook.getId(), userUid)) {
					  assignmentsToReturn = getAssignments(gradebookUid);
				  }

				  // categories are enabled, so we need to check the category restrictions
				  List allCategories = getCategoriesWithAssignments(gradebook.getId());
				  if (allCategories != null && !allCategories.isEmpty()) {
					  List<Category> viewableCategories = getGradebookPermissionService().getCategoriesForUser(gradebook.getId(), userUid, allCategories, gradebook.getCategory_type());

					  for (Iterator catIter = viewableCategories.iterator(); catIter.hasNext();) {
						  Category cat = (Category) catIter.next();
						  if (cat != null) {
							  List assignments = cat.getAssignmentList();
							  if (assignments != null && !assignments.isEmpty()) {
								  viewableAssignments.addAll(assignments);
							  }
						  }
					  }
				  }
			  }
		  }
	  } else if (getAuthz().isUserAbleToViewOwnGrades(gradebookUid)) {
		  // if user is just a student, we need to filter out unreleased items
		  List allAssigns = getAssignments(gradebook.getId(), null, true);
		  if (allAssigns != null) {
			  for (Iterator aIter = allAssigns.iterator(); aIter.hasNext();) {
				  Assignment assign = (Assignment) aIter.next();
				  if (assign != null && assign.isReleased()) {
					  viewableAssignments.add(assign);
				  }
			  }
		  }
	  }

	  // Now we need to convert these to the assignment template objects
	  if (viewableAssignments != null && !viewableAssignments.isEmpty()) {
		  for (Iterator assignIter = viewableAssignments.iterator(); assignIter.hasNext();) {
			  Assignment assignment = (Assignment) assignIter.next();
			  assignmentsToReturn.add(getAssignmentDefinition(assignment));
		  }
	  }

	  return assignmentsToReturn;

  }
  
  public Map<String, String> getViewableStudentsForItemForCurrentUser(final String gradebookUid, final Long gradableObjectId) {
	  String userUid = authn.getUserUid();

	  return getViewableStudentsForItemForUser(userUid, gradebookUid, gradableObjectId);
  }
  
  public Map<String, String> getViewableStudentsForItemForUser(final String userUid, final String gradebookUid, final Long gradableObjectId) {

      if (gradebookUid == null || gradableObjectId == null || userUid == null) {
          throw new IllegalArgumentException("null gradebookUid or gradableObjectId or " +
                  "userId passed to getViewableStudentsForUserForItem." +
                  " gradebookUid: " + gradebookUid + " gradableObjectId:" + 
                  gradableObjectId + " userId: " + userUid);
      }

      Assignment gradebookItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException {
              return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
          }
      });

      if (gradebookItem == null) {
          log.debug("The gradebook item does not exist, so returning empty set");
          return new HashMap();
      }

      Long categoryId = gradebookItem.getCategory() == null ? null : gradebookItem.getCategory().getId();

      Map<EnrollmentRecord, String> enrRecFunctionMap = new HashMap();

      enrRecFunctionMap = authz.findMatchingEnrollmentsForItemForUser(userUid, gradebookUid, categoryId, getGradebook(gradebookUid).getCategory_type(), null, null);
      if (enrRecFunctionMap == null) {
          return new HashMap();
      }

      Map<String, String> studentIdFunctionMap = new HashMap();
      for (Iterator enrIter = enrRecFunctionMap.keySet().iterator(); enrIter.hasNext();) {
          EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
          if (enr != null && enrRecFunctionMap.get(enr) != null) {
              studentIdFunctionMap.put(enr.getUser().getUserUid(), enrRecFunctionMap.get(enr));
          }
      }
      return studentIdFunctionMap;
  }

  public boolean isGradableObjectDefined(Long gradableObjectId) {
	  if (gradableObjectId == null) {
		  throw new IllegalArgumentException("null gradableObjectId passed to isGradableObjectDefined");
	  }
	  
	  return isAssignmentDefined(gradableObjectId);
  }
  
  public Map getViewableSectionUuidToNameMap(String gradebookUid) {
	  if (gradebookUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid passed to getViewableSectionIdToNameMap");
	  }
	  
	  Map<String, String> sectionIdNameMap = new HashMap();
	  
	  List viewableCourseSections = getAuthz().getViewableSections(gradebookUid); 
	  if (viewableCourseSections == null || viewableCourseSections.isEmpty()) {
		  return sectionIdNameMap;
	  }
	  
	  for (Iterator sectionIter = viewableCourseSections.iterator(); sectionIter.hasNext();) {
		  CourseSection section = (CourseSection) sectionIter.next();
		  if (section != null) {
			  sectionIdNameMap.put(section.getUuid(), section.getTitle());
		  }
	  }
	  
	  return sectionIdNameMap;
  }
  
  public boolean currentUserHasGradeAllPerm(String gradebookUid) {
	  return authz.isUserAbleToGradeAll(gradebookUid);
  }
  
  public boolean isUserAllowedToGradeAll(String gradebookUid, String userUid) {
      return authz.isUserAbleToGradeAll(gradebookUid, userUid);
  }

  public boolean currentUserHasGradingPerm(String gradebookUid) {
	  return authz.isUserAbleToGrade(gradebookUid);
  }
  
  public boolean isUserAllowedToGrade(String gradebookUid, String userUid) {
      return authz.isUserAbleToGrade(gradebookUid, userUid);
  }

  public boolean currentUserHasEditPerm(String gradebookUid) {
	  return authz.isUserAbleToEditAssessments(gradebookUid);
  }

  public boolean currentUserHasViewOwnGradesPerm(String gradebookUid) {
	  return authz.isUserAbleToViewOwnGrades(gradebookUid);
  }
  
  public List<GradeDefinition> getGradesForStudentsForItem(final String gradebookUid, final Long gradableObjectId, List<String> studentIds) {
	  if (gradableObjectId == null) {
		  throw new IllegalArgumentException("null gradableObjectId passed to getGradesForStudentsForItem");
	  }
	  
	  List<org.sakaiproject.service.gradebook.shared.GradeDefinition> studentGrades = new ArrayList();
	  
	  if (studentIds != null && !studentIds.isEmpty()) {
		  // first, we need to make sure the current user is authorized to view the
		  // grades for all of the requested students
		  Assignment gbItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			  public Object doInHibernate(Session session) throws HibernateException {
				  return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
			  }
		  });
		  
		  if (gbItem != null) {
			  Gradebook gradebook = gbItem.getGradebook();
			  
			  if (!authz.isUserAbleToGrade(gradebook.getUid())) {
				  throw new SecurityException("User " + authn.getUserUid() + 
						  " attempted to access grade information without permission in gb " + 
						  gradebook.getUid() + " using gradebookService.getGradesForStudentsForItem");
			  }
			  
			  Long categoryId = gbItem.getCategory() != null ? gbItem.getCategory().getId() : null;
			  Map enrRecFunctionMap = authz.findMatchingEnrollmentsForItem(gradebook.getUid(), categoryId, gradebook.getCategory_type(), null, null);
			  Set enrRecs = enrRecFunctionMap.keySet();
			  Map studentIdEnrRecMap = new HashMap();
			  if (enrRecs != null) {
				  for (Iterator enrIter = enrRecs.iterator(); enrIter.hasNext();) {
					  EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
					  if (enr != null) {
						  studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
					  }
				  }
			  }
			  
			  for (Iterator stIter = studentIds.iterator(); stIter.hasNext();) {
				  String studentId = (String) stIter.next();
				  if (studentId != null) {
					  if (!studentIdEnrRecMap.containsKey(studentId)) {
						  throw new SecurityException("User " + authn.getUserUid() + 
						  " attempted to access grade information for student " + studentId + 
						  " without permission in gb " + gradebook.getUid() + 
						  " using gradebookService.getGradesForStudentsForItem");
					  }
				  }
			  }
			  
			  // retrieve the grading comments for all of the students
			  List<Comment> commentRecs = getComments(gbItem, studentIds);
			  Map<String, String> studentIdCommentTextMap = new HashMap();
			  if (commentRecs != null) {
				  for (Iterator<Comment> cIter = commentRecs.iterator(); cIter.hasNext();) {
					  Comment comment = cIter.next();
					  if (comment != null) {
						  studentIdCommentTextMap.put(comment.getStudentId(), comment.getCommentText());
					  }
				  }
			  }
			  
			  // now, we can populate the grade information
			  List<AssignmentGradeRecord> gradeRecs = getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIds);
			  if (gradeRecs != null) {
				  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					  convertPointsToLetterGrade(gradebook, gradeRecs);
				  } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					  convertPointsToPercentage(gradebook, gradeRecs);
				  }
				  
				  boolean gradeReleased = gradebook.isAssignmentsDisplayed() && gbItem.isReleased();
				  
				  for (Iterator gradeIter = gradeRecs.iterator(); gradeIter.hasNext();) {
					  AssignmentGradeRecord agr = (AssignmentGradeRecord) gradeIter.next();
					  if (agr != null) {
						  GradeDefinition gradeDef = new GradeDefinition();
						  gradeDef.setStudentUid(agr.getStudentId());
						  gradeDef.setGradeEntryType(gradebook.getGrade_type());
						  gradeDef.setGradeReleased(gradeReleased);
						  gradeDef.setGraderUid(agr.getGraderId());
						  gradeDef.setDateRecorded(agr.getDateRecorded());
						  
						  String commentText = studentIdCommentTextMap.get(agr.getStudentId());
						  if (commentText != null) {
							  gradeDef.setGradeComment(commentText);
						  }
						  
						  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
							  gradeDef.setGrade(agr.getLetterEarned());
						  } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
						      String grade = agr.getPercentEarned() != null ? agr.getPercentEarned().toString() : null;
							  gradeDef.setGrade(grade);
						  } else {
						      String grade = agr.getPointsEarned() != null ? agr.getPointsEarned().toString() : null;
							  gradeDef.setGrade(grade);
						  }
					  	  	
						  studentGrades.add(gradeDef);
					  }
				  }
			  }
		  }
	  }
	  
	  return studentGrades;
  }
  
  public boolean isGradeValid(String gradebookUuid, String grade) {
	  if (gradebookUuid == null) {
		  throw new IllegalArgumentException("Null gradebookUuid passed to isGradeValid");
	  }
	  Gradebook gradebook;
	  try {
		  gradebook = getGradebook(gradebookUuid);
	  } catch (GradebookNotFoundException gnfe) {
		  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
				  gradebookUuid + "Error: " + gnfe.getMessage());
	  }
	  
	  int gradeEntryType = gradebook.getGrade_type();
	  LetterGradePercentMapping mapping = null;
	  if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
		  mapping = getLetterGradePercentMapping(gradebook);
	  }
	  
	  return isGradeValid(grade, gradeEntryType, mapping);
  }
  
  private boolean isGradeValid(String grade, int gradeEntryType, LetterGradePercentMapping gradeMapping) {

	  boolean gradeIsValid = false;

	  if (grade == null || "".equals(grade)) {

		  gradeIsValid = true;

	  } else {

		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS ||
				  gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE) {
			  try {
				  Double gradeAsDouble = Double.parseDouble(grade);
				  // grade must be greater than or equal to 0
				  if (gradeAsDouble.doubleValue() >= 0) {
					  // check that there are no more than 2 decimal places
					  String[] splitOnDecimal = grade.split("\\.");
					  if (splitOnDecimal == null || splitOnDecimal.length < 2) {
						  gradeIsValid = true;
					  } else if (splitOnDecimal.length == 2) {
						  String decimal = splitOnDecimal[1];
						  if (decimal.length() <= 2) {
							  gradeIsValid = true;
						  }
					  }
				  }
			  } catch (NumberFormatException nfe) {
				  if (log.isDebugEnabled()) log.debug("Passed grade is not a numeric value");
			  }

		  } else if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
			  if (gradeMapping == null) {
				  throw new IllegalArgumentException("Null mapping passed to isGradeValid for a letter grade-based gradeook");
			  }

			  String standardizedGrade = gradeMapping.standardizeInputGrade(grade);
			  if (standardizedGrade != null) {
				  gradeIsValid = true;
			  }
		  } else {
			  throw new IllegalArgumentException("Invalid gradeEntryType passed to isGradeValid");
		  }
	  }

	  return gradeIsValid;
  }

  public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap) {
	  if (gradebookUid == null) {
		  throw new IllegalArgumentException("null gradebookUid passed to identifyStudentsWithInvalidGrades");
	  }

	  List<String> studentsWithInvalidGrade = new ArrayList<String>();

	  if (studentIdToGradeMap != null) {
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid);
		  } catch (GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  LetterGradePercentMapping gradeMapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  gradeMapping = getLetterGradePercentMapping(gradebook);
		  }

		  for (String studentId : studentIdToGradeMap.keySet()) {
			  String grade = studentIdToGradeMap.get(studentId);
			  if (!isGradeValid(grade, gradebook.getGrade_type(), gradeMapping)) {
				  studentsWithInvalidGrade.add(studentId);
			  }
		  }
	  }  
	  return studentsWithInvalidGrade;
  }

  public void saveGradeAndCommentForStudent(String gradebookUid, Long gradableObjectId, String studentUid, String grade, String comment) {
	  if (gradebookUid == null || gradableObjectId == null || studentUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId or studentUid passed to saveGradeAndCommentForStudent");
	  }

	  GradeDefinition gradeDef = new GradeDefinition();
	  gradeDef.setStudentUid(studentUid);
	  gradeDef.setGrade(grade);
	  gradeDef.setGradeComment(comment);

	  List<GradeDefinition> gradeDefList = new ArrayList<GradeDefinition>();
	  gradeDefList.add(gradeDef);

	  saveGradesAndComments(gradebookUid, gradableObjectId, gradeDefList);
  }

  public void saveGradesAndComments(final String gradebookUid, final Long gradableObjectId, List<GradeDefinition> gradeDefList) {
	  if (gradebookUid == null || gradableObjectId == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId passed to saveGradesAndComments");
	  }

	  if (gradeDefList != null) {
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid); 
		  } catch (GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			  public Object doInHibernate(Session session) throws HibernateException {
				  return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
			  }
		  });

		  if (assignment == null) {
			  throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + gradableObjectId);
		  }

		  if (!currentUserHasGradingPerm(gradebookUid)) {
			  log.warn("User attempted to save grades and comments without authorization");
			  throw new SecurityException("Current user is not authorized to save grades or comments in gradebook " + gradebookUid);
		  }

		  // let's identify all of the students being updated first
		  Map<String, GradeDefinition> studentIdGradeDefMap = new HashMap<String, GradeDefinition>();
		  Map<String, String> studentIdToGradeMap = new HashMap<String, String>();

		  for (GradeDefinition gradeDef: gradeDefList) {
			  studentIdGradeDefMap.put(gradeDef.getStudentUid(), gradeDef);
			  studentIdToGradeMap.put(gradeDef.getStudentUid(), gradeDef.getGrade());
		  }

		  // check for invalid grades
		  List invalidStudents = identifyStudentsWithInvalidGrades(gradebookUid, studentIdToGradeMap);
		  if (invalidStudents != null && !invalidStudents.isEmpty()) {
			  throw new InvalidGradeException ("At least one grade passed to be updated is " +
			  "invalid. No grades or comments were updated.");
		  }

		  boolean userHasGradeAllPerm = currentUserHasGradeAllPerm(gradebookUid);

		  // let's retrieve all of the existing grade recs for the given students
		  // and assignments
		  List<AssignmentGradeRecord> allGradeRecs = 
			  getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIdGradeDefMap.keySet());


		  // put in map for easier accessibility
		  Map<String, AssignmentGradeRecord> studentIdToAgrMap = new HashMap<String, AssignmentGradeRecord>();
		  if (allGradeRecs != null) {
			  for (AssignmentGradeRecord rec : allGradeRecs) {
				  studentIdToAgrMap.put(rec.getStudentId(), rec);
			  }
		  }

		  // set up the grader and grade time
		  String graderId = getAuthn().getUserUid();
		  Date now = new Date();

		  // get grade mapping, if nec, to convert grades to points
		  LetterGradePercentMapping mapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  mapping = getLetterGradePercentMapping(gradebook);
		  }

		  // get all of the comments, as well
		  List<Comment> allComments = getComments(assignment, studentIdGradeDefMap.keySet());
		  // put in a map for easier accessibility
		  Map<String, Comment> studentIdCommentMap = new HashMap<String, Comment>();
		  if (allComments != null) {
			  for (Comment comment : allComments) {
				  studentIdCommentMap.put(comment.getStudentId(), comment);
			  }
		  }

		  // these are the records that will need to be updated. iterate through
		  // everything and then we'll save it all at once
		  Set<AssignmentGradeRecord> agrToUpdate = new HashSet<AssignmentGradeRecord>();
		  Set<Comment> commentsToUpdate = new HashSet<Comment>();
		  Set<GradingEvent> eventsToAdd = new HashSet<GradingEvent>();

		  for (GradeDefinition gradeDef : gradeDefList) {

			  String studentId = gradeDef.getStudentUid();

			  // check specific grading privileges if user does not have
			  // grade all perm
			  if (!userHasGradeAllPerm) {
				  if (!isUserAbleToGradeItemForStudent(gradebookUid, gradableObjectId, studentId)) {
					  log.warn("User " + graderId + " attempted to save a grade for " + studentId + 
					  " without authorization");

					  throw new SecurityException("User " + graderId + " attempted to save a grade for " + 
							  studentId + " without authorization");
				  }
			  }

			  Double convertedGrade = convertInputGradeToPoints(gradebook.getGrade_type(), mapping, assignment.getPointsPossible(), gradeDef.getGrade());

			  // let's see if this agr needs to be updated
			  AssignmentGradeRecord gradeRec = studentIdToAgrMap.get(studentId);
			  if (gradeRec != null) {
				  if ((convertedGrade == null && gradeRec.getPointsEarned() != null) || 
						  (convertedGrade != null && gradeRec.getPointsEarned() == null) ||
						  (convertedGrade != null && gradeRec.getPointsEarned() != null && 
								  !convertedGrade.equals(gradeRec.getPointsEarned()))) {
					  
					  gradeRec.setPointsEarned(convertedGrade);
					  gradeRec.setGraderId(graderId);
					  gradeRec.setDateRecorded(now);

					  agrToUpdate.add(gradeRec);

					  // we also need to add a GradingEvent
					  // the event stores the actual input grade, not the converted one
					  GradingEvent event = new GradingEvent(assignment, graderId, studentId, gradeDef.getGrade());
					  eventsToAdd.add(event);
				  }
			  } else {
				  // if the grade is something other than null, add a new AGR
				  if (gradeDef.getGrade() != null && !gradeDef.getGrade().trim().equals("")) {
					  gradeRec =  new AssignmentGradeRecord(assignment, studentId, convertedGrade);
					  gradeRec.setPointsEarned(convertedGrade);
					  gradeRec.setGraderId(graderId);
					  gradeRec.setDateRecorded(now);

					  agrToUpdate.add(gradeRec);

					  // we also need to add a GradingEvent
					  // the event stores the actual input grade, not the converted one
					  GradingEvent event = new GradingEvent(assignment, graderId, studentId, gradeDef.getGrade());
					  eventsToAdd.add(event);
				  }
			  }

			  // let's see if the comment needs to be updated
			  Comment comment = studentIdCommentMap.get(studentId);
			  if (comment != null) {
				  boolean oldCommentIsNull = comment.getCommentText() == null || comment.getCommentText().equals("");
				  boolean newCommentIsNull = gradeDef.getGradeComment() == null || gradeDef.getGradeComment().equals("");
				  
				  if ((oldCommentIsNull && !newCommentIsNull) || 
						  (!oldCommentIsNull && newCommentIsNull) ||
						  (!oldCommentIsNull && !newCommentIsNull && 
								  !gradeDef.getGradeComment().equals(comment.getCommentText()))) {
					  // update this comment
					  comment.setCommentText(gradeDef.getGradeComment());
					  comment.setGraderId(graderId);
					  comment.setDateRecorded(now);

					  commentsToUpdate.add(comment);
				  }
			  } else {
				  // if there is a comment, add it
				  if (gradeDef.getGradeComment() != null && !gradeDef.getGradeComment().trim().equals("")) {
					  comment = new Comment(studentId, gradeDef.getGradeComment(), assignment);
					  comment.setGraderId(graderId);
					  comment.setDateRecorded(now);

					  commentsToUpdate.add(comment);
				  }
			  }
		  }

		  // now let's save them
		  try {
			  getHibernateTemplate().saveOrUpdateAll(agrToUpdate);
			  getHibernateTemplate().saveOrUpdateAll(commentsToUpdate);
			  getHibernateTemplate().saveOrUpdateAll(eventsToAdd);
		  }	catch (HibernateOptimisticLockingFailureException holfe) {
			  if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item " + gradableObjectId);
			  throw new StaleObjectModificationException(holfe);
		  } catch (StaleObjectStateException sose) {
			  if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item " + gradableObjectId);
			  throw new StaleObjectModificationException(sose);
		  }
	  }
  }

  /**
   * 
   * @param gradeEntryType
   * @param mapping
   * @param gbItemPointsPossible
   * @param grade
   * @return given a generic String grade, converts it to the equivalent Double
   * point value that will be stored in the db based upon the gradebook's grade entry type
   */
  private Double convertInputGradeToPoints(int gradeEntryType, LetterGradePercentMapping mapping, 
		  Double gbItemPointsPossible, String grade) throws InvalidGradeException {
	  Double convertedValue = null;

	  if (grade != null && !"".equals(grade)) {
		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS) {
			  try {
				  Double pointValue = Double.parseDouble(grade);
				  convertedValue = pointValue;
			  } catch (NumberFormatException nfe) {
				  throw new InvalidGradeException("Invalid grade passed to convertInputGradeToPoints");
			  }
		  } else if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE ||
				  gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {

			  // for letter or %-based grading, we need to calculate the equivalent point value
			  if (gbItemPointsPossible == null) {
				  throw new IllegalArgumentException("Null points possible passed" +
				  " to convertInputGradeToPoints for letter or % based grading");
			  }

			  Double percentage = null;
			  if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
				  if (mapping == null) {
					  throw new IllegalArgumentException("No mapping passed to convertInputGradeToPoints for a letter-based gb");
				  }

				  if(mapping.getGradeMap() != null)
				  {
					  percentage = mapping.getValue(grade);
					  if(percentage == null)
					  {
						  throw new IllegalArgumentException("Invalid grade passed to convertInputGradeToPoints");
					  }
				  }
			  } else {
				  try {
					  percentage = Double.parseDouble(grade);
				  } catch (NumberFormatException nfe) {
					  throw new IllegalArgumentException("Invalid % grade passed to convertInputGradeToPoints");
				  }
			  }

			  convertedValue = calculateEquivalentPointValueForPercent(gbItemPointsPossible, percentage);

		  } else {
			  throw new InvalidGradeException("invalid grade entry type passed to convertInputGradeToPoints");
		  }
	  }

	  return convertedValue;
  }
  
	private List getTotalPointsEarnedInternalFixing(final Long gradebookId, final String studentId, final Session session, final Gradebook gradebook, final List categories) 
	{
  	double totalPointsEarned = 0;
  	double literalTotalPointsEarned = 0;
  	Iterator scoresIter = session.createQuery(
  			"select agr.pointsEarned, asn from AssignmentGradeRecord agr, Assignment asn where agr.gradableObject=asn and agr.studentId=:student and asn.gradebook.id=:gbid and asn.removed=false and asn.pointsPossible > 0").
  			setParameter("student", studentId).
  			setParameter("gbid", gradebookId).
  			list().iterator();

  	List assgnsList = session.createQuery(
  	"from Assignment as asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
  	setParameter("gbid", gradebookId).
  	list();

  	Map cateScoreMap = new HashMap();
  	Map cateTotalScoreMap = new HashMap();

  	Set assignmentsTaken = new HashSet();
  	while (scoresIter.hasNext()) {
  		Object[] returned = (Object[])scoresIter.next();
  		Double pointsEarned = (Double)returned[0]; 
  		Assignment go = (Assignment) returned[1];  		
  		if (go.isCounted() && pointsEarned != null) {
  			Double fixingPointsEarned = fixingPointsEarned(pointsEarned, go, gradebook);
  			if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
  			{
  				totalPointsEarned += fixingPointsEarned.doubleValue();
  				literalTotalPointsEarned += fixingPointsEarned.doubleValue();
  				assignmentsTaken.add(go.getId());
  			}
  			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
  			{
  				totalPointsEarned += fixingPointsEarned.doubleValue();
  				literalTotalPointsEarned += fixingPointsEarned.doubleValue();
  				assignmentsTaken.add(go.getId());
  			}
  			else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
  			{
  				for(int i=0; i<categories.size(); i++)
  				{
  					Category cate = (Category) categories.get(i);
  					if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
  					{
  						assignmentsTaken.add(go.getId());
  						literalTotalPointsEarned += fixingPointsEarned.doubleValue();
  						if(cateScoreMap.get(cate.getId()) != null)
  						{
  							cateScoreMap.put(cate.getId(), Double.valueOf(((Double)cateScoreMap.get(cate.getId())).doubleValue() + fixingPointsEarned.doubleValue()));
  						}
  						else
  						{
  							cateScoreMap.put(cate.getId(), Double.valueOf(fixingPointsEarned));
  						}
  						break;
  					}
  				}
  			}
  		}
  	}

  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
  	{
  		Iterator assgnsIter = assgnsList.iterator();
  		while (assgnsIter.hasNext()) 
  		{
  			Assignment asgn = (Assignment)assgnsIter.next();
  			if(assignmentsTaken.contains(asgn.getId()))
  			{
  				for(int i=0; i<categories.size(); i++)
  				{
  					Category cate = (Category) categories.get(i);
  					if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()))
  					{
  						if(cateTotalScoreMap.get(cate.getId()) == null)
  						{
  							cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
  						}
  						else
  						{
  							cateTotalScoreMap.put(cate.getId(), Double.valueOf(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));
  						}
  					}
  				}
  			}
  		}
  	}

  	if(assignmentsTaken.isEmpty())
  		totalPointsEarned = -1;

  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
  	{
  		for(int i=0; i<categories.size(); i++)
  		{
  			Category cate = (Category) categories.get(i);
  			if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
  			{
  				totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
  			}
  		}
  	}

  	if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
  	List returnList = new ArrayList();
  	returnList.add(Double.valueOf(totalPointsEarned));
  	returnList.add(Double.valueOf(literalTotalPointsEarned));
  	return returnList;
	}
	
	private Double fixingPointsEarned(Double pointsEarned, Assignment assignment, Gradebook gradebook)
	{
  	Double pointPossible = assignment.getPointsPossible();
  	String letterGrade;
  	if(pointPossible != null && pointPossible.doubleValue() > 0)
  	{
  		LetterGradePercentMapping lgpm = getLetterGradePercentMapping(assignment.getGradebook());
  		letterGrade = lgpm.getGrade(calculateEquivalentPercent(pointPossible, pointsEarned));
  		
  		GradeMapping gradeMap = gradebook.getSelectedGradeMapping();

  		if(gradeMap != null)
  		{
  			Double rightPercent = gradeMap.getValue(letterGrade);
  			BigDecimal rightPercentBD = new BigDecimal(rightPercent);
  			BigDecimal pointPossibleBD = new BigDecimal(pointPossible);
  			
				return Double.valueOf(rightPercentBD.multiply(pointPossibleBD).divide(new BigDecimal(Double.valueOf("100.0"))).doubleValue());
  		}
  	}
  	return null;
	}
	
	public Map getFixedGrade(String gradebookUid)
	{
		HashMap returnMap = new HashMap();

		try
		{
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map enrollmentMap;
			String userUid = authn.getUserUid();
			
			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			enrollmentMap = new HashMap();

			Map enrollmentMapUid = new HashMap();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecordsFixing(courseGrade, enrollmentMap.keySet());
			ArrayList grades = new ArrayList();
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					if(!nonAssignment)
						returnMap.put(enr.getUser().getDisplayId(), (String)gradeMap.getGrade(gradeRecord.getNonNullAutoCalculatedGrade()));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnMap;
	}
	
	public List getPointsEarnedCourseGradeRecordsFixing(final CourseGrade courseGrade, final Collection studentUids) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				if(studentUids == null || studentUids.size() == 0) {
					if(log.isInfoEnabled()) log.info("Returning no grade records for an empty collection of student UIDs");
					return new ArrayList();
				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

				Long gradebookId = courseGrade.getGradebook().getId();
				Gradebook gradebook = getGradebook(gradebookId);
				List cates = getCategories(gradebookId);
				//double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
				//if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				for(Iterator iter = records.iterator(); iter.hasNext();) {
					CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
					//double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
					List totalEarned = getTotalPointsEarnedInternalFixing(gradebookId, cgr.getStudentId(), session, gradebook, cates);
					double totalPointsEarned = ((Double)totalEarned.get(0)).doubleValue();
					double literalTotalPointsEarned = ((Double)totalEarned.get(1)).doubleValue();
					double totalPointsPossible = getTotalPointsInternal(gradebookId, session, gradebook, cates, cgr.getStudentId());
					cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
					if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
				}

				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}
	
	public Map getFixedPoint(String gradebookUid)
	{
		HashMap returnMap = new HashMap();

		try
		{
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map enrollmentMap;
			String userUid = authn.getUserUid();
			
			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			enrollmentMap = new HashMap();

			Map enrollmentMapUid = new HashMap();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecordsFixing(courseGrade, enrollmentMap.keySet());
			ArrayList grades = new ArrayList();
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					if(!nonAssignment)
						returnMap.put(enr.getUser().getDisplayId(), gradeRecord.getNonNullAutoCalculatedGrade().toString());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnMap;
	}

	public Map getOldPoint(String gradebookUid)
	{
		HashMap returnMap = new HashMap();

		try
		{
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map enrollmentMap;
			String userUid = authn.getUserUid();
			
			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			enrollmentMap = new HashMap();

			Map enrollmentMapUid = new HashMap();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet());
			ArrayList grades = new ArrayList();
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					if(!nonAssignment)
						returnMap.put(enr.getUser().getDisplayId(), gradeRecord.getNonNullAutoCalculatedGrade().toString());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnMap;
	}
	
	public int getGradeEntryType(String gradebookUid) {
		if (gradebookUid == null) {
			throw new IllegalArgumentException("null gradebookUid passed to getGradeEntryType");
		}
		
		try {
			Gradebook gradebook = getGradebook(gradebookUid);
			return gradebook.getGrade_type();
		} catch (GradebookNotFoundException gnfe) {
			throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + gradebookUid);
		}
	}

	
	public Map getEnteredCourseGrade(final String gradebookUid)
	{
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Gradebook thisGradebook = getGradebook(gradebookUid);

				Long gradebookId = thisGradebook.getId();
				CourseGrade courseGrade = getCourseGrade(gradebookId);

				Map enrollmentMap;
				String userUid = authn.getUserUid();

				Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
				enrollmentMap = new HashMap();

				Map enrollmentMapUid = new HashMap();
				for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
				{
					EnrollmentRecord enr = (EnrollmentRecord)iter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
					enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), enrollmentMap.keySet());

				Map returnMap = new HashMap();

				for(int i=0; i<records.size(); i++)
				{
					CourseGradeRecord cgr = (CourseGradeRecord) records.get(i);
					if(cgr.getEnteredGrade() != null && !cgr.getEnteredGrade().equalsIgnoreCase(""))
					{		
						EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(cgr.getStudentId());
						if(enr != null)
						{
							returnMap.put(enr.getUser().getDisplayId(), cgr.getEnteredGrade());
						}
					}
				}

				return returnMap;
			}
		};
		return (Map)getHibernateTemplate().execute(hc);		
	}
	
	public Map getCalculatedCourseGrade(String gradebookUid)
	{
		HashMap returnMap = new HashMap();

		try
		{
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map enrollmentMap;
			String userUid = authn.getUserUid();
			
			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			enrollmentMap = new HashMap();

			Map enrollmentMapUid = new HashMap();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet());
			ArrayList grades = new ArrayList();
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					if(!nonAssignment)
						returnMap.put(enr.getUser().getDisplayId(), (String)gradeMap.getGrade(gradeRecord.getNonNullAutoCalculatedGrade()));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnMap;
	}

  public String getAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid) 
  throws GradebookNotFoundException, AssessmentNotFoundException
  {
  	final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);

  	Double assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
  			if (assignment == null) {
  				throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
  			}

  			if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
  				log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + assignmentName);
  				throw new SecurityException("You do not have permission to perform this operation");
  			}

  			// If this is the student, then the assignment needs to have
  			// been released.
  			if (studentRequestingOwnScore && !assignment.isReleased()) {
  				log.error("AUTHORIZATION FAILURE: Student " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve score for unreleased assignment " + assignment.getName());
  				throw new SecurityException("You do not have permission to perform this operation");					
  			}

  			AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
  			if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
  			if (gradeRecord == null) {
  				return null;
  			} else {
  				return gradeRecord.getPointsEarned();
  			}
  		}
  	});
  	if (log.isDebugEnabled()) log.debug("returning " + assignmentScore);
  	
  	//TODO: when ungraded items is considered, change column to ungraded-grade 
  	//its possible that the assignment score is null
  	if (assignmentScore == null)
  		return null;
  	
  	return Double.valueOf(assignmentScore).toString();
  }

  public String getAssignmentScoreString(final String gradebookUid, final Long gbItemId, String studentUid) 
  throws GradebookNotFoundException, AssessmentNotFoundException
  {
		if (gradebookUid == null || gbItemId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScore");
		}
		
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, gbItemId, session);
			}
		});
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with the gbItemId " + gbItemId);
		}
		
		return getAssignmentScoreString(gradebookUid, assignment.getName(), studentUid);
  }

	public void setAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid, final String score, final String clientServiceDescription) 
	throws GradebookNotFoundException, AssessmentNotFoundException 
	{
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade externally maintained assignment " + assignmentName + " from " + clientServiceDescription);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				if (!isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade student " + studentUid + " from " + clientServiceDescription + " for item " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				Date now = new Date();
				String graderId = getAuthn().getUserUid();
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
				if (gradeRecord == null) {
					// Creating a new grade record.
					gradeRecord = new AssignmentGradeRecord(assignment, studentUid, Double.valueOf(score));
					//TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
				} else {
					//TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
					gradeRecord.setPointsEarned(Double.valueOf(score));
				}
				gradeRecord.setGraderId(graderId);
				gradeRecord.setDateRecorded(now);
				session.saveOrUpdate(gradeRecord);
				
				session.save(new GradingEvent(assignment, graderId, studentUid, score));
				
				// Sync database.
				session.flush();
				session.clear();
				return null;
			}
		});

		if (log.isInfoEnabled()) log.info("Score updated in gradebookUid=" + gradebookUid + ", assignmentName=" + assignmentName + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
	}

	public void finalizeGrades(String gradebookUid)
			throws GradebookNotFoundException {
		if (!getAuthz().isUserAbleToGradeAll(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to finalize grades");
			throw new SecurityException("You do not have permission to perform this operation");
		}
		finalizeNullGradeRecords(getGradebook(gradebookUid));
	}
	
	public String getLowestPossibleGradeForGbItem(final String gradebookUid, final Long gradebookItemId) {
	    if (gradebookUid == null || gradebookItemId == null) {
	        throw new IllegalArgumentException("Null gradebookUid and/or gradebookItemId " +
	        		"passed to getLowestPossibleGradeForGbItem. gradebookUid:" + 
	        		gradebookUid + " gradebookItemId:" + gradebookItemId);
	    }
	    
	    Assignment gbItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                return getAssignmentWithoutStats(gradebookUid, gradebookItemId, session);
            }
        });
	    
	    if (gbItem == null) {
	        throw new AssessmentNotFoundException("No gradebook item found with id " + gradebookItemId);
	    }
	    
	    Gradebook gradebook = gbItem.getGradebook();
	    
	    // double check that user has some permission to access gb items in this site
	    if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
	        throw new SecurityException("User attempted to access gradebookItem: " + 
	                gradebookItemId + " in gradebook:" + gradebookUid + " without permission!");
	    }
	    
	    String lowestPossibleGrade = null;
	    
	    if (gbItem.getUngraded()) {
	        lowestPossibleGrade = null;
	    } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE || 
	            gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
	        lowestPossibleGrade = "0";
	    } else if (gbItem.getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
	        LetterGradePercentMapping mapping = getLetterGradePercentMapping(gradebook);
	        lowestPossibleGrade = mapping.getGrade(0d);
	    }
	    
	    return lowestPossibleGrade;
	}
	
	public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid) {
	    if (gradebookUid == null) {
	        throw new IllegalArgumentException("Null gradebookUid passed to getCategoryDefinitions");
	    }

	    if (!isUserAbleToViewAssignments(gradebookUid)) {
	        log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve all categories without permission");
	        throw new SecurityException("You do not have permission to perform this operation");
	    }

	    List<CategoryDefinition> categoryDefList = new ArrayList<CategoryDefinition>();

	    List<Category> gbCategories = getCategories(getGradebook(gradebookUid).getId());
	    if (gbCategories != null) {
	        for (Category category : gbCategories) {
	            categoryDefList.add(getCategoryDefinition(category));
	        }
	    }

	    return categoryDefList;
	}

	private CategoryDefinition getCategoryDefinition(Category category) {
	    CategoryDefinition categoryDef = new CategoryDefinition();
	    if (category != null) {
	        categoryDef.setId(category.getId());
	        categoryDef.setName(category.getName());
	        categoryDef.setWeight(category.getWeight());
	    }

	    return categoryDef;
	}
}