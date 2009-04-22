/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.jackrabbit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.persistence.bundle.MSSqlPersistenceManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jcr.api.internal.StartupAction;
import org.sakaiproject.jcr.jackrabbit.persistance.BundleDbSharedPersistenceManager;
import org.sakaiproject.jcr.jackrabbit.persistance.DerbySharedPersistenceManager;
import org.sakaiproject.jcr.jackrabbit.persistance.MSSqlSharedPersistenceManager;
import org.sakaiproject.jcr.jackrabbit.persistance.MySqlSharedPersistenceManager;
import org.sakaiproject.jcr.jackrabbit.persistance.Oracle9SharedPersistenceManager;
import org.sakaiproject.jcr.jackrabbit.persistance.OracleSharedPersistenceManager;
import org.sakaiproject.jcr.jackrabbit.sakai.SakaiJCRCredentials;

public class RepositoryBuilder
{

	private static final Log log = LogFactory.getLog(RepositoryBuilder.class);

	private static final String DB_URL = "\\$\\{db.url\\}";

	private static final String DB_USER = "\\$\\{db.user\\}";

	private static final String DB_PASS = "\\$\\{db.pass\\}";

	private static final String DB_DRIVER = "\\$\\{db.driver\\}";

	private static final String CONTENT_ID_DB = "\\$\\{content.filesystem\\}";

	private static final String USE_SHARED_FS_BLOB_STORE = "\\$\\{content.shared\\}";
	
	private static final String SHARED_CONTENT_BLOB_LOCATION = "\\$\\{content.shared.location\\}";

	private static final String DB_DIALECT = "\\$\\{db.dialect\\}";

	private static final String CLUSTER_NODE_ID = "\\$\\{sakai.cluster\\}";

	private static final String JOURNAL_LOCATION = "\\$\\{journal.location\\}";
	
	private static final String PERSISTANCE_MANAGER = "\\$\\{persistance.manager.class\\}";


	/*
	 * These constants are the default Sakai Properties we will use if the
	 * values are not custom injected.
	 */
	public static final String DEFAULT_DBDIALECT_PROP = "vendor@org.sakaiproject.db.api.SqlService";

	public static final String DEFAULT_DBUSER_PROP = "username@javax.sql.BaseDataSource";

	public static final String DEFAULT_DBPASS_PROP = "password@javax.sql.BaseDataSource";

	public static final String DEFAULT_DBDRIVER_PROP = "driverClassName@javax.sql.BaseDataSource";

	public static final String DEFAULT_DBURL_PROP = "url@javax.sql.BaseDataSource";

	public static final String DEFAULT_DSPERSISTMNGR_PROP = "dataSourcePersistanceManager@org.sakaiproject.jcr.api.JCRService.repositoryBuilder";

	private RepositoryImpl repository;

	private String repositoryConfig;

	private String dbURL;

	private String dbUser;

	private String dbPass;

	private String dbDriver;

	private String repositoryHome;

	private String contentOnFilesystem;
	
	private String useSharedFSBlobStore;

	private String sharedFSBlobLocation;

	private String dbDialect;

	private Map<String, String> namespaces;

	private String nodeTypeConfiguration;

	private boolean dataSourcePersistanceManager = true;

	private List<StartupAction> startupActions;

	private String clusterNodeId;

	private String journalLocation;

	private ServerConfigurationService serverConfigurationService;

	private SqlService sqlService;

	private boolean enabled = false;

	private String persistanceManagerClass;

	private static Map<String, String> vendors = new HashMap<String, String>();
	

	static
	{
		// TODO, could map to special Persistance managers to make use of the
		// Oracle Optimised version
		vendors.put("mysql", "mysql");
		vendors.put("oracle", "oracle");
		vendors.put("oracle9", "oracle9");
		vendors.put("mssql", "mssql");
		vendors.put("hsqldb", "default");
		vendors.put("derby", "derby");
	}
	
	private static Map<String, String> persistanceManagers = new HashMap<String, String>();
	static
	{
		// TODO, could map to special Persistance managers to make use of the
		// Oracle Optimised version
		persistanceManagers.put("mysql", MySqlSharedPersistenceManager.class.getName());
		persistanceManagers.put("oracle", OracleSharedPersistenceManager.class.getName());
		persistanceManagers.put("oracle9", Oracle9SharedPersistenceManager.class.getName());
		persistanceManagers.put("mssql", MSSqlSharedPersistenceManager.class.getName());
		persistanceManagers.put("derby", DerbySharedPersistenceManager.class.getName());
		persistanceManagers.put("default", BundleDbSharedPersistenceManager.class.getName());
	}

