/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.api.ToolRenderService;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * @author andrew
 */
// FIXME: Component WITH FIXES, remove deps on page link render impl
public class ToolRenderServiceImpl implements ToolRenderService
{
	private static Log log = LogFactory.getLog(ToolRenderServiceImpl.class);

	private RenderService renderService = null;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		renderService = (RenderService) load(cm, RenderService.class.getName());
		log.error("Render Service is "+renderService);
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#publicRenderPage(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiObject,
	 *      java.lang.String)
	 */
	public String renderPublicPage(RWikiObject rwo, boolean withBreadCrumbs)
	{
		return renderPublicPage(rwo, rwo.getRealm(), withBreadCrumbs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#publicRenderPage(uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiObject,
	 *      java.lang.String, java.lang.String)
	 */
	public String renderPublicPage(RWikiObject rwo, String defaultRealm,
			boolean withBreadCrumbs)
	{
		// SAK-2519
		String localSpace = NameHelper.localizeSpace(rwo.getName(),
				defaultRealm);
		PublicPageLinkRendererImpl plr = new PublicPageLinkRendererImpl(
				localSpace, defaultRealm, withBreadCrumbs);
		return renderService.renderPage(rwo, localSpace, plr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#renderPage(uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject)
	 */
	public String renderPage(RWikiObject rwo)
	{
		return renderPage(rwo, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderService#renderPage(uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject,
	 *      java.lang.String)
	 */
	public String renderPage(RWikiObject rwo, String defaultRealm)
	{
		// SAK-2519
		String localSpace = NameHelper.localizeSpace(rwo.getName(),
				defaultRealm);
		PageLinkRendererImpl plr = new PageLinkRendererImpl(localSpace,
				defaultRealm);
		return renderService.renderPage(rwo, localSpace, plr);
	}

	public String renderPage(RWikiObject rwo, boolean cachable)
	{
		// SAK-2519
		String localSpace = NameHelper.localizeSpace(rwo.getName(), rwo
				.getRealm());
		PageLinkRendererImpl plr = new PageLinkRendererImpl(localSpace, rwo
				.getRealm());
		plr.setUseCache(cachable);
		plr.setCachable(cachable);
		return renderService.renderPage(rwo, localSpace, plr);
	}

	

}
