package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

/**
 * Represents a readResultResponse according to LTI 1.1.1 Basic Outcomes spec (Figure 6 / Section 6.1.2).
 * 
 * NOTE: There is an inconsistency in the LTI 1.1.1 spec structure:
 * - replaceResultRequest wraps <result> in <resultRecord> with <sourcedGUID>: 
 *   <replaceResultRequest><resultRecord><sourcedGUID><sourcedId>...</sourcedId></sourcedGUID><result>...</result></resultRecord></replaceResultRequest>
 * - readResultResponse contains <result> directly without <resultRecord> wrapper:
 *   <readResultResponse><result><resultScore>...</resultScore></result></readResultResponse>
 * 
 * This inconsistency exists in the spec itself. The readResultResponse structure differs from
 * replaceResultRequest - it does not include resultRecord or sourcedGUID, just the result directly.
 * 
 * Also note: Unlike replaceResultResponse and deleteResultResponse (which are empty elements),
 * readResultResponse contains actual content (the result with resultScore and optionally resultData).
 */
@Data
@ToString
@JacksonXmlRootElement(localName = "readResultResponse")
public class ReadResultResponse {
    
    @JacksonXmlProperty(localName = "result")
    private Result result;
    
    /**
     * Factory method to create a ReadResultResponse with resultScore and optionally resultData.
     * 
     * STRUCTURAL DIFFERENCES (spec inconsistency):
     * 1. readResultResponse vs replaceResultResponse/deleteResultResponse:
     *    - readResultResponse contains a <result> element with <resultScore> (and optionally <resultData>)
     *    - replaceResultResponse and deleteResultResponse are empty elements (<replaceResultResponse/>, <deleteResultResponse/>)
     * 
     * 2. readResultResponse vs replaceResultRequest (spec inconsistency):
     *    - replaceResultRequest wraps <result> in <resultRecord> with <sourcedGUID>:
     *      <replaceResultRequest><resultRecord><sourcedGUID><sourcedId>...</sourcedId></sourcedGUID><result>...</result></resultRecord></replaceResultRequest>
     *    - readResultResponse contains <result> directly without <resultRecord> wrapper:
     *      <readResultResponse><result><resultScore>...</resultScore></result></readResultResponse>
     *    This structural difference exists in the spec itself.
     *
     * 3. The <result> element in readResultResponse does NOT include <sourcedId> - that field
     *    is only used in requests (within <resultRecord><sourcedGUID><sourcedId>) to identify which result to read.
     * 
     * @param grade The grade/score text string (e.g., "0.91", "A", etc.)
     * @param comment Optional comment/feedback text. If null or empty, resultData will not be included.
     * @param language The language code for the result score (typically "en")
     * @return A ReadResultResponse instance with the specified resultScore and optionally resultData
     */
    public static ReadResultResponse create(String grade, String comment, String language) {
        ReadResultResponse readResultResponse = new ReadResultResponse();
        Result result = new Result();
        // sourcedId is NOT included in readResultResponse per LTI 1.1.1 spec
        
        ResultScore resultScore = new ResultScore();
        resultScore.setLanguage(language);
        resultScore.setTextString(grade);
        result.setResultScore(resultScore);
        
        if (comment != null && !comment.isEmpty()) {
            ResultData resultData = new ResultData();
            resultData.setText(comment);
            result.setResultData(resultData);
        }
        
        readResultResponse.setResult(result);
        return readResultResponse;
    }
}
