/**
 * Copyright (c) 2005-2011 The Apereo Foundation
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
package org.sakaiproject.connector.fck;

import java.util.Comparator;

import org.w3c.dom.Element;

/**
 * This implementation of Comparator is for the very specific case of displaying files and folders in the FCK Editor in alphabetical order.
 * 
 * In order for this Comparator to work, the objects it is comparing must not be null and must also have non-null values for the name and url attributes. This is not a problem for the FCK Editor
 * Connector as all elements are created in the code and all elements must have the name and url attribute to work with FCK Editor.
 * 
 * The comparator will take two elements and first perform a case-insensitive comparison on the name attribute. If the result of the case-insensitive comparison is not 0, the comparator returns that
 * result. Otherwise, the comparator will next performa a case-sensitive comparison on the name attribute. If the result of the case-sensitive comparison is not 0, the comparator returns that result.
 * Otherwise, the comparator will return the result of a case-sensitive comparsion of the URL attributes of the elements.
 * 
 * @author mizematr@notes.udayton.edu
 * 
 */
public class SortElementsForDisplay implements Comparator<Element> {

  public int compare(Element o1, Element o2) {
    if (o1 == null || o2 == null || o1.getAttribute("name") == null || o2.getAttribute("name") == null || o1.getAttribute("url") == null || o2.getAttribute("url") == null) {
      throw new IllegalArgumentException("This comparator will not work if any object is null, one of the object's name attribute is null, or one of the object's url attributes is null.");
    }

    int rval = o1.getAttribute("name").compareToIgnoreCase(o2.getAttribute("name"));
    if (rval == 0) {
      rval = o1.getAttribute("name").compareTo(o2.getAttribute("name"));
      if (rval == 0) {
        rval = o1.getAttribute("url").compareTo(o2.getAttribute("url"));
      }
    }
    return rval;
  }

}