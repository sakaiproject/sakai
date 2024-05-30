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

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.entityproviders.LessonsEntityProvider;

import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

public class LessonConditionUtil {


    public static UIComponent makeConditionEditor(SimplePageBean simplePageBean, UIContainer parent, String rsfId) {
        return UIOutput.make(parent, rsfId)
                .decorate(new UIFreeAttributeDecorator("site-id", simplePageBean.getCurrentSiteId()))
                .decorate(new UIFreeAttributeDecorator("tool-id", LessonsEntityProvider.TOOL_COMMON_ID));
    }

    public static UIComponent makeConditionPicker(SimplePageBean simplePageBean, UIContainer parent, String rsfId) {
        SimplePageItem currentPageItem = simplePageBean.getCurrentPageItem(null);

        // Create the component only if the current page item is defined
        return currentPageItem != null
                ? UIOutput.make(parent, rsfId)
                        .decorate(new UIFreeAttributeDecorator("site-id", simplePageBean.getCurrentSiteId()))
                        .decorate(new UIFreeAttributeDecorator("tool-id", LessonsEntityProvider.TOOL_COMMON_ID))
                        .decorate(new UIFreeAttributeDecorator("lesson-id", String.valueOf(currentPageItem.getId())))
                : null;
   }
}
