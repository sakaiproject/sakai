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

package org.sakaiproject.tool.gradebook.standalone.impl;

import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.standalone.FrameworkManager;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class FrameworkManagerImpl extends HibernateDaoSupport implements FrameworkManager {
	private static final Log logger = LogFactory.getLog(FrameworkManagerImpl.class);

	public List getAccessibleGradebooks(final String userUid) {
		return (List)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List gradebooks = session.find(
					"select distinct gb from Gradebook as gb, EnrollmentStandalone as enr, TeachingAssignmentStandalone as ta, UserStandalone as user where user.userUid=? and ((ta.user=user and ta.gradebook=gb) or (enr.user=user and enr.gradebook=gb)) order by gb.name",
					new Object[] {userUid},
					new Type[] {Hibernate.STRING});
				if (logger.isInfoEnabled()) {
					logger.info("userUid " + userUid + " has " + gradebooks.size() + " gradebooks");
					for (Iterator iter = gradebooks.iterator(); iter.hasNext(); ) {
						Gradebook gb = (Gradebook)iter.next();
						logger.info("  gradebook Id=" + gb.getId() + ", Uid=" + gb.getUid() + ", name=" + gb.getName());
					}
				}
				return gradebooks;
			}
		});
	}

}


