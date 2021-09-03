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

import org.sakaiproject.rubrics.api.model.Criterion;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CriterionTransferBean {

    public Long id;
    public String title;
    public String description;
    public Float weight;
    public List<RatingTransferBean> ratings;
    public String ownerId;

    public static CriterionTransferBean of(Criterion criterion) {

        CriterionTransferBean bean = new CriterionTransferBean();
        bean.id = criterion.getId();
        bean.title = criterion.getTitle();
        bean.description = criterion.getDescription();
        bean.weight = criterion.getWeight();
        bean.ratings = criterion.getRatings().stream().map(RatingTransferBean::of).collect(Collectors.toList());
        bean.ownerId = criterion.getOwnerId();
        return bean;
    }

    public Criterion toCriterion() {

        Criterion criterion = new Criterion();
        criterion.setId(id);
        criterion.setTitle(title);
        criterion.setDescription(description);
        criterion.setWeight(weight);
        criterion.setRatings(ratings.stream().map(r -> r.toRating()).collect(Collectors.toList()));
        criterion.setOwnerId(ownerId);
        return criterion;
    }
}
