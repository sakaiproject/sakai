package org.sakaiproject.portal.render.api;
/**
 * Results rendered from the portlet must impliment this interface
 * @author ieb
 *
 */
public interface RenderResult {
	/**
	 * get the portlet title
	 * @return
	 * @throws ToolRenderException
	 */
	String getTitle() throws ToolRenderException;
	/**
	 * get the portlet content
	 * @return
	 * @throws ToolRenderException
	 */
	String getContent() throws ToolRenderException;
}
