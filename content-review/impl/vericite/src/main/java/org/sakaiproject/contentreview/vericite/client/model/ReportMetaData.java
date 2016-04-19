package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.sakaiproject.contentreview.vericite.client.model.ExternalContentData;
import java.util.ArrayList;
import java.util.List;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class ReportMetaData   {
  
  private String userFirstName = null;
  private String userLastName = null;
  private String userEmail = null;
  private String userRole = null;
  private String assignmentTitle = null;
  private String contextTitle = null;
  private List<ExternalContentData> externalContentData = new ArrayList<ExternalContentData>();

  
  /**
   * Users First Name
   **/
  public ReportMetaData userFirstName(String userFirstName) {
    this.userFirstName = userFirstName;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Users First Name")
  @JsonProperty("userFirstName")
  public String getUserFirstName() {
    return userFirstName;
  }
  public void setUserFirstName(String userFirstName) {
    this.userFirstName = userFirstName;
  }

  
  /**
   * Users Last Name
   **/
  public ReportMetaData userLastName(String userLastName) {
    this.userLastName = userLastName;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Users Last Name")
  @JsonProperty("userLastName")
  public String getUserLastName() {
    return userLastName;
  }
  public void setUserLastName(String userLastName) {
    this.userLastName = userLastName;
  }

  
  /**
   * Users Email
   **/
  public ReportMetaData userEmail(String userEmail) {
    this.userEmail = userEmail;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Users Email")
  @JsonProperty("userEmail")
  public String getUserEmail() {
    return userEmail;
  }
  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  
  /**
   * User Role
   **/
  public ReportMetaData userRole(String userRole) {
    this.userRole = userRole;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "User Role")
  @JsonProperty("userRole")
  public String getUserRole() {
    return userRole;
  }
  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }

  
  /**
   * Title of Assignment
   **/
  public ReportMetaData assignmentTitle(String assignmentTitle) {
    this.assignmentTitle = assignmentTitle;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Title of Assignment")
  @JsonProperty("assignmentTitle")
  public String getAssignmentTitle() {
    return assignmentTitle;
  }
  public void setAssignmentTitle(String assignmentTitle) {
    this.assignmentTitle = assignmentTitle;
  }

  
  /**
   * Title of Context
   **/
  public ReportMetaData contextTitle(String contextTitle) {
    this.contextTitle = contextTitle;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Title of Context")
  @JsonProperty("contextTitle")
  public String getContextTitle() {
    return contextTitle;
  }
  public void setContextTitle(String contextTitle) {
    this.contextTitle = contextTitle;
  }

  
  /**
   **/
  public ReportMetaData externalContentData(List<ExternalContentData> externalContentData) {
    this.externalContentData = externalContentData;
    return this;
  }
  
  @ApiModelProperty(example = "null", required = true, value = "")
  @JsonProperty("externalContentData")
  public List<ExternalContentData> getExternalContentData() {
    return externalContentData;
  }
  public void setExternalContentData(List<ExternalContentData> externalContentData) {
    this.externalContentData = externalContentData;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportMetaData reportMetaData = (ReportMetaData) o;
    return Objects.equals(this.userFirstName, reportMetaData.userFirstName) &&
        Objects.equals(this.userLastName, reportMetaData.userLastName) &&
        Objects.equals(this.userEmail, reportMetaData.userEmail) &&
        Objects.equals(this.userRole, reportMetaData.userRole) &&
        Objects.equals(this.assignmentTitle, reportMetaData.assignmentTitle) &&
        Objects.equals(this.contextTitle, reportMetaData.contextTitle) &&
        Objects.equals(this.externalContentData, reportMetaData.externalContentData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userFirstName, userLastName, userEmail, userRole, assignmentTitle, contextTitle, externalContentData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportMetaData {\n");
    
    sb.append("    userFirstName: ").append(toIndentedString(userFirstName)).append("\n");
    sb.append("    userLastName: ").append(toIndentedString(userLastName)).append("\n");
    sb.append("    userEmail: ").append(toIndentedString(userEmail)).append("\n");
    sb.append("    userRole: ").append(toIndentedString(userRole)).append("\n");
    sb.append("    assignmentTitle: ").append(toIndentedString(assignmentTitle)).append("\n");
    sb.append("    contextTitle: ").append(toIndentedString(contextTitle)).append("\n");
    sb.append("    externalContentData: ").append(toIndentedString(externalContentData)).append("\n");
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

