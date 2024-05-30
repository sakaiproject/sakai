/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import org.apache.commons.lang3.ObjectUtils;
import org.sakaiproject.lessonbuildertool.SimplePageItem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class LessonItemRestBean {


    private Long id;
    private Long pageId;
    private Integer sequence;
    private Integer type;
    private String contentRef;
    private String title;
    private String format;
    private String html;


    @JsonIgnore
    public boolean isCreatable() {
        boolean requiredFieldsPresent = ObjectUtils.allNotNull(
                pageId,
                type,
                title
        );

        return requiredFieldsPresent;
    }

    public static LessonItemRestBean of(SimplePageItem lessonItem) {
        return LessonItemRestBean.builder()
                .id(lessonItem.getId())
                .pageId(lessonItem.getPageId())
                .sequence(lessonItem.getSequence())
                .type(lessonItem.getType())
                .contentRef(lessonItem.getSakaiId())
                .title(lessonItem.getName())
                .format(lessonItem.getFormat())
                .html(lessonItem.getHtml())
                .build();
    }
}

