/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.impl.common;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.cover.SqlService;

/**
 * Logic for borrowing and returning DB connections.
 */
@Slf4j
public class DB {

    private DB() {
        // No public constructor for this utility class
    }

    /**
     * Run some database queries within a transaction.
     */
    public static <E> E transaction(DBAction<E> action) throws RuntimeException {
        return transaction(action.toString(), action);
    }

    /**
     * Run some database queries within a transaction with a helpful message if something goes wrong.
     */
    public static <E> E transaction(String actionDescription, DBAction<E> action) throws RuntimeException {
        try {
            Connection db = SqlService.borrowConnection();
            DBConnection dbc = new DBConnection(db);
            boolean autocommit = db.getAutoCommit();

            try {
                db.setAutoCommit(false);

                return action.call(dbc);
            } finally {

                if (!dbc.wasResolved()) {
                    log.warn("**************\nDB Transaction was neither committed nor rolled back.  Committing for you.");
                    dbc.commit();
                }

                if (autocommit) {
                    db.setAutoCommit(true);
                }
                SqlService.returnConnection(db);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failure in database action: " + actionDescription, e);
        }
    }
}
