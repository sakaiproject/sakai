



/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.test;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.sakaiproject.service.gradebook.shared.ConflictingSpreadsheetNameException;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Spreadsheet;

/**
 * User: louis
 * Date: Jun 13, 2006
 * Time: 12:16:35 PM
 */
public class SpreadsheetTest extends GradebookTestBase {

    protected Gradebook gradebook;
    protected static final String SPT_NAME = "SP1 #1";
    protected static final String SPT_NAME2 = "SP2 #1";
    protected static final String SPT_NAME3 = "SP3 #1";
    protected static final String CONTENT = "one,two,three,four,five,six,seven,eight \n";

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        // Create a gradebook to work with
        String className = this.getClass().getName();
        String gradebookName = className + (new Date()).getTime();
        gradebookFrameworkService.addGradebook(gradebookName, gradebookName);

        // Set up a holder for enrollments, teaching assignments, and sections.
        integrationSupport.createCourse(gradebookName, gradebookName, false, false, false);

        // Grab the gradebook for use in the tests
        gradebook = gradebookManager.getGradebook(gradebookName);
    }

    /**
     *
     * @throws Exception
     */
    public void testCreateAndUpdateSpreadsheet() throws Exception {

        Long sptId = gradebookManager.createSpreadsheet(gradebook.getId(),SPT_NAME,"test_uid",new Date(),CONTENT);

        // Fetch the updated spreadsheet
        Spreadsheet  persistentSpreadsheet = gradebookManager.getSpreadsheet(sptId);
        // Ensure the DB update was successful
        Assert.assertEquals(persistentSpreadsheet.getCreator(), "test_uid");

        // Try to save a new assignment with the same name
        boolean errorThrown = false;
        try {
            gradebookManager.createSpreadsheet(gradebook.getId(),SPT_NAME,"test_uid",new Date(),CONTENT);
        } catch (ConflictingSpreadsheetNameException e) {
            errorThrown = true;
        }
        Assert.assertTrue(errorThrown);        

    }


    /**
     *
     * @throws Exception
     */
    public void testRemoveSpreadsheet()throws Exception {


        Long id1 = gradebookManager.createSpreadsheet(gradebook.getId(),SPT_NAME,"test_uid",new Date(),CONTENT);
        Long id2 = gradebookManager.createSpreadsheet(gradebook.getId(),SPT_NAME2,"test_uid",new Date(),CONTENT);
        Long id3 = gradebookManager.createSpreadsheet(gradebook.getId(),SPT_NAME3,"test_uid",new Date(),CONTENT);

        List spreadsheets = gradebookManager.getSpreadsheets(gradebook.getId());
        Spreadsheet spt = gradebookManager.getSpreadsheet(id1);


		// Remove the spreadsheets.
        // (We remove all of them to make sure that the calculated course grade can be emptied.)
        gradebookManager.removeSpreadsheet(id2);
        gradebookManager.removeSpreadsheet(id3);
        gradebookManager.removeSpreadsheet(id1);

        // Get the list of spreadsheets again, and make sure it's missing the removed spreadsheets
        spreadsheets = gradebookManager.getSpreadsheets(gradebook.getId());
        Assert.assertTrue(!spreadsheets.contains(spt));


        // Make sure we can add a new Spreadsheet with the same name as the removed one.
        // This will throw an exception if it doesn't like the assignment name.
        gradebookManager.createSpreadsheet(gradebook.getId(),SPT_NAME,"test_uid",new Date(),CONTENT);

    }

}
