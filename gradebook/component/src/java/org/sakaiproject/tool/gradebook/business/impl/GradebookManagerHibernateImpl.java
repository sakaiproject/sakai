/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.business.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.StaleObjectStateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.springframework.orm.hibernate.HibernateCallback;

/**
 * Manages Gradebook persistence via hibernate.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class GradebookManagerHibernateImpl extends BaseHibernateManager
    implements GradebookManager {

    private static final Log log = LogFactory.getLog(GradebookManagerHibernateImpl.class);

    private GradeManager gradeManager;

    public Gradebook getGradebook(String uid) throws GradebookNotFoundException {
    	Gradebook gradebook = null;
    	List list = getHibernateTemplate().find("from Gradebook as gb where gb.uid=?",
    		uid, Hibernate.STRING);
		if (list.size() == 1) {
			return (Gradebook)list.get(0);
		} else {
            throw new GradebookNotFoundException("Could not find gradebook uid=" + uid);
        }
    }

	public void updateGradebook(final Gradebook gradebook) throws StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                // Get the gradebook and selected mapping from persistence
                Gradebook gradebookFromPersistence = (Gradebook)session.load(
                        gradebook.getClass(), gradebook.getId());
                GradeMapping mappingFromPersistence = gradebookFromPersistence.getSelectedGradeMapping();

                // If the mapping has changed, and there are explicitly entered
                // course grade records, disallow this update.
                if (!mappingFromPersistence.getId().equals(gradebook.getSelectedGradeMapping().getId())) {
                    if(gradeManager.isExplicitlyEnteredCourseGradeRecords(gradebook.getId())) {
                        throw new IllegalStateException("Selected grade mapping can not be changed, since explicit course grades exist.");
                    }
                }

                // Evict the persisted objects from the session and update the gradebook
                // so the new grade mapping is used in the sort column update
                //session.evict(mappingFromPersistence);
                for(Iterator iter = gradebookFromPersistence.getGradeMappings().iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }
                session.evict(gradebookFromPersistence);
                try {
                    session.update(gradebook);
                    session.flush();
                } catch (StaleObjectStateException e) {
                    throw new StaleObjectModificationException(e);
                }

                // If the same mapping is selected, but it has been modified, we need
                // to trigger a sort value update on the explicitly entered course grades
                if(!mappingFromPersistence.equals(gradebook.getSelectedGradeMapping())) {
					gradeManager.updateCourseGradeRecordSortValues(gradebook.getId(), true);
                }

                return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

    public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Assignment asn = (Assignment)session.load(Assignment.class, assignmentId);
                asn.setRemoved(true);
                session.update(asn);
                if(logger.isInfoEnabled()) logger.info("Assignment " + asn.getName() + " has been removed from " + asn.getGradebook());

                // Update the course grade records
                Gradebook gradebook = asn.getGradebook();
                recalculateCourseGradeRecords(gradebook, session);

                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    public Gradebook getGradebook(Long id) {
        return (Gradebook)getHibernateTemplate().load(Gradebook.class, id);
    }

    public String getGradebookUid(Long id) {
        return ((Gradebook)getHibernateTemplate().load(Gradebook.class, id)).getUid();
    }

	public List getEnrollments(Long gradebookId) {
        return new ArrayList(courseManagement.getEnrollments(getGradebookUid(gradebookId)));
	}

	public void setGradeManager(GradeManager gradeManager) {
		this.gradeManager = gradeManager;
	}
}


