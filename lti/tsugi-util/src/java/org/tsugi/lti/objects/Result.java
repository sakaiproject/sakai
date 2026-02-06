package org.tsugi.lti.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a <result> element in LTI 1.1.x Basic Outcomes POX messages.
 * 
 * Structure: A <result> element may contain:
 * - <resultScore> (required for replaceResultRequest, present in readResultResponse)
 * - <resultData> (optional, commonly used for grade comments/feedback)
 * 
 * NOTE on resultData: While the LTI 1.1.1 narrative specification and most examples
 * focus primarily on <resultScore>, the official IMS/1EdTech LTI Outcomes XSD
 * defines <resultData> as an optional child of <result>. Many LMSs use
 * <resultData><text>...</text></resultData> to carry grade comments or feedback
 * messages in replaceResultRequest operations.
 * 
 * Interoperability note: Some LMSs send grade comments in resultData/text; others
 * ignore it. This implementation keeps parsing tolerant to handle both cases.
 * 
 * Authoritative XSD: https://www.imsglobal.org/lti/media/ltiv1p1/OMSv1p0_LTIv1p1Profile_SyncXSD_v1p0.xsd
 * Schemas index: https://www.imsglobal.org/specs/lti/xml
 */
@Data
@ToString
public class Result {
    
    @JacksonXmlProperty(localName = "sourcedId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sourcedId;
    
    @JacksonXmlProperty(localName = "resultScore")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResultScore resultScore;
    
    @JacksonXmlProperty(localName = "resultData")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResultData resultData;
}
