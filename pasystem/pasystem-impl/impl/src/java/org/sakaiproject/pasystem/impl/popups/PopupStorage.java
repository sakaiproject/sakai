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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.pasystem.api.AcknowledgementType;
import org.sakaiproject.pasystem.api.Acknowledger;
import org.sakaiproject.pasystem.api.MissingUuidException;
import org.sakaiproject.pasystem.api.Popup;
import org.sakaiproject.pasystem.api.Popups;
import org.sakaiproject.pasystem.api.TemplateStream;
import org.sakaiproject.pasystem.impl.acknowledgements.AcknowledgementStorage;
import org.sakaiproject.pasystem.impl.common.DB;
import org.sakaiproject.pasystem.impl.common.DBAction;
import org.sakaiproject.pasystem.impl.common.DBConnection;
import org.sakaiproject.pasystem.impl.common.DBResults;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * Query and store Popup objects in the database.
 */
@Slf4j
public class PopupStorage implements Popups, Acknowledger {

    @Override
    public String createCampaign(Popup popup,
                                 TemplateStream templateInput,
                                 Optional<List<String>> assignToEids) {
        return DB.transaction
                ("Popup creation",
                        new DBAction<String>() {
                            @Override
                            public String call(DBConnection db) throws SQLException {
                                String uuid = UUID.randomUUID().toString();

                                db.run("INSERT INTO pasystem_popup_screens (uuid, descriptor, start_time, end_time, open_campaign) VALUES (?, ?, ?, ?, ?)")
                                        .param(uuid)
                                        .param(popup.getDescriptor())
                                        .param(popup.getStartTime())
                                        .param(popup.getEndTime())
                                        .param(popup.isOpenCampaign() ? 1 : 0)
                                        .executeUpdate();

                                setPopupContent(db, uuid, templateInput);
                                setPopupAssignees(db, uuid, assignToEids);

                                db.commit();

                                return uuid;
                            }
                        }
                );
    }

    @Override
    public void updateCampaign(Popup popup,
        Optional<TemplateStream> templateInput,
        Optional<List<String>> assignToEids) {
        try {
            final String uuid = popup.getUuid();

            DB.transaction
                    ("Update an existing popup campaign",
                            new DBAction<Void>() {
                                @Override
                                public Void call(DBConnection db) throws SQLException {

                                    db.run("UPDATE pasystem_popup_screens SET descriptor = ?, start_time = ?, end_time = ?, open_campaign = ? WHERE uuid = ?")
                                            .param(popup.getDescriptor())
                                            .param(popup.getStartTime())
                                            .param(popup.getEndTime())
                                            .param(popup.isOpenCampaign() ? 1 : 0)
                                            .param(uuid)
                                            .executeUpdate();

                                    setPopupAssignees(db, uuid, assignToEids);

                                    if (templateInput.isPresent()) {
                                        setPopupContent(db, uuid, templateInput.get());
                                    }

                                    db.commit();

                                    log.info("Update of popup {} completed", uuid);

                                    return null;
                                }
                            }
                    );
        } catch (MissingUuidException e) {
            throw new RuntimeException("Can't update a popup with no UUID specified", e);
        }
    }

