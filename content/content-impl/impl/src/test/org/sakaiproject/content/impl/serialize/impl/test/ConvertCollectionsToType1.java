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

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
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
		cpds.setDriver("org.hsqldb.jdbcDriver");
		cpds.setUrl("jdbc:hsqldb:mem:aname");
		cpds.setUser("sa");
		cpds.setPassword("");

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
		SchemaConversionController scc = new SchemaConversionController();
		Type1BlobCollectionConversionHandler bch = new Type1BlobCollectionConversionHandler();
		while(scc.migrate(tds, bch));
		Type1BlobResourcesConversionHandler brh = new Type1BlobResourcesConversionHandler();
		while(scc.migrate(tds, brh));
	}
}
