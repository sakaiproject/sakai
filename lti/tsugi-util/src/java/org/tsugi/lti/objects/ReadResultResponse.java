package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
@JacksonXmlRootElement(localName = "readResultResponse")
public class ReadResultResponse {
    
    @JacksonXmlProperty(localName = "result")
    private Result result;
}
