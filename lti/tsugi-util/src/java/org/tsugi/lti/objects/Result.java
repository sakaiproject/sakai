package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class Result {
    
    @JacksonXmlProperty(localName = "sourcedId")
    private String sourcedId;
    
    @JacksonXmlProperty(localName = "resultScore")
    private ResultScore resultScore;
    
    @JacksonXmlProperty(localName = "resultData")
    private ResultData resultData;
}
