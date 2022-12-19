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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sakaiproject.rubrics.api.model.Criterion;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class CriterionTransferBean {

    private Long id;
    private String title;
    private String description;
    private Float weight;
    private List<RatingTransferBean> ratings = new ArrayList<>();
    private String ownerId;
    private boolean isNew;

    public CriterionTransferBean(Criterion criterion) {
        id = criterion.getId();
        title = criterion.getTitle();
        description = criterion.getDescription();
        weight = criterion.getWeight();
        ratings = criterion.getRatings().stream().map(RatingTransferBean::new).collect(Collectors.toList());
    }
}
