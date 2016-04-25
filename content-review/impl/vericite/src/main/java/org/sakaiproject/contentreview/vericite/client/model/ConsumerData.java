package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class ConsumerData   {
  
  private String description = null;
  private Boolean trial = null;
  private Integer trialEndDate = null;
  private String contactEmail = null;
  private String contactName = null;
  private String timeZone = null;
  private Integer fteCount = null;
  private String notes = null;

  
  /**
   * Description
   **/
  public ConsumerData description(String description) {
    this.description = description;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * Is Trial?
   **/
  public ConsumerData trial(Boolean trial) {
    this.trial = trial;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Is Trial?")
  @JsonProperty("trial")
  public Boolean getTrial() {
    return trial;
  }
  public void setTrial(Boolean trial) {
    this.trial = trial;
  }

  
  /**
   * Trial End Date
   **/
  public ConsumerData trialEndDate(Integer trialEndDate) {
    this.trialEndDate = trialEndDate;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Trial End Date")
  @JsonProperty("trialEndDate")
  public Integer getTrialEndDate() {
    return trialEndDate;
  }
  public void setTrialEndDate(Integer trialEndDate) {
    this.trialEndDate = trialEndDate;
  }

  
  /**
   * Contact Email
   **/
  public ConsumerData contactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Contact Email")
  @JsonProperty("contactEmail")
  public String getContactEmail() {
    return contactEmail;
  }
  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  
  /**
   * Contact Name
   **/
  public ConsumerData contactName(String contactName) {
    this.contactName = contactName;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Contact Name")
  @JsonProperty("contactName")
  public String getContactName() {
    return contactName;
  }
  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  
  /**
   * Time Zone
   **/
  public ConsumerData timeZone(String timeZone) {
    this.timeZone = timeZone;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Time Zone")
  @JsonProperty("timeZone")
  public String getTimeZone() {
    return timeZone;
  }
  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  
  /**
   * FTE Student Count
   **/
  public ConsumerData fteCount(Integer fteCount) {
    this.fteCount = fteCount;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "FTE Student Count")
  @JsonProperty("fteCount")
  public Integer getFteCount() {
    return fteCount;
  }
  public void setFteCount(Integer fteCount) {
    this.fteCount = fteCount;
  }

  
  /**
   * Additional Notes
   **/
  public ConsumerData notes(String notes) {
    this.notes = notes;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "Additional Notes")
  @JsonProperty("notes")
  public String getNotes() {
    return notes;
  }
  public void setNotes(String notes) {
    this.notes = notes;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumerData consumerData = (ConsumerData) o;
    return Objects.equals(this.description, consumerData.description) &&
        Objects.equals(this.trial, consumerData.trial) &&
        Objects.equals(this.trialEndDate, consumerData.trialEndDate) &&
        Objects.equals(this.contactEmail, consumerData.contactEmail) &&
        Objects.equals(this.contactName, consumerData.contactName) &&
        Objects.equals(this.timeZone, consumerData.timeZone) &&
        Objects.equals(this.fteCount, consumerData.fteCount) &&
        Objects.equals(this.notes, consumerData.notes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, trial, trialEndDate, contactEmail, contactName, timeZone, fteCount, notes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsumerData {\n");
    
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    trial: ").append(toIndentedString(trial)).append("\n");
    sb.append("    trialEndDate: ").append(toIndentedString(trialEndDate)).append("\n");
    sb.append("    contactEmail: ").append(toIndentedString(contactEmail)).append("\n");
    sb.append("    contactName: ").append(toIndentedString(contactName)).append("\n");
    sb.append("    timeZone: ").append(toIndentedString(timeZone)).append("\n");
    sb.append("    fteCount: ").append(toIndentedString(fteCount)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
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

