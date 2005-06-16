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

import java.util.HashSet;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.standalone.UserStandalone;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Loads user data via Hibernate for testing
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class UserLoaderHibernateImpl extends HibernateDaoSupport implements UserLoader {
    protected static final Log log = LogFactory.getLog(UserLoaderHibernateImpl.class);

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.standalone.dataload.UserLoader#loadUsers()
	 */
	public void loadUsers() {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {

            	// Load teachers.
                session.save(new UserStandalone(AUTHID_TEACHER_ALL, "Bizzy Teacher", "Teacher, Bizzy", "uTeacher"));
                session.save(new UserStandalone(AUTHID_TEACHER_AND_STUDENT, "Teaching Assistant", "Assistant, Teaching", "uTA"));

                // Load students.
                for(int i=0; i < 400; i++) {
                    String firstName;
                    String lastName;
                    switch(i) {
                        case 0:
                        	firstName = "Abby Lynn";
                        	lastName = "Astudent";
                            break;
                        case 1:
                            firstName = "Mary";
                            lastName = "LongLastNameThatExceedsTheMaximumInTheGradebook";
                            break;
                        case 3:
                            firstName = "Susan";
                            lastName = "Smith-Morris";
                            break;
                        case 4:
                            firstName = "Nathan Q., Jr.";
                            lastName = "Brewster";
                            break;
                        case 5:
                            firstName = "Carol Lee";
                            lastName = "Williams";
                            break;
                        case 6:
                            firstName = "Kim";
                            lastName = "Jones Parker";
                            break;
                        case 7:
                            firstName = "Joe";
                            lastName = "Brown";
                            break;
                        case 8:
                            firstName = "Joe";
                            lastName = "Brown";
                            break;
                        case 9:
                            firstName = "Sarah Jane";
                            lastName = "Miller";
                            break;
                        case 10:
                            firstName = "Rachel";
                            lastName = "Wilson";
                            break;
                        case 11:
                            firstName = "Ali";
                            lastName = "Moore";
                            break;
                        case 12:
                            firstName = "Chen-Wai";
                            lastName = "Taylor";
                            break;
                        case 13:
                            firstName = "Samuel Taylor Coleridge";
                            lastName = "Ascot";
                            break;
                        case 14:
                            firstName = "Jane Quincy";
                            lastName = "Brandenburg";
                            break;
                        case 15:
                            firstName = "Thor";
                            lastName = "Mj\u00F8lner";
                            break;
                        case 16:
                            firstName = "Lazy";
                            lastName = "Etudient1";
                            break;
                        case 17:
                            firstName = "Lazy";
                            lastName = "Etudient2";
                            break;
                        default:
                            firstName = "First Middle";
                            lastName = "LastName" + i;
                    }


                    String uidPrefix = (i != 3) ? "uid_" : "uID_";
                    session.save(new UserStandalone(AUTHID_STUDENT_PREFIX + i, firstName + " " + lastName, lastName + ", " + firstName, uidPrefix + i));
                }
                session.save(new UserStandalone(AUTHID_NO_GRADEBOOK, "Johnny Nobody", "Nobody, Johnny", AUTHID_NO_GRADEBOOK));
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

	public Set getUsers() {
        Set set = new HashSet();
        set.addAll(getHibernateTemplate().find("select user from UserStandalone as user"));
        return set;
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
