/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener.SectionActivityData;
import org.sakaiproject.user.cover.UserDirectoryService;

@Slf4j
public class SectionActivityBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SelectItem> displayNamesList;
    private String selectedUser;
    private List<SectionActivityData> sectionActivityDataList;
    private String sortType="assessmentName";
    private boolean sortAscending = true;

    public List getDisplayNamesList() {
        return displayNamesList;
    }

    public void setDisplayNamesList(List<SelectItem> displayNamesList) {
        this.displayNamesList = displayNamesList;
    }

    public String getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(String selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List getSectionActivityDataList() {
        return sectionActivityDataList;
    }
   

    public void setSectionActivityDataList(List<SectionActivityData> sectionActivityDataList) {
        this.sectionActivityDataList = sectionActivityDataList;
    }

    public String getSelectedUserDisplayName() {
        String displayName="";
        try {      
            displayName = UserDirectoryService.getUser(selectedUser).getDisplayName();
        } catch (Exception e) {
            log.debug("Can't find user", e);
        }
        return displayName ;
    }
   
    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }  
   
}
