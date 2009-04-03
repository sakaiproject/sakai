/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation, The MIT Corporation
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
package org.sakaiproject.tool.gradebook.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.gradebook.GradebookDefinition;
import org.sakaiproject.component.gradebook.VersionedExternalizable;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * Test the service methods which interact with internally maintained data.
 * These methods have more complex authorization requirements.
 */
public class GradebookServiceNewTest extends GradebookTestBase {

	private static final Log log = LogFactory.getLog(GradebookServiceNewTest.class);

	private static final String GRADEBOOK_UID_NO_CAT = "gradebookNoCategories";
	private static final String ASN_TITLE1 = "Assignment #1";
	private static final String ASN_TITLE2 = "Assignment #2";
	private static final String INSTRUCTOR_UID = "Inst-1";
	private static final String TA_UID = "TA-1";
	private static final String TA_NO_SECT_UID = "TA-2";
	private static final String SECTION_NAME = "Lab 01";
	private static final String STUDENT_IN_SECTION_UID1 = "StudentInLab1";
	private static final String STUDENT_NOT_IN_SECTION_UID1 = "StudentNotInLab1";
	private static final Double ASN_POINTS1 = new Double(40.0);
	private static final Double ASN_POINTS2 = new Double(60.0);
	private Long asn1IdNoCat;
	private Long asn2IdNoCat;
	
	private static final String GRADEBOOK_UID_WITH_CAT = "gradebookWithCategories";
	private static final String STUDENT_IN_SECTION_UID2 = "StudentInLab2";
	private static final String STUDENT_NOT_IN_SECTION_UID2 = "StudentNotInLab2";
	private Long asn1IdWithCat;
	private Long asn2IdWithCat;
	private String section2Uid;
	private Long cat1Id;
	private Long cat2Id;
	private static final String CAT1_NAME = "Category 1";
	private static final String CAT2_NAME = "Category 2";
	
	

	/**
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
	 */
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		
		userManager.createUser(INSTRUCTOR_UID, null, null, null);
		userManager.createUser(TA_UID, null, null, null);
		userManager.createUser(TA_NO_SECT_UID, null, null, null);
		
