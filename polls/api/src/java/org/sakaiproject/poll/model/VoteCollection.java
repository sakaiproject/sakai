/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

package org.sakaiproject.poll.model;

import java.util.List;
import java.util.UUID;

public class VoteCollection {

    private String id;
    private List<Vote> votes;
    private Long pollId;
    public String[] optionsSelected;
    public String option;
    private String submittionStatus;

    public VoteCollection(){
        //need a new id here
        id = UUID.randomUUID().toString();
    }

    public void setId(String value) {
        id = value;
    }

    public String getId() {
        return id;
    }

    public void setVotes(List<Vote> rvotes) {
        votes = rvotes;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setPollId (Long pid) {
        this.pollId=pid;
    }
    public Long getPollId(){
        return this.pollId;
    }

    public void setOption(String s){
        this.option = s;
    }
    public String getOption(){
        return option;
    }

    public void setOptionsSelected(String[] s) {
        this.optionsSelected = s;
    }
    public String[] getOptionsSelected(){
        return this.optionsSelected;
    }

    public void setSubmissionStatus(String s){
        this.submittionStatus=s;
    }

    public String getSubmissionStatus(){
        return this.submittionStatus;
    }
}

