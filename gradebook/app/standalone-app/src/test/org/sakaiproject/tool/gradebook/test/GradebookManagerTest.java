/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007 Sakai Foundation, the MIT Corporation
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

import junit.framework.Assert;

import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * TODO Document org.sakaiproject.tool.gradebook.test.GradebookManagerTest
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradebookManagerTest extends GradebookTestBase {

    public void testCreateGradebook() throws Exception {
        // Create a gradebook
        String className = this.getClass().getName();
        gradebookFrameworkService.addGradebook(className, className);
        setComplete();
    }

    public void testUpdateGradebook() throws Exception {
        // Fetch the gradebook
        Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());

        // Modify the gradebook (including the grade mapping)
        persistentGradebook.setAllAssignmentsEntered(true);
        persistentGradebook.getSelectedGradeMapping().getGradeMap().put("A", new Double(99));

        // Update the gradebook
        gradebookManager.updateGradebook(persistentGradebook);

        // Ensure that the DB update was successful
        persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
        Assert.assertTrue(persistentGradebook.isAllAssignmentsEntered());
        Assert.assertTrue(persistentGradebook.getSelectedGradeMapping().getGradeMap().get("A").equals(new Double(99)));
    }

}




