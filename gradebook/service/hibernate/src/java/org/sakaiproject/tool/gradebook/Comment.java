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

package org.sakaiproject.tool.gradebook;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Oct 20, 2006
 * Time: 10:56:34 AM
 */
public class Comment extends AbstractComment {

    public Comment() {
       super();
    }

    public Comment(String studentId, String comment, GradableObject gradableObject) {
        this.gradableObject = gradableObject;
        this.studentId = studentId;
        this.commentText = comment;
    }

}

