package org.tsugi.pox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class POXConstants {
    
    private POXConstants() {
        // Utility class - prevent instantiation
    }
    
    public static final String MAJOR_SUCCESS = "success";
    public static final String MAJOR_FAILURE = "failure";
    public static final String MAJOR_UNSUPPORTED = "unsupported";
    public static final String MAJOR_PROCESSING = "processing";
    
    public static final List<String> VALID_MAJOR = Collections.unmodifiableList(Arrays.asList(
        MAJOR_SUCCESS, MAJOR_FAILURE, MAJOR_UNSUPPORTED, MAJOR_PROCESSING
    ));
    
    public static final String SEVERITY_ERROR = "error";
    public static final String SEVERITY_WARNING = "warning";
    public static final String SEVERITY_STATUS = "status";
    
    public static final List<String> VALID_SEVERITY = Collections.unmodifiableList(Arrays.asList(
        SEVERITY_ERROR, SEVERITY_WARNING, SEVERITY_STATUS
    ));
    
    public static final String MINOR_FULLSUCCESS = "fullsuccess";
    public static final String MINOR_NOSOURCEDIDS = "nosourcedids";
    public static final String MINOR_IDALLOC = "idalloc";
    public static final String MINOR_OVERFLOWFAIL = "overflowfail";
    public static final String MINOR_IDALLOCINUSEFAIL = "idallocinusefail";
    public static final String MINOR_INVALIDDATAFAIL = "invaliddata";
    public static final String MINOR_INCOMPLETEDATA = "incompletedata";
    public static final String MINOR_PARTIALSTORAGE = "partialdatastorage";
    public static final String MINOR_UNKNOWNOBJECT = "unknownobject";
    public static final String MINOR_DELETEFAILURE = "deletefailure";
    public static final String MINOR_TARGETREADFAILURE = "targetreadfailure";
    public static final String MINOR_SAVEPOINTERROR = "savepointerror";
    public static final String MINOR_SAVEPOINTSYNCERROR = "savepointsyncerror";
    public static final String MINOR_UNKNOWNQUERY = "unknownquery";
    public static final String MINOR_UNKNOWNVOCAB = "unknownvocab";
    public static final String MINOR_TARGETISBUSY = "targetisbusy";
    public static final String MINOR_UNKNOWNEXTENSION = "unknownextension";
    public static final String MINOR_UNAUTHORIZEDREQUEST = "unauthorizedrequest";
    public static final String MINOR_LINKFAILURE = "linkfailure";
    public static final String MINOR_UNSUPPORTED = "unsupported";
    
    public static final List<String> VALID_MINOR = Collections.unmodifiableList(Arrays.asList(
        MINOR_FULLSUCCESS, MINOR_NOSOURCEDIDS, MINOR_IDALLOC, MINOR_OVERFLOWFAIL,
        MINOR_IDALLOCINUSEFAIL, MINOR_INVALIDDATAFAIL, MINOR_INCOMPLETEDATA,
        MINOR_PARTIALSTORAGE, MINOR_UNKNOWNOBJECT, MINOR_DELETEFAILURE,
        MINOR_TARGETREADFAILURE, MINOR_SAVEPOINTERROR, MINOR_SAVEPOINTSYNCERROR,
        MINOR_UNKNOWNQUERY, MINOR_UNKNOWNVOCAB, MINOR_TARGETISBUSY,
        MINOR_UNKNOWNEXTENSION, MINOR_UNAUTHORIZEDREQUEST, MINOR_LINKFAILURE,
        MINOR_UNSUPPORTED
    ));
    
    public static final String OPERATION_REPLACE_RESULT = "replaceResultRequest";
    public static final String OPERATION_READ_RESULT = "readResultRequest";
    public static final String OPERATION_DELETE_RESULT = "deleteResultRequest";
    public static final String OPERATION_READ_MEMBERSHIP = "readMembershipRequest";
    
    public static final List<String> VALID_OPERATIONS = Collections.unmodifiableList(Arrays.asList(
        OPERATION_REPLACE_RESULT, OPERATION_READ_RESULT, 
        OPERATION_DELETE_RESULT, OPERATION_READ_MEMBERSHIP
    ));
    
    public static final String ROLE_LEARNER = "Learner";
    public static final String ROLE_INSTRUCTOR = "Instructor";
    public static final String ROLE_CONTENT_DEVELOPER = "ContentDeveloper";
    public static final String ROLE_MEMBER = "Member";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_MENTOR = "Mentor";
    public static final String ROLE_ADMINISTRATOR = "Administrator";
    public static final String ROLE_TEACHING_ASSISTANT = "TeachingAssistant";
    
    public static final List<String> VALID_ROLES = Collections.unmodifiableList(Arrays.asList(
        ROLE_LEARNER, ROLE_INSTRUCTOR, ROLE_CONTENT_DEVELOPER, ROLE_MEMBER,
        ROLE_MANAGER, ROLE_MENTOR, ROLE_ADMINISTRATOR, ROLE_TEACHING_ASSISTANT
    ));
    
    public static final String FIELD_TYPE_BOOLEAN = "Boolean";
    public static final String FIELD_TYPE_INTEGER = "Integer";
    public static final String FIELD_TYPE_REAL = "Real";
    public static final String FIELD_TYPE_STRING = "String";
    
    public static final List<String> VALID_FIELD_TYPES = Collections.unmodifiableList(Arrays.asList(
        FIELD_TYPE_BOOLEAN, FIELD_TYPE_INTEGER, FIELD_TYPE_REAL, FIELD_TYPE_STRING
    ));
    
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";
    public static final String STATUS_DELETED = "Deleted";
    
    public static final List<String> VALID_STATUS = Collections.unmodifiableList(Arrays.asList(
        STATUS_ACTIVE, STATUS_INACTIVE, STATUS_DELETED
    ));
    
    public static final String NAMESPACE_IMSOMS = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0";
    public static final String NAMESPACE_IMSMMS = "http://www.imsglobal.org/services/lis/mms2p0/wsdl11/sync/imsmms_v2p0";
    
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    public static final String POX_VERSION = "V1.0";
    public static final String IMSX_VERSION = "V1.0";
    
    public static boolean isValidMajorCode(String major) {
        return isValidValue(major, VALID_MAJOR);
    }

    public static boolean isValidSeverity(String severity) {
        return isValidValue(severity, VALID_SEVERITY);
    }
    
    public static boolean isValidMinorCode(String minor) {
        return isValidValue(minor, VALID_MINOR);
    }
    
    public static boolean isValidOperation(String operation) {
        return isValidValue(operation, VALID_OPERATIONS);
    }
    
    public static boolean isValidRole(String role) {
        return isValidValue(role, VALID_ROLES);
    }
    
    public static boolean isValidFieldType(String fieldType) {
        return isValidValue(fieldType, VALID_FIELD_TYPES);
    }
    
    public static boolean isValidStatus(String status) {
        return isValidValue(status, VALID_STATUS);
    }
        
    private static boolean isValidValue(String value, List<String> validValues) {
        if (value == null) {
            return false;
        }
        return validValues.contains(value);
    }
}

