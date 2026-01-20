package org.tsugi.lti.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class POXRequestBody {
    
    @JacksonXmlProperty(localName = "replaceResultRequest")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReplaceResultRequest replaceResultRequest;
    
    @JacksonXmlProperty(localName = "readResultRequest")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReadResultRequest readResultRequest;
    
    @JacksonXmlProperty(localName = "deleteResultRequest")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DeleteResultRequest deleteResultRequest;
    
    @JacksonXmlProperty(localName = "readMembershipRequest")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ReadMembershipRequest readMembershipRequest;
    
    @JacksonXmlProperty(localName = "rawContent")
    private String rawContent;
}
