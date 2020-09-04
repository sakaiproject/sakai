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
package org.sakaiproject.webapi.formatter;

import org.springframework.format.Formatter;
import org.springframework.lang.UsesJava8;

import java.text.ParseException;
import java.time.Instant;
import java.util.Locale;

@UsesJava8
public class EpochMillisFormatter implements Formatter<Instant> {

    @Override
    public Instant parse(String text, Locale locale) throws ParseException {
        return Instant.ofEpochMilli(Long.decode(text)/1000);
    }

    @Override
    public String print(Instant object, Locale locale) {
        return Long.toString((object.getEpochSecond() * 1000));
    }

}
