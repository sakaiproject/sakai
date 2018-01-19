package org.sakaiproject.assignment.impl.conversion.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.conversion.AssignmentConversionService;
import org.sakaiproject.assignment.api.conversion.AssignmentDataProvider;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.persistence.AssignmentRepository;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.sakaiproject.util.BasicConfigItem;

@Slf4j
public class AssignmentConversionServiceImpl implements AssignmentConversionService {

    @Setter private static boolean cleanUTF8 = true;
    @Setter private static String replacementUTF8 = "";
    private static final DateTimeFormatter dateTimeFormatter;
    private static final XmlMapper xmlMapper;

    static {
        // DateTimeParseException Text '20171222220000000' could not be parsed at index 0
        // https://bugs.openjdk.java.net/browse/JDK-8031085
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();

        SimpleModule stringModule = new SimpleModule();
        stringModule.addDeserializer(String.class, new StdDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                String str = StringDeserializer.instance.deserialize(p, ctxt);
                if (StringUtils.isBlank(str)) return null;
                return str;
            }
        });

        xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new Jdk8Module());
        xmlMapper.registerModule(stringModule);
    }

    @Setter private AssignmentRepository assignmentRepository;
    @Setter private AssignmentDataProvider dataProvider;
    @Setter private ServerConfigurationService serverConfigurationService;

    private int assignmentsMigrated;
    private int submissionsMigrated;
    private int submissionsFailed;
    private int assignmentsFailed;
    private int assignmentsTotal;
    private int progress;

    public void init() {
        cleanUTF8 = serverConfigurationService.getBoolean("content.cleaner.filter.utf8", Boolean.TRUE);
        replacementUTF8 = serverConfigurationService.getString("content.cleaner.filter.utf8.replacement", "");
    }

    @Override
    public Object serializeFromXml(String xml, Class clazz) {
        // only for O11Assignment do we need to do some group processing
        if (O11Assignment.class.equals(clazz)) {
            xml = adjustXmlForGroups(xml);
        }

        if (StringUtils.isNotBlank(xml)) {
            try {
                return xmlMapper.readValue(xml, clazz);
            } catch (IOException ioe) {
                log.warn("deserialization failed for xml: {}\n{}", xml, ioe.getMessage());
            }
        }
        return null;
    }

    @Override
    public void runConversion() {
        assignmentsMigrated = submissionsMigrated = submissionsFailed = assignmentsFailed = assignmentsTotal = progress = 0;

        String configValue = "org.sakaiproject.assignment.api.model.Assignment,org.sakaiproject.assignment.api.model.AssignmentSubmission";
        String currentValue = serverConfigurationService.getConfig(AssignableUUIDGenerator.HIBERNATE_ASSIGNABLE_ID_CLASSES, null);
        if (StringUtils.isNotBlank(currentValue)) {
            configValue = configValue + "," + currentValue;
        }
        ServerConfigurationService.ConfigItem configItem = BasicConfigItem.makeConfigItem(
                AssignableUUIDGenerator.HIBERNATE_ASSIGNABLE_ID_CLASSES,
                configValue,
                AssignmentConversionServiceImpl.class.getName(),
                true);
        serverConfigurationService.registerConfigItem(configItem);

        List<String> preAssignments = dataProvider.fetchAssignmentsToConvert();
        List<String> postAssignments = assignmentRepository.findAllAssignmentIds();
        List<String> convertAssignments = new ArrayList<>(preAssignments);
        convertAssignments.removeAll(postAssignments);
        assignmentsTotal = convertAssignments.size();

        log.info("<===== Assignments pre 12 {} and post 12 {} to migrate {} =====>", preAssignments.size(), postAssignments.size(), assignmentsTotal);

        for (String assignment : convertAssignments) {
            convert(assignment);
            int percent = new Double(((assignmentsMigrated + assignmentsFailed) / (double) assignmentsTotal) * 100).intValue();
            if (progress != percent) {
                progress = percent;
                log.info("<===== Assignments migration completed {}%", percent);
            }
        }

        configItem = BasicConfigItem.makeConfigItem(
                AssignableUUIDGenerator.HIBERNATE_ASSIGNABLE_ID_CLASSES,
                StringUtils.trimToEmpty(currentValue),
                AssignmentConversionServiceImpl.class.getName());
        serverConfigurationService.registerConfigItem(configItem);

        log.info("<===== Assignments migrated {} =====>", assignmentsMigrated);
        log.info("<===== Submissions migrated {} =====>", submissionsMigrated);
        log.info("<===== Assignments that failed to be migrated {} =====>", assignmentsFailed);
        log.info("<===== Submissions that failed to be migrated {} =====>", submissionsFailed);
    }

    private String adjustXmlForGroups(String xml) {
        if (StringUtils.isBlank(xml)) return null;
        // special processing for assignments in order to deserialize groups correctly
        // see https://stackoverflow.com/questions/47199799/jackson-xml-tag-and-attribute-with-the-same-name
        // see https://github.com/FasterXML/jackson-dataformat-xml/issues/65
        try (InputStream in = new ByteArrayInputStream(xml.getBytes());
             OutputStream out = new ByteArrayOutputStream()) {

            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(in);
            XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(out);
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            List<XMLEvent> groupEvents = new ArrayList<>();

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement() && "group".equals(event.asStartElement().getName().getLocalPart())) {
                    for (; !event.isEndElement(); event = reader.nextEvent()) {
                        groupEvents.add(event);
                    }
                    groupEvents.add(event);
                } else if (event.isEndElement() && "assignment".equals(event.asEndElement().getName().getLocalPart())) {
                    writer.add(eventFactory.createStartElement("", "", "groups"));
                    for (XMLEvent e : groupEvents) {
                        writer.add(e);
                    }
                    writer.add(eventFactory.createEndElement("", "", "groups"));
                    writer.add(event);
                } else {
                    writer.add(event);
                }
            }
            writer.flush();
            String adjustedXml = out.toString();
            log.debug("adjusted groups in xml: {}", adjustedXml);
            return adjustedXml;
        } catch (XMLStreamException xse) {
            log.warn("xml parsing error while adjusting for groups, {}\n{}", xse.getMessage(), xml);
        } catch (IOException ioe) {
            log.warn("could not adjust xml for groups, {}\n{}", ioe.getMessage(), xml);
        }
        return null;
    }
    private void convert(String assignmentId) {
        String aXml = dataProvider.fetchAssignment(assignmentId);
        if (StringUtils.isNotBlank(aXml)) {
            O11Assignment o11a = (O11Assignment) serializeFromXml(aXml, O11Assignment.class);
            if (o11a != null) {
                String contentReference = o11a.getAssignmentcontent();
                String contentId = AssignmentReferenceReckoner.reckoner().reference(contentReference).reckon().getId();
                String cXml = dataProvider.fetchAssignmentContent(contentId);
                if (StringUtils.isNotBlank(cXml)) {
                    O11AssignmentContent o11ac = (O11AssignmentContent) serializeFromXml(cXml, O11AssignmentContent.class);
                    if (o11ac != null) {
                        Assignment assignment = assignmentReintegration(o11a, o11ac);

                        if (assignment != null) {
                            List<String> sXml = dataProvider.fetchAssignmentSubmissions(assignmentId);
                            for (String xml : sXml) {
                                O11Submission o11s = (O11Submission) serializeFromXml(xml, O11Submission.class);
                                if (o11s != null) {
                                    AssignmentSubmission submission = submissionReintegration(assignment, o11s);
                                    if (submission != null) {
                                        submission.setAssignment(assignment);
                                        assignment.getSubmissions().add(submission);
                                    } else {
                                        log.warn("reintegration of submission {} in assignment {} failed skipping submission", o11s.getId(), assignmentId);
                                        submissionsFailed++;
                                    }
                                } else {
                                    log.warn("deserialization of a submission failed in assignment {} skipping submission", assignmentId);
                                    submissionsFailed++;
                                }
                            }

                            // at this point everything has been added to the persistence context
                            // so we just need to merge and flush so that every assignment is persisted
                            try {
                                assignmentRepository.merge(assignment);
                                assignmentsMigrated++;
                                submissionsMigrated += assignment.getSubmissions().size();
                            } catch (HibernateException he) {
                                log.warn("could not persist assignment {}, {}", assignmentId, he.getMessage());
                                assignmentsFailed++;
                                submissionsFailed += assignment.getSubmissions().size();
                            }
                        } else {
                            log.warn("reintegration of assignment {} and content {} failed skipping assignment", assignmentId, contentId);
                            assignmentsFailed++;
                        }
                    } else {
                        log.warn("deserialization of content {} failed skipping assignment {}", contentId, assignmentId);
                        assignmentsFailed++;
                    }
                } else {
                    log.warn("content {} xml is invalid skipping assignment {}", contentId, assignmentId);
                    assignmentsFailed++;
                }
            } else {
                log.warn("deserialization of assignment {} failed skipping", assignmentId);
                assignmentsFailed++;
            }
        } else {
            log.warn("assignment {} xml is not valid skipping", assignmentId);
            assignmentsFailed++;
        }
    }

    private Assignment assignmentReintegration(O11Assignment assignment, O11AssignmentContent content) {
        Map<String, Object> assignmentAny = assignment.getAny();
        Map<String, Object> contentAny = content.getAny();
        String[] assignmentAnyKeys = assignmentAny.keySet().toArray(new String[assignmentAny.size()]);
        String[] contentAnyKeys = contentAny.keySet().toArray(new String[contentAny.size()]);
        Predicate<String> attachmentFilter = Pattern.compile("attachment\\d+").asPredicate();

        Assignment a = new Assignment();
        a.setAllowAttachments(content.getAllowattach());
        a.setAllowPeerAssessment(assignment.getAllowpeerassessment());
        a.setCloseDate(convertStringToTime(assignment.getClosedate()));
        a.setContentReview(content.getAllowreview());
        a.setContext(assignment.getContext());
        a.setDateCreated(convertStringToTime(content.getDatecreated()));
        a.setDateModified(convertStringToTime(content.getLastmod()));
        a.setDraft(assignment.getDraft());
        a.setDropDeadDate(convertStringToTime(assignment.getDropdeaddate()));
        a.setDueDate(convertStringToTime(assignment.getDuedate()));
        a.setHideDueDate(content.getHideduedate());
        a.setHonorPledge(content.getHonorpledge() == 2 ? Boolean.TRUE : Boolean.FALSE);
        a.setId(assignment.getId());
        a.setIndividuallyGraded(content.getIndivgraded());
        a.setInstructions(decodeBase64(content.getInstructionsHtml()));
        a.setIsGroup(assignment.getGroup());
        a.setMaxGradePoint(content.getScaled_maxgradepoint());
        a.setOpenDate(convertStringToTime(assignment.getOpendate()));
        a.setPeerAssessmentAnonEval(assignment.getPeerassessmentanoneval());
        a.setPeerAssessmentInstructions(assignment.getPeerassessmentinstructions());
        a.setPeerAssessmentNumberReviews(assignment.getPeerassessmentnumreviews());
        a.setPeerAssessmentPeriodDate(convertStringToTime(assignment.getPeerassessmentperiodtime()));
        a.setPeerAssessmentStudentReview(assignment.getPeerassessmentstudentviewreviews());
        a.setPosition(assignment.getPosition_order());
        a.setReleaseGrades(content.getReleasegrades());
        a.setScaleFactor(content.getScaled_factor());
        a.setSection(assignment.getSection());
        a.setTitle(assignment.getTitle());
        a.setTypeOfAccess("site".equals(assignment.getAccess()) ? Assignment.Access.SITE : Assignment.Access.GROUP);
        a.setTypeOfGrade(Assignment.GradeType.values()[content.getTypeofgrade()]);
        a.setTypeOfSubmission(Assignment.SubmissionType.values()[content.getSubmissiontype()]);
        a.setVisibleDate(convertStringToTime(assignment.getVisibledate()));

        // support for list of attachment0
        Set<String> attachmentKeys = Arrays.stream(contentAnyKeys).filter(attachmentFilter).collect(Collectors.toSet());
        attachmentKeys.forEach(k -> a.getAttachments().add((String) contentAny.get(k)));

        Map<String, String> properties = a.getProperties();
        if (content.getAllowstudentview() != null) properties.put("s_view_report", Boolean.toString(content.getAllowstudentview()));
        if (content.getSubmitReviewRepo() != null) properties.put("submit_papers_to", content.getSubmitReviewRepo().toString());
        if (content.getGenerateOriginalityReport() != null) properties.put("report_gen_speed", content.getGenerateOriginalityReport().toString());
        if (content.getCheckInstitution() != null) properties.put("institution_check", content.getCheckInstitution().toString());
        if (content.getCheckInternet() != null) properties.put("internet_check", content.getCheckInternet().toString());
        if (content.getCheckPublications() != null) properties.put("journal_check", content.getCheckPublications().toString());
        if (content.getCheckTurnitin() != null) properties.put("s_paper_check", content.getCheckTurnitin().toString());
        if (content.getExcludeBibliographic() != null) properties.put("exclude_biblio", content.getExcludeBibliographic().toString());
        if (content.getExcludeQuoted() != null) properties.put("exclude_quoted", content.getExcludeQuoted().toString());
        // properties.put("exclude_self_plag", content.?);
        // properties.put("store_inst_index", content.?);
        // properties.put("student_preview", content.?);
        if (content.getExcludeType() != null) properties.put("exclude_type", content.getExcludeType().toString());
        if (content.getExcludeValue() != null) properties.put("exclude_value", content.getExcludeValue().toString());

        // add any keys that we don't convert explicitly as a property
        // this covers the case where institutions may have their own features
        Set<String> extraAssignmentKeys = Arrays.stream(assignmentAnyKeys).collect(Collectors.toSet());
        extraAssignmentKeys.forEach(k -> properties.put(k, (String) assignmentAny.get(k)));

        Set<String> extraContentKeys = Arrays.stream(contentAnyKeys)
                .filter(attachmentFilter.negate())
                .collect(Collectors.toSet());
        extraContentKeys.forEach(k -> properties.put(k, (String) contentAny.get(k)));

        content.getProperties().forEach(p -> properties.put(p.getName(), p.getDecodedValue()));

        for (O11Property property : assignment.getProperties()) {
            properties.put(property.getName(), property.getDecodedValue());
            if (ResourceProperties.PROP_MODIFIED_BY.equals(property.getName())) {
                a.setModifier(property.getDecodedValue());
            } else if (ResourceProperties.PROP_ASSIGNMENT_DELETED.equals(property.getName())) {
                a.setDeleted("true".equals(property.getDecodedValue()) ? Boolean.TRUE : Boolean.FALSE);
            } else if (ResourceProperties.PROP_CREATOR.equals(property.getName())) {
                a.setAuthor(property.getDecodedValue());
            }
        }

        if (a.getTypeOfAccess() == Assignment.Access.GROUP) {
            assignment.getGroups().forEach(g -> a.getGroups().add(g.getAuthzGroup()));
        }

        return a;
    }

    private AssignmentSubmission submissionReintegration(Assignment assignment, O11Submission submission) {
        Map<String, Object> submissionAny = submission.getAny();
        String[] submissionAnyKeys = submissionAny.keySet().toArray(new String[submissionAny.size()]);
        Predicate<String> submitterFilter = Pattern.compile("submitter\\d+").asPredicate();
        Predicate<String> feedbackAttachmentFilter = Pattern.compile("feedbackattachment\\d+").asPredicate();
        Predicate<String> submittedAttachmentFilter = Pattern.compile("submittedattachment\\d+").asPredicate();


        AssignmentSubmission s = new AssignmentSubmission();
        Map<String, String> properties = s.getProperties();

        // add any keys that we don't convert explicitly as a property
        // this covers the case where institutions may have their own features
        Set<String> extraKeys = Arrays.stream(submissionAnyKeys)
                .filter(submitterFilter.negate())
                .filter(feedbackAttachmentFilter.negate())
                .filter(submittedAttachmentFilter.negate())
                .collect(Collectors.toSet());
        extraKeys.forEach(k -> properties.put(k, (String) submissionAny.get(k)));

        s.setDateModified(convertStringToTime(submission.getLastmod()));
        s.setDateReturned(convertStringToTime(submission.getDatereturned()));
        s.setDateSubmitted(convertStringToTime(submission.getDatesubmitted()));
        s.setFactor(submission.getScaled_factor());
        s.setFeedbackComment(decodeBase64(submission.getFeedbackcommentHtml()));
        s.setFeedbackText(decodeBase64(submission.getFeedbacktextHtml()));
        s.setGrade(submission.getScaled_grade());
        s.setGraded(submission.getGraded());
        s.setGradedBy(submission.getGradedBy());
        s.setGradeReleased(submission.getGradereleased());
        s.setHiddenDueDate(submission.getHideduedate());
        s.setHonorPledge(submission.getPledgeflag());
        s.setId(submission.getId());
        s.setReturned(submission.getReturned());
        s.setSubmitted(submission.getSubmitted());
        s.setSubmittedText(decodeBase64(submission.getSubmittedtextHtml()));
        s.setUserSubmission(submission.getIsUserSubmission());

        Set<AssignmentSubmissionSubmitter> submitters = s.getSubmitters();
        if (assignment.getIsGroup()) {
            // submitterid is the group
            if (StringUtils.isNotBlank(submission.getSubmitterid())) {
                s.setGroupId(submission.getSubmitterid());
            } else {
                // the submitterid must not be blank for a group submission
                return null;
            }

            // support for a list of submitter0, grade0
            Set<String> submitterKeys = Arrays.stream(submissionAnyKeys).filter(submitterFilter).collect(Collectors.toSet());
            for (String submitterKey : submitterKeys) {
                AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
                String submitterId = (String) submissionAny.get(submitterKey);
                if (StringUtils.isNotBlank(submitterId)) {
                    submitter.setSubmitter(submitterId);

                    String gradeKey = submitterKey.replace("submitter", "grade");
                    submitter.setGrade((String) submissionAny.get(gradeKey));

                    submitter.setSubmission(s);
                    submitters.add(submitter);
                }
            }

            // the creator we add as the one who actually submitted
            Optional<O11Property> submittee = submission.getProperties().stream().filter(p -> "CHEF:creator".equals(p.getName())).findAny();
            if (submittee.isPresent()) {
                String submitterId = decodeBase64(submittee.get().getValue());
                AssignmentSubmissionSubmitter submitter = submitters.stream().filter(r -> r.getSubmitter().equals(submitterId)).findAny().orElse(new AssignmentSubmissionSubmitter());
                submitter.setSubmitter(submitterId);
                submitter.setSubmittee(true);
                submitter.setSubmission(s);
                submitters.add(submitter);
            }
        } else {
            // non group AssignmentSubmissionSubmitter
            AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
            String submitterId = submission.getSubmitterid();
            if (StringUtils.isBlank(submitterId)) {
                submitterId = (String) submissionAny.get("submitter0");
                // don't create a AssignmentSubmissionSubmitter with a null submitter
                if (StringUtils.isBlank(submitterId)) return null;
            }
            submitter.setSubmitter(submitterId);
            submitter.setSubmittee(true);
            submitter.setGrade(submission.getScaled_grade());
            submitter.setSubmission(s);
            submitters.add(submitter);
        }
        if (s.getSubmitters().isEmpty()) {
            // every submission must have at least one submitter
            return null;
        }

        for (O11Property property : submission.getProperties()) {
            properties.put(property.getName(), property.getDecodedValue());
            if (ResourceProperties.PROP_CREATION_DATE.equals(property.getName())) {
                s.setDateCreated(convertStringToTime(property.getDecodedValue()));
            }
        }

        // support for list of feedbackattachment0
        Set<String> feedbackAttachmentKeys = Arrays.stream(submissionAnyKeys).filter(feedbackAttachmentFilter).collect(Collectors.toSet());
        feedbackAttachmentKeys.forEach(k -> s.getFeedbackAttachments().add((String) submissionAny.get(k)));

        // support for list of submittedattachment0
        Set<String> submittedAttachmentKeys = Arrays.stream(submissionAnyKeys).filter(submittedAttachmentFilter).collect(Collectors.toSet());
        submittedAttachmentKeys.forEach(k -> s.getAttachments().add((String) submissionAny.get(k)));

        return s;
    }

    private Instant convertStringToTime(String time) {
        if (StringUtils.isNotBlank(time)) {
            try {
                TemporalAccessor date = dateTimeFormatter.parse(time);
                return LocalDateTime.from(date).atOffset(ZoneOffset.UTC).toInstant();
            } catch (DateTimeException dte) {
                log.warn("could not parse time: {}, {}", time, dte.getMessage());
            }
        }
        return null;
    }

    public static String decodeBase64(String text) {
        if (StringUtils.isBlank(text)) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(text));
            if (cleanUTF8) {
                // replaces any unicode characters outside the first 3 bytes
                // with the last 3 byte char
                decoded = decoded.replaceAll("[^\\u0000-\\uFFFF]", replacementUTF8);
            }
            return decoded;
        } catch (IllegalArgumentException iae) {
            log.warn("invalid base64 string during decode: {}", text);
        }
        return text;
    }
}
