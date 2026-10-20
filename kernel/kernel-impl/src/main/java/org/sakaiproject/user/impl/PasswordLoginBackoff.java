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

import java.io.Serializable;
import java.time.Clock;
import java.util.concurrent.TimeUnit;

import javax.cache.CacheException;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationThrottledException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/** Shared, temporary backoff for password evidence only. No source-address limits. */
@Slf4j
public class PasswordLoginBackoff {
    private static final int DEFAULT_THRESHOLD = 5;
    private static final int DEFAULT_WINDOW_SECONDS = 900;
    private static final int DEFAULT_MAX_DELAY_SECONDS = 16;
    private static final String INVALID_LOGIN = "Invalid Login: Either user not found or password incorrect.";

    @Setter private Ignite ignite;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private Clock clock = Clock.systemUTC();

    private IgniteCache<String, Attempts> cache;
    private int threshold;
    private long windowMillis;
    private long maxDelayMillis;

    public void init() {
        threshold = serverConfigurationService.getInt("login.password.backoff.threshold", DEFAULT_THRESHOLD);
        if (threshold < 1 || threshold > 100) {
            log.warn("Invalid login.password.backoff.threshold {}, using {}", threshold, DEFAULT_THRESHOLD);
            threshold = DEFAULT_THRESHOLD;
        }
        int maxDelaySeconds = serverConfigurationService.getInt("login.password.backoff.maxDelaySeconds", DEFAULT_MAX_DELAY_SECONDS);
        if (maxDelaySeconds < 2 || maxDelaySeconds > DEFAULT_MAX_DELAY_SECONDS) {
            log.warn("Invalid login.password.backoff.maxDelaySeconds {}, using {}", maxDelaySeconds, DEFAULT_MAX_DELAY_SECONDS);
            maxDelaySeconds = DEFAULT_MAX_DELAY_SECONDS;
        }
        int windowSeconds = serverConfigurationService.getInt("login.password.backoff.windowSeconds", DEFAULT_WINDOW_SECONDS);
        if (windowSeconds <= maxDelaySeconds) {
            // The cache expiry in ignite-components.xml reads this property directly and is not corrected here.
            log.warn("Invalid login.password.backoff.windowSeconds {}, must exceed maxDelaySeconds {}; using {}",
                    windowSeconds, maxDelaySeconds, DEFAULT_WINDOW_SECONDS);
            windowSeconds = DEFAULT_WINDOW_SECONDS;
        }
        windowMillis = TimeUnit.SECONDS.toMillis(windowSeconds);
        maxDelayMillis = TimeUnit.SECONDS.toMillis(maxDelaySeconds);
        cache = ignite.cache(PasswordLoginBackoff.class.getName());
        if (cache == null) {
            throw new IllegalStateException("Password login backoff cache is not configured");
        }
    }

    /** Reject paused requests before they can inspect or evict credential cache entries. */
    public void check(String accountKey) throws AuthenticationException {
        try {
            Attempts attempts = cache.get(accountKey);
            if (attempts != null && clock.millis() < attempts.blockedUntil) {
                throw paused();
            }
        } catch (CacheException | IllegalStateException e) {
            log.warn("Password login backoff unavailable", e);
            throw invalidLogin();
        }
    }

    /**
     * Reserve before checking a password, so concurrent guesses consume the same allowance.
     * Successful authentication clears these provisional failures. Denied requests do not
     * modify the entry or extend either its pause or its expiry.
     */
    public void reserve(String accountKey) throws AuthenticationException {
        try {
            if (!cache.invoke(accountKey, new ReserveAttempt(clock.millis(), threshold, windowMillis, maxDelayMillis))) {
                throw paused();
            }
        } catch (CacheException | IllegalStateException e) {
            log.warn("Password login backoff unavailable", e);
            // Do not silently allow unlimited password guesses during a cache outage.
            throw invalidLogin();
        }
    }

    public void clear(String accountKey) throws AuthenticationException {
        try {
            cache.remove(accountKey);
        } catch (CacheException | IllegalStateException e) {
            log.warn("Unable to clear password login backoff", e);
            throw invalidLogin();
        }
    }

    private static AuthenticationException invalidLogin() {
        return new AuthenticationException(INVALID_LOGIN);
    }

    private static AuthenticationThrottledException paused() {
        // Password clients other than the login tool keep seeing the usual invalid-login message.
        return new AuthenticationThrottledException(INVALID_LOGIN);
    }

    private static class ReserveAttempt implements EntryProcessor<String, Attempts, Boolean>, Serializable {
        private static final long serialVersionUID = 1L;
        private final long now;
        private final int threshold;
        private final long windowMillis;
        private final long maxDelayMillis;

        ReserveAttempt(long now, int threshold, long windowMillis, long maxDelayMillis) {
            this.now = now;
            this.threshold = threshold;
            this.windowMillis = windowMillis;
            this.maxDelayMillis = maxDelayMillis;
        }

        @Override
        public Boolean process(MutableEntry<String, Attempts> entry, Object... arguments) {
            Attempts previous = entry.exists() ? entry.getValue() : null;
            if (previous != null && now < previous.blockedUntil) {
                return false;
            }
            int failures = previous == null || now - previous.lastAttempt >= windowMillis
                    ? 1 : Math.min(previous.failures + 1, threshold + 5);
            long delay = failures < threshold ? 0
                    : Math.min(2000L << Math.min(failures - threshold, 5), maxDelayMillis);
            entry.setValue(new Attempts(failures, now, now + delay));
            return true;
        }
    }

    private static class Attempts implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int failures;
        private final long lastAttempt;
        private final long blockedUntil;

        Attempts(int failures, long lastAttempt, long blockedUntil) {
            this.failures = failures;
            this.lastAttempt = lastAttempt;
            this.blockedUntil = blockedUntil;
        }
    }
}
