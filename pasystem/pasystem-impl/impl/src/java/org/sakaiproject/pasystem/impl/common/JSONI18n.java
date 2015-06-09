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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sakaiproject.pasystem.api.I18n;

/**
 * A thread-safe I18N implementation that reads from a JSON file.
 */
public class JSONI18n implements I18n {

    private Map<String, String> translations;

    public JSONI18n(ClassLoader loader, String resourceBase, Locale locale) {
        String language = "default";

        if (locale != null) {
            language = locale.getLanguage();
        }

        InputStream stream = loader.getResourceAsStream(resourceBase + "/" + language + ".json");

        if (stream == null) {
            stream = loader.getResourceAsStream(resourceBase + "/default.json");
        }

        if (stream == null) {
            throw new RuntimeException("Missing default I18n file: " + resourceBase + "/default.json");
        }

        try {
            JSONParser parser = new JSONParser();
            translations = new ConcurrentHashMap((JSONObject) parser.parse(new InputStreamReader(stream)));
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failure when reading I18n stream", e);
        }
    }

    @Override
    public String t(String key) {
        String result = translations.get(key);

        if (result == null) {
            throw new RuntimeException("Missing translation for key: " + key);
        }

        return result;
    }

}
