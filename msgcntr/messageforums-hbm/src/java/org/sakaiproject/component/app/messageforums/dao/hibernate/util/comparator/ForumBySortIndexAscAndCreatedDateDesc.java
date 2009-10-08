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
