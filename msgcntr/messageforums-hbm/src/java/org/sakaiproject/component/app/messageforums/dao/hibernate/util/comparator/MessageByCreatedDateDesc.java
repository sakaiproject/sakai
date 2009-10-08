package org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator;

import java.util.Comparator;

import org.sakaiproject.api.app.messageforums.Message;

/**
 * Compares two messages such that the messages may be sorted from newest to oldest:
 * 
 * If o1.getCreated() < o2.getCreated(), returns a positive integer.
 * If o1.getCreated() == o2.getCreated(), returns 0.
 * If o1.getCreated() > o2.getCreated(), returns a negative integer.
 * 
 * If the message itself or its create date is null (if this is possible), it will be treated as older 
 * than valid messages and messages with valid create dates.
 * 
 * @author mizematr
 */
public class MessageByCreatedDateDesc implements Comparator<Message> {

  public int compare(Message o1, Message o2) {
    if (o1 == null && o2 == null) {
      return 0;
    } else if (o2 == null || o2.getCreated() == null) {
      return o1.getCreated() == null ? 0 : -1;
    } else if (o1 == null || o1.getCreated() == null) {
      return o2.getCreated() == null ? 0 : 1;
    } else {
      int rval = o2.getCreated().compareTo(o1.getCreated());
      if (rval == 0) {
        return o2.getId().compareTo(o1.getId());
      } else {
        return rval;
      }
    }
  }

}
