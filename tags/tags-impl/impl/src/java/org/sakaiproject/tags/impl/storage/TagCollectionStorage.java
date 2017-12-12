/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
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

package org.sakaiproject.tags.impl.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagCollections;
import org.sakaiproject.tags.impl.common.DB;
import org.sakaiproject.tags.impl.common.DBAction;
import org.sakaiproject.tags.impl.common.DBConnection;
import org.sakaiproject.tags.impl.common.DBPreparedStatement;
import org.sakaiproject.tags.impl.common.DBResults;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Query and store Tag Collection objects in the database.
 */
@Slf4j
public class TagCollectionStorage implements TagCollections {

    private static final String QUERY_GET_ALL ="SELECT * from tagservice_collection ORDER BY name";
    private static final String QUERY_COLLECTIONS_PAGINATED_MYSQL = "SELECT * from tagservice_collection ORDER by name LIMIT ?,?";
    private static final String QUERY_COLLECTIONS_PAGINATED_ORACLE_PART1 = "SELECT * FROM (SELECT /*+ FIRST_ROWS(";
    private static final String QUERY_COLLECTIONS_PAGINATED_ORACLE_PART2 = ") */ tc.*, rownum rn FROM (SELECT *" +
            "                  FROM tagservice_collection" +
            "                 ORDER BY name) tc" +
            "         WHERE rownum <= ?)" +
            " WHERE rn >= ?";
    private static final String QUERY_TOTAL_TAG_COLLECTIONS = "SELECT COUNT(*) AS total from tagservice_collection";
    private static final String QUERY_GET_FOR_ID_WITH_CONNECTION = "SELECT * from tagservice_collection WHERE tagcollectionid = ?";
    private static final String QUERY_GET_FOR_EXTERNAL_SOURCENAME = "SELECT * from tagservice_collection WHERE externalsourcename = ?";
    private static final String QUERY_GET_FOR_NAME = "SELECT * from tagservice_collection WHERE name = ?";
    private static final String QUERY_CREATE_TAG_COLLECTION = "INSERT INTO tagservice_collection (tagcollectionid, name, description, externalsourcename, " +
            "externalsourcedescription, createdby, creationdate, lastmodifiedby, lastmodificationdate," +
            "externalupdate, externalcreation, lastsynchronizationdate, lastupdatedateinexternalsystem) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String QUERY_UPDATE_TAG_COLLECTION = "UPDATE tagservice_collection SET name = ?, description = ?, externalsourcename = ?, " +
            "externalsourcedescription = ?, lastmodifiedby = ?, lastmodificationdate = ?," +
            "externalupdate = ?,externalcreation = ?, lastsynchronizationdate = ? ,lastupdatedateinexternalsystem = ? WHERE tagcollectionid = ?";
    private static final String QUERY_DELETE_TAG_FROM_TAG_COLLECTION = "DELETE FROM tagservice_tag WHERE tagcollectionid = ?";
    private static final String QUERY_DELETE_TAG_COLLECTION = "DELETE FROM tagservice_collection WHERE tagcollectionid = ?";

    private SessionManager sessionManager;
    private EventTrackingService eventTrackingService;
    private DB db;
    private Supplier<Long> timestampProvider = () -> new Date().getTime();
    private Supplier<String> idProvider = () -> UUID.randomUUID().toString();

