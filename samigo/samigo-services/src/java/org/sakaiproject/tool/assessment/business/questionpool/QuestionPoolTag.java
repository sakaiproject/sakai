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
