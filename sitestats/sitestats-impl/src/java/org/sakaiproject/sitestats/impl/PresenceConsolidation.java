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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.sakaiproject.sitestats.api.presence.Presence;
import lombok.extern.slf4j.Slf4j;

import lombok.NonNull;

@Slf4j
public class PresenceConsolidation {


    private static final Comparator<Presence> PRESENCE_RECORDS_ORDER = Presence.BY_BEGIN_ASC;

    private List<PresenceRecord> records;


    public PresenceConsolidation() {
        records = new ArrayList<>();
    }


    public boolean add(@NonNull Presence presence) {
        PresenceRecord additialRecord = PresenceRecord.from(presence);

        if (!additialRecord.isComplete()) {
            throw new IllegalArgumentException("Can not add incomplete record to consolidation");
        }

        if (records.contains(additialRecord)) {
            // Record is already present in list, leave unchanged
            log.debug("Record already present: {}", additialRecord);
            return false;
        }

        PresenceRecord firstOverlappingRecord = null;
        PresenceRecord lastOverlappingRecord = null;

        // Assign first and last overlapping records
        for (PresenceRecord currentRecord : records) {
            if (currentRecord.overlapsWith(additialRecord)) {
                if (firstOverlappingRecord == null) {
                    firstOverlappingRecord = currentRecord;
                }

                lastOverlappingRecord = currentRecord;
            } else if (lastOverlappingRecord != null) {
                break;
            }
        }

        log.debug("First overlapping record: {}", firstOverlappingRecord);
        log.debug("Last overlapping record: {}", lastOverlappingRecord);

        // If we have no intersections, just add the new record
        if (firstOverlappingRecord == null && lastOverlappingRecord == null) {
            records.add(additialRecord);
            sort();
            log.debug("Added new record: {}", additialRecord);
            return true;
        }

        // If there is only one intersection, extend the existing record
        if (firstOverlappingRecord != null && firstOverlappingRecord == lastOverlappingRecord) {
            Presence onlyIntersectingRecord = firstOverlappingRecord;

            if (additialRecord.isWithin(onlyIntersectingRecord)) {
                log.debug("New record is within the existing record: {}", additialRecord);
                return false;
            }

            Instant additionalRecordBegin = additialRecord.getBegin();
            Instant additionalRecordEnd = additialRecord.getEnd();

            if (additionalRecordBegin.isBefore(onlyIntersectingRecord.getBegin())) {
                onlyIntersectingRecord.setBegin(additionalRecordBegin);
            }

            if (additionalRecordEnd.isAfter(onlyIntersectingRecord.getEnd())) {
                onlyIntersectingRecord.setEnd(additionalRecordEnd);
            }

            log.debug("Extended existing record: {}", onlyIntersectingRecord);
            return true;
        }

        // If there are multiple intersections merge all from fist begin to last end
        if (firstOverlappingRecord != null && firstOverlappingRecord != lastOverlappingRecord) {
            int firstIndex = records.indexOf(firstOverlappingRecord);
            int lastIndex = records.indexOf(lastOverlappingRecord);

            for (int i = firstIndex + 1; i < lastIndex; i++) {
                records.remove(i);
            }

            PresenceRecord mergedRecord = merge(additialRecord, firstOverlappingRecord, lastOverlappingRecord);
            records.add(firstIndex, mergedRecord);
            log.debug("Merged records into: {}", mergedRecord);
            return true;
        }

        throw new IllegalStateException("Unknown condition, record should have been added");
    }

