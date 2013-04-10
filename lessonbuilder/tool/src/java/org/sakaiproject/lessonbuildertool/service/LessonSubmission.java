/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Date;

import org.sakaiproject.lessonbuildertool.SimplePageItem;


/**
 * Interface for individual submission to assignments, tests and other external assignment-like things
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
public class LessonSubmission {

    // this is used in a context where we have know most of what we
    // care about, e.g. user, assignment type.  This is really only used
    // to show whether something was submitted and what its grade was.

    private Double grade;
    private int type;
    private String gradeString = null;

    // add grading type when we do assignments. We can't hide that, since
    // it's in the UI. I sure hope Assignment 2 uses the same grading types

    public LessonSubmission(Double grade) {
	this.grade = grade;
    }

    public Double getGrade() {
	return grade;
    }

    public String getGradeString() {
	return gradeString;
    }

    public void setGradeString(String s) {
	gradeString = s;
    }

}