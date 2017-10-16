/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.logic.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

import lombok.Getter;
import lombok.Setter;

import net.sf.ehcache.Cache;

import org.apache.log4j.Logger;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.rubrics.model.Criterion;
import org.sakaiproject.rubrics.model.Evaluation;
import org.sakaiproject.rubrics.model.Rating;
import org.sakaiproject.rubrics.model.Rubric;
import org.sakaiproject.rubrics.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.logic.api.RubricsService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import javax.annotation.PostConstruct;

/**
 * Implementation of {@link RubricsService}
 */
@Slf4j
public class RubricsServiceImpl implements RubricsService {

    protected static ResourceLoader rb = new ResourceLoader("rubricsMessages");

    private static final String RBCS_PERMISSIONS_EVALUATOR = "rbcs.evaluator";
    private static final String RBCS_PERMISSIONS_EDITOR = "rbcs.editor";
    private static final String RBCS_PERMISSIONS_EVALUEE = "rbcs.evaluee";
    private static final String RBCS_PERMISSIONS_ASSOCIATOR = "rbcs.associator";
    private static final String RBCS_PERMISSIONS_SUPERUSER = "rbcs.superuser";

    private static final String RBCS_SERVICE_URL_PREFIX = "/rubrics-service/rest/";

    private static final String RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY = "rubrics.integration.token-secret";
    private static final String SITE_CONTEXT_TYPE = "site";

    private static final String SERVER_ID_PROPERTY = "sakai.serverId";

    private static final String JWT_ISSUER = "sakai";
    private static final String JWT_AUDIENCE = "rubrics";
    private static final String JWT_CUSTOM_CLAIM_TOOL_ID = "toolId";
    private static final String JWT_CUSTOM_CLAIM_SESSION_ID = "sessionId";
    private static final String JWT_CUSTOM_CLAIM_ROLES = "roles";
    private static final String JWT_CUSTOM_CLAIM_CONTEXT_ID = "contextId";
    private static final String JWT_CUSTOM_CLAIM_CONTEXT_TYPE = "contextType";

    @Getter
    @Setter
    private ToolManager toolManager;

    @Getter @Setter
    private SessionManager sessionManager;

    @Getter @Setter
    private UserDirectoryService userDirectoryService;

    @Getter @Setter
    private SecurityService securityService;

    @Getter @Setter
    private EventTrackingService eventTrackingService;

    @Getter @Setter
    private ServerConfigurationService serverConfigurationService;

    @Getter @Setter
    private SiteService siteService;

    @Getter @Setter
    private FunctionManager functionManager;

    @Getter @Setter
    private AuthzGroupService authzGroupService;

    @Setter
    private Cache cache;


    public void init() {
        if (StringUtils.isBlank(serverConfigurationService.getString(RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY))) {
            throw new IllegalStateException(String.format("Required deployment property %s was not found. Please " +
                    "configure it in sakai.properties.", RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY));
        }

        setFunction(RBCS_PERMISSIONS_EVALUATOR);
        setFunction(RBCS_PERMISSIONS_EDITOR);
        setFunction(RBCS_PERMISSIONS_EVALUEE);
        setFunction(RBCS_PERMISSIONS_ASSOCIATOR);
    }

    /**
     * {@inheritDoc}
     */
    private void setFunction(String function) {
        functionManager.registerFunction(function);
    }

    @PostConstruct
    private void postConstruct() {

    }

