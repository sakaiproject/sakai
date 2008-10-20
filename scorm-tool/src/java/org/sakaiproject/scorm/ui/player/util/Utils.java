package org.sakaiproject.scorm.ui.player.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.Strings;

public final class Utils {

	public final static String generateUrl(IBehavior behavior, RequestListenerInterface rlix, 
			Component component, boolean isRelative) {
		
		if (component == null)
			throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
		
		final RequestListenerInterface rli = IBehaviorListener.INTERFACE;
		
		String relativePagePath = component.urlFor(behavior, rli).toString();
		
		String url = null;
		
		if (!isRelative) {
			WebRequest webRequest = (WebRequest)component.getRequest();
			HttpServletRequest servletRequest = webRequest.getHttpServletRequest();
			//url.append(servletRequest.getContextPath()).append("/");
			//String requestUrl = servletRequest.getRequestURL().toString();
			//url = RequestUtils.toAbsolutePath(requestUrl, relativePagePath);
			String contextPath = servletRequest.getContextPath();
			String relativePath = relativePagePath.replaceAll("\\.\\.\\/", "");
			url = new StringBuilder(contextPath).append("/").append(relativePath).toString();
		} else {
			url = relativePagePath;
		}
		
		return url;
	}
	
	
	static String removeDoubleDots(String path)
	{
		List newcomponents = new ArrayList(Arrays.asList(path.split("/")));

		for (int i = 0; i < newcomponents.size(); i++)
		{
			if (i < newcomponents.size() - 1)
			{
				// Verify for a ".." component at next iteration
				if (((String)newcomponents.get(i)).length() > 0 &&
					newcomponents.get(i + 1).equals(".."))
				{
					newcomponents.remove(i);
					newcomponents.remove(i);
					i = i - 2;
					if (i < -1)
						i = -1;
				}
			}
		}
		String newpath = Strings.join("/", (String[])newcomponents.toArray(new String[0]));
		if (path.endsWith("/"))
			return newpath + "/";
		return newpath;
	}
	
}
