/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.model.RWikiPropertyImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiPropertyDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiProperty;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component

public class RWikiPropertyDaoImpl extends HibernateDaoSupport implements
		RWikiPropertyDao
{

	private static Log log = LogFactory.getLog(RWikiPropertyDaoImpl.class);

	private String schemaVersion;

	public RWikiProperty getProperty(final String name)
	{
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback callback = new HibernateCallback()
			{
				public Object doInHibernate(Session session)
						throws HibernateException
				{
					return session.createCriteria(RWikiProperty.class).add(
							Expression.eq("name", name)).list();
				}

			};
			List found = (List) getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Found " + found.size() + " objects with name "
							+ name);
				}
				return null;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Found " + found.size() + " objects with name "
						+ name + " returning most recent one.");
			}
			return (RWikiProperty) found.get(0);
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiPropertyDaoImpl.getContentObject: "
					+ name, start, finish);
		}
	}

	public RWikiProperty createProperty()
	{
		return new RWikiPropertyImpl();
	}

	public void update(RWikiProperty property)
	{
		getHibernateTemplate().saveOrUpdate(property);
	}

	/**
	 * @return Returns the schemaVersion.
	 */
	public String getSchemaVersion()
	{
		return schemaVersion;
	}

	/**
	 * @param schemaVersion
	 *        The schemaVersion to set.
	 */
	public void setSchemaVersion(String schemaVersion)
	{
		this.schemaVersion = schemaVersion;
	}

}
