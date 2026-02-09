package org.tsugi.lti.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JacksonXmlRootElement(localName = "message_response")
public class MessageResponse {
    
    @JacksonXmlProperty(localName = "lti_message_type")
    private String ltiMessageType;
    
    @JacksonXmlProperty(localName = "members")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Members members;
    
    @JacksonXmlProperty(localName = "result")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Result result;
    
    @JacksonXmlProperty(localName = "statusinfo")
    private StatusInfo statusInfo;
}

