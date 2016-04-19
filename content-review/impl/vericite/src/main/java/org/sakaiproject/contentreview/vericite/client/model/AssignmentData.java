package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class AssignmentData   {
  
  private String assignmentTitle = null;
  private String assignmentInstructions = null;
  private Boolean assignmentExcludeQuotes = null;
  private Long assignmentDueDate = null;
  private Integer assignmentGrade = null;
  private List<ExternalContentData> assignmentAttachmentExternalContent = new ArrayList<ExternalContentData>();

  
  /**
   * The title of the assignment
   **/
  public AssignmentData assignmentTitle(String assignmentTitle) {
    this.assignmentTitle = assignmentTitle;
    return this;
  }
  
  @ApiModelProperty(example = "null", required = true, value = "The title of the assignment")
  @JsonProperty("assignmentTitle")
  public String getAssignmentTitle() {
    return assignmentTitle;
  }
  public void setAssignmentTitle(String assignmentTitle) {
    this.assignmentTitle = assignmentTitle;
  }

  
  /**
   * Instructions for assignment
   **/
  public AssignmentData assignmentInstructions(String assignmentInstructions) {
    this.assignmentInstructions = assignmentInstructions;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Instructions for assignment")
  @JsonProperty("assignmentInstructions")
  public String getAssignmentInstructions() {
    return assignmentInstructions;
  }
  public void setAssignmentInstructions(String assignmentInstructions) {
    this.assignmentInstructions = assignmentInstructions;
  }

  
  /**
   * exclude quotes
   **/
  public AssignmentData assignmentExcludeQuotes(Boolean assignmentExcludeQuotes) {
    this.assignmentExcludeQuotes = assignmentExcludeQuotes;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "exclude quotes")
  @JsonProperty("assignmentExcludeQuotes")
  public Boolean getAssignmentExcludeQuotes() {
    return assignmentExcludeQuotes;
  }
  public void setAssignmentExcludeQuotes(Boolean assignmentExcludeQuotes) {
    this.assignmentExcludeQuotes = assignmentExcludeQuotes;
  }

  
  /**
   * Assignment due date. Pass in 0 to delete.
   **/
  public AssignmentData assignmentDueDate(Long assignmentDueDate) {
    this.assignmentDueDate = assignmentDueDate;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Assignment due date. Pass in 0 to delete.")
  @JsonProperty("assignmentDueDate")
  public Long getAssignmentDueDate() {
    return assignmentDueDate;
  }
  public void setAssignmentDueDate(Long assignmentDueDate) {
    this.assignmentDueDate = assignmentDueDate;
  }

  
  /**
   * Assignment grade. Pass in 0 to delete.
   **/
  public AssignmentData assignmentGrade(Integer assignmentGrade) {
    this.assignmentGrade = assignmentGrade;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Assignment grade. Pass in 0 to delete.")
  @JsonProperty("assignmentGrade")
  public Integer getAssignmentGrade() {
    return assignmentGrade;
  }
  public void setAssignmentGrade(Integer assignmentGrade) {
    this.assignmentGrade = assignmentGrade;
  }

  
  /**
   **/
  public AssignmentData assignmentAttachmentExternalContent(List<ExternalContentData> assignmentAttachmentExternalContent) {
    this.assignmentAttachmentExternalContent = assignmentAttachmentExternalContent;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "")
  @JsonProperty("assignmentAttachmentExternalContent")
  public List<ExternalContentData> getAssignmentAttachmentExternalContent() {
    return assignmentAttachmentExternalContent;
  }
  public void setAssignmentAttachmentExternalContent(List<ExternalContentData> assignmentAttachmentExternalContent) {
    this.assignmentAttachmentExternalContent = assignmentAttachmentExternalContent;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssignmentData assignmentData = (AssignmentData) o;
    return Objects.equals(this.assignmentTitle, assignmentData.assignmentTitle) &&
        Objects.equals(this.assignmentInstructions, assignmentData.assignmentInstructions) &&
        Objects.equals(this.assignmentExcludeQuotes, assignmentData.assignmentExcludeQuotes) &&
        Objects.equals(this.assignmentDueDate, assignmentData.assignmentDueDate) &&
        Objects.equals(this.assignmentGrade, assignmentData.assignmentGrade) &&
        Objects.equals(this.assignmentAttachmentExternalContent, assignmentData.assignmentAttachmentExternalContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assignmentTitle, assignmentInstructions, assignmentExcludeQuotes, assignmentDueDate, assignmentGrade, assignmentAttachmentExternalContent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssignmentData {\n");
    
    sb.append("    assignmentTitle: ").append(toIndentedString(assignmentTitle)).append("\n");
    sb.append("    assignmentInstructions: ").append(toIndentedString(assignmentInstructions)).append("\n");
    sb.append("    assignmentExcludeQuotes: ").append(toIndentedString(assignmentExcludeQuotes)).append("\n");
    sb.append("    assignmentDueDate: ").append(toIndentedString(assignmentDueDate)).append("\n");
    sb.append("    assignmentGrade: ").append(toIndentedString(assignmentGrade)).append("\n");
    sb.append("    assignmentAttachmentExternalContent: ").append(toIndentedString(assignmentAttachmentExternalContent)).append("\n");
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

