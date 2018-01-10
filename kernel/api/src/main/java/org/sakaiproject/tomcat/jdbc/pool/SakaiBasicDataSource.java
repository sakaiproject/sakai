/**********************************************************************************
 * $URL: $
 * $Id: SakaiBasicDataSource.java 105077 2012-02-24 22:54:29Z ottenhoff@longsight.com $
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

package org.sakaiproject.tomcat.jdbc.pool;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import lombok.extern.slf4j.Slf4j;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * <p>
 * SakaiBasicDataSource extends apache tomcat's DataSource ...
 * </p>
 */
@Slf4j
public class SakaiBasicDataSource extends DataSource
{
	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

	/** Configuration: to rollback each connection when returned to the pool. */
	protected boolean m_rollbackOnReturn = false;
	//Needed for DBCP compat
	private boolean poolPreparedStatements;
	private int maxOpenPreparedStatements;

	public MBeanServer getMBeanServer()
	{
		return mBeanServer;
	}

	public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
		log.info("MaxOpenPreparedStatments not used");
		this.maxOpenPreparedStatements = maxOpenPreparedStatements;
	}

	public void setPoolPreparedStatements(boolean poolPreparedStatements) {
		log.info("PoolPreparedStatements not used");
		this.poolPreparedStatements = poolPreparedStatements;
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
			setDefaultTransactionIsolation(DataSourceFactory.UNKNOWN_TRANSACTIONISOLATION);
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
			setDefaultTransactionIsolation(DataSourceFactory.UNKNOWN_TRANSACTIONISOLATION);
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

	/**
	 * @exception SQLException
	 *            if the object pool cannot be created.
	 */
	protected void init() throws MalformedObjectNameException, MBeanRegistrationException, NotCompliantMBeanException, SQLException, InstanceAlreadyExistsException
	{
		log.info("init()");
		// Load the JDBC driver class
		PoolProperties connectionPool = (PoolProperties) createPool().getPoolProperties();
		String driverClassName = getDriverClassName();
		//Validate the class
		if (driverClassName != null)
		{
			try
			{
				Class.forName(driverClassName);
			}
			catch (Throwable t)
			{
				String message = "Cannot load JDBC driver class '" + driverClassName + "'";
				log.error(message, t);
				throw new SQLException(message,t);
			}
		}

		// Can't test without a validationQuery
		if (getValidationQuery() == null)
		{
			connectionPool.setTestOnBorrow(false);
			connectionPool.setTestOnReturn(false);
			connectionPool.setTestWhileIdle(false);
		} 

		// Set up statement pool, if desired
		// What did this do??
		/*
		GenericKeyedObjectPoolFactory statementPoolFactory = null;
		if (isPoolPreparedStatements())
		{
			statementPoolFactory = new GenericKeyedObjectPoolFactory(null, -1, // unlimited maxActive (per key)
					GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL, 0, // maxWait
					1, // maxIdle (per key)
					maxOpenPreparedStatements);
		}
		*/
		// Set up the driver connection factory we will use
		if (getUsername() == null) {
			log.warn("Tomcat DataSource configured without a 'username'");
		}
		
		String password = createPool().getPoolProperties().getUsername();

		if (password == null) {
			log.warn("Tomcat DataSource configured without a 'password'");
		}
		
		setPoolProperties(connectionPool);

		// Register an MBean so that we can view statistics on the pool via JMX
		ObjectName on = new ObjectName("TomcatJDBC:type=statistics,application=TomcatJDBCSakaiPool");
		if (!mBeanServer.isRegistered(on)) {
			log.info("Registering Tomcat JDBC pool with JMX " + mBeanServer);
			mBeanServer.registerMBean(getPool().getJmxPool(), on);
		}
		
		
		//Does it need any of this??
		/*
		DriverConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, url, connectionProperties);
		

		// Set up the poolable connection factory we will use
		PoolableConnectionFactory connectionFactory = null;
		try
		{
			connectionFactory = new SakaiPoolableConnectionFactory(driverConnectionFactory, connectionPool, statementPoolFactory,
					validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog,
					,null, m_rollbackOnReturn);
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
			throw new SQLException("Cannot create PoolableConnectionFactory (" + e.getMessage() + ")", e);
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
			throw new SQLException("Error preloading the connection pool", e);
		}
		*/

	}
}
