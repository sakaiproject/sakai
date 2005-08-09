/**********************************************************************************
*
* $Id: $
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
package org.sakaiproject.tool.section.manager;

import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.uuid.UuidManager;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.CourseOffering;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.exception.MembershipException;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.tool.section.CourseSectionImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class SectionManagerHibernateImpl extends HibernateDaoSupport implements
        SectionManager {

	private static final Log log = LogFactory.getLog(SectionManagerHibernateImpl.class);
	
	// Fields configured via dep. injection
	protected UuidManager uuidManager;
	protected SectionAwareness sectionAwareness;
    protected Authn authn;
    protected Context context;
    
    public void joinSection(String sectionId) {
        // TODO Auto-generated method stub

    }

    public void dropSection(String sectionId) {
        // TODO Auto-generated method stub

    }

    public void switchSection(String newSectionId) {
        // TODO Auto-generated method stub

    }

    public void addSectionMembership(String userId, Role role, String sectionId)
            throws MembershipException {
        // TODO Auto-generated method stub

    }

	public void setSectionMemberships(Set userIds, Role role, String sectionId) {
		// TODO Auto-generated method stub
	}

    public void dropSectionMembership(String userId, String sectionId) {
        // TODO Auto-generated method stub

    }

	public int getTotalEnrollments(String sectionId) {
		// TODO Auto-generated method stub
		return 0;
	}

    public CourseSection addSection(final String courseOfferingUuid, final String title,
            final String meetingTimes, final int maxEnrollments, final String location, final String category) {
    	final String uuid = uuidManager.createUuid();
        if(log.isDebugEnabled()) log.debug("Creating section with uuid = " + uuid);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
                    // Get the primary section
                	Query q = session.createQuery("from CourseOfferingImpl as course where course.uuid=:uuid");
                	q.setParameter("uuid", courseOfferingUuid);
                	List list = q.list();
                	if(list.size() == 0) {
                		throw new IllegalArgumentException("Course offering uuid = " + courseOfferingUuid + "does not exist");
                	}
                	CourseOffering course = (CourseOffering)list.get(0);
                	String uuid = uuidManager.createUuid();
                	CourseSectionImpl section = new CourseSectionImpl(course, title, category, meetingTimes, location, uuid);
                    session.save(section);
                    return section;
            }
        };
            
        return (CourseSection)getHibernateTemplate().execute(hc);
    }

    public void updateSection(CourseSection section) {
        // TODO Auto-generated method stub

    }

    public void disbandSection(final CourseSection section) {
        if(log.isDebugEnabled()) log.debug("Disbanding " + section);
        HibernateCallback hc = new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException {
            	session.delete(section);
            	return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    public boolean isSelfRegistrationAllowed(String courseOfferingId) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSelfRegistrationAllowed(String courseOfferingId,
            boolean allowed) {
        // TODO Auto-generated method stub

    }

    public boolean isSectionSwitchingAllowed(String courseId) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSectionSwitchingAllowed(String courseId, boolean allowed) {
        // TODO Auto-generated method stub

    }

	// Field accessors
	
    public SectionAwareness getSectionAwareness() {
    	return sectionAwareness;
    }

    // Dependency injection
    public void setSectionAwareness(SectionAwareness sectionAwareness) {
        this.sectionAwareness = sectionAwareness;
    }
    
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }

	public void setUuidManager(UuidManager uuidManager) {
		this.uuidManager = uuidManager;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}

}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
