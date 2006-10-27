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

import java.util.*;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Oct 23, 2006
 * Time: 1:19:06 PM
 *
 *  set of comments that allow for lookup of assignment comments for students in a gradebook
 */
public class AssignmentCommentSet {

    protected GradableObject gradableObject;
    protected Map commentMap;

    public AssignmentCommentSet(GradableObject go) {
        if(go == null) {
            throw new IllegalArgumentException("A AssignmentCommentSet must be constructed with a non-null GradableObject");
        }
        this.gradableObject = go;
        commentMap = new HashMap();
    }

    public void addComment(Comment comment) {
            commentMap.put(comment.getStudentId(), comment);

    }

    public Comment getComment(String studentId){
         return (Comment) commentMap.get(studentId);
    }


    public Collection getAllComments() {
          Collection coll = new HashSet();
          for(Iterator iter = commentMap.keySet().iterator(); iter.hasNext();) {
              coll.add(commentMap.get(iter.next()));
          }
          return coll;
      }

    public Set getAllStudentIds() {
        return new HashSet(commentMap.keySet());
    }

    /**
     * The whole idea of this class is to hide the map from the API, but since
     * we need access to the map to get the grade records in JSF, the accessor
     * for the map remains (student id -> comment).
     *
     * @return  commentMap
     */
    public Map getCommentMap() {
        return commentMap;
    }


    public GradableObject getGradableObject() {
        return gradableObject;
    }

}
