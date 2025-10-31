package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ReadMembershipRequest {
    
    @JacksonXmlProperty(localName = "sourcedId")
    private String sourcedId;
}
