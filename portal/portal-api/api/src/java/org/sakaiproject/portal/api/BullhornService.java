/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.api;

import java.util.List;

import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.tool.api.Placement;

/**
 * Service for the bullhorn alerts at the top of the portal
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public interface BullhornService {

    /**
     * Used by the bullhorn alerts code
     */
    public static final String ACADEMIC = "ACADEMIC";

    /**
     * Used by the bullhorn alerts code
     */
    public static final String SOCIAL = "SOCIAL";

    /**
     * @param userId The user to retrieve the alert count for
     * @return the number of current social alerts for the specified user
     */
    public long getSocialAlertCount(String userId);

    /**
     * @param userId The user to retrieve alerts for
     * @return the list of current social alerts for the specified user
     */
    public List<BullhornAlert> getSocialAlerts(String userId);

    /**
     * @param userId The user to clear the alerts for
     * @return boolean to indicate success
     */
    public boolean clearAllSocialAlerts(String userId);

    /**
     * @param userId The user to retrieve the alert count for
     * @return the number of current academic alerts for the specified user
     */
    public long getAcademicAlertCount(String userId);

    /**
     * @param userId The user to retrieve alerts for
     * @return the list of current academic alerts for the specified user
     */
    public List<BullhornAlert> getAcademicAlerts(String userId);

    /**
     * @param userId The user to clear the alert for
     * @param alertId The alert to clear
     * @return boolean to indicate success
     */
    public boolean clearBullhornAlert(String userId, long alertId);

    /**
     * @param userId The user to clear the alerts for
     * @return boolean to indicate success
     */
    public boolean clearAllAcademicAlerts(String userId);

    /**
     * Toggles the bullhorn alert flag to be shown or not by saving
     * a boolean in the user preferences table.
     *
     * @param userId, User Id to toggle
     * @return The value of hidden preference after save
     */
    boolean toggleHideBullhornAlerts(String userId);

    /**
     * Check the user's preferences for if the alert is hidden or shown.
     *
     * @param userId, The user id to check
     * @return The value of the hide alert preference.
     */
    boolean isAlertHidden(final String userId);
}
