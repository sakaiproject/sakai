/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.tool.bean;

import java.io.Serializable;


public class MenuBean implements Serializable {
	private static final long	serialVersionUID	= 2249380244869839983L;
	
	public String processSiteList(){
		return "sitelist";
	}
	
	public String processServerWide(){
		return "serverwide";
	}
	
	public String processOverview(){
		return "overview";
	}

	public String processReports(){
		return "reports";
	}
	
	public String processPrefs(){
		return "prefs";
	}
	
	public String processPermissions(){
		return "perms";
	}
}
