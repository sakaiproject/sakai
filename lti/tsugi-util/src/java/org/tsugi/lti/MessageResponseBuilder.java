package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.tsugi.lti.objects.Group;
import org.tsugi.lti.objects.GroupSet;
import org.tsugi.lti.objects.Member;
import org.tsugi.lti.objects.Members;
import org.tsugi.lti.objects.MessageResponse;
import org.tsugi.lti.objects.Result;
import org.tsugi.lti.objects.ResultData;
import org.tsugi.lti.objects.ResultScore;
import org.tsugi.lti.objects.StatusInfo;
import org.tsugi.pox.POXConstants;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MessageResponseBuilder {
    
    private static final XmlMapper xmlMapper = new XmlMapper();
    
    private MessageResponse response;
    
    public MessageResponseBuilder() {
        this.response = new MessageResponse();
    }
    
    public MessageResponseBuilder withLtiMessageType(String ltiMessageType) {
        response.setLtiMessageType(ltiMessageType);
        return this;
    }
    
    public MessageResponseBuilder withStatusInfo(String codeMajor, String severity) {
        return withStatusInfo(codeMajor, severity, null, null);
    }

    public MessageResponseBuilder withStatusInfo(String codeMajor, String severity, String codeMinor, String description) {
        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setCodeMajor(codeMajor);
        statusInfo.setSeverity(severity);
        statusInfo.setCodeMinor(codeMinor);
        statusInfo.setDescription(description);
        response.setStatusInfo(statusInfo);
        return this;
    }

    public MessageResponseBuilder addMember(Member member) {
        if (response.getMembers() == null) {
            response.setMembers(new Members());
            response.getMembers().setMember(new ArrayList<>());
        }
        response.getMembers().getMember().add(member);
        return this;
    }
 
    public MessageResponseBuilder withResult(String resultScore, String resultData) {
        Result result = new Result();
        
        if (resultScore != null) {
            ResultScore score = new ResultScore();
            score.setTextString(resultScore);
            result.setResultScore(score);
        }
        
        if (resultData != null) {
            ResultData data = new ResultData();
            data.setText(resultData);
            result.setResultData(data);
        }
        
        response.setResult(result);
        return this;
    }

    public MessageResponse build() {
        return response;
    }
    
    public String buildAsXml(boolean pretty) {
        try {
            MessageResponse msgResponse = build();
            if (pretty) {
                return xmlMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(msgResponse);
            } else {
                return xmlMapper.writeValueAsString(msgResponse);
            }
        } catch (Exception e) {
            log.error("Error serializing MessageResponse to XML", e);
            throw new RuntimeException("Failed to serialize MessageResponse to XML", e);
        }
    }

    public String buildAsXml() {
        return buildAsXml(true);
    }
    


    public static MessageResponseBuilder success() {
        return new MessageResponseBuilder()
            .withStatusInfo(POXConstants.MAJOR_SUCCESS, POXConstants.SEVERITY_STATUS,
                POXConstants.MINOR_FULLSUCCESS, null);
    }

    public static MessageResponseBuilder error(String description) {
        return new MessageResponseBuilder()
            .withStatusInfo(POXConstants.MAJOR_FAILURE, POXConstants.SEVERITY_ERROR, null, description);
    }

    public static Member createMember(String userId, String role) {
        Member member = new Member();
        member.setUserId(userId);
        member.setRole(role);
        member.setRoles(role);
        return member;
    }

    public static Group createGroup(String id, String title) {
        Group group = new Group();
        group.setId(id);
        group.setTitle(title);
        
        GroupSet set = new GroupSet();
        set.setId(id);
        set.setTitle(title);
        group.setSet(set);
        
        return group;
    }
}

