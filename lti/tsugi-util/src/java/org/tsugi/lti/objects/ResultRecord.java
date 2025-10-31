package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResultRecord {
    
    @JacksonXmlProperty(localName = "sourcedGUID")
    private SourcedGUID sourcedGUID;
    
    @JacksonXmlProperty(localName = "result")
    private Result result;
}
