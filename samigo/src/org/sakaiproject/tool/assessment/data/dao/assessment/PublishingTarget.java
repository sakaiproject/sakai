package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.util.HashMap;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PublishingTarget {
  public PublishingTarget() {
  }
  public HashMap getTargets()
  {
     HashMap map = new HashMap();
     map.put("Anonymous Users", "ANONYMOUS_USERS");
     map.put("Authenticated Users", "AUTHENTICATED_USERS");
     return map;
  }
}