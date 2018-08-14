package org.sakaiproject.sitestats.api.event.detailed.web;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for a web content tool
 * @author plukasew
 */
public class WebData implements ResolvedEventData
{
	public final String pageName;
	public final Optional<String> toolName;
	public final String url;

	/**
	 * Constructor
	 * @param pageName the name of the page
	 * @param toolName the name of the tool (may be null if not set)
	 * @param url the url the web content tool points to
	 */
	public WebData(String pageName, String toolName, String url)
	{
		this.pageName = pageName;
		this.toolName = Optional.ofNullable(StringUtils.trimToNull(toolName));
		this.url = url;
	}
}
