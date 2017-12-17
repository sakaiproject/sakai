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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tags.api.MissingUuidException;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.Tags;
import org.sakaiproject.tags.impl.common.DB;
import org.sakaiproject.tags.impl.common.DBAction;
import org.sakaiproject.tags.impl.common.DBConnection;
import org.sakaiproject.tags.impl.common.DBPreparedStatement;
import org.sakaiproject.tags.impl.common.DBResults;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Query and store Tag  objects in the database.
 */
@Slf4j
public class TagStorage implements Tags {

    private static final String QUERY_GET_ALL = "SELECT * from tagservice_tag ORDER by tagLabel,tagcollectionid";
    private static final String QUERY_GET_ALL_IN_COLLECTION = "SELECT * from tagservice_tag WHERE tagcollectionid = ? ORDER by tagLabel";
    private static final String QUERY_GET_TAGS_BY_EXACT_LABEL = "SELECT * from tagservice_tag WHERE taglabel = ? ORDER by tagcollectionid";
    private static final String QUERY_GET_TAGS_BY_PARTIAL_LABEL = "SELECT * from tagservice_tag WHERE taglabel LIKE ? ORDER by tagcollectionid";
    private static final String QUERY_GET_TAGS_BY_PREFIX_IN_LABEL = "SELECT * from tagservice_tag WHERE LOWER(taglabel) LIKE LOWER(?) ORDER by taglabel,tagcollectionid";
    private static final String QUERY_GET_TOTAL_TAGS_BY_PREFIX_IN_LABEL = "SELECT COUNT(*) as total_tags from tagservice_tag WHERE LOWER(taglabel) LIKE LOWER(?) ORDER by taglabel,tagcollectionid";
    private static final String QUERY_GET_TAGS_PAGINATED_BY_PREFIX_IN_LABEL_1_ORACLE = "SELECT * " +
            "  FROM (SELECT /*+ FIRST_ROWS(";
    private static final String QUERY_GET_TAGS_PAGINATED_BY_PREFIX_IN_LABEL_2_ORACLE = ") */ tc.*, rownum rn" +
            "          FROM (SELECT *" +
            "                  FROM tagservice_tag" +
            "                  WHERE LOWER(taglabel) LIKE LOWER(?) ORDER by taglabel,tagcollectionid) tc" +
            "         WHERE rownum <= ?)" +
            " WHERE rn >= ?";
    private static final String QUERY_GET_TAGS_PAGINATED_BY_PREFIX_IN_LABEL_MYSQL = "SELECT * from tagservice_tag WHERE LOWER(taglabel) LIKE LOWER(?) ORDER by taglabel,tagcollectionid LIMIT ?,?";
    private static final String QUERY_GET_FOR_EXTERNALID_IN_COLLECTION = "SELECT * from tagservice_tag WHERE externalid = ? AND tagcollectionid = ?";
    private static final String QUERY_GET_TAG_BY_ID = "SELECT * from tagservice_tag WHERE tagid = ?";
    private static final String QUERY_GET_TAGS_PAGINATED_IN_COLLECTION_1_ORACLE = "SELECT * " +
            "  FROM (SELECT /*+ FIRST_ROWS(" ;
    private static final String QUERY_GET_TAGS_PAGINATED_IN_COLLECTION_2_ORACLE = ") */ tc.*, rownum rn" +
            "          FROM (SELECT *" +
            "                  FROM tagservice_tag" +
            "                  WHERE tagcollectionid = ?" +
            "                 ORDER BY tagLabel) tc" +
            "         WHERE rownum <= ?)" +
            " WHERE rn >= ?";
    private static final String QUERY_GET_TAGS_PAGINATED_IN_COLLECTION_MYSQL = "SELECT * from tagservice_tag WHERE tagcollectionid = ? ORDER by tagLabel LIMIT ?,?";
    private static final String QUERY_GET_TOTAL_TAGS_IN_COLLECTION = "SELECT COUNT(*) AS total from tagservice_tag WHERE tagcollectionid = ?";
    private static final String QUERY_CREATE_TAG = "INSERT INTO tagservice_tag (tagid, tagcollectionid, taglabel, description, createdby, creationdate, lastmodifiedby, " +
            "lastmodificationdate, externalid, alternativelabels, externalcreation, externalcreationdate, externalupdate, " +
            "lastupdatedateinexternalsystem, parentid, externalhierarchycode, externaltype, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String QUERY_UPDATE_TAG_SIMPLE = "UPDATE tagservice_tag SET lastmodifiedby = ?, " +
            "lastmodificationdate = ? WHERE tagid = ?";
    private static final String QUERY_UPDATE_TAG_FULL = "UPDATE tagservice_tag SET tagcollectionid = ?, taglabel = ?, description = ?, lastmodifiedby = ?, " +
            "lastmodificationdate = ?, externalid = ?, alternativelabels = ?, externalcreation = ?, externalcreationdate = ?, externalupdate = ?, " +
            "lastupdatedateinexternalsystem = ?, parentid = ?, externalhierarchycode = ?, externaltype = ?, data = ?  WHERE tagid = ?";
    private static final String QUERY_DELETE_TAG = "DELETE FROM tagservice_tag WHERE tagid = ?";
    private static final String QUERY_SELECT_TAG_OLDER_THEN_DATE_FROM_EXTERNAL_COLLECTION = "SELECT tagid FROM tagservice_tag WHERE tagCollectionid = ? AND lastmodificationdate < ?";
    private static final String QUERY_DELETE_TAG_OLDER_THEN_DATE_FROM_EXTERNAL_COLLECTION = "DELETE FROM tagservice_tag WHERE tagCollectionid = ? AND lastmodificationdate < ?";
    private static final String QUERY_SELECT_TAG_FROM_EXTERNAL_COLLECTION = "SELECT tagid from tagservice_tag WHERE tagCollectionid = ? AND externalid = ?";
    private static final String QUERY_DELETE_TAG_FROM_EXTERNAL_COLLECTION = "DELETE FROM tagservice_tag WHERE tagCollectionid = ? AND externalid = ?";
    private static final String QUERY_GET_COLLECTION_NAME = "SELECT name from tagservice_collection WHERE tagcollectionid = ?";

