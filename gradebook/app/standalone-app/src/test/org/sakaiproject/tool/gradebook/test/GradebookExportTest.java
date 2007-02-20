/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California, The MIT Corporation
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.test;

import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.ui.*;
import junit.framework.Assert;

import java.util.*;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Feb 16, 2007
 * Time: 3:22:39 PM
 */
public class GradebookExportTest extends GradebookTestBase{

    protected Gradebook gradebook;

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();

        String gradebookName = this.getClass().getName();
        gradebookFrameworkService.addGradebook(gradebookName, gradebookName);

        // Set up a holder for enrollments, teaching assignments, and sections.
        integrationSupport.createCourse(gradebookName, gradebookName, false, false, false);

        // Grab the gradebook for use in the tests
        gradebook = gradebookManager.getGradebook(gradebookName);
    }




    public void testRosterExport(){

        List studentUidsList = Arrays.asList(new String[] {
                "Student1",
                "Student2",
                "Student3",
        });
        List enrollments = addUsersEnrollments(gradebook, studentUidsList);

        gradebookManager.createAssignment(gradebook.getId(), "Assignment 1", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn = (Assignment)gradebookManager.getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

        // Create a grade record set
        List gradeRecords = new ArrayList();
        gradeRecords.add(new AssignmentGradeRecord(asn, "Student1", new Double(9)));
        gradeRecords.add(new AssignmentGradeRecord(asn, "Student2", new Double(10)));
        gradeRecords.add(new AssignmentGradeRecord(asn, "Student3", new Double(9)));

       gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);

        gradebookManager.createAssignment(gradebook.getId(), "Assignment 2", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        asn = (Assignment)gradebookManager.getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

       gradebookManager.updateAssignmentGradeRecords(asn, gradeRecords);


        GradebookBean gb = new GradebookBean();
        gb.setAuthnService(authn);
        gb.setAuthzService(authz);
        gb.setGradebookManager(gradebookManager);
        gb.setGradebookUid(gradebook.getUid());
        gb.setSectionAwareness(sectionAwareness);
        gb.setUserDirectoryService(userDirectoryService);

        PreferencesBean pb = new PreferencesBean();

        RosterExportFormatterBean exportFormatter = new RosterExportFormatterBean();

        exportFormatter.setGradebookBean(gb);
        exportFormatter.setPreferencesBean(pb);




        List gradableObjects = gradebookManager.getAssignments(gradebook.getId());

        // create a list of list of values representing rows and columns
        //List formattedData = exportFormatter.getExportRows(gradableObjects,enrollments);

        //verify that list has 4 entries representing header and  row for each student
        //Assert.assertTrue(formattedData.size() == 2);
        //verify that list has entries has list with 4 entries each entry represent a coum 2 for the assignments (above)
        //an one coloumn for userid and one for name
        //Iterator it = formattedData.iterator();
        //List row = (ArrayList) it.next();
        //Assert.assertTrue(row.size() == 4);

    }



}
