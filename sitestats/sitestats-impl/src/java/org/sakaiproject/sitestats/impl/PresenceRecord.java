/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.apache.commons.beanutils.BeanUtils;
import org.sakaiproject.sitestats.api.presence.Presence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceRecord implements Presence, Comparable<PresenceRecord> {


    private Instant begin;
    private Instant end;

    @Override
    public int compareTo(PresenceRecord other) {
        return BY_BEGIN_ASC
                .thenComparing(BY_END_ASC)
                .compare(this, other);
    }

    @Override
    public boolean isEnding() {
        return begin == null && end != null;
    }

    @Override
    public boolean isBeginning() {
        return begin != null && end == null;
    }

    @Override
    public boolean isComplete() {
        return begin != null && end != null;
    }

    @Override
    public boolean isCrossDay() {
        if (isComplete()) {
            // Is begin and end on the same day?
            return !Objects.equals(toDay(begin), toDay(end));
        } else if (isBeginning()) {
            // Did the presence begin today?
            return !Objects.equals(toDay(begin), today());
        }

        // Ending or empty presence
        return false;
    }

    @Override
    public Duration getDuration() {
        if (!isComplete()) {
            return Duration.ZERO;
        }

        return Duration.between(begin, end);
    }

    @Override
    public Instant getDay() {
        if (isComplete() || isEnding()) {
            return toDay(end);
        } else if (isBeginning()) {
            return toDay(begin);
        }

        // Empty presence
        return null;
    }

    @Override
    public boolean overlapsWith(@NonNull Presence other) {
        Instant otherBegin = other.getBegin();
        Instant otherEnd = other.getEnd();

        return (begin == null || (otherEnd == null || begin.isBefore(otherEnd)))
                && (otherBegin == null || (end == null || otherBegin.isBefore(end)));
    }

    @Override
    public boolean isWithin(@NonNull Presence other) {
        if (end == null || begin == null) {
            return false;
        }

        Instant otherBegin = other.getBegin();
        Instant otherEnd = other.getEnd();

        return (otherBegin == null || otherBegin.isBefore(begin)) && (otherEnd == null || otherEnd.isAfter(end));
    }

    /**
     * Converts {@link org.sakaiproject.sitestats.api.presence.Presence}
     * to       {@link org.sakaiproject.sitestats.impl.PresenceRecord}
     *
     * @param presence The presence to convert
     * @return The presence object if it's already a record else a new record instance
     */
    public static PresenceRecord from(@NonNull Presence presence) {
        if (presence instanceof PresenceRecord) {
            return (PresenceRecord) presence;
        }

        PresenceRecord record = new PresenceRecord();

        try {
            BeanUtils.copyProperties(record, presence);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalArgumentException(String.format("Provided %s of type %s is not complatible with %s",
                    Presence.class.getName(), presence.getClass().getName(), PresenceRecord.class.getName()), exception);
        }

        return record;
    }

    private static Instant toDay(@NonNull Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();
    }

    private static Instant today() {
        return toDay(Instant.now());
    }
}
