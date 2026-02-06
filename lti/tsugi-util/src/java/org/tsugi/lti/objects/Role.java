package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Role {
    
    @JacksonXmlProperty(localName = "roleType")
    private String roleType;
    
    @JacksonXmlProperty(localName = "subRole")
    private String subRole;
    
    @JacksonXmlProperty(localName = "timeFrame")
    private TimeFrame timeFrame;
    
    @JacksonXmlProperty(localName = "status")
    private String status;
    
    @JacksonXmlProperty(localName = "dateTime")
    private String dateTime;
    
    @JacksonXmlProperty(localName = "dataSource")
    private String dataSource;
}
