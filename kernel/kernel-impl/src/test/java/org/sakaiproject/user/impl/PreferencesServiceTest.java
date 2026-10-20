/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.user.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.user.api.model.PreferencesRecord;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesConflictException;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PreferencesServiceTestConfiguration.class)
@TestPropertySource(properties = {
        "hibernate.connection.url=jdbc:hsqldb:mem:preferences-service",
        "hibernate.hbm2ddl.auto=create-drop"
})
public class PreferencesServiceTest {

    @Autowired private PreferencesService preferencesService;
    @Autowired private SpringCrudRepository<PreferencesRecord, String> repository;
    @Autowired private SecurityService securityService;
    @Autowired private DataSource dataSource;
    @Autowired private PlatformTransactionManager transactionManager;

    private String userId;

    @Before
    public void setUp() {
        userId = UUID.randomUUID().toString();
        when(securityService.unlock(anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void savesVersionedPreferencesWithoutALockTable() {
        assertTrue(preferencesService.applyEditWithAutoCommit(userId, edit -> {
            edit.getPropertiesEdit().addProperty("root", "value");
            edit.getPropertiesEdit("portal").addProperty("theme", "dark");
            edit.getPropertiesEdit("calendar").addPropertyToList("sites", "one");
            edit.getPropertiesEdit("calendar").addPropertyToList("sites", "two");
        }));
        Long firstVersion = repository.findById(userId).orElseThrow().getVersion();
        assertNotNull(firstVersion);

        assertTrue(preferencesService.applyEditWithAutoCommit(userId,
                edit -> edit.getPropertiesEdit("account").addProperty("language", "fr")));
        assertTrue(repository.findById(userId).orElseThrow().getVersion() > firstVersion);
        Preferences saved = preferencesService.getPreferences(userId);
        assertEquals("value", saved.getProperties().getProperty("root"));
        assertEquals("dark", saved.getProperties("portal").getProperty("theme"));
        assertEquals("fr", saved.getProperties("account").getProperty("language"));
        assertEquals(List.of("one", "two"), saved.getProperties("calendar").getPropertyList("sites"));
        assertEquals(Integer.valueOf(0), new JdbcTemplate(dataSource).queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SAKAI_LOCKS'", Integer.class));
    }

    @Test
    public void commitsPreferencesIndependentlyOfTheCallerTransaction() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            save("independent", "saved");
            status.setRollbackOnly();
        });

        assertEquals("saved", preferencesService.getPreferences(userId).getProperties().getProperty("independent"));
        assertTrue(repository.findById(userId).isPresent());
    }

    @Test
    public void readsAndUpdatesAnExistingXmlDocument() {
        new JdbcTemplate(dataSource).update("INSERT INTO SAKAI_PREFERENCES (PREFERENCES_ID, XML) VALUES (?, ?)",
                userId, "<preferences id=\"" + userId + "\"><properties><property name=\"old\" value=\"saved\"/></properties></preferences>");
        assertEquals(Long.valueOf(0), repository.findById(userId).orElseThrow().getVersion());
        assertEquals("saved", preferencesService.getPreferences(userId).getProperties().getProperty("old"));
        save("new", "value");
        assertEquals("saved", preferencesService.getPreferences(userId).getProperties().getProperty("old"));
        assertEquals(Long.valueOf(1), repository.findById(userId).orElseThrow().getVersion());
    }

    @Test
    public void rejectsAStaleEditWithoutOverwritingTheWinner() throws Exception {
        save("initial", "value");
        PreferencesEdit first = preferencesService.edit(userId);
        PreferencesEdit stale = preferencesService.edit(userId);
        first.getPropertiesEdit("portal").addProperty("theme", "dark");
        stale.getPropertiesEdit("account").addProperty("language", "fr");
        preferencesService.commit(first);

        assertConflict(() -> preferencesService.commit(stale));
        assertFalse(stale.isActiveEdit());
        assertEquals("dark", preferencesService.getPreferences(userId).getProperties("portal").getProperty("theme"));
        assertNull(preferencesService.getPreferences(userId).getProperties("account").getProperty("language"));
    }

    @Test
    public void reportsAConcurrentUpdateWithoutReplayingTheCallback() throws Exception {
        save("initial", "value");
        assertConcurrentSaves(false);
    }

    @Test
    public void reportsAConcurrentFirstSaveWithoutReplayingTheCallback() throws Exception {
        assertConcurrentSaves(false);
    }

    @Test
    public void rejectsConflictingListChangesWithoutReplayingTheCallback() throws Exception {
        save("initial", "value");
        assertConcurrentSaves(true);
    }

    @Test
    public void aNewSaveCanSucceedAfterAConflict() {
        save("original", "value");
        AtomicInteger callbacks = new AtomicInteger();
        assertFalse(preferencesService.applyEditWithAutoCommit(userId, edit -> {
            callbacks.incrementAndGet();
            edit.getPropertiesEdit().addProperty("loser", "value");
            // Commit another edit after this callback's document was read.
            save("winner", "value");
        }));
        assertEquals(1, callbacks.get());
        assertNull(preferencesService.getPreferences(userId).getProperties().getProperty("loser"));
        assertEquals("value", preferencesService.getPreferences(userId).getProperties().getProperty("winner"));

        save("loser", "value");
        Preferences saved = preferencesService.getPreferences(userId);
        assertEquals("value", saved.getProperties().getProperty("original"));
        assertEquals("value", saved.getProperties().getProperty("winner"));
        assertEquals("value", saved.getProperties().getProperty("loser"));
    }

    @Test
    public void cancelDiscardsAnEditAndDoesNotDeleteAConcurrentFirstSave() throws Exception {
        PreferencesEdit abandoned = preferencesService.add(userId);
        abandoned.getPropertiesEdit().addProperty("abandoned", "value");
        save("committed", "value");
        preferencesService.cancel(abandoned);
        assertFalse(abandoned.isActiveEdit());
        assertEquals("value", preferencesService.getPreferences(userId).getProperties().getProperty("committed"));
        assertNull(preferencesService.getPreferences(userId).getProperties().getProperty("abandoned"));

        PreferencesEdit existing = preferencesService.edit(userId);
        existing.getPropertiesEdit().addProperty("committed", "unsaved");
        preferencesService.cancel(existing);
        assertEquals("value", preferencesService.getPreferences(userId).getProperties().getProperty("committed"));
    }

    @Test
    public void cancelANewEditDoesNotPersistAnything() throws Exception {
        PreferencesEdit edit = preferencesService.add(userId);
        edit.getPropertiesEdit().addProperty("unsaved", "value");
        preferencesService.cancel(edit);
        assertTrue(repository.findById(userId).isEmpty());
    }

    @Test
    public void callbackFailureDoesNotPersistPartialChanges() {
        save("original", "value");
        assertFalse(preferencesService.applyEditWithAutoCommit(userId, edit -> {
            edit.getPropertiesEdit().addProperty("partial", "value");
            throw new IllegalStateException("An edit failed");
        }));
        assertNull(preferencesService.getPreferences(userId).getProperties().getProperty("partial"));
        assertEquals("value", preferencesService.getPreferences(userId).getProperties().getProperty("original"));
    }

    @Test
    public void callbackFailureOnANewUserDoesNotCreatePreferences() {
        assertFalse(preferencesService.applyEditWithAutoCommit(userId, edit -> {
            edit.getPropertiesEdit().addProperty("partial", "value");
            throw new IllegalStateException("An edit failed");
        }));
        assertTrue(repository.findById(userId).isEmpty());
    }

    @Test
    public void staleRemovalCannotDeleteAnUpdatedDocument() throws Exception {
        save("original", "value");
        PreferencesEdit stale = preferencesService.edit(userId);
        save("new", "value");
        assertConflict(() -> preferencesService.remove(stale));
        assertFalse(stale.isActiveEdit());
        assertEquals("value", preferencesService.getPreferences(userId).getProperties().getProperty("new"));
        preferencesService.remove(preferencesService.edit(userId));
        assertTrue(repository.findById(userId).isEmpty());
        assertNull(preferencesService.getPreferences(userId).getProperties().getProperty("new"));
    }

    @Test
    public void addRejectsAnExistingDocument() throws Exception {
        save("original", "value");
        assertThrows(IdUsedException.class, () -> preferencesService.add(userId));
    }

    @Test
    public void permissionDenialDoesNotRunTheCallbackOrWrite() throws Exception {
        when(securityService.unlock(anyString(), anyString())).thenReturn(false);
        AtomicBoolean called = new AtomicBoolean();
        assertFalse(preferencesService.applyEditWithAutoCommit(userId, edit -> called.set(true)));
        assertFalse(called.get());
        assertTrue(repository.findById(userId).isEmpty());
        assertThrows(PermissionException.class, () -> preferencesService.edit(userId));
    }

    @Test
    public void doesNotRetryAnUnrelatedIntegrityFailure() {
        AtomicInteger attempts = new AtomicInteger();
        assertFalse(preferencesService.applyEditWithAutoCommit("x".repeat(100), edit -> {
            attempts.incrementAndGet();
            edit.getPropertiesEdit().addProperty("value", "invalid user ID length");
        }));
        assertEquals(1, attempts.get());
    }

    @Test
    public void reportsADuplicateFirstCommitAsASakaiConflict() throws Exception {
        PreferencesEdit first = preferencesService.add(userId);
        PreferencesEdit second = preferencesService.add(userId);
        first.getPropertiesEdit().addProperty("winner", "saved");
        second.getPropertiesEdit().addProperty("loser", "discarded");
        preferencesService.commit(first);
        assertConflict(() -> preferencesService.commit(second));
        assertFalse(second.isActiveEdit());
        assertEquals("saved", preferencesService.getPreferences(userId).getProperties().getProperty("winner"));
        assertNull(preferencesService.getPreferences(userId).getProperties().getProperty("loser"));
    }

    @Test
    public void rejectsAnEmptyUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> preferencesService.applyEditWithAutoCommit(" ", edit -> fail("Must not edit")));
    }

    private void save(String name, String value) {
        assertTrue(preferencesService.applyEditWithAutoCommit(userId,
                edit -> edit.getPropertiesEdit().addProperty(name, value)));
    }

    private void assertConcurrentSaves(boolean appendToList) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch bothRead = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger callbacks = new AtomicInteger();
        try {
            Future<Boolean> first = executor.submit(() -> saveConcurrently("one", appendToList, bothRead, release, callbacks));
            Future<Boolean> second = executor.submit(() -> saveConcurrently("two", appendToList, bothRead, release, callbacks));
            assertTrue("Both requests must read before either writes", bothRead.await(10, TimeUnit.SECONDS));
            release.countDown();
            boolean firstSaved = first.get(10, TimeUnit.SECONDS);
            boolean secondSaved = second.get(10, TimeUnit.SECONDS);
            assertNotEquals("Exactly one concurrent save must succeed", firstSaved, secondSaved);
            assertEquals("Each callback must run once", 2, callbacks.get());
            String winner = firstSaved ? "one" : "two";
            String loser = firstSaved ? "two" : "one";
            Preferences saved = preferencesService.getPreferences(userId);
            if (appendToList) {
                assertEquals(List.of(winner), saved.getProperties("portal").getPropertyList("sites"));
            } else {
                assertEquals("value", saved.getProperties("portal").getProperty(winner));
                assertNull(saved.getProperties("portal").getProperty(loser));
            }
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private boolean saveConcurrently(String name, boolean appendToList, CountDownLatch bothRead,
            CountDownLatch release, AtomicInteger callbacks) {
        return preferencesService.applyEditWithAutoCommit(userId, edit -> {
            callbacks.incrementAndGet();
            bothRead.countDown();
            try {
                if (!release.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting for concurrent reads");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            if (appendToList) {
                edit.getPropertiesEdit("portal").addPropertyToList("sites", name);
            } else {
                edit.getPropertiesEdit("portal").addProperty(name, "value");
            }
        });
    }

    private void assertConflict(Runnable operation) {
        assertThrows(PreferencesConflictException.class, operation::run);
    }
}
