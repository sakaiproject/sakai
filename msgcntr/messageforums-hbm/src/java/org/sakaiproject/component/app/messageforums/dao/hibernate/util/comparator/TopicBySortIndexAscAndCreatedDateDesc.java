package org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator; 

import java.util.Comparator; 

import org.sakaiproject.api.app.messageforums.Topic;

/** 
 * Compares two topics and sorts them first by their sortIndex in ascending order.  If the 
 * value for the sortIndex for the two topics is equal, the topics are then arranged in 
 * descending order (newest to oldest) according to their createdDate. 
 * 
 * Any topic with a null sortIndex will be treated as greater than a topic with any sortIndex 
 * and any topic with a null createdDate will be treated as greater (older) than a topic 
 * with any valid createdDate. 
 * 
 * @author mizematr 
 */ 
public class TopicBySortIndexAscAndCreatedDateDesc implements Comparator<Topic> { 

  public int compare(Topic o1, Topic o2) { 
    if (o1 == null && o2 == null) { 
      return 0;
    } else if (o2 == null || o2.getSortIndex() == null) {
      return 1;
    } else if (o1 == null || o1.getSortIndex() == null) {
      return -1;
    }

    int rval = o1.getSortIndex().compareTo(o2.getSortIndex());
    if (rval != 0) {
      return rval;
    } else if (o2.getCreated() == null) {
      return 1;
    } else if (o1.getCreated() == null) {
      return -1;
    } 
    
    rval = o2.getCreated().compareTo(o1.getCreated());
    if (rval != 0) {
      return rval;
    } else if (o2.getId() == null) {
      return 1;
    } else if (o1.getId() == null) {
      return -1;
    } else {
      return o1.getId().compareTo(o2.getId());
    }
  }
  
}