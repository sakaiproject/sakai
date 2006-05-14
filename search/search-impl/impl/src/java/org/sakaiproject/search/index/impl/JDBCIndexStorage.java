/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.index.IndexStorage;

/**
 * 
 * @author ieb
 *
 */
public class JDBCIndexStorage implements IndexStorage
{

	public IndexReader getIndexReader() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IndexSearcher getIndexSearcher() throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void doPostIndexUpdate() throws IOException
	{
		// TODO Auto-generated method stub
		
	}

	public void doPreIndexUpdate() throws IOException
	{
		// TODO Auto-generated method stub
		
	}

}
