package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GroupSet {
    
    @JacksonXmlProperty(localName = "id")
    private String id;
    
    @JacksonXmlProperty(localName = "title")
    private String title;
}

