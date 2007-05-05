package org.sakaiproject.scorm.client;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ScormFilter implements Filter {

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		
		
        chain.doFilter(request, new WrappedResponse(httpRequest, httpResponse));
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	public class WrappedResponse extends HttpServletResponseWrapper
	{
		/** The request. */
		protected HttpServletRequest m_req = null;

		/** Wrapped Response * */
		protected HttpServletResponse m_res = null;

		public WrappedResponse(HttpServletRequest req, HttpServletResponse res)
		{
			super(res);

			m_req = req;
			m_res = res;
		}

		public String encodeRedirectUrl(String url)
		{
			return rewriteURL(url);
		}

		public String encodeRedirectURL(String url)
		{
			return rewriteURL(url);
		}

		public String encodeUrl(String url)
		{
			return rewriteURL(url);
		}

		public String encodeURL(String url)
		{
			return rewriteURL(url);
		}

		public void sendRedirect(String url) throws IOException
		{
			url = rewriteURL(url);
			m_req.setAttribute("scorm.redirect", url);
			super.sendRedirect(url);
		}
		
		protected String rewriteURL(String url) {	
			int index = url.indexOf("/sakai.scorm.tool");

			if (-1 != index) {
				String beginning = url.substring(0, index);
				String remainder = url.substring(index);
				
				//httpRequest.setAttribute("remainder", remainder);
				
				return beginning;
			}
			return url;
		}
		
	}
	

}
