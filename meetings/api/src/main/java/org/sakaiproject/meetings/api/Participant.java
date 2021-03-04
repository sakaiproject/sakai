/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.api;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Participant {

    public static final String MODERATOR = "moderator";
    public static final String ATTENDEE = "attendee";

    public static final String SELECTION_ALL = "all";
    public static final String SELECTION_GROUP = "group";
    public static final String SELECTION_ROLE  = "role";
    public static final String SELECTION_USER  = "user";
    
    private String selectionType = null;
    private String selectionId = null;
    private String role = null;
    
    public Participant() {}
    
    public Participant(String selectionType, String selectionId, String role) {

        this.selectionType = selectionType;
        this.selectionId = selectionId;
        this.role = role;
    }
    
    public boolean isModerator() {
        return MODERATOR.equals(role);
    }
    
    public String toString(){
        return "[" + selectionType + ", " + selectionId + ", " + role + "]"; 
    }
}
