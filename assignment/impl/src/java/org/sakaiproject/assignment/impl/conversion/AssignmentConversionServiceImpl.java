package org.sakaiproject.assignment.impl.conversion;

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

import com.ctc.wstx.api.ReaderConfig;
import com.ctc.wstx.api.WstxInputProperties;
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
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.conversion.AssignmentConversionService;
import org.sakaiproject.assignment.api.conversion.AssignmentDataProvider;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.persistence.AssignmentRepository;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.util.BasicConfigItem;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.*;

@Slf4j
public class AssignmentConversionServiceImpl implements AssignmentConversionService {

    @Setter private static boolean cleanUTF8 = true;
    @Setter private static String replacementUTF8 = "";

    @Setter private AssignmentRepository assignmentRepository;
    @Setter private AssignmentDataProvider dataProvider;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SiteService siteService;

    private Predicate<String> attachmentFilter = Pattern.compile("attachment\\d+").asPredicate();
    private Predicate<String> submitterFilter = Pattern.compile("submitter\\d+").asPredicate();
    private Predicate<String> feedbackAttachmentFilter = Pattern.compile("feedbackattachment\\d+").asPredicate();
    private Predicate<String> submittedAttachmentFilter = Pattern.compile("submittedattachment\\d+").asPredicate();

    private DateTimeFormatter dateTimeFormatter;
    private int assignmentsConverted;
    private int submissionsConverted;
    private int submissionsFailed;
    private int assignmentsFailed;
    private XmlMapper xmlMapper;

