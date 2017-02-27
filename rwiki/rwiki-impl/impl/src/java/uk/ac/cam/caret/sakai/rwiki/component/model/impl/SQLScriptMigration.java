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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.sakaiproject.db.cover.SqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.DataMigrationAgent;

// FIXME: Component

public class SQLScriptMigration extends HibernateDaoSupport implements DataMigrationAgent
{
	private static Logger log = LoggerFactory.getLogger(SQLScriptMigration.class);

	private String from;

	private String to;

	private String scriptPattern;

	public String migrate(String current, String target, final boolean newdb) throws Exception
	{
		if ((current != null && from == null)
				|| (current != null && !current.equals(from)))
		{
			log.info("Skipping Migration for " + from + " to " + to);
			return current;
		}
		String targetScript = null;
		String targetDialect = SqlService.getVendor();
		if (targetDialect == null || targetDialect.length() == 0)
			targetDialect = "hsqldb";
		targetScript = MessageFormat.format(scriptPattern,
				new Object[] { targetDialect });
		// to matches
		// perform the migration
		log.info("Migrating database schema from " + from + " to " + to
				+ " using " + targetScript);
		InputStream inStream = getClass().getResourceAsStream(targetScript);
		if (inStream == null)
		{
			log.warn("Migration Script " + targetScript + " was not found ");
			return current;
		}
		InputStreamReader stream = null;
		BufferedReader br = null;

		try {
			stream = new InputStreamReader(inStream); // validated as closing
			br = new BufferedReader(stream); // validated as closing
			String line = br.readLine();
			StringBuffer currentLine = new StringBuffer();
			List<String> lines = new ArrayList<String>();
			while (line != null)
			{
				if (line.trim().endsWith(";"))
				{
					currentLine.append(line);
					String sqlcmd = currentLine.toString().trim();
					sqlcmd = sqlcmd.substring(0, sqlcmd.length() - 1);
					if (sqlcmd != null && sqlcmd.length() > 0)
					{
						lines.add(sqlcmd);
					}
					currentLine = new StringBuffer();
				}
				else
				{
					currentLine.append(line);
				}
				line = br.readLine();
			}
			final String[] sql = (String[]) lines.toArray(new String[lines.size()]);

			getHibernateTemplate().execute(session -> {
				FlushMode flushMode = session.getFlushMode();
				// flush after every query
				session.setFlushMode(FlushMode.ALWAYS);
                executeSchemaScript(session, sql, newdb);
                // set back to the saved FlushMode
                session.setFlushMode(flushMode);
                return null;
            });
			return to;
		}
		finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(br);
		}

	}

	/**
	 * borrowed from LocalSessionFactoryBean in spring
	 *
	 * @param session
	 * @param sql
	 * @param newdb
	 * @throws SQLException
	 */
	private void executeSchemaScript(Session session, String[] sql, boolean newdb) {
		if (sql != null && sql.length > 0) {
			for (int i = 0; i < sql.length; i++) {
				if (sql[i].startsWith("message")) {
					log.info("Data Migration {}", sql[i]);
				} else {
					log.debug("Executing data migration statement: {}", sql[i]);
					try {
						long start = System.currentTimeMillis();
						int l = session.createSQLQuery(sql[i]).executeUpdate();
						log.debug("   Done {} rows in {} ms", l, (System.currentTimeMillis() - start));
					} catch (HibernateException ex) {
						if (newdb) {
							log.debug("Unsuccessful data migration statement: {}", sql[i]);
							log.debug("Cause: " + ex.getMessage());
						} else {
							log.warn("Unsuccessful data migration statement: {}", sql[i]);
							log.debug("Cause: " + ex.getMessage());
						}
					}
				}
			}
		}
	}

	/**
	 * @return Returns the from.
	 */
	public String getFrom()
	{
		return from;
	}

	/**
	 * @param from
	 *        The from to set.
	 */
	public void setFrom(String from)
	{
		this.from = from;
	}

	/**
	 * @return Returns the to.
	 */
	public String getTo()
	{
		return to;
	}

	/**
	 * @param to
	 *        The to to set.
	 */
	public void setTo(String to)
	{
		this.to = to;
	}

	/**
	 * @return Returns the scriptPattern.
	 */
	public String getScriptPattern()
	{
		return scriptPattern;
	}

	/**
	 * @param scriptPattern
	 *        The scriptPattern to set.
	 */
	public void setScriptPattern(String scriptPattern)
	{
		this.scriptPattern = scriptPattern;
	}

}
