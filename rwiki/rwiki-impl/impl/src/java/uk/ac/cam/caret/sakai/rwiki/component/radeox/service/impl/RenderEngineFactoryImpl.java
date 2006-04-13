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
package uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radeox.api.engine.RenderEngine;

import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderEngineFactory;

/**
 * @author andrew
 */
// FIXME: Component
public class RenderEngineFactoryImpl implements RenderEngineFactory
{
	private static Log log = LogFactory.getLog(RenderEngineFactoryImpl.class);

	private RWikiObjectService objectService;

	private RenderEngine deligate;

	private String externalImageLink;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RenderEngineFactory#getRenderEngine(java.lang.String,
	 *      uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer)
	 */
	public RenderEngine getRenderEngine(String space, PageLinkRenderer plr)
	{
		SpecializedRenderEngine renderEngine = new SpecializedRenderEngine(
				deligate, objectService, plr, space, externalImageLink);
		return renderEngine;
	}

	public RWikiObjectService getObjectService()
	{
		return objectService;
	}

	public void setObjectService(RWikiObjectService objectService)
	{
		this.objectService = objectService;
	}

	public RenderEngine getRenderEngine()
	{
		return deligate;
	}

	public void setRenderEngine(RenderEngine deligate)
	{
		this.deligate = deligate;
	}

	public String getExternalImageLink()
	{
		return externalImageLink;
	}

	public void setExternalImageLink(String externalImageLink)
	{
		this.externalImageLink = externalImageLink;
	}

}
