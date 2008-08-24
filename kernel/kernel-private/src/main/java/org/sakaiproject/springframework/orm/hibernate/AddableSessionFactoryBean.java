/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.ClassicAvgFunction;
import org.hibernate.dialect.function.ClassicCountFunction;
import org.hibernate.dialect.function.ClassicSumFunction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class AddableSessionFactoryBean extends LocalSessionFactoryBean implements ApplicationContextAware
{
	protected final transient Log logger = LogFactory.getLog(getClass());

	private ApplicationContext applicationContext;

	/**
	 * To be implemented by subclasses that want to to perform custom post-processing of the Configuration object after this FactoryBean performed its default initialization.
	 * 
	 * @param config
	 *        the current Configuration object
	 * @throws org.hibernate.HibernateException
	 *         in case of Hibernate initialization errors
	 */
	protected void postProcessConfiguration(Configuration config) throws HibernateException
	{
		super.postProcessConfiguration(config);

		String[] names = applicationContext.getBeanNamesForType(AdditionalHibernateMappings.class, false, false);

		try
		{
			List beans = new ArrayList();
			for (int i = 0; i < names.length; i++)
			{
				AdditionalHibernateMappings mappings = (AdditionalHibernateMappings) applicationContext.getBean(names[i]);

				beans.add(mappings);
			}

			Collections.sort(beans);

			for (Iterator i = beans.iterator(); i.hasNext();)
			{
				AdditionalHibernateMappings mappings = (AdditionalHibernateMappings) i.next();
				mappings.processConfig(config);
			}

		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Set the ApplicationContext that this object runs in. Normally this call will be used to initialize the object.
	 * <p>
	 * Invoked after population of normal bean properties but before an init callback like InitializingBean's afterPropertiesSet or a custom init-method. Invoked after ResourceLoaderAware's setResourceLoader.
	 * 
	 * @param applicationContext
	 *        ApplicationContext object to be used by this object
	 * @throws org.springframework.context.ApplicationContextException
	 *         in case of applicationContext initialization errors
	 * @throws org.springframework.beans.BeansException
	 *         if thrown by application applicationContext methods
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}
	
	/**
	 * Provide backwards compatibility with Hibernate 3.1.x behavior for
	 * aggregate functions.
	 */
	@Override
	protected Configuration newConfiguration() throws HibernateException 
	{
		final Configuration classicCfg = new Configuration();
		classicCfg.addSqlFunction("count", new ClassicCountFunction());
		classicCfg.addSqlFunction("avg", new ClassicAvgFunction());
		classicCfg.addSqlFunction("sum", new ClassicSumFunction());
		return classicCfg;
	}
}
