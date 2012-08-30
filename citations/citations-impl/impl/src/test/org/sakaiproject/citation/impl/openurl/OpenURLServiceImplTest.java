package org.sakaiproject.citation.impl.openurl;

import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.impl.openurl.ContextObject.Entity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.AbstractSingleSpringContextTests;

public class OpenURLServiceImplTest extends AbstractSingleSpringContextTests {

	private static final String PRIMO_EXAMPLE = "ctx_ver=Z39.88-2004&ctx_enc=info:ofi/enc:UTF-8&" +
	"ctx_tim=2010-10-20T13:27:00IST&url_ver=Z39.88-2004&url_ctx_fmt=infofi/fmt:kev:mtx:ctx&" +
	"rfr_id=info:sid/primo.exlibrisgroup.com:primo3-Journal-UkOxU&rft_val_fmt=info:ofi/fmt:kev:mtx:book&" +
	"rft.genre=book&rft.atitle=&rft.jtitle=&rft.btitle=Linux in a nutshell&rft.aulast=Siever&" +
	"rft.auinit=&rft.auinit1=&rft.auinitm=&rft.ausuffix=&rft.au=&rft.aucorp=&rft.volume=&rft.issue=&" +
	"rft.part=&rft.quarter=&rft.ssn=&rft.spage=&rft.epage=&rft.pages=&rft.artnum=&rft.issn=&rft.eissn=&" +
	"rft.isbn=9780596154486&rft.sici=&rft.coden=&rft_id=info:doi/&rft.object_id=&" +
	"rft_dat=<UkOxU>UkOxUb17140770</UkOxU>&rft.eisbn=";
	
	private static final String PRIMO_EXAMPLE_FULL_ID = "ctx_ver=Z39.88-2004&ctx_enc=info:ofi/enc:UTF-8&ctx_tim=2011-04-11T15%3A40%3A37IST&url_ver=Z39.88-2004&url_ctx_fmt=infofi/fmt:kev:mtx:ctx&rfr_id=info:sid/primo.exlibrisgroup.com:primo3-Article-crossref&rft_val_fmt=info:ofi/fmt:kev:mtx:&rft.genre=article&rft.atitle=Cheese&rft.jtitle=Journal of Agricultural %26 Food Information&rft.btitle=&rft.aulast=Cherubin&rft.auinit=&rft.auinit1=&rft.auinitm=&rft.ausuffix=&rft.au=Cherubin, Dan&rft.aucorp=&rft.date=2007049&rft.volume=7&rft.issue=4&rft.part=&rft.quarter=&rft.ssn=&rft.spage=3&rft.epage=10&rft.pages=&rft.artnum=&rft.issn=1049-6505&rft.eissn=&rft.isbn=&rft.sici=&rft.coden=&rft_id=info:doi/10.1300/J108v07n04_02&rft.object_id=&rft_dat=%3Ccrossref%3E10.1300/J108v07n04_02%3C/crossref%3E&rft.eisbn=&rft_id=http%3A%2F%2Fsolo.bodleian.ox.ac.uk%2Fprimo_library%2Flibweb%2Faction%2Fdisplay.do%3Fdoc%3DTN_crossref10.1300/J108v07n04_02%26vid%3DOXVU1%26fn%3Ddisplay%26displayMode%3Dfull&rft_id=info:oai/";

	private OpenURLServiceImpl service;

	protected void onSetUp() throws Exception {
		this.service = (OpenURLServiceImpl) getApplicationContext().getBean("org.sakaiproject.citation.impl.openurl.OpenURLServiceImpl");

	}
	
	protected String[] getConfigLocations() {
		return new String[] {
				"classpath:org/sakaiproject/citation/impl/openurl.xml",
				"classpath:org/sakaiproject/citation/impl/openurl/test-beans.xml"
		};
	}
	
	public void testConvertCitation() {
		assertNull(service.convert((Citation)null));
	}
	
	public void testConvertContextObject() {
		assertNull(service.convert((ContextObject)null));
	}
	
