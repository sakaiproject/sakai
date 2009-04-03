/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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
import java.util.Date;
import java.util.List;

import org.sakaiproject.component.section.support.IntegrationSupport;
import org.sakaiproject.component.section.support.UserManager;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Create a Gradebook for each site context in the database.
 */
public class TestGradebookLoader extends GradebookLoaderBase {
	public static String GRADEBOOK_WITH_GRADES = "QA_6";

    static String ASN_BASE_NAME = "Homework #";
    static String EXTERNAL_ASN_NAME1 = "External Assessment #1";
    static String EXTERNAL_ASN_NAME2 = "External Assessment #2";
    static String ASN_NO_DUE_DATE_NAME = "Fl\u00F8ating Assignment (Due Whenever)";
    // static String ASN_NO_DUE_DATE_NAME = "F\u4E40\u00F8ating Assignment";	// Test for Chinese support
    static String ASN_NOT_COUNTED_NAME = "Self-Assessment";

	protected IntegrationSupport integrationSupport;
	protected UserManager userManager;

	public TestGradebookLoader() {
    	// Don't roll these tests back, since they are intended to load data
		setDefaultRollback(false);
	}

	public IntegrationSupport getIntegrationSupport() {
		return integrationSupport;
	}
	public void setIntegrationSupport(IntegrationSupport integrationSupport) {
		this.integrationSupport = integrationSupport;
	}
	
	private void loadGradingScales() {
    	List<GradingScaleDefinition> newMappings = new ArrayList<GradingScaleDefinition>();
    	GradingScaleDefinition def;
 
    	def = new GradingScaleDefinition();
    	def.setUid("LetterGradePlusMinusMapping");
    	def.setName("Letter Grades with +/-");
    	def.setGrades(Arrays.asList(new String[] {"A+", "A", "A-", "B+", 
    			"B", "B-", "C+", "C", 
    			"C-", "D+", "D", "D-", 
    			"F", "I", "NR"}));
    	def.setDefaultBottomPercents(Arrays.asList(new Object[] {new Double(100.0), new Double(95.0), new Double(90.0), new Double(87.0), 
    			new Double(83.0), new Double(80.0), new Double(77.0), new Double(73.0), 
    			new Double(70.0), new Double(67.0), new Double(63.0), new Double(60.0), 
    			new Double(0), null, null}));
    	newMappings.add(def);
    	 
    	def = new GradingScaleDefinition();
    	def.setUid("LetterGradeMapping");
    	def.setName("Letter Grades");
    	def.setGrades(Arrays.asList(new String[] {"A", "B", "C", "D", "F", "I"}));
    	def.setDefaultBottomPercents(Arrays.asList(new Object[] {new Double(90.0), new Double(80.0), new Double(70.0), new Double(60.0), new Double(0), null}));
    	newMappings.add(def);

    	def = new GradingScaleDefinition();
    	def.setUid("PassNotPassMapping");
    	def.setName("Pass / Not Pass");
    	def.setGrades(Arrays.asList(new String[] {"P", "NP"}));
    	def.setDefaultBottomPercents(Arrays.asList(new Object[] {new Double(75), new Double(0)}));
    	newMappings.add(def);

    	gradebookFrameworkService.setAvailableGradingScales(newMappings);
    	gradebookFrameworkService.setDefaultGradingScale("LetterGradePlusMinusMapping");

	}

	public void testLoadGradebooks() throws Exception {
		loadGradingScales();
		
        List gradebooks = new ArrayList();
        List gradebookUids = new ArrayList();

        // Create some gradebooks
        for(int i = 0; i < StandaloneSectionsDataLoader.SITE_UIDS.length; i++) {
        	String gradebookUid = StandaloneSectionsDataLoader.SITE_UIDS[i];
        	gradebookFrameworkService.addGradebook(gradebookUid, StandaloneSectionsDataLoader.SITE_NAMES[i]);
            gradebookUids.add(gradebookUid);
        }

        // Fetch the gradebooks
        for(int i=0; i < StandaloneSectionsDataLoader.SITE_UIDS.length; i++) {
            gradebooks.add(gradebookManager.getGradebook((String)gradebookUids.get(i)));
        }

        // Add assignments for gradebook #6
        Gradebook gb = (Gradebook)gradebooks.get(5);
        for(int i = 0; i < 7; i++) {
        	int pts = (i + 1) * 10;
        	Date date = new Date();
            date.setTime(date.getTime() - ((6 - i) * 86400000));
            gradebookManager.createAssignment(gb.getId(), ASN_BASE_NAME + i, new Double(pts), date, Boolean.FALSE,Boolean.FALSE);
        }

        // Add an assignment without a due date.
        gradebookManager.createAssignment(gb.getId(), ASN_NO_DUE_DATE_NAME, new Double(50), null, Boolean.FALSE,Boolean.FALSE);

        // Add external assessments
        gradebookExternalAssessmentService.addExternalAssessment(gb.getUid(), EXTERNAL_ASN_NAME1, "samigo://external1", EXTERNAL_ASN_NAME1, new Double(10), new Date(), "Test and Quiz", new Boolean(false));
        gradebookExternalAssessmentService.addExternalAssessment(gb.getUid(), EXTERNAL_ASN_NAME2, null, EXTERNAL_ASN_NAME2, new Double(10), new Date(), "Test and Quiz", new Boolean(false));

        // Add an assignment which won't count towards the final grade.
        gradebookManager.createAssignment(gb.getId(), ASN_NOT_COUNTED_NAME, new Double(100), new Date(), Boolean.TRUE,Boolean.FALSE);

        // Ensure that this is actually saved to the database
        setComplete();
	}
}
