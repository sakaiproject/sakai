/**
 * Copyright (c) 2015 The Apereo Foundation
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
