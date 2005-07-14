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
        gradebookService.addGradebook(className, className);
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




