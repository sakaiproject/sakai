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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.log.impl;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.log.api.LogConfigurationManager;
import org.sakaiproject.log.api.LogPermissionException;

/**
 * <p>
 * Log4jConfigurationManager lets us configure the log4j system with overrides from sakai.properties. Someday it might even have a service API for other fun things!
 * </p>
 */
@Slf4j
public abstract class Log4jConfigurationManager implements LogConfigurationManager {

	// Configuration: enable special log handling or not.
	protected boolean enabled = true;

	// Map by logger name of set of message string starts to ignore.
	protected Map<String, Set<String>> ignore = new HashMap<>();

	// Log4j logger context
	private LoggerContext loggerContext;

	protected abstract ServerConfigurationService serverConfigurationService();
	protected abstract SecurityService securityService();

	public void setEnabled(String enabled) {
		this.enabled = Boolean.parseBoolean(enabled);
	}

	public void setIgnore(Map<String, Set<String>> ignore) {
		this.ignore = ignore;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init() {
		if (enabled) {
			if (loggerContext == null) {
				loggerContext = LoggerContext.getContext(false);
			}
		    // Load optional log4j.properties file from sakai home
		    String log4jConfigFilePath = serverConfigurationService().getSakaiHomePath() + "log4j2.properties";
		    if (StringUtils.isNotEmpty(log4jConfigFilePath)) {
		    	loggerContext.setConfigLocation(new File(log4jConfigFilePath).toURI());
		    }

			// slip in our appender
			Appender appender = loggerContext.getRootLogger().getAppenders().get("Sakai");
			if (appender != null) {
				loggerContext.getRootLogger().removeAppender(appender);
				loggerContext.getRootLogger().addAppender(new SakaiAppender(appender));
			}

			// set the log4j logging system with some overrides from sakai.properties
			// each in the form LEVEL.NAME where LEVEL is OFF | TRACE | DEBUG | INFO | WARN | ERROR | FATAL | ALL, name is the logger name (such as org.sakaiproject)
			// example:
			// log.config.count=3
			// log.config.1 = ALL.org.sakaiproject.log.impl
			// log.config.2 = OFF.org.sakaiproject
			// log.config.3 = DEBUG.org.sakaiproject.db.impl
			String[] configs = serverConfigurationService().getStrings("log.config");
			if (configs != null) {
				for (String config : configs) {
					String[] parts = StringUtils.split(config, ".", 2);
					if ((parts != null) && (parts.length >= 2)) {
						doSetLogLevel(parts[0], parts[1]);
					} else {
						log.warn("Invalid log.config entry: {}, ignoring", config);
					}
				}
			}
		}

		log.info("Log4j configuration is enabled: {}", enabled);
	}

	/**
	 * Final cleanup.
	 */
	public void destroy() {
		log.info("Log4j shutdown");
	}

	/**
	 * Set the log level
	 * 
	 * @param levelName
	 *        The log level string - one of OFF | TRACE | DEBUG | INFO | WARN | ERROR | FATAL | ALL
	 * @param loggerName
	 *        The logger name.
	 */
	protected void doSetLogLevel(String levelName, String loggerName) {
		Configuration configuration = loggerContext.getConfiguration();
		Logger logger = loggerContext.getLogger(loggerName);
		LoggerConfig config = configuration.getLoggerConfig(logger.getName());
		Level level = Level.toLevel(levelName, Level.INFO);
		if (config.getName().equals(logger.getName())) {
			config.setLevel(level);
			log.info("Logging for [{}] change to level {}", logger.getName(), level.toString());
		} else {
			LoggerConfig cfg = new LoggerConfig(logger.getName(), level, true);
			cfg.setParent(config);
			configuration.addLogger(logger.getName(), cfg);
			log.info("Adding logging config for [{}] with level {}", logger.getName(), level.toString());
		}
		loggerContext.updateLoggers();
	}

	@Override
	public void setLogLevel(String level, String loggerName) throws LogPermissionException {
		// check that this is a "super" user with the security service
		if (!securityService().isSuperUser()) {
			throw new LogPermissionException();
		}

		doSetLogLevel(level, loggerName);
	}

	/**
	 * An appender that wraps the log4j appender and adds the ability to ignore certain messages
	 * that start with a specific text
	 */
	class SakaiAppender implements Appender {

		final private Appender originalAppender;

		public SakaiAppender(Appender appender) {
			originalAppender = appender;
		}

		@Override
		public void append(LogEvent event) {
			String logger = event.getLoggerName();
			String message = event.getMessage().getFormattedMessage();

			Set<String> toIgnore = ignore.get(logger);
			if (toIgnore != null) {
				// if any of the strings in the set start our message, skip it
				if (toIgnore.stream().anyMatch(message::startsWith)) return;
			}

			originalAppender.append(event);
		}

		@Override
		public Layout<? extends Serializable> getLayout() {
			return originalAppender.getLayout();
		}

		@Override
		public boolean ignoreExceptions() {
			return false;
		}

		@Override
		public ErrorHandler getHandler() {
			return originalAppender.getHandler();
		}

		@Override
		public void setHandler(ErrorHandler handler) {
			originalAppender.setHandler(handler);
		}

		@Override
		public String getName() {
			return originalAppender.getName();
		}

		@Override
		public State getState() {
			return originalAppender.getState();
		}

		@Override
		public void initialize() {
			originalAppender.initialize();
		}

		@Override
		public void start() {
			originalAppender.start();
		}

		@Override
		public void stop() {
			originalAppender.stop();
		}

		@Override
		public boolean isStarted() {
			return originalAppender.isStarted();
		}

		@Override
		public boolean isStopped() {
			return originalAppender.isStopped();
		}
	}
}
