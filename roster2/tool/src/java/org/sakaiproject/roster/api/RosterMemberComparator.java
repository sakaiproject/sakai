/**
 * Copyright (c) 2010-2016 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.api;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.util.comparator.UserSortNameComparator;

import java.util.Comparator;

/**
 * <code>Comparator</code> for <code>RosterMember</code>s.
 *
 * @author d.b.robinson@lancaster.ac.uk
 */
@Slf4j
public class RosterMemberComparator implements Comparator<RosterMember> {

    private final boolean firstNameLastName;
    private UserSortNameComparator sortNameComparator = new UserSortNameComparator();
    private UserSortNameComparator displayNameComparator = new UserSortNameComparator(true, true);

    public RosterMemberComparator(boolean firstNameLastName) {
        this.firstNameLastName = firstNameLastName;
    }
	
    /**
     * Compares two <code>RosterMember</code> objects according to the sorting
     * order configured in this instance of <code>RosterMemberComparator</code>.
     *
     * @see UserSortNameComparator
     */
    public int compare(RosterMember member1, RosterMember member2) {

        if (firstNameLastName) {
            return displayNameComparator.compare(member1.getUser(), member2.getUser());
        } else {
            return sortNameComparator.compare(member1.getUser(), member2.getUser());
        }
    }
}
