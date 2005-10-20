/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;



/**
 * An implementation of standalone authorization needs based
 * on the shared Section Awareness API.
 * this is just a stub for now 
 */
public class SectionAwareServiceHelperImpl implements SectionAwareServiceHelper{
    private static final Log log = LogFactory.getLog(SectionAwareServiceHelperImpl.class);

	public boolean isUserAbleToGrade(String siteid, String userUid) {
		return true;
	}

	public boolean isUserAbleToGradeAll(String siteid, String userUid) {
		return true;
	}

	public boolean isUserAbleToGradeSection(String sectionUid, String userUid) {
		return true;
	}

	public boolean isUserAbleToEdit(String siteid, String userUid) {
		return true;
	}

	public boolean isUserGradable(String siteid, String userUid) {
		return true;
	}

	/**
	 */
	public List getAvailableEnrollments(String siteid, String userUid) {
		return new ArrayList();
	}

	public List getAvailableSections(String siteid, String userUid) {
		return new ArrayList();
	}

	private List getSectionEnrollmentsTrusted(String sectionUid, String userUid) {
		return new ArrayList();
	}

	public List getSectionEnrollments(String siteid, String sectionUid, String userUid) {
		return new ArrayList();
	}

	public List findMatchingEnrollments(String siteid, String searchString, String optionalSectionUid, String userUid) {
		return new ArrayList();
	}

        public boolean isSectionMemberInRoleStudent(String sectionId, String studentId) {
		return true;
        }

}