    private SessionManager sessionManager;
    private EventTrackingService eventTrackingService;
    private DB db;
    private Supplier<Long> timestampProvider = () -> new Date().getTime();
    private Supplier<String> idProvider = () -> UUID.randomUUID().toString();

    @Override
    public List<Tag> getAll() {
        return db.transaction
                ("Find all tags",
                        new DBAction<List<Tag>>() {
                            @Override
                            public List<Tag> call(DBConnection db) throws SQLException {
                                List<Tag> tags = new ArrayList<>();
                                try (DBResults results = db.run(QUERY_GET_ALL)
                                        .executeQuery()) {
                                    HashMap<String,String> collectionNames = new HashMap<>();
                                    for (ResultSet result : results) {
                                        String collectionName = collectionNames.get(result.getString("tagcollectionid"));
                                        if (collectionName==null){
                                            collectionName = getCollectionName(result.getString("tagcollectionid"));
                                            collectionNames.put(result.getString("tagcollectionid"),collectionName);
                                        }
                                        tags.add(new Tag(result.getString("tagid"),
                                                result.getString("tagcollectionid"),
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                collectionName));

                                    }

                                    return tags;
                                }
                            }
                        }
                );
    }

    @Override
    public List<Tag> getAllInCollection(final String tagcollectionid) {
        return db.transaction
                ("Find all tags in a collection",
                        new DBAction<List<Tag>>() {
                            @Override
                            public List<Tag> call(DBConnection db) throws SQLException {
                                List<Tag> tags = new ArrayList<>();
                                try (DBResults results = db.run(QUERY_GET_ALL_IN_COLLECTION)
                                        .param(tagcollectionid)
                                        .executeQuery()) {
                                    HashMap<String,String> collectionNames = new HashMap<>();
                                    for (ResultSet result : results) {
                                        String collectionName = collectionNames.get(result.getString("tagcollectionid"));
                                        if (collectionName==null){
                                            collectionName = getCollectionName(result.getString("tagcollectionid"));
                                            collectionNames.put(result.getString("tagcollectionid"),collectionName);
                                        }
                                        tags.add(new Tag(result.getString("tagid"),
                                                result.getString("tagcollectionid"),
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                collectionName));

                                    }

                                    return tags;
                                }
                            }
                        }
                );
    }


