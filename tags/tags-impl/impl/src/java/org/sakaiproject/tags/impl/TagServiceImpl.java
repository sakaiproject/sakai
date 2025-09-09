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

package org.sakaiproject.tags.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.tags.api.I18n;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagAssociation;
import org.sakaiproject.tags.api.TagAssociationRepository;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagCollections;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tags.api.TagServiceException;
import org.sakaiproject.tags.api.Tags;
import org.sakaiproject.tags.impl.common.SakaiI18n;

/**
 * The implementation of the Tags Service service.  Provides system initialization
 * and access to the Tags Service and sub-services.
 */
@Slf4j
public class TagServiceImpl implements TagService {

    private static final String TAGSERVICE_AUTODDL_PROPERTY =  "tagservice.auto.ddl";
    private static final String SAKAI_AUTODDL_PROPERTY =  "auto.ddl";
    private static final String SAKAI_DB_VENDOR_PROPERTY =  "vendor@org.sakaiproject.db.api.SqlService";
    private static final String TAGSERVICE_MAXPAGESIZE =  "tagservice.maxpagesize";
    private static final String TAGSERVICE_ENABLED =  "tagservice.enabled";
    private static final Boolean TAGSERVICE_ENABLED_DEFAULT_VALUE =  false;
    private static final int TAGSERVICE_MAXPAGESIZE_DEFAULT_VALUE = 200;
    private static final int TAG_MAX_LABEL = 255;

    @Getter @Setter private SqlService sqlService;
    @Getter @Setter private FunctionManager functionManager;
    @Getter @Setter private ServerConfigurationService serverConfigurationService;
    @Getter @Setter private TagAssociationRepository tagAssociationRepository;

    //At this moment we will leave the template cache, but I feel the service doesn't need it.
    @Getter @Setter private TagCollections tagCollections;
    @Getter @Setter private Tags tags;

    @Override
    public void init() {
        if (serverConfigurationService.getBoolean(SAKAI_AUTODDL_PROPERTY, false) || serverConfigurationService.getBoolean(TAGSERVICE_AUTODDL_PROPERTY, false)) {
            runDBMigration(serverConfigurationService.getString(SAKAI_DB_VENDOR_PROPERTY));
        }
        functionManager.registerFunction(TAGSERVICE_MANAGE_PERMISSION);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void saveTagAssociation(String itemId, String tagId) {
        TagAssociation tagAssociation = new TagAssociation();
        tagAssociation.setItemId(itemId);
        tagAssociation.setTagId(tagId);
        tagAssociationRepository.newTagAssociation(tagAssociation);
    }

    @Override
    public List<String> getTagAssociationIds(String collectionId, String itemId) {
        return tagAssociationRepository.findTagAssociationByCollectionAndItem(collectionId, itemId).stream().map(TagAssociation::getTagId).collect(Collectors.toList());
    }

    @Override
    public List<Tag> getAssociatedTagsForItem(String collectionId, String itemId) {//if we turn Tag object to jpa we could add a foreign key to the association and retrieve them directly
        List<String> tagIds = getTagAssociationIds(collectionId, itemId);
        List<Tag> associatedTags = new ArrayList<>();
        for (String tagId : tagIds) {
            Tag t = tags.getForId(tagId).orElse(null);
            if (t != null) {
                associatedTags.add(t);
            } else {
                log.warn("Associated tag with id {} does not exist anymore" + tagId);
            }
        }
        return associatedTags;
    }

    @Override
    public void updateTagAssociations(String collectionId, String itemId, Collection<String> tagIds, boolean isSite) {
        // create collection if it doesn't exist
        TagCollection col = tagCollections.getForId(collectionId).orElse(null);
        if (col == null) {
            I18n i18n = getI18n(this.getClass().getClassLoader(), "org.sakaiproject.tags.api.i18n.tagservice");
            String description = i18n.t("user_collection");
            if (isSite) {
                description = i18n.tFormatted("site_collection", collectionId);
            }
            col = new TagCollection(collectionId, collectionId, description, null, 0L, null, null, null, 0L, Boolean.FALSE, Boolean.FALSE, 0L, 0L);
            tagCollections.createTagCollection(col);
        }

        // obtain previous asociations
        List<String> oldAssociationIds = getTagAssociationIds(collectionId, itemId);
        for (String tagId : tagIds) {
            // skip if already associated or empty
            if (StringUtils.isEmpty(tagId) || oldAssociationIds.contains(tagId)) {
                continue;
            }
            // we cut the tag
            if (tagId.length() > TAG_MAX_LABEL) {
                tagId = tagId.substring(0, TAG_MAX_LABEL);
            }
            // new association, check tag exists
            Tag t = tags.getForId(tagId).orElse(null);
            if (t == null) {
                
                t = new Tag(null, collectionId, tagId, null, null,
                        0L, null, 0L, null, null, Boolean.FALSE, 0L,
                        Boolean.FALSE, 0L, null, null, null, null, null);
                String id = tags.createTag(t);
                t.setTagId(id);
            }

            // save tag association
            saveTagAssociation(itemId, t.getTagId());
        }

        // remove deselected
        oldAssociationIds.removeAll(tagIds);
        for (String oldId : oldAssociationIds) {
            TagAssociation ta = tagAssociationRepository.findTagAssociationByItemIdAndTagId(itemId, oldId);
            tagAssociationRepository.deleteTagAssociation(ta.getId());
        }
    
    }

    @Override
    public I18n getI18n(ClassLoader loader, String resourceBase) {
        return new SakaiI18n(loader, resourceBase);
    }

    @Override
    public int getMaxPageSize() { return serverConfigurationService.getInt(TAGSERVICE_MAXPAGESIZE, TAGSERVICE_MAXPAGESIZE_DEFAULT_VALUE); }


    @Override
    public Boolean getServiceActive (){
        return serverConfigurationService.getBoolean(TAGSERVICE_ENABLED, TAGSERVICE_ENABLED_DEFAULT_VALUE);
    }

    private void runDBMigration(final String vendor) {
        String migrationFile = "db/migration/" + vendor + ".sql";
        InputStream is = TagServiceImpl.class.getClassLoader().getResourceAsStream(migrationFile);

        if (is == null) {
            throw new TagServiceException("Failed to find migration file: " + migrationFile);
        }

        InputStreamReader migrationInput = new InputStreamReader(is);

        try {
            Connection db = sqlService.borrowConnection();

            try {
                for (String sql : parseMigrationFile(migrationInput)) {
                    try {
                        PreparedStatement ps = db.prepareStatement(sql);
                        ps.execute();
                        ps.close();
                    } catch (SQLException e) {
                        log.warn("runDBMigration: " + e + "(sql: " + sql + ")");
                    }
                }
            } catch (IOException e) {
                throw new TagServiceException("Failed to read migration file: " + migrationFile, e);
            } finally {
                sqlService.returnConnection(db);

                try {
                    migrationInput.close();
                } catch (IOException e) {}
            }
        } catch (SQLException e) {
            throw new TagServiceException("Database migration failed", e);
        }
    }

    private String[] parseMigrationFile(InputStreamReader migrationInput) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];

        int len;
        while ((len = migrationInput.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }

        return sb.toString().replace("\n", " ").split(";\\s*");
    }

}
