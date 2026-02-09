package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a replaceResultResponse according to LTI 1.1.1 Basic Outcomes spec (Figure 4 / Section 6.1.1).
 * 
 * NOTE: This is an empty response element: <replaceResultResponse/>
 * 
 * Contrast this with readResultResponse, which contains a <result> element with <resultScore>
 * (and optionally <resultData>). The replaceResultResponse and deleteResultResponse are both
 * empty elements, while readResultResponse has content.
 * 
 * Also note: There is a structural inconsistency in the spec between requests and responses:
 * - replaceResultRequest wraps <result> in <resultRecord> with <sourcedGUID>
 * - readResultResponse contains <result> directly without <resultRecord> wrapper
 * This inconsistency exists in the LTI 1.1.1 spec itself.
 */
@Data
@ToString
@JacksonXmlRootElement(localName = "replaceResultResponse")
public class ReplaceResultResponse {
    // Empty response body for replace result - success/failure indicated in header statusInfo
}
