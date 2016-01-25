package org.sakaiproject.cmprovider.data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
  @NotNull
  @Size(min=1)
  public String userId;
  
  public String role;
  public String status;
  
  @NotNull
  @Size(min=1)
  public String memberContainer;

  public String getId() { return userId + "," + memberContainer; }

  public MembershipData() {}
}
