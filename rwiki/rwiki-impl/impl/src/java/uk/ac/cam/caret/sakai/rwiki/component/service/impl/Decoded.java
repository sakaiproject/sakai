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

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import org.sakaiproject.entity.api.Entity;

/**
 * Represents a decoded entity url
 * 
 * @author ieb
 */
public class Decoded
{
	public Decoded()
	{

	}

	public Decoded(String context, String container, String page, String version)
	{
		this.context = context;
		this.container = container;
		this.page = page;
		this.version = version;

	}

	private String id = null;

	private String context = null;

	private String container = null;

	private String page = null;

	private String version = null;

	/**
	 * @return Returns the container.
	 */
	public String getContainer()
	{
		return container;
	}

	/**
	 * @param container
	 *        The container to set.
	 */
	public void setContainer(String container)
	{
		this.container = container;
	}

	/**
	 * @return Returns the context.
	 */
	public String getContext()
	{
		return context;
	}

	/**
	 * @param context
	 *        The context to set.
	 */
	public void setContext(String context)
	{
		this.context = context;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId()
	{
		if (id == null)
		{
			if (container.equals("/"))
			{
				id = context + container + page;
			}
			else
			{
				id = context + container + Entity.SEPARATOR + page;
			}
		}
		return id;
	}

	/**
	 * @return Returns the page.
	 */
	public String getPage()
	{
		return page;
	}

	/**
	 * @param page
	 *        The page to set.
	 */
	public void setPage(String page)
	{
		id = null;
		this.page = page;
	}

	/**
	 * @return Returns the version.
	 */
	public String getVersion()
	{
		id = null;
		return version;
	}

	/**
	 * @param version
	 *        The version to set.
	 */
	public void setVersion(String version)
	{
		id = null;
		this.version = version;
	}

	public boolean equals(Object o)
	{
		if (o instanceof Decoded)
		{
			Decoded d = (Decoded) o;
			String dcontainer = d.getContainer();
			String dcontext = d.getContext();
			String dpage = d.getPage();
			String dversion = d.getVersion();
			if (container == null && dcontainer != null) return false;
			if (context == null && dcontext != null) return false;
			if (page == null && dpage != null) return false;
			if (version == null && dversion != null) return false;
			if (container != null && !container.equals(dcontainer))
				return false;
			if (context != null && !context.equals(dcontext)) return false;
			if (page != null && !page.equals(dpage)) return false;
			if (version != null && !version.equals(dversion)) return false;
			return true;
		}
		return false;
	}

	public String toString()
	{
		return getId() + "," + version;
	}

}
