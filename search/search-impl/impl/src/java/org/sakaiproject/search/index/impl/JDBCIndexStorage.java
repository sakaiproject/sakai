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
package org.sakaiproject.search.index.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcDirectorySettings;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;
import org.apache.lucene.store.jdbc.dialect.Dialect;
import org.apache.lucene.store.jdbc.dialect.HSQLDialect;
import org.apache.lucene.store.jdbc.dialect.MySQLMyISAMDialect;
import org.apache.lucene.store.jdbc.dialect.OracleDialect;
import org.apache.lucene.store.jdbc.index.JdbcBufferedIndexInput;
import org.apache.lucene.store.jdbc.lock.PhantomReadLock;
import org.apache.lucene.store.jdbc.support.JdbcTable;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.index.IndexStorage;

/**
 * This is a pure JDBC implementation of the IndexStore. It uses the 
 * JDBCDrirectory from the Compass OpenSymphony search framework. This storeds
 * the segments of the index in the database directly as Blobs. These are then directly 
 * accessed when searching. Obviously there are significant performance implications of
 * this approach. (lots of DB traffic on search and index creationg and about 1/10
 * performance.
 * @author ieb
 */
public class JDBCIndexStorage implements IndexStorage
{
	
	private static Log log = LogFactory.getLog(JDBCIndexStorage.class);


	private AnalyzerFactory analyzerFactory = null;

	public DataSource dataSource = null;

	private String searchTableName;

	private JdbcDirectory getJdbcDirectory()
	{
		JdbcDirectorySettings settings = new JdbcDirectorySettings();
		settings.setLockClass(PhantomReadLock.class);
		JdbcFileEntrySettings deletableSettings = new JdbcFileEntrySettings();
		// set the buffer size to 32K
		int buffersize = 4*1024;		
        settings.getFileEntrySettings("deletable").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);
        settings.getFileEntrySettings("deleteable.new").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);
        // in case lucene fix the spelling mistake
        settings.getFileEntrySettings("deletable.new").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);
        settings.getFileEntrySettings("segments").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);
        settings.getFileEntrySettings("segments.new").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);
        settings.getFileEntrySettings("del").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);
        settings.getFileEntrySettings("tmp").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);
        settings.getFileEntrySettings("fnm").setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING,buffersize);

        Dialect d = null;
		String targetDB = SqlService.getVendor().trim();
		if ( "mysql".equalsIgnoreCase(targetDB) ) {
			// MySQL BLOBS is not seekable hence there WILL be a big performance drop
			d = new MySQLMyISAMDialect(false);
			log.warn("Search Index is using the MySQL Driver wich does not have support for seekable BLOBS without emulateLocators=true. Performance will be downgraded");
		} else if ( "oracle".equalsIgnoreCase(targetDB) ) {
			d = new OracleDialect();
		} else {
			d = new HSQLDialect();
			log.warn("Search Index is using the HSQL Driver wich does not have support for seekable BLOBS. Performance will be downgraded");
		}
			

		JdbcTable table = new JdbcTable(settings, d,
				searchTableName);
		JdbcDirectory jd = new JdbcDirectory(dataSource, table);
		if (  !indexExists() ) {
			try
			{
				jd.create();
			}
			catch (IOException e)
			{
				log.error("Failed to create search engine index storage in the db",e);
			}
		}
		
		return jd;
	}

	public IndexReader getIndexReader() throws IOException
	{

		JdbcDirectory directory = getJdbcDirectory();
		return IndexReader.open(directory);
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		JdbcDirectory directory = getJdbcDirectory();
		IndexWriter iw =  new IndexWriter(directory, getAnalyzer(), create);
		iw.setUseCompoundFile(false);
		return iw;
	}

	public IndexSearcher getIndexSearcher() throws IOException
	{
		long reloadStart = System.currentTimeMillis();
		JdbcDirectory directory = getJdbcDirectory();
		IndexSearcher indexSearcher = new IndexSearcher(directory);

		long reloadEnd = System.currentTimeMillis();
		log.info("Reload Complete " + indexSearcher.maxDoc() + " in "
				+ (reloadEnd - reloadStart));

		return indexSearcher;

	}

	public void doPostIndexUpdate() throws IOException
	{
		Connection c = DataSourceUtils.getConnection(dataSource);
		DataSourceUtils.commitConnectionIfPossible(c);
		DataSourceUtils.releaseConnection(c);
	}

	public void doPreIndexUpdate() throws IOException
	{
		DataSourceUtils.getConnection(dataSource);
	}

	public boolean indexExists()
	{
		
		Connection c = null;
		Statement s = null;
		ResultSet rs = null;
		try
		{
			c = DataSourceUtils.getConnection(dataSource);
			s = c.createStatement();
			rs = s.executeQuery("select count(*) from " + searchTableName);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
		finally
		{
			DataSourceUtils.closeResultSet(rs);
			DataSourceUtils.closeStatement(s);
			DataSourceUtils.releaseConnection(c);
		}
	}

	public Analyzer getAnalyzer()
	{
		return analyzerFactory.newAnalyzer();
	}

	/**
	 * @return Returns the analyzerFactory.
	 */
	public AnalyzerFactory getAnalyzerFactory()
	{
		return analyzerFactory;
	}

	/**
	 * @param analyzerFactory The analyzerFactory to set.
	 */
	public void setAnalyzerFactory(AnalyzerFactory analyzerFactory)
	{
		this.analyzerFactory = analyzerFactory;
	}

	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource()
	{
		return dataSource;
	}

	/**
	 * @param dataSource The dataSource to set.
	 */
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	/**
	 * @return Returns the searchTableName.
	 */
	public String getSearchTableName()
	{
		return searchTableName;
	}

	/**
	 * @param searchTableName The searchTableName to set.
	 */
	public void setSearchTableName(String searchTableName)
	{
		this.searchTableName = searchTableName;
	}

	public void setRecoverCorruptedIndex(boolean recover)
	{
		log.info("Using JDBCIndexStorage, storing the index "
				+ "on in the database  in " + searchTableName
				+ " if the index is corrupted recovery automatic" +
						" recovery is not available");
		if ( recover ) {
			log.warn("Automatic recovery not available on the JDBC Search Index Driver");
		}
		
	}

	public void setLocation(String location)
	{
		searchTableName = location;
		
	}

	public long getLastUpdate()
	{
		log.warn("Last Update not implemented, relying on cluster events for reload ");
		return 0;
	}

	public List getSegmentInfoList()
	{
		List l = new ArrayList();
		l.add(new Object[]{"Index Segment Info is not implemented for JDBC file system index stores","",""});
		return l;
	}

	public void closeIndexReader(IndexReader indexReader) throws IOException
	{
		if ( indexReader != null ) {
			indexReader.close();
		}
	}

	public void closeIndexWriter(IndexWriter indexWrite) throws IOException
	{
		if ( indexWrite != null ) {
			indexWrite.close();
		}		
	}
	public boolean isMultipleIndexers()
	{
		return false;
	}


}
