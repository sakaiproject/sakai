/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.springframework.orm.hibernate;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.jmx.StatisticsService;

/**
 * @author ieb
 */
public class HibernateJMXAgent
{
	private static final Log log = LogFactory.getLog(HibernateJMXAgent.class);

	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

	private SessionFactory sessionFactory;

	public MBeanServer getMBeanServer()
	{
		return mBeanServer;
	}

	public void init() throws MalformedObjectNameException, NullPointerException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException
	{
		log.info("Registering Hibernate Session Factory with JMX " + mBeanServer);

		ObjectName on = new ObjectName(
				"Hibernate:type=statistics,application=HibernateSakai");

		// Enable Hibernate JMX Statistics
		StatisticsService statsMBean = new StatisticsService();
		statsMBean.setSessionFactory(sessionFactory);
		statsMBean.setStatisticsEnabled(true);
		mBeanServer.registerMBean(statsMBean, on);
	}

	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	/**
	 * @param sessionFactory
	 *        the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}
}
