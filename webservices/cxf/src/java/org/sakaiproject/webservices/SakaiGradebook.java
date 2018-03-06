/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.gradebook.GradingScale;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;

/**
 * Created by: Diego del Blanco, SCRIBA
 * Date: 9/18/15
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class SakaiGradebook extends AbstractWebService {

    protected GradebookFrameworkService gradebookFrameworkService;

    @WebMethod(exclude = true)
    public void setGradebookFrameworkService(GradebookFrameworkService gradebookFrameworkService) {
        this.gradebookFrameworkService = gradebookFrameworkService;
    }


    @WebMethod
    @Path("/createOrUpdateGradeScale")
    @Produces("text/plain")
    @GET
    public String createOrUpdateGradeScale(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "scaleUuid", partName = "scaleUuid") @QueryParam("scaleUuid") String scaleUuid,
            @WebParam(name = "scaleName", partName = "scaleName") @QueryParam("scaleName") String scaleName,
            @WebParam(name = "grades", partName = "grades") @QueryParam("grades") String[] grades,
            @WebParam(name = "percents", partName = "percents") @QueryParam("percents") String[] percents,
            @WebParam(name = "updateOld", partName = "updateOld") @QueryParam("updateOld") boolean updateOld,
            @WebParam(name = "updateOnlyNotCustomized", partName = "updateOnlyNotCustomized") @QueryParam("updateOnlyNotCustomized") boolean updateOnlyNotCustomized) {

        Session session = establishSession(sessionid);
        Map defaultBottomPercentsOld = new HashMap(); //stores the old default values, to check if a gradeSet is customized or not.

        if (!securityService.isSuperUser()) {
            log.warn("NonSuperUser trying to change Gradebook Scales: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to change Gradebook Scales: " + session.getUserId());
        }

        try {
            boolean isUpdate = false;

            //In the case it is called as a restful service we need to read correctly the parameters
            if (percents[0].endsWith("}") && percents[0].startsWith("{") && grades[0].endsWith("}") && grades[0].startsWith("{")) {
                grades = grades[0].substring(1, grades[0].length() - 1).split(",");
                percents = percents[0].substring(1, percents[0].length() - 1).split(",");
            }

            //Get all the scales
            List<GradingScale> gradingScales = gradebookFrameworkService.getAvailableGradingScales();

            List<GradingScaleDefinition> gradingScaleDefinitions= new ArrayList<>();
            //The API returns GradingScales, but needs GradingScalingDefinitions, so we'll need to convert them.

            //Compare the UID of the scale to check if we need to update ot create a new one
            for (Iterator iter = gradingScales.iterator(); iter.hasNext();) {
                GradingScale gradingScale = (GradingScale)iter.next();
                GradingScaleDefinition gradingScaleDefintion = gradingScale.toGradingScaleDefinition();
                if (gradingScaleDefintion.getUid().equals(scaleUuid)) {   //If it is an update...

                    //Store the previous default values to compare later if updateOnlyNotCustomized=true
                    Iterator gradesIterOld = gradingScaleDefintion.getGrades().iterator();
                    Iterator defaultBottomPercentsIterOld = gradingScaleDefintion.getDefaultBottomPercentsAsList().iterator();
                    while (gradesIterOld.hasNext() && defaultBottomPercentsIterOld.hasNext()) {
                        String gradeOld = (String)gradesIterOld.next();
                        Double valueOld = (Double)defaultBottomPercentsIterOld.next();
                        defaultBottomPercentsOld.put(gradeOld, valueOld);
                    }
                    // Set the new Values
                    gradingScaleDefintion.setName(scaleName);
                    gradingScaleDefintion.setGrades(Arrays.asList(grades));
                    gradingScaleDefintion.setDefaultBottomPercentsAsList(Arrays.asList(percents));
                    isUpdate=true;
                }
                gradingScaleDefinitions.add(gradingScaleDefintion); //always add the Scale
            }

            if (!isUpdate) {   //If it is not an update we create the scale and add it.
                GradingScaleDefinition scale = new GradingScaleDefinition();
                scale.setUid(scaleUuid);
                scale.setName(scaleName);
                scale.setGrades(Arrays.asList(grades));
                scale.setDefaultBottomPercentsAsList(Arrays.asList(percents));
                gradingScaleDefinitions.add(scale);//always add the Scale
            }

            //Finally we update all the scales
            gradebookFrameworkService.setAvailableGradingScales(gradingScaleDefinitions);

            // Now we need to add this scale to ALL the actual gradebooks if it is new,
            // and if not new, then update (if updateOld=true) the values in the ALL the old gradebooks.
            // Seems that there is not any service that returns the full list of all the gradebooks,
            // but with the siteid we can call gradebookService.isGradebookDefined(siteId)
            // and know if the site has gradebook or not, and use gradebookService.getGradebook(siteId); to
            // have all of them.

            List<String> siteList = siteService.getSiteIds(SelectionType.NON_USER, null, null, null, SortType.NONE, null);

            for (String siteId : siteList) {
                if (gradebookService.isGradebookDefined(siteId)){
                    //If the site has gradebook then we
                    Gradebook gradebook = (Gradebook)gradebookService.getGradebook(siteId);
                    String gradebookUid=gradebook.getUid();
                    Long gradebookId=gradebook.getId();

                    if (!isUpdate) { //If it is new then we need to add the scale to every actual gradebook in the list
                            gradebookFrameworkService.saveGradeMappingToGradebook(scaleUuid, gradebookUid);
                            log.debug("SakaiGradebook: Adding the new scale " + scaleUuid + " in gradebook: " + gradebook.getUid());

                    }else{ //If it is not new, then update the actual gradebooks with the new values ONLY if updateOld is true
                        if (updateOld)  {
                            Set<GradeMapping> gradeMappings =gradebookService.getGradebookGradeMappings(gradebookId);
                                for (Iterator iter2 = gradeMappings.iterator(); iter2.hasNext();) {
                                    GradeMapping gradeMapping = (GradeMapping)iter2.next();
                                    if (gradeMapping.getGradingScale().getUid().equals(scaleUuid)){
                                        if (updateOnlyNotCustomized){ //We will only update the ones that teachers have not customized
                                            if (mapsAreEqual(defaultBottomPercentsOld, gradeMapping.getGradeMap())){
                                                log.debug("SakaiGradebook:They are equals " + gradebook.getUid());
                                                gradeMapping.setDefaultValues();
                                            }else{
                                                log.debug("SakaiGradebook:They are NOT equals " + gradebook.getUid());
                                            }
                                        }else{
                                            gradeMapping.setDefaultValues();
                                        }
                                        log.debug("SakaiGradebook: updating gradeMapping" + gradeMapping.getName());
                                        gradebookFrameworkService.updateGradeMapping(gradeMapping.getId(),gradeMapping.getGradeMap());
                                    }
                                }

                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error("SakaiGradebook: createOrUpdateGradeScale: Error attempting to manage a gradescale " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();
        }
        return "success";
    }

    public boolean mapsAreEqual(Map<String, Double> mapA, Map<String, Double> mapB) {

        try{
            for (String k : mapB.keySet())
            {
                log.debug("SakaiGradebook:Comparing the default old value:" + mapA.get(k) + " with actual value:" + mapB.get(k));
                if (!(mapA.get(k).compareTo(mapB.get(k))==0)) {
                    return false;
                }
            }
            for (String y : mapA.keySet())
            {
                if (!mapB.containsKey(y)) {
                    log.debug("SakaiGradebook:Key not found comparing, so they are different:" + !mapB.containsKey(y));
                    return false;
                }
            }
        } catch (NullPointerException np) {
            return false;
        }
        return true;
    }
}
