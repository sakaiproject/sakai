package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class ReportURLLinkReponse   {
  
  private String url = null;
  private String contextID = null;
  private String assignmentID = null;
  private String userID = null;
  private String externalContentID = null;

  
  /**
   * The url to retrieve the report
   **/
  public ReportURLLinkReponse url(String url) {
    this.url = url;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "The url to retrieve the report")
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  
  /**
   * Context ID.
   **/
  public ReportURLLinkReponse contextID(String contextID) {
    this.contextID = contextID;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Context ID.")
  @JsonProperty("contextID")
  public String getContextID() {
    return contextID;
  }
  public void setContextID(String contextID) {
    this.contextID = contextID;
  }

  
  /**
   * Assignment ID.
   **/
  public ReportURLLinkReponse assignmentID(String assignmentID) {
    this.assignmentID = assignmentID;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Assignment ID.")
  @JsonProperty("assignmentID")
  public String getAssignmentID() {
    return assignmentID;
  }
  public void setAssignmentID(String assignmentID) {
    this.assignmentID = assignmentID;
  }

  
  /**
   * User ID.
   **/
  public ReportURLLinkReponse userID(String userID) {
    this.userID = userID;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "User ID.")
  @JsonProperty("userID")
  public String getUserID() {
    return userID;
  }
  public void setUserID(String userID) {
    this.userID = userID;
  }

  
  /**
   * external Content ID
   **/
  public ReportURLLinkReponse externalContentID(String externalContentID) {
    this.externalContentID = externalContentID;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "external Content ID")
  @JsonProperty("externalContentID")
  public String getExternalContentID() {
    return externalContentID;
  }
  public void setExternalContentID(String externalContentID) {
    this.externalContentID = externalContentID;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportURLLinkReponse reportURLLinkReponse = (ReportURLLinkReponse) o;
    return Objects.equals(this.url, reportURLLinkReponse.url) &&
        Objects.equals(this.contextID, reportURLLinkReponse.contextID) &&
        Objects.equals(this.assignmentID, reportURLLinkReponse.assignmentID) &&
        Objects.equals(this.userID, reportURLLinkReponse.userID) &&
        Objects.equals(this.externalContentID, reportURLLinkReponse.externalContentID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, contextID, assignmentID, userID, externalContentID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportURLLinkReponse {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    contextID: ").append(toIndentedString(contextID)).append("\n");
    sb.append("    assignmentID: ").append(toIndentedString(assignmentID)).append("\n");
    sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
    sb.append("    externalContentID: ").append(toIndentedString(externalContentID)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

