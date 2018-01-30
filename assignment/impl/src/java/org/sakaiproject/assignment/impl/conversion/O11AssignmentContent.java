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
public class O11AssignmentContent {
    // allowattach="true"
    private Boolean allowattach;
    // allowreview="false"
    private Boolean allowreview;
    // allowstudentview="false"
    private Boolean allowstudentview;
    // checkInstitution="false"
    private Boolean checkInstitution;
    // checkInternet="false"
    private Boolean checkInternet;
    // checkPublications="false"
    private Boolean checkPublications;
    // checkTurnitin="false"
    private Boolean checkTurnitin;
    // context="CDOR_789U_0626"
    private String context;
    // datecreated="20171215211634441"
    private String datecreated;
    // excludeBibliographic="false"
    private Boolean excludeBibliographic;
    // excludeQuoted="false"
    private Boolean excludeQuoted;
    // excludeType="0"
    private Integer excludeType;
    // excludeValue="1"
    private Integer excludeValue;
    // generateOriginalityReport="0"
    private Integer generateOriginalityReport;
    // groupproject="true"
    private Boolean groupproject;
    // hideduedate="true"
    private Boolean hideduedate;
    // honorpledge="2"
    private Integer honorpledge;
    // id="7854df94-61ca-45e0-9348-a4a3a738292d"
    private String id;
    // indivgraded="false"
    private Boolean indivgraded;
    // instructions="BASE64"
    private String instructions;
    // instructions-html="BASE64"
    @JacksonXmlProperty(localName = "instructions-html")
    private String instructionsHtml;
    // lastmod="20171215211634441"
    private String lastmod;
    // numberofattachments="1"
    private Integer numberofattachments;
    // numberofauthors="0"
    private Integer numberofauthors;
    // releasegrades="false"
    private Boolean releasegrades;
    // scaled_factor="100"
    private Integer scaled_factor;
    // scaled_maxgradepoint="2000"
    private Integer scaled_maxgradepoint;
    // submissiontype="3"
    private Integer submissiontype;
    // submitReviewRepo="0"
    private Integer submitReviewRepo;
    // title="Assignment Everyone"
    private String title;
    // typeofgrade="3"
    private Integer typeofgrade;

    /**
     * List of name=value where value is enc is the encoding used in value<br/>
     * <property enc="BASE64" name="XXX" value="YYY"/>
     */
    private List<O11Property> properties = new ArrayList<>();

    /**
     * Used to catch unmapped values<br/>
     * specifically important for catching enumerated values like<br/>
     *      attachment[0..n]
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
