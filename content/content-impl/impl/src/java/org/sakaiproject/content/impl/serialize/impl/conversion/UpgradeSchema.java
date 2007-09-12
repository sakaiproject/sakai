/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.content.impl.serialize.impl.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public class UpgradeSchema
{

	private static final Log log = LogFactory.getLog(UpgradeSchema.class);

	private SharedPoolDataSource tds;

	public static void main(String[] argv)
	{
		UpgradeSchema cc = new UpgradeSchema();
		String configFile = null;
		if ( argv.length > 0 ) {
			configFile = argv[0];
		}
		try
		{
			cc.convert(configFile);
		}
		catch (Exception ex)
		{
			log.info("Failed to perform conversion ", ex);
		}
	}

	/**
	 * @throws Exception
	 * @throws Exception
	 */
	private void convert(String config) throws Exception
	{

		DriverAdapterCPDS cpds = new DriverAdapterCPDS();

		Properties p = new Properties();
		if (config != null)
		{
			log.info("Using Config " + config);
			File f = new File(config);
			FileInputStream fin = new FileInputStream(config);
			p.load(fin);
			fin.close();
			for (Iterator<Object> i = p.keySet().iterator(); i.hasNext();)
			{
				Object k = i.next();
				log.info("   Test Properties " + k + ":" + p.get(k));
			}
		}

		cpds.setDriver(p.getProperty("dbDriver", "com.mysql.jdbc.Driver"));
		cpds
				.setUrl(p
						.getProperty("dbURL",
								"jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&characterEncoding=UTF-8"));
		cpds.setUser(p.getProperty("dbUser", "sakai22"));
		cpds.setPassword(p.getProperty("dbPass", "sakai22"));

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(10);
		tds.setMaxWait(5);
		tds.setDefaultAutoCommit(false);

		doMigrate();

		tds.close();

	}

	public void doMigrate()
	{
		try
		{
			CheckConnection cc = new CheckConnection();
			cc.check(tds);
			
			
			SchemaConversionController scc = new SchemaConversionController();
			Type1BlobCollectionConversionHandler bch = new Type1BlobCollectionConversionHandler();
			log.info("Migrating Collection to Type 1 Binary Block Encoding");
			while (scc.migrate(tds, bch));
			log.info("Done Migrating Collection to Type 1 Binary Block Encoding");
			Type1BlobResourcesConversionHandler brh = new Type1BlobResourcesConversionHandler();
			log.info("Migrating Resources to Type 1 Binary Block Encoding");
		    while (scc.migrate(tds, brh));
			log.info("Done Migrating Resources to Type 1 Binary Block Encoding");
			FileSizeResourcesConversionHandler fsh = new FileSizeResourcesConversionHandler();
			log.info("Migrating Resources to Size/Quota Patch");
			while (scc.migrate(tds, fsh));
			log.info("Done Migrating Resources to Size/Quota Patch ");

		}
		catch (Exception ex)
		{
			log.info("Failed ", ex);
		}
	}
}
