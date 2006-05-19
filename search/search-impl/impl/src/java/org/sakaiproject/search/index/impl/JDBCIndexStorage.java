/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcDirectorySettings;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;
import org.apache.lucene.store.jdbc.dialect.Dialect;
import org.apache.lucene.store.jdbc.dialect.HSQLDialect;
import org.apache.lucene.store.jdbc.dialect.MySQLMyISAMDialect;
import org.apache.lucene.store.jdbc.dialect.OracleDialect;
import org.apache.lucene.store.jdbc.lock.PhantomReadLock;
import org.apache.lucene.store.jdbc.support.JdbcTable;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.index.IndexStorage;

/**
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
		
		Dialect d = null;
		String targetDB = SqlService.getVendor().trim();
		if ( "mysql".equalsIgnoreCase(targetDB) ) {
			d = new MySQLMyISAMDialect();
		} else if ( "oracle".equalsIgnoreCase(targetDB) ) {
			d = new OracleDialect();
		} else {
			d = new HSQLDialect();
		}
			

		JdbcTable table = new JdbcTable(settings, d,
				searchTableName);
		return new JdbcDirectory(dataSource, table);
	}

	public IndexReader getIndexReader() throws IOException
	{

		JdbcDirectory directory = getJdbcDirectory();
		return IndexReader.open(directory);
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		JdbcDirectory directory = getJdbcDirectory();
		return new IndexWriter(directory, getAnalyzer(), create);
	}

	public IndexSearcher getIndexSearcher() throws IOException
	{
		JdbcDirectory directory = getJdbcDirectory();
		return new IndexSearcher(directory);

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
		// TODO Auto-generated method stub
		
	}

	public void setLocation(String location)
	{
		searchTableName = location;
		
	}

}
