package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a replaceResultRequest according to LTI 1.1.1 Basic Outcomes spec (Figure 3 / Section 6.1.1).
 * 
 * NOTE: There is a structural inconsistency in the LTI 1.1.1 spec between requests and responses:
 * - replaceResultRequest wraps <result> in <resultRecord> with <sourcedGUID>:
 *   <replaceResultRequest><resultRecord><sourcedGUID><sourcedId>...</sourcedId></sourcedGUID><result>...</result></resultRecord></replaceResultRequest>
 * - readResultResponse contains <result> directly without <resultRecord> wrapper:
 *   <readResultResponse><result><resultScore>...</resultScore></result></readResultResponse>
 * 
 * This inconsistency exists in the spec itself. The request structure uses resultRecord to wrap
 * both the sourcedGUID (for identification) and the result (for replacement), while the response
 * structure omits the resultRecord wrapper and contains only the result directly.
 */
@Data
@ToString
public class ReplaceResultRequest {
    
    @JacksonXmlProperty(localName = "resultRecord")
    private ResultRecord resultRecord;
}
