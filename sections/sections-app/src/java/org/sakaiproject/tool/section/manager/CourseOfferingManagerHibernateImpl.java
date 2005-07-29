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

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.uuid.UuidManager;
import org.sakaiproject.api.section.coursemanagement.CourseOffering;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.tool.section.CourseOfferingImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class CourseOfferingManagerHibernateImpl extends HibernateDaoSupport
	implements CourseOfferingManager {

	private static final Log log = LogFactory.getLog(CourseOfferingManagerHibernateImpl.class);
	
	protected Authn authn;
	protected UuidManager uuidManager;
	
	public CourseOffering createCourseOffering(final String context, final String title,
			final boolean switchingAllowed, final boolean selfRegAllowed) {
	       HibernateCallback hc = new HibernateCallback(){
	            public Object doInHibernate(Session session) throws HibernateException {
	            	CourseOfferingImpl course = new CourseOfferingImpl();
	            	course.setTitle(title);
	            	course.setUuid(uuidManager.createUuid());
	            	course.setSectionSwitchingAllowed(switchingAllowed);
	            	course.setSelfRegistrationAllowed(selfRegAllowed);
	            	course.setContext(context);
	            	session.save(course);
	            	return course;
	            }
	       };
	       return (CourseOffering)getHibernateTemplate().execute(hc);
	}
	
	/**
	 * Assuming one course offering per site context.
	 */
	public String getCourseOfferingUuid(final String context) {
	       HibernateCallback hc = new HibernateCallback(){
	            public Object doInHibernate(Session session) throws HibernateException {
	            	Query q = session.createQuery("select course.uuid from CourseOfferingImpl as course where course.context=:context");
	            	q.setParameter("context", context);
	            	List list = q.list();
	            	if(list.size() == 0) {
	            		log.error("There is no course offering associated with this context");
	            		return null;
	            	} else {
	            		return (String)list.get(0);
	            	}
	            }
	       };
	       return (String)getHibernateTemplate().execute(hc);
	}

	
	// Dependency injection
	public void setAuthn(Authn authn) {
		this.authn = authn;
	}
	
	public void setUuidManager(UuidManager uuidManager) {
		this.uuidManager = uuidManager;
	}
}



/**********************************************************************************
 * $Id: $
 *********************************************************************************/
