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

import org.sakaiproject.cardgame.api.model.CardGameStatItem;
import org.sakaiproject.user.api.User;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class CardGameUserRestBean {


    public String id;
    public String displayName;
    public Integer hits;
    public Integer misses;
    public Boolean markedAsLearned;


    public static CardGameUserRestBean of(@NonNull User user, CardGameStatItem statItem) {
        // If passed statItem is null create one with defaults
        CardGameStatItem userStatItem = statItem != null ? statItem : CardGameStatItem.builderWithDefaults().build();

        return CardGameUserRestBean.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .hits(userStatItem.getHits())
                .misses(userStatItem.getMisses())
                .markedAsLearned(userStatItem.getMarkedAsLearned())
                .build();
    }

}
