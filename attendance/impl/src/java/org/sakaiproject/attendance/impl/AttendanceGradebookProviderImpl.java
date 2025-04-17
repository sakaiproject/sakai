/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.attendance.api.AttendanceGradebookProvider;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.util.AttendanceConstants;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AttendanceGradebookProvider, {@link org.sakaiproject.attendance.api.AttendanceGradebookProvider}
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class AttendanceGradebookProviderImpl implements AttendanceGradebookProvider {
    @Setter private AttendanceLogic                     attendanceLogic;
    @Setter private SakaiProxy                          sakaiProxy;
    @Setter private ToolManager                         toolManager;
    @Setter private GradingService                      gradingService;

    /**
     * {@inheritDoc}
     */
    public void init() {
        log.debug("AttendanceGradebookProviderImpl init()");
    }

    /**
     * {@inheritDoc}
     */
    public boolean create(AttendanceSite aS, String categoryId) {
        if(log.isDebugEnabled()) {
            log.debug("create Gradebook");
        }

        boolean returnVal = false;

        String siteID = aS.getSiteID();

        String appName = AttendanceConstants.SAKAI_TOOL_NAME;
        Long categoryIdNumber = null;
        if(categoryId != null){
            categoryIdNumber = Long.valueOf(categoryId);
        }
        String aSUID = getAttendanceUID(aS);
        try {
            gradingService.addExternalAssessment(siteID, aSUID, null, aS.getGradebookItemName(), aS.getMaximumGrade(), null, appName, null, false, categoryIdNumber);// add it to the gradebook

            Map<String, String> scores = attendanceLogic.getAttendanceGradeScores();
            gradingService.updateExternalAssessmentScoresString(siteID, aSUID, scores);
            returnVal = true;
        } catch (Exception e) {
            log.warn("Error creating external GB", e);
        }

        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(AttendanceSite aS) {
        if(log.isDebugEnabled()) {
            log.debug("remove GB for AS " + aS.getSiteID());
        }

        String siteID = aS.getSiteID();
        String aUID = getAttendanceUID(aS);
        if(gradingService.isExternalAssignmentDefined(siteID, aUID)) {
            try {
                gradingService.removeExternalAssignment(siteID, aUID);
                return true;
            } catch (AssessmentNotFoundException e) {
                log.warn("Attempted to remove AttendanceSite " + siteID + " from GB failed. Assessment not found", e);
                return false;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean update(AttendanceSite aS, String categoryId) {
        if(log.isDebugEnabled()) {
            log.debug("Updating GB for AS " + aS.getSiteID());
        }
        Long categoryIdNumber = null;
        if(categoryId != null){
            categoryIdNumber = Long.valueOf(categoryId);
        }
        String siteID = aS.getSiteID();
        String aUID = getAttendanceUID(aS);
        if(gradingService.isExternalAssignmentDefined(siteID, aUID)) {
        	try {
        		    gradingService.updateExternalAssessment(siteID, aUID, null, null, aS.getGradebookItemName(), categoryIdNumber, aS.getMaximumGrade(), null, false);
                    return true;
            } catch (ConflictingAssignmentNameException e) {
            	log.warn("Failed to update AttendanceSite for site " + siteID + " in Gradebook", e);
                return false;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean sendToGradebook(final AttendanceGrade aG) {
        if(aG == null) {
            return false;
        }

        final AttendanceSite aS = aG.getAttendanceSite();
        final String siteID = aS.getSiteID();
        String aSUID = getAttendanceUID(aS);
        

        final Boolean sendToGradebook = aS.getSendToGradebook();
        if(sendToGradebook != null && sendToGradebook) {
            if(isAssessmentDefined(siteID, aSUID)) {
                // exists, update current grade
                String grade = aG.getGrade() == null ? null : aG.getGrade().toString();
                gradingService.updateExternalAssessmentScore(siteID, aSUID, aG.getUserID(), grade);
                return true;
            } else {
                //does not exist, add to GB and add all grades
                return create(aS,null);
            }
        }

        return false;
    }

    public Map<String,String> getGradebookCategories(String gbUID){
        if(!doesGradebookHaveCategories(gbUID)){
            return null;
        }
        Map<String,String> categoryMap = new HashMap<>();
        List<CategoryDefinition> categories = gradingService.getCategoryDefinitions(gbUID);
        for(CategoryDefinition d: categories){
            categoryMap.put(d.getId().toString(),d.getName());
        }
        return categoryMap;
    }

    public boolean doesGradebookHaveCategories(String gbUID){
        return gradingService.isCategoriesEnabled(gbUID);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isGradebookAssignmentDefined(String gbUID, String title) {
        return gradingService.isAssignmentDefined(gbUID, title);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAssessmentDefined(String gbUID, Long aSID) {
        return isAssessmentDefined(gbUID, getAttendanceUID(aSID));
    }

    private boolean isAssessmentDefined(String gbUID, String id) {
        return gradingService.isExternalAssignmentDefined(gbUID, id);
    }

    public Long getCategoryForItem(String gbUID,Long aSID){
        Long category = null;
        try {
            category = gradingService.getExternalAssessmentCategoryId(gbUID,getAttendanceUID(aSID));
        } catch (AssessmentNotFoundException g){
            //just return Null.
        }
        return category;
    }

    //this is hacky
    private String getAttendanceUID(AttendanceSite aS) {
        return getAttendanceUID(aS.getId());
    }

    private String getAttendanceUID(Long id) {
        return AttendanceConstants.SAKAI_TOOL_NAME + "." + id.toString();
    }
}
