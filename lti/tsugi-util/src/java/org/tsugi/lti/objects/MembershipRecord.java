package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MembershipRecord {
    
    @JacksonXmlProperty(localName = "sourcedId")
    private String sourcedId;
    
    @JacksonXmlProperty(localName = "membership")
    private Membership membership;
}
