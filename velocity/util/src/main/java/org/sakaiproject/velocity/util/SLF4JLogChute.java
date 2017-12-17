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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * Provides a LogChute for velocity to log to SLF4J
 */
@Slf4j
public class SLF4JLogChute implements LogChute {

    private static final String RUNTIME_LOG_SLF4J_LOGGER = "runtime.log.logsystem.slf4j.logger";
    public static final String DEFAULT_LOGGER = "velocity";

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
        log(INFO_ID, "SLF4JLogChute using logger '" + name + '\'');
    }

    /**
     * @see LogChute#log(int, String)
     */
    public void log(int level, String message) {
        switch (level) {
            case LogChute.WARN_ID:
                log.warn(message);
                break;
            case LogChute.INFO_ID:
                log.info(message);
                break;
            case LogChute.TRACE_ID:
                log.trace(message);
                break;
            case LogChute.ERROR_ID:
                log.error(message);
                break;
            case LogChute.DEBUG_ID:
            default:
                log.debug(message);
                break;
        }
    }

    /**
     * @see LogChute#log(int, String, Throwable)
     */
    public void log(int level, String message, Throwable t) {
        switch (level) {
            case LogChute.WARN_ID:
                log.warn(message, t);
                break;
            case LogChute.INFO_ID:
                log.info(message, t);
                break;
            case LogChute.TRACE_ID:
                log.trace(message, t);
                break;
            case LogChute.ERROR_ID:
                log.error(message, t);
                break;
            case LogChute.DEBUG_ID:
            default:
                log.debug(message, t);
                break;
        }
    }

    /**
     * @see LogChute#isLevelEnabled(int)
     */
    public boolean isLevelEnabled(int level) {
        switch (level) {
            case LogChute.DEBUG_ID:
                return log.isDebugEnabled();
            case LogChute.INFO_ID:
                return log.isInfoEnabled();
            case LogChute.TRACE_ID:
                return log.isTraceEnabled();
            case LogChute.WARN_ID:
                return log.isWarnEnabled();
            case LogChute.ERROR_ID:
                return log.isErrorEnabled();
            default:
                return true;
        }
    }
}