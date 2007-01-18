/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.model.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiPropertyDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.DataMigrationAgent;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.DataMigrationController;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiProperty;

/**
 * Provides a specification for data migration between versions
 * 
 * @author ieb
 */

public class DataMigrationSpecification implements DataMigrationController
{
	private static Log log = LogFactory
			.getLog(DataMigrationSpecification.class);

	private RWikiProperty targetVersion;

	private List migrationAgents;

	private RWikiPropertyDao propertyDao;

	private boolean performDataMigrations;

	public boolean isPerformDataMigrations()
	{
		return performDataMigrations;
	}

	public void setPerformDataMigrations(boolean performDataMigrations)
	{
		this.performDataMigrations = performDataMigrations;
	}

	public void update() throws Exception
	{
		String targetVersionString = targetVersion.getValue();
		RWikiProperty currentVersion = propertyDao.getProperty(targetVersion
				.getName());

		String currentVersionString = null;

		if (currentVersion != null)
			currentVersionString = currentVersion.getValue();

		// Check whether we are at the current version
		if (currentVersionString != null
				&& currentVersionString.equals(targetVersionString))
		{
			log.info("No data migration performed, target version present "
					+ targetVersionString);
			return;
		}

		// We need to do migration
		if (performDataMigrations)
		{
			boolean newdb = (currentVersionString == null);
			for (Iterator i = migrationAgents.iterator(); i.hasNext();)
			{
				DataMigrationAgent a = (DataMigrationAgent) i.next();
				currentVersionString = a.migrate(currentVersionString,
						targetVersionString, newdb);
			}
			if (currentVersion == null)
			{
				currentVersion = propertyDao.createProperty();
				currentVersion.setName(targetVersion.getName());
			}

			currentVersion.setValue(currentVersionString);
			propertyDao.update(currentVersion);
			if (currentVersionString != null
					&& currentVersionString.equals(targetVersionString))
			{
				log.info("RWiki Data migrated to version "
						+ currentVersionString + " sucessfuly");
				// SUCCESS!!
				return;
			}
		}
		log
				.fatal("RWiki Data has NOT been migrated to the current version, "
						+ "you MUST investigate before using the RWiki Tool\n currentVersion: "
						+ currentVersionString
						+ " targetVersion: "
						+ targetVersionString);
		throw new RuntimeException(
				"RWiki Data has NOT been migrated to the current version, "
						+ "you MUST investigate before using the RWiki Tool\n currentVersion: "
						+ currentVersionString + " targetVersion: "
						+ targetVersionString);
	}

	/**
	 * @return Returns the propertyDao.
	 */
	public RWikiPropertyDao getPropertyDao()
	{
		return propertyDao;
	}

	/**
	 * @param propertyDao
	 *        The propertyDao to set.
	 */
	public void setPropertyDao(RWikiPropertyDao propertyDao)
	{
		this.propertyDao = propertyDao;
	}

	/**
	 * @param targetVersion
	 *        The targetVersion to set.
	 */
	public void setTargetVersion(RWikiProperty targetVersion)
	{
		this.targetVersion = targetVersion;
	}

	/**
	 * @return Returns the migrationAgents.
	 */
	public List getMigrationAgents()
	{
		return migrationAgents;
	}

	/**
	 * @param migrationAgents
	 *        The migrationAgents to set.
	 */
	public void setMigrationAgents(List migrationAgents)
	{
		this.migrationAgents = migrationAgents;
	}

	/**
	 * @return Returns the targetVersion.
	 */
	public RWikiProperty getTargetVersion()
	{
		return targetVersion;
	}

}
