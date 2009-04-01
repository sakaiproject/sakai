/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 Sakai Foundation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingScale;
import org.sakaiproject.tool.gradebook.LetterGradePlusMinusMapping;
import org.sakaiproject.tool.gradebook.test.support.BackwardCompatabilityBusiness;

public class GradeMappingTest extends GradebookTestBase {
	private static Log log = LogFactory.getLog(GradeMappingTest.class);

	private BackwardCompatabilityBusiness backwardCompatabilityBusiness;

    protected void onSetUpInTransaction() throws Exception {
    	super.onSetUpInTransaction();
        backwardCompatabilityBusiness = (BackwardCompatabilityBusiness)applicationContext.getBean("backwardCompatabilityBusiness");
	}

    public void testSetGradeMappings() throws Exception {
    	GradeMapping gradeMapping;

        // By default, we get Letter Grades as a default mapping,
        // and three possible mappings per gradebook.
        String gradebook1Name = "SetGradeMappingsTest1";
        gradebookFrameworkService.addGradebook(gradebook1Name, gradebook1Name);
        Gradebook gradebook1 = gradebookManager.getGradebook(gradebook1Name);
        gradeMapping = gradebook1.getSelectedGradeMapping();
        GradeMapping oldStaticDefault = new LetterGradePlusMinusMapping();
        Assert.assertTrue(gradeMapping.getName().equals(oldStaticDefault.getName()));
        Assert.assertTrue(gradebook1.getGradeMappings().size() == 3);

        // Now make LetterGradeMapping the default.
        gradebookFrameworkService.setDefaultGradingScale("LetterGradeMapping");
        String gradebook2Name = "SetGradeMappingsTest2";
        gradebookFrameworkService.addGradebook(gradebook2Name, gradebook2Name);
        Gradebook gradebook2 = gradebookManager.getGradebook(gradebook2Name);
        gradeMapping = gradebook2.getSelectedGradeMapping();
        GradingScale letterGradingScale = gradeMapping.getGradingScale();
        Assert.assertTrue(gradeMapping.getName().equals("Letter Grades"));
        Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(90)));

        // Now replace the LetterGradePlusMinusMapping with LoseWinScale...
        ArrayList newMappings = new ArrayList();
        GradingScaleDefinition def = new GradingScaleDefinition();
        def.setUid("LoseWinScale");
        def.setName("Win, Lose, or Draw");
        def.setGrades(Arrays.asList(new String[] {"Win", "Draw", "Lose"}));
        def.setDefaultBottomPercents(Arrays.asList(new Object[] {new Double(80), new Double(40), new Double(0)}));
        newMappings.add(def);

        // ... and change the default values of LetterGradeMapping.
        def = new GradingScaleDefinition();
        def.setUid(letterGradingScale.getUid());
        def.setName(letterGradingScale.getName());
        List gradesList = letterGradingScale.getGrades();
        def.setGrades(new ArrayList(gradesList));
        Map bottomPercentsMap = letterGradingScale.getDefaultBottomPercents();
        List bottomPercentsList = new ArrayList();
        for (int i = 0; i < gradesList.size(); i++) {
        	String grade = (String)gradesList.get(i);
        	Double bottomPercent = (Double)bottomPercentsMap.get(grade);
        	if (i == 0) {
        		bottomPercent = new Double(89);
        	}
        	bottomPercentsList.add(bottomPercent);
        }
        def.setDefaultBottomPercents(bottomPercentsList);
        newMappings.add(def);
        gradebookFrameworkService.setAvailableGradingScales(newMappings);

        // Make sure a new gradebook is as expected.
        String gradebook3Name = "SetGradeMappingsTest3";
        gradebookFrameworkService.addGradebook(gradebook3Name, gradebook3Name);
        Gradebook gradebook3 = gradebookManager.getGradebook(gradebook3Name);
        gradeMapping = gradebook3.getSelectedGradeMapping();
		Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(89)));
		Assert.assertTrue(gradebook3.getGradeMappings().size() == 2);
		GradeMapping newGradeMapping = null;
		for (Iterator iter = gradebook3.getGradeMappings().iterator(); iter.hasNext() && (newGradeMapping == null); ) {
			GradeMapping gm = (GradeMapping)iter.next();
			if (!gm.getId().equals(gradeMapping.getId())) {
				newGradeMapping = gm;
			}
		}
		gradebook3.setSelectedGradeMapping(newGradeMapping);
		gradebookManager.updateGradebook(gradebook3);
		Assert.assertTrue(gradebook3.getSelectedGradeMapping().getName().equals("Win, Lose, or Draw"));

		// Make sure the old gradebook doesn't change until we tell it to.
		gradebook2 = gradebookManager.getGradebook(gradebook2Name);
        gradeMapping = gradebook2.getSelectedGradeMapping();
        Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(90)));
        gradeMapping.setDefaultValues();
        Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(89)));
    }

    public void testBackwardCompatability() throws Exception {
        String gradebookName = "PreTemplateGB";
        backwardCompatabilityBusiness.addGradebook(gradebookName, gradebookName);
        Gradebook gradebook = gradebookManager.getGradebook(gradebookName);

		// Play the old songs.
        GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
        GradeMapping oldStaticDefault = new LetterGradePlusMinusMapping();
        Assert.assertTrue(gradeMapping.getName().equals(oldStaticDefault.getName()));
        Assert.assertTrue(gradebook.getGradeMappings().size() == 3);
    }

    public void testBadDefaultGradingScale() throws Exception {
		List newMappings = new ArrayList();
        GradingScaleDefinition def = new GradingScaleDefinition();
        def.setUid("JustOneScale");
        def.setName("Just One Grading Scale");
        def.setGrades(Arrays.asList(new String[] {"Win", "Draw", "Lose"}));
        def.setDefaultBottomPercents(Arrays.asList(new Object[] {new Double(80), new Double(40), new Double(0)}));
        newMappings.add(def);
        gradebookFrameworkService.setAvailableGradingScales(newMappings);

        gradebookFrameworkService.setDefaultGradingScale("NoSuchGradeMapping");

        String gradebook1Name = "SetGradeMappingsTest1";
        if (log.isInfoEnabled()) log.info("Ignore the upcoming warning about no default...");
        gradebookFrameworkService.addGradebook(gradebook1Name, gradebook1Name);
        Gradebook gradebook1 = gradebookManager.getGradebook(gradebook1Name);
        GradeMapping gradeMapping = gradebook1.getSelectedGradeMapping();

    	// The service should have defaulted the mapping to the only
    	// one available.
    	Assert.assertTrue(gradeMapping.getName().equals(def.getName()));
    }

    public void testManualOnlyGrades() throws Exception {
    	String scaleUid = "TheNineties";
    	List<GradingScaleDefinition> newMappings = new ArrayList<GradingScaleDefinition>();
    	GradingScaleDefinition def = new GradingScaleDefinition();
    	def.setUid(scaleUid);
    	def.setName("Nineties Grading");
    	def.setGrades(Arrays.asList(new String[] {"Wicked", "Slacker", "Whatever", "Whoa", "As If"}));
    	def.setDefaultBottomPercents(Arrays.asList(new Object[] {new Double(100), new Double(0), null, "", null}));
    	newMappings.add(def);
    	gradebookFrameworkService.setAvailableGradingScales(newMappings);
    	gradebookFrameworkService.setDefaultGradingScale(scaleUid);

    	String gradebookUid = "ManualGB";
    	gradebookFrameworkService.addGradebook(gradebookUid, gradebookUid);
    	integrationSupport.createCourse(gradebookUid, gradebookUid, false, false, false);
    	Gradebook gradebook = gradebookManager.getGradebook(gradebookUid);
    	GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
    	Assert.assertTrue(gradeMapping.getName().equals(def.getName()));

    	// Make sure the ordering sticks.
    	Collection<String> grades = gradeMapping.getGrades();
    	int pos = 0;
    	for (String grade : grades) {
    		Assert.assertTrue(grade.equals(def.getGrades().get(pos++)));
    	}

    	// Make sure we get the right bottom grade.
    	String grade = gradeMapping.getGrade(1.0);
    	Assert.assertTrue(grade.equals("Slacker"));
    	grade = gradeMapping.getGrade(0.0);
    	Assert.assertTrue(grade.equals("Slacker"));

    	// To make testing dead-simple, we make student UIDs equal to
    	// the grades we intent to give them.
    	Set<String> students = new HashSet<String>(def.getGrades());
    	addUsersEnrollments(gradebook, students);

    	CourseGrade courseGrade = gradebookManager.getCourseGrade(gradebook.getId());
    	List<CourseGradeRecord> gradeRecords = new ArrayList<CourseGradeRecord>();
    	for (String studentAndGrade : students) {
    		CourseGradeRecord record = new CourseGradeRecord(courseGrade, studentAndGrade);
    		record.setEnteredGrade(studentAndGrade);
    		gradeRecords.add(record);
    	}
    	gradebookManager.updateCourseGradeRecords(courseGrade, gradeRecords);

    	// Make sure we can assign a manual-only grade to a student.
    	CourseGradeRecord gradeRecord = gradebookManager.getStudentCourseGradeRecord(gradebook, "Whoa");
    	Assert.assertTrue(gradeRecord.getDisplayGrade().equals("Whoa"));

    	// Test the sorting of assigned grades. Need to make sure that the manual-only
    	// entered grades keep their positions relative to the bottom of the grading scale.
    	gradeRecords = gradebookManager.getPointsEarnedCourseGradeRecordsWithStats(courseGrade, students);

    	//  a) Shuffle the records by sorting them alphabetically.
    	Comparator<CourseGradeRecord> comparator = new Comparator<CourseGradeRecord>() {
    		public int compare(CourseGradeRecord cgr1, CourseGradeRecord cgr2) {
    			return cgr1.getStudentId().compareTo(cgr2.getStudentId());
    		}
    	};
    	Collections.sort(gradeRecords, comparator);
    	Assert.assertTrue(gradeRecords.get(0).getStudentId().equals("As If"));

    	//  b) Now try the "override grade" sort, which should be percentage based except
    	//     for the manual-only grades.
    	Collections.sort(gradeRecords, CourseGradeRecord.getOverrideComparator(gradeMapping));
    	Collections.reverse(gradeRecords);	// Grading scale is highest to lowest.
    	pos = 0;
    	for (String gradeCode : grades) {
    		Assert.assertTrue(gradeCode.equals(gradeRecords.get(pos++).getDisplayGrade()));
    	}

    	// Make sure we can't accidentally get a letter grade from a null input.
    	grade = gradeMapping.getGrade(null);
    	Assert.assertTrue(grade == null);
    	
    	// Make sure manual-only grades aren't included in calculating the average course grade,
    	// but that null (calculated) grades are.
    	List<CourseGradeRecord> newGradeRecords = new ArrayList<CourseGradeRecord>();
    	gradeRecord = gradeRecords.remove(0);
    	gradeRecord.setEnteredGrade("Wicked");	// Worth 100%
    	newGradeRecords.add(gradeRecord);
    	gradeRecord = gradeRecords.remove(0);
    	gradeRecord.setEnteredGrade(null);	// Worth 0
    	newGradeRecords.add(gradeRecord);
    	for (CourseGradeRecord record : gradeRecords) {
    		record.setEnteredGrade("Whatever");	// Manual-only
    		newGradeRecords.add(record);
    	}
    	gradebookManager.updateCourseGradeRecords(courseGrade, newGradeRecords);
    	gradebookManager.getPointsEarnedCourseGradeRecordsWithStats(courseGrade, students);
//comment it out because of new calculation method
//    	Assert.assertTrue(courseGrade.getMean() == 50.0);
    }

    public void testResetDefaultGradeScale() throws Exception {
    	// Because the default grading scale is set via a Java property rather than through
    	// the database, the last value set by these tests will hang around polluting the JVM.
    	// Do a little cleanup before leaving.
    	gradebookFrameworkService.setDefaultGradingScale("LetterGradePlusMinusMapping");
    }

}