    @Override
    public List<Tag> getTagsByExactLabel(final String label) {
        return db.transaction
                ("Find all tags in a collection",
                        new DBAction<List<Tag>>() {
                            @Override
                            public List<Tag> call(DBConnection db) throws SQLException {
                                List<Tag> tags = new ArrayList<>();
                                try (DBResults results = db.run(QUERY_GET_TAGS_BY_EXACT_LABEL)
                                        .param(label)
                                        .executeQuery()) {
                                    HashMap<String,String> collectionNames = new HashMap<>();
                                    for (ResultSet result : results) {
                                        String collectionName = collectionNames.get(result.getString("tagcollectionid"));
                                        if (collectionName==null){
                                            collectionName = getCollectionName(result.getString("tagcollectionid"));
                                            collectionNames.put(result.getString("tagcollectionid"),collectionName);
                                        }
                                        tags.add(new Tag(result.getString("tagid"),
                                                result.getString("tagcollectionid"),
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                collectionName));

                                    }

                                    return tags;
                                }
                            }
                        }
                );
    }



    @Override
    public List<Tag> getTagsByPartialLabel(final String label) {
        return db.transaction
                ("Find tags that mathc a pattern",
                        new DBAction<List<Tag>>() {
                            @Override
                            public List<Tag> call(DBConnection db) throws SQLException {
                                List<Tag> tags = new ArrayList<>();
                                try (DBResults results = db.run(QUERY_GET_TAGS_BY_PARTIAL_LABEL)
                                        .param("%"+ label + "%")
                                        .executeQuery()) {
                                    HashMap<String,String> collectionNames = new HashMap<>();
                                    for (ResultSet result : results) {
                                        String collectionName = collectionNames.get(result.getString("tagcollectionid"));
                                        if (collectionName==null){
                                            collectionName = getCollectionName(result.getString("tagcollectionid"));
                                            collectionNames.put(result.getString("tagcollectionid"),collectionName);
                                        }
                                        tags.add(new Tag(result.getString("tagid"),
                                                result.getString("tagcollectionid"),
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                collectionName));

                                    }

                                    return tags;
                                }
                            }
                        }
                );
    }

    @Override
    public List<Tag> getTagsByPrefixInLabel(final String label) {
        return db.transaction
                ("Find tags that match a pattern",
                        new DBAction<List<Tag>>() {
                            @Override
                            public List<Tag> call(DBConnection db) throws SQLException {
                                List<Tag> tags = new ArrayList<>();
                                try (DBResults results = db.run(QUERY_GET_TAGS_BY_PREFIX_IN_LABEL)
                                        .param(label + "%")
                                        .executeQuery()) {
                                    HashMap<String,String> collectionNames = new HashMap<>();
                                    for (ResultSet result : results) {
                                        String collectionName = collectionNames.get(result.getString("tagcollectionid"));
                                        if (collectionName==null){
                                            collectionName = getCollectionName(result.getString("tagcollectionid"));
                                            collectionNames.put(result.getString("tagcollectionid"),collectionName);
                                        }
                                        tags.add(new Tag(result.getString("tagid"),
                                                result.getString("tagcollectionid"),
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                collectionName));

                                    }

                                    return tags;
                                }
                            }
                        }
                );
    }


