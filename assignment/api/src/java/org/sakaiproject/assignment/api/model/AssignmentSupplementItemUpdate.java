/*
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
package org.sakaiproject.assignment.api.model;

import java.time.Instant;
import java.util.Set;

/** Changes to an assignment's supplemental items. A null change leaves that item untouched. */
public record AssignmentSupplementItemUpdate(
        Change<ModelAnswer> modelAnswer,
        Change<Note> note,
        Change<AllPurpose> allPurpose) {

    public record Change<T>(boolean delete, T value) {
        public static <T> Change<T> remove() {
            return new Change<>(true, null);
        }

        public static <T> Change<T> save(T value) {
            return new Change<>(false, value);
        }
    }

    public record ModelAnswer(String text, int showTo, Set<String> attachmentIds) {}

    public record Note(String text, int shareWith) {}

    /** A null selectedAccess leaves existing access entries unchanged. */
    public record AllPurpose(String title, String text, boolean hide, Instant releaseTime,
                             Instant retractTime, Set<String> attachmentIds, Set<String> selectedAccess) {}
}
