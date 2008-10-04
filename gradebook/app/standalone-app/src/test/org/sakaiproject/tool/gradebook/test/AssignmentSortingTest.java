/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006 Sakai Foundation, the MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class AssignmentSortingTest extends TestCase {
	private static final Log log = LogFactory.getLog(AssignmentSortingTest.class);

	protected void setUp() throws Exception {
    }

	/**
     * Tests in-memory assignment sorting
     *
	 * @throws Exception
	 */
    public void testAssignmentSorting() throws Exception {
        Gradebook gb = new Gradebook("sort test gb");
        Date now = new Date();

        List assignments = new ArrayList();
        Assignment asn1 = new Assignment(gb, "Asn A", new Double(10), now);
        asn1.setMean(new Double(90));
        assignments.add(asn1);

        Assignment asn2 = new Assignment(gb, "Asn B", new Double(11), new Date(now.getTime() - 1000));
        asn2.setMean(new Double(80));
        assignments.add(asn2);

        Assignment asn3 = new Assignment(gb, "Asn C", new Double(12), new Date(now.getTime() - 2000));
        asn3.setMean(new Double(70));
        assignments.add(asn3);

        // Make sure the name sorting works properly
        List nameSortedAsc = new ArrayList(assignments);
        Collections.sort(nameSortedAsc, Assignment.nameComparator);
        Assert.assertTrue(nameSortedAsc.indexOf(asn1) < nameSortedAsc.indexOf(asn2));
        Assert.assertTrue(nameSortedAsc.indexOf(asn2) < nameSortedAsc.indexOf(asn3));

        // Make sure the date sorting works properly
        List dateSortedAsc = new ArrayList(assignments);
        Collections.sort(dateSortedAsc, Assignment.dateComparator);
        Assert.assertTrue(dateSortedAsc.indexOf(asn1) > dateSortedAsc.indexOf(asn2));
        Assert.assertTrue(dateSortedAsc.indexOf(asn2) > dateSortedAsc.indexOf(asn3));

        // Make sure the mean sorting works properly
        List meanSortedAsc = new ArrayList(assignments);
        Collections.sort(meanSortedAsc, Assignment.meanComparator);
        Assert.assertTrue(meanSortedAsc.indexOf(asn1) > meanSortedAsc.indexOf(asn2));
        Assert.assertTrue(meanSortedAsc.indexOf(asn2) > meanSortedAsc.indexOf(asn3));

        // Make sure the points sorting works properly
        List pointsSortedAsc = new ArrayList(assignments);
        Collections.sort(pointsSortedAsc, Assignment.pointsComparator);
        Assert.assertTrue(pointsSortedAsc.indexOf(asn1) < pointsSortedAsc.indexOf(asn2));
        Assert.assertTrue(pointsSortedAsc.indexOf(asn2) < pointsSortedAsc.indexOf(asn3));
    }
}


