/*
 * Copyright (c) 2017, University of Dayton
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

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.util.api.FormattedText;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class SimplePageItemUtilities {
    /**
     * Get's a name to display to the user from a SimplePageItem
     * @param name, SimplePageItem
     * @return String of the name
     */
    public static String getDisplayName(final SimplePageItem name) {
        String itemName = name.getName();
        if(itemName.isEmpty()) {
            if(!name.getHtml().isEmpty()) {
                itemName = ComponentManager.get(FormattedText.class).stripHtmlFromText(name.getHtml(), true, true);
                if(itemName.length() > 50) {
                    itemName = itemName.substring(0, 47) + "...";
                }
            }
        }

        return itemName;
    }
}
