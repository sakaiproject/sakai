/*******************************************************************************
 * Copyright (c) 2006, 2007 Sakai Foundation, the MIT Corporation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Oct 24, 2006
 * Time: 2:20:55 PM
 */
public class GradeCommentTest extends GradebookTestBase  {

    protected Gradebook gradebook;

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

    public void testAssignmentGradeComments() throws Exception {
    	// Create enrollment records.
        List studentUids = Arrays.asList(new String[] {
    			"testStudentUserUid1",
    			"testStudentUserUid2",
    			"testStudentUserUid3",
    		});
    	addUsersEnrollments(gradebook, studentUids);

        // Create an asssignment.
        Long asnId = gradebookManager.createAssignment(gradebook.getId(), "Scores Entered Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn = gradebookManager.getAssignmentWithStats(asnId);
    	
    	// Make sure comments start off as null.
        List persistentComments = gradebookManager.getComments(asn, studentUids);
        Assert.assertTrue(persistentComments.isEmpty());
    	
    	// Add a comment.
        List comments = new ArrayList();
        comments.add(new Comment((String)studentUids.get(0), "First Comment", asn));
        gradebookManager.updateComments(comments);
    	
    	// Make sure we stored just the one comment.
        persistentComments = gradebookManager.getComments(asn, studentUids);
        Assert.assertTrue(persistentComments.size() == 1);
        Comment comment = (Comment)persistentComments.get(0);
        Assert.assertTrue(comment.getCommentText().equals("First Comment"));
    	
    	// Leave the first comment as is, add a comment.
        comments = new ArrayList();
        comments.add(new Comment((String)studentUids.get(1), "Next comment", asn));
        comment.setCommentText("");
        comments.add(comment);
        gradebookManager.updateComments(comments);
    	
        persistentComments = gradebookManager.getComments(asn, studentUids);
        Assert.assertTrue(persistentComments.size() == 2);
        for (Iterator iter = persistentComments.iterator(); iter.hasNext(); ) {
        	comment = (Comment)iter.next();
        	if (comment.getStudentId().equals(studentUids.get(0))) {
        		Assert.assertTrue(comment.getCommentText().length() == 0);
        	}
        }
        
        // Currently the Student View reads comments from the database
        // into an ArrayList
        List studentComments = gradebookManager.getStudentAssignmentComments((String)studentUids.get(1),gradebook.getId());
        Iterator iter  = studentComments.iterator();
        while(iter.hasNext()){
            Comment asnComment = (Comment) iter.next();
            if(asnComment.getStudentId().equals(studentUids.get(1))) Assert.assertTrue(asnComment.getCommentText().equals("Next comment"));
            if(asnComment.getStudentId().equals(studentUids.get(2))) Assert.assertTrue(asnComment == null);
        }

    	// Make sure we emulate an optimistic locking failure when we try
        // to create a new comment record that's already in the database.
        // (This test has to go last, since it will cause transaction
        // rollback.)
        comments = new ArrayList();
        comments.add(new Comment((String)studentUids.get(0), "Oops", asn));
        try {
        	gradebookManager.updateComments(comments);
        	fail();
        } catch (StaleObjectModificationException e) {}
      
    }

}
