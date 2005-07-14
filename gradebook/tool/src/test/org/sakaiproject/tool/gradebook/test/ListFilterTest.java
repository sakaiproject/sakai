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
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.ListFilter;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.facades.Enrollment;
import org.sakaiproject.tool.gradebook.facades.standalone.EnrollmentStandalone;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;

/**
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 *
 */
public class ListFilterTest extends TestCase {
	private static final Log log = LogFactory.getLog(ListFilterTest.class);

	List enrollments;
    Enrollment josh1;
    Enrollment josh2;
    Enrollment ray1;
    Enrollment ray2;
    Enrollment oliver1;
    Enrollment oliver2;
    Enrollment harry1;
    Enrollment harry2;
    Gradebook gb;

    protected void setUp() throws Exception {
    	gb = new Gradebook("Test Gradebook");
        enrollments = new ArrayList();
        josh1 = new EnrollmentStandalone(new UserStandalone("jholtzman1@berkeley.edu", "Joshua Holtzman", "Holtzman, Joshua", "1111"), gb);
        enrollments.add(josh1);
        josh2 = new EnrollmentStandalone(new UserStandalone("jholtzmansky@berkeley.edu", "Josh Holtzmansky", "Holtzmansky, Josh", "1111111111"), gb);
        enrollments.add(josh2);
        ray1 = new EnrollmentStandalone(new UserStandalone("ray@media.berkeley.edu", "Ray Davis", "Davis, Ray", "1212"), gb);
        enrollments.add(ray1);
        ray2 = new EnrollmentStandalone(new UserStandalone("ray@berkeley.edu", "Ray Davidson", "Davidson, Ray", "12121212121212"), gb);
        enrollments.add(ray2);
        oliver1 = new EnrollmentStandalone(new UserStandalone("oliver@media.berkeley.edu", "Oliver Heyer", "Heyer, Oliver", "2323"), gb);
        enrollments.add(oliver1);
        oliver2 = new EnrollmentStandalone(new UserStandalone("ollie@media.berkeley.edu", "Ollie Heyer", "Heyer, Ollie", "23232323232323"), gb);
        enrollments.add(oliver2);
        harry1 = new EnrollmentStandalone(new UserStandalone("harry@berkeley.edu", "Harry Davenstaffer", "Davenstaffer, Harry", "3434"), gb);
        enrollments.add(harry1);
        harry2 = new EnrollmentStandalone(new UserStandalone("harry@media.berkeley.edu", "Harry Oogleblast", "Oogleblast, Harry", "343434343434"), gb);
        enrollments.add(harry2);
    }

    /**
     * Tests User's comparators
     *
     * @throws Exception
     */
    public void testSortStudents() throws Exception {
        Collections.sort(enrollments, FacadeUtils.ENROLLMENT_NAME_COMPARATOR);
        Assert.assertTrue(enrollments.indexOf(josh1) < enrollments.indexOf(josh2));
        Assert.assertTrue(enrollments.indexOf(oliver1) < enrollments.indexOf(oliver2));

        Collections.sort(enrollments, FacadeUtils.ENROLLMENT_DISPLAY_UID_NUMERIC_COMPARATOR);
        Assert.assertTrue(enrollments.indexOf(josh1) < enrollments.indexOf(josh2));
        Assert.assertTrue(enrollments.indexOf(oliver1) < enrollments.indexOf(oliver2));
        Assert.assertTrue(enrollments.indexOf(harry1) < enrollments.indexOf(josh2));
    }


	/**
     * Tests the user search filter
     *
	 * @throws Exception
	 */
    public void testFilterStudents() throws Exception {
        ListFilter listFilter = new ListFilter("holt");
        List filtered = listFilter.filterEnrollments(enrollments);
        Assert.assertTrue(filtered.contains(josh1));
        Assert.assertTrue(filtered.contains(josh2));

        Assert.assertTrue(!filtered.contains(ray1));
        Assert.assertTrue(!filtered.contains(ray2));
        Assert.assertTrue(!filtered.contains(oliver1));
        Assert.assertTrue(!filtered.contains(oliver2));
        Assert.assertTrue(!filtered.contains(harry1));
        Assert.assertTrue(!filtered.contains(harry2));

        listFilter.setSearchFilter("HEY");
        filtered = listFilter.filterEnrollments(enrollments);
        Assert.assertTrue(filtered.contains(oliver1));
        Assert.assertTrue(filtered.contains(oliver2));

        Assert.assertTrue(!filtered.contains(josh1));
        Assert.assertTrue(!filtered.contains(josh2));
        Assert.assertTrue(!filtered.contains(ray1));
        Assert.assertTrue(!filtered.contains(ray2));
        Assert.assertTrue(!filtered.contains(harry1));
        Assert.assertTrue(!filtered.contains(harry2));

        listFilter.setSearchFilter("h");
        filtered = listFilter.filterEnrollments(enrollments);
        Assert.assertTrue(filtered.contains(oliver1));
        Assert.assertTrue(filtered.contains(oliver2));
        Assert.assertTrue(filtered.contains(josh1));
        Assert.assertTrue(filtered.contains(josh2));
        Assert.assertTrue(filtered.contains(harry1));
        Assert.assertTrue(filtered.contains(harry2));

        Assert.assertTrue(!filtered.contains(ray1));
        Assert.assertTrue(!filtered.contains(ray2));

        listFilter.setSearchFilter("dav");
        filtered = listFilter.filterEnrollments(enrollments);
        Assert.assertTrue(filtered.contains(ray1));
        Assert.assertTrue(filtered.contains(ray2));
        Assert.assertTrue(filtered.contains(harry1));

        Assert.assertTrue(!filtered.contains(oliver1));
        Assert.assertTrue(!filtered.contains(oliver2));
        Assert.assertTrue(!filtered.contains(josh1));
        Assert.assertTrue(!filtered.contains(josh2));
        Assert.assertTrue(!filtered.contains(harry2));
    }

}


