/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

package org.sakaiproject.poll.dao.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PollUtil {
    public static final Map pollCollectionToMap(Collection c) {
        TreeMap togo = new TreeMap();
        for (Iterator tit = c.iterator(); tit.hasNext();) {
            Poll task = (Poll) tit.next();
            togo.put(task.getId(), task);
        }
        return togo;
    }


    public static final List pollCollectionToList(Collection c) {
        List togo = new ArrayList();
        for (Iterator tit = c.iterator(); tit.hasNext();) {
            Poll task = (Poll) tit.next();
            togo.add(task);
        }
        return togo;
    }

    public static final List optionCollectionToList(Collection c) {
        List togo = new ArrayList();
        for (Iterator tit = c.iterator(); tit.hasNext();) {
            Option task = (Option) tit.next();
            togo.add(task);
        }
        return togo;
    }

    public static Element optionToXml(Option option, Document doc, Stack stack) {
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
        stack.pop();

        return element;
    }
}
