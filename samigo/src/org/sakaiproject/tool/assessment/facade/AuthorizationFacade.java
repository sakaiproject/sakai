package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;

/**
 *
 * An Authorization Facade
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 * @version 1.0
 */
public class AuthorizationFacade implements Serializable {

  public static boolean isAuthorized
    (String agentId, String function, String qualifier)
  {
    return true;
  }

  public static boolean createAuthorization
    (String agentId, String function, String qualifier)
  {
    return true;
  }
}
