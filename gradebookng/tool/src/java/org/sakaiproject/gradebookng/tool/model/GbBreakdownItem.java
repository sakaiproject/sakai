package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;

@Data
@AllArgsConstructor
public class GbBreakdownItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private Long itemId;

    @Getter
    @Setter
    private CategoryDefinition categoryDefinition;

    @Getter
    @Setter
    private Assignment gradebookItem;

    @Getter
    @Setter
    private Double categoryPointsOrWeight;

    @Getter
    @Setter
    private Integer numGraded;
}