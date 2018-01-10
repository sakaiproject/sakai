/**********************************************************************************
 * $URL: $
 * $Id: SakaiBasicDataSource.java 105077 2012-02-24 22:54:29Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2015 Sakai Foundation
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

package org.sakaiproject.hikaricp.jdbc.pool;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * SakaiBasicDataSource extends hikari CP's DataSource for compatibility with prior settings...
 * </p>
 */
@Slf4j
public class SakaiBasicDataSource extends HikariDataSource
{
    /** Configuration: to rollback each connection when returned to the pool. */
    //TODO: Is this a data source property?
    protected boolean m_rollbackOnReturn = false;
    //Needed for DBCP compat, not sure if these are still useful
    //TODO: Are these data source properties?
    private boolean poolPreparedStatements;
    private int maxOpenPreparedStatements;

    private String isolationLevel;
    
    //For compatibility with previous
    private String url;
    //DriveclassName in AbstractHikariConfig is protected and no getter, we need to get at it
    private String driverClassName;

    public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        log.info("MaxOpenPreparedStatments not used");
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        log.info("PoolPreparedStatements not used");
        this.poolPreparedStatements = poolPreparedStatements;
    }

    private void setDefaultTransactionIsolation(String isolationLevel) {
        this.isolationLevel = isolationLevel;
        
    }
    /**
     * Set the default transaction isolation level from a string value, based on the settings and values in java.sql.Connection
     * 
     * @param defaultTransactionIsolation
     */
    public void setDefaultTransactionIsolationString(String defaultTransactionIsolation)
    {
        if ((defaultTransactionIsolation == null) || (defaultTransactionIsolation.trim().length() == 0))
        {
            setDefaultTransactionIsolation(null);
        }
        else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_NONE"))
        {
            setDefaultTransactionIsolation("TRANSACTION_NONE");
        }
        else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_READ_UNCOMMITTED"))
        {
            setDefaultTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        }
        else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_READ_COMMITTED"))
        {
            setDefaultTransactionIsolation("TRANSACTION_READ_COMMITTED");
        }
        else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_REPEATABLE_READ"))
        {
            setDefaultTransactionIsolation("TRANSACTION_REPEATABLE_READ");
        }
        else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_SERIALIZABLE"))
        {
            setDefaultTransactionIsolation("TRANSACTION_SERIALIZABLE");
        }
        else
        {
            setDefaultTransactionIsolation(null);
            log.warn("invalid transaction isolation level: " + defaultTransactionIsolation);
        }
    }

    /**
     * Set the rollback on borrow configuration.
     * 
     * @param value
     *        if true, rollback each connection when borrowed from the pool, if false, do not.
     */
    public synchronized void setRollbackOnBorrow(boolean value)
    {
        m_rollbackOnReturn = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    //validationQuery in Hikari is called connectionTestQuery, map to it
    public String getValidationQuery() {
        return super.getConnectionTestQuery();
    }

    public void setValidationQuery(String validationQuery) {
        super.setConnectionTestQuery(validationQuery);
    }

    /**
     * @exception SQLException
     *            if the object pool cannot be created.
     */
    protected void init() throws SQLException
    {
        log.info("init()");

        //Do some quick validation
        if (getUsername() == null) {
            log.warn("Hikari DataSource configured without a 'username'");
        }
        
        if (getPassword() == null) {
            log.warn("Hikari DataSource configured without a 'password'");
        }

        //For backward compatibility with old methods
        if (url != null && !"".equals(url)) {
            super.setJdbcUrl(url);
            //This seems to also be required as HikariCP isn't registering this class if it's specified and it gives an error
            if (driverClassName != null) {
                super.setDriverClassName(driverClassName);
                try {
                    Class driverClass;
                    driverClass = Class.forName(driverClassName);
                    DriverManager.registerDriver((Driver) driverClass.newInstance());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    String message = "driverClass not specified, might not be able to load the driver'";
                    log.info(message, e);
                }
            }
        }
        
        super.setTransactionIsolation(isolationLevel);
        
        //Validate the class to verify it loaded
        try {
            super.validate();
        }
        catch (Exception t)
        {
            String message = "Cannot load JDBC driver class '" + driverClassName + "'";
            log.error(message, t);
            throw new SQLException(message,t);
        }
    }
}
