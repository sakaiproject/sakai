/**
 * Copyright (c) 2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.util;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.serialization.MapperFactory;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.entityproviders.LessonsEntityProvider;

import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

public class LessonConditionUtil {

    private static final ObjectMapper JSON_MAPPER = MapperFactory.createDefaultJsonMapper();

    private record ItemLabel(long id, String name) {}


    public static UIComponent makeConditionEditor(SimplePageBean simplePageBean, UIContainer parent, String rsfId) {
        return UIOutput.make(parent, rsfId)
                .decorate(new UIFreeAttributeDecorator("site-id", simplePageBean.getCurrentSiteId()))
                .decorate(new UIFreeAttributeDecorator("tool-id", LessonsEntityProvider.TOOL_COMMON_ID));
    }

    public static UIComponent makeConditionPicker(SimplePageBean simplePageBean, UIContainer parent, String rsfId) {
        List<SimplePageItem> pageItems = simplePageBean.getSimplePageToolDao()
                .findPageItemsBySakaiId(Long.toString(simplePageBean.getCurrentPageId()));
        if (pageItems == null || pageItems.isEmpty()) {
            return null;
        }

        List<ItemLabel> items = simplePageBean.getLessonsAccess()
                .getAccessibleItemsOnPage(simplePageBean.getCurrentSiteId(), simplePageBean.getCurrentPageId(),
                        simplePageBean.getCurrentUserId(), simplePageBean).stream()
                .map(item -> {
                    String name = item.getType() == SimplePageItem.QUESTION ? item.getAttribute("questionText") : null;
                    return new ItemLabel(item.getId(), name != null ? name : item.getName());
                }).toList();

        try {
            return UIOutput.make(parent, rsfId)
                    .decorate(new UIFreeAttributeDecorator("site-id", simplePageBean.getCurrentSiteId()))
                    .decorate(new UIFreeAttributeDecorator("tool-id", LessonsEntityProvider.TOOL_COMMON_ID))
                    .decorate(new UIFreeAttributeDecorator("lesson-items", JSON_MAPPER.writeValueAsString(items)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize condition picker items for page "
                    + simplePageBean.getCurrentPageId(), e);
        }
    }
}
