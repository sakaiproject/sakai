/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation
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

package org.sakaiproject.tool.gradebook.test.support;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.sakaiproject.tool.gradebook.CourseGrade;
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
