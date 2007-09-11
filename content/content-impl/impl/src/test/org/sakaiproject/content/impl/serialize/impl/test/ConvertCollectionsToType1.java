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

package org.sakaiproject.content.impl.serialize.impl.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.persister.entity.PropertyMapping;
import org.sakaiproject.content.impl.serialize.impl.conversion.FileSizeResourcesConversionHandler;
import org.sakaiproject.content.impl.serialize.impl.conversion.SchemaConversionController;
import org.sakaiproject.content.impl.serialize.impl.conversion.Type1BlobCollectionConversionHandler;
import org.sakaiproject.content.impl.serialize.impl.conversion.Type1BlobResourcesConversionHandler;

import junit.framework.TestCase;

/**
 * @author ieb
 *
 */
public class ConvertCollectionsToType1 extends TestCase
{

	private static final Log log = LogFactory.getLog(ConvertCollectionsToType1.class);
	
	private SharedPoolDataSource tds;


	/**
	 * @param name
	 */
	public ConvertCollectionsToType1(String name)
	{
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		
		super.setUp();
		DriverAdapterCPDS cpds = new DriverAdapterCPDS();

		String config = System.getProperty("migrate.config"); //,"migrate.properties");
		Properties p = new Properties();
		if ( config != null ) {
			log.info("Using Config "+config);
			File f = new File(config);
			FileInputStream fin = new FileInputStream(config);
			p.load(fin);
			fin.close();
			for(Iterator<Object> i = p.keySet().iterator(); i.hasNext(); ) {
				Object k = i.next();
				log.info("   Test Properties "+k+":"+p.get(k));
			}
		}
		
		
		
		cpds.setDriver(p.getProperty("dbDriver","com.mysql.jdbc.Driver"));
		cpds.setUrl(p.getProperty("dbURL","jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&characterEncoding=UTF-8"));
		cpds.setUser(p.getProperty("dbUser","sakai22"));
		cpds.setPassword(p.getProperty("dbPass","sakai22"));

		tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(10);
		tds.setMaxWait(5);
		tds.setDefaultAutoCommit(false);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		tds.close();
		super.tearDown();
		
	}

	
	public void testConvertCollections() {
		try {
			SchemaConversionController scc = new SchemaConversionController();
			Type1BlobCollectionConversionHandler bch = new Type1BlobCollectionConversionHandler();
			while(scc.migrate(tds, bch));
			Type1BlobResourcesConversionHandler brh = new Type1BlobResourcesConversionHandler();
			while(scc.migrate(tds, brh));
			FileSizeResourcesConversionHandler fsh = new FileSizeResourcesConversionHandler();
			while(scc.migrate(tds, fsh));
		} catch ( Exception ex ) {
			log.info("Failed ",ex);
		}
	}
}
