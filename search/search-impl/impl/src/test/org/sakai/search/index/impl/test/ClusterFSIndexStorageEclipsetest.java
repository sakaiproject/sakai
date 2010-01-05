package org.sakai.search.index.impl.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.sakaiproject.search.index.impl.ClusterFSIndexStorage;
import org.sakaiproject.search.index.impl.JDBCClusterIndexStore;
import org.sakaiproject.search.index.impl.SnowballAnalyzerFactory;

public class ClusterFSIndexStorageEclipsetest extends TestCase
{

	private static final Log log = LogFactory
			.getLog(ClusterFSIndexStorageEclipsetest.class);

	ClusterFSIndexStorage cfs = null;

	JDBCClusterIndexStore cis = null;

	BasicDataSource dataSource;

	private String dbtype = "hsqldb";

	private int docnum = 0;

	protected void setUp() throws Exception
	{
		super.setUp();
		if ("hsqldb".equals(dbtype))
		{
			Properties properties = new Properties();
			properties.setProperty("driverClassName", "org.hsqldb.jdbcDriver");
			properties.setProperty("url", "jdbc:hsqldb:mem:search-test");
			properties.setProperty("maxActive", "10");
			properties.setProperty("maxWait", "500");
			properties.setProperty("defaultAutoCommit", "false");
			properties.setProperty("defaultReadOnly", "false");
			properties.setProperty("username", "sa");
			properties.setProperty("password", "");
			properties.setProperty("validationQuery",
					"select 1 from SYSTEM_USERS");
			dataSource = (BasicDataSource) BasicDataSourceFactory
					.createDataSource(properties);
			cis = new JDBCClusterIndexStore();
			cis.setDataSource(dataSource);

			Connection c = dataSource.getConnection();
			Statement s = c.createStatement();
			try
			{
				s.execute("CREATE TABLE search_segments " + "("
						+ "	name_ varchar(255) not null,"
						+ "	version_ BIGINT not null," 
						+ " size_ BIGINT not null"
						+ "	packet_ BINARY,"
						+ "    CONSTRAINT search_segments_index UNIQUE (name_)"
						+ "); ");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();

			}
			s.close();
			c.close();
		}

		if ("mysql".equals(dbtype))
		{
			Properties properties = new Properties();
			properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
			properties
					.setProperty("url",
							"jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&characterEncoding=UTF-8");
			properties.setProperty("maxActive", "10");
			properties.setProperty("maxWait", "500");
			properties.setProperty("defaultAutoCommit", "false");
			properties.setProperty("defaultReadOnly", "false");
			properties.setProperty("defaultTransactionIsolation",
					"READ_COMMITTED");
			properties.setProperty("username", "sakai22");
			properties.setProperty("password", "sakai22");
			properties.setProperty("validationQuery", "select 1 from DUAL");
			dataSource = (BasicDataSource) BasicDataSourceFactory
					.createDataSource(properties);
			cis = new JDBCClusterIndexStore();
			cis.setDataSource(dataSource);

			Connection c = dataSource.getConnection();
			Statement s = c.createStatement();
			try
			{
				s.execute("CREATE TABLE search_segments" + "				("
						+ "					name_ varchar(255) not null,"
						+ "					version_ BIGINT not null,"
						+ "					size_ BIGINT not null,"
						+ "					packet_ LONGBLOB" + "				);");
				s
						.execute("CREATE UNIQUE INDEX search_segments_index ON search_segments"
								+ "				(" + "				        name_" + "				);");
			}
			catch (Exception ex)
			{
				log.debug(ex);

			}
			s.close();
			c.close();
		}
		cfs = new ClusterFSIndexStorage();
		cfs.setAnalyzerFactory(new SnowballAnalyzerFactory());
		cfs.setClusterFS(cis);
		cis.setLocation("tmpindexstore");
	}

