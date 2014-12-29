package org.sakaiproject.portal.render.api;

/**
 * Results rendered from the portlet must impliment this interface
 * 
 * @author ieb
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public interface RenderResult
{

	/**
	 * get the portlet title
	 * 
	 * @return
	 * @throws ToolRenderException
	 *         if the title can not be retrieved
	 */
	String getTitle() throws ToolRenderException;

	/**
	 * get the portlet content
	 * 
	 * @return content
	 * @throws ToolRenderException
	 *         if the content can not be rendered
	 */
	String getContent() throws ToolRenderException;

	/**
	 * set the portlet content
	 */
	void setContent(String content);

	/**
	 * get the JSR168 Help Url
	 * 
	 * @return Url
	 * @throws ToolRenderException
	 *         if the content can not be rendered
	 */
	String getJSR168HelpUrl() throws ToolRenderException;

	/**
	 * get the JSR168 Edit Url
	 * 
	 * @return Url
	 * @throws ToolRenderException
	 *         if the content can not be rendered
	 */
	String getJSR168EditUrl() throws ToolRenderException;

	/**
	 * @return
	 */
	String getHead();
}
