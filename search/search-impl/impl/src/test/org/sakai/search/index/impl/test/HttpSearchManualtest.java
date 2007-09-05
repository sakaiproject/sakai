/**
 * 
 */
package org.sakai.search.index.impl.test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.api.SearchService;

/**
 * @author ieb
 */
public class HttpSearchManualtest extends TestCase
{

	private static final Log log = LogFactory.getLog(HttpSearchManualtest.class);
	private String sharedKey = "";
	private String searchServerUrl = "http://localhost:8080/sakai-search-tool/xmlsearch/";
//	 The HttpClient used for processing transactions
	private static HttpClient httpClient;
	private static HttpConnectionManagerParams params = new HttpConnectionManagerParams();
	private static HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

//	 This runs when the class is initialized
	static {
//	 Configure params for the Connection Manager
	params.setDefaultMaxConnectionsPerHost( 20 );
	params.setMaxTotalConnections( 30 );

//	 This next line may not be necessary since we specified default 2 lines ago, but here it is anyway
	params.setMaxConnectionsPerHost( HostConfiguration.ANY_HOST_CONFIGURATION, 20 );

//	 Set up the connection manager
	connectionManager.setParams( params );

//	 Finally set up the static multithreaded HttpClient
	httpClient = new HttpClient( connectionManager );
	}
	/**
	 * @param arg0
	 */
	public HttpSearchManualtest(String arg0)
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
	public void testDummy() {
		
	}

	public void xtestHttpSearchRequest() throws Exception
	{
		PostMethod post = new PostMethod(searchServerUrl);
		String userId = "admin";
		String contextParam = "admin";
		String searchTerms = "cowslip";
		post.setParameter(SearchService.REST_CHECKSUM, digestCheck(userId,
				searchTerms));
		post.setParameter(SearchService.REST_CONTEXTS, contextParam);
		post.setParameter(SearchService.REST_END, "10");
		post.setParameter(SearchService.REST_START, "0");
		post.setParameter(SearchService.REST_TERMS, searchTerms);
		post.setParameter(SearchService.REST_USERID, userId);
		

		httpClient.executeMethod(post);

		String response = post.getResponseBodyAsString();
		log.info("Got repose as " + response);
		

	}

	private String digestCheck(String userid, String searchTerms)
			throws GeneralSecurityException, IOException
	{
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		String chstring = sharedKey + userid + searchTerms;
		return byteArrayToHexStr(sha1.digest(chstring.getBytes("UTF-8")));
	}

	private static String byteArrayToHexStr(byte[] data)
	{
		char[] chars = new char[data.length * 2];
		for (int i = 0; i < data.length; i++)
		{
			byte current = data[i];
			int hi = (current & 0xF0) >> 4;
			int lo = current & 0x0F;
			chars[2 * i] = (char) (hi < 10 ? ('0' + hi) : ('A' + hi - 10));
			chars[2 * i + 1] = (char) (lo < 10 ? ('0' + lo) : ('A' + lo - 10));
		}
		return new String(chars);
	}

}
