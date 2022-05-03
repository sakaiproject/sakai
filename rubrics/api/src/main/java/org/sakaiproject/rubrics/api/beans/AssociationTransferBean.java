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

import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssociationTransferBean {

    public Long id;
    public String toolId;
    public String itemId;
    public Long rubricId;
    public String siteId;
    private Map<String, Boolean> parameters;

    public static AssociationTransferBean of(ToolItemRubricAssociation assoc) {

        AssociationTransferBean bean = new AssociationTransferBean();
        bean.id = assoc.getId();
        bean.toolId = assoc.getToolId();
        bean.itemId = assoc.getItemId();
        bean.siteId = assoc.getSiteId();
        bean.rubricId = assoc.getRubricId();
        bean.parameters = assoc.getParameters();
        return bean;
    }

    public ToolItemRubricAssociation toAssociation() {

        ToolItemRubricAssociation assoc = new ToolItemRubricAssociation();
        assoc.setId(id);
        assoc.setToolId(toolId);
        assoc.setItemId(itemId);
        assoc.setSiteId(siteId);
        assoc.setRubricId(rubricId);
        assoc.setParameters(parameters);
        return assoc;
    }
}