    @Override
    public int getTotalTagsByPrefixInLabel(final String label) {
        return db.transaction
                ("Find total number of tags that match a pattern",
                        new DBAction<Integer>() {
                            @Override
                            public Integer call(DBConnection db) throws SQLException {
                                String query=QUERY_GET_TOTAL_TAGS_BY_PREFIX_IN_LABEL;
                                try (DBResults results = db.run(query)
                                        .param(label + "%")
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        return result.getInt("total_tags");
                                    }
                                }
                                return null;
                            }
                        }
                );
    }


    @Override
    public List<Tag> getTagsPaginatedByPrefixInLabel(final int pageNum,final int pageSize, final String label) {
        int limit = (pageNum-1) * pageSize;
        return db.transaction
                ("Find tags that match a pattern",
                        new DBAction<List<Tag>>() {
                            @Override
                            public List<Tag> call(DBConnection conn) throws SQLException {
                                List<Tag> tags = new ArrayList<>();
                                final String query;
                                final boolean isOracle = db.getVendor().equals("oracle");
                                if (isOracle){
                                    // http://stackoverflow.com/a/13740166 with additional syntax help from
                                    // http://www.oracle.com/technetwork/issue-archive/2006/06-sep/o56asktom-086197.html
                                    query=QUERY_GET_TAGS_PAGINATED_BY_PREFIX_IN_LABEL_1_ORACLE + pageSize + QUERY_GET_TAGS_PAGINATED_BY_PREFIX_IN_LABEL_2_ORACLE;
                                } else {
                                    query=QUERY_GET_TAGS_PAGINATED_BY_PREFIX_IN_LABEL_MYSQL;
                                }
                                try (
                                        DBPreparedStatement ps = conn.run(query);
                                        DBResults results = (isOracle
                                                ? (ps.param(label + "%").param(limit + pageSize).param(limit))
                                                : (ps.param(label + "%").param(limit).param(pageSize))).executeQuery();
                                ) {
                                    HashMap<String,String> collectionNames = new HashMap<>();
                                    for (ResultSet result : results) {
                                        String collectionName = collectionNames.get(result.getString("tagcollectionid"));
                                        if (collectionName==null){
                                            collectionName = getCollectionName(result.getString("tagcollectionid"));
                                            collectionNames.put(result.getString("tagcollectionid"),collectionName);
                                        }
                                        tags.add(new Tag(result.getString("tagid"),
                                                result.getString("tagcollectionid"),
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                collectionName));

                                    }

                                    return tags;
                                }
                            }
                        }
                );
    }



    @Override
    public Optional<Tag> getForExternalIdAndCollection(final String externalId, final String tagCollectionId) {
        return db.transaction
                ("Find a tag  by id",
                        new DBAction<Optional<Tag>>() {
                            @Override
                            public Optional<Tag> call(DBConnection db) throws SQLException {
                                try (DBResults results = db.run(QUERY_GET_FOR_EXTERNALID_IN_COLLECTION)
                                        .param(externalId)
                                        .param(tagCollectionId)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        return Optional.of(new Tag(result.getString("tagid"),
                                                result.getString("tagcollectionid"),
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                getCollectionName(result.getString("tagcollectionid"))));
                                    }

                                    return Optional.empty();
                                }
                            }
                        }
                );
    }


    @Override
    public Optional<Tag> getForId(final String tagid) {
        return db.transaction
                ("Find a tag  by id",
                        new DBAction<Optional<Tag>>() {
                            @Override
                            public Optional<Tag> call(DBConnection db) throws SQLException {
                                return getForIdWithConnection(tagid, db);
                            }
                        }
                );
    }

    private Optional<Tag> getForIdWithConnection(String tagId, DBConnection conn) throws SQLException {
        try (
                DBPreparedStatement ps = conn.run(QUERY_GET_TAG_BY_ID);
                DBResults results = ps.param(tagId).executeQuery()
        ) {
            for (ResultSet result : results) {
                return Optional.of(new Tag(result.getString("tagid"),
                        result.getString("tagcollectionid"),
                        result.getString("taglabel"),
                        result.getString("description"),
                        result.getString("createdby"),
                        result.getLong("creationdate"),
                        result.getString("lastmodifiedby"),
                        result.getLong("lastmodificationdate"),
                        result.getString("externalid"),
                        result.getString("alternativelabels"),
                        result.getBoolean("externalcreation"),
                        result.getLong("externalcreationdate"),
                        result.getBoolean("externalupdate"),
                        result.getLong("lastupdatedateinexternalsystem"),
                        result.getString("parentid"),
                        result.getString("externalhierarchycode"),
                        result.getString("externaltype"),
                        result.getString("data"),
                        getCollectionName(result.getString("tagcollectionid"))));
            }
            return Optional.empty();
        }
    }


    @Override
    public List<Tag> getTagsPaginatedInCollection(final int pageNum,final int pageSize, final String tagcollectionid) {
        int limit = (pageNum-1) * pageSize;
        return db.transaction
                ("Retrieve the next results",
                        new DBAction<List<Tag>>() {
                            @Override
                            public List<Tag> call(DBConnection conn) throws SQLException {
                                final List<Tag> tags = new ArrayList<>(pageSize);
                                final String query;
                                final boolean isOracle = db.getVendor().equals("oracle");
                                if (isOracle){
                                    // http://stackoverflow.com/a/13740166 with additional syntax help from
                                    // http://www.oracle.com/technetwork/issue-archive/2006/06-sep/o56asktom-086197.html
                                    query=QUERY_GET_TAGS_PAGINATED_IN_COLLECTION_1_ORACLE + pageSize + QUERY_GET_TAGS_PAGINATED_IN_COLLECTION_2_ORACLE;
                                } else {
                                    query=QUERY_GET_TAGS_PAGINATED_IN_COLLECTION_MYSQL;
                                }

                                try (
                                        DBPreparedStatement ps = conn.run(query);
                                        DBResults results = (isOracle
                                                ? (ps.param(tagcollectionid).param(limit + pageSize).param(limit))
                                                : (ps.param(tagcollectionid).param(limit).param(pageSize))).executeQuery();
                                ) {
                                    HashMap<String,String> collectionNames = new HashMap<>();
                                    for (ResultSet result : results) {
                                        final String tagCollectionId = result.getString("tagcollectionid");
                                        String collectionName = collectionNames.get(tagCollectionId);
                                        if (collectionName==null){
                                            collectionName = getCollectionName(tagCollectionId);
                                            collectionNames.put(tagCollectionId,collectionName);
                                        }
                                        tags.add(new Tag(result.getString("tagid"),
                                                tagCollectionId,
                                                result.getString("taglabel"),
                                                result.getString("description"),
                                                result.getString("createdby"),
                                                result.getLong("creationdate"),
                                                result.getString("lastmodifiedby"),
                                                result.getLong("lastmodificationdate"),
                                                result.getString("externalid"),
                                                result.getString("alternativelabels"),
                                                result.getBoolean("externalcreation"),
                                                result.getLong("externalcreationdate"),
                                                result.getBoolean("externalupdate"),
                                                result.getLong("lastupdatedateinexternalsystem"),
                                                result.getString("parentid"),
                                                result.getString("externalhierarchycode"),
                                                result.getString("externaltype"),
                                                result.getString("data"),
                                                collectionName));
                                    }

                                    return tags;
                                }
                            }
                        }
                );
    }


    @Override
    public int getTotalTagsInCollection(final String tagcollectionid) {
        return db.transaction
                ("Retrieve the next results",
                        new DBAction<Integer>() {
                            @Override
                            public Integer call(DBConnection db) throws SQLException {
                                String query=QUERY_GET_TOTAL_TAGS_IN_COLLECTION;
                                try (DBResults results = db.run(query)
                                        .param(tagcollectionid)
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
    public String createTag(Tag tag) {
        final String currentUserId = sessionManager.getCurrentSessionUserId();
        final Long currentTime = timestampProvider.get();
        final String proposedTagId = idProvider.get();
        final String createdId = db.transaction("Create a tag ",
                new DBAction<String>() {
                    @Override
                    public String call(DBConnection db) throws SQLException {

                        try ( DBPreparedStatement ps = db.run(QUERY_CREATE_TAG)) {

                            ps.param(proposedTagId)
                                    .param(tag.getTagCollectionId())
                                    .param(tag.getTagLabel())
                                    .param(tag.getDescription())
                                    .param(currentUserId)     //Needs to be the actual user
                                    .param(currentTime)  //Needs to be now
                                    .param(currentUserId)//Needs to be the actual user
                                    .param(currentTime)//Needs to be now
                                    .param(tag.getExternalId())
                                    .param(tag.getAlternativeLabels())
                                    .param(tag.getExternalCreation() ? 1 : 0)
                                    .param(tag.getExternalCreationDate())
                                    .param(tag.getExternalUpdate() ? 1 : 0)
                                    .param(tag.getLastUpdateDateInExternalSystem())
                                    .param(tag.getParentId())
                                    .param(tag.getExternalHierarchyCode())
                                    .param(tag.getExternalType())
                                    .param(tag.getData())
                                    .executeUpdate();

                            db.commit();
                            return proposedTagId;
                        }
                    }
                }
        );
        if ( createdId != null ) {// sanity
            eventTrackingService.post(eventTrackingService.newEvent("tags.new.tag", "/tags/" + createdId, true));
        }
        return createdId;
    }

    @Override
    public void updateTag(Tag tag) {
        final String tagId;
        tagId = tag.getTagId();
        if (tagId == null) {
            throw new RuntimeException("Can't update a tag with no tagId specified");
        }
        final AtomicBoolean generateEvent = new AtomicBoolean(false);
        final String currentUserId = sessionManager.getCurrentSessionUserId();
        final Long currentTime = timestampProvider.get();
        db.transaction("Update tag  with tagid " + tagId,
                new DBAction<Void>() {
                    @Override
                    public Void call(DBConnection db) throws SQLException {

                        final Optional<Tag> previousState = getForIdWithConnection(tagId, db);
                        if ( !(previousState.isPresent()) ) {
                            throw new RuntimeException("Can't update a tag that doesn't exist. ID: " + tagId);
                        }

                        if ( !(isDirtyingUpdate(tag, previousState.get())) ) {
                            log.debug("No changes to non-calculated fields. No event launched and only updating lastmodificationdate and lastmodifiedby to tag wih ID: " + tagId);

                            try (DBPreparedStatement ps = db.run(QUERY_UPDATE_TAG_SIMPLE)) {
                                ps.param(currentUserId) //Needs to be the actual user
                                        .param(currentTime) //Needs to be now
                                        .param(tagId)
                                        .executeUpdate();
                                db.commit();
                            }
                            return null;
                        }

                        generateEvent.set(isEventGeneratingUpdate(tag, previousState.get()));

                        try (DBPreparedStatement ps = db.run(QUERY_UPDATE_TAG_FULL)) {
                            ps.param(tag.getTagCollectionId())
                                    .param(tag.getTagLabel())
                                    .param(tag.getDescription())
                                    .param(currentUserId) //Needs to be the actual user
                                    .param(currentTime) //Needs to be now
                                    .param(tag.getExternalId())
                                    .param(tag.getAlternativeLabels())
                                    .param(tag.getExternalCreation() ? 1 : 0)
                                    .param(tag.getExternalCreationDate())
                                    .param(tag.getExternalUpdate() ? 1 : 0)
                                    .param(tag.getLastUpdateDateInExternalSystem())
                                    .param(tag.getParentId())
                                    .param(tag.getExternalHierarchyCode())
                                    .param(tag.getExternalType())
                                    .param(tag.getData())
                                    .param(tagId)
                                    .executeUpdate();

                            db.commit();
                        }
                        return null;
                    }
                }
        );
        if ( generateEvent.get() ) {
            eventTrackingService.post(eventTrackingService.newEvent("tags.update.tag", "/tags/" + tagId, true));
        }
    }

    /**
     * Have any persistent, non-generated fields changed?
     *
     * @param proposed
     * @param original
     * @return
     */
    private boolean isDirtyingUpdate(Tag proposed, Tag original) {
        return isEventGeneratingUpdate(proposed, original)
                || !(Objects.equals(proposed.getExternalCreation(), original.getExternalCreation()))
                || !(Objects.equals(proposed.getExternalCreationDate(), original.getExternalCreationDate()))
                || !(Objects.equals(proposed.getExternalUpdate(), original.getExternalUpdate()))
                || !(Objects.equals(proposed.getLastUpdateDateInExternalSystem(), original.getLastUpdateDateInExternalSystem()));
    }

    /**
     * Have any perisistent, non-generated fields of potential interest to update event listeners changed?
     * (Making some heuristic judgments here in an attempt to reduce the likelihood of event storms resulting
     * from scheduled imports of large tag collections.)
     *
     * @param proposed
     * @param original
     * @return
     */
    private boolean isEventGeneratingUpdate(Tag proposed, Tag original) {
        return !(Objects.equals(proposed.getTagLabel(), original.getTagLabel()))
                || !(Objects.equals(proposed.getDescription(), original.getDescription()))
                || !(Objects.equals(proposed.getExternalId(), original.getExternalId()))
                || !(Objects.equals(proposed.getAlternativeLabels(), original.getAlternativeLabels()))
                || !(Objects.equals(proposed.getParentId(), original.getParentId()))
                || !(Objects.equals(proposed.getExternalHierarchyCode(), original.getExternalHierarchyCode()))
                || !(Objects.equals(proposed.getExternalType(), original.getExternalType()))
                || !(Objects.equals(proposed.getData(), original.getData()));
    }

    @Override
    public void deleteTag(String tagid) {
        int deletedRows = db.transaction("Delete tag  with tagid " + tagid,
                new DBAction<Integer>() {

                    @Override
                    public Integer call(DBConnection db) throws SQLException {

                        try ( DBPreparedStatement ps = db.run(QUERY_DELETE_TAG) ) {
                            int affectedRows = ps
                                    .param(tagid)
                                    .executeUpdate();

                            db.commit();

                            return affectedRows;
                        }
                    }
                }
        );
        if ( deletedRows > 0 ) {
            postTagDeleteEvent(tagid);
        }
    }

    @Override
    public List<String> deleteTagsOlderThanDateFromCollection(String tagCollectionId, long lastmodificationdate ) {
        final List<String> idsToEventOn =
            db.transaction("Delete tags from the collection" + tagCollectionId + " modified before " + lastmodificationdate,
                    new DBAction<List<String>>() {
                        @Override
                        public List<String> call(DBConnection db) throws SQLException {

                            final List<String> deletedIds = new ArrayList<>();

                            try (DBPreparedStatement ps =
                                         db.run(QUERY_SELECT_TAG_OLDER_THEN_DATE_FROM_EXTERNAL_COLLECTION)) {
                                try (DBResults idsToDelete = ps
                                        .param(tagCollectionId)
                                        .param(lastmodificationdate)
                                        .executeQuery()) {

                                    while (idsToDelete.hasNext()) {
                                        deletedIds.add(idsToDelete.next().getString(1));
                                    }

                                }
                            }

                            try (DBPreparedStatement ps =
                                         db.run(QUERY_DELETE_TAG_OLDER_THEN_DATE_FROM_EXTERNAL_COLLECTION)) {
                                final int affectedRowCount = ps.param(tagCollectionId)
                                        .param(lastmodificationdate)
                                        .executeUpdate();
                                if (affectedRowCount == 0) {
                                    deletedIds.clear();
                                } // otherwise we'll just live with the discrepancy
                            }

                            db.commit();
                            return deletedIds;
                        }
                    }
            );

        for ( String id : idsToEventOn ) {
            postTagDeleteEvent(id);
        }
        return idsToEventOn;
    }

    @Override
    public List<String> deleteTagFromExternalCollection(String externalId, String tagCollectionId ) {

        final List<String> idsToEventOn =
                db.transaction("Delete tag from the collection" + tagCollectionId + " with externalID " + externalId,
                        new DBAction<List<String>>() {
                    @Override
                    public List<String> call(DBConnection db) throws SQLException {

                        final List<String> deletedIds = new ArrayList<>();
                        try (DBPreparedStatement ps =
                                     db.run(QUERY_SELECT_TAG_FROM_EXTERNAL_COLLECTION)) {

                            try (DBResults idsToDelete = ps
                                    .param(tagCollectionId)
                                    .param(externalId)
                                    .executeQuery()) {

                                while (idsToDelete.hasNext()) {
                                    deletedIds.add(idsToDelete.next().getString(1));
                                }

                            }
                        }

                        try (DBPreparedStatement ps =
                                     db.run(QUERY_DELETE_TAG_FROM_EXTERNAL_COLLECTION)) {
                            final int affectedRowCount = ps.param(tagCollectionId)
                                    .param(externalId)
                                    .executeUpdate();
                            if (affectedRowCount == 0) {
                                deletedIds.clear();
                            } // otherwise we'll just live with the discrepancy
                        }

                        db.commit();
                        return deletedIds;

                    }
                }
        );

        for ( String id : idsToEventOn ) {
            postTagDeleteEvent(id);
        }
        return idsToEventOn;

    }


    private String getCollectionName(final String tagcollectionid) {
        return db.transaction
                ("Return a collection name by id",
                        new DBAction<String>() {
                            @Override
                            public String call(DBConnection db) throws SQLException {
                                try (DBResults results = db.run(QUERY_GET_COLLECTION_NAME)
                                        .param(tagcollectionid)
                                        .executeQuery()) {
                                    for (ResultSet result : results) {
                                        return result.getString("name");
                                    }
                                    return null;
                                }
                            }
                        }
                );
    }

    private void postTagDeleteEvent(String tagId) {
        eventTrackingService.post(eventTrackingService.newEvent("tags.delete.tag", "/tags/" + tagId, true));
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
