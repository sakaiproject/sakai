package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class ExternalContentData   {
  
  private String fileName = null;
  private String uploadContentType = null;
  private Integer uploadContentLength = null;
  private String externalContentID = null;

  
  /**
   * The name of the file
   **/
  public ExternalContentData fileName(String fileName) {
    this.fileName = fileName;
    return this;
  }
  
  @ApiModelProperty(example = "null", required = true, value = "The name of the file")
  @JsonProperty("fileName")
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  
  /**
   * The content type of the file
   **/
  public ExternalContentData uploadContentType(String uploadContentType) {
    this.uploadContentType = uploadContentType;
    return this;
  }
  
  @ApiModelProperty(example = "null", required = true, value = "The content type of the file")
  @JsonProperty("uploadContentType")
  public String getUploadContentType() {
    return uploadContentType;
  }
  public void setUploadContentType(String uploadContentType) {
    this.uploadContentType = uploadContentType;
  }

  
  /**
   * The content length of the file
   **/
  public ExternalContentData uploadContentLength(Integer uploadContentLength) {
    this.uploadContentLength = uploadContentLength;
    return this;
  }
  
  @ApiModelProperty(example = "null", required = true, value = "The content length of the file")
  @JsonProperty("uploadContentLength")
  public Integer getUploadContentLength() {
    return uploadContentLength;
  }
  public void setUploadContentLength(Integer uploadContentLength) {
    this.uploadContentLength = uploadContentLength;
  }

  
  /**
   * External Content ID
   **/
  public ExternalContentData externalContentID(String externalContentID) {
    this.externalContentID = externalContentID;
    return this;
  }
  
  @ApiModelProperty(example = "null", required = true, value = "External Content ID")
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
    ExternalContentData externalContentData = (ExternalContentData) o;
    return Objects.equals(this.fileName, externalContentData.fileName) &&
        Objects.equals(this.uploadContentType, externalContentData.uploadContentType) &&
        Objects.equals(this.uploadContentLength, externalContentData.uploadContentLength) &&
        Objects.equals(this.externalContentID, externalContentData.externalContentID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fileName, uploadContentType, uploadContentLength, externalContentID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExternalContentData {\n");
    
    sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
    sb.append("    uploadContentType: ").append(toIndentedString(uploadContentType)).append("\n");
    sb.append("    uploadContentLength: ").append(toIndentedString(uploadContentLength)).append("\n");
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

