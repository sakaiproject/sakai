/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
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
package org.sakaiproject.rubrics.api.beans;

import java.util.Map;
import java.util.Objects;

import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssociationTransferBean {

    private Long id;
    private String itemId;
    private Map<String, Integer> parameters;
    private Long rubricId;
    private String siteId;
    private String toolId;

    public AssociationTransferBean(ToolItemRubricAssociation association) {
        Objects.requireNonNull(association, "association must not be null in constructor");
        id = association.getId();
        itemId = association.getItemId();
        toolId = association.getToolId();
        rubricId = association.getRubric().getId();
        siteId = association.getRubric().getOwnerId();
        parameters = Map.copyOf(association.getParameters());
    }
}