    public String generateJsonWebToken(String tool) {

        String token = null;

        String userId = sessionManager.getCurrentSessionUserId();

        try {

            String siteId = toolManager.getCurrentPlacement().getContext();

            DateTime now = DateTime.now();

            JWTCreator.Builder jwtBuilder = JWT.create();
            jwtBuilder.withIssuer(JWT_ISSUER)
                    .withAudience(JWT_AUDIENCE)
                    .withSubject(userId)
                    .withClaim(JWT_CUSTOM_CLAIM_TOOL_ID, tool)
                    .withClaim(JWT_CUSTOM_CLAIM_SESSION_ID, sessionManager.getCurrentSession().getId())
                    .withIssuedAt(now.toDate());
            int sessionTimeoutInSeconds = sessionManager.getCurrentSession().getMaxInactiveInterval();
            if (sessionTimeoutInSeconds > 0) {
                jwtBuilder.withExpiresAt(now.plusSeconds(sessionTimeoutInSeconds).toDate());
            } else {
                // if Sakai is configured for sessions to never timeout (negative value), we will set 30 minutes for
                // tokens - the rubrics service will check Sakai session validity if it receives an expired token.
                jwtBuilder.withExpiresAt(now.plusMinutes(30).toDate());
            }

            if (securityService.isSuperUser()) {
                jwtBuilder.withArrayClaim(JWT_CUSTOM_CLAIM_ROLES,
                        new String[]{ RBCS_PERMISSIONS_EDITOR,
                                RBCS_PERMISSIONS_ASSOCIATOR,
                                RBCS_PERMISSIONS_EVALUATOR,
                                RBCS_PERMISSIONS_EVALUEE,
                                RBCS_PERMISSIONS_SUPERUSER });

            } else {

                List<String> roles = new ArrayList<>();
                if (authzGroupService.isAllowed(userId, RBCS_PERMISSIONS_EDITOR, "/site/" + siteId)) {
                    roles.add(RBCS_PERMISSIONS_EDITOR);
                }
                if (authzGroupService.isAllowed(userId, RBCS_PERMISSIONS_ASSOCIATOR, "/site/" + siteId)) {
                    roles.add(RBCS_PERMISSIONS_ASSOCIATOR);
                }
                if (authzGroupService.isAllowed(userId, RBCS_PERMISSIONS_EVALUATOR, "/site/" + siteId)) {
                    roles.add(RBCS_PERMISSIONS_EVALUATOR);
                }
                if (authzGroupService.isAllowed(userId, RBCS_PERMISSIONS_EVALUEE, "/site/" + siteId)) {
                    roles.add(RBCS_PERMISSIONS_EVALUEE);
                }
                jwtBuilder.withArrayClaim(JWT_CUSTOM_CLAIM_ROLES, roles.toArray(new String[]{}));
            }
            jwtBuilder.withClaim(JWT_CUSTOM_CLAIM_CONTEXT_ID, siteId);
            jwtBuilder.withClaim(JWT_CUSTOM_CLAIM_CONTEXT_TYPE, SITE_CONTEXT_TYPE);
            token =  jwtBuilder.sign(Algorithm.HMAC256(serverConfigurationService.getString(
                    RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY)));

        } catch (UnsupportedEncodingException e){
            throw new RuntimeException(String.format("An error occurred while generating a JSON Web Token to " +
                    "authorize communication with the Rubrics service. Please verify the %s property is " +
                    "defined in the sakai.properties file.", RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY), e);
        }

        return token;
    }


    public boolean hasAssociatedRubric(String tool, String id ){

        boolean exists = false;

        try {

            Optional<ToolItemRubricAssociation> association = getRubricAssociation(tool, id);
            exists = association.isPresent();

        }catch (Exception e){
            log.debug("No previous association or rubrics not answering", e);
        }

        return exists;
    }


    /**
     * call the rubrics-service to save the binding between assignment and rubric
     * @param params A hashmap with all the rbcs params comming from the component. The tool should generate it.
     * @param tool the tool id, something like "sakai.assignment"
     * @param id the id of the element to
     */

    public void saveRubricAssociation(String tool, String id, HashMap<String,String> params) {

        String associationHref = null;
        String created = "";
        String owner = "";
        Map <String,Boolean> oldParams = new HashMap<>();

        try {
            Optional<Resource<ToolItemRubricAssociation>> associationResource = getRubricAssociationResource(tool, id);
            if (associationResource.isPresent()) {
                associationHref = associationResource.get().getLink(Link.REL_SELF).getHref();
                ToolItemRubricAssociation association = associationResource.get().getContent();
                created = association.getMetadata().getCreated().toString();
                owner = association.getMetadata().getOwner();
                oldParams = association.getParameters();
            }

            //we will create a new one or update if the parameter rbcs-associate is true
            String nowTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            if (params.get("rbcs-associate").equals("1")) {

                if (associationHref ==null) {  // create a new one.
                    String input = "{\"toolId\" : \""+tool+"\",\"itemId\" : \"" + id + "\",\"rubricId\" : " + params.get("rbcs-rubricslist") + ",\"metadata\" : {\"created\" : \"" + nowTime + "\",\"modified\" : \"" + nowTime + "\",\"owner\" : \"" + userDirectoryService.getCurrentUser().getId() + "\"},\"parameters\" : {" + setConfigurationParameters(params,oldParams) + "}}";
                    log.debug("New association " + input);
                    String query = serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX + "rubric-associations/";
                    String resultPost = postRubricResource(query, input, tool);
                    log.debug("resultPost: " +  resultPost);
                }else{
                    String input = "{\"toolId\" : \""+tool+"\",\"itemId\" : \"" + id + "\",\"rubricId\" : " + params.get("rbcs-rubricslist") + ",\"metadata\" : {\"created\" : \"" + created + "\",\"modified\" : \"" + nowTime + "\",\"owner\" : \"" + owner + "\"},\"parameters\" : {" + setConfigurationParameters(params,oldParams) + "}}";
                    log.debug("Existing association update" + input);
                    String resultPut = putRubricResource(associationHref, input, tool);
                    //update the actual one.
                    log.debug("resultPUT: " +  resultPut);
                }
            } else {
                // We delete the association
                if (associationHref !=null) {
                    deleteRubricAssociation(associationHref,tool);
                }
            }

        } catch (Exception e) {
            //TODO If we have an error here, maybe we should return say something to the user
        }
    }

