package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JacksonXmlRootElement(localName = "readMembershipResponse")
public class ReadMembershipResponse {
    
    @JacksonXmlProperty(localName = "membershipRecord")
    private MembershipRecord membershipRecord;
}
