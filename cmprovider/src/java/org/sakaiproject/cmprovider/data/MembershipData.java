package org.sakaiproject.cmprovider.data;

import org.sakaiproject.cmprovider.data.validation.NotEmpty;

/**
 * Represents a membership
 * @see org.sakaiproject.coursemanagement.api.Membership
 *
 * Example json:
 * { "userId": "crs142",
 *   "role": "student",
 *   "status": "active",
 *   "containerEid": "term1.CS101.1" }
 *
 * @author Christopher Schauer
 */
public class MembershipData {
  @NotEmpty public String userId;
  public String role;
  public String status;
  @NotEmpty public String memberContainer;

  public String getId() { return userId + "," + memberContainer; }

  public MembershipData() {}
}