    /**
     * Adds a presence record to the consolidation
     *
     * @param records the presence record to be added
     * @return true if the record consolidation has changed by adding the new records
     */
    public boolean addAll(Collection<Presence> records) {
        boolean changed = false;

        for (Presence presenceRecordImpl : records) {
            if (add(presenceRecordImpl)) {
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Calculates the duration of the consolidated presence records
     *
     * @return Total duration of consolidated records
     */
    public Duration getDuration() {
        return records.stream()
                .map(Presence::getDuration)
                .reduce(Duration.ZERO, (subtotalDuration, duration) -> subtotalDuration.plus(duration));
    }

    public Map<Instant, PresenceConsolidation> mapByDay() {
        Map<Instant, PresenceConsolidation> recordsByDay = new HashMap<>();
        Function<Instant, PresenceConsolidation> consolidationFacrory = (day) -> new PresenceConsolidation();

        // Split cross day records
        for (int i = 0; i < records.size(); i++) {
            PresenceRecord record = records.get(i);
            log.debug("Processing record: {}", record);

            if (record.isCrossDay()) {
                Instant begin = record.getBegin();
                Instant end = record.getEnd();
                Instant beginDay = toDay(begin);
                Instant endDay = toDay(end);

                log.debug("Record crosses day: {}", record);
                log.debug("Begin: {}, End: {}", begin, end);
                log.debug("Begin day: {}, End day: {}", beginDay, endDay);

                for (Instant day = beginDay;
                        day.isBefore(end);
                        day = day.plus(1, ChronoUnit.DAYS)) {
                    
                    // If the slice is on begin or end day, use the actual begin/end
                    Instant sliceBegin = day.equals(beginDay) ? begin : day;
                    Instant sliceEnd = day.equals(endDay) ? end : day.plus(1, ChronoUnit.DAYS);

                    log.debug("Creating slice: Begin: {}, End: {}", sliceBegin, sliceEnd);

                    PresenceRecord presenceSlice = PresenceRecord.builder()
                            .begin(sliceBegin)
                            .end(sliceEnd)
                            .build();

                    // Get or create presence consolidation for day and add record
                    recordsByDay.computeIfAbsent(day, consolidationFacrory).add(presenceSlice);
                    log.debug("Added slice to day: {}, Slice: {}", day, presenceSlice);
                }
            } else {
                recordsByDay.computeIfAbsent(record.getDay(), consolidationFacrory).add(record);
                log.debug("Added record to day: {}, Record: {}", record.getDay(), record);
            }
        }

        return recordsByDay;
    }

    /**
     * Merges a presence record with one or more records that it overlaps with
     *
     * @param mergeRecord the presence record to merge
     * @param baseRecords the existing presence records must be in order of {@link #PRESENCES_ORDER}
     * @return a merged presence record
     */
    private PresenceRecord merge(@NonNull Presence mergeRecord, Presence... baseRecords) {
        // base records:       b-----------x      x----------x        x----------e
        // merge record:               b-----------------------------------------------e
        // merge:              b-------------------------------------------------------e

        Instant firstBegin = baseRecords[0].getBegin();
        Instant lastEnd = baseRecords[baseRecords.length - 1].getEnd();
    
        Instant mergedBegin = mergeRecord.getBegin();
        Instant mergedEnd = mergeRecord.getEnd();
    
        if (mergedBegin.isAfter(firstBegin)) {
            mergedBegin = firstBegin;
        }
    
        if (mergedEnd.isBefore(lastEnd)) {
            mergedEnd = lastEnd;
        }
    
        return PresenceRecord.builder()
                .begin(mergedBegin)
                .end(mergedEnd)
                .build();
    }

    /**
     * Prints a representation of the internal presence records for debugging purposes,
     * using default scale
     *
     * @return String representation of presence consolidation
     */
    public String print() {
        return print(0.5);
    }

    /**
     * Prints a representation of the internal presence records for debugging purposes
     *
     * @param scale Number of characters that represent one minute
     * @return String representation of presence consolidation
     */
    public String print(@NonNull Double scale) {
        char beginChar = 'b';
        char endChar = 'e';
        char durationChar = '-';
        char spaceChar = ' ';
        StringBuilder sb = new StringBuilder();

        Instant previousEnd = null;
        for (PresenceRecord record : records) {
            if (previousEnd != null) {
                long space = (long) Math.floor(Duration.between(previousEnd, record.getBegin()).toMinutes() * scale);
                for (int i = 0; i < space; i++) {
                    sb.append(spaceChar);
                }
            }
            previousEnd = record.getEnd();

            sb.append(beginChar);

            long len = (long) Math.floor(record.getDuration().toMinutes() * scale);
            len = len > 2 ? len - 2 : len;
            for (long i = 0; i < len ; i++) {
                sb.append(durationChar);
            }

            sb.append(endChar);
        }

        return sb.toString();
    }

    private void sort() {
        Collections.sort(records, PRESENCE_RECORDS_ORDER);
    }

    public static Instant toDay(@NonNull Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();
    }
}
