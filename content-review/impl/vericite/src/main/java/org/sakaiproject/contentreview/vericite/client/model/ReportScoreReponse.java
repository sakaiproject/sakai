package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class ReportScoreReponse   {
  
  private String user = null;
  private String assignment = null;
  private String externalContentId = null;
  private Integer score = null;

  
  /**
   **/
  public ReportScoreReponse user(String user) {
    this.user = user;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  
  /**
   **/
  public ReportScoreReponse assignment(String assignment) {
    this.assignment = assignment;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("assignment")
  public String getAssignment() {
    return assignment;
  }
  public void setAssignment(String assignment) {
    this.assignment = assignment;
  }

  
  /**
   **/
  public ReportScoreReponse externalContentId(String externalContentId) {
    this.externalContentId = externalContentId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("externalContentId")
  public String getExternalContentId() {
    return externalContentId;
  }
  public void setExternalContentId(String externalContentId) {
    this.externalContentId = externalContentId;
  }

  
  /**
   **/
  public ReportScoreReponse score(Integer score) {
    this.score = score;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("score")
  public Integer getScore() {
    return score;
  }
  public void setScore(Integer score) {
    this.score = score;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportScoreReponse reportScoreReponse = (ReportScoreReponse) o;
    return Objects.equals(this.user, reportScoreReponse.user) &&
        Objects.equals(this.assignment, reportScoreReponse.assignment) &&
        Objects.equals(this.externalContentId, reportScoreReponse.externalContentId) &&
        Objects.equals(this.score, reportScoreReponse.score);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, assignment, externalContentId, score);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportScoreReponse {\n");
    
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    assignment: ").append(toIndentedString(assignment)).append("\n");
    sb.append("    externalContentId: ").append(toIndentedString(externalContentId)).append("\n");
    sb.append("    score: ").append(toIndentedString(score)).append("\n");
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

