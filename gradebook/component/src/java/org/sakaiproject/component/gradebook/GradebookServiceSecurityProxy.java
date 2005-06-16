/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.component.gradebook;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * Delegates gradbook service methods to the GradebookServiceHibernateImpl while
 * adding security checking.
 *
 * @author <a href="jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradebookServiceSecurityProxy implements GradebookService {
    private static final Log log = LogFactory.getLog(GradebookServiceSecurityProxy.class);

    private GradebookServiceHibernateImpl serviceImpl;

    private void authorize(String gradebookUid) {
        // If this user has instructor role in this context, allow the creation
        String userUid = serviceImpl.getAuthn().getUserUid(null);
        if(!serviceImpl.getAuthz().getGradebookRole(gradebookUid, userUid).isInstructor()) {
        	if (log.isInfoEnabled()) log.info("User uid=" + userUid + " is not authorized to maintain gradebook uid=" + gradebookUid);
            throw new SecurityException("You do not have permission to perform this operation");
        }
    }

    public void addGradebook(String uid, String name) {
        if(log.isDebugEnabled()) log.debug("checking authorization for addGradebook(" + uid + ")");
        authorize(uid);
        serviceImpl.addGradebook(uid, name);
    }

    public boolean gradebookExists(String gradebookUid) {
        if(log.isDebugEnabled()) log.debug("not checking authorization for gradebookExists(). This is a publicly available method.");
        return serviceImpl.gradebookExists(gradebookUid);

    }

    public void addExternalAssessment(String gradebookUid, String externalId,
            String externalUrl, String title, long points, Date dueDate,
            String externalServiceDescription)
            throws GradebookNotFoundException,
            ConflictingAssignmentNameException, ConflictingExternalIdException {
        if(log.isDebugEnabled()) log.debug("checking authorization for addExternalAssesment()");
        authorize(gradebookUid);
        serviceImpl.addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription);
    }

    public void updateExternalAssessment(String gradebookUid,
            String externalId, String externalUrl, String title, long points,
            Date dueDate) throws GradebookNotFoundException,
            AssessmentNotFoundException, ConflictingAssignmentNameException {
        if(log.isDebugEnabled()) log.debug("checking authorization for updateExternalAssessment()");
        authorize(gradebookUid);
        serviceImpl.updateExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate);
    }

    public void removeExternalAssessment(String gradebookUid, String externalId)
            throws GradebookNotFoundException, AssessmentNotFoundException {
        if(log.isDebugEnabled()) log.debug("checking authorization for removeExternalAssessment()");
        authorize(gradebookUid);
        serviceImpl.removeExternalAssessment(gradebookUid, externalId);
    }

    public void updateExternalAssessmentScore(String gradebookUid,
            String externalId, String studentId, Double points)
            throws GradebookNotFoundException, AssessmentNotFoundException {
        if(log.isDebugEnabled()) log.debug("checking authorization for updateExternalAssessmentScore()");

        try {
            authorize(gradebookUid);
        } catch (SecurityException se) {
            // If the current user id matches the id of the score to update, let this through the security check
            // Otherwise, throw the exception.
            if(!serviceImpl.getAuthn().getUserUid(null).equals(studentId)) {
                log.warn("User " + serviceImpl.getAuthn().getUserUid(null) + " illegally attempted to update a score for user " + studentId);
                throw se;
            }
        }
        
        serviceImpl.updateExternalAssessmentScore(gradebookUid, externalId, studentId, points);
    }

    public void setServiceImpl(GradebookServiceHibernateImpl serviceImpl) {
        this.serviceImpl = serviceImpl;
    }
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