	/**
	 * @return the dataSourcePersistanceManager
	 */
	public boolean isDataSourcePersistanceManager()
	{
		return dataSourcePersistanceManager;
	}

	/**
	 * @param dataSourcePersistanceManager
	 *        the dataSourcePersistanceManager to set
	 */
	public void setDataSourcePersistanceManager(boolean dataSourcePersistanceManager)
	{
		this.dataSourcePersistanceManager = dataSourcePersistanceManager;
	}

	public Repository getInstance()
	{
		return repository;
	}


	public void init()
	{
		enabled = serverConfigurationService.getBoolean("jcr.experimental",false);
		if (!enabled ) {
			log.info("Repository Builder is not enabled");
			return;
		}
		/*
		 * Unless the explicit values have been injected into this instance, try
		 * to get the database properties from the Sakai Defaults.
		 */
		if (dbDialect == null)
		{
			dbDialect = serverConfigurationService.getString(DEFAULT_DBDIALECT_PROP);
		}

		if (dbUser == null)
		{
			dbUser = serverConfigurationService.getString(DEFAULT_DBUSER_PROP);
		}

		if (dbPass == null)
		{
			dbPass = serverConfigurationService.getString(DEFAULT_DBPASS_PROP);
		}

		if (dbDriver == null)
		{
			dbDriver = serverConfigurationService.getString(DEFAULT_DBDRIVER_PROP);
		}

		if (dbURL == null)
		{
			String theDbURL = serverConfigurationService.getString(DEFAULT_DBURL_PROP);
			// The seperators need to be escaped since this is going into an XML
			// Attribute.
			dbURL = theDbURL.replaceAll("&", "&amp;");
		}

		if (contentOnFilesystem == null)
		{
			contentOnFilesystem = "false";
		}

		if ( useSharedFSBlobStore == null )
		{
			 useSharedFSBlobStore = "false";
		}
		if ( sharedFSBlobLocation == null ) 
		{
			 sharedFSBlobLocation="jcrblobs";
		}

		/*
		 * Currently Setting the journal location, journalLocation, in
		 * compenents.xml using the sakai.home macro
		 */

		dataSourcePersistanceManager = serverConfigurationService.getBoolean(
				DEFAULT_DSPERSISTMNGR_PROP, false);

		boolean error = false;
		try
		{
			clusterNodeId = serverConfigurationService.getServerId();
			if (dbDialect == null)
			{
				dbDialect = vendors.get(sqlService.getVendor());
			}
			if (dbDialect == null)
			{
					dbDialect = "default";
			}
			persistanceManagerClass = persistanceManagers.get(dbDialect);
			if (dataSourcePersistanceManager)
			{
				log.info(MessageFormat.format("\nJCR Repository Config is \n"
						+ "\trepositoryConfig = {0} \n" + "\tDB Using Datasource\n"
						+ "\tdbDialect = {5} \n" + "\trepository Home = {6}\n"
						+ "\tcontentOnFilesystem = {7}\n" + "\tjournalLocation= {8}\n"
						+ "\tpersistanceManageerClass= {9}\n",
						new Object[] { repositoryConfig, dbURL, dbUser, dbPass, dbDriver,
								dbDialect, repositoryHome, contentOnFilesystem,
								journalLocation,persistanceManagerClass }));
				if ((repositoryConfig == null) || (dbDialect == null)
						|| (repositoryHome == null) || (contentOnFilesystem == null)
						|| (journalLocation == null))
				{
					throw new IllegalStateException(
							"You must set all the services (none can be null): "
									+ "repositoryConfig, dbDialect, repositoryHome, contentOnFilesystem, journalLocation ");

				}
			}
			else
			{
				log.info(MessageFormat.format("\nJCR Repository Config is \n"
						+ "\trepositoryConfig = {0} \n" + "\tdbURL = {1}\n"
						+ "\tdbUser = {2} \n"
						+ "\tdbDriver = {4} \n" + "\tdbDialect = {5} \n"
						+ "\trepository Home = {6}\n" + "\tcontentOnFilesystem = {7}\n"
						+ "\tpersistanceManageerClass= {8}\n",
						new Object[] { repositoryConfig, dbURL, dbUser, dbPass, dbDriver,
								dbDialect, repositoryHome, contentOnFilesystem, persistanceManagerClass }));
				if ((repositoryConfig == null) || (dbURL == null) || (dbUser == null)

				|| (dbPass == null) || (dbDriver == null) || (dbDialect == null)
						|| (repositoryHome == null) || (contentOnFilesystem == null))
				{
					throw new IllegalStateException(
							"You must set all the services (none can be null): "
									+ "repositoryConfig, dbURL, dbUser, dbPass, dbDriver, dbDialect, repositoryHome, contentOnFilesystem ");
				}
			}
			if (namespaces == null)
			{
				throw new IllegalStateException("You must set the namespace list");
			}
			StringBuffer content = new StringBuffer();
			try
			{
			    InputStream is = this.getClass().getResourceAsStream(repositoryConfig);
			    if (is != null) {
	                Reader r = new InputStreamReader(is);
	                try {
                        char[] c = new char[4096];
                        for (int i = 0; (i = r.read(c)) != -1;)
                        {
                            content.append(c, 0, i);
                        }
                    } finally {
                        try {
                            r.close();
                        } catch (Exception e) {
                            log.warn("failed to close reader");
                        }
                        try {
                            is.close();
                        } catch (Exception e) {
                            log.warn("failed to close inputstream");
                        }
                    }
			    }
			}
			catch (Exception ex)
			{
				throw new Exception("Failed to load repository config "
						+ repositoryConfig, ex);
			}

			String contentStr = content.toString().replaceAll(DB_URL, dbURL);
			contentStr = contentStr.replaceAll(DB_USER, dbUser);
			contentStr = contentStr.replaceAll(DB_PASS, dbPass);
			contentStr = contentStr.replaceAll(DB_DRIVER, dbDriver);
			contentStr = contentStr.replaceAll(DB_DIALECT, dbDialect);
			contentStr = contentStr.replaceAll(CONTENT_ID_DB, contentOnFilesystem);
			contentStr = contentStr.replaceAll(USE_SHARED_FS_BLOB_STORE, useSharedFSBlobStore);
			contentStr = contentStr.replaceAll(SHARED_CONTENT_BLOB_LOCATION, sharedFSBlobLocation);
			contentStr = contentStr.replaceAll(CLUSTER_NODE_ID, clusterNodeId);
			contentStr = contentStr.replaceAll(JOURNAL_LOCATION, journalLocation);
			contentStr = contentStr.replaceAll(PERSISTANCE_MANAGER, persistanceManagerClass);

			if (log.isDebugEnabled()) log.debug("Repositroy Config is \n" + contentStr);

			ByteArrayInputStream bais = new ByteArrayInputStream(contentStr.getBytes());
			try
			{

				RepositoryConfig rc = RepositoryConfig.create(bais, repositoryHome);
				repository = RepositoryImpl.create(rc);
				setup();

			}
			finally
			{
				bais.close();
			}

		}
		catch (Throwable ex)
		{
			log.error("init() failure: " + ex);
			error = true;
		}
		finally
		{
			if (error)
			{
				throw new RuntimeException(
						"Fatal error initialising JCRService... (see previous logged ERROR for details)");

			}
		}
		log.info("Repository Builder passed init ");
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run()
			{
				shutdown();
			}
		});

	}

	public void shutdown()
	{
		if (!enabled ) return;
		if (repository != null)
		{
			log.info("Start repository shutdown  ");
            try {
				repository.shutdown();
			} catch ( Exception ex ) {
				log.warn("Repository Shutdown failed, this may be normal "+ex.getMessage());
			}
			log.info("Shutdown of repository complete  ");
			repository = null;
		}

	}

	public void destroy()
	{
		shutdown();
	}

	private void setup() throws RepositoryException, IOException
	{
		if (!enabled ) return;
		SakaiJCRCredentials ssp = new SakaiJCRCredentials();
		Session s = repository.login(ssp);
		try
		{
			Workspace w = s.getWorkspace();
			NamespaceRegistry reg = w.getNamespaceRegistry();
			for (Iterator i = namespaces.keySet().iterator(); i.hasNext();)
			{
				String prefix = (String) i.next();
				String uri = (String) namespaces.get(prefix);
				try
				{
					reg.getPrefix(uri);
				}
				catch (NamespaceException nex)
				{
					try
					{
						log.info("Registering Namespage [" + prefix + "] [" + uri + "]");
						reg.registerNamespace(prefix, uri);
					}
					catch (Exception ex)
					{
						throw new RuntimeException(
								"Failed to register namespace prefix (" + prefix
										+ ") with uri (" + uri + ") in workspace: "
										+ w.getName(), ex);
					}
				}
			}
			try
			{
				NodeTypeManagerImpl ntm = (NodeTypeManagerImpl) w.getNodeTypeManager();
				ntm.registerNodeTypes(this.getClass().getResourceAsStream(
						nodeTypeConfiguration), "text/xml");
			}
			catch (Exception ex)
			{
				log
						.info("Exception Loading Types, this is expected for all loads after the first one: "
								+ ex.getMessage()
								+ "(this message is here because Jackrabbit does not give us a good way to detect that the node types are already added)");
			}
			if (startupActions != null)
			{
				for (Iterator<StartupAction> i = startupActions.iterator(); i.hasNext();)
				{
					i.next().startup(s);
				}
			}

			s.save();
		}
		finally
		{
			s.logout();
		}

	}

	public String getDbDriver()
	{
		return dbDriver;
	}

	public void setDbDriver(String dbDriver)
	{
		this.dbDriver = dbDriver;
	}

	public String getDbPass()
	{
		return dbPass;
	}

	public void setDbPass(String dbPass)
	{
		this.dbPass = dbPass;
	}

	public String getDbURL()
	{
		return dbURL;
	}

	public void setDbURL(String dbURL)
	{
		this.dbURL = dbURL;
	}

	public String getDbUser()
	{
		return dbUser;
	}

	public void setDbUser(String dbUser)
	{
		this.dbUser = dbUser;
	}

	public String getRepositoryHome()
	{
		return repositoryHome;
	}

	public void setRepositoryHome(String repositoryHome)
	{
		this.repositoryHome = repositoryHome;
	}

	public String getContentOnFilesystem()
	{
		return contentOnFilesystem;
	}

	public void setContentOnFilesystem(String contentInDb)
	{
		contentOnFilesystem = contentInDb;
	}

	public String getRepositoryConfig()
	{
		return repositoryConfig;
	}

	public void setRepositoryConfig(String repositoryConfig)
	{
		this.repositoryConfig = repositoryConfig;
	}

	public String getDbDialect()
	{
		return dbDialect;
	}

	public void setDbDialect(String dbDialect)
	{
		this.dbDialect = dbDialect;
	}

	public Map<String, String> getNamespaces()
	{
		return namespaces;
	}

	public void setNamespaces(Map<String, String> namespaces)
	{
		this.namespaces = namespaces;
	}

	public String getNodeTypeConfiguration()
	{
		return nodeTypeConfiguration;
	}

	public void setNodeTypeConfiguration(String nodeTypeConfiguration)
	{
		this.nodeTypeConfiguration = nodeTypeConfiguration;
	}

	/**
	 * @return the startupActions
	 */
	public List<StartupAction> getStartupActions()
	{
		return startupActions;
	}

	/**
	 * @param startupActions
	 *        the startupActions to set
	 */
	public void setStartupActions(List<StartupAction> startupActions)
	{
		this.startupActions = startupActions;
	}

	/**
	 * @return the journalLocation
	 */
	public String getJournalLocation()
	{
		return journalLocation;
	}

	/**
	 * @param journalLocation
	 *        the journalLocation to set
	 */
	public void setJournalLocation(String journalLocation)
	{
		this.journalLocation = journalLocation;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService
	 *        the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @return the sqlService
	 */
	public SqlService getSqlService()
	{
		return sqlService;
	}

	/**
	 * @param sqlService
	 *        the sqlService to set
	 */
	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * If true, shared bobs will be stored on the file system
	 * @return
	 */
	public String getUseSharedFSBlobStore() {
		return useSharedFSBlobStore;
	}

	/**
	 * 
	 * @param useSharedFSBlobStore
	 */
	public void setUseSharedFSBlobStore(String useSharedFSBlobStore) {
		this.useSharedFSBlobStore = useSharedFSBlobStore;
	}

	/**
	 * The shared location of blobs stored on the filesystem
	 * @return
	 */
	public String getSharedFSBlobLocation() {
		return sharedFSBlobLocation;
	}

	public void setSharedFSBlobLocation(String sharedFSBlobLocation) {
		this.sharedFSBlobLocation = sharedFSBlobLocation;
	}

}
