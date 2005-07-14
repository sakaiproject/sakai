/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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


