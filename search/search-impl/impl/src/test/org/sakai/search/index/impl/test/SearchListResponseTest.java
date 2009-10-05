/**
 * 
 */
package org.sakai.search.index.impl.test;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.component.service.impl.SearchListResponseImpl;
import org.sakaiproject.search.filter.SearchItemFilter;
import org.sakaiproject.search.filter.impl.NullSearchFilter;

/**
 * @author ieb
 */
public class SearchListResponseTest extends TestCase
{

	private static final Log log = LogFactory.getLog(SearchListResponseTest.class);
	/**
	 * @param arg0
	 */
	public SearchListResponseTest(String arg0)
	{
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testParseError() throws Exception
	{
		String testString = "<?xml version=\"1.0\"?><fault><request>"
				+ "<![CDATA[ userid = null\n"
				+ "searchTerms = null\n"
				+ "checksum = null\n"
				+ "contexts = null\n"
				+ "ss = null\n"
				+ "se = null\n"
				+ "]]>"
				+ "</request><error><![CDATA[java.lang.Exception: Invalid Request\n"
				+ "at org.sakaiproject.search.component.service.impl.SearchServiceImpl.searchXML(SearchServiceImpl.java:846)\n"
				+ "at org.sakaiproject.search.tool.RestSearchServlet.execute(RestSearchServlet.java:103)\n"
				+ "at org.sakaiproject.search.tool.RestSearchServlet.doPost(RestSearchServlet.java:78)\n"
				+ "at javax.servlet.http.HttpServlet.service(HttpServlet.java:709)\n"
				+ "at javax.servlet.http.HttpServlet.service(HttpServlet.java:802)\n"
				+ "at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:252)\n"
				+ "at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:173)\n"
				+ "at org.sakaiproject.util.RequestFilter.doFilter(RequestFilter.java:540)\n"
				+ "at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:202)\n"
				+ "at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:173)\n"
				+ "at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:213)\n"
				+ "at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:178)\n"
				+ "at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:126)\n"
				+ "at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:105)\n"
				+ "at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:107)\n"
				+ "at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:148)\n"
				+ "at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:869)\n"
				+ "at org.apache.coyote.http11.Http11BaseProtocol$Http11ConnectionHandler.processConnection(Http11BaseProtocol.java:664)\n"
				+ "at org.apache.tomcat.util.net.PoolTcpEndpoint.processSocket(PoolTcpEndpoint.java:527)\n"
				+ "at org.apache.tomcat.util.net.LeaderFollowerWorkerThread.runIt(LeaderFollowerWorkerThread.java:80)\n"
				+ "at org.apache.tomcat.util.threads.ThreadPool$ControlRunnable.run(ThreadPool.java:684)\n"
				+ "at java.lang.Thread.run(Thread.java:613)"
				+ "]]></error></fault>";
		try
		{
			SearchListResponseImpl slri = new SearchListResponseImpl(testString,
					null, 0, 10, null, null, null, null);
			fail("Should have thrown an IOException ");
		}
		catch (IOException ex)
		{
			assertEquals("Should have found exception ", "Failed to perform remote request ",ex.getMessage());
		}

	}
	public void testResultsSet() throws Exception {
		String testString = 
			"<?xml version=\"1.0\"?>" +
			"<results  fullsize=\"17\"  start=\"11\"  size=\"9\"  >" +
			"<result index=\"11\"  score=\"0.99999994\"  " +
			"sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page11\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page11.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxMQ==\"  " +
			"tool=\"wiki\" " +
			" url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page11.html\" />" +
			"<result index=\"12\"  score=\"0.99999994\"  " +
			"sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page12\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page12.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxMg==\"  " +
			"tool=\"wiki\" " +
			" url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page12.html\" />" +
			"<result index=\"13\"  " +
			"score=\"0.99999994\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page13\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page13.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxMw==\"  " +
			"tool=\"wiki\"  url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page13.html\" />" +
			"<result index=\"14\"  " +
			"score=\"0.99999994\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page14\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page14.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxNA==\"  " +
			"tool=\"wiki\"  " +
			"url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page14.html\" />" +
			"<result index=\"15\"  " +
			"score=\"0.99999994\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page15\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page15.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxNQ==\"  " +
			"tool=\"wiki\"  " +
			"url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page15.html\" />" +
			"<result index=\"16\"  " +
			"score=\"0.5273437\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/home\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/home.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL2hvbWU=\"  " +
			"tool=\"wiki\"  " +
			"url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/home.html\" />" +
			"</results>";
		SearchItemFilter filter = new NullSearchFilter();
		SearchListResponseImpl slri = new SearchListResponseImpl(testString,
				null, 0, 10, null, filter,  null, null);
		for ( Iterator<SearchResult> i = slri.iterator(); i.hasNext(); ) {
			SearchResult sr = (SearchResult) i.next();
			log.debug("    Id         :"+sr.getId());
			log.debug("    Index      :"+sr.getIndex());
			log.debug("    Reference  :"+sr.getReference());
			log.debug("    Score      :"+sr.getScore());
			log.debug("    Title      :"+sr.getTitle());
			log.debug("    Tool       :"+sr.getTool());
			log.debug("    Url        :"+sr.getUrl());
			log.debug("    Terms      :"+sr.getTerms());
			log.debug("    Field Names:");
		}
	}
	
	public void testResultsSetEscape() throws Exception {
		String testString = 
			"<?xml version=\"1.0\"?>" +
			"<results  fullsize=\"17\"  start=\"11\"  size=\"9\"  >" +
			"<result index=\"11\"  score=\"0.99999994\"  " +
			"sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page11\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page11.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxMQ==\"  " +
			"tool=\"wiki\" " +
			" url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page11 &amp; page12.html\" />" +
			"<result index=\"12\"  score=\"0.99999994\"  " +
			"sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page12\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page12.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxMg==\"  " +
			"tool=\"wiki\" " +
			" url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page12.html\" />" +
			"<result index=\"13\"  " +
			"score=\"0.99999994\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page13\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page13.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxMw==\"  " +
			"tool=\"wiki\"  url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page13.html\" />" +
			"<result index=\"14\"  " +
			"score=\"0.99999994\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page14\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page14.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxNA==\"  " +
			"tool=\"wiki\"  " +
			"url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page14.html\" />" +
			"<result index=\"15\"  " +
			"score=\"0.99999994\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/page15\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page15.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL3BhZ2UxNQ==\"  " +
			"tool=\"wiki\"  " +
			"url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/page15.html\" />" +
			"<result index=\"16\"  " +
			"score=\"0.5273437\"  sid=\"/site/97c4d057-9de3-49db-80df-421b2d05ed52/home\"  " +
			"reference=\"/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/home.\"  " +
			"title=\"L3NpdGUvOTdjNGQwNTctOWRlMy00OWRiLTgwZGYtNDIxYjJkMDVlZDUyL2hvbWU=\"  " +
			"tool=\"wiki\"  " +
			"url=\"http://localhost:8088/access/wiki/site/97c4d057-9de3-49db-80df-421b2d05ed52/home.html\" />" +
			"</results>";
		SearchItemFilter filter = new NullSearchFilter();
		SearchListResponseImpl slri = new SearchListResponseImpl(testString,
				null, 0, 10, null, filter,  null, null);
		for ( Iterator<SearchResult> i = slri.iterator(); i.hasNext(); ) {
			SearchResult sr = (SearchResult) i.next();
			log.debug("    Id         :"+sr.getId());
			log.debug("    Index      :"+sr.getIndex());
			log.debug("    Reference  :"+sr.getReference());
			log.debug("    Score      :"+sr.getScore());
			log.debug("    Title      :"+sr.getTitle());
			log.debug("    Tool       :"+sr.getTool());
			log.debug("    Url        :"+sr.getUrl());
			log.debug("    Terms      :"+sr.getTerms());
			log.debug("    Field Names:");
		}
	}

}
