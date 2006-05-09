/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
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

import java.util.*;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradeMapping;
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
    	Collection grades;
    	List defaultValues;
    	GradeMapping gradeMapping;

        // By default, we get Letter Grades as a default mapping,
        // and three possible mappings per gradebook.
        String gradebook1Name = "SetGradeMappingsTest1";
        gradebookService.addGradebook(gradebook1Name, gradebook1Name);
        Gradebook gradebook1 = gradebookManager.getGradebook(gradebook1Name);
        gradeMapping = gradebook1.getSelectedGradeMapping();
        GradeMapping oldStaticDefault = new LetterGradePlusMinusMapping();
        Assert.assertTrue(gradeMapping.getName().equals(oldStaticDefault.getName()));
        Assert.assertTrue(gradebook1.getGradeMappings().size() == 3);

        // Now make LetterGradeMapping the default.
        gradebookService.setDefaultGradingScale("LetterGradeMapping");
        String gradebook2Name = "SetGradeMappingsTest2";
        gradebookService.addGradebook(gradebook2Name, gradebook2Name);
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
        def.setGrades(Arrays.asList(new Object[] {"Win", "Draw", "Lose"}));
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
        gradebookService.setAvailableGradingScales(newMappings);

        // Make sure a new gradebook is as expected.
        String gradebook3Name = "SetGradeMappingsTest3";
        gradebookService.addGradebook(gradebook3Name, gradebook3Name);
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
        def.setGrades(Arrays.asList(new Object[] {"Win", "Draw", "Lose"}));
        def.setDefaultBottomPercents(Arrays.asList(new Object[] {new Double(80), new Double(40), new Double(0)}));
        newMappings.add(def);
        gradebookService.setAvailableGradingScales(newMappings);

        gradebookService.setDefaultGradingScale("NoSuchGradeMapping");

        String gradebook1Name = "SetGradeMappingsTest1";
        if (log.isInfoEnabled()) log.info("Ignore the upcoming warning about no default...");
        gradebookService.addGradebook(gradebook1Name, gradebook1Name);
        Gradebook gradebook1 = gradebookManager.getGradebook(gradebook1Name);
        GradeMapping gradeMapping = gradebook1.getSelectedGradeMapping();

    	// The service should have defaulted the mapping to the only
    	// one available.
    	Assert.assertTrue(gradeMapping.getName().equals(def.getName()));
    }

}
