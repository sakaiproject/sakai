package org.sakaiproject.content.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.migration.api.CHStoJCRMigrator;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;
import org.sakaiproject.content.migration.api.MigrationStatusReporter;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class CHStoJCRMigratorImpl implements CHStoJCRMigrator
{

	private static final Log log = LogFactory.getLog(CHStoJCRMigratorImpl.class);

	// Injected Services
	private SqlService sqlService;

	private boolean autoDDL = false;

	private JCRService jcrService;

	private ContentToJCRCopier contentToJCRCopier;

	private MigrationStatusReporter migrationStatusReporter;

	// End Injected Services

	protected final String CURRENT_USER_MARKER = "originalTestUser";

	protected final String ADMIN_USER = "admin";

	private static final String ORIGINAL_MIGRATION_EVENT = "ORIGINAL_MIGRATION";

	/*
	 * Property to allow you to stop and start the system from migrating things
	 * in the background.
	 */
	private boolean isCurrentlyMigrating = false;

	private int batchSize = 20;

	private int delayBetweenBatchesMilliSeconds = 1000;

	/* Our things to do the work. */
	private Timer timer = new Timer(false);

	private javax.jcr.Session jcrSession;
	

	private String HACKUSER = "admin";

	private UserDirectoryService userDirectoryService;

	private SessionManager sessionManager;

	private AuthzGroupService authzGroupService;


	public void init()
	{
		if ( !jcrService.isEnabled() ) return;
		log.info("init()");
		try
		{
			jcrSession = jcrService.login();
		}
		catch (LoginException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RepositoryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// creating the needed tables
		if (autoDDL)
		{
			sqlService.ddl(getClass().getClassLoader(), "setup-migration-dbtables");
		}
	}

	public void destroy()
	{
		if ( !jcrService.isEnabled() ) return;
		jcrSession.logout();
		log.info("destroy()");
	}

	private void markContentItemFinished(String contentId)
	{
		if ( !jcrService.isEnabled() ) return;
		sqlService.dbWrite(MigrationSqlQueries.finish_content_item, contentId);
	}

	public boolean isCurrentlyMigrating()
	{
		return isCurrentlyMigrating;
	}

	private void addOriginalItemsToQueue()
	{
		if ( !jcrService.isEnabled() ) return;
		try
		{
			Connection conn = sqlService.borrowConnection();
			sqlService.dbInsert(conn,
					MigrationSqlQueries.add_original_collections_to_migrate, null, "id");
			sqlService.dbInsert(conn,
					MigrationSqlQueries.add_original_resources_to_migrate, null, "id");
			sqlService.returnConnection(conn);
		}
		catch (SQLException e)
		{
			log.error(
					"Problems adding the original content migration items to the queue.",
					e);
		}
	}

	public void startMigrating()
	{
		if ( !jcrService.isEnabled() ) return;
		this.isCurrentlyMigrating = true;
		if (!migrationStatusReporter.hasMigrationStarted())
		{
			addOriginalItemsToQueue();
		}
		scheduleBatch();
	}

	public void stopMigrating()
	{
		if ( !jcrService.isEnabled() ) return;
		this.isCurrentlyMigrating = false;
	}

	private void migrateOneItem(javax.jcr.Session session, ThingToMigrate item)
	{
		// ContentResources in the Original CHS always end with '/'
		if (item.contentId.endsWith("/"))
		{
			// This is a ContentCollection
			if (item.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				contentToJCRCopier.copyCollectionFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				contentToJCRCopier.copyCollectionFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				contentToJCRCopier.deleteItem(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				contentToJCRCopier.copyCollectionFromCHStoJCR(session, item.contentId);
			}
		}
		else
		{
			// This is a ContentResource
			if (item.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				contentToJCRCopier.copyResourceFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				contentToJCRCopier.copyResourceFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				contentToJCRCopier.deleteItem(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				contentToJCRCopier.copyResourceFromCHStoJCR(session, item.contentId);
			}
		}
		markContentItemFinished(item.contentId);
	}

	@SuppressWarnings("unchecked")
	private void migrateSomeItems(int numberToMigrate)
	{
		List<ThingToMigrate> thingsToMigrate = sqlService.dbRead(
				MigrationSqlQueries.select_unfinished_items,
				new Object[] { numberToMigrate }, new MigrationTableSqlReader());

		// try {
		// javax.jcr.Session session = jcrService.login();
		for (ThingToMigrate thing : thingsToMigrate)
		{
			migrateOneItem(jcrSession, thing);
		}
		// session.logout();
		// jcrService.logout();
		// }
		// catch (Exception e) {
		// log.error("Error Migrating some CHS to JCR items: ", e);
		// }
	}

	private void scheduleBatch()
	{
		TimerTask batchTask = new TimerTask()
		{

			public void run()
			{

				/*
				 * There seems to be a problem running this permission wise,
				 * perhaps because it's in it's own thread and the user
				 * information isn't getting carried over? ERROR: Problems
				 * migrating collection:
				 * /group/usedtools/jcr-2.0/docs/javax/jcr/nodetype/ (2007-11-29
				 * 11:42:06,723
				 * Timer-17_org.sakaiproject.content.impl.jcr.migration.ContentToJCRCopierImpl)
				 * org.sakaiproject.exception.PermissionException user=null
				 * lock=content.revise.any
				 * resource=/content/group/usedtools/jcr-2.0/docs/javax/jcr/nodetype/
				 * at
				 * org.sakaiproject.content.impl.BaseContentService.editCollection(BaseContentService.java:4101)
				 * at
				 * org.sakaiproject.content.impl.jcr.migration.ContentToJCRCopierImpl.copyCollectionFromCHStoJCR(ContentToJCRCopierImpl.java:53)
				 * at
				 * org.sakaiproject.content.impl.jcr.migration.CHStoJCRMigratorImpl.migrateOneItem(CHStoJCRMigratorImpl.java:94)
				 */
				becomeHackUser();
				// If there is stuff left, migrate it.
				if (!migrationStatusReporter.hasMigrationFinished())
				{
					migrateSomeItems(batchSize);
				}
				else
				{
					isCurrentlyMigrating = false;
					return;
				}

				if (isCurrentlyMigrating)
				{
					scheduleBatch();
				}
			}
		};
		if (timer == null) timer = new Timer(false);
		try
		{
			timer.schedule(batchTask, delayBetweenBatchesMilliSeconds);
		}
		catch (IllegalStateException ise)
		{
			// If there was a problem before, the timer will have been
			// cancelled.
			log
					.info("There was an error previously with the migration, recreating the Migration Timer");
			timer.cancel();
			timer = new Timer(false);
			timer.schedule(batchTask, delayBetweenBatchesMilliSeconds);
		}
	}

	public int getBatchSize()
	{
		return batchSize;
	}

	public void setBatchSize(int batchSize)
	{
		this.batchSize = batchSize;
	}

	public int getDelayBetweenBatchesMilliSeconds()
	{
		return delayBetweenBatchesMilliSeconds;
	}

	public void setDelayBetweenBatchesMilliSeconds(int delayBetweenBatchesMilliSeconds)
	{
		this.delayBetweenBatchesMilliSeconds = delayBetweenBatchesMilliSeconds;
	}

	/*
	 * Various Injections Below
	 */
	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	public void setJcrService(JCRService jcrService)
	{
		this.jcrService = jcrService;
	}

	public void setMigrationStatusReporter(MigrationStatusReporter migrationStatusReporter)
	{
		this.migrationStatusReporter = migrationStatusReporter;
	}

	public void setContentToJCRCopier(ContentToJCRCopier contentToJCRCopier)
	{
		this.contentToJCRCopier = contentToJCRCopier;
	}

	public void setAutoDDL(boolean autoDDL)
	{
		this.autoDDL = autoDDL;
	}

	private void becomeHackUser()
	{

		User u = null;
		try
		{
			u = userDirectoryService.getUserByEid(HACKUSER);
		}
		catch (UserNotDefinedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Session s = sessionManager.getCurrentSession();

		s.setUserEid(u.getEid());
		s.setUserId(u.getId());
		s.setActive();
		sessionManager.setCurrentSession(s);
		authzGroupService.refreshUser(u.getId());
	}

	/**
	 * @return the authzGroupService
	 */
	public AuthzGroupService getAuthzGroupService()
	{
		return authzGroupService;
	}

	/**
	 * @param authzGroupService the authzGroupService to set
	 */
	public void setAuthzGroupService(AuthzGroupService authzGroupService)
	{
		this.authzGroupService = authzGroupService;
	}

	/**
	 * @return the sessionManager
	 */
	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	/**
	 * @param sessionManager the sessionManager to set
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	/**
	 * @return the userDirectoryService
	 */
	public UserDirectoryService getUserDirectoryService()
	{
		return userDirectoryService;
	}

	/**
	 * @param userDirectoryService the userDirectoryService to set
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

}