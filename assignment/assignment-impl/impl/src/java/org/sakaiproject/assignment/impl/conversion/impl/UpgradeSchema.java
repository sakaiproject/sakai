/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

package org.sakaiproject.assignment.impl.conversion.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.assignment.impl.conversion.api.SchemaConversionHandler;
import org.sakaiproject.assignment.impl.conversion.impl.SchemaConversionController;;

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
			configFile = argv[0];
		}
		try
		{
			cc.convert(configFile);
		}
		catch (Exception ex)
		{
			log.info("Assignment UpgradeSchema main Failed to perform conversion ", ex);
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
			FileInputStream fin = new FileInputStream(config);
			try {
    			p.load(fin);
			} finally {
			    fin.close();
			}
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
			p.load(this.getClass().getResourceAsStream("upgradeschema.config"));
			StringBuilder sb = new StringBuilder();
			Object[] keys = p.keySet().toArray();
			Arrays.sort(keys);
			for (Object k : keys )
			{
				sb.append("\n " + k + ":" + p.get(k));
			}
			log.info("Loaded Default Properties " + config + " as " + sb.toString());
		}

		cpds.setDriver(p.getProperty("dbDriver"));
		cpds.setUrl(p.getProperty("dbURL"));
		cpds.setUser(p.getProperty("dbUser"));
		cpds.setPassword(p.getProperty("dbPass"));

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
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

		doMigrate(sequence, p.getProperty("dbDriver"));

		tds.close();

	}

	public void doMigrate(List<SchemaConversionDriver> sequence, String dbDriver)
	{
		try
		{
			SchemaConversionController scc = new SchemaConversionController();
			for (SchemaConversionDriver spec : sequence)
			{
				Class handlerClass = Class.forName(spec.getHandlerClass());
				SchemaConversionHandler sch = (SchemaConversionHandler) handlerClass
						.newInstance();
				sch.setDbDriver(dbDriver);
				log.info("Migrating using Handler " + spec.getHandler());
				int k = 0;
				scc.init(tds, sch, spec);
				while (scc.migrate(tds, sch, spec)) {
					log.info("Completed Batch "+(k++));
				}
				log.info("Done Migrating using Handler " + spec.getHandler());
			}

		}
		catch (Exception ex)
		{
			log.info(this + ":doMigrate Failed ", ex);
		}
	}
}

