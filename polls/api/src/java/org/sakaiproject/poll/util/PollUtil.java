/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

package org.sakaiproject.poll.util;

import java.util.Stack;

import org.sakaiproject.poll.model.Option;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PollUtil {

    /** Attribute names **/
    private static final String UUID = "id";
    private static final String OPTION_ID = "optionid";
    private static final String TEXT = "text";
    private static final String DELETED = "deleted";
    private static final String OPTION_ORDER = "optionorder";
    private static final String STATUS = "status";
    

    public static Element optionToXml(Option option, Document doc, Stack<Element> stack) {
        Element element = doc.createElement("option");

        if (stack.isEmpty())
        {
            doc.appendChild(element);
        }
        else
        {
            ((Element) stack.peek()).appendChild(element);
        }

        stack.push(element);

        element.setAttribute(UUID, option.getUuid());
        element.setAttribute(OPTION_ID, option.getOptionId().toString());
        element.setAttribute(TEXT, option.getText());
        element.setAttribute(DELETED, option.getDeleted().toString());
        element.setAttribute(STATUS, option.getStatus());
        element.setAttribute(OPTION_ORDER, option.getOptionOrder().toString());
        stack.pop();

        return element;
    }

    public static Option xmlToOption(Element element) {
        Option option = new Option();
        option.setUuid(element.getAttribute(UUID));
        if (!"".equals(element.getAttribute(OPTION_ID))) {
            try {
                option.setOptionId(Long.parseLong(element.getAttribute(OPTION_ID)));
            } catch (NumberFormatException e) {
                //LOG THIS
            }
        }
        option.setText(element.getAttribute(TEXT));
        option.setDeleted(Boolean.parseBoolean(element.getAttribute(DELETED)));
        option.setStatus(element.getAttribute(STATUS));
        option.setOptionOrder(Integer.parseInt(element.getAttribute(OPTION_ORDER)));
        return option;
    }
}
