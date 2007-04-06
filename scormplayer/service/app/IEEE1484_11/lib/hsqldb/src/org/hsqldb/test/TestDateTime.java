/* Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Calendar;

import junit.framework.Assert;

/**
 * Tests for normalisation of Time and Date values.
 * Base on the original test submission.
 * @author Miro Halas
 */
public class TestDateTime extends TestBase {

    public TestDateTime(String s) {
        super(s);
    }

    protected void setUp() {

        super.setUp();

        try {
            Connection connection = super.newConnection();
            Statement  statement  = connection.createStatement();

            statement.execute("drop table time_test if exists");
            statement.execute("drop table date_test if exists");
            statement.execute("create table time_test(time_test time)");
            statement.execute("create table date_test(date_test date)");
            connection.close();
        } catch (Exception e) {}
    }

    /**
     * Test the database support for Date objects. Date object ignores the time
     * portion of the Java Date.
     *
     * This class inserts date into database, then retrieve it back using
     * different java time
     *
     * @throws Throwable - an error has occured during test
     */
    public void testBasicDateSupport() throws Throwable {

        final String INSERT_DATE =
            "insert into date_test(date_test) values (?)";

        // See OracleTests class why we need to select tablename.*
        final String SELECT_DATE =
            "select date_test.* from date_test where date_test = ?";
        final String DELETE_DATE =
            "delete from date_test where date_test = ?";
        Calendar          calGenerate = Calendar.getInstance();
        java.sql.Date     insertDate;
        Connection        connection = super.newConnection();
        PreparedStatement insertStatement;
        int               iUpdateCount = 0;

        // Set date of my birthday ;-)
        calGenerate.set(1995, 9, 15, 1, 2, 3);

        insertDate      = new java.sql.Date(calGenerate.getTime().getTime());
        insertStatement = connection.prepareStatement(INSERT_DATE);

        insertStatement.setDate(1, insertDate);

        iUpdateCount = insertStatement.executeUpdate();

        insertStatement.close();
        Assert.assertEquals(
            "Exactly one record with date data shoud have been inserted.",
            iUpdateCount, 1);

        // Now select it back to be sure it is there
        PreparedStatement selectStatement = null;
        PreparedStatement deleteStatement = null;
        ResultSet         results         = null;
        java.sql.Date     retrievedDate   = null;
        boolean           bHasMoreThanOne;
        int               iDeletedCount = 0;

        // Set different time, since when we are dealing with just dates it
        // shouldn't matter
        calGenerate.set(1995, 9, 15, 2, 3, 4);

        java.sql.Date selectDate =
            new java.sql.Date(calGenerate.getTime().getTime());

        selectStatement = connection.prepareStatement(SELECT_DATE);

        selectStatement.setDate(1, selectDate);

        results = selectStatement.executeQuery();

        // Get the date from the database
        Assert.assertTrue("The inserted date is not in the database.",
                          results.next());

        retrievedDate   = results.getDate(1);
        deleteStatement = connection.prepareStatement(DELETE_DATE);

        deleteStatement.setDate(1, insertDate);

        iDeletedCount = deleteStatement.executeUpdate();

        deleteStatement.close();
        Assert.assertEquals(
            "Exactly one record with date data shoud have been deleted.",
            iDeletedCount, 1);

        boolean result = retrievedDate.toString().startsWith(
            insertDate.toString().substring(0, 10));

        Assert.assertTrue(
            "The date retrieved from database "
            + DateFormat.getDateTimeInstance().format(retrievedDate)
            + " is not the same as the inserted one "
            + DateFormat.getDateTimeInstance().format(insertDate), result);
    }

    /**
     * Test the database support for Time objects. Time object ignores the date
     * portion of the Java Date.
     *
     * This class inserts time into database, then retrieve it back using
     * different java date and deletes it using cursor.
     *
     * Uses the already setup connection and transaction.
     * No need to close the connection since base class is doing it for us.
     *
     * @throws Throwable - an error has occured during test
     */
    public void testBasicTimeSupport() throws Throwable {

        final String INSERT_TIME =
            "insert into time_test(time_test) values (?)";

        // See OracleTests class why we need to select tablename.*
        final String SELECT_TIME =
            "select time_test.* from time_test where time_test = ?";
        final String DELETE_TIME =
            "delete from time_test where time_test = ?";
        Calendar          calGenerate = Calendar.getInstance();
        java.sql.Time     insertTime;
        Connection        connection = super.newConnection();
        PreparedStatement insertStatement;
        int               iUpdateCount = 0;

        // Set date of my birthday ;-)
        calGenerate.set(1995, 9, 15, 1, 2, 3);

        insertTime      = new java.sql.Time(calGenerate.getTime().getTime());
        insertStatement = connection.prepareStatement(INSERT_TIME);

        insertStatement.setTime(1, insertTime);

        iUpdateCount = insertStatement.executeUpdate();

        insertStatement.close();
        Assert.assertEquals(
            "Exactly one record with time data shoud have been inserted.",
            iUpdateCount, 1);

        // Now select it back to be sure it is there
        PreparedStatement selectStatement = null;
        PreparedStatement deleteStatement = null;
        ResultSet         results         = null;
        java.sql.Time     retrievedTime;
        int               iDeletedCount = 0;
        java.sql.Time     selectTime;

        selectStatement = connection.prepareStatement(SELECT_TIME);

        // Set different date, since when we are dealing with just time it
        // shouldn't matter
        // fredt - but make sure the date is in the same daylight saving range as today !
        calGenerate.set(1975, 4, 16, 1, 2, 3);

        selectTime = new java.sql.Time(calGenerate.getTime().getTime());

        selectStatement.setTime(1, selectTime);

        results = selectStatement.executeQuery();

        // Get the date from the database
        Assert.assertTrue("The inserted time is not in the database.",
                          results.next());

        retrievedTime = results.getTime(1);

        //
        deleteStatement = connection.prepareStatement(DELETE_TIME);

        deleteStatement.setTime(1, insertTime);

        iDeletedCount = deleteStatement.executeUpdate();

        Assert.assertEquals(
            "Exactly one record with time data shoud have been deleted.",
            iDeletedCount, 1);

        // And now test the date
        Assert.assertNotNull(
            "The inserted time shouldn't be retrieved as null from the database",
            retrievedTime);

        // Ignore milliseconds when comparing dates
        boolean result =
            retrievedTime.toString().equals(insertTime.toString());

        Assert.assertTrue(
            "The time retrieved from database "
            + DateFormat.getDateTimeInstance().format(retrievedTime)
            + " is not the same as the inserted one "
            + DateFormat.getDateTimeInstance().format(insertTime), result);
    }
}
