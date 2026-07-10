/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.samigo.api;

import java.util.function.BooleanSupplier;

public interface SamigoGradebookTitleRepairService {

    /**
     * Compare externally maintained Samigo Gradebook item names with decoded published
     * assessment titles and optionally update mismatches.
     *
     * The scan includes every Samigo external Gradebook item (batched), not just rows
     * that appear to contain HTML entities. Use {@code siteId} to limit work to one site.
     *
     * @param apply when false, only report candidates
     * @param logChanges when true, log each candidate row
     * @param siteId optional gradebook UID to limit the scan
     * @param shouldContinue when false, stop processing further batches
     */
    void repair(boolean apply, boolean logChanges, String siteId, BooleanSupplier shouldContinue);
}
