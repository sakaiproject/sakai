package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class ExternalContentUploadInfo   {
  
  private String externalContentId = null;
  private String urlPost = null;
  private String filePath = null;
  private Integer contentLength = null;
  private String contentType = null;

  
  /**
   * ID used to ID the uploaded file
   **/
  public ExternalContentUploadInfo externalContentId(String externalContentId) {
    this.externalContentId = externalContentId;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "ID used to ID the uploaded file")
  @JsonProperty("externalContentId")
  public String getExternalContentId() {
    return externalContentId;
  }
  public void setExternalContentId(String externalContentId) {
    this.externalContentId = externalContentId;
  }

  
  /**
   * URL to submit the attachment to
   **/
  public ExternalContentUploadInfo urlPost(String urlPost) {
    this.urlPost = urlPost;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "URL to submit the attachment to")
  @JsonProperty("urlPost")
  public String getUrlPost() {
    return urlPost;
  }
  public void setUrlPost(String urlPost) {
    this.urlPost = urlPost;
  }

  
  /**
   * The file path
   **/
  public ExternalContentUploadInfo filePath(String filePath) {
    this.filePath = filePath;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "The file path")
  @JsonProperty("filePath")
  public String getFilePath() {
    return filePath;
  }
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  
  /**
   * The length of the file
   **/
  public ExternalContentUploadInfo contentLength(Integer contentLength) {
    this.contentLength = contentLength;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "The length of the file")
  @JsonProperty("contentLength")
  public Integer getContentLength() {
    return contentLength;
  }
  public void setContentLength(Integer contentLength) {
    this.contentLength = contentLength;
  }

  
  /**
   * The files content type
   **/
  public ExternalContentUploadInfo contentType(String contentType) {
    this.contentType = contentType;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "The files content type")
  @JsonProperty("contentType")
  public String getContentType() {
    return contentType;
  }
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExternalContentUploadInfo externalContentUploadInfo = (ExternalContentUploadInfo) o;
    return Objects.equals(this.externalContentId, externalContentUploadInfo.externalContentId) &&
        Objects.equals(this.urlPost, externalContentUploadInfo.urlPost) &&
        Objects.equals(this.filePath, externalContentUploadInfo.filePath) &&
        Objects.equals(this.contentLength, externalContentUploadInfo.contentLength) &&
        Objects.equals(this.contentType, externalContentUploadInfo.contentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(externalContentId, urlPost, filePath, contentLength, contentType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExternalContentUploadInfo {\n");
    
    sb.append("    externalContentID: ").append(toIndentedString(externalContentId)).append("\n");
    sb.append("    urlPost: ").append(toIndentedString(urlPost)).append("\n");
    sb.append("    filePath: ").append(toIndentedString(filePath)).append("\n");
    sb.append("    contentLength: ").append(toIndentedString(contentLength)).append("\n");
    sb.append("    contentType: ").append(toIndentedString(contentType)).append("\n");
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

