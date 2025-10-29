/*
 * Copyright (c) 2025 The Apereo Foundation
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
package org.sakaiproject.scorm.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchState;

/**
 * In-memory registry holding active SCORM launch sessions.
 * Each launch session is scoped to a Sakai user and retains the associated {@link SessionBean}.
 * Entries expire automatically after a configurable time-to-live (two hours by default) to avoid leaks.
 * <p>
 * The registry is strictly node-local; deployments that use round-robin load balancing must rely on
 * sticky sessions or introduce a distributed cache alternative if cross-node access is required.
 * Nothing is written to persistent storage—entries only reference the existing {@link SessionBean}
 * and {@link ContentPackage} objects already managed by the surrounding services.
 */
public class ScormLaunchSessionRegistry
{
    private final ConcurrentMap<String, Entry> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private Duration entryTtl = Duration.ofHours(2);

    public String register(SessionBean sessionBean, ContentPackage contentPackage, String userId, ScormLaunchState state, String message)
    {
        Objects.requireNonNull(sessionBean, "SessionBean must not be null");
        Objects.requireNonNull(contentPackage, "ContentPackage must not be null");
        Objects.requireNonNull(userId, "User id must not be null");
        Objects.requireNonNull(state, "state must not be null");

        purgeExpired();

        String sessionId = generateSessionId();
        sessions.put(sessionId, new Entry(sessionBean, contentPackage, userId, state, message));
        locks.putIfAbsent(sessionId, new ReentrantLock());
        return sessionId;
    }

    public Optional<Entry> lookup(String sessionId)
    {
        purgeExpired();
        return Optional.ofNullable(sessionId)
            .map(sessions::get)
            .filter(entry -> !isExpired(entry, Instant.now()));
    }

	public void remove(String sessionId)
	{
		if (sessionId != null)
		{
			sessions.remove(sessionId);
			locks.remove(sessionId);
		}
	}

	public ReentrantLock getLock(String sessionId)
	{
		if (sessionId == null)
		{
			return null;
		}
		return locks.get(sessionId);
	}

	public void updateState(String sessionId, ScormLaunchState state, String message)
	{
		if (sessionId == null || state == null)
		{
            return;
        }
        Entry entry = sessions.get(sessionId);
        if (entry != null)
        {
            entry.setState(state, message);
        }
    }

    public void setEntryTtl(Duration entryTtl)
    {
        this.entryTtl = Objects.requireNonNull(entryTtl, "entryTtl must not be null");
    }

    public void setEntryTtl(String entryTtl)
    {
        Objects.requireNonNull(entryTtl, "entryTtl must not be null");
        setEntryTtl(Duration.parse(entryTtl.trim()));
    }

    private String generateSessionId()
    {
        byte[] random = new byte[24];
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private void purgeExpired()
    {
        if (entryTtl.isZero() || entryTtl.isNegative())
        {
            return;
        }
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> {
            boolean expired = isExpired(entry.getValue(), now);
            if (expired)
            {
                locks.remove(entry.getKey());
            }
            return expired;
        });
    }

    private boolean isExpired(Entry entry, Instant reference)
    {
        if (entryTtl.isZero() || entryTtl.isNegative())
        {
            return false;
        }
        return reference.isAfter(entry.getCreatedAt().plus(entryTtl));
    }

    public static final class Entry
    {
        private final SessionBean sessionBean;
        private final ContentPackage contentPackage;
        private final String userId;
        private final Instant createdAt;
        private volatile ScormLaunchState state;
        private volatile String message;

        private Entry(SessionBean sessionBean, ContentPackage contentPackage, String userId, ScormLaunchState state, String message)
        {
            this.sessionBean = sessionBean;
            this.contentPackage = contentPackage;
            this.userId = userId;
            this.createdAt = Instant.now();
            this.state = state;
            this.message = message;
        }

        private void setState(ScormLaunchState state, String message)
        {
            this.state = state;
            this.message = message;
        }

        public SessionBean getSessionBean()
        {
            return sessionBean;
        }

        public ContentPackage getContentPackage()
        {
            return contentPackage;
        }

        public String getUserId()
        {
            return userId;
        }

        public Instant getCreatedAt()
        {
            return createdAt;
        }

        public ScormLaunchState getState()
        {
            return state;
        }

        public String getMessage()
        {
            return message;
        }
    }
}
