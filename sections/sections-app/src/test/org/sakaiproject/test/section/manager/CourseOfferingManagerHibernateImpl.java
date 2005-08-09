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

package org.sakaiproject.test.section.manager;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.uuid.UuidManager;
import org.sakaiproject.tool.section.CourseOfferingImpl;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class CourseOfferingManagerHibernateImpl extends HibernateDaoSupport
	implements CourseOfferingManager {

	private static final Log log = LogFactory.getLog(CourseOfferingManagerHibernateImpl.class);

	protected UuidManager uuidManager;

	public void createCourseOffering(final String siteContext, final String title,
			final boolean selfRegAllowed, final boolean selfSwitchingAllowed,
			final boolean externallyManaged) {
		
		if(log.isDebugEnabled()) log.debug("Creating a new course offering named " + title);

		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException ,SQLException {
				CourseOfferingImpl course = new CourseOfferingImpl();
        		course.setExternallyManaged(false);
        		course.setSelfRegistrationAllowed(selfRegAllowed);
        		course.setSelfSwitchingAllowed(selfSwitchingAllowed);
        		course.setSiteContext(siteContext);
        		course.setTitle(title);
        		course.setUuid(uuidManager.createUuid());
        		session.save(course);
				return null;
			};
		};

		getHibernateTemplate().execute(hc);
	}

	// Dependency injection

	public void setUuidManager(UuidManager uuidManager) {
		this.uuidManager = uuidManager;
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
