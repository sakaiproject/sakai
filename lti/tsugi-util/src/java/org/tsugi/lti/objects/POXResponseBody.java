package org.tsugi.lti.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class POXResponseBody {
    
    @JacksonXmlProperty(localName = "replaceResultResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReplaceResultResponse replaceResultResponse;
    
    @JacksonXmlProperty(localName = "readResultResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReadResultResponse readResultResponse;
    
    @JacksonXmlProperty(localName = "deleteResultResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DeleteResultResponse deleteResultResponse;
    
    @JacksonXmlProperty(localName = "readMembershipResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReadMembershipResponse readMembershipResponse;
    
    @JacksonXmlProperty(localName = "rawContent")
    private String rawContent;
}
