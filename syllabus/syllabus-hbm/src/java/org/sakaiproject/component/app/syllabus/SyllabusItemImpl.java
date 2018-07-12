/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.syllabus;


import java.util.Objects;
import java.util.Set;

import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;

/**
 * A syllabus item contains information relating to a syllabus and an order
 * within a particular context (site).
 */

@Data
@EqualsAndHashCode(of = {"userId", "contextId", "redirectURL"})
@NoArgsConstructor
@ToString(of = {"surrogateKey", "userId", "contextId", "redirectURL", "lockId"})
public class SyllabusItemImpl implements SyllabusItem {
    private Long surrogateKey;
    private String userId;
    private String contextId;
    private String redirectURL;
    private Integer lockId;
    private Set<SyllabusData> syllabi = new TreeSet<>();

    public SyllabusItemImpl(String userId, String contextId, String redirectURL) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(contextId);

        this.userId = userId;
        this.contextId = contextId;
        this.redirectURL = redirectURL;
    }
}
