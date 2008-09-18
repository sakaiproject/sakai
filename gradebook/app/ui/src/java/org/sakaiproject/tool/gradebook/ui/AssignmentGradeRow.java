/**********************************************************************************
*
* $Id:$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006, 2007 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.List;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;

public class AssignmentGradeRow implements Serializable {
	private Assignment assignment;
    private AssignmentGradeRecord gradeRecord;
    private String commentText;
    private Gradebook gradebook;
    private Double score;
    private String letterScore;
    private List eventRows;
    private String eventsLogTitle;
    private boolean userCanGrade;

    public AssignmentGradeRow(Assignment assignment, Gradebook gradebook) {
    	this.assignment = assignment;
    	this.gradebook = gradebook;
    	commentText = "";
    }
    public AssignmentGradeRow(Assignment assignment, Gradebook gradebook, boolean userCanGrade) {
    	this.assignment = assignment;
    	this.gradebook = gradebook;
    	commentText = "";
    	this.userCanGrade = userCanGrade;
    }
    public void setGradeRecord(AssignmentGradeRecord gradeRecord) {
    	this.gradeRecord = gradeRecord;
    }
    // not getAssignment b/c will clash with isAssignment in UI
    public Assignment getAssociatedAssignment() {
    	return assignment;
    }
    public AssignmentGradeRecord getGradeRecord() {
    	return gradeRecord;
    }
    public Gradebook getGradebook() {
    	return gradebook;
    }

    public Double getPointsEarned() {
    	if (gradeRecord == null) {
    		return null;
    	} else {
    		return gradeRecord.getPointsEarned();
    	}
    }
    public Double getGradeAsPercentage() {
    	if (gradeRecord == null) {
    		return null;
    	} else {
    		return gradeRecord.getGradeAsPercentage();
    	}
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
    
    public Double getScore() {   	
    	return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	
	public String getLetterScore() {
		return letterScore;
	}
	public void setLetterScore(String letterScore) {
		this.letterScore = letterScore;
	}
	
	public List getEventRows() {
		return eventRows;
	}
	public void setEventRows(List eventRows) {
		this.eventRows = eventRows;
	}
	
	public String getEventsLogTitle() {
    	return eventsLogTitle;
    }
	public void setEventsLogTitle(String eventsLogTitle) {
		this.eventsLogTitle = eventsLogTitle;
	}
    
    /**
     * Used by GradebookItemTable
     * @return false
     */
    public boolean getIsCategory() {
    	return false;
    }
    
    /**
     * Returns true to align with renderer for GradebookItemTable
     * @return true
     */
    public boolean isAssignment() {
    	return true;
    }
    
    public boolean isUserCanGrade() {
    	return userCanGrade;
    }
    public void setUserCanGrader(boolean userCanGrade) {
    	this.userCanGrade = userCanGrade;
    }

}
