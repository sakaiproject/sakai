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
package org.sakaiproject.cmprovider;

import java.util.Map;

import org.sakaiproject.cmprovider.data.MembershipData;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.EntityView;

/**
 * Base class for member container entity providers. Specifies custom actions for
 * working with memberships.
 *
 * @author Christopher Schauer
 */
public abstract class AbstractContainerEntityProvider extends AbstractCmEntityProvider {

  /**
   * Adds or updates a membership on a CourseOffering, CourseSet, or Section.
   *
   * POST and PUT requests to /direct/ENTITY_PREFIX/eid/membership.json will be routed here.
   * @see MembershipData
   *
   * The following parameters are required:
   *
   * userId = Id of the user being added as a member
   * role   = User's role
   * status = String indicating this memberships status (e.g. if it's inactive or not)
   *
   * Request body should be a CmEntityData in json.
   * Possible responses:
   * 200 = create/update successful
   * 400 = missing required parameter userId, role, or status
   * 403 = current user does not have permission to create this entity
   */
  public abstract void addOrUpdateMembership(MembershipData data);

  /**
   * Deletes a membership on a CourseOffering, CourseSet, or Section.
   *
   * DELETE requests to /direct/ENTITY_PREFIX/eid/membership.json will be routed here.
   *
   * Request body should be a CmEntityData in json.
   * Possible responses:
   * 200 = delete successful
   * 403 = current user does not have permission to create this entity
   */
  public abstract void removeMembership(String userId, String containerId);

  @EntityCustomAction(action="membership", viewKey=EntityView.VIEW_EDIT)
  public ActionReturn addOrUpdateMembership(EntityView view, Map<String, Object> params) {
    validateRequest(params, null);
    String eid = view.getPathSegment(1);
    String userEid = (String)params.get("userId");
    if (userEid == null) throw new IllegalArgumentException("Missing required parameter: userEid");

    String role = (String)params.get("role");
    if (role == null) throw new IllegalArgumentException("Missing required parameter: role");

    String status = (String)params.get("status");
    if (status == null) throw new IllegalArgumentException("Missing required parameter: status");

    MembershipData data = new MembershipData();
    data.userId = userEid;
    data.role = role;
    data.status = status;
    data.memberContainer = eid;

    addOrUpdateMembership(data);
    return null;
  }

  @EntityCustomAction(action="membership", viewKey=EntityView.VIEW_DELETE)
  public ActionReturn removeMembership(EntityView view, Map<String, Object> params) {
    validateRequest(params, null);
    String eid = view.getPathSegment(1);
    String userEid = (String)params.get("userId");
    if (userEid == null) throw new IllegalArgumentException("Missing required parameter: userEid");

    removeMembership(userEid, eid);
    return null;
  }
}
