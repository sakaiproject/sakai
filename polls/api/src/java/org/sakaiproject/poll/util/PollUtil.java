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
 *       http://www.osedu.org/licenses/ECL-2.0
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

        element.setAttribute("id", option.getUUId());
        element.setAttribute("optionid", option.getOptionId().toString());
        element.setAttribute("title", option.getOptionText());
        element.setAttribute("deleted", option.getDeleted().toString());
        stack.pop();

        return element;
    }
}
