package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AdminPeriod {
    
    @JacksonXmlProperty(localName = "language")
    private String language;
    
    @JacksonXmlProperty(localName = "textString")
    private String textString;
}
