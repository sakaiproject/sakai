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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedRow;

/**
 * Identifier utility for user EIDs.
 * 
 * @author plukasew, bjones86
 */
@Slf4j
public class UsernameIdentifier implements Serializable
{
    private final Map<String, GbUser> userEidMap;

    @Getter
    private final UserIdentificationReport report;

    public UsernameIdentifier(List<ImportedRow> rows, Map<String, GbUser> eidMap)
    {
        userEidMap = eidMap;
        report = new UserIdentificationReport(new HashSet<>(userEidMap.values()));
        validateUsers(rows);
    }

    /**
     * Finds the user by the given identifier
     * @param row - the row data 
     * @return the user
     */
    private GbUser getUser(ImportedRow row)
    {
        String userEID = row.getStudentEid();
        GbUser user = userEidMap.get(userEID);
        if (user != null)
        {
            report.addIdentifiedUser(user);
            log.debug("User {} identified as UUID: {}", userEID, user.getUserUuid());
        }
        else
        {
            user = GbUser.forDisplayOnly(userEID, row.getStudentName().trim());
            report.addUnknownUser(user);
            log.debug("User {} is unknown to this gradebook", userEID);
        }

        return user;
    }

    /**
     * Validate all users for the imported data.
     *
     * @param userIdentifier
     * @param rows
     * @param rosterMap
     */
    private void validateUsers(List<ImportedRow> rows)
    {
        for (ImportedRow row : rows)
        {
            GbUser user = getUser(row);
            if (user != null)
            {
                row.setUser(user);
            }
        }
    }
}
