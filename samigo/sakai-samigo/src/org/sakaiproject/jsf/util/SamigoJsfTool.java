/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.jsf.util;

import org.sakaiproject.jsf.util.JsfTool;

/**
 * <p>
 * Customized JsfTool for Samigo - just to workaround the fact that Samigo
 * has the JSF URL mapping "*.faces" hard-coded in several places.  If
 * all instances of "*.faces" were changed to "*.jsf", this class could be removed.
 * </p>
 * 
 */
public class SamigoJsfTool extends JsfTool
{

	/**
	 * Recognize a path that is a resource request. It must have an "extension", i.e. a dot followed by characters that do not include a slash.
	 * 
	 * @param path
	 *        The path to check
	 * @return true if the path is a resource request, false if not.
	 */
	protected boolean isResourceRequest(String path)
	{
		// we need some path
		if ((path == null) || (path.length() == 0)) return false;

		// we need a last dot
		int pos = path.lastIndexOf(".");
		if (pos == -1) return false;

		// we need that last dot to be the end of the path, not burried in the path somewhere (i.e. no more slashes after the last dot)
		String ext = path.substring(pos);
		if (ext.indexOf("/") != -1) return false;

		// these are JSF pages, not resources		
		// THESE LINES OF CODE IS THE ONLY REASON THIS CLASS EXISTS!
		if (ext.equals(".jsf")) return false;
		if (ext.equals(".faces")) return false;
		if (path.startsWith("/faces/")) return false;
		
		// ok, it's a resource request
		return true;
	}
}



