/*
 * Created on May 29, 2006
 */
package org.sakaiproject.poll.dao.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;

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
}
