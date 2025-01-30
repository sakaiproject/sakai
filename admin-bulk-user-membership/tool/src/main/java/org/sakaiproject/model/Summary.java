/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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

package org.sakaiproject.bulk.membership.model;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

public class Summary {

    private final String EMAIL_SEPARATOR = "@";

    @Getter @Setter
    private String userCriteria;
    
    @Getter @Setter
    private String userName;

    @Getter @Setter
    private ArrayList<String> failedSites;

    @Getter @Setter
    private ArrayList<String> workedSites;

    public Summary () {
        userCriteria = "";
        userName = "";
        failedSites = new ArrayList<String>();
        workedSites = new ArrayList<String>();
    }

    public Summary (String userCriteria) {
        this.userCriteria = userCriteria;
        userName = "";
        failedSites = new ArrayList<String>();
        workedSites = new ArrayList<String>();
    }

    public Summary (String userCriteria, String userName) {
        this.userCriteria = userCriteria;
        this.userName = userName;
        failedSites = new ArrayList<String>();
        workedSites = new ArrayList<String>();
    }

    public void addFailedSite(String failedSite) {
        failedSites.add(failedSite);
    }

    public void addWorkedSite(String workedSite) {
        workedSites.add(workedSite);
    }

    public String getCleanedUserName() {
        int indexEmailSep = userName.indexOf(EMAIL_SEPARATOR);
        return (indexEmailSep == -1 ? userName : userName.substring(0, indexEmailSep));
    }
} 