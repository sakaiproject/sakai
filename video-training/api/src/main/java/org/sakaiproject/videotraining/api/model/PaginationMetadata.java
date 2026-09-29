/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.api.model;

import lombok.Data;

@Data
public class PaginationMetadata {

    private final int page;
    private final int size;
    private final long totalCount;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrev;

    public PaginationMetadata(long totalCount, int page, int size) {
        this.totalCount = totalCount;
        this.size = size > 0 ? size : 10;

        this.totalPages = (totalCount == 0)
                ? 1
                : (int) Math.ceil((double) totalCount / this.size);

        this.page = Math.max(1, Math.min(page, this.totalPages));

        this.hasPrev = this.page > 1;
        this.hasNext = this.page < this.totalPages;
    }
}
