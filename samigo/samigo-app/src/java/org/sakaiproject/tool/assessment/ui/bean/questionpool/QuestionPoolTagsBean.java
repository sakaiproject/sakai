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
