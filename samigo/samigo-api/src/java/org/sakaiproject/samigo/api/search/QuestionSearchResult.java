/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.samigo.api.search;

import java.io.Serializable;
import java.util.Set;

import lombok.Value;

/**
 * A question found by {@link QuestionSearchService}. Held in session scoped beans, so it must stay serializable.
 */
@Value
public class QuestionSearchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The item id, without the index's {@code /sam_item/} prefix. */
    String id;
    String typeId;
    String questionText;
    Set<String> tags;
    /** Where the question lives: a pool title or {@code site title : assessment title}. Empty if unknown. */
    String origin;
}
