package org.sakaiproject.assignment.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.assignment.api.conversion.AssignmentConversionService;
import org.sakaiproject.assignment.api.conversion.AssignmentDataProvider;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.impl.conversion.AssignmentConversionServiceImpl;
import org.sakaiproject.assignment.impl.conversion.O11Assignment;
import org.sakaiproject.assignment.impl.conversion.O11AssignmentContent;
import org.sakaiproject.assignment.impl.conversion.O11Submission;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AssignmentTestConfiguration.class})
public class AssignmentConversionTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final DateTimeFormatter dateTimeFormatter;

    static {
        // DateTimeParseException Text '20171222220000000' could not be parsed at index 0
        // https://bugs.openjdk.java.net/browse/JDK-8031085
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();
    }

    @Autowired private AssignmentConversionService conversion;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private ServerConfigurationService serviceConfigurationService;
    @Autowired private SiteService siteService;

    private AssignmentDataProvider mockDataProvider;

    @Before
    public void setup() {
        mockDataProvider = Mockito.mock(AssignmentDataProvider.class);
        ReflectionTestUtils.setField(conversion, "dataProvider", mockDataProvider);
        Mockito.when(serviceConfigurationService.getStrings(AssignableUUIDGenerator.HIBERNATE_ASSIGNABLE_ID_CLASSES))
                .thenReturn(new String[] {"org.sakaiproject.assignment.api.model.Assignment", "org.sakaiproject.assignment.api.model.AssignmentSubmission"});
        Mockito.when(serviceConfigurationService.getConfigItem(AssignableUUIDGenerator.HIBERNATE_ASSIGNABLE_ID_CLASSES)).thenReturn(null);
        AssignableUUIDGenerator.setServerConfigurationService(serviceConfigurationService);
        try {
            Site site = (Site) Mockito.mock(Site.class);
            Mockito.when(siteService.getSite("2614_G_2015_N_N")).thenReturn(site);
            Group group1 = (Group) Mockito.mock(Group.class);
            Mockito.when(site.getGroup("3d53024d-0f55-44fc-b1dc-acc03ff53b2b")).thenReturn(group1);
            Group group2 = (Group) Mockito.mock(Group.class);
            Mockito.when(site.getGroup("53453ae9-afa2-4983-996b-aeda741bf14b")).thenReturn(group2);
            Role student = (Role) Mockito.mock(Role.class);
            Mockito.when(student.isAllowed(SECURE_ADD_ASSIGNMENT)).thenReturn(false);
            Mockito.when(student.isAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION)).thenReturn(false);
            Role instructor = (Role) Mockito.mock(Role.class);
            Mockito.when(instructor.isAllowed(SECURE_ADD_ASSIGNMENT)).thenReturn(true);
            Mockito.when(instructor.isAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION)).thenReturn(true);
            Set<Member> members = new HashSet<>();
            Member m1 = (Member) Mockito.mock(Member.class);
            members.add(m1);
            Mockito.when(m1.getRole()).thenReturn(student);
            Mockito.when(m1.getUserId()).thenReturn("bd67eb80-721c-44dd-9fee-599be7086ad4");
            Member m2 = (Member) Mockito.mock(Member.class);
            members.add(m2);
            Mockito.when(m2.getRole()).thenReturn(student);
            Mockito.when(m2.getUserId()).thenReturn("cef95910-fbd7-4e3c-bdb9-623061c23686");
            Member m3 = (Member) Mockito.mock(Member.class);
            members.add(m3);
            Mockito.when(m3.getRole()).thenReturn(student);
            Mockito.when(m3.getUserId()).thenReturn("97597bf2-80b1-41ee-a298-d4e0944dc9dc");
            Member m4 = (Member) Mockito.mock(Member.class);
            members.add(m4);
            Mockito.when(m4.getRole()).thenReturn(instructor);
            Mockito.when(m4.getUserId()).thenReturn("aaa7eb80-721c-44dd-9fee-599be7086ad4");
            Mockito.when(group1.getMembers()).thenReturn(members);
            Mockito.when(group2.getMembers()).thenReturn(members);

            Site site2 = (Site) Mockito.mock(Site.class);
            Mockito.when(siteService.getSite("BVCC_942A_9301")).thenReturn(site2);
            Group group1_1 = (Group) Mockito.mock(Group.class);
            Mockito.when(site2.getGroup("8fff8c00-6814-4434-a4d9-50b72ecc4819")).thenReturn(group1_1);
            Set<Member> members1_1 = new HashSet<>();
            Member m1_1 = (Member) Mockito.mock(Member.class);
            members1_1.add(m1_1);
            Mockito.when(m1_1.getRole()).thenReturn(student);
            Mockito.when(m1_1.getUserId()).thenReturn("77597bf2-80b1-41ee-a298-d4e0944dc9dc");            
            Mockito.when(group1_1.getMembers()).thenReturn(members1_1);

        } catch (IdUnusedException iue) {
            log.warn("IdUnusedException: ",iue);
        }
    }

    @Test
    public void decodeBase64() {
        AssignmentConversionServiceImpl.setCleanUTF8(true);
        String text = "PHA+VGhhbmsgeW91IGZvciB5b3VyIGNvbnNpZGVyYXRpb24uIPCfmIogSSBob3BlIHlvdSBoYWQgYSB3b25kZXJmdWwgZGF5LjwvcD4=";
        String decodedText = AssignmentConversionServiceImpl.decodeBase64(text);
        assertEquals("<p>Thank you for your consideration.  I hope you had a wonderful day.</p>", decodedText);

        AssignmentConversionServiceImpl.setCleanUTF8(false);
        decodedText = AssignmentConversionServiceImpl.decodeBase64(text);
        assertEquals("<p>Thank you for your consideration. 😊 I hope you had a wonderful day.</p>", decodedText);
    }

    @Test
    public void deserializeFromXml() {
        O11Assignment o11a = (O11Assignment) conversion.serializeFromXml(readResourceToString("/simple_asn.xml"), O11Assignment.class);
        assertEquals("cac11b82-64ec-4cc3-87b9-cd0385aebd71", o11a.getId());

        O11AssignmentContent o11ac = (O11AssignmentContent) conversion.serializeFromXml(readResourceToString("/simple_asn_content.xml"), O11AssignmentContent.class);
        assertEquals("7854df94-61ca-45e0-9348-a4a3a738292d", o11ac.getId());

        O11Submission o11s = (O11Submission) conversion.serializeFromXml(readResourceToString("/simple_asn_submission.xml"), O11Submission.class);
        assertEquals("aa5d91c4-eeb0-4b51-aaae-3b27991fdac2", o11s.getId());
    }

    @Test
    public void simpleAssignmentConversion() {
        List<String> aList = Arrays.asList("cac11b82-64ec-4cc3-87b9-cd0385aebd71");
        String aXml = readResourceToString("/simple_asn.xml");
        String cXml = readResourceToString("/simple_asn_content.xml");
        List<String> sXml = Arrays.asList(readResourceToString("/simple_asn_submission.xml"));

        Mockito.when(mockDataProvider.fetchAssignmentsToConvert()).thenReturn(aList);
        Mockito.when(mockDataProvider.fetchAssignment("cac11b82-64ec-4cc3-87b9-cd0385aebd71")).thenReturn(aXml);
        Mockito.when(mockDataProvider.fetchAssignmentContent("7854df94-61ca-45e0-9348-a4a3a738292d")).thenReturn(cXml);
        Mockito.when(mockDataProvider.fetchAssignmentSubmissions("cac11b82-64ec-4cc3-87b9-cd0385aebd71")).thenReturn(sXml);

        conversion.runConversion(0, 0);

        // assignment verification
        Map<String, String> assignmentPropertiesToCheck = new HashMap<>();
        assignmentPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("ZTJjNzk1ZTYtY2RmYS00OGZmLTgxZGEtNWE0ZTg0YzI5YWVh"));
        assignmentPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("ZTJjNzk1ZTYtY2RmYS00OGZmLTgxZGEtNWE0ZTg0YzI5YWVh"));
        assignmentPropertiesToCheck.put("new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("YXNzb2NpYXRl"));
        assignmentPropertiesToCheck.put("CHEF:assignment_opendate_announcement_message_id", AssignmentConversionServiceImpl.decodeBase64("NThmZGZkN2MtNzA3Ny00NDkyLWE3ZmQtZmI1NTQzYjFjYmMw"));
        assignmentPropertiesToCheck.put("allow_resubmit_closeTime", AssignmentConversionServiceImpl.decodeBase64("MTUxMzk4MDAwMDAwMA=="));
        assignmentPropertiesToCheck.put("assignment_releasereturn_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlcmV0dXJuX25vdGlmaWNhdGlvbl9lYWNo"));
        assignmentPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNzEyMTUyMTE2MzQ1ODg="));
        assignmentPropertiesToCheck.put("prop_new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("L2Fzc2lnbm1lbnQvYS9DRE9SXzc4OVVfMDYyNi9jYWMxMWI4Mi02NGVjLTRjYzMtODdiOS1jZDAzODVhZWJkNzE="));
        assignmentPropertiesToCheck.put("allow_resubmit_number", AssignmentConversionServiceImpl.decodeBase64("Mg=="));
        assignmentPropertiesToCheck.put("new_assignment_open_date_announced", AssignmentConversionServiceImpl.decodeBase64("dHJ1ZQ=="));
        assignmentPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNzEyMTUyMTE2MzQ0NjA="));
        assignmentPropertiesToCheck.put("new_assignment_check_auto_announce", AssignmentConversionServiceImpl.decodeBase64("dHJ1ZQ=="));
        assignmentPropertiesToCheck.put("newAssignment", AssignmentConversionServiceImpl.decodeBase64("dHJ1ZQ=="));
        assignmentPropertiesToCheck.put("assignment_releasegrade_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlZ3JhZGVfbm90aWZpY2F0aW9uX2VhY2g="));
        assignmentPropertiesToCheck.put("assignment_instructor_notifications_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9pbnN0cnVjdG9yX25vdGlmaWNhdGlvbnNfZWFjaA=="));
        assignmentPropertiesToCheck.put("new_assignment_check_anonymous_grading", null);

        assignmentVerification(
                "cac11b82-64ec-4cc3-87b9-cd0385aebd71",
                true,
                false,
                "20171222220000000",
                false,
                "CDOR_789U_0626",
                "20171215211634441",
                "20171215211634441",
                false,
                "20171222220000000",
                "20171222220000000",
                true,
                true,
                false,
                AssignmentConversionServiceImpl.decodeBase64("PHA+Q29tcGxleCBBc3NpZ25tZW50IGZvciBub24gZ3JvdXBzPC9wPg=="),
                false,
                2000,
                "20171215170000000",
                true,
                null,
                "20171222221000000",
                1,
                true,
                0,
                false,
                100,
                null,
                "Assignment Everyone",
                Assignment.Access.SITE,
                Assignment.GradeType.SCORE_GRADE_TYPE,
                Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION,
                null,
                new HashSet<>(),
                new HashSet<>(),
                assignmentPropertiesToCheck
        );

        // submission verification
        Map<String, String> submissionPropertiesToCheck = new HashMap<>();
        submissionPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("ZDVjMTljNzgtNjQxNC00NGE4LWJlYmQtYjY4NGY4YTZmOTcz"));
        submissionPropertiesToCheck.put("allow_resubmit_closeTime", AssignmentConversionServiceImpl.decodeBase64("MTUxMzk4MDAwMDAwMA=="));
        submissionPropertiesToCheck.put("allow_resubmit_number", AssignmentConversionServiceImpl.decodeBase64("Mg=="));
        submissionPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("ZDVjMTljNzgtNjQxNC00NGE4LWJlYmQtYjY4NGY4YTZmOTcz"));
        submissionPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNzEyMTUyMTE5NDUzMDc="));
        submissionPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNzEyMTUyMTE5NDUyODk="));

        Set<String> submittersToCheck = new HashSet<>();
        submittersToCheck.add("d5c19c78-6414-44a8-bebd-b684f8a6f973");

        submissionVerification(
                "aa5d91c4-eeb0-4b51-aaae-3b27991fdac2",
                "20171215211945307",
                null,
                "20171215211945306",
                0,
                null,
                null,
                false,
                null,
                false,
                false,
                true,
                false,
                true,
                AssignmentConversionServiceImpl.decodeBase64("PHAgY2xhc3M9Im5vcm0iIHN0eWxlPSJjb2xvcjogcmdiKDAsMCwwKTtmb250LWZhbWlseTogQXJpYWwgLCBIZWx2ZXRpY2EgLCBzYW5zLXNlcmlmO2ZvbnQtc2l6ZTogMTIuMHB4O2ZvbnQtc3R5bGU6IG5vcm1hbDtmb250LXdlaWdodDogNDAwO2xldHRlci1zcGFjaW5nOiBub3JtYWw7b3JwaGFuczogMjt0ZXh0LWluZGVudDogMC4wcHg7dGV4dC10cmFuc2Zvcm06IG5vbmU7d2hpdGUtc3BhY2U6IG5vcm1hbDt3aWRvd3M6IDI7d29yZC1zcGFjaW5nOiAwLjBweDsiPlRlcm1pbmFsIHN5bWJvbHMgYXJlIHNob3duIGluPHNwYW4+wqA8L3NwYW4+PGNvZGUgY2xhc3M9ImxpdGVyYWwiPmZpeGVkIHdpZHRoPC9jb2RlPjxzcGFuPsKgPC9zcGFuPmZvbnQgaW4gdGhlIHByb2R1Y3Rpb25zIG9mIHRoZSBsZXhpY2FsIGFuZCBzeW50YWN0aWMgZ3JhbW1hcnMsIGFuZCB0aHJvdWdob3V0IHRoaXMgc3BlY2lmaWNhdGlvbiB3aGVuZXZlciB0aGUgdGV4dCBpcyBkaXJlY3RseSByZWZlcnJpbmcgdG8gc3VjaCBhIHRlcm1pbmFsIHN5bWJvbC4gVGhlc2UgYXJlIHRvIGFwcGVhciBpbiBhIHByb2dyYW0gZXhhY3RseSBhcyB3cml0dGVuLjwvcD4gIDxwIGNsYXNzPSJub3JtIiBzdHlsZT0iY29sb3I6IHJnYigwLDAsMCk7Zm9udC1mYW1pbHk6IEFyaWFsICwgSGVsdmV0aWNhICwgc2Fucy1zZXJpZjtmb250LXNpemU6IDEyLjBweDtmb250LXN0eWxlOiBub3JtYWw7Zm9udC13ZWlnaHQ6IDQwMDtsZXR0ZXItc3BhY2luZzogbm9ybWFsO29ycGhhbnM6IDI7dGV4dC1pbmRlbnQ6IDAuMHB4O3RleHQtdHJhbnNmb3JtOiBub25lO3doaXRlLXNwYWNlOiBub3JtYWw7d2lkb3dzOiAyO3dvcmQtc3BhY2luZzogMC4wcHg7Ij5Ob250ZXJtaW5hbCBzeW1ib2xzIGFyZSBzaG93biBpbjxzcGFuPsKgPC9zcGFuPjxzcGFuIGNsYXNzPSJlbXBoYXNpcyI+PGVtPml0YWxpYzwvZW0+PC9zcGFuPjxzcGFuPsKgPC9zcGFuPnR5cGUuIFRoZSBkZWZpbml0aW9uIG9mIGEgbm9udGVybWluYWwgaXMgaW50cm9kdWNlZCBieSB0aGUgbmFtZSBvZiB0aGUgbm9udGVybWluYWwgYmVpbmcgZGVmaW5lZCwgZm9sbG93ZWQgYnkgYSBjb2xvbi4gT25lIG9yIG1vcmUgYWx0ZXJuYXRpdmUgZGVmaW5pdGlvbnMgZm9yIHRoZSBub250ZXJtaW5hbCB0aGVuIGZvbGxvdyBvbiBzdWNjZWVkaW5nIGxpbmVzLjwvcD4="),
                true,
                null,
                submittersToCheck,
                new HashSet<>(),
                submissionPropertiesToCheck
        );
    }

    @Test
    public void complexAssignmentConversion() {
        List<String> aList = Arrays.asList("447711b7-4148-4be5-912c-3aea3f4dd4b5");
        String aXml = readResourceToString("/complex_asn.xml");
        String cXml = readResourceToString("/complex_asn_content.xml");
        List<String> sXml = Arrays.asList(readResourceToString("/complex_asn_submission.xml"));

        Mockito.when(mockDataProvider.fetchAssignmentsToConvert()).thenReturn(aList);
        Mockito.when(mockDataProvider.fetchAssignment("447711b7-4148-4be5-912c-3aea3f4dd4b5")).thenReturn(aXml);
        Mockito.when(mockDataProvider.fetchAssignmentContent("eb1ea4f1-1a4a-46b9-aaea-928db68b005f")).thenReturn(cXml);
        Mockito.when(mockDataProvider.fetchAssignmentSubmissions("447711b7-4148-4be5-912c-3aea3f4dd4b5")).thenReturn(sXml);

        conversion.runConversion(0, 0);

        Set<String> attachmentsToCheck = new HashSet<>();
        attachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/ea1201e5-cae4-4c93-bda8-85a2218d2560/AssignmentGraph.png");
        attachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/4d99b0ae-26c0-45ac-8386-3f1b19e9179f/SakaiDB.png");
        attachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/9ab07af7-d387-48b8-bfb2-61a77d406150/db_asn.jpg");
        attachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/4b9bd4d7-2053-4d95-b6a2-90d45b8498ca/accusamus-velit-facere-dolorem_3778.txt");

        Map<String, String> assignmentPropertiesToCheck = new HashMap<>();
        assignmentPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("Y2I0ZmZhYmQtNmQ5YS00ZTVhLTk5NjktZGUxYmRhMmVmYmZi"));
        assignmentPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("Y2I0ZmZhYmQtNmQ5YS00ZTVhLTk5NjktZGUxYmRhMmVmYmZi"));
        assignmentPropertiesToCheck.put("new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("YXNzb2NpYXRl"));
        assignmentPropertiesToCheck.put("allow_resubmit_closeTime", AssignmentConversionServiceImpl.decodeBase64("MTUxNjc0NDgwMDAwMA=="));
        assignmentPropertiesToCheck.put("assignment_releasereturn_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlcmV0dXJuX25vdGlmaWNhdGlvbl9lYWNo"));
        assignmentPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDI0MzYzNDg="));
        assignmentPropertiesToCheck.put("prop_new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("L2Fzc2lnbm1lbnQvYS9CVkNDXzk0MkFfOTMwMS80NDc3MTFiNy00MTQ4LTRiZTUtOTEyYy0zYWVhM2Y0ZGQ0YjU="));
        assignmentPropertiesToCheck.put("allow_resubmit_number", AssignmentConversionServiceImpl.decodeBase64("NQ=="));
        assignmentPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDI0MzYzNDQ="));
        assignmentPropertiesToCheck.put("new_assignment_check_auto_announce", AssignmentConversionServiceImpl.decodeBase64("ZmFsc2U="));
        assignmentPropertiesToCheck.put("newAssignment", AssignmentConversionServiceImpl.decodeBase64("dHJ1ZQ=="));
        assignmentPropertiesToCheck.put("assignment_releasegrade_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlZ3JhZGVfbm90aWZpY2F0aW9uX2VhY2g="));
        assignmentPropertiesToCheck.put("assignment_instructor_notifications_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9pbnN0cnVjdG9yX25vdGlmaWNhdGlvbnNfZGlnZXN0"));
        assignmentPropertiesToCheck.put("new_assignment_check_anonymous_grading", null);
        assignmentPropertiesToCheck.put("s_view_report", "false");
        assignmentPropertiesToCheck.put("submit_papers_to", "0");
        assignmentPropertiesToCheck.put("report_gen_speed", "0");
        assignmentPropertiesToCheck.put("institution_check", "false");
        assignmentPropertiesToCheck.put("internet_check", "false");
        assignmentPropertiesToCheck.put("journal_check", "false");
        assignmentPropertiesToCheck.put("s_paper_check", "false");
        assignmentPropertiesToCheck.put("exclude_biblio", "false");
        assignmentPropertiesToCheck.put("exclude_quoted", "false");
        assignmentPropertiesToCheck.put("exclude_type", "0");
        assignmentPropertiesToCheck.put("exclude_value", "1");

        assignmentVerification(
                "447711b7-4148-4be5-912c-3aea3f4dd4b5",
                true,
                true,
                "20180116220000000",
                false,
                "BVCC_942A_9301",
                "20180102202436339",
                "20180102202436339",
                false,
                "20180109220000000",
                "20180109220000000",
                true,
                true,
                false,
                AssignmentConversionServiceImpl.decodeBase64("PHA+Q29tcGxleCBBc3NpZ25tZW50IGZvciBub24gZ3JvdXBzPC9wPg=="),
                false,
                2500,
                "20180102170000000",
                true,
                null,
                "20180123221000000",
                2,
                true,
                0,
                false,
                100,
                null,
                "Complex Assignment",
                Assignment.Access.SITE,
                Assignment.GradeType.SCORE_GRADE_TYPE,
                Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION,
                null,
                new HashSet<>(),
                attachmentsToCheck,
                assignmentPropertiesToCheck
        );

        Map<String, String> submissionPropertiesToCheck = new HashMap<>();
        submissionPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("Nzc1OTdiZjItODBiMS00MWVlLWEyOTgtZDRlMDk0NGRjOWRj"));
        submissionPropertiesToCheck.put("allow_resubmit_closeTime", AssignmentConversionServiceImpl.decodeBase64("MTUxNjc0NDgwMDAwMA=="));
        submissionPropertiesToCheck.put("allow_resubmit_number", AssignmentConversionServiceImpl.decodeBase64("NQ=="));
        submissionPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("Nzc1OTdiZjItODBiMS00MWVlLWEyOTgtZDRlMDk0NGRjOWRj"));
        submissionPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDI5MDI4NjA="));
        submissionPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDI5MDI4NTM="));

        Set<String> submittersToCheck = new HashSet<>();
        submittersToCheck.add("77597bf2-80b1-41ee-a298-d4e0944dc9dc");

        Set<String> submissionAttachmentsToCheck = new HashSet<>();
        submissionAttachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/c918122b-068f-42fe-bb45-3b1abab0c287/Screen Shot 2017-10-18 at 10.24.43 AM.png");

        submissionVerification(
                "8a5b98ea-cb9c-4bb5-a496-6043d4fdb6df",
                "20180102202902860",
                null,
                "20180102202902860",
                0,
                null,
                null,
                false,
                null,
                false,
                false,
                true,
                false,
                false,
                AssignmentConversionServiceImpl.decodeBase64("PHA+MjAwMjQxMjI2IGE7ZGFsO2Zsc2tmamxzZGtmamxkc2ZubHNkbmZsc2RuZmxza2RuZm1sc2Rma21sc2ttZGZsc2ttZGZsa3NtZmRmPC9wPg=="),
                true,
                null,
                submittersToCheck,
                submissionAttachmentsToCheck,
                submissionPropertiesToCheck
        );
    }

    @Test
    public void groupAssignmentConversion() {
        List<String> aList = Arrays.asList("cd9b83ce-6864-453f-ab5d-059fdb2c9e28");
        String aXml = readResourceToString("/group_asn.xml");
        String cXml = readResourceToString("/group_asn_content.xml");
        List<String> sXml = Arrays.asList(readResourceToString("/group_asn_submission.xml"));

        Mockito.when(mockDataProvider.fetchAssignmentsToConvert()).thenReturn(aList);
        Mockito.when(mockDataProvider.fetchAssignment("cd9b83ce-6864-453f-ab5d-059fdb2c9e28")).thenReturn(aXml);
        Mockito.when(mockDataProvider.fetchAssignmentContent("b1dde8c2-ce1f-412d-88e2-5fd5cfda20f6")).thenReturn(cXml);
        Mockito.when(mockDataProvider.fetchAssignmentSubmissions("cd9b83ce-6864-453f-ab5d-059fdb2c9e28")).thenReturn(sXml);

        conversion.runConversion(0, 0);

        Map<String, String> assignmentPropertiesToCheck = new HashMap<>();
        assignmentPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("Y2I0ZmZhYmQtNmQ5YS00ZTVhLTk5NjktZGUxYmRhMmVmYmZi"));
        assignmentPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("Y2I0ZmZhYmQtNmQ5YS00ZTVhLTk5NjktZGUxYmRhMmVmYmZi"));
        assignmentPropertiesToCheck.put("new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("YXNzb2NpYXRl"));
        assignmentPropertiesToCheck.put("assignment_releasereturn_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlcmV0dXJuX25vdGlmaWNhdGlvbl9ub25l"));
        assignmentPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDE3MzEzNjA="));
        assignmentPropertiesToCheck.put("prop_new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("L2Fzc2lnbm1lbnQvYS9CVkNDXzk0MkFfOTMwMS9jZDliODNjZS02ODY0LTQ1M2YtYWI1ZC0wNTlmZGIyYzllMjg="));
        assignmentPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDE3MzEzNTU="));
        assignmentPropertiesToCheck.put("new_assignment_check_auto_announce", AssignmentConversionServiceImpl.decodeBase64("ZmFsc2U="));
        assignmentPropertiesToCheck.put("newAssignment", AssignmentConversionServiceImpl.decodeBase64("dHJ1ZQ=="));
        assignmentPropertiesToCheck.put("assignment_releasegrade_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlZ3JhZGVfbm90aWZpY2F0aW9uX25vbmU="));
        assignmentPropertiesToCheck.put("assignment_instructor_notifications_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9pbnN0cnVjdG9yX25vdGlmaWNhdGlvbnNfZWFjaA=="));
        assignmentPropertiesToCheck.put("new_assignment_check_anonymous_grading", null);
        assignmentPropertiesToCheck.put("s_view_report", "false");
        assignmentPropertiesToCheck.put("submit_papers_to", "0");
        assignmentPropertiesToCheck.put("report_gen_speed", "0");
        assignmentPropertiesToCheck.put("institution_check", "false");
        assignmentPropertiesToCheck.put("internet_check", "false");
        assignmentPropertiesToCheck.put("journal_check", "false");
        assignmentPropertiesToCheck.put("s_paper_check", "false");
        assignmentPropertiesToCheck.put("exclude_biblio", "false");
        assignmentPropertiesToCheck.put("exclude_quoted", "false");
        assignmentPropertiesToCheck.put("exclude_type", "0");
        assignmentPropertiesToCheck.put("exclude_value", "1");

        Set<String> attachmentsToCheck = new HashSet<>();
        attachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/2e901aeb-3dc7-49b4-8ab8-8ce8c0df64d4/AssignmentGraph.png");
        attachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/ba2db179-5a71-4a24-a92d-d07b52431b23/SakaiDB.png");

        Set<String> groupsToCheck = new HashSet<>();
        groupsToCheck.add("/site/BVCC_942A_9301/group/b9ff34b8-1465-4ba8-b532-ed8e097b88fa");
        groupsToCheck.add("/site/BVCC_942A_9301/group/ea70c24b-237f-475a-8146-ff39e1d48345");
        groupsToCheck.add("/site/BVCC_942A_9301/group/8fff8c00-6814-4434-a4d9-50b72ecc4819");

        assignmentVerification(
                "cd9b83ce-6864-453f-ab5d-059fdb2c9e28",
                true,
                false,
                "20180109220000000",
                false,
                "BVCC_942A_9301",
                "20180102201731349",
                "20180102201731349",
                false,
                "20180109220000000",
                "20180109220000000",
                false,
                false,
                false,
                AssignmentConversionServiceImpl.decodeBase64("PHA+R3JvdXAgQXNzaWdubWVudCBGb3IgR3JvdXBzPC9wPiAgPHA+U2luZ2xlIHN1Ym1pc3Npb24gZm9yIGdyb3VwcyBSZWQgR3JlZW4gYW5kIEJsdWUuPC9wPg=="),
                true,
                2500,
                "20180102170000000",
                true,
                null,
                "20180109221000000",
                1,
                true,
                0,
                false,
                100,
                null,
                "Group Assignment For Groups",
                Assignment.Access.GROUP,
                Assignment.GradeType.SCORE_GRADE_TYPE,
                Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION,
                null,
                groupsToCheck,
                attachmentsToCheck,
                assignmentPropertiesToCheck
        );

        Set<String> submissionAttachmentsToCheck = new HashSet<>();
        submissionAttachmentsToCheck.add("/content/attachment/BVCC_942A_9301/Assignments/75b91e14-c12d-4b83-88eb-5cce47343d3a/Screen Shot 2017-10-27 at 10.11.16 AM.png");

        Map<String, String> submissionPropertiesToCheck = new HashMap<>();
        submissionPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("Nzc1OTdiZjItODBiMS00MWVlLWEyOTgtZDRlMDk0NGRjOWRj"));
        submissionPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("Nzc1OTdiZjItODBiMS00MWVlLWEyOTgtZDRlMDk0NGRjOWRj"));
        submissionPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDI4MTcwMTI="));
        submissionPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxODAxMDIyMDI4MTY5ODc="));

        Set<String> submittersToCheck = new HashSet<>();
        submittersToCheck.add("77597bf2-80b1-41ee-a298-d4e0944dc9dc");

        submissionVerification(
                "df39b635-1ce9-480f-98b2-8726c7ea6e78",
                "20180102202817012",
                null,
                "20180102202817011",
                0,
                null,
                null,
                false,
                null,
                false,
                false,
                false,
                false,
                true,
                AssignmentConversionServiceImpl.decodeBase64("PHA+MjAwMjQxMjI2IGFzZGZhc2xmanNhbGpsYWRqbGFkamxhc2Q8L3A+ICA8cD5hc2RsZmtqYXNsZGpsc2RqYTwvcD4gIDxwPsKgPC9wPg=="),
                true,
                "8fff8c00-6814-4434-a4d9-50b72ecc4819",
                submittersToCheck,
                submissionAttachmentsToCheck,
                submissionPropertiesToCheck
        );
    }

    @Test
    public void incompleteAssignmentConversion() {
        List<String> aList = Arrays.asList("0fe0293a-dbd4-4f8c-ba12-cbb60c3d88b5");
        String aXml = readResourceToString("/incomplete_asn.xml");
        String cXml = readResourceToString("/incomplete_asn_content.xml");
        List<String> sXml = Arrays.asList(new String[] {
        		readResourceToString("/incomplete_asn_submission1.xml"),
        		readResourceToString("/incomplete_asn_submission0.xml"),
        		readResourceToString("/incomplete_asn_submission2.xml")});

        Mockito.when(mockDataProvider.fetchAssignmentsToConvert()).thenReturn(aList);
        Mockito.when(mockDataProvider.fetchAssignment("0fe0293a-dbd4-4f8c-ba12-cbb60c3d88b5")).thenReturn(aXml);
        Mockito.when(mockDataProvider.fetchAssignmentContent("0fd801dc-91a8-46d5-80f4-5964a7a4360c")).thenReturn(cXml);
        Mockito.when(mockDataProvider.fetchAssignmentSubmissions("0fe0293a-dbd4-4f8c-ba12-cbb60c3d88b5")).thenReturn(sXml);

        conversion.runConversion(0, 0);

        Map<String, String> assignmentPropertiesToCheck = new HashMap<>();
        assignmentPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("MDBkNGE4NDAtMDEyYy00NzQ5LWJlNjUtNmJmYzM0ODA2NTE1"));
        assignmentPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("MDBkNGE4NDAtMDEyYy00NzQ5LWJlNjUtNmJmYzM0ODA2NTE1"));
        assignmentPropertiesToCheck.put("new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("bm8="));
        assignmentPropertiesToCheck.put("assignment_releasereturn_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlcmV0dXJuX25vdGlmaWNhdGlvbl9ub25l"));
        assignmentPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNTEwMTgyMTA3Mjk3NTA="));
        assignmentPropertiesToCheck.put("prop_new_assignment_add_to_gradebook", null);
        assignmentPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNTEwMTgxOTQxMTkxMDE="));
        assignmentPropertiesToCheck.put("new_assignment_check_auto_announce", AssignmentConversionServiceImpl.decodeBase64("ZmFsc2U="));
        assignmentPropertiesToCheck.put("new_assignment_check_add_due_date", AssignmentConversionServiceImpl.decodeBase64("ZmFsc2U="));
        assignmentPropertiesToCheck.put("assignment_releasegrade_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlZ3JhZGVfbm90aWZpY2F0aW9uX25vbmU="));
        assignmentPropertiesToCheck.put("assignment_instructor_notifications_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9pbnN0cnVjdG9yX25vdGlmaWNhdGlvbnNfZWFjaA=="));
        assignmentPropertiesToCheck.put("new_assignment_check_anonymous_grading", null);

        Set<String> attachmentsToCheck = new HashSet<>();
        attachmentsToCheck.add("/content/attachment/2614_G_2015_N_N/Tareas/6bad591a-b529-4a8d-89e0-fbe41e069589/Tareas para grupos de Seminario S1.pdf");

        Set<String> groupsToCheck = new HashSet<>();
        groupsToCheck.add("/site/2614_G_2015_N_N/group/3d53024d-0f55-44fc-b1dc-acc03ff53b2b");
        groupsToCheck.add("/site/2614_G_2015_N_N/group/53453ae9-afa2-4983-996b-aeda741bf14b");

        assignmentVerification(
                "0fe0293a-dbd4-4f8c-ba12-cbb60c3d88b5",
                true,
                false,
                "20151031225500000",
                false,
                "2614_G_2015_N_N",
                "20151018194119092",
                "20151018194119092",
                false,
                "20151030225500000",
                "20151030225500000",
                false,
                false,
                false,
                AssignmentConversionServiceImpl.decodeBase64("PHA+R3JvdXAgQXNzaWdubWVudCBGb3IgR3JvdXBzPC9wPiAgPHA+U2luZ2xlIHN1Ym1pc3Npb24gZm9yIGdyb3VwcyBSZWQgR3JlZW4gYW5kIEJsdWUuPC9wPg=="),
                true,
                0,
                "20151018100000000",
                true,
                null,
                "20151025161000000",
                1,
                true,
                0,
                false,
                10,
                null,
                "Group Assignment For Groups",
                Assignment.Access.GROUP,
                Assignment.GradeType.UNGRADED_GRADE_TYPE,
                Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION,
                null,
                groupsToCheck,
                attachmentsToCheck,
                assignmentPropertiesToCheck
        );

        Set<String> submissionAttachmentsToCheck = new HashSet<>();
        submissionAttachmentsToCheck.add("/content/attachment/2614_G_2015_N_N/Assignments/76907fe8-d843-4397-9d16-3b0c73d25553/Intervención quirúrgica. S1 G1.docx");

        Map<String, String> submissionPropertiesToCheck = new HashMap<>();
        submissionPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("YmQ2N2ViODAtNzIxYy00NGRkLTlmZWUtNTk5YmU3MDg2YWQ0"));
        submissionPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("YmQ2N2ViODAtNzIxYy00NGRkLTlmZWUtNTk5YmU3MDg2YWQ0"));
        submissionPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNTEwMzAxMzAyMDg0ODM="));
        submissionPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNTEwMzAxMzAyMDgxMzk="));

        Map<String, String> submissionPropertiesToCheck2 = new HashMap<>();
        submissionPropertiesToCheck2.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("Y2VmOTU5MTAtZmJkNy00ZTNjLWJkYjktNjIzMDYxYzIzNjg2"));
        submissionPropertiesToCheck2.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("Y2VmOTU5MTAtZmJkNy00ZTNjLWJkYjktNjIzMDYxYzIzNjg2"));
        submissionPropertiesToCheck2.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNTEwMzAxNDM5MjAyMzA="));
        submissionPropertiesToCheck2.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNTEwMzAxNDM5MTk5NTE="));
        
        Set<String> submittersToCheck = new HashSet<>();
        submittersToCheck.add("bd67eb80-721c-44dd-9fee-599be7086ad4");
        submittersToCheck.add("cef95910-fbd7-4e3c-bdb9-623061c23686");
        submittersToCheck.add("97597bf2-80b1-41ee-a298-d4e0944dc9dc");

        submissionVerification(
                "631c4955-8f90-486d-bf26-e6987f8a1af2",
                "20151030130208483",
                null,
                "20151030130208481",
                null,
                null,
                null,
                false,
                null,
                false,
                false,
                false,
                false,
                true,
                AssignmentConversionServiceImpl.decodeBase64("PHA+MjAwMjQxMjI2IGFzZGZhc2xmanNhbGpsYWRqbGFkamxhc2Q8L3A+ICA8cD5hc2RsZmtqYXNsZGpsc2RqYTwvcD4gIDxwPsKgPC9wPg=="),
                true,
                "3d53024d-0f55-44fc-b1dc-acc03ff53b2b",
                submittersToCheck,
                submissionAttachmentsToCheck,
                submissionPropertiesToCheck
        );
        submissionVerification(
                "6a484d18-49f9-4987-9a88-d8ee17c7fc1c",
                "20151030143920230",
                null,
                "20151030143920227",
                null,
                null,
                null,
                false,
                null,
                false,
                false,
                false,
                false,
                true,
                AssignmentConversionServiceImpl.decodeBase64("PHA+MjAwMjQxMjI2IGFzZGZhc2xmanNhbGpsYWRqbGFkamxhc2Q8L3A+ICA8cD5hc2RsZmtqYXNsZGpsc2RqYTwvcD4gIDxwPsKgPC9wPg=="),
                true,
                "53453ae9-afa2-4983-996b-aeda741bf14b",
                submittersToCheck,
                submissionAttachmentsToCheck,
                submissionPropertiesToCheck2
        );
        // Submission0 is not valid doesn't belong to any assignment group
        AssignmentSubmission submission = assignmentRepository.findSubmission("xxxc4955-8f90-486d-bf26-e6987f8a1af2");
        assertEquals(submission,null);
        
    }
    
    @Test
    public void siteGroupAssignmentConversion() {
        List<String> aList = Arrays.asList("0434fb09-ca9a-4808-a9fc-b3f7c88a7134");
        String aXml = readResourceToString("/sitegrp_asn.xml");
        String cXml = readResourceToString("/sitegrp_asn_content.xml");
        List<String> sXml = Arrays.asList(new String[] {
        		readResourceToString("/sitegrp_asn_submission0.xml"),
        		readResourceToString("/sitegrp_asn_submission1.xml")});

        Mockito.when(mockDataProvider.fetchAssignmentsToConvert()).thenReturn(aList);
        Mockito.when(mockDataProvider.fetchAssignment("0434fb09-ca9a-4808-a9fc-b3f7c88a7134")).thenReturn(aXml);
        Mockito.when(mockDataProvider.fetchAssignmentContent("5853c01f-aae8-48b6-bb79-fc4636e85954")).thenReturn(cXml);
        Mockito.when(mockDataProvider.fetchAssignmentSubmissions("0434fb09-ca9a-4808-a9fc-b3f7c88a7134")).thenReturn(sXml);

        conversion.runConversion(0, 0);

        Map<String, String> assignmentPropertiesToCheck = new HashMap<>();
        assignmentPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("OTg2NGRkYmUtOTQ1OS00OTE1LWEzYzktNjUzZjVjNzI1ZWYz"));
        assignmentPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("OTg2NGRkYmUtOTQ1OS00OTE1LWEzYzktNjUzZjVjNzI1ZWYz"));
        assignmentPropertiesToCheck.put("new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("YXNzb2NpYXRl"));
        assignmentPropertiesToCheck.put("assignment_releasereturn_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlcmV0dXJuX25vdGlmaWNhdGlvbl9ub25l"));
        assignmentPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNjAyMjkxMDMyNDk5MjE="));
        assignmentPropertiesToCheck.put("prop_new_assignment_add_to_gradebook", AssignmentConversionServiceImpl.decodeBase64("L2Fzc2lnbm1lbnQvYS8xNTk3X0dfMjAxNV9OX04vMDQzNGZiMDktY2E5YS00ODA4LWE5ZmMtYjNmN2M4OGE3MTM0"));
        assignmentPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNjAyMjkxMDI3MTA1ODI="));
        assignmentPropertiesToCheck.put("new_assignment_check_auto_announce", AssignmentConversionServiceImpl.decodeBase64("dHJ1ZQ=="));
        assignmentPropertiesToCheck.put("new_assignment_check_add_due_date", AssignmentConversionServiceImpl.decodeBase64("ZmFsc2U="));
        assignmentPropertiesToCheck.put("assignment_releasegrade_notification_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9yZWxlYXNlZ3JhZGVfbm90aWZpY2F0aW9uX2VhY2g="));
        assignmentPropertiesToCheck.put("assignment_instructor_notifications_value", AssignmentConversionServiceImpl.decodeBase64("YXNzaWdubWVudF9pbnN0cnVjdG9yX25vdGlmaWNhdGlvbnNfZGlnZXN0"));
        assignmentPropertiesToCheck.put("new_assignment_check_anonymous_grading", null);

        Set<String> attachmentsToCheck = new HashSet<>();
        Set<String> groupsToCheck = new HashSet<>();
        groupsToCheck.add("/site/2614_G_2015_N_N/group/53453ae9-afa2-4983-996b-aeda741bf14b");
        groupsToCheck.add("/site/2614_G_2015_N_N/group/3d53024d-0f55-44fc-b1dc-acc03ff53b2b");

        assignmentVerification(
                "0434fb09-ca9a-4808-a9fc-b3f7c88a7134",
                true,
                false,
                "20160417200000000",
                false,
                "2614_G_2015_N_N",
                "20160229102710547",
                "20160229102710547",
                false,
                "20160417200000000",
                "20160417200000000",
                false,
                true,
                false,
                AssignmentConversionServiceImpl.decodeBase64("PHA+R3JvdXAgQXNzaWdubWVudCBGb3IgR3JvdXBzPC9wPiAgPHA+U2luZ2xlIHN1Ym1pc3Npb24gZm9yIGdyb3VwcyBSZWQgR3JlZW4gYW5kIEJsdWUuPC9wPg=="),
                true,
                100,
                "20160229090000000",
                false,
                null,
                "20160320211000000",
                0,
                false,
                0,
                false,
                10,
                null,
                "Site Group Assignment For Groups",
                Assignment.Access.GROUP,
                Assignment.GradeType.SCORE_GRADE_TYPE,
                Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION,
                null,
                groupsToCheck,
                attachmentsToCheck,
                assignmentPropertiesToCheck
        );

        Set<String> submissionAttachmentsToCheck = new HashSet<>();
        submissionAttachmentsToCheck.add("/content/attachment/1597_G_2015_N_N/Assignments/e03db00c-24d3-4460-9a13-2f6aa46e0535/CFL.pdf");
        submissionAttachmentsToCheck.add("/content/attachment/1597_G_2015_N_N/Assignments/2111b71d-0479-4eab-bbf7-bd9b34d05ccc/Simulacion_CFL.zip");

        Map<String, String> submissionPropertiesToCheck = new HashMap<>();
        submissionPropertiesToCheck.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("MDgyODEwYjItZGRiYi00YTA1LWIzMzYtMDYyMzU3NDQxOWU0"));
        submissionPropertiesToCheck.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("Yjk0MmIyYTAtNTg5Yy00OTE0LWFkYTctYWI4MGI4ZWVlOTRl"));
        submissionPropertiesToCheck.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNjA0MTcxOTU5NTA5NDY="));
        submissionPropertiesToCheck.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNjA0MTcxNzMzNTQ5MDQ="));

        Map<String, String> submissionPropertiesToCheck2 = new HashMap<>();
        submissionPropertiesToCheck2.put("CHEF:creator", AssignmentConversionServiceImpl.decodeBase64("OTE2ODliN2EtYzM5Yy00OTE4LWEwZTQtMTNmYTBjZTVkZWMw"));
        submissionPropertiesToCheck2.put("CHEF:modifiedby", AssignmentConversionServiceImpl.decodeBase64("OTE2ODliN2EtYzM5Yy00OTE4LWEwZTQtMTNmYTBjZTVkZWMw"));
        submissionPropertiesToCheck2.put("DAV:getlastmodified", AssignmentConversionServiceImpl.decodeBase64("MjAxNjA0MTcxOTI1MzM3Mzk="));
        submissionPropertiesToCheck2.put("DAV:creationdate", AssignmentConversionServiceImpl.decodeBase64("MjAxNjA0MTcxOTI1MzM0MDI="));
        
        Set<String> submittersToCheck = new HashSet<>();
        submittersToCheck.add("e8126235-b1c5-4e5b-bc0f-bfb9421fd95b");
        submittersToCheck.add("3522f934-133a-4c97-aafd-5dbccf51efb9");
        submittersToCheck.add("b942b2a0-589c-4914-ada7-ab80b8eee94e");
        
        Set<String> submittersToCheck2 = new HashSet<>();
        submittersToCheck2.add("bd67eb80-721c-44dd-9fee-599be7086ad4");
        submittersToCheck2.add("cef95910-fbd7-4e3c-bdb9-623061c23686");
        submittersToCheck2.add("97597bf2-80b1-41ee-a298-d4e0944dc9dc");

        submissionVerification(
                "b3ec1478-1ac2-4da8-bab0-65bd739370d9",
                "20160417195950947",
                null,
                "20160417195950944",
                null,
                null,
                null,
                false,
                null,
                false,
                false,
                true,
                false,
                true,
                AssignmentConversionServiceImpl.decodeBase64("PHA+RW50cmVnYSBncnVwYWw8L3A+"),
                true,
                "3d53024d-0f55-44fc-b1dc-acc03ff53b2b",
                submittersToCheck,
                submissionAttachmentsToCheck,
                submissionPropertiesToCheck
        );
        submissionVerification(
                "20207596-5286-42cf-b7f1-7462e83f10d6",
                "20160417192533739",
                null,
                "20160417192533733",
                null,
                null,
                null,
                false,
                null,
                false,
                false,
                true,
                false,
                true,
                null,
                true,
                "53453ae9-afa2-4983-996b-aeda741bf14b",
                submittersToCheck2,
                submissionAttachmentsToCheck,
                submissionPropertiesToCheck2
        );
        assertTrue(Boolean.TRUE);
    }
    
    private void assignmentVerification(String aId,
                                        Boolean cAllowAttachments,
                                        Boolean aAllowPeerAssessment,
                                        String aCloseDate,
                                        Boolean aContentReview,
                                        String aContext,
                                        String cDateCreated,
                                        String cDateModified,
                                        Boolean aDraft,
                                        String aDropDeadDate,
                                        String aDueDate,
                                        Boolean cHideDueDate,
                                        Boolean cHonorPledge,
                                        Boolean cIndividuallyGraded,
                                        String cInstructions,
                                        Boolean aIsGroup,
                                        Integer cMaxGradePoint,
                                        String aOpenDate,
                                        Boolean aPeerAssessmentAnonEval,
                                        String aPeerAssessmentInstructions,
                                        String aPeerAssessmentPeriodDate,
                                        Integer aPeerAssessmentNumberReviews,
                                        Boolean aPeerAssessmentStudentReview,
                                        Integer aPosition,
                                        Boolean aReleaseGrades,
                                        Integer cScaleFactor,
                                        String aSection,
                                        String aTitle,
                                        Assignment.Access aAccess,
                                        Assignment.GradeType cTypeOfGrade,
                                        Assignment.SubmissionType cTypeOfSubmission,
                                        String aVisibleDate,
                                        Set<String> aGroups,
                                        Set<String> cAttachments,
                                        Map<String, String> assignmentPropertiesToCheck) {
        Assignment assignment = assignmentRepository.findAssignment(aId);

        // assignment verification
        assertNotNull("could not load assignment " + aId, assignment);
        assertEquals(aId, assignment.getId());
        assertEquals(cAllowAttachments, assignment.getAllowAttachments());
        assertEquals(aAllowPeerAssessment, assignment.getAllowPeerAssessment());
        assertEquals(aCloseDate, convertInstantToString(assignment.getCloseDate()));
        assertEquals(aContentReview, assignment.getContentReview());
        assertEquals(aContext, assignment.getContext());
        assertEquals(cDateCreated, convertInstantToString(assignment.getDateCreated()));
        assertEquals(cDateModified, convertInstantToString(assignment.getDateModified()));
        assertEquals(aDraft, assignment.getDraft());
        assertEquals(aDropDeadDate, convertInstantToString(assignment.getDropDeadDate()));
        assertEquals(aDueDate, convertInstantToString(assignment.getDueDate()));
        assertEquals(cHideDueDate, assignment.getHideDueDate());
        assertEquals(cHonorPledge, assignment.getHonorPledge());
        assertEquals(cIndividuallyGraded, assignment.getIndividuallyGraded());
        assertEquals(cInstructions, assignment.getInstructions());
        assertEquals(aIsGroup, assignment.getIsGroup());
        assertEquals(cMaxGradePoint, assignment.getMaxGradePoint());
        assertEquals(aOpenDate, convertInstantToString(assignment.getOpenDate()));
        assertEquals(aPeerAssessmentAnonEval, assignment.getPeerAssessmentAnonEval());
        assertEquals(aPeerAssessmentInstructions, assignment.getPeerAssessmentInstructions());
        assertEquals(aPeerAssessmentNumberReviews, assignment.getPeerAssessmentNumberReviews());
        assertEquals(aPeerAssessmentPeriodDate, convertInstantToString(assignment.getPeerAssessmentPeriodDate()));
        assertEquals(aPeerAssessmentStudentReview, assignment.getPeerAssessmentStudentReview());
        assertEquals(aPosition, assignment.getPosition());
        assertEquals(aReleaseGrades, assignment.getReleaseGrades());
        assertEquals(cScaleFactor, assignment.getScaleFactor());
        assertEquals(aSection, assignment.getSection());
        assertEquals(aTitle, assignment.getTitle());
        assertEquals(aAccess, assignment.getTypeOfAccess());
        assertEquals(cTypeOfGrade, assignment.getTypeOfGrade());
        assertEquals(cTypeOfSubmission, assignment.getTypeOfSubmission());
        assertEquals(aVisibleDate, convertInstantToString(assignment.getVisibleDate()));
        assertEquals(cAttachments, assignment.getAttachments());
        assertEquals(aGroups, assignment.getGroups());

        assertTrue(compareProperties(assignment.getProperties(), assignmentPropertiesToCheck));
    }

    private void submissionVerification(String sId,
                                        String sDateModified,
                                        String sDateReturned,
                                        String sDateSubmitted,
                                        Integer sFactor,
                                        String sFeedbackComment,
                                        String sFeedbackText,
                                        Boolean sGraded,
                                        String sGradedBy,
                                        Boolean sGradeReleased,
                                        Boolean sHiddenDueDate,
                                        Boolean sHonorPledge,
                                        Boolean sReturned,
                                        Boolean sSubmitted,
                                        String sSubmittedText,
                                        Boolean sUserSubmission,
                                        String sGroupId,
                                        Set<String> sSubmitters,
                                        Set<String> sAttachmentsToCheck,
                                        Map<String, String> submissionPropertiesToCheck) {
        // submission verification
        AssignmentSubmission submission = assignmentRepository.findSubmission(sId);
        assertNotNull("could not load submission " + sId, submission);
        assertEquals(sId, submission.getId());
        assertEquals(sDateModified, convertInstantToString(submission.getDateModified()));
        assertEquals(sDateReturned, convertInstantToString(submission.getDateReturned()));
        assertEquals(sDateSubmitted, convertInstantToString(submission.getDateSubmitted()));
        assertEquals(sFactor, submission.getFactor());
        assertEquals(sFeedbackComment, submission.getFeedbackComment());
        assertEquals(sFeedbackText, submission.getFeedbackText());
        assertNull(submission.getGrade());
        assertEquals(sGraded, submission.getGraded());
        assertEquals(sGradedBy, submission.getGradedBy());
        assertEquals(sGradeReleased, submission.getGradeReleased());
        assertEquals(sHiddenDueDate, submission.getHiddenDueDate());
        assertEquals(sHonorPledge, submission.getHonorPledge());
        assertEquals(sReturned, submission.getReturned());
        assertEquals(sSubmitted, submission.getSubmitted());
        assertEquals(sSubmittedText, submission.getSubmittedText());
        assertEquals(sUserSubmission, submission.getUserSubmission());
        assertEquals(sGroupId, submission.getGroupId());
        assertEquals(sAttachmentsToCheck, submission.getAttachments());

        Set<String> submitters = submission.getSubmitters().stream().map(AssignmentSubmissionSubmitter::getSubmitter).collect(Collectors.toSet());
        assertEquals(sSubmitters, submitters);

        assertTrue(compareProperties(submission.getProperties(), submissionPropertiesToCheck));
    }

    private String readResourceToString(String resource) {
        InputStream is = this.getClass().getResourceAsStream(resource);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines().collect(Collectors.joining("\n"));
    }

    private String convertInstantToString(Instant time) {
        if (time != null) {
            try {
                return dateTimeFormatter.withZone(ZoneOffset.UTC).format(time);
            } catch (DateTimeException dte) {
                log.warn("could not parse time: {}, {}", time, dte.getMessage());
            }
        }
        return null;
    }

    private boolean compareProperties(Map<String, String> properties, Map<String, String> propertiesToCheck) {
        Set<Map.Entry<String, String>> nonMatches = propertiesToCheck.entrySet().stream()
                .filter(e -> !StringUtils.equals(e.getValue(), properties.get(e.getKey())))
                .collect(Collectors.toSet());

        if (!nonMatches.isEmpty()) {
            nonMatches.forEach(e -> log.warn("the following properties differ key: {}, values: [{}|{}]", e.getKey(), e.getValue(), properties.get(e.getKey())));
            return false;
        }
        return true;
    }
}
