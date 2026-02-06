package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;
import lombok.ToString;
import java.util.List;

@Data
@ToString
public class Membership {
    
    @JacksonXmlProperty(localName = "collectionSourcedId")
    private String collectionSourcedId;
    
    @JacksonXmlProperty(localName = "membershipIdType")
    private String membershipIdType;
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "member")
    private List<Member> members;
    
    @JacksonXmlProperty(localName = "creditHours")
    private Integer creditHours;
    
    @JacksonXmlProperty(localName = "dataSource")
    private String dataSource;
}
