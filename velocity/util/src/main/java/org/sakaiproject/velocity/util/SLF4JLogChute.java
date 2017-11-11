/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.velocity.util;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a LogChute for velocity to log to SLF4J
 */
public class SLF4JLogChute implements LogChute {

    private static final String RUNTIME_LOG_SLF4J_LOGGER = "runtime.log.logsystem.slf4j.logger";
    public static final String DEFAULT_LOGGER = "velocity";

    private Logger logger = null;

    /**
     * @see LogChute#init(RuntimeServices)
     */
    public void init(RuntimeServices rs) throws Exception {

        // override from velocity.properties
        String name = (String) rs.getProperty(RUNTIME_LOG_SLF4J_LOGGER);
        if (StringUtils.isBlank(name)) {
            // lets try Sakai convention
            ServletContext context = (ServletContext) rs.getApplicationAttribute("javax.servlet.ServletContext");
            if (context != null) {
                name = DEFAULT_LOGGER + "." + context.getServletContextName();
                name = name.replace("-", ".");
            } else {
                // default to "velocity"
                name = DEFAULT_LOGGER;
            }
        }
        logger = LoggerFactory.getLogger(name);
        log(INFO_ID, "SLF4JLogChute using logger '" + name + '\'');
    }

    /**
     * @see LogChute#log(int, String)
     */
    public void log(int level, String message) {
        switch (level) {
            case LogChute.WARN_ID:
                logger.warn(message);
                break;
            case LogChute.INFO_ID:
                logger.info(message);
                break;
            case LogChute.TRACE_ID:
                logger.trace(message);
                break;
            case LogChute.ERROR_ID:
                logger.error(message);
                break;
            case LogChute.DEBUG_ID:
            default:
                logger.debug(message);
                break;
        }
    }

    /**
     * @see LogChute#log(int, String, Throwable)
     */
    public void log(int level, String message, Throwable t) {
        switch (level) {
            case LogChute.WARN_ID:
                logger.warn(message, t);
                break;
            case LogChute.INFO_ID:
                logger.info(message, t);
                break;
            case LogChute.TRACE_ID:
                logger.trace(message, t);
                break;
            case LogChute.ERROR_ID:
                logger.error(message, t);
                break;
            case LogChute.DEBUG_ID:
            default:
                logger.debug(message, t);
                break;
        }
    }

    /**
     * @see LogChute#isLevelEnabled(int)
     */
    public boolean isLevelEnabled(int level) {
        switch (level) {
            case LogChute.DEBUG_ID:
                return logger.isDebugEnabled();
            case LogChute.INFO_ID:
                return logger.isInfoEnabled();
            case LogChute.TRACE_ID:
                return logger.isTraceEnabled();
            case LogChute.WARN_ID:
                return logger.isWarnEnabled();
            case LogChute.ERROR_ID:
                return logger.isErrorEnabled();
            default:
                return true;
        }
    }
}