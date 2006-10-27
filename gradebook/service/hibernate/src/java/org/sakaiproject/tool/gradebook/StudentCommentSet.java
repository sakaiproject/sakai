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

import java.util.Map;
import java.util.HashMap;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Oct 25, 2006
 * Time: 1:10:17 PM
 */
public class StudentCommentSet {

    protected Map commentMap;

    public StudentCommentSet() {
        commentMap = new HashMap();
    }

    public void addComment(Comment comment) {
            commentMap.put(comment.getGradableObject().getId(), comment);

    }

    public Comment getComment(String assignmentId){
         return (Comment) commentMap.get(assignmentId);
    }

    /**
     * The whole idea of this class is to hide the map from the API, but since
     * we need access to the map to get the grade records in JSF, the accessor
     * for the map remains (assignment id -> commentText).
     *
     * @return  commentMap
     */
    public Map getCommentMap() {
        return commentMap;
    }
}
