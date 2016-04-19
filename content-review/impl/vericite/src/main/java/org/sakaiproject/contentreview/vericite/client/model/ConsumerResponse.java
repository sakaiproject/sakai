package org.sakaiproject.contentreview.vericite.client.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-03-29T13:45:18.525Z")
public class ConsumerResponse   {
  
  private String consumerKey = null;
  private String consumerSecret = null;

  
  /**
   * The key of the newly created consumer
   **/
  public ConsumerResponse consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "The key of the newly created consumer")
  @JsonProperty("consumerKey")
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  
  /**
   * The secret of the newly created consumer
   **/
  public ConsumerResponse consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }
  
  @ApiModelProperty(example = "null", value = "The secret of the newly created consumer")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumerResponse consumerResponse = (ConsumerResponse) o;
    return Objects.equals(this.consumerKey, consumerResponse.consumerKey) &&
        Objects.equals(this.consumerSecret, consumerResponse.consumerSecret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsumerResponse {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
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

