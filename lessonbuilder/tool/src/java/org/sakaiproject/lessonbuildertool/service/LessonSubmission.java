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
 *       http://www.osedu.org/licenses/ECL-2.0
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
public interface LessonSubmission {

    // constructor should be
    // public LessonSubmission(Object actual);

    // underlying Sakai object, e.g. AssignmentSubmission
    public Object getActual();
    
    // type. same types codes as LessonEntity
    public int getType();
    // don't think we need a reference, as it is always looked up by user

    // was it fully submitted. For some types just returns true
    public boolean isAvailable();

    // meets requirements. Typically that it is above user-required minimum grade
    // but some types have weirder requirements. Details have to be coordinated
    // with the UI
    public boolean isComplete(SimplePageItem item);

}