    public void saveRubricEvaluation(String toolId, String associatedItemId, String evaluatedItemId,
            String evaluatedItemOwnerId, String evaluatorId, HashMap<String,String> params) {

        String evaluationUri = null;
        String created = "";
        String owner = "";

        try {

            // Check for an existing evaluation
            Evaluation existingEvaluation = null;
            String rubricEvaluationId = null;

            try {

                TypeReferences.ResourcesType<Resource<Evaluation>> resourceParameterizedTypeReference =
                        new TypeReferences.ResourcesType<Resource<Evaluation>>() {};

                URI apiBaseUrl = new URI(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX);
                Traverson traverson = new Traverson(apiBaseUrl, MediaTypes.HAL_JSON);

                Traverson.TraversalBuilder builder = traverson.follow("evaluations", "search",
                        "by-tool-item-and-associated-item-and-evaluated-item-ids");

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(toolId)));
                builder.withHeaders(headers);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("toolId", toolId);
                parameters.put("itemId", associatedItemId);
                parameters.put("evaluatedItemId", evaluatedItemId);
                parameters.put("evaluatorId", evaluatorId);

                Resources<Resource<Evaluation>> evaluationResources = builder.withTemplateParameters(parameters).toObject(
                        resourceParameterizedTypeReference);

                // Should only be one matching this search criterion
                if (evaluationResources.getContent().size() > 1) {
                    throw new IllegalStateException(String.format("Number of evaluation resources greater than one for request: %s",
                            evaluationResources.getLink(Link.REL_SELF).toString()));
                }
                for (Resource<Evaluation> evaluationResource : evaluationResources) {
                    existingEvaluation = evaluationResource.getContent();
                    evaluationUri = evaluationResource.getLink(Link.REL_SELF).getHref();
                }

            } catch (Exception ex){
                log.info(ex.getMessage());
                //no previous evaluation
            }

            // Get the actual association (necessary to get the rubrics association resource for persisting
            // the evaluation)
            Resource<ToolItemRubricAssociation> rubricToolItemAssociationResource = getRubricAssociationResource(
                    toolId, associatedItemId).get();

            String criterionJsonData = createCriterionJsonPayload(params, rubricToolItemAssociationResource);

