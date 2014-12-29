/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 Sakai Foundation
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

package org.sakaiproject.util;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigHistory;


/**
 * This is a basic version of the Config history to make it easy for people to 
 * create items to place in the config history
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class BasicConfigHistory implements ConfigHistory {
    protected int version = 0;
    /**
     * This is the time when the config was changed
     */
    protected long timestamp = 0;
    protected String source = ServerConfigurationService.UNKNOWN;
    /**
     * This is the previous value of the config (the value from before the timestamp)
     */
    protected Object value = null;
    protected boolean secured = false;

    /**
     * Make a basic history config to store in the history of a config item
     * 
     * @param version the version (always > 0)
     * @param source the source name (default to UNKNOWN)
     * @param value the previous config value
     */
    public BasicConfigHistory(int version, String source, Object value) {
        if (version <= 0) {
            throw new IllegalArgumentException("minimum version is 1: version="+version);
        }
        this.version = version;
        if (source != null && !"".equals(source)) {
            this.source = source;
        }
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }
    
    protected BasicConfigHistory(int version, long timestamp, String source, Object value) {
        this(version, source, value);
        this.timestamp = timestamp;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService.ConfigHistory#getVersion()
     */
    public int getVersion() {
        return version;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService.ConfigHistory#getTimestamp()
     */
    public long getTimestamp() {
        return timestamp;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService.ConfigHistory#getSource()
     */
    public String getSource() {
        return source;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService.ConfigHistory#getValue()
     */
    public Object getValue() {
        return value;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public boolean isSecured() {
        return secured;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BasicConfigHistory other = (BasicConfigHistory) obj;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return source + "(v"+version+","+timestamp+"):" + (this.secured ? "**SECURITY**" : (String) this.value);
    }

}
