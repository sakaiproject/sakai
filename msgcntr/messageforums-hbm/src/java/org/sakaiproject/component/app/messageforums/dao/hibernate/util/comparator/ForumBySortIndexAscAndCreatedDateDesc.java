/**
 * Copyright (c) 2005-2009 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator;

import java.util.Comparator;
import java.util.Date;

import org.sakaiproject.api.app.messageforums.BaseForum;

public class ForumBySortIndexAscAndCreatedDateDesc implements Comparator<BaseForum> {

  public int compare(BaseForum forum, BaseForum otherForum) {
    if (forum != null && otherForum != null) {
      Integer index1 = forum.getSortIndex();
      Integer index2 = otherForum.getSortIndex();
      if (index1.intValue() != index2.intValue()) return index1.intValue() - index2.intValue();
      Date date1 = forum.getCreated();
      Date date2 = otherForum.getCreated();
      int rval = date2.compareTo(date1);
      if (rval == 0) {
        return otherForum.getId().compareTo(forum.getId());
      } else {
        return rval;
      }
    }
    return -1;
  }

}