            if (existingEvaluation == null) { // Create a new one

                String input = String.format("{ \"evaluatorId\" : \"%s\",\"evaluatedItemId\" : \"%s\", " +
                        "\"evaluatedItemOwnerId\" : \"%s\"," +
                        "\"overallComment\" : \"%s\", " +
                        "\"toolItemRubricAssociation\" : \"%s\", " +
                        "\"criterionOutcomes\" : [ %s ] " +
                        "}", evaluatorId, evaluatedItemId, evaluatedItemOwnerId, "",
                        rubricToolItemAssociationResource.getLink(Link.REL_SELF).getHref(), criterionJsonData);

                String requestUri = serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX + "evaluations/";
                String resultPost = postRubricResource(requestUri, input, toolId);
                log.debug("resultPost: " +  resultPost);

            } else { // Update existing evaluation

                // Resource IDs return as null when using Spring HATEOAS due to https://github.com/spring-projects/spring-hateoas/issues/67
                // so ID is not added and the resource URI is where it is derived from.

                String input = String.format("{ \"evaluatorId\" : \"%s\",\"evaluatedItemId\" : \"%s\", " +
                        "\"evaluatedItemOwnerId\" : \"%s\", \"overallComment\" : \"%s\", \"toolItemRubricAssociation\" : \"%s\", " +
                        "\"criterionOutcomes\" : [ %s ] }", evaluatorId, evaluatedItemId, evaluatedItemOwnerId, "",
                        rubricToolItemAssociationResource.getLink(Link.REL_SELF).getHref(), criterionJsonData);

                String resultPut = putRubricResource(evaluationUri, input, toolId);
                //lets update the actual one.
                log.debug("resultPUT: " +  resultPut);
            }

        } catch (Exception e) {
            //TODO If we have an error here, maybe we should return say something to the user
            log.error("Error in SaveRubricEvaluation" + e.getMessage());
        }

    }

    private String createCriterionJsonPayload(HashMap<String,String> formPostParameters,
                                              Resource<ToolItemRubricAssociation> association) throws Exception {

        Map<String, Map<String, String>> criterionDataMap = extractCriterionDataFromParams(formPostParameters);

        String criterionJsonData = "";
        int index = 0;
        boolean pointsAdjusted = false;
        String points = null;
        String selectedRatingId = null;

        String inlineRubricUri = String.format("%s?%s", association.getLink("rubric").getHref(), "projection=inlineRubric");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(association.getContent().getToolId())));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Rubric> rubricEntity = restTemplate.exchange(inlineRubricUri, HttpMethod.GET, requestEntity, Rubric.class);

        Map<String, Criterion> criterions =  new HashMap<>();
        for (Criterion criterion : rubricEntity.getBody().getCriterions()) {
            criterions.put(String.valueOf(criterion.getId()), criterion);
        }

        for (Map.Entry<String, Map<String, String>> criterionData : criterionDataMap.entrySet()) {
            if (index > 0) {
                criterionJsonData += ", ";
            }
            index++;

            final String selectedRatingPoints = criterionData.getValue().get("rbcs-criterion");

            if (StringUtils.isNotBlank(criterionData.getValue().get("rbcs-criterion-override"))) {
                pointsAdjusted = true;
                points = criterionData.getValue().get("rbcs-criterion-override");
            } else {
                pointsAdjusted = false;
                points = selectedRatingPoints;
            }

            Criterion criterion = criterions.get(criterionData.getKey());
            Optional<Rating> rating = criterion.getRatings().stream().filter(c -> String.valueOf(c.getPoints()).equals(selectedRatingPoints)).findFirst();

            if (rating.isPresent()) {
                selectedRatingId =  String.valueOf(rating.get().getId());
            }

            if (StringUtils.isEmpty(points)){
                points = "0";
            }

            criterionJsonData += String.format("{ \"criterionId\" : \"%s\", \"points\" : \"%s\", " +
                            "\"comments\" : \"%s\", \"pointsAdjusted\" : %b, \"selectedRatingId\" : \"%s\"  }",
                    criterionData.getKey(), points, StringEscapeUtils.escapeJson(criterionData.getValue().get("rbcs-criterion-comment")),
                    pointsAdjusted, selectedRatingId);
        }

        return criterionJsonData;
    }

    private Map<String, Map<String, String>> extractCriterionDataFromParams(HashMap<String, String> params) {

        Map<String, Map<String, String>> criterionDataMap = new HashMap();

        for (Map.Entry<String, String> param : params.entrySet()) {
            String possibleCriterionId = StringUtils.substringAfterLast(param.getKey(), "-");
            String criterionDataLabel = StringUtils.substringBeforeLast(param.getKey(), "-");
            if (StringUtils.isNumeric(possibleCriterionId)) {
                if (!criterionDataMap.containsKey(possibleCriterionId)) {
                    criterionDataMap.put(possibleCriterionId, new HashMap());
                }
                criterionDataMap.get(possibleCriterionId).put(criterionDataLabel, param.getValue());
            }
        }

        return criterionDataMap;
    }

    /**
     * Prepare the association params in json format
     * @param params the full list of rubrics params coming from the component
     * @return
     */

    private String setConfigurationParameters(HashMap<String,String> params, Map<String,Boolean> oldParams ){
        String configuration = "";
        Boolean noFirst=false;
        //Get the parameters
        Iterator it2 = params.keySet().iterator();
        while (it2.hasNext()) {
            String name = it2.next().toString();
            if (name.startsWith("rbcs-config-")) {
                if (noFirst) {
                    configuration = configuration + " , ";
                }
                String value = "false";
                if ((params.get(name) != null) && (params.get(name).equals("1"))) {
                    value = "true";
                }
                configuration = configuration + "\"" + name.substring(12) + "\" : " + value;
                noFirst = true;
            }
        }
        Iterator itOld = oldParams.keySet().iterator();
        while (itOld.hasNext()) {
            String name = itOld.next().toString();
            if (!(params.containsKey("rbcs-config-" + name))) {
                if (noFirst) {
                    configuration = configuration + " , ";
                }
                configuration = configuration + "\"" + name + "\" : false";
                noFirst = true;
            }
        }
        log.debug(configuration);
        return configuration;
    }

    /**
     * Returns the ToolItemRubricAssociation for the given tool and associated item ID, wrapped as an Optional.
     * @param toolId the tool id, something like "sakai.assignment"
     * @param associatedToolItemId the id of the associated element within the tool
     * @return
     */
    public Optional<ToolItemRubricAssociation> getRubricAssociation(String toolId, String associatedToolItemId) throws Exception {

        Optional<ToolItemRubricAssociation> association = Optional.empty();

        Optional<Resource<ToolItemRubricAssociation>> associationResource = getRubricAssociationResource(toolId, associatedToolItemId);
        if (associationResource.isPresent()) {
            association = Optional.of(associationResource.get().getContent());
        }
        return association;
    }


    /**
     * Returns the ToolItemRubricAssociation resource for the given tool and associated item ID, wrapped as an Optional.
     * @param toolId the tool id, something like "sakai.assignment"
     * @param associatedToolItemId the id of the associated element within the tool
     * @return
     */
    protected Optional<Resource<ToolItemRubricAssociation>> getRubricAssociationResource(String toolId, String associatedToolItemId) throws Exception {

        TypeReferences.ResourcesType<Resource<ToolItemRubricAssociation>> resourceParameterizedTypeReference =
                new TypeReferences.ResourcesType<Resource<ToolItemRubricAssociation>>() {};

        URI apiBaseUrl = new URI(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX);
        Traverson traverson = new Traverson(apiBaseUrl, MediaTypes.HAL_JSON);

        Traverson.TraversalBuilder builder = traverson.follow("rubric-associations", "search",
                "by-tool-item-ids");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(toolId)));
        builder.withHeaders(headers);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("toolId", toolId);
        parameters.put("itemId", associatedToolItemId);

        Resources<Resource<ToolItemRubricAssociation>> associationResources = builder.withTemplateParameters(
                parameters).toObject(resourceParameterizedTypeReference);

        // Should only be one matching this search criterion
        if (associationResources.getContent().size() > 1) {
            throw new IllegalStateException(String.format(
                    "Number of rubric association resources greater than one for request: %s",
                    associationResources.getLink(Link.REL_SELF).toString()));
        }

        Optional<Resource<ToolItemRubricAssociation>> associationResource = associationResources.getContent().stream().findFirst();

        return associationResource;
    }

    //TODO generate a public String postRubricAssociation(String tool, String id, HashMap<String,String> params)

    /**
     * Posts the rubric association.
     * @param json The json to post.
     * @return
     */
    private String postRubricResource(String targetUri, String json, String toolId) throws IOException {
        log.debug(String.format("Post to URI '%s' body:", targetUri, json));

        HttpURLConnection conn = null;
        try {
            URL url = new URL(targetUri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/hal+json; charset=UTF-8");
            conn.setRequestProperty("Content-Length", "" + json.length());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            String cookie = "JSESSIONID=" + sessionManager.getCurrentSession().getId() + "" + System.getProperty(SERVER_ID_PROPERTY);
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Authorization", "Bearer " + generateJsonWebToken(toolId));

            try(OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
                os.close();
            }

            // read the response
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            StringWriter result = new StringWriter();
            while ((output = br.readLine()) != null) {
                result.append(output + "\n");
            }

            return result.toString();

        } catch (IOException ioException) {

            log.warn(String.format("Error creating a rubric resource at %s", targetUri), ioException);
            return null;

        } finally {
            if(conn != null) {
                try{
                    conn.disconnect();
                }catch(Exception e){

                }
            }
        }
    }

    //TODO generate a public String putRubricAssociation(String tool, String id, HashMap<String,String> params)

    /**
     * Put the rubric association.
     * @param targetUri The association href.
     * @param json The json to post.
     * @return
     */
    private String putRubricResource(String targetUri,String json, String toolId) throws IOException {
        log.debug(String.format("PUT to URI '%s' body:", targetUri, json));

        HttpURLConnection conn = null;
        try {
            URL url = new URL(targetUri);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Content-Length", "" + json.length());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("PUT");
            String cookie = "JSESSIONID=" + sessionManager.getCurrentSession().getId() + "" + System.getProperty(SERVER_ID_PROPERTY);
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Authorization", "Bearer " + generateJsonWebToken(toolId));

            try(OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
                os.close();
            }

            // read the response
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            StringWriter result = new StringWriter();
            while ((output = br.readLine()) != null) {
                result.append(output + "\n");
            }

            return result.toString();

        } catch (IOException ioException) {

            log.warn(String.format("Error updating a rubric resource at %s", targetUri), ioException);
            return null;
        } finally {
            if(conn != null) {
                try{
                    conn.disconnect();
                }catch(Exception e){

                }
            }
        }
    }


    //TODO generate a public String deleteRubricAssociation(String tool, String id)

    /**
     * Delete the rubric association.
     * @param query The association href.
     * @return
     */

    private void deleteRubricAssociation(String query,String toolId) throws IOException {

        HttpURLConnection conn = null;
        try{
            URL url = new URL(query);
            String cookie = "JSESSIONID=" + sessionManager.getCurrentSession().getId() + "" + System.getProperty(SERVER_ID_PROPERTY);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cookie", cookie );
            conn.setRequestProperty("Authorization", "Bearer " + generateJsonWebToken(toolId));

            if (conn.getResponseCode() != 204) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }


        } catch (MalformedURLException e) {

            log.warn("Error deleting a rubric association " + e.getMessage());

        } catch (IOException e) {

            log.warn("Error deleting a rubric association" + e.getMessage());

        } finally {
            if(conn != null) {
                try{
                    conn.disconnect();
                }catch(Exception e){

                }
            }
        }
    }

    /**
     * Returns the JSON string for the rubric evaluation
     * @param toolId the tool id, something like "sakai.assignment"
     * @param associatedToolItemId the id of the tool item which has a rubric associated to it (e.g. assignment ID)
     * @param evaluatedItemId  the id of the tool item which is being evaluated using a rubric (e.g. assignment submission ID)
     * @return
     */
    public String getRubricEvaluation(String toolId, String associatedToolItemId, String evaluatedItemId) throws IOException {

        HttpURLConnection conn = null;
        try{
            URL url = new URL(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX + "evaluations/search/by-tool-item-and-associated-item-and-evaluated-item-ids?toolId="+toolId+"&itemId="+associatedToolItemId+"&evaluatedItemId="+evaluatedItemId);

            String cookie = "JSESSIONID=" + sessionManager.getCurrentSession().getId() + "" + System.getProperty(SERVER_ID_PROPERTY);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cookie", cookie );
            conn.setRequestProperty("Authorization", "Bearer" + generateJsonWebToken(toolId));

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            StringWriter result = new StringWriter();
            while ((output = br.readLine()) != null) {
                result.append(output + "\n");
            }

            return result.toString();

        } catch (MalformedURLException e) {

            log.warn("Error getting a rubric evaluation " + e.getMessage());
            return null;

        } catch (IOException e) {

            log.warn("Error getting a rubric evaluation" + e.getMessage());
            return null;
        } finally {
            if(conn != null) {
                try{
                    conn.disconnect();
                }catch(Exception e){

                }
            }
        }
    }


    public String generateLang(){

        StringBuilder lines = new StringBuilder();
        lines.append("var rubricsLang = {");

        Locale locale = rb.getLocale();
        Set properties = rb.keySet();
        lines.append("'" + locale.toLanguageTag() + "': {");
        Iterator keys = properties.iterator();
        while (keys.hasNext()){
            String key = keys.next().toString();
            if (keys.hasNext()) {
                lines.append("'" + key + "': '" + rb.getString(key) + "',");
            }else{
                lines.append("'" + key + "': '" + rb.getString(key) + "'");
            }
        }

        lines.append("}");
        lines.append("}");

        log.debug(lines.toString());

        return lines.toString();
    }


}