    @Override
    public List<TagCollection> getAll() {
        return db.transaction
                ("Find all tag collections",
                        new DBAction<List<TagCollection>>() {
                            @Override
                            public List<TagCollection> call(DBConnection db) throws SQLException {
                                List<TagCollection> tagCollections = new ArrayList<>();
                                try (DBResults results = db.run(QUERY_GET_ALL)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        tagCollections.add(new TagCollection(result.getString("tagcollectionid"),
                                                result.getString("name"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("externalsourcename"),
                                                result.getString("externalsourcedescription"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("lastsynchronizationdate"),
                                                result.getLong("lastupdatedateinexternalsystem")));
                                    }


                                    return tagCollections;
                                }
                            }
                        }
                );
    }




    @Override
    public List<TagCollection> getTagCollectionsPaginated(int pageNum, int pageSize) {
        int limit = (pageNum-1) * pageSize;
        return db.transaction
                ("Retrieve Collections paginated",
                        new DBAction<List<TagCollection>>() {
                            @Override
                            public List<TagCollection> call(DBConnection conn) throws SQLException {
                                List<TagCollection> tagCollections = new ArrayList<>();
                                final String query;
                                final boolean isOracle = db.getVendor().equals("oracle");
                                if (isOracle){
                                    // http://stackoverflow.com/a/13740166 with additional syntax help from
                                    // http://www.oracle.com/technetwork/issue-archive/2006/06-sep/o56asktom-086197.html
                                    query=QUERY_COLLECTIONS_PAGINATED_ORACLE_PART1 + pageSize + QUERY_COLLECTIONS_PAGINATED_ORACLE_PART2;
                                } else {
                                    query=QUERY_COLLECTIONS_PAGINATED_MYSQL;
                                }
                                try (
                                    DBPreparedStatement ps = conn.run(query);
                                    DBResults results = (isOracle
                                            ? (ps.param(limit + pageSize).param(limit))
                                            : (ps.param(limit).param(pageSize))).executeQuery();
                                ) {
                                    for (ResultSet result : results) {
                                        tagCollections.add(new TagCollection(result.getString("tagcollectionid"),
                                                result.getString("name"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("externalsourcename"),
                                                result.getString("externalsourcedescription"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("lastsynchronizationdate"),
                                                result.getLong("lastupdatedateinexternalsystem")));
                                    }


                                    return tagCollections;
                                }
                            }
                        }
                );
    }


    @Override
    public int getTotalTagCollections() {
        return db.transaction
                ("Retrieve the number of Tag Collections",
                        new DBAction<Integer>() {
                            @Override
                            public Integer call(DBConnection db) throws SQLException {
                                String query=QUERY_TOTAL_TAG_COLLECTIONS;
                                try (DBResults results = db.run(query)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        return result.getInt("total");
                                    }
                                }
                                return null;
                            }
                        }
                );
    }

    @Override
    public Optional<TagCollection> getForId(final String tagcollectionid) {
        return db.transaction
                ("Find a tag Collection by id",
                        new DBAction<Optional<TagCollection>>() {
                            @Override
                            public Optional<TagCollection> call(DBConnection db) throws SQLException {
                                return getForIdWithConnection(tagcollectionid, db);
                            }
                        }
                );
    }

    private Optional<TagCollection> getForIdWithConnection(String tagcollectionid, DBConnection db) throws SQLException {
        try (
                DBPreparedStatement ps = db.run(QUERY_GET_FOR_ID_WITH_CONNECTION);
                DBResults results = ps.param(tagcollectionid).executeQuery()
        ) {
            for (ResultSet result : results) {
                return Optional.of(new TagCollection(result.getString("tagcollectionid"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("createdby"),
                        result.getLong("creationdate"),
                        result.getString("externalsourcename"),
                        result.getString("externalsourcedescription"),
                        result.getString("lastmodifiedby"),
                        result.getLong("lastmodificationdate"),
                        result.getBoolean("externalupdate"),
                        result.getBoolean("externalcreation"),
                        result.getLong("lastsynchronizationdate"),
                        result.getLong("lastupdatedateinexternalsystem")));
            }

            return Optional.empty();
        }
    }

    @Override
    public Optional<TagCollection> getForExternalSourceName(final String externalsourcename) {
        return db.transaction
                ("Find a tag Collection by external source name",
                        new DBAction<Optional<TagCollection>>() {
                            @Override
                            public Optional<TagCollection> call(DBConnection db) throws SQLException {
                                try (DBResults results = db.run(QUERY_GET_FOR_EXTERNAL_SOURCENAME)
                                        .param(externalsourcename)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        return Optional.of(new TagCollection(result.getString("tagcollectionid"),
                                                result.getString("name"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("externalsourcename"),
                                                result.getString("externalsourcedescription"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("lastsynchronizationdate"),
                                                result.getLong("lastupdatedateinexternalsystem")));
                                    }

                                    return Optional.empty();
                                }
                            }
                        }
                );
    }

    @Override
    public Optional<TagCollection> getForName(final String name) {
        return db.transaction
                ("Find a tag Collection by name",
                        new DBAction<Optional<TagCollection>>() {
                            @Override
                            public Optional<TagCollection> call(DBConnection db) throws SQLException {
                                try (DBResults results = db.run(QUERY_GET_FOR_NAME)
                                        .param(name)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        return Optional.of(new TagCollection(result.getString("tagcollectionid"),
                                                result.getString("name"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("externalsourcename"),
                                                result.getString("externalsourcedescription"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("lastsynchronizationdate"),
                                                result.getLong("lastupdatedateinexternalsystem")));
                                    }

                                    return Optional.empty();
                                }
                            }
                        }
                );
    }
    
    @Override
    public String createTagCollection(TagCollection tagCollection) {
        final String currentUserId = sessionManager.getCurrentSessionUserId();
        final Long currentTime = timestampProvider.get();
        final String proposedTagCollectionId = idProvider.get();
        final String createdId =  db.transaction("Create a tag Collection",
                new DBAction<String>() {
                    @Override
                    public String call(DBConnection db) throws SQLException {

                        try (DBPreparedStatement ps = db.run(QUERY_CREATE_TAG_COLLECTION)) {

                            ps.param(proposedTagCollectionId)
                                    .param(tagCollection.getName())
                                    .param(tagCollection.getDescription())
                                    .param(tagCollection.getExternalSourceName())
                                    .param(tagCollection.getExternalSourceDescription())
                                    .param(currentUserId)
                                    .param(currentTime)
                                    .param(currentUserId)
                                    .param(currentTime)
                                    .param(tagCollection.getExternalUpdate() ? 1 : 0)
                                    .param(tagCollection.getExternalCreation() ? 1 : 0)
                                    .param(tagCollection.getLastSynchronizationDate())
                                    .param(tagCollection.getLastUpdateDateInExternalSystem())
                                    .executeUpdate();

                            db.commit();
                            return proposedTagCollectionId;
                        }
                    }
                });
        if ( createdId != null ) {// sanity
            eventTrackingService.post(eventTrackingService.newEvent("tags.new.collection", "/tagcollections/" + createdId, true));
        }
        return createdId;
    }

    @Override
    public void updateTagCollection(TagCollection tagCollection) {
        final String tagcollectionid = tagCollection.getTagCollectionId();
        if ( tagcollectionid == null ) {
            throw new RuntimeException("Can't update a tag collection with no tagCollectionId specified");
        }
        final AtomicBoolean generateEvent = new AtomicBoolean(false);
        final String currentUserId = sessionManager.getCurrentSessionUserId();
        final Long currentTime = timestampProvider.get();
        db.transaction("Update tag Collection with collectionid " + tagcollectionid,
                new DBAction<Void>() {
                    @Override
                    public Void call(DBConnection db) throws SQLException {

                        final Optional<TagCollection> previousState = getForIdWithConnection(tagcollectionid, db);
                        if ( !(previousState.isPresent()) ) {
                            throw new RuntimeException("Can't update a tag collection that doesn't exist. ID: " + tagcollectionid);
                        }

                        if ( !(isDirtyingUpdate(tagCollection, previousState.get())) ) {
                            log.debug("No changes to non-calculated fields. Skipping update to tag collection wih ID: " + tagcollectionid);
                            return null;
                        }

                        generateEvent.set(isEventGeneratingUpdate(tagCollection, previousState.get()));

                        try (DBPreparedStatement ps = db.run(QUERY_UPDATE_TAG_COLLECTION)) {
                            ps.param(tagCollection.getName())
                                    .param(tagCollection.getDescription())
                                    .param(tagCollection.getExternalSourceName())
                                    .param(tagCollection.getExternalSourceDescription())
                                    .param(currentUserId)
                                    .param(currentTime)
                                    .param(tagCollection.getExternalUpdate() ? 1 : 0)
                                    .param(tagCollection.getExternalCreation() ? 1 : 0)
                                    .param(tagCollection.getLastSynchronizationDate())
                                    .param(tagCollection.getLastUpdateDateInExternalSystem())
                                    .param(tagcollectionid)
                                    .executeUpdate();
                            db.commit();
                        }
                        return null;
                    }
                }
        );
        if ( generateEvent.get() ) {
            eventTrackingService.post(eventTrackingService.newEvent("tags.update.collection",
                    "/tagcollections/" + tagcollectionid, true));
        }
    }

    /**
     * Have any persistent, non-generated fields changed?
     *
     * @param proposed
     * @param original
     * @return
     */
    private boolean isDirtyingUpdate(TagCollection proposed, TagCollection original) {
        return isEventGeneratingUpdate(proposed, original)
                || !(Objects.equals(proposed.getExternalUpdate(), original.getExternalUpdate()))
                || !(Objects.equals(proposed.getLastSynchronizationDate(), original.getLastSynchronizationDate()))
                || !(Objects.equals(proposed.getLastUpdateDateInExternalSystem(), original.getLastUpdateDateInExternalSystem()));
    }

    /**
     * * Have any perisistent, non-generated fields of potential interest to update event listeners changed?
     * (Making some heuristic judgments here in an attempt to reduce the likelihood of event storms resulting
     * from repeatedly scheduled imports of large tag collections.)
     *
     * @param proposed
     * @param original
     * @return
     */
    private boolean isEventGeneratingUpdate(TagCollection proposed, TagCollection original) {
        return !(Objects.equals(proposed.getName(), original.getName()))
                || !(Objects.equals(proposed.getDescription(), original.getDescription()))
                || !(Objects.equals(proposed.getExternalSourceName(), original.getExternalSourceName()))
                || !(Objects.equals(proposed.getExternalSourceDescription(), original.getExternalSourceDescription()));
    }

    @Override
    public void deleteTagCollection(String tagcollectionid) {
        int deletedRows = db.transaction("Delete tag Collection with tagcollectionid " + tagcollectionid,
                new DBAction<Integer>() {
                    @Override
                    public Integer call(DBConnection db) throws SQLException {

                        try (DBPreparedStatement ps = db.run(QUERY_DELETE_TAG_FROM_TAG_COLLECTION)) {
                            ps.param(tagcollectionid).executeUpdate();
                        }

                        int deletedCollectionCount = 0;
                        try (DBPreparedStatement ps = db.run(QUERY_DELETE_TAG_COLLECTION)) {
                            deletedCollectionCount = ps.param(tagcollectionid).executeUpdate();
                        }

                        db.commit();
                        return deletedCollectionCount;
                    }
                }
        );
        if ( deletedRows > 0 ) {
            postTagCollectionDeleteEvent(tagcollectionid);
        }
    }

    private void postTagCollectionDeleteEvent(String tagCollectionId) {
        eventTrackingService.post(eventTrackingService.newEvent("tags.delete.collection", "/tagcollections/" + tagCollectionId, true));
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public void setTimestampProvider(Supplier<Long> provider) {
        this.timestampProvider = provider;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setIdProvider(Supplier<String> idProvider) {
        this.idProvider = idProvider;
    }
}
