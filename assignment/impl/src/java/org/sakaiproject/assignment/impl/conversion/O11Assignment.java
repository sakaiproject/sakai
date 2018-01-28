package org.sakaiproject.assignment.impl.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(of = "id")
public class O11Assignment {
    // access="site"
    private String access;
    // allowpeerassessment="false"
    private Boolean allowpeerassessment = Boolean.FALSE;
    // assignmentcontent="/assignment/c/CDOR_789U_0626/7854df94-61ca-45e0-9348-a4a3a738292d"
    private String assignmentcontent;
    // closedate="20171222220000000"
    private String closedate;
    // context="CDOR_789U_0626"
    private String context="CDOR_789U_0626";
    // draft="false"
    private Boolean draft = Boolean.FALSE;
    // dropdeaddate="20171222220000000"
    private String dropdeaddate;
    // duedate="20171222220000000"
    private String duedate;
    // group="false"
    private Boolean group = Boolean.FALSE;
    // hideduedate="false"
    private Boolean hideduedate;
    // id="cac11b82-64ec-4cc3-87b9-cd0385aebd71"
    private String id;
    // numberofauthors="0"
    private Integer numberofauthors;
    // opendate="20171215170000000"
    private String opendate;
    // peerassessmentanoneval="true"
    private Boolean peerassessmentanoneval;
    // peerassessmentinstructions=""
    private String peerassessmentinstructions;
    // peerassessmentnumreviews="1"
    private Integer peerassessmentnumreviews;
    // peerassessmentperiodtime="20171222221000000"
    private String peerassessmentperiodtime;
    // peerassessmentstudentviewreviews="true"
    private Boolean peerassessmentstudentviewreviews;
    // position_order="0"
    private Integer position_order;
    // section=""
    private String section;
    // title="Assignment one"
    private String title;
    // visibledate=""
    private String visibledate;
    // List of authzGroups
    // <group authzGroup="/site/BVCC_942A_9301/group/b9ff34b8-1465-4ba8-b532-ed8e097b88fa"/>
    private List<O11Group> groups = new ArrayList<>();
    // List of name=value where value is enc is the encoding used in value
    // <property enc="BASE64" name="XXX" value="YYY"/>
    // <property enc="BASE64" name="CHEF:creator" value="ZTJjNzk1ZTYtY2RmYS00OGZmLTgxZGEtNWE0ZTg0YzI5YWVh"/>
    // <property enc="BASE64" name="CHEF:modifiedby" value="ZTJjNzk1ZTYtY2RmYS00OGZmLTgxZGEtNWE0ZTg0YzI5YWVh"/>
    // <property enc="BASE64" name="new_assignment_add_to_gradebook" value="YXNzb2NpYXRl"/>
    // <property enc="BASE64" name="CHEF:assignment_opendate_announcement_message_id" value="NThmZGZkN2MtNzA3Ny00NDkyLWE3ZmQtZmI1NTQzYjFjYmMw"/>
    // <property enc="BASE64" name="allow_resubmit_closeTime" value="MTUxMzk4MDAwMDAwMA=="/>
    // <property enc="BASE64" name="assignment_releasereturn_notification_value" value="YXNzaWdubWVudF9yZWxlYXNlcmV0dXJuX25vdGlmaWNhdGlvbl9lYWNo"/>
    // <property enc="BASE64" name="DAV:getlastmodified" value="MjAxNzEyMTUyMTE2MzQ1ODg="/>
    // <property enc="BASE64" name="prop_new_assignment_add_to_gradebook" value="L2Fzc2lnbm1lbnQvYS9DRE9SXzc4OVVfMDYyNi9jYWMxMWI4Mi02NGVjLTRjYzMtODdiOS1jZDAzODVhZWJkNzE="/>
    // <property enc="BASE64" name="allow_resubmit_number" value="Mg=="/>
    // <property enc="BASE64" name="new_assignment_open_date_announced" value="dHJ1ZQ=="/>
    // <property enc="BASE64" name="DAV:creationdate" value="MjAxNzEyMTUyMTE2MzQ0NjA="/>
    // <property enc="BASE64" name="new_assignment_check_auto_announce" value="dHJ1ZQ=="/>
    // <property enc="BASE64" name="newAssignment" value="dHJ1ZQ=="/>
    // <property enc="BASE64" name="assignment_releasegrade_notification_value" value="YXNzaWdubWVudF9yZWxlYXNlZ3JhZGVfbm90aWZpY2F0aW9uX2VhY2g="/>
    // <property enc="BASE64" name="assignment_instructor_notifications_value" value="YXNzaWdubWVudF9pbnN0cnVjdG9yX25vdGlmaWNhdGlvbnNfZWFjaA=="/>
    // <property enc="BASE64" name="new_assignment_check_anonymous_grading" value=""/>
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

    // prevents nulling the member groups, this is not need in jackson 2.9
    public void setGroups(List<O11Group> groups) {
        if (groups != null) {
            this.groups = groups;
        }
    }

    // prevents nulling the member properties, this is not need in jackson 2.9
    public void setProperties(List<O11Property> properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }
}
