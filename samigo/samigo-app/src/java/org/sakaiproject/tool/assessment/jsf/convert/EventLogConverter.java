/**********************************************************************************
 * Copyright (c) ${license.git.copyrightYears} ${holder}

 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *             http://opensource.org/licenses/ecl2

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.jsf.convert;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.util.ResourceLoader;

@FacesConverter("org.sakaiproject.tool.assessment.jsf.convert.EventLogConverter")
@Slf4j
public class EventLogConverter implements Converter {

    private static final ResourceLoader eventLogMessages = new ResourceLoader(SamigoConstants.EVENT_LOG_BUNDLE);

    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        return getAsString(arg0, arg1, arg2);
    }

    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        String text = (String) arg2;
        if (StringUtils.isBlank(text)) {
            return "";
        }
        boolean isTranslationString = text.contains("_");
        String translatedString = eventLogMessages.getString(text);
        // This provides retro compatibility, translation strings will be displayed in the locale, DB strings will be displayed as is.
        return isTranslationString ? translatedString : text;
    }

}