    @Override
    public List<Popup> getAll() {
        return DB.transaction
                ("Find all popups",
                        new DBAction<List<Popup>>() {
                            @Override
                            public List<Popup> call(DBConnection db) throws SQLException {
                                List<Popup> popups = new ArrayList<Popup>();
                                try (DBResults results = db.run("SELECT * from pasystem_popup_screens")
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        popups.add(Popup.create(result.getString("uuid"),
                                                result.getString("descriptor"),
                                                result.getLong("start_time"),
                                                result.getLong("end_time"),
                                                result.getInt("open_campaign") == 1));
                                    }

                                    return popups;
                                }
                            }
                        }
                );
    }

    @Override
    public String getPopupContent(final String uuid) {
        return DB.transaction
                ("Get the content for a popup",
                        new DBAction<String>() {
                            @Override
                            public String call(DBConnection db) throws SQLException {
                                try (DBResults results = db.run("SELECT template_content from pasystem_popup_content where uuid = ?")
                                        .param(uuid)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        Clob contentClob = result.getClob("template_content");
                                        return contentClob.getSubString(1, (int) contentClob.length());
                                    }

                                    return "";
                                }
                            }
                        }
                );
    }

    @Override
    public Optional<Popup> getForId(final String uuid) {
        return DB.transaction
                ("Find a popup by uuid",
                        new DBAction<Optional<Popup>>() {
                            @Override
                            public Optional<Popup> call(DBConnection db) throws SQLException {
                                try (DBResults results = db.run("SELECT * from pasystem_popup_screens WHERE UUID = ?")
                                        .param(uuid)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        return Optional.of(Popup.create(result.getString("uuid"),
                                                result.getString("descriptor"),
                                                result.getLong("start_time"),
                                                result.getLong("end_time"),
                                                result.getInt("open_campaign") == 1));
                                    }

                                    return Optional.empty();
                                }
                            }
                        }
                );
    }

    @Override
    public List<String> getAssigneeEids(final String uuid) {
        List<String> userIds = DB.transaction
                ("Find a list of assignees by popup uuid",
                        new DBAction<List<String>>() {
                            @Override
                            public List<String> call(DBConnection db) throws SQLException {
                                final List<String> userIds = new ArrayList<String>();

                                try (DBResults results = db.run("SELECT user_id from pasystem_popup_assign WHERE UUID = ? AND user_id is not NULL")
                                    .param(uuid)
                                    .executeQuery()) {
                                    for (ResultSet result : results) {
                                        userIds.add(result.getString("user_id"));
                                    }
                                }

                                return userIds;
                            }
                        }
                );

        return userIdsToEids(userIds);
    }

    private void setPopupContent(DBConnection db, String uuid, TemplateStream templateContent) throws SQLException {
        // A little hoop jumping here to avoid having to rewind the InputStream
        //
        // Add an empty record if one is missing
        try {
            db.run("INSERT INTO pasystem_popup_content (uuid) VALUES (?)")
                    .param(uuid)
                    .executeUpdate();
        } catch (SQLException e) {
            // Expected for updates
        }

        // Set the content CLOB
        db.run("UPDATE pasystem_popup_content set template_content = ? WHERE uuid = ?")
                .param(new InputStreamReader(templateContent.getInputStream()),
                        templateContent.getLength())
                .param(uuid)
                .executeUpdate();
    }

    private void setPopupAssignees(DBConnection db, String uuid, Optional<List<String>> assignToEids) throws SQLException {
        if (assignToEids.isPresent()) {
            db.run("DELETE FROM pasystem_popup_assign where uuid = ? AND user_id is not NULL")
                    .param(uuid)
                    .executeUpdate();

            for (String userId : eidsToUserIds(assignToEids.get())) {
                db.run("INSERT INTO pasystem_popup_assign (uuid, user_id) VALUES (?, ?)")
                        .param(uuid)
                        .param(userId)
                        .executeUpdate();
            }
        }
    }

    @Override
    public boolean deleteCampaign(final String uuid) {
        return DB.transaction
                ("Delete an existing popup campaign",
                        new DBAction<Boolean>() {
                            @Override
                            public Boolean call(DBConnection db) throws SQLException {
                                db.run("DELETE FROM pasystem_popup_assign where uuid = ?")
                                        .param(uuid)
                                        .executeUpdate();

                                db.run("DELETE FROM pasystem_popup_dismissed where uuid = ?")
                                        .param(uuid)
                                        .executeUpdate();

                                db.run("DELETE FROM pasystem_popup_content where uuid = ?")
                                        .param(uuid)
                                        .executeUpdate();

                                db.run("DELETE FROM pasystem_popup_screens WHERE uuid = ?")
                                        .param(uuid)
                                        .executeUpdate();

                                db.commit();

                                return true;
                            }
                        }
                );
    }

    @Override
    public void acknowledge(final String uuid, final String userId, final AcknowledgementType acknowledgementType) {
        new AcknowledgementStorage(AcknowledgementStorage.NotificationType.POPUP).acknowledge(uuid, userId, acknowledgementType);
    }

    @Override
    public void acknowledge(final String uuid, final String userId) {
        acknowledge(uuid, userId, AcknowledgementType.TEMPORARY);
    }

    private static List<String> eidsToUserIds(List<String> eids) {
        List<String> userIds = new ArrayList<String>();

        for (User user : UserDirectoryService.getUsersByEids(eids)) {
            userIds.add(user.getId());
        }

        return userIds;
    }

    private static List<String> userIdsToEids(List<String> userIds) {
        List<String> eids = new ArrayList<String>();

        for (User user : (List<User>)UserDirectoryService.getUsers(userIds)) {
            eids.add(user.getEid());
        }

        return eids;
    }


}
