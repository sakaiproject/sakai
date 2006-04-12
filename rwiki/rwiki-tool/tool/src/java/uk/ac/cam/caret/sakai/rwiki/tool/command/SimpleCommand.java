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
package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;

/**
 * The command that simply dispatches to the set servletPath.
 * 
 * @author andrew
 */
//FIXME: Tool

public class SimpleCommand implements HttpCommand {

	private String servletPath;
	
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// So far we need do nothing except dispatch
		RequestDispatcher rd = request.getRequestDispatcher(servletPath);
		rd.forward(request, response);
		
	}

    /**
     * @return the path to the jsp file
     */
	public String getServletPath() {
		return servletPath;
	}

    /**
     * 
     * @param servletPath the path to the jsp file
     */
	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

}
