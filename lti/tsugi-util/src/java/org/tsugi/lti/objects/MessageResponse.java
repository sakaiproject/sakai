package org.tsugi.lti.objects;

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
    private Members members;
    
    @JacksonXmlProperty(localName = "result")
    private Result result;
    
    @JacksonXmlProperty(localName = "statusinfo")
    private StatusInfo statusInfo;
}