		// set up gb with no categories /////////////////////
		gradebookFrameworkService.addGradebook(GRADEBOOK_UID_NO_CAT, GRADEBOOK_UID_NO_CAT);
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);

		// Set up users, enrollments, teaching assignments, and sections.
		Course courseSite1 = integrationSupport.createCourse(GRADEBOOK_UID_NO_CAT, GRADEBOOK_UID_NO_CAT, false, false, false);
		addUsersEnrollments(gradebookNoCat, Arrays.asList(new String[] {STUDENT_IN_SECTION_UID1, STUDENT_NOT_IN_SECTION_UID1}));
		
		integrationSupport.addSiteMembership(INSTRUCTOR_UID, GRADEBOOK_UID_NO_CAT, Role.INSTRUCTOR);
		
		integrationSupport.addSiteMembership(TA_UID, GRADEBOOK_UID_NO_CAT, Role.TA);
		integrationSupport.addSiteMembership(TA_NO_SECT_UID, GRADEBOOK_UID_NO_CAT, Role.TA);
		List sectionCategories1 = sectionAwareness.getSectionCategories(GRADEBOOK_UID_NO_CAT);
		CourseSection section1 = integrationSupport.createSection(courseSite1.getUuid(), SECTION_NAME,
				(String)sectionCategories1.get(0),
				new Integer(40), null, null, null, true, false, true,  false, false, false, false);
		integrationSupport.addSectionMembership(STUDENT_IN_SECTION_UID1, section1.getUuid(), Role.STUDENT);
		integrationSupport.addSectionMembership(TA_UID, section1.getUuid(), Role.TA);

		// Add internal assignments
		asn1IdNoCat = gradebookManager.createAssignment(gradebookNoCat.getId(), ASN_TITLE1, ASN_POINTS1, new Date(), Boolean.FALSE,Boolean.FALSE);
		asn2IdNoCat = gradebookManager.createAssignment(gradebookNoCat.getId(), ASN_TITLE2, ASN_POINTS2, new Date(), Boolean.FALSE,Boolean.FALSE);
		
		// set up gb with categories ///////////////////
		gradebookFrameworkService.addGradebook(GRADEBOOK_UID_WITH_CAT, GRADEBOOK_UID_WITH_CAT);
		Gradebook gradebookWithCat = gradebookManager.getGradebook(GRADEBOOK_UID_WITH_CAT);
		gradebookWithCat.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(gradebookWithCat);

		// Set up users, enrollments, teaching assignments, and sections.
		Course courseSite2 = integrationSupport.createCourse(GRADEBOOK_UID_WITH_CAT, GRADEBOOK_UID_WITH_CAT, false, false, false);
		addUsersEnrollments(gradebookWithCat, Arrays.asList(new String[] {STUDENT_IN_SECTION_UID2, STUDENT_NOT_IN_SECTION_UID2}));
		
		integrationSupport.addSiteMembership(INSTRUCTOR_UID, GRADEBOOK_UID_WITH_CAT, Role.INSTRUCTOR);
		integrationSupport.addSiteMembership(TA_UID, GRADEBOOK_UID_WITH_CAT, Role.TA);
		integrationSupport.addSiteMembership(TA_NO_SECT_UID, GRADEBOOK_UID_WITH_CAT, Role.TA);
		
		List sectionCategories2 = sectionAwareness.getSectionCategories(GRADEBOOK_UID_WITH_CAT);
		CourseSection section2 = integrationSupport.createSection(courseSite2.getUuid(), SECTION_NAME,
				(String)sectionCategories2.get(0),
				new Integer(40), null, null, null, true, false, true,  false, false, false, false);
		section2Uid = section2.getUuid();
		
		integrationSupport.addSectionMembership(STUDENT_IN_SECTION_UID2, section2.getUuid(), Role.STUDENT);
		integrationSupport.addSectionMembership(TA_UID, section2.getUuid(), Role.TA);

		// Add an internal assignment.
		cat1Id = gradebookManager.createCategory(gradebookWithCat.getId(), CAT1_NAME, new Double(0), 0);
		asn1IdWithCat = gradebookManager.createAssignmentForCategory(gradebookWithCat.getId(), cat1Id, ASN_TITLE1, ASN_POINTS1, new Date(), Boolean.FALSE,Boolean.FALSE);
		
		cat2Id = gradebookManager.createCategory(gradebookWithCat.getId(), CAT2_NAME, new Double(0), 0);
		asn2IdWithCat = gradebookManager.createAssignmentForCategory(gradebookWithCat.getId(), cat2Id, ASN_TITLE2, ASN_POINTS2, new Date(), Boolean.FALSE,Boolean.FALSE);

	}

	public void testGetCategory() throws Exception{
		String className = this.getClass().getName();
		gradebookFrameworkService.addGradebook(className, className);
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Long cate1Long = gradebookManager.createCategory(persistentGradebook.getId(), "cate 1", new Double(0.40), 0);
		Long cate2Long = gradebookManager.createCategory(persistentGradebook.getId(), "cate 2", new Double(0.60), 0);

		List list = (List) gradebookManager.getCategories(persistentGradebook.getId());

		for(int i=0; i<list.size(); i++)
		{
			Category cat = (Category) list.get(i);
			if(i == 0)
			{
				Long assgn1Long = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
						cat.getName() + "_assignment_1", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
			}
			if(i == 1)
			{
				Long assgn3Long = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
						cat.getName() + "_assignment_1", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
			}
			Long assign2 = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
					cat.getName() + "_assignment_2", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
		}

		Category cat1 = gradebookManager.getCategory(cate1Long);
		Category cat2 = gradebookManager.getCategory(cate2Long);
//		System.out.println(cat1 + "---" + cat1.getName());
//		System.out.println(cat2 + "---" + cat2.getName());
	}

	public void testStudentRebuff() throws Exception {
		setAuthnId(INSTRUCTOR_UID);

		// Score the unreleased assignment.
		gradebookService.setAssignmentScoreString(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_IN_SECTION_UID1, new String("39"), "Service Test");

		// Try to get a list of assignments as the student.
		setAuthnId(STUDENT_IN_SECTION_UID1);
		try {
			if (log.isInfoEnabled()) log.info("Ignore the upcoming authorization errors...");
			gradebookService.getAssignments(GRADEBOOK_UID_NO_CAT);
			fail();
		} catch (SecurityException e) {
		}

		// And then try to get the score.
		Double score;
		try {
			score = new Double(gradebookService.getAssignmentScoreString(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1));
			fail();
		} catch (SecurityException e) {
		}

		// try to get grades for students for item
		List grades;
		try {
			List studentIds = new ArrayList();
			studentIds.add(STUDENT_IN_SECTION_UID1);
			studentIds.add(STUDENT_NOT_IN_SECTION_UID1);
			grades = gradebookService.getGradesForStudentsForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, studentIds);
		} catch (SecurityException e) {
		}

		// try to get gradeDef for student for item - should return null if not released
		GradeDefinition gradeDef;
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(gradeDef.getGrade() == null);

		// Now release the assignment.
		setAuthnId(INSTRUCTOR_UID);
		org.sakaiproject.tool.gradebook.Assignment assignment = gradebookManager.getAssignment(asn1IdNoCat);
		assignment.setReleased(true);
		gradebookManager.updateAssignment(assignment);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);

		// Now see if the student gets lucky.
		setAuthnId(STUDENT_IN_SECTION_UID1);
		score = new Double(gradebookService.getAssignmentScoreString(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1));
		Assert.assertTrue(score.doubleValue() == 39.0);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(gradeDef.getGrade().equals((new Double(39).toString())));
		
		setAuthnId(STUDENT_IN_SECTION_UID1);

		Map viewableStudents = gradebookService.getViewableStudentsForItemForCurrentUser(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
		assertEquals(0, viewableStudents.size());
	}
	
	public void testGetGradeDefinitionForStudentForItem() throws Exception {
		setAuthnId(INSTRUCTOR_UID);
		
		// start with a points-based gb
		GradeDefinition gradeDef = 
			gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(gradeDef != null);
		Assert.assertEquals(GradebookService.GRADE_TYPE_POINTS, gradeDef.getGradeEntryType());
		Assert.assertEquals(STUDENT_IN_SECTION_UID1, gradeDef.getStudentUid());
		Assert.assertTrue(gradeDef.isGradeReleased() == false);
		Assert.assertNull(gradeDef.getGrade());
		Assert.assertNull(gradeDef.getGraderUid());
		Assert.assertNull(gradeDef.getDateRecorded());

		String graderComment = "grader comment";
		gradebookService.setAssignmentScoreString(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_IN_SECTION_UID1, new String("35"), "Service Test");
		gradebookService.setAssignmentScoreComment(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_IN_SECTION_UID1, graderComment);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(gradeDef != null);
		Assert.assertEquals(GradebookService.GRADE_TYPE_POINTS, gradeDef.getGradeEntryType());
		Assert.assertEquals(STUDENT_IN_SECTION_UID1, gradeDef.getStudentUid());
		Assert.assertTrue(gradeDef.isGradeReleased() == false);
		Assert.assertEquals("35.0", gradeDef.getGrade());
		Assert.assertEquals(INSTRUCTOR_UID, gradeDef.getGraderUid());
		Assert.assertEquals(graderComment, gradeDef.getGradeComment());
		Assert.assertNotNull(gradeDef.getDateRecorded());
		
		setAuthnId(STUDENT_IN_SECTION_UID1);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		// the grade is not released, so grade info should be null
		Assert.assertEquals(GradebookService.GRADE_TYPE_POINTS, gradeDef.getGradeEntryType());
		Assert.assertEquals(STUDENT_IN_SECTION_UID1, gradeDef.getStudentUid());
		Assert.assertTrue(gradeDef.isGradeReleased() == false);
		Assert.assertNull(gradeDef.getGrade()); // should be null if grade not released
		Assert.assertNull(gradeDef.getGradeComment()); // should be null if grade not released
		Assert.assertNull(gradeDef.getGraderUid());  // should be null if grade not released
		Assert.assertNull(gradeDef.getDateRecorded()); // should be null if grade not released
		
		// switch back to instructor and double check other grade entry types
		setAuthnId(INSTRUCTOR_UID);
		
		// %-based gradebook
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		gradebookManager.updateGradebook(gradebookNoCat);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertEquals("87.5", gradeDef.getGrade());
		Assert.assertEquals(graderComment, gradeDef.getGradeComment());
		Assert.assertEquals(GradebookService.GRADE_TYPE_PERCENTAGE, gradeDef.getGradeEntryType());
		
		// letter-based gradebook
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		gradebookManager.updateGradebook(gradebookNoCat);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);

		Assert.assertEquals("B+", gradeDef.getGrade());
		Assert.assertEquals(graderComment, gradeDef.getGradeComment());
		Assert.assertEquals(GradebookService.GRADE_TYPE_LETTER, gradeDef.getGradeEntryType());
		
		// the TA with standard grader perms should trigger exception for student
		// not in section
		setAuthnId(TA_UID);
		try {
			gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_NOT_IN_SECTION_UID1);
			Assert.fail();
		} catch (SecurityException se) {
			
		}
		
		// let's release the grade and make sure the student can access it
		setAuthnId(INSTRUCTOR_UID);
		org.sakaiproject.tool.gradebook.Assignment assignment = gradebookManager.getAssignment(asn1IdNoCat);
		assignment.setReleased(true);
		gradebookManager.updateAssignment(assignment);
		
		setAuthnId(STUDENT_IN_SECTION_UID1);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertEquals(GradebookService.GRADE_TYPE_LETTER, gradeDef.getGradeEntryType());
		Assert.assertEquals(STUDENT_IN_SECTION_UID1, gradeDef.getStudentUid());
		Assert.assertTrue(gradeDef.isGradeReleased());
		Assert.assertEquals("B+", gradeDef.getGrade());
        Assert.assertEquals(graderComment, gradeDef.getGradeComment());
		Assert.assertEquals(INSTRUCTOR_UID, gradeDef.getGraderUid());
		Assert.assertNotNull(gradeDef.getDateRecorded());
		
		// try to retrieve another student's score
		try {
		    gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID2);
		    fail("Did not catch a student trying to retrieve another student's grade info!");
		} catch (SecurityException se) {}
	}

	public void testAssignmentScoreComment() throws Exception {
		setAuthnId(INSTRUCTOR_UID);

		// Comment on a student score as if the Gradebook application was doing it.
		org.sakaiproject.tool.gradebook.Assignment assignment = gradebookManager.getAssignment(asn1IdNoCat);
		List<AssignmentGradeRecord> gradeRecords = new ArrayList<AssignmentGradeRecord>();
		List<Comment> comments = Arrays.asList(new Comment[] {new Comment(STUDENT_IN_SECTION_UID1, "First comment", assignment)});
		gradebookManager.updateAssignmentGradesAndComments(assignment, gradeRecords, comments);

		// Make sure we don't get a comment for the student who doesn't have one.
		CommentDefinition commentDefinition = gradebookService.getAssignmentScoreComment(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_NOT_IN_SECTION_UID1);
		Assert.assertTrue(commentDefinition == null);
		CommentDefinition commentDefById = gradebookService.getAssignmentScoreComment(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_NOT_IN_SECTION_UID1);
		Assert.assertTrue(commentDefById == null);

		// Make sure we can retrieve the comment.
		commentDefinition = gradebookService.getAssignmentScoreComment(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(commentDefinition.getAssignmentName().equals(ASN_TITLE1));
		Assert.assertTrue(commentDefinition.getCommentText().equals("First comment"));
		Assert.assertTrue(commentDefinition.getGraderUid().equals(INSTRUCTOR_UID));
		Assert.assertTrue(commentDefinition.getStudentUid().equals(STUDENT_IN_SECTION_UID1));

		// Now change the comment.
		setAuthnId(TA_UID);
		gradebookService.setAssignmentScoreComment(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_IN_SECTION_UID1, "Second comment");
		commentDefinition = gradebookService.getAssignmentScoreComment(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(commentDefinition.getCommentText().equals("Second comment"));
		Assert.assertTrue(commentDefinition.getGraderUid().equals(TA_UID));
	}

	public void testPermissionChecks() throws Exception {
		// test the instructor
		setAuthnId(INSTRUCTOR_UID);
		Assert.assertTrue(gradebookService.currentUserHasGradeAllPerm(GRADEBOOK_UID_NO_CAT));
		Assert.assertTrue(gradebookService.currentUserHasEditPerm(GRADEBOOK_UID_NO_CAT));
		Assert.assertTrue(gradebookService.currentUserHasGradingPerm(GRADEBOOK_UID_NO_CAT));

		// test the TA
		setAuthnId(TA_UID);
		Assert.assertTrue(gradebookService.currentUserHasGradingPerm(GRADEBOOK_UID_NO_CAT));
		Assert.assertFalse(gradebookService.currentUserHasGradeAllPerm(GRADEBOOK_UID_NO_CAT));
		Assert.assertFalse(gradebookService.currentUserHasEditPerm(GRADEBOOK_UID_NO_CAT));

		// test the student
		setAuthnId(STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(gradebookService.currentUserHasViewOwnGradesPerm(GRADEBOOK_UID_NO_CAT));
		Assert.assertFalse(gradebookService.currentUserHasEditPerm(GRADEBOOK_UID_NO_CAT));
		Assert.assertFalse(gradebookService.currentUserHasGradingPerm(GRADEBOOK_UID_NO_CAT));
		Assert.assertFalse(gradebookService.currentUserHasGradeAllPerm(GRADEBOOK_UID_NO_CAT));
	}

	public void testGradableObjectDefined() throws Exception {
		Assert.assertFalse(gradebookService.isGradableObjectDefined(new Long(15)));
		Assert.assertTrue(gradebookService.isGradableObjectDefined(asn1IdNoCat));
		
		try {
			gradebookService.isGradableObjectDefined(null);
			log.error("isGradableObjectDefined did not catch null gbObjectId");
			Assert.fail();
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testGetViewableAssignmentsForCurrentUser() throws Exception {
		// first test gb without grader permissions
		// test instructor 
		setAuthnId(INSTRUCTOR_UID);
		List viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(viewableAssignments.size() == 2);
		
		// ta 
		setAuthnId(TA_UID);
		viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(viewableAssignments.size() == 2);
		
		// student
		setAuthnId(STUDENT_IN_SECTION_UID1);
		// only returns released items
		viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(viewableAssignments.size() == 0);
		
		// now test gb with categories and grader permissions
		Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID_WITH_CAT);
		// ta may only grade one category, so only 1 assignment
		gradebookManager.addPermission(gb.getId(), TA_UID, GradebookService.gradePermission, cat1Id, section2Uid);
		
		setAuthnId(INSTRUCTOR_UID);
		viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_WITH_CAT);
		Assert.assertTrue(viewableAssignments.size() == 2);
		
		setAuthnId(TA_UID);
		viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_WITH_CAT);
		Assert.assertTrue(viewableAssignments.size() == 1);
		
		setAuthnId(STUDENT_IN_SECTION_UID2);
		// only returns released assign
		viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_WITH_CAT);
		Assert.assertTrue(viewableAssignments.size() == 0);
	}
	
	public void testGetViewableSectionUuidToNameMap() throws Exception {
		setAuthnId(INSTRUCTOR_UID);
		Map sectionUuidNameMap = gradebookService.getViewableSectionUuidToNameMap(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(sectionUuidNameMap.size() == 1);
		
		setAuthnId(TA_UID);
		sectionUuidNameMap = gradebookService.getViewableSectionUuidToNameMap(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(sectionUuidNameMap.size() == 1);
		
		setAuthnId(TA_NO_SECT_UID);
		sectionUuidNameMap = gradebookService.getViewableSectionUuidToNameMap(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(sectionUuidNameMap.size() == 0);
		
		setAuthnId(STUDENT_IN_SECTION_UID1);
		sectionUuidNameMap = gradebookService.getViewableSectionUuidToNameMap(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(sectionUuidNameMap.size() == 0);
		
		// now double check with grader permissions
		// now test gb with categories and grader permissions
		Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID_WITH_CAT);
		// this ta is not a member of a section, but grader perms will allow viewing section
		gradebookManager.addPermission(gb.getId(), TA_NO_SECT_UID, GradebookService.gradePermission, null, section2Uid);
		setAuthnId(TA_NO_SECT_UID);
		sectionUuidNameMap = gradebookService.getViewableSectionUuidToNameMap(GRADEBOOK_UID_WITH_CAT);
		Assert.assertTrue(sectionUuidNameMap.size() == 1);
		
	}
	
	public void testGetViewableStudentsForItemForCurrentUser() throws Exception {
		// testing w/o grader permissions
		setAuthnId(INSTRUCTOR_UID);
		Map viewableStudentsMap = gradebookService.getViewableStudentsForItemForCurrentUser(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
		Assert.assertEquals(2, viewableStudentsMap.size());
		for (Iterator stIter = viewableStudentsMap.keySet().iterator(); stIter.hasNext();) {
			String studentId = (String) stIter.next();
			Assert.assertEquals(GradebookService.gradePermission, (String)viewableStudentsMap.get(studentId));
		}
		
		setAuthnId(TA_NO_SECT_UID);
		viewableStudentsMap = gradebookService.getViewableStudentsForItemForCurrentUser(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
		Assert.assertTrue(viewableStudentsMap.size() == 0);
		
		setAuthnId(TA_UID);
		viewableStudentsMap = gradebookService.getViewableStudentsForItemForCurrentUser(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
		Assert.assertTrue(viewableStudentsMap.size() == 1);
		Assert.assertEquals(GradebookService.gradePermission, (String)viewableStudentsMap.get(STUDENT_IN_SECTION_UID1));
		
		// now test w/ grader perms
		Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID_WITH_CAT);
		// this ta is not a member of a section, but grader perms will allow viewing section
		gradebookManager.addPermission(gb.getId(), TA_NO_SECT_UID, GradebookService.viewPermission, null, section2Uid);
		
		setAuthnId(TA_NO_SECT_UID);
		viewableStudentsMap = gradebookService.getViewableStudentsForItemForCurrentUser(GRADEBOOK_UID_WITH_CAT, asn1IdWithCat);
		Assert.assertTrue(viewableStudentsMap.size() == 1);
		Assert.assertEquals(GradebookService.viewPermission, (String)viewableStudentsMap.get(STUDENT_IN_SECTION_UID2));
	}
	
	public void testGetViewableStudentsForItemForUser() throws Exception {
        // testing w/o grader permissions
        Map viewableStudentsMap = gradebookService.getViewableStudentsForItemForUser(INSTRUCTOR_UID, GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
        Assert.assertEquals(2, viewableStudentsMap.size());
        for (Iterator stIter = viewableStudentsMap.keySet().iterator(); stIter.hasNext();) {
            String studentId = (String) stIter.next();
            Assert.assertEquals(GradebookService.gradePermission, (String)viewableStudentsMap.get(studentId));
        }
        
        viewableStudentsMap = gradebookService.getViewableStudentsForItemForUser(TA_NO_SECT_UID, GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
        Assert.assertEquals(0, viewableStudentsMap.size());

        viewableStudentsMap = gradebookService.getViewableStudentsForItemForUser(TA_UID, GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
        Assert.assertEquals(1, viewableStudentsMap.size());
        Assert.assertEquals(GradebookService.gradePermission, (String)viewableStudentsMap.get(STUDENT_IN_SECTION_UID1));
        
        // now test w/ grader perms
        Gradebook gb = gradebookManager.getGradebook(GRADEBOOK_UID_WITH_CAT);
        // this ta is not a member of a section, but grader perms will allow viewing section
        gradebookManager.addPermission(gb.getId(), TA_NO_SECT_UID, GradebookService.viewPermission, null, section2Uid);

        viewableStudentsMap = gradebookService.getViewableStudentsForItemForUser(TA_NO_SECT_UID, GRADEBOOK_UID_WITH_CAT, asn1IdWithCat);
        Assert.assertEquals(1, viewableStudentsMap.size());
        Assert.assertEquals(GradebookService.viewPermission, (String)viewableStudentsMap.get(STUDENT_IN_SECTION_UID2));
    }
	
	public void testGetGradesForStudentsForItem() throws Exception {
		setAuthnId(INSTRUCTOR_UID);
		Map studentIdFunctionMap = 
			gradebookService.getViewableStudentsForItemForCurrentUser(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
		List studentIds = new ArrayList(studentIdFunctionMap.keySet());
		
		List gradeDefs = gradebookService.getGradesForStudentsForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, studentIds);
		Assert.assertNotNull(gradeDefs);
		Assert.assertTrue(gradeDefs.size() == 0);
		
		// add a score to the gb
		gradebookService.setAssignmentScoreString(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_IN_SECTION_UID1, new String("35"), "Service Test");
		gradeDefs = gradebookService.getGradesForStudentsForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, studentIds);
		Assert.assertEquals(1, gradeDefs.size());
		GradeDefinition gradeForS1 = (GradeDefinition)gradeDefs.get(0);
		Assert.assertTrue(gradeForS1 != null);
		Assert.assertEquals(GradebookService.GRADE_TYPE_POINTS, gradeForS1.getGradeEntryType());
		Assert.assertEquals(STUDENT_IN_SECTION_UID1, gradeForS1.getStudentUid());
		Assert.assertTrue(gradeForS1.isGradeReleased() == false);
		Assert.assertEquals("35.0", gradeForS1.getGrade());
		Assert.assertEquals(INSTRUCTOR_UID, gradeForS1.getGraderUid());
		Assert.assertNotNull(gradeForS1.getDateRecorded());
		
		// add another score to gb
		gradebookService.setAssignmentScoreString(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_NOT_IN_SECTION_UID1, new String("40"), "Service Test");
		gradeDefs = gradebookService.getGradesForStudentsForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, studentIds);
		Assert.assertTrue(gradeDefs.size() == 2);
		
		setAuthnId(TA_UID);
		//should throw SecurityException b/c trying to get one student that user
		// is not authorized to view
		try {
			gradeDefs = gradebookService.getGradesForStudentsForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, studentIds);
			Assert.fail();
		} catch (SecurityException e) {
			
		}
		
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		
		// try with the auth students
		List studentInSection = new ArrayList();
		studentInSection.add(STUDENT_IN_SECTION_UID1);
		gradeDefs = gradebookService.getGradesForStudentsForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, studentInSection);
		Assert.assertTrue(gradeDefs.size() == 1);
		gradeForS1 = (GradeDefinition)gradeDefs.get(0);
		Assert.assertTrue(gradeForS1 != null);
		Assert.assertEquals(GradebookService.GRADE_TYPE_PERCENTAGE, gradeForS1.getGradeEntryType());
		Assert.assertEquals(STUDENT_IN_SECTION_UID1, gradeForS1.getStudentUid());
		Assert.assertTrue(gradeForS1.isGradeReleased() == false);
		Assert.assertEquals("87.5", gradeForS1.getGrade());
		Assert.assertEquals(INSTRUCTOR_UID, gradeForS1.getGraderUid());
		Assert.assertNotNull(gradeForS1.getDateRecorded());
		
		gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		gradeDefs = gradebookService.getGradesForStudentsForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, studentInSection);
		Assert.assertTrue(gradeDefs.size() == 1);
		gradeForS1 = (GradeDefinition)gradeDefs.get(0);
		Assert.assertEquals(GradebookService.GRADE_TYPE_LETTER, gradeForS1.getGradeEntryType());
		Assert.assertEquals("B+", gradeForS1.getGrade());

	}
	
	public void testGetAssignment() throws Exception {
		setAuthnId(INSTRUCTOR_UID);
		Assignment assnDef = gradebookService.getAssignment(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
		Assert.assertNotNull(assnDef);
		Assert.assertEquals(ASN_TITLE1, assnDef.getName());
		Assert.assertEquals(new Double(40), assnDef.getPoints()); 
		
		try {
			assnDef = gradebookService.getAssignment(GRADEBOOK_UID_NO_CAT, new Long(-1));
			Assert.fail();
		} catch (AssessmentNotFoundException anfe) {
			
		}
		
	}
	
	public void testIsGradeValid() throws Exception {
		// try a null gradebookUuid
		try {
			gradebookService.isGradeValid(null, null);
			fail("did not catch null gradebookUuid passed to isGradeValid");
		} catch (IllegalArgumentException iae) {}
		
		// let's start with a points-based gradebook
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		// null grades are valid
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, null));
		// try some positive point values
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "25.34"));
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "0"));
		// negative should fail
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "-1"));
		// more than 2 decimal places should fail
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "10.125"));
		// try non-numeric
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "A"));
		
		// switch to %-based gradebook
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		// null grades are valid
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, null));
		// try some positive point values
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "25.34"));
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "0"));
		// negative should fail
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "-1"));
		// more than 2 decimal places should fail
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "10.125"));
		// try non-numeric
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "A"));
		
		// switch to letter-based gradebook
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		gradebookManager.updateGradebook(gradebookNoCat);
		// null grades are valid
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, null));
		// try some point values
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "25.34"));
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "0"));
		// negative should fail
		assertFalse(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "-1"));
		// try some valid ones
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "A"));
		assertTrue(gradebookService.isGradeValid(GRADEBOOK_UID_NO_CAT, "c-"));
	}
	
	public void testIdentifyStudentsWithInvalidGrades() throws Exception {
		// try some null parameters
		try {
			gradebookService.identifyStudentsWithInvalidGrades(null, new HashMap<String, String>());
			fail("did not catch null gradebookUid passed to identifyStudentsWithInvalidGrades");
		} catch (IllegalArgumentException iae) {}
		
		// try a null map 
		List<String> invalidStudentIds = gradebookService.identifyStudentsWithInvalidGrades(GRADEBOOK_UID_NO_CAT, null);
		assertTrue(invalidStudentIds.isEmpty());
		
		//add some students and grades for a points-based gb
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		Map<String, String> studentIdGradeMap = new HashMap<String, String>();
		studentIdGradeMap.put(STUDENT_IN_SECTION_UID1, "15");
		studentIdGradeMap.put(STUDENT_IN_SECTION_UID2, "-1");  // invalid
		studentIdGradeMap.put(STUDENT_NOT_IN_SECTION_UID1, "A"); //invalid
		studentIdGradeMap.put(STUDENT_NOT_IN_SECTION_UID2, "25.67"); //valid//
		
		invalidStudentIds = gradebookService.identifyStudentsWithInvalidGrades(GRADEBOOK_UID_NO_CAT, studentIdGradeMap);
		assertEquals(2, invalidStudentIds.size());
		assertTrue(invalidStudentIds.contains(STUDENT_IN_SECTION_UID2));
		assertTrue(invalidStudentIds.contains(STUDENT_NOT_IN_SECTION_UID1));
		
		//use a %-based gradebook
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		gradebookManager.updateGradebook(gradebookNoCat);
		invalidStudentIds = gradebookService.identifyStudentsWithInvalidGrades(GRADEBOOK_UID_NO_CAT, studentIdGradeMap);
		assertEquals(2, invalidStudentIds.size());
		assertTrue(invalidStudentIds.contains(STUDENT_IN_SECTION_UID2));
		assertTrue(invalidStudentIds.contains(STUDENT_NOT_IN_SECTION_UID1));
		
		// use a letter-based gb
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		gradebookManager.updateGradebook(gradebookNoCat);
		invalidStudentIds = gradebookService.identifyStudentsWithInvalidGrades(GRADEBOOK_UID_NO_CAT, studentIdGradeMap);
		assertEquals(3, invalidStudentIds.size());
		assertTrue(invalidStudentIds.contains(STUDENT_IN_SECTION_UID1));
		assertTrue(invalidStudentIds.contains(STUDENT_IN_SECTION_UID2));
		assertTrue(invalidStudentIds.contains(STUDENT_NOT_IN_SECTION_UID2));
	}
	
	public void testSaveGradeAndCommentForStudent() throws Exception {
		// try some null params
		try {
			gradebookService.saveGradeAndCommentForStudent(null, new Long(1), STUDENT_IN_SECTION_UID1, "A", "Good work");
			fail("Did not catch null gradebookUuid passed to saveGradeAndCommentForStudent");
		} catch (IllegalArgumentException iae) {}
		
		try {
			gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, null, STUDENT_IN_SECTION_UID1, "A", "Good work");
			fail("Did not catch null gradableObjectId passed to saveGradeAndCommentForStudent");
		} catch (IllegalArgumentException iae) {}
		
		try {
			gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, new Long(1), null, "A", "Good work");
			fail("Did not catch null gradableObjectId passed to saveGradeAndCommentForStudent");
		} catch (IllegalArgumentException iae) {}
		
		// try invalid gradebookUuid
		try {
			gradebookService.saveGradeAndCommentForStudent("bogus!", asn1IdNoCat, STUDENT_IN_SECTION_UID1, null, null);
			fail("did not catch bogus gradebookUuid passed to saveGradeAndCommentForStudent");
		} catch (GradebookNotFoundException gnfe) {}
		
		// try invalid gradableObjectId
		try {
			gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, new Long(12345), STUDENT_IN_SECTION_UID1, null, null);
			fail("did not catch bogus gradableObjectId passed to saveGradeAndCommentForStudent");
		} catch (AssessmentNotFoundException anfe) {}
		
		// use point-based gb
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		// add a grade and comment for student
		String score = "15.0";
		String comment = "Good work";
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1, score, comment);
		
		// try to retrieve it again
		GradeDefinition gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		assertEquals(score, gradeDef.getGrade());
		assertEquals(comment, gradeDef.getGradeComment());
		
		// try an invalid grade
		try {
			gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1, "A", comment);
			fail("did not catch invalid grade passed to saveGradeAndCommentForStudent");
		} catch (InvalidGradeException ige) {}
		
		// try setting the score and comment to null
		score = null;
		comment = null;
		
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1, score, comment);
		
		// try to retrieve it again
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		assertNull(gradeDef.getGrade());
		assertNull(gradeDef.getGradeComment());
		
		// set just a comment
		comment = "Try again!";
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1, score, comment);
		
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		assertNull(gradeDef.getGrade());
		assertEquals(comment, gradeDef.getGradeComment());
		
		// try to update a student as a TA
		authn.setAuthnContext(TA_UID);
		// should only have auth to update student in her section
		score = "25.78";
		comment = "Jolly good show";
		
		gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1, score, comment);
		
		// try to retrieve it again
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		assertEquals(score, gradeDef.getGrade());
		assertEquals(comment, gradeDef.getGradeComment());
		
		// now try to update a student not in the section
		try {
			gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_NOT_IN_SECTION_UID1, score, comment);
			fail("did not catch ta trying to update score and comment w/o authorization!");
		} catch (SecurityException se) {}
		
		// make sure students don't have authorization
		authn.setAuthnContext(STUDENT_IN_SECTION_UID1);
		try {
			gradebookService.saveGradeAndCommentForStudent(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_NOT_IN_SECTION_UID1, score, comment);
			fail("did not catch student trying to update score and comment w/o authorization!");
		} catch (SecurityException se) {}
	}
	
	public void testSaveGradesAndComments() throws Exception {
		// try nulls
		try {
			gradebookService.saveGradesAndComments(null, asn1IdNoCat, new ArrayList<GradeDefinition>());
			fail("did not catch null gradebookUuid passed to saveGradesAndComments");
		} catch (IllegalArgumentException iae) {}
		
		try {
			gradebookService.saveGradesAndComments(GRADEBOOK_UID_NO_CAT, null, new ArrayList<GradeDefinition>());
			fail("did not catch null gradableObjectId passed to saveGradesAndComments");
		} catch (IllegalArgumentException iae) {}
		
		// try invalid gradebookUuid
		try {
			gradebookService.saveGradesAndComments("bogus!", asn1IdNoCat, new ArrayList<GradeDefinition>());
			fail("did not catch bogus gradebookUuid passed to saveGradesAndComments");
		} catch (GradebookNotFoundException gnfe) {}
		
		// try invalid gradableObjectId
		try {
			gradebookService.saveGradesAndComments(GRADEBOOK_UID_NO_CAT, new Long(12345), new ArrayList<GradeDefinition>());
			fail("did not catch bogus gradableObjectId passed to saveGradesAndComments");
		} catch (AssessmentNotFoundException anfe) {}
		
		authn.setAuthnContext(INSTRUCTOR_UID);
		
		// null gradeDef list shouldn't do anything
		gradebookService.saveGradesAndComments(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, null);
		
		// Add some grade defs
		String def1Grade = "10.5";
		String def1Comment = "Good work";
		String def2Grade = "";
		String def2Comment = "Turn this in";

		GradeDefinition def1 = new GradeDefinition();
		def1.setStudentUid(STUDENT_IN_SECTION_UID1);
		def1.setGrade(def1Grade);
		def1.setGradeComment(def1Comment);
		
		GradeDefinition def2 = new GradeDefinition();
		def2.setStudentUid(STUDENT_NOT_IN_SECTION_UID1);
		def2.setGrade(def2Grade);
		def2.setGradeComment(def2Comment);
		
		List<GradeDefinition> gradeDefList = new ArrayList<GradeDefinition>();
		gradeDefList.add(def1);
		gradeDefList.add(def2);
		
		// use point-based gb
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		gradebookService.saveGradesAndComments(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, gradeDefList);
		
		// now let's see if the change was successful
		// try to retrieve it again
		GradeDefinition gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		assertEquals(def1Grade, gradeDef.getGrade());
		assertEquals(def1Comment, gradeDef.getGradeComment());
		
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_NOT_IN_SECTION_UID1);
		assertNull(gradeDef.getGrade());
		assertEquals(def2Comment, gradeDef.getGradeComment());
		
		// let's try this as a TA - should throw SecurityException b/c not auth for STUDENT_NOT_IN_SECTION_UID1
		authn.setAuthnContext(TA_UID);
		try {
			gradebookService.saveGradesAndComments(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, gradeDefList);
			fail("did not catch TA trying to save grades for students w/o auth");
		} catch (SecurityException se) {}
		
		// if we remove def2, TA should be able to update
		gradeDefList = new ArrayList<GradeDefinition>();		
		def1Grade = "37.0";
		def1Comment = "";
		def1.setGrade(def1Grade);
		def1.setGradeComment(def1Comment);
		gradeDefList.add(def1);
		
		gradebookService.saveGradesAndComments(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, gradeDefList);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		assertEquals(def1Grade, gradeDef.getGrade());
		assertEquals(def1Comment, gradeDef.getGradeComment());
		
		// let's try an invalid grade
		gradeDefList = new ArrayList<GradeDefinition>();		
		def1.setGrade("A");
		def1.setGradeComment("I like letters");
		gradeDefList.add(def1);
		
		try {
			gradebookService.saveGradesAndComments(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, gradeDefList);
			fail("Did not catch invalid grade passed to saveGradesAndComments");
		} catch (InvalidGradeException ige) {}
		
		// double check grade wasn't updated
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		assertEquals(def1Grade, gradeDef.getGrade());
		assertEquals(def1Comment, gradeDef.getGradeComment());
	}
	
	public void testGetGradeEntryType() throws Exception {
		// try passing a null gradebookUid
		try {
			gradebookService.getGradeEntryType(null);
			fail("did not catch null gradebookUid passed to getGradeEntryType");
		} catch (IllegalArgumentException iae) {}
		
		// try a bogus gradebookUid
		try {
			gradebookService.getGradeEntryType("bogus");
			fail("did not catch gradebook that didn't exist");
		} catch (GradebookNotFoundException gnfe) {}
		
		// let's start with a point gb
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		assertEquals(GradebookService.GRADE_TYPE_POINTS, gradebookService.getGradeEntryType(GRADEBOOK_UID_NO_CAT));
		
		// change it to %
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		assertEquals(GradebookService.GRADE_TYPE_PERCENTAGE, gradebookService.getGradeEntryType(GRADEBOOK_UID_NO_CAT));
		
		// change it to letter
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		gradebookManager.updateGradebook(gradebookNoCat);
		
		assertEquals(GradebookService.GRADE_TYPE_LETTER, gradebookService.getGradeEntryType(GRADEBOOK_UID_NO_CAT));
	}
	
	public void testGetLowestPossibleGradeForGbItem() throws Exception {
	    // try passing some nulls
	    try {
	        gradebookService.getLowestPossibleGradeForGbItem(null, asn1IdNoCat);
	        fail("did not catch null gradebookUid passed to getLowestPossibleGradeForGbItem");
	    } catch (IllegalArgumentException iae) {}

	    try {
	        gradebookService.getLowestPossibleGradeForGbItem(GRADEBOOK_UID_NO_CAT, null);
	        fail("did not catch null gradebookItemId passed to getLowestPossibleGradeForGbItem");
	    } catch (IllegalArgumentException iae) {}

	    // let's start with a point gb
	    Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
	    gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
	    gradebookManager.updateGradebook(gradebookNoCat);
	    String lowestPointsPossible = gradebookService.getLowestPossibleGradeForGbItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
	    assertEquals("0", lowestPointsPossible);

	    // change it to %
	    gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
	    gradebookManager.updateGradebook(gradebookNoCat);

	    lowestPointsPossible = gradebookService.getLowestPossibleGradeForGbItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
	    assertEquals("0", lowestPointsPossible);

	    // change it to letter
	    gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
	    gradebookManager.updateGradebook(gradebookNoCat);

	    lowestPointsPossible = gradebookService.getLowestPossibleGradeForGbItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
	    assertEquals("F", lowestPointsPossible);

	    // now let's change the gb item to ungraded
	    org.sakaiproject.tool.gradebook.Assignment assignment = gradebookManager.getAssignment(asn1IdNoCat);
	    assignment.setUngraded(true);
	    gradebookManager.updateAssignment(assignment);

	    lowestPointsPossible = gradebookService.getLowestPossibleGradeForGbItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
	    assertNull(lowestPointsPossible);
	}
}
