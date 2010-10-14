package org.sakaiproject.scorm.ui.player;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sakaiproject.authz.cover.SecurityService;
import org.springframework.web.filter.GenericFilterBean;

public class ScormSecurityFilter extends GenericFilterBean {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		SecurityService.pushAdvisor(new ScormSecurityAdvisor());
		try {
			chain.doFilter(request, response);
		} finally {
			SecurityService.popAdvisor();
		}
	}

}
