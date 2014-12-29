package org.sakaiproject.citation.impl.openurl;

import javax.servlet.http.HttpServletRequest;

/**
 * This isn't part of the the OpenURL spec but it's the best way to think of the COinS spec.
 * Currently this isn't used.
 * @see <a href="http://ocoins.info/">http://ocoins.info/</a>
 * @author buckett
 *
 */
public class COinSTransport implements Transport {

	public RawContextObject parse(HttpServletRequest request) {
		// This isn't supported as normally COinS come out of a HTML page.
		return null;
	}

	public String encode(String data) {
		return "<span class=\"Z3988\" title=\""+ data+ "\"></span>";
	}

}
