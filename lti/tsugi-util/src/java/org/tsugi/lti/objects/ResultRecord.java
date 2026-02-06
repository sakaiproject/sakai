package org.tsugi.lti.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResultRecord {
    
    @JacksonXmlProperty(localName = "sourcedGUID")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SourcedGUID sourcedGUID;
    
    @JacksonXmlProperty(localName = "result")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Result result;
}
