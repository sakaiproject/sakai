/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://www.opensource.org/licenses/ecl1.php
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.gradebook.test.support;

import java.util.*;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.type.Type;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookExistsException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.LetterGradeMapping;
import org.sakaiproject.tool.gradebook.LetterGradePlusMinusMapping;
import org.sakaiproject.tool.gradebook.PassNotPassMapping;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 */
public class BackwardCompatabilityBusinessImpl extends HibernateDaoSupport implements BackwardCompatabilityBusiness {
    private static final Log log = LogFactory.getLog(BackwardCompatabilityBusinessImpl.class);

	public void addGradebook(final String uid, final String name) {
        getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// Create and save the gradebook
				Gradebook gradebook = new Gradebook(name);
				gradebook.setUid(uid);
				gradebook.setId((Long)session.save(gradebook)); // Grab the new id

				// Create the course grade for the gradebook
				CourseGrade cg = new CourseGrade();
				cg.setGradebook(gradebook);
				session.save(cg);

				// According to the specification, Display Assignment Grades is
				// on by default, and Display course grade is off.
				gradebook.setAssignmentsDisplayed(true);
				gradebook.setCourseGradeDisplayed(false);

				// Add and save the grade mappings

				// Set gms = gradebook.getAvailableGradeMappings();
				Set gms = new HashSet();
				gms.add(new LetterGradeMapping());
				gms.add(new LetterGradePlusMinusMapping());
				gms.add(new PassNotPassMapping());

				for(Iterator iter = gms.iterator(); iter.hasNext();) {
					GradeMapping gm = (GradeMapping)iter.next();
					gm.setGradebook(gradebook);
					gm.setDefaultValues(); // Populate the grade map
					gm.setId((Long)session.save(gm)); // grab the new id
//					if(gm.isDefault()) {
					if(gm.getClass().equals(LetterGradePlusMinusMapping.class)) {
						gradebook.setSelectedGradeMapping(gm);
					}
				}
				gradebook.setGradeMappings(gms);

				// Update the gradebook with the new selected grade mapping
				session.update(gradebook);
				return null;
			}
		});
	}
}
