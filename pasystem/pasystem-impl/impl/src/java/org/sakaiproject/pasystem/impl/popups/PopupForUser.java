/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
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

package org.sakaiproject.pasystem.impl.popups;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.pasystem.api.Popup;
import org.sakaiproject.pasystem.impl.common.DB;
import org.sakaiproject.pasystem.impl.common.DBAction;
import org.sakaiproject.pasystem.impl.common.DBConnection;
import org.sakaiproject.pasystem.impl.common.DBResults;
import org.sakaiproject.user.api.User;

/**
 * Determine which popup (if any) should be shown to a given user.
 */
@Slf4j
public class PopupForUser {

    private User user;
    private String userId;

    public PopupForUser(User currentUser) {
        user = currentUser;

        if (user != null && user.getId() != null) {
            userId = user.getId();
        } else {
            user = null;
        }
    }

    /**
     * Return a popup that the current user hasn't seen lately.
     */
    public Popup getPopup() {
        if (user == null) {
            // No user.
            return Popup.createNullPopup();
        }

        String sql = ("SELECT popup.uuid, popup.descriptor, popup.start_time, popup.end_time, popup.open_campaign, content.template_content " +

                // Find a popup screen
                " FROM pasystem_popup_screens popup" +

                // And its content
                " INNER JOIN pasystem_popup_content content on content.uuid = popup.uuid" +

                // That is either assigned to the current user
                " LEFT OUTER join pasystem_popup_assign assign " +
                " on assign.uuid = popup.uuid AND assign.user_id = ?" +

                // Which the current user hasn't yet dismissed
                " LEFT OUTER JOIN pasystem_popup_dismissed dismissed " +
                " on dismissed.uuid = popup.uuid AND dismissed.user_id = ?" +

                " WHERE " +

                // It's assigned to us or open to call
                " ((assign.uuid IS NOT NULL) OR (popup.open_campaign = 1)) AND " +

                // And currently active
                " popup.start_time <= ? AND " +
                " ((popup.end_time = 0) OR (popup.end_time > ?)) AND " +

                // And either hasn't been dismissed yet
                " (dismissed.state is NULL OR" +

                // Or was dismissed temporarily, but some time has passed
                "  (dismissed.state = 'temporary' AND" +
                "   (? - dismissed.dismiss_time) >= ?))");

        try {
            long now = System.currentTimeMillis();

            return DB.transaction
                    ("Find a popup for the current user",
                            new DBAction<Popup>() {
                                @Override
                                public Popup call(DBConnection db) throws SQLException {
                                    try (DBResults results = db.run(sql)
                                            .param(userId).param(userId)
                                            .param(now).param(now).param(now)
                                            .param(getTemporaryTimeoutMilliseconds())
                                            .executeQuery()) {
                                        for (ResultSet result : results) {
                                            Clob contentClob = result.getClob("template_content");
                                            String templateContent = contentClob.getSubString(1, (int) contentClob.length());

                                            // Got one!
                                            return Popup.create(result.getString("uuid"),
                                                    result.getString("descriptor"),
                                                    result.getLong("start_time"),
                                                    result.getLong("end_time"),
                                                    result.getInt("open_campaign") == 1,
                                                    templateContent);
                                        }

                                        // Otherwise, no suitable popup was found
                                        return Popup.createNullPopup();
                                    }
                                }
                            }
                    );
        } catch (Exception e) {
            log.error("Error determining active popup", e);
            return Popup.createNullPopup();
        }
    }

    private int getTemporaryTimeoutMilliseconds() {
        return ServerConfigurationService.getInt("pasystem.popup.temporary-timeout-ms", (24 * 60 * 60 * 1000));
    }
}
