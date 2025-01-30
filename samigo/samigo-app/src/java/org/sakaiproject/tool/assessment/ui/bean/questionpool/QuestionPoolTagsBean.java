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
package org.sakaiproject.tool.assessment.ui.bean.questionpool;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolTag;

import lombok.Data;

@Data
public class QuestionPoolTagsBean {


    private static final char TAGS_SEPARATOR = ',';

    private Set<QuestionPoolTag> tags;


    public Set<String> getTagIds() {
        return tags != null
                ? tags.stream()
                        .map(QuestionPoolTag::getTagId)
                        .collect(Collectors.toSet())
                : null;
    }

    public String getTagIdsCsv() {
        Set<String> tagIds = getTagIds();

        return tagIds != null
                ? StringUtils.join(tagIds, TAGS_SEPARATOR)
                : null;
    }

    public void setTagIdsCsv(String tagIdsCsv) {
        if (tagIdsCsv != null) {
            tags = Arrays.stream(StringUtils.split(tagIdsCsv, TAGS_SEPARATOR))
                    .map(tagId -> QuestionPoolTag.builder().tagId(tagId).build())
                    .collect(Collectors.toSet());
        } else {
            tags = Collections.emptySet();
        }
    }
}
