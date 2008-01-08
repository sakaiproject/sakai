/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
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
		System.out.println("gb id: " + gradebookWithCat.getId());
		System.out.println("CAT1_NAME : " + CAT1_NAME);
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
		gradebookService.setAssignmentScore(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_IN_SECTION_UID1, new Double(39), "Service Test");

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
			score = gradebookService.getAssignmentScore(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
			fail();
		} catch (SecurityException e) {
		}

		// try to get grades for students for item
		List grades;
		try {
			List studentIds = new ArrayList();
			studentIds.add(STUDENT_IN_SECTION_UID1);
			studentIds.add(STUDENT_NOT_IN_SECTION_UID1);
			grades = gradebookService.getGradesForStudentsForItem(asn1IdNoCat, studentIds);
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
		System.out.println("instructor sees grade: " + gradeDef.getGrade());

		// Now see if the student gets lucky.
		setAuthnId(STUDENT_IN_SECTION_UID1);
		score = gradebookService.getAssignmentScore(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(score.doubleValue() == 39.0);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(gradeDef.getGrade().equals((new Double(39).toString())));
		
		setAuthnId(STUDENT_IN_SECTION_UID1);
		try {
			Map viewableStudents = gradebookService.getViewableStudentsForItemForCurrentUser(GRADEBOOK_UID_NO_CAT, asn1IdNoCat);
			Assert.fail();
		} catch(SecurityException e) {
		}
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

		gradebookService.setAssignmentScore(GRADEBOOK_UID_NO_CAT, ASN_TITLE1, STUDENT_IN_SECTION_UID1, new Double(35), "Service Test");
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertTrue(gradeDef != null);
		Assert.assertEquals(GradebookService.GRADE_TYPE_POINTS, gradeDef.getGradeEntryType());
		Assert.assertEquals(STUDENT_IN_SECTION_UID1, gradeDef.getStudentUid());
		Assert.assertTrue(gradeDef.isGradeReleased() == false);
		Assert.assertEquals("35.0", gradeDef.getGrade());
		Assert.assertEquals(INSTRUCTOR_UID, gradeDef.getGraderUid());
		Assert.assertNotNull(gradeDef.getDateRecorded());
		
		// %-based gradebook
		Gradebook gradebookNoCat = gradebookManager.getGradebook(GRADEBOOK_UID_NO_CAT);
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		gradebookManager.updateGradebook(gradebookNoCat);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		Assert.assertEquals("87.5", gradeDef.getGrade());
		Assert.assertEquals(GradebookService.GRADE_TYPE_PERCENTAGE, gradeDef.getGradeEntryType());
		
		// letter-based gradebook
		gradebookNoCat.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		gradebookManager.updateGradebook(gradebookNoCat);
		gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_IN_SECTION_UID1);
		System.out.println("gradeDef.getGrade" + gradeDef.getGrade());
		System.out.println("gradeDef.getGraderUid " + gradeDef.getGraderUid());
		Assert.assertEquals("B+", gradeDef.getGrade());
		Assert.assertEquals(GradebookService.GRADE_TYPE_LETTER, gradeDef.getGradeEntryType());
		
		// the TA with standard grader perms should trigger exception for student
		// not in section
		setAuthnId(TA_UID);
		try {
			gradeDef = gradebookService.getGradeDefinitionForStudentForItem(GRADEBOOK_UID_NO_CAT, asn1IdNoCat, STUDENT_NOT_IN_SECTION_UID1);
			Assert.fail();
		} catch (SecurityException se) {
			
		}
		
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
		viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_NO_CAT);
		Assert.assertTrue(viewableAssignments.size() == 2);
		
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
		viewableAssignments = gradebookService.getViewableAssignmentsForCurrentUser(GRADEBOOK_UID_WITH_CAT);
		Assert.assertTrue(viewableAssignments.size() == 2);
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
		Assert.assertTrue(viewableStudentsMap.size() == 2);
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
	
	public void testGetGradesForStudentsForItem() throws Exception {

	}
}
