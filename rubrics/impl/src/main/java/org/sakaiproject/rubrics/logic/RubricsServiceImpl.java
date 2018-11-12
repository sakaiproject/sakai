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

package org.sakaiproject.rubrics.logic;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import javax.annotation.PostConstruct;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.rubrics.logic.model.Criterion;
import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.sakaiproject.rubrics.logic.model.Rating;
import org.sakaiproject.rubrics.logic.model.Rubric;
import org.sakaiproject.rubrics.logic.model.ToolItemRubricAssociation;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Implementation of {@link RubricsService}
 */
@Slf4j
public class RubricsServiceImpl implements RubricsService, EntityProducer, EntityTransferrer, EntityTransferrerRefMigrator {

    protected static ResourceLoader rb = new ResourceLoader("rubricsMessages");

    private static final String RBCS_PERMISSIONS_EVALUATOR = "rubrics.evaluator";
    private static final String RBCS_PERMISSIONS_EDITOR = "rubrics.editor";
    private static final String RBCS_PERMISSIONS_EVALUEE = "rubrics.evaluee";
    private static final String RBCS_PERMISSIONS_ASSOCIATOR = "rubrics.associator";
    private static final String RBCS_PERMISSIONS_SUPERUSER = "rubrics.superuser";

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

    @Getter @Setter
    private EntityManager entityManager;

