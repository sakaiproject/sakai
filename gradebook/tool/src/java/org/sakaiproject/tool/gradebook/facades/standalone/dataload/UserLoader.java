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

package org.sakaiproject.tool.gradebook.facades.standalone.dataload;

import java.util.Set;

/**
 * A utility to load users into a database table for standalone operation of the
 * gradebook.  This interface is not needed to deploy inside sakai.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface UserLoader {

    public static final String TABLE = "gb_users_t";

    /** Students that never turn anything in */
    public static final String AUTHID_WITHOUT_GRADES_1 = "stu_16";
    public static final String AUTHID_WITHOUT_GRADES_2 = "stu_17";

    /** Other special users */
    public final static String AUTHID_TEACHER_ALL = "authid_teacher";
    public final static String AUTHID_TEACHER_AND_STUDENT = "authid_teacher_student";
    public final static String AUTHID_STUDENT_ALL = "stu_0";
    public final static String AUTHID_NO_GRADEBOOK = "authid_nowhere";

    public final static String AUTHID_STUDENT_PREFIX = "stu_";

    /**
     * Loads sample users into the database
	 */
    public void loadUsers();

    /**
     * Returns all valid users, for the convenience of other test facades.
     */
    public Set getUsers();
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
