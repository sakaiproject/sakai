/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2023 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
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

import org.sakaiproject.util.ResourceLoader;

// AccessType conversion between the integer value and the localized String.
@FacesConverter("org.sakaiproject.tool.assessment.jsf.convert.AccessTypeConverter")
public class AccessTypeConverter implements Converter {

    private ResourceLoader messages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages");

    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        String value = "";
        if ("java.lang.String".equals(arg2.getClass().getName())) {
            value = (String) arg2;
        } else if ("java.lang.Long".equals(arg2.getClass().getName())) {
            value = ((Long) arg2).toString();
        }

        switch(value) {
            case "31":
                return messages.getString("read_only");
            case "32":
                return messages.getString("modify");
            case "33":
                return messages.getString("read_write");
            case "34":
                return messages.getString("admin");
            default:
                break;
        }

        return "";
    }

    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        return getAsString(arg0, arg1, arg2);
    }
}
