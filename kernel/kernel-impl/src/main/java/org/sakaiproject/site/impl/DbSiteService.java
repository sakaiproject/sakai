/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.site.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlServiceDeadlockException;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * DbSiteService is an extension of the BaseSiteService with a database storage.
 * </p>
 */
public abstract class DbSiteService extends BaseSiteService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(DbSiteService.class);

	/** Table name for sites. */
	protected String m_siteTableName = "SAKAI_SITE";

	/** Table name for site properties. */
	protected String m_sitePropTableName = "SAKAI_SITE_PROPERTY";

	/** ID field for site. */
	protected String m_siteIdFieldName = "SITE_ID";

	/** Site sort field. */
	protected String m_siteSortField = "TITLE";

	/** All fields for site. */
	protected String[] m_siteFieldNames = {"SITE_ID", "TITLE", "TYPE", "SHORT_DESC", "DESCRIPTION", "ICON_URL", "INFO_URL", "SKIN", "PUBLISHED",
			"JOINABLE", "PUBVIEW", "JOIN_ROLE", "IS_SPECIAL", "IS_USER", "CREATEDBY", "MODIFIEDBY", "CREATEDON", "MODIFIEDON", "CUSTOM_PAGE_ORDERED"};

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract SqlService sqlService();

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_useExternalLocks = true;

	/**
	 * Configuration: set the external locks value.
	 * 
	 * @param value
	 *        The external locks value.
	 */
	public void setExternalLocks(String value)
	{
		m_useExternalLocks = Boolean.valueOf(value).booleanValue();
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = Boolean.valueOf(value).booleanValue();
	}

	/** contains a map of database handlers. */
	protected Map<String, SiteServiceSql> databaseBeans;

	/** The database handler we are using. */
	protected SiteServiceSql siteServiceSql;

	public void setDatabaseBeans(Map databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	public SiteServiceSql getSiteServiceSql()
	{
		return siteServiceSql;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setSiteServiceSql(String vendor)
	{
		this.siteServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site");

				// also load the 2.1 new site database tables
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site_group");

				// also load the 2.1.0.003 field insertion
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site_2_1_0_003");

				// also load the 2.4.0 field insertion
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site_2_4_0_001");

				// also load the 2.5.3 removal of synoptic discussion
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site_2_5_3");

			}

			super.init();
			setSiteServiceSql(sqlService().getVendor());

			M_log.info("init(): site table: " + m_siteTableName + " external locks: " + m_useExternalLocks);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/*************************************************************************************************************************************************
	 * BaseSiteService extensions
	 ************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage(this);
	}

	/*************************************************************************************************************************************************
	 * Storage implementation
	 ************************************************************************************************************************************************/

	protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader
	{
		/** A prior version's storage model. */
		protected Storage m_oldStorage = null;

		/** The service. */
		protected BaseSiteService m_service = null;

		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(BaseSiteService service)
		{
			super(m_siteTableName, m_siteIdFieldName, m_siteFieldNames, m_sitePropTableName, m_useExternalLocks, null, sqlService());
			m_reader = this;

			m_service = service;

			setSortField(m_siteSortField, null);

			// no locking
			setLocking(false);
		}

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public Site get(String id)
		{
			return (Site) super.getResource(id);
		}

		public List getAll()
		{
			return super.getAllResources();
		}

		public Site put(String id)
		{
			// check for already exists
			if (check(id)) return null;

			BaseSite rv = (BaseSite) super.putResource(id, fields(id, null, false));
			if (rv != null) rv.activate();
			return rv;
		}

		public void save(final Site edit)
		{
			// run our save code in a transaction that will restart on deadlock
			// if deadlock retry fails, or any other error occurs, a runtime error will be thrown
			m_sql.transact(new Runnable()
			{
				public void run()
				{
					saveTx(edit);
				}
			}, "site:" + edit.getId());
		}

		/**
		 * The transaction code to save a site.
		 * 
		 * @param edit
		 *        The site to save.
		 */
		protected void saveTx(Site edit)
		{
			// TODO: (SAK-8669) re-write to commit only the diff, not a complete delete / add -ggolden

			// write the pages, tools, properties,
			// and then commit the site and release the lock

			// delete the pages, tools, page properties, tool properties
			Object fields[] = new Object[1];
			fields[0] = caseId(edit.getId());

			String statement = siteServiceSql.getDeleteToolPropertiesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteToolsSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeletePagePropertiesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeletePagesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteGroupPropertiesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteGroupsSql();
			m_sql.dbWrite(statement, fields);

			// since we've already deleted the old values, don't delete them again.
			boolean deleteAgain = false;

			// add each page
			int pageOrder = 1;
			for (Iterator iPages = edit.getPages().iterator(); iPages.hasNext();)
			{
				SitePage page = (SitePage) iPages.next();

				// write the page
				statement = siteServiceSql.getInsertPageSql();

				fields = new Object[6];
				fields[0] = page.getId();
				fields[1] = caseId(edit.getId());
				fields[2] = page.getTitle();
				fields[3] = Integer.toString(page.getLayout());
				fields[4] = ((((BaseSitePage) page).m_popup) ? "1" : "0");
				fields[5] = Integer.valueOf(pageOrder++);
				m_sql.dbWrite(statement, fields);

				// write the page's properties
				writeProperties("SAKAI_SITE_PAGE_PROPERTY", "PAGE_ID", page.getId(), "SITE_ID", caseId(edit.getId()), page.getProperties(),
						deleteAgain);

				// write the tools
				int toolOrder = 1;
				for (Iterator iTools = page.getTools().iterator(); iTools.hasNext();)
				{
					ToolConfiguration tool = (ToolConfiguration) iTools.next();

					// write the tool
					statement = siteServiceSql.getInsertToolSql();

					fields = new Object[7];
					fields[0] = tool.getId();
					fields[1] = page.getId();
					fields[2] = caseId(edit.getId());
					fields[3] = tool.getToolId();
					fields[4] = Integer.valueOf(toolOrder++);
					fields[5] = tool.getTitle();
					fields[6] = tool.getLayoutHints();
					m_sql.dbWrite(statement, fields);

					// write the tool's properties
					writeProperties("SAKAI_SITE_TOOL_PROPERTY", "TOOL_ID", tool.getId(), "SITE_ID", caseId(edit.getId()), tool.getPlacementConfig(),
							deleteAgain);
				}
			}

			// add each group
			for (Iterator iGroups = edit.getGroups().iterator(); iGroups.hasNext();)
			{
				Group group = (Group) iGroups.next();

				// write the group
				statement = siteServiceSql.getInsertGroupSql();

				fields = new Object[4];
				fields[0] = group.getId();
				fields[1] = caseId(edit.getId());
				fields[2] = group.getTitle();
				fields[3] = group.getDescription();
				m_sql.dbWrite(statement, fields);

				// write the group's properties
				writeProperties("SAKAI_SITE_GROUP_PROPERTY", "GROUP_ID", group.getId(), "SITE_ID", caseId(edit.getId()), group.getProperties(),
						deleteAgain);
			}

			// write the site and properties, releasing the lock
			super.commitResource(edit, fields(edit.getId(), edit, true), edit.getProperties(), null);
		}

		/**
		 * @inheritDoc
		 */
		public void saveInfo(String siteId, String description, String infoUrl)
		{
			String statement = siteServiceSql.getUpdateSiteSql(m_siteTableName);

			Object fields[] = new Object[3];
			fields[0] = description;
			fields[1] = infoUrl;
			fields[2] = caseId(siteId);

			m_sql.dbWrite(statement, fields);
		}

		/**
		 * @inheritDoc
		 */
		public void saveToolConfig(final ToolConfiguration tool)
		{
			// in a transaction
			m_sql.transact(new Runnable()
			{
				public void run()
				{
					saveToolConfigTx(tool);
				}
			}, "siteToolConfig:" + tool.getId());
		}

		/**
		 * The transactino code for saving a tool config.
		 */
		protected void saveToolConfigTx(ToolConfiguration tool)
		{
			// delete this tool and tool properties
			Object fields[] = new Object[2];
			fields[0] = caseId(tool.getSiteId());
			fields[1] = caseId(tool.getId());

			String statement = siteServiceSql.getDeleteToolPropertySql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteToolSql();
			m_sql.dbWrite(statement, fields);

			// write the tool
			statement = siteServiceSql.getInsertToolSql();

			fields = new Object[7];
			fields[0] = tool.getId();
			fields[1] = tool.getPageId();
			fields[2] = caseId(tool.getSiteId());
			fields[3] = tool.getToolId();
			fields[4] = Integer.valueOf(tool.getPageOrder());
			fields[5] = tool.getTitle();
			fields[6] = tool.getLayoutHints();
			m_sql.dbWrite(statement, fields);

			// write the tool's properties
			writeProperties("SAKAI_SITE_TOOL_PROPERTY", "TOOL_ID", tool.getId(), "SITE_ID", caseId(tool.getSiteId()), tool.getPlacementConfig());
		}

		/**
		 * @inheritDoc
		 */
		public void remove(final Site edit)
		{
			// in a transaction
			m_sql.transact(new Runnable()
			{
				public void run()
				{
					removeTx(edit);
				}
			}, "siteRemove:" + edit.getId());
		}

		/**
		 * Transaction code to remove a site.
		 */
		protected void removeTx(Site edit)
		{
			// delete all the pages, tools, properties, permissions
			// and then the site and release the lock

			// delete the pages, tools, page properties, tool properties, permissions
			Object fields[] = new Object[1];
			fields[0] = caseId(edit.getId());

			String statement = siteServiceSql.getDeleteToolPropertiesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteToolsSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeletePagePropertiesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeletePagesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteUsersSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteGroupPropertiesSql();
			m_sql.dbWrite(statement, fields);

			statement = siteServiceSql.getDeleteGroupsSql();
			m_sql.dbWrite(statement, fields);

			// delete the site and properties
			super.removeResource(edit, null);
		}

		public int count()
		{
			return super.countAllResources();
		}

		/**
		 * {@inheritDoc}
		 */
		public List getSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria, SortType sort, PagingPosition page)
		{
			// Note: super users are not treated any differently - they get only those sites they have permission for,
			// not based on super user status

			// if we are joining, start our where with the join clauses
			StringBuilder where = new StringBuilder();
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE))
			{
				// join on site id and also select the proper user
				where.append(siteServiceSql.getSitesWhere1Sql());
			}

			// ignore user sites
			if (type.isIgnoreUser()) where.append(siteServiceSql.getSitesWhere2Sql());
			// reject special sites
			if (type.isIgnoreSpecial()) where.append(siteServiceSql.getSitesWhere3Sql());
			// reject unpublished sites
			if (type.isIgnoreUnpublished()) where.append(siteServiceSql.getSitesWhere4Sql());

			if (ofType != null)
			{
				if (ofType.getClass().equals(String.class))
				{
					// type criteria is a simple String value
					where.append(siteServiceSql.getSitesWhere5Sql());
				}
				else if (ofType instanceof String[] || ofType instanceof List || ofType instanceof Set)
				{
					// more complex type criteria
					int size = 0;
					if (ofType instanceof String[])
					{
						size = ((String[]) ofType).length;
					}
					else if (ofType instanceof List)
					{
						size = ((List) ofType).size();
					}
					else if (ofType instanceof Set)
					{
						size = ((Set) ofType).size();
					}
					if (size > 0)
					{
						where.append(siteServiceSql.getSitesWhere6Sql());
						for (int i = 1; i < size; i++)
						{
							where.append(",?");
						}
						where.append(") and ");
					}
				}
			}

			// reject non-joinable sites
			if (type == SelectionType.JOINABLE) where.append(siteServiceSql.getSitesWhere7Sql());
			// check for pub view status
			if (type == SelectionType.PUBVIEW) where.append(siteServiceSql.getSitesWhere8Sql());
			// check criteria
			if (criteria != null) where.append(siteServiceSql.getSitesWhere9Sql());
			// update permission
			if (type == SelectionType.UPDATE) where.append(siteServiceSql.getSitesWhere10Sql());
			// access permission
			if (type == SelectionType.ACCESS) where.append(siteServiceSql.getSitesWhere11Sql());
			// joinable requires NOT access permission
			if (type == SelectionType.JOINABLE) where.append(siteServiceSql.getSitesWhere12Sql());

			// do we need a join?
			String join = null;
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE))
			{
				// join with the SITE_USER table
				join = siteServiceSql.getSitesJoin1Sql();
			}
			if (sort == SortType.CREATED_BY_ASC || sort == SortType.CREATED_BY_DESC || sort == SortType.MODIFIED_BY_ASC
					|| sort == SortType.MODIFIED_BY_DESC)
			{
				// join with SITE_USER_ID_MAP table
				if (join != null)
				{
					join += siteServiceSql.getSitesJoin2Sql();
				}
				else
				{
					join = siteServiceSql.getSitesJoin3Sql();
				}
			}

			// add propertyCriteria if specified
			if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
			{
				for (int i = 0; i < propertyCriteria.size(); i++)
				{
					where.append(siteServiceSql.getSitesWhere13Sql());
				}
			}

			// where sorted by createdby, need to join with SAKAI_USER_ID_MAP in order to find out the user eid
			if (sort == SortType.CREATED_BY_ASC || sort == SortType.CREATED_BY_DESC)
			{
				// add more to where clause
				where.append(siteServiceSql.getSitesWhere14Sql());
			}
			else if (sort == SortType.MODIFIED_BY_ASC || sort == SortType.MODIFIED_BY_DESC)
			{
				// sort by modifiedby
				where.append(siteServiceSql.getSitesWhere15Sql());
			}

			// add order by if needed
			String order = null;
			if (sort == SortType.ID_ASC)
			{
				order = siteServiceSql.getSitesOrder1Sql();
			}
			else if (sort == SortType.ID_DESC)
			{
				order = siteServiceSql.getSitesOrder2Sql();
			}
			else if (sort == SortType.TITLE_ASC)
			{
				order = siteServiceSql.getSitesOrder3Sql();
			}
			else if (sort == SortType.TITLE_DESC)
			{
				order = siteServiceSql.getSitesOrder4Sql();
			}
			else if (sort == SortType.TYPE_ASC)
			{
				order = siteServiceSql.getSitesOrder5Sql();
			}
			else if (sort == SortType.TYPE_DESC)
			{
				order = siteServiceSql.getSitesOrder6Sql();
			}
			else if (sort == SortType.PUBLISHED_ASC)
			{
				order = siteServiceSql.getSitesOrder7Sql();
			}
			else if (sort == SortType.PUBLISHED_DESC)
			{
				order = siteServiceSql.getSitesOrder8Sql();
			}
			else if (sort == SortType.CREATED_BY_ASC)
			{
				order = siteServiceSql.getSitesOrder9Sql();
			}
			else if (sort == SortType.CREATED_BY_DESC)
			{
				order = siteServiceSql.getSitesOrder10Sql();
			}
			else if (sort == SortType.MODIFIED_BY_ASC)
			{
				order = siteServiceSql.getSitesOrder11Sql();
			}
			else if (sort == SortType.MODIFIED_BY_DESC)
			{
				order = siteServiceSql.getSitesOrder12Sql();
			}
			else if (sort == SortType.CREATED_ON_ASC)
			{
				order = siteServiceSql.getSitesOrder13Sql();
			}
			else if (sort == SortType.CREATED_ON_DESC)
			{
				order = siteServiceSql.getSitesOrder14Sql();
			}
			else if (sort == SortType.MODIFIED_ON_ASC)
			{
				order = siteServiceSql.getSitesOrder15Sql();
			}
			else if (sort == SortType.MODIFIED_ON_DESC)
			{
				order = siteServiceSql.getSitesOrder16Sql();
			}

			int fieldCount = 0;
			if (ofType != null)
			{
				if (ofType instanceof String)
				{
					// type criteria is a simple String value
					fieldCount++;
				}
				// more complex types
				else if (ofType instanceof String[])
				{
					fieldCount += ((String[]) ofType).length;
				}
				else if (ofType instanceof List)
				{
					fieldCount += ((List) ofType).size();
				}
				else if (ofType instanceof Set)
				{
					fieldCount += ((Set) ofType).size();
				}
			}
			if (criteria != null) fieldCount += 1;
			if ((type == SelectionType.JOINABLE) || (type == SelectionType.ACCESS) || (type == SelectionType.UPDATE)) fieldCount++;
			if (propertyCriteria != null) fieldCount += (2 * propertyCriteria.size());
			Object fields[] = null;
			if (fieldCount > 0)
			{
				fields = new Object[fieldCount];
				int pos = 0;
				if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE))
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
				if (ofType != null)
				{
					if (ofType instanceof String)
					{
						// type criteria is a simple String value
						fields[pos++] = ofType;
					}
					else if (ofType instanceof String[])
					{
						for (int i = 0; i < ((String[]) ofType).length; i++)
						{
							// of type String[]
							fields[pos++] = (String) ((String[]) ofType)[i];
						}
					}
					else if (ofType instanceof List)
					{
						for (Iterator l = ((List) ofType).iterator(); l.hasNext();)
						{
							// of type List
							fields[pos++] = l.next();
						}
					}
					else if (ofType instanceof Set)
					{
						for (Iterator l = ((Set) ofType).iterator(); l.hasNext();)
						{
							// of type Set
							fields[pos++] = l.next();
						}
					}
				}
				if (criteria != null)
				{
					criteria = "%" + criteria + "%";
					fields[pos++] = criteria;
				}
				if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
				{
					for (Iterator i = propertyCriteria.entrySet().iterator(); i.hasNext();)
					{
						Map.Entry entry = (Map.Entry) i.next();
						String name = (String) entry.getKey();
						String value = (String) entry.getValue();
						fields[pos++] = name;
						fields[pos++] = "%" + value + "%";
					}
				}
				if (type == SelectionType.JOINABLE)
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
			}

			List rv = null;

			// where has a trailing 'and ' to remove
			if ((where.length() > 5) && (where.substring(where.length() - 5).equals(" and ")))
			{
				where.setLength(where.length() - 5);
			}

			// paging
			if (page != null)
			{
				// adjust to the size of the set found
				// page.validate(rv.size());
				rv = getSelectedResources(where.toString(), order, fields, page.getFirst(), page.getLast(), join);
			}
			else
			{
				rv = getSelectedResources(where.toString(), order, fields, join);
			}

			if ( m_siteCache == null ) return rv;

			// Loop through the sites to see if we have cached copies 
			// of the sites
			List newrv = new ArrayList();

			int count = 0;
			for ( Site s : (List<Site>) rv) { 
				Site news = getCachedSite(s.getId()); 
				if ( news != null )
				{
					newrv.add(news);
					count++;
				}
				else
				{
					newrv.add(s);
				}
               		} 
			return newrv;
		}

		/**
		 * {@inheritDoc}
		 */
		public List getSiteTypes()
		{
			String statement = siteServiceSql.getTypesSql();

			List rv = sqlService().dbRead(statement);

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getSiteSkin(final String siteId)
		{
			if (siteId == null) return m_service.adjustSkin(null, true);

			// let the db do the work
			String statement = siteServiceSql.getSkinSql();
			Object fields[] = new Object[1];
			fields[0] = caseId(siteId);

			List rv = sqlService().dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String skin = result.getString(1);
						int published = result.getInt(2);

						// adjust the skin value
						skin = m_service.adjustSkin(skin, (published == 1));

						return skin;
					}
					catch (SQLException e)
					{
						M_log.warn("getSiteSkin: " + siteId + " : " + e);
						return null;
					}
				}
			});

			if ((rv != null) && (rv.size() > 0))
			{
				return (String) rv.get(0);
			}

			return m_service.adjustSkin(null, true);
		}

		/**
		 * {@inheritDoc}
		 */
		public int countSites(SelectionType type, Object ofType, String criteria, Map propertyCriteria)
		{
			// if we are joining, start our where with the join clauses
			StringBuilder where = new StringBuilder();
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE))
			{
				// join on site id and also select the proper user
				where.append(siteServiceSql.getSitesWhere1Sql());
			}

			// ignore user sites
			if (type.isIgnoreUser()) where.append(siteServiceSql.getSitesWhere2Sql());
			// reject special sites
			if (type.isIgnoreSpecial()) where.append(siteServiceSql.getSitesWhere3Sql());
			// reject unpublished sites
			if (type.isIgnoreUnpublished()) where.append(siteServiceSql.getSitesWhere4Sql());

			// reject unwanted site types
			if (ofType != null)
			{
				if (ofType instanceof String)
				{
					// type criteria is a simple String value
					where.append(siteServiceSql.getSitesWhere5Sql());
				}
				else if (ofType instanceof String[] || ofType instanceof List || ofType instanceof Set)
				{
					// more complex type criteria
					int size = 0;
					if (ofType instanceof String[])
					{
						size = ((String[]) ofType).length;
					}
					else if (ofType instanceof List)
					{
						size = ((List) ofType).size();
					}
					else if (ofType instanceof Set)
					{
						size = ((Set) ofType).size();
					}
					if (size > 0)
					{
						where.append(siteServiceSql.getSitesWhere6Sql());
						for (int i = 1; i < size; i++)
						{
							where.append(",?");
						}
						where.append(") and ");
					}
				}
			}

			// reject non-joinable sites
			if (type == SelectionType.JOINABLE) where.append(siteServiceSql.getSitesWhere7Sql());
			// check for pub view status
			if (type == SelectionType.PUBVIEW) where.append(siteServiceSql.getSitesWhere8Sql());
			// check criteria
			if (criteria != null) where.append(siteServiceSql.getSitesWhere9Sql());
			// update permission
			if (type == SelectionType.UPDATE) where.append(siteServiceSql.getSitesWhere10Sql());
			// access permission
			if (type == SelectionType.ACCESS) where.append(siteServiceSql.getSitesWhere11Sql());
			// joinable requires NOT access permission
			if (type == SelectionType.JOINABLE) where.append(siteServiceSql.getSitesWhere12Sql());

			// do we need a join?
			String join = null;
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE))
			{
				// join with the SITE_USER table
				join = siteServiceSql.getSitesJoin1Sql();
			}

			// add propertyCriteria if specified
			if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
			{
				for (int i = 0; i < propertyCriteria.size(); i++)
				{
					where.append(siteServiceSql.getSitesWhere13Sql());
				}
			}

			int fieldCount = 0;
			if (ofType != null)
			{
				if (ofType instanceof String)
				{
					// type criteria is a simple String value
					fieldCount++;
				}
				// more complex types
				else if (ofType instanceof String[])
				{
					fieldCount += ((String[]) ofType).length;
				}
				else if (ofType instanceof List)
				{
					fieldCount += ((List) ofType).size();
				}
				else if (ofType instanceof Set)
				{
					fieldCount += ((Set) ofType).size();
				}
			}
			if (criteria != null) fieldCount += 1;
			if ((type == SelectionType.JOINABLE) || (type == SelectionType.ACCESS) || (type == SelectionType.UPDATE)) fieldCount++;
			if (propertyCriteria != null) fieldCount += (2 * propertyCriteria.size());
			Object fields[] = null;
			if (fieldCount > 0)
			{
				fields = new Object[fieldCount];
				int pos = 0;
				if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE))
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
				if (ofType != null)
				{
					if (ofType instanceof String)
					{
						// type criteria is a simple String value
						fields[pos++] = ofType;
					}
					else if (ofType instanceof String[])
					{
						for (int i = 0; i < ((String[]) ofType).length; i++)
						{
							// of type String[]
							fields[pos++] = (String) ((String[]) ofType)[i];
						}
					}
					else if (ofType instanceof List)
					{
						for (Iterator l = ((List) ofType).iterator(); l.hasNext();)
						{
							// of type List
							fields[pos++] = l.next();
						}
					}
					else if (ofType instanceof Set)
					{
						for (Iterator l = ((Set) ofType).iterator(); l.hasNext();)
						{
							// of type Set
							fields[pos++] = l.next();
						}
					}
				}
				if (criteria != null)
				{
					criteria = "%" + criteria + "%";
					fields[pos++] = criteria;
				}
				if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
				{
					for (Iterator i = propertyCriteria.entrySet().iterator(); i.hasNext();)
					{
						Map.Entry entry = (Map.Entry) i.next();
						String name = (String) entry.getKey();
						String value = (String) entry.getValue();
						fields[pos++] = name;
						fields[pos++] = "%" + value + "%";
					}
				}
				if (type == SelectionType.JOINABLE)
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
			}

			// where has a trailing 'and ' to remove
			if ((where.length() > 5) && (where.substring(where.length() - 5).equals(" and ")))
			{
				where.setLength(where.length() - 5);
			}

			int rv = countSelectedResources(where.toString(), fields, join);

			return rv;
		}

		/**
		 * Access the ToolConfiguration that has this id, if one is defined, else return null. The tool may be on any SitePage in the site.
		 * 
		 * @param id
		 *        The id of the tool.
		 * @return The ToolConfiguration that has this id, if one is defined, else return null.
		 */
		public ToolConfiguration findTool(final String id)
		{
			String sql = siteServiceSql.getToolFields1Sql();

			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String registration = result.getString(1);
						String title = result.getString(2);
						String layout = result.getString(3);
						String siteId = result.getString(4);
						String pageId = result.getString(5);
						String skin = result.getString(6);
						int published = result.getInt(7);
						int pageOrder = result.getInt(8);

						// adjust the skin value
						skin = m_service.adjustSkin(skin, (published == 1));

						// make the tool
						BaseToolConfiguration tool = new BaseToolConfiguration(DbSiteService.this,id, registration, title, layout, pageId, siteId, skin, pageOrder);

						return tool;
					}
					catch (SQLException e)
					{
						M_log.warn("findTool: " + id + " : " + e);
						return null;
					}
				}
			});

			if (found.size() > 1)
			{
				M_log.warn("findTool: multiple results for tool id: " + id);
			}

			ToolConfiguration rv = null;
			if (found.size() > 0)
			{
				rv = (ToolConfiguration) found.get(0);
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public SitePage findPage(final String id)
		{
			String sql = siteServiceSql.getPageFields1Sql();

			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String pageId = result.getString(1);
						String title = result.getString(2);
						String layout = result.getString(3);
						String siteId = result.getString(4);
						String skin = result.getString(5);
						int published = result.getInt(6);
						boolean popup = "1".equals(result.getString(7)) ? true : false;

						// adjust the skin value
						skin = m_service.adjustSkin(skin, (published == 1));

						// make the page
						BaseSitePage page = new BaseSitePage(DbSiteService.this,pageId, title, layout, popup, siteId, skin);

						return page;
					}
					catch (SQLException e)
					{
						M_log.warn("findPage: " + id + " : " + e);
						return null;
					}
				}
			});

			if (found.size() > 1)
			{
				M_log.warn("findPage: multiple results for page id: " + id);
			}

			SitePage rv = null;
			if (found.size() > 0)
			{
				rv = (SitePage) found.get(0);
			}

			return rv;
		}

		/**
		 * Access the Site id for the page with this id.
		 * 
		 * @param id
		 *        The id of the page.
		 * @return The Site id for the page with this id, if the page is found, else null.
		 */
		public String findPageSiteId(String id)
		{
			String sql = siteServiceSql.getSiteId1Sql();
			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, null);

			if (found.size() > 1)
			{
				M_log.warn("findPageSiteId: multiple results for page id: " + id);
			}

			String rv = null;
			if (found.size() > 0)
			{
				rv = (String) found.get(0);
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public String findGroupSiteId(String id)
		{
			String sql = siteServiceSql.getSiteId2Sql();
			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, null);

			if (found.size() > 1)
			{
				M_log.warn("findGroupSiteId: multiple results for page id: " + id);
			}

			String rv = null;
			if (found.size() > 0)
			{
				rv = (String) found.get(0);
			}

			return rv;
		}

		/**
		 * Access the Site id for the tool with this id.
		 * 
		 * @param id
		 *        The id of the tool.
		 * @return The Site id for the tool with this id, if the tool is found, else null.
		 */
		public String findToolSiteId(String id)
		{
			String sql = siteServiceSql.getSiteId3Sql();
			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, null);

			if (found.size() > 1)
			{
				M_log.warn("findToolSiteId: multiple results for page id: " + id);
			}

			String rv = null;
			if (found.size() > 0)
			{
				rv = (String) found.get(0);
			}

			return rv;
		}

		/**
		 * Establish the internal security for this site. Previous security settings are replaced for this site. Assigning a user with update implies
		 * the two reads; assigning a user with unp read implies the other read.
		 * 
		 * @param siteId
		 *        The id of the site.
		 * @param updateUsers
		 *        The set of String User Ids who have update access.
		 * @param visitUnpUsers
		 *        The set of String User Ids who have visit unpublished access.
		 * @param visitUsers
		 *        The set of String User Ids who have visit access.
		 */
		public void setSiteSecurity(final String siteId, Set updateUsers, Set visitUnpUsers, Set visitUsers)
		{
			// normalize the input parameters - remove any user in more than one set

			// adjust visitUsers to remove any that are in visitUnpUsers or updateUsers
			Set targetVisit = new HashSet();
			targetVisit.addAll(visitUsers);
			targetVisit.removeAll(visitUnpUsers);
			targetVisit.removeAll(updateUsers);

			// adjust visitUnpUsers to remove any that are in updateUsers
			Set targetUnp = new HashSet();
			targetUnp.addAll(visitUnpUsers);
			targetUnp.removeAll(updateUsers);

			Set targetUpdate = updateUsers;

			// read existing
			String statement = siteServiceSql.getUserIdSql();
			Object[] fields = new Object[1];
			fields[0] = caseId(siteId);

			// collect the current data in three sets, update, unp, visit
			final Set existingUpdate = new HashSet();
			final Set existingUnp = new HashSet();
			final Set existingVisit = new HashSet();

			m_sql.dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String userId = result.getString(1);
						int permission = result.getInt(2);
						if (permission == -1)
						{
							existingUpdate.add(userId);
						}
						else if (permission == 0)
						{
							existingUnp.add(userId);
						}
						else if (permission == 1)
						{
							existingVisit.add(userId);
						}
						else
						{
							M_log.warn("setSiteSecurity: invalid permission " + permission + " site: " + siteId + " user: " + userId);
						}
					}
					catch (Throwable ignore)
					{
						return null;
					}
					return null;
				}
			});

			// compute the delete and insert sets for each of the three permissions

			// delete if the user is in targetUpdate, but it is already in one of the other categories
			Set updDeletes = new HashSet();
			updDeletes.addAll(existingUnp);
			updDeletes.addAll(existingVisit);
			updDeletes.retainAll(targetUpdate);

			// also delete if the user is in the existing and not in the target
			Set obsolete = new HashSet();
			obsolete.addAll(existingUpdate);
			obsolete.removeAll(targetUpdate);
			updDeletes.addAll(obsolete);

			// insert if the user is in targetUpdate, but is not already in update
			Set updInserts = new HashSet();
			updInserts.addAll(targetUpdate);
			updInserts.removeAll(existingUpdate);

			// delete if the user is in targetUnp, but it is already in one of the other categories
			Set unpDeletes = new HashSet();
			unpDeletes.addAll(existingUpdate);
			unpDeletes.addAll(existingVisit);
			unpDeletes.retainAll(targetUnp);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingUnp);
			obsolete.removeAll(targetUnp);
			unpDeletes.addAll(obsolete);

			// insert if the user is in targetUnp, but is not already in unp
			Set unpInserts = new HashSet();
			unpInserts.addAll(targetUnp);
			unpInserts.removeAll(existingUnp);

			// delete if the user is in targetVisit, but it is already in one of the other categories
			Set visitDeletes = new HashSet();
			visitDeletes.addAll(existingUpdate);
			visitDeletes.addAll(existingUnp);
			visitDeletes.retainAll(targetVisit);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingVisit);
			obsolete.removeAll(targetVisit);
			visitDeletes.addAll(obsolete);

			// insert if the user is in targetVisit, but is not already in visit
			Set visitInserts = new HashSet();
			visitInserts.addAll(targetVisit);
			visitInserts.removeAll(existingVisit);

			// if there's anything to do
			if (updDeletes.size() > 0 || updInserts.size() > 0 || unpDeletes.size() > 0 || unpInserts.size() > 0 || visitDeletes.size() > 0
					|| visitInserts.size() > 0)
			{
				// delete old, write new, each in it's own transaction to avoid possible deadlock
				// involving modifications to multiple rows in a transaction
				fields = new Object[2];
				fields[0] = caseId(siteId);

				// delete
				statement = siteServiceSql.getDeleteUserSql();
				for (Iterator i = updDeletes.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = unpDeletes.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = visitDeletes.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}

				// insert
				statement = siteServiceSql.getInsertUserSql();
				fields = new Object[3];
				fields[0] = caseId(siteId);

				fields[2] = Integer.valueOf(-1);
				for (Iterator i = updInserts.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = Integer.valueOf(0);
				for (Iterator i = unpInserts.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = Integer.valueOf(1);
				for (Iterator i = visitInserts.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void setUserSecurity(final String userId, Set updateSites, Set visitUnpSites, Set visitSites)
		{
			// normalize the input parameters - remove any user in more than one set

			// adjust visitSites to remove any that are in visitUnpSites or updateSites
			Set targetVisit = new HashSet();
			targetVisit.addAll(visitSites);
			targetVisit.removeAll(visitUnpSites);
			targetVisit.removeAll(updateSites);

			// adjust visitUnpSites to remove any that are in updateSites
			Set targetUnp = new HashSet();
			targetUnp.addAll(visitUnpSites);
			targetUnp.removeAll(updateSites);

			Set targetUpdate = updateSites;

			// read existing
			String statement = siteServiceSql.getSiteId4Sql();
			Object[] fields = new Object[1];
			fields[0] = userId;

			// collect the current data in three sets, update, unp, visit
			final Set existingUpdate = new HashSet();
			final Set existingUnp = new HashSet();
			final Set existingVisit = new HashSet();

			m_sql.dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String siteId = result.getString(1);
						int permission = result.getInt(2);
						if (permission == -1)
						{
							existingUpdate.add(siteId);
						}
						else if (permission == 0)
						{
							existingUnp.add(siteId);
						}
						else if (permission == 1)
						{
							existingVisit.add(siteId);
						}
						else
						{
							M_log.warn("setUserSecurity: invalid permission " + permission + " site: " + siteId + " user: " + userId);
						}
					}
					catch (Throwable ignore)
					{
						return null;
					}
					return null;
				}
			});

			// compute the delete and insert sets for each of the three permissions

			// delete if the site is in targetUpdate, but it is already in one of the other categories
			Set updDeletes = new HashSet();
			updDeletes.addAll(existingUnp);
			updDeletes.addAll(existingVisit);
			updDeletes.retainAll(targetUpdate);

			// also delete if the user is in the existing and not in the target
			Set obsolete = new HashSet();
			obsolete.addAll(existingUpdate);
			obsolete.removeAll(targetUpdate);
			updDeletes.addAll(obsolete);

			// insert if the site is in targetUpdate, but is not already in update
			Set updInserts = new HashSet();
			updInserts.addAll(targetUpdate);
			updInserts.removeAll(existingUpdate);

			// delete if the site is in targetUnp, but it is already in one of the other categories
			Set unpDeletes = new HashSet();
			unpDeletes.addAll(existingUpdate);
			unpDeletes.addAll(existingVisit);
			unpDeletes.retainAll(targetUnp);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingUnp);
			obsolete.removeAll(targetUnp);
			unpDeletes.addAll(obsolete);

			// insert if the site is in targetUnp, but is not already in unp
			Set unpInserts = new HashSet();
			unpInserts.addAll(targetUnp);
			unpInserts.removeAll(existingUnp);

			// delete if the site is in targetVisit, but it is already in one of the other categories
			Set visitDeletes = new HashSet();
			visitDeletes.addAll(existingUpdate);
			visitDeletes.addAll(existingUnp);
			visitDeletes.retainAll(targetVisit);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingVisit);
			obsolete.removeAll(targetVisit);
			visitDeletes.addAll(obsolete);

			// insert if the site is in targetVisit, but is not already in visit
			Set visitInserts = new HashSet();
			visitInserts.addAll(targetVisit);
			visitInserts.removeAll(existingVisit);

			// if there's anything to do
			if (updDeletes.size() > 0 || updInserts.size() > 0 || unpDeletes.size() > 0 || unpInserts.size() > 0 || visitDeletes.size() > 0
					|| visitInserts.size() > 0)
			{
				// delete old, write new, each in it's own transaction to avoid possible deadlock
				// involving modifications to multiple rows in a transaction
				fields = new Object[2];
				fields[1] = userId;

				// delete
				statement = siteServiceSql.getDeleteUserSql();
				for (Iterator i = updDeletes.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = unpDeletes.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = visitDeletes.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}

				// insert
				statement = siteServiceSql.getInsertUserSql();
				fields = new Object[3];
				fields[1] = userId;

				fields[2] = Integer.valueOf(-1);
				for (Iterator i = updInserts.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = Integer.valueOf(0);
				for (Iterator i = unpInserts.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = Integer.valueOf(1);
				for (Iterator i = visitInserts.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}
			}
		}

		/**
		 * Read site properties from storage into the site's properties.
		 * 
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readSiteProperties(Site site, ResourcePropertiesEdit props)
		{
			super.readProperties(site, props);
		}

		/**
		 * Read site properties and all page and tool properties for the site from storage.
		 * 
		 * @param site
		 *        The site for which properties are desired.
		 */
		public void readAllSiteProperties(Site site)
		{
			// read and un-lazy the site properties
			readSiteProperties(site, ((BaseSite) site).m_properties);
			((BaseResourcePropertiesEdit) ((BaseSite) site).m_properties).setLazy(false);

			// read and unlazy the page properties for the entire site
			readSitePageProperties((BaseSite) site);
			for (Iterator i = site.getPages().iterator(); i.hasNext();)
			{
				BaseSitePage page = (BaseSitePage) i.next();
				((BaseResourcePropertiesEdit) page.m_properties).setLazy(false);
			}

			// read and unlazy the tool properties for the entire site
			readSiteToolProperties((BaseSite) site);
			for (Iterator i = site.getPages().iterator(); i.hasNext();)
			{
				BaseSitePage page = (BaseSitePage) i.next();
				for (Iterator t = page.getTools().iterator(); t.hasNext();)
				{
					BaseToolConfiguration tool = (BaseToolConfiguration) t.next();
					tool.m_configLazy = false;
				}
			}

			// read and unlazy the group properties for the entire site
			readSiteGroupProperties((BaseSite) site);
			for (Iterator i = site.getGroups().iterator(); i.hasNext();)
			{
				BaseGroup group = (BaseGroup) i.next();
				((BaseResourcePropertiesEdit) group.m_properties).setLazy(false);
			}
		}

		/**
		 * Read properties for all pages in the site
		 * 
		 * @param site
		 *        The site to read properties for.
		 */
		protected void readSitePageProperties(final BaseSite site)
		{
			// get the properties from the db for all pages in the site
			String sql = siteServiceSql.getPagePropertiesSql();

			Object fields[] = new Object[1];
			fields[0] = site.getId();
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// read the fields
						String pageId = result.getString(1);
						String name = result.getString(2);
						String value = result.getString(3);

						// get the page
						BaseSitePage page = (BaseSitePage) site.getPage(pageId);
						if (page != null)
						{
							page.m_properties.addProperty(name, value);
						}

						// nothing to return
						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSitePageProperties: " + e);
						return null;
					}
				}
			});
		}

		/**
		 * Read properties for all tools in the site
		 * 
		 * @param site
		 *        The site to read properties for.
		 */
		protected void readSiteToolProperties(final BaseSite site)
		{
			// get the properties from the db for all pages in the site
			String sql = siteServiceSql.getToolPropertiesSql();

			Object fields[] = new Object[1];
			fields[0] = site.getId();
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// read the fields
						String toolId = result.getString(1);
						String name = result.getString(2);
						String value = result.getString(3);

						// get the page
						BaseToolConfiguration tool = (BaseToolConfiguration) site.getTool(toolId);
						if (tool != null)
						{
							tool.getMyConfig().setProperty(name, value);
						}

						// nothing to return
						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSitePageProperties: " + e);
						return null;
					}
				}
			});
		}

		/**
		 * Read properties for all groups in the site
		 * 
		 * @param site
		 *        The site to read group properties for.
		 */
		protected void readSiteGroupProperties(final BaseSite site)
		{
			// get the properties from the db for all pages in the site
			String sql = siteServiceSql.getGroupPropertiesSql();

			Object fields[] = new Object[1];
			fields[0] = site.getId();
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// read the fields
						String groupId = result.getString(1);
						String name = result.getString(2);
						String value = result.getString(3);

						// get the group
						BaseGroup group = (BaseGroup) site.getGroup(groupId);
						if (group != null)
						{
							group.m_properties.addProperty(name, value);
						}

						// nothing to return
						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSiteGroupProperties: " + e);
						return null;
					}
				}
			});
		}

		/**
		 * Read page properties from storage into the page's properties.
		 * 
		 * @param page
		 *        The page for which properties are desired.
		 */
		public void readPageProperties(SitePage page, ResourcePropertiesEdit props)
		{
			super.readProperties(null, "SAKAI_SITE_PAGE_PROPERTY", "PAGE_ID", page.getId(), props);
		}

		/**
		 * Read tool properties from storage into the tool's properties.
		 * 
		 * @param tool
		 *        The tool for which properties are desired.
		 */
		public void readToolProperties(ToolConfiguration tool, Properties props)
		{
			super.readProperties(null, "SAKAI_SITE_TOOL_PROPERTY", "TOOL_ID", tool.getId(), props);
		}

		/**
		 * Read group properties from storage into the group's properties.
		 * 
		 * @param group
		 *        The group for which properties are desired.
		 */
		public void readGroupProperties(Group group, Properties props)
		{
			super.readProperties(null, "SAKAI_SITE_GROUP_PROPERTY", "GROUP_ID", group.getId(), props);
		}

		/**
		 * Read site pages from storage into the site's pages.
		 * 
		 * @param site
		 *        The site for which pages are desired.
		 */
		public void readSitePages(final Site site, final ResourceVector pages)
		{
			// read all resources from the db with a where
			String sql = siteServiceSql.getPageFields2Sql();
			Object fields[] = new Object[1];
			fields[0] = site.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String id = result.getString(1);
						String title = result.getString(2);
						String layout = result.getString(3);
						boolean popup = "1".equals(result.getString(4)) ? true : false;

						// make the page
						BaseSitePage page = new BaseSitePage(DbSiteService.this, site, id, title, layout, popup);

						// add it to the pages
						pages.add(page);

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});
		}

		/**
		 * Read site page tools from storage into the page's tools.
		 * 
		 * @param page
		 *        The page for which tools are desired.
		 */
		public void readPageTools(final SitePage page, final ResourceVector tools)
		{
			// read all resources from the db with a where
			String sql = siteServiceSql.getToolFields2Sql();
			Object fields[] = new Object[1];
			fields[0] = page.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String id = result.getString(1);
						String registration = result.getString(2);
						String title = result.getString(3);
						String layout = result.getString(4);
						int pageOrder = result.getInt(5);

						// make the tool
						BaseToolConfiguration tool = new BaseToolConfiguration(DbSiteService.this,page, id, registration, title, layout, pageOrder);

						// add it to the tools
						tools.add(tool);

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});
		}

		/**
		 * Read tools for all pages from storage into the site's page's tools.
		 * 
		 * @param site
		 *        The site for which tools are desired.
		 */
		public void readSiteTools(final Site site)
		{
			// read all tools for the site
			String sql = siteServiceSql.getToolFields3Sql();
			Object fields[] = new Object[1];
			fields[0] = site.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String id = result.getString(1);
						String pageId = result.getString(2);
						String registration = result.getString(3);
						String title = result.getString(4);
						String layout = result.getString(5);
						int pageOrder = result.getInt(6);

						// get the page
						BaseSitePage page = (BaseSitePage) site.getPage(pageId);
						if ((page != null) && (page.m_toolsLazy))
						{
							// make the tool
							BaseToolConfiguration tool = new BaseToolConfiguration(DbSiteService.this,page, id, registration, title, layout, pageOrder);

							// add it to the tools
							page.m_tools.add(tool);
						}

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			// unlazy the page tools
			for (Iterator i = site.getPages().iterator(); i.hasNext();)
			{
				BaseSitePage page = (BaseSitePage) i.next();
				page.m_toolsLazy = false;
			}
		}

		/**
		 * @inheritDoc
		 */
		public void readSiteGroups(final Site site, final Collection groups)
		{
			String sql = siteServiceSql.getGroupFieldsSql();
			// TODO: order by? title? -ggolden

			Object fields[] = new Object[1];
			fields[0] = site.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String groupId = result.getString(1);
						String title = result.getString(2);
						String description = result.getString(3);

						// make the group
						BaseGroup group = new BaseGroup(DbSiteService.this,groupId, title, description, site);

						// add it to the groups
						groups.add(group);

						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSiteGroups: " + site.getId() + " : " + e);
						return null;
					}
				}
			});
		}

		/**
		 * Get the fields for the database from the edit for this id, and the id again at the end if needed
		 * 
		 * @param id
		 *        The resource id
		 * @param edit
		 *        The edit (may be null in a new)
		 * @param idAgain
		 *        If true, include the id field again at the end, else don't.
		 * @return The fields for the database.
		 */
		protected Object[] fields(String id, Site edit, boolean idAgain)
		{
			Object[] rv = new Object[idAgain ? 20 : 19];
			rv[0] = caseId(id);
			if (idAgain)
			{
				rv[19] = rv[0];
			}

			if (edit == null)
			{
				String current = sessionManager().getCurrentSessionUserId();

				// if no current user, since we are working up a new user record, use the user id as creator...
				if (current == null) current = "";

				Time now = timeService().newTime();

				rv[1] = "";
				rv[2] = "";
				rv[3] = "";
				rv[4] = "";
				rv[5] = "";
				rv[6] = "";
				rv[7] = "";
				rv[8] = Integer.valueOf(0);
				rv[9] = "0";
				rv[10] = "0";
				rv[11] = "";
				rv[12] = isSpecialSite(id) ? "1" : "0";
				rv[13] = isUserSite(id) ? "1" : "0";
				rv[14] = current;
				rv[15] = current;
				rv[16] = now;
				rv[17] = now;
				rv[18] = "0";
			}

			else
			{
				rv[1] = StringUtil.trimToZero(((BaseSite) edit).m_title);
				rv[2] = StringUtil.trimToZero(((BaseSite) edit).m_type);
				rv[3] = StringUtil.trimToZero(((BaseSite) edit).m_shortDescription);
				rv[4] = StringUtil.trimToZero(((BaseSite) edit).m_description);
				rv[5] = StringUtil.trimToZero(((BaseSite) edit).m_icon);
				rv[6] = StringUtil.trimToZero(((BaseSite) edit).m_info);
				rv[7] = StringUtil.trimToZero(((BaseSite) edit).m_skin);
				rv[8] = Integer.valueOf((((BaseSite) edit).m_published) ? 1 : 0);
				rv[9] = ((((BaseSite) edit).m_joinable) ? "1" : "0");
				rv[10] = ((((BaseSite) edit).m_pubView) ? "1" : "0");
				rv[11] = StringUtil.trimToZero(((BaseSite) edit).m_joinerRole);
				rv[12] = isSpecialSite(id) ? "1" : "0";
				rv[13] = isUserSite(id) ? "1" : "0";
				rv[14] = StringUtil.trimToZero(((BaseSite) edit).m_createdUserId);
				rv[15] = StringUtil.trimToZero(((BaseSite) edit).m_lastModifiedUserId);
				rv[16] = edit.getCreatedTime();
				rv[17] = edit.getModifiedTime();
				rv[18] = edit.isCustomPageOrdered() ? "1" : "0";
			}

			return rv;
		}

		/**
		 * Read from the result one set of fields to create a Resource.
		 * 
		 * @param result
		 *        The Sql query result.
		 * @return The Resource object.
		 */
		public Object readSqlResultRecord(ResultSet result)
		{
			try
			{
				String id = result.getString(1);
				String title = result.getString(2);
				String type = result.getString(3);
				String shortDesc = result.getString(4);
				String description = result.getString(5);
				String icon = result.getString(6);
				String info = result.getString(7);
				String skin = result.getString(8);
				boolean published = result.getInt(9) == 1;
				boolean joinable = "1".equals(result.getString(10)) ? true : false;
				boolean pubView = "1".equals(result.getString(11)) ? true : false;
				String joinRole = result.getString(12);
				boolean isSpecial = "1".equals(result.getString(13)) ? true : false;
				boolean isUser = "1".equals(result.getString(14)) ? true : false;
				String createdBy = result.getString(15);
				String modifiedBy = result.getString(16);
				java.sql.Timestamp ts = result.getTimestamp(17, sqlService().getCal());
				Time createdOn = null;
				if (ts != null)
				{
					createdOn = timeService().newTime(ts.getTime());
				}
				ts = result.getTimestamp(18, sqlService().getCal());
				Time modifiedOn = null;
				if (ts != null)
				{
					modifiedOn = timeService().newTime(ts.getTime());
				}
				boolean customPageOrdered = "1".equals(result.getString(19)) ? true : false;

				// create the Resource from these fields
				return new BaseSite(DbSiteService.this,id, title, type, shortDesc, description, icon, info, skin, published, joinable, pubView, joinRole, isSpecial,
						isUser, createdBy, createdOn, modifiedBy, modifiedOn, customPageOrdered);
			}
			catch (SQLException e)
			{
				M_log.warn("readSqlResultRecord: " + e);
				return null;
			}
		}
	}
}
