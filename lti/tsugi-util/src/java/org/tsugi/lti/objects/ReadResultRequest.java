package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ReadResultRequest {
    
    @JacksonXmlProperty(localName = "resultRecord")
    private ResultRecord resultRecord;
}
