package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DeleteResultRequest {
    
    @JacksonXmlProperty(localName = "resultRecord")
    private ResultRecord resultRecord;
}
