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
package org.sakaiproject.sitestats.api.presence;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

public interface Presence {


    public static final Comparator<Presence> BY_BEGIN_ASC = Comparator.comparing(Presence::getBegin, Comparator.nullsLast(Comparator.naturalOrder()));
    public static final Comparator<Presence> BY_END_ASC = Comparator.comparing(Presence::getEnd, Comparator.nullsLast(Comparator.naturalOrder()));


    public Instant getBegin();

    public void setBegin(Instant begin);

    public Instant getEnd();

    public void setEnd(Instant end);

    public boolean isEnding();

    public boolean isBeginning();

    public boolean isComplete();

    public boolean isCrossDay();

    public boolean isWithin(Presence other);

    public boolean overlapsWith(Presence other);

    public Duration getDuration();

    public Instant getDay();
}
