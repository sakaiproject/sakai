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

public class Option {

    private Long id;
    private Long pollId;
    private String text;
    private String status;
    private String uuid;
	private Boolean deleted;
	
	
    


    public Option() {}

    public Option(Long oId) {
        this.id = oId;
    }

    public void setOptionId(Long value) {
        id = value;
    }

    public Long getOptionId() {
        return id;
    }

    public void setOptionText(String option) {
        text = option;
    }

    public String getOptionText() {
        return text;
    }

    public Long getPollId() {
        return pollId;
    }

    public void setPollId(Long pollid) {
        this.pollId = pollid;
    }

    public void setStatus(String s) {
        this.status = s;
    }

    public String getStatus() {
        return this.status;
    }

    public String getId() {
        return id+"";
    }

    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getUUId() {
        return uuid;
    }
    
    public void setUUId(String id) {
        uuid = id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Boolean getDeleted() {
		return (deleted == null) ? Boolean.FALSE : deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

}
