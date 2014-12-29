/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.component.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService.ConfigData;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigItem;


/**
 * 
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class ConfigDataImpl implements ConfigData {
    public int totalConfigItems = 0;
    public int registeredConfigItems = 0;
    public int unRegisteredConfigItems = 0;
    public String[] sources = new String[0];
    public List<ConfigItem> items = null;

    public ConfigDataImpl(List<ConfigItem> configItems) {
        ArrayList<ConfigItemImpl> cis = new ArrayList<ConfigItemImpl>(configItems.size());
        HashSet<String> sourceSet = new HashSet<String>();
        for (ConfigItem configItem : configItems) {
            if (configItem != null) {
                cis.add((ConfigItemImpl)configItem.copy());
                if (configItem.getSource() != null && !"UNKNOWN".equals(configItem.getSource())) {
                    sourceSet.add(configItem.getSource());
                }
                totalConfigItems++;
                if (configItem.isRegistered()) {
                    registeredConfigItems++;
                } else {
                    unRegisteredConfigItems++;
                }
            }
        }
        this.sources = sourceSet.toArray(new String[sourceSet.size()]);
        Collections.sort(cis);
        this.items = new ArrayList<ConfigItem>(cis);
    }
    public int getTotalConfigItems() {
        return totalConfigItems;
    }

    public int getRegisteredConfigItems() {
        return registeredConfigItems;
    }

    public int getUnRegisteredConfigItems() {
        return unRegisteredConfigItems;
    }

    public String[] getSources() {
        return sources;
    }

    public List<ConfigItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (ConfigItem ci : items) {
            i++;
            sb.append("  ");
            sb.append(i);
            sb.append(": ");
            sb.append(ci.toString());
            sb.append("\n");
        }
        return "Config items: "+totalConfigItems+" (" + registeredConfigItems + ", " + unRegisteredConfigItems + ")\n" + sb.toString();
    }

}
