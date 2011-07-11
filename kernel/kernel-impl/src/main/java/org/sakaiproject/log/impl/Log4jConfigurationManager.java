/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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

package org.sakaiproject.log.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.log.api.LogConfigurationManager;
import org.sakaiproject.log.api.LogPermissionException;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * Log4jConfigurationManager lets us configure the log4j system with overrides from sakai.properties. Someday it might even have a service API for other fun things!
 * </p>
 */
public abstract class Log4jConfigurationManager implements LogConfigurationManager
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(Log4jConfigurationManager.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the UsageSessionService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Configuration: enable special log handling or not. */
	protected boolean m_enabled = true;

	/**
	 * Configuration: enable special log handling or not.
	 * 
	 * @param value
	 *        the setting (true of false) for enabled.
	 */
	public void setEnabled(String value)
	{
		m_enabled = Boolean.valueOf(value).booleanValue();
	}

	/** Map by logger name of set of message string starts to ignore. */
	protected Map m_ignore = new HashMap();

	public void setIgnore(Map ignore)
	{
		m_ignore = ignore;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		if (m_enabled)
		{
			// slip in our appender
			Appender a = Logger.getRootLogger().getAppender("Sakai");
			if (a != null)
			{
				Logger.getRootLogger().removeAppender(a);
				Logger.getRootLogger().addAppender(new SakaiAppender(a));
			}

			// set the log4j logging system with some overrides from sakai.properties
			// each in the form LEVEL.NAME where LEVEL is OFF | TRACE | DEBUG | INFO | WARN | ERROR | FATAL | ALL, name is the logger name (such as org.sakaiproject)
			// example:
			// log.config.count=3
			// log.config.1 = ALL.org.sakaiproject.log.impl
			// log.config.2 = OFF.org.sakaiproject
			// log.config.3 = DEBUG.org.sakaiproject.db.impl
			String configs[] = serverConfigurationService().getStrings("log.config");
			if (configs != null)
			{
				for (int i = 0; i < configs.length; i++)
				{
					String parts[] = StringUtil.splitFirst(configs[i], ".");
					if ((parts != null) && (parts.length == 2))
					{
						doSetLogLevel(parts[0], parts[1]);
					}
					else
					{
						M_log.warn("invalid log.config entry: ignoring: " + configs[i]);
					}
				}
			}
		}

		M_log.info("init(): enabled: " + m_enabled);
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * Set the log level
	 * 
	 * @param level
	 *        The log level string - one of OFF | TRACE | DEBUG | INFO | WARN | ERROR | FATAL | ALL
	 * @param loggerName
	 *        The logger name.
	 */
	protected boolean doSetLogLevel(String level, String loggerName)
	{
		if (level.equals("OFF"))
		{
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.OFF);
				M_log.info("OFF logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else if (level.equals("TRACE"))
		{
			// Note: log4j has nothing below debug
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.DEBUG);
				M_log.info("TRACE (DEBUG) logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else if (level.equals("DEBUG"))
		{
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.DEBUG);
				M_log.info("DEBUG logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else if (level.equals("INFO"))
		{
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.INFO);
				M_log.info("INFO logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else if (level.equals("WARN"))
		{
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.WARN);
				M_log.info("WARN logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else if (level.equals("ERROR"))
		{
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.ERROR);
				M_log.info("ERROR logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else if (level.equals("FATAL"))
		{
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.FATAL);
				M_log.info("FATAL logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else if (level.equals("ALL"))
		{
			Logger logger = Logger.getLogger(loggerName);
			if (logger != null)
			{
				logger.setLevel(org.apache.log4j.Level.ALL);
				M_log.info("ALL logging for: " + loggerName);
			}
			else
			{
				M_log.warn("no logger found: ignoring: " + loggerName);
			}
		}
		else
		{
			M_log.warn("invalid log level: ignoring: " + level);
			return false;
		}

		return true;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: LogConfigurationManager
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public boolean setLogLevel(String level, String loggerName) throws LogPermissionException
	{
		// check that this is a "super" user with the security service
		if (!securityService().isSuperUser())
		{
			throw new LogPermissionException();
		}

		return doSetLogLevel(level, loggerName);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Our special Appender
	 *********************************************************************************************************************************************************************************************************************************************************/

	class SakaiAppender implements org.apache.log4j.Appender
	{
		protected Appender m_other = null;

		public SakaiAppender(Appender other)
		{
			m_other = other;
		}

		public void addFilter(Filter arg0)
		{
			m_other.addFilter(arg0);
		}

		public void clearFilters()
		{
			m_other.clearFilters();
		}

		public void close()
		{
			m_other.close();
		}

		public void doAppend(LoggingEvent arg0)
		{
			String logger = arg0.getLoggerName();
			String message = arg0.getRenderedMessage();
			Level level = arg0.getLevel();

			Set toIgnore = (Set) m_ignore.get(logger);
			if (toIgnore != null)
			{
				// if any of the strings in the set start our message, skip it
				for (Iterator i = toIgnore.iterator(); i.hasNext();)
				{
					String start = (String) i.next();
					if (message.startsWith(start)) return;
				}
			}

			m_other.doAppend(arg0);
		}

		public ErrorHandler getErrorHandler()
		{
			return m_other.getErrorHandler();
		}

		public Filter getFilter()
		{
			return m_other.getFilter();
		}

		public Layout getLayout()
		{
			return m_other.getLayout();
		}

		public String getName()
		{
			return m_other.getName();
		}

		public boolean requiresLayout()
		{
			return m_other.requiresLayout();
		}

		public void setErrorHandler(ErrorHandler arg0)
		{
			m_other.setErrorHandler(arg0);
		}

		public void setLayout(Layout arg0)
		{
			m_other.setLayout(arg0);
		}

		public void setName(String arg0)
		{
			m_other.setName(arg0);
		}
	}
}
