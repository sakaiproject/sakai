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

import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;

import org.mockito.internal.matchers.Any;
import org.mockito.internal.matchers.Not;
import org.mockito.internal.matchers.Or;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tags.api.MissingUuidException;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.impl.common.DB;
import org.sakaiproject.tags.impl.common.DBConnection;
import org.sakaiproject.tags.impl.common.DBPreparedStatement;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.when;

public abstract class BaseStorageTest {

    // data fixture
    protected EmbeddedDatabase hsqldb;

    // SUTs
    protected TagCollectionStorage tagCollectionStorage;
    protected TagStorage tagStorage;

    protected DB db;

    @Mock
    protected SessionManager sessionManager;

    // Separate EventTrackingServices for each of our SUTs b/c it makes it easier to assert
    // on events related to one subsystem or another, especially when verifying that no
    // tag-related events were posted, but where test setup might have required the creation
    // of one or more tag collections. In a real deployment, of course, there would only
    // be one EventTrackingService.
    @Mock
    protected EventTrackingService tagCollectionsEventTrackingService;
    @Mock
    protected EventTrackingService tagsEventTrackingService;

    // mock timestamp providers rather than simple lambdas b/c we need to verify() interactions
    @Mock
    protected Supplier<Long> tagCollectionTimestampProvider;
    @Mock
    protected Supplier<Long> tagTimestampProvider;

    // Optional DB extension hook - if non-null, effectivey overrides DB.wrapConnection()
    // Can be helpful when needing to force database errors.
    protected Function<Connection, DBConnection> connectionWrapSupplier;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        hsqldb = new EmbeddedDatabaseBuilder().addScripts("db/migration/hsqldb.sql").build();

        db = new DB() {
            @Override
            protected DBConnection wrapConnection(Connection conn) {
                if ( connectionWrapSupplier == null ) {
                    return super.wrapConnection(conn);
                } else {
                    return connectionWrapSupplier.apply(conn);
                }
            }
        };
        db.setDataSource(hsqldb);
        db.setVendor("hsqldb");

        tagCollectionStorage = new TagCollectionStorage();
        tagCollectionStorage.setDb(db);
        tagCollectionStorage.setSessionManager(sessionManager);
        tagCollectionStorage.setEventTrackingService(tagCollectionsEventTrackingService);
        tagCollectionStorage.setTimestampProvider(tagCollectionTimestampProvider);

        tagStorage = new TagStorage();
        tagStorage.setDb(db);
        tagStorage.setSessionManager(sessionManager);
        tagStorage.setEventTrackingService(tagsEventTrackingService);
        tagStorage.setTimestampProvider(tagTimestampProvider);

