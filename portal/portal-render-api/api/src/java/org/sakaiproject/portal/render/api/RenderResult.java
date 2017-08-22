/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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
