/*
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
package org.sakaiproject.tool.assessment.business.questionpool;

import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tool.assessment.data.ifc.assessment.TagIfc;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = "tagId")
public class QuestionPoolTag implements TagIfc {


    private String tagId;
    private String tagLabel;
    private String tagCollectionId;
    private String tagCollectionName;


    public static QuestionPoolTag of(Tag tag) {
        return QuestionPoolTag.builder()
                .tagId(tag.getTagId())
                .tagLabel(tag.getTagLabel())
                .tagCollectionId(tag.getTagCollectionId())
                .tagCollectionName(tag.getCollectionName())
                .build();
    }
}
