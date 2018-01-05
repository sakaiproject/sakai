/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.apache.commons.dbcp;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * <p>
 * SakaiBasicDataSource extends apache common's BasicDataSource ...
 * </p>
 */
@Slf4j
public class SakaiBasicDataSource extends BasicDataSource
{
	/** Configuration: to rollback each connection when returned to the pool. */
	protected boolean m_rollbackOnReturn = false;

	/**
	 * Set the default transaction isolation level from a string value, based on the settings and values in java.sql.Connection
	 * 
	 * @param defaultTransactionIsolation
	 */
	public void setDefaultTransactionIsolationString(String defaultTransactionIsolation)
	{
		if ((defaultTransactionIsolation == null) || (defaultTransactionIsolation.trim().length() == 0))
		{
			setDefaultTransactionIsolation(PoolableConnectionFactory.UNKNOWN_TRANSACTIONISOLATION);
		}
		else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_NONE"))
		{
			setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
		}
		else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_READ_UNCOMMITTED"))
		{
			setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		}
		else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_READ_COMMITTED"))
		{
			setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_REPEATABLE_READ"))
		{
			setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		}
		else if (defaultTransactionIsolation.trim().equalsIgnoreCase("TRANSACTION_SERIALIZABLE"))
		{
			setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		}
		else
		{
			setDefaultTransactionIsolation(PoolableConnectionFactory.UNKNOWN_TRANSACTIONISOLATION);
			log.warn("invalid transaction isolation level: {}", defaultTransactionIsolation);
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

	/**
	 * <p>
	 * Sakai changes: use the SakaiPoolableConnectionFactory, removed some not-visible (damn the use of private!) code.
	 * </p>
	 * <p>
	 * Create (if necessary) and return the internal data source we are using to manage our connections.
	 * </p>
	 * <p>
	 * <strong>IMPLEMENTATION NOTE</strong> - It is tempting to use the "double checked locking" idiom in an attempt to avoid synchronizing on every single call to this method. However, this idiom fails to work correctly in the face of some optimizations
	 * that are legal for a JVM to perform.
	 * </p>
	 * 
	 * @exception SQLException
	 *            if the object pool cannot be created.
	 */
	protected synchronized DataSource createDataSource() throws SQLException
	{

		// Return the pool if we have already created it
		if (dataSource != null)
		{
			return (dataSource);
		}

		// Load the JDBC driver class
		if (driverClassName != null)
		{
			try
			{
				Class.forName(driverClassName);
			}
			catch (Throwable t)
			{
				String message = "Cannot load JDBC driver class '" + driverClassName + "'";
				logWriter.println(message);
				log.error(t.getMessage(), t);
				throw new SQLNestedException(message, t);
			}
		}

		// Create a JDBC driver instance
		Driver driver = null;
		try
		{
			driver = DriverManager.getDriver(url);
		}
		catch (Throwable t)
		{
			String message = "Cannot create JDBC driver of class '" + (driverClassName != null ? driverClassName : "")
					+ "' for connect URL '" + url + "'";
			logWriter.println(message);
			log.error(t.getMessage(), t);
			throw new SQLNestedException(message, t);
		}

		// Can't test without a validationQuery
		if (validationQuery == null)
		{
			setTestOnBorrow(false);
			setTestOnReturn(false);
			setTestWhileIdle(false);
		}

		// Create an object pool to contain our active connections
		// Sakai:
		// if ((abandonedConfig != null) && (abandonedConfig.getRemoveAbandoned() == true))
		// {
		// connectionPool = new AbandonedObjectPool(null, abandonedConfig);
		// }
		// else
		{
			connectionPool = new GenericObjectPool();
		}
		connectionPool.setMaxActive(maxActive);
		connectionPool.setMaxIdle(maxIdle);
		connectionPool.setMinIdle(minIdle);
		connectionPool.setMaxWait(maxWait);
		connectionPool.setTestOnBorrow(testOnBorrow);
		connectionPool.setTestOnReturn(testOnReturn);
		connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		connectionPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		connectionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		connectionPool.setTestWhileIdle(testWhileIdle);

		// Set up statement pool, if desired
		GenericKeyedObjectPoolFactory statementPoolFactory = null;
		if (isPoolPreparedStatements())
		{
			statementPoolFactory = new GenericKeyedObjectPoolFactory(null, -1, // unlimited maxActive (per key)
					GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL, 0, // maxWait
					1, // maxIdle (per key)
					maxOpenPreparedStatements);
		}

		// Set up the driver connection factory we will use
		if (username != null)
		{
			connectionProperties.put("user", username);
		}
		else
		{
			// Sakai: log("DBCP DataSource configured without a 'username'");
		}

		if (password != null)
		{
			connectionProperties.put("password", password);
		}
		else
		{
			// Sakai: log("DBCP DataSource configured without a 'password'");
		}

		DriverConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, url, connectionProperties);

		// Set up the poolable connection factory we will use
		PoolableConnectionFactory connectionFactory = null;
		try
		{
			connectionFactory = new SakaiPoolableConnectionFactory(driverConnectionFactory, connectionPool, statementPoolFactory,
					validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog,
					/* abandonedConfig Sakai: */null, m_rollbackOnReturn);
			if (connectionFactory == null)
			{
				throw new SQLException("Cannot create PoolableConnectionFactory");
			}
			// Sakai: validateConnectionFactory(connectionFactory);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new SQLNestedException("Cannot create PoolableConnectionFactory (" + e.getMessage() + ")", e);
		}

		// Create and return the pooling data source to manage the connections
		dataSource = new PoolingDataSource(connectionPool);
		((PoolingDataSource) dataSource).setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
		dataSource.setLogWriter(logWriter);

		try
		{
			for (int i = 0; i < initialSize; i++)
			{
				connectionPool.addObject();
			}
		}
		catch (Exception e)
		{
			throw new SQLNestedException("Error preloading the connection pool", e);
		}

		return dataSource;
	}


   /**
      * Sets the connection properties passed to driver.connect(...).
      *
      * Format of the string must be [propertyName=property;]*
      *
      * NOTE - The "user" and "password" properties will be added
      * explicitly, so they do not need to be included here.
      *
      * @param connectionProperties the connection properties used to
      * create new connections
      */
     public void setConnectionProperties(String connectionProperties) {
         if (connectionProperties == null) throw new NullPointerException("connectionProperties is null");

         String[] entries = connectionProperties.split(";");
         Properties properties = new Properties();
         for (int i = 0; i < entries.length; i++) {
             String entry = entries[i];
             if (entry.length() > 0) {
                 int index = entry.indexOf('=');
                 if (index > 0) {
                     String name = entry.substring(0, index);
                     String value = entry.substring(index + 1);
                     properties.setProperty(name, value);
                 } else {
                    // no value is empty string which is how
                     properties.setProperty(entry, "");
                 }
             }
         }
         this.connectionProperties = properties;
     }

}
