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

import java.util.Comparator;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

import lombok.extern.slf4j.Slf4j;

/**
 * <code>Comparator</code> for <code>RosterMember</code>s.
 *
 * @author d.b.robinson@lancaster.ac.uk
 */
@Slf4j
public class RosterMemberComparator implements Comparator<RosterMember> {

    private final boolean firstNameLastName;
    private RuleBasedCollator collator;

    public RosterMemberComparator(boolean firstNameLastName) {

        this.firstNameLastName = firstNameLastName;

        try {
            RuleBasedCollator collator_ini = (RuleBasedCollator) Collator.getInstance();
            collator = new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        } catch (ParseException pe) {
            log.error("ERROR: Failed to setup collator", pe);
        }
    }
	
    /**
     * Compares two <code>RosterMember</code> objects according to the sorting
     * order configured in this instance of <code>RosterMemberComparator</code>.
     *
     * @see java.text.Collator#compare(java.lang.String, java.lang.String)
     */
    public int compare(RosterMember member1, RosterMember member2) {

        if (firstNameLastName) {
            return this.collator.compare (member1.getDisplayName(),member2.getDisplayName());
        } else {
            return this.collator.compare (member1.getSortName(),member2.getSortName());
        }
    }
}
