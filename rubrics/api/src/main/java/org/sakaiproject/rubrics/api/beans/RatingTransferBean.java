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

import org.sakaiproject.rubrics.api.model.Rating;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingTransferBean {

    public Long id;
    public String title;
    public String description;
    public Double points;

    public static RatingTransferBean of(Rating rating) {

        RatingTransferBean bean = new RatingTransferBean();
        bean.id = rating.getId();
        bean.title = rating.getTitle();
        bean.description = rating.getDescription();
        bean.points = rating.getPoints();
        return bean;
    }

    public Rating toRating() {

        Rating rating = new Rating();
        rating.setId(id);
        rating.setTitle(title);
        rating.setDescription(description);
        rating.setPoints(points);
        return rating;
    }
}
