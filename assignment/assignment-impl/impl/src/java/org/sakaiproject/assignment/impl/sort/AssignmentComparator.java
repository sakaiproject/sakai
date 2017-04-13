package org.sakaiproject.assignment.impl.sort;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.time.api.Time;

import java.util.Comparator;

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
        Time t1 = o1.getDueTime();
        Time t2 = o2.getDueTime();

        if (t1 == null) {
            result = -1;
        } else if (t2 == null) {
            result = 1;
        } else if (t1.before(t2)) {
            result = -1;
        } else {
            result = 1;
        }
        return result;
    }


}