    public void init() {
        if (StringUtils.isBlank(serverConfigurationService.getString(RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY))) {
            throw new IllegalStateException(String.format("Required deployment property %s was not found. Please " +
                    "configure it in sakai.properties.", RUBRICS_TOKEN_SIGNING_SHARED_SECRET_PROPERTY));
        }

        // register as an entity producer
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

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

    private String getCurrentSiteId(String method){
        if(toolManager.getCurrentPlacement() == null){
            log.error("{}: current placement is null, Rubrics token won't be generated.", method);
            return null;
        }
        return toolManager.getCurrentPlacement().getContext();
    }

    public String generateJsonWebToken(String tool) {
        return generateJsonWebToken(tool, getCurrentSiteId("generateJsonWebToken"));
    }

    public String generateJsonWebToken(String tool, String siteId) {

        String token = null;
        String userId = sessionManager.getCurrentSessionUserId();

        try {
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
    public void saveRubricAssociation(String tool, String id, Map<String,String> params) {

        String associationHref = null;
        String created = "";
        String owner = "";
        String ownerType = "";
        String creatorId = "";
        Map <String,Boolean> oldParams = new HashMap<>();

        try {
            Optional<Resource<ToolItemRubricAssociation>> associationResource = getRubricAssociationResource(tool, id, null);
            if (associationResource.isPresent()) {
                associationHref = associationResource.get().getLink(Link.REL_SELF).getHref();
                ToolItemRubricAssociation association = associationResource.get().getContent();
                created = association.getMetadata().getCreated().toString();
                owner = association.getMetadata().getOwnerId();
                ownerType = association.getMetadata().getOwnerType();
                creatorId = association.getMetadata().getCreatorId();
                oldParams = association.getParameters();
            }

            //we will create a new one or update if the parameter rbcs-associate is true
            String nowTime = LocalDateTime.now().toString();
            if (params.get(RubricsConstants.RBCS_ASSOCIATE).equals("1")) {

                if (associationHref ==null) {  // create a new one.
                    String input = "{\"toolId\" : \""+tool+"\",\"itemId\" : \"" + id + "\",\"rubricId\" : " + params.get(RubricsConstants.RBCS_LIST) + ",\"metadata\" : {\"created\" : \"" + nowTime + /*"\",\"modified\" : \"" + nowTime +*/ "\",\"ownerId\" : \"" + userDirectoryService.getCurrentUser().getId() + "\"},\"parameters\" : {" + setConfigurationParameters(params,oldParams) + "}}";
                    log.debug("New association " + input);
                    String query = serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX + "rubric-associations/";
                    String resultPost = postRubricResource(query, input, tool, null);
                    log.debug("resultPost: " +  resultPost);
                } else {
                    String input = "{\"toolId\" : \""+tool+"\",\"itemId\" : \"" + id + "\",\"rubricId\" : " + params.get(RubricsConstants.RBCS_LIST) + ",\"metadata\" : {\"created\" : \"" + created + /*"\",\"modified\" : \"" + nowTime +*/ "\",\"ownerId\" : \"" + owner +
					"\",\"ownerType\" : \"" + ownerType + "\",\"creatorId\" : \"" + creatorId + "\"},\"parameters\" : {" + setConfigurationParameters(params,oldParams) + "}}";
                    log.debug("Existing association update" + input);
                    String resultPut = putRubricResource(associationHref, input, tool);
                    //update the actual one.
                    log.debug("resultPUT: " +  resultPut);
                }
            } else {
                // We delete the association
                if (associationHref !=null) {
                    deleteRubricAssociation(associationHref,tool,null);
                }
            }

        } catch (Exception e) {
            //TODO If we have an error here, maybe we should return say something to the user
        }
    }

    public void saveRubricEvaluation(String toolId, String associatedItemId, String evaluatedItemId,
            String evaluatedItemOwnerId, String evaluatorId, Map<String,String> params) {

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
                log.info("Exception on saveRubricEvaluation: " + ex.getMessage());
                //no previous evaluation
            }

            // Get the actual association (necessary to get the rubrics association resource for persisting the evaluation)
            Resource<ToolItemRubricAssociation> rubricToolItemAssociationResource = getRubricAssociationResource(
                    toolId, associatedItemId, null).get();

            String criterionJsonData = createCriterionJsonPayload(associatedItemId, evaluatedItemId, params, rubricToolItemAssociationResource);

            if (existingEvaluation == null) { // Create a new one

                String input = String.format("{ \"evaluatorId\" : \"%s\",\"evaluatedItemId\" : \"%s\", " +
                        "\"evaluatedItemOwnerId\" : \"%s\"," +
                        "\"overallComment\" : \"%s\", " +
                        "\"toolItemRubricAssociation\" : \"%s\", " +
                        "\"criterionOutcomes\" : [ %s ] " +
                        "}", evaluatorId, evaluatedItemId, evaluatedItemOwnerId, "",
                        rubricToolItemAssociationResource.getLink(Link.REL_SELF).getHref(), criterionJsonData);

                String requestUri = serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX + "evaluations/";
                String resultPost = postRubricResource(requestUri, input, toolId, null);
                log.debug("resultPost: " +  resultPost);

            } else { // Update existing evaluation

                // Resource IDs return as null when using Spring HATEOAS due to https://github.com/spring-projects/spring-hateoas/issues/67
                // so ID is not added and the resource URI is where it is derived from.
                String input = String.format("{ \"evaluatorId\" : \"%s\",\"evaluatedItemId\" : \"%s\", " +
                        "\"evaluatedItemOwnerId\" : \"%s\", \"overallComment\" : \"%s\", \"toolItemRubricAssociation\" : \"%s\", \"criterionOutcomes\" : [ %s ], " + 
                        "\"metadata\" : {\"created\" : \"%s\", \"ownerId\" : \"%s\", \"ownerType\" : \"%s\", \"creatorId\" : \"%s\"} }", evaluatorId, evaluatedItemId, evaluatedItemOwnerId, existingEvaluation.getOverallComment(),
                        rubricToolItemAssociationResource.getLink(Link.REL_SELF).getHref(), criterionJsonData, existingEvaluation.getMetadata().getCreated(), existingEvaluation.getMetadata().getOwnerId(), 
						existingEvaluation.getMetadata().getOwnerType(), existingEvaluation.getMetadata().getCreatorId());

                String resultPut = putRubricResource(evaluationUri, input, toolId);
                //lets update the actual one.
                log.debug("resultPUT: " +  resultPut);
            }

        } catch (Exception e) {
            //TODO If we have an error here, maybe we should return say something to the user
            log.error("Error in SaveRubricEvaluation" + e.getMessage());
        }

    }

    private String createCriterionJsonPayload(String associatedItemId, String evaluatedItemId,
                                              Map<String,String> formPostParameters,
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

            final String selectedRatingPoints = criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-"+ associatedItemId + "-criterion");

            if (StringUtils.isNotBlank(criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-" + associatedItemId + "-criterion-override"))) {
                pointsAdjusted = true;
                points = criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-" + associatedItemId + "-criterion-override");
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
                    criterionData.getKey(), points, StringEscapeUtils.escapeJson(criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-"+ associatedItemId + "-criterion-comment")),
                    pointsAdjusted, selectedRatingId);
        }

        return criterionJsonData;
    }

    private Map<String, Map<String, String>> extractCriterionDataFromParams(Map<String, String> params) {

        Map<String, Map<String, String>> criterionDataMap = new HashMap<>();

        for (Map.Entry<String, String> param : params.entrySet()) {
            String possibleCriterionId = StringUtils.substringAfterLast(param.getKey(), "-");
            String criterionDataLabel = StringUtils.substringBeforeLast(param.getKey(), "-");
            if (StringUtils.isNumeric(possibleCriterionId)) {
                if (!criterionDataMap.containsKey(possibleCriterionId)) {
                    criterionDataMap.put(possibleCriterionId, new HashMap<>());
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

    private String setConfigurationParameters(Map<String,String> params, Map<String,Boolean> oldParams ){
        String configuration = "";
        Boolean noFirst=false;
        //Get the parameters
        Iterator it2 = params.keySet().iterator();
        while (it2.hasNext()) {
            String name = it2.next().toString();
            if (name.startsWith(RubricsConstants.RBCS_CONFIG)) {
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
            if (!(params.containsKey(RubricsConstants.RBCS_CONFIG + name))) {
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

    private String setConfigurationParametersForDuplication(Map<String,Boolean> params ){
        String configuration = "";
        Boolean noFirst=false;
        for (Map.Entry<String, Boolean> parameter : params.entrySet()) {
            if (noFirst) {
                configuration = configuration + " , ";
            }
            configuration = configuration + "\"" + parameter.getKey() + "\" : " + parameter.getValue();
            noFirst = true;
        }
        log.debug(configuration);
        return configuration;
    }

    public Optional<ToolItemRubricAssociation> getRubricAssociation(String toolId, String associatedToolItemId) throws Exception {
        return getRubricAssociation(toolId, associatedToolItemId, getCurrentSiteId("getRubricAssociation"));
    }

	/**
     * Returns the ToolItemRubricAssociation for the given tool and associated item ID, wrapped as an Optional.
     * @param toolId the tool id, something like "sakai.assignment"
     * @param associatedToolItemId the id of the associated element within the tool
     * @return
     */
    public Optional<ToolItemRubricAssociation> getRubricAssociation(String toolId, String associatedToolItemId, String siteId) throws Exception {

        Optional<ToolItemRubricAssociation> association = Optional.empty();

        Optional<Resource<ToolItemRubricAssociation>> associationResource = getRubricAssociationResource(toolId, associatedToolItemId, siteId);
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
    protected Optional<Resource<ToolItemRubricAssociation>> getRubricAssociationResource(String toolId, String associatedToolItemId, String siteId) throws Exception {

        TypeReferences.ResourcesType<Resource<ToolItemRubricAssociation>> resourceParameterizedTypeReference =
                new TypeReferences.ResourcesType<Resource<ToolItemRubricAssociation>>() {};

        URI apiBaseUrl = new URI(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX);
        Traverson traverson = new Traverson(apiBaseUrl, MediaTypes.HAL_JSON);

        Traverson.TraversalBuilder builder = traverson.follow("rubric-associations", "search",
                "by-tool-item-ids");

        HttpHeaders headers = new HttpHeaders();
        if(siteId != null) {
            headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(toolId, siteId)));
        } else {
            headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(toolId)));
        }
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

    public String getRubricEvaluationObjectId(String associationId, String userId, String toolId) {
        try {
            URI apiBaseUrl = new URI(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX);
            Traverson traverson = new Traverson(apiBaseUrl, MediaTypes.HAL_JSON);

            Traverson.TraversalBuilder builder = traverson.follow("evaluations", "search", "by-association-and-user");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(toolId)));
            builder.withHeaders(headers);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("associationId", associationId);
            parameters.put("userId", userId);

            String response = builder.withTemplateParameters(parameters).toObject(String.class);
            if (StringUtils.isNotEmpty(response)){
                return response.replace("\"", "");
            }
        } catch (Exception e) {
            log.warn("Error {} while getting a rubric evaluation in assignment {} for user {}", e.getMessage(), associationId, userId);
        }
        return null;
    }

    /**
     * Posts the rubric association.
     * @param json The json to post.
     * @return
     */
    private String postRubricResource(String targetUri, String json, String toolId, String siteId) throws IOException {
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
            if(siteId != null){
                conn.setRequestProperty("Authorization", "Bearer " + generateJsonWebToken(toolId, siteId));
            } else {
                conn.setRequestProperty("Authorization", "Bearer " + generateJsonWebToken(toolId));
            }
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

    private void deleteRubricAssociation(String query, String toolId, String siteId) throws IOException {

        HttpURLConnection conn = null;
        try{
            URL url = new URL(query);
            String cookie = "JSESSIONID=" + sessionManager.getCurrentSession().getId() + System.getProperty(SERVER_ID_PROPERTY);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cookie", cookie );
            conn.setRequestProperty("Authorization", "Bearer " + generateJsonWebToken(toolId, siteId));

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

    private void deleteRubric(String query, String toolId, String siteId) throws IOException {
        HttpURLConnection conn = null;
        try{
            URL url = new URL(query);
            String cookie = "JSESSIONID=" + sessionManager.getCurrentSession().getId() + System.getProperty(SERVER_ID_PROPERTY);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cookie", cookie );
            conn.setRequestProperty("Authorization", "Bearer " + generateJsonWebToken(toolId, siteId));
            if (conn.getResponseCode() != 204) {
                throw new RuntimeException("Failed deleteRubric : HTTP error code : " + conn.getResponseCode());
            }
        } catch (MalformedURLException e) {
            log.warn("Error deleting a rubric " + e.getMessage());
        } catch (IOException e) {
            log.warn("Error deleting a rubric" + e.getMessage());
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

    public String getCurrentSessionId() {
        return sessionManager.getCurrentSession().getId();
    }

    //SAK-40154 related functions

    @Override
    public void transferCopyEntities(String fromContext, String toContext, List<String> ids, boolean cleanup) {
        transferCopyEntitiesRefMigrator(fromContext, toContext, ids, cleanup);
    }

    @Override
    public void transferCopyEntities(String fromContext, String toContext, List<String> ids) {
        transferCopyEntitiesRefMigrator(fromContext, toContext, ids);
    }

    @Override
    public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids) {
        Map<String, String> transversalMap = new HashMap<>();
        try {
            TypeReferences.ResourcesType<Resource<Rubric>> resourceParameterizedTypeReference = new TypeReferences.ResourcesType<Resource<Rubric>>() {};
            URI apiBaseUrl = new URI(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX);
            Traverson traverson = new Traverson(apiBaseUrl, MediaTypes.HAL_JSON);
            Traverson.TraversalBuilder builder = traverson.follow("rubrics", "search", "rubrics-from-site");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(RubricsConstants.RBCS_TOOL, toContext)));
            builder.withHeaders(headers);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("siteId", fromContext);

            Resources<Resource<Rubric>> rubricResources = builder.withTemplateParameters(parameters).toObject(resourceParameterizedTypeReference);
            for (Resource<Rubric> rubricResource : rubricResources) {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers2 = new HttpHeaders();
                headers2.setContentType(MediaType.APPLICATION_JSON);
                headers2.add("Authorization", String.format("Bearer %s", generateJsonWebToken(RubricsConstants.RBCS_TOOL, toContext)));
                HttpEntity<?> requestEntity = new HttpEntity<>(headers2);
                ResponseEntity<Rubric> rubricEntity = restTemplate.exchange(rubricResource.getLink(Link.REL_SELF).getHref()+"?projection=inlineRubric", HttpMethod.GET, requestEntity, Rubric.class);
                Rubric rEntity = rubricEntity.getBody();
                String newId = cloneRubricToSite(String.valueOf(rEntity.getId()), toContext);
                String oldId = String.valueOf(rEntity.getId());
                transversalMap.put(RubricsConstants.RBCS_PREFIX+oldId, RubricsConstants.RBCS_PREFIX+newId);
            }
        } catch (Exception ex){
            log.info("Exception on duplicateRubricsFromSite: " + ex.getMessage());
        }
        return transversalMap;
    }

    @Override
    public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List<String> ids, boolean cleanup) {
        if(cleanup){
            try {
                TypeReferences.ResourcesType<Resource<Rubric>> resourceParameterizedTypeReference = new TypeReferences.ResourcesType<Resource<Rubric>>() {};
                URI apiBaseUrl = new URI(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX);
                Traverson traverson = new Traverson(apiBaseUrl, MediaTypes.HAL_JSON);
                Traverson.TraversalBuilder builder = traverson.follow("rubrics", "search", "rubrics-from-site");

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(RubricsConstants.RBCS_TOOL, toContext)));
                builder.withHeaders(headers);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("siteId", toContext);
                Resources<Resource<Rubric>> rubricResources = builder.withTemplateParameters(parameters).toObject(resourceParameterizedTypeReference);
                for (Resource<Rubric> rubricResource : rubricResources) {
                    String [] rubricSplitted = rubricResource.getLink(Link.REL_SELF).getHref().split("/");
                    Collection<Resource<ToolItemRubricAssociation>> assocs = getRubricAssociationByRubric(rubricSplitted[rubricSplitted.length-1],toContext);
                    for(Resource<ToolItemRubricAssociation> associationResource : assocs){
                        String associationHref = associationResource.getLink(Link.REL_SELF).getHref();
                        deleteRubricAssociation(associationHref, RubricsConstants.RBCS_TOOL, toContext);
                    }
                    deleteRubric(rubricResource.getLink(Link.REL_SELF).getHref(), RubricsConstants.RBCS_TOOL, toContext);
                }
            } catch(Exception e){
                log.error("Rubrics - transferCopyEntitiesRefMigrator : error trying to delete rubric -> {}" , e.getMessage());
            }
        }
        return transferCopyEntitiesRefMigrator(fromContext, toContext, ids);
    }

    private String cloneRubricToSite(String rubricId, String toSite){
        try{
            String url = serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX + "rubrics/clone";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(RubricsConstants.RBCS_TOOL, toSite)));
            headers.add("x-copy-source", rubricId);
            headers.add("site", toSite);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Rubric> rubricEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Rubric.class);
            Rubric rub = rubricEntity.getBody();
            return String.valueOf(rub.getId());
        } catch(Exception e){
            log.error("Exception when cloning rubric {} to site {} : {}", rubricId, toSite, e.getMessage());
        }
        return null;
    }

    @Override
    public String[] myToolIds() {
        return new String[] { RubricsConstants.RBCS_TOOL };
    }

    @Override
    public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {
        if (transversalMap != null && !transversalMap.isEmpty()) {
            for (Map.Entry<String, String> entry : transversalMap.entrySet()) {
                String key = entry.getKey();
                //1 get all the rubrics from map
                if (key.startsWith(RubricsConstants.RBCS_PREFIX)) {
                    try {
                        //2 for each, get its associations
                        Collection<Resource<ToolItemRubricAssociation>> assocs = getRubricAssociationByRubric(key.substring(RubricsConstants.RBCS_PREFIX.length()),toContext);

                        //2b get association params
                        for(Resource<ToolItemRubricAssociation> associationResource : assocs){
                            ToolItemRubricAssociation association = associationResource.getContent();
                            Map<String,Boolean> originalParams = association.getParameters();

                            String newRubricId = entry.getValue().substring(RubricsConstants.RBCS_PREFIX.length());
                            String tool = association.getToolId();
                            String itemId = association.getItemId();
                            String newItemId = null;
                            //3 association type
                            if(RubricsConstants.RBCS_TOOL_ASSIGNMENT.equals(tool)){
                                //3a if assignments
                                log.debug("Handling Rubrics association transfer for Assignment entry " + itemId);
                                if(transversalMap.get("assignment/"+itemId) != null){
                                    newItemId = transversalMap.get("assignment/"+itemId).substring("assignment/".length());
                                }
                            } else if(RubricsConstants.RBCS_TOOL_SAMIGO.equals(tool)){
                                //3b if samigo
                                if(itemId.startsWith(RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX)){
                                    log.debug("Skipping published item {}", itemId);
                                }
                                log.debug("Handling Rubrics association transfer for Samigo entry " + itemId);
                                if(transversalMap.get("sam_item/"+itemId) != null){
                                    newItemId = transversalMap.get("sam_item/"+itemId).substring("sam_item/".length());
                                }
                            } else if(RubricsConstants.RBCS_TOOL_FORUMS.equals(tool)){
                                //3c if forums
                                newItemId = itemId.substring(0, 4);
                                String strippedId = itemId.substring(4);//every forum prefix have this size
                                log.debug("Handling Rubrics association transfer for Forums entry " + strippedId);
                                if(RubricsConstants.RBCS_FORUM_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum/"+strippedId) != null){
                                    newItemId += transversalMap.get("forum/"+strippedId).substring("forum/".length());
                                } else if(RubricsConstants.RBCS_TOPIC_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum_topic/"+strippedId) != null){
                                    newItemId += transversalMap.get("forum_topic/"+strippedId).substring("forum_topic/".length());
                                } else {
                                    log.debug("Not found updated id for item {}", itemId);
                                }
                            } else if(RubricsConstants.RBCS_TOOL_GRADEBOOKNG.equals(tool)){
                                //3d if gradebook
                                log.debug("Handling Rubrics association transfer for Gradebook entry " + itemId);
                                if(transversalMap.get("gb/"+itemId) != null){
                                    newItemId = transversalMap.get("gb/"+itemId).substring("gb/".length());
                                }
                            } else {
                                log.warn("Unhandled tool for Rubrics transfer between sites");
                            }

                            //4 save new association
                            if(newItemId != null){
                                try {
                                    String input = "{\"toolId\" : \""+tool+"\",\"itemId\" : \"" + newItemId + "\",\"rubricId\" : " + newRubricId + ",\"parameters\" : {" + setConfigurationParametersForDuplication(originalParams) + "}}";
                                    log.debug("New association " + input);
                                    String query = serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX + "rubric-associations/";
                                    String resultPost = postRubricResource(query, input, tool, toContext);
                                    log.debug("resultPost: " +  resultPost);
                                } catch(Exception exc){
                                    log.error("Error while trying to save new association with item it {} : {}", newItemId, exc.getMessage());
                                }
                            }
                        }
                    } catch(Exception ex){
                        log.error("Error while trying to update association for Rubric {} : {}", key, ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public HttpAccess getHttpAccess(){
        return null;
    }

    @Override
    public Collection<String> getEntityAuthzGroups(Reference reference, String userId) {
        return null;
    }

    @Override
    public String getEntityUrl(Reference reference) {
        return getEntity(reference).getUrl();
    }

    @Override
    public Entity getEntity(Reference reference) {
        return null;
    }

    @Override
    public ResourceProperties getEntityResourceProperties(Reference ref) {
       return null;
    }
   
    @Override
    public String getEntityDescription(Reference ref) {
       return null;
    }
   
    @Override
    public boolean parseEntityReference(String reference, Reference ref) {
        return true;
    }

    @Override
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
       return null;
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        return null;
    }

    @Override
    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String getLabel() {
        return "rubric";
    }

    protected Collection<Resource<ToolItemRubricAssociation>> getRubricAssociationByRubric(String rubricId, String toSite) throws Exception {
        TypeReferences.ResourcesType<Resource<ToolItemRubricAssociation>> resourceParameterizedTypeReference = new TypeReferences.ResourcesType<Resource<ToolItemRubricAssociation>>() {};

        URI apiBaseUrl = new URI(serverConfigurationService.getServerUrl() + RBCS_SERVICE_URL_PREFIX);
        Traverson traverson = new Traverson(apiBaseUrl, MediaTypes.HAL_JSON);
        Traverson.TraversalBuilder builder = traverson.follow("rubric-associations", "search", "by-rubric-id");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", generateJsonWebToken(RubricsConstants.RBCS_TOOL, toSite)));
        builder.withHeaders(headers);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("rubricId", Long.valueOf(rubricId));
        Resources<Resource<ToolItemRubricAssociation>> associationResources = builder.withTemplateParameters(parameters).toObject(resourceParameterizedTypeReference);

        return associationResources.getContent();
    }

}
