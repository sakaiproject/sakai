package org.sakaiproject.contentreview.vericite.client.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.contentreview.vericite.client.ApiClient;
import org.sakaiproject.contentreview.vericite.client.ApiException;
import org.sakaiproject.contentreview.vericite.client.Configuration;
import org.sakaiproject.contentreview.vericite.client.Pair;
import org.sakaiproject.contentreview.vericite.client.model.AssignmentData;
import org.sakaiproject.contentreview.vericite.client.model.ConsumerData;
import org.sakaiproject.contentreview.vericite.client.model.ConsumerResponse;
import org.sakaiproject.contentreview.vericite.client.model.ExternalContentUploadInfo;
import org.sakaiproject.contentreview.vericite.client.model.ReportMetaData;
import org.sakaiproject.contentreview.vericite.client.model.ReportScoreReponse;
import org.sakaiproject.contentreview.vericite.client.model.ReportURLLinkReponse;

import com.sun.jersey.api.client.GenericType;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class DefaultApi {
  private ApiClient apiClient;

  public DefaultApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DefaultApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * 
   * Create/update assignment
   * @param contextID Context ID (required)
   * @param assignmentID ID of assignment (required)
   * @param consumer the consumer (required)
   * @param consumerSecret the consumer secret (required)
   * @param assignmentData  (required)
   * @return List<ExternalContentUploadInfo>
   * @throws ApiException if fails to make API call
   */
  public List<ExternalContentUploadInfo> assignmentsContextIDAssignmentIDPost(String contextID, String assignmentID, String consumer, String consumerSecret, AssignmentData assignmentData) throws ApiException {
    Object localVarPostBody = assignmentData;
    
    // verify the required parameter 'contextID' is set
    if (contextID == null) {
      throw new ApiException(400, "Missing the required parameter 'contextID' when calling assignmentsContextIDAssignmentIDPost");
    }
    
    // verify the required parameter 'assignmentID' is set
    if (assignmentID == null) {
      throw new ApiException(400, "Missing the required parameter 'assignmentID' when calling assignmentsContextIDAssignmentIDPost");
    }
    
    // verify the required parameter 'consumer' is set
    if (consumer == null) {
      throw new ApiException(400, "Missing the required parameter 'consumer' when calling assignmentsContextIDAssignmentIDPost");
    }
    
    // verify the required parameter 'consumerSecret' is set
    if (consumerSecret == null) {
      throw new ApiException(400, "Missing the required parameter 'consumerSecret' when calling assignmentsContextIDAssignmentIDPost");
    }
    
    // verify the required parameter 'assignmentData' is set
    if (assignmentData == null) {
      throw new ApiException(400, "Missing the required parameter 'assignmentData' when calling assignmentsContextIDAssignmentIDPost");
    }
    
    // create path and map variables
    String localVarPath = "/assignments/{contextID}/{assignmentID}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "contextID" + "\\}", apiClient.escapeString(contextID.toString()))
      .replaceAll("\\{" + "assignmentID" + "\\}", apiClient.escapeString(assignmentID.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    if (consumer != null)
      localVarHeaderParams.put("consumer", apiClient.parameterToString(consumer));
    if (consumerSecret != null)
      localVarHeaderParams.put("consumerSecret", apiClient.parameterToString(consumerSecret));
    

    

    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    
    GenericType<List<ExternalContentUploadInfo>> localVarReturnType = new GenericType<List<ExternalContentUploadInfo>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * 
   * Retrieves scores for the reports
   * @param contextID Context ID (required)
   * @param consumer the consumer (required)
   * @param consumerSecret the consumer secret (required)
   * @param assignmentID ID of assignment (optional)
   * @param userID ID of user (optional)
   * @param externalContentID external content id (optional)
   * @return List<ReportScoreReponse>
   * @throws ApiException if fails to make API call
   */
  public List<ReportScoreReponse> reportsScoresContextIDGet(String contextID, String consumer, String consumerSecret, String assignmentID, String userID, String externalContentID) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'contextID' is set
    if (contextID == null) {
      throw new ApiException(400, "Missing the required parameter 'contextID' when calling reportsScoresContextIDGet");
    }
    
    // verify the required parameter 'consumer' is set
    if (consumer == null) {
      throw new ApiException(400, "Missing the required parameter 'consumer' when calling reportsScoresContextIDGet");
    }
    
    // verify the required parameter 'consumerSecret' is set
    if (consumerSecret == null) {
      throw new ApiException(400, "Missing the required parameter 'consumerSecret' when calling reportsScoresContextIDGet");
    }
    
    // create path and map variables
    String localVarPath = "/reports/scores/{contextID}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "contextID" + "\\}", apiClient.escapeString(contextID.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "assignmentID", assignmentID));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userID", userID));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "externalContentID", externalContentID));
    

    if (consumer != null)
      localVarHeaderParams.put("consumer", apiClient.parameterToString(consumer));
    if (consumerSecret != null)
      localVarHeaderParams.put("consumerSecret", apiClient.parameterToString(consumerSecret));
    

    

    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    
    GenericType<List<ReportScoreReponse>> localVarReturnType = new GenericType<List<ReportScoreReponse>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * 
   * Request a file submission
   * @param contextID Context ID (required)
   * @param assignmentID ID of assignment (required)
   * @param userID ID of user (required)
   * @param consumer the consumer (required)
   * @param consumerSecret the consumer secret (required)
   * @param reportMetaData  (required)
   * @return List<ExternalContentUploadInfo>
   * @throws ApiException if fails to make API call
   */
  public List<ExternalContentUploadInfo> reportsSubmitRequestContextIDAssignmentIDUserIDPost(String contextID, String assignmentID, String userID, String consumer, String consumerSecret, ReportMetaData reportMetaData) throws ApiException {
    Object localVarPostBody = reportMetaData;
    
    // verify the required parameter 'contextID' is set
    if (contextID == null) {
      throw new ApiException(400, "Missing the required parameter 'contextID' when calling reportsSubmitRequestContextIDAssignmentIDUserIDPost");
    }
    
    // verify the required parameter 'assignmentID' is set
    if (assignmentID == null) {
      throw new ApiException(400, "Missing the required parameter 'assignmentID' when calling reportsSubmitRequestContextIDAssignmentIDUserIDPost");
    }
    
    // verify the required parameter 'userID' is set
    if (userID == null) {
      throw new ApiException(400, "Missing the required parameter 'userID' when calling reportsSubmitRequestContextIDAssignmentIDUserIDPost");
    }
    
    // verify the required parameter 'consumer' is set
    if (consumer == null) {
      throw new ApiException(400, "Missing the required parameter 'consumer' when calling reportsSubmitRequestContextIDAssignmentIDUserIDPost");
    }
    
    // verify the required parameter 'consumerSecret' is set
    if (consumerSecret == null) {
      throw new ApiException(400, "Missing the required parameter 'consumerSecret' when calling reportsSubmitRequestContextIDAssignmentIDUserIDPost");
    }
    
    // verify the required parameter 'reportMetaData' is set
    if (reportMetaData == null) {
      throw new ApiException(400, "Missing the required parameter 'reportMetaData' when calling reportsSubmitRequestContextIDAssignmentIDUserIDPost");
    }
    
    // create path and map variables
    String localVarPath = "/reports/submit/request/{contextID}/{assignmentID}/{userID}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "contextID" + "\\}", apiClient.escapeString(contextID.toString()))
      .replaceAll("\\{" + "assignmentID" + "\\}", apiClient.escapeString(assignmentID.toString()))
      .replaceAll("\\{" + "userID" + "\\}", apiClient.escapeString(userID.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    if (consumer != null)
      localVarHeaderParams.put("consumer", apiClient.parameterToString(consumer));
    if (consumerSecret != null)
      localVarHeaderParams.put("consumerSecret", apiClient.parameterToString(consumerSecret));
    

    

    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    
    GenericType<List<ExternalContentUploadInfo>> localVarReturnType = new GenericType<List<ExternalContentUploadInfo>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * 
   * Retrieves URLS for the reports
   * @param contextID Context ID (required)
   * @param assignmentIDFilter ID of assignment to filter results on (required)
   * @param consumer the consumer (required)
   * @param consumerSecret the consumer secret (required)
   * @param tokenUser ID of user who will view the report (required)
   * @param tokenUserRole role of user who will view the report (required)
   * @param userIDFilter ID of user to filter results on (optional)
   * @param externalContentIDFilter external content id to filter results on (optional)
   * @return List<ReportURLLinkReponse>
   * @throws ApiException if fails to make API call
   */
  public List<ReportURLLinkReponse> reportsUrlsContextIDGet(String contextID, String assignmentIDFilter, String consumer, String consumerSecret, String tokenUser, String tokenUserRole, String userIDFilter, String externalContentIDFilter) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'contextID' is set
    if (contextID == null) {
      throw new ApiException(400, "Missing the required parameter 'contextID' when calling reportsUrlsContextIDGet");
    }
    
    // verify the required parameter 'assignmentIDFilter' is set
    if (assignmentIDFilter == null) {
      throw new ApiException(400, "Missing the required parameter 'assignmentIDFilter' when calling reportsUrlsContextIDGet");
    }
    
    // verify the required parameter 'consumer' is set
    if (consumer == null) {
      throw new ApiException(400, "Missing the required parameter 'consumer' when calling reportsUrlsContextIDGet");
    }
    
    // verify the required parameter 'consumerSecret' is set
    if (consumerSecret == null) {
      throw new ApiException(400, "Missing the required parameter 'consumerSecret' when calling reportsUrlsContextIDGet");
    }
    
    // verify the required parameter 'tokenUser' is set
    if (tokenUser == null) {
      throw new ApiException(400, "Missing the required parameter 'tokenUser' when calling reportsUrlsContextIDGet");
    }
    
    // verify the required parameter 'tokenUserRole' is set
    if (tokenUserRole == null) {
      throw new ApiException(400, "Missing the required parameter 'tokenUserRole' when calling reportsUrlsContextIDGet");
    }
    
    // create path and map variables
    String localVarPath = "/reports/urls/{contextID}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "contextID" + "\\}", apiClient.escapeString(contextID.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "assignmentIDFilter", assignmentIDFilter));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userIDFilter", userIDFilter));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "externalContentIDFilter", externalContentIDFilter));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "tokenUser", tokenUser));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "tokenUserRole", tokenUserRole));
    

    if (consumer != null)
      localVarHeaderParams.put("consumer", apiClient.parameterToString(consumer));
    if (consumerSecret != null)
      localVarHeaderParams.put("consumerSecret", apiClient.parameterToString(consumerSecret));
    

    

    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    
    GenericType<List<ReportURLLinkReponse>> localVarReturnType = new GenericType<List<ReportURLLinkReponse>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
}
