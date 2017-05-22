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

package org.sakaiproject.pasystem.impl.acknowledgements;

import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;
import org.sakaiproject.pasystem.api.AcknowledgementType;
import org.sakaiproject.pasystem.api.Acknowledger;
import org.sakaiproject.pasystem.impl.common.DB;
import org.sakaiproject.pasystem.impl.common.DBAction;
import org.sakaiproject.pasystem.impl.common.DBConnection;

/**
 * Mark a popup or banner as acknowledged by a user.
 */
public class AcknowledgementStorage {

    public enum NotificationType {
        BANNER,
        POPUP
    }

    private final String tableName;

    public AcknowledgementStorage(NotificationType type) {
        tableName = ("PASYSTEM_" + type + "_dismissed").toLowerCase(Locale.ROOT);
    }

    /**
     * Record the fact that a user has acknowledged a particular popup/banner.
     */
    public void acknowledge(final String uuid, final String userId, final AcknowledgementType acknowledgementType) {
        DB.transaction
                ("Acknowledge a notification on behalf of a user",
                        new DBAction<Void>() {
                            @Override
                            public Void call(DBConnection db) throws SQLException {
                                db.run("DELETE FROM " + tableName + " where uuid = ? AND user_id = ?")
                                        .param(uuid)
                                        .param(userId)
                                        .executeUpdate();

                                db.run("INSERT INTO " + tableName + " (uuid, user_id, state, dismiss_time) values (?, ?, ?, ?)")
                                        .param(uuid)
                                        .param(userId)
                                        .param(acknowledgementType.dbValue())
                                        .param(System.currentTimeMillis())
                                        .executeUpdate();

                                db.commit();
                                return null;
                            }
                        }
                );
    }

    /**
     * Forget all temporary acknowledgements created by a user.
     */
    public void clearTemporaryDismissedForUser(String userId) {
        DB.transaction
                ("Delete all temporarily dismissed banners for a user",
                        new DBAction<Void>() {
                            @Override
                            public Void call(DBConnection db) throws SQLException {
                                db.run("DELETE FROM " + tableName + " WHERE state = ? AND user_id = ?")
                                        .param(AcknowledgementType.TEMPORARY.dbValue())
                                        .param(userId)
                                        .executeUpdate();

                                db.commit();
                                return null;
                            }
                        }
                );
    }

}
