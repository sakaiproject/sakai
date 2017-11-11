/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.assignment.impl.sort;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.time.api.Time;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;

/**
 * The AssignmentComparator class that sorts by the due date of the assignment.
 */
public class AssignmentComparator implements Comparator<Assignment> {
    /**
     * implementing the compare function
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return The compare result. 1 is o1 < o2; -1 otherwise
     */
    public int compare(Assignment o1, Assignment o2) {
        int result = -1;

        // sorted by the assignment due date
        Instant t1 = o1.getDueDate();
        Instant t2 = o2.getDueDate();

        if (t1 == null) {
            result = -1;
        } else if (t2 == null) {
            result = 1;
        } else if (t1.isBefore(t2)) {
            result = -1;
        } else {
            result = 1;
        }
        return result;
    }
}
