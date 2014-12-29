package org.sakaiproject.citation.impl.openurl;

import javax.servlet.http.HttpServletRequest;

/**
 * All the transports are at the HTTP level at the moment.
 * 
 * @author buckett
 *
 */
public interface Transport {

	public static final String URL_VER = "url_ver";
	public static final String URL_CTX_VAL = "url_ctx_val";
	public static final String URL_CTX_FMT = "url_ctx_fmt";
	public static final String URL_TIM = "url_tim";
	
	/**
	 * Finds the format and the data.
	 * @param request
	 */
	public RawContextObject parse(HttpServletRequest request);
	
	public String encode(String data);
}
