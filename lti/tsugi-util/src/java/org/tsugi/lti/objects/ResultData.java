package org.tsugi.lti.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a <resultData> element in LTI 1.1.x Basic Outcomes POX messages.
 * 
 * Schema definition: According to the IMS/1EdTech LTI Outcomes XSD, <resultData>
 * can include <text>, <url>, and/or <ltiLaunchUrl> (all optional).
 * 
 * Common usage: The <text> element is commonly used for free-form grade comments
 * or feedback messages in replaceResultRequest operations. Many LMSs send
 * <resultData><text>...</text></resultData> to provide instructor feedback
 * alongside the numeric grade in <resultScore>.
 * 
 * Note: While the LTI 1.1.1 narrative specification doesn't extensively document
 * <resultData>, it is formally defined in the XSD and widely used in practice.
 * 
 * Authoritative XSD: https://www.imsglobal.org/lti/media/ltiv1p1/OMSv1p0_LTIv1p1Profile_SyncXSD_v1p0.xsd
 * Schemas index: https://www.imsglobal.org/specs/lti/xml
 */
@Data
@ToString
public class ResultData {
    
    @JacksonXmlProperty(localName = "text")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;
    
    @JacksonXmlProperty(localName = "url")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String url;
    
    @JacksonXmlProperty(localName = "ltiLaunchUrl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ltiLaunchUrl;
}