    public void init() {
        // DateTimeParseException Text '20171222220000000' could not be parsed at index 0
        // https://bugs.openjdk.java.net/browse/JDK-8031085
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();

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
                log.warn("deserialization failed for xml: {}\n{}", xml.substring(0, Math.min(xml.length(), 200)), ioe.getMessage());
            }
        }
        return null;
    }

    @Override
    public void runConversion(int numberOfAttributes, int lengthOfAttribute) {
        int assignmentsTotal, progress = 0;
        assignmentsConverted = submissionsConverted = submissionsFailed = assignmentsFailed = 0;

        SimpleModule module = new SimpleModule().addDeserializer(String.class, new StdDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                String str = StringDeserializer.instance.deserialize(p, ctxt);
                if (StringUtils.isBlank(str)) return null;
                return str;
            }
        });

        // woodstox xml parser defaults we don't allow values smaller than the default
        if (numberOfAttributes < ReaderConfig.DEFAULT_MAX_ATTRIBUTES_PER_ELEMENT) numberOfAttributes = ReaderConfig.DEFAULT_MAX_ATTRIBUTES_PER_ELEMENT;
        if (lengthOfAttribute < ReaderConfig.DEFAULT_MAX_ATTRIBUTE_LENGTH) lengthOfAttribute = ReaderConfig.DEFAULT_MAX_ATTRIBUTE_LENGTH;

        log.info("<===== Assignments conversion xml parser limits: number of attributes={}, attribute size={} =====>", numberOfAttributes, lengthOfAttribute);

        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTES_PER_ELEMENT, numberOfAttributes);
        xmlInputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, lengthOfAttribute);
        xmlMapper = new XmlMapper(xmlInputFactory);
        xmlMapper.registerModule(new Jdk8Module());
        xmlMapper.registerModule(module);

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

        log.info("<===== Assignments pre 12 [{}] and post 12 [{}] to convert {} =====>", preAssignments.size(), postAssignments.size(), assignmentsTotal);

        for (String assignmentId : convertAssignments) {
            try {
                convert(assignmentId);
            } catch (Exception e) {
                log.warn("Assignment conversion exception for {}", assignmentId, e);
            }
            int percent = new Double(((assignmentsConverted + assignmentsFailed) / (double) assignmentsTotal) * 100).intValue();
            if (progress != percent) {
                progress = percent;
                log.info("<===== Assignments conversion completed {}% =====>", percent);
            }
        }

        configItem = BasicConfigItem.makeConfigItem(
                AssignableUUIDGenerator.HIBERNATE_ASSIGNABLE_ID_CLASSES,
                StringUtils.trimToEmpty(currentValue),
                AssignmentConversionServiceImpl.class.getName());
        serverConfigurationService.registerConfigItem(configItem);

        log.info("<===== Assignments converted {} =====>", assignmentsConverted);
        log.info("<===== Submissions converted {} =====>", submissionsConverted);
        log.info("<===== Assignments that failed to be converted {} =====>", assignmentsFailed);
        log.info("<===== Submissions that failed to be converted {} =====>", submissionsFailed);
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
                        	List<String> submissionGroups = new ArrayList<>();
                            List<String> sXml = dataProvider.fetchAssignmentSubmissions(assignmentId);
                            for (String xml : sXml) {
                                O11Submission o11s = (O11Submission) serializeFromXml(xml, O11Submission.class);
                                if (o11s != null) {
                                    AssignmentSubmission submission = submissionReintegration(assignment, o11s);
                                    if (submission != null) {
                                        submission.setAssignment(assignment);
                                        assignment.getSubmissions().add(submission);
                                        if (Assignment.Access.SITE.equals(assignment.getTypeOfAccess()) && assignment.getIsGroup()) {
                                        	String submissionGrp = "/site/"+assignment.getContext()+"/group/"+submission.getGroupId(); 
                                        	if (!submissionGroups.contains(submissionGrp)) {
                                        		submissionGroups.add(submissionGrp);
                                        	}
                                        }
                                    } else {
                                        log.warn("reintegration of submission {} in assignment {} failed skipping submission", o11s.getId(), assignmentId);
                                        submissionsFailed++;
                                    }
                                } else {
                                    log.warn("deserialization of a submission failed in assignment {} skipping submission", assignmentId);
                                    submissionsFailed++;
                                }
                            }
                            // Fix corrupted data: Group submission shouldn't be accessed by site
                            if (Assignment.Access.SITE.equals(assignment.getTypeOfAccess()) && assignment.getIsGroup()) {
                    			assignment.setTypeOfAccess(Assignment.Access.GROUP);
                    			submissionGroups.forEach(g -> assignment.getGroups().add(g));
                            }
                            // at this point everything has been added to the persistence context
                            // so we just need to merge and flush so that every assignment is persisted
                            try {
                                assignmentRepository.merge(assignment);
                                assignmentsConverted++;
                                submissionsConverted += assignment.getSubmissions().size();
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
                        log.warn("deserialization of assignment content {} failed skipping assignment {}", contentId, assignmentId);
                        assignmentsFailed++;
                    }
                } else {
                    log.warn("assignment content {} xml is invalid skipping assignment {}", contentId, assignmentId);
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

        // if an assignment context is missing we ignore the assignment
        if (StringUtils.isBlank(assignment.getContext())) {
            log.warn("Assignment {} does not have a CONTEXT", assignment.getId());
            return null;
        }

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
        a.setScaleFactor(content.getScaled_factor() == null ? AssignmentConstants.DEFAULT_SCALED_FACTOR : content.getScaled_factor());
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

        // remove any properties that are null or blank
        properties.values().removeIf(StringUtils::isBlank);

        return a;
    }

    private AssignmentSubmission submissionReintegration(Assignment assignment, O11Submission submission) {
        Map<String, Object> submissionAny = submission.getAny();
        String[] submissionAnyKeys = submissionAny.keySet().toArray(new String[submissionAny.size()]);

        AssignmentSubmission s = new AssignmentSubmission();
        Map<String, String> properties = s.getProperties();

        // add any keys that we don't convert explicitly as a property
        // this covers the case where institutions may have their own features
        Set<String> extraKeys = Arrays.stream(submissionAnyKeys)
                .filter(submitterFilter.negate())
                .filter(feedbackAttachmentFilter.negate())
                .filter(submittedAttachmentFilter.negate())
                .collect(Collectors.toSet());

        s.setDateModified(convertStringToTime(submission.getLastmod()));
        s.setDateReturned(convertStringToTime(submission.getDatereturned()));
        s.setDateSubmitted(convertStringToTime(submission.getDatesubmitted()));
        s.setFactor(submission.getScaled_factor());
        s.setFeedbackComment(decodeBase64(submission.getFeedbackcommentHtml()));
        s.setFeedbackText(decodeBase64(submission.getFeedbacktextHtml()));
        s.setGrade(submission.getScaled_grade());
        s.setGraded(submission.getGraded());
        if (StringUtils.contains(submission.getGradedBy(), "AssignmentPeerAssessmentService")) {
            // set peer assessment back to null as the assessor id is not recorded
            s.setGradedBy(null);
        } else {
            s.setGradedBy(submission.getGradedBy());
        }
        s.setGradeReleased(submission.getGradereleased());
        s.setHiddenDueDate(submission.getHideduedate());
        s.setHonorPledge(submission.getPledgeflag());
        s.setId(submission.getId());
        s.setReturned(submission.getReturned());
        s.setSubmitted(submission.getSubmitted());
        s.setSubmittedText(decodeBase64(submission.getSubmittedtextHtml()));
        s.setUserSubmission(submission.getIsUserSubmission()!=null?submission.getIsUserSubmission(): 
        		StringUtils.isNotBlank(s.getSubmittedText()) 
        		|| Arrays.stream(submissionAnyKeys).filter(submittedAttachmentFilter).collect(Collectors.toSet()).size() > 0 
        		|| assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION);

        Set<AssignmentSubmissionSubmitter> submitters = s.getSubmitters();
        if (assignment.getIsGroup()) {
            // submitterid is the group
            if (StringUtils.isNotBlank(submission.getSubmitterid())) {
            	try {
            		// The id must be any group if is SITE mode or a some of the accesed groups if its GROUPED
            		if (Assignment.Access.GROUP.equals(assignment.getTypeOfAccess())) {
            			if (assignment.getGroups().contains("/site/"+assignment.getContext()+"/group/"+submission.getSubmitterid())) {
            				s.setGroupId(submission.getSubmitterid());
            			} else {
            				return null;
            			}
            		} else {
            			Site assignmentSite = siteService.getSite(assignment.getContext());
            			if (assignmentSite.getGroup(submission.getSubmitterid())!=null) {
            				s.setGroupId(submission.getSubmitterid());
            			} else {
            				return null;
            			}
            		}
            	} catch (Exception ex) {
            		return null;
            	}
            } else {
                // the submitterid must not be blank for a group submission
                return null;
            }

            // support for a list of submitter0, grade0
            Set<String> submitterKeys = Arrays.stream(submissionAnyKeys).filter(submitterFilter).collect(Collectors.toSet());
            // Maybe the xml has no submitterN attributes, check the group members
            if (submitterKeys.size() == 0) {
            	// Maybe submitter keys are missing check group members
		        try {
		            Site assignmentSite = siteService.getSite(assignment.getContext());
		            Group submissionGroup = assignmentSite.getGroup(s.getGroupId());
		            int k=0;
		            for (Member member : submissionGroup.getMembers()) {
	                	if (!member.getRole().isAllowed(SECURE_ADD_ASSIGNMENT) 
	                			&& !member.getRole().isAllowed(SECURE_GRADE_ASSIGNMENT_SUBMISSION)) {
	                		submissionAny.put("submitter"+k, member.getUserId());
	                		submitterKeys.add("submitter"+k);
	                		k++;
	                	}
		            }
        		} catch (Exception ex) {
        			log.warn("Error looking for real group members in submission: {} site: {} group: {}",s.getId(),assignment.getContext(),s.getGroupId());
        		}
            }
            
            for (String submitterKey : submitterKeys) {
                AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
                String submitterId = (String) submissionAny.get(submitterKey);
                if (StringUtils.isNotBlank(submitterId)) {
                    submitter.setSubmitter(submitterId);

                    String gradeKey = submitterKey.replace("submitter", "grade");
                    extraKeys.remove(gradeKey);

                    String gradeValue = (String) submissionAny.get(gradeKey);
                    if (gradeValue != null && gradeValue.contains("::")) {
                        String values[] = gradeValue.split("::", 2);
                        gradeValue = values[1];
                    }
                    submitter.setGrade(gradeValue);

                    submitter.setSubmission(s);
                    submitters.add(submitter);
                }
            }

            // the creator we add as the one who actually submitted
            Optional<O11Property> submittee = submission.getProperties().stream().filter(p -> "CHEF:creator".equals(p.getName())).findAny();
            if (submittee.isPresent()) {
                String submitterId = decodeBase64(submittee.get().getValue());
                AssignmentSubmissionSubmitter submitter = submitters.stream().filter(r -> r.getSubmitter().equals(submitterId)).findAny().orElse(new AssignmentSubmissionSubmitter());
                if (submitterId.equals(submitter.getSubmitter())) {
                    submitter.setSubmittee(true);
                    submitter.setSubmission(s);
                    submitters.add(submitter);
                }
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

        // Add any remaining undefined keys as properties
        extraKeys.forEach(k -> properties.put(k, (String) submissionAny.get(k)));

        // remove any properties that are null or blank
        properties.values().removeIf(StringUtils::isBlank);

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
