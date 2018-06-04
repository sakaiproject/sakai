package org.sakaiproject.assignment.impl.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(of = "id")
public class O11Submission {
    // anonymousSubmissionId="7991fdac2 (anonymous)"
    private String anonymousSubmissionId;
    // assignment="cac11b82-64ec-4cc3-87b9-cd0385aebd71"
    private String assignment;
    // context="CDOR_789U_0626"
    private String context;
    // datereturned=""
    private String datereturned;
    // datesubmitted="20171215211945306"
    private String datesubmitted;
    // feedbackcomment=""
    private String feedbackcomment;
    // feedbackcomment-html=""
    @JacksonXmlProperty(localName = "feedbackcomment-html")
    private String feedbackcommentHtml;
    // feedbacktext=""
    private String feedbacktext;
    // feedbacktext-html=""
    @JacksonXmlProperty(localName = "feedbacktext-html")
    private String feedbacktextHtml;
    // graded="false"
    private Boolean graded;
    // gradedBy=""
    private String gradedBy;
    // gradereleased="false"
    private Boolean gradereleased = Boolean.FALSE;
    // hideduedate="false"
    private Boolean hideduedate = Boolean.FALSE;
    // id="aa5d91c4-eeb0-4b51-aaae-3b27991fdac2"
    private String id;
    // isUserSubmission="true"
    private Boolean isUserSubmission = null;
    // lastmod="20171215211945307"
    private String lastmod;
    // pledgeflag="true"
    private Boolean pledgeflag = Boolean.FALSE;
    // returned="false"
    private Boolean returned = Boolean.FALSE;
    // scaled_factor="0"
    private Integer scaled_factor;
    // scaled_grade=""
    private String scaled_grade;
    // submitted="true"
    private Boolean submitted = Boolean.FALSE;
    // submittedtext=""
    private String submittedtext;
    // submittedtext-html=""
    @JacksonXmlProperty(localName = "submittedtext-html")
    private String submittedtextHtml;
    // submitterid="d5c19c78-6414-44a8-bebd-b684f8a6f973"
    private String submitterid;

    // List of name=value where value is enc is the encoding used in value
    // <property enc="BASE64" name="XXX" value="YYY"/>
    private List<O11Property> properties = new ArrayList<>();

    /**
     * Used to catch unmapped values<br/>
     * specifically important for catching enumerated values like<br/>
     *      attachment[0..n]
     *      log[0..n]
     *      submitter[0..n]
     */
    @Getter
    private Map<String, Object> any = new HashMap<>();

    @JsonAnySetter
    public void setAny(String key, Object value) {
        any.put(key, value);
    }

    // prevents nulling the member properties, this is not need in jackson 2.9
    public void setProperties(List<O11Property> properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }
}