	protected void tearDown() throws Exception
	{
		dataSource.close();
		super.tearDown();
	}
	public void disabled_testAAASaveAllSegments() throws Exception
	{
		cis.saveAllSegments();

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.getIndexReader()'
	 */
	public void testGetIndexReader() throws Exception
	{
		IndexReader ir = cfs.getIndexReader();
		assertNotNull(ir);
		ir.numDocs();
		ir.close();

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.getIndexWriter(boolean)'
	 */
	public void testGetIndexWriter() throws Exception
	{
		cfs.doPreIndexUpdate();
		IndexWriter iw = cfs.getIndexWriter(false);
		assertNotNull(iw);
		Document doc = new Document();
		doc.add(new Field("id", String.valueOf(System.currentTimeMillis()),
				Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("contents", "some content about something",
				Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("name", "AName", Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		iw.addDocument(doc);
		iw.close();
		cfs.doPostIndexUpdate();

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.getIndexSearcher()'
	 */
	public void testGetIndexSearcher() throws Exception
	{

		IndexSearcher is = cfs.getIndexSearcher(false);
		assertNotNull(is);
		BooleanQuery query = new BooleanQuery();

		QueryParser qp = new QueryParser("contents", cfs.getAnalyzer());
		Query textQuery = qp.parse("about");
		query.add(textQuery, BooleanClause.Occur.MUST);
		log.info("Query is " + query.toString());
		Hits h = is.search(query);
		log.info("Got " + h.length() + " hits");

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.indexExists()'
	 */
	public void testIndexExists()
	{

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.getAnalyzer()'
	 */
	public void testGetAnalyzer()
	{
		assertNotNull(cfs.getAnalyzer());
	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.doPreIndexUpdate()'
	 */
	public void testDoPreIndexUpdate()
	{

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.doPostIndexUpdate()'
	 */
	public void testDoPostIndexUpdate()
	{

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.getAnalyzerFactory()'
	 */
	public void testGetAnalyzerFactory()
	{
		assertNotNull(cfs.getAnalyzerFactory());

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.setAnalyzerFactory(AnalyzerFactory)'
	 */
	public void testSetAnalyzerFactory()
	{

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.setRecoverCorruptedIndex(boolean)'
	 */
	public void testSetRecoverCorruptedIndex()
	{

	}

	/*
	 * Test method for
	 * 'org.sakaiproject.search.index.impl.ClusterFSIndexStorage.setClusterFS(ClusterFilesystem)'
	 */
	public void testSetClusterFS()
	{

	}

	public void testXBigTest() throws Exception
	{
		docnum = 0;
		cfs.doPreIndexUpdate();
		IndexWriter iw = cfs.getIndexWriter(false);
		File f = new File("testcontent");
		iw = loadDocuments(f, iw);
		iw.close();
		cfs.doPostIndexUpdate();
	}
	
	private IndexWriter reopen(IndexWriter iw) throws IOException, ParseException {
		log.info("Optimize===============");
		iw.optimize();
		iw.close();
		iw = null;
		cfs.doPostIndexUpdate();
		
		
		log.info("Query==================");
		
		long start = System.currentTimeMillis();
		
		IndexSearcher is = cfs.getIndexSearcher(false);
		
		log.info("----Open took "+(System.currentTimeMillis()-start));
		start = System.currentTimeMillis();
		
		BooleanQuery query = new BooleanQuery();

		QueryParser qp = new QueryParser("contents", cfs.getAnalyzer());
		Query textQuery = qp.parse("sakai");
		query.add(textQuery, BooleanClause.Occur.MUST);
		log.info("Query is " + query.toString());
		Hits h = is.search(query);
		log.info("Got " + h.length() + " hits  in "+(System.currentTimeMillis()-start)+" ms");
		
		log.info("Reopen=================");
		cfs.doPreIndexUpdate();
		iw = cfs.getIndexWriter(false);
		log.info("Indexing===============");
		return iw;
	}

	private IndexWriter loadDocuments(File f, IndexWriter iw) throws IOException, ParseException
	{
		if (f.isDirectory())
		{
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					iw = loadDocuments(files[i], iw);
				}
				else
				{
					iw = loadDocument(files[i], iw);
				}
			}
		}
		else
		{
			iw = loadDocument(f, iw);
		}
		return iw;
	}

	private IndexWriter loadDocument(File file, IndexWriter iw) throws IOException, ParseException
	{
		docnum++;
		if ( (docnum%20) == 0 ) {
			iw = reopen(iw);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String s = null;
		while ((sb.length() < 1000000) && (s = br.readLine()) != null )
		{
			sb.append(s).append("\n");
		}
		Document doc = new Document();
		doc.add(new Field("name", file.getAbsolutePath(), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field("contents", sb.toString(), Field.Store.YES,
				Field.Index.ANALYZED));
		iw.addDocument(doc);
		br.close();
		return iw;

	}

}
