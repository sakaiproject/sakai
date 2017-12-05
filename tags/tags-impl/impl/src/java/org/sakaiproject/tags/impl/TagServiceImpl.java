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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.tags.api.I18n;
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

    private static final String TAGSERVICE_MANAGE_PERMISSION =  "tagservice.manage";
    private static final String TAGSERVICE_AUTODDL_PROPERTY =  "tagservice.auto.ddl";
    private static final String SAKAI_AUTODDL_PROPERTY =  "auto.ddl";
    private static final String SAKAI_DB_VENDOR_PROPERTY =  "vendor@org.sakaiproject.db.api.SqlService";
    private static final String TAGSERVICE_MAXPAGESIZE =  "tagservice.maxpagesize";
    private static final String TAGSERVICE_ENABLED =  "tagservice.enabled";
    private static final Boolean TAGSERVICE_ENABLED_DEFAULT_VALUE =  true;
    private static final int TAGSERVICE_MAXPAGESIZE_DEFAULT_VALUE = 200;

    private SqlService	sqlService	= null;
    private FunctionManager	functionManager	= null;
    private ServerConfigurationService	serverConfigurationService = null;

    //At this moment we will leave the template cache, but I feel the service doesn't need it.
    private TagCollections tagCollections;
    private Tags tags;

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
    public TagCollections getTagCollections() {
        return tagCollections;
    }

    @Override
    public Tags getTags() {
        return tags;
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



    public SqlService getSqlService() {
        return sqlService;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public FunctionManager getFunctionManager() {
        return functionManager;
    }

    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setTagCollections(TagCollections tagCollections) {
        this.tagCollections = tagCollections;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }
}
