/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;

@Data
public class EmailEntry { 
    private boolean allIds = false;
    private Map<String, String> roleIds = new HashMap<>();
    private Map<String, String> sectionIds = new HashMap<>();
    private Map<String, String> groupIds = new HashMap<>();
    private Map<String, String> userIds = new HashMap<>();
    private String from;
    private String otherRecipients = "";
    private String subject = "";
    private String content = "";
    private List<String> attachments = new ArrayList<>();
    private ConfigEntry config;

    public EmailEntry(ConfigEntry config) {
        this.config = config;
    }
        
    public void setSubject(String subject) {
        if (subject == null || subject.trim().length() == 0) {
            this.subject = "";
        } else {
            this.subject = subject;
        }
    }

    public void setContent(String content) {
        if (content == null || content.trim().length() == 0) {
            this.content = "";
        } else {
            this.content = content;
        }
    }

}
