/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
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

package org.sakaiproject.tool.section.jsf.backingbean;

/**
 * Caches whether the instructor features are enabled for the current user in
 * the current request.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class NavMenuBean extends CourseDependentBean {
	private static final long serialVersionUID = 1L;
	
	private boolean sectionTaManagementEnabled;
	private boolean sectionEnrollmentMangementEnabled;
	private boolean sectionOptionsManagementEnabled;
	private boolean sectionManagementEnabled;

	public NavMenuBean() {
		this.sectionManagementEnabled = super.isSectionManagementEnabled();
		this.sectionOptionsManagementEnabled = super.isSectionOptionsManagementEnabled();
		this.sectionEnrollmentMangementEnabled = super.isSectionEnrollmentMangementEnabled();
		this.sectionTaManagementEnabled = super.isSectionTaManagementEnabled();
	}

	public boolean isSectionEnrollmentMangementEnabled() {
		return sectionEnrollmentMangementEnabled;
	}

	public void setSectionEnrollmentMangementEnabled(
			boolean sectionEnrollmentMangementEnabled) {
		this.sectionEnrollmentMangementEnabled = sectionEnrollmentMangementEnabled;
	}

	public boolean isSectionManagementEnabled() {
		return sectionManagementEnabled;
	}

	public void setSectionManagementEnabled(boolean sectionManagementEnabled) {
		this.sectionManagementEnabled = sectionManagementEnabled;
	}

	public boolean isSectionOptionsManagementEnabled() {
		return sectionOptionsManagementEnabled;
	}

	public void setSectionOptionsManagementEnabled(
			boolean sectionOptionsManagementEnabled) {
		this.sectionOptionsManagementEnabled = sectionOptionsManagementEnabled;
	}

	public boolean isSectionTaManagementEnabled() {
		return sectionTaManagementEnabled;
	}

	public void setSectionTaManagementEnabled(boolean sectionTaManagementEnabled) {
		this.sectionTaManagementEnabled = sectionTaManagementEnabled;
	}

}
