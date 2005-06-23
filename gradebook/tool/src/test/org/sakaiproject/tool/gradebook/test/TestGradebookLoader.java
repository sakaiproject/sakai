/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class TestGradebookLoader extends GradebookDbTestBase {
	private static final Log log = LogFactory.getLog(TestGradebookLoader.class);

    public static final String[] GRADEBOOK_UIDS = {
			"QA_1",

            "QA_2",
            "QA_3",
            "QA_4",

            "QA_5",
            "QA_6",

            "QA_7",
            "QA_8",
    };
    public static final String[] GRADEBOOK_NAMES = {
			"QA Gradebook #1 [no students, no assignments, no grades]",

            "QA Gradebook #2 [10 students, no assignments, no grades]",
            "QA Gradebook #3 [10 students, no assignments, no grades]",
            "QA Gradebook #4 [10 students, no assignments, no grades]",

            "QA Gradebook #5 [50 students, no assignments, no grades]",
            "QA Gradebook #6 [50 students, 10 assignments, 500 grades]",

            "QA Gradebook #7 [150 students, no assignments, no grades]",
            "QA Gradebook #8 [400 students, no assignments, no grades]",
    };

    public static String GRADEBOOK_WITH_GRADES = "QA_6";

    static String ASN_BASE_NAME = "Homework #";
    static String EXTERNAL_ASN_NAME1 = "External Assessment #1";
    static String EXTERNAL_ASN_NAME2 = "External Assessment #2";
    static String ASN_NO_DUE_DATE_NAME = "Fl\u00F8ating Assignment (Due Whenever)";

	public void testLoadGradebooks() throws Exception {
        List gradebooks = new ArrayList();
        List gradebookUids = new ArrayList();

        // Create some gradebooks
        for(int i=0; i<GRADEBOOK_UIDS.length; i++) {
        	String gradebookUid = GRADEBOOK_UIDS[i];
        	gradebookService.addGradebook(gradebookUid, GRADEBOOK_NAMES[i]);
            gradebookUids.add(gradebookUid);
        }

        // Fetch the gradebooks
        for(int i=0; i < GRADEBOOK_UIDS.length; i++) {
            gradebooks.add(gradebookManager.getGradebook((String)gradebookUids.get(i)));
        }

        // Add assignments for gradebook #6
        Gradebook gb = (Gradebook)gradebooks.get(5);
        for(int i = 0; i < 7; i++) {
        	int pts = (i + 1) * 10;
        	Date date = new Date();
            date.setTime(date.getTime() - ((6 - i) * 86400000));

            log.info("i=" + i + ", date=" + date);

            gradeManager.createAssignment(gb.getId(), ASN_BASE_NAME + i, new Double(pts), date);
        }

        // Add an assignment without a due date.
        gradeManager.createAssignment(gb.getId(), ASN_NO_DUE_DATE_NAME, new Double(50), null);

        // Add external assessments
        gradebookService.addExternalAssessment(gb.getUid(), EXTERNAL_ASN_NAME1, "samigo://external1", EXTERNAL_ASN_NAME1, 10, new Date(), "Test and Quiz");
        gradebookService.addExternalAssessment(gb.getUid(), EXTERNAL_ASN_NAME2, null, EXTERNAL_ASN_NAME2, 10, new Date(), "Test and Quiz");
        
        // Ensure that this is actually saved to the database
        setComplete();
	}
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
