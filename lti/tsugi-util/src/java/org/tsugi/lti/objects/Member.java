package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;
import java.util.List;

@Data
@ToString
public class Member {
    
    @JacksonXmlProperty(localName = "user_id")
    private String userId;
    
    @JacksonXmlProperty(localName = "role")
    private String role;
    
    @JacksonXmlProperty(localName = "roles")
    private String roles;
    
    @JacksonXmlProperty(localName = "person_name_given")
    private String personNameGiven;
    
    @JacksonXmlProperty(localName = "person_name_family")
    private String personNameFamily;
    
    @JacksonXmlProperty(localName = "person_name_full")
    private String personNameFull;
    
    @JacksonXmlProperty(localName = "person_contact_email_primary")
    private String personContactEmailPrimary;
    
    @JacksonXmlProperty(localName = "person_sourcedid")
    private String personSourcedId;
    
    @JacksonXmlProperty(localName = "lis_result_sourcedid")
    private String lisResultSourcedId;
    
    @JacksonXmlProperty(localName = "groups")
    private Groups groups;
}
