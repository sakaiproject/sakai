/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
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
package org.sakaiproject.api.app.messageforums;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.UUID;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.user.api.User;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Recipient Item for storing different types of recipients user/group/role
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Slf4j
public class MembershipItem implements Comparable<MembershipItem> {

    public static final int TYPE_NOT_SPECIFIED = 0;
    public static final int TYPE_ALL_PARTICIPANTS = 1;
    public static final int TYPE_ROLE = 2;
    public static final int TYPE_GROUP = 3;
    public static final int TYPE_USER = 4;
    public static final int TYPE_MYGROUPS = 5;
    public static final int TYPE_MYGROUPROLES = 6;
    public static final int TYPE_MYGROUPMEMBERS = 7;

    private static Collator collator = Collator.getInstance();
    static
    {
        try
        {
            // collator to ignore spaces
            collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
        }
        catch (ParseException e)
        {
            log.error("Unable to create RuleBasedCollator", e);
        }
    }

    public static final Comparator<MembershipItem> compareByType = Comparator.comparingInt(MembershipItem::getType);
    public static final Comparator<MembershipItem> compareByName = (o1, o2) -> collator.compare(o1.getName(), o2.getName());

    @EqualsAndHashCode.Include
    private String id;
    private Group group;
    private String name;
    private Role role;
    private int type = TYPE_NOT_SPECIFIED;
    private User user;
    @Setter private boolean viewable = false;

    private MembershipItem() {
    }

    public static MembershipItem makeMembershipItem(String name, int type) {
        return makeMembershipItem(name, type, null, null, null);
    }

    public static MembershipItem makeMembershipItem(String name, int type, Group group, Role role, User user) {
        return makeMembershipItem(name, type, group, role, user, false);
    }

    public static MembershipItem makeMembershipItem(String name, int type, Group group, Role role, User user, boolean viewable) {
        MembershipItem item = new MembershipItem();
        item.id = UUID.randomUUID().toString();
        item.name = name;
        item.type = type >= TYPE_NOT_SPECIFIED && type <= TYPE_MYGROUPMEMBERS ? type : TYPE_NOT_SPECIFIED;
        item.group = group;
        item.role = role;
        item.user = user;
        item.viewable = viewable;
        return item;
    }

    @Override
    public int compareTo(MembershipItem item) {
        return compareByType.thenComparing(compareByName).compare(this, item);
    }
}
 
