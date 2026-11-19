/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/** Controllable time shared by the real quota and account-validation services. */
public class MutableClock extends Clock {
    private final AtomicReference<Instant> now;
    private final ZoneId zone;

    public MutableClock() {
        this(new AtomicReference<>(Instant.now()), ZoneOffset.UTC);
    }

    private MutableClock(AtomicReference<Instant> now, ZoneId zone) {
        this.now = now;
        this.zone = zone;
    }

    public void setInstant(Instant instant) {
        now.set(instant);
    }

    public void advanceSeconds(long seconds) {
        now.updateAndGet(instant -> instant.plusSeconds(seconds));
    }

    @Override public Instant instant() { return now.get(); }
    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId zone) { return new MutableClock(now, zone); }
}
