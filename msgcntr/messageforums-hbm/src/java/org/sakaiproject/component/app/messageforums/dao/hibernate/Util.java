/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/Util.java $
 * $Id: Util.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Util {

    public static List setToList(Set set) {
        List list = new ArrayList();
        if (set != null) {
            for (Iterator iter = set.iterator(); iter.hasNext();) {
                Object object = (Object) iter.next();
                if (object != null) {
                    list.add(object);
                }
            }
        }
        return list;
    }
    
    public static Set listToSet(List list) {
        Set set = new HashSet();
        if (list != null) {
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object object = (Object) iter.next();
                if (object != null) {
                    set.add(object);
                }
            }
        }
        return set;
    }
    
    
}