	public void testParseNull() {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "http://localhost:8080/someurl");
		ContextObject contextObject = service.parse(req);
		assertNull(contextObject);
	}
	
	public void testParsePrimo() {
		MockHttpServletRequest req = createRequest("http://localhost:8080/someurl?"+PRIMO_EXAMPLE);
		
		ContextObject contextObject = service.parse(req);
		assertNotNull(contextObject);
		ContextObjectEntity book = contextObject.getEntity(Entity.REFERENT);
		assertEquals("Linux in a nutshell", book.getValue("btitle"));
		
		Citation bookCitation = service.convert(contextObject);
		assertEquals("Linux in a nutshell", bookCitation.getCitationProperty(Schema.TITLE, false));
	}

	private MockHttpServletRequest createRequest(String openUrl) {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", openUrl);
		req.setQueryString(openUrl);
		req.setParameters(parseQueryString(openUrl));
		return req;
	}
	
	public void testParsePrimoFullId() {
		MockHttpServletRequest req = createRequest("http://localhost:8080/someurl?"+PRIMO_EXAMPLE_FULL_ID);
		
		ContextObject contextObject = service.parse(req);
		Citation citation = service.convert(contextObject);
		
		assertNotNull(citation.getCitationProperty("otherIds", false));
		assertEquals("Cheese", citation.getCitationProperty("title", false));
	}
	
	public void testParseBook() {
		Citation book = convert(find(mockGetRequest(SamplePrimoOpenURLs.BOOK)));
		Map props = book.getCitationProperties();
		assertEquals("Patent searching: tools & techniques", props.get("title"));
		assertEquals("047178379X", props.get("isnIdentifier"));
		assertEquals("[edited By] David Hunt, Long Nguyen, Matthew Rodgers.", props.get("creator"));
	}
	
	public Citation convert(ContextObject contextObject) {
		Citation citation = service.convert(contextObject);
		assertNotNull(citation);
		return citation;
	}
	
	public HttpServletRequest mockGetRequest(String url) {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", url);
		init(req);
		return req;
	}
	
	public ContextObject find(HttpServletRequest req) {
		ContextObject contextObject = service.parse(req);
		assertNotNull(contextObject);
		return contextObject;
	}
	
	/**
	 * Sets up the query string and parameters based on the URL.
	 * @param req
	 */
	public void init(MockHttpServletRequest req) {
		String url = req.getRequestURL().toString();
		int queryStart = url.indexOf('?');
		if (queryStart >= 0) {
			String query = url.substring(queryStart+1);
			req.setQueryString(query);
			req.setParameters(parseQueryString(query));
		}
	}
	
	// Stolen from HttpUtils.parseQueryString()
	public static Map parseQueryString(String s) {
		String valArray[] = null;

		if (s == null) {
			throw new IllegalArgumentException();
		}
		Hashtable ht = new Hashtable();
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(s, "&");
		while (st.hasMoreTokens()) {
			String pair = (String) st.nextToken();
			int pos = pair.indexOf('=');
			if (pos == -1) {
				// XXX
				// should give more detail about the illegal argument
				throw new IllegalArgumentException();
			}
			String key = parseName(pair.substring(0, pos), sb);
			String val = parseName(pair.substring(pos + 1, pair.length()), sb);
			if (ht.containsKey(key)) {
				String oldVals[] = (String[]) ht.get(key);
				valArray = new String[oldVals.length + 1];
				for (int i = 0; i < oldVals.length; i++)
					valArray[i] = oldVals[i];
				valArray[oldVals.length] = val;
			} else {
				valArray = new String[1];
				valArray[0] = val;
			}
			ht.put(key, valArray);
		}
		return ht;
	}
	
	// Stolen from HttpUtils.parseQueryString()
	static private String parseName(String s, StringBuffer sb) {
		sb.setLength(0);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '+':
				sb.append(' ');
				break;
			case '%':
				try {
					sb.append((char) Integer.parseInt(
							s.substring(i + 1, i + 3), 16));
					i += 2;
				} catch (NumberFormatException e) {
					// XXX
					// need to be more specific about illegal arg
					throw new IllegalArgumentException();
				} catch (StringIndexOutOfBoundsException e) {
					String rest = s.substring(i);
					sb.append(rest);
					if (rest.length() == 2)
						i++;
				}

				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
	
	public void testParseSampleArticle() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.ARTICLE);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Patent", entity.getValue("atitle"));
		Citation citation = service.convert(co);
		assertEquals("Patent", citation.getCitationProperty("title", false));
		assertEquals("Metal Powder Report", citation.getCitationProperty("sourceTitle", false));
		assertEquals("199206", citation.getCitationProperty("date", false));
		assertEquals("47", citation.getCitationProperty("volume", false));
		assertEquals("6", citation.getCitationProperty("issue", false));
		assertEquals("59", citation.getCitationProperty("startPage", false));
		assertEquals("61", citation.getCitationProperty("endPage", false));
		assertEquals("00260657", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1016/0026-0657(92)91523-M", citation.getCitationProperty("doi", false));
	}
	
	public void testParseSampleNewspaper() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.NEWSPAPER);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("THE NATIONAL ERA", entity.getValue("jtitle"));
		Citation citation = service.convert(co);
		assertTrue(((String)citation.getCitationProperty("title", false)).startsWith("AZA")); 
		assertEquals("THE NATIONAL ERA", citation.getCitationProperty("sourceTitle", false));
		assertEquals("18590310", citation.getCitationProperty("date", false));
		assertEquals("XIII", citation.getCitationProperty("volume", false));
		assertEquals("636", citation.getCitationProperty("issue", false));
	}
	
	public void testParseSampleOther() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.OTHER);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("European Journal of Echocardiography", entity.getValue("jtitle"));
		Citation citation = service.convert(co);
		// Fails because it's described as a book, but is actually a journal
		assertEquals("Eustachian valve interfering with transcatheter closure of patent foramen ovale", citation.getCitationProperty("title", false));
		assertEquals("European Journal of Echocardiography", citation.getCitationProperty("sourceTitle", false));
		assertEquals("Roelandt, Philip", citation.getCitationProperty("creator", false));
		assertEquals("200707", citation.getCitationProperty("date", false));
		assertEquals("9", citation.getCitationProperty("volume", false));
		assertEquals("1", citation.getCitationProperty("issue", false));
		assertEquals("158", citation.getCitationProperty("startPage", false));
		assertEquals("159", citation.getCitationProperty("endPage", false));
		assertEquals("1525-2167", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1016/j.euje.2007.05.006", citation.getCitationProperty("doi", false));
	}
	
	public void testParseSampleText() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.TEXT);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Cardiology in the Young", entity.getValue("jtitle"));
		Citation citation = service.convert(co);
		assertEquals("Complete transcatheter closure of a patent arterial duct with subsequent haemolysis", citation.getCitationProperty("title", false));
		assertEquals("Cardiology in the Young", citation.getCitationProperty("sourceTitle", false));
		assertEquals("Cace, Neven", citation.getCitationProperty("creator", false));
		assertEquals("20100325201008", citation.getCitationProperty("date", false));
		assertEquals("20", citation.getCitationProperty("volume", false));
		assertEquals("4", citation.getCitationProperty("issue", false));
		assertEquals("462", citation.getCitationProperty("startPage", false));
		assertEquals("464", citation.getCitationProperty("endPage", false));
		assertEquals("1047-9511", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1017/S1047951110000326", citation.getCitationProperty("doi", false));
	}
	
	public void testParseSampleBook() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.BOOK);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Patent searching: tools & techniques", entity.getValue("btitle"));
		Citation citation = service.convert(co);
		assertEquals("Patent searching: tools & techniques", citation.getCitationProperty("title", false));
		assertEquals("[edited By] David Hunt, Long Nguyen, Matthew Rodgers.", citation.getCitationProperty("creator", false));
		assertEquals("047178379X", citation.getCitationProperty("isnIdentifier", false));
	}
	
	public void testParseSampleReview() {
		// Rather screwed, a book by rft_val_fmt but using journal properties.
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.REVIEW);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Theatre Research International", entity.getValue("jtitle"));
		Citation citation = service.convert(co);
		assertEquals("Children of the Queen's Revels: A Jacobean Theatre Repertory (Book Review)", citation.getCitationProperty("title", false));
		assertEquals("Theatre Research International", citation.getCitationProperty("sourceTitle", false));
		assertEquals("PRICE, VICTORIA E", citation.getCitationProperty("creator", false));
		assertEquals("200707", citation.getCitationProperty("date", false));
		assertEquals("32", citation.getCitationProperty("volume", false));
		assertEquals("2", citation.getCitationProperty("issue", false));
		assertEquals("225", citation.getCitationProperty("startPage", false));
		assertEquals("0307-8833", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1017/S0307883306002653", citation.getCitationProperty("doi", false));
	}
	
	public void testSampleJournal() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.JOURNAL);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Kingston Journal", entity.getValue("jtitle"));
		Citation citation = service.convert(co);
		assertEquals("Kingston Journal", citation.getCitationProperty("title", false));
		assertEquals("1789", citation.getCitationProperty("date", false));
	}
	
	public void testSampleLegal() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.LEGAL);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("198604", entity.getValue("date"));
		Citation citation = service.convert(co);
		assertTrue(((String)citation.getCitationProperty("title", false)).startsWith("Patent Policy"));
		assertEquals("198604", citation.getCitationProperty("date", false));
	}
	
	public void testSampleDissertation() {
		// Again a book, but using article title.
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.DISSERTATION);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Thomas Blanchard's Patent Management", entity.getValue("atitle"));
		Citation citation = service.convert(co);
		assertEquals("Thomas Blanchard's Patent Management", citation.getCitationProperty("title", false));
		assertEquals("The Journal of Economic History", citation.getCitationProperty("sourceTitle", false));
		assertEquals("Cooper, Carolyn C", citation.getCitationProperty("creator", false));
		assertEquals("198706", citation.getCitationProperty("date", false));
		assertEquals("47", citation.getCitationProperty("volume", false));
		assertEquals("2", citation.getCitationProperty("issue", false));
		assertEquals("487", citation.getCitationProperty("startPage", false));
		assertEquals("488", citation.getCitationProperty("endPage", false));
		assertEquals("0022-0507", citation.getCitationProperty("isnIdentifier", false));
		assertEquals("10.1017/S002205070004821X", citation.getCitationProperty("doi", false));
	}
	
	public void testSamplePrimoImage() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.IMAGE);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Cocktail Hour", entity.getValue("atitle"));
		Citation citation = service.convert(co);
		assertEquals("Cocktail Hour", citation.getCitationProperty("title", false));
		assertEquals("2008", citation.getCitationProperty("date", false));
		assertEquals("Nov/Dec 2008", citation.getCitationProperty("issue", false));
		assertEquals("58", citation.getCitationProperty("startPage", false));
		assertEquals("63", citation.getCitationProperty("endPage", false));
		assertEquals("1836-7526", citation.getCitationProperty("isnIdentifier", false));
	}
	
	// We had an issue with this one where both the title and sourceTitle ended up with being the the same.
	public void testSamplePrimoJournalCorrect() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.CORRECT_JOURNAL);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertEquals("Theory and practice of logic programming", entity.getValue("jtitle"));
		Citation citation = service.convert(co);
		assertEquals("Theory and practice of logic programming", citation.getCitationProperty("title", false));
		assertEquals("", citation.getCitationProperty("sourceTitle", false));
	}
	
	public void testLinuxInANutshellGood() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.WORKING_IMPORT);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertNotNull(entity.getIds());
		assertTrue(entity.getIds().contains("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=UkOxUUkOxUb15585873&vid=OXVU1&fn=display&displayMode=full"));
	}
	
	public void testLinuxInANutshellBad() {
		HttpServletRequest req = createRequest(SamplePrimoOpenURLs.BROKEN_IMPORT);
		ContextObject co = service.parse(req);
		ContextObjectEntity entity = co.getEntity(Entity.REFERENT);
		assertNotNull(entity.getIds());
		assertTrue(entity.getIds().contains("http://solo.bodleian.ox.ac.uk/primo_library/libweb/action/display.do?doc=UkOxUUkOxUb17140770&vid=OXVU1&fn=display&displayMode=full"));
	}
}
