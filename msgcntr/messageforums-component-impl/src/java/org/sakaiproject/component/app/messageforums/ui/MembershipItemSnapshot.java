/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.ui;

import java.util.Date;

import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.PermissionLevel;

import lombok.Data;

/**
 * Plain, cache-marshalable snapshot of a DBMembershipItem.
 * <p>
 * DBMembershipItemImpl's area/forum/topic many-to-one associations are mapped {@code lazy="false"}
 * (always real, eagerly-loaded objects), but Area/OpenForum/Topic each map several of their own
 * collections {@code lazy="true"} (Area: openForumsSet, privateForumsSet, discussionForumsSet,
 * membershipItemSet, hiddenGroups; OpenForum/Topic have similar lazy sets). Caching a
 * DBMembershipItem directly would mean Ignite's marshaller has to walk area/forum/topic and
 * almost certainly hits one of those uninitialized Hibernate collections somewhere in the
 * reachable graph. permissionLevel is safe to embed directly - PermissionLevelImpl
 * (MFR_PERMISSION_LEVEL_T) is a flat entity with no collections of its own - so only
 * area/forum/topic are reduced to plain ids here (all that any real caller ever reads off them).
 */
@Data
public class MembershipItemSnapshot implements DBMembershipItem {

	private Date created;
	private String createdBy;
	private Long id;
	private String modifiedBy;
	private Date modified;
	private String uuid;
	private Integer version;

	private String name;
	private Integer type;
	private String permissionLevelName;
	private PermissionLevel permissionLevel;

	private Long areaId;
	private Long forumId;
	private Long topicId;
}
