/**
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti.impl;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lti.api.SiteEmailPreferenceSetter;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;

/**
 *  @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
@Slf4j
public class SiteEmailPreferenceSetterImpl implements SiteEmailPreferenceSetter {

    private PreferencesService preferencesService = null;
    public void setPreferencesService(PreferencesService  preferencesService) {
        this.preferencesService = preferencesService;
    }

    public void setupUserEmailPreferenceForSite(Map payload, User user, Site site, boolean isTrustedConsumer) {

    	if (isTrustedConsumer) return;

        // Set up user's email preference.
        String emailDeliveryPreference = (String) payload.get("ext_email_delivery_preference");
        if (emailDeliveryPreference != null && emailDeliveryPreference.length() > 0) {

            try {

                PreferencesEdit pe = null;
                try {
                    pe = preferencesService.edit(user.getId());
                } catch(IdUnusedException idue) {
                    pe = preferencesService.add(user.getId());
                }
                
                if (emailDeliveryPreference != null && emailDeliveryPreference.length() > 0) {

                    int notificationPref = NotificationService.PREF_IMMEDIATE;

                    if (emailDeliveryPreference.equals("none")) {
                        notificationPref = NotificationService.PREF_NONE;
                    } else if (emailDeliveryPreference.equals("digest")) {
                        notificationPref = NotificationService.PREF_DIGEST;
                    }

                    String toolId = ((String) payload.get("tool_id")).replaceFirst("\\.",":");

                    ResourcePropertiesEdit propsEdit = pe.getPropertiesEdit(NotificationService.PREFS_TYPE + toolId + "_override");
                    propsEdit.removeProperty(site.getId());
                    propsEdit.addProperty(site.getId(), Integer.toString(notificationPref));
                }

                preferencesService.commit(pe);
            } catch (Exception e) {
                log.error("Failed to setup launcher's locale and/or email preference.",e);
            }
        }
    }
}
