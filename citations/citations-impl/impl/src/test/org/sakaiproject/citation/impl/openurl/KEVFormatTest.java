/**
 * Copyright (c) 2003-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.citation.impl.openurl;

import junit.framework.TestCase;

public class KEVFormatTest extends TestCase {

	// Example from solo.ouls.ox.ac.uk
	// url_ctx_fmt=infofi/fmt:kev:mtx:ctx - looks like the value is bad.
	// ctx_enc=info:ofi/enc:UTF-8 - Should just be UTF-8 according to the docs.
	// ctx_tim= Shouldn't include a timezone (must be in UST).
	private static final String PRIMO_EXAMPLE = "ctx_ver=Z39.88-2004&ctx_enc=info:ofi/enc:UTF-8&" +
			"ctx_tim=2010-10-20T13:27:00IST&url_ver=Z39.88-2004&url_ctx_fmt=infofi/fmt:kev:mtx:ctx&" +
			"rfr_id=info:sid/primo.exlibrisgroup.com:primo3-Journal-UkOxU&rft_val_fmt=info:ofi/fmt:kev:mtx:book&" +
			"rft.genre=book&rft.atitle=&rft.jtitle=&rft.btitle=Linux%20in%20a%20nutshell&rft.aulast=Siever&" +
			"rft.auinit=&rft.auinit1=&rft.auinitm=&rft.ausuffix=&rft.au=&rft.aucorp=&rft.volume=&rft.issue=&" +
			"rft.part=&rft.quarter=&rft.ssn=&rft.spage=&rft.epage=&rft.pages=&rft.artnum=&rft.issn=&rft.eissn=&" +
			"rft.isbn=9780596154486&rft.sici=&rft.coden=&rft_id=info:doi/&rft.object_id=&" +
			"rft_dat=<UkOxU>UkOxUb17140770</UkOxU>&rft.eisbn=";
	
	// Example from worldcat
	private static final String WORLDCAT_EXAMPLE = "FirstSearch:WorldCat&genre=book&isbn=9780596154486&" +
			"title=Linux+in+a+nutshell.&date=2009&aulast=Siever&aufirst=Ellen&id=doi:&" +
			"pid=<accession+number>403436468</accession+number><fssessid>0</fssessid><edition>6th+ed.+/</edition>&" +
			"url_ver=Z39.88-2004&rfr_id=info:sid/firstsearch.oclc.org:WorldCat&rft_val_fmt=info:ofi/fmt:kev:mtx:book&" +
			"req_dat=<sessionid>0</sessionid>&rfe_dat=<accessionnumber>403436468</accessionnumber>&" +
			"rft_id=info:oclcnum/403436468&rft_id=urn:ISBN:9780596154486&rft.aulast=Siever&" +
			"rft.aufirst=Ellen&rft.btitle=Linux+in+a+nutshell.&rft.date=2009&rft.isbn=9780596154486&" +
			"rft.place=Beijing+;;Farnham+;;Sebastopol&rft.pub=O'Reilly&rft.edition=6th+ed.+/&rft.genre=book";
	
	// Example from copac
	private static final String COPAC_EXAMPLE = "ctx_ver=Z39.88-2004&rfr_id=info:sid/mimas.ac.uk:copac&" +
			"rft_val_fmt=info:ofi/fmt:kev:mtx:book&rft.isbn=0596154488&rft.genre=book&" +
			"rft.btitle=Linux%20in%20a%20nutshell.&rft.au=Siever,%20Ellen.&rft.place=Beijing%20&rft.pub=O'Reilly,&" +
			"rft.date=2009&rft.edition=6th%20ed.%20/%20Ellen%20Siever%20...%20[et%20al.].&" +
			"rft.pages=xxii,%20917%20p.%20:%20ill.%20&%2023%20cm.=&rft.series=";
	
	public void testParsePrimo() {
		KEVFormat format = new KEVFormat();
		ContextObject linuxNutshell = format.parse(PRIMO_EXAMPLE);
		ContextObjectEntity book = linuxNutshell.getEntity(ContextObject.Entity.REFERENT);
		
		assertEquals("Linux in a nutshell", book.getValue("btitle"));
		assertEquals("9780596154486", book.getValue("isbn"));
		assertTrue(book.getIds().contains("info:doi/") );
		assertEquals("<UkOxU>UkOxUb17140770</UkOxU>", book.getData());
	}
	
	public void testParseWorldcat() {
		KEVFormat format = new KEVFormat();
		ContextObject linuxNutshell = format.parse(WORLDCAT_EXAMPLE);
		ContextObjectEntity book = linuxNutshell.getEntity(ContextObject.Entity.REFERENT);
		
		assertEquals("info:ofi/fmt:kev:mtx:book",book.getFormat());
		assertEquals("Beijing ;;Farnham ;;Sebastopol", book.getValue("place"));
		
	}
	
	public void testParseCopac() {
		KEVFormat format = new KEVFormat();
		ContextObject linuxNutshell = format.parse(WORLDCAT_EXAMPLE);
		ContextObjectEntity book = linuxNutshell.getEntity(ContextObject.Entity.REFERENT);
		
		assertEquals("info:ofi/fmt:kev:mtx:book",book.getFormat());
		assertEquals("Beijing ;;Farnham ;;Sebastopol", book.getValue("place"));
		assertEquals("book", book.getValue("genre"));
	}
}
