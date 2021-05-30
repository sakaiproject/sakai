/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.time.api;

import java.time.Instant;

/**
 * TimeRange Service
 *
 */
public interface TimeRangeService {

    /**
     * Get a TimeRange, from our string format.
     * 
     * @param value
     *        The TimeRange string.
     * @return A TimeRange.
     */
    TimeRange newTimeRange(String value);


    /**
     * Get a TimeRange, from a time value long start and duration
     * 
     * @param start
     *        The long start time (milliseconds since).
     * @param duration
     *        The long milliseconds duration.
     * @return A TimeRange.
     * 
     */
    TimeRange newTimeRange(long start, long duration);

    /**
     * Get a TimeRange, from a single time.
     * 
     * @param startAndEnd
     *        The Time for the range.
     * @return A TimeRange.
     * @deprecated the use of time is discouraged in favour of {@link TimeRangeService#newTimeRange(Instant)}. This interface will be removed in 2.0
     */
    TimeRange newTimeRange(Time startAndEnd);

    /**
     * Get a TimeRange, from two times, inclusive.
     * 
     * @param start
     *        The start time.
     * @param end
     *        The end time.
     * @return A TimeRange.
     * @deprecated the use of time is discouraged in favour of {@link TimeRangeService#newTimeRange(Instant, Instant)}. This interface will be removed in 2.0
     */
    TimeRange newTimeRange(Time start, Time end);
    
    /**
     * Get a TimeRange, from parts.
     * 
     * @param start
     *        The start Time.
     * @param end
     *        The end Time.
     * @param startIncluded
     *        true if start is part of the range, false if not.
     * @param endIncluded
     *        true of end is part of the range, false if not.
     * @return A TimeRange.
     * @deprecated the use of time is discouraged in favour of {@link TimeRangeService#newTimeRange(Instant, Instant, boolean, boolean)}. This interface will be removed in 2.0
     */
    TimeRange newTimeRange(Time start, Time end, boolean startIncluded, boolean endIncluded);

    /**
     * Get a TimeRange, from a single time.
     * 
     * @param startAndEnd
     *        The Time for the range.
     * @return A TimeRange.
     */
    TimeRange newTimeRange(Instant startAndEnd);

    /**
     * Get a TimeRange, from two times, inclusive.
     * 
     * @param start
     *        The start time.
     * @param end
     *        The end time.
     * @return A TimeRange.
     */
    TimeRange newTimeRange(Instant start, Instant end);
    
    /**
     * Get a TimeRange, from parts.
     * 
     * @param start
     *        The start Time.
     * @param end
     *        The end Time.
     * @param startIncluded
     *        true if start is part of the range, false if not.
     * @param endIncluded
     *        true of end is part of the range, false if not.
     * @return A TimeRange.
     */
    TimeRange newTimeRange(Instant start, Instant end, boolean startIncluded, boolean endIncluded);
}
