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
import org.radeox.api.engine.context.RenderContext;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderContextFactory;

/**
 * @author andrew
 */
// FIXME: Component
public class RenderContextFactoryImpl implements RenderContextFactory
{
	private static Log log = LogFactory.getLog(RenderContextFactoryImpl.class);

	private RWikiObjectService objectService;

	private RWikiSecurityService securityService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.radeox.RenderContextFactory#getRenderContext(uk.ac.cam.caret.sakai.rwiki.tool.service.RWikiObject,
	 *      org.radeox.api.engine.RenderEngine)
	 */
	public RenderContext getRenderContext(RWikiObject rwo,
			RenderEngine renderEngine)
	{

		SpecializedRenderContext context = new SpecializedRenderContext(rwo,
				objectService, securityService);
		context.setRenderEngine(renderEngine);
		return context;
	}

	public RWikiObjectService getObjectService()
	{
		return objectService;
	}

	public void setObjectService(RWikiObjectService objectService)
	{
		this.objectService = objectService;
	}

	public RWikiSecurityService getSecurityService()
	{
		return securityService;
	}

	public void setSecurityService(RWikiSecurityService securityService)
	{
		this.securityService = securityService;
	}

}
