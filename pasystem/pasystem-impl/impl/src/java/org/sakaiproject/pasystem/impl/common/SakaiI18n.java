/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.impl.common;

import java.util.MissingResourceException;
import org.sakaiproject.pasystem.api.I18n;
import org.sakaiproject.util.ResourceLoader;

/**
 * An I18N implementation based on Sakai's ResourceLoader.
 */
public class SakaiI18n implements I18n {

    private ResourceLoader resourceLoader;

    public SakaiI18n(ClassLoader loader, String resourceBase) {
        resourceLoader = new ResourceLoader(resourceBase, loader);
    }

    @Override
    public String t(String key) {
        String result = resourceLoader.getString(key);

        if (result == null) {
            throw new RuntimeException("Missing translation for key: " + key);
        }

        return result;
    }
}