        when(tagTimestampProvider.get()).thenReturn(now()); // some reasonable defaulting
        when(tagCollectionTimestampProvider.get()).thenReturn(now()); // some reasonable defaulting
        when(sessionManager.getCurrentSessionUserId()).thenReturn("thisuserid");
    }

    @After
    public void tearDown() {
        if ( hsqldb != null ) {
            hsqldb.shutdown();
        }
    }

    protected TagCollection newTagCollection(String discriminator) {
        final long tagCollectionCreationDate = now();
        final long tagCollectionLastSynchronizationDate = now();
        final long tagCollectionLastUpdateInExternalSystemDate = now();
        final TagCollection tagCollection = new TagCollection(null,
                "tagCollectionName"+discriminator,
                "tagCollectionDescription"+discriminator,
                "thisuserid",
                tagCollectionCreationDate,
                "tagCollectionExternalSourceName"+discriminator,
                "tagCollectionExternalSourceDescription"+discriminator,
                "thisuserid",
                tagCollectionCreationDate,
                true,
                true,
                tagCollectionLastSynchronizationDate,
                tagCollectionLastUpdateInExternalSystemDate);
        return tagCollection;
    }

    protected TagCollection saveAndReadBackNewTagCollection(TagCollection tagCollection) {
        final long creationDate = tagCollection.getCreationDate();
        when(tagCollectionTimestampProvider.get()).thenReturn(creationDate);
        final String tagCollectionId = tagCollectionStorage.createTagCollection(tagCollection);
        return tagCollectionStorage.getForId(tagCollectionId).get();
    }

    protected TagCollection newPersistentTagCollection(String discriminator) {
        final TagCollection tagCollection = newTagCollection(discriminator);
        return saveAndReadBackNewTagCollection(tagCollection);
    }

    protected Tag newTag(String discriminator, TagCollection inCollection) {
        final long tagCreationDate = now();
        final long tagExternalCreateDate = now();
        final long tagLastUpdateInExternalSystemDate = now();
        final Tag tag = new Tag(null,
                inCollection.getTagCollectionId(),
                "tagLabel"+discriminator,
                "tagDescription"+discriminator,
                "thisuserid",
                tagCreationDate,
                "thisuserid",
                tagCreationDate,
                "tagExternalId"+discriminator,
                "tagAlternativeLabels"+discriminator,
                true,
                tagExternalCreateDate,
                true,
                tagLastUpdateInExternalSystemDate,
                "tagParentId"+discriminator,
                "tagExternalHierarchyCode"+discriminator,
                "tagExternalType"+discriminator,
                "tagData"+discriminator,
                inCollection.getName());
        return tag;
    }

    protected Tag copyTag(Tag from) throws MissingUuidException {
        final Tag newTag = new Tag(from.getTagId(),
                from.getTagCollectionId(),
                from.getTagLabel(),
                from.getDescription(),
                from.getCreatedBy(),
                from.getCreationDate(),
                from.getLastModifiedBy(),
                from.getLastModificationDate(),
                from.getExternalId(),
                from.getAlternativeLabels(),
                from.getExternalCreation(),
                from.getExternalCreationDate(),
                from.getExternalUpdate(),
                from.getLastUpdateDateInExternalSystem(),
                from.getParentId(),
                from.getExternalHierarchyCode(),
                from.getExternalType(),
                from.getData(),
                from.getCollectionName());
        return newTag;
    }

    protected TagCollection copyTagCollection(TagCollection from) {
        final TagCollection newTagCollection = new TagCollection(from.getTagCollectionId(),
                from.getName(),
                from.getDescription(),
                from.getCreatedBy(),
                from.getCreationDate(),
                from.getExternalSourceName(),
                from.getExternalSourceDescription(),
                from.getLastModifiedBy(),
                from.getLastModificationDate(),
                from.getExternalUpdate(),
                from.getExternalCreation(),
                from.getLastSynchronizationDate(),
                from.getLastUpdateDateInExternalSystem());
        return newTagCollection;
    }

    protected void forceErrorOnDBPreparedStatementCreation() {
        this.connectionWrapSupplier = newAngryDbStatementFactory();
    }

    protected void stopForceErrorOnDBPreparedStatementCreation() {
        this.connectionWrapSupplier = null;
    }

    protected Function<Connection, DBConnection> newAngryDbStatementFactory() {
        return connection -> new DBConnection(connection) {
            // force a transaction failure, giving the 'real' statement a chance to fail on its own before forcing it
            public DBPreparedStatement run(String sql) throws SQLException {
                super.run(sql);
                throw new SQLException("Oops");
            }
        };
    }

    /**
     * Shorthand for using the Hamcrest {@code hasProperty()} matcher as a Mockito {@code ArgumentMatcher}. Useful
     * when {@code verify()ing} a method that takes a bean as an arg, but you only care about matching on a subset
     * of its properties.
     *
     * @param propertyName
     * @param matcher
     * @param <T>
     * @return
     */
    protected <T> HamcrestArgumentMatcher<T> argHasProperty(String propertyName, Matcher<?> matcher) {
        return new HamcrestArgumentMatcher<>(hasProperty(propertyName, matcher));
    }

    protected <T> T argThatMatchesAnyOf(ArgumentMatcher<?>... matchers) {
        ArgumentMatcher or = new Not(Any.ANY);
        for (ArgumentMatcher<?> matcher : matchers) {
            or = new Or(matcher, or);
        }
        return (T) argThat(or);
    }

    protected long now() {
        return new Date().getTime();
    }
    
}
