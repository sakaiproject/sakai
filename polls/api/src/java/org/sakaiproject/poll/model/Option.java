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

import lombok.Data;

@Data
public class Option {

    private Long optionId;
    private Long pollId;
    private String text;
    private String status;
    private String uuid;
    private Boolean deleted = Boolean.FALSE;
    private Integer optionOrder;

    public Option() {}

    public Option(Long oId) {
        this.optionId = oId;
    }

    public String getId() {
        return optionId+"";
    }

    public void setId(Long id) {	
        this.optionId = id;	
    }
}
