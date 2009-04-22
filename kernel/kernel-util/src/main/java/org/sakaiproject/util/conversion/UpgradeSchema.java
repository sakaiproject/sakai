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

package org.sakaiproject.util.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
		if (argv.length > 0)
		{
			configFile = argv[0].trim();
		}
		log.info("configFile=" + configFile);
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
		Properties p = new Properties();
		if (config != null)
		{
			log.info("Using Config " + config);
			FileInputStream fin = new FileInputStream(config);
			p.load(fin);
			fin.close();
			StringBuilder sb = new StringBuilder();
			Object[] keys = p.keySet().toArray();
			Arrays.sort(keys);
			for (Object k : keys )
			{
				sb.append("\n " + k + ":" + p.get(k));
			}
			log.info("Loaded Properties from " + config + " as " + sb.toString());
		}
		else
		{
			log.info("Using Default Config: upgradeschema.config");
			InputStream is = this.getClass().getResourceAsStream("upgradeschema.config");
			if (is != null) {
    			try {
                    p.load(is);
                    StringBuilder sb = new StringBuilder();
                    Object[] keys = p.keySet().toArray();
                    Arrays.sort(keys);
                    for (Object k : keys )
                    {
                    	sb.append("\n " + k + ":" + p.get(k));
                    }
                    log.info("Loaded Default Properties as " + sb.toString());
                } finally {
                    is.close();
                }
			}
		}

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(getDataSource(p));
		tds.setMaxActive(10);
		tds.setMaxWait(5);
		tds.setDefaultAutoCommit(false);

//		CheckConnection cc = new CheckConnection();
//		cc.check(tds);

		List<SchemaConversionDriver> sequence = new ArrayList<SchemaConversionDriver>();
		int k = 0;
		while(true) {
			if ( p.get("convert."+k) != null ) {
				SchemaConversionDriver s = new SchemaConversionDriver();
				s.load(p, "convert."+k);
				sequence.add(s);
				k++;
			} else {
				break;
			}


		}

		doMigrate(sequence);

		tds.close();

	}

	public void doMigrate(List<SchemaConversionDriver> sequence)
	{
		try
		{
			SchemaConversionController scc = new SchemaConversionController();
			boolean earlyTerminationRequested = false;
			for (SchemaConversionDriver spec : sequence)
			{
				earlyTerminationRequested = earlyTerminationSignalled(spec.getEarlyTerminationSignal());
				if(earlyTerminationRequested)
				{
					log.info("Early termination requested");
					break;
				}
				Class handlerClass = Class.forName(spec.getHandlerClass());
				SchemaConversionHandler sch = (SchemaConversionHandler) handlerClass
						.newInstance();
				log.info("Migrating using Handler " + spec.getHandler());
				int k = 0;
				scc.init(tds, sch, spec);
				log.info("UpdateRecord query == " + spec.getUpdateRecord());

				while (scc.migrate(tds, sch, spec)) {
					log.info("Completed Batch "+(k++));
					earlyTerminationRequested = earlyTerminationSignalled(spec.getEarlyTerminationSignal());
					if(earlyTerminationRequested)
					{
						log.info("Early termination requested");
						break;
					}
				}
				if(earlyTerminationRequested)
				{
					break;
				}
				log.info("Done Migrating using Handler " + spec.getHandler());
			}

		}
		catch (Exception ex)
		{
			log.info("Failed ", ex);
		}
	}

	/**
	 * Make it easy to target the local database. Data source properties
	 * are searched in the following order, with the first match winning:
	 * <ul>
	 * <li> A Java system property named "sakai.properties" which will be used to load a
	 * properties file containing property names such as "url@javax.sql.BaseDataSource".
	 * <li> Input configProperties named "dbDriver", "dbURL", "dbUser", and "dbPass".
	 * </ul>
	 * (Side note: this configuration logic would be easy to externalize with Spring.)
	 * @param configProperties
	 */
	private DriverAdapterCPDS getDataSource(Properties configProperties) throws Exception
	{
		String dbDriver = null;
		String dbUrl = null;
		String dbUser = null;
		String dbPassword = null;
		String sakaiPropertiesPath = System.getProperty("sakai.properties");
		if (sakaiPropertiesPath != null)
		{
			File sakaiPropertiesFile = new File(sakaiPropertiesPath);
			if (sakaiPropertiesFile.exists())
			{
				try {
					FileInputStream sakaiPropertiesInput = new FileInputStream(sakaiPropertiesFile);
					Properties sakaiProperties = new Properties();
					sakaiProperties.load(sakaiPropertiesInput);
					sakaiPropertiesInput.close();
					dbDriver = sakaiProperties.getProperty("driverClassName@javax.sql.BaseDataSource");
					dbUrl = sakaiProperties.getProperty("url@javax.sql.BaseDataSource");
					dbUser = sakaiProperties.getProperty("username@javax.sql.BaseDataSource");
					dbPassword = sakaiProperties.getProperty("password@javax.sql.BaseDataSource");
				} catch (IOException e) {
					log.info("Error loading properties from " + sakaiPropertiesFile.getAbsolutePath());
				}
			}
		}
		if (dbDriver == null) dbDriver = configProperties.getProperty("dbDriver");
		if (dbUrl == null) dbUrl = configProperties.getProperty("dbURL");
		if (dbUser == null)dbUser  = configProperties.getProperty("dbUser");
		if (dbPassword == null) dbPassword = configProperties.getProperty("dbPass");

		DriverAdapterCPDS cpds = new DriverAdapterCPDS();
		cpds.setDriver(dbDriver);
		cpds.setUrl(dbUrl);
		cpds.setUser(dbUser);
		cpds.setPassword(dbPassword);
		return cpds;
	}

	private boolean earlyTerminationSignalled(String earlyEndSignal)
	{
		boolean endNow = false;
		if(earlyEndSignal != null)
		{
			File file = new File(earlyEndSignal);
			log.info("Checking for early termination: " + file.getAbsolutePath());
			endNow = file.exists();
		}
		return endNow;
	}
}
