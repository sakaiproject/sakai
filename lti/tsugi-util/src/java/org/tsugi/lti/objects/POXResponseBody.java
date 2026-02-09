package org.tsugi.lti.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class POXResponseBody {
    
    // Only one of these will be present at a time
    @JacksonXmlProperty(localName = "replaceResultResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReplaceResultResponse replaceResultResponse;
    
    @JacksonXmlProperty(localName = "readResultResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReadResultResponse readResultResponse;
    
    @JacksonXmlProperty(localName = "deleteResultResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DeleteResultResponse deleteResultResponse;
}
