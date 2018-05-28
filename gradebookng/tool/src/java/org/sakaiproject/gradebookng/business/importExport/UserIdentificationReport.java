/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.importExport;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import lombok.Getter;

import org.sakaiproject.gradebookng.business.model.GbUser;

/**
 * Contains the data relevant to user identification: identified users, missing users, unknown users and duplicate users.
 *
 * @author plukasew, bjones86
 */
public class UserIdentificationReport implements Serializable
{
    @Getter
    private final SortedSet<GbUser> identifiedUsers; // users that were matched against an id

    @Getter
    private final SortedSet<GbUser> missingUsers; // users that could have been matched against an id but weren't

    @Getter
    private final SortedSet<GbUser> unknownUsers; // ids that couldn't be matched against a user

    @Getter
    private final SortedSet<GbUser> duplicateUsers; // users that have more than one entry in the sheet

    public UserIdentificationReport(Set<GbUser> allUsers)
    {
        missingUsers = new ConcurrentSkipListSet<>(allUsers);
        identifiedUsers = new ConcurrentSkipListSet<>();
        unknownUsers = new ConcurrentSkipListSet<>();
        duplicateUsers = new ConcurrentSkipListSet<>();
    }

    public void addIdentifiedUser(GbUser user)
    {
        if (user.isValid())
        {
            if (identifiedUsers.contains(user))
            {
                duplicateUsers.add(user);
            }
            else
            {
                identifiedUsers.add(user);
                missingUsers.remove(user);
            }
        }
    }

    public void addUnknownUser(GbUser user)
    {
        if (user != null)
        {
            unknownUsers.add(user);
        }
    }

    public int getOmittedUserCount()
    {
        return unknownUsers.size() + missingUsers.size();
    }
}
