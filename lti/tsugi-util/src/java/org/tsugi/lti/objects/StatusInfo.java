package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StatusInfo {
    
    @JacksonXmlProperty(localName = "codemajor")
    private String codeMajor;
    
    @JacksonXmlProperty(localName = "codeminor")
    private String codeMinor;
    
    @JacksonXmlProperty(localName = "severity")
    private String severity;
    
    @JacksonXmlProperty(localName = "description")
    private String description;
}

