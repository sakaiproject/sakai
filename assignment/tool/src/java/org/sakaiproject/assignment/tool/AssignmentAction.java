/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.assignment.tool;

import static org.sakaiproject.assignment.api.AssignmentConstants.*;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.*;

import org.sakaiproject.calendar.api.CalendarConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.json.simple.JSONObject;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.ContentReviewResult;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentModelAnswerItem;
import org.sakaiproject.assignment.api.model.AssignmentNoteItem;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemAttachment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemWithAttachment;
import org.sakaiproject.assignment.api.model.PeerAssessmentAttachment;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;
import org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService;
import org.sakaiproject.assignment.api.sort.AssignmentComparator;
import org.sakaiproject.assignment.api.taggable.AssignmentActivityProducer;
import org.sakaiproject.assignment.taggable.tool.DecoratedTaggingProvider;
import org.sakaiproject.assignment.taggable.tool.DecoratedTaggingProvider.Pager;
import org.sakaiproject.assignment.taggable.tool.DecoratedTaggingProvider.Sort;
import org.sakaiproject.assignment.tool.AssignmentAction.SubmitterSubmission;
import org.sakaiproject.assignment.tool.AssignmentAction.UploadGradeWrapper;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.tsugi.lti.LTIUtil;
import org.tsugi.lti13.LTICustomVars;
import org.tsugi.lti13.DeepLinkResponse;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.FileConversionService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.grading.api.AssignmentHasIllegalPointsException;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.rubrics.api.beans.AssociationTransferBean;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.scoringservice.api.ScoringAgent;
import org.sakaiproject.scoringservice.api.ScoringComponent;
import org.sakaiproject.scoringservice.api.ScoringService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.taggable.api.TaggingHelperInfo;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.DateFormatterUtil;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.AlphaNumericComparator;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * AssignmentAction is the action class for the assignment tool.
 * </p>
 */
@Slf4j
public class AssignmentAction extends PagedResourceActionII {

    // Grading
    private static final String NEW_ASSIGNMENT_GRADE_ASSIGNMENT = "new_assignment_grade_assignment";
    private static final String NEW_ASSIGNMENT_SEND_TO_GRADEBOOK = "new_assignment_send_to_gradebook";

    //Peer Assessment
    private static final String NEW_ASSIGNMENT_USE_PEER_ASSESSMENT = "new_assignment_use_peer_assessment";
    private static final String NEW_ASSIGNMENT_PEERPERIOD_MONTH = "new_assignment_peerperiod_month";
    private static final String NEW_ASSIGNMENT_PEERPERIOD_DAY = "new_assignment_peerperiod_day";
    private static final String NEW_ASSIGNMENT_PEERPERIOD_YEAR = "new_assignment_peerperiod_year";
    private static final String NEW_ASSIGNMENT_PEERPERIOD_HOUR = "new_assignment_peerperiod_hour";
    private static final String NEW_ASSIGNMENT_PEERPERIOD_MIN = "new_assignment_peerperiod_min";
    private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL = "new_assignment_peer_assessment_anon_eval";
    private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS = "new_assignment_peer_assessment_student_view_review";
    private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS = "new_assignment_peer_assessment_num_reviews";
    private static final String NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS = "new_assignment_peer_assessment_instructions";

    /**
     * Is the review service available?
     */
    private static final String NEW_ASSIGNMENT_USE_REVIEW_SERVICE = "new_assignment_use_review_service";
    private static final String NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW = "new_assignment_allow_student_view";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO = "submit_papers_to";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE = "0";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD = "1";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION = "2";
    // When to generate reports
    // although the service allows for a value of "1" --> Generate report immediately but overwrite until due date,
    // this doesn't make sense for assignment2. We limit the UI to 0 - Immediately
    // or 2 - On Due Date
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO = "report_gen_speed";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY = "0";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY_AND_DUE = "1";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE = "2";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN = "s_paper_check";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET = "internet_check";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB = "journal_check";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION = "institution_check";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC = "exclude_biblio";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED = "exclude_quoted";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG = "exclude_self_plag";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX = "store_inst_index";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW = "student_preview";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES = "exclude_smallmatches";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE = "exclude_type";
    private static final String NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE = "exclude_value";
    private static final String SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT = "review_service_eula_agreement";
    private static final String SUBMISSION_REVIEW_CHECK_SERVICE_EULA_AGREEMENT = "review_check_service_eula_agreement";

    // Video Submissions
    private static final String VIDEO_SUBMISSION_PARAM = "video-submission";
    private static final String VIDEO_SUBMISSION_MIMETYPE_PARAM = "video-submission-mimetype";

    /**
     * Peer Review Attachments
     **/
    private static final String PEER_ATTACHMENTS = "peer_attachments";
    private static final String PEER_ASSESSMENT = "peer_assessment";
    /**
     * The attachments
     */
    private static final String ATTACHMENTS = "Assignment.attachments";
    private static final String ATTACHMENTS_FOR = "Assignment.attachments_for";
    /**
     * The property name associated with Groups that are Sections
     **/
    private static final String GROUP_SECTION_PROPERTY = "sections_category";
    /**
     * The calendar object
     */
    private static final String CALENDAR = "calendar";
    /**
     * Additional calendar service
     */
    private static final String ADDITIONAL_CALENDAR = "additonal_calendar";
    /**
     * The calendar tool
     */
    private static final String CALENDAR_TOOL_EXIST = "calendar_tool_exisit";
    /**
     * Additional calendar tool
     */
    private static final String ADDITIONAL_CALENDAR_TOOL_READY = "additional_calendar_tool_ready";
    /**
     * Default value for the 'Add due date to calendar' checkbox
     */
    private static final boolean DUE_DATE_TO_CALENDAR_DEFAULT = true;
    /**
     * The announcement tool
     */
    private static final String ANNOUNCEMENT_TOOL_EXIST = "announcement_tool_exist";
    /**
     * The announcement channel
     */
    private static final String ANNOUNCEMENT_CHANNEL = "announcement_channel";
    /**
     * The state mode
     */
    private static final String STATE_MODE = "Assignment.mode";
    /**
     * The user
     */
    private static final String STATE_USER = "Assignment.user";
    /**
     * The submitter
     */
    private static final String STATE_SUBMITTER = "Assignment.submitter";
    /**
     * Used to keep track of the section info not currently being used.
     */
    private static final String STATE_SECTION_STRING = "Assignment.section_string";

    // SECTION MOD
    /**
     * state sort *
     */
    static final String SORTED_BY = "Assignment.sorted_by";

    /* **************************** sort assignment ********************** */
    /**
     * state sort ascendingly *
     */
    static final String SORTED_ASC = "Assignment.sorted_asc";
    /**
     * default sorting
     */
    private static final String SORTED_BY_DEFAULT = "default";
    /**
     * sort by assignment title
     */
    private static final String SORTED_BY_TITLE = "title";
    /**
     * sort by assignment section
     */
    private static final String SORTED_BY_SECTION = "section";
    /**
     * sort by assignment due date
     */
    private static final String SORTED_BY_DUEDATE = "duedate";
    /**
     * sort by assignment open date
     */
    private static final String SORTED_BY_OPENDATE = "opendate";
    /**
     * sort by assignment soft removed date
     */
    private static final String SORTED_BY_SOFT_REMOVED_DATE = "softremoveddate";
    /**
     * sort by group modified 
     */
    static final String SORTED_BY_MODIFIEDUSER = "modifieduser";
    /**
     * sort by group modified 
     */
    static final String SORTED_BY_MODIFIEDDATE = "modifieddate";
    /**
     * sort by assignment status
     */
    private static final String SORTED_BY_ASSIGNMENT_STATUS = "assignment_status";
    /**
     * sort by assignment submission status
     */
    private static final String SORTED_BY_SUBMISSION_STATUS = "submission_status";
    /**
     * sort by assignment number of submissions
     */
    private static final String SORTED_BY_NUM_SUBMISSIONS = "num_submissions";
    /**
     * sort by assignment number of ungraded submissions
     */
    private static final String SORTED_BY_NUM_UNGRADED = "num_ungraded";
    /**
     * sort by assignment submission grade
     */
    private static final String SORTED_BY_GRADE = "grade";
    /**
     * sort by assignment maximun grade available
     */
    private static final String SORTED_BY_MAX_GRADE = "max_grade";
    /**
     * sort by assignment range
     */
    private static final String SORTED_BY_FOR = "for";
    /**
     * sort by group title
     */
    static final String SORTED_BY_GROUP_TITLE = "group_title";
    /**
     * sort by group description
     */
    static final String SORTED_BY_GROUP_DESCRIPTION = "group_description";
    /**
     * state sort submission*
     */
    private static final String SORTED_GRADE_SUBMISSION_BY = "Assignment.grade_submission_sorted_by";

    /* *************************** sort submission in instructor grade view *********************** */
    /**
     * state sort submission ascendingly *
     */
    private static final String SORTED_GRADE_SUBMISSION_ASC = "Assignment.grade_submission_sorted_asc";
    /**
     * state sort submission by submitters last name *
     */
    private static final String SORTED_GRADE_SUBMISSION_BY_LASTNAME = "sorted_grade_submission_by_lastname";
    /**
     * state sort submission by submit time *
     */
    private static final String SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME = "sorted_grade_submission_by_submit_time";
    /**
     * state sort submission by submission status *
     */
    private static final String SORTED_GRADE_SUBMISSION_BY_STATUS = "sorted_grade_submission_by_status";
    /**
     * state sort submission by submission grade *
     */
    private static final String SORTED_GRADE_SUBMISSION_BY_GRADE = "sorted_grade_submission_by_grade";
    /**
     * state sort submission by submission released *
     */
    private static final String SORTED_GRADE_SUBMISSION_BY_RELEASED = "sorted_grade_submission_by_released";
    /**
     * state sort submissuib by content review score
     **/
    private static final String SORTED_GRADE_SUBMISSION_CONTENTREVIEW = "sorted_grade_submission_by_contentreview";
    /**
     * state sort submission*
     */
    private static final String SORTED_SUBMISSION_BY = "Assignment.submission_sorted_by";

    /* *************************** sort submission *********************** */
    /**
     * state sort submission ascendingly *
     */
    private static final String SORTED_SUBMISSION_ASC = "Assignment.submission_sorted_asc";
    /**
     * state sort submission by submitters last name *
     */
    private static final String SORTED_SUBMISSION_BY_LASTNAME = "sorted_submission_by_lastname";
    /**
     * state sort submission by submit time *
     */
    private static final String SORTED_SUBMISSION_BY_SUBMIT_TIME = "sorted_submission_by_submit_time";
    /**
     * state sort submission by submission grade *
     */
    private static final String SORTED_SUBMISSION_BY_GRADE = "sorted_submission_by_grade";
    /**
     * state sort submission by submission status *
     */
    private static final String SORTED_SUBMISSION_BY_STATUS = "sorted_submission_by_status";
    /**
     * state sort submission by submission released *
     */
    private static final String SORTED_SUBMISSION_BY_RELEASED = "sorted_submission_by_released";
    /**
     * state sort submission by assignment title
     */
    private static final String SORTED_SUBMISSION_BY_ASSIGNMENT = "sorted_submission_by_assignment";
    /**
     * state sort submission by max grade
     */
    private static final String SORTED_SUBMISSION_BY_MAX_GRADE = "sorted_submission_by_max_grade";
    /*********************** Sort by user sort name *****************************************/
    private static final String SORTED_USER_BY_SORTNAME = "sorted_user_by_sortname";
    /**
     * filter by group in assignments list
     */
    private static final String FILTER_BY_GROUP = "filterByGroup";
    /**
     * filter by group in assignments list
     */
    private static final String FILTER_OPTION = "filterOption";
     /**
     * the assignment object been viewing *
     */
    private static final String VIEW_SUBMISSION_ASSIGNMENT_REFERENCE = "Assignment.view_submission_assignment_reference";

    /** ******************** student's view assignment submission ****************************** */
    /**
     * the assignment object been viewing by instructor *
     */
    private static final String VIEW_SUBMISSION_ASSIGNMENT_INSTRUCTOR = "Assignment.view_submission_assignment_instructor";
    /**
     * the submission text to the assignment *
     */
    private static final String VIEW_SUBMISSION_TEXT = "Assignment.view_submission_text";
    /**
     * the assignment id *
     */
    private static final String PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE = "preview_submission_assignment_reference";

    /* ***************** student's preview of submission *************************** */
    /**
     * the submission text *
     */
    private static final String PREVIEW_SUBMISSION_TEXT = "preview_submission_text";
    /**
     * the submission attachments *
     */
    private static final String PREVIEW_SUBMISSION_ATTACHMENTS = "preview_attachments";
    /**
     * the flag indicate whether the to show the student view or not
     */
    private static final String PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG = "preview_assignment_student_view_hide_flag";
    /**
     * the flag indicate whether the to show the assignment info or not
     */
    private static final String PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG = "preview_assignment_assignment_hide_flag";
    /**
     * the assignment id
     */
    private static final String PREVIEW_ASSIGNMENT_ASSIGNMENT_ID = "preview_assignment_assignment_id";
    /**
     * the assignment content id
     */
    private static final String PREVIEW_ASSIGNMENT_ASSIGNMENTCONTENT_ID = "preview_assignment_assignmentcontent_id";
    /**
     * the hide assignment flag in the view assignment page *
     */
    private static final String VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG = "view_assignment_hide_assignment_flag";
    /**
     * the warning for content review requiring EULA
     */
    private static final String CONTENT_REVIEW_EULA_REQUIRED_ALERTED = "content_review_eula_required_alerted";
    /* ************** view assignment ***************************************** */
    /**
     * the hide student view flag in the view assignment page *
     */
    private static final String VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG = "view_assignment_hide_student_view_flag";
    /**
     * ****************** instructor's view assignment *****************************
     */
    private static final String VIEW_ASSIGNMENT_ID = "view_assignment_id";
    /**
     * ****************** instructor's edit assignment *****************************
     */
    private static final String EDIT_ASSIGNMENT_ID = "edit_assignment_id";
    /**
     * ****************** instructor's selected assignment ids *****************************
     */
    private static final String SELECTED_ASSIGNMENT_IDS = "selected_assignment_ids";
    /**
     * ****************** flags controls the grade assignment page layout *******************
     */
    private static final String GRADE_SUBMISSION_EXPAND_FLAG = "grade_submission_expand_flag";
    /**
     * ****************** instructor's grade submission *****************************
     */
    private static final String GRADE_SUBMISSION_ALLOW_RESUBMIT = "grade_submission_allow_resubmit";
    private static final String GRADE_SUBMISSION_DONE = "grade_submission_done";
    private static final String GRADE_SUBMISSION_SUBMIT = "grade_submission_submit";
    private static final String GRADE_SUBMISSION_SHOW_STUDENT_DETAILS = "grade_showStudentDetails";
    private static final String GRADE_SUBMISSION_SUBMITTERS_NAMES = "grade_ssubmission_submitters_names";
    private static final String RUBRIC_ASSOCIATION = "rubric_association";
    /**
     * ****************** instructor's export assignment *****************************
     */
    private static final String EXPORT_ASSIGNMENT_REF = "export_assignment_ref";
    /**
     * Is review service enabled?
     */
    private static final String ENABLE_REVIEW_SERVICE = "enable_review_service";
    private static final String EXPORT_ASSIGNMENT_ID = "export_assignment_id";
    /**
     * ***************** instructor's new assignment ******************************
     */
    private static final String NEW_ASSIGNMENT_TITLE = "new_assignment_title";
    // assignment order for default view
    private static final String NEW_ASSIGNMENT_ORDER = "new_assignment_order";
    // open date
    private static final String NEW_ASSIGNMENT_OPEN_MONTH = "new_assignment_open_month";
    private static final String NEW_ASSIGNMENT_OPEN_DAY = "new_assignment_open_day";
    private static final String NEW_ASSIGNMENT_OPEN_YEAR = "new_assignment_open_year";
    private static final String NEW_ASSIGNMENT_OPEN_HOUR = "new_assignment_open_hour";
    private static final String NEW_ASSIGNMENT_OPEN_MIN = "new_assignment_open_min";
    // visible date
    private static final String NEW_ASSIGNMENT_VISIBLE_MONTH = "new_assignment_visible_month";
    private static final String NEW_ASSIGNMENT_VISIBLE_DAY = "new_assignment_visible_day";
    private static final String NEW_ASSIGNMENT_VISIBLE_YEAR = "new_assignment_visible_year";
    private static final String NEW_ASSIGNMENT_VISIBLE_HOUR = "new_assignment_visible_hour";
    private static final String NEW_ASSIGNMENT_VISIBLE_MIN = "new_assignment_visible_min";
    private static final String NEW_ASSIGNMENT_VISIBLETOGGLE = "new_assignment_visibletoggle";
    // due date
    private static final String NEW_ASSIGNMENT_DUE_MONTH = "new_assignment_due_month";
    private static final String NEW_ASSIGNMENT_DUE_DAY = "new_assignment_due_day";
    private static final String NEW_ASSIGNMENT_DUE_YEAR = "new_assignment_due_year";
    private static final String NEW_ASSIGNMENT_DUE_HOUR = "new_assignment_due_hour";
    private static final String NEW_ASSIGNMENT_DUE_MIN = "new_assignment_due_min";
    private static final String NEW_ASSIGNMENT_PAST_DUE_DATE = "new_assignment_past_due_date";
    // close date
    private static final String NEW_ASSIGNMENT_ENABLECLOSEDATE = "new_assignment_enableclosedate";
    private static final String NEW_ASSIGNMENT_REMINDER_EMAIL = "student_reminder_email";
    private static final String NEW_ASSIGNMENT_CLOSE_MONTH = "new_assignment_close_month";
    private static final String NEW_ASSIGNMENT_CLOSE_DAY = "new_assignment_close_day";
    private static final String NEW_ASSIGNMENT_CLOSE_YEAR = "new_assignment_close_year";
    private static final String NEW_ASSIGNMENT_CLOSE_HOUR = "new_assignment_close_hour";
    private static final String NEW_ASSIGNMENT_CLOSE_MIN = "new_assignment_close_min";
    private static final String NEW_ASSIGNMENT_ATTACHMENT = "new_assignment_attachment";
    private static final String NEW_ASSIGNMENT_SECTION = "new_assignment_section";
    private static final String NEW_ASSIGNMENT_SUBMISSION_TYPE = "new_assignment_submission_type";
    private static final String NEW_ASSIGNMENT_CONTENT_ID = "new_assignment_content_id";
    private static final String NEW_ASSIGNMENT_CONTENT_TITLE = "new_assignment_content_title";
    private static final String NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW = "new_assignment_content_launch_new_window";
    private static final String NEW_ASSIGNMENT_CONTENT_TOOL_NEWPAGE = "new_assignment_content_tool_newpage";
    private static final String NEW_ASSIGNMENT_CONTENT_DELETED = "new_assignment_content_deleted";
    private static final String NEW_ASSIGNMENT_CATEGORY = "new_assignment_category";
    private static final String NEW_ASSIGNMENT_GRADE_TYPE = "new_assignment_grade_type";
    private static final String NEW_ASSIGNMENT_GRADE_TYPE_SWITCHING = "new_assignment_grade_type_switching";
    private static final String NEW_ASSIGNMENT_GRADE_TYPE_SWITCH_CONFIRM = "new_assignment_grade_type_switch_confirm";
    private static final String NEW_ASSIGNMENT_GRADE_POINTS = "new_assignment_grade_points";
    private static final String NEW_ASSIGNMENT_DESCRIPTION = "new_assignment_instructions";
    private static final String NEW_ASSIGNMENT_DUE_DATE_SCHEDULED = "new_assignment_due_date_scheduled";
    private static final String NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED = "new_assignment_open_date_announced";
    private static final String NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE = "new_assignment_check_add_honor_pledge";
    private static final String NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT = "new_assignment_check_auto_submit";
    private static final String NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS = "new_assignment_check_add_group_tags";
    private static final String NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS = "new_assignment_check_add_instructor_tags";
    private static final String NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE = "new_assignment_check_hide_due_date";
    private static final String NEW_ASSIGNMENT_FOCUS = "new_assignment_focus";
    private static final String NEW_ASSIGNMENT_DESCRIPTION_EMPTY = "new_assignment_description_empty";
    static final String VIEW_SUBMISSION_GROUP = "view_submission_group";
    static final String VIEW_SUBMISSION_ORIGINAL_GROUP = "view_submission_original_group";
    private static final String NEW_ASSIGNMENT_PAST_CLOSE_DATE = "new_assignment_past_close_date";
    /*************************** assignment model answer attributes *************************/
    private static final String NEW_ASSIGNMENT_MODEL_ANSWER = "new_assignment_model_answer";
    private static final String NEW_ASSIGNMENT_MODEL_ANSWER_TEXT = "new_assignment_model_answer_text";
    private static final String NEW_ASSIGNMENT_MODEL_SHOW_TO_STUDENT = "new_assignment_model_answer_show_to_student";
    private static final String NEW_ASSIGNMENT_MODEL_ANSWER_ATTACHMENT = "new_assignment_model_answer_attachment";
    /**************************** assignment year range *************************/
    private static final String NEW_ASSIGNMENT_YEAR_RANGE_FROM = "new_assignment_year_range_from";
    private static final String NEW_ASSIGNMENT_YEAR_RANGE_TO = "new_assignment_year_range_to";
    // submission level of resubmit due time
    private static final String ATTACHMENTS_MODIFIED = "attachments_modified";
    private static final String NEW_ASSIGNMENT_PREVIOUSLY_ASSOCIATED = "new_assignment_previously_associated";
    /**
     * *************************** instructor's view student submission *****************
     */
    // the show/hide table based on member id
    private static final String STUDENT_LIST_SHOW_TABLE = "STUDENT_LIST_SHOW_TABLE";
    /**
     * *************************** student view grade submission id ***********
     */
    private static final String VIEW_GRADE_SUBMISSION_ID = "view_grade_submission_id";
    // alert for grade exceeds max grade setting
    private static final String GRADE_GREATER_THAN_MAX_ALERT = "grade_greater_than_max_alert";

    private static final String DEFAULT_GRADE = "defaultGrade";
    private static final String STATE_PEER_REVIEW_DRAFT_SKIPPED_MESSAGE = "peer_review_draft_skipped_message";
    /**
     * The list view of assignments
     */
    private static final String MODE_LIST_ASSIGNMENTS = "lisofass1"; // set in velocity template

    /** **************************** modes *************************** */
    /**
     * The student view of an assignment submission
     */
    private static final String MODE_STUDENT_VIEW_SUBMISSION = "Assignment.mode_view_submission";
    /**
     * The student view of an assignment submission confirmation
     */
    private static final String MODE_STUDENT_VIEW_SUBMISSION_CONFIRMATION = "Assignment.mode_view_submission_confirmation";
    /**
     * The student confirm step of an assignment submission
     */
    private static final String MODE_STUDENT_CONFIRM_SUBMISSION = "Assignment.mode_student_confirm_submission";
    /**
     * The student preview of an assignment submission
     */
    private static final String MODE_STUDENT_PREVIEW_SUBMISSION = "Assignment.mode_student_preview_submission";
    /**
     * The student view of graded submission
     */
    private static final String MODE_STUDENT_VIEW_GRADE = "Assignment.mode_student_view_grade";
    /**
     * The student view of graded submission
     */
    private static final String MODE_STUDENT_VIEW_GRADE_PRIVATE = "Assignment.mode_student_view_grade_private";
    /**
     * The student view of a group submission error (user is in multiple groups
     */
    private static final String MODE_STUDENT_VIEW_GROUP_ERROR = "Assignment.mode_student_view_group_error";
    /**
     * The student view of assignments
     */
    private static final String MODE_STUDENT_VIEW_ASSIGNMENT = "Assignment.mode_student_view_assignment";
    /**
     * The student view of assignment honor pledge
     */
    private static final String MODE_STUDENT_VIEW_ASSIGNMENT_HONORPLEDGE = "Assignment.mode_student_view_assignment_honor_pledge";
    /**
     * The instructor view of creating a new assignment or editing an existing one
     */
    private static final String MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT = "Assignment.mode_instructor_new_edit_assignment";
    /**
     * The instructor view to reorder assignments
     */
    private static final String MODE_INSTRUCTOR_REORDER_ASSIGNMENT = "reorder";
    /**
     * The instructor view to delete an assignment
     */
    private static final String MODE_INSTRUCTOR_DELETE_ASSIGNMENT = "Assignment.mode_instructor_delete_assignment";
    /**
     * The instructor view to publish an assignment
     */
    private static final String MODE_INSTRUCTOR_PUBLISH_ASSIGNMENT = "Assignment.mode_instructor_publish_assignment";
    /**
     * The instructor view to unpublish an assignment
     */
    private static final String MODE_INSTRUCTOR_UNPUBLISH_ASSIGNMENT = "Assignment.mode_instructor_unpublish_assignment";
    /**
     * The instructor view to grade an assignment
     */
    private static final String MODE_INSTRUCTOR_GRADE_ASSIGNMENT = "Assignment.mode_instructor_grade_assignment";
    /**
     * The instructor view to grade a submission
     */
    private static final String MODE_INSTRUCTOR_GRADE_SUBMISSION = "Assignment.mode_instructor_grade_submission";
    /**
     * The instructor view of preview grading a submission
     */
    private static final String MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION = "Assignment.mode_instructor_preview_grade_submission";
    /**
     * The instructor preview of one assignment
     */
    private static final String MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT = "Assignment.mode_instructor_preview_assignments";
    /**
     * The instructor view of one assignment
     */
    private static final String MODE_INSTRUCTOR_VIEW_ASSIGNMENT = "Assignment.mode_instructor_view_assignments";
    /**
     * The instructor view to list students of an assignment
     */
    private static final String MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT = "lisofass2"; // set in velocity template
    /**
     * The instructor view of assignment submission report
     */
    private static final String MODE_INSTRUCTOR_REPORT_SUBMISSIONS = "grarep"; // set in velocity template
    /**
     * The instructor view of download all file
     */
    private static final String MODE_INSTRUCTOR_DOWNLOAD_ALL = "downloadAll";
    /**
     * The instructor view of uploading all from archive file
     */
    private static final String MODE_INSTRUCTOR_UPLOAD_ALL = "uploadAll";
    /**
     * The student view of assignment submission report
     */
    private static final String MODE_STUDENT_VIEW = "stuvie"; // set in velocity template
    /**
     * The option view
     */
    private static final String MODE_OPTIONS = "options"; // set in velocity template
    /**
     * Review Edit page for students
     */
    private static final String MODE_STUDENT_REVIEW_EDIT = "Assignment.mode_student_review_edit"; // set in velocity template
    /**
     * The list view of assignments
     */
    private static final String TEMPLATE_LIST_ASSIGNMENTS = "_list_assignments";

    /**
     * The list view of softly deleted assignments
     */
    private static final String MODE_LIST_DELETED_ASSIGNMENTS = "Assignment.mode_list_removed_assignments";

    /**
     * The view to launch an external tool
     */
    private static final String MODE_EXTERNAL_TOOL_LAUNCH = "Assignment.mode_external_tool_launch";

    /* ************************* vm names ************************** */
    /**
     * The student view of assignment
     */
    private static final String TEMPLATE_STUDENT_VIEW_ASSIGNMENT = "_student_view_assignment";
    /**
     * The student view of the assignments honor pledge
     */
    private static final String TEMPLATE_STUDENT_VIEW_ASSIGNMENT_HONORPLEDGE = "_student_view_assignment_honor_pledge";
    /**
     * The student view of showing an assignment submission
     */
    private static final String TEMPLATE_STUDENT_VIEW_SUBMISSION = "_student_view_submission";
    /**
     * The student view of showing a group assignment grouping error
     */
    private static final String TEMPLATE_STUDENT_VIEW_GROUP_ERROR = "_student_view_group_error";
    /**
     * The student view of an assignment submission confirmation
     */
    private static final String TEMPLATE_STUDENT_VIEW_SUBMISSION_CONFIRMATION = "_student_view_submission_confirmation";
    /**
     * The student confirm step for an assignment submission
     */
    private static final String TEMPLATE_STUDENT_CONFIRM_SUBMISSION = "_student_confirm_submission";
    /**
     * The student preview an assignment submission
     */
    private static final String TEMPLATE_STUDENT_PREVIEW_SUBMISSION = "_student_preview_submission";
    /**
     * The student view of graded submission
     */
    private static final String TEMPLATE_STUDENT_VIEW_GRADE = "_student_view_grade";
    /**
     * The instructor view to create a new assignment or edit an existing one
     */
    private static final String TEMPLATE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT = "_instructor_new_edit_assignment";
    /**
     * The instructor view to reorder the default assignments
     */
    private static final String TEMPLATE_INSTRUCTOR_REORDER_ASSIGNMENT = "_instructor_reorder_assignment";
    /**
     * The instructor view to delete assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_BULK_OPERATION = "_instructor_bulk_operation";
    /**
     * The instructor view to edit assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION = "_instructor_grading_submission";
    /**
     * The instructor view to edit assignment with the grader
     */
    private static final String TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION_WITH_GRADER = "_instructor_grading_submission_withgrader";
    /**
     * The instructor preview to edit assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION = "_instructor_preview_grading_submission";
    /**
     * The instructor view to grade the assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_GRADE_ASSIGNMENT = "_instructor_list_submissions";
    /**
     * The instructor preview of assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_PREVIEW_ASSIGNMENT = "_instructor_preview_assignment";
    /**
     * The instructor view of assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_VIEW_ASSIGNMENT = "_instructor_view_assignment";
    /**
     * The instructor view to edit assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT = "_instructor_student_list_submissions";
    /**
     * The instructor view to assignment submission report
     */
    private static final String TEMPLATE_INSTRUCTOR_REPORT_SUBMISSIONS = "_instructor_report_submissions";
    /**
     * The instructor view to upload all information from archive file
     */
    private static final String TEMPLATE_INSTRUCTOR_UPLOAD_ALL = "_instructor_uploadAll";
    /**
     * The student view to edit reviews
     **/
    private static final String TEMPLATE_STUDENT_REVIEW_EDIT = "_student_review_edit";
    /**
     * The instructor view to list users details
     **/
    private static final String TEMPLATE_INSTRUCTOR_VIEW_STUDENTS_DETAILS = "_instructor_view_students_details";
    /**
     * The instructor view to list deleted assignment
     */
    private static final String TEMPLATE_INSTRUCTOR_LIST_DELETED_ASSIGNMENTS = "_instructor_list_deleted_assignments";
    /**
     * The student view of showing an assignment submission
     */
    private static final String TEMPLATE_VIEW_LAUNCH = "_view_launch";
    /**
     * The options page
     */
    private static final String TEMPLATE_OPTIONS = "_options";
    /**
     * The opening mark comment
     */
    private static final String COMMENT_OPEN = "{{";
    /**
     * The closing mark for comment
     */
    private static final String COMMENT_CLOSE = "}}";
    /**
     * The selected view
     */
    private static final String STATE_SELECTED_VIEW = "state_selected_view";
    /**
     * The configuration choice of showing or hiding the number of submissions column
     */
    private static final String SHOW_NUMBER_SUBMISSION_COLUMN = "showNumSubmissionColumn";
    /**
     * The alert flag when doing global navigation from improper mode
     */
    private static final String ALERT_GLOBAL_NAVIGATION = "alert_global_navigation";
    /**
     * The total list item before paging
     */
    private static final String STATE_PAGEING_TOTAL_ITEMS = "state_paging_total_items";
    /**
     * is current user allowed to grade assignment?
     */
    private static final String STATE_ALLOW_GRADE_SUBMISSION = "state_allow_grade_submission";
    /**
     * property for previous feedback attachments
     **/
    private static final String PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS = "prop_submission_previous_feedback_attachments";
    /**
     * the user and submission list for list of submissions page
     */
    private static final String USER_SUBMISSIONS = "user_submissions";
    /**
     * the user and submission list for list of adittional info page
     */
    private static final String USER_NOTES = "user_notes";
    /**
     * the items for storing the comments and grades for peer assessment
     **/
    private static final String PEER_ASSESSMENT_ITEMS = "peer_assessment_items";
    private static final String PEER_ASSESSMENT_ASSESSOR_ID = "peer_assessment_assesor_id";
    private static final String PEER_ASSESSMENT_REMOVED_STATUS = "peer_assessment_removed_status";
    /**
     * identifier of tagging provider that will provide the appropriate helper
     */
    private static final String PROVIDER_ID = "providerId";

    /* ************************* Taggable constants ************************** */
    /**
     * Reference to an activity
     */
    private static final String ACTIVITY_REF = "activityRef";
    /**
     * Reference to an item
     */
    private static final String ITEM_REF = "itemRef";
    /**
     * session attribute for list of decorated tagging providers
     */
    private static final String PROVIDER_LIST = "providerList";
    // whether the choice of emails instructor submission notification is available in the installation
    private static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS = "assignment.instructor.notifications";
    // default for whether or how the instructor receive submission notification emails, none(default)|each|digest
    private static final String ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DEFAULT = "assignment.instructor.notifications.default";
    // name for release grade notification
    private static final String ASSIGNMENT_RELEASEGRADE_NOTIFICATION = "assignment.releasegrade.notification";
    private static final String ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION = "assignment.releasereturn.notification";
    /****************************** Upload all screen ***************************/
    private static final String UPLOAD_ALL_HAS_SUBMISSION_TEXT = "upload_all_has_submission_text";
    private static final String UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT = "upload_all_has_submission_attachment";
    private static final String UPLOAD_ALL_HAS_GRADEFILE = "upload_all_has_gradefile";
    private static final String UPLOAD_ALL_GRADEFILE_FORMAT = "upload_all_gradefile_format";
    private static final String UPLOAD_ALL_HAS_COMMENTS = "upload_all_has_comments";
    private static final String UPLOAD_ALL_HAS_FEEDBACK_TEXT = "upload_all_has_feedback_text";
    private static final String UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT = "upload_all_has_feedback_attachment";
    private static final String UPLOAD_ALL_WITHOUT_FOLDERS = "upload_all_without_folders";
    private static final String UPLOAD_ALL_RELEASE_GRADES = "upload_all_release_grades";
    // this is to track whether the site has multiple assignment, hence if true, show the reorder link
    private static final String HAS_MULTIPLE_ASSIGNMENTS = "has_multiple_assignments";
    // view all or grouped submission list
    private static final String VIEW_SUBMISSION_LIST_OPTION = "view_submission_list_option";
    /************************* SAK-17606 - Upload all grades.csv columns ********************/
    private static final int IDX_GRADES_CSV_EID = 1;
    private static final int IDX_GRADES_CSV_GRADE = 5;
    // search string for submission list
    private static final String VIEW_SUBMISSION_SEARCH = "view_submission_search";
    // search string for assignment list
    private static final String SEARCH_ASSIGNMENTS = "searchString";
    /******** Model Answer ************/
    private static final String MODELANSWER = "modelAnswer";
    private static final String MODELANSWER_TEXT = "modelAnswer.text";
    private static final String MODELANSWER_SHOWTO = "modelAnswer.showTo";
    private static final String MODELANSWER_ATTACHMENTS = "modelanswer_attachments";
    private static final String MODELANSWER_TO_DELETE = "modelanswer.toDelete";
    /******** Note ***********/
    private static final String NOTE = "note";
    private static final String NOTE_TEXT = "note.text";
    private static final String NOTE_SHAREWITH = "note.shareWith";
    private static final String NOTE_TO_DELETE = "note.toDelete";
    /******** AllPurpose *******/
    private static final String ALLPURPOSE = "allPurpose";
    private static final String ALLPURPOSE_TITLE = "allPurpose.title";
    private static final String ALLPURPOSE_TEXT = "allPurpose.text";
    private static final String ALLPURPOSE_HIDE = "allPurpose.hide";
    private static final String ALLPURPOSE_SHOW_FROM = "allPurpose.show.from";
    private static final String ALLPURPOSE_SHOW_TO = "allPurpose.show.to";
    private static final String ALLPURPOSE_RELEASE_DATE = "allPurpose.releaseDate";
    private static final String ALLPURPOSE_RETRACT_DATE = "allPurpose.retractDate";
    private static final String ALLPURPOSE_ACCESS = "allPurpose.access";
    private static final String ALLPURPOSE_ATTACHMENTS = "allPurpose_attachments";
    private static final String ALLPURPOSE_RELEASE_YEAR = "all_purpose_release_year";
    private static final String ALLPURPOSE_RELEASE_MONTH = "all_purpose_release_month";
    private static final String ALLPURPOSE_RELEASE_DAY = "all_purpose_release_day";
    private static final String ALLPURPOSE_RELEASE_HOUR = "all_purpose_release_hour";
    private static final String ALLPURPOSE_RELEASE_MIN = "all_purpose_release_min";
    private static final String ALLPURPOSE_RETRACT_YEAR = "all_purpose_retract_year";
    private static final String ALLPURPOSE_RETRACT_MONTH = "all_purpose_retract_month";
    private static final String ALLPURPOSE_RETRACT_DAY = "all_purpose_retract_day";
    private static final String ALLPURPOSE_RETRACT_HOUR = "all_purpose_retract_hour";
    private static final String ALLPURPOSE_RETRACT_MIN = "all_purpose_retract_min";
    private static final String ALLPURPOSE_TO_DELETE = "allPurpose.toDelete";
    private static final String RETURNED_FEEDBACK = "feedback_returned_to_selected_users";
    private static final String OW_FEEDBACK = "feedback_overwritten";
    private static final String SAVED_FEEDBACK = "feedback_saved";
    private static final int INPUT_BUFFER_SIZE = 102400;
    private static final long MEGABYTE = 1024L * 1024L;
    private static final String INVOKE = "invoke_via";
    private static final String INVOKE_BY_LINK = "link";
    private static final String INVOKE_BY_PORTAL = "portal";
    private static final String SUBMISSIONS_SEARCH_ONLY = "submissions_search_only";
    private static final String TAG_SELECTOR = "tag_selector";
    private static final String GB_SELECTOR = "gb_selector";
    /*************** search related *******************/
    private static final String STATE_SEARCH = "state_search";
    private static final String FORM_SEARCH = "form_search";
    private static final String STATE_DOWNLOAD_URL = "state_download_url";
    /**
     * To know if grade_submission go from view_students_assignment view or not
     **/
    private static final String FROM_VIEW = "from_view";
    /**
     * Whether or not to reset the table state stored on the frontend
     */
    private static final String RESET_TABLE_STATE = "reset_table_state";

    /**
     * Site property for export rubric in pdf
     */
    private static final String RUBRICS_EXPORT_PDF = "enablePdfExport";

    private static final String FLAG_ON = "on";
    private static final String FLAG_TRUE = "true";
    private static final String FLAG_NEXT = "next";
    private static final String FLAG_PREV = "prev";
    private static final String FLAG_NEXT_UNGRADED = "nextUngraded";
    private static final String FLAG_PREV_UNGRADED = "prevUngraded";
    private static final String FLAG_NEXT_WITH_SUB = "nextWithSubmission";
    private static final String FLAG_PREV_WITH_SUB = "prevWithSubmission";
    private static final String FLAG_NEXT_UNGRADED_WITH_SUB = "nextUngradedWithSubmission";
    private static final String FLAG_PREV_UNGRADED_WITH_SUB = "prevUngradedWithSubmission";
    private static final String STATE_VIEW_SUBS_ONLY = "state_view_submissions_only_selected";
    private static final String CONTEXT_VIEW_SUBS_ONLY = "subsOnlySelected";
    private static final String CONTEXT_NEXT_UNGRADED_SUB_ID = "nextUngradedSubmissionID";
    private static final String CONTEXT_PREV_UNGRADED_SUB_ID = "prevUngradedSubmissionID";
    private static final String CONTEXT_NEXT_WITH_SUB_ID = "nextWithSubmissionID";
    private static final String CONTEXT_PREV_WITH_SUB_ID = "prevWithSubmissionID";
    private static final String CONTEXT_NEXT_UNGRADED_WITH_SUB_ID = "nextUngradedWithSubmissionID";
    private static final String CONTEXT_PREV_UNGRADED_WITH_SUB_ID = "prevUngradedWithSubmissionID";
    private static final String CONTEXT_GO_NEXT_UNGRADED_ENABLED = "goNextUngradedEnabled";
    private static final String CONTEXT_GO_PREV_UNGRADED_ENABLED = "goPrevUngradedEnabled";
    private static final String PARAMS_VIEW_SUBS_ONLY_CHECKBOX = "chkSubsOnly1";
    private static final String EXPANDED_USER_ID = "expandedUserId";
    private static ResourceLoader rb = new ResourceLoader("assignment");
    private boolean nextUngraded = false;
    private boolean prevUngraded = false;
    private boolean nextWithSubmission = false;
    private boolean prevWithSubmission = false;
    private boolean nextUngradedWithSubmission = false;
    private boolean prevUngradedWithSubmission = false;
    private String nextUngradedRef = "";
    private String prevUngradedRef = "";
    private String nextWithSubmissionRef = "";
    private String prevWithSubmissionRef = "";
    private String nextUngradedWithSubmissionRef = "";
    private String prevUngradedWithSubmissionRef = "";

    // Content providers names
    private static final String CONTENT_PROVIDER_TURNITIN = "Turnitin";
    private static final String CONTENT_PROVIDER_URKUND = "Urkund";

    private enum AssignmentFilter {
        ALL,
        DRAFT,
        PUB_ALL,
        PUB_ACTIVE,
        PUB_INACTIVE
    }

    private enum BulkOperation {

        DELETE("delete"),
        PUBLISH("publish"),
        UNPUBLISH("unpublish");

        public final String label;

        private BulkOperation(String label) {
            this.label = label;
        }
    }

    private AnnouncementService announcementService;
    private AssignmentActivityProducer assignmentActivityProducer;
    private AssignmentDueReminderService assignmentDueReminderService;
    private AssignmentPeerAssessmentService assignmentPeerAssessmentService;
    private AssignmentService assignmentService;
    private AssignmentToolUtils assignmentToolUtils;
    private AssignmentSupplementItemService assignmentSupplementItemService;
    private AuthzGroupService authzGroupService;
    private CalendarService calendarService;
    private CandidateDetailProvider candidateDetailProvider;
    private ContentHostingService contentHostingService;
    private ContentReviewService contentReviewService;
    private ContentTypeImageService contentTypeImageService;
    private EntityManager entityManager;
    private EventTrackingService eventTrackingService;
    private FileConversionService fileConversionService;
    private FormattedText formattedText;
    private GradingService gradingService;
    private LearningResourceStoreService learningResourceStoreService;
    private NotificationService notificationService;
	private PreferencesService preferencesService;
	private RubricsService rubricsService;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private SessionManager sessionManager;
    private SiteService siteService;
    private TaggingManager taggingManager;
    private TimeService timeService;
    private ToolManager toolManager;
    private UserDirectoryService userDirectoryService;
    private UserTimeService userTimeService;
    private RangeAndGroupsDelegate rangeAndGroups;
    private LTIService ltiService;
    private TagService tagService;
    
    /**
     * sort by assignment timesheet
     */
    private static final String SORTED_BY_ESTIMATE = "timesheet";
    /**
     * sort by assignment estimate
     */
    private static final String SORTED_GRADE_SUBMISSION_BY_ESTIMATE = "sorted_grade_submission_by_estimate";

    public AssignmentAction() {
        super();

        announcementService = ComponentManager.get(AnnouncementService.class);
        assignmentActivityProducer = ComponentManager.get(AssignmentActivityProducer.class);
        assignmentDueReminderService = ComponentManager.get(AssignmentDueReminderService.class);
        assignmentPeerAssessmentService = ComponentManager.get(AssignmentPeerAssessmentService.class);
        assignmentService = ComponentManager.get(AssignmentService.class);
        assignmentSupplementItemService = ComponentManager.get(AssignmentSupplementItemService.class);
        authzGroupService = ComponentManager.get(AuthzGroupService.class);
        calendarService = ComponentManager.get(CalendarService.class);
        contentHostingService = ComponentManager.get(ContentHostingService.class);
        contentReviewService = ComponentManager.get(ContentReviewService.class);
        contentTypeImageService = ComponentManager.get(ContentTypeImageService.class);
        entityManager = ComponentManager.get(EntityManager.class);
        eventTrackingService = ComponentManager.get(EventTrackingService.class);
        fileConversionService = ComponentManager.get(FileConversionService.class);
        formattedText = ComponentManager.get(FormattedText.class);
        gradingService = (GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");
        learningResourceStoreService = ComponentManager.get(LearningResourceStoreService.class);
        notificationService = ComponentManager.get(NotificationService.class);
		preferencesService  = ComponentManager.get(PreferencesService.class);
		rubricsService  = ComponentManager.get(RubricsService.class);
        securityService = ComponentManager.get(SecurityService.class);
        serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
        sessionManager = ComponentManager.get(SessionManager.class);
        siteService = ComponentManager.get(SiteService.class);
        taggingManager = ComponentManager.get(TaggingManager.class);
        timeService = ComponentManager.get(TimeService.class);
        toolManager = ComponentManager.get(ToolManager.class);
        userDirectoryService = ComponentManager.get(UserDirectoryService.class);
        userTimeService = ComponentManager.get(UserTimeService.class);
        ltiService = ComponentManager.get(LTIService.class);
        tagService = ComponentManager.get(TagService.class);
        rangeAndGroups = new RangeAndGroupsDelegate(assignmentService, rb, siteService, securityService, formattedText, gradingService);
    }

    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        assignmentToolUtils = applicationContext.getBean("org.sakaiproject.assignment.tool.AssignmentToolUtils", AssignmentToolUtils.class);
    }

    /**
     * Normalizes a string for case and accent-insensitive search
     * @param input the string to normalize
     * @return the normalized string without accents and in lowercase
     */
    private String normalizeStringForSearch(String input) {
        if (input == null) return null;
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Called to deal with old Chef-style assignment feedback annotation, {{like this}}.
     *
     * @param value A formatted text string that may contain {{}} style markup
     * @return HTML ready to for display on a browser
     */
    public String escapeAssignmentFeedback(String value) {
        if (value == null || value.length() == 0) return value;

        value = fixAssignmentFeedback(value);

        StringBuilder buf = new StringBuilder(value);
        int pos = -1;

        while ((pos = buf.indexOf("{{")) != -1) {
            buf.replace(pos, pos + "{{".length(), "<span class='highlight'>");
        }

        while ((pos = buf.indexOf("}}")) != -1) {
            buf.replace(pos, pos + "}}".length(), "</span>");
        }

        return formattedText.escapeHtmlFormattedText(buf.toString());
    }

    /**
     * Escapes the given assignment feedback text, to be edited as formatted text (perhaps using the formatted text widget)
     */
    public String escapeAssignmentFeedbackTextarea(String value) {
        if (value == null || value.length() == 0) return value;

        value = fixAssignmentFeedback(value);

        return formattedText.escapeHtmlFormattedTextarea(value);
    }

    /**
     * Apply the fix to pre 1.1.05 assignments submissions feedback.
     */
    private String fixAssignmentFeedback(String value) {
        if (value == null || value.length() == 0) return value;

        StringBuilder buf = new StringBuilder(value);
        int pos = -1;

        // <br/> -> \n
        while ((pos = buf.indexOf("<br/>")) != -1) {
            buf.replace(pos, pos + "<br/>".length(), "\n");
        }

        // <span class='chefAlert'>( -> {{
        while ((pos = buf.indexOf("<span class='chefAlert'>(")) != -1) {
            buf.replace(pos, pos + "<span class='chefAlert'>(".length(), "{{");
        }

        // )</span> -> }}
        while ((pos = buf.indexOf(")</span>")) != -1) {
            buf.replace(pos, pos + ")</span>".length(), "}}");
        }

        while ((pos = buf.indexOf("<ins>")) != -1) {
            buf.replace(pos, pos + "<ins>".length(), "{{");
        }

        while ((pos = buf.indexOf("</ins>")) != -1) {
            buf.replace(pos, pos + "</ins>".length(), "}}");
        }

        return buf.toString();

    } // fixAssignmentFeedback

    public String buildLinkedPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        state.setAttribute(INVOKE, INVOKE_BY_LINK);
        return buildMainPanelContext(portlet, context, data, state);
    }

    /**
     * central place for dispatching the build routines based on the state name
     */
    public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        String template = null;
        context.put("action", "AssignmentAction");
        context.put("tlang", rb);
        context.put("dateFormat", getDateFormatString());
        context.put("cheffeedbackhelper", this);
        context.put("service", assignmentService);

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

        // allow add assignment?
        boolean allowAddAssignment = assignmentService.allowAddAssignment(contextString);
        context.put("allowAddAssignment", Boolean.valueOf(allowAddAssignment));

        Object allowGradeSubmission = state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION);

        // allow update site?
        boolean allowUpdateSite = siteService.allowUpdateSite((String) state.getAttribute(STATE_CONTEXT_STRING));
        context.put("allowUpdateSite", Boolean.valueOf(allowUpdateSite));

        //group related settings
        context.put("siteAccess", Assignment.Access.SITE);
        context.put("groupAccess", Assignment.Access.GROUP);

        // allow all.groups?
        boolean allowAllGroups = assignmentService.allowAllGroups(contextString);
        context.put("allowAllGroups", Boolean.valueOf(allowAllGroups));

        // allow recover assignment?
        boolean allowRecoverAssignment = assignmentService.allowRemoveAssignmentInContext(contextString);
        context.put("allowRecoverAssignment", allowRecoverAssignment);

        //Is the review service allowed?
        Site s = null;
        try {
            s = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
        } catch (IdUnusedException iue) {
            log.warn(this + ":buildMainPanelContext: Site not found!" + iue.getMessage());
        }

        // Check whether content review service is enabled, present and enabled for this site
        context.put("allowReviewService", assignmentService.allowReviewService(s));

        if (assignmentService.allowReviewService(s)) {
            //put the review service stings in context
            String reviewServiceName = contentReviewService.getServiceName();
            String reviewServiceTitle = rb.getFormattedMessage("review.title", new Object[]{reviewServiceName});
            String reviewServiceUse = rb.getFormattedMessage("review.use", new Object[]{reviewServiceName});
            String reviewServiceNonElectronic1 = rb.getFormattedMessage("review.switch.ne.1", reviewServiceName);
            String reviewServiceNonElectronic2 = rb.getFormattedMessage("review.switch.ne.2", reviewServiceName);
            context.put("reviewServiceName", reviewServiceName);
            context.put("reviewServiceProviderId", contentReviewService.getProviderId());
            context.put("turnitinProviderId", ContentReviewConstants.TURNITIN_PROVIDER_ID);
            context.put("turnitinOCProviderId", ContentReviewConstants.TURNITINOC_PROVIDER_ID);
            context.put("reviewServiceTitle", reviewServiceTitle);
            context.put("reviewServiceUse", reviewServiceUse);
            context.put("reviewIndicator", rb.getFormattedMessage("review.contentReviewIndicator", new Object[]{reviewServiceName}));
            context.put("reviewSwitchNe1", reviewServiceNonElectronic1);
            context.put("reviewSwitchNe2", reviewServiceNonElectronic2);
        }

        //Peer Assessment
        context.put("allowPeerAssessment", serverConfigurationService.getBoolean("assignment.usePeerAssessment", true));
        if ((Boolean) serverConfigurationService.getBoolean("assignment.usePeerAssessment", true)) {
            context.put("peerAssessmentName", rb.getFormattedMessage("peerAssessmentName", new Object[]{rb.getFormattedMessage("peerAssessmentStudents")}));
            context.put("peerAssessmentUse", rb.getFormattedMessage("peerAssessmentUse"));
        }

        // the grade type table
        context.put("gradeTypeTable", gradeTypeTable());

        // set the allowSubmitByInstructor option
        context.put("allowSubmitByInstructor", assignmentService.getAllowSubmitByInstructor());

        // get the system setting for whether to show the Option tool link or not
        context.put("enableViewOption", serverConfigurationService.getBoolean("assignment.enableViewOption", true));

        String mode = StringUtils.defaultIfBlank((String) state.getAttribute(STATE_MODE), MODE_LIST_ASSIGNMENTS);

        if (!MODE_LIST_ASSIGNMENTS.equals(mode)) {
            // allow grade assignment?
            if (state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION) == null) {
                state.setAttribute(STATE_ALLOW_GRADE_SUBMISSION, Boolean.FALSE);
            }
            context.put("allowGradeSubmission", state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION));
        }

        log.debug("MODE = {}", mode);
        switch (mode) {
            case MODE_LIST_ASSIGNMENTS:
                // build the context for the student assignment view
                template = build_list_assignments_context(portlet, context, data, state);
                break;
            case MODE_STUDENT_VIEW_ASSIGNMENT:
                // the student view of assignment
                template = build_student_view_assignment_context(portlet, context, data, state);
                break;
            case MODE_STUDENT_VIEW_GROUP_ERROR:
                // build the context for showing group submission error
                template = build_student_view_group_error_context(portlet, context, data, state);
                break;
            case MODE_STUDENT_VIEW_SUBMISSION:
                // build the context for showing one assignment submission
                template = build_student_view_submission_context(portlet, context, data, state);
                break;
            case MODE_STUDENT_VIEW_SUBMISSION_CONFIRMATION:
                context.put("site", s);

                // build the context for showing one assignment submission confirmation
                template = build_student_view_submission_confirmation_context(portlet, context, data, state);
                break;
            case MODE_STUDENT_CONFIRM_SUBMISSION:
                // build the context for confirming one assignment submission
                template = build_student_confirm_submission_context(portlet, context, data, state);
                break;
            case MODE_STUDENT_PREVIEW_SUBMISSION:
                // build the context for showing one assignment submission
                template = build_student_preview_submission_context(portlet, context, data, state);
                break;
            case MODE_STUDENT_VIEW_GRADE_PRIVATE:
            case MODE_STUDENT_VIEW_GRADE:
                context.put("site", s);
                // build the context for showing one graded submission
                template = build_student_view_grade_context(portlet, context, data, state);
                break;
            case MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT:
                // allow add assignment?
                boolean allowAddSiteAssignment = assignmentService.allowAddSiteAssignment(contextString);
                context.put("allowAddSiteAssignment", allowAddSiteAssignment);

                // build the context for the instructor's create new assignment view
                template = build_instructor_new_edit_assignment_context(portlet, context, data, state);
                break;
            case MODE_INSTRUCTOR_DELETE_ASSIGNMENT:
                if (state.getAttribute(SELECTED_ASSIGNMENT_IDS) != null) {
                    // build the context for the instructor's delete assignment
                    template = build_instructor_bulk_operation_context(portlet, context, data, state, BulkOperation.DELETE);
                }
                break;
            case MODE_INSTRUCTOR_PUBLISH_ASSIGNMENT:
                if (state.getAttribute(SELECTED_ASSIGNMENT_IDS) != null) {
                    // build the context for the instructor's publish assignment
                    template = build_instructor_bulk_operation_context(portlet, context, data, state, BulkOperation.PUBLISH);
                }
                break;
            case MODE_INSTRUCTOR_UNPUBLISH_ASSIGNMENT:
                if (state.getAttribute(SELECTED_ASSIGNMENT_IDS) != null) {
                    // build the context for the instructor's unpublish assignment
                    template = build_instructor_bulk_operation_context(portlet, context, data, state, BulkOperation.UNPUBLISH);
                }
                break;
            case MODE_INSTRUCTOR_GRADE_ASSIGNMENT:
                if (allowGradeSubmission != null && (Boolean) allowGradeSubmission) {
                    // if allowed for grading, build the context for the instructor's grade assignment
                    template = build_instructor_grade_assignment_context(portlet, context, data, state);
                }
                break;
            case MODE_INSTRUCTOR_GRADE_SUBMISSION:
                context.put("site", s);
                context.put("allowAddAssignment", allowAddAssignment);
                context.put("showStudentPhoto", serverConfigurationService.getBoolean("assignment.show.official.photo", true));
                if (allowGradeSubmission != null && (Boolean) allowGradeSubmission) {
                    // build the context for the instructor's grade submission
                    template = build_instructor_grade_submission_context(portlet, context, data, state);
                }
                break;
            case MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION:
                if (allowGradeSubmission != null && (Boolean) allowGradeSubmission) {
                    // if allowed for grading, build the context for the instructor's preview grade submission
                    template = build_instructor_preview_grade_submission_context(portlet, context, data, state);
                }
                break;
            case MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT:
                // build the context for preview one assignment
                template = build_instructor_preview_assignment_context(portlet, context, data, state);
                break;
            case MODE_INSTRUCTOR_VIEW_ASSIGNMENT:
                context.put("site", s);
                // build the context for view one assignment
                template = build_instructor_view_assignment_context(portlet, context, data, state);
                break;
            case MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT:
                if (allowGradeSubmission != null && (Boolean) allowGradeSubmission) {
                    // if allowed for grading, build the context for the instructor's create new assignment view
                    template = build_instructor_view_students_assignment_context(portlet, context, data, state);
                }
                break;
            case MODE_INSTRUCTOR_REPORT_SUBMISSIONS:
                context.put("site", s);
                if (allowGradeSubmission != null && (Boolean) allowGradeSubmission) {
                    // if allowed for grading, build the context for the instructor's view of report submissions
                    template = build_instructor_report_submissions(portlet, context, data, state);
                }
                break;
            case MODE_INSTRUCTOR_DOWNLOAD_ALL:
                if (allowGradeSubmission != null && (Boolean) allowGradeSubmission) {
                    // if allowed for grading, build the context for the instructor's view of uploading all info from archive file
                    template = build_instructor_download_upload_all(portlet, context, data, state);
                }
                break;
            case MODE_INSTRUCTOR_UPLOAD_ALL:
                if (allowGradeSubmission != null && (Boolean) allowGradeSubmission) {
                    // if allowed for grading, build the context for the instructor's view of uploading all info from archive file
                    template = build_instructor_download_upload_all(portlet, context, data, state);
                }
                break;
            case MODE_INSTRUCTOR_REORDER_ASSIGNMENT:
                context.put("site", s);

                // build the context for the instructor's create new assignment view
                template = build_instructor_reorder_assignment_context(portlet, context, data, state);
                break;
            case MODE_OPTIONS:
                if (allowUpdateSite) {
                    // build the options page
                    template = build_options_context(portlet, context, data, state);
                }
                break;
            case MODE_STUDENT_REVIEW_EDIT:
                template = build_student_review_edit_context(portlet, context, data, state);
                break;
            case MODE_LIST_DELETED_ASSIGNMENTS:
                if (allowRecoverAssignment) {
                    template = build_list_deleted_assignments_context(portlet, context, data, state);
                }
                break;
            case MODE_STUDENT_VIEW_ASSIGNMENT_HONORPLEDGE:
                template = build_student_view_assignment_honorPledge_context(portlet, context, data, state);
                break;
            case MODE_PERMISSIONS:
                template = build_permissions_context(portlet, context, data, state);
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
                break;
            case MODE_EXTERNAL_TOOL_LAUNCH:
                // build the context for launch external tool (LTI)
                template = build_view_external_tool_launch_context(portlet, context, data, state);
                break;
        }

        if (template == null) {
            // default to student list view
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
            template = build_list_assignments_context(portlet, context, data, state);
        }

        // this is a check for seeing if there are any assignments.  The check is used to see if we display a Reorder link in the vm files
        if (state.getAttribute(HAS_MULTIPLE_ASSIGNMENTS) != null) {
            context.put("assignmentscheck", state.getAttribute(HAS_MULTIPLE_ASSIGNMENTS));
        }

        return template;

    } // buildMainPanelContext

    /**
     * local function for getting assignment object
     *
     * @param assignmentReference
     * @param callingFunctionName
     * @param state
     * @return
     */
    private Assignment getAssignment(String assignmentReference, String callingFunctionName, SessionState state) {
        Assignment rv = null;
        SecurityAdvisor secAdv = null;
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();
        // if assignmentId is blank then it is not an assignment reference and is likely an id
        if (StringUtils.isBlank(assignmentId)) assignmentId = assignmentReference;
        Session session = sessionManager.getCurrentSession();

        try {
            secAdv = pushSecurityAdvisor(session, "assignment.security.advisor", false);
            rv = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException e) {
            log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentReference);
            addAlert(state, rb.getFormattedMessage("cannotfin_assignment", assignmentReference));
        } catch (PermissionException e) {
            log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + assignmentReference);
            addAlert(state, rb.getFormattedMessage("youarenot_viewAssignment", assignmentReference));
        } finally {
            securityService.popAdvisor(secAdv);
        }

        return rv;
    }

    /**
     * local function for getting assignment submission object
     *
     * @param submissionReference
     * @param callingFunctionName
     * @param state
     * @return
     */
    private AssignmentSubmission getSubmission(String submissionReference, String callingFunctionName, SessionState state) {
        log.debug("function {} requesting submission with reference = {}", callingFunctionName, submissionReference);
        AssignmentSubmission rv = null;
        SecurityAdvisor secAdv = null;
        String submissionId = AssignmentReferenceReckoner.reckoner().reference(submissionReference).reckon().getId();
        Session session = sessionManager.getCurrentSession();

        try {
            secAdv = pushSecurityAdvisor(session, "assignment.grade.security.advisor", false);
            rv = assignmentService.getSubmission(submissionId);
        } catch (IdUnusedException e) {
            log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + submissionId);
            addAlert(state, rb.getFormattedMessage("cannotfin_submission", submissionId));
        } catch (PermissionException e) {
            log.warn(this + ":" + callingFunctionName + " " + e.getMessage() + " " + submissionId);
            addAlert(state, rb.getFormattedMessage("youarenot_viewSubmission", submissionId));
        } finally {
            securityService.popAdvisor(secAdv);
        }

        return rv;
    }

    /**
     * local function for getting assignment submission object
     *
     * @param assignmentReference
     * @param callingFunctionName
     * @param state
     * @return
     */
    private AssignmentSubmission getSubmission(String assignmentReference, User user, String callingFunctionName, SessionState state) {
        if (StringUtils.isBlank(assignmentReference)) return null;
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();
        try {
            return assignmentService.getSubmission(assignmentId, user);
        } catch (PermissionException e) {
            log.warn("Couldn't get submission, {}", e.getMessage());
        }
        return null;
    }

    /**
     * local function for getting assignment submission object for a group id (or is that submitter id instead of group id)
     */
    private AssignmentSubmission getSubmission(String assignmentReference, String group_id, String callingFunctionName, SessionState state) {
        if (StringUtils.isBlank(assignmentReference)) return null;
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();
        try {
            return assignmentService.getSubmission(assignmentId, group_id);
        } catch (PermissionException e) {
            log.warn("Couldn't get submission, {}", e.getMessage());
        }
        return null;
    }

    private void clearContentReviewContext(Context context) {
        if (context == null) {
            return;
        }
        context.put("plagiarismNote", null);
        context.put("plagiarismFileTypes", null);
        context.put("plagiarismProviderInfo", null);
        context.put("plagiarismStudentPreview", null);
        context.put("plagiarismEULALink", null);
        context.put("name_plagiarism_eula_agreement", null);
        context.put("value_plagiarism_eula_agreement", null);
        context.put("name_check_plagiarism_eula_agreement", null);
    }

    private void clearContentReviewSubmissionState(SessionState state) {
        if (state == null) {
            return;
        }
        state.removeAttribute("plagiarismNote");
        state.removeAttribute("plagiarismFileTypes");
        state.removeAttribute("plagiarismProviderInfo");
        state.removeAttribute("plagiarismStudentPreview");
        state.removeAttribute("plagiarismEULALink");
        state.removeAttribute("eulaServiceLink");
        state.removeAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
        state.removeAttribute(AssignmentConstants.SUBMISSION_REVIEW_CHECK_SERVICE_EULA_AGREEMENT);
    }

    /**
     * build the student view of showing an assignment submission
     */
    private String build_student_view_submission_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        String invokedByStatus = (String) state.getAttribute(INVOKE);
        if (invokedByStatus != null) {
            if (invokedByStatus.equalsIgnoreCase(INVOKE_BY_LINK)) {
                context.put("linkInvoked", Boolean.TRUE);
            } else {
                context.put("linkInvoked", Boolean.FALSE);
            }
        } else {
            context.put("linkInvoked", Boolean.FALSE);
        }
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        context.put("context", contextString);
        clearContentReviewContext(context);
        context.put("NamePropSubmissionScaledPreviousGrades", ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES);
        context.put("showUserId", serverConfigurationService.getBoolean("assignment.users.ids.show", true));

        User user = (User) state.getAttribute(STATE_USER);
        log.debug(this + " BUILD SUBMISSION FORM WITH USER " + user.getId() + " NAME " + user.getDisplayName());
        String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        Assignment assignment = getAssignment(currentAssignmentReference, "build_student_view_submission_context", state);
        if (assignment == null || !assignment.getContentReview()) {
            clearContentReviewSubmissionState(state);
        }
        AssignmentSubmission s = null;
        boolean newAttachments = false;
        boolean isAnyRegTimeSheet = false;

        if (assignment != null) {
            context.put("assignment", assignment);
            context.put("canSubmit", assignmentService.canSubmit(assignment));

            Map<String, Reference> assignmentAttachmentReferences = new HashMap<>();
            assignment.getAttachments().forEach(r -> assignmentAttachmentReferences.put(r, entityManager.newReference(r)));
            context.put("assignmentAttachmentReferences", assignmentAttachmentReferences);

            if (assignment.getContentReview()) {
                Map<String, String> properties = assignment.getProperties();

                // Indicate that the student's submission is going to Turnitin, and whether their submission will be indexed to be compared against by other submissions
                boolean isSubmissionIndexed = "true".equalsIgnoreCase(properties.get("store_inst_index"));
                String plagiarismNoteKey = isSubmissionIndexed ? "gen.yoursubwill.indexed" : "gen.yoursubwill";
                state.setAttribute("plagiarismNote", rb.getFormattedMessage(plagiarismNoteKey, contentReviewService.getServiceName()));
                context.put("plagiarismNote", state.getAttribute("plagiarismNote"));

                if (!contentReviewService.allowAllContent() && assignmentSubmissionTypeTakesAttachments(assignment)) {
                    state.setAttribute("plagiarismFileTypes", rb.getFormattedMessage("gen.onlythefoll", getContentReviewAcceptedFileTypesMessage()));
                    context.put("plagiarismFileTypes", state.getAttribute("plagiarismFileTypes"));

                    String reviewServiceName = contentReviewService.getServiceName();
                    switch(reviewServiceName) {
                        case CONTENT_PROVIDER_URKUND:
                            long maxFileSizeBytes = Long.parseLong(serverConfigurationService.getString("urkund.maxFileSize", "20971520"));
                            String plagiarismProviderInfo = rb.getFormattedMessage("gen.onlythefoll." + reviewServiceName.toLowerCase(), new Object[]{maxFileSizeBytes/MEGABYTE});
                            state.setAttribute("plagiarismProviderInfo", plagiarismProviderInfo);
                            context.put("plagiarismProviderInfo", state.getAttribute("plagiarismProviderInfo"));
                            break;
                        case CONTENT_PROVIDER_TURNITIN:
                            state.setAttribute("plagiarismProviderInfo", rb.getString("gen.onlythefoll." + reviewServiceName.toLowerCase()));
                            context.put("plagiarismProviderInfo", state.getAttribute("plagiarismProviderInfo"));
                            break;
                        default:
                    }

                    // SAK-31649 commenting this out to remove file picker filters, as the results vary depending on OS and browser.
                    // If in the future browser support for the 'accept' attribute on a file picker becomes more robust and
                    // ubiquitous across browsers, we can re-enable this feature.
                    //context.put("content_review_acceptedMimeTypes", getContentReviewAcceptedMimeTypes());
                }
                try {
                    if (Boolean.valueOf(properties.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW))) {
                        state.setAttribute("plagiarismStudentPreview", rb.getString("gen.subStudentPreview"));
                        context.put("plagiarismStudentPreview", state.getAttribute("plagiarismStudentPreview"));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                //Check if content review service requires a EULA
                String eulaServiceLink = contentReviewService.getEndUserLicenseAgreementLink(user.getId());
                if(StringUtils.isNotEmpty(eulaServiceLink)) {
	                	Instant providerEulaTimestamp = contentReviewService.getEndUserLicenseAgreementTimestamp();
	                	Instant userEulaTimestamp = contentReviewService.getUserEULATimestamp(user.getId());
	                	String providerEulaVersion = contentReviewService.getEndUserLicenseAgreementVersion();
	                	String userEulaVersion = contentReviewService.getUserEULAVersion(user.getId());
	                	if((providerEulaTimestamp != null && (userEulaTimestamp == null || userEulaTimestamp.isBefore(providerEulaTimestamp)))
	                			|| (providerEulaVersion != null && (userEulaVersion == null || !userEulaVersion.equals(providerEulaVersion)))) {
	                		//user has either not accepted the required EULA or there is a new EULA that needs to be re-accepted
	                		context.put("plagiarismEULALink", eulaServiceLink);
	                		state.setAttribute("plagiarismEULALink", eulaServiceLink);
	                		context.put("name_plagiarism_eula_agreement", AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
	                		context.put("value_plagiarism_eula_agreement", state.getAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT) != null ? state.getAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT) : "false");
	                		context.put("name_check_plagiarism_eula_agreement", AssignmentConstants.SUBMISSION_REVIEW_CHECK_SERVICE_EULA_AGREEMENT);
	                	}
                }
            }
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                context.put("nonElectronicType", Boolean.TRUE);
            }
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION) {
                context.put("externalTool", Boolean.TRUE);
                if ( ! putExternalToolIntoContext(context, assignment, state) ) {
                    context.put("externalToolDeleted", Boolean.TRUE);
                    addAlert(state, rb.getString("contentitem.tool.deleted"));
                }
            }

            User submitter = (User) state.getAttribute("student");
            if (submitter == null) {
                submitter = user;
            }
            context.put("submitter", submitter);
            s = getSubmission(currentAssignmentReference, submitter, "build_student_view_submission_context", state);
            List<Reference> currentAttachments = (List<Reference>) state.getAttribute(ATTACHMENTS);

            if (s != null) {
                // figure out if attachments have been modified
                // the attachments from the previous submission
                Set<String> submittedAttachments = s.getAttachments();
                newAttachments = areAttachmentsModified(submittedAttachments, currentAttachments);
            }else {
                // new submission
                // if assignment is a group submission... send group id and not user id
                String submitterId = assignmentService.getSubmitterIdForAssignment(assignment, user.getId());
                try {
                    s = assignmentService.addSubmission(assignment.getId(), submitterId);
                    if (s != null ) {
                        setResubmissionProperties(assignment, s);
                        s.setSubmitted(true);
                        s.setUserSubmission(false);
                        s.setDateModified(Instant.now());
                        s.getSubmitters().stream().filter(sb -> sb.getSubmitter().equals(submitterId)).findAny().ifPresent(sb -> sb.setSubmittee(false));
                        assignmentService.updateSubmission(s);
                    }
                } catch (PermissionException e) {
                    log.warn("Could not add submission for assignment/submitter: {}/{}, {}", assignment.getId(), submitterId, e.getMessage());
                    addAlert(state, rb.getString("youarenot13"));
                }

                // There is no previous submission, attachments are modified if anything has been uploaded
                newAttachments = CollectionUtils.isNotEmpty(currentAttachments);
            }

            if (s != null) {
	            log.debug("BUILD SUBMISSION FORM HAS SUBMISSION FOR USER {}", submitter);
	            context.put("submission", s);
	            String currentUser = userDirectoryService.getCurrentUser().getId();
	            String grade = assignmentService.getGradeForSubmitter(s, currentUser);
	            context.put("grade", grade);
	            if (assignment.getIsGroup()) {
	                context.put("selectedGroup", s.getGroupId());
	                context.put("originalGroup", s.getGroupId());
	                context.put("submitterId", s.getGroupId());
	            }else {
	                s.getSubmitters().stream().findAny().ifPresent(u -> context.put("submitterId", u.getId()));
	            }

	            Optional<AssignmentSubmissionSubmitter> submitterA = s.getSubmitters().stream().findAny();
	            if(submitterA.isPresent()) {
	                String submissionTimeSpent = StringUtils.isNotBlank(submitterA.get().getTimeSpent()) ?
	                        submitterA.get().getTimeSpent() : "";
	                state.setAttribute(AssignmentConstants.ASSIGNMENT_INPUT_ADD_SUBMISSION_TIME_SPENT, submissionTimeSpent);

	                String timeSpent = assignmentService.getTotalTimeSheet(submitterA.get());
	                state.setAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT, timeSpent != null ? timeSpent : "");
	                if (StringUtils.isNotBlank(timeSpent)) {
		                context.put("timeSheetEntries", assignmentService.getTimeSheetEntries(submitterA.get()));
		                isAnyRegTimeSheet = true;
	                }
	            }
	            setScoringAgentProperties(context, assignment, s, false);

	            Map<String, Reference> submissionFeedbackAttachmentReferences = new HashMap<>();
	            s.getFeedbackAttachments().forEach(r -> submissionFeedbackAttachmentReferences.put(r, entityManager.newReference(r)));
	            context.put("submissionFeedbackAttachmentReferences", submissionFeedbackAttachmentReferences);

	            Map<String, String> p = s.getProperties();
	            if (p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null) {
	                context.put("prevFeedbackText", p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT));
	            }

	            if (p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null) {
	                context.put("prevFeedbackComment", p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
	            }

	            if (p.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS) != null) {
	                context.put("prevFeedbackAttachments", getPrevFeedbackAttachments(p));
	            }

	            String resubmitNumber = p.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) != null ? p.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) : "0";
	            context.put("allowResubmitNumber", resubmitNumber);
	            if (!"0".equals(resubmitNumber)) {
	                Instant resubmitTime = null;
	                if (p.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME) == null) {
	                    resubmitTime = assignment.getCloseDate();
	                } else {
	                    resubmitTime = Instant.ofEpochSecond(Long.parseLong(p.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME)));
	                }
	               context.put("allowResubmitCloseTime", resubmitTime);
	             }

	            context.put("isAnyRegTimeSheet", isAnyRegTimeSheet);
	            state.setAttribute("isAnyRegTimeSheet", isAnyRegTimeSheet);
	            // put the resubmit information into context
	            assignment_resubmission_option_into_context(context, state);
	            assignment_extension_option_into_context(context, state);
	            context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

	            rangeAndGroups.buildStudentViewSubmissionContext(state, context, user.getId(), assignment, this);

	            // can the student view model answer or not
	            canViewAssignmentIntoContext(context, assignment, s);

	            addAdditionalNotesToContext(submitter, context, state);

	            putSubmissionLogMessagesInContext(context, s);

	            context.put("rubricSelfReport", assignmentToolUtils.hasRubricSelfReview(assignment.getId()));
	            context.put("studentSelfReportMode", assignmentToolUtils.getRubricSelfReviewMode(assignment.getId()));
            }
        }

        if (taggingManager.isTaggable() && assignment != null) {
            addProviders(context, state);
            addActivity(context, assignment);
            context.put("taggable", Boolean.TRUE);
        }

        // name value pairs for the vm
        context.put("name_submission_text", VIEW_SUBMISSION_TEXT);
        context.put("value_submission_text", state.getAttribute(VIEW_SUBMISSION_TEXT));
        context.put("name_plagiarism_eula_agreement", AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
        context.put("value_plagiarism_eula_agreement", state.getAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT));
        context.put("attachments", stripInvisibleAttachments(state.getAttribute(ATTACHMENTS)));
        context.put("new_attachments", newAttachments);
        context.put("userDirectoryService", userDirectoryService);
        context.put(RUBRICS_EXPORT_PDF, serverConfigurationService.getBoolean(RubricsConstants.RBCS_EXPORT_PDF, true));

        context.put("contentTypeImageService", contentTypeImageService);
        context.put("currentTime", Instant.now());
        context.put("NamePropContentReviewOptoutUrl", ContentReviewConstants.URKUND_OPTOUT_URL);

        context.put("name_ASSIGNMENT_INPUT_ADD_TIME_SPENT", ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        context.put("value_ASSIGNMENT_INPUT_ADD_TIME_SPENT", state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT));

        // SAK-21525 - Groups were not being queried for authz
        boolean allowSubmit = assignmentService.allowAddSubmissionCheckGroups(assignment);
        if (!allowSubmit) {
            addAlert(state, rb.getString("not_allowed_to_submit"));
        }
        context.put("allowSubmit", allowSubmit);

        // put supplement item into context
        supplementItemIntoContext(state, context, assignment, s);

        initViewSubmissionListOption(state);
        String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
        String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
        Boolean searchFilterOnly = Boolean.TRUE.equals(state.getAttribute(SUBMISSIONS_SEARCH_ONLY));

        // if the instructor is allowed to submit assignment on behalf of student, add the student list to the page
        User student = (User) state.getAttribute("student");
        if (assignmentService.getAllowSubmitByInstructor() && student != null) {
            List<String> submitterIds = assignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, currentAssignmentReference, contextString);
            if (submitterIds != null && !submitterIds.isEmpty() && submitterIds.contains(student.getId())) {
                // we want to come back to the instructor view page
                state.setAttribute(FROM_VIEW, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
                context.put("student", student);
            }
        }

        String template = getContext(data).get("template");
        return template + TEMPLATE_STUDENT_VIEW_SUBMISSION;

    } // build_student_view_submission_context

    /**
     * build the external tool launch (LTI) view
     */
    private String build_view_external_tool_launch_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        context.put("context", contextString);

        User user = (User) state.getAttribute(STATE_USER);
        if (log.isDebugEnabled())
            log.debug(this + " BUILD SUBMISSION GROUP ERROR WITH USER " + user.getId() + " NAME " + user.getDisplayName());
        String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        Assignment assignment = getAssignment(currentAssignmentReference, "build_student_view_submission_context", state);

        if (assignment == null) {
			String template = getContext(data).get("template");
			return template + TEMPLATE_VIEW_LAUNCH;
        }

       boolean newpage = assignment.getContentLaunchNewWindow();

        context.put("assignment", assignment);

        if ( assignment.getContentId() != null ) {
            try {
                Long contentKey = assignment.getContentId().longValue();
                Site site = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
                Map<String, Object> content = ltiService.getContent(contentKey, site.getId());
                String content_launch = ltiService.getContentLaunch(content);
                context.put("value_ContentLaunchURL", content_launch);
                context.put("placement", "assignment_launch_"+contentKey);

                // Figure out if this is a submission in context of a group - there
                // should only be one group for the current user
                String courseGroupId = null;
                try {
                    courseGroupId = assignment.getIsGroup() ?
                        assignmentService.getSubmission(assignment.getId(), user.getId()).getGroupId() : null;
                } catch(PermissionException e) {
                    courseGroupId = null;
                }
                Long toolKey = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
                Map<String, Object> tool = null;
                if (toolKey != null) {
                    tool = ltiService.getTool(toolKey, site.getId());
                }

                // Ignore the Content Item - use the value in the assignment if tool allows
                context.put("newpage", Boolean.valueOf(SakaiLTIUtil.getNewpage(tool, null, newpage)));
                context.put("height",SakaiLTIUtil.getFrameHeight(tool, content, "1200px"));
                context.put("browser-feature-allow", serverConfigurationService.getBrowserFeatureAllowString());

                // Copy title, description, and dates from Assignment to content if mis-match
                int protect = LTIUtil.toInt(content.get(LTIService.LTI_PROTECT));
                String assignmentTitle = StringUtils.trimToEmpty(assignment.getTitle());
                String assignmentDesc = StringUtils.trimToEmpty(assignment.getInstructions());
                Instant visibleDate = assignment.getVisibleDate();
                String assignmentVisibleDate = StringUtils.trimToEmpty(visibleDate == null ? null : visibleDate.toString());
                Instant openDate = assignment.getOpenDate();
                String assignmentOpenDate = StringUtils.trimToEmpty(openDate == null ? null : openDate.toString());
                Instant dueDate = assignment.getDueDate();
                String assignmentDueDate = StringUtils.trimToEmpty(dueDate == null ? null : dueDate.toString());
                Instant closeDate = assignment.getCloseDate();
                String assignmentCloseDate = StringUtils.trimToEmpty(closeDate == null ? null : closeDate.toString());

                String contentTitle = StringUtils.trimToEmpty((String) content.get(LTIService.LTI_TITLE));
                String contentDesc = StringUtils.trimToEmpty((String) content.get(LTIService.LTI_DESCRIPTION));

                String placement_secret = StringUtils.trimToNull((String) content.get(LTIService.LTI_PLACEMENTSECRET));

                String content_settings = (String) content.get(LTIService.LTI_SETTINGS);
                JSONObject content_json = LTIUtil.parseJSONObject(content_settings);
                String contentVisibleDate = StringUtils.trimToEmpty((String) content_json.get(DeepLinkResponse.RESOURCELINK_AVAILABLE_STARTDATETIME));
                String contentOpenDate = StringUtils.trimToEmpty((String) content_json.get(DeepLinkResponse.RESOURCELINK_SUBMISSION_STARTDATETIME));
                String contentDueDate = StringUtils.trimToEmpty((String) content_json.get(DeepLinkResponse.RESOURCELINK_SUBMISSION_ENDDATETIME));
                String contentCloseDate = StringUtils.trimToEmpty((String) content_json.get(DeepLinkResponse.RESOURCELINK_AVAILABLE_ENDDATETIME));
                if ( protect < 1 || !assignmentTitle.equals(contentTitle) || !assignmentDesc.equals(contentDesc) ||
                        ! contentVisibleDate.equals(assignmentVisibleDate) || ! contentOpenDate.equals(assignmentOpenDate) ||
                        ! contentDueDate.equals(assignmentDueDate) || ! contentCloseDate.equals(assignmentCloseDate) ||
                        placement_secret == null ) {
                    Map<String, Object> updates = new TreeMap<String, Object>();
                    updates.put(LTIService.LTI_TITLE, assignmentTitle);
                    updates.put(LTIService.LTI_DESCRIPTION, assignmentDesc);
                    updates.put(LTIService.LTI_PROTECT, new Integer(1));
                    if ( placement_secret == null ) {
                        placement_secret = UUID.randomUUID().toString();
                        updates.put(LTIService.LTI_PLACEMENTSECRET, placement_secret);
                        // Need to update for the protection key computation to work
                        content.put(LTIService.LTI_PLACEMENTSECRET, placement_secret);
                    }

                    // SAK-43709 - Prior to Sakai-21 - also copy these in the settings area
                    content_json.put(LTIService.LTI_DESCRIPTION, assignmentDesc);
                    content_json.put(LTIService.LTI_PROTECT, new Integer(1));

                    // Copy assignment specific custom parameter substitutions to pass into SakaiLTIUtil
                    content_json.put(DeepLinkResponse.RESOURCELINK_AVAILABLE_STARTDATETIME, assignmentVisibleDate);
                    content_json.put(DeepLinkResponse.RESOURCELINK_SUBMISSION_STARTDATETIME, assignmentOpenDate);
                    content_json.put(DeepLinkResponse.RESOURCELINK_AVAILABLE_ENDDATETIME, assignmentDueDate);
                    content_json.put(DeepLinkResponse.RESOURCELINK_SUBMISSION_ENDDATETIME, assignmentCloseDate);
                    content_json.put(LTICustomVars.COURSEGROUP_ID, courseGroupId);
                    updates.put(LTIService.LTI_SETTINGS, content_json.toString());

                    // This uses the Dao access since 99% of the time we are launching as a student
                    // after the instructor updates the assignment, and the student is
                    // the first to launch after the change.
                    ltiService.updateContentDao(contentKey, updates);
                    log.debug("Content Item id={} updated.", contentKey);
                }

                // Unlock this assignment for one launch...
                String launch_code_key = SakaiLTIUtil.getLaunchCodeKey(content);
                String launch_code = SakaiLTIUtil.getLaunchCode(content);
                if ( launch_code_key != null && launch_code != null ) {
                    Session session = sessionManager.getCurrentSession();
                    session.setAttribute(launch_code_key, launch_code);

                }
            } catch(org.sakaiproject.exception.IdUnusedException e ) {
                // Send error to template
                context.put("value_ContentLaunchURL", null);
            }
        }

        context.put("browser-feature-allow", serverConfigurationService.getBrowserFeatureAllowString());

        String template = getContext(data).get("template");
        return template + TEMPLATE_VIEW_LAUNCH;

    } // build_view_external_tool_launch_context

    /**
     * Determines if the attachments have been modified
     *
     * @return true if currentAttachments isn't equal to oldAttachments
     */
    private boolean areAttachmentsModified(Set<String> oldAttachments, List<Reference> currentAttachments) {
        boolean hasCurrent = CollectionUtils.isNotEmpty(currentAttachments);
        boolean hasOld = CollectionUtils.isNotEmpty(oldAttachments);

        if (!hasCurrent) {
            //there are no current attachments
            return hasOld;
        }
        if (!hasOld) {
            //there are no old attachments (and there are new ones)
            return true;
        }

        Set<String> currentSet = currentAttachments.stream().map(Reference::getReference).collect(Collectors.toSet());
        //.equals on Sets of Strings will compare .equals on the contained Strings
        return !oldAttachments.equals(currentSet);
    }

    /**
     * Returns a clone of the passed in List of attachments minus any attachments that should not be displayed in the UI
     */
    private List<Reference> stripInvisibleAttachments(Object attachments) {
        List<Reference> stripped = new ArrayList<>();
        if (attachments == null || !(attachments instanceof List)) {
            return stripped;
        }
        Iterator itAttachments = ((List) attachments).iterator();
        while (itAttachments.hasNext()) {
            Object next = itAttachments.next();
            if (next instanceof Reference) {
                Reference attachment = (Reference) next;
                // inline submissions should not show up in the UI's lists of attachments
                if (isVisibleAttachment(attachment)) {
                    stripped.add(attachment);
                }
            }
        }
        return stripped;
    }

    /**
     * Filters the submission's attachments to include only attachments that should be displayed in the UI,
     * then returns as a map of attachment reference IDs to their associated references
     */
    private Map<String, Reference> getVisibleAttachmentIdsToReferences(AssignmentSubmission submission) {
        Map<String, Reference> submissionAttachmentReferences = new HashMap<>();
        submission.getAttachments().forEach(refId -> {
            Reference reference = entityManager.newReference(refId);
            if (isVisibleAttachment(reference)) {
                submissionAttachmentReferences.put(refId, reference);
            }
        });
        return submissionAttachmentReferences;
    }

    private boolean isVisibleAttachment(Reference r) {
        return r.getProperties() != null && !"true".equals(r.getProperties().getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION));
    }

    /**
     * Get a list of accepted mime types suitable for an 'accept' attribute in an html file picker
     *
     * @throws IllegalArgumentException if the assignment accepts all attachments
     */
    private String getContentReviewAcceptedMimeTypes() {
        if (contentReviewService.allowAllContent()) {
            throw new IllegalArgumentException("getContentReviewAcceptedMimeTypes invoked, but the content review service accepts all attachments");
        }

        StringBuilder mimeTypes = new StringBuilder();
        Collection<SortedSet<String>> mimeTypesCollection = contentReviewService.getAcceptableExtensionsToMimeTypes().values();
        String delimiter = "";
        for (SortedSet<String> mimeTypesList : mimeTypesCollection) {
            for (String mimeType : mimeTypesList) {
                mimeTypes.append(delimiter).append(mimeType);
                delimiter = ",";
            }
        }
        return mimeTypes.toString();
    }

    /**
     * return true if the assignment's submission type takes attachments.
     *
     * @throws IllegalArgumentException if assignment is null
     */
    private boolean assignmentSubmissionTypeTakesAttachments(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("assignmentSubmissionTypeTakesAttachments invoked with assignment = null");
        }

        Assignment.SubmissionType submissionType = assignment.getTypeOfSubmission();

        if (submissionType == Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION) {
            return true;
        }
        if (submissionType == Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION) {
            return true;
        }
        if (submissionType == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
            return true;
        }

        return false;
    }

    /**
     * build the student view of showing a group assignment error with eligible groups
     * a user can only be in one eligible group
     *
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return the student error message for this context
     */
    protected String build_student_view_group_error_context(VelocityPortlet portlet, Context context, RunData data,
                                                            SessionState state) {
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        context.put("context", contextString);

        User user = (User) state.getAttribute(STATE_USER);
        if (log.isDebugEnabled())
            log.debug(this + " BUILD SUBMISSION GROUP ERROR WITH USER " + user.getId() + " NAME " + user.getDisplayName());
        String currentAssignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        Assignment assignment = getAssignment(currentAssignmentReference, "build_student_view_submission_context", state);

        if (assignment != null) {
            context.put("assignment", assignment);
        }

        if (taggingManager.isTaggable() && assignment != null) {
            addProviders(context, state);
            addActivity(context, assignment);
            context.put("taggable", Boolean.TRUE);
        }
        context.put("userDirectoryService", userDirectoryService);
        context.put("currentTime", Instant.now());

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_STUDENT_VIEW_GROUP_ERROR;
    } // build_student_view_group_error_context

    /**
     * build the student view of showing an assignment submission confirmation
     */
    private String build_student_view_submission_confirmation_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        context.put("context", contextString);

        String invokedByStatus = (String) state.getAttribute(INVOKE);
        if (invokedByStatus != null) {
            if (invokedByStatus.equalsIgnoreCase(INVOKE_BY_LINK)) {
                context.put("linkInvoked", Boolean.valueOf(true));
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
            } else {
                context.put("linkInvoked", Boolean.valueOf(false));
            }
        } else {
            context.put("linkInvoked", Boolean.valueOf(false));
        }


        context.put("view", MODE_LIST_ASSIGNMENTS);
        // get user information
        User user = (User) state.getAttribute(STATE_USER);
        String submitterId = (String) state.getAttribute(STATE_SUBMITTER);
        User submitter = user;
        if (submitterId != null) {
            try {
                submitter = userDirectoryService.getUser(submitterId);
            } catch (UserNotDefinedException ex) {
                log.warn(this + ":build_student_view_submission cannot find user with id " + submitterId + " " + ex.getMessage());
            }
        }
        context.put("user_name", submitter.getDisplayName());
        context.put("user_id", submitter.getDisplayId());
        if (StringUtils.trimToNull(user.getEmail()) != null)
            context.put("user_email", user.getEmail());

        // get site information
        try {
            // get current site
            Site site = siteService.getSite(contextString);
            context.put("site_title", site.getTitle());
        } catch (Exception e) {
            log.warn("Could not find site with id={}, {}", contextString, e.toString());
        }

        // get assignment and submission information
        String assignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        Assignment assignment = getAssignment(assignmentReference, "build_student_view_submission_confirmation_context", state);
        if (assignment != null) {
            context.put("assignment", assignment);

            // differenciate submission type
            Assignment.SubmissionType submissionType = assignment.getTypeOfSubmission();
            if (submissionType == Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION || submissionType == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
                context.put("attachmentSubmissionOnly", Boolean.TRUE);
            } else {
                context.put("attachmentSubmissionOnly", Boolean.FALSE);
            }
            if (submissionType == Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION) {
                context.put("textSubmissionOnly", Boolean.TRUE);
            } else {
                context.put("textSubmissionOnly", Boolean.FALSE);
            }

            AssignmentSubmission submission = getSubmission(assignmentReference, submitter, "build_student_view_submission_confirmation_context", state);
            if (submission != null) {
                context.put("submission", submission);
                context.put("submissionAttachmentReferences", getVisibleAttachmentIdsToReferences(submission));
                context.put("submit_text", StringUtils.trimToNull(submission.getSubmittedText()));
                context.put("email_confirmation", serverConfigurationService.getBoolean("assignment.submission.confirmation.email", true));

                if (assignment.getIsGroup()) {
					context.put("submitterNames", getSubmitterFormattedNames(submission, false));
                }

                putSubmissionLogMessagesInContext(context, submission);
            }
        }

        context.put("name_ASSIGNMENT_INPUT_ADD_TIME_SPENT", ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        context.put("value_ASSIGNMENT_INPUT_ADD_TIME_SPENT", state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT));
        context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

        try {
            // Get site ID
            String siteId = toolManager.getCurrentPlacement().getContext();
            // Get site by ID
            Site currentSite = siteService.getSite(siteId);
            // Assignments Tool Configuration
            ToolConfiguration toolConfig = currentSite.getToolForCommonId(AssignmentConstants.TOOL_ID);

            // Get visibility value of assignments tool
            String isAssignmentsVisible = toolConfig.getConfig().getProperty(ToolManager.PORTAL_VISIBLE);
            boolean isVisible = StringUtils.isBlank(isAssignmentsVisible) || StringUtils.equalsIgnoreCase(isAssignmentsVisible, Boolean.TRUE.toString());

            // Checks if the assignments tool is visible from the LHS Menu.
            context.put("isAssignmentsToolVisible", isVisible);

        } catch(IdUnusedException e) {
            log.error(e.getMessage(), e);
        }

        state.removeAttribute(STATE_SUBMITTER);
        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_STUDENT_VIEW_SUBMISSION_CONFIRMATION;

    } // build_student_view_submission_confirmation_context

    //Get all submitter names from submission, comma separated and processed in formattedText
	private String getSubmitterFormattedNames(AssignmentSubmission s, boolean includeDisplayID) {
		final Map<String, User> users = assignmentToolUtils.getSubmitters(s)
						.collect(Collectors.toMap(User::getId, Function.identity()));
		final String submitterNames;
		if (includeDisplayID) {
			submitterNames = users.values().stream().map(u -> u.getDisplayName() + " (" + u.getDisplayId() + ")").collect(Collectors.joining(", "));
		} else {
			submitterNames = users.values().stream().map(u -> u.getDisplayName()).collect(Collectors.joining(", "));
		}
		return formattedText.escapeHtml(submitterNames);
	}

    /**
     * build the student view of assignment
     *
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return
     */
    protected String build_student_view_assignment_context(VelocityPortlet portlet, Context context, RunData data,
                                                           SessionState state) {
        context.put("context", state.getAttribute(STATE_CONTEXT_STRING));

        String aReference = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
        User user = (User) state.getAttribute(STATE_USER);

        AssignmentSubmission submission = null;

        Assignment assignment = getAssignment(aReference, "build_student_view_assignment_context", state);
        if (assignment != null) {
            context.put("assignment", assignment);
            context.put("assignmentReference", aReference);

            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                context.put("nonElectronicType", Boolean.TRUE);
            }

            // put creator information into context
            putCreatorIntoContext(context, assignment);

            submission = getSubmission(aReference, user, "build_student_view_assignment_context", state);
            context.put("submission", submission);

            rangeAndGroups.buildStudentViewAssignmentContext(state, user.getId(), assignment);

            // can the student view model answer or not
            canViewAssignmentIntoContext(context, assignment, submission);

            // put resubmit information into context
            assignment_resubmission_option_into_context(context, state);
            assignment_extension_option_into_context(context, state);

            Map<String, Reference> assignmentAttachmentReferences = new HashMap<>();
            assignment.getAttachments().forEach(r -> assignmentAttachmentReferences.put(r, entityManager.newReference(r)));
            context.put("assignmentAttachmentReferences", assignmentAttachmentReferences);
        }

        if (taggingManager.isTaggable() && assignment != null) {
            addProviders(context, state);
            addActivity(context, assignment);
            context.put("taggable", Boolean.TRUE);
        }

        context.put("contentTypeImageService", contentTypeImageService);
        context.put("userDirectoryService", userDirectoryService);

        // put supplement item into context
        supplementItemIntoContext(state, context, assignment, submission);

        context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_STUDENT_VIEW_ASSIGNMENT;

    } // build_student_view_assignment_context

    protected String build_student_view_assignment_honorPledge_context(final VelocityPortlet portlet, final Context context, final RunData data, final SessionState state) {
        String assignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        String template = getContext(data).get("template");

        Assignment a = getAssignment(assignmentReference, "build_student_view_assignment_honorPledge_context", state);
        if (a != null) {
            context.put("assignment", a);
            context.put("assignmentReference", assignmentReference);
            context.put("honorPledgeText", serverConfigurationService.getString("assignment.honor.pledge", rb.getString("gen.hpa.text")));
        } else {
            addAlert(state, rb.getString("youarenot18"));
            doList_assignments(data);
            return template + TEMPLATE_LIST_ASSIGNMENTS;
        }

        return template + TEMPLATE_STUDENT_VIEW_ASSIGNMENT_HONORPLEDGE;
    }

    protected String build_student_confirm_submission_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        User user = (User) state.getAttribute(STATE_USER);
        String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        clearContentReviewContext(context);

        Assignment assignment = getAssignment(aReference, "build_student_confirm_submission_context", state);
        if (assignment == null || !assignment.getContentReview()) {
            clearContentReviewSubmissionState(state);
        }
        if (assignment != null) {
            context.put("assignment", assignment);
            context.put("assignmentReference", AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
            context.put("typeOfGradeString", getTypeOfGradeString(assignment.getTypeOfGrade()));
            context.put("canSubmit", assignmentService.canSubmit(assignment));

            AssignmentSubmission submission = getSubmission(aReference, user, "build_student_confirm_submission_context", state);
            if (submission != null) {
                context.put("submission", submission);
                String currentUser = userDirectoryService.getCurrentUser().getId();
                String grade = assignmentService.getGradeForSubmitter(submission, currentUser);
                context.put("grade", grade);
                context.put("submissionReference", AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference());
                if (assignment.getIsGroup()) {
                    context.put("selectedGroup", submission.getGroupId());
                }
            }

            if (assignment.getIsGroup() && state.getAttribute(VIEW_SUBMISSION_GROUP) != null) {
                context.put(VIEW_SUBMISSION_GROUP, (String) state.getAttribute(VIEW_SUBMISSION_GROUP));
            }

            setScoringAgentProperties(context, assignment, submission, false);

            // can the student view model answer or not
            canViewAssignmentIntoContext(context, assignment, submission);

            // put the resubmit information into context
            assignment_resubmission_option_into_context(context, state);

            if (state.getAttribute(SAVED_FEEDBACK) != null) {
                context.put("savedFeedback", Boolean.TRUE);
                state.removeAttribute(SAVED_FEEDBACK);
            }
            if (state.getAttribute(OW_FEEDBACK) != null) {
                context.put("overwriteFeedback", Boolean.TRUE);
                state.removeAttribute(OW_FEEDBACK);
            }
            if (state.getAttribute(RETURNED_FEEDBACK) != null) {
                context.put("returnedFeedback", Boolean.TRUE);
                state.removeAttribute(RETURNED_FEEDBACK);
            }
            if (assignment.getContentReview()) {
                context.put("plagiarismStudentPreview", state.getAttribute("plagiarismStudentPreview"));
                context.put("plagiarismFileTypes", state.getAttribute("plagiarismFileTypes"));
                context.put("plagiarismProviderInfo", state.getAttribute("plagiarismProviderInfo"));
                context.put("plagiarismNote", state.getAttribute("plagiarismNote"));
                context.put("name_plagiarism_eula_agreement", AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
                context.put("value_plagiarism_eula_agreement", state.getAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT));
                context.put("plagiarismEULALink", state.getAttribute("eulaServiceLink"));
                context.put("name_check_plagiarism_eula_agreement", AssignmentConstants.SUBMISSION_REVIEW_CHECK_SERVICE_EULA_AGREEMENT);
            }
        }

        context.put("submitter", Optional.ofNullable((User) state.getAttribute("student")).orElse(user));
        context.put("rubricSelfReport", assignmentToolUtils.hasRubricSelfReview(assignment.getId()));

        context.put("text", state.getAttribute(VIEW_SUBMISSION_TEXT));
        Map<String, Reference> submissionAttachmentReferences = new HashMap<>();
        stripInvisibleAttachments(state.getAttribute(ATTACHMENTS)).forEach(r -> submissionAttachmentReferences.put(r.getId(), r));
        context.put("name_ASSIGNMENT_INPUT_ADD_TIME_SPENT", ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        context.put("value_ASSIGNMENT_INPUT_ADD_TIME_SPENT", state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT));

        context.put("submissionAttachmentReferences", submissionAttachmentReferences);
        context.put("contentTypeImageService", contentTypeImageService);

        context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

        //confirm custom messages
        String confirmMessage = serverConfigurationService.getString("assignment.submissionConfirmMessage", rb.getFormattedMessage("submissionConfirmMessage"));
        context.put("studentConfirmMessage", confirmMessage);
        context.put("submissionConfirmOptionsRequired", serverConfigurationService.getBoolean("assignment.submissionConfirmOptionsRequired", false));
        String[] submissionConfirmOptionsList = serverConfigurationService.getStrings("assignment.submissionConfirmOptions");
        List<String> items = new ArrayList<>();
        if (submissionConfirmOptionsList != null) {
            for (String item : submissionConfirmOptionsList) {
                if (StringUtils.isNotBlank(item)) {
                    items.add(item);
                }
            }
        }
        context.put("submissionConfirmOptions", items);
        context.put("submissionConfirmAlert", rb.getFormattedMessage("submissionConfirmAlert"));

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_STUDENT_CONFIRM_SUBMISSION;
    }

    /**
     * build the student preview of showing an assignment submission
     *
     * @param portlet
     * @param context
     * @param data
     * @param state
     * @return
     */
    protected String build_student_preview_submission_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        User user = (User) state.getAttribute(STATE_USER);
        String aReference = (String) state.getAttribute(PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        clearContentReviewContext(context);

        Assignment assignment = getAssignment(aReference, "build_student_preview_submission_context", state);
        if (assignment == null || !assignment.getContentReview()) {
            clearContentReviewSubmissionState(state);
        }
        if (assignment != null) {
            context.put("assignment", assignment);
            context.put("assignmentReference", AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
            context.put("typeOfGradeString", getTypeOfGradeString(assignment.getTypeOfGrade()));
            context.put("canSubmit", assignmentService.canSubmit(assignment));

            AssignmentSubmission submission = getSubmission(aReference, user, "build_student_preview_submission_context", state);
            if (submission != null) {
                context.put("submission", submission);
                String currentUser = userDirectoryService.getCurrentUser().getId();
                String grade = assignmentService.getGradeForSubmitter(submission, currentUser);
                context.put("grade", grade);
                context.put("submissionReference", AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference());
            }

            if (assignment.getIsGroup() && state.getAttribute(VIEW_SUBMISSION_GROUP) != null) {
                context.put(VIEW_SUBMISSION_GROUP, (String) state.getAttribute(VIEW_SUBMISSION_GROUP));
            }

            setScoringAgentProperties(context, assignment, submission, false);

            // can the student view model answer or not
            canViewAssignmentIntoContext(context, assignment, submission);

            // put the resubmit information into context
            assignment_resubmission_option_into_context(context, state);
            assignment_extension_option_into_context(context, state);

            if (state.getAttribute(SAVED_FEEDBACK) != null) {
                context.put("savedFeedback", Boolean.TRUE);
                state.removeAttribute(SAVED_FEEDBACK);
            }
            if (state.getAttribute(OW_FEEDBACK) != null) {
                context.put("overwriteFeedback", Boolean.TRUE);
                state.removeAttribute(OW_FEEDBACK);
            }
            if (state.getAttribute(RETURNED_FEEDBACK) != null) {
                context.put("returnedFeedback", Boolean.TRUE);
                state.removeAttribute(RETURNED_FEEDBACK);
            }
            if (assignment.getContentReview()) {
                context.put("plagiarismStudentPreview", state.getAttribute("plagiarismStudentPreview"));
                context.put("plagiarismFileTypes", state.getAttribute("plagiarismFileTypes"));
                context.put("plagiarismProviderInfo", state.getAttribute("plagiarismProviderInfo"));
                context.put("plagiarismNote", state.getAttribute("plagiarismNote"));
                context.put("name_plagiarism_eula_agreement", AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
                context.put("value_plagiarism_eula_agreement", state.getAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT));
                context.put("plagiarismEULALink", state.getAttribute("eulaServiceLink"));
                context.put("name_check_plagiarism_eula_agreement", AssignmentConstants.SUBMISSION_REVIEW_CHECK_SERVICE_EULA_AGREEMENT);
            }
        }

        context.put("text", state.getAttribute(PREVIEW_SUBMISSION_TEXT));
        context.put("name_ASSIGNMENT_INPUT_ADD_TIME_SPENT", ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        context.put("value_ASSIGNMENT_INPUT_ADD_TIME_SPENT", state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT));

        Map<String, Reference> submissionAttachmentReferences = new HashMap<>();
        stripInvisibleAttachments(state.getAttribute(PREVIEW_SUBMISSION_ATTACHMENTS)).forEach(r -> submissionAttachmentReferences.put(r.getId(), r));
        context.put("submissionAttachmentReferences", submissionAttachmentReferences);
        context.put("contentTypeImageService", contentTypeImageService);

        context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_STUDENT_PREVIEW_SUBMISSION;
    } // build_student_preview_submission_context

    private void canViewAssignmentIntoContext(Context context, Assignment assignment, AssignmentSubmission submission) {
        boolean canViewModelAnswer = assignmentSupplementItemService.canViewModelAnswer(assignment, submission);
        context.put("allowViewModelAnswer", Boolean.valueOf(canViewModelAnswer));
        if (canViewModelAnswer) {
            context.put("modelAnswer", assignmentSupplementItemService.getModelAnswer(assignment.getId()));
        }
    }

    /**
     * Look up a security advisor from the session with the given key, and then push it on the security service stack.
     *
     * @param session
     * @param sessionKey        String key used to look up a SecurityAdvisor stored in the session object
     * @param removeFromSession boolean flag indicating if the value should be removed from the session once retrieved
     * @return
     */
    private SecurityAdvisor pushSecurityAdvisor(Session session, String sessionKey, boolean removeFromSession) {
        SecurityAdvisor asgnAdvisor = (SecurityAdvisor) session.getAttribute(sessionKey);
        if (asgnAdvisor != null) {
            securityService.pushAdvisor(asgnAdvisor);
            if (removeFromSession)
                session.removeAttribute(sessionKey);
        }
        return asgnAdvisor;
    }

    /**
     * If necessary, put a "decoratedUrlMap" into the context
     *
     * @param session
     * @param context           Context object that will have a "decoratedUrlMap" object put into it
     * @param removeFromSession boolean flag indicating if the value should be removed from the session once retrieved
     */
    private void addDecoUrlMapToContext(Session session, Context context, boolean removeFromSession) {
        SecurityAdvisor contentAdvisor = (SecurityAdvisor) session.getAttribute("assignment.content.security.advisor");

        String decoratedContentWrapper = (String) session.getAttribute("assignment.content.decoration.wrapper");
        String[] contentRefs = (String[]) session.getAttribute("assignment.content.decoration.wrapper.refs");

        if (removeFromSession) {
            session.removeAttribute("assignment.content.decoration.wrapper");
            session.removeAttribute("assignment.content.decoration.wrapper.refs");
        }

        if (contentAdvisor != null && contentRefs != null) {
            securityService.pushAdvisor(contentAdvisor);

            Map<String, String> urlMap = new HashMap<String, String>();
            for (String refStr : contentRefs) {
                Reference ref = entityManager.newReference(refStr);
                String url = ref.getUrl();
                urlMap.put(url, url.replaceFirst("access/content", "access/" + decoratedContentWrapper + "/content"));
            }
            context.put("decoratedUrlMap", urlMap);
            securityService.popAdvisor(contentAdvisor);
        }
    }

    /**
     * build the student view of showing a graded submission
     */
    protected String build_student_view_grade_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        context.put("contentTypeImageService", contentTypeImageService);

        Session session = sessionManager.getCurrentSession();
        addDecoUrlMapToContext(session, context, false);
        SecurityAdvisor asgnAdvisor = pushSecurityAdvisor(session, "assignment.security.advisor", false);

        String submissionId = (String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID);
        AssignmentSubmission submission = getSubmission(submissionId, "build_student_view_grade_context", state);
        Assignment assignment = null;
        if (submission != null) {
            assignment = submission.getAssignment();
            context.put("assignment", assignment);

            context.put("typeOfGradeString", getTypeOfGradeString(assignment.getTypeOfGrade()));
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                context.put("nonElectronicType", Boolean.TRUE);
            }
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION) {
                context.put("externalTool", Boolean.TRUE);
            }

            context.put("users", assignmentService.getSubmissionSubmittersAsUsers(submission));
            context.put("rubricSelfReport", assignmentToolUtils.hasRubricSelfReview(assignment.getId()));
            context.put("rubricHiddenToStudent", assignmentToolUtils.hasRubricHiddenToStudent(assignment.getId()));

            Map<String, Reference> assignmentAttachmentReferences = new HashMap<>();
            assignment.getAttachments().forEach(r -> assignmentAttachmentReferences.put(r, entityManager.newReference(r)));
            context.put("assignmentAttachmentReferences", assignmentAttachmentReferences);

            context.put("submission", submission);
            context.put("submissionAttachmentReferences", getVisibleAttachmentIdsToReferences(submission));

            Map<String, Reference> submissionFeedbackAttachmentReferences = new HashMap<>();
            submission.getFeedbackAttachments().forEach(r -> submissionFeedbackAttachmentReferences.put(r, entityManager.newReference(r)));
            context.put("submissionFeedbackAttachmentReferences", submissionFeedbackAttachmentReferences);

            String currentUser = userDirectoryService.getCurrentUser().getId();
            context.put("currentUserId", currentUser);
            String grade = assignmentService.getGradeForSubmitter(submission, currentUser);
            context.put("grade", grade);

            Optional<AssignmentSubmissionSubmitter> submissionSubmitter = submission.getSubmitters().stream().filter(s -> s.getSubmitter().equals(currentUser)).findAny();
            if (submissionSubmitter.isPresent()) {
                context.put("submitter_time_spent", submissionSubmitter.get().getTimeSpent());
            }

            boolean isAnyRegTimeSheet = false;
            if (submissionSubmitter.isPresent() && StringUtils.isNotBlank(assignmentService.getTotalTimeSheet(submissionSubmitter.get()))) {
                context.put("timeSheetEntries", assignmentService.getTimeSheetEntries(submissionSubmitter.get()));
            	isAnyRegTimeSheet = true;
            }
            context.put("isAnyRegTimeSheet", isAnyRegTimeSheet);

            Map<String, String> submissionProperties = submission.getProperties();
            if (submissionProperties.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null) {
                context.put("prevFeedbackComment", submissionProperties.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
            }

            // can the student view model answer or not
            canViewAssignmentIntoContext(context, assignment, submission);

            // scoring agent integration
            setScoringAgentProperties(context, assignment, submission, false);

            //peer review
            if (assignment.getAllowPeerAssessment()
                    && assignment.getPeerAssessmentStudentReview()
                    && assignmentService.isPeerAssessmentClosed(assignment)) {
                List<PeerAssessmentItem> reviews = assignmentPeerAssessmentService.getPeerAssessmentItems(submission.getId(), assignment.getScaleFactor());
                if (reviews != null) {
                    List<PeerAssessmentItem> completedReviews = new ArrayList<PeerAssessmentItem>();
                    for (PeerAssessmentItem review : reviews) {
                        if (!review.getRemoved() && (review.getScore() != null || (review.getComment() != null && !"".equals(review.getComment().trim())))) {
                            //only show peer reviews that have either a score or a comment saved
                            if (assignment.getPeerAssessmentAnonEval()) {
                                //annonymous eval
                                review.setAssessorDisplayName(rb.getFormattedMessage("gen.reviewer.countReview", completedReviews.size() + 1));
                            } else {
                                //need to set the assessor's display name
                                try {
                                    if (assignment.getIsGroup()) {
                                        String siteId = toolManager.getCurrentPlacement().getContext();
                                        Site site = siteService.getSite(siteId);
                                        review.setAssessorDisplayName(site.getGroup(review.getId().getAssessorUserId()).getTitle());
                                    } else {
                                        review.setAssessorDisplayName(userDirectoryService.getUser(review.getId().getAssessorUserId()).getDisplayName());
                                    }
                                } catch (IdUnusedException | UserNotDefinedException e) {
                                    //reviewer doesn't exist or one of userId/groupId/siteId is wrong
                                    log.error(e.getMessage(), e);
                                    //set a default one:
                                    review.setAssessorDisplayName(rb.getFormattedMessage("gen.reviewer.countReview", completedReviews.size() + 1));
                                }
                            }
                            // get attachments for peer review item
                            List<PeerAssessmentAttachment> attachments = assignmentPeerAssessmentService.getPeerAssessmentAttachments(review.getId().getSubmissionId(), review.getId().getAssessorUserId());
                            if (attachments != null && !attachments.isEmpty()) {
                                List<Reference> attachmentRefList = new ArrayList<>();
                                for (PeerAssessmentAttachment attachment : attachments) {
                                    try {
                                        Reference ref = entityManager.newReference(contentHostingService.getReference(attachment.getResourceId()));
                                        attachmentRefList.add(ref);
                                    } catch (Exception e) {
                                        log.warn(e.getMessage(), e);
                                    }
                                }
                                if (!attachmentRefList.isEmpty())
                                    review.setAttachmentRefList(attachmentRefList);
                            }
                            completedReviews.add(review);

                        }
                    }
                    if (completedReviews.size() > 0) {
                        context.put("peerReviews", completedReviews);
                    }
                }
            }
            context.put("NamePropContentReviewOptoutUrl", ContentReviewConstants.URKUND_OPTOUT_URL);
            putSubmissionLogMessagesInContext(context, submission);
        }

        if (taggingManager.isTaggable() && submission != null) {
            List<DecoratedTaggingProvider> providers = addProviders(context, state);
            List<TaggingHelperInfo> itemHelpers = new ArrayList<TaggingHelperInfo>();
            for (DecoratedTaggingProvider provider : providers) {
                TaggingHelperInfo helper = provider.getProvider().getItemHelperInfo(
                        assignmentActivityProducer.getItem(
                                submission,
                                userDirectoryService.getCurrentUser().getId()).getReference());
                if (helper != null) {
                    itemHelpers.add(helper);
                }
            }
            addItem(context, submission, userDirectoryService.getCurrentUser().getId());
            addActivity(context, submission.getAssignment());
            context.put("itemHelpers", itemHelpers);
            context.put("taggable", Boolean.valueOf(true));
        }
        
        try {
            // Get site ID
            String siteId = toolManager.getCurrentPlacement().getContext();
            // Get site by ID
            Site currentSite = siteService.getSite(siteId);
            // Assignments Tool Configuration
            ToolConfiguration toolConfig = currentSite.getToolForCommonId(AssignmentConstants.TOOL_ID);
                    
            // Get visibility value of assignments tool
            String isAssignmentsVisible = toolConfig.getConfig().getProperty(ToolManager.PORTAL_VISIBLE);
            boolean isVisible = StringUtils.isBlank(isAssignmentsVisible) || StringUtils.equalsIgnoreCase(isAssignmentsVisible, Boolean.TRUE.toString());

            // Checks if the assignments tool is visible from the LHS Menu.
            context.put("isAssignmentsToolVisible", isVisible);
            
        } catch(IdUnusedException e) {
            log.error(e.getMessage(), e);
        }

        // put supplement item into context
        supplementItemIntoContext(state, context, assignment, submission);

        if (asgnAdvisor != null) {
            securityService.popAdvisor(asgnAdvisor);
        }

        context.put("submitterNames", getSubmitterFormattedNames(submission, false));
        context.put(RUBRICS_EXPORT_PDF, serverConfigurationService.getBoolean(RubricsConstants.RBCS_EXPORT_PDF, true));

        context.put("name_ASSIGNMENT_INPUT_ADD_TIME_SPENT", ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        context.put("value_ASSIGNMENT_INPUT_ADD_TIME_SPENT", state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT));
        context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_STUDENT_VIEW_GRADE;

    } // build_student_view_grade_context

    /**
     * build the view of assignments list
     */
    private String build_list_assignments_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        if (taggingManager.isTaggable()) {
            context.put("producer", assignmentActivityProducer);
            context.put("providers", taggingManager.getProviders());
            context.put("taggable", Boolean.TRUE);
        }

        Map<String, String> assignmentAlertMap = new HashMap<>();

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        context.put("contextString", contextString);
        context.put("user", state.getAttribute(STATE_USER));
        context.put("AuthzGroupService", authzGroupService);
        context.put("userDirectoryService", userDirectoryService);
        context.put("LongObject", Instant.now().toEpochMilli());
        context.put("currentTime", Instant.now());
        context.put("filterOptions", AssignmentFilter.values());
        context.put("currentFilterOption", state.getAttribute(FILTER_OPTION));
        String sortedBy = (String) state.getAttribute(SORTED_BY);
        String sortedAsc = (String) state.getAttribute(SORTED_ASC);
        // clean sort criteria
        if (SORTED_BY_GROUP_TITLE.equals(sortedBy) || SORTED_BY_GROUP_DESCRIPTION.equals(sortedBy)) {
            sortedBy = SORTED_BY_DUEDATE;
            sortedAsc = Boolean.TRUE.toString();
            state.setAttribute(SORTED_BY, sortedBy);
            state.setAttribute(SORTED_ASC, sortedAsc);
        }
        context.put("sortedBy", sortedBy);
        context.put("sortedAsc", sortedAsc);

        if (state.getAttribute(STATE_SELECTED_VIEW) != null &&
                // this is not very elegant, but the view cannot be 'lisofass2' here.
                !state.getAttribute(STATE_SELECTED_VIEW).equals(MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT)) {
            context.put("view", state.getAttribute(STATE_SELECTED_VIEW));
        }

        List<Assignment> assignments = prepPage(state);
        context.put("hasAssignments", !assignments.isEmpty());
        context.put("assignments", assignments.iterator());

        // allow add assignment?
        Map<String, List<PeerAssessmentItem>> peerAssessmentItemsMap = new HashMap<>();
        boolean allowAddAssignment = assignmentService.allowAddAssignment(contextString);
        if (!allowAddAssignment) {
            //this is the same requirement for displaying the assignment link for students
            //now lets create a map for peer reviews for each eligible assignment
            for (Assignment assignment : assignments) {
                if (assignment.getAllowPeerAssessment() && (assignmentService.isPeerAssessmentOpen(assignment) || assignmentService.isPeerAssessmentClosed(assignment))) {
                    String peerId = assignmentService.getSubmitterIdForAssignment(assignment, userDirectoryService.getCurrentUser().getId());
                    peerAssessmentItemsMap.put(assignment.getId(), assignmentPeerAssessmentService.getPeerAssessmentItems(assignment.getId(), peerId, assignment.getScaleFactor()));
                }
            }
        }
        HashMap<String, String> rate = new HashMap<String, String>();
        for (Assignment assignment : assignments) {
            if(StringUtils.isNotBlank(assignment.getEstimate()) ) {
                rate.put(formattedText.escapeHtml(assignment.getId()), getRateTimeSpent(assignment.getSubmissions()));
            }
        }
        context.put("rate", rate);
        state.setAttribute("rate", rate);

        HashMap<String, String> rateStudent = new HashMap<String, String>();
        for (Assignment assignment : assignments) {
            if(StringUtils.isNotBlank(assignment.getEstimate()) ) {
                try {
                rateStudent.put(formattedText.escapeHtml(assignment.getId()), getRateSubmissionTimeSpent(assignmentService.getSubmission(assignment.getId(), userDirectoryService.getCurrentUser())));
                } catch (PermissionException e) {
                    log.warn("Could not get submission for assignment: {}, user: {}", assignment.getId(), userDirectoryService.getCurrentUser().getId());
                }
            }
        }
        context.put("rateStudent", rateStudent);
        state.setAttribute("rateStudent", rateStudent);

        context.put("peerAssessmentItemsMap", peerAssessmentItemsMap);

        Map<String, Long> rubricMap = new HashMap<>();
        assignments.forEach(a -> {

            try {
                Optional<AssociationTransferBean> optAssociation
                    = rubricsService.getAssociationForToolAndItem(AssignmentConstants.TOOL_ID, a.getId(), a.getContext());
                if (optAssociation.isPresent()) {
                    rubricMap.put(a.getId(), optAssociation.get().getRubricId());
                }
            } catch (Exception e) {
                log.error("Failed to load rubric map", e);
            }
        });

        context.put("rubricMap", rubricMap);

        if (serverConfigurationService.getBoolean("tagservice.enable.integrations", true)) {
            Set<String> allTaggedGroupsAndInstructorsSet = new HashSet<>();
            Map<String, List<String>> tagsMap = new HashMap<>();
            assignments.forEach(a -> {
                List<String> tagLabels = tagService.getAssociatedTagsForItem(a.getContext(), a.getId()).stream().map(Tag::getTagLabel).collect(Collectors.toList());
                List<String> instructorAndGroupTags = addInstructorAndGroupTags(a);
                tagLabels.addAll(instructorAndGroupTags);
                allTaggedGroupsAndInstructorsSet.addAll(instructorAndGroupTags);
                tagsMap.put(a.getId(), tagLabels);
            });
            context.put("tagsEnabled", Boolean.TRUE);
            context.put("tagTool", TagService.TOOL_ASSIGNMENTS);
            context.put("allowAddTags", assignmentService.allowAddTags(contextString));
            context.put("tagsMap", tagsMap);
            context.put("siteId", contextString);
            List<String> allTaggedGroupsAndInstructors = new ArrayList<>(allTaggedGroupsAndInstructorsSet);
            Collections.sort(allTaggedGroupsAndInstructors, String.CASE_INSENSITIVE_ORDER);
            context.put("allTaggedGroupsAndInstructors", String.join(",", allTaggedGroupsAndInstructors));
            context.put(SHOW_TAGS_STUDENT, state.getAttribute(SHOW_TAGS_STUDENT));
            context.put(TAG_SELECTOR, state.getAttribute(TAG_SELECTOR));
        }

        // allow get assignment
        context.put("allowGetAssignment", assignmentService.allowGetAssignment(contextString));

        // test whether user user can grade at least one assignment
        // and update the state variable.
        boolean allowGradeSubmission = assignments.stream().anyMatch(a -> assignmentService.allowGradeSubmission(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference()));

        state.setAttribute(STATE_ALLOW_GRADE_SUBMISSION, allowGradeSubmission);
        context.put("allowGradeSubmission", state.getAttribute(STATE_ALLOW_GRADE_SUBMISSION));

        // allow remove assignment?
        boolean allowRemoveAssignment = assignments.stream().anyMatch(a -> assignmentService.allowRemoveAssignment(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference()));
        context.put("allowRemoveAssignment", allowRemoveAssignment);

        add2ndToolbarFields(data, context);

        pagingInfoToContext(state, context);

        // put site object into context
        try {
            // get current site
            Site site = siteService.getSite(contextString);
            context.put("site", site);
            // any group in the site?
            Collection groups = getAllGroupsInSite(contextString);

            context.put("groups", (groups != null && groups.size() > 0) ? Boolean.TRUE : Boolean.FALSE);

            boolean groupFilterEnabled = serverConfigurationService.getBoolean(PROP_ASSIGNMENT_GROUP_FILTER_ENABLED, false);
            context.put("groupFilterEnabled", groupFilterEnabled);
            if(groupFilterEnabled){
                User user = (User) state.getAttribute(STATE_USER);
                Collection<Group> groupCollection = assignmentService.getGroupsAllowAddAssignment(contextString);
                //If the user doesn't have any group, show the groups where is member (Mostly students)
                if(groupCollection.isEmpty()){
                    groupCollection = site.getGroupsWithMember(user.getId());
                }
                context.put("filterGroupIterator", new SortedIterator(groupCollection.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString())));
            }

            // add active user list
            AuthzGroup realm = authzGroupService.getAuthzGroup(siteService.siteReference(contextString));
            if (realm != null) {
                context.put("activeUserIds", realm.getUsers());
            }

            // for sorting assignment groups using the standard comparator
            Map<String, List<String>> groupTitleMap = new HashMap<>(assignments.size());
            AssignmentComparator groupComparator = new AssignmentComparator(null, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString());
            for (Assignment asn : assignments) {
                groupTitleMap.put(asn.getId(), getSortedAsnGroupTitles(asn, site, groupComparator));
            }
            context.put("asnGroupTitleMap", groupTitleMap);

            for (Assignment assignment : assignments) {
                Integer contentKey = assignment.getContentId();
                if (contentKey != null && !isContentToolIdValid(contentKey, site)) {
                    assignmentAlertMap.put(assignment.getId(), rb.getString("external.tool.deleted"));
                    continue;
                }
            }

        } catch (Exception ignore) {
            log.warn(this + ":build_list_assignments_context " + ignore.getMessage());
            log.warn(this + ignore.getMessage() + " siteId= " + contextString);
        }

        boolean allowSubmit = assignmentService.allowAddSubmission(contextString);
        context.put("allowSubmit", allowSubmit);

        // related to resubmit settings
        context.put("allowResubmitNumberProp", AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
        context.put("allowResubmitCloseTimeProp", AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);

        // the type int for non-electronic submission
        context.put("typeNonElectronic", Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION.ordinal());

        // show or hide the number of submission column
        context.put(SHOW_NUMBER_SUBMISSION_COLUMN, state.getAttribute(SHOW_NUMBER_SUBMISSION_COLUMN));

        // clear out peer_attachment list just in case
        state.setAttribute(PEER_ATTACHMENTS, entityManager.newReferenceList());
        context.put(PEER_ATTACHMENTS, entityManager.newReferenceList());

        // set time sheet visibility
        context.put("isTimesheet", assignmentService.isTimeSheetEnabled(contextString));

        context.put("assignmentAlertMap", assignmentAlertMap);

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_LIST_ASSIGNMENTS;

    } // build_list_assignments_context

    private List<String> addInstructorAndGroupTags(Assignment a) {
        List<String> tagLabels = new ArrayList<>();
        if (a.getProperties().get(NEW_ASSIGNMENT_TAG_CREATOR) != null && Boolean.parseBoolean(a.getProperties().get(NEW_ASSIGNMENT_TAG_CREATOR))) {
            try {
                String author = userDirectoryService.getUser(a.getAuthor()).getDisplayName();
                tagLabels.add(author);
            } catch (Exception e) {
                log.warn(this + ":build_list_assignments_context cannot get user " + e.getMessage() + " user id=" + a.getAuthor());
            }
            if (!a.getAuthor().equals(a.getModifier())){
                try {
                    String modifier = userDirectoryService.getUser(a.getModifier()).getDisplayName();
                    tagLabels.add(modifier);
                } catch (Exception e) {
                    log.warn(this + ":build_list_assignments_context cannot get user " + e.getMessage() + " user id=" + a.getModifier());
                }
            }
        }
        if (a.getProperties().get(NEW_ASSIGNMENT_TAG_GROUPS) != null && Boolean.parseBoolean(a.getProperties().get(NEW_ASSIGNMENT_TAG_GROUPS))) {
            try {
                Site site = siteService.getSite(a.getContext());
                List<Group> asnGroups = a.getGroups().stream().map(id -> site.getGroup(id)).filter(Objects::nonNull).collect(Collectors.toList());
                List<String> groupTitles = asnGroups.stream().map(Group::getTitle).collect(Collectors.toList());
                tagLabels.addAll(groupTitles);
            } catch (Exception e) {
                log.warn(this + ":build_list_assignments_context cannot get site " + e.getMessage() + " site id=" + a.getContext());
            }
        }
        return tagLabels;
    }

    private List<String> getSortedAsnGroupTitles(Assignment asn, Site site, AssignmentComparator groupComparator) {
        List<Group> asnGroups = asn.getGroups().stream().map(id -> site.getGroup(id)).filter(Objects::nonNull).collect(Collectors.toList());
        asnGroups.sort(groupComparator);
        return asnGroups.stream().map(Group::getTitle).collect(Collectors.toList());
    }

    private Set<String> getSubmittersIdSet(List<AssignmentSubmission> submissions) {
        return submissions.stream().map(AssignmentSubmission::getSubmitters).flatMap(Set::stream).map(AssignmentSubmissionSubmitter::getSubmitter).collect(Collectors.toSet());
    }

    public boolean isContentToolIdValid(Integer contentKey, Site site) {
        if ( contentKey == null ) return true;
        Long contentLong = contentKey.longValue();
        Map<String, Object> content = ltiService.getContent(contentLong, site.getId());
        if ( content == null ) return false;

        Long toolKey = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
        if ( toolKey == null ) return false;

        Map<String, Object> tool = ltiService.getTool(toolKey, site.getId());
        if ( tool == null ) return false;

        return true;
    }

    /**
     * build the view of assignments list
     */
    private String build_list_deleted_assignments_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        // cleaning from view attribute
        state.removeAttribute(FROM_VIEW);

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        String sortedBy = (String) state.getAttribute(SORTED_BY);
        String sortedAsc = (String) state.getAttribute(SORTED_ASC);
        context.put("sortedBy", sortedBy);
        context.put("sortedAsc", sortedAsc);

        List<Assignment> assignments = prepPage(state);
        context.put("assignments", assignments.iterator());

        add2ndToolbarFields(data, context);

        pagingInfoToContext(state, context);

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_LIST_DELETED_ASSIGNMENTS;
    } // build_list_deleted_assignments_context

    /**
     * build the instructor view of creating a new assignment or editing an existing one
     */
    protected String build_instructor_new_edit_assignment_context(VelocityPortlet portlet, Context context, RunData data,
                                                                  SessionState state) {
        // If the user adds the schedule or alternate calendar tool after using the assignment tool,
        // we need to remove these state attributes so they are re-initialized with the updated
        // availability of the tools.
        state.removeAttribute(CALENDAR_TOOL_EXIST);
        state.removeAttribute(ADDITIONAL_CALENDAR_TOOL_READY);
        initState(state, portlet, (JetspeedRunData) data);

        // Anon grading enabled/disabled
        context.put("enableAnonGrading", serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_ENABLE_ANON_GRADING, false));
        boolean forceAnonGrading = false;
        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
        try {
            Site site = siteService.getSite(siteId);
            forceAnonGrading = site.getProperties().getBooleanProperty(AssignmentConstants.SAK_PROP_FORCE_ANON_GRADING);
        } catch (EntityPropertyTypeException | EntityPropertyNotDefinedException | SakaiException se) {
            log.debug("Failed to find if anonymous grading is forced.");
        }
        context.put("forceAnonGrading", forceAnonGrading);
        context.put("siteId", siteId);

        // is the assignment an new assignment
        String assignmentId = (String) state.getAttribute(EDIT_ASSIGNMENT_ID);
        if (assignmentId != null) {
            Assignment a = getAssignment(assignmentId, "build_instructor_new_edit_assignment_context", state);
            if (a != null) {
                context.put("assignmentId", assignmentId);
                context.put("assignment", a);
                if (state.getAttribute(RUBRIC_ASSOCIATION) == null) {
                    try {
                        Optional<AssociationTransferBean> optAssociation
                            = rubricsService.getAssociationForToolAndItem(AssignmentConstants.TOOL_ID, a.getId(), siteId);
                        if (optAssociation.isPresent()) {
                            state.setAttribute(RUBRIC_ASSOCIATION, (new ObjectMapper()).writeValueAsString(optAssociation.get()));
                        }
                    } catch (Exception e) {
                        log.warn("Failed to get rubric association for assignment {}", assignmentId);
                    }
                }

                rangeAndGroups.buildInstructorNewEditAssignmentContextGroupCheck(context, a);

                // controls to disable which groups can be changed
                Collection<Group> groupsWithUserSubmission = new ArrayList<>();
                if (a.getIsGroup()) {
                    try {
                        Site site = siteService.getSite(a.getContext());
                        Collection<Group> siteGroups = site.getGroups();

                        a.getSubmissions().stream()
                                .filter(AssignmentSubmission::getUserSubmission)
                                .forEach(s -> siteGroups.stream()
                                        .filter(g -> g.getId().equals(s.getGroupId()))
                                        .findAny().ifPresent(groupsWithUserSubmission::add));
                    } catch (IdUnusedException e) {
                        log.warn("Site not found for context {}, {}", a.getContext(), e.getMessage());
                    }
                }
                context.put("groupsWithUserSubmission", groupsWithUserSubmission);
            }
        } else {
            boolean instructorGroupsProp = serverConfigurationService.getBoolean("assignments.assign.to.instructor.groups", false);
            Collection<Group> instructorGroups = new ArrayList<Group>();
            if (instructorGroupsProp) {
                try {
                    Site site = siteService.getSite(siteId);
                    User user = (User) state.getAttribute(STATE_USER);
                    instructorGroups = site.getGroupsWithMember(user.getId());
                    
                } catch (IdUnusedException ex){
                    log.warn("Site not found for site id:{}", siteId);
                }
            }
            context.put("instructorGroups", instructorGroups);
        }

        // set up context variables
        setAssignmentFormContext(state, context);

        context.put("fField", state.getAttribute(NEW_ASSIGNMENT_FOCUS));

        context.put("group_submissions_enabled", serverConfigurationService.getBoolean("assignment.group.submission.enabled", true));
        context.put("visible_date_enabled", serverConfigurationService.getBoolean("assignment.visible.date.enabled", false));

        String sortedBy = (String) state.getAttribute(SORTED_BY);
        String sortedAsc = (String) state.getAttribute(SORTED_ASC);
        context.put("sortedBy", sortedBy);
        context.put("sortedAsc", sortedAsc);

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT;
    } // build_instructor_new_assignment_context

    protected void setAssignmentFormContext(SessionState state, Context context) {
        // put the names and values into vm file
        context.put("name_UsePeerAssessment", NEW_ASSIGNMENT_USE_PEER_ASSESSMENT);
        context.put("name_PeerAssessmentAnonEval", NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL);
        context.put("name_PeerAssessmentStudentViewReviews", NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS);
        context.put("name_PeerAssessmentNumReviews", NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS);
        context.put("name_PeerAssessmentInstructions", NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS);
        context.put("name_UseReviewService", AssignmentConstants.NEW_ASSIGNMENT_USE_REVIEW_SERVICE);
        context.put("name_AllowStudentView", AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY_AND_DUE", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY_AND_DUE);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE);
        context.put("name_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE", AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE);

        context.put("name_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE", ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE);
        context.put("name_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED", ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED);
        context.put("name_NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE", ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE);

        context.put("name_title", NEW_ASSIGNMENT_TITLE);
        context.put("name_order", NEW_ASSIGNMENT_ORDER);

        // set open time context variables
        putTimePropertiesInContext(context, state, "Open", NEW_ASSIGNMENT_OPEN_MONTH, NEW_ASSIGNMENT_OPEN_DAY, NEW_ASSIGNMENT_OPEN_YEAR, NEW_ASSIGNMENT_OPEN_HOUR, NEW_ASSIGNMENT_OPEN_MIN);

        // set visible time context variables
        if (serverConfigurationService.getBoolean("assignment.visible.date.enabled", false)) {
            putTimePropertiesInContext(context, state, "Visible", NEW_ASSIGNMENT_VISIBLE_MONTH, NEW_ASSIGNMENT_VISIBLE_DAY, NEW_ASSIGNMENT_VISIBLE_YEAR, NEW_ASSIGNMENT_VISIBLE_HOUR, NEW_ASSIGNMENT_VISIBLE_MIN);
            context.put(NEW_ASSIGNMENT_VISIBLETOGGLE, ((Boolean) state.getAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE)));
        }

        // set due time context variables
        putTimePropertiesInContext(context, state, "Due", NEW_ASSIGNMENT_DUE_MONTH, NEW_ASSIGNMENT_DUE_DAY, NEW_ASSIGNMENT_DUE_YEAR, NEW_ASSIGNMENT_DUE_HOUR, NEW_ASSIGNMENT_DUE_MIN);

        context.put("name_EnableCloseDate", NEW_ASSIGNMENT_ENABLECLOSEDATE);
        // set close time context variables
        putTimePropertiesInContext(context, state, "Close", NEW_ASSIGNMENT_CLOSE_MONTH, NEW_ASSIGNMENT_CLOSE_DAY, NEW_ASSIGNMENT_CLOSE_YEAR, NEW_ASSIGNMENT_CLOSE_HOUR, NEW_ASSIGNMENT_CLOSE_MIN);

        context.put("name_Section", NEW_ASSIGNMENT_SECTION);
        context.put("name_SubmissionType", NEW_ASSIGNMENT_SUBMISSION_TYPE);
        context.put("name_ContentId", NEW_ASSIGNMENT_CONTENT_ID);
        context.put("name_ContentTitle", NEW_ASSIGNMENT_CONTENT_TITLE);
        context.put("name_ContentLaunchNewWindow", NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW);
        context.put("name_ContentToolNewpage", NEW_ASSIGNMENT_CONTENT_TOOL_NEWPAGE);
        context.put("name_Category", NEW_ASSIGNMENT_CATEGORY);
        context.put("name_GradeAssignment", NEW_ASSIGNMENT_GRADE_ASSIGNMENT);
        context.put("name_GradeType", NEW_ASSIGNMENT_GRADE_TYPE);
        context.put("name_GradePoints", NEW_ASSIGNMENT_GRADE_POINTS);
        context.put("name_Description", NEW_ASSIGNMENT_DESCRIPTION);
        // do not show the choice when there is no Schedule tool yet
        if (state.getAttribute(CALENDAR) != null || state.getAttribute(ADDITIONAL_CALENDAR) != null)
            context.put("name_CheckAddDueDate", ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);

        context.put("name_CheckHideDueDate", NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE);
        //don't show the choice when there is no Announcement tool yet
        if (state.getAttribute(ANNOUNCEMENT_CHANNEL) != null) {
            context.put("name_CheckAutoAnnounce", ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
            context.put("name_OpenDateNotification", AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION);
        }
        context.put("name_CheckAddHonorPledge", NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);

        context.put("allowAutoSubmit", serverConfigurationService.getBoolean("assignment.autoSubmit.enabled", false));
        if ((Boolean) serverConfigurationService.getBoolean("assignment.autoSubmit.enabled", false)) {
            context.put("name_CheckAutoSubmit", NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT);
        }

        context.put("name_CheckAddInstructorTags", NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS);
        context.put("name_CheckAddGroupTags", NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS);

        // SAK-17606
        context.put("name_CheckAnonymousGrading", NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING);

        // set the values
        Assignment a = null;
        String assignmentRef = (String) state.getAttribute(EDIT_ASSIGNMENT_ID);
        if (assignmentRef != null) {
            a = getAssignment(assignmentRef, "setAssignmentFormContext", state);
        }

        // put the re-submission info into context
        putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSE_MONTH, ALLOW_RESUBMIT_CLOSE_DAY, ALLOW_RESUBMIT_CLOSE_YEAR, ALLOW_RESUBMIT_CLOSE_HOUR, ALLOW_RESUBMIT_CLOSE_MIN);

        context.put("value_year_from", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM));
        context.put("value_year_to", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO));
        context.put("value_title", state.getAttribute(NEW_ASSIGNMENT_TITLE));
        context.put("value_position_order", state.getAttribute(NEW_ASSIGNMENT_ORDER));

        context.put("value_EnableCloseDate", state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE));

        context.put("value_Sections", state.getAttribute(NEW_ASSIGNMENT_SECTION));
        context.put("value_SubmissionType", state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE));
        context.put("value_ContentId", state.getAttribute(NEW_ASSIGNMENT_CONTENT_ID));
        context.put("value_ContentTitle", state.getAttribute(NEW_ASSIGNMENT_CONTENT_TITLE));
        context.put("value_ContentLaunchNewWindow", state.getAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW));
        context.put("value_ContentToolNewpage", state.getAttribute(NEW_ASSIGNMENT_CONTENT_TOOL_NEWPAGE));
        context.put("value_ContentDeleted", state.getAttribute(NEW_ASSIGNMENT_CONTENT_DELETED));

        // information related to gradebook categories
        putGradebookCategoryInfoIntoContext(state, context);

        context.put("value_totalSubmissionTypes", Assignment.SubmissionType.values().length - 1);

        Integer scaleFactor;
        Boolean anonGrading;
        // Preserve the user's selection from state if present (e.g., when the form
        // is re-rendered after validation alerts like past-due warning). Otherwise
        // fall back to the assignment's stored value (when editing) or default.
        Boolean anonFromState = (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING);
        if (a != null) {
            scaleFactor = a.getScaleFactor() != null ? a.getScaleFactor() : assignmentService.getScaleFactor();
            anonGrading = anonFromState != null ? anonFromState : assignmentService.assignmentUsesAnonymousGrading(a);
        } else {
            scaleFactor = assignmentService.getScaleFactor();
            anonGrading = anonFromState;
        }

        Assignment.GradeType gradeType = Assignment.GradeType.values()[(Integer) state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE)];
        String maxGrade = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
        context.put("value_GradeType", gradeType.ordinal());
        context.put("value_GradePoints", assignmentService.getGradeDisplay(maxGrade, gradeType, scaleFactor));
        context.put("value_Description", state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION));
        context.put("value_CheckAnonymousGrading", anonGrading);

        //Peer Assessment
        String peer = (String) state.getAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT);
        context.put("value_UsePeerAssessment", peer);
        context.put("value_PeerAssessmentAnonEval", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL));
        context.put("value_PeerAssessmentStudentViewReviews", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS));
        context.put("value_PeerAssessmentNumReviews", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS));
        context.put("value_PeerAssessmentInstructions", state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS));
        putTimePropertiesInContext(context, state, "PeerPeriod", NEW_ASSIGNMENT_PEERPERIOD_MONTH, NEW_ASSIGNMENT_PEERPERIOD_DAY, NEW_ASSIGNMENT_PEERPERIOD_YEAR, NEW_ASSIGNMENT_PEERPERIOD_HOUR, NEW_ASSIGNMENT_PEERPERIOD_MIN);

        // Keep the use review service setting
        context.put("value_UseReviewService", state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_USE_REVIEW_SERVICE));
        if (!contentReviewService.allowAllContent()) {
            String fileTypesMessage = getContentReviewAcceptedFileTypesMessage();
            String reviewServiceName = contentReviewService.getServiceName();
            Object[] params = new Object[3];
            params[0] = fileTypesMessage;
            params[1] = "";
            params[2] = "";
            switch(reviewServiceName) {
                case CONTENT_PROVIDER_URKUND:
                    long maxFileSizeBytes = Long.parseLong(serverConfigurationService.getString("urkund.maxFileSize", "20971520"));
                    params[1] = rb.getFormattedMessage("gen.onlythefoll." + reviewServiceName.toLowerCase(), new Object[]{maxFileSizeBytes/MEGABYTE});
                    break;
                case CONTENT_PROVIDER_TURNITIN:
                    params[2] = rb.getString("content_review.note." + reviewServiceName.toLowerCase());
                    break;
                default:
            }
            String contentReviewNote = rb.getFormattedMessage("content_review.note", params);
            context.put("content_review_note", contentReviewNote);
        }
        context.put("turnitin_forceSingleAttachment", serverConfigurationService.getBoolean("turnitin.forceSingleAttachment", false));
        //Rely on the deprecated "turnitin.allowStudentView.default" setting if set, otherwise use "contentreview.allowStudentView.default"
        boolean defaultAllowStudentView = serverConfigurationService.getBoolean("turnitin.allowStudentView.default", serverConfigurationService.getBoolean("contentreview.allowStudentView.default", Boolean.FALSE));
        context.put("value_AllowStudentView", state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW) == null ? Boolean.toString(defaultAllowStudentView) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW));

        List<String> subOptions = getSubmissionRepositoryOptions();
        String submitRadio = serverConfigurationService.getString("turnitin.repository.setting.value", null) == null ? AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE : serverConfigurationService.getString("turnitin.repository.setting.value");
        if (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO) != null && subOptions.contains(state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO)))
            submitRadio = state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO).toString();
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO", submitRadio);
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT", subOptions);

        List<String> reportGenOptions = getReportGenOptions();
        String reportRadio = serverConfigurationService.getString("turnitin.report_gen_speed.setting.value", null) == null ? reportGenOptions.get(0) : serverConfigurationService.getString("turnitin.report_gen_speed.setting.value");
        if (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO) != null && reportGenOptions.contains(state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO)))
            reportRadio = state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO).toString();
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO", reportRadio);
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT", reportGenOptions);

        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN", serverConfigurationService.getBoolean("turnitin.option.s_paper_check", true));
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET", serverConfigurationService.getBoolean("turnitin.option.internet_check", true));
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB", serverConfigurationService.getBoolean("turnitin.option.journal_check", true));
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", serverConfigurationService.getBoolean("turnitin.option.institution_check", false));

        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN) == null) ? Boolean.toString(serverConfigurationService.getBoolean("turnitin.option.s_paper_check.default", serverConfigurationService.getBoolean("turnitin.option.s_paper_check", true) ? true : false)) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN));
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET", state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET) == null ? Boolean.toString(serverConfigurationService.getBoolean("turnitin.option.internet_check.default", serverConfigurationService.getBoolean("turnitin.option.internet_check", true) ? true : false)) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET));
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB", state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB) == null ? Boolean.toString(serverConfigurationService.getBoolean("turnitin.option.journal_check.default", serverConfigurationService.getBoolean("turnitin.option.journal_check", true) ? true : false)) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB));
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION", state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION) == null ? Boolean.toString(serverConfigurationService.getBoolean("turnitin.option.institution_check.default", serverConfigurationService.getBoolean("turnitin.option.institution_check", true) ? true : false)) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION));

        //exclude bibliographic materials
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC", serverConfigurationService.getBoolean("turnitin.option.exclude_bibliographic", true));
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC) == null) ? Boolean.toString(serverConfigurationService.getBoolean("turnitin.option.exclude_bibliographic.default", serverConfigurationService.getBoolean("turnitin.option.exclude_bibliographic", true) ? true : false)) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC));

        //exclude quoted materials
        //Rely on the deprecated "turnitin.option.exclude_quoted" setting if set, otherwise use "contentreview.option.exclude_quoted"
        boolean showExcludeQuoted = serverConfigurationService.getBoolean("turnitin.option.exclude_quoted", serverConfigurationService.getBoolean("contentreview.option.exclude_quoted", Boolean.TRUE));
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED", showExcludeQuoted);
        //Rely on the deprecated "turnitin.option.exclude_quoted.default" setting if set, otherwise use "contentreview.option.exclude_quoted.default"
        boolean defaultExcludeQuoted = serverConfigurationService.getBoolean("turnitin.option.exclude_quoted.default", serverConfigurationService.getBoolean("contentreview.option.exclude_quoted.default", showExcludeQuoted));
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED) == null) ? Boolean.toString(defaultExcludeQuoted) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED));

        //exclude self plag
        boolean showExcludeSelfPlag = serverConfigurationService.getBoolean("contentreview.option.exclude_self_plag", Boolean.TRUE);
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG", showExcludeSelfPlag);
        //Rely on the deprecated "turnitin.option.exclude_self_plag.default" setting if set, otherwise use "contentreview.option.exclude_self_plag.default"
        boolean defaultExcludeSelfPlag = serverConfigurationService.getBoolean("contentreview.option.exclude_self_plag.default", showExcludeSelfPlag);
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG) == null) ? Boolean.toString(defaultExcludeSelfPlag) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG));

        //Store Inst Index
        boolean showStoreInstIndex = serverConfigurationService.getBoolean("contentreview.option.store_inst_index", Boolean.TRUE);
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX", showStoreInstIndex);
        //Rely on the deprecated "turnitin.option.store_inst_index.default" setting if set, otherwise use "contentreview.option.store_inst_index.default"
        boolean defaultStoreInstIndex = serverConfigurationService.getBoolean("contentreview.option.store_inst_index.default", showStoreInstIndex);
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX) == null) ? Boolean.toString(defaultStoreInstIndex) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX));
        //Use Student Preview
        boolean showStudentPreview = serverConfigurationService.getBoolean("contentreview.option.student_preview", Boolean.FALSE);
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW", showStudentPreview);
        boolean defaultStudentPreview = serverConfigurationService.getBoolean("contentreview.option.student_preview.default", Boolean.FALSE);
        context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW) == null) ? Boolean.toString(defaultStudentPreview) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW));
        //exclude small matches
        boolean displayExcludeType = serverConfigurationService.getBoolean("turnitin.option.exclude_smallmatches", true);
        context.put("show_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES", displayExcludeType);
        if (displayExcludeType) {
            context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE) == null) ? Integer.toString(serverConfigurationService.getInt("turnitin.option.exclude_type.default", 0)) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE));
            context.put("value_NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE", (state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE) == null) ? Integer.toString(serverConfigurationService.getInt("turnitin.option.exclude_value.default", 1)) : state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE));
        }

        // don't show the choice when there is no Schedule tool yet
        if (state.getAttribute(CALENDAR) != null || state.getAttribute(ADDITIONAL_CALENDAR) != null)
            context.put("value_CheckAddDueDate", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));

        context.put("value_CheckHideDueDate", state.getAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE));

        context.put("value_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE", (Boolean) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE));
        context.put("value_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED", (Boolean) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED));
        context.put("value_NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE));

        // don't show the choice when there is no Announcement tool yet
        if (state.getAttribute(ANNOUNCEMENT_CHANNEL) != null) {
            context.put("value_CheckAutoAnnounce", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
            context.put("value_OpenDateNotification", state.getAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION));
            // the option values
            context.put("value_opendate_notification_none", AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
            context.put("value_opendate_notification_low", AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
            context.put("value_opendate_notification_high", AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
        }
        context.put("name_CheckEmailReminder",NEW_ASSIGNMENT_REMINDER_EMAIL);
        context.put("value_CheckEmailReminder",state.getAttribute(NEW_ASSIGNMENT_REMINDER_EMAIL));
        context.put("value_reminder_hours", serverConfigurationService.getInt("assignment.reminder.hours", 24));

        context.put("value_CheckAddHonorPledge", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE));

        if ((Boolean) serverConfigurationService.getBoolean("assignment.autoSubmit.enabled", false)) {
            context.put("value_CheckAutoSubmit", state.getAttribute(NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT));
        }

        context.put("value_CheckAddInstructorTags", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS));
        context.put("value_CheckAddGroupTags", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS));

        // put resubmission option into context
        assignment_resubmission_option_into_context(context, state);
        assignment_extension_option_into_context(context, state);

        // get all available assignments from Gradebook tool except for those created fromcategoryTable
        if (canGrade()) {
            String gradebookUid = toolManager.getCurrentPlacement().getContext();

            // how many gradebook assignment have been integrated with Assignment tool already
            currentAssignmentGradebookIntegrationIntoContext(context, state, a);

            if (StringUtils.isBlank((String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK))) {
                state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
            }

            context.put("name_SendToGradebook", NEW_ASSIGNMENT_SEND_TO_GRADEBOOK);
            context.put("name_Addtogradebook", NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
            context.put("name_AssociateGradebookAssignment", PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

            String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);

            if (gradingService.isGradebookGroupEnabled(siteId)) {
                context.put("gb_selector", GB_SELECTOR);
            }

            if (state.getAttribute(EDIT_ASSIGNMENT_ID) == null) {
                // This is a new assignment. Pick add new item to gradebook radio option.
                state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ADD);
            }
            context.put("gradebookChoice", state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
            context.put("gradebookChoice_no", GRADEBOOK_INTEGRATION_NO);
            context.put("gradebookChoice_add", GRADEBOOK_INTEGRATION_ADD);
            context.put("gradebookChoice_associate", GRADEBOOK_INTEGRATION_ASSOCIATE);
            String associateGradebookAssignment = (String) state.getAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

            if (StringUtils.isNotBlank(associateGradebookAssignment)) {
                context.put("associateGradebookAssignment", associateGradebookAssignment);
                if (a != null) {
                    // the premise here is that if there is a VALID value for PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT
                    // then the assignment is already associated to the gradebook and the user should not see the add to gradebook option
                    boolean valid = false;
                    if (StringUtils.startsWith(associateGradebookAssignment, REFERENCE_ROOT)) {
                        // checking externally maintained by gradebook
                        if (AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference().equals(associateGradebookAssignment)) {
                            // the reference matches that of the assignment
                            valid = true;
                        } else {
                            // why do we have a reference to some other assignment?
                            log.warn("Gradebook association for assignment {} was pointing to assignment {}", a.getId(), associateGradebookAssignment);
                        }
                    } else {
                        // check internally maintained by gradebook
                        try {
                            if (gradingService.getAssignmentByNameOrId(gradebookUid, gradebookUid, associateGradebookAssignment) != null) {
                                valid = true;
                            }
                        } catch (AssessmentNotFoundException anfe) {
                            log.debug("Gradebook item not found for assignment {} with {} = {}", a.getId(), PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, associateGradebookAssignment);
                        }
                    }
                    context.put("noAddToGradebookChoice", valid);
                }
            }

            if (StringUtils.isBlank((String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK))) {
                state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
            }
        }

        context.put("monthTable", monthTable());
        context.put("submissionTypeTable", submissionTypeTable());
        context.put("attachments", state.getAttribute(ATTACHMENTS));
        context.put("contentTypeImageService", contentTypeImageService);

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        // put site object into context
        try {
            // get current site
            Site site = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
            context.put("site", site);
        } catch (Exception ignore) {
            log.warn(this + ":setAssignmentFormContext " + ignore.getMessage());
        }

        rangeAndGroups.setAssignmentFormContext(state, context, contextString, this);

        context.put("isTimesheet", assignmentService.isTimeSheetEnabled(contextString));

        // the notification email choices
        // whether the choice of emails instructor submission notification is available in the installation
        // system installation allowed assignment submission notification
        boolean allowNotification = serverConfigurationService.getBoolean(ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS, true);
        if (allowNotification) {
            // whether current user can receive notification. If not, don't show the notification choices in the create/edit assignment page
            allowNotification = assignmentService.allowReceiveSubmissionNotification(contextString);
        }
        if (allowNotification) {
            context.put("name_assignment_instructor_notifications", ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS);
            if (state.getAttribute(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE) == null) {
                // set the notification value using site default
                // whether or how the instructor receive submission notification emails, none(default)|each|digest
                state.setAttribute(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, serverConfigurationService.getString(ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DEFAULT, AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE));
            }
            context.put("value_assignment_instructor_notifications", state.getAttribute(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE));
            // the option values
            context.put("value_assignment_instructor_notifications_none", AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE);
            context.put("value_assignment_instructor_notifications_each", AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH);
            context.put("value_assignment_instructor_notifications_digest", AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST);
        }

        // release grade notification option
        putReleaseGradeNotificationOptionIntoContext(state, context);

        // release grade notification option
        putReleaseResubmissionNotificationOptionIntoContext(state, context, a);

        context.put(RUBRIC_ASSOCIATION, state.getAttribute(RUBRIC_ASSOCIATION));

        // the supplement information
        // model answers
        context.put("modelanswer", state.getAttribute(MODELANSWER) != null ? Boolean.TRUE : Boolean.FALSE);
        context.put("modelanswer_text", state.getAttribute(MODELANSWER_TEXT));
        context.put("modelanswer_showto", state.getAttribute(MODELANSWER_SHOWTO));
        // get attachment for model answer object
        putSupplementItemAttachmentStateIntoContext(state, context, MODELANSWER_ATTACHMENTS);
        // private notes
        context.put("allowReadAssignmentNoteItem", assignmentSupplementItemService.canReadNoteItem(a, contextString));
        context.put("allowEditAssignmentNoteItem", assignmentSupplementItemService.canEditNoteItem(a));
        context.put("note", state.getAttribute(NOTE) != null ? Boolean.TRUE : Boolean.FALSE);
        context.put("note_text", state.getAttribute(NOTE_TEXT));
        context.put("note_to", state.getAttribute(NOTE_SHAREWITH) != null ? state.getAttribute(NOTE_SHAREWITH) : String.valueOf(0));
        // all purpose item
        context.put("allPurpose", state.getAttribute(ALLPURPOSE) != null ? Boolean.TRUE : Boolean.FALSE);
        context.put("value_allPurposeTitle", state.getAttribute(ALLPURPOSE_TITLE));
        context.put("value_allPurposeText", state.getAttribute(ALLPURPOSE_TEXT));
        context.put("value_allPurposeHide", state.getAttribute(ALLPURPOSE_HIDE) != null ? state.getAttribute(ALLPURPOSE_HIDE) : Boolean.FALSE);
        context.put("value_allPurposeShowFrom", state.getAttribute(ALLPURPOSE_SHOW_FROM) != null ? state.getAttribute(ALLPURPOSE_SHOW_FROM) : Boolean.FALSE);
        context.put("value_allPurposeShowTo", state.getAttribute(ALLPURPOSE_SHOW_TO) != null ? state.getAttribute(ALLPURPOSE_SHOW_TO) : Boolean.FALSE);
        context.put("value_allPurposeAccessList", state.getAttribute(ALLPURPOSE_ACCESS));
        putTimePropertiesInContext(context, state, "allPurposeRelease", ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN);
        putTimePropertiesInContext(context, state, "allPurposeRetract", ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN);
        // get attachment for all purpose object
        putSupplementItemAttachmentStateIntoContext(state, context, ALLPURPOSE_ATTACHMENTS);

        if (serverConfigurationService.getBoolean("tagservice.enable.integrations", true)) {
            context.put("tagsEnabled", Boolean.TRUE);
            context.put("tagTool", TagService.TOOL_ASSIGNMENTS);
            context.put("allowAddTags", assignmentService.allowAddTags(contextString));
        }

        // put role information into context
        HashMap<String, List> roleUsers = new HashMap<String, List>();
        try {
            AuthzGroup realm = authzGroupService.getAuthzGroup(siteService.siteReference(contextString));
            Set<Role> roles = realm.getRoles();
            for (Iterator iRoles = roles.iterator(); iRoles.hasNext(); ) {
                Role r = (Role) iRoles.next();
                Set<String> users = realm.getUsersHasRole(r.getId());
                if (users != null && users.size() > 0) {
                    List<User> usersList = new ArrayList<>();
                    for (Iterator<String> iUsers = users.iterator(); iUsers.hasNext(); ) {
                        String userId = iUsers.next();
                        try {
                            User u = userDirectoryService.getUser(userId);
                            usersList.add(u);
                        } catch (Exception e) {
                            log.warn(this + ":setAssignmentFormContext cannot get user " + e.getMessage() + " user id=" + userId);
                        }
                    }
                    Collections.sort(usersList, new UserSortNameComparator());
                    roleUsers.put(r.getId(), usersList);
                }
            }
            context.put("roleUsers", roleUsers);
        } catch (Exception e) {
            log.warn(this + ":setAssignmentFormContext role cast problem " + e.getMessage() + " site =" + contextString);
        }

        Placement placement = toolManager.getCurrentPlacement();
        // String contentReturn = SakaiLTIUtil.getOurServerUrl() + "/portal/tool/" + placement.getId() +
        String contentReturn = serverConfigurationService.getToolUrl() + "/" + placement.getId()
                + "/sakai.lti.admin.helper.helper"
                + "?panel=AssignmentsMain"
				+ "&flow=assignment";
         context.put("findExternalToolUrl", contentReturn);
    } // setAssignmentFormContext

    /**
     * Get a user facing String message represeting the list of file types that are accepted by the content review service
     * They appear in this form: PowerPoint (.pps, .ppt, .ppsx, .pptx), plain text (.txt), ...
     */
    private String getContentReviewAcceptedFileTypesMessage() {
        StringBuilder sb = new StringBuilder();
        Map<String, SortedSet<String>> fileTypesToExtensions = contentReviewService.getAcceptableFileTypesToExtensions();
        // The delimiter is a comma. Commas still need to be internationalized (the arabic comma is not the english comma)
        String i18nDelimiter = rb.getString("content_review.accepted.types.delimiter") + " ";
        String i18nLParen = " " + rb.getString("content_review.accepted.types.lparen");
        String i18nRParen = rb.getString("content_review.accepted.types.rparen");
        String fDelimiter = "";
        // don't worry about conjunctions; just separate with commas
        for (Map.Entry<String, SortedSet<String>> entry : fileTypesToExtensions.entrySet()) {
            String fileType = entry.getKey();
            SortedSet<String> extensions = entry.getValue();
            sb.append(fDelimiter).append(fileType).append(i18nLParen);
            String eDelimiter = "";
            for (String extension : extensions) {
                sb.append(eDelimiter).append(extension);
                // optimized by java compiler
                eDelimiter = i18nDelimiter;
            }
            sb.append(i18nRParen);

            // optimized by java compiler
            fDelimiter = i18nDelimiter;
        }

        return sb.toString();
    }

    /**
     * how many gradebook items has been assoicated with assignment
     *
     * @param context
     * @param state
     * @param gradebookUid
     * @param aTitle
     */
    private void currentAssignmentGradebookIntegrationIntoContext(Context context, SessionState state, Assignment assignment) {
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

        boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(contextString);
        context.put("isGradebookGroupEnabled", isGradebookGroupEnabled);

        List<Gradebook> gradebookList = gradingService.getGradebookGroupInstances(contextString);

        for (Gradebook gb : gradebookList) {
            List<org.sakaiproject.grading.api.Assignment> gradebookAssignments = gradingService.getAssignments(gb.getUid(), contextString, SortType.SORT_BY_NONE);

            if (gradebookAssignments != null && gradebookAssignments.size() > 0) {
                context.put("existAnyItemGradebook", true);
                break;
            }
        }

        // Determine if we're hiding the option to link to existing gradebook items
        context.put("allowLinkToExistingGradebookItem",
        serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_ALLOW_LINK_TO_EXISTING_GB_ITEM,
            AssignmentConstants.SAK_PROP_ALLOW_LINK_TO_EXISTING_GB_ITEM_DFLT));

        if (isGradebookGroupEnabled) {
            String gbItems = null;

            if (assignment != null) {
                gbItems = assignment.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
            } else {
                Object associateGradebook = state.getAttribute(GB_SELECTOR);
                gbItems = associateGradebook != null ? associateGradebook.toString() : null;
            }

            if (gbItems != null && !gbItems.isBlank()) {
                List<String> itemList = Arrays.asList(gbItems.split(","));

                String selectedGradebook = "";

                for (String item : itemList) {
                    List<String> gradebookUids = gradingService.getGradebookUidByExternalId(item);

                    boolean isExternalAssignmentDefined = false;

                    for (String gradebookUid : gradebookUids) {
                        isExternalAssignmentDefined = gradingService.isExternalAssignmentDefined(gradebookUid, item);

                        if (isExternalAssignmentDefined) {
                            org.sakaiproject.grading.api.Assignment a = gradingService.getExternalAssignment(gradebookUid, item);

                            selectedGradebook = assignmentToolUtils.fillSelectedGradebook(contextString, a.getId(), selectedGradebook);
                        }
                    }

                    if (!isExternalAssignmentDefined) {
                        Long itemId = Long.parseLong(item);

                        selectedGradebook = assignmentToolUtils.fillSelectedGradebook(contextString, itemId, selectedGradebook);
                    }
                }

                context.put("selectedGradebook", selectedGradebook);
            } else {
                context.put("selectedCategories", state.getAttribute(NEW_ASSIGNMENT_CATEGORY));
            }
        } else {
            String aTitle = assignment != null ? assignment.getTitle() : null;

            HashMap<String, String> gAssignmentIdTitles = new HashMap<>();

            HashMap<String, String> gradebookAssignmentsSelectedDisabled = new HashMap<>();
            HashMap<String, String> gradebookAssignmentsLabel = new HashMap<>();

            for (Assignment a : assignmentService.getAssignmentsForContext(contextString)) {
                String gradebookItem = a.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                if (StringUtils.isNotBlank(gradebookItem)) {
                    String associatedAssignmentTitles = "";
                    if (gAssignmentIdTitles.containsKey(gradebookItem)) {
                        // get the current associated assignment titles first
                        associatedAssignmentTitles = gAssignmentIdTitles.get(gradebookItem) + ", ";
                    }

                    // append the current assignment title
                    associatedAssignmentTitles += a.getTitle();

                    // put the current associated assignment titles back
                    gAssignmentIdTitles.put(gradebookItem, associatedAssignmentTitles);
                }
            }

            // get all assignments in Gradebook
            List gradebookAssignments = gradingService.getAssignments(contextString, contextString, SortType.SORT_BY_NONE);

            // filtering out those from Samigo
            for (Iterator i = gradebookAssignments.iterator(); i.hasNext(); ) {
                org.sakaiproject.grading.api.Assignment gAssignment = (org.sakaiproject.grading.api.Assignment) i.next();
                if (!gAssignment.getExternallyMaintained() || gAssignment.getExternallyMaintained() && gAssignment.getExternalAppName().equals(assignmentService.getToolId())) {

                    // gradebook item has been associated or not
                    String gaId = gAssignment.getExternallyMaintained() ? gAssignment.getExternalId() : gAssignment.getId().toString();
                    String status = "";
                    if (gAssignmentIdTitles.containsKey(gaId)) {
                        String assignmentTitle = gAssignmentIdTitles.get(gaId);
                        if (aTitle != null && aTitle.equals(assignmentTitle)) {
                            // this gradebook item is associated with current assignment, make it selected
                            status = "selected";
                        }
                    }

                    // check with the state variable
                    if (StringUtils.equals((String) state.getAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT), gaId)) {
                        status = "selected";
                    }

                    gradebookAssignmentsSelectedDisabled.put(formattedText.escapeHtml(gaId), status);

                    // gradebook assignment label
                    String label = gAssignment.getName();
                    if (gAssignmentIdTitles.containsKey(gaId)) {
                        label += " ( " + rb.getFormattedMessage("usedGradebookAssignment", new Object[]{gAssignmentIdTitles.get(gaId)}) + " )";
                    }
                    gradebookAssignmentsLabel.put(formattedText.escapeHtml(gaId), label);
                }
            }

            // Items sorted by name
            gradebookAssignmentsLabel = gradebookAssignmentsLabel.entrySet().stream()
                    .sorted(Entry.comparingByValue(Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));

            context.put("gradebookAssignmentsSelectedDisabled", gradebookAssignmentsSelectedDisabled);
            context.put("gradebookAssignmentsLabel", gradebookAssignmentsLabel);
        }
    }

    private void putGradebookCategoryInfoIntoContext(SessionState state, Context context) {
        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);

        if (!gradingService.isGradebookGroupEnabled(siteId)) {
            Map<Long, String> categoryTable = getCategoryTable();
            if (categoryTable != null) {
                long categoryTableSize = categoryTable.size();
                context.put("value_totalCategories", Long.valueOf(categoryTableSize));

                // selected category
                context.put("value_Category", state.getAttribute(NEW_ASSIGNMENT_CATEGORY));

                // Preserve insertion order (matches Gradebook Settings order)
                List<Long> categoryList = new ArrayList<>(categoryTable.keySet());
                context.put("categoryKeys", categoryList);
                context.put("categoryTable", categoryTable);
            } else {
                context.put("value_totalCategories", Long.valueOf(0));
            }
        }
    }

    /**
     * put the release grade notification options into context
     *
     * @param state
     * @param context
     */
    private void putReleaseGradeNotificationOptionIntoContext(SessionState state, Context context) {
        if (state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE) == null) {
            // set the notification value using site default to be none: no email will be sent to student when the grade is released
            state.setAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_NONE);
        }
        // input fields
        context.put("name_assignment_releasegrade_notification", ASSIGNMENT_RELEASEGRADE_NOTIFICATION);
        context.put("value_assignment_releasegrade_notification", state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE));
        // the option values
        context.put("value_assignment_releasegrade_notification_none", AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_NONE);
        context.put("value_assignment_releasegrade_notification_each", AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_EACH);
    }

    /**
     * put the release resubmission grade notification options into context
     *
     * @param state
     * @param context
     */
    private void putReleaseResubmissionNotificationOptionIntoContext(SessionState state, Context context, Assignment a) {
        if (state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE) == null && a != null) {
            // get the assignment property for notification setting first
            state.setAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, a.getProperties().get(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE));
        }
        if (state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE) == null) {
            // set the notification value using site default to be none: no email will be sent to student when the grade is released
            state.setAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_NONE);
        }
        // input fields
        context.put("name_assignment_releasereturn_notification", ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION);
        context.put("value_assignment_releasereturn_notification", state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE));
        // the option values
        context.put("value_assignment_releasereturn_notification_none", AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_NONE);
        context.put("value_assignment_releasereturn_notification_each", AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_EACH);
    }

    /**
     * build the instructor view of create a new assignment
     */
    private String build_instructor_preview_assignment_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        context.put("time", Instant.now());

        context.put("user", userDirectoryService.getCurrentUser());

        context.put("value_Title", (String) state.getAttribute(NEW_ASSIGNMENT_TITLE));
        context.put("name_order", NEW_ASSIGNMENT_ORDER);
        context.put("value_position_order", (String) state.getAttribute(NEW_ASSIGNMENT_ORDER));

        Instant openTime = getTimeFromState(state, NEW_ASSIGNMENT_OPEN_MONTH, NEW_ASSIGNMENT_OPEN_DAY, NEW_ASSIGNMENT_OPEN_YEAR, NEW_ASSIGNMENT_OPEN_HOUR, NEW_ASSIGNMENT_OPEN_MIN);
        context.put("value_OpenDate", openTime);

        if (Boolean.valueOf(serverConfigurationService.getBoolean("assignment.visible.date.enabled", false))) {
            Instant visibleTime = getTimeFromState(state, NEW_ASSIGNMENT_VISIBLE_MONTH, NEW_ASSIGNMENT_VISIBLE_DAY, NEW_ASSIGNMENT_VISIBLE_YEAR, NEW_ASSIGNMENT_VISIBLE_HOUR, NEW_ASSIGNMENT_VISIBLE_MIN);
            context.put("value_VisibleDate", visibleTime);
            context.put(NEW_ASSIGNMENT_VISIBLETOGGLE, visibleTime != null);
        }

        // due time
        Instant dueTime = getTimeFromState(state, NEW_ASSIGNMENT_DUE_MONTH, NEW_ASSIGNMENT_DUE_DAY, NEW_ASSIGNMENT_DUE_YEAR, NEW_ASSIGNMENT_DUE_HOUR, NEW_ASSIGNMENT_DUE_MIN);
        context.put("value_DueDate", dueTime);

        // close time
        Boolean enableCloseDate = (Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
        context.put("value_EnableCloseDate", enableCloseDate);
        if ((enableCloseDate).booleanValue()) {
            Instant closeTime = getTimeFromState(state, NEW_ASSIGNMENT_CLOSE_MONTH, NEW_ASSIGNMENT_CLOSE_DAY, NEW_ASSIGNMENT_CLOSE_YEAR, NEW_ASSIGNMENT_CLOSE_HOUR, NEW_ASSIGNMENT_CLOSE_MIN);
            context.put("value_CloseDate", closeTime);
        }

        context.put("value_Sections", state.getAttribute(NEW_ASSIGNMENT_SECTION));
        context.put("value_SubmissionType", state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE));
        context.put("value_ContentId", state.getAttribute(NEW_ASSIGNMENT_CONTENT_ID));
        context.put("value_ContentTitle", state.getAttribute(NEW_ASSIGNMENT_CONTENT_TITLE));
        context.put("value_ContentLaunchNewWindow", state.getAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW));
        context.put("value_ContentToolNewpage", state.getAttribute(NEW_ASSIGNMENT_CONTENT_TOOL_NEWPAGE));
        context.put("value_GradeType", state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE));
        context.put("value_Description", state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION));
        context.put("value_CheckAddDueDate", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
        context.put("value_CheckHideDueDate", state.getAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE));
        context.put("value_CheckAutoAnnounce", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
        context.put("Value_OpenDateNotification", state.getAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION));
        // the option values
        context.put("value_opendate_notification_none", AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
        context.put("value_opendate_notification_low", AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
        context.put("value_opendate_notification_high", AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
        context.put("value_CheckAddHonorPledge", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE));
        context.put("value_CheckAddInstructorTags", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS));
        context.put("value_CheckAddGroupTags", state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS));

        context.put("value_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE));
        context.put("value_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED));
        context.put("value_NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE));

        context.put("value_CheckAnonymousGrading", Boolean.FALSE);

        // get all available assignments from Gradebook tool except for those created from
        if (canGrade()) {
            context.put("gradebookChoice", state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
            context.put("associateGradebookAssignment", state.getAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));

            // information related to gradebook categories
            putGradebookCategoryInfoIntoContext(state, context);
        }

        context.put("monthTable", monthTable());
        context.put("submissionTypeTable", submissionTypeTable());
        context.put("attachments", state.getAttribute(ATTACHMENTS));

        context.put("contentTypeImageService", contentTypeImageService);

        context.put("preview_assignment_assignment_hide_flag", state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG));
        context.put("preview_assignment_student_view_hide_flag", state.getAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG));
        String assignmentId = StringUtils.trimToNull((String) state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_ID));
        if (assignmentId != null) {
            // editing existing assignment
            context.put("value_assignment_id", assignmentId);
            Assignment a = getAssignment(assignmentId, "build_instructor_preview_assignment_context", state);
            if (a != null) {
            	context.put("gradeName", getGradeName(a, assignmentId));
                context.put("value_CheckAnonymousGrading", assignmentService.assignmentUsesAnonymousGrading(a));
                context.put("isDraft", a.getDraft());
                if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                    String maxGrade = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
                    context.put("value_GradePoints", displayGrade(state, maxGrade, a.getScaleFactor()));
                }
            }
        } else {
            // new assignment
            context.put("isDraft", Boolean.TRUE);
        }

        context.put("value_assignmentcontent_id", state.getAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENTCONTENT_ID));

        context.put("currentTime", Instant.now());

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_PREVIEW_ASSIGNMENT;

    } // build_instructor_preview_assignment_context

    /**
     * build the instructor view to publish or unpublish
     */
    protected String build_instructor_bulk_operation_context(VelocityPortlet portlet, Context context, RunData data,
                                                                SessionState state, BulkOperation operation) {

        List<Assignment> assignments = new ArrayList<>();
        List<String> assignmentIds = (List<String>) state.getAttribute(SELECTED_ASSIGNMENT_IDS);
        HashMap<String, Integer> submissionCountTable = new HashMap<>();
        for (String assignmentId : assignmentIds) {
            Assignment a = getAssignment(assignmentId, "build_instructor_" + operation.label + "_assignment_context", state);
            if (a != null) {
                int submittedCount = 0;
                Set<AssignmentSubmission> submissions = assignmentService.getSubmissions(a);
                for (AssignmentSubmission submission : submissions) {
                    if (submission.getSubmitted() && submission.getDateSubmitted() != null) {
                        submittedCount++;
                    }
                }
                if (submittedCount > 0) {
                    // if there is submission to the assignment, show the alert
                    String message = "";
                    switch (operation) {
                        case DELETE:
                            message = "areyousur_withSubmission";
                            break;
                        case PUBLISH:
                            message = "areyousur_pubWithSubmission";
                            break;
                        case UNPUBLISH:
                            message = "areyousur_unpubWithSubmission";
                            break;
                        default:
                    }
                    addAlert(state, rb.getFormattedMessage(message, a.getTitle()));
                }
                assignments.add(a);
                submissionCountTable.put(a.getId(), submittedCount);
            }
        }

        String singleMessage = "";
        String multipleMessage = "";
        String submitName = "";
        String submitLabel = "";
        String singleConfirmMessage = "";
        String multipleConfirmMessage = "";

        switch (operation) {
            case DELETE:
                singleMessage = "delete_assig.delanass";
                multipleMessage = "delete_assig.delass";
                submitName = "eventSubmit_doDelete_assignment";
                submitLabel = "delet";
                singleConfirmMessage = "areyousur_single";
                multipleConfirmMessage = "areyousur_multiple";
                break;
            case PUBLISH:
                singleMessage = "publish_assignment";
                multipleMessage = "publish_assignments";
                submitName = "eventSubmit_doPublish_assignment";
                submitLabel = "publish";
                singleConfirmMessage = "areyousur_pub_single";
                multipleConfirmMessage = "areyousur_pub_multiple";
                break;
            case UNPUBLISH:
                singleMessage = "unpublish_assignment";
                multipleMessage = "unpublish_assignments";
                submitName = "eventSubmit_doUnpublish_assignment";
                submitLabel = "unpublish";
                singleConfirmMessage = "areyousur_unpub_single";
                multipleConfirmMessage = "areyousur_unpub_multiple";
                break;
            default:
        }

        context.put("singleMessage", singleMessage);
        context.put("multipleMessage", multipleMessage);
        context.put("submitName", submitName);
        context.put("submitLabel", submitLabel);
        context.put("assignments", assignments);
        context.put("confirmMessage", assignments.size() > 1 ? rb.getString(multipleConfirmMessage) : rb.getString(singleConfirmMessage));
        context.put("currentTime", Instant.now());
        context.put("submissionCountTable", submissionCountTable);

        String template = getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_BULK_OPERATION;
    }

    /**
     * build the instructor view to grade an submission
     */
    protected String build_instructor_grade_submission_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        Assignment.GradeType gradeType = Assignment.GradeType.GRADE_TYPE_NONE;
        Integer scaleFactor = assignmentService.getScaleFactor();

        // need to show the alert for grading drafts?
        boolean addGradeDraftAlert = false;

        String assignmentRef = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);
        Optional<Assignment> assignment = Optional.ofNullable(getAssignment(assignmentRef, "build_instructor_grade_submission_context", state));

        String submissionRef = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
        // assignment submission
        Optional<AssignmentSubmission> submission = Optional.ofNullable(getSubmission(submissionRef, "build_instructor_grade_submission_context", state));
        Optional<AssignmentSubmissionSubmitter> submitterId = null;
        if (submission.isPresent()) {
            AssignmentSubmission s = submission.get();
            context.put("submission", s);
            context.put("submissionReference", submissionRef);

            final Map<String, User> users = assignmentToolUtils.getSubmitters(s)
                   .collect(Collectors.toMap(User::getId, Function.identity()));

            context.put("users", users);

            context.put("submitterNames", getSubmitterFormattedNames(s, true));
            context.put("submissionStatus", assignmentService.getSubmissionStatus(s.getId(), true));
            submitterId = s.getSubmitters().stream().findAny();
            if (submitterId.isPresent()) {
                context.put("submitterId", submitterId.get().getSubmitter());
            }

            s.getSubmitters().stream().findAny().ifPresent(spent -> context.put("submitterTimeSpent", spent.getTimeSpent()));
            if (assignment.isPresent()) {
                Assignment a = assignment.get();
                setScoringAgentProperties(context, a, s, true);

                // try to put in grade overrides (not applicable when anonymous grading is enabled)
                if (AssignmentToolUtils.allowGroupOverrides(a, assignmentService)) {
                    context.put("ownerGroupId", s.getGroupId());
                    Map<String, Object> grades = new HashMap<>();
                    for (String userId : users.keySet()) {
                        String userGrade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE + "_" + userId);
                        if (userGrade != null) {
                            grades.put(userId, userGrade);
                        }
                    }
                    context.put("value_grades", grades);
                }
		
                // Check if the assignment has a rubric associated or not
                context.put("hasAssociatedRubric", rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_ASSIGNMENT_GRADES, a.getId()));
                context.put("rubricSelfReport", assignmentToolUtils.hasRubricSelfReview(a.getId()));

                //peer review
                if (a.getAllowPeerAssessment() && a.getPeerAssessmentStudentReview() && assignmentService.isPeerAssessmentClosed(a)) {
                    List<PeerAssessmentItem> reviews = assignmentPeerAssessmentService.getPeerAssessmentItems(s.getId(), a.getScaleFactor());
                    List<PeerAssessmentItem> completedReviews = new ArrayList<>();
                    for (PeerAssessmentItem review : reviews) {
                        if (!review.getRemoved() && (review.getScore() != null || (review.getComment() != null && !"".equals(review.getComment().trim())))) {
                            //only show peer reviews that have either a score or a comment saved
                            try {
                                if (a.getIsGroup()) {
                                    String siteId = toolManager.getCurrentPlacement().getContext();
                                    Site site = siteService.getSite(siteId);
                                    review.setAssessorDisplayName(site.getGroup(review.getId().getAssessorUserId()).getTitle());
                                } else {
                                    review.setAssessorDisplayName(userDirectoryService.getUser(review.getId().getAssessorUserId()).getDisplayName());
                                }
                            } catch (IdUnusedException | UserNotDefinedException e) {
                                //reviewer doesn't exist or one of userId/groupId/siteId is wrong
                                log.error(e.getMessage(), e);
                                //set a default one:
                                review.setAssessorDisplayName(rb.getFormattedMessage("gen.reviewer.countReview", completedReviews.size() + 1));
                            }
                            completedReviews.add(review);
                        }
                    }
                    if (completedReviews.size() > 0) {
                        context.put("peerReviews", completedReviews);
                    }
                }
            }

            // show alert if student is working on a draft
            if (assignmentToolUtils.isDraftSubmission(s)) {
                if (s.getAssignment().getCloseDate().isAfter(Instant.now())) {
                    // not pass the close date yet
                    addGradeDraftAlert = true;
                } else {
                    // passed the close date already
                    addGradeDraftAlert = false;
                }
            }

            Map<String, String> p = s.getProperties();
            if (StringUtils.isNotBlank(p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT))) {
                context.put("prevFeedbackText", p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT));
            }
            if (StringUtils.isNotBlank(p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT))) {
                context.put("prevFeedbackComment", p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
            }
            if (StringUtils.isNotBlank(p.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS))) {
                context.put("prevFeedbackAttachments", getPrevFeedbackAttachments(p));
            }
            if (StringUtils.isNotBlank(p.get(ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES))) {
                context.put("NamePropSubmissionScaledPreviousGrades", ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES);
            }

            // put the re-submission info into context
            putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSE_MONTH, ALLOW_RESUBMIT_CLOSE_DAY, ALLOW_RESUBMIT_CLOSE_YEAR, ALLOW_RESUBMIT_CLOSE_HOUR, ALLOW_RESUBMIT_CLOSE_MIN);
            assignment_resubmission_option_into_context(context, state);
            putTimePropertiesInContext(context, state, "Extension", ALLOW_EXTENSION_CLOSE_MONTH, ALLOW_EXTENSION_CLOSE_DAY, ALLOW_EXTENSION_CLOSE_YEAR, ALLOW_EXTENSION_CLOSE_HOUR, ALLOW_EXTENSION_CLOSE_MIN);
            assignment_extension_option_into_context(context, state);
            if (s.getDateSubmitted() != null){ //if there is a submission date, we assume the student submitted; then, extension options should not be available.
                context.put("displayExtensionOptions", false);
            } else {
                context.put("displayExtensionOptions", true);
            }
            boolean isAdditionalNotesEnabled = false;
            Site st = null;
            try {
                st = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
                isAdditionalNotesEnabled = candidateDetailProvider != null && candidateDetailProvider.isAdditionalNotesEnabled(st);

                context.put("isAdditionalNotesEnabled", isAdditionalNotesEnabled);

                if (isAdditionalNotesEnabled && candidateDetailProvider != null && assignment.isPresent() && !assignment.get().getIsGroup()) {
                    if (users.size() == 1) {
                        context.put("notes", candidateDetailProvider.getAdditionalNotes(users.values().toArray(new User[]{})[0], st).orElse(new ArrayList<>()));
                    } else {
                        log.warn(":build_instructor_grade_submission_context: Incorrect number of submitters detected");
                    }
                }
            } catch (IdUnusedException e) {
                log.warn(":build_instructor_grade_submission_context: Site not found!", e);
                context.put("isAdditionalNotesEnabled", false);
            }

            context.put("submissionAttachmentReferences", getVisibleAttachmentIdsToReferences(s));

            putSubmissionLogMessagesInContext(context, s);
            rangeAndGroups.buildInstructorGradeSubmissionContextGroupCheck(assignment, s.getGroupId(), state);
        }

        if (assignment.isPresent()) {
            Assignment a = assignment.get();
            context.put("assignment", a);
            context.put("assignmentReference", assignmentRef);
            gradeType = a.getTypeOfGrade();
            scaleFactor = a.getScaleFactor() != null ? a.getScaleFactor() : scaleFactor;

            state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, assignmentService.assignmentUsesAnonymousGrading(a));

            boolean allowToGrade = true;
            String assignmentAssociateGradebook = a.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

            if (StringUtils.isNotBlank(assignmentAssociateGradebook) && submitterId.isPresent()) {
                String gradebookUid = toolManager.getCurrentPlacement().getContext();
                String siteId = toolManager.getCurrentPlacement().getContext();
                // S2U-26 even if a user can potentially have multiple gradebooks, we're only allowing one return column
                if (gradingService.isGradebookGroupEnabled(siteId)) {
                    List<String> userGradebooks = gradingService.getGradebookInstancesForUser(siteId, submitterId.get().getSubmitter());
                    gradebookUid = userGradebooks.get(0);
                }
                // If the assignment reference is not equal to the associated gradebook item, then a custom gb item is being used
                // S2U-34 In the 22x version this check was looking if the retrieved object from the gradingservice is null, now we check if an exception is thrown
                if (!assignmentRef.equals(assignmentAssociateGradebook)) {
                    try {
                        org.sakaiproject.grading.api.Assignment gbAssignment = null;

                        List<String> associateGbList = Arrays.asList(assignmentAssociateGradebook.split(","));
                        for (String associateString : associateGbList) {
                            org.sakaiproject.grading.api.Assignment gbAssignmentFound = gradingService.getAssignment(gradebookUid, siteId, associateString);

                            if (gbAssignmentFound != null) {
                                gbAssignment = gbAssignmentFound;
                                break;
                            }
                        }

                        if (gbAssignment != null) {
                            Long associateGradebookAssignmentId = gbAssignment.getId();
                            context.put("associatedToGbItem", true);
                            context.put("associatedToGbEntityId", associateGradebookAssignmentId);
                        }
                    } catch (AssessmentNotFoundException e) {
                        log.error("Assignment not found while building grade submission context for custom gradebook item due to: {} {}", e.toString(), ExceptionUtils.getStackTrace(e));
                    }
                }

                if (!gradingService.currentUserHasGradingPerm(siteId)) {
                    context.put("notAllowedToGradeWarning", rb.getString("not_allowed_to_grade_in_gradebook"));
                    allowToGrade = false;
                }
            }
            context.put("allowToGrade", allowToGrade);

            Map<String, Reference> attachmentReferences = new HashMap<>();
            a.getAttachments().forEach(r -> attachmentReferences.put(r, entityManager.newReference(r)));
            context.put("assignmentAttachmentReferences", attachmentReferences);

            if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                context.put("value_grade", displayGrade(state, (String) state.getAttribute(GRADE_SUBMISSION_GRADE), scaleFactor));
            }
        }

        context.put("user", state.getAttribute(STATE_USER));
        context.put("submissionTypeTable", submissionTypeTable());
        context.put("instructorAttachments", state.getAttribute(ATTACHMENTS));
        context.put("contentTypeImageService", contentTypeImageService);

        // names
        context.put("name_grade_assignment_id", GRADE_SUBMISSION_ASSIGNMENT_ID);
        context.put("name_feedback_comment", GRADE_SUBMISSION_FEEDBACK_COMMENT);
        context.put("name_feedback_text", GRADE_SUBMISSION_FEEDBACK_TEXT);
        context.put("name_feedback_attachment", GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
        context.put("name_grade", GRADE_SUBMISSION_GRADE);
        context.put("name_allowResubmitNumber", AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
        context.put("NamePropContentReviewOptoutUrl", ContentReviewConstants.URKUND_OPTOUT_URL);
        context.put("name_private_notes", GRADE_SUBMISSION_PRIVATE_NOTES);

        // values
        context.put("value_year_from", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM));
        context.put("value_year_to", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO));
        context.put("value_grade_assignment_id", state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID));
        context.put("value_feedback_comment", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT));
        context.put("value_feedback_text", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT));
        context.put("value_feedback_attachment", state.getAttribute(ATTACHMENTS));
        context.put("value_CheckAnonymousGrading", assignmentService.assignmentUsesAnonymousGrading(assignment.get()));
        context.put("value_private_notes", state.getAttribute(GRADE_SUBMISSION_PRIVATE_NOTES));

        // is this a non-electronic submission type of assignment
        context.put("nonElectronic", (assignment.isPresent() && assignment.get().getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) ? Boolean.TRUE : Boolean.FALSE);

        context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

        if (addGradeDraftAlert) {
            addAlert(state, rb.getString("grading.alert.draft.beforeclosedate"));
        }
        context.put("alertGradeDraft", Boolean.valueOf(addGradeDraftAlert));

        // SAK-29314
        // Since USER_SUBMISSIONS is restricted to the page size, it is not very useful here
        // Therefore, we will prefer to use STATE_PAGEING_TOTAL_ITEMS. However, sometimes this contains
        // Assignment objects instead of SubmitterSubmission objects, so we have to fall back to USER_SUBMISSIONS
        // if this occurs, although in practise this seems to never happen
        List<SubmitterSubmission> userSubmissions = Collections.EMPTY_LIST;
        List totalItems = (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);
        if (!CollectionUtils.isEmpty(totalItems)) {
            if (totalItems.get(0) instanceof SubmitterSubmission) {
                userSubmissions = (List<SubmitterSubmission>) totalItems;
            }
        }

        if (userSubmissions.isEmpty()) {
            userSubmissions = (List<SubmitterSubmission>) state.getAttribute(USER_SUBMISSIONS);
        }

        // SAK-29314
        resetNavOptions();
        if (userSubmissions != null && submission.isPresent()) {
            for (int index = 0; index < userSubmissions.size(); index++) {
                if (userSubmissions.get(index).getSubmission().getId().equals(submission.get().getId())) {
                    // Determine next/previous
                    boolean goPT = false;
                    boolean goNT = false;
                    if (index > 0) {
                        goPT = true;
                    }
                    if (index < userSubmissions.size() - 1) {
                        goNT = true;
                    }

                    // Determine next ungraded, next with submission, next ungraded with submission
                    for (int i = index + 1; i < userSubmissions.size(); i++) {
                        if (!nextUngraded) {
                            processIfUngraded(userSubmissions.get(i).getSubmission(), true);
                        }

                        if (!nextWithSubmission) {
                            processIfHasSubmission(userSubmissions.get(i).getSubmission(), true);
                        }

                        if (!nextUngradedWithSubmission) {
                            processIfHasUngradedSubmission(userSubmissions.get(i).getSubmission(), true);
                        }

                        if (nextUngraded && nextWithSubmission && nextUngradedWithSubmission) {
                            break;
                        }
                    }

                    // Determine previous ungraded, previous with submission, previous ungraded with submission
                    for (int i = index - 1; i >= 0; i--) {
                        if (!prevUngraded) {
                            processIfUngraded(userSubmissions.get(i).getSubmission(), false);
                        }

                        if (!prevWithSubmission) {
                            processIfHasSubmission(userSubmissions.get(i).getSubmission(), false);
                        }

                        if (!prevUngradedWithSubmission) {
                            processIfHasUngradedSubmission(userSubmissions.get(i).getSubmission(), false);
                        }

                        if (prevUngraded && prevWithSubmission && prevUngradedWithSubmission) {
                            break;
                        }
                    }

                    // Determine if subs only was previously selected
                    boolean subsOnlySelected = false;
                    if (state.getAttribute(STATE_VIEW_SUBS_ONLY) != null) {
                        subsOnlySelected = (Boolean) state.getAttribute(STATE_VIEW_SUBS_ONLY);
                        context.put(CONTEXT_VIEW_SUBS_ONLY, subsOnlySelected);
                    }

                    // Get the previous/next ids as necessary
                    if (goPT) {
                        context.put("prevSubmissionId", AssignmentReferenceReckoner.reckoner().submission(userSubmissions.get(index - 1).getSubmission()).reckon().getReference());
                    }
                    if (goNT) {
                        context.put("nextSubmissionId", AssignmentReferenceReckoner.reckoner().submission(userSubmissions.get(index + 1).getSubmission()).reckon().getReference());
                    }
                    if (nextUngraded) {
                        context.put(CONTEXT_NEXT_UNGRADED_SUB_ID, nextUngradedRef);
                    }
                    if (prevUngraded) {
                        context.put(CONTEXT_PREV_UNGRADED_SUB_ID, prevUngradedRef);
                    }
                    if (nextWithSubmission) {
                        context.put(CONTEXT_NEXT_WITH_SUB_ID, nextWithSubmissionRef);
                    }
                    if (prevWithSubmission) {
                        context.put(CONTEXT_PREV_WITH_SUB_ID, prevWithSubmissionRef);
                    }
                    if (nextUngradedWithSubmission) {
                        context.put(CONTEXT_NEXT_UNGRADED_WITH_SUB_ID, nextUngradedWithSubmissionRef);
                    }
                    if (prevUngradedWithSubmission) {
                        context.put(CONTEXT_PREV_UNGRADED_WITH_SUB_ID, prevUngradedWithSubmissionRef);
                    }

                    // Alter any enabled/disabled states if view subs only is selected
                    if (subsOnlySelected) {
                        if (!nextWithSubmission) {
                            goNT = false;
                        }
                        if (!prevWithSubmission) {
                            goPT = false;
                        }
                        if (!nextUngradedWithSubmission) {
                            nextUngraded = false;
                        }
                        if (!prevUngradedWithSubmission) {
                            prevUngraded = false;
                        }
                    }

                    // Put the button enable/disable flags into the context
                    context.put("goPTButton", goPT);
                    context.put("goNTButton", goNT);
                    context.put(CONTEXT_GO_NEXT_UNGRADED_ENABLED, nextUngraded);
                    context.put(CONTEXT_GO_PREV_UNGRADED_ENABLED, prevUngraded);
                }
            }
        }

        // put supplement item into context
        if (assignment.isPresent()) supplementItemIntoContext(state, context, assignment.get(), null);

        // put the grade confirmation message if applicable
        if (state.getAttribute(GRADE_SUBMISSION_DONE) != null) {
            context.put("gradingDone", Boolean.TRUE);
            state.removeAttribute(GRADE_SUBMISSION_DONE);
        }

        // put the grade confirmation message if applicable
        if (state.getAttribute(GRADE_SUBMISSION_SUBMIT) != null) {
            context.put("gradingSubmit", Boolean.TRUE);
            state.removeAttribute(GRADE_SUBMISSION_SUBMIT);
        }

        // letter grading
        letterGradeOptionsIntoContext(context);

        context.put(RUBRICS_EXPORT_PDF, serverConfigurationService.getBoolean(RubricsConstants.RBCS_EXPORT_PDF, true));

        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
        String toolId = toolManager.getCurrentPlacement().getId();

        if (assignment.isPresent()) {
            if (assignment.get().getTypeOfSubmission() == Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION) {
                context.put("externalTool", Boolean.TRUE);
                if ( ! putExternalToolIntoContext(context, assignment.get(), state) ) {
                    context.put("externalToolDeleted", Boolean.TRUE);
                    addAlert(state, rb.getString("contentitem.tool.deleted"));
                }
            }
        }

        context.put("browser-feature-allow", serverConfigurationService.getBrowserFeatureAllowString());
        String template = (String) getContext(data).get("template");
        if (useSakaiGrader()) {
            if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(state.getAttribute(FROM_VIEW))) {
                context.put("method", "doView");
                context.put("urlParams", "view=" + MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
            } else {
                context.put("method", "doGrade_assignment");
                context.put("urlParams", "assignmentId=" + assignmentRef);
                context.put("selectedGroup", state.getAttribute(VIEW_SUBMISSION_LIST_OPTION));
            }
            return template + TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION_WITH_GRADER;
        } else {
            return template + TEMPLATE_INSTRUCTOR_GRADE_SUBMISSION;
        }

    } // build_instructor_grade_submission_context

    private boolean useSakaiGrader() {
        boolean useSakaiGrader = serverConfigurationService.getBoolean("assignment.usegraderbydefault", true);
        Preferences prefs = preferencesService.getPreferences(sessionManager.getCurrentSessionUserId());
        ResourceProperties props = prefs.getProperties("viewpreferences");
        if (props != null) {
            try {
                String assignmentsViewPrefs = (String) props.getProperty("assignments");
                if (assignmentsViewPrefs != null) {
                    ObjectMapper m = new ObjectMapper();
                    Map<String, Object> prefsMap = m.readValue(URLDecoder.decode(assignmentsViewPrefs, "UTF-8"), Map.class);
                    useSakaiGrader = (Boolean) prefsMap.get("usegrader");
                }
            } catch (Exception e) {
                log.error("Failed to parse assignments view preferences", e);
            }
        }
        return useSakaiGrader;
    }

    public class BasicUser {

        public String id;
        public String displayName;
        public String url;

        public BasicUser(User u) {

            this.id = u.getId();
            this.displayName = u.getDisplayName();
        }
    }

    /**
     * SAK-29314 - Resets all navigation options
     */
    private void resetNavOptions() {
        nextUngraded = false;
        prevUngraded = false;
        nextWithSubmission = false;
        prevWithSubmission = false;
        nextUngradedWithSubmission = false;
        prevUngradedWithSubmission = false;
        nextUngradedRef = "";
        prevUngradedRef = "";
        nextWithSubmissionRef = "";
        prevWithSubmissionRef = "";
        nextUngradedWithSubmissionRef = "";
        prevUngradedWithSubmissionRef = "";
    }

    /**
     * SAK-29314 - Reset the appropriate navigation options
     *
     * @param flag denotes which navigation options to reset
     */
    private void resetNavOptions(String flag) {
        if (null != flag) {
            switch (flag) {
                case FLAG_NEXT_UNGRADED:
                    nextUngraded = false;
                    nextUngradedRef = "";
                    break;
                case FLAG_PREV_UNGRADED:
                    prevUngraded = false;
                    prevUngradedRef = "";
                    break;
                case FLAG_NEXT_WITH_SUB:
                    nextWithSubmission = false;
                    nextWithSubmissionRef = "";
                    break;
                case FLAG_PREV_WITH_SUB:
                    prevWithSubmission = false;
                    prevWithSubmissionRef = "";
                    break;
                case FLAG_NEXT_UNGRADED_WITH_SUB:
                    nextUngradedWithSubmission = false;
                    nextUngradedWithSubmissionRef = "";
                    break;
                case FLAG_PREV_UNGRADED_WITH_SUB:
                    prevUngradedWithSubmission = false;
                    prevUngradedWithSubmissionRef = "";
                    break;
            }
        }
    }

    /**
     * SAK-29314 - Apply the appropriate navigation options
     *
     * @param flag       denotes the navigation options to apply
     * @param submission the submission object in question
     */
    private void applyNavOption(String flag, AssignmentSubmission submission) {
        if (null != flag) {
            String reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            switch (flag) {
                case FLAG_NEXT_UNGRADED:
                    nextUngraded = true;
                    nextUngradedRef = reference;
                    break;
                case FLAG_PREV_UNGRADED:
                    prevUngraded = true;
                    prevUngradedRef = reference;
                    break;
                case FLAG_NEXT_WITH_SUB:
                    nextWithSubmission = true;
                    nextWithSubmissionRef = reference;
                    break;
                case FLAG_PREV_WITH_SUB:
                    prevWithSubmission = true;
                    prevWithSubmissionRef = reference;
                    break;
                case FLAG_NEXT_UNGRADED_WITH_SUB:
                    nextUngradedWithSubmission = true;
                    nextUngradedWithSubmissionRef = reference;
                    break;
                case FLAG_PREV_UNGRADED_WITH_SUB:
                    prevUngradedWithSubmission = true;
                    prevUngradedWithSubmissionRef = reference;
                    break;
            }
        }
    }

    /**
     * SAK-29314 - Determine if the given assignment submission is graded or not, whether
     * it has an actual 'submission' or not.
     *
     * @param submission - the submission to be checked
     * @param isNext     - true/false; is for next submission (true), or previous (false)
     */
    private void processIfUngraded(AssignmentSubmission submission, boolean isNext) {
        String flag = isNext ? FLAG_NEXT_UNGRADED : FLAG_PREV_UNGRADED;
        resetNavOptions(flag);

        // If the submission is ungraded, set the appropriate flag and reference; return true
        if (!submission.getGraded()) {
            applyNavOption(flag, submission);
        }
    }

    /**
     * SAK-29314 - Determine if the given assignment submission actually has a user submission
     *
     * @param submission - the submission to be checked
     * @param isNext     - true/false; is for the next submission (true), or previous (false)
     */
    private void processIfHasSubmission(AssignmentSubmission submission, boolean isNext) {
        String flag = isNext ? FLAG_NEXT_WITH_SUB : FLAG_PREV_WITH_SUB;
        resetNavOptions(flag);

        // If the submission is actually a submission, set the appropriate flag and reference; return true
        if (!AssignmentConstants.SubmissionStatus.NO_SUBMISSION.equals(assignmentService.getSubmissionCanonicalStatus(submission, true)) && submission.getUserSubmission()) {
            applyNavOption(flag, submission);
        }
    }

    /**
     * SAK-29314 - Determine if the given assignment submission actually has a submission
     * and is ungraded.
     *
     * @param submission - the submission to be checked
     * @param isNext     - true/false; is for the next submission (true), or previous (false)
     */
    private void processIfHasUngradedSubmission(AssignmentSubmission submission, boolean isNext) {
        String flag = isNext ? FLAG_NEXT_UNGRADED_WITH_SUB : FLAG_PREV_UNGRADED_WITH_SUB;
        resetNavOptions(flag);

        // If the submisison is actually a submission and is ungraded, set the appropriate flag and reference; return true
        if (!submission.getGraded() && !AssignmentConstants.SubmissionStatus.NO_SUBMISSION.equals(assignmentService.getSubmissionCanonicalStatus(submission, true)) && submission.getUserSubmission()) {
            applyNavOption(flag, submission);
        }
    }

    /**
     * Checks whether the time is already past.
     * If yes, return the time of three days from current time;
     * Otherwise, return the original time
     *
     * @param originalTime
     * @return
     */
    private Instant getProperFutureTime(Instant originalTime) {
        // check whether the time is past already.
        // If yes, add three days to the current time
        Instant time = originalTime;
        if (Instant.now().isAfter(time)) {
            time = Instant.now().plus(Duration.ofDays(3)/*add three days*/);
        }

        return time;
    }

    public void doPrev_back_next_submission_review(RunData rundata, String option, boolean submit) {
        if (!"POST".equals(rundata.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
        // save the instructor input
        boolean hasChange = saveReviewGradeForm(rundata, state, submit ? "submit" : "save");
        switch (option) {
            case "back":
            case "backListStudent":
                state.removeAttribute(PEER_ASSESSMENT_ASSESSOR_ID);
                break;
            default: // Do nothing
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            ParameterParser params = rundata.getParameters();
            List<String> submissionIds = new ArrayList<String>();
            if (state.getAttribute(USER_SUBMISSIONS) != null) {
                submissionIds = (List<String>) state.getAttribute(USER_SUBMISSIONS);
            }

            String submissionId = null;
            String assessorId = null;
            if ("next".equals(option)) {
                submissionId = params.get("nextSubmissionId");
                assessorId = params.get("nextAssessorId");
            } else if ("prev".equals(option)) {
                submissionId = params.get("prevSubmissionId");
                assessorId = params.get("prevAssessorId");
            } else if ("back".equals(option)) {
                String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
                List userSubmissionsState = state.getAttribute(STATE_PAGEING_TOTAL_ITEMS) != null ? (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS) : null;
                if (userSubmissionsState != null && userSubmissionsState.size() > 0 && userSubmissionsState.get(0) instanceof SubmitterSubmission
                        && assignmentService.allowGradeSubmission(assignmentId)) {
                    //coming from instructor view submissions page
                    state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
                    state.setAttribute(PEER_ATTACHMENTS, entityManager.newReferenceList());
                } else {
                    state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
                    state.setAttribute(PEER_ATTACHMENTS, entityManager.newReferenceList());
                }
            }
            if (submissionId != null && submissionIds.contains(submissionId)) {
                state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
            }
            if (assessorId != null) {
                state.setAttribute(PEER_ASSESSMENT_ASSESSOR_ID, assessorId);
            }
        }
    }

    /**
     * Responding to the request of submission navigation
     *
     * @param rundata
     * @param option
     */
    public void doPrev_back_next_submission(RunData rundata, String option) {
        if (!"POST".equals(rundata.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
        // save the instructor input
        boolean hasChange = readGradeForm(rundata, state, "save");
        if (state.getAttribute(STATE_MESSAGE) == null && hasChange) {
            grade_submission_option(rundata, AssignmentConstants.SUBMISSION_OPTION_SAVE);
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            if ("back".equals(option)) {
                // SAK-29314 - calculate our position relative to the list so we can return to the correct page
                state.setAttribute(STATE_GOTO_PAGE, calcPageFromSubmission(state));
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
            } else if ("backListStudent".equals(option)) {
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
            } else if (option.startsWith(FLAG_NEXT) || option.startsWith(FLAG_PREV)) {
                // SAK-29314
                ParameterParser params = rundata.getParameters();
                String submissionsOnlySelected = (String) params.getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX);
                if (FLAG_ON.equals(submissionsOnlySelected)) {
                    switch (option) {
                        case FLAG_NEXT:
                            navigateToSubmission(rundata, CONTEXT_NEXT_WITH_SUB_ID);
                            break;
                        case FLAG_PREV:
                            navigateToSubmission(rundata, CONTEXT_PREV_WITH_SUB_ID);
                            break;
                        case FLAG_NEXT_UNGRADED:
                            navigateToSubmission(rundata, CONTEXT_NEXT_UNGRADED_WITH_SUB_ID);
                            break;
                        case FLAG_PREV_UNGRADED:
                            navigateToSubmission(rundata, CONTEXT_PREV_UNGRADED_WITH_SUB_ID);
                            break;
                    }
                } else {
                    switch (option) {
                        case FLAG_NEXT:
                            navigateToSubmission(rundata, "nextSubmissionId");
                            break;
                        case FLAG_PREV:
                            navigateToSubmission(rundata, "prevSubmissionId");
                            break;
                        case FLAG_NEXT_UNGRADED:
                            navigateToSubmission(rundata, CONTEXT_NEXT_UNGRADED_SUB_ID);
                            break;
                        case FLAG_PREV_UNGRADED:
                            navigateToSubmission(rundata, CONTEXT_PREV_UNGRADED_SUB_ID);
                            break;
                    }
                }
            }
        }

    } // doPrev_back_next_submission

    /**
     * SAK-29314 - Calculate the page of the submission list that the current submission belongs to
     *
     * @param state
     * @return
     */
    private Integer calcPageFromSubmission(SessionState state) {
        int pageSize = 1;
        try {
            pageSize = Integer.parseInt(state.getAttribute(STATE_PAGESIZE).toString());
        } catch (NumberFormatException ex) {
            log.debug(ex.getMessage());
        }

        if (pageSize <= 1) {
            return 1;
        }

        String submissionId = state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID).toString();
        List<SubmitterSubmission> subs = (List<SubmitterSubmission>) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);
        if (!subs.isEmpty() && !(subs.get(0) instanceof SubmitterSubmission)) {
            return 1;
        }
        int subIndex = 0;

        for (int i = 0; i < subs.size(); ++i) {
            SubmitterSubmission sub = subs.get(i);
            String ref = AssignmentReferenceReckoner.reckoner().submission(sub.getSubmission()).reckon().getReference();
            if (ref.equals(submissionId)) {
                subIndex = i;
                break;
            }
        }

        int page = subIndex / pageSize + 1;
        return page;
    }

    private void navigateToSubmission(RunData rundata, String paramString) {
        ParameterParser params = rundata.getParameters();
        SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
        String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);
        String submissionId = StringUtils.trimToNull(params.getString(paramString));
        // SAK-29314 - put submission information into state
        boolean viewSubsOnlySelected = stringToBool((String) params.getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
        if (submissionId != null) {
            // put submission information into state
            putSubmissionInfoIntoState(state, assignmentId, submissionId, viewSubsOnlySelected);
        }
    }

    /**
     * SAK-29314 - Convert the given string into a boolean value
     *
     * @param boolString - the string to be parsed to boolean (may be 'on'/'off' or 'true'/'false')
     * @return the boolean value representing the string given
     */
    private boolean stringToBool(String boolString) {
        return FLAG_ON.equals(boolString) || FLAG_TRUE.equals(boolString);
    }

    /**
     * Parse time value and put corresponding values into state
     *
     * @param state
     * @param timeValue
     * @param month
     * @param day
     * @param year
     * @param hour
     * @param min
     */
    private void putTimePropertiesInState(SessionState state, Instant timeValue, String month, String day, String year, String hour, String min) {
        if (timeValue == null) {
            timeValue = Instant.now().truncatedTo(ChronoUnit.DAYS);
        }
        LocalDateTime bTime = timeValue.atZone(userTimeService.getLocalTimeZone().toZoneId()).toLocalDateTime();
        state.setAttribute(month, bTime.getMonthValue());
        state.setAttribute(day, bTime.getDayOfMonth());
        state.setAttribute(year, bTime.getYear());
        state.setAttribute(hour, bTime.getHour());
        state.setAttribute(min, bTime.getMinute());
    }

    /**
     * put related time information into context variable
     *
     * @param context
     * @param state
     * @param timeName
     * @param month
     * @param day
     * @param year
     * @param hour
     * @param min
     */
    private void putTimePropertiesInContext(Context context, SessionState state, String timeName,
                                            String month, String day, String year, String hour, String min) {
        // get the submission level of close date setting
        context.put("name_" + timeName + "Month", month);
        context.put("name_" + timeName + "Day", day);
        context.put("name_" + timeName + "Year", year);
        context.put("name_" + timeName + "Hour", hour);
        context.put("name_" + timeName + "Min", min);
        context.put("value_" + timeName + "Month", (Integer) state.getAttribute(month));
        context.put("value_" + timeName + "Day", (Integer) state.getAttribute(day));
        context.put("value_" + timeName + "Year", (Integer) state.getAttribute(year));
        context.put("value_" + timeName + "Hour", (Integer) state.getAttribute(hour));
        context.put("value_" + timeName + "Min", (Integer) state.getAttribute(min));
    }

    private void putSubmissionLogMessagesInContext(Context context, AssignmentSubmission submission) {
        List<String> messages = new LinkedList<>();
        Map<String, String> properties = submission.getProperties();
        Map<Integer, String> orderedLogKeys = properties.keySet().stream()
                .filter(k -> k.startsWith("log"))
                .map(k -> new String[] {k, StringUtils.split(k, "log")[0]})
                .collect(Collectors.toMap(a -> new Integer(a[1]), a -> a[0], (e1, e2) -> e1, TreeMap::new));

        orderedLogKeys.values().stream().map(properties::get).map(formattedText::escapeHtml).forEach(messages::add);

        context.put("submissionLog", messages);
    }

    private String getNextSubmissionLogKey(AssignmentSubmission submission) {
        String keyPrefix = "log";
        Map<String, String> properties = submission.getProperties();
        List<Integer> keys = properties.keySet().stream()
                .filter(k -> k.startsWith("log"))
                .map(k -> new Integer(StringUtils.split(k, "log")[0]))
                .sorted()
                .collect(Collectors.toList());
        int next = keys.isEmpty() ? 0 : keys.get(keys.size() - 1) + 1;
        return keyPrefix + next;
    }

    private List getPrevFeedbackAttachments(Map<String, String> p) {
        String attachmentsString = p.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS);
        String[] attachmentsReferences = attachmentsString.split(",");
        List<Reference> prevFeedbackAttachments = entityManager.newReferenceList();
        for (String attachmentsReference : attachmentsReferences) {
            prevFeedbackAttachments.add(entityManager.newReference(attachmentsReference));
        }
        return prevFeedbackAttachments;
    }

    /**
     * build the instructor preview of grading submission
     */
    private String build_instructor_preview_grade_submission_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

        AssignmentSubmission submission = getSubmission((String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID), "build_instructor_preview_grade_submission_context", state);
        if (submission != null) {
            context.put("submission", submission);

            context.put("submissionAttachmentReferences", getVisibleAttachmentIdsToReferences(submission));

            Assignment assignment = submission.getAssignment();
            context.put("assignment", assignment);

            Map<String, Reference> assignmentAttachmentReferences = new HashMap<>();
            assignment.getAttachments().forEach(r -> assignmentAttachmentReferences.put(r, entityManager.newReference(r)));
            context.put("assignmentAttachmentReferences", assignmentAttachmentReferences);

            context.put("submitterNames", getSubmitterFormattedNames(submission, false));

            setScoringAgentProperties(context, assignment, submission, false);

            User user = (User) state.getAttribute(STATE_USER);
            context.put("user", user);
            context.put("submissionTypeTable", submissionTypeTable());
            context.put("contentTypeImageService", contentTypeImageService);

            // filter the feedback text for the instructor comment and mark it as red
            String feedbackText = (String) state.getAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT);
            context.put("feedback_comment", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT));
            context.put("feedback_text", feedbackText);
            context.put("feedback_attachment", state.getAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT));

            context.put("value_CheckAnonymousGrading", assignmentService.assignmentUsesAnonymousGrading(assignment));

            // format to show "factor" decimal places
            String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);
            if (assignment.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                grade = displayGrade(state, grade, assignment.getScaleFactor());
            }
            context.put("grade", grade);

            context.put("comment_open", COMMENT_OPEN);
            context.put("comment_close", COMMENT_CLOSE);

            context.put("allowResubmitNumber", StringUtils.defaultString((String) state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER), "0"));
            String closeTimeString = (String) state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
            if (closeTimeString != null) {
                // close time for resubmit
                Instant time = Instant.ofEpochMilli(Long.parseLong(closeTimeString));
                context.put("allowResubmitCloseTime", time);
            }
        }

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION;

    } // build_instructor_preview_grade_submission_context

    /**
     * build the instructor view to grade an assignment
     */
    private String build_instructor_grade_assignment_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

        boolean showStudentDetails = (Boolean) state.getAttribute(GRADE_SUBMISSION_SHOW_STUDENT_DETAILS);

        if (showStudentDetails) {
            build_show_students_additional_information_context(context, state);

            state.setAttribute(GRADE_SUBMISSION_SHOW_STUDENT_DETAILS, Boolean.FALSE);

            String template = (String) getContext(data).get("template");
            return template + TEMPLATE_INSTRUCTOR_VIEW_STUDENTS_DETAILS;
        }

        context.put("user", state.getAttribute(STATE_USER));

        // sorting related fields
        context.put("sortedBy", state.getAttribute(SORTED_GRADE_SUBMISSION_BY));
        context.put("sortedAsc", state.getAttribute(SORTED_GRADE_SUBMISSION_ASC));
        context.put("sort_lastName", SORTED_GRADE_SUBMISSION_BY_LASTNAME);
        context.put("sort_submitTime", SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME);
        context.put("sort_submitStatus", SORTED_GRADE_SUBMISSION_BY_STATUS);
        context.put("sort_submitGrade", SORTED_GRADE_SUBMISSION_BY_GRADE);
        context.put("sort_submitReleased", SORTED_GRADE_SUBMISSION_BY_RELEASED);
        context.put("sort_submitReview", SORTED_GRADE_SUBMISSION_CONTENTREVIEW);
        context.put("userDirectoryService", userDirectoryService);
        context.put("sort_estimate", SORTED_GRADE_SUBMISSION_BY_ESTIMATE);

        String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
        Assignment assignment = getAssignment(assignmentRef, "build_instructor_grade_assignment_context", state);

        if (assignment != null) {
            context.put("assignment", assignment);
            context.put("isPeerAssessmentClosed", assignmentService.isPeerAssessmentClosed(assignment));
            context.put("assignmentReference", assignmentRef);
            state.setAttribute(EXPORT_ASSIGNMENT_ID, assignment.getId());
            context.put("value_SubmissionType", assignment.getTypeOfSubmission().ordinal());

            Assignment.GradeType gradeType = assignment.getTypeOfGrade();
            context.put("typeOfGrade", gradeType.ordinal());
            context.put("typeOfGradeString", getTypeOfGradeString(gradeType));
            if (gradeType.equals(Assignment.GradeType.SCORE_GRADE_TYPE)) {
                Integer scaleFactor = assignment.getScaleFactor() != null ? assignment.getScaleFactor() : assignmentService.getScaleFactor();
                context.put("maxGradePointString", assignmentService.getMaxPointGradeDisplay(scaleFactor, assignment.getMaxGradePoint()));
            }

            // put creator information into context
            putCreatorIntoContext(context, assignment);

            initViewSubmissionListOption(state);

            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION) {
                if ( ! putExternalToolIntoContext(context, assignment, state) ) {
                    context.put("externalToolDeleted", Boolean.TRUE);
                    addAlert(state, rb.getString("contentitem.tool.deleted"));
                }
            }

            String view = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
            context.put("view", view);

            context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH) != null ? state.getAttribute(VIEW_SUBMISSION_SEARCH) : "");


            // access point url for zip file download
            String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
            String accessPointUrl = serverConfigurationService.getAccessUrl().concat((String) state.getAttribute(EXPORT_ASSIGNMENT_REF));
            if (view != null && !AssignmentConstants.ALL.equals(view)) {
                // append the group info to the end
                accessPointUrl = accessPointUrl.concat(view);
            }

            context.put("accessPointUrl", accessPointUrl);

            state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, assignmentService.assignmentUsesAnonymousGrading(assignment));
            Collection groupsAllowGradeAssignment = assignmentService.getGroupsAllowGradeAssignment(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());

            // group list which user can add message to
            if (groupsAllowGradeAssignment.size() > 0) {
                List<Group> groupList = new ArrayList<>(groupsAllowGradeAssignment);
                sortGroupList(groupList, state);
                context.put("groups", groupList.iterator());
            }

            context.put("value_CheckAnonymousGrading", assignmentService.assignmentUsesAnonymousGrading(assignment));

            List<SubmitterSubmission> userSubmissions = prepPage(state);

            // attach the assignment to these submissions now to avoid costly lookup for each submission later in the velocity template
            for (SubmitterSubmission s : userSubmissions) {
                s.getSubmission().setAssignment(assignment);
            }

            state.setAttribute(USER_SUBMISSIONS, userSubmissions);
            context.put("userSubmissions", state.getAttribute(USER_SUBMISSIONS));

            addAdditionalNotesToContext(userSubmissions, context, state);

            //find peer assessment grades if exist
            if (assignment.getAllowPeerAssessment()) {
                List<String> submissionIds = new ArrayList<String>();
                //get list of submission ids to look up reviews in db
                for (SubmitterSubmission s : userSubmissions) {
                    submissionIds.add(s.getSubmission().getId());
                }
                //look up reviews for these submissions
                List<PeerAssessmentItem> items = assignmentPeerAssessmentService.getPeerAssessmentItems(submissionIds, assignment.getScaleFactor());
                //create a map for velocity to use in displaying the submission reviews
                Map<String, List<PeerAssessmentItem>> itemsMap = new HashMap<String, List<PeerAssessmentItem>>();
                Map<String, User> reviewersMap = new HashMap<>();
                Map<String, Group> groupsReviewersMap = new HashMap<>();
                Site site = null;
                try {
                    site = siteService.getSite(assignment.getContext());
                } catch (IdUnusedException ex) {
                    log.error("Could not get the site {}", context);
                }
                if (items != null) {
                    for (PeerAssessmentItem item : items) {
                        //update items map
                        List<PeerAssessmentItem> sItems = itemsMap.get(item.getId().getSubmissionId());
                        if (sItems == null) {
                            sItems = new ArrayList<>();
                        }
                        sItems.add(item);
                        itemsMap.put(item.getId().getSubmissionId(), sItems);
                        //update users/groups map:
                        if (assignment.getIsGroup()) {
                            Group gr = groupsReviewersMap.get(item.getId().getAssessorUserId());
                            if (gr == null && site != null) {
                                gr = site.getGroup(item.getId().getAssessorUserId());
                                groupsReviewersMap.put(item.getId().getAssessorUserId(), gr);
                            }
                        } else {
                            User u = reviewersMap.get(item.getId().getAssessorUserId());
                            if (u == null) {
                                try {
                                    u = userDirectoryService.getUser(item.getId().getAssessorUserId());
                                    reviewersMap.put(item.getId().getAssessorUserId(), u);
                                } catch (UserNotDefinedException e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
                //go through all the submissions and make sure there aren't any nulls
                for (String id : submissionIds) {
                    List<PeerAssessmentItem> sItems = itemsMap.get(id);
                    if (sItems == null) {
                        sItems = new ArrayList<PeerAssessmentItem>();
                        itemsMap.put(id, sItems);
                    }
                }
                context.put("peerAssessmentItems", itemsMap);
                context.put("reviewersMap", reviewersMap);
                context.put("groupsReviewersMap", groupsReviewersMap);
            }

            // try to put in grade overrides for list context (not when anonymous grading)
            if (AssignmentToolUtils.allowGroupOverrides(assignment, assignmentService)) {
                Map<String, Object> userGrades = new HashMap<>();
                for (SubmitterSubmission ss : userSubmissions) {
                    if (ss != null && ss.getSubmission() != null) {
                        List<String> users = ss.getSubmission().getSubmitters().stream().map(AssignmentSubmissionSubmitter::getSubmitter).toList();
                        for (String user : users) {
                            String agrade = assignmentService.getGradeForSubmitter(ss.getSubmission(), user);
                            if (agrade != null) {
                                userGrades.put(user, agrade);
                            }
                        }
                    }
                }
                context.put("value_grades", userGrades);

            }

            if (assignment.getIsGroup() || gradingService.isGradebookGroupEnabled(assignment.getContext())) {
                rangeAndGroups.buildInstructorGradeAssignmentContext(state, context, assignment);
            }

            // put the re-submission info into context
            assignment_resubmission_option_into_state(assignment, null, state);
            putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSE_MONTH, ALLOW_RESUBMIT_CLOSE_DAY, ALLOW_RESUBMIT_CLOSE_YEAR, ALLOW_RESUBMIT_CLOSE_HOUR, ALLOW_RESUBMIT_CLOSE_MIN);
            assignment_resubmission_option_into_context(context, state);
            assignment_extension_option_into_state(assignment, null, state);
            putTimePropertiesInContext(context, state, "Extension", ALLOW_EXTENSION_CLOSE_MONTH, ALLOW_EXTENSION_CLOSE_DAY, ALLOW_EXTENSION_CLOSE_YEAR, ALLOW_EXTENSION_CLOSE_HOUR, ALLOW_EXTENSION_CLOSE_MIN);
            assignment_extension_option_into_context(context, state);
        }

        if (taggingManager.isTaggable() && assignment != null) {
            context.put("producer", assignmentActivityProducer);
            addProviders(context, state);
            addActivity(context, assignment);
            context.put("taggable", Boolean.TRUE);
        }

        try {
            Optional<AssociationTransferBean> optAssociation
                = rubricsService.getAssociationForToolAndItem(AssignmentConstants.TOOL_ID, assignment.getId(), assignment.getContext());
            if (optAssociation.isPresent()) {
                context.put("rubricId", optAssociation.get().getRubricId());
            }
        } catch (Exception e) {
            log.error("Failed to get association", e);
        }

        context.put("submissionTypeTable", submissionTypeTable());
        context.put("attachments", state.getAttribute(ATTACHMENTS));
        context.put("contentTypeImageService", contentTypeImageService);
        context.put("submission_expand_flag", state.getAttribute(GRADE_SUBMISSION_EXPAND_FLAG));
        context.put("isTimesheet", assignmentService.isTimeSheetEnabled((String) state.getAttribute(STATE_CONTEXT_STRING)));

        add2ndToolbarFields(data, context);

        pagingInfoToContext(state, context);

        // put supplement item into context
        supplementItemIntoContext(state, context, assignment, null);

        // search context
        String searchString = (String) state.getAttribute(STATE_SEARCH) != null ? (String) state.getAttribute(STATE_SEARCH) : "";
        context.put("searchString", searchString);

        context.put("form_search", FORM_SEARCH);
        context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE : Boolean.FALSE);

        // letter grading
        letterGradeOptionsIntoContext(context);

        context.put("useGraderByDefault", serverConfigurationService.getBoolean("assignment.usegraderbydefault", true));

        // ever set the default grade for no-submissions
        if (assignment != null && assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
            // non-electronic submissions
            context.put("form_action", "eventSubmit_doSet_defaultNotGradedNonElectronicScore");
            context.put("form_label", rb.getFormattedMessage("not.graded.non.electronic.submission.grade", state.getAttribute(STATE_NUM_MESSAGES, 0)));
        } else {
            // other types of submissions
            context.put("form_action", "eventSubmit_doSet_defaultNoSubmissionScore");
            context.put("form_label", rb.getFormattedMessage("non.submission.grade", state.getAttribute(STATE_NUM_MESSAGES, 0)));
        }

        // show the reminder for download all url
        String downloadUrl = (String) state.getAttribute(STATE_DOWNLOAD_URL);
        if (downloadUrl != null) {
            context.put("download_url_reminder", rb.getString("download_url_reminder"));
            context.put("download_url_link", downloadUrl);
            context.put("download_url_link_label", rb.getString("download_url_link_label"));
            state.removeAttribute(STATE_DOWNLOAD_URL);
        }

        String peerReviewDraftSkippedMessage = (String) state.getAttribute(STATE_PEER_REVIEW_DRAFT_SKIPPED_MESSAGE);
        if (peerReviewDraftSkippedMessage != null) {
            context.put("peerReviewDraftSkippedMessage", peerReviewDraftSkippedMessage);
            state.removeAttribute(STATE_PEER_REVIEW_DRAFT_SKIPPED_MESSAGE);
        }

        // show "feedback saved" success messages if appropriate
        if (state.getAttribute(SAVED_FEEDBACK) != null) {
            context.put("savedFeedback", Boolean.TRUE);
            state.removeAttribute(SAVED_FEEDBACK);
        }
        if (state.getAttribute(OW_FEEDBACK) != null) {
            context.put("overwriteFeedback", Boolean.TRUE);
            state.removeAttribute(OW_FEEDBACK);
        }
        if (state.getAttribute(RETURNED_FEEDBACK) != null) {
            context.put("returnedFeedback", Boolean.TRUE);
            state.removeAttribute(RETURNED_FEEDBACK);
        }

        String template = (String) getContext(data).get("template");

        return template + TEMPLATE_INSTRUCTOR_GRADE_ASSIGNMENT;

    } // build_instructor_grade_assignment_context

    private void build_show_students_additional_information_context(Context context, SessionState state) {

        List<SubmitterSubmission> returnResources = new ArrayList<>();
        String aRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        Site sst = null;
        try {
            sst = siteService.getSite(contextString);

            Map<User, AssignmentSubmission> submitters = assignmentService.getSubmitterMap(Boolean.FALSE.toString(), "all", null, aRef, contextString);
            for (User u : submitters.keySet()) {
                if (candidateDetailProvider != null && !candidateDetailProvider.getAdditionalNotes(u, sst).isPresent()) {
                    log.debug("Skipping user with no additional notes " + u.getEid());
                    continue;
                }

                AssignmentSubmission sub = submitters.get(u);
                SubmitterSubmission us = new SubmitterSubmission(u, sub);
                returnResources.add(us);
            }

            String ascending = "true";
            String sort = "sorted_grade_submission_by_lastname";
            Boolean anon = state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING) != null ? (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING) : Boolean.FALSE;
            context.put("value_CheckAnonymousGrading", anon);
            AssignmentComparator ac = new AssignmentComparator(state, sort, ascending);
            ac.setAnon(anon);
            try {
                returnResources.sort(ac);
            } catch (Exception e) {
                // log exception during sorting for helping debugging
                log.warn(this + ":build_show_students_additional_information_context sort=" + sort + " ascending=" + ascending, e);
            }

            state.setAttribute(USER_NOTES, returnResources);
            context.put("userNotes", state.getAttribute(USER_NOTES));

            addAdditionalNotesToContext(returnResources, context, state);
        } catch (IdUnusedException iue) {
            log.warn(this + ":build_show_students_additional_information_context: Site not found!" + iue.getMessage());
            context.put("isAdditionalNotesEnabled", false);
        }


    } // build_show_students_additional_information_context

    /**
     * make sure the state variable VIEW_SUBMISSION_LIST_OPTION is not null
     *
     * @param state
     */
    private void initViewSubmissionListOption(SessionState state) {
        if (state.getAttribute(VIEW_SUBMISSION_LIST_OPTION) == null
                && (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) == null
                || !(Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY))) {
            state.setAttribute(VIEW_SUBMISSION_LIST_OPTION, AssignmentConstants.ALL);
        }
    }

    /**
     * put the supplement item information into context
     *
     * @param state
     * @param context
     * @param assignment
     * @param s
     */
    private void supplementItemIntoContext(SessionState state, Context context, Assignment assignment, AssignmentSubmission s) {

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

        // for model answer
        boolean allowViewModelAnswer = assignmentSupplementItemService.canViewModelAnswer(assignment, s);
        context.put("allowViewModelAnswer", allowViewModelAnswer);
        if (allowViewModelAnswer) {
            context.put("assignmentModelAnswerItem", assignmentSupplementItemService.getModelAnswer(assignment.getId()));
        }

        // for note item
        boolean allowReadAssignmentNoteItem = assignmentSupplementItemService.canReadNoteItem(assignment, contextString);
        context.put("allowReadAssignmentNoteItem", allowReadAssignmentNoteItem);
        if (allowReadAssignmentNoteItem) {
            context.put("assignmentNoteItem", assignmentSupplementItemService.getNoteItem(assignment.getId()));
        }
        // for all purpose item
        boolean allowViewAllPurposeItem = assignmentSupplementItemService.canViewAllPurposeItem(assignment);
        context.put("allowViewAllPurposeItem", allowViewAllPurposeItem);
        if (allowViewAllPurposeItem) {
            context.put("assignmentAllPurposeItem", assignmentSupplementItemService.getAllPurposeItem(assignment.getId()));
        }
    }

    /**
     * build the instructor view of an assignment
     */
    private String build_instructor_view_assignment_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        context.put("tlang", rb);

        String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
        Assignment assignment = getAssignment(assignmentId, "build_instructor_view_assignment_context", state);
        if (assignment != null) {
            context.put("assignment", assignment);
            context.put("assignmentReference", assignmentId);

            // In case we need to launch an external tool to test it - doView_external_tool_launch
            state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE, assignmentId);

            // put the resubmit information into context
            assignment_resubmission_option_into_context(context, state);
            assignment_extension_option_into_context(context, state);

            // put external tool information into context
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION) {
                context.put("externalTool", Boolean.TRUE);
                if ( ! putExternalToolIntoContext(context, assignment, state) ) {
                    context.put("externalToolDeleted", Boolean.TRUE);
                    addAlert(state, rb.getString("contentitem.tool.deleted"));
                }
            }

            // put creator information into context
            putCreatorIntoContext(context, assignment);
            context.put("typeOfGradeString", getTypeOfGradeString(assignment.getTypeOfGrade()));

            if (assignment.getTypeOfGrade().equals(Assignment.GradeType.SCORE_GRADE_TYPE)) {
                Integer scaleFactor = assignment.getScaleFactor() != null ? assignment.getScaleFactor() : assignmentService.getScaleFactor();
                context.put("maxGradePointString", assignmentService.getMaxPointGradeDisplay(scaleFactor, assignment.getMaxGradePoint()));
            }

            Map<String, String> properties = assignment.getProperties();
            context.put("scheduled", properties.get(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));
            context.put("announced", properties.get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));

            context.put("value_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE));
            context.put("value_NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED));
            context.put("value_NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE", state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE));

            Map<String, Reference> attachmentReferences = new HashMap<>();
            assignment.getAttachments().forEach(r -> attachmentReferences.put(r, entityManager.newReference(r)));
            context.put("attachmentReferences", attachmentReferences);

            supplementItemIntoContext(state, context, assignment, null);

            // for sorting assignment groups using the standard comparator
            String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
            context.put("isTimesheet", assignmentService.isTimeSheetEnabled(contextString));
            try {
                Site site = siteService.getSite(contextString);
                AssignmentComparator groupComparator = new AssignmentComparator(null, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString());
                String groupTitles = StringUtils.join(getSortedAsnGroupTitles(assignment, site, groupComparator), ", ");
                context.put("asnGroupTitles", groupTitles.isEmpty() ? rb.getString("gen.viewallgroupssections") : groupTitles);
            }
            catch (IdUnusedException e) {
                // ignore
            }
        }

        if (taggingManager.isTaggable() && assignment != null) {
            Session session = sessionManager.getCurrentSession();
            List<DecoratedTaggingProvider> providers = addProviders(context, state);
            List<TaggingHelperInfo> activityHelpers = new ArrayList<TaggingHelperInfo>();
            for (DecoratedTaggingProvider provider : providers) {
                TaggingHelperInfo helper = provider.getProvider().getActivityHelperInfo(assignmentActivityProducer.getActivity(assignment).getReference());
                if (helper != null) {
                    activityHelpers.add(helper);
                }
            }
            addActivity(context, assignment);
            context.put("activityHelpers", activityHelpers);
            context.put("taggable", Boolean.TRUE);

            addDecoUrlMapToContext(session, context, false);
        }

        context.put("currentTime", Instant.now());
        context.put("submissionTypeTable", submissionTypeTable());
        context.put("hideAssignmentFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG));
        context.put("hideStudentViewFlag", state.getAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG));
        context.put("contentTypeImageService", contentTypeImageService);

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_VIEW_ASSIGNMENT;

    } // build_instructor_view_assignment_context

    private void putCreatorIntoContext(Context context, Assignment assignment) {
        // the creator
        String creatorId = assignment.getAuthor();
        try {
            User creator = userDirectoryService.getUser(creatorId);
            context.put("creator", creator.getDisplayName());
        } catch (Exception ee) {
            context.put("creator", creatorId);
            log.warn(this + ":build_instructor_view_assignment_context " + ee.getMessage());
        }
    }

    private boolean putExternalToolIntoContext(Context context, Assignment assignment, SessionState state) {
        context.put("value_ContentId", null);
        context.put("value_ContentTitle", null);
        context.put("value_ContentLaunchURL", null);
        try {
            if ( assignment == null || assignment.getContentId() == null) return false;
            Site site = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
            Long contentKey = assignment.getContentId().longValue();

            if ( contentKey < 1 ) {
                log.warn("contentId not set {} ", assignment);
                return false;
            }
            Map<String, Object> content = ltiService.getContent(contentKey, site.getId());
            if ( content == null ) {
                log.warn("contentId not loaded {} ", contentKey);
                return false;
            }
            context.put("value_ContentId", contentKey);
            String content_launch = ltiService.getContentLaunch(content);
            context.put("value_ContentLaunchURL", content_launch);

            Long toolKey = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
            if (toolKey != null) {
                Map<String, Object> tool = ltiService.getTool(toolKey, site.getId());
                if ( tool == null ) {
                    log.warn("tool not loaded {} ", toolKey);
                    return false;
                }
                String toolTitle = (String) tool.get(LTIService.LTI_TITLE);
                context.put("value_ContentTitle", toolTitle);
            }
            return true;
        } catch(org.sakaiproject.exception.IdUnusedException e ) {
            log.warn("could not find site {} ", e);
            return false;
        }
    }

    /**
     * build the instructor view of reordering assignments
     */
    private String build_instructor_reorder_assignment_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        context.put("context", state.getAttribute(STATE_CONTEXT_STRING));

        List assignments = prepPage(state);

        context.put("assignments", assignments.iterator());
        context.put("assignmentsize", assignments.size());

        String sortedBy = (String) state.getAttribute(SORTED_BY);
        String sortedAsc = (String) state.getAttribute(SORTED_ASC);
        context.put("sortedBy", sortedBy);
        context.put("sortedAsc", sortedAsc);

        context.put("contentTypeImageService", contentTypeImageService);
        context.put("userDirectoryService", userDirectoryService);

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_REORDER_ASSIGNMENT;

    } // build_instructor_reorder_assignment_context

    private String build_student_review_edit_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        Assignment.GradeType gradeType = Assignment.GradeType.GRADE_TYPE_NONE;
        context.put("context", state.getAttribute(STATE_CONTEXT_STRING));
        List<PeerAssessmentItem> peerAssessmentItems = (List<PeerAssessmentItem>) state.getAttribute(PEER_ASSESSMENT_ITEMS);
        String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
        Assignment assignment = getAssignment(assignmentId, "build_student_review_edit_context", state);
        User sessionUser = (User) state.getAttribute(STATE_USER);
        String assessorId = (String) state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID);
        if (assessorId == null) assessorId = sessionUser.getId();
        String currentUserGroup = null;
        if (assignment.getIsGroup()) {
            assessorId = assignmentService.getSubmitterIdForAssignment(assignment, assessorId);
            currentUserGroup = assignmentService.getSubmitterIdForAssignment(assignment, sessionUser.getId());
        }
        context.put("assessorId", assessorId);

        int factor = assignmentService.getScaleFactor();
        int dec = (int) Math.log10(factor);
        if (assignment != null) {
            context.put("assignment", assignment);
            factor = assignment.getScaleFactor();
            dec = (int) Math.log10(factor);
            context.put("peerAssessmentInstructions", StringUtils.isEmpty(assignment.getPeerAssessmentInstructions()) ? "" : assignment.getPeerAssessmentInstructions());

            Map<String, Reference> assignmentAttachmentReferences = new HashMap<>();
            assignment.getAttachments().forEach(r -> assignmentAttachmentReferences.put(r, entityManager.newReference(r)));
            context.put("assignmentAttachmentReferences", assignmentAttachmentReferences);
            context.put("value_CheckAnonymousGrading", assignmentService.assignmentUsesAnonymousGrading(assignment));
        }
        String submissionId = "";
        SecurityAdvisor secAdv = (userId, function, reference) -> {
            if ("asn.submit".equals(function) || "asn.grade".equals(function)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
            return null;
        };
        AssignmentSubmission s = null;
		String submissionReference = null;
        try {
            //surround with a try/catch/finally for the security advisor
            securityService.pushAdvisor(secAdv);
            submissionReference = assignmentService.submissionReference(assignment.getContext(), (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID), assignment.getId());
            s = getSubmission(submissionReference, "build_student_review_edit_context", state);
            securityService.popAdvisor(secAdv);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (secAdv != null) {
                securityService.popAdvisor(secAdv);
            }
        }
        context.put(RubricsConstants.RBCS_HAS_ASSOCIATED_RUBRIC, rubricsService.hasAssociatedRubric(RubricsConstants.RBCS_TOOL_ASSIGNMENT_GRADES, assignment.getId()));
        if (s != null) {
            submissionId = s.getId();
            context.put("submission", s);
            context.put("submissionReference", submissionReference);
            
            final String submitterNames = assignmentToolUtils.getSubmitters(s)
					.map(u -> u.getDisplayName() + " (" + u.getDisplayId() + ")").collect(Collectors.joining(", "));
            context.put("submitterNames", formattedText.escapeHtml(submitterNames));
            if (assignment.getIsGroup()) {
                context.put("ownerGroupId", s.getGroupId());
            } else {
                s.getSubmitters().stream().findAny().ifPresent(u -> context.put("submitterId", u.getSubmitter()));
            }
            Map<String, String> p = s.getProperties();
            if (p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT) != null) {
                context.put("prevFeedbackText", p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT));
            }

            if (p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT) != null) {
                context.put("prevFeedbackComment", p.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
            }

            if (p.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS) != null) {
                context.put("prevFeedbackAttachments", getPrevFeedbackAttachments(p));
            }
            if ((s.getFeedbackText() == null) || (s.getFeedbackText().length() == 0)) {
                context.put("value_feedback_text", s.getSubmittedText());
            } else {
                context.put("value_feedback_text", s.getFeedbackText());
            }

            context.put("value_feedback_text", s.getSubmittedText());
            List<Reference> v = entityManager.newReferenceList();
            s.getFeedbackAttachments().forEach(f -> v.add(entityManager.newReference(f)));
            context.put("value_feedback_attachment", v);
            state.setAttribute(ATTACHMENTS, v);

            context.put("submissionAttachmentReferences", getVisibleAttachmentIdsToReferences(s));
        }
        if (peerAssessmentItems != null && submissionId != null) {
            //find the peerAssessmentItem for this submission:
            PeerAssessmentItem peerAssessmentItem = null;
            for (PeerAssessmentItem item : peerAssessmentItems) {
                if (submissionId.equals(item.getId().getSubmissionId())
                        && assessorId.equals(item.getId().getAssessorUserId())) {
                    peerAssessmentItem = item;
                    break;
                }
            }
            if (peerAssessmentItem != null) {
                //check if current user is the peer assessor, if not, only display data (no editing)
                String currentUserReviewerId = sessionUser.getId();
                if (assignment.getIsGroup() && currentUserGroup != null) {
                    currentUserReviewerId = currentUserGroup;
                }
                if (!currentUserReviewerId.equals(peerAssessmentItem.getId().getAssessorUserId())) {
                    context.put("view_only", true);
                    try {
                        if (assignment.getIsGroup()) {
                            try {
                                Site site = siteService.getSite(assignment.getContext());
                                Group group = site.getGroup(peerAssessmentItem.getId().getAssessorUserId());
                                context.put("reviewer", group);
                            } catch (IdUnusedException ex) {
                                log.warn("Could not get the site {}", context);
                            }
                        } else {
                            User reviewer = userDirectoryService.getUser(peerAssessmentItem.getId().getAssessorUserId());
                            context.put("reviewer", reviewer);
                        }
                    } catch (UserNotDefinedException e) {
                        log.error(e.getMessage(), e);
                    }
                } else {
                    context.put("view_only", false);
                }
                context.put(RubricsConstants.RBCS_ASSESSOR_ID, peerAssessmentItem.getId().getAssessorUserId());
                // get attachments for peer review item
                List<PeerAssessmentAttachment> attachments = assignmentPeerAssessmentService.getPeerAssessmentAttachments(peerAssessmentItem.getId().getSubmissionId(), peerAssessmentItem.getId().getAssessorUserId());
                List<Reference> attachmentRefList = new ArrayList<>();
                if (attachments != null && !attachments.isEmpty()) {
                    for (PeerAssessmentAttachment attachment : attachments) {
                        try {
                            Reference ref = entityManager.newReference(contentHostingService.getReference(attachment.getResourceId()));
                            attachmentRefList.add(ref);
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                        }
                    }
                    if (!attachmentRefList.isEmpty()) {
                        context.put("peer_attachments", attachmentRefList);
                        state.setAttribute(PEER_ATTACHMENTS, attachmentRefList);
                    }
                } else {
                    context.put("peer_attachments", attachmentRefList);
                    state.setAttribute(PEER_ATTACHMENTS, attachmentRefList);
                }

                //scores are saved as whole values
                //so a score of 1.3 would be stored as 13
                //so a DB score of 13 needs to be 1.3:
                String decSeparator = formattedText.getDecimalSeparator();
                if (peerAssessmentItem.getScore() != null) {
                    double score = peerAssessmentItem.getScore() / (double) factor;
                    try {
                        String rv = StringUtils.replace(Double.toString(score), (",".equals(decSeparator) ? "." : ","), decSeparator);
                        NumberFormat nbFormat = formattedText.getNumberFormat(dec, dec, false);
                        DecimalFormat dcformat = (DecimalFormat) nbFormat;
                        Double dblGrade = dcformat.parse(rv).doubleValue();
                        rv = nbFormat.format(dblGrade);
                        context.put("value_grade", rv);
                        context.put("display_grade", rv);
                    } catch (Exception e) {
                        log.warn(this + ":build_student_review_edit_context: Parse Error in display_Grade peerAssesmentItem" + e.getMessage());
                    }
                } else {
                    context.put("value_grade", null);
                    context.put("display_grade", "");
                }
                context.put("item_removed", peerAssessmentItem.getRemoved());
                context.put("value_feedback_comment", peerAssessmentItem.getComment());

                //set previous/next values
                List<SubmitterSubmission> userSubmissionsState = state.getAttribute(STATE_PAGEING_TOTAL_ITEMS) != null ? (List<SubmitterSubmission>) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS) : null;
                List<String> userSubmissions = new ArrayList<>();
                if (userSubmissionsState != null && !userSubmissionsState.isEmpty() && userSubmissionsState.get(0) instanceof SubmitterSubmission) {
                    //from instructor view
                    for (SubmitterSubmission userSubmission : userSubmissionsState) {
                        if (!userSubmissions.contains(userSubmission.getSubmission().getId()) && userSubmission.getSubmission().getSubmitted()) {
                            userSubmissions.add(userSubmission.getSubmission().getId());
                        }
                    }
                } else {
                    //student view
                    for (PeerAssessmentItem item : peerAssessmentItems) {
                        if (!userSubmissions.contains(item.getId().getSubmissionId()) && !item.getSubmitted()) {
                            userSubmissions.add(item.getId().getSubmissionId());
                        }
                    }
                }
                context.put("totalReviews", peerAssessmentItems.size());
                //first setup map to make the navigation logic easier:
                Map<String, List<PeerAssessmentItem>> itemMap = new HashMap<String, List<PeerAssessmentItem>>();
                for (String userSubmissionId : userSubmissions) {
                    for (PeerAssessmentItem item : peerAssessmentItems) {
                        if (userSubmissionId.equals(item.getId().getSubmissionId())) {
                            List<PeerAssessmentItem> items = itemMap.get(userSubmissionId);
                            if (items == null) {
                                items = new ArrayList<PeerAssessmentItem>();
                            }
                            items.add(item);
                            itemMap.put(item.getId().getSubmissionId(), items);
                        }
                    }
                }
                for (int i = 0; i < userSubmissions.size(); i++) {
                    String userSubmissionId = userSubmissions.get(i);
                    if (userSubmissionId.equals(submissionId)) {
                        //we found the right submission, now find the items
                        List<PeerAssessmentItem> submissionItems = itemMap.get(submissionId);
                        if (submissionItems != null) {
                            for (int j = 0; j < submissionItems.size(); j++) {
                                PeerAssessmentItem item = submissionItems.get(j);
                                if (item.getId().getAssessorUserId().equals(assessorId)) {
                                    context.put("reviewNumber", (peerAssessmentItems.indexOf(item) + 1));
                                    context.put("anonNumber", i + 1);
                                    boolean goPT = false;
                                    boolean goNT = false;
                                    if ((i - 1) >= 0 || (j - 1) >= 0) {
                                        goPT = true;
                                    }
                                    if ((i + 1) < userSubmissions.size() || (j + 1) < submissionItems.size()) {
                                        goNT = true;
                                    }
                                    context.put("goPTButton", goPT);
                                    context.put("goNTButton", goNT);

                                    if (j > 0) {
                                        // retrieve the previous submission id
                                        context.put("prevSubmissionId", (submissionItems.get(j - 1).getId().getSubmissionId()));
                                        context.put("prevAssessorId", (submissionItems.get(j - 1).getId().getAssessorUserId()));
                                    } else if (i > 0) {
                                        //go to previous submission and grab the last item in that list
                                        int k = i - 1;
                                        while (k >= 0 && !itemMap.containsKey(userSubmissions.get(k))) {
                                            k--;
                                        }
                                        if (k >= 0 && itemMap.get(userSubmissions.get(k)).size() > 0) {
                                            List<PeerAssessmentItem> pItems = itemMap.get(userSubmissions.get(k));
                                            PeerAssessmentItem pItem = pItems.get(pItems.size() - 1);
                                            context.put("prevSubmissionId", (pItem.getId().getSubmissionId()));
                                            context.put("prevAssessorId", (pItem.getId().getAssessorUserId()));
                                        } else {
                                            //no previous option, set to false
                                            context.put("goPTButton", Boolean.FALSE);
                                        }
                                    }

                                    if (j < submissionItems.size() - 1) {
                                        // retrieve the next submission id
                                        context.put("nextSubmissionId", (submissionItems.get(j + 1).getId().getSubmissionId()));
                                        context.put("nextAssessorId", (submissionItems.get(j + 1).getId().getAssessorUserId()));
                                    } else if (i < userSubmissions.size() - 1) {
                                        //go to previous submission and grab the last item in that list
                                        int k = i + 1;
                                        while (k < userSubmissions.size() && !itemMap.containsKey(userSubmissions.get(k))) {
                                            k++;
                                        }
                                        if (k < userSubmissions.size() && itemMap.get(userSubmissions.get(k)).size() > 0) {
                                            List<PeerAssessmentItem> pItems = itemMap.get(userSubmissions.get(k));
                                            PeerAssessmentItem pItem = pItems.get(0);
                                            context.put("nextSubmissionId", (pItem.getId().getSubmissionId()));
                                            context.put("nextAssessorId", (pItem.getId().getAssessorUserId()));
                                        } else {
                                            //no next option, set to false
                                            context.put("goNTButton", Boolean.FALSE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        context.put("user", sessionUser);
        context.put("submissionTypeTable", submissionTypeTable());
        context.put("instructorAttachments", state.getAttribute(ATTACHMENTS));
        context.put("contentTypeImageService", contentTypeImageService);
        context.put("name_grade_assignment_id", GRADE_SUBMISSION_ASSIGNMENT_ID);
        context.put("name_feedback_comment", GRADE_SUBMISSION_FEEDBACK_COMMENT);
        context.put("name_feedback_text", GRADE_SUBMISSION_FEEDBACK_TEXT);
        context.put("name_feedback_attachment", GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
        context.put("name_grade", GRADE_SUBMISSION_GRADE);
        context.put("name_allowResubmitNumber", AssignmentConstants.ALLOW_RESUBMIT_NUMBER);

        // put supplement item into context
        try {
            //surround with a try/catch/finally for the security advisor
            securityService.pushAdvisor(secAdv);
            supplementItemIntoContext(state, context, assignment, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (secAdv != null) {
                securityService.popAdvisor(secAdv);
            }
        }
        // put the grade confirmation message if applicable
        if (state.getAttribute(GRADE_SUBMISSION_DONE) != null) {
            context.put("gradingDone", Boolean.TRUE);
            state.removeAttribute(GRADE_SUBMISSION_DONE);
            if (state.getAttribute(PEER_ASSESSMENT_REMOVED_STATUS) != null) {
                context.put("itemRemoved", state.getAttribute(PEER_ASSESSMENT_REMOVED_STATUS));
                state.removeAttribute(PEER_ASSESSMENT_REMOVED_STATUS);
            }
        }
        // put the grade confirmation message if applicable
        if (state.getAttribute(GRADE_SUBMISSION_SUBMIT) != null) {
            context.put("gradingSubmit", Boolean.TRUE);
            state.removeAttribute(GRADE_SUBMISSION_SUBMIT);
        }

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_STUDENT_REVIEW_EDIT;
    }

    /**
     * build the instructor view to view the list of students for an assignment
     */
    private String build_instructor_view_students_assignment_context(VelocityPortlet portlet, Context context, RunData data,
                                                                     SessionState state) {
        // cleaning from view attribute
        state.removeAttribute(FROM_VIEW);

        // Null or true means we want to reset the tables state
        boolean resetTableState = !Boolean.FALSE.equals(state.getAttribute(RESET_TABLE_STATE));
        context.put("resetTableState", resetTableState);
        state.setAttribute(RESET_TABLE_STATE, false);

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        Site site;
        try {
            site = siteService.getSite(contextString);
        } catch (IdUnusedException e) {
            throw new IllegalStateException("Can not build context for invalid site with id [" + contextString + "]");
        }

        initViewSubmissionListOption(state);
        String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
        Boolean searchFilterOnly = Boolean.TRUE.equals(state.getAttribute(SUBMISSIONS_SEARCH_ONLY));

        String accessPointUrl = serverConfigurationService.getAccessUrl() +
                AssignmentReferenceReckoner.reckoner().context(contextString).reckon().getReference() +
                "?contextString=" + contextString +
                "&viewString=" + allOrOneGroup +
                "&searchFilterOnly=" + searchFilterOnly.toString() +
                "&estimate=true";
        context.put("accessPointUrl", accessPointUrl);

        Collection<Assignment> assignments = assignmentService.getAssignmentsForContext(contextString);

        boolean hasAtLeastOneAnonAssigment = false;
        for (Assignment assignment : assignments) {
            if (assignmentService.assignmentUsesAnonymousGrading(assignment)) {
                hasAtLeastOneAnonAssigment = true;
                break;
            }
        }
        context.put("hasAtLeastOneAnonAssignment", hasAtLeastOneAnonAssigment);

        List<String> nonSubmitterPermissions = serverConfigurationService.getStringList(AssignmentConstants.SAK_PROP_NON_SUBMITTER_PERMISSIONS,
                AssignmentConstants.SAK_PROP_NON_SUBMITTER_PERMISSIONS_DEFAULT);

        Predicate<String> isNonSubmitter = (userId) -> nonSubmitterPermissions.stream()
                .filter(permission -> securityService.unlock(userId, permission, site.getReference()))
                .findAny()
                .isEmpty();

        Set<String> groupUsers = Collections.emptySet();

        if (StringUtils.isBlank(allOrOneGroup) || AssignmentConstants.ALL.equals(allOrOneGroup)) {
            if (securityService.unlock(userDirectoryService.getCurrentUser(), SECURE_ALL_GROUPS, siteService.siteReference(site.getId()))){
                groupUsers = site.getUsers();
            } else {
                groupUsers = site.getGroupsWithMember(userDirectoryService.getCurrentUser().getId()).stream().map(Group::getUsers).flatMap(Set::stream).collect(Collectors.toSet());
            }
        } else {
            groupUsers = site.getGroup(allOrOneGroup).getUsers();
        }

        Map<String, User> studentMembers = groupUsers.stream()
                    .filter(isNonSubmitter)
                    .map(userDirectoryService::getOptionalUser)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toMap(User::getId, Function.identity()));

        Comparator<Assignment> assignmentComparator = new AssignmentComparator(state, SORTED_BY_DEFAULT, Boolean.TRUE.toString());

        Map<User, Iterator<Assignment>> showStudentAssignments = new HashMap<>();
        Set<String> expandedStudents = (Set<String>) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
        if (expandedStudents != null) {
            context.put("studentListShowSet", expandedStudents);
            for (String userId : expandedStudents) {
                Set<Assignment> userSubmittableAssignments = assignments.stream()
                        .filter(Predicate.not(assignmentService::assignmentUsesAnonymousGrading))
                        .collect(Collectors.toSet());

                showStudentAssignments.put(studentMembers.get(userId), userSubmittableAssignments.iterator());
            }
        }

        context.put("expandedUserId", state.getAttribute(EXPANDED_USER_ID));
        context.put("studentMembersMap", studentMembers);
        context.put("studentMembers", new SortedIterator(studentMembers.values().iterator(), new AssignmentComparator(state, SORTED_USER_BY_SORTNAME, Boolean.TRUE.toString())));
        context.put("viewGroup", state.getAttribute(VIEW_SUBMISSION_LIST_OPTION));
        context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH) != null ? state.getAttribute(VIEW_SUBMISSION_SEARCH) : "");
        context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null ? (Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY) : Boolean.FALSE);
        Collection<Group> groups = getCurrentUserGroupsInSite(contextString);
        context.put("groups", new SortedIterator(groups.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString())));


        context.put("studentAssignmentsTable", showStudentAssignments);
        context.put("currentTime", Instant.now());

        // set visible time context variables
        context.put("isTimesheet", assignmentService.isTimeSheetEnabled(contextString));

        add2ndToolbarFields(data, context);

        context.put("view", MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);

        return getContext(data).get("template") + TEMPLATE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT;

    } // build_instructor_view_students_assignment_context

    /**
     * build the instructor view to report the submissions
     */
    private String build_instructor_report_submissions(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        List submissions = prepPage(state);
        context.put("submissions", submissions);

        List<SubmitterSubmission> allSubmissions = (List<SubmitterSubmission>) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);
        boolean hasAtLeastOneAnonAssigment = false;
        for (SubmitterSubmission submission : allSubmissions) {
            Assignment assignment = submission.getSubmission().getAssignment();
            if (assignmentService.assignmentUsesAnonymousGrading(assignment)) {
                hasAtLeastOneAnonAssigment = true;
                break;
            }
        }
        context.put("hasAtLeastOneAnonAssignment", hasAtLeastOneAnonAssigment);

        context.put("sortedBy", state.getAttribute(SORTED_SUBMISSION_BY));
        context.put("sortedAsc", state.getAttribute(SORTED_SUBMISSION_ASC));

        context.put("sortedBy_lastName", SORTED_GRADE_SUBMISSION_BY_LASTNAME);
        context.put("sortedBy_submitTime", SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME);
        context.put("sortedBy_grade", SORTED_GRADE_SUBMISSION_BY_GRADE);
        context.put("sortedBy_status", SORTED_GRADE_SUBMISSION_BY_STATUS);
        context.put("sortedBy_released", SORTED_GRADE_SUBMISSION_BY_RELEASED);
        context.put("sortedBy_estimate", SORTED_GRADE_SUBMISSION_BY_ESTIMATE);

        // get current site
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

        context.put("view", MODE_INSTRUCTOR_REPORT_SUBMISSIONS);
        String viewString = state.getAttribute(VIEW_SUBMISSION_LIST_OPTION) != null ? (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION) : "";
        context.put("viewString", viewString);
        String searchString = state.getAttribute(VIEW_SUBMISSION_SEARCH) != null ? (String) state.getAttribute(VIEW_SUBMISSION_SEARCH) : "";
        context.put("searchString", searchString);

        Boolean showSubmissionByFilterSearchOnly = state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE : Boolean.FALSE;
        context.put("showSubmissionByFilterSearchOnly", showSubmissionByFilterSearchOnly);

        Collection<Group> groups = getCurrentUserGroupsInSite(contextString);
        context.put("groups", new SortedIterator(groups.iterator(), new AssignmentComparator(state, SORTED_BY_GROUP_TITLE, Boolean.TRUE.toString())));

        add2ndToolbarFields(data, context);

        String accessPointUrl = serverConfigurationService.getAccessUrl() +
                AssignmentReferenceReckoner.reckoner().context(contextString).reckon().getReference() +
                "?contextString=" + contextString +
                "&viewString=" + viewString +
                "&searchString=" + searchString +
                "&searchFilterOnly=" + showSubmissionByFilterSearchOnly.toString();
        context.put("accessPointUrl", accessPointUrl + "&byColumns=" + Boolean.FALSE);
        context.put("accessPointUrlByColumns", accessPointUrl + "&byColumns=" + Boolean.TRUE);

        pagingInfoToContext(state, context);

        addAdditionalNotesToContext(submissions, context, state);

        String template = getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_REPORT_SUBMISSIONS;

    } // build_instructor_report_submissions

    // Is Gradebook defined for the site?
    private boolean canGrade() {
        boolean rv = false;
        try {
            String siteId = toolManager.getCurrentPlacement().getContext();
            if (gradingService.currentUserHasEditPerm(siteId) || gradingService.currentUserHasGradingPerm(siteId)) {
                rv = true;
            }
        } catch (Exception e) {
            log.debug(this + "canGrade " + rb.getFormattedMessage("addtogradebook.alertMessage", new Object[]{e.getMessage()}));
        }

        return rv;

    } // canGrade()

    /**
     * build the instructor view to download/upload information from archive file
     */
    private String build_instructor_download_upload_all(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
        context.put("download", MODE_INSTRUCTOR_DOWNLOAD_ALL.equals(state.getAttribute(STATE_MODE)));
        context.put("hasSubmissionText", state.getAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT));
        context.put("hasSubmissionAttachment", state.getAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT));
        context.put("hasGradeFile", state.getAttribute(UPLOAD_ALL_HAS_GRADEFILE));
        context.put("gradeFileFormat", StringUtils.defaultString((String) state.getAttribute(UPLOAD_ALL_GRADEFILE_FORMAT), "csv"));
        context.put("hasComments", state.getAttribute(UPLOAD_ALL_HAS_COMMENTS));
        context.put("hasFeedbackText", state.getAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT));
        context.put("hasFeedbackAttachment", state.getAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT));
        context.put("releaseGrades", state.getAttribute(UPLOAD_ALL_RELEASE_GRADES));
        context.put("withoutFolders", state.getAttribute(UPLOAD_ALL_WITHOUT_FOLDERS));
        context.put("enableFlatDownload", serverConfigurationService.getBoolean("assignment.download.flat", false));
        context.put("contextString", state.getAttribute(STATE_CONTEXT_STRING));

        String maxFileSizeMB = serverConfigurationService.getString("content.upload.max", "1");
        context.put("uploadallInstruction5", rb.getFormattedMessage("uploadall.instruction5", maxFileSizeMB));

        String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
        Assignment a = getAssignment(assignmentRef, "build_instructor_download_upload_all", state);

        if (a != null) {
            Optional<AssociationTransferBean> optAssociation = rubricsService.getAssociationForToolAndItem(AssignmentConstants.TOOL_ID, a.getId(), a.getContext());
            context.put("hasRubric", optAssociation.isPresent() && optAssociation.get().getRubricId() != null);

            context.put("accessPointUrl", serverConfigurationService.getAccessUrl().concat(assignmentRef));

            Assignment.SubmissionType submissionType = a.getTypeOfSubmission();
            // if the assignment is of text-only or allow both text and attachment, include option for uploading student submit text
            context.put("includeSubmissionText", Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION == submissionType || Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION == submissionType);

            // if the assignment is of attachment-only or allow both text and attachment, include option for uploading student attachment
            context.put("includeSubmissionAttachment", Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION == submissionType || Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION == submissionType || Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION == submissionType);

            context.put("viewString", state.getAttribute(VIEW_SUBMISSION_LIST_OPTION) != null ? state.getAttribute(VIEW_SUBMISSION_LIST_OPTION) : "");

            context.put("searchString", state.getAttribute(VIEW_SUBMISSION_SEARCH) != null ? state.getAttribute(VIEW_SUBMISSION_SEARCH) : "");

            context.put("showSubmissionByFilterSearchOnly", state.getAttribute(SUBMISSIONS_SEARCH_ONLY) != null && ((Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY)) ? Boolean.TRUE : Boolean.FALSE);

            if (a.getContentReview())
            {
                Map<String, String> properties = a.getProperties();
                boolean isSubmissionIndexed = "true".equalsIgnoreCase(properties.get("store_inst_index"));
                String plagiarismNoteKey = isSubmissionIndexed ? "gen.thesubswill.indexed" : "gen.thesubswill";
                context.put("plagiarismNote", rb.getFormattedMessage(plagiarismNoteKey, contentReviewService.getServiceName()));
            }
        }

        String template = getContext(data).get("template");
        return template + TEMPLATE_INSTRUCTOR_UPLOAD_ALL;
    } // build_instructor_upload_all

    public void doSearchTags(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        resetPaging(state);
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

        if (data.getParameters().getString(TAG_SELECTOR) != null && StringUtils.trimToNull(data.getParameters().getString(TAG_SELECTOR)) != null) {
            state.setAttribute(TAG_SELECTOR, data.getParameters().getString(TAG_SELECTOR));
        } else {
            state.removeAttribute(TAG_SELECTOR);
        }
    } // doSearchTags

    /**
     * Filter the assignments list by group
     */
    public void doFilterByGroup(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        resetPaging(state);
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
        state.setAttribute(FILTER_BY_GROUP, (String) data.getParameters().getString(FILTER_BY_GROUP));
    } // doFilterByGroup

    public void doFilter(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        try {
            AssignmentFilter selectedFilter = Enum.valueOf(AssignmentFilter.class, data.getParameters().getString(FILTER_OPTION));
            if(selectedFilter != null && !selectedFilter.equals(state.getAttribute(FILTER_OPTION))) {
                resetPaging(state);
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
                state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
                state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
                state.setAttribute(FILTER_OPTION, selectedFilter);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Can not parse filterOption: {} as an AssignmentsFilter", data.getParameters().getString(FILTER_OPTION));
        }
    }

    /**
     * Search assignments by title
     */
    public void doView_assignments_list_search(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        resetPaging(state);
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

        ParameterParser params = data.getParameters();
        state.setAttribute(SEARCH_ASSIGNMENTS, params.getString("search"));
    }

    public void doView_assignments_list_search_clear(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
        
        state.removeAttribute(SEARCH_ASSIGNMENTS);
    }

    /**
     * Filter the assignments list by AssignmentFilter
     */
    public List<Assignment> filterAssignments(List<Assignment> assignmentList,AssignmentFilter filter) {
        Instant now;
        switch(filter) {
            case ALL:
                return assignmentList; 
            case DRAFT:
                return assignmentList.stream().filter(assignment -> assignment.getDraft()).collect(Collectors.toList());
            case PUB_ALL:
                return assignmentList.stream().filter(assignment -> !assignment.getDraft()).collect(Collectors.toList());
            case PUB_ACTIVE:
                now = Instant.now();
                return assignmentList.stream()
                           .filter(assignment -> !assignment.getDraft())
                           .filter(assignment -> assignment.getCloseDate().isAfter(now))
                       .collect(Collectors.toList());
            case PUB_INACTIVE:
                now = Instant.now();
                return assignmentList.stream()
                           .filter(assignment -> !assignment.getDraft())
                           .filter(assignment -> assignment.getCloseDate().isBefore(now))
                       .collect(Collectors.toList());
            default: 
                return Collections.EMPTY_LIST;
        }
    }

    /**
     * Go to the instructor view
     */
    public void doView_instructor(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

    } // doView_instructor

    /**
     * Go to the student view
     */
    public void doView_student(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // to the student list of assignment view
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

    } // doView_student

    public void doView_submission_evap(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(INVOKE, INVOKE_BY_LINK);
        doView_submission(data);
    }

    public void doView_assignment_honorPledge(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();
        String assignmentReference = params.getString("assignmentReference");
        User user = (User) state.getAttribute(STATE_USER);

        AssignmentSubmission submission = getSubmission(assignmentReference, user, "build_student_view_assignment_honorPledge_context", state);
        Assignment assignment;

        if (submission != null) {
            assignment = submission.getAssignment();
            Boolean honorPledge = submission.getHonorPledge();
            if (honorPledge) {
                // if already accepted honor pledge can continue to view submission
                doView_submission(data);
                return;
            }
        } else {
            assignment = getAssignment(assignmentReference, "doView_assignment_honorPledge", state);
        }

        if (assignment != null) {
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                // non electronic assignments don't have to accept honor pledge
                doView_submission(data);
                return;
            } else {
                state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE, assignmentReference);
                if (assignmentService.canSubmit(assignment)) {
                    state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_ASSIGNMENT_HONORPLEDGE);
                } else {
                    doView_submission(data);
                    return;
                }
            }
        } else {
            // if no assignment add alert and return to assignment list
            addAlert(state, rb.getFormattedMessage("youarenot_viewAssignment", assignmentReference));
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        }
    }

    /**
     * Action is to view the content of one specific assignment submission
     */
    public void doView_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the submission context
        resetViewSubmission(state);

        ParameterParser params = data.getParameters();
        String assignmentReference = params.getString("assignmentReference");
        String id = params.getString("submitterId");
        state.setAttribute(EXPANDED_USER_ID, id);

        if (assignmentReference == null) {
            assignmentReference = params.getString("assignmentId");
        }
        state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE, assignmentReference);

        User user = (User) state.getAttribute(STATE_USER);

        String mode = MODE_LIST_ASSIGNMENTS;
        // determine which view the student should see
        // if they can't access the assignment then return to assignment list
        // if they can't submit then view grade
        // if they can submit but need to accept the honor pledge then honor pledge view
        // if they can submit and don't need to accept honor pledge then submission view

        Assignment assignment = getAssignment(assignmentReference, "doView_submission", state);

        if (assignment != null) {
            AssignmentSubmission submission = getSubmission(assignmentReference, user, "doView_submission", state);
            if (assignmentService.canSubmit(assignment)) {
                String submitterId = params.get("submitterId");

                // From submit as student link chef_assignments_list_assignments.vm
                String submitterIdInstructor = params.getString("submitterIdInstructor");

                // From enter as student link chef_assignments_list_assignments.vm
                if (securityService.isUserRoleSwapped()) {
                    submitterIdInstructor = "instructor";
                }

                if ("instructor".equals(submitterIdInstructor)) {
                    state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_INSTRUCTOR, submitterIdInstructor);
                }

                if (submitterId != null && (assignmentService.allowGradeSubmission(assignmentReference))) {
                    try {
                        user = userDirectoryService.getUser(submitterId);
                        state.setAttribute("student", user);
                    } catch (UserNotDefinedException unde) {
                        log.warn("While setting the submitter could not find user with id [{}], {}", submitterId, unde.toString());
                    }
                }

                // put resubmission option into state
                assignment_resubmission_option_into_state(assignment, submission, state);
                assignment_extension_option_into_state(assignment, submission, state);

                if (assignment.getIsGroup() && !rangeAndGroups.validateUserGroups(state, user.getId(), assignment)) {
                    // if assignment is a group project and there is an issue with the assigned groups
                    // show the submission with group error
                    mode = MODE_STUDENT_VIEW_GROUP_ERROR;
                } else {
                    if (assignment.getHonorPledge() && (submission == null || !submission.getHonorPledge())) {
                        // if assignment uses honor pledge then and student hasn't accepted
                        mode = MODE_STUDENT_VIEW_ASSIGNMENT_HONORPLEDGE;
                    } else {
                        // otherwise show submission entry page
                        mode = MODE_STUDENT_VIEW_SUBMISSION;
                    }
                }

                List<Reference> referenceList = entityManager.newReferenceList();

                if (submission != null) {
                    state.setAttribute(VIEW_SUBMISSION_TEXT, submission.getSubmittedText());
                    submission.getAttachments().forEach(r -> referenceList.add(entityManager.newReference(r)));

                    String timeSpent = "";
                    String submissionTimeSpent = "";
                    for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                        if (StringUtils.isNotBlank(submitter.getTimeSpent())) {
                            submissionTimeSpent = submitter.getTimeSpent();
                        }
                        timeSpent = assignmentService.getTotalTimeSheet(submitter);
                    }
                    state.setAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT, timeSpent);
                    state.setAttribute(AssignmentConstants.ASSIGNMENT_INPUT_ADD_SUBMISSION_TIME_SPENT, submissionTimeSpent);

                    // submission read event
                    LRS_Statement statement = getStatementForViewSubmittedAssignment(submission.getId(), assignment.getTitle());
                    String ref = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
                    Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT_SUBMISSION, ref, null, false, NotificationService.NOTI_OPTIONAL, statement);
                    eventTrackingService.post(event);
                } else {
                    // otherwise, the student just read assignment description and prepare for submission
                    LRS_Statement statement = getStatementForViewAssignment(assignment.getId(), assignment.getTitle());
                    String ref = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
                    Event event = eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT, ref, null, false, NotificationService.NOTI_OPTIONAL, statement);
                    eventTrackingService.post(event);
                }
                state.setAttribute(ATTACHMENTS, referenceList);
            } else {
                // student can't submit
                if (submission != null && (submission.getUserSubmission() || submission.getReturned())) {
                    // if submission is by student or it has been returned
                    if (assignment.getIsGroup() && !rangeAndGroups.validateUserGroups(state, user.getId(), assignment)) {
                        // group project and groups are invalid
                        mode = MODE_STUDENT_VIEW_GROUP_ERROR;
                    } else {
                        // otherwise, send to grade view
                        state.setAttribute(VIEW_GRADE_SUBMISSION_ID, AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference());
                        mode = MODE_STUDENT_VIEW_GRADE;
                    }
                } else {
                    // no submission send to assignment view
                    state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentReference);
                    mode = MODE_STUDENT_VIEW_ASSIGNMENT;
                }
            }
        }
        state.setAttribute(STATE_MODE, mode);
    }

    /**
     * Dispatcher for view submission list options
     */
    public void doView_submission_list_option(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();
        String option = params.getString("option");
        switch (option) {
            case "releaseCommented":
            case "releaseGrades":
                doRelease_grades(data);
                break;
            case "upload":
                doPrep_upload_all(data);
                break;
            default:
        }
    } // doView_submission_list_option

	public void doView_submission_list_search(RunData data)
	{
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();
        state.setAttribute(VIEW_SUBMISSION_SEARCH, params.getString("search"));
	}

	public void doView_submission_list_search_clear(RunData data)
	{
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.removeAttribute(VIEW_SUBMISSION_SEARCH);
	}

    /**
     * Action is to view the content of one specific assignment submission
     */
    public void doView_external_tool_launch(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_EXTERNAL_TOOL_LAUNCH);
        }

    } // doView_submission_list_option


    /**
     * Action is to view the content of one specific assignment submission
     */
    public void doChange_submission_list_option(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();
        String view = params.getString("viewgroup");
        //Case where two dropdowns on same page
        if (view == null) {
            view = params.getString("view");
        }
        state.setAttribute(VIEW_SUBMISSION_LIST_OPTION, view);

    } // doView_submission_list_option

    public void doConfirm_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        doSave_submission(data);
        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_STUDENT_CONFIRM_SUBMISSION);
        }
    }

    /**
     * Preview of the submission
     */
    public void doPreview_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();
        String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        state.setAttribute(PREVIEW_SUBMISSION_ASSIGNMENT_REFERENCE, aReference);
        Assignment a = getAssignment(aReference, "doPreview_submission", state);

        String[] groupChoice = params.getStrings("selectedGroups");
        if (groupChoice != null && ArrayUtils.isNotEmpty(groupChoice)) {
            state.setAttribute(VIEW_SUBMISSION_GROUP, groupChoice[0]);
        }

        saveSubmitInputs(state, params);

        // retrieve the submission text (as formatted text)
        String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT), true);

        state.setAttribute(PREVIEW_SUBMISSION_TEXT, text);
        state.setAttribute(VIEW_SUBMISSION_TEXT, text);

        // assign the EULA attribute
        String eulaAgreementYes = params.getString(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
        if(StringUtils.isEmpty(eulaAgreementYes)) {
            eulaAgreementYes = "false";
        }
        state.setAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT, eulaAgreementYes);

        // get attachment input and generate alert message according to assignment submission type
        checkSubmissionTextAttachmentInput(data, state, a, text, false);
        state.setAttribute(PREVIEW_SUBMISSION_ATTACHMENTS, state.getAttribute(ATTACHMENTS));

        String timeSpent = params.getCleanString(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        if(timeSpent == null) {
            timeSpent = (String)state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        }

        state.setAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT, timeSpent);

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_STUDENT_PREVIEW_SUBMISSION);
        }
    } // doPreview_submission

    /**
     * Preview of the grading of submission
     */
    public void doPreview_grade_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // read user input
        readGradeForm(data, state, "read");

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION);
        }
    } // doPreview_grade_submission

    /**
     * Action is to end the preview submission process
     */
    public void doDone_preview_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // back to the student list view of assignments
        state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_SUBMISSION);

    } // doDone_preview_submission

    public void doAccept_assignment_honor_pledge(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String assignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        User user = (User) state.getAttribute(STATE_USER);

        Assignment assignment = getAssignment(assignmentReference, "doView_assignment_honorPledge", state);
        boolean isVisibleDateEnabled = serverConfigurationService.getBoolean("assignment.visible.date.enabled", false);
        Instant now = Instant.now();
        if (now.isBefore(assignment.getOpenDate())) {
            if (isVisibleDateEnabled && assignment.getVisibleDate() != null && now.isAfter(assignment.getVisibleDate())) {
                state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentReference);
                doView_submission(data);
            } else {
                addAlert(state, rb.getString("notpermis4"));
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
            }
        } else {
            if (assignmentService.canSubmit(assignment, user.getId())) {
                AssignmentSubmission submission = getSubmission(assignmentReference, user, "build_student_view_assignment_honorPledge_context", state);
                if (submission == null) {
                    String submitter;
                    if (assignment.getIsGroup()) {
                        try {
                            Site site = siteService.getSite(assignment.getContext());
                            Collection<Group> groups = site.getGroupsWithMember(user.getId());
                            Set<String> assignmentGroups = assignment.getGroups();
                            Optional<String> groupId = groups.stream().filter(g -> assignmentGroups.contains(g.getReference())).map(Group::getId).findAny();
                            submitter = groupId.orElseThrow(Exception::new);
                        } catch (Exception e) {
                            addAlert(state, "group.submission.not.found");
                            log.warn("User {} could not add submission for assignment {}", user.getId(), assignment.getId());
                            doView_assignment_honorPledge(data);
                            return;
                        }
                    } else {
                        submitter = user.getId();
                    }

                    try {
                        submission = assignmentService.addSubmission(assignment.getId(), submitter);
                        if (submission != null ) {
                            submission.setSubmitted(true);
                            submission.setUserSubmission(false);
                            submission.setDateModified(Instant.now());
                            submission.getSubmitters().stream().filter(sb -> sb.getSubmitter().equals(submitter)).findAny().ifPresent(sb -> sb.setSubmittee(false));
                        }
                    } catch (PermissionException pe) {
                        addAlert(state, rb.getString("notpermis4"));
                        log.warn("User {} could not add submission {}", user.getId(), submission.getId(), pe);
                        doView_assignment_honorPledge(data);
                        return;
                    }
                }

                try {
                    setResubmissionProperties(assignment, submission);
                    submission.setHonorPledge(Boolean.TRUE);
                    assignmentService.updateSubmission(submission);
                    doView_submission(data);
                } catch (PermissionException pe) {
                    addAlert(state, rb.getString("notpermis4"));
                    log.warn("User {} could not update submission {}", user.getId(), submission.getId(), pe);
                    doView_assignment_honorPledge(data);
                }
            } else {
                doView_submission(data);
            }
        }
    }

    public void doCancel_assignment_honor_pledge(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
    }

    /**
     * Action is to end the view assignment process
     */
    public void doDone_view_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // back to the student list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

    } // doDone_view_assignments

    /**
     * Action is to end the preview new assignment process
     */
    public void doDone_preview_new_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // back to the new assignment page
        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);

    } // doDone_preview_new_assignment

    /**
     * Action is to end the user view assignment process and redirect him to the assignment list view
     */
    public void doCancel_student_view_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the view assignment
        state.setAttribute(VIEW_ASSIGNMENT_ID, "");

        // back to the student list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

    } // doCancel_student_view_assignment

    /**
     * Action is to end the show submission process
     */
    public void doCancel_show_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the view assignment
        state.setAttribute(VIEW_ASSIGNMENT_ID, "");
        clearContentReviewSubmissionState(state);

        String fromView = (String) state.getAttribute(FROM_VIEW);
        if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
        } else {
            // back to the student list view of assignments
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        }


    } // doCancel_show_submission

    /**
     * Action is to cancel an operation on a selected list of assignments
     */
    public void doCancel_bulk_operation(RunData data) {

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the show assignment object
        state.setAttribute(SELECTED_ASSIGNMENT_IDS, new ArrayList());

        // back to the instructor list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

    }

    /**
     * Action is to end the show submission process
     */
    public void doCancel_edit_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the assignment object
        resetAssignment(state);

        // back to the student list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

        // reset sorting
        setDefaultSort(state);

    } // doCancel_edit_assignment

    /**
     * Action is to end the show submission process
     */
    public void doCancel_new_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the assignment object
        resetAssignment(state);

        // back to the student list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

        // reset sorting
        setDefaultSort(state);

    } // doCancel_new_assignment

    /**
     * Action is to cancel the grade submission process
     */
    public void doCancel_grade_submission(RunData data) {
        // put submission information into state
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String sId = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
        String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);

        // SAK-29314
        boolean viewSubsOnlySelected = stringToBool((String) data.getParameters().getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
        putSubmissionInfoIntoState(state, assignmentId, sId, viewSubsOnlySelected);

        String fromView = (String) state.getAttribute(FROM_VIEW);
        if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
        } else {
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
        }
    } // doCancel_grade_submission

    /**
     * clean the state variables related to grading page
     *
     * @param state
     */
    private void resetGradeSubmission(SessionState state) {
        // reset the grade parameters
        state.removeAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT);
        state.removeAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT);
        state.removeAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT);
        // remove all GRADE_SUBMISSION_GRADE states including possible grade overrides
        // looking like GRADE_SUBMISSION_GRADE_[id of user]
        Iterator<String> _attribute_names = state.getAttributeNames().iterator();
        while (_attribute_names.hasNext()) {
            String _attribute_name = _attribute_names.next();
            if (_attribute_name.startsWith(GRADE_SUBMISSION_GRADE)) {
                state.removeAttribute(_attribute_name);
            }
        }
        state.removeAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
        state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
        state.removeAttribute(GRADE_SUBMISSION_DONE);
        state.removeAttribute(GRADE_SUBMISSION_SUBMIT);
        state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);

        // SAK-29314
        state.removeAttribute(STATE_VIEW_SUBS_ONLY);

        resetAllowResubmitParams(state);
        resetAllowExtensionParams(state);
    }

    /**
     * Action is to cancel the preview grade process
     */
    public void doCancel_preview_grade_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // back to the instructor view of grading a submission
        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);

    } // doCancel_preview_grade_submission

    /**
     * Action is to cancel the reorder process
     */
    public void doCancel_reorder(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // back to the list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

    } // doCancel_reorder

    /**
     * Action is to cancel the preview grade process
     */
    public void doCancel_preview_to_list_submission(RunData data) {
        doCancel_grade_submission(data);

    } // doCancel_preview_to_list_submission

    /**
     * Action is to return to the view of list assignments
     */
    public void doList_assignments(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // back to the student list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

    } // doList_assignments

    /**
     * Action is to view the list of deleted assignments
     */
    public void doView_deletedAssignments(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        state.setAttribute(STATE_MODE, MODE_LIST_DELETED_ASSIGNMENTS);
        state.setAttribute(SORTED_BY, SORTED_BY_TITLE);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

    } // doView_deletedAssignments

    /**
     * Action is to cancel the student view grade process
     */
    public void doCancel_view_grade(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the view grade submission id
        state.setAttribute(VIEW_GRADE_SUBMISSION_ID, "");

        // back to the student list view of assignments
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

    } // doCancel_view_grade

    /**
     * Action is to save the grade to submission
     */
    public void doSave_grade_submission(RunData data) {

        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        readGradeForm(data, state, "save");
        if (state.getAttribute(STATE_MESSAGE) == null) {
            grade_submission_option(data, AssignmentConstants.SUBMISSION_OPTION_RETRACT);
        }

    } // doSave_grade_submission

    public void doSave_grade_submission_review(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        saveReviewGradeForm(data, state, "save");
    }

    public void doSave_toggle_remove_review(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        if (state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID) != null) {
            String peerAssessor = (String) state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID);
            ParameterParser params = data.getParameters();
            String submissionRef = params.getString("submissionId");
            String submissionId = null;
            String assignmentRef = null;
            String siteId = null;
            AssignmentSubmission submission = null;
            if(submissionRef != null){
            	submissionRef = submissionRef.endsWith("/") ? StringUtils.chop(submissionRef) : submissionRef;
            	AssignmentReferenceReckoner.AssignmentReference submissionReference = AssignmentReferenceReckoner.reckoner().reference(submissionRef).reckon();
                submissionId = submissionReference.getId();
                siteId = submissionReference.getContext();
            }
            if (submissionId != null) {
                //call the DB to make sure this user can edit this assessment, otherwise it wouldn't exist
                PeerAssessmentItem item = assignmentPeerAssessmentService.getPeerAssessmentItem(submissionId, peerAssessor);
                if (item != null) {
                    item.setRemoved(!item.getRemoved());
                    assignmentPeerAssessmentService.savePeerAssessmentItem(item, siteId, AssignmentConstants.EVENT_SAVE_PEER_REVIEW);
                    if (item.getScore() != null) {
                        //item was part of the calculation, re-calculate
                        boolean saved = assignmentPeerAssessmentService.updateScore(submissionId, peerAssessor);
                        if (saved) {
                        	submission = getSubmission(submissionRef, "saveReviewGradeForm", state);
                        	Assignment a = submission.getAssignment();
                        	if (a != null) {
                        		assignmentRef = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
                                if (assignmentRef != null) {
                                    String associateGradebookAssignment = a.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                                    // update grade in gradebook
                                    updateGradebookAssociation(state, siteId, assignmentRef, associateGradebookAssignment, submissionId, "update");
                                }
                            }
                        }
                    }
                    state.setAttribute(GRADE_SUBMISSION_DONE, Boolean.TRUE);
                    state.setAttribute(PEER_ASSESSMENT_REMOVED_STATUS, item.getRemoved());
                    //update session state:
                    List<PeerAssessmentItem> peerAssessmentItems = (List<PeerAssessmentItem>) state.getAttribute(PEER_ASSESSMENT_ITEMS);
                    if (peerAssessmentItems != null) {
                        for (int i = 0; i < peerAssessmentItems.size(); i++) {
                            PeerAssessmentItem sItem = peerAssessmentItems.get(i);
                            if (sItem.getId().getSubmissionId().equals(item.getId().getSubmissionId())
                                    && sItem.getId().getAssessorUserId().equals(item.getId().getAssessorUserId())) {
                                //found it, just update it
                                peerAssessmentItems.set(i, item);
                                state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, Object> stateToMap(SessionState state) {
        return state.getAttributeNames().stream().collect(Collectors.toMap(n -> n, n -> state.getAttribute(n)));
    }

    /**
     * Action is to release the grade to submission
     */
    public void doRelease_grade_submission(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        readGradeForm(data, state, "release");
        if (state.getAttribute(STATE_MESSAGE) == null) {
            grade_submission_option(data, AssignmentConstants.SUBMISSION_OPTION_RELEASE);
        }

    } // doRelease_grade_submission

    /**
     * Action is to return submission with or without grade
     */
    public void doReturn_grade_submission(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        readGradeForm(data, state, "return");
        if (state.getAttribute(STATE_MESSAGE) == null) {
            grade_submission_option(data, AssignmentConstants.SUBMISSION_OPTION_RETURN);
        }

    } // doReturn_grade_submission

    /**
     * Action is to return submission with or without grade from preview
     */
    public void doReturn_preview_grade_submission(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        grade_submission_option(data, AssignmentConstants.SUBMISSION_OPTION_RETURN);

    } // doReturn_grade_preview_submission

    /**
     * Action is to save submission with or without grade from preview
     */
    public void doSave_preview_grade_submission(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        grade_submission_option(data, AssignmentConstants.SUBMISSION_OPTION_SAVE);

    } // doSave_grade_preview_submission

    /**
     * Common grading routine plus specific operation to differenciate cases when saving, releasing or returning grade.
     */
    private void grade_submission_option(RunData data, String gradeOption) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        String sId = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
        AssignmentSubmission submission = getSubmission(sId, "grade_submission_option", state);

        Map<String, Object> options = stateToMap(state);

        // Add the rubrics params to the map, too
        ParameterParser params = data.getParameters();
        Iterable<String> iterable = () -> params.getNames();
        StreamSupport.stream(iterable.spliterator(), false).filter(n -> n.startsWith(RubricsConstants.RBCS_PREFIX)).forEach(n -> options.put(n, params.get(n)));

        List<String> alerts = new ArrayList<>();

        assignmentToolUtils.gradeSubmission(submission, gradeOption, options, alerts);
        alerts.forEach(a -> addAlert(state, a));

        String assignmentId = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);

        if (state.getAttribute(STATE_MESSAGE) == null) {
            // SAK-29314 - put submission information into state
            boolean viewSubsOnlySelected = stringToBool((String) data.getParameters().getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
            putSubmissionInfoIntoState(state, assignmentId, sId, viewSubsOnlySelected);

            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
            state.setAttribute(GRADE_SUBMISSION_DONE, Boolean.TRUE);
        } else {
            state.removeAttribute(GRADE_SUBMISSION_DONE);
        }

        // Remove any rubrics related state
        state.getAttributeNames().stream().filter(n -> n.startsWith(RubricsConstants.RBCS_PREFIX)).forEach(state::removeAttribute);

        // SAK-29314 - update the list being iterated over
        sizeResources(state);

    } // grade_submission_option

    /**
     * Action is to save the submission as a draft
     */
    public void doSave_submission(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        // save submission
        post_save_submission(data, false);
    } // doSave_submission

    /**
     * set the resubmission related properties in AssignmentSubmission object if they are missing
     *
     * @param assignment
     * @param submission
     */
    private void setResubmissionProperties(Assignment assignment, AssignmentSubmission submission) {
        Map<String, String> submissionProperties = submission.getProperties();

        if (StringUtils.isBlank(submissionProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER))) {
            Map<String, String> assignmentProperties = assignment.getProperties();
            String assignmentAllowResubmitNumber = assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);

            if (StringUtils.isNotBlank(assignmentAllowResubmitNumber)) {
                submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, assignmentAllowResubmitNumber);

                // if assignment's setting of resubmit close time is null, use assignment close time as the close time for resubmit
                String assignmentAllowResubmitCloseDate = StringUtils.isNotBlank(assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME))
                        ? assignmentProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME)
                        : String.valueOf(assignment.getCloseDate().toEpochMilli());

                submissionProperties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, assignmentAllowResubmitCloseDate);
            }
        }
    }

    /**
     * Action is to post the submission
     */
    public void doPost_submission(RunData data) {

        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }
        // post submission
        post_save_submission(data, true);

    }    // doPost_submission

    /**
     * Inner method used for post or save submission
     *
     * @param data
     * @param post
     */
    private void post_save_submission(RunData data, boolean post) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        String aReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        Assignment a = getAssignment(aReference, "post_save_submission", state);

        if (assignmentService.canSubmit(a)) {
            ParameterParser params = data.getParameters();
            // retrieve the submission text (as formatted text)
            boolean checkForFormattingErrors = true; // check formatting error whether the student is posting or saving
            String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT), checkForFormattingErrors);

            if (text == null) {
                text = (String) state.getAttribute(VIEW_SUBMISSION_TEXT);
            } else {
                state.setAttribute(VIEW_SUBMISSION_TEXT, text);
            }

            String timeSpent = params.getCleanString(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
            if(timeSpent == null) {
                timeSpent = (String)state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
            }

            state.setAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT, timeSpent);

            String saveSpent = timeSpent;
            String eulaAgreementYes = params.getString(SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
            if(StringUtils.isEmpty(eulaAgreementYes)) {
            		eulaAgreementYes = "false";
            }

            User u = (User) state.getAttribute(STATE_USER);
            User submitter = null;
            String studentId = params.get("submit_on_behalf_of");
            if (studentId != null && !studentId.equals("-1")) {
                // SAK-23817: return to the Assignments List by Student
                state.setAttribute(FROM_VIEW, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
                try {
                    submitter = u;
                    u = userDirectoryService.getUser(studentId);
                } catch (UserNotDefinedException ex1) {
                    log.warn("Unable to find user with ID [" + studentId + "]");
                    submitter = null;
                }
            }

            String instructor = null;
            instructor = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_INSTRUCTOR);
            if (instructor != null) { log.warn("Instructor using student view"); }

            String group_id = null;
            String original_group_id = null;

            if (a.getIsGroup()) {
                original_group_id =
                        (params.getString("originalGroup") == null || params.getString("originalGroup").trim().length() == 0) ? null : params.getString("originalGroup");

                if (original_group_id != null) {
                    state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, original_group_id);
                } else {
                    if (state.getAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP) != null) {
                        original_group_id = (String) state.getAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP);
                    } else {
                        state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, null);
					}
                }

                String[] groupChoice = params.getStrings("selectedGroups");

                if (groupChoice != null && groupChoice.length != 0) {
                    if (groupChoice.length > 1) {
                        state.setAttribute(VIEW_SUBMISSION_GROUP, null);
                        addAlert(state, rb.getString("java.alert.youchoosegroup"));
                    } else {
                        group_id = groupChoice[0];
                        state.setAttribute(VIEW_SUBMISSION_GROUP, groupChoice[0]);
                    }
                } else {
                    // get the submitted group id
                    if (state.getAttribute(VIEW_SUBMISSION_GROUP) != null) {
                        group_id = (String) state.getAttribute(VIEW_SUBMISSION_GROUP);
                    } else {
                        state.setAttribute(VIEW_SUBMISSION_GROUP, null);
                        addAlert(state, rb.getString("java.alert.youchoosegroup"));
                    }
                }
            }


            if (state.getAttribute(STATE_MESSAGE) == null) {
                if (StringUtils.isNotEmpty(params.getString(AssignmentConstants.SUBMISSION_REVIEW_CHECK_SERVICE_EULA_AGREEMENT))) {
                	boolean requireEulaForSubmission = serverConfigurationService.getBoolean("contentreview.submission.eula.required", false);
                    if (!Boolean.valueOf(eulaAgreementYes)) {
                    	//if the user hasn't seen the EULA alert or the EULA is required to submit the assignment, then show the user the alert
                    	if (state.getAttribute(CONTENT_REVIEW_EULA_REQUIRED_ALERTED) == null
                    			|| requireEulaForSubmission) {
                    		addAlert(state, rb.getFormattedMessage("youarenot21", contentReviewService.getServiceName()));
                    		state.setAttribute(CONTENT_REVIEW_EULA_REQUIRED_ALERTED, true);
                    	}
                    } else {
                    	state.removeAttribute(CONTENT_REVIEW_EULA_REQUIRED_ALERTED);
                    }
                    state.setAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT, eulaAgreementYes);
                }

                // SAK-26322
                List nonInlineAttachments = getNonInlineAttachments(state, a);
                Assignment.SubmissionType typeOfSubmission = a.getTypeOfSubmission();
                if (typeOfSubmission == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION && nonInlineAttachments.size() > 1) {
                    //Single uploaded file and there are multiple attachments
                    adjustAttachmentsToSingleUpload(data, state, a, nonInlineAttachments);
                }

                // clear text if submission type does not allow it
                if (typeOfSubmission == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION || typeOfSubmission == Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION) {
                    text = null;
                }

                // get attachment input and generate alert message according to assignment submission type
                checkSubmissionTextAttachmentInput(data, state, a, text, post);
            }
            if ((state.getAttribute(STATE_MESSAGE) == null) && (a != null)) {
                AssignmentSubmission submission;
                if (contentReviewService != null && Boolean.valueOf(eulaAgreementYes)) {
                		//update EULA agreement timestamp
                		contentReviewService.updateUserEULATimestamp(sessionManager.getCurrentSessionUserId());
                }

                if (a.getIsGroup()) {
                    String g = StringUtils.isNotBlank(original_group_id) ? original_group_id : group_id;
                    submission = getSubmission(aReference, g, "post_save_submission", state);
                } else {
                    submission = getSubmission(aReference, u, "post_save_submission", state);
                }

                if (submission != null) {
                    // the submission already exists, change the text and honor pledge value, post it
                    Map<String, String> properties = submission.getProperties();

                    if (a.getIsGroup()) {
                        if (StringUtils.isNotBlank(original_group_id) && !StringUtils.equals(original_group_id, group_id)) {
                            // changing group id so we need to check if a submission has already been made for that group
                            AssignmentSubmission submissioncheck = getSubmission(aReference, group_id, "post_save_submission", state);
                            if (submissioncheck != null) {
                                addAlert(state, rb.getString("group.already.submitted"));
                                log.warn("The group {} has already submitted {}!", group_id, submissioncheck.getId());
                            }
                        }
                        submission.setGroupId(group_id);
                    }

                    if (NumberUtils.isParsable(properties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER))) {
                        // if this submission has been already been submitted previously
                        if (post && submission.getDateSubmitted() != null) {
                            // decrease the allow_resubmit_number,
                            int number = Integer.parseInt(properties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER));
                            // minus 1 from the submit number, if the number is not -1 (not unlimited)
                            if (number >= 1) {
                                properties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, String.valueOf(number - 1));
                            }
                        }
                    } else {
                        setResubmissionProperties(a, submission);
                    }

                    // update submission info after resubmission which prevents updating resubmission count for the first submission
                    submission.setUserSubmission(true);
                    submission.setSubmittedText(text);
                    submission.setSubmitted(post);
                    // post differentiates a submission from saving a draft
                    if (post) submission.setDateSubmitted(Instant.now());

                    String currentUser = sessionManager.getCurrentSessionUserId();
                    // identify who the submittee is using the session
                    submission.getSubmitters().stream().filter(s -> s.getSubmitter().equals(currentUser)).findAny().ifPresent(s -> s.setSubmittee(true)); 
                    submission.getSubmitters().stream().filter(s -> s.getSubmitter().equals(currentUser)).findAny().ifPresent(s -> s.setTimeSpent(saveSpent));

                    // for resubmissions
                    // when resubmit, keep the Returned flag on till the instructor grade again.
                    // need this to handle feedback and comments, which we have to do even if ungraded
                    // get the previous graded date
                    String prevGradedDate = properties.get(AssignmentConstants.PROP_LAST_GRADED_DATE);
                    if (StringUtils.isBlank(prevGradedDate) && submission.getDateModified() != null) {
                        // since this is a newly added property, if no value is set, get the default as the submission last modified date
                        // this date is shown in the UI so we format the date and time using the system zone vs the users for consistency
                        prevGradedDate = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(ZoneId.systemDefault()).format(submission.getDateModified());
                        properties.put(AssignmentConstants.PROP_LAST_GRADED_DATE, prevGradedDate);
                    }

                    if (submission.getGraded() && submission.getReturned() && submission.getGradeReleased()) {

                        // add the current grade into previous grade histroy
                        String previousGrades = properties.get(ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES);
                        if (StringUtils.isBlank(previousGrades)) {
                            previousGrades = properties.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_GRADES);
                            if (StringUtils.isNotBlank(previousGrades)) {
                                if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                                    // point grade assignment type
                                    // some old unscaled grades, need to scale the number and remove the old property
                                    String[] grades = StringUtils.split(previousGrades, " ");
                                    String newGrades = "";

                                    String decSeparator = formattedText.getDecimalSeparator();

                                    for (String grade : grades) {
                                        if (!grade.contains(decSeparator)) {
                                            // show the grade with decimal point
                                            grade = grade.concat(decSeparator).concat("0");
                                        }
                                        newGrades = newGrades.concat(grade + " ");
                                    }
                                    previousGrades = newGrades;
                                }
                                properties.remove(ResourceProperties.PROP_SUBMISSION_PREVIOUS_GRADES);
                            } else {
                                previousGrades = "";
                            }
                        }

                        String displayGrade = assignmentService.getGradeDisplay(submission.getGrade(), a.getTypeOfGrade(), a.getScaleFactor());
                        if (StringUtils.isNotBlank(displayGrade)) {
                            previousGrades = "<h4>" + prevGradedDate + "</h4>" + "<div style=\"margin:0;padding:0\">" + displayGrade + "</div>" + previousGrades;
                            properties.put(ResourceProperties.PROP_SUBMISSION_SCALED_PREVIOUS_GRADES, previousGrades);
                        }

                        // clear the current grade and make the submission ungraded
                        submission.setGraded(false);
                        submission.setGradedBy(null);
                        submission.setGrade(null);
                        submission.setGradeReleased(false);

                    }

                    // following involves content, not grading, so always do on resubmit, not just if graded
                    if (StringUtils.isNotBlank(submission.getFeedbackText())) {
                        // keep the history of assignment feed back text
                        String feedbackTextHistory = StringUtils.trimToEmpty(properties.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT));
                        feedbackTextHistory = "<h4>" + prevGradedDate + "</h4>" + "<div style=\"margin:0;padding:0\">" + submission.getFeedbackText() + "</div>" + feedbackTextHistory;
                        properties.put(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT, feedbackTextHistory);
                    }

                    if (StringUtils.isNotBlank(submission.getFeedbackComment())) {
                        // keep the history of assignment feed back comment
                        String feedbackCommentHistory = StringUtils.trimToEmpty(properties.get(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT));
                        feedbackCommentHistory = "<h4>" + prevGradedDate + "</h4>" + "<div style=\"margin:0;padding:0\">" + submission.getFeedbackComment() + "</div>" + feedbackCommentHistory;
                        properties.put(ResourceProperties.PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT, feedbackCommentHistory);
                    }

                    // keep the history of assignment feed back comment
                    String feedbackAttachmentHistory = StringUtils.trimToEmpty(properties.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS));
                    Set<String> feedbackAttachments = submission.getFeedbackAttachments();
                    // use comma as separator for attachments
                    feedbackAttachmentHistory = StringUtils.join(feedbackAttachments, ",") + feedbackAttachmentHistory;

                    if (StringUtils.isNotBlank(feedbackAttachmentHistory)) {
                    	properties.put(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS, feedbackAttachmentHistory);
                    }

                    // reset the previous grading context
                    submission.setFeedbackText(null);
                    submission.setFeedbackComment(null);
                    submission.getFeedbackAttachments().clear();

                    // SAK-26322
                    if (a.getTypeOfSubmission() == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
                        List<Reference> nonInlineAttachments = getNonInlineAttachments(state, a);
                        //clear out inline attachments for content-review
                        //filter the attachments in the state to exclude inline attachments (nonInlineAttachments, is a subset of what's currently in the state)
                        state.setAttribute(ATTACHMENTS, nonInlineAttachments);
                    }
                } else {
                    // new submission
                    // if assignment is a group submission... send group id and not user id
                    String submitterId;
                    if (a.getIsGroup()) {
                        submitterId = group_id;
                    } else {
                        submitterId = u.getId();
                    }
                    try {
                        submission = assignmentService.addSubmission(a.getId(), submitterId);
                        if (submission == null) {
                            log.warn("NULL returned while attempting to add a submission to assignment : {}, for submitter: {}", a.getId(), submitterId);
                            addAlert(state, rb.getString("youarenot13"));
                            return;
                        }
                        submission.setUserSubmission(true);
                        submission.setSubmittedText(text);
                        submission.setSubmitted(post);
                        if (post) submission.setDateSubmitted(Instant.now());

                        submission.getSubmitters().stream().filter(s -> s.getSubmitter().equals(submitterId)).findAny().ifPresent(s -> s.setTimeSpent(saveSpent));
                        // set the resubmission properties
                        setResubmissionProperties(a, submission);
                    } catch (PermissionException e) {
                        log.warn("Could not add submission for assignment/submitter: {}/{}, {}", a.getId(), submitterId, e.toString());
                        addAlert(state, rb.getString("youarenot13"));
                        return;
                    }
                }

                // add attachments
                List<Reference> attachments = (List<Reference>) state.getAttribute(ATTACHMENTS);
                if (attachments != null) {
                    Set<String> submittedAttachments = submission.getAttachments();
                    submittedAttachments.clear();
                    if (a.getTypeOfSubmission() != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION) {
                        attachments.forEach(att -> submittedAttachments.add(att.getReference()));
                    }
                }

                Map<String, String> properties = submission.getProperties();

                if (submitter != null) {
                    properties.put(AssignmentConstants.SUBMITTER_USER_ID, submitter.getId());
                    state.setAttribute(STATE_SUBMITTER, u.getId());
                } else {
                    properties.remove(AssignmentConstants.SUBMITTER_USER_ID);
                }

                // SAK-26322 - add inline as an attachment for the content review service
                if (post && !isHtmlEmpty(text)) {
                    /* prepares a file representing the inline content;
                     * needed whether or not content review is used - it will be queued retroactively if we enable content review on the assignment in the future */
                    prepareInlineForContentReview(text, submission, state, u);
                }

                // submission log
                StringBuilder logEntry = new StringBuilder();
                logEntry.append(userTimeService.shortLocalizedTimestamp(Instant.now(), userTimeService.getLocalTimeZone(u.getId()), preferencesService.getLocale(u.getId())));
                boolean anonymousGrading = Boolean.parseBoolean(a.getProperties().get(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING));
                String subOrDraft = post ? rb.getString("listsub.submitted") : rb.getString("listsub.submitted.draft");
                if (!anonymousGrading) {
                    if (submitter != null && !submitter.getEid().equals(u.getEid())) {
                        logEntry.append(" ").append(submitter.getDisplayName())
                                .append(" (").append(submitter.getEid()).append(") ").append(subOrDraft)
                                .append(" ").append(rb.getString("listsub.submitted.on.behalf"))
                                .append(" ").append(u.getDisplayName()).append(" (").append(u.getEid()).append(")");
                    } else {
                        logEntry.append(" ").append(u.getDisplayName())
                                .append(" (").append(u.getEid()).append(") ")
                                .append(subOrDraft);
                    }
                }
                submission.getProperties().put(getNextSubmissionLogKey(submission), logEntry.toString());

                try {
                    assignmentService.updateSubmission(submission);
                } catch (PermissionException e) {
                    log.warn("Could not update submission: {}, {}", submission.getId(), e.getMessage());
                    addAlert(state, rb.getString("youarenot13"));
                    return;
                }

                // Check if we need to post the attachments
                if (post && a.getContentReview() && !submission.getAttachments().isEmpty()) {
                    assignmentService.postReviewableSubmissionAttachments(submission);
                }
            }

            if (state.getAttribute(STATE_MESSAGE) == null) {
                state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_SUBMISSION_CONFIRMATION);
            }
        } else {
            addAlert(state, rb.getString("stuviewsubm.theclodat"));
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        }
    } // post_save_submission

    /**
     * Takes the inline submission, prepares it as an attachment to the submission and queues the attachment with the content review service
     */
    private void prepareInlineForContentReview(String text, AssignmentSubmission submission, SessionState state, User student) {

        // Why does it need to remove the users submission?
        // If it needs to remove the submission it should first add a new one and only remove the old one if the new one was added successfully.

        //We will be replacing the inline submission's attachment
        //firstly, disconnect any existing attachments with AssignmentSubmission.PROP_INLINE_SUBMISSION set
        Set<String> attachments = submission.getAttachments();
        attachments.removeIf(attachment ->
        {
            Reference reference = entityManager.newReference(attachment);
            ResourceProperties referenceProperties = reference.getProperties();
            return "true".equals(referenceProperties.getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION));
        });

        //now prepare the new resource
        //provide lots of info for forensics - filename=InlineSub_assignmentId_userDisplayId_(for_studentDisplayId)_date.html
        User currentUser = userDirectoryService.getCurrentUser();
        String currentDisplayName = currentUser.getDisplayId();
        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
        SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        //avoid semicolons in filenames, right?
        dform.applyPattern("yyyy-MM-dd_HH-mm-ss");
        StringBuilder sb_resourceId = new StringBuilder("InlineSub_");
        String u = "_";
        sb_resourceId.append(submission.getAssignment().getId()).append(u).append(currentDisplayName).append(u);
        boolean isOnBehalfOfStudent = student != null && !student.equals(currentUser);
        if (isOnBehalfOfStudent) {
            // We're submitting on behalf of somebody
            sb_resourceId.append("for_").append(student.getDisplayId()).append(u);
        }
        sb_resourceId.append(dform.format(new Date()));

        String fileExtension = ".html";

		/*
		 * TODO: add and use a method in ContentHostingService to get the length of the ID of an attachment collection
		 * Attachment collections currently look like this:
		 * /attachment/dc126c4a-a48f-42a6-bda0-cf7b9c4c5c16/Assignments/eac7212a-9597-4b7d-b958-89e1c47cdfa7/
		 * See BaseContentService.addAttachmentResource for more information
		 */
        String toolName = "Assignments";
        // TODO: add and use a method in IdManager to get the maxUuidLength
        int maxUuidLength = 36;
        int esl = Entity.SEPARATOR.length();
        int attachmentCollectionLength = ContentHostingService.ATTACHMENTS_COLLECTION.length() + siteId.length() + esl + toolName.length() + esl + maxUuidLength + esl;
        int maxChars = ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH - attachmentCollectionLength - fileExtension.length() - 1;
        String resourceId = StringUtils.substring(sb_resourceId.toString(), 0, maxChars) + fileExtension;

        ResourcePropertiesEdit inlineProps = contentHostingService.newResourceProperties();
        inlineProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, rb.getString("submission.inline"));
        inlineProps.addProperty(ResourceProperties.PROP_DESCRIPTION, resourceId);
        inlineProps.addProperty(AssignmentConstants.PROP_INLINE_SUBMISSION, "true");

        //create a byte array input stream
        //text is almost in html format, but it's missing the start and ending tags
        //(Is this always the case? Does the content review service care?)
        String toHtml = "<html><head></head><body>" + text + "</body></html>";
        InputStream contentStream = new ByteArrayInputStream(toHtml.getBytes());

        String contentType = "text/html";

        //duplicating code from doAttachUpload. TODO: Consider refactoring into a method

        SecurityAdvisor sa = createSubmissionSecurityAdvisor();
        try {
            securityService.pushAdvisor(sa);
            ContentResource attachment = contentHostingService.addAttachmentResource(resourceId, siteId, toolName, contentType, contentStream, inlineProps);

            try {
                Reference ref = entityManager.newReference(contentHostingService.getReference(attachment.getId()));
                attachments.add(ref.getReference());
            } catch (Exception e) {
                log.warn(this + "prepareInlineForContentReview() cannot find reference for " + attachment.getId() + e.getMessage());
            }
        } catch (PermissionException e) {
            addAlert(state, rb.getString("notpermis4"));
        } catch (RuntimeException e) {
            if (contentHostingService.ID_LENGTH_EXCEPTION.equals(e.getMessage())) {
                addAlert(state, rb.getFormattedMessage("alert.toolong", resourceId));
            }
        } catch (ServerOverloadException e) {
            log.debug(this + ".prepareInlineForContentReview() ***** DISK IO Exception ***** " + e.getMessage());
            addAlert(state, rb.getString("failed.diskio"));
        } catch (Exception ignore) {
            log.debug(this + ".prepareInlineForContentReview() ***** Unknown Exception ***** " + ignore.getMessage());
            addAlert(state, rb.getString("failed"));
        } finally {
            securityService.popAdvisor(sa);
        }
    }

    /**
     * Used when students are selecting from a list of previous attachments for their single uploaded file
     */
    private void adjustAttachmentsToSingleUpload(RunData data, SessionState state, Assignment assignment, List nonInlineAttachments) {
        if (assignment == null || assignment.getTypeOfSubmission() != Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
            throw new IllegalArgumentException("adjustAttachmentsToSingleUpload called, but the assignment type is not Single Uploaded File");
        }
        if (nonInlineAttachments == null) {
            throw new IllegalArgumentException("adjustAttachmentsToSingleUpload called, but nonInlineAttachments is null");
        }

        String selection = data.getParameters().get("attachmentSelection");
        if ("newAttachment".equals(selection)) {
            Reference attachment = (Reference) state.getAttribute("newSingleUploadedFile");
            if (attachment == null) {
                // Try the newSingleAttachmentList
                List l = (List) state.getAttribute("newSingleAttachmentList");
                if (l != null && !l.isEmpty()) {
                    attachment = (Reference) l.get(0);
                }
            }
            if (attachment != null) {
                List attachments = entityManager.newReferenceList();
                attachments.add(attachment);
                state.setAttribute(ATTACHMENTS, attachments);
                state.removeAttribute("newSingleUploadedFile");
                state.removeAttribute("newSingleAttachmentList");
                state.removeAttribute(VIEW_SUBMISSION_TEXT);
            }
            // ^ if attachment is null, we don't care - checkSubmissionTextAttachmentInput() handles that for us
        } else {
            //they selected a previous attachment. selection represents an index in the nonInlineAttachments list
            boolean error = false;
            int index = -1;
            try {
                //get the selected attachment
                index = Integer.parseInt(selection);
                if (nonInlineAttachments.size() <= index) {
                    error = true;
                }
            } catch (NumberFormatException nfe) {
                error = true;
            }

            if (error) {
                log.warn("adjustAttachmentsToSingleUpload() - couldn't parse the selected index as an integer, or the selected index wasn't in the range of attachment indices");
                //checkSubmissionTextAttachmentInput() handles the alert message for us
            } else {
                Reference attachment = (Reference) nonInlineAttachments.get(index);
                //remove all the attachments from the state and add the selected one back for resubmission
                List attachments = (List) state.getAttribute(ATTACHMENTS);
                attachments.clear();
                attachments.add(attachment);
            }
        }
    }

    private void checkSubmissionTextAttachmentInput(RunData data,
                                                    SessionState state, Assignment assignment, String text, boolean post) {
        // SAK-26329 - determine if the submission has text
        boolean textIsEmpty = isHtmlEmpty(text);
        if (assignment != null) {
            // check the submission inputs based on the submission type
            Assignment.SubmissionType submissionType = assignment.getTypeOfSubmission();
            if (submissionType == Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION) {
                // for the inline only submission
                if (textIsEmpty) {
                    addAlert(state, rb.getString("youmust7"));
                }
            } else if (submissionType == Assignment.SubmissionType.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION) {
                // for the attachment only submission
                List<Reference> v = getNonInlineAttachments(state, assignment);
                if (v.isEmpty()) {
                    addAlert(state, rb.getString("youmust1"));
                }
            } else if (submissionType == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
                // for the single uploaded file only submission
                List<Reference> v = getNonInlineAttachments(state, assignment);
                if (v.size() != 1) {
                    addAlert(state, rb.getString("youmust8"));
                }
            } else if (submissionType == Assignment.SubmissionType.VIDEO_SUBMISSION) {
                // get the video from the form post
                List<Reference> v = post ? (List<Reference>) state.getAttribute(ATTACHMENTS) : this.getVideoAttachments(data, state, assignment);
                if ((v == null) || (v.size() != 1)) {
                    addAlert(state, rb.getString("youmust9"));
                }
                state.setAttribute(PREVIEW_SUBMISSION_ATTACHMENTS, v);
            } else {
                // for the inline and attachment submission / other submission types
                // There must be at least one thing submitted: inline text or at least one attachment
                List<Reference> v = getNonInlineAttachments(state, assignment);
                if (textIsEmpty && v.isEmpty()) {
                    addAlert(state, rb.getString("youmust2"));
                }
            }

            if (assignmentService.isTimeSheetEnabled(assignment.getContext())) {
                String timeSpent = (String)state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
                if (StringUtils.isNotBlank(assignment.getEstimate()) && assignment.getEstimateRequired()) {
                    if (StringUtils.isBlank(timeSpent)) {
                        addAlert(state, rb.getString("timeempty"));
                    } else if (!assignmentService.isValidTimeSheetTime(timeSpent)) {
                        addAlert(state, rb.getFormattedMessage("timeformat"));
                    }
                }
                if (StringUtils.isNotBlank(timeSpent) && !assignmentService.isValidTimeSheetTime(timeSpent)) {
                	addAlert(state, rb.getFormattedMessage("timeformat"));
                }
            }
        }
    }

    /**
     * When using content review, inline text gets turned into an attachment. This method returns all the attachments that do not represent inline text
     */
    private List<Reference> getNonInlineAttachments(SessionState state, Assignment assignment) {
        List<Reference> attachments = (List<Reference>) state.getAttribute(ATTACHMENTS);
        List<Reference> nonInlineAttachments = new ArrayList<>();
        nonInlineAttachments.addAll(attachments);
        if (assignment.getContentReview()) {
            for (Reference attachment : attachments) {
                if ("true".equals(attachment.getProperties().getProperty(AssignmentConstants.PROP_INLINE_SUBMISSION))) {
                    nonInlineAttachments.remove(attachment);
                }
            }
        }
        return nonInlineAttachments;
    }

    /**
     * SAK-26329 - Parses html and determines whether it contains printable characters.
     */
    private boolean isHtmlEmpty(String html) {
        return html == null || formattedText.stripHtmlFromText(html, false, true).isEmpty();
    }

    /**
     * Action is to confirm the submission and return to list view
     */
    public void doConfirm_assignment_submission(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        // SAK-23817 if the instructor submitted on behalf of the student, go back to Assignment List by Student
        String fromView = (String) state.getAttribute(FROM_VIEW);
        if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);
        } else {
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        }
        state.setAttribute(ATTACHMENTS, entityManager.newReferenceList());
    }

    /**
     * Action is to show the new assignment screen
     */
    public void doNew_assignment(RunData data, Context context) {

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        if (!alertGlobalNavigation(state, data)) {
			boolean allowAddAssignment = assignmentService.allowAddAssignment((String) state.getAttribute(STATE_CONTEXT_STRING));
			boolean allowUpdateAssignment = assignmentService.allowUpdateAssignmentInContext((String) state.getAttribute(STATE_CONTEXT_STRING));
            if (allowAddAssignment && allowUpdateAssignment) {
            	resetAssignment(state);
                initializeAssignment(state);

                state.setAttribute(ATTACHMENTS, entityManager.newReferenceList());
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);
            } else {
                addAlert(state, rb.getString("youarenot_addAssignment"));
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
            }

            // reset the global navigaion alert flag
            if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null) {
                state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
            }
        }

    } // doNew_Assignment

    /**
     * Action is to show the reorder assignment screen
     */
    public void doReorder(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // this insures the default order is loaded into the reordering tool
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

        if (!alertGlobalNavigation(state, data)) {
            if (assignmentService.allowAllGroups((String) state.getAttribute(STATE_CONTEXT_STRING))) {
                state.setAttribute(ATTACHMENTS, entityManager.newReferenceList());
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_REORDER_ASSIGNMENT);
            } else {
                addAlert(state, rb.getString("youarenot19"));
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
            }

            // reset the global navigaion alert flag
            if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null) {
                state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
            }
        }

    } // doReorder

    /**
     * Action is to save the input infos for assignment fields
     *
     * @param validify Need to validify the inputs or not
     */
    private void setNewAssignmentParameters(RunData data, boolean validify) {
        // read the form inputs
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String assignmentRef = params.getString("assignmentId");
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentRef).reckon().getId();

        // put the input value into the state attributes
        String title = params.getString(NEW_ASSIGNMENT_TITLE);
        state.setAttribute(NEW_ASSIGNMENT_TITLE, title);

        // put the input value into the state attributes
        try {
            Integer contentId = Integer.valueOf((String) params.getString(NEW_ASSIGNMENT_CONTENT_ID));
            state.setAttribute(NEW_ASSIGNMENT_CONTENT_ID, contentId);
        } catch (NumberFormatException e) {
            state.setAttribute(NEW_ASSIGNMENT_CONTENT_ID, null);
        }

        state.setAttribute(NEW_ASSIGNMENT_CONTENT_TITLE, (String) params.getString(NEW_ASSIGNMENT_CONTENT_TITLE));

        boolean contentLaunchNewWindow = params.getBoolean(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW);
		if ( contentLaunchNewWindow ) {
			state.setAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW, Boolean.TRUE);
		} else {
			state.setAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW, Boolean.FALSE);
		}

        String order = params.getString(NEW_ASSIGNMENT_ORDER);
        state.setAttribute(NEW_ASSIGNMENT_ORDER, order);

        String siteId = toolManager.getCurrentPlacement().getContext();

        boolean groupAssignment = rangeAndGroups.setNewOrEditedAssignmentParameters(data, state, siteId);

        if (StringUtils.isBlank(title)) {
            // empty assignment title
            addAlert(state, rb.getString("plespethe1"));
        } else if (sameAssignmentTitleInContext(assignmentRef, title, (String) state.getAttribute(STATE_CONTEXT_STRING))) {
            // assignment title already exist
            addAlert(state, rb.getFormattedMessage("same_assignment_title", title));
        }

        // open time
        Instant openTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_OPEN_MONTH, NEW_ASSIGNMENT_OPEN_DAY, NEW_ASSIGNMENT_OPEN_YEAR, NEW_ASSIGNMENT_OPEN_HOUR, NEW_ASSIGNMENT_OPEN_MIN, "newassig.opedat");

        // visible time
        if (serverConfigurationService.getBoolean("assignment.visible.date.enabled", false)) {
            if (params.get("allowVisibleDateToggle") == null) {
                state.setAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE, false);
            } else {
                putTimeInputInState(params, state, NEW_ASSIGNMENT_VISIBLE_MONTH, NEW_ASSIGNMENT_VISIBLE_DAY, NEW_ASSIGNMENT_VISIBLE_YEAR, NEW_ASSIGNMENT_VISIBLE_HOUR, NEW_ASSIGNMENT_VISIBLE_MIN, "newassig.visdat");
                state.setAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE, true);
            }

        }

        // due time
        Instant dueTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_DUE_MONTH, NEW_ASSIGNMENT_DUE_DAY, NEW_ASSIGNMENT_DUE_YEAR, NEW_ASSIGNMENT_DUE_HOUR, NEW_ASSIGNMENT_DUE_MIN, "gen.duedat");
        // show alert message when due date is in past. Remove it after user confirms the choice.
        if (dueTime.isBefore(Instant.now()) && state.getAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE) == null) {
            state.setAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE, Boolean.TRUE);
        } else {
            // clean the attribute after user confirm
            state.removeAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE);
        }
        if (state.getAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE) != null && validify) {
            addAlert(state, rb.getString("assig4"));
        }

        if (!dueTime.isAfter(openTime)) {
            addAlert(state, rb.getString("assig3"));
        }

        if (StringUtils.equalsIgnoreCase(params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE), Boolean.TRUE.toString())) {
            String timeSheet = params.getString(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE);
            if (StringUtils.isBlank(timeSheet)) {
                addAlert(state, rb.getString("timeempty"));
            } else if (!assignmentService.isValidTimeSheetTime(timeSheet)) {
                addAlert(state, rb.getFormattedMessage("timeformat"));
            }
        }

        state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.TRUE);
        if(params.getBoolean(NEW_ASSIGNMENT_REMINDER_EMAIL)){
            state.setAttribute(NEW_ASSIGNMENT_REMINDER_EMAIL, Boolean.TRUE);
        }else{
            state.setAttribute(NEW_ASSIGNMENT_REMINDER_EMAIL, Boolean.FALSE);
        }

        // close time
        Instant closeTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_CLOSE_MONTH, NEW_ASSIGNMENT_CLOSE_DAY, NEW_ASSIGNMENT_CLOSE_YEAR, NEW_ASSIGNMENT_CLOSE_HOUR, NEW_ASSIGNMENT_CLOSE_MIN, "date.closedate");
        if (closeTime != null && !closeTime.isAfter(openTime)) {
            addAlert(state, rb.getString("acesubdea3"));
        }
        if (closeTime != null && closeTime.isBefore(dueTime)) {
            addAlert(state, rb.getString("acesubdea2"));
        }

        // SECTION MOD
        String sections_string = "";
        String mode = (String) state.getAttribute(STATE_MODE);
        if (mode == null) mode = "";

        state.setAttribute(NEW_ASSIGNMENT_SECTION, sections_string);
        Assignment.SubmissionType submissionType = Assignment.SubmissionType.values()[params.getInt(NEW_ASSIGNMENT_SUBMISSION_TYPE)];
        state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, submissionType.ordinal());

        if (submissionType != Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION) {
            state.setAttribute(NEW_ASSIGNMENT_CONTENT_ID, null);
            state.setAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW, Boolean.FALSE);
        }

        // ------------------- BEGIN GRADING PARAMS -------------------

        // grade type and grade points
        boolean sendToGradebook = params.getBoolean(NEW_ASSIGNMENT_SEND_TO_GRADEBOOK);
        boolean gradeAssignment = params.getBoolean(NEW_ASSIGNMENT_GRADE_ASSIGNMENT);
        state.setAttribute(NEW_ASSIGNMENT_GRADE_ASSIGNMENT, gradeAssignment);
        int gType = gradeAssignment ? params.getInt(NEW_ASSIGNMENT_GRADE_TYPE) : Assignment.GradeType.UNGRADED_GRADE_TYPE.ordinal();
        Assignment.GradeType gradeType = Assignment.GradeType.values()[gType];
        state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, gradeType.ordinal());

        // check if grade type is switching and prompt for confirmation if graded submissions exist
        if (StringUtils.isNotBlank(assignmentRef)) {
            Assignment asn = getAssignment(assignmentRef, "setNewAssignmentParameters", state);
            if (asn != null && gradeType != Assignment.GradeType.GRADE_TYPE_NONE && asn.getTypeOfGrade() != gradeType) {
                state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE_SWITCHING, Boolean.TRUE);
                Set<AssignmentSubmission> submissions = assignmentService.getSubmissions(asn);
                if (submissions.stream().anyMatch(s -> s.getGraded())) {
                    if (state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE_SWITCH_CONFIRM) == null && validify) {
                        state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE_SWITCH_CONFIRM, Boolean.TRUE);
                        addAlert(state, rb.getString("grading.type.change.confirm"));
                    }
                    else {
                        state.removeAttribute(NEW_ASSIGNMENT_GRADE_TYPE_SWITCH_CONFIRM); // user has confirmed or we aren't validating
                    }
                }
            }
        }

        String grading = sendToGradebook ? params.getString(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) : GRADEBOOK_INTEGRATION_NO;

        // Currently, gradebook can only handle score types. Force GRADEBOOK_INTEGRATION_NO if the
        // grade type is anything other
        if (gradeType != Assignment.GradeType.SCORE_GRADE_TYPE) {
            grading = GRADEBOOK_INTEGRATION_NO;
        }

        state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, grading);

        state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, Boolean.valueOf(params.getString(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING)));

        // Skip category if it was never set.
        Long catInt = -1L;
        String newCategoryString = "-1";
        boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(siteId);

        if (params.getString(NEW_ASSIGNMENT_CATEGORY) != null && GRADEBOOK_INTEGRATION_ADD.equals(grading)) {
            String categorySelected = params.getString(NEW_ASSIGNMENT_CATEGORY);

            if (isGradebookGroupEnabled) {
                newCategoryString = categorySelected;
                state.setAttribute(NEW_ASSIGNMENT_CATEGORY, categorySelected);
            } else {
                catInt = Long.valueOf(categorySelected);
                state.setAttribute(NEW_ASSIGNMENT_CATEGORY, catInt);
            }
        } else {
            state.setAttribute(NEW_ASSIGNMENT_CATEGORY, catInt);
        }

        // only when choose to associate with assignment in Gradebook
        String associateAssignment = params.getString(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
        String gbSelector = params.getString(GB_SELECTOR);

        Map<String, Double> gradebookPointsMap = new HashMap<>();

        // FIXME Official bug: If we save an assignment and an error occurs that returns us to the form,
        // the fields must be pre-selected, including the single selector for gradebook items and categories.
        // Currently, if an error occurs in the task, the selector remains unfilled.
        if (grading != null && gradeType != Assignment.GradeType.UNGRADED_GRADE_TYPE) {
            if (grading.equals(GRADEBOOK_INTEGRATION_ASSOCIATE)) {
                if (isGradebookGroupEnabled) {
                    state.setAttribute(GB_SELECTOR, gbSelector);
                } else {
                    state.setAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, associateAssignment);
                }
            } else {
                if (isGradebookGroupEnabled) {
                    state.removeAttribute(GB_SELECTOR);
                } else {
                    state.removeAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                }
            }

            if (!grading.equals(GRADEBOOK_INTEGRATION_NO)) {
                // gradebook integration only available to point-grade assignment
                if (gradeType != Assignment.GradeType.SCORE_GRADE_TYPE) {
                    addAlert(state, rb.getString("addtogradebook.wrongGradeScale"));
                }
                // if chosen as "associate", have to choose one assignment from Gradebook
                String[] groupChoice = params.getStrings("selectedGroups");

                String associateGradebookAssignment = (String) state.getAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                if ((grading.equals(GRADEBOOK_INTEGRATION_ASSOCIATE) &&
                    ((isGradebookGroupEnabled && StringUtils.trimToNull(gbSelector) == null) ||
                    (!isGradebookGroupEnabled && StringUtils.trimToNull(associateGradebookAssignment) == null)))) {
                        addAlert(state, rb.getString("grading.associate.alert"));
                }

                switch (grading) {
                    case GRADEBOOK_INTEGRATION_ADD:
                        if (isGradebookGroupEnabled) {
                            String assignTo = params.getString("assignTo");

                            if (!assignTo.equals("individuals")) {
                                String categorySelected = params.getString(NEW_ASSIGNMENT_CATEGORY);
                                List<String> selectedCategories = Arrays.asList(categorySelected.split(","));

                                boolean areCategoriesInGroups =
                                    gradingService.checkMultiSelectorList(siteId,
                                        groupChoice != null ? Arrays.asList(groupChoice) : new ArrayList<>(), selectedCategories, true);

                                if (!areCategoriesInGroups) {
                                    addAlert(state, rb.getFormattedMessage("group.sitegradebook.categories.error"));
                                }
                            }

                            List<Gradebook> gbList = gradingService.getGradebookGroupInstances(siteId);
                            List<String> existingGradebookUids = gbList.stream().map(Gradebook::getUid).collect(Collectors.toList());

                            for (String gbUid : existingGradebookUids) {
                                gradingService.buildGradebookPointsMap(gbUid, siteId, assignmentRef, gradebookPointsMap, newCategoryString);
                            }
                        } else {
                            gradingService.buildGradebookPointsMap(siteId, siteId, assignmentRef, gradebookPointsMap, catInt.toString());
                        }
                        break;
                    case GRADEBOOK_INTEGRATION_ASSOCIATE:
                        if (isGradebookGroupEnabled) {
                            String assignTo = params.getString("assignTo");

                            if (!assignTo.equals("individuals")) {
                                List<String> gbItemList = Arrays.asList(gbSelector.split(","));

                                boolean areItemsInGroups =
                                    gradingService.checkMultiSelectorList(siteId,
                                        groupChoice != null ? Arrays.asList(groupChoice) : new ArrayList<>(), gbItemList, false);

                                if (!areItemsInGroups) {
                                    addAlert(state, rb.getFormattedMessage("group.sitegradebook.items.error"));
                                }
                            }
                        }

                        List<String> gradebookReferenceList = new ArrayList<>();

                        if (isGradebookGroupEnabled) {
                            List<String> gbItemList = Arrays.asList(gbSelector.split(","));

                            for (String gbItem : gbItemList) {
                                Long itemId = Long.parseLong(gbItem);

                                GradebookAssignment gradebookAssignment = gradingService.getGradebookAssigment(siteId, itemId);

                                String reference = gradebookAssignment.getExternallyMaintained() ?
                                    gradebookAssignment.getExternalId() : gradebookAssignment.getId().toString();

                                gradebookReferenceList.add(reference);
                            }
                        } else {
                            gradebookReferenceList.add(associateAssignment);
                        }

                        String associatedAssignmentTitles = "";
                        // check assignments from the site
                        for (Assignment a : assignmentService.getAssignmentsForContext(siteId)) {
                            if (assignmentId.equals(a.getId())) {
                                continue;
                            }

                            String selectedAssociateAssignment = a.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

                            for (String gradebookReference : gradebookReferenceList) {
                                if (selectedAssociateAssignment != null) {
                                    List<String> selectedAssociateAssignmentList = Arrays.asList(selectedAssociateAssignment.split(","));

                                    if (selectedAssociateAssignmentList.contains(gradebookReference)) {
                                        associatedAssignmentTitles += a.getTitle();
                                    }
                                }
                            }
                        }
                        if (StringUtils.isNotBlank(associatedAssignmentTitles) && state.getAttribute(NEW_ASSIGNMENT_PREVIOUSLY_ASSOCIATED) == null) {
                            state.setAttribute(NEW_ASSIGNMENT_PREVIOUSLY_ASSOCIATED, Boolean.TRUE);
                        } else {
                            // clean the attribute after user confirm
                            state.removeAttribute(NEW_ASSIGNMENT_PREVIOUSLY_ASSOCIATED);
                        }
                        if (state.getAttribute(NEW_ASSIGNMENT_PREVIOUSLY_ASSOCIATED) != null && validify) {
                            addAlert(state, rb.getString("addtogradebook.previouslyAssoc"));
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        String rubricId = params.getString(RubricsConstants.RBCS_LIST);
        if (StringUtils.isNotBlank(rubricId)) {
            Map<String, Object> rubricAssociationMap = new HashMap<>();
            rubricAssociationMap.put("rubricId", rubricId);
            Map<String, String> rubricAssociationParameters = new HashMap<>();
            rubricAssociationParameters.put("fineTunePoints", params.getString(RubricsConstants.RBCS_CONFIG + "fineTunePoints"));
            rubricAssociationParameters.put("hideStudentPreview", params.getString(RubricsConstants.RBCS_CONFIG + "hideStudentPreview"));
            rubricAssociationParameters.put("studentSelfReport", params.getString(RubricsConstants.RBCS_CONFIG + "studentSelfReport"));
            rubricAssociationParameters.put("studentSelfReportMode", params.getString("rbcs-multiple-options-config-studentSelfReportMode"));
            rubricAssociationMap.put("parameters", rubricAssociationParameters);
            try {
                state.setAttribute(RUBRIC_ASSOCIATION, (new ObjectMapper()).writeValueAsString(rubricAssociationMap));
            } catch (Exception e) {
                log.error("Failed to serialise rubrics parameters to JSON", e);
            }
        }

        state.setAttribute(TAG_SELECTOR, params.getString(TAG_SELECTOR));
        state.setAttribute(GB_SELECTOR, params.getString(GB_SELECTOR));

        //Peer Assessment
        boolean peerAssessment = false;
        if (gradeType == Assignment.GradeType.SCORE_GRADE_TYPE && params.getBoolean(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT)) {
            state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, Boolean.TRUE.toString());
            peerAssessment = true;
        } else {
            state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, Boolean.FALSE.toString());
        }

        if (peerAssessment) {
            //do not allow non-electronic assignments
            if (Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION == submissionType) {
                addAlert(state, rb.getString("peerassessment.invliadSubmissionTypeAssignment"));
            }

            Instant peerPeriodTime = putTimeInputInState(params, state, NEW_ASSIGNMENT_PEERPERIOD_MONTH, NEW_ASSIGNMENT_PEERPERIOD_DAY, NEW_ASSIGNMENT_PEERPERIOD_YEAR, NEW_ASSIGNMENT_PEERPERIOD_HOUR, NEW_ASSIGNMENT_PEERPERIOD_MIN, "newassig.opedat");
            Instant peerPeriodMinTime = Instant.from(closeTime.plus(Duration.ofMinutes(10)));
            //peer assessment must complete at a minimum of 10 mins after close time
            if (peerPeriodTime.isBefore(peerPeriodMinTime)) {
                addAlert(state, rb.getString("peerassessment.invliadPeriodTime"));
            }
        }

        state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL, params.getBoolean(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL));
        state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS, params.getBoolean(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS));
        if (peerAssessment) {
            if (params.get(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS) != null && !"".equals(params.get(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS))) {
                try {
                    int peerAssessmentNumOfReviews = Integer.parseInt(params.getString(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS));
                    if (peerAssessmentNumOfReviews > 0) {
                        state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS, Integer.valueOf(peerAssessmentNumOfReviews));
                    } else {
                        addAlert(state, rb.getString("peerassessment.invalidNumReview"));
                    }
                } catch (Exception e) {
                    addAlert(state, rb.getString("peerassessment.invalidNumReview"));
                }
            } else {
                addAlert(state, rb.getString("peerassessment.specifyNumReview"));
            }
        }

        String peerAssessmentInstructions = processFormattedTextFromBrowser(state, params.getString(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS), true);
        state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS, peerAssessmentInstructions);

        // -------------   END GRADING PARAMS (EXCEPT FOR POINTS AT THE END OF THIS METHOD)  ----------------------

        String b, r;
        //REVIEW SERVICE
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_USE_REVIEW_SERVICE);
        // set whether we use the review service or not
        if (r == null) b = Boolean.FALSE.toString();
        else {
            b = Boolean.TRUE.toString();
            if (state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE).equals(Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION)) {
                //can't use content-review with non-electronic submissions
                addAlert(state, rb.getFormattedMessage("review.switch.ne.1", contentReviewService.getServiceName()));
            }
        }
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_USE_REVIEW_SERVICE, b);

        //set whether students can view the review service results
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW, b);

        //set submit options
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO);
        if (r == null || (!AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD.equals(r) && !AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION.equals(r)))
            r = AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE;
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO, r);
        //set originality report options
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
        if (r == null || (!AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE.equals(r) && !AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY_AND_DUE.equals(r)))
            r = AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY;
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO, r);
        //set check repository options:
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN, b);

        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET, b);

        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB, b);

        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION, b);

        //exclude bibliographic materials:
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC, b);

        //exclude quoted materials:
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED, b);

        //exclude self plag
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG, b);

        //store inst index
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX, b);

        //student preview
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW);
        b = (r == null) ? Boolean.FALSE.toString() : Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW, b);

        //exclude small matches
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES);
        if (r == null) b = Boolean.FALSE.toString();
        else b = Boolean.TRUE.toString();
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES, b);

        //exclude type:
        //only options are 0=none, 1=words, 2=percentages
        r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE);
        if (!"0".equals(r) && !"1".equals(r) && !"2".equals(r)) {
            //this really shouldn't ever happen (unless someone's messing with the parameters)
            r = "0";
        }
        state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE, r);

        //exclude value
        if (!"0".equals(r)) {
            r = params.getString(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE);
            try {
                int rInt = Integer.parseInt(r);
                if (rInt < 0 || rInt > 100) {
                    addAlert(state, rb.getString("review.exclude.matches.value_error"));
                }
            } catch (Exception e) {
                addAlert(state, rb.getString("review.exclude.matches.value_error"));
            }
            state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE, r);
        } else {
            state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE, "1");
        }

        // treat the new assignment description as formatted text
        boolean checkForFormattingErrors = true; // instructor is creating a new assignment - so check for errors
        String description = processFormattedTextFromBrowser(state, params.getCleanString(NEW_ASSIGNMENT_DESCRIPTION), checkForFormattingErrors);
        state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, description);

        if (state.getAttribute(CALENDAR) != null || state.getAttribute(ADDITIONAL_CALENDAR) != null) {
            // calendar enabled for the site
            if (params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE) != null
                    && params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE).equalsIgnoreCase(Boolean.TRUE.toString())) {
                state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.TRUE.toString());
            } else {
                state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
            }
        } else {
            // no calendar yet for the site
            state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
        }

        if (params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE) != null
                && params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE)
                .equalsIgnoreCase(Boolean.TRUE.toString())) {
            state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.TRUE.toString());
            if (params.getString(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION) != null) {
                if (params.getString(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION)
                        .equalsIgnoreCase(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE)) {
                    state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
                } else if (params.getString(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION)
                        .equalsIgnoreCase(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW)) {
                    state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
                } else if (params.getString(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION)
                        .equalsIgnoreCase(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH)) {
                    state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
                }
            }
        } else {
            state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
            if (params.getString(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION) != null) {
                state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
            }
        }

        if (StringUtils.equalsIgnoreCase(params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE), Boolean.TRUE.toString())) {
            state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE, Boolean.TRUE);
            String isEstimateRequiredStr = params.getString(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED);
            state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED, StringUtils.equalsIgnoreCase(isEstimateRequiredStr, Boolean.TRUE.toString()));
            if (StringUtils.isNotBlank(params.getString(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE))) {
                state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE, params.getString(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE));
            }
        }else {
            state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE, Boolean.FALSE);
            state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED, Boolean.FALSE);
            state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE, StringUtils.EMPTY);
        }

        Boolean hdd = params.getBoolean(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE);
        state.setAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE, hdd);

        Boolean hp = params.getBoolean(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);
        Boolean autoSubmit = params.getBoolean(NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT);

        // set the honor pledge to be "no honor pledge"
        state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, hp);
        state.setAttribute(NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT, autoSubmit);

        Boolean ait = params.getBoolean(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS);
        state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS, ait);
        Boolean agt = params.getBoolean(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS);
        state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS, agt);

        List attachments = (List) state.getAttribute(ATTACHMENTS);
        if (attachments == null || attachments.isEmpty()) {
            // read from vm file
            String[] attachmentIds = data.getParameters().getStrings("attachments");
            if (attachmentIds != null && attachmentIds.length != 0) {
                attachments = new ArrayList();
                for (int i = 0; i < attachmentIds.length; i++) {
                    attachments.add(entityManager.newReference(attachmentIds[i]));
                }
            }
        }
        state.setAttribute(NEW_ASSIGNMENT_ATTACHMENT, attachments);

        if (validify) {
            if ((description == null) || (description.length() == 0) || ("<br/>".equals(description)) && ((attachments == null || attachments.size() == 0))) {
                // if there is no description nor an attachment, show the following alert message.
                // One could ignore the message and still post the assignment
                if (state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY) == null) {
                    state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY, Boolean.TRUE.toString());
                } else {
                    state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);
                }
            } else {
                state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);
            }
        }

        if (validify && state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY) != null) {
            addAlert(state, rb.getString("thiasshas"));
        }

        Integer assignmentType = params.getInt(NEW_ASSIGNMENT_SUBMISSION_TYPE);
        if ( assignmentType != null && assignmentType == 6 ) {
            Integer contentId = params.getInt(NEW_ASSIGNMENT_CONTENT_ID);
            if ( contentId < 1 ) {
                addAlert(state, rb.getString("pleaseselectlti"));
            }
        }

        // allow resubmission numbers
        if (params.getString("allowResToggle") != null && params.getString(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) != null) {
            // read in allowResubmit params
            Instant resubmitCloseTime = readAllowResubmitParams(params, state, null);
            if (resubmitCloseTime != null) {
                // check the date is valid
                if (!resubmitCloseTime.isAfter(openTime)) {
                    addAlert(state, rb.getString("acesubdea6"));
                }
                if (resubmitCloseTime.isBefore(dueTime)) {
                	addAlert(state, rb.getString("acesubdea7"));
                }
            }
        } else if (Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION != Assignment.SubmissionType.values()[(Integer) state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE)]) {
			/*
			 * SAK-26640: If the instructor switches to non-electronic by mistake, the resubmissions settings should persist so they can be easily retrieved.
			 * So we only reset resubmit params for electronic assignments.
			 */
            resetAllowResubmitParams(state);
        }

        // assignment notification option
        String notiOption = params.getString(ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS);
        if (notiOption != null) {
            state.setAttribute(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, notiOption);
        }

        // release grade notification option
        String releaseGradeOption = params.getString(ASSIGNMENT_RELEASEGRADE_NOTIFICATION);
        if (releaseGradeOption != null) {
            state.setAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, releaseGradeOption);
        }
        // release resubmission notification option
        String releaseResubmissionOption = params.getString(ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION);
        if (releaseResubmissionOption != null) {
            state.setAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, releaseResubmissionOption);
        }
        // read inputs for supplement items
        setNewAssignmentParametersSupplementItems(validify, state, params);

        String option = params.get("option");

        if (!"attach".equals(option)) {
            // the grade point
            String gradePoints = params.getString(NEW_ASSIGNMENT_GRADE_POINTS);
            state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, gradePoints);
            if (gradePoints != null && gradeType == Assignment.GradeType.SCORE_GRADE_TYPE) {
                if (gradePoints.length() == 0) {
                    // in case of point grade assignment, user must specify maximum grade point
                    addAlert(state, rb.getString("plespethe3"));
                } else {
                    Integer scaleFactor = assignmentService.getScaleFactor();
                    try {
                        if (StringUtils.isNotEmpty(assignmentRef)) {
                            Assignment assignment = assignmentService.getAssignment(assignmentId);
                            if (assignment != null && assignment.getScaleFactor() != null) {
                                scaleFactor = assignment.getScaleFactor();
                            }
                        }
                    } catch (IdUnusedException | PermissionException e) {
                        log.error(e.getMessage());
                    }

                    validPointGrade(state, gradePoints, scaleFactor);
                    // when scale is points, grade must be integer and less than maximum value
                    gradePoints = scalePointGrade(state, gradePoints, scaleFactor);
                    state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, gradePoints);

                    if (gradebookPointsMap.size() > 0) {
                        Double enteredPoints = Double.parseDouble(displayGrade(state, gradePoints, scaleFactor));
                        for (Map.Entry<String, Double> entry : gradebookPointsMap.entrySet()) {
                            Double gradebookCategoryPoints = entry.getValue();
                            if (!enteredPoints.equals(gradebookCategoryPoints)) {
                                addAlert(state, rb.getFormattedMessage("pleasee6", new Object[] {gradebookCategoryPoints.toString()}));
                            }
                        }
                    }
                }
            }
        }

    } // setNewAssignmentParameters

    /**
     * check to see whether there is already an assignment with the same title in the site
     *
     * @param assignmentRef
     * @param title
     * @param contextString
     * @return
     */
    private boolean sameAssignmentTitleInContext(String assignmentRef, String title, String contextString) {
        boolean rv = false;
        // in the student list view of assignments
        for (Assignment a : assignmentService.getAssignmentsForContext(contextString)) {
            if (assignmentRef == null || !assignmentRef.equals(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference())) {
                // don't do self-compare
                String aTitle = a.getTitle();
                if (aTitle != null && aTitle.length() > 0 && title.equals(aTitle)) {
                    //further check whether the assignment is marked as deleted or not
                    if (!a.getDeleted()) {
                        rv = true;
                    }
                }
            }
        }
        return rv;
    }

    /**
     * read inputs for supplement items
     *
     * @param validify
     * @param state
     * @param params
     */
    private void setNewAssignmentParametersSupplementItems(boolean validify,
                                                           SessionState state, ParameterParser params) {
        /********************* MODEL ANSWER ITEM *********************/
        String modelAnswer_to_delete = StringUtils.trimToNull(params.getString("modelanswer_to_delete"));
        if (modelAnswer_to_delete != null) {
            state.setAttribute(MODELANSWER_TO_DELETE, modelAnswer_to_delete);
        }
        String modelAnswer_text = StringUtils.trimToNull(params.getString("modelanswer_text"));
        if (modelAnswer_text != null) {
            state.setAttribute(MODELANSWER_TEXT, modelAnswer_text);
        }
        String modelAnswer_showto = StringUtils.trimToNull(params.getString("modelanswer_showto"));
        if (modelAnswer_showto != null) {
            state.setAttribute(MODELANSWER_SHOWTO, modelAnswer_showto);
        }
        if (modelAnswer_text != null || !"0".equals(modelAnswer_showto) || state.getAttribute(MODELANSWER_ATTACHMENTS) != null) {
            // there is Model Answer input
            state.setAttribute(MODELANSWER, Boolean.TRUE);

            if (validify && !"true".equalsIgnoreCase(modelAnswer_to_delete)) {
                // show alert when user didn't select show-to option
                if ("0".equals(modelAnswer_showto)) {
                    addAlert(state, rb.getString("modelAnswer.alert.showto"));
                }
            }
        } else {
            state.removeAttribute(MODELANSWER);
        }

        /**************** NOTE ITEM ********************/
        String note_to_delete = StringUtils.trimToNull(params.getString("note_to_delete"));
        if (note_to_delete != null) {
            state.setAttribute(NOTE_TO_DELETE, note_to_delete);
        }
        String note_text = StringUtils.trimToNull(params.getString("note_text"));
        if (note_text != null) {
            state.setAttribute(NOTE_TEXT, note_text);
        }
        String note_to = StringUtils.trimToNull(params.getString("note_to"));
        if (note_to != null) {
            state.setAttribute(NOTE_SHAREWITH, note_to);
        }
        if (note_text != null || !"0".equals(note_to)) {
            // there is Note Item input
            state.setAttribute(NOTE, Boolean.TRUE);

            if (validify && !"true".equalsIgnoreCase(note_to_delete)) {
                // show alert when there is no note text
                if (note_text == null) {
                    addAlert(state, rb.getString("note.alert.text"));
                }
                // show alert when there is no share option
                if ("0".equals(note_to)) {
                    addAlert(state, rb.getString("note.alert.to"));
                }
            }
        } else {
            state.removeAttribute(NOTE);
        }


        /****************** ALL PURPOSE ITEM **********************/
        String allPurpose_to_delete = StringUtils.trimToNull(params.getString("allPurpose_to_delete"));
        if (allPurpose_to_delete != null) {
            state.setAttribute(ALLPURPOSE_TO_DELETE, allPurpose_to_delete);
        }
        String allPurposeTitle = StringUtils.trimToNull(params.getString("allPurposeTitle"));
        if (allPurposeTitle != null) {
            state.setAttribute(ALLPURPOSE_TITLE, allPurposeTitle);
        }
        String allPurposeText = StringUtils.trimToNull(params.getString("allPurposeText"));
        if (allPurposeText != null) {
            state.setAttribute(ALLPURPOSE_TEXT, allPurposeText);
        }
        if (StringUtils.trimToNull(params.getString("allPurposeHide")) != null) {
            state.setAttribute(ALLPURPOSE_HIDE, Boolean.valueOf(params.getString("allPurposeHide")));
        }
        if (StringUtils.trimToNull(params.getString("allPurposeShowFrom")) != null) {
            state.setAttribute(ALLPURPOSE_SHOW_FROM, Boolean.valueOf(params.getString("allPurposeShowFrom")));
            // allpurpose release time
            putTimeInputInState(params, state, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN, "date.allpurpose.releasedate");
        } else {
            state.removeAttribute(ALLPURPOSE_SHOW_FROM);
        }
        if (StringUtils.trimToNull(params.getString("allPurposeShowTo")) != null) {
            state.setAttribute(ALLPURPOSE_SHOW_TO, Boolean.valueOf(params.getString("allPurposeShowTo")));
            // allpurpose retract time
            putTimeInputInState(params, state, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN, "date.allpurpose.retractdate");
        } else {
            state.removeAttribute(ALLPURPOSE_SHOW_TO);
        }

        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
        List<String> accessList = new ArrayList<String>();
        try {
            AuthzGroup realm = authzGroupService.getAuthzGroup(siteService.siteReference(siteId));
            Set<Role> roles = realm.getRoles();
            for (Iterator iRoles = roles.iterator(); iRoles.hasNext(); ) {
                // iterator through roles first
                Role role = (Role) iRoles.next();
                if (params.getString("allPurpose_" + role.getId()) != null) {
                    accessList.add(role.getId());
                } else {
                    // if the role is not selected, iterate through the users with this role
                    Set userIds = realm.getUsersHasRole(role.getId());
                    for (Iterator iUserIds = userIds.iterator(); iUserIds.hasNext(); ) {
                        String userId = (String) iUserIds.next();
                        if (params.getString("allPurpose_" + userId) != null) {
                            accessList.add(userId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn(this + ":setNewAssignmentParameters" + e.toString() + "error finding authzGroup for = " + siteId);
        }
        state.setAttribute(ALLPURPOSE_ACCESS, accessList);

        if (allPurposeTitle != null || allPurposeText != null || (accessList != null && !accessList.isEmpty()) || state.getAttribute(ALLPURPOSE_ATTACHMENTS) != null) {
            // there is allpupose item input
            state.setAttribute(ALLPURPOSE, Boolean.TRUE);

            if (validify && !"true".equalsIgnoreCase(allPurpose_to_delete)) {
                if (accessList == null || accessList.isEmpty()) {
                    // missing access choice
                    addAlert(state, rb.getString("allPurpose.alert.access"));
                }
            }
        } else {
            state.removeAttribute(ALLPURPOSE);
        }
    }

    /**
     * read time input and assign it to state attributes
     *
     * @param params
     * @param state
     * @param monthString
     * @param dayString
     * @param yearString
     * @param hourString
     * @param minString
     * @param invalidBundleMessage
     * @return
     */
    private Instant putTimeInputInState(ParameterParser params, SessionState state, String monthString, String dayString, String yearString, String hourString, String minString, String invalidBundleMessage) {
        String monthStr = params.getString(monthString);
        int month = monthStr != null ? Integer.valueOf(monthStr) : 0;
        state.setAttribute(monthString, month);

        String dayStr = params.getString(dayString);
        int day = dayStr != null ? Integer.valueOf(dayStr) : 0;
        state.setAttribute(dayString, day);

        String yearStr = params.getString(yearString);
        int year = yearStr != null ? Integer.valueOf(yearStr) : 0;
        state.setAttribute(yearString, year);

        String hourStr = params.getString(hourString);
        int hour = hourStr != null ? Integer.valueOf(hourStr) : 0;
        state.setAttribute(hourString, hour);

        String minStr = params.getString(minString);
        int min = minStr != null ? Integer.valueOf(minStr) : 0;
        state.setAttribute(minString, min);
        // validate date
        if (!DateFormatterUtil.checkDate(day, month, year)) {
            addAlert(state, rb.getFormattedMessage("date.invalid", rb.getString(invalidBundleMessage)));
        }
		try {
			return LocalDateTime.of(year, month, day, hour, min, 0).atZone(userTimeService.getLocalTimeZone().toZoneId()).toInstant();
		} catch (java.time.DateTimeException e) {
            addAlert(state, rb.getFormattedMessage("date.invalid", rb.getString(invalidBundleMessage)));
			return LocalDateTime.now().atZone(userTimeService.getLocalTimeZone().toZoneId()).toInstant();
		}
    }

    /**
     * Action is to hide the preview assignment student view
     */
    public void doHide_submission_assignment_instruction(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // save user input
        readGradeForm(data, state, "read");

    } // doHide_preview_assignment_student_view

    /**
     * Action is to hide the preview assignment student view
     */
    public void doHide_submission_assignment_instruction_review(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // save user input
        saveReviewGradeForm(data, state, "read");

    }

    public void doShow_submission_assignment_instruction_review(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }


        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // save user input
        saveReviewGradeForm(data, state, "read");
    }

    /**
     * Action is to show the preview assignment student view
     */
    public void doShow_submission_assignment_instruction(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // save user input
        readGradeForm(data, state, "read");

    } // doShow_submission_assignment_instruction

    /**
     * Action is to hide the preview assignment student view
     */
    public void doHide_preview_assignment_student_view(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, Boolean.TRUE);

    } // doHide_preview_assignment_student_view

    /**
     * Action is to show the preview assignment student view
     */
    public void doShow_preview_assignment_student_view(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, Boolean.FALSE);

    } // doShow_preview_assignment_student_view

    /**
     * Action is to hide the preview assignment assignment infos
     */
    public void doHide_preview_assignment_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, Boolean.TRUE);

    } // doHide_preview_assignment_assignment

    /**
     * Action is to show the preview assignment assignment info
     */
    public void doShow_preview_assignment_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, Boolean.FALSE);

    } // doShow_preview_assignment_assignment

    /**
     * Action is to hide the assignment content in the view assignment page
     */
    public void doHide_view_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, Boolean.TRUE);

    } // doHide_view_assignment

    /**
     * Action is to show the assignment content in the view assignment page
     */
    public void doShow_view_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, Boolean.FALSE);

    } // doShow_view_assignment

    /**
     * Action is to hide the student view in the view assignment page
     */
    public void doHide_view_student_view(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, Boolean.TRUE);

    } // doHide_view_student_view

    /**
     * Action is to show the student view in the view assignment page
     */
    public void doShow_view_student_view(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, Boolean.FALSE);

    } // doShow_view_student_view

    /**
     * Action is to post assignment
     */
    public void doPost_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        // post assignment
        post_save_assignment(data, "post");

    } // doPost_assignment

    /**
     * Action is to tag items via an items tagging helper
     */
    public void doHelp_items(RunData data) {
        SessionState state = ((JetspeedRunData) data)
                .getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        TaggingProvider provider = taggingManager.findProviderById(params.getString(PROVIDER_ID));

        String activityRef = params.getString(ACTIVITY_REF);

        TaggingHelperInfo helperInfo = provider.getItemsHelperInfo(activityRef);

        // get into helper mode with this helper tool
        startHelper(data.getRequest(), helperInfo.getHelperId());

        Map<String, ?> helperParms = helperInfo.getParameterMap();

        for (Map.Entry<String, ?> entry : helperParms.entrySet()) {
            state.setAttribute(entry.getKey(), entry.getValue());
        }
    } // doHelp_items

    /**
     * Action is to tag an individual item via an item tagging helper
     */
    public void doHelp_item(RunData data) {
        SessionState state = ((JetspeedRunData) data)
                .getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        TaggingProvider provider = taggingManager.findProviderById(params.getString(PROVIDER_ID));

        String itemRef = params.getString(ITEM_REF);

        TaggingHelperInfo helperInfo = provider.getItemHelperInfo(itemRef);

        // get into helper mode with this helper tool
        startHelper(data.getRequest(), helperInfo.getHelperId());

        Map<String, ? extends Object> helperParms = helperInfo.getParameterMap();

        for (Map.Entry<String, ? extends Object> entry : helperParms.entrySet()) {
            state.setAttribute(entry.getKey(), entry.getValue());
        }
    } // doHelp_item

    /**
     * Action is to tag an activity via an activity tagging helper
     */
    public void doHelp_activity(RunData data) {
        SessionState state = ((JetspeedRunData) data)
                .getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        TaggingProvider provider = taggingManager.findProviderById(params.getString(PROVIDER_ID));

        String activityRef = params.getString(ACTIVITY_REF);

        TaggingHelperInfo helperInfo = provider.getActivityHelperInfo(activityRef);

        // get into helper mode with this helper tool
        startHelper(data.getRequest(), helperInfo.getHelperId());

        Map<String, ?> helperParms = helperInfo.getParameterMap();

        for (Map.Entry<String, ?> entry : helperParms.entrySet()) {
            state.setAttribute(entry.getKey(), entry.getValue());
        }
    } // doHelp_activity

    /**
     * post or save assignment
     * TODO much of the logic in this method should be moved to the assignment service
     */
    private void post_save_assignment(RunData data, String postOrSave) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();

        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
        boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(siteId);

        boolean post = (postOrSave != null) && "post".equals(postOrSave);

        // assignment old title
        String aOldTitle;

        // assignment old access setting
        Assignment.Access aOldAccess = null;

        // assignment old group setting
        Collection<String> aOldGroups = new ArrayList<String>();

        // assignment old open date setting
        Instant oldOpenTime;

        // assignment old due date setting
        Instant oldDueTime;

        // assignment old close date setting
        Instant oldCloseTime = null;

        String mode = (String) state.getAttribute(STATE_MODE);
        if (!MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT.equals(mode)) {
            // read input data if the mode is not preview mode
            setNewAssignmentParameters(data, true);
        }

        String assignmentId = params.getString("assignmentId");

        // whether there is a change in the assignment resubmission choice
        boolean bool_change_resubmit_option = false;

        boolean switchingGradeType = state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE_SWITCHING) != null;

        // if there is a message at this point usually means there was some type of error
        if (StringUtils.isNotBlank((String) state.getAttribute(STATE_MESSAGE))) {
            return;
        }

        Assignment a;
        boolean newAssignment = false;
        // if there is no assignmentId
        if (StringUtils.isBlank(assignmentId)) {
            //  create a new assignment
            try {
                a = assignmentService.addAssignment(siteId);
                newAssignment = true;
            } catch (PermissionException e) {
                log.warn("Could not create new assignment for site: {}, {}", siteId, e.getMessage());
                addAlert(state, rb.getFormattedMessage("youarenot_editAssignment", siteId));
                return;
            }
        } else {
            // otherwise get the existing
            a = getAssignment(assignmentId, "post_save_assignment", state);
        }

        if (a == null) {
            log.warn("Could not create/retrieve assignment in/for site: {}, assignment is null", siteId);
            addAlert(state, rb.getFormattedMessage("theisno"));
        } else {
            Map<String, String> p = a.getProperties();

            if (propertyValueChanged(state, p, AssignmentConstants.ALLOW_RESUBMIT_NUMBER) || propertyValueChanged(state, p, AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME)) {
                bool_change_resubmit_option = true;
            }

            String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
            // put the names and values into vm file
            String title = (String) state.getAttribute(NEW_ASSIGNMENT_TITLE);
            String order = (String) state.getAttribute(NEW_ASSIGNMENT_ORDER);

            // open time
            Instant openTime = getTimeFromState(state, NEW_ASSIGNMENT_OPEN_MONTH, NEW_ASSIGNMENT_OPEN_DAY, NEW_ASSIGNMENT_OPEN_YEAR, NEW_ASSIGNMENT_OPEN_HOUR, NEW_ASSIGNMENT_OPEN_MIN);

            // visible time
            boolean enableVisibleTime = serverConfigurationService.getBoolean("assignment.visible.date.enabled", false);
            Instant visibleTime = null;
            if (enableVisibleTime) {
                if (((Boolean) state.getAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE)))
                    visibleTime = getTimeFromState(state, NEW_ASSIGNMENT_VISIBLE_MONTH, NEW_ASSIGNMENT_VISIBLE_DAY, NEW_ASSIGNMENT_VISIBLE_YEAR, NEW_ASSIGNMENT_VISIBLE_HOUR, NEW_ASSIGNMENT_VISIBLE_MIN);
            }

            // due time
            Instant dueTime = getTimeFromState(state, NEW_ASSIGNMENT_DUE_MONTH, NEW_ASSIGNMENT_DUE_DAY, NEW_ASSIGNMENT_DUE_YEAR, NEW_ASSIGNMENT_DUE_HOUR, NEW_ASSIGNMENT_DUE_MIN);

            boolean emailReminder = (Boolean) state.getAttribute(NEW_ASSIGNMENT_REMINDER_EMAIL);
            
            // close time
            Instant closeTime = dueTime;
            boolean enableCloseDate = (Boolean) state.getAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
            if (enableCloseDate) {
                closeTime = getTimeFromState(state, NEW_ASSIGNMENT_CLOSE_MONTH, NEW_ASSIGNMENT_CLOSE_DAY, NEW_ASSIGNMENT_CLOSE_YEAR, NEW_ASSIGNMENT_CLOSE_HOUR, NEW_ASSIGNMENT_CLOSE_MIN);
            }

            // sections
            String section = (String) state.getAttribute(NEW_ASSIGNMENT_SECTION);

            Assignment.SubmissionType submissionType = Assignment.SubmissionType.values()[(Integer) state.getAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE)];

            Assignment.GradeType gradeType = Assignment.GradeType.values()[(Integer) state.getAttribute(NEW_ASSIGNMENT_GRADE_TYPE)];

            String gradePoints = (String) state.getAttribute(NEW_ASSIGNMENT_GRADE_POINTS);

            Integer contentId = (Integer) state.getAttribute(NEW_ASSIGNMENT_CONTENT_ID);

            String contentTitle = (String) state.getAttribute(NEW_ASSIGNMENT_CONTENT_TITLE);

            Boolean contentLaunchNewWindow = (Boolean) state.getAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW);

            if (submissionType != Assignment.SubmissionType.EXTERNAL_TOOL_SUBMISSION) {
                contentId = null;
                contentLaunchNewWindow = Boolean.FALSE;
            }

            String description = (String) state.getAttribute(NEW_ASSIGNMENT_DESCRIPTION);

            String checkAddDueTime = state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE) != null ? (String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE) : null;
            Boolean hideDueDate = (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE);

            String checkAutoAnnounce = (String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);

            String valueOpenDateNotification = (String) state.getAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION);

            Boolean checkAddHonorPledge = (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);
            Boolean checkAutoSubmit = (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT);

            Boolean checkAddInstructorTags = state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS) != null ? (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS) : null;
            Boolean checkAddGroupTags = state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS) != null ? (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS) : null;

            RangeAndGroupsDelegate.RangeAndGroupSettings rangeAndGroupSettings = rangeAndGroups.postSaveAssignmentSettings(state, siteService, siteId);

            String addtoGradebook = GRADEBOOK_INTEGRATION_NO;
            if (((Boolean) state.getAttribute(NEW_ASSIGNMENT_GRADE_ASSIGNMENT))) {
                addtoGradebook = StringUtils.isNotBlank((String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK)) ? (String) state.getAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) : GRADEBOOK_INTEGRATION_NO;
            }

            if (gradeType != Assignment.GradeType.SCORE_GRADE_TYPE) {
                addtoGradebook = GRADEBOOK_INTEGRATION_NO;
            }

            List<String> selectedGradebookUids = new ArrayList<>();
            List<String> selectedGroups = rangeAndGroupSettings.groups.stream().map(Group::getId).collect(Collectors.toList());

            buildGradebookUidList(state, siteId, selectedGradebookUids, addtoGradebook, selectedGroups, isGradebookGroupEnabled);

            Map<String, String> gradebookCategoriesMap = new HashMap<>();
            Map<String, String> gradebookItemMap = new HashMap<>();

            if (GRADEBOOK_INTEGRATION_ADD.equals(addtoGradebook)) {
                if (isGradebookGroupEnabled) {
                    String categoriesString = (String) state.getAttribute(NEW_ASSIGNMENT_CATEGORY);

                    if (categoriesString == null || categoriesString.isBlank()) {
                        selectedGradebookUids.forEach(gbUid -> gradebookCategoriesMap.put(gbUid, "-1"));
                    } else {
                        List<String> selectedCategories = Arrays.asList(categoriesString.split(","));

                        for (String gbUid : selectedGradebookUids) {
                            List<CategoryDefinition> categoryDefinitions = gradingService.getCategoryDefinitions(gbUid, siteId);

                            String categoryId = categoryDefinitions.stream()
                                .filter(category -> selectedCategories.contains(category.getId().toString()))
                                .map(category -> category.getId().toString())
                                .findFirst()
                                .orElse("-1");

                            gradebookCategoriesMap.put(gbUid, categoryId);
                        }
                    }
                } else {
                    gradebookCategoriesMap.put(siteId,
                        state.getAttribute(NEW_ASSIGNMENT_CATEGORY) != null
                        ? ((Long) state.getAttribute(NEW_ASSIGNMENT_CATEGORY)).toString()
                        : "-1");
                }
            } else if (GRADEBOOK_INTEGRATION_ASSOCIATE.equals(addtoGradebook)) {
                if (isGradebookGroupEnabled) {
                    String itemString = (String) state.getAttribute(GB_SELECTOR);

                    if (itemString != null && StringUtils.isNotBlank(itemString)) {
                        List<String> itemList = Arrays.asList(itemString.split(","));

                        for (String item : itemList) {
                            Long itemId = Long.parseLong(item);

                            GradebookAssignment gradebookAssignment = gradingService.getGradebookAssigment(siteId, itemId);

                            String reference = gradebookAssignment.getExternallyMaintained() ?
                                gradebookAssignment.getExternalId() : gradebookAssignment.getId().toString();

                            gradebookItemMap.put(reference,
                                gradingService.getGradebookUidByAssignmentById(siteId, itemId));
                        }
                    }
                } else {
                    String associateGradebookAssignment = (String) state.getAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                    gradebookItemMap.put(associateGradebookAssignment, siteId);
                }
            }

            String allowResubmitNumber = state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) != null ? (String) state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) : null;

            Boolean checkAnonymousGrading = state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING) != null ? (Boolean) state.getAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING) : Boolean.FALSE;

            // SAK-26319 - we no longer clear the resubmit number for non electronic submissions; the instructor may switch to another submission type in the future

            //Peer Assessment
            boolean usePeerAssessment = "true".equalsIgnoreCase((String) state.getAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT));
            Instant peerPeriodTime = getTimeFromState(state, NEW_ASSIGNMENT_PEERPERIOD_MONTH, NEW_ASSIGNMENT_PEERPERIOD_DAY, NEW_ASSIGNMENT_PEERPERIOD_YEAR, NEW_ASSIGNMENT_PEERPERIOD_HOUR, NEW_ASSIGNMENT_PEERPERIOD_MIN);
            boolean peerAssessmentAnonEval = (Boolean) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL);
            boolean peerAssessmentStudentViewReviews = (Boolean) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS);
            int peerAssessmentNumReviews = 0;
            if (state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS) != null) {
                peerAssessmentNumReviews = (Integer) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS);
            }
            String peerAssessmentInstructions = (String) state.getAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS);

            //Review Service
            boolean useReviewService = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_USE_REVIEW_SERVICE));

            boolean allowStudentViewReport = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW));

            // If the assignment switched to non-electronic, we need to use some of the assignment's previous content-review settings.
            // This way, students will maintain access to their originality reports when appropriate.
            if (submissionType == Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                useReviewService = a.getContentReview();
                allowStudentViewReport = Boolean.valueOf(p.get(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW));
                allowResubmitNumber = null;
            }
            boolean checkIsEstimate = false;
            boolean checkEstimateRequired = false;
            String timeEstimate = "";
            if (BooleanUtils.toBoolean((Boolean) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE))) {
                checkIsEstimate = Boolean.TRUE;
                checkEstimateRequired = BooleanUtils.toBoolean((Boolean) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED));
                timeEstimate = StringUtils.isNotBlank((String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE)) ?
                        (String) state.getAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE) : "";
            }

            String submitReviewRepo = (String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO);
            String generateOriginalityReport = (String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
            boolean checkTurnitin = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN));
            boolean checkInternet = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET));
            boolean checkPublications = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB));
            boolean checkInstitution = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION));
            //exclude bibliographic materials
            boolean excludeBibliographic = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC));
            //exclude quoted materials
            boolean excludeQuoted = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED));
            //exclude self plag
            boolean excludeSelfPlag = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG));
            //store inst index
            boolean storeInstIndex = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX));
            //student preview
            boolean studentPreview = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW));
            //exclude small matches
            boolean excludeSmallMatches = "true".equalsIgnoreCase((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES));
            //exclude type 0=none, 1=words, 2=percentages
            int excludeType = 0;
            int excludeValue = 1;
            if (excludeSmallMatches) {
                try {
                    excludeType = Integer.parseInt((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE));
                    if (excludeType != 0 && excludeType != 1 && excludeType != 2) {
                        excludeType = 0;
                    }
                } catch (Exception e) {
                    //Numberformatexception
                }
                //exclude value
                try {
                    excludeValue = Integer.parseInt((String) state.getAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE));
                    if (excludeValue < 0 || excludeValue > 100) {
                        excludeValue = 1;
                    }
                } catch (Exception e) {
                    //Numberformatexception
                }
            }

            // the attachments
            List attachments = (List) state.getAttribute(NEW_ASSIGNMENT_ATTACHMENT);

            if ((state.getAttribute(STATE_MESSAGE) == null) && (a != null)) {
                aOldTitle = a.getTitle();

                aOldAccess = a.getTypeOfAccess();

                aOldGroups.addAll(a.getGroups());

                // old open time
                oldOpenTime = a.getOpenDate();
                // old due time
                oldDueTime = a.getDueDate();
                // old close time
                oldCloseTime = a.getCloseDate();

                // set the Assignment Properties object
                Map<String, String> aProperties = a.getProperties();
                String oAssociateGradebookAssignment = aProperties.get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                Instant resubmitCloseTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSE_MONTH, ALLOW_RESUBMIT_CLOSE_DAY, ALLOW_RESUBMIT_CLOSE_YEAR, ALLOW_RESUBMIT_CLOSE_HOUR, ALLOW_RESUBMIT_CLOSE_MIN);

                String gradebookItemKeys = gradebookItemMap.keySet().stream().collect(Collectors.joining(","));

                editAssignmentProperties(a, checkAddDueTime, checkAutoAnnounce,
                    addtoGradebook, gradebookItemKeys, allowResubmitNumber,
                    aProperties, post, resubmitCloseTime, checkAnonymousGrading);

                // Store category information in assignment properties for drafts
                // so it can be restored when editing the assignment later
                if (!post) {
                    // On draft saves, only persist category when adding to gradebook; otherwise remove to prevent stale state
                    if (GRADEBOOK_INTEGRATION_ADD.equals(addtoGradebook)) {
                        if (isGradebookGroupEnabled) {
                            String categoriesString = (String) state.getAttribute(NEW_ASSIGNMENT_CATEGORY);
                            if (categoriesString != null && !(categoriesString = categoriesString.trim()).isEmpty() && !"-1".equals(categoriesString)) {
                                aProperties.put(NEW_ASSIGNMENT_CATEGORY, categoriesString);
                            }
                            else {
                                aProperties.remove(NEW_ASSIGNMENT_CATEGORY);
                            }
                        } else {
                            Object categoryObj = state.getAttribute(NEW_ASSIGNMENT_CATEGORY);
                            String categoryStr = (categoryObj != null) ? categoryObj.toString().trim() : null;
                            if (categoryStr != null && !categoryStr.isEmpty() && !"-1".equals(categoryStr)) {
                                aProperties.put(NEW_ASSIGNMENT_CATEGORY, categoryStr);
                            }
                            else {
                                aProperties.remove(NEW_ASSIGNMENT_CATEGORY);
                            }
                        }
                    } else {
                        aProperties.remove(NEW_ASSIGNMENT_CATEGORY);
                    }
                } else if (post) {
                    // Remove category from properties when publishing, as it will be managed by gradebook
                    aProperties.remove(NEW_ASSIGNMENT_CATEGORY);
                }

                //TODO: ADD_DUE_DATE
                if (state.getAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION) != null) {
                    aProperties.put(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, (String) state.getAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION));
                }

                // the notification option
                if (state.getAttribute(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE) != null) {
                    aProperties.put(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, (String) state.getAttribute(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE));
                }

                // the release grade notification option
                if (state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE) != null) {
                    aProperties.put(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, (String) state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE));
                }

                if (state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE) != null) {
                    aProperties.put(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE, (String) state.getAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE));
                }

                if (serverConfigurationService.getBoolean("tagservice.enable.integrations", true) && assignmentService.allowAddTags(siteId)) {
                    aProperties.put(NEW_ASSIGNMENT_TAG_CREATOR, Boolean.toString(checkAddInstructorTags));
                    aProperties.put(NEW_ASSIGNMENT_TAG_GROUPS, Boolean.toString(checkAddGroupTags));
                }

                // persist the Assignment changes
                commitAssignment(state, post, a, assignmentReference, title, submissionType, useReviewService, allowStudentViewReport,
                        gradeType, gradePoints, description, checkAddHonorPledge, checkAutoSubmit, attachments, section, rangeAndGroupSettings.range,
                        visibleTime, openTime, dueTime, closeTime, hideDueDate, enableCloseDate, emailReminder, rangeAndGroupSettings.isGroupSubmit, rangeAndGroupSettings.groups,
                        usePeerAssessment, peerPeriodTime, peerAssessmentAnonEval, peerAssessmentStudentViewReviews, peerAssessmentNumReviews, peerAssessmentInstructions,
                        submitReviewRepo, generateOriginalityReport, checkTurnitin, checkInternet, checkPublications, checkInstitution, excludeBibliographic, excludeQuoted,
                        excludeSelfPlag, storeInstIndex, studentPreview, excludeType, excludeValue, contentId, contentLaunchNewWindow, checkIsEstimate, checkEstimateRequired, timeEstimate);

                //RUBRICS, Save the binding between the assignment and the rubric
                Map<String, String> rubricParams = getRubricConfigurationParameters(params, gradeType);
                if (!rubricParams.isEmpty()) {
                    rubricsService.saveRubricAssociation(AssignmentConstants.TOOL_ID, a.getId(), rubricParams);
                }

                if (serverConfigurationService.getBoolean("tagservice.enable.integrations", true) && assignmentService.allowAddTags(siteId)) {                
                    List<String> tagIds = new ArrayList<>();
                    if (state.getAttribute(TAG_SELECTOR) != null && StringUtils.trimToNull((String) state.getAttribute(TAG_SELECTOR)) != null) {
                        tagIds.addAll(Arrays.asList(((String) state.getAttribute(TAG_SELECTOR)).split(",")));
                    }
                    tagService.updateTagAssociations(a.getContext(), a.getId(), tagIds, true);
                }

                if (post) {
                    // we need to update the submission
                    if (switchingGradeType || bool_change_resubmit_option) {
                        Set<AssignmentSubmission> submissions = assignmentService.getSubmissions(a);
                        if (submissions != null) {
                            // assignment already exist and with submissions
                            for (AssignmentSubmission s : submissions) {
                                if (s != null) {
                                    Map<String, String> sProperties = s.getProperties();
                                    if (switchingGradeType) {
                                        // set the grade to be empty for now
                                        s.setGrade("");
                                        s.setGraded(false);
                                        s.setGradedBy(null);
                                        s.setGradeReleased(false);
                                        s.setReturned(false);
                                    }
                                    if (bool_change_resubmit_option) {
                                        String aAllowResubmitNumber = a.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
                                        if (aAllowResubmitNumber == null || aAllowResubmitNumber.length() == 0 || "0".equals(aAllowResubmitNumber)) {
                                            sProperties.remove(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
                                            sProperties.remove(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                                        } else {
                                            sProperties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, a.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER));
                                            sProperties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, a.getProperties().get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME));
                                        }
                                    }
                                    try {
                                        assignmentService.updateSubmission(s);
                                    } catch (PermissionException e) {
                                        log.warn("Could not update submission: {}, {}", s.getId(), e.getMessage());
                                    }
                                }
                            }
                        }

                    }

                } //if

                // save supplement item information
                saveAssignmentSupplementItem(state, params, siteId, a);

                // set default sorting
                setDefaultSort(state);

                if (state.getAttribute(STATE_MESSAGE) == null) {
                    // set the state navigation variables
                    state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
                    state.setAttribute(ATTACHMENTS, entityManager.newReferenceList());
                    resetAssignment(state);

                    // integrate with other tools only if the assignment is posted
                    if (post) {
                        // add the due date to schedule if the schedule exists
                        integrateWithCalendar(state, a, title, dueTime, checkAddDueTime, oldDueTime, aProperties);

                        // the open date been announced
                        integrateWithAnnouncement(state, aOldTitle, a, title, openTime, checkAutoAnnounce, valueOpenDateNotification, oldOpenTime);

                        // It should only be called once when updateAssignment has already been done
                        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT, assignmentReference, true));

                        // integrate with Gradebook
                        try {
                            if (GRADEBOOK_INTEGRATION_ADD.equals(addtoGradebook) && gradebookCategoriesMap.size() > 0) {
                                for (Map.Entry<String, String> entry : gradebookCategoriesMap.entrySet()) {
                                    String gbUid = entry.getKey();
                                    String categorieId = entry.getValue();

                                    initIntegrateWithGradebook(state, gbUid,
                                        aOldTitle, oAssociateGradebookAssignment,
                                        a, title, dueTime, gradeType, gradePoints,
                                        addtoGradebook, null,
                                        Long.parseLong(categorieId));
                                }
                            } else if (GRADEBOOK_INTEGRATION_ASSOCIATE.equals(addtoGradebook) && gradebookItemMap.size() > 0) {
                                for (Map.Entry<String, String> entry : gradebookItemMap.entrySet()) {
                                    String reference = entry.getKey();
                                    String gradebookUid = entry.getValue();

                                    initIntegrateWithGradebook(state, gradebookUid,
                                        aOldTitle, oAssociateGradebookAssignment,
                                        a, title, dueTime, gradeType, gradePoints,
                                        addtoGradebook, reference, -1L);
                                }
                            } else if (GRADEBOOK_INTEGRATION_NO.equals(addtoGradebook)) {
                                if (oAssociateGradebookAssignment != null) {
                                    List<String> itemList = Arrays.asList(oAssociateGradebookAssignment.split(","));
                                    for (String item : itemList) {
                                        List<String> gradebookUids = gradingService.getGradebookUidByExternalId(item);
                                        for (String gradebookUid : gradebookUids) {
                                            initIntegrateWithGradebook(state, gradebookUid, aOldTitle, oAssociateGradebookAssignment, a, title, dueTime, gradeType, gradePoints, addtoGradebook, null, -1);
                                        }
                                    }
                                }
                            }
                        } catch (AssignmentHasIllegalPointsException e) {
                            addAlert(state, rb.getString("addtogradebook.illegalPoints"));
                            log.warn(this + ":post_save_assignment " + e.getMessage());
                        }

                        // log event if there is a title update
                        if (!StringUtils.equals(aOldTitle, title)) {
                            // title changed
                            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_TITLE, assignmentReference, true));
                        }

                        if (!aOldAccess.equals(a.getTypeOfAccess())) {
                            //SAK-45992
                            if(openTime.isBefore(Instant.now())){
                                // site-group access setting changed
                                eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS, assignmentReference, true));
                            }
                        } else {
                            Collection aGroups = a.getGroups();
                            if (!(aOldGroups == null && aGroups == null)
                                    && !(aOldGroups != null && aGroups != null && aGroups.containsAll(aOldGroups) && aOldGroups.containsAll(aGroups))) {
                                //SAK-45992
                                if(openTime.isBefore(Instant.now())){
                                    //group changed
                                    eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS, assignmentReference, true));
                                }
                            }
                        }


                        if (oldOpenTime != null && !oldOpenTime.equals(a.getOpenDate())) {
                            //cancel not fired event
                            eventTrackingService.cancelDelays(assignmentReference, AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT);
                            // open time change
                            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_OPENDATE, assignmentId, true));
                            if (a.getOpenDate().isAfter(Instant.now())) {
                                eventTrackingService.delay(eventTrackingService.newEvent(AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT, assignmentReference,
                                        true), openTime);
                            }
                        }

                        if (oldOpenTime != null && !oldOpenTime.equals(a.getOpenDate())) {
                            // open time change
                            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_OPENDATE, assignmentReference, true));
                        }

                        if (oldDueTime != null && !oldDueTime.equals(a.getDueDate())) {
                            // due time change
                            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_DUEDATE, assignmentReference, true));
                        }

                        if (oldCloseTime != null && !oldCloseTime.equals(a.getCloseDate())) {
                            // due time change
                            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_CLOSEDATE, assignmentReference, true));
                        }
                    }
                }
            }

            if ((newAssignment && !a.getDraft()) || (!a.getDraft() && !newAssignment)) {
                // If already open
                if (openTime.isBefore(Instant.now())) {
                    // post new assignment event since it is fully initialized by now
                    eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ADD_ASSIGNMENT, assignmentReference, true));
                } else {
                    // Not open yet, delay the event
                    eventTrackingService.delay(eventTrackingService.newEvent(AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT, assignmentReference, true), openTime);
                }
            }
        }
    }

    /**
     * supplement item related information
     *
     * @param state
     * @param params
     * @param siteId
     * @param assignment
     */
    private void saveAssignmentSupplementItem(SessionState state,
                                              ParameterParser params, String siteId, Assignment assignment) {
        // assignment supplement items
        String aId = assignment.getId();
        //model answer
        if (state.getAttribute(MODELANSWER_TO_DELETE) != null && "true".equals((String) state.getAttribute(MODELANSWER_TO_DELETE))) {
            // to delete the model answer
            AssignmentModelAnswerItem mAnswer = assignmentSupplementItemService.getModelAnswer(aId);
            if (mAnswer != null) {
                assignmentSupplementItemService.cleanAttachment(mAnswer);
                mAnswer.setAttachmentSet(new HashSet<>());
                assignmentSupplementItemService.removeModelAnswer(mAnswer);
            }
        } else if (state.getAttribute(MODELANSWER_TEXT) != null) {
            // edit/add model answer
            AssignmentModelAnswerItem mAnswer = assignmentSupplementItemService.getModelAnswer(aId);
            if (mAnswer == null) {
                mAnswer = assignmentSupplementItemService.newModelAnswer();
                mAnswer.setAssignmentId(aId);
                assignmentSupplementItemService.saveModelAnswer(mAnswer);
            }
            mAnswer.setText((String) state.getAttribute(MODELANSWER_TEXT));
            mAnswer.setShowTo(state.getAttribute(MODELANSWER_SHOWTO) != null ? Integer.parseInt((String) state.getAttribute(MODELANSWER_SHOWTO)) : 0);
            mAnswer.setAttachmentSet(getAssignmentSupplementItemAttachment(state, mAnswer, MODELANSWER_ATTACHMENTS));
            assignmentSupplementItemService.saveModelAnswer(mAnswer);
        }
        // note
        if (state.getAttribute(NOTE_TO_DELETE) != null && "true".equals((String) state.getAttribute(NOTE_TO_DELETE))) {
            // to remove note item
            AssignmentNoteItem nNote = assignmentSupplementItemService.getNoteItem(aId);
            if (nNote != null)
                assignmentSupplementItemService.removeNoteItem(nNote);
        } else if (state.getAttribute(NOTE_TEXT) != null) {
            // edit/add private note
            AssignmentNoteItem nNote = assignmentSupplementItemService.getNoteItem(aId);
            if (nNote == null)
                nNote = assignmentSupplementItemService.newNoteItem();
            nNote.setAssignmentId(assignment.getId());
            nNote.setNote((String) state.getAttribute(NOTE_TEXT));
            nNote.setShareWith(state.getAttribute(NOTE_SHAREWITH) != null ? Integer.parseInt((String) state.getAttribute(NOTE_SHAREWITH)) : 0);
            nNote.setCreatorId(userDirectoryService.getCurrentUser().getId());
            assignmentSupplementItemService.saveNoteItem(nNote);
        }
        // all purpose
        if (state.getAttribute(ALLPURPOSE_TO_DELETE) != null && "true".equals((String) state.getAttribute(ALLPURPOSE_TO_DELETE))) {
            // to remove allPurpose item
            AssignmentAllPurposeItem nAllPurpose = assignmentSupplementItemService.getAllPurposeItem(aId);
            if (nAllPurpose != null) {
                assignmentSupplementItemService.cleanAttachment(nAllPurpose);
                nAllPurpose.setAttachmentSet(new HashSet<>());
                assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurpose);
                nAllPurpose.setAccessSet(new HashSet<>());
                assignmentSupplementItemService.removeAllPurposeItem(nAllPurpose);
            }
        } else if (state.getAttribute(ALLPURPOSE_TITLE) != null) {
            // edit/add allPurpose item
            AssignmentAllPurposeItem nAllPurpose = assignmentSupplementItemService.getAllPurposeItem(aId);
            if (nAllPurpose == null) {
                nAllPurpose = assignmentSupplementItemService.newAllPurposeItem();
                nAllPurpose.setAssignmentId(assignment.getId());
                nAllPurpose.setHide(false);//SAK-33681
                assignmentSupplementItemService.saveAllPurposeItem(nAllPurpose);
            }
            nAllPurpose.setTitle((String) state.getAttribute(ALLPURPOSE_TITLE));
            nAllPurpose.setText((String) state.getAttribute(ALLPURPOSE_TEXT));

            boolean allPurposeShowFrom = state.getAttribute(ALLPURPOSE_SHOW_FROM) != null ? (Boolean) state.getAttribute(ALLPURPOSE_SHOW_FROM) : false;
            boolean allPurposeShowTo = state.getAttribute(ALLPURPOSE_SHOW_TO) != null ? (Boolean) state.getAttribute(ALLPURPOSE_SHOW_TO) : false;
            boolean allPurposeHide = state.getAttribute(ALLPURPOSE_HIDE) != null ? (Boolean) state.getAttribute(ALLPURPOSE_HIDE) : false;
            nAllPurpose.setHide(allPurposeHide);
            // save the release and retract dates
            if (allPurposeShowFrom && !allPurposeHide) {
                // save release date
                Instant releaseTime = getTimeFromState(state, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN);
                nAllPurpose.setReleaseDate(Date.from(releaseTime));
            } else {
                nAllPurpose.setReleaseDate(null);
            }
            if (allPurposeShowTo && !allPurposeHide) {
                // save retract date
                Instant retractTime = getTimeFromState(state, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN);
                nAllPurpose.setRetractDate(Date.from(retractTime));
            } else {
                nAllPurpose.setRetractDate(null);
            }
            nAllPurpose.setAttachmentSet(getAssignmentSupplementItemAttachment(state, nAllPurpose, ALLPURPOSE_ATTACHMENTS));

            // clean the access list first
            if (state.getAttribute(ALLPURPOSE_ACCESS) != null) {
                // get the access settings
                List<String> accessList = (List<String>) state.getAttribute(ALLPURPOSE_ACCESS);

                assignmentSupplementItemService.cleanAllPurposeItemAccess(nAllPurpose);
                Set<AssignmentAllPurposeItemAccess> accessSet = new HashSet<AssignmentAllPurposeItemAccess>();
                try {
                    AuthzGroup realm = authzGroupService.getAuthzGroup(siteService.siteReference(siteId));
                    Set<Role> roles = realm.getRoles();
                    for (Iterator iRoles = roles.iterator(); iRoles.hasNext(); ) {
                        // iterator through roles first
                        Role r = (Role) iRoles.next();
                        if (accessList.contains(r.getId())) {
                            AssignmentAllPurposeItemAccess access = assignmentSupplementItemService.newAllPurposeItemAccess();
                            access.setAccess(r.getId());
                            access.setAssignmentAllPurposeItem(nAllPurpose);
                            assignmentSupplementItemService.saveAllPurposeItemAccess(access);
                            accessSet.add(access);
                        } else {
                            // if the role is not selected, iterate through the users with this role
                            Set userIds = realm.getUsersHasRole(r.getId());
                            for (Iterator iUserIds = userIds.iterator(); iUserIds.hasNext(); ) {
                                String userId = (String) iUserIds.next();
                                if (accessList.contains(userId)) {
                                    AssignmentAllPurposeItemAccess access = assignmentSupplementItemService.newAllPurposeItemAccess();
                                    access.setAccess(userId);
                                    access.setAssignmentAllPurposeItem(nAllPurpose);
                                    assignmentSupplementItemService.saveAllPurposeItemAccess(access);
                                    accessSet.add(access);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn(this + ":post_save_assignment " + e.toString() + "error finding authzGroup for = " + siteId);
                }
                nAllPurpose.setAccessSet(accessSet);
            }
            assignmentSupplementItemService.saveAllPurposeItem(nAllPurpose);
        }
    }

    private Set<AssignmentSupplementItemAttachment> getAssignmentSupplementItemAttachment(SessionState state, AssignmentSupplementItemWithAttachment mItem, String attachmentString) {
        Set<AssignmentSupplementItemAttachment> sAttachments = new HashSet<AssignmentSupplementItemAttachment>();
        List<String> attIdList = assignmentSupplementItemService.getAttachmentListForSupplementItem(mItem);
        if (state.getAttribute(attachmentString) != null) {
            List currentAttachments = (List) state.getAttribute(attachmentString);
            for (Iterator aIterator = currentAttachments.iterator(); aIterator.hasNext(); ) {
                Reference attRef = (Reference) aIterator.next();
                String attRefId = attRef.getReference();
                // if the attachment is not exist, add it into db
                if (!attIdList.contains(attRefId)) {
                    AssignmentSupplementItemAttachment mAttach = assignmentSupplementItemService.newAttachment();
                    mAttach.setAssignmentSupplementItemWithAttachment(mItem);
                    mAttach.setAttachmentId(attRefId);
                    assignmentSupplementItemService.saveAttachment(mAttach);
                    sAttachments.add(mAttach);
                }
            }
        }
        return sAttachments;
    }

    /**
     * Get the rubrics params and includes all them in a HashMap to pass them to the rubrics service.
     * @param params
     * @return
     */
    private Map<String,String> getRubricConfigurationParameters(ParameterParser params, Assignment.GradeType gradeType){

        Map<String,String> parametersHash = new HashMap<>();
        //Get the parameters
        Iterator it2 = params.getNames();
        while (it2.hasNext()) {
            String name = it2.next().toString();
            if (name.startsWith("rbcs")) {
                parametersHash.put(name,params.getString(name));
            }
        }

        // if the grading type is not points (score) remove the rubric association regardless of the posted rubric params
        if (gradeType != Assignment.GradeType.SCORE_GRADE_TYPE && "1".equals(parametersHash.get("rbcs-associate"))) {
            parametersHash.put("rbcs-associate", "0");
        }

        return parametersHash;
    }

    /**
     * whether the resubmit option has been changed
     *
     * @param state
     * @param properties
     * @return
     */
    private boolean change_resubmit_option(SessionState state, Map<String, String> properties) {
        if (properties != null) {
            return propertyValueChanged(state, properties, AssignmentConstants.ALLOW_RESUBMIT_NUMBER) || propertyValueChanged(state, properties, AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
        }
        return false;
    }

    /**
     * whether there is a change between state variable and object's property value
     *
     * @param state
     * @param properties
     * @param propertyName
     * @return
     */
    private boolean propertyValueChanged(SessionState state, Map<String, String> properties, String propertyName) {
        String o_property_value = properties.get(propertyName);
        String n_property_value = state.getAttribute(propertyName) != null ? (String) state.getAttribute(propertyName) : null;
        if ((o_property_value == null && n_property_value != null)
                || (o_property_value != null && n_property_value == null)
                || (o_property_value != null && !o_property_value.equals(n_property_value))) {
            // there is a change
            return true;
        }
        return false;
    }

    /**
     * default sorting
     */
    private void setDefaultSort(SessionState state) {
        state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());
    }

    /**
     * Add submission objects if necessary for non-electronic type of assignment
     *
     * @param state
     * @param submissions
     * @param addSubmissionForUsers
     * @param assignment
     */
    private void addRemoveSubmissionsForNonElectronicAssignment(SessionState state, List submissions, HashSet<String> addSubmissionForUsers, HashSet<String> removeSubmissionForUsers, Assignment assignment) {
        // create submission object for those user who doesn't have one yet
        for (String userId : addSubmissionForUsers) {
            try {
                User u = userDirectoryService.getUser(userId);
                // only include those users that can submit to this assignment
                if (u != null) {
                    // construct fake submissions for grading purpose
                    AssignmentSubmission submission = assignmentService.addSubmission(assignment.getId(), userId);
                    if (submission != null) {
                        submission.setDateSubmitted(Instant.now());
                        submission.setSubmitted(true);
                        submission.setUserSubmission(false);
                        submission.setAssignment(assignment);
                        assignmentService.updateSubmission(submission);
                    }
                }
            } catch (Exception e) {
                log.warn("Cannot add submission for assignment: {}, userId: {}, {}", assignment.getId(), userId, e.getMessage());
            }
        }

        // remove submission object for those who no longer in the site
        for (String userId : removeSubmissionForUsers) {
            try {
                User user = userDirectoryService.getUser(userId);
                AssignmentSubmission submission = assignmentService.getSubmission(assignment.getId(), user);
                if (submission != null) {
                    assignmentService.removeSubmission(submission);
                }
            } catch (Exception e) {
                log.warn("Cannot remove submission for assignment: {}, userId: {}, {}", assignment.getId(), userId, e.getMessage());
                addAlert(state, rb.getFormattedMessage("youarenot_removeSubmission", assignment.getId()));
            }
        }
    }

    private void initIntegrateWithGradebook(SessionState state, String gradebookUid, String aOldTitle, String oAssociateGradebookAssignment, Assignment assignment, String title, Instant dueTime, Assignment.GradeType gradeType, String gradePoints, String addtoGradebook, String associateGradebookAssignment, long category) {

        String context = (String) state.getAttribute(STATE_CONTEXT_STRING);
        String assignmentReference = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
        String addUpdateRemoveAssignment = "remove";
        if (!addtoGradebook.equals(GRADEBOOK_INTEGRATION_NO)) {
            // if integrate with Gradebook
            if (addtoGradebook.equals(GRADEBOOK_INTEGRATION_ADD)) {
                addUpdateRemoveAssignment = GRADEBOOK_INTEGRATION_ADD;
            } else if (addtoGradebook.equals(GRADEBOOK_INTEGRATION_ASSOCIATE)) {
                addUpdateRemoveAssignment = "update";
            }

            if (!"remove".equals(addUpdateRemoveAssignment) && gradeType == Assignment.GradeType.SCORE_GRADE_TYPE) {
                try {
                    addAlerts(state, assignmentToolUtils.integrateGradebook(stateToMap(state), gradebookUid, assignmentReference, associateGradebookAssignment, addUpdateRemoveAssignment, title, Integer.parseInt(gradePoints), dueTime, null, null, category));

                    // add all existing grades, if any, into Gradebook
                    addAlerts(state, assignmentToolUtils.integrateGradebook(stateToMap(state), gradebookUid, assignmentReference, associateGradebookAssignment, null, null, -1, null, null, "update", category));

                    // if the assignment has been assoicated with a different entry in gradebook before, remove those grades from the entry in Gradebook
                    if (StringUtils.trimToNull(oAssociateGradebookAssignment) != null) {
                        List<String> oldGradebookAssignmentList = Arrays.asList(oAssociateGradebookAssignment.split(","));

                        String reference = associateGradebookAssignment;

                        boolean isExternalAssignmentDefined = gradingService.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment);

                        if (isExternalAssignmentDefined) {
                            org.sakaiproject.grading.api.Assignment gAssignment = gradingService.getExternalAssignment(gradebookUid, reference);

                            reference = gAssignment.getExternallyMaintained() ? gAssignment.getExternalId() : gAssignment.getId().toString();
                        } else {
                            String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
                            Long itemId = Long.parseLong(reference);

                            GradebookAssignment gradebookAssignment = gradingService.getGradebookAssigment(siteId, itemId);
                            reference = gradebookAssignment.getExternallyMaintained() ? gradebookAssignment.getExternalId() : gradebookAssignment.getId().toString();
                        }

                        boolean gradebookAssignmentExists = oldGradebookAssignmentList.contains(reference);

                        if (!gradebookAssignmentExists) {
                            // remove all previously associated grades, if any, into Gradebook
                            addAlerts(state, assignmentToolUtils.integrateGradebook(stateToMap(state), gradebookUid, assignmentReference, oAssociateGradebookAssignment, null, null, -1, null, null, "remove", category));

                            // if the old associated assignment entry in GB is an external one, but doesn't have anything assoicated with it in Assignment tool, remove it
                            assignmentToolUtils.removeNonAssociatedExternalGradebookEntry(context, assignmentReference, oAssociateGradebookAssignment, gradebookUid);
                        }
                    }
                } catch (NumberFormatException nE) {
                    alertInvalidPoint(state, gradePoints, assignment.getScaleFactor());
                    log.warn(this + ":initIntegrateWithGradebook " + nE.getMessage());
                }
            } else {
                addAlerts(state, assignmentToolUtils.integrateGradebook(stateToMap(state), gradebookUid, assignmentReference, associateGradebookAssignment, "remove", null, -1, null, null, null, category));
            }
        } else {
            // remove all previously associated grades, if any, into Gradebook
            addAlerts(state, assignmentToolUtils.integrateGradebook(stateToMap(state), gradebookUid, assignmentReference, oAssociateGradebookAssignment, null, null, -1, null, null, "remove", category));

            // need to remove the associated gradebook entry if 1) it is external and 2) no other assignment are associated with it
            assignmentToolUtils.removeNonAssociatedExternalGradebookEntry(context, assignmentReference, oAssociateGradebookAssignment, gradebookUid);
        }
    }

    private void integrateWithAnnouncement(SessionState state, String aOldTitle, Assignment assignment, String title, Instant openTime, String checkAutoAnnounce, String valueOpenDateNotification, Instant oldOpenTime) {
        if (checkAutoAnnounce.equalsIgnoreCase(Boolean.TRUE.toString())) {
            AnnouncementChannel channel = (AnnouncementChannel) state.getAttribute(ANNOUNCEMENT_CHANNEL);
            if (channel != null) {
                // whether the assignment's title or open date has been updated
                boolean updatedTitle = false;
                boolean updatedOpenDate = false;
                boolean updateAccess = false;

                String openDateAnnounced = StringUtils.trimToNull(assignment.getProperties().get(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
                String openDateAnnouncementId = StringUtils.trimToNull(assignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
                if (openDateAnnounced != null && openDateAnnouncementId != null) {
                    AnnouncementMessage message = null;

                    try {
                        message = channel.getAnnouncementMessage(openDateAnnouncementId);
                        if (!message.getAnnouncementHeader().getSubject().contains(title))/*whether title has been changed*/ {
                            updatedTitle = true;
                        }
                        if (!message.getBody().contains(assignmentService.getUsersLocalDateTimeString(openTime))) /*whether open date has been changed*/ {
                            updatedOpenDate = true;
                        }
                        if ((message.getAnnouncementHeader().getAccess().equals(MessageHeader.MessageAccess.CHANNEL) && !assignment.getTypeOfAccess().equals(Assignment.Access.SITE))
                                || (!message.getAnnouncementHeader().getAccess().equals(MessageHeader.MessageAccess.CHANNEL) && assignment.getTypeOfAccess().equals(Assignment.Access.SITE))) {
                            updateAccess = true;
                        } else if (assignment.getTypeOfAccess() == Assignment.Access.GROUP) {
                            Collection<String> assnGroups = assignment.getGroups();
                            Collection<String> anncGroups = message.getAnnouncementHeader().getGroups();
                            if (!assnGroups.equals(anncGroups)) {
                                updateAccess = true;
                            }
                        }
                    } catch (IdUnusedException | PermissionException e) {
                        log.warn(this + ":integrateWithAnnouncement " + e.getMessage());
                    }

                    if (updateAccess) {
                        try {
                            // if the access level has changed in assignment, remove the original announcement
                            channel.removeAnnouncementMessage(message.getId());
                        } catch (PermissionException e) {
                            log.warn("PermissionException for remove message id={} for assignment id={}, {}", message.getId(), assignment.getId(), e.getMessage());
                        }
                    }
                }

                // need to create announcement message if assignment is added or assignment has been updated
                if (openDateAnnounced == null || updatedTitle || updatedOpenDate || updateAccess) {
                    try {
                        AnnouncementMessageEdit message = channel.addAnnouncementMessage();
                        if (message != null) {
                            AnnouncementMessageHeaderEdit header = message.getAnnouncementHeaderEdit();

                            // add assignment id into property, to facilitate assignment lookup in Annoucement tool
                            message.getPropertiesEdit().addProperty("assignmentReference", AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());

                            header.setDraft(/* draft */false);
                            header.replaceAttachments(/* attachment */entityManager.newReferenceList());

                            if (openDateAnnounced == null) {
                                // making new announcement
                                header.setSubject(/* subject */rb.getFormattedMessage("assig6", title));
                            } else {
                                // updated title
                                header.setSubject(/* subject */rb.getFormattedMessage("assig5", title));
                            }

                            String formattedOpenTime = userTimeService.dateTimeFormat(openTime, FormatStyle.MEDIUM, FormatStyle.LONG);
                            if (updatedOpenDate) {
                                // revised assignment open date
                                message.setBody(/* body */ "<p>" + rb.getFormattedMessage("newope", formattedText.convertPlaintextToFormattedText(title), formattedOpenTime) + "</p>");
                            } else {
                                // assignment open date
                                message.setBody(/* body */ "<p>" + rb.getFormattedMessage("opedat", formattedText.convertPlaintextToFormattedText(title), formattedOpenTime) + "</p>");
                            }

                            // group information
                            if (assignment.getTypeOfAccess().equals(Assignment.Access.GROUP)) {
                                try {
                                    // get the group ids selected
                                    Collection groupRefs = assignment.getGroups();

                                    // make a collection of Group objects
                                    Collection groups = new ArrayList();

                                    //make a collection of Group objects from the collection of group ref strings
                                    Site site = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
                                    for (Iterator iGroupRefs = groupRefs.iterator(); iGroupRefs.hasNext(); ) {
                                        String groupRef = (String) iGroupRefs.next();
                                        groups.add(site.getGroup(groupRef));
                                    }

                                    // set access
                                    header.setGroupAccess(groups);
                                } catch (Exception exception) {
                                    // log
                                    log.warn(this + ":integrateWithAnnouncement " + exception.getMessage());
                                }
                            } else {
                                // site announcement
                                header.clearGroupAccess();
                            }

                            // save notification level if this is a future notification message
                            int notiLevel = NotificationService.NOTI_NONE;
                            String notification = "n";
                            if (AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW.equals(valueOpenDateNotification)) {
                                notiLevel = NotificationService.NOTI_OPTIONAL;
                                notification = "o";
                            } else if (AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH.equals(valueOpenDateNotification)) {
                                notiLevel = NotificationService.NOTI_REQUIRED;
                                notification = "r";
                            }

                            Instant now = Instant.now();
                            if (openDateAnnounced != null && now.isBefore(oldOpenTime)) {
                                message.getPropertiesEdit().addProperty("notificationLevel", notification);
                                message.getPropertiesEdit().addPropertyToList("noti_history", now.toString() + "_" + notiLevel + "_" + openDateAnnounced);
                            } else {
                                message.getPropertiesEdit().addPropertyToList("noti_history", now.toString() + "_" + notiLevel);
                            }

                            channel.commitMessage(message, notiLevel, "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc");
                        }

                        // commit related properties into Assignment object
                        assignment.getProperties().put(NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED, Boolean.TRUE.toString());
                        if (message != null) {
                            assignment.getProperties().put(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID, message.getId());
                        }
                        assignmentService.updateAssignment(assignment);

                    } catch (PermissionException ee) {
                        log.warn(this + ":IntegrateWithAnnouncement " + rb.getString("cannotmak"));
                    }
                }
            }
        } // if
    }

    private void integrateWithCalendar(SessionState state, Assignment assignment, String title, Instant dueTime, String checkAddDueTime, Instant oldDueTime, Map<String, String> properties) {
        // Integrate with Sakai calendar tool
        Calendar c = (Calendar) state.getAttribute(CALENDAR);

        integrateWithCalendarTool(state, assignment, title, dueTime, checkAddDueTime,
                oldDueTime, properties, c, ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);

        // Integrate with additional calendar tool if deployed.
        Calendar additionalCal = (Calendar) state.getAttribute(ADDITIONAL_CALENDAR);

        if (additionalCal != null) {
            integrateWithCalendarTool(state, assignment, title, dueTime, checkAddDueTime,
                    oldDueTime, properties, additionalCal, ResourceProperties.PROP_ASSIGNMENT_DUEDATE_ADDITIONAL_CALENDAR_EVENT_ID);
        }
    }

    // Checks to see if due date event in assignment properties exists on the calendar.
    // If so, remove it and then add a new due date event to the calendar. Then update assignment property
    // with new event id.
    private void integrateWithCalendarTool(SessionState state, Assignment assignment, String title, Instant dueTime, String checkAddDueTime, Instant oldDueTime, Map<String, String> properties, Calendar c, String dueDateProperty) {
        if (c == null) {
            return;
        }
        String dueDateScheduled = assignment.getProperties().get(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED);
        String oldEventId = properties.get(dueDateProperty);
        CalendarEvent e = null;

        if (dueDateScheduled != null || oldEventId != null) {
            // find the old event
            boolean found = false;
            if (oldEventId != null) {
                try {
                    e = c.getEvent(oldEventId);
                    found = true;
                } catch (IdUnusedException ee) {
                    log.warn(this + ":integrateWithCalendarTool The old event has been deleted: event id=" + oldEventId + ". " + c.getClass().getName());
                } catch (PermissionException ee) {
                    log.warn(this + ":integrateWithCalendarTool You do not have the permission to view the schedule event id= "
                            + oldEventId + ". " + c.getClass().getName());
                }
            } else {
                Instant startTime = LocalDateTime.of(oldDueTime.get(ChronoField.YEAR), oldDueTime.get(ChronoField.MONTH_OF_YEAR), oldDueTime.get(ChronoField.DAY_OF_MONTH), 0, 0, 0).toInstant(ZoneOffset.UTC);
                Instant endTime = LocalDateTime.of(oldDueTime.get(ChronoField.YEAR), oldDueTime.get(ChronoField.MONTH_OF_YEAR), oldDueTime.get(ChronoField.DAY_OF_MONTH), 23, 59, 59).toInstant(ZoneOffset.UTC);
                try {
                    Iterator events = c.getEvents(timeService.newTimeRange(timeService.newTime(startTime.toEpochMilli()), timeService.newTime(endTime.toEpochMilli())), null).iterator();

                    while ((!found) && (events.hasNext())) {
                        e = (CalendarEvent) events.next();
                        if (e.getDisplayName().contains(rb.getString("gen.assig") + " " + title)) {
                            found = true;
                        }
                    }
                } catch (PermissionException ignore) {
                    // ignore PermissionException
                }
            }
            if (found) {
                removeOldEvent(title, c, e);
            }

        }

        if (checkAddDueTime.equalsIgnoreCase(Boolean.TRUE.toString())) {
            updateAssignmentWithEventId(state, assignment, title, dueTime, c, dueDateProperty);
        }
    }

    /**
     * Add event to calendar and then persist the event id to the assignment properties
     *
     * @param state
     * @param assignment      Assignment
     * @param title           Event title
     * @param dueTime         Assignment due date/time
     * @param c               Calendar
     * @param dueDateProperty Property name specifies the appropriate calendar
     */
    private void updateAssignmentWithEventId(SessionState state, Assignment assignment, String title, Instant dueTime, Calendar c, String dueDateProperty) {
        CalendarEvent e;
        // commit related properties into Assignment object
        if (assignment != null) {
            try {
                e = null;
                CalendarEvent.EventAccess eAccess = CalendarEvent.EventAccess.SITE;
                List<Group> eGroups = new ArrayList<>();

                if (assignment.getTypeOfAccess().equals(Assignment.Access.GROUP)) {
                    eAccess = CalendarEvent.EventAccess.GROUPED;
                    Collection<String> groupRefs = assignment.getGroups();

                    // make a collection of Group objects from the collection of group ref strings
                    Site site = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
                    for (String groupRef : groupRefs) {
                        Group group = site.getGroup(groupRef);
                        if (group != null) eGroups.add(group);
                    }
                }
                String formattedDueTime = userTimeService.dateTimeFormat(dueTime, FormatStyle.MEDIUM, FormatStyle.LONG);
                e = c.addEvent(/* TimeRange */timeService.newTimeRange(dueTime.toEpochMilli(), 0),
						/* title */rb.getString("gen.due") + " " + title,
			       /* description */rb.getFormattedMessage("assign_due_event_desc", title, formattedDueTime),
						/* type */"Deadline",
						/* location */"",
						/* access */ eAccess,
						/* groups */ eGroups,
						/* attachments */null /*SAK-27919 do not include assignment attachments.*/);

                assignment.getProperties().put(NEW_ASSIGNMENT_DUE_DATE_SCHEDULED, Boolean.TRUE.toString());
                if (e != null) {
                    assignment.getProperties().put(dueDateProperty, e.getId());

                    // edit the calendar object and add an assignment id field
                    addAssignmentIdToCalendar(assignment, c, e);
                }
                // TODO do we care if the event is null?

            } catch (IdUnusedException ee) {
                log.warn(this + ":updateAssignmentWithEventId " + ee.getMessage());
            } catch (PermissionException ee) {
                log.warn(this + ":updateAssignmentWithEventId " + rb.getString("cannotfin1"));
            } catch (Exception ee) {
                log.warn(this + ":updateAssignmentWithEventId " + ee.getMessage());
            }
            // try-catch


            try {
                assignmentService.updateAssignment(assignment);
            } catch (PermissionException e1) {
                log.warn("Cannot update assignment, {}", e1.getMessage());
            }
        }
    }

    // Persist the assignment id to the calendar
    private void addAssignmentIdToCalendar(Assignment assignment, Calendar c, CalendarEvent e) throws IdUnusedException, PermissionException, InUseException {

        if (c != null && e != null && assignment != null) {
            CalendarEventEdit edit = c.getEditEvent(e.getId(), org.sakaiproject.calendar.api.CalendarService.EVENT_ADD_CALENDAR);

            edit.setField(CalendarConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID, assignment.getId());
            edit.setField(CalendarConstants.EVENT_OWNED_BY_TOOL_ID, AssignmentConstants.TOOL_ID);
            edit.setField(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED, assignmentService.getUsersLocalDateTimeString(assignment.getOpenDate()));

            c.commitEvent(edit);
        }
    }

    // Remove an existing event from the calendar
    private void removeOldEvent(String title, Calendar c, CalendarEvent e) {
        // remove the found old event
        if (c != null && e != null) {
            try {
                c.removeEvent(c.getEditEvent(e.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
            } catch (PermissionException ee) {
                log.warn(this + ":removeOldEvent " + rb.getFormattedMessage("cannotrem", title));
            } catch (InUseException ee) {
                log.warn(this + ":removeOldEvent " + rb.getString("somelsis_calendar"));
            } catch (IdUnusedException ee) {
                log.warn(this + ":removeOldEvent " + rb.getFormattedMessage("cannotfin6", e.getId()));
            }
        }
    }

    private void editAssignmentProperties(Assignment assignment, String checkAddDueTime, String checkAutoAnnounce, String addtoGradebook, String associateGradebookAssignment, String allowResubmitNumber, Map<String, String> properties, boolean post, Instant closeTime, boolean checkAnonymousGrading) {
        if (properties.get("newAssignment") != null) {
            if (properties.get("newAssignment").equalsIgnoreCase(Boolean.TRUE.toString())) {
                // not a newly created assignment, been added.
                properties.put("newAssignment", Boolean.FALSE.toString());
            }
        } else {
            // for newly created assignment
            properties.put("newAssignment", Boolean.TRUE.toString());
        }
        if (checkAddDueTime != null) {
            properties.put(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, checkAddDueTime);
        } else {
            properties.remove(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
        }
        properties.put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, checkAutoAnnounce);

        properties.put(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, Boolean.toString(checkAnonymousGrading));

        switch (addtoGradebook) {
            case GRADEBOOK_INTEGRATION_ADD:
                if (!post) {  // save as draft, retain original values for now
                    properties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ADD);
                    if (StringUtils.isNotBlank(associateGradebookAssignment)) {
                        properties.put(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, associateGradebookAssignment);
                    }
                    break;
                }
                associateGradebookAssignment = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
            case GRADEBOOK_INTEGRATION_ASSOCIATE:
                properties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_ASSOCIATE);
                if (StringUtils.isNotBlank(associateGradebookAssignment)) {
                    properties.put(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, associateGradebookAssignment);
                }
                break;
            case GRADEBOOK_INTEGRATION_NO:
            default:
                properties.put(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);
                properties.remove(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
        }

        // allow resubmit number and default assignment resubmit closeTime (dueTime)
        if (allowResubmitNumber != null && closeTime != null) {
            properties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, allowResubmitNumber);
            properties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(closeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        } else if (allowResubmitNumber == null || allowResubmitNumber.length() == 0 || "0".equals(allowResubmitNumber)) {
            properties.remove(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
            properties.remove(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
        }
    }

    private void commitAssignment(SessionState state,
                                  boolean post,
                                  // Assignment options
                                  Assignment a,
                                  String assignmentRef,
                                  String title,
                                  Assignment.SubmissionType submissionType,
                                  boolean useReviewService,
                                  boolean allowStudentViewReport,
                                  Assignment.GradeType gradeType,
                                  String gradePoints,
                                  String description,
                                  boolean checkAddHonorPledge,
                                  boolean checkAutoSubmit,
                                  List<Reference> attachments,
                                  String section,
                                  String range,
                                  Instant visibleTime,
                                  Instant openTime,
                                  Instant dueTime,
                                  Instant closeTime,
                                  boolean hideDueDate,
                                  boolean enableCloseDate,
                                  boolean emailReminder,
                                  boolean isGroupSubmit,
                                  Collection<Group> groups,
                                  // Peer Assessment options
                                  boolean usePeerAssessment,
                                  Instant peerAssessmentPeriodTime,
                                  boolean peerAssessmentAnonEval,
                                  boolean peerAssessmentStudentViewReviews,
                                  int peerAssessmentNumReviews,
                                  String peerAssessmentInstructions,
                                  // Content Review Options
                                  String submitReviewRepo,
                                  String generateOriginalityReport,
                                  boolean checkTurnitin,
                                  boolean checkInternet,
                                  boolean checkPublications,
                                  boolean checkInstitution,
                                  boolean excludeBibliographic,
                                  boolean excludeQuoted,
                                  boolean excludeSelfPlag,
                                  boolean storeInstIndex,
                                  boolean studentPreview,
                                  int excludeType,
                                  int excludeValue,
								  Integer contentId,
                                  boolean contentLaunchNewWindow,
                                  boolean checkIsEstimate,
                                  boolean checkEstimateRequired,
                                  String timeEstimate) {
        a.setTitle(processFormattedTextFromBrowser(state, title, true));
        a.setContext((String) state.getAttribute(STATE_CONTEXT_STRING));
        a.setSection(section);
        a.setIsGroup(isGroupSubmit);
        a.setInstructions(processFormattedTextFromBrowser(state, description, true));
        a.setHonorPledge(checkAddHonorPledge);
        a.setHideDueDate(hideDueDate);
        a.setTypeOfSubmission(submissionType);
        a.setContentReview(useReviewService);
        a.setTypeOfGrade(gradeType);

        a.setOpenDate(openTime);
        a.setDueDate(dueTime);
        a.setDropDeadDate(dueTime);
        a.setVisibleDate(visibleTime);

        a.setEstimateRequired(checkEstimateRequired);
        a.setEstimate(timeEstimate);
        
        if (closeTime != null) a.setCloseDate(closeTime);
		a.setContentId(contentId);
		a.setContentLaunchNewWindow(contentLaunchNewWindow);

        Map<String, String> p = a.getProperties();
        p.put("s_view_report", Boolean.toString(allowStudentViewReport));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO, submitReviewRepo);
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO, generateOriginalityReport);
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION, Boolean.toString(checkInstitution));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET, Boolean.toString(checkInternet));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB, Boolean.toString(checkPublications));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN, Boolean.toString(checkTurnitin));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC, Boolean.toString(excludeBibliographic));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED, Boolean.toString(excludeQuoted));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG, Boolean.toString(excludeSelfPlag));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX, Boolean.toString(storeInstIndex));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW, Boolean.toString(studentPreview));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE, Integer.toString(excludeType));
        p.put(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE, Integer.toString(excludeValue));
        p.put(NEW_ASSIGNMENT_REMINDER_EMAIL,Boolean.toString(emailReminder));
        
        // Add auto-submit property
        if (checkAutoSubmit) {
            p.put(AssignmentConstants.ASSIGNMENT_AUTO_SUBMIT_ENABLED, "true");
        } else {
            p.remove(AssignmentConstants.ASSIGNMENT_AUTO_SUBMIT_ENABLED);
        }

        if (!enableCloseDate) {
            // remove close date
            a.setCloseDate(null);
        }

        // post the assignment if appropriate
        a.setDraft(!post);

        if (gradeType == Assignment.GradeType.SCORE_GRADE_TYPE) {
            if (a.getScaleFactor() == null) {
                // only set the default scale factor if the assignment does not have one
                a.setScaleFactor(assignmentService.getScaleFactor());
            }
            try {
                a.setMaxGradePoint(Integer.parseInt(gradePoints));
            } catch (NumberFormatException e) {
                alertInvalidPoint(state, gradePoints, a.getScaleFactor());
                log.warn(this + ":commitAssignment " + e.getMessage());
            }
        }
        a.setIndividuallyGraded(false);

        if (submissionType != Assignment.SubmissionType.TEXT_ONLY_ASSIGNMENT_SUBMISSION) {
            a.setAllowAttachments(true);
        } else {
            a.setAllowAttachments(false);
        }

        Set<String> aAttachments = a.getAttachments();
        // clear attachments
        aAttachments.clear();

        if (attachments != null && !attachments.isEmpty()) {
            // add each attachment
            for (Reference reference : entityManager.newReferenceList(attachments)) {
                aAttachments.add(reference.getReference());
            }
        } else {
            state.setAttribute(ATTACHMENTS_MODIFIED, Boolean.FALSE);
        }

        a.setAllowPeerAssessment(usePeerAssessment);
        if (peerAssessmentPeriodTime != null) {
            a.setPeerAssessmentPeriodDate(peerAssessmentPeriodTime);
        }
        a.setPeerAssessmentAnonEval(peerAssessmentAnonEval);
        a.setPeerAssessmentStudentReview(peerAssessmentStudentViewReviews);
        a.setPeerAssessmentNumberReviews(peerAssessmentNumReviews);
        a.setPeerAssessmentInstructions(peerAssessmentInstructions);

        try {
            if (Assignment.Access.SITE.toString().equals(range)) {
                a.setTypeOfAccess(Assignment.Access.SITE);
                a.getGroups().clear();
            } else if (Assignment.Access.GROUP.toString().equals(range)) {
                a.setTypeOfAccess(Assignment.Access.GROUP);
                Set<String> previousGroupRefs = new HashSet<>(a.getGroups());
                Set<String> currentGroupRefs = groups.stream().map(Group::getReference).collect(Collectors.toSet());

                // if groups have changed
                if (!previousGroupRefs.equals(currentGroupRefs)) {
                    previousGroupRefs.removeAll(currentGroupRefs);
                    Set<String> submitterIds;

                    if (a.getIsGroup()) {
                        submitterIds = previousGroupRefs.stream().map(ref -> {
                            String[] parts = StringUtils.split(ref, '/');
                            return "group".equals(parts[2]) ? parts[3] : null;
                        }).filter(Objects::nonNull).collect(Collectors.toSet());
                    } else {
                        submitterIds = previousGroupRefs.stream().map(ref -> {
                            try {
                                return authzGroupService.getAuthzGroup(ref);
                            } catch (GroupNotDefinedException e) {
                                return null;
                            }
                        }).filter(Objects::nonNull).flatMap(ag -> ag.getMembers().stream()).map(Member::getUserId).collect(Collectors.toSet());
                    }

                    List<AssignmentSubmission> submissionsToRemove = submitterIds.stream().map(id -> {
                        try {
                            return assignmentService.getSubmission(a.getId(), id);
                        } catch (PermissionException pe) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList());

                    submissionsToRemove.stream()
                            .filter(s -> !s.getUserSubmission())
                            .forEach(s -> {
                                try {
                                    assignmentService.removeSubmission(s);
                                } catch (PermissionException pe) {
                                    log.warn("Group update detected on assignment {}, could not remove submission {}", a.getId(), s);
                                }
                            });
                    a.setGroups(currentGroupRefs);
                }
            }

            // commit the changes
            assignmentService.updateAssignment(a);

            // content review (after changes are stored)
            if (a.getContentReview()) {
                String errorMsg = assignmentService.createContentReviewAssignment(a, assignmentRef, openTime, dueTime, closeTime);
                if (StringUtils.isNotBlank(errorMsg)) {
                    String uiService = serverConfigurationService.getString("ui.service", "sakai");
                    String[] args = new String[]{contentReviewService.getServiceName(), uiService, errorMsg};
                    state.setAttribute("alertMessage", rb.getFormattedMessage("content_review.error.createAssignment", args));
                    a.setDraft(true);
                    assignmentService.updateAssignment(a);
                }
            }
        } catch (PermissionException e) {
            log.warn("Can't update Assignment, {}", e.getMessage());
            addAlert(state, rb.getString("youarenot_addAssignmentContent"));
        }

        if (a.getIsGroup() || gradingService.isGradebookGroupEnabled(a.getContext())) {
            rangeAndGroups.checkAssignmentForUsersInMultipleGroups(a, state);
        }

        if(!a.getDraft() && a.getAllowPeerAssessment()){
            assignmentPeerAssessmentService.schedulePeerReview(a.getId());
        }else{
            assignmentPeerAssessmentService.removeScheduledPeerReview(a.getId());
        }

        if (!a.getDraft() && emailReminder) {
            assignmentDueReminderService.scheduleDueDateReminder(a.getId());
        } else {
            assignmentDueReminderService.removeScheduledReminder(a.getId());
        }

    }

    /**
     * reorderAssignments
     */
    private void reorderAssignments(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        List assignments = prepPage(state);

        Iterator it = assignments.iterator();

        while (it.hasNext()) // reads and writes the parameter for default ordering
        {
            Assignment a = (Assignment) it.next();
            String assignmentid = a.getId();
            String assignmentposition = params.getString("position_" + assignmentid);
            SecurityAdvisor sa = new SecurityAdvisor() {
                public SecurityAdvice isAllowed(String userId, String function, String reference) {
                    return function.equals(SECURE_UPDATE_ASSIGNMENT) ? SecurityAdvice.ALLOWED : SecurityAdvice.PASS;
                }
            };
            try {
                // put in a security advisor so we can create citationAdmin site without need
                // of further permissions
                securityService.pushAdvisor(sa);
                a.setPosition(Integer.valueOf(assignmentposition));
                assignmentService.updateAssignment(a);
            } catch (Exception e) {
                log.warn(this + ":reorderAssignments : not able to edit assignment " + assignmentid + e.toString());
            } finally {
                // remove advisor
                securityService.popAdvisor(sa);
            }
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
            state.setAttribute(ATTACHMENTS, entityManager.newReferenceList());
        }
    } // reorderAssignments

    /**
     * construct time object based on various state variables
     *
     * @param state
     * @param monthString
     * @param dayString
     * @param yearString
     * @param hourString
     * @param minString
     * @return
     */
    private Instant getTimeFromState(SessionState state, String monthString, String dayString, String yearString, String hourString, String minString) {
        if (state.getAttribute(monthString) != null ||
                state.getAttribute(dayString) != null ||
                state.getAttribute(yearString) != null ||
                state.getAttribute(hourString) != null ||
                state.getAttribute(minString) != null) {
            int month = (Integer) state.getAttribute(monthString);
            int day = (Integer) state.getAttribute(dayString);
            int year = (Integer) state.getAttribute(yearString);
            int hour = (Integer) state.getAttribute(hourString);
            int min = (Integer) state.getAttribute(minString);
            return LocalDateTime.of(year, month, day, hour, min, 0).atZone(userTimeService.getLocalTimeZone().toZoneId()).toInstant();
        } else {
            return null;
        }
    }

    /**
     * Action is to post new assignment
     */
    public void doSave_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        post_save_assignment(data, "save");

    } // doSave_assignment

    /**
     * Action is to reorder assignments
     */
    public void doReorder_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        reorderAssignments(data);
    } // doReorder_assignments

    /**
     * Action is to preview the selected assignment
     */
    public void doPreview_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        setNewAssignmentParameters(data, true);

        String assignmentId = data.getParameters().getString("assignmentId");
        state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_ID, assignmentId);

        String assignmentContentId = data.getParameters().getString("assignmentContentId");
        state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENTCONTENT_ID, assignmentContentId);

        state.setAttribute(PREVIEW_ASSIGNMENT_ASSIGNMENT_HIDE_FLAG, Boolean.FALSE);
        state.setAttribute(PREVIEW_ASSIGNMENT_STUDENT_VIEW_HIDE_FLAG, Boolean.TRUE);
        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT);
        }

    } // doPreview_assignment

    /**
     * Action is to view the selected assignment
     */
    public void doView_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        // show the assignment portion
        state.setAttribute(VIEW_ASSIGNMENT_HIDE_ASSIGNMENT_FLAG, Boolean.FALSE);
        // show the student view portion
        state.setAttribute(VIEW_ASSIGNMENT_HIDE_STUDENT_VIEW_FLAG, Boolean.TRUE);

        String assignmentId = params.getString("assignmentId");
        state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);

        Assignment a = getAssignment(assignmentId, "doView_assignment", state);

        String siteId = a.getContext();

        //String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

        boolean allowSubmitAssignment = assignmentService.allowAddSubmission(siteId);
        boolean allowAddAssignment = assignmentService.allowAddAssignment(siteId);

        if (allowSubmitAssignment && !allowAddAssignment) {
            // Retrieve the status of the assignment
            String assignStatus = getAssignmentStatus(assignmentId, state);
            if (assignStatus != null && !assignStatus.equals(rb.getString("gen.open")) && !allowAddAssignment) {
                addAlert(state, rb.getFormattedMessage("gen.notavail", assignStatus));
            }
            doView_submission(data);
        } else {
            // get resubmission option into state
            assignment_resubmission_option_into_state(a, null, state);
            assignment_extension_option_into_state(a, null, state);

            // assignment read event
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT, assignmentId, false));

            if (state.getAttribute(STATE_MESSAGE) == null) {
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_ASSIGNMENT);
            }
        }

        eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT, assignmentId, false));

    } // doView_Assignment

    /**
     * Action is for student to view one assignment content
     */
    public void doView_assignment_as_student(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String assignmentId = params.getString("assignmentId");
        state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_ASSIGNMENT);
        }

    } // doView_assignment_as_student

    public void doView_submissionReviews(RunData data) {
        String submissionId = data.getParameters().getString("submissionId");
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String assessorId = data.getParameters().getString(RubricsConstants.RBCS_ASSESSOR_ID);
        String assignmentId = StringUtils.trimToNull(data.getParameters().getString("assignmentId"));
        Assignment a = getAssignment(assignmentId, "doEdit_assignment", state);
        if (submissionId != null && !"".equals(submissionId) && a != null) {
            //set the page to go to
            state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);
            List<PeerAssessmentItem> peerAssessmentItems = assignmentPeerAssessmentService.getPeerAssessmentItemsByAssignmentId(a.getId(), a.getScaleFactor());
            state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
            List<String> submissionIds = new ArrayList<String>();
            if (peerAssessmentItems != null) {
                for (PeerAssessmentItem item : peerAssessmentItems) {
                    submissionIds.add(item.getId().getSubmissionId());
                }
            }
            state.setAttribute(USER_SUBMISSIONS, submissionIds);
            state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
            state.setAttribute(PEER_ASSESSMENT_ASSESSOR_ID, assessorId);
            state.setAttribute(STATE_MODE, MODE_STUDENT_REVIEW_EDIT);
        } else {
            addAlert(state, rb.getString("peerassessment.notavailable"));
        }
    } // doView_submissionReviews

    public void doEdit_review(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String assignmentId = StringUtils.trimToNull(params.getString("assignmentId"));
        state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE, assignmentId);
        Assignment a = getAssignment(assignmentId, "doEdit_assignment", state);
        if (a != null && assignmentService.isPeerAssessmentOpen(a)) {
            //set the page to go to
            state.setAttribute(VIEW_ASSIGNMENT_ID, assignmentId);
            String submissionId = null;
            String peerId = assignmentService.getSubmitterIdForAssignment(a, userDirectoryService.getCurrentUser().getId());
            List<PeerAssessmentItem> peerAssessmentItems = peerAssessmentItems = assignmentPeerAssessmentService.getPeerAssessmentItems(a.getId(), peerId, a.getScaleFactor());
            state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
            List<String> submissionIds = new ArrayList<String>();
            if (peerAssessmentItems != null) {
                for (PeerAssessmentItem item : peerAssessmentItems) {
                    if (!item.getSubmitted()) {
                        submissionIds.add(item.getId().getSubmissionId());
                    }
                }
            }
            if (params.getString("submissionId") != null && submissionIds.contains(params.getString("submissionId"))) {
                submissionId = StringUtils.trimToNull(params.getString("submissionId"));
            } else if (submissionIds.size() > 0) {
                //submission Id wasn't passed in, let's find one for this user
                //grab the first one:
                submissionId = submissionIds.get(0);
            }
            if (submissionId != null) {
                state.setAttribute(USER_SUBMISSIONS, submissionIds);
                state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);
                state.setAttribute(STATE_MODE, MODE_STUDENT_REVIEW_EDIT);
            } else {
                if (peerAssessmentItems != null && peerAssessmentItems.size() > 0) {
                    //student has submitted all their peer reviews, nothing left to review
                    //(student really shouldn't get to this warning)
                    addAlert(state, rb.getString("peerassessment.allSubmitted"));
                } else {
                    //wasn't able to find a submission id, throw error
                    addAlert(state, rb.getString("peerassessment.notavailable"));
                }
            }
        } else {
            addAlert(state, rb.getString("peerassessment.notavailable"));
        }
    } // doEdit_review

    /**
     * Action is to show the edit assignment screen
     */
    public void doEdit_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String assignmentId = StringUtils.trimToNull(params.getString("assignmentId"));
        if (assignmentService.allowUpdateAssignment(assignmentId)) {
            Assignment a = getAssignment(assignmentId, "doEdit_assignment", state);
            if (a != null) {
                // whether the user can modify the assignment
                state.setAttribute(EDIT_ASSIGNMENT_ID, assignmentId);

                // for the non_electronice assignment, submissions are auto-generated by the time that assignment is created;
                // don't need to go through the following checkings.
                if (a.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
                    Iterator submissions = assignmentService.getSubmissions(a).iterator();
                    if (submissions.hasNext()) {
                        // any submitted?
                        boolean anySubmitted = false;
                        for (; submissions.hasNext() && !anySubmitted; ) {
                            AssignmentSubmission s = (AssignmentSubmission) submissions.next();
                            if (s.getSubmitted() && s.getDateSubmitted() != null) {
                                anySubmitted = true;
                            }
                        }

                        // any draft submission
                        boolean anyDraft = false;
                        for (; submissions.hasNext() && !anyDraft; ) {
                            AssignmentSubmission s = (AssignmentSubmission) submissions.next();
                            if (!s.getSubmitted()) {
                                anyDraft = true;
                            }
                        }
                        if (anySubmitted) {
                            // if there is any submitted submission to this assignment, show alert
                            addAlert(state, rb.getFormattedMessage("hassum", a.getTitle()));
                        }

                        if (anyDraft) {
                            // otherwise, show alert about someone has started working on the assignment, not necessarily submitted
                            addAlert(state, rb.getString("hasDraftSum"));
                        }
                    }
                }

                // SECTION MOD
                state.setAttribute(STATE_SECTION_STRING, a.getSection());

                // put the names and values into vm file
                state.setAttribute(NEW_ASSIGNMENT_TITLE, a.getTitle());
                state.setAttribute(NEW_ASSIGNMENT_CONTENT_ID, a.getContentId());
                if ( a.getContentId() != null ) {
                   try {
                        Site site = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
                        Long contentKey = a.getContentId().longValue();
                        Map<String, Object> content = ltiService.getContent(contentKey, site.getId());
                        Long toolKey = new Long(content.get(LTIService.LTI_TOOL_ID).toString());
                        if (toolKey != null) {
                            Map<String, Object> tool = ltiService.getTool(toolKey, site.getId());
                            if ( tool == null ) {
                                state.setAttribute(NEW_ASSIGNMENT_CONTENT_TITLE, null);
                                state.setAttribute(NEW_ASSIGNMENT_CONTENT_DELETED, Boolean.TRUE);
                                addAlert(state, rb.getString("contentitem.tool.deleted"));
                            } else {
                                String toolTitle = (String) tool.get(LTIService.LTI_TITLE);
                                state.setAttribute(NEW_ASSIGNMENT_CONTENT_TITLE, toolTitle);
                                Long toolNewpage = LTIUtil.toLong(tool.get(LTIService.LTI_NEWPAGE));
                                state.setAttribute(NEW_ASSIGNMENT_CONTENT_TOOL_NEWPAGE, toolNewpage);
                            }
                        }
                    } catch(org.sakaiproject.exception.IdUnusedException e ) {
                        // Send error to template
                        state.setAttribute(NEW_ASSIGNMENT_CONTENT_TITLE, null);
                    }
                } else {
                        state.setAttribute(NEW_ASSIGNMENT_CONTENT_TITLE, null);
                }

                state.setAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW, a.getContentLaunchNewWindow());
                state.setAttribute(NEW_ASSIGNMENT_ORDER, a.getPosition());

                if (serverConfigurationService.getBoolean("assignment.visible.date.enabled", false)) {
                    putTimePropertiesInState(state, a.getVisibleDate(), NEW_ASSIGNMENT_VISIBLE_MONTH, NEW_ASSIGNMENT_VISIBLE_DAY, NEW_ASSIGNMENT_VISIBLE_YEAR, NEW_ASSIGNMENT_VISIBLE_HOUR, NEW_ASSIGNMENT_VISIBLE_MIN);
                    state.setAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE, a.getVisibleDate() != null);
                }

                putTimePropertiesInState(state, a.getOpenDate(), NEW_ASSIGNMENT_OPEN_MONTH, NEW_ASSIGNMENT_OPEN_DAY, NEW_ASSIGNMENT_OPEN_YEAR, NEW_ASSIGNMENT_OPEN_HOUR, NEW_ASSIGNMENT_OPEN_MIN);
                // generate alert when editing an assignment past open date
                if (a.getOpenDate().isBefore(Instant.now())) {
                    addAlert(state, rb.getString("youarenot20"));
                }

                putTimePropertiesInState(state, a.getDueDate(), NEW_ASSIGNMENT_DUE_MONTH, NEW_ASSIGNMENT_DUE_DAY, NEW_ASSIGNMENT_DUE_YEAR, NEW_ASSIGNMENT_DUE_HOUR, NEW_ASSIGNMENT_DUE_MIN);
                // generate alert when editing an assignment past due date
                if (a.getDueDate().isBefore(Instant.now())) {
                    addAlert(state, rb.getString("youarenot17"));
                }

                if (a.getCloseDate() != null) {
                    state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.TRUE);
                    putTimePropertiesInState(state, a.getCloseDate(), NEW_ASSIGNMENT_CLOSE_MONTH, NEW_ASSIGNMENT_CLOSE_DAY, NEW_ASSIGNMENT_CLOSE_YEAR, NEW_ASSIGNMENT_CLOSE_HOUR, NEW_ASSIGNMENT_CLOSE_MIN);
                } else {
                    state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.FALSE);
                    state.setAttribute(NEW_ASSIGNMENT_CLOSE_MONTH, state.getAttribute(NEW_ASSIGNMENT_DUE_MONTH));
                    state.setAttribute(NEW_ASSIGNMENT_CLOSE_DAY, state.getAttribute(NEW_ASSIGNMENT_DUE_DAY));
                    state.setAttribute(NEW_ASSIGNMENT_CLOSE_YEAR, state.getAttribute(NEW_ASSIGNMENT_DUE_YEAR));
                    state.setAttribute(NEW_ASSIGNMENT_CLOSE_HOUR, state.getAttribute(NEW_ASSIGNMENT_DUE_HOUR));
                    state.setAttribute(NEW_ASSIGNMENT_CLOSE_MIN, state.getAttribute(NEW_ASSIGNMENT_DUE_MIN));
                }
                state.setAttribute(NEW_ASSIGNMENT_SECTION, a.getSection());

                state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, a.getTypeOfSubmission().ordinal());

                // Load current category from assignment properties (drafts); published items default to "no category"
                String siteId = a.getContext();
                loadCurrentAssignmentCategory(state, a, siteId);

                state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, a.getTypeOfGrade().ordinal());
                if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                    state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, a.getMaxGradePoint().toString());
                }
                state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, a.getInstructions());
                state.setAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE, a.getHideDueDate());
                Map<String, String> properties = a.getProperties();
                state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, properties.get(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE));

                state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, properties.get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));

                if (StringUtils.isNotBlank(a.getEstimate())) {
                    state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE, Boolean.TRUE);
                    state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED, a.getEstimateRequired());
                    state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE, a.getEstimate());
                }

                if (properties.get(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION) != null) {
                    state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, properties.get(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION));
                }

                if(properties.get(NEW_ASSIGNMENT_REMINDER_EMAIL) != null){
                    state.setAttribute(NEW_ASSIGNMENT_REMINDER_EMAIL, Boolean.valueOf(properties.get(NEW_ASSIGNMENT_REMINDER_EMAIL).toString()));
                }

                state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, a.getHonorPledge());
                
                // Load auto-submit setting
                boolean autoSubmit = "true".equals(properties.get(AssignmentConstants.ASSIGNMENT_AUTO_SUBMIT_ENABLED));
                state.setAttribute(NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT, autoSubmit);

                if (properties.get(NEW_ASSIGNMENT_TAG_CREATOR) != null) {
                    state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS, Boolean.valueOf(properties.get(NEW_ASSIGNMENT_TAG_CREATOR).toString()));
                }
                if (properties.get(NEW_ASSIGNMENT_TAG_GROUPS) != null) {
                    state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS, Boolean.valueOf(properties.get(NEW_ASSIGNMENT_TAG_GROUPS).toString()));
                }

                state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, properties.get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK));
                state.setAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT, properties.get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));

                List<Reference> v = entityManager.newReferenceList();
                a.getAttachments().forEach(f -> v.add(entityManager.newReference(f)));
                state.setAttribute(ATTACHMENTS, v);

                // submission notification option
                if (properties.get(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE) != null) {
                    state.setAttribute(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE, properties.get(AssignmentConstants.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE));
                }
                // release grade notification option
                if (properties.get(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE) != null) {
                    state.setAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE, properties.get(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE));
                }

                // group setting
                rangeAndGroups.doEditAssignment(state, a);

                state.setAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING, assignmentService.assignmentUsesAnonymousGrading(a));

                // put the resubmission option into state
                assignment_resubmission_option_into_state(a, null, state);
                assignment_extension_option_into_state(a, null, state);

                // set whether we use peer assessment or not
                Instant peerAssessmentPeriod = a.getPeerAssessmentPeriodDate();
                //check if peer assessment time exist? if not, this could be an old assignment, so just set it
                //to 10 min after accept until date
                if (peerAssessmentPeriod == null && a.getCloseDate() != null) {
                    // set the peer period time to be 10 mins after accept until date
                    peerAssessmentPeriod = a.getCloseDate().plus(Duration.ofMinutes(10));
                }
                if (peerAssessmentPeriod != null) {
                    state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, a.getAllowPeerAssessment().toString());
                    putTimePropertiesInState(state, peerAssessmentPeriod, NEW_ASSIGNMENT_PEERPERIOD_MONTH, NEW_ASSIGNMENT_PEERPERIOD_DAY, NEW_ASSIGNMENT_PEERPERIOD_YEAR, NEW_ASSIGNMENT_PEERPERIOD_HOUR, NEW_ASSIGNMENT_PEERPERIOD_MIN);
                    state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL, a.getPeerAssessmentAnonEval());
                    state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS, a.getPeerAssessmentStudentReview());
                    state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS, a.getPeerAssessmentNumberReviews());
                    state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS, a.getPeerAssessmentInstructions());
                }
                if (!serverConfigurationService.getBoolean("assignment.usePeerAssessment", true)) {
                    state.setAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT, Boolean.FALSE.toString());
                }
                // set whether we use the review service or not
                // TODO content review
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_USE_REVIEW_SERVICE, a.getContentReview());

                //set whether students can view the review service results
                Map<String, String> p = a.getProperties();
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW, Boolean.valueOf(p.get("s_view_report")).toString());

                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_RADIO));
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO));
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_TURNITIN));
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INTERNET));
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_PUB));
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION));
                //exclude bibliographic
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC));
                //exclude quoted
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED));
                //exclude self plag
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG));
                //store inst index
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX));
                //student preview
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW));
                //exclude type
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE));
                //exclude value
                state.setAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE, p.get(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE));

                // get all supplement item info into state
                setAssignmentSupplementItemInState(state, a);

                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT);
            }
        } else {
            addAlert(state, rb.getString("youarenot6"));
        }

    } // doEdit_Assignment

    
	private String getGradeName(final Assignment a, final String assignmentReference) {
        final Map<String, String> properties = a.getProperties();
        final String gradebookChoice = StringUtils.defaultIfBlank(properties.get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK), GRADEBOOK_INTEGRATION_NO);
        final StringBuffer sb = new StringBuffer();

        switch(gradebookChoice) {
            case GRADEBOOK_INTEGRATION_ADD:
                // add new entry to gradebook
                sb.append(rb.getString("grading.add"))
                        .append(rb.getString("grading.ofcategory"))
                        .append(getCategoryTable().get(0));
                break;
            case GRADEBOOK_INTEGRATION_ASSOCIATE:
			    // associated with one existing entry in Gradebook
                sb.append(rb.getString("grading.associate"))
                        .append(": ");
                if (assignmentReference.equals(properties.get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))) {
                    sb.append(a.getTitle());
                } else {
                    sb.append(properties.get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
                }
                break;
            case GRADEBOOK_INTEGRATION_NO:
            default:
                // not associating with gradebook
                sb.append(rb.getString("grading.no"));
        }
        return sb.toString();
	}
    
    public List<String> getSubmissionRepositoryOptions() {
        List<String> submissionRepoSettings = new ArrayList<String>();
        String[] propertyValues = serverConfigurationService.getStrings("turnitin.repository.setting");
        if (propertyValues != null && propertyValues.length > 0) {
            for (int i = 0; i < propertyValues.length; i++) {
                String propertyVal = propertyValues[i];
                if (propertyVal.equals(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE) ||
                        propertyVal.equals(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_INSITUTION) ||
                        propertyVal.equals(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD)) {
                    submissionRepoSettings.add(propertyVal);
                }
            }
        }

        // if there are still no valid settings in the list at this point, use the default
        if (submissionRepoSettings.isEmpty()) {
            // add all three
            submissionRepoSettings.add(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_NONE);
            submissionRepoSettings.add(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_SUBMIT_STANDARD);
        }

        return submissionRepoSettings;
    }

    public List<String> getReportGenOptions() {
        List<String> reportGenSettings = new ArrayList<String>();
        String[] propertyValues = serverConfigurationService.getStrings("turnitin.report_gen_speed.setting");
        if (propertyValues != null && propertyValues.length > 0) {
            for (int i = 0; i < propertyValues.length; i++) {
                String propertyVal = propertyValues[i];
                if (propertyVal.equals(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY) || propertyVal.equals(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY_AND_DUE) ||
                        propertyVal.equals(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE)) {
                    reportGenSettings.add(propertyVal);
                }
            }
        }

        // if there are still no valid settings in the list at this point, use the default
        if (reportGenSettings.isEmpty()) {
            // add only the defaults used in original Turnitin integration
        		reportGenSettings.add(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_IMMEDIATELY);
            reportGenSettings.add(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_DUE);
        }

        return reportGenSettings;
    }

    /**
     * put all assignment supplement item info into state
     *
     * @param state
     * @param a
     */
    private void setAssignmentSupplementItemInState(SessionState state, Assignment a) {

        String assignmentId = a.getId();

        // model answer
        AssignmentModelAnswerItem mAnswer = assignmentSupplementItemService.getModelAnswer(assignmentId);
        if (mAnswer != null) {
            if (state.getAttribute(MODELANSWER_TEXT) == null) {
                state.setAttribute(MODELANSWER_TEXT, mAnswer.getText());
            }
            if (state.getAttribute(MODELANSWER_SHOWTO) == null) {
                state.setAttribute(MODELANSWER_SHOWTO, String.valueOf(mAnswer.getShowTo()));
            }
            if (state.getAttribute(MODELANSWER) == null) {
                state.setAttribute(MODELANSWER, Boolean.TRUE);
            }
        }

        // get attachments for model answer object
        putSupplementItemAttachmentInfoIntoState(state, mAnswer, MODELANSWER_ATTACHMENTS);

        // private notes
        AssignmentNoteItem mNote = assignmentSupplementItemService.getNoteItem(assignmentId);
        if (mNote != null) {
            if (state.getAttribute(NOTE) == null) {
                state.setAttribute(NOTE, Boolean.TRUE);
            }
            if (state.getAttribute(NOTE_TEXT) == null) {
                state.setAttribute(NOTE_TEXT, mNote.getNote());
            }
            if (state.getAttribute(NOTE_SHAREWITH) == null) {
                state.setAttribute(NOTE_SHAREWITH, String.valueOf(mNote.getShareWith()));
            }
        }

        // all purpose item
        AssignmentAllPurposeItem aItem = assignmentSupplementItemService.getAllPurposeItem(assignmentId);
        if (aItem != null) {
            if (state.getAttribute(ALLPURPOSE) == null) {
                state.setAttribute(ALLPURPOSE, Boolean.TRUE);
            }
            if (state.getAttribute(ALLPURPOSE_TITLE) == null) {
                state.setAttribute(ALLPURPOSE_TITLE, aItem.getTitle());
            }
            if (state.getAttribute(ALLPURPOSE_TEXT) == null) {
                state.setAttribute(ALLPURPOSE_TEXT, aItem.getText());
            }
            if (state.getAttribute(ALLPURPOSE_HIDE) == null) {
                state.setAttribute(ALLPURPOSE_HIDE, Boolean.valueOf(aItem.getHide()));
            }
            if (state.getAttribute(ALLPURPOSE_SHOW_FROM) == null) {
                state.setAttribute(ALLPURPOSE_SHOW_FROM, aItem.getReleaseDate() != null);
            }
            if (state.getAttribute(ALLPURPOSE_SHOW_TO) == null) {
                state.setAttribute(ALLPURPOSE_SHOW_TO, aItem.getRetractDate() != null);
            }
            if (state.getAttribute(ALLPURPOSE_ACCESS) == null) {
                Set<AssignmentAllPurposeItemAccess> aSet = aItem.getAccessSet();
                List<String> aList = new ArrayList<String>();
                for (Iterator<AssignmentAllPurposeItemAccess> aIterator = aSet.iterator(); aIterator.hasNext(); ) {
                    AssignmentAllPurposeItemAccess access = aIterator.next();
                    aList.add(access.getAccess());
                }
                state.setAttribute(ALLPURPOSE_ACCESS, aList);
            }

            // get attachments for model answer object
            putSupplementItemAttachmentInfoIntoState(state, aItem, ALLPURPOSE_ATTACHMENTS);
        }

        // get the AllPurposeItem and AllPurposeReleaseTime/AllPurposeRetractTime
        //default to assignment open time
        Instant releaseTime = a.getOpenDate();
        // default to assignment close time
        Instant retractTime = a.getCloseDate();
        if (aItem != null) {
            Instant releaseDate = aItem.getReleaseDate() != null ? aItem.getReleaseDate().toInstant() : null;
            if (releaseDate != null) {
                // overwrite if there is a release date
                releaseTime = releaseDate;
            }

            Instant retractDate = aItem.getRetractDate() != null ? aItem.getRetractDate().toInstant() : null;
            if (retractDate != null) {
                // overwriteif there is a retract date
                retractTime = retractDate;
            }
        }
        putTimePropertiesInState(state, releaseTime, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY, ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN);

        putTimePropertiesInState(state, retractTime, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY, ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN);
    }

    /**
     * Action is to show the delete assigment confirmation screen
     */
    public void doDelete_confirm_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String[] assignmentIds = params.getStrings("selectedAssignments");

        if (assignmentIds != null) {
            List<String> ids = new ArrayList<>();
            for (String id : assignmentIds) {
                if (!assignmentService.allowRemoveAssignment(id)) {
                    addAlert(state, rb.getFormattedMessage("youarenot_removeAssignment", id));
                }
                ids.add(id);
            }

            if (state.getAttribute(STATE_MESSAGE) == null) {
                // can remove all the selected assignments
                state.setAttribute(SELECTED_ASSIGNMENT_IDS, ids);
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_DELETE_ASSIGNMENT);
            }
        } else {
            addAlert(state, rb.getString("youmust6"));
        }

    } // doDelete_confirm_Assignment

    /**
     * Action is to delete the confirmed assignments
     */
    public void doDelete_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);

        // get the delete assignment references
        List<String> references = (List<String>) state.getAttribute(SELECTED_ASSIGNMENT_IDS);
        for (String ref : references) {
            try {
                String id = AssignmentReferenceReckoner.reckoner().reference(ref).reckon().getId();
                Assignment assignment = assignmentService.getAssignment(id);
                Map<String, String> properties = assignment.getProperties();

                if (assignment.getIsGroup()) {
                    if (!siteService.allowUpdateSite(siteId)) {
                        addAlert(state, rb.getFormattedMessage("group.editsite.nopermission"));
                    }
                }

                String title = assignment.getTitle();

                // remove rubric association if there is one
                rubricsService.softDeleteRubricAssociation(AssignmentConstants.TOOL_ID, id);
				
                String associateGradebookAssignment = properties.get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                if (associateGradebookAssignment != null) {
                    List<String> itemList = Arrays.asList(associateGradebookAssignment.split(","));
                    Set<String> itemSet = new HashSet<>(itemList);
                    for (String item : itemSet) {
                        // remove from Gradebook
                        updateGradebookAssociation(state, siteId, ref, item, null, "remove");
                    }
                }
                // we use to check "assignment.delete.cascade.submission" setting. But the implementation now is always remove submission objects when the assignment is removed.
                // delete assignment and its submissions altogether
                deleteAssignmentObjects(state, assignment);
            } catch (IdUnusedException e) {
                log.warn("Cannot find site with ref: {}", siteId);
                addAlert(state, rb.getFormattedMessage("options_cannotFindSite"));
            } catch (PermissionException e) {
                log.warn("User does not have permission to edit site with ref: {}", siteId);
                addAlert(state, rb.getFormattedMessage("options_cannotEditSite"));
            }
        } // for

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(SELECTED_ASSIGNMENT_IDS, new ArrayList());

            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

            // reset paging information after the assignment been deleted
            resetPaging(state);
        }

    } // doDelete_Assignment

    /**
     * private method to remove assignment and related objects
     *
     * @param state
     * @param assignment
     */
    private void deleteAssignmentObjects(SessionState state, Assignment assignment) {

        try {
            assignmentService.deleteAssignmentAndAllReferences(assignment);
        } catch (PermissionException e) {
            addAlert(state, rb.getString("youarenot11") + " " + assignment.getTitle() + ". ");
            log.warn("Could not delete assignment, {}", e.getMessage());
        }
    }

    /**
     * Action is to show the publish assigment confirmation screen
     *
     * @param data A JetspeedRunData instance with the request state
     */
    public void doPublish_confirm_assignment(RunData data) {

        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String[] assignmentIds = params.getStrings("selectedAssignments");

        if (assignmentIds != null) {
            List<String> ids = new ArrayList<>();
            for (String id : assignmentIds) {
                if (!assignmentService.allowUpdateAssignment(id)) {
                    addAlert(state, rb.getFormattedMessage("youarenot_editAssignment", id));
                }
                ids.add(id);
            }

            if (state.getAttribute(STATE_MESSAGE) == null) {
                // can remove all the selected assignments
                state.setAttribute(SELECTED_ASSIGNMENT_IDS, ids);
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_PUBLISH_ASSIGNMENT);
            }
        } else {
            addAlert(state, rb.getString("youmust6"));
        }

    } // doDelete_confirm_Assignment

    /**
     * Action is to poblish the confirmed assignments
     */
    public void doPublish_assignment(RunData data) {

        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);

        // get the delete assignment references
        List<String> references = (List<String>) state.getAttribute(SELECTED_ASSIGNMENT_IDS);
        for (String ref : references) {
            try {
                String id = AssignmentReferenceReckoner.reckoner().reference(ref).reckon().getId();
                Assignment assignment = assignmentService.getAssignment(id);
                assignment.setDraft(Boolean.FALSE);
                assignmentService.updateAssignment(assignment);
            } catch (IdUnusedException e) {
                log.warn("Cannot find assignment with ref: {}", ref);
                addAlert(state, rb.getFormattedMessage("options_cannotFindAssignment", ref));
            } catch (PermissionException e) {
                log.warn("User does not have permission to edit site with ref: {}", siteId);
                addAlert(state, rb.getFormattedMessage("options_cannotEditSite"));
            }
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(SELECTED_ASSIGNMENT_IDS, new ArrayList());

            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

            // reset paging information after the assignment been deleted
            resetPaging(state);
        }
    }

    /**
     * Action is to show the unpublish assigment confirmation screen
     */
    public void doUnpublish_confirm_assignment(RunData data) {

        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String[] assignmentIds = params.getStrings("selectedAssignments");

        if (assignmentIds != null) {
            List<String> ids = new ArrayList<>();
            for (String id : assignmentIds) {
                if (!assignmentService.allowUpdateAssignment(id)) {
                    addAlert(state, rb.getFormattedMessage("youarenot_editAssignment", id));
                }
                ids.add(id);
            }

            if (state.getAttribute(STATE_MESSAGE) == null) {
                // can remove all the selected assignments
                state.setAttribute(SELECTED_ASSIGNMENT_IDS, ids);
                state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_UNPUBLISH_ASSIGNMENT);
            }
        } else {
            addAlert(state, rb.getString("youmust6"));
        }

    }

    /**
     * Action is to unpoblish the confirmed assignments
     */
    public void doUnpublish_assignment(RunData data) {

        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);

        // get the delete assignment references
        List<String> references = (List<String>) state.getAttribute(SELECTED_ASSIGNMENT_IDS);
        for (String ref : references) {
            try {
                String id = AssignmentReferenceReckoner.reckoner().reference(ref).reckon().getId();
                Assignment assignment = assignmentService.getAssignment(id);
                assignment.setDraft(Boolean.TRUE);
                assignmentService.updateAssignment(assignment);
            } catch (IdUnusedException e) {
                log.warn("Cannot find assignment with ref: {}", ref);
                addAlert(state, rb.getFormattedMessage("options_cannotFindAssignment", ref));
            } catch (PermissionException e) {
                log.warn("User does not have permission to edit site with ref: {}", siteId);
                addAlert(state, rb.getFormattedMessage("options_cannotEditSite"));
            }
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(SELECTED_ASSIGNMENT_IDS, new ArrayList());

            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);

            // reset paging information after the assignment been deleted
            resetPaging(state);
        }
    }


    /**
     * Action is to delete the assignment and also the related AssignmentSubmission
     */
    public void doHardRemove_confirm_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        // get the assignment ids
        String[] assignmentIds = params.getStrings("selectedAssignments");
        if (assignmentIds != null) {
            for (String id : assignmentIds) {
                Assignment a = null;
                try {
                    a = assignmentService.getAssignment(id);

                    if (a != null) {
                        if (taggingManager.isTaggable()) {
                            for (TaggingProvider provider : taggingManager.getProviders()) {
                                provider.removeTags(assignmentActivityProducer.getActivity(a));
                            }
                        }

                        // remove rubric association if there is one
                        // TODO: this should be in the service and in a transaction!!
                        rubricsService.deleteRubricAssociationsByItemIdPrefix(id, AssignmentConstants.TOOL_ID);

                        assignmentService.deleteAssignment(a);
                    }

                } catch (IdUnusedException | PermissionException e) {
                    addAlert(state, rb.getFormattedMessage("youarenot_editAssignment", id));
                    log.warn(e.getMessage());
                }
            }

            if (state.getAttribute(STATE_MESSAGE) == null) {
                state.setAttribute("selectedAssignments", new ArrayList());
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
                state.setAttribute(STATE_SELECTED_VIEW, MODE_LIST_ASSIGNMENTS);
                state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
            }
        } else {
            addAlert(state, rb.getString("youmust6"));
        }
    } // doHardRemove_confirm_assignment

    public void buildGradebookUidList(SessionState state, String siteId, List<String> selectedGradebookUids, String addtoGradebook,
        List<String> selectedGroups, boolean isGradebookGroupEnabled) {
        if (!GRADEBOOK_INTEGRATION_NO.equals(addtoGradebook)) {
            if (!isGradebookGroupEnabled) {
                selectedGradebookUids.add(siteId);
            } else {
                List<Gradebook> gbList = gradingService.getGradebookGroupInstances(siteId);
                List<String> existingGradebookUids = gbList.stream().map(Gradebook::getUid).collect(Collectors.toList());
                if (!existingGradebookUids.containsAll(selectedGroups)) {
                    addAlert(state, rb.getString("selected.groups.without.gradebook"));
                } else {
                    selectedGradebookUids.addAll(selectedGroups);
                }
            }
        }
    }

    /**
     * Action is to show the restore assigment confirmation screen
     */
    public void doRestore_confirm_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();
        // get the assignment ids
        String[] assignmentIds = params.getStrings("selectedAssignments");
        if (assignmentIds != null) {
            for (String id : assignmentIds) {
                Assignment a = null;
                try {
                    a = assignmentService.getAssignment(id);

                    if (a != null) {
                        a.setDeleted(false);
                        assignmentService.updateAssignment(a);

                        rubricsService.restoreRubricAssociation(AssignmentConstants.TOOL_ID, id);

                        // restore email reminder only if reminder is set and the due date is after 1 day
                        if (BooleanUtils.toBoolean(a.getProperties().get(NEW_ASSIGNMENT_REMINDER_EMAIL))
                                && a.getDueDate() != null
                                && Instant.now().plus(1, ChronoUnit.DAYS).isBefore(a.getDueDate())) {
                            assignmentDueReminderService.scheduleDueDateReminder(a.getId());
                        }

                        // Restore gradebook item only if assignment was previously associated
                        if (StringUtils.equals(a.getProperties().get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK), GRADEBOOK_INTEGRATION_ASSOCIATE)) {
                            String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
                            Site site = siteService.getSite(siteId);

                            boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(siteId);

                            String title = a.getTitle();
                            String associateGradebookAssignment = null; // Stored in properties, but we need to pass null so the gradebook integration algorithm creates the item rather than updating the (non-existing) original
                            String addToGradebook = GRADEBOOK_INTEGRATION_ADD; // Stored in properties as "associate" already, but we need to pass "add" to recreate the original
                            long category = -1L; // We have to default to no category because the original gradebook item was hard deleted and category ID is not stored in assignment properties

                            List<Group> asnGroups = a.getGroups().stream().map(groupId -> site.getGroup(groupId)).filter(Objects::nonNull).collect(Collectors.toList());
                            List<String> groupIdList = asnGroups.stream().map(Group::getId).collect(Collectors.toList());

                            List<String> selectedGradebookUids = new ArrayList<>();

                            buildGradebookUidList(state, siteId, selectedGradebookUids, addToGradebook, groupIdList, isGradebookGroupEnabled);

                            for (String gbUid : selectedGradebookUids) {
                                initIntegrateWithGradebook(state, gbUid, title, associateGradebookAssignment, a, title, a.getDueDate(), a.getTypeOfGrade(), a.getMaxGradePoint().toString(),
                                                        addToGradebook, associateGradebookAssignment, category);
                            }
                        }
                    }
                } catch (IdUnusedException | PermissionException e) {
                    addAlert(state, rb.getFormattedMessage("youarenot_editAssignment", id));
                    log.warn(e.getMessage());
                }
            }

            if (state.getAttribute(STATE_MESSAGE) == null) {
                state.setAttribute("selectedAssignments", new ArrayList());
                state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
                state.setAttribute(STATE_SELECTED_VIEW, MODE_LIST_ASSIGNMENTS);
                state.setAttribute(SORTED_BY, SORTED_BY_DEFAULT);
            }
        } else {
            addAlert(state, rb.getString("youmust6"));
        }

    } // doRestore_confirm_assignment

    /**
     * Action is to show the duplicate assignment screen
     */
    public void doDuplicate_assignment(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // we are changing the view, so start with first page again.
        resetPaging(state);

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        ParameterParser params = data.getParameters();
        String assignmentReference = StringUtils.trimToNull(params.getString("assignmentId"));
        String assignmentId = AssignmentReferenceReckoner.reckoner().reference(assignmentReference).reckon().getId();

        if (assignmentId != null) {
            try {
                Assignment assignment = assignmentService.addDuplicateAssignment(contextString, assignmentId);
            } catch (PermissionException e) {
                addAlert(state, rb.getString("youarenot5"));
                log.warn(this + ":doDuplicate_assignment " + e.getMessage());
            } catch (IdInvalidException e) {
                addAlert(state, rb.getFormattedMessage("theassiid_isnotval", assignmentId));
                log.warn(this + ":doDuplicate_assignment " + e.getMessage());
            } catch (IdUnusedException e) {
                addAlert(state, rb.getFormattedMessage("theassiid_hasnotbee", assignmentId));
                log.warn(this + ":doDuplicate_assignment " + e.getMessage());
            } catch (Exception e) {
                log.warn(this + ":doDuplicate_assignment " + e.getMessage());
            }
        }
    } // doDuplicate_Assignment

    /**
     * Action is to show the grade submission screen
     */
    public void doGrade_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // reset the submission context
        resetViewSubmission(state);

        ParameterParser params = data.getParameters();
        String assignmentId = params.getString("assignmentId");
        state.setAttribute(EXPORT_ASSIGNMENT_REF, assignmentId);
        String submissionId = params.getString("submissionId");
        String id = params.getString("submitterId");
        state.setAttribute(EXPANDED_USER_ID, id);
        
        // SAK-29314 - put submission information into state
        boolean viewSubsOnlySelected = stringToBool((String) data.getParameters().getString(PARAMS_VIEW_SUBS_ONLY_CHECKBOX));
        putSubmissionInfoIntoState(state, assignmentId, submissionId, viewSubsOnlySelected);
        assignment_extension_option_into_state(getAssignment(assignmentId, "doGrade_submission", state), getSubmission(submissionId, "doGrade_submission", state), state);
        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_SUBMISSION);
            state.setAttribute(FROM_VIEW, (String) params.getString("option"));
            // assignment read event
            eventTrackingService.post(eventTrackingService.newEvent(AssignmentConstants.EVENT_ACCESS_ASSIGNMENT_SUBMISSION, submissionId, false));
        }
    } // doGrade_submission

    /**
     * put all the submission information into state variables
     *
     * @param state
     * @param assignmentId
     * @param submissionId
     * @param viewSubsOnlySelected
     */
    private void putSubmissionInfoIntoState(SessionState state, String assignmentId, String submissionId, boolean viewSubsOnlySelected) {
        // reset grading submission variables
        resetGradeSubmission(state);

        // reset the grade assignment id and submission id
        state.setAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID, assignmentId);
        state.setAttribute(GRADE_SUBMISSION_SUBMISSION_ID, submissionId);

        // SAK-29314
        state.setAttribute(STATE_VIEW_SUBS_ONLY, viewSubsOnlySelected);

        // allow resubmit number
        String allowResubmitNumber = "0";
        Assignment a = getAssignment(assignmentId, "putSubmissionInfoIntoState", state);
        if (a != null) {
            AssignmentSubmission s = getSubmission(submissionId, "putSubmissionInfoIntoState", state);
            if (s != null) {
                state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, s.getSubmittedText());
                state.setAttribute(GRADE_SUBMISSION_SUBMITTERS_NAMES, getSubmitterFormattedNames(s, false));

                if ((s.getFeedbackText() == null) || (s.getFeedbackText().length() == 0)) {
                    state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, s.getSubmittedText());
                } else {
                    state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, s.getFeedbackText());
                }

                state.setAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT, s.getFeedbackComment());
                state.setAttribute(GRADE_SUBMISSION_PRIVATE_NOTES, s.getPrivateNotes());

                List<Reference> v = entityManager.newReferenceList();
                s.getFeedbackAttachments().forEach(f -> v.add(entityManager.newReference(f)));
                state.setAttribute(ATTACHMENTS, v);
                state.setAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT, v);
                String grade = assignmentService.getGradeDisplay(s.getGrade(), a.getTypeOfGrade(), a.getScaleFactor());
                state.setAttribute(GRADE_SUBMISSION_GRADE, grade);

                // populate grade overrides if they exist (skip when anonymous grading)
                if (AssignmentToolUtils.allowGroupOverrides(a, assignmentService)) {
                        for (AssignmentSubmissionSubmitter submitter : s.getSubmitters()) {
                            String gradeOverride = assignmentService.getGradeForSubmitter(s, submitter.getSubmitter());
                            if (!StringUtils.equals(grade, gradeOverride)) {
                                state.setAttribute(GRADE_SUBMISSION_GRADE + "_" + submitter.getSubmitter(), gradeOverride);
                            }
	                }
                }

                // put the resubmission info into state
                assignment_resubmission_option_into_state(a, s, state);
                assignment_extension_option_into_state(a, s, state);
            }
        }
    }

    /**
     * Action is to release all the grades of the submission
     */
    public void doRelease_grades(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();

        String assignmentId = params.getString("assignmentId");

        Assignment a = getAssignment(assignmentId, "doRelease_grades", state);

        if (a != null) {
            String aReference = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
            List<AssignmentSubmission> submissions = getFilteredSubmitters(state, aReference);
            for (AssignmentSubmission s : submissions) {
                // if the submission is not already released
                // if the assignment type is either
                //   UNGRADED and comments have been left on the submission
                //   GRADED and a grade exists on the submission
                if (!s.getGradeReleased()
                        && (a.getTypeOfGrade() == Assignment.GradeType.UNGRADED_GRADE_TYPE && assignmentService.doesSubmissionHaveInstructorFeedback(s)
                        || ((a.getTypeOfGrade() != Assignment.GradeType.UNGRADED_GRADE_TYPE) && StringUtils.isNotBlank(s.getGrade())))) {
                    s.setGraded(true);
                    s.setGradeReleased(true);
                    s.setReturned(true);
                    s.setDateReturned(Instant.now());
                    try {
                        assignmentService.updateSubmission(s);
                    } catch (PermissionException e) {
                        log.warn("Failed to update submission [{}] while releasing grades, {}", s.getId(), e.toString());
                    }
                }
            }

            // add grades into Gradebook
            String integrateWithGradebook = a.getProperties().get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
            if (integrateWithGradebook != null && !integrateWithGradebook.equals(GRADEBOOK_INTEGRATION_NO)) {
                // integrate with Gradebook
                String associateGradebookAssignment = a.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                String siteId = a.getContext();
                updateGradebookAssociation(state, siteId, aReference, associateGradebookAssignment, null, "update");
            }
        }
    }

    /**
     * Action is to show the assignment in grading page
     */
    public void doExpand_grade_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

    } // doExpand_grade_assignment

    /**
     * Action is to hide the assignment in grading page
     */
    public void doCollapse_grade_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

    } // doCollapse_grade_assignment

    /**
     * Action is to show the submissions in grading page
     */
    public void doExpand_grade_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, Boolean.TRUE);

    } // doExpand_grade_submission

    /**
     * Action is to hide the submissions in grading page
     */
    public void doCollapse_grade_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, Boolean.FALSE);

    } // doCollapse_grade_submission

    /**
     * Action is to show the grade assignment
     */
    public void doGrade_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        // clean state attribute
        state.removeAttribute(USER_SUBMISSIONS);
        state.removeAttribute(SAVED_FEEDBACK);
        state.removeAttribute(OW_FEEDBACK);
        state.removeAttribute(RETURNED_FEEDBACK);

        String assignmentId = params.getString("assignmentId");
        state.setAttribute(EXPORT_ASSIGNMENT_REF, assignmentId);

        Assignment a = getAssignment(assignmentId, "doGrade_assignment", state);
        if (a != null) {
            state.setAttribute(EXPORT_ASSIGNMENT_ID, a.getId());
            state.setAttribute(GRADE_SUBMISSION_EXPAND_FLAG, Boolean.TRUE);
            state.setAttribute(GRADE_SUBMISSION_SHOW_STUDENT_DETAILS, params.getBoolean(GRADE_SUBMISSION_SHOW_STUDENT_DETAILS));
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);

            // initialize the resubmission params
            assignment_resubmission_option_into_state(a, null, state);
            assignment_extension_option_into_state(a, null, state);
            // we are changing the view, so start with first page again.
            resetPaging(state);
        }
    } // doGrade_assignment

    /**
     * Action is to show the View Students assignment screen
     */
    public void doView_students_assignment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT);

    } // doView_students_Assignment

    /**
     * Action is to show the student submissions
     */
    public void doShow_student_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        Set t = (Set) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
        ParameterParser params = data.getParameters();

        String id = params.getString("studentId");
        state.setAttribute(EXPANDED_USER_ID, id);
        // add the student id into the table
        t.add(id);

        state.setAttribute(STUDENT_LIST_SHOW_TABLE, t);

    } // doShow_student_submission

    /**
     * Action is to hide the student submissions
     */
    public void doHide_student_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        Set t = (Set) state.getAttribute(STUDENT_LIST_SHOW_TABLE);
        ParameterParser params = data.getParameters();

        String id = params.getString("studentId");
        state.setAttribute(EXPANDED_USER_ID, id);
        // remove the student id from the table
        t.remove(id);

        state.setAttribute(STUDENT_LIST_SHOW_TABLE, t);

    } // doHide_student_submission

    /**
     * Action is to show the graded assignment submission
     */
    public void doView_grade(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();

        String submissionReference = params.getString("submissionId");

        prepareStudentViewGrade(state, submissionReference);
    } // doView_grade

    /**
     * Prepares the state for the student to view their grade
     */
    private void prepareStudentViewGrade(SessionState state, String submissionReference) {
        state.setAttribute(VIEW_GRADE_SUBMISSION_ID, submissionReference);

        String mode = MODE_STUDENT_VIEW_GRADE;

        AssignmentSubmission s = getSubmission((String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID), "doView_grade", state);
        // whether the user can access the Submission object
        if (s != null) {
            String status = assignmentService.getSubmissionStatus(s.getId(), true);

            // show submission view unless group submission with group error
            Assignment a = s.getAssignment();
            User u = (User) state.getAttribute(STATE_USER);
            if (a.getIsGroup() && !rangeAndGroups.validateUserGroups(state, u.getId(), a)) {
                mode = MODE_STUDENT_VIEW_GROUP_ERROR;
                state.setAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE, s.getAssignment().getId());
            }
            state.setAttribute(STATE_MODE, mode);
        }
    }

    /**
     * Action is to show the graded assignment submission while keeping specific information private
     */
    public void doView_grade_private(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();

        state.setAttribute(VIEW_GRADE_SUBMISSION_ID, params.getString("submissionId"));

        // whether the user can access the Submission object
        if (getSubmission((String) state.getAttribute(VIEW_GRADE_SUBMISSION_ID), "doView_grade_private", state) != null) {
            state.setAttribute(STATE_MODE, MODE_STUDENT_VIEW_GRADE_PRIVATE);
        }

    } // doView_grade_private

    /**
     * Action is to show the student submissions
     */
    public void doReport_submissions(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_REPORT_SUBMISSIONS);
        state.setAttribute(SORTED_BY, SORTED_SUBMISSION_BY_LASTNAME);
        state.setAttribute(SORTED_SUBMISSION_BY, SORTED_GRADE_SUBMISSION_BY_LASTNAME);
        state.setAttribute(SORTED_ASC, Boolean.TRUE.toString());

    } // doReport_submissions

    /**
     *
     *
     */
    public void doAssignment_form(RunData data) {
        ParameterParser params = data.getParameters();
        //Added by Branden Visser: Grab the submission id from the query string
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String actualGradeSubmissionId = (String) params.getString("submissionId");

        log.debug("actualGradeSubmissionId = {}", actualGradeSubmissionId);

        String option = (String) params.getString("option");
        String fromView = (String) state.getAttribute(FROM_VIEW);
        if (option != null) {
            if ("post".equals(option)) {
                // post assignment
                doPost_assignment(data);
            } else if ("save".equals(option)) {
                // save assignment
                doSave_assignment(data);
            } else if ("reorder".equals(option)) {
                // reorder assignments
                doReorder_assignment(data);
            } else if ("preview".equals(option)) {
                // preview assignment
                doPreview_assignment(data);
            } else if ("cancel".equals(option)) {
                // cancel creating assignment
                doCancel_new_assignment(data);
            } else if ("canceledit".equals(option)) {
                // cancel editing assignment
                doCancel_edit_assignment(data);
            } else if ("attach".equals(option)) {
                // attachments
                doAttachmentsFrom(data, null);
            } else if ("removeAttachment".equals(option)) {
                // remove selected attachment
                doRemove_attachment(data);
            } else if ("removeAttachment_review".equals(option)) {
                // remove selected attachment
                doRemove_attachment(data);
                doSave_grade_submission_review(data);
            } else if ("modelAnswerAttach".equals(option)) {
                doAttachmentsFrom(data, "modelAnswer");
            } else if ("allPurposeAttach".equals(option)) {
                doAttachmentsFrom(data, "allPurpose");
            } else if ("view".equals(option)) {
                // view
                doView(data);
            } else if ("permissions".equals(option)) {
                // permissions
                doPermissions(data, null);
            } else if ("new".equals(option)) {
                doNew_assignment(data, null);
            } else if ("returngrade".equals(option)) {
                // return grading
                doReturn_grade_submission(data);
            } else if ("savegrade".equals(option)) {
                // save grading
                doSave_grade_submission(data);
            } else if ("savegrade_review".equals(option)) {
                // save review grading
                doSave_grade_submission_review(data);
            } else if ("submitgrade_review".equals(option)) {
                //we basically need to submit, save, and move the user to the next review (if available)
                if (data.getParameters().get("nextSubmissionId") != null) {
                    //go next
                    doPrev_back_next_submission_review(data, "next", true);
                } else if (data.getParameters().get("prevSubmissionId") != null) {
                    //go previous
                    doPrev_back_next_submission_review(data, "prev", true);
                } else {
                    //go back to the list
                    doPrev_back_next_submission_review(data, "back", true);
                }
            } else if ("toggleremove_review".equals(option)) {
                // save review grading
                doSave_toggle_remove_review(data);
            } else if ("previewgrade".equals(option)) {
                // preview grading
                doPreview_grade_submission(data);
            } else if ("cancelgrade".equals(option)) {
                // cancel grading
                doCancel_grade_submission(data);
            } else if ("cancelgrade_review".equals(option)) {
                // cancel grade review
                doCancel_edit_assignment(data);
            } else if ("cancelreorder".equals(option)) {
                // cancel reordering
                doCancel_reorder(data);
            } else if ("sortbygrouptitle".equals(option)) {
                // read input data
                setNewAssignmentParameters(data, true);

                // sort by group title
                doSortbygrouptitle(data);
            } else if ("sortbygroupdescription".equals(option)) {
                // read input data
                setNewAssignmentParameters(data, true);

                // sort group by description
                doSortbygroupdescription(data);
            } else if ("hide_instruction".equals(option)) {
                // hide the assignment instruction
                doHide_submission_assignment_instruction(data);
            } else if ("hide_instruction_review".equals(option)) {
                // hide the assignment instruction
                doHide_submission_assignment_instruction_review(data);
            } else if ("show_instruction".equals(option)) {
                // show the assignment instruction
                doShow_submission_assignment_instruction(data);
            } else if ("show_instruction_review".equals(option)) {
                // show the assignment instruction
                doShow_submission_assignment_instruction_review(data);
            } else if ("sortbygroupdescription".equals(option)) {
                // show the assignment instruction
                doShow_submission_assignment_instruction(data);
            } else if ("revise".equals(option) || "done".equals(option)) {
                // back from the preview mode
                doDone_preview_new_assignment(data);
            } else if ("prevsubmission".equals(option)) {
                // save and navigate to previous submission
                doPrev_back_next_submission(data, "prev");
            } else if ("nextsubmission".equals(option)) {
                // save and navigate to previous submission
                doPrev_back_next_submission(data, "next");
            } else if (FLAG_NEXT_UNGRADED.equals(option) || FLAG_PREV_UNGRADED.equals(option)) {
                // SAK-29314
                doPrev_back_next_submission(data, option);
            } else if ("prevsubmission_review".equals(option)) {
                // save and navigate to previous submission
                doPrev_back_next_submission_review(data, "prev", false);
            } else if ("nextsubmission_review".equals(option)) {
                // save and navigate to previous submission
                doPrev_back_next_submission_review(data, "next", false);
            } else if ("cancelgradesubmission".equals(option)) {
                if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(fromView)) {
                    doPrev_back_next_submission(data, "backListStudent");
                } else {
                    // save and navigate to previous submission
                    doPrev_back_next_submission(data, "back");
                }
            } else if ("cancelgradesubmission_review".equals(option)) {
                // save and navigate to previous submission
                doPrev_back_next_submission_review(data, "back", false);
            } else if ("reorderNavigation".equals(option)) {
                // save and do reorder
                doReorder(data);
            } else if ("options".equals(option)) {
                // go to the options view
                doOptions(data);
            }

        }
    }

    // added by Branden Visser - Check that the state is consistent
    private boolean checkSubmissionStateConsistency(SessionState state, String actualGradeSubmissionId) {
        String stateGradeSubmissionId = (String) state.getAttribute(GRADE_SUBMISSION_SUBMISSION_ID);
        log.debug("stateGradeSubmissionId = {}", stateGradeSubmissionId);
        boolean is_good = stateGradeSubmissionId.equals(actualGradeSubmissionId);
        if (!is_good) {
            log.warn("State is inconsistent! Aborting grade save.");
            addAlert(state, rb.getString("grading.alert.multiTab"));
        }
        return is_good;
    }

    /**
     * Action is to use when doAattchmentsadding requested, corresponding to chef_Assignments-new "eventSubmit_doAattchmentsadding" when "add attachments" is clicked
     */
    public void doAttachmentsFrom(RunData data, String from) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        doAttachments(data);

        // use the real attachment list
        if (state.getAttribute(STATE_MESSAGE) == null) {
            if (from != null && "modelAnswer".equals(from)) {
                state.setAttribute(ATTACHMENTS_FOR, MODELANSWER_ATTACHMENTS);
                state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(MODELANSWER_ATTACHMENTS));
                state.setAttribute(MODELANSWER, Boolean.TRUE);
            } else if (from != null && "allPurpose".equals(from)) {
                state.setAttribute(ATTACHMENTS_FOR, ALLPURPOSE_ATTACHMENTS);
                state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(ALLPURPOSE_ATTACHMENTS));
                state.setAttribute(ALLPURPOSE, Boolean.TRUE);
            } else if (from != null && "peerAttach".equals(from)) {
                state.setAttribute(ATTACHMENTS_FOR, PEER_ATTACHMENTS);
                state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(PEER_ATTACHMENTS));
                state.setAttribute(PEER_ASSESSMENT, Boolean.TRUE);
            }
        }
    }

    /**
     * put supplement item attachment info into state
     *
     * @param state
     * @param item
     * @param attachmentsKind
     */
    private void putSupplementItemAttachmentInfoIntoState(SessionState state, AssignmentSupplementItemWithAttachment item, String attachmentsKind) {
        List refs = new ArrayList();

        if (item != null) {
            // get reference list
            Set<AssignmentSupplementItemAttachment> aSet = item.getAttachmentSet();
            if (aSet != null && aSet.size() > 0) {
                for (Iterator<AssignmentSupplementItemAttachment> aIterator = aSet.iterator(); aIterator.hasNext(); ) {
                    AssignmentSupplementItemAttachment att = aIterator.next();
                    // add reference
                    refs.add(entityManager.newReference(att.getAttachmentId()));
                }
                state.setAttribute(attachmentsKind, refs);
            }
        }
    }

    /**
     * put supplement item attachment state attribute value into context
     *
     * @param state
     * @param context
     * @param attachmentsKind
     */
    private void putSupplementItemAttachmentStateIntoContext(SessionState state, Context context, String attachmentsKind) {
        List refs = new ArrayList();

        String attachmentsFor = (String) state.getAttribute(ATTACHMENTS_FOR);
        boolean wasProcessingAttachments = false;
        if (attachmentsFor != null && attachmentsFor.equals(attachmentsKind)) {
            wasProcessingAttachments = true;
            ToolSession session = sessionManager.getCurrentToolSession();
            if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
                    session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
                refs = (List) session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
                // set the correct state variable
                state.setAttribute(attachmentsKind, refs);
            }
            session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
            session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);

            state.removeAttribute(ATTACHMENTS_FOR);
        }

        // show attachments content
        if (state.getAttribute(attachmentsKind) != null) {
            context.put(attachmentsKind, state.getAttribute(attachmentsKind));
        }

        // this is to keep the proper node div open
        // Only set attachments_for if this is the type that was being processed for attachments
        if (wasProcessingAttachments) {
            // Map the attachment kind to the corresponding node name for JavaScript
            String nodeName = attachmentsKind;
            if (MODELANSWER_ATTACHMENTS.equals(attachmentsKind)) {
                nodeName = "modelanswer";
            } else if (ALLPURPOSE_ATTACHMENTS.equals(attachmentsKind)) {
                nodeName = "allPurpose";
            }
            context.put("attachments_for", nodeName);
        }
    }

    /**
     * Action is to use when doAattchmentsadding requested, corresponding to chef_Assignments-new "eventSubmit_doAattchmentsadding" when "add attachments" is clicked
     */
    public void doAttachments(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        // determines if the file picker can only add a single attachment
        boolean singleAttachment = false;

        // when content-review is enabled, the inline text will have an associated attachment. It should be omitted from the file picker
        Assignment assignment = null;
        String assignmentTitle = "";
        String assignmentUser = "";
        boolean omitInlineAttachments = false;

        String mode = (String) state.getAttribute(STATE_MODE);
        if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode)) {
            // save the current input before leaving the page
            saveSubmitInputs(state, params);

            // Restrict file picker configuration if using content-review (Turnitin):
            String assignmentRef = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
            assignment = getAssignment(assignmentRef, "doAttachments", state);
            if (assignment != null) {
                assignmentUser = userDirectoryService.getCurrentUser().getDisplayName();
                if (assignment.getContentReview()) {
                    state.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, FilePickerHelper.CARDINALITY_MULTIPLE);
                    state.setAttribute(FilePickerHelper.FILE_PICKER_SHOW_URL, Boolean.FALSE);
                }

                if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
                    singleAttachment = true;
                }

                // need also to upload local file if any
                doAttachUpload(data, false);

                // TODO: file picker to save in dropbox? -ggolden
                // User[] users = { userDirectoryService.getCurrentUser() };
                // state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);

                // Always omit inline attachments. Even if content-review is not enabled,
                // this could be a resubmission to an assignment that was previously content-review enabled,
                // in which case the file will be present and should be omitted.
                omitInlineAttachments = true;
            }
        } else if (MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode)) {
            setNewAssignmentParameters(data, false);
            assignmentTitle = (String) state.getAttribute(NEW_ASSIGNMENT_TITLE);
            assignmentUser = userDirectoryService.getCurrentUser().getDisplayName();
        // Submission grading page attachment
        } else if (MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode)) {
            readGradeForm(data, state, "read");
            String assignmentRef = (String) state.getAttribute(GRADE_SUBMISSION_ASSIGNMENT_ID);
            assignment = getAssignment(assignmentRef, "doAttachments", state);
            assignmentUser = (String) state.getAttribute(GRADE_SUBMISSION_SUBMITTERS_NAMES);
        } else if (MODE_STUDENT_REVIEW_EDIT.equals(mode)) {
            saveReviewGradeForm(data, state, "save");
        }

        //Set the title and override for anonymous assignment
        if (assignment != null) {
        	assignmentTitle = assignment.getTitle();
        	if (assignmentService.assignmentUsesAnonymousGrading(assignment)) {
        		assignmentUser = rb.getString("grading.anonymous.title");
        	}
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            // get into helper mode with this helper tool
            startHelper(data.getRequest(), "sakai.filepicker");

            if (singleAttachment) {
                // SAK-27595 - added a resources file picker for single uploaded file only assignments; we limit it here to accept a maximum of 1 file
                state.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, 1);
                state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getFormattedMessage("gen.addatttoassig.singular", assignmentTitle, assignmentUser));
            } else {
                state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getFormattedMessage("gen.addatttoassig", assignmentTitle, assignmentUser));
            }
            state.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT, rb.getString("gen.addatttoassiginstr"));

            // If there is no title or user set, use the default text
            if ("".equals(assignmentTitle) && "".equals(assignmentUser)) {
                state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("gen.addatt"));
            }

            // process existing attachments
            List<Reference> attachments = (List<Reference>) state.getAttribute(ATTACHMENTS);
            if (singleAttachment && attachments != null && attachments.size() > 1) {
                // multiple attachments -> Single Uploaded File Only
                List newSingleAttachmentList = entityManager.newReferenceList();
                state.setAttribute("newSingleAttachmentList", newSingleAttachmentList);
                state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, newSingleAttachmentList);
            } else {
                // use the real attachment list
                // but omit the inline submission under conditions determined in the logic above
                if (omitInlineAttachments && assignment != null) {
                    attachments = getNonInlineAttachments(state, assignment);
                    state.setAttribute(ATTACHMENTS, attachments);
                }
                state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, attachments);
            }
        }
    }

    /**
     * saves the current input before navigating off to other pages
     *
     * @param state
     * @param params
     */
    private void saveSubmitInputs(SessionState state, ParameterParser params) {
        // retrieve the submission text (as formatted text)
        String text = processFormattedTextFromBrowser(state, params.getCleanString(VIEW_SUBMISSION_TEXT), true);
        state.setAttribute(VIEW_SUBMISSION_TEXT, text);

        String timeSpent = params.getCleanString(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        if(timeSpent == null) {
            timeSpent = (String)state.getAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
        }
        
        state.setAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT, timeSpent);
        if(params.getString(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT) != null) {
            state.setAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT, "true");

        } else {
            state.removeAttribute(AssignmentConstants.SUBMISSION_REVIEW_SERVICE_EULA_AGREEMENT);
        }

        String assignmentRef = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        try {
            Assignment assignment = assignmentService.getAssignment(assignmentRef);
            if (assignment.getIsGroup()) {
                String[] groupChoice = params.getStrings("selectedGroups");
                if (groupChoice != null && groupChoice.length != 0) {
                    if (groupChoice.length > 1) {
                        state.setAttribute(VIEW_SUBMISSION_GROUP, null);
                        addAlert(state, rb.getString("java.alert.youchoosegroup"));
                    } else {
                        state.setAttribute(VIEW_SUBMISSION_GROUP, groupChoice[0]);
                    }
                } else {
                    state.setAttribute(VIEW_SUBMISSION_GROUP, null);
                    addAlert(state, rb.getString("java.alert.youchoosegroup"));
                }
                String original_group_id = params.getString("originalGroup") == null
                        || params.getString("originalGroup").trim().length() == 0 ? null : params.getString("originalGroup");

                if (original_group_id != null) {
                    state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, original_group_id);
                } else {
                    state.setAttribute(VIEW_SUBMISSION_ORIGINAL_GROUP, null);
                }
            }

        } catch (PermissionException p) {
            log.debug(this + " :saveSubmitInputs permission error getting assignment. ");
        } catch (IdUnusedException e) {
        }
    }

    /**
     * read review grade information form and see if any grading information has been changed
     *
     * @param data
     * @param state
     * @param gradeOption
     * @return
     */
    public boolean saveReviewGradeForm(RunData data, SessionState state, String gradeOption) {

	    boolean preExistingAlerts = state.getAttribute(STATE_MESSAGE) != null;

        ParameterParser params = data.getParameters();
        String submissionId = params.getString("submissionId");
        if (submissionId != null) {
            AssignmentSubmission s = getSubmission(submissionId, "saveReviewGradeForm", state);
            if (s != null) {
                submissionId = s.getId();//using the id instead of the reference
            }

            Assignment assignment = s.getAssignment();
            String assessorUserId = assignmentService.getSubmitterIdForAssignment(assignment, userDirectoryService.getCurrentUser().getId());

            if (state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID) != null && !state.getAttribute(PEER_ASSESSMENT_ASSESSOR_ID).equals(assessorUserId)) {
                //this is only set during the read only view, so just return
                return false;
            }

            //call the DB to make sure this user can edit this assessment, otherwise it wouldn't exist
            PeerAssessmentItem item = assignmentPeerAssessmentService.getPeerAssessmentItem(submissionId, assessorUserId);
            if (item != null) {
                //find the original assessment item and compare to see if it has changed
                //if so, save it
                boolean changed = false;

                if (submissionId.equals(item.getId().getSubmissionId())
                        && assessorUserId.equals(item.getId().getAssessorUserId())) {
                    //Grade
                    String g = StringUtils.trimToNull(params.getCleanString(GRADE_SUBMISSION_GRADE));
                    Integer score = item.getScore();
                    if (StringUtils.isNotEmpty(g)) {
                        try {
                            String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
                            if (assignmentId == null) {
                                addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));
                            } else {
                                Assignment a = getAssignment(assignmentId, "saveReviewGradeForm", state);
                                if (a == null) {
                                    addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));
                                } else {
                                    int factor = a.getScaleFactor();
                                    int dec = (int) Math.log10(factor);
                                    validPointGrade(state, g, factor);
                                    if (state.getAttribute(STATE_MESSAGE) == null) {
	                                    NumberFormat nbFormat = formattedText.getNumberFormat(dec, dec, false);
	                                    DecimalFormat dcformat = (DecimalFormat) nbFormat;
	                                    Double dScore = dcformat.parse(g).doubleValue();
	
	                                    if (dScore < 0) {
	                                        addAlert(state, rb.getString("peerassessment.alert.saveinvalidscore"));
	                                    } else if (dScore <= a.getMaxGradePoint() / (double) factor) {
	                                        //scores are saved as whole values
	                                        //so a score of 1.3 would be stored as 13
	                                        score = (int) Math.round(dScore * factor);
	                                    } else {
	                                        addAlert(state, rb.getFormattedMessage("plesuse4", g, a.getMaxGradePoint() / (double) factor));
	                                    }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            addAlert(state, rb.getString("peerassessment.alert.saveinvalidscore"));
                        }
                    } else {
                        score = null;
                    }
                    boolean scoreChanged = false;
                    if (score != null && item.getScore() == null
                            || score == null && item.getScore() != null
                            || (score != null && item.getScore() != null && !score.equals(item.getScore()))) {
                        //Score changed
                        changed = true;
                        scoreChanged = true;
                        item.setScore(score);
                    }

                    //Comment:
                    String feedbackComment = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_COMMENT), true);
                    if (feedbackComment != null && item.getComment() == null
                            || feedbackComment == null && item.getComment() != null
                            || (feedbackComment != null && item.getComment() != null && !feedbackComment.equals(item.getComment()))) {
                        //comment changed
                        changed = true;
                        item.setComment(feedbackComment);
                    }

                    /* Attachments */
                    // Get attachments already added to this item
                    List<PeerAssessmentAttachment> savedAttachments = assignmentPeerAssessmentService.getPeerAssessmentAttachments(submissionId, assessorUserId);

                    // get attachments added to the review form
                    List submittedAttachmentRefs = null;

                    if (state.getAttribute(PEER_ATTACHMENTS) != null && !((List) state.getAttribute(PEER_ATTACHMENTS)).isEmpty()) {
                        submittedAttachmentRefs = (List) state.getAttribute(PEER_ATTACHMENTS);
                    }

                    boolean attachmentsChanged = false;
                    // if review was saved/submitted with attachments added
                    if (submittedAttachmentRefs != null && !submittedAttachmentRefs.isEmpty()) {
                        // build set of attachment reference ids from review form
                        List<PeerAssessmentAttachment> attachmentsFromForm = new ArrayList<>();
                        for (Object attachment : submittedAttachmentRefs) {
                            // Try to get existing attachment first
                            PeerAssessmentAttachment peerAssessmentAttachment = assignmentPeerAssessmentService.getPeerAssessmentAttachment(item.getId().getSubmissionId(), item.getId().getAssessorUserId(), ((Reference) attachment).getId());
                            if (peerAssessmentAttachment != null) {
                                attachmentsFromForm.add(peerAssessmentAttachment);
                            } else {
                                // Build a new attachment
                                peerAssessmentAttachment = new PeerAssessmentAttachment(item.getId().getSubmissionId(), item.getId().getAssessorUserId(), ((Reference) attachment).getId());
                                attachmentsFromForm.add(peerAssessmentAttachment);
                            }
                        }

                        // check if there were previously saved attachments
                        if (savedAttachments != null && !savedAttachments.isEmpty()) {
                            // Find if any previously saved attachments need to be removed from item's attachments
                            List<PeerAssessmentAttachment> attachmentsToDelete = new ArrayList<>(savedAttachments);
                            attachmentsToDelete.removeAll(attachmentsFromForm);

                            // remove no longer needed attachments from peer review item's attachments
                            if (!attachmentsToDelete.isEmpty()) {
                                // remove previously saved attachments
                                attachmentsToDelete.forEach(assignmentPeerAssessmentService::removePeerAttachment);
                            }
                        }
                        item.setAttachmentList(attachmentsFromForm);
                        attachmentsChanged = true;
                    } else {
                        // no attachments added to the review so we need to clear out previously saved attachments
                        if (savedAttachments != null && !savedAttachments.isEmpty()) {
                            savedAttachments.forEach(assignmentPeerAssessmentService::removePeerAttachment);
                            attachmentsChanged = true;
                        }
                        item.setAttachmentList(new ArrayList<>());
                    }

                    //Submitted
                    if ("submit".equals(gradeOption)) {
                        if (item.getScore() != null || (item.getComment() != null && !"".equals(item.getComment().trim()))) {
                            item.setSubmitted(true);
                            changed = true;
                        } else {
                            addAlert(state, rb.getString("peerassessment.alert.savenoscorecomment"));
                        }
                    }
                    if (("submit".equals(gradeOption) || "save".equals(gradeOption))) {
                        if (changed && state.getAttribute(STATE_MESSAGE) == null) {
                            String event = "submit".equals(gradeOption) ? AssignmentConstants.EVENT_SUBMIT_PEER_REVIEW : AssignmentConstants.EVENT_SAVE_PEER_REVIEW;
                            //save this in the DB
                            assignmentPeerAssessmentService.savePeerAssessmentItem(item, assignment.getContext(), event);
                            if (scoreChanged) {
                                //need to re-calcuate the overall score:
                                boolean saved = assignmentPeerAssessmentService.updateScore(submissionId, assessorUserId);
                                if (saved) {
                                    //we need to make sure the GB is updated correctly (or removed)
                                    String assignmentId = (String) state.getAttribute(VIEW_ASSIGNMENT_ID);
                                    if (assignmentId != null) {
                                        Assignment a = getAssignment(assignmentId, "saveReviewGradeForm", state);
                                        if (a != null) {
                                            String aReference = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
                                            String associateGradebookAssignment = a.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                                            // update grade in gradebook
                                            String siteId = assignment.getContext();
                                            updateGradebookAssociation(state, siteId, aReference, associateGradebookAssignment, submissionId, "update");
                                        }
                                    }
                                }
                            }
                            state.setAttribute(GRADE_SUBMISSION_DONE, Boolean.TRUE);
                            if ("submit".equals(gradeOption)) {
                                state.setAttribute(GRADE_SUBMISSION_SUBMIT, Boolean.TRUE);
                            }
                        }
                        if (attachmentsChanged && !preExistingAlerts) {
                            //save new attachments to the DB as long as there weren't any pre-existing alerts from prior to the form validation
                            assignmentPeerAssessmentService.savePeerAssessmentAttachments(item);
                        }
                    }

                    //update session state:
                    List<PeerAssessmentItem> peerAssessmentItems = (List<PeerAssessmentItem>) state.getAttribute(PEER_ASSESSMENT_ITEMS);
                    if (peerAssessmentItems != null) {
                        for (int i = 0; i < peerAssessmentItems.size(); i++) {
                            PeerAssessmentItem sItem = peerAssessmentItems.get(i);
                            if (sItem.getId().getSubmissionId().equals(item.getId().getSubmissionId())
                                    && sItem.getId().getAssessorUserId().equals(item.getId().getAssessorUserId())) {
                                //found it, just update it
                                peerAssessmentItems.set(i, item);
                                state.setAttribute(PEER_ASSESSMENT_ITEMS, peerAssessmentItems);
                                break;
                            }
                        }
                    }

                }

                return changed;
            } else {
                addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));
            }
        } else {
            addAlert(state, rb.getString("peerassessment.alert.saveerrorunkown"));

        }
        return false;
    }

    /**
     * read grade information form and see if any grading information has been changed
     *
     * @param data
     * @param state
     * @param gradeOption
     * @return
     */
    public boolean readGradeForm(RunData data, SessionState state, String gradeOption) {
        // whether user has changed anything from previous grading information
        boolean hasChange = false;

        ParameterParser params = data.getParameters();
        String sId = params.getString("submissionId");

        //Added by Branden Visser - Check that the state is consistent
        if (!checkSubmissionStateConsistency(state, sId)) {
            return false;
        }

        AssignmentSubmission submission = getSubmission(sId, "readGradeForm", state);

        // security check for allowing grading submission or not
        if (assignmentService.allowGradeSubmission(sId)) {

            String feedbackComment = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_COMMENT), true);
            // comment value changed?
            hasChange = submission != null && valueDiffFromStateAttribute(state, feedbackComment, submission.getFeedbackComment());
            if (feedbackComment != null) {
                state.setAttribute(GRADE_SUBMISSION_FEEDBACK_COMMENT, feedbackComment);
            }

            String privateNotes = processFormattedTextFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_PRIVATE_NOTES), true);
            // private notes value changed?
            hasChange = !hasChange && submission != null ? valueDiffFromStateAttribute(state, privateNotes, submission.getPrivateNotes()) : hasChange;
            if (privateNotes != null) {
                state.setAttribute(GRADE_SUBMISSION_PRIVATE_NOTES, privateNotes);
            }

            String feedbackText = processAssignmentFeedbackFromBrowser(state, params.getCleanString(GRADE_SUBMISSION_FEEDBACK_TEXT));
            // feedbackText value changed?
            hasChange = !hasChange && submission != null ? valueDiffFromStateAttribute(state, feedbackText, submission.getFeedbackText()) : hasChange;
            if (feedbackText != null) {
                state.setAttribute(GRADE_SUBMISSION_FEEDBACK_TEXT, feedbackText);
            }

            // any change inside attachment list?
            if (!hasChange && submission != null) {
                List<Reference> inputAttachments = (List<Reference>) state.getAttribute(ATTACHMENTS);
                Set<String> stateAttachments = submission.getFeedbackAttachments();
                Set<String> inputAttachmentsRefs = inputAttachments.stream().map(Reference::getReference).collect(Collectors.toSet());

                if (!stateAttachments.equals(inputAttachmentsRefs)) {
                    hasChange = true;
                }
            }
            state.setAttribute(GRADE_SUBMISSION_FEEDBACK_ATTACHMENT, state.getAttribute(ATTACHMENTS));

            String g = StringUtils.trimToNull(params.getCleanString(GRADE_SUBMISSION_GRADE));

            if (submission != null) {
                Assignment a = submission.getAssignment();
                int factor = a.getScaleFactor() != null ? a.getScaleFactor() : assignmentService.getScaleFactor();
                Assignment.GradeType typeOfGrade = a.getTypeOfGrade();

                // any change in grade. Do not check for ungraded assignment type
                if (!hasChange && typeOfGrade != Assignment.GradeType.UNGRADED_GRADE_TYPE) {
                    if (typeOfGrade == Assignment.GradeType.SCORE_GRADE_TYPE) {
                        String currentGrade = submission.getGrade();

                        String decSeparator = formattedText.getDecimalSeparator();

                        if (currentGrade != null && currentGrade.contains(decSeparator)) {
                            currentGrade = scalePointGrade(state, submission.getGrade(), factor);
                        }
                        hasChange = valueDiffFromStateAttribute(state, scalePointGrade(state, g, factor), currentGrade);
                    } else {
                        hasChange = valueDiffFromStateAttribute(state, g, submission.getGrade());
                    }
                }
                if (g != null) {
                    state.setAttribute(GRADE_SUBMISSION_GRADE, g);
                } else {
                    state.removeAttribute(GRADE_SUBMISSION_GRADE);
                }

                // for points grading, one have to enter number as the points
                String grade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE);

                // do grade validation only for Assignment with Grade tool
                if (typeOfGrade == Assignment.GradeType.SCORE_GRADE_TYPE) {
                    if ((grade != null)) {
                        // the preview grade process might already scaled up the grade by "factor"
                        if (!((String) state.getAttribute(STATE_MODE)).equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION)) {
                            if (state.getAttribute(STATE_MESSAGE) == null) {
                                validPointGrade(state, grade, factor);
                                int maxGrade = a.getMaxGradePoint();
                                try {
                                    if (state.getAttribute(STATE_MESSAGE) == null && Integer.parseInt(scalePointGrade(state, grade, factor)) > maxGrade) {
                                        if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null) {
                                            // alert user first when he enters grade bigger than max scale
                                            addAlert(state, rb.getFormattedMessage("grad2", grade, displayGrade(state, String.valueOf(maxGrade), factor)));
                                            state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
                                        } else {
                                            // remove the alert once user confirms he wants to give student higher grade
                                            state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    alertInvalidPoint(state, grade, factor);
                                    log.warn(this + ":readGradeForm " + e.getMessage());
                                }
                            }

                            state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
                        }
                    }
                }

                // if ungraded and grade type is not "ungraded" type
                if ((grade == null || UNGRADED_GRADE_STRING.equals(grade)) && (typeOfGrade != Assignment.GradeType.UNGRADED_GRADE_TYPE) && "release".equals(gradeOption)) {
                    addAlert(state, rb.getString("plespethe2"));
                }

                // check for grade overrides (not applicable when anonymous grading)
                if (AssignmentToolUtils.allowGroupOverrides(a, assignmentService)) {
                    HashMap<String, String> scaledValues = new HashMap<String, String>();
                    Set<AssignmentSubmissionSubmitter> submitters = submission.getSubmitters();
                    for (AssignmentSubmissionSubmitter submitter : submitters) {
                        String ug = StringUtils.trimToNull(params.getCleanString(GRADE_SUBMISSION_GRADE + "_" + submitter.getSubmitter()));
                        if ("null".equals(ug)) ug = null;
                        if (!hasChange && typeOfGrade != Assignment.GradeType.UNGRADED_GRADE_TYPE) {
                            hasChange = valueDiffFromStateAttribute(state, ug, submitter.getGrade());
                        }
                        if (ug == null) {
                            state.removeAttribute(GRADE_SUBMISSION_GRADE + "_" + submitter.getSubmitter());
                        } else {
                            state.setAttribute(GRADE_SUBMISSION_GRADE + "_" + submitter.getSubmitter(), ug);
                        }
                        // for points grading, one have to enter number as the points
                        String ugrade = (String) state.getAttribute(GRADE_SUBMISSION_GRADE + "_" + submitter.getSubmitter());
                        // do grade validation only for Assignment with Grade tool
                        if (typeOfGrade == Assignment.GradeType.SCORE_GRADE_TYPE) {
                            if (ugrade != null && !(ugrade.equals("null"))) {
                                // the preview grade process might already scaled up the grade by "factor"
                                if (!((String) state.getAttribute(STATE_MODE)).equals(MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION)) {
                                    validPointGrade(state, ugrade, factor);
                                    if (state.getAttribute(STATE_MESSAGE) == null) {
                                        int maxGrade = a.getMaxGradePoint();
                                        try {
                                            if (Integer.parseInt(scalePointGrade(state, ugrade, factor)) > maxGrade) {
                                                if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null) {
                                                    // alert user first when he enters grade bigger than max scale
                                                    addAlert(state, rb.getFormattedMessage("grad2", ugrade, displayGrade(state, String.valueOf(maxGrade), factor)));
                                                    state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
                                                } else {
                                                    // remove the alert once user confirms he wants to give student higher grade
                                                    state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
                                                }
                                            }
                                        } catch (NumberFormatException e) {
                                            alertInvalidPoint(state, ugrade, factor);
                                            log.warn(this + ":readGradeForm User " + e.getMessage());
                                        }
                                    }
                                    scaledValues.put(GRADE_SUBMISSION_GRADE + "_" + submitter.getSubmitter(), scalePointGrade(state, ugrade, factor));
                                }
                            }
                        }
                    }
                    // SAK-28182 If all grades are right place scaled values in state
                    if (state.getAttribute(STATE_MESSAGE) == null) {
                        for (Map.Entry<String, String> entry : scaledValues.entrySet()) {
                            state.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }
                }

                // allow resubmit number and due time
                if (params.getString("allowResToggle") != null && params.getString(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) != null) {
                    // read in allowResubmit params
                    readAllowResubmitParams(params, state, submission.getProperties());
                } else {
                    state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                    state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);

                    if (!"read".equals(gradeOption)) {
                        resetAllowResubmitParams(state);
                    }
                }
                if (params.getString("allowExtensionToggle") != null){  //if the Allow Extension box is checked, we can read in the allowExtension params.
                    readAllowExtensionParams(params, state, submission.getProperties());
                } else {    //if it's not checked, State should have no data about it.
                    state.removeAttribute(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME);
                    if (!"read".equals(gradeOption)) {
                        resetAllowExtensionParams(state);
                    }
                }
                // record whether the resubmission options has been changed or not
                hasChange = hasChange || change_resubmit_option(state, submission.getProperties());
                hasChange = hasChange || propertyValueChanged(state, submission.getProperties(), AssignmentConstants.ALLOW_EXTENSION_CLOSETIME);    //same for Extension.
                if (state.getAttribute(STATE_MESSAGE) == null) {
                    grade = (typeOfGrade == Assignment.GradeType.SCORE_GRADE_TYPE) ? scalePointGrade(state, grade, factor) : grade;
                    state.setAttribute(GRADE_SUBMISSION_GRADE, grade);
                }
            }
        } else {
            // generate alert
            addAlert(state, rb.getFormattedMessage("not_allowed_to_grade_submission", sId));
        }

        return hasChange;
    }

    /**
     * whether the current input value is different from existing oldValue
     *
     * @param state
     * @param value
     * @param oldValue
     * @return
     */
    private boolean valueDiffFromStateAttribute(SessionState state, String value, String oldValue) {
        boolean rv = false;
        value = StringUtils.trimToNull(value);
        oldValue = StringUtils.trimToNull(oldValue);
        if (oldValue == null && value != null
                || oldValue != null && value == null
                || oldValue != null && !normalizeAttributeSpaces(oldValue).equals(normalizeAttributeSpaces(value))) {
            rv = true;
        }
        return rv;
    }

    /**
     * Remove extraneous spaces between tag attributes, to allow a better
     * equality test in valueDiffFromStateAttribute.
     *
     * @param s the input string, to be normalized
     * @return the normalized string.
     */
    private String normalizeAttributeSpaces(String s) {
        if (s == null) {
            return null;
        }
        Pattern p = Pattern.compile("(=\".*?\")( +)");
        Matcher m = p.matcher(s);
        return m.replaceAll("$1 ");
    }

    /**
     * read in the resubmit parameters into state variables
     *
     * @param params
     * @param state
     * @return the time set for the resubmit close OR null if it is not set
     */
    private Instant readAllowResubmitParams(ParameterParser params, SessionState state, Map<String, String> properties) {
        Instant resubmitCloseTime = null;
        String allowResubmitNumberString = params.getString(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
        state.setAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, params.getString(AssignmentConstants.ALLOW_RESUBMIT_NUMBER));

        if (allowResubmitNumberString != null && Integer.parseInt(allowResubmitNumberString) != 0) {
            int closeMonth = Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSE_MONTH));
            state.setAttribute(ALLOW_RESUBMIT_CLOSE_MONTH, closeMonth);
            int closeDay = Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSE_DAY));
            state.setAttribute(ALLOW_RESUBMIT_CLOSE_DAY, closeDay);
            int closeYear = Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSE_YEAR));
            state.setAttribute(ALLOW_RESUBMIT_CLOSE_YEAR, closeYear);
            int closeHour = Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSE_HOUR));
            state.setAttribute(ALLOW_RESUBMIT_CLOSE_HOUR, closeHour);
            int closeMin = Integer.valueOf(params.getString(ALLOW_RESUBMIT_CLOSE_MIN));
            state.setAttribute(ALLOW_RESUBMIT_CLOSE_MIN, closeMin);
            resubmitCloseTime = LocalDateTime.of(closeYear, closeMonth, closeDay, closeHour, closeMin, 0, 0).atZone(userTimeService.getLocalTimeZone().toZoneId()).toInstant();
            state.setAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(resubmitCloseTime.toEpochMilli()));
            // no need to show alert if the resubmission setting has not changed
            if (properties == null || change_resubmit_option(state, properties)) {
                // validate date
                if (resubmitCloseTime.isBefore(Instant.now()) && state.getAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE) == null) {
                    state.setAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE, Boolean.TRUE);
                } else {
                    // clean the attribute after user confirm
                    state.removeAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE);
                }
                if (state.getAttribute(NEW_ASSIGNMENT_PAST_CLOSE_DATE) != null) {
                    addAlert(state, rb.getString("acesubdea5"));
                }
                if (!DateFormatterUtil.checkDate(closeDay, closeMonth, closeYear)) {
                    addAlert(state, rb.getFormattedMessage("date.invalid", rb.getString("date.resubmission.closedate")));
                }
            }
        } else {
            // reset the state attributes
            resetAllowResubmitParams(state);
        }
        return resubmitCloseTime;
    }

    protected void resetAllowResubmitParams(SessionState state) {
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_MONTH, state.getAttribute(NEW_ASSIGNMENT_CLOSE_MONTH));
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_DAY, state.getAttribute(NEW_ASSIGNMENT_CLOSE_DAY));
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_YEAR, state.getAttribute(NEW_ASSIGNMENT_CLOSE_YEAR));
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_HOUR, state.getAttribute(NEW_ASSIGNMENT_CLOSE_HOUR));
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_MIN, state.getAttribute(NEW_ASSIGNMENT_CLOSE_MIN));
        state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
        state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
    }

    private Instant readAllowExtensionParams (ParameterParser params, SessionState state, Map<String, String> properties) { //put Param data about extensions into the State.
        Instant extensionCloseTime = null;
        int closeMonth = Integer.valueOf(params.getString(ALLOW_EXTENSION_CLOSE_MONTH)); //parse the datepicker's data into Ints and put them in the State individually
        state.setAttribute(ALLOW_EXTENSION_CLOSE_MONTH, closeMonth);
        int closeDay = Integer.valueOf(params.getString(ALLOW_EXTENSION_CLOSE_DAY));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_DAY, closeDay);
        int closeYear = Integer.valueOf(params.getString(ALLOW_EXTENSION_CLOSE_YEAR));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_YEAR, closeYear);
        int closeHour = Integer.valueOf(params.getString(ALLOW_EXTENSION_CLOSE_HOUR));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_HOUR, closeHour);
        int closeMin = Integer.valueOf(params.getString(ALLOW_EXTENSION_CLOSE_MIN));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_MIN, closeMin);
        extensionCloseTime = LocalDateTime.of(closeYear, closeMonth, closeDay, closeHour, closeMin, 0, 0).atZone(timeService.getLocalTimeZone().toZoneId()).toInstant(); //turn the Ints we just made into a single Instant time
        state.setAttribute(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, String.valueOf(extensionCloseTime.toEpochMilli()));
        return extensionCloseTime;
    }

    protected void resetAllowExtensionParams (SessionState state){
        state.setAttribute(ALLOW_EXTENSION_CLOSE_MONTH, state.getAttribute(NEW_ASSIGNMENT_CLOSE_MONTH));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_DAY, state.getAttribute(NEW_ASSIGNMENT_CLOSE_DAY));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_YEAR, state.getAttribute(NEW_ASSIGNMENT_CLOSE_YEAR));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_HOUR, state.getAttribute(NEW_ASSIGNMENT_CLOSE_HOUR));
        state.setAttribute(ALLOW_EXTENSION_CLOSE_MIN, state.getAttribute(NEW_ASSIGNMENT_CLOSE_MIN));
        state.removeAttribute(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME);
    }

    /**
     * Populate the state object, if needed - override to do something!
     */
    protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData data) {
        super.initState(state, portlet, data);

        String siteId = toolManager.getCurrentPlacement().getContext();

        // show the list of assignment view first
        if (state.getAttribute(STATE_SELECTED_VIEW) == null) {
            state.setAttribute(STATE_SELECTED_VIEW, MODE_LIST_ASSIGNMENTS);
        }

        if (state.getAttribute(STATE_USER) == null) {
            state.setAttribute(STATE_USER, userDirectoryService.getCurrentUser());
        }

        if (state.getAttribute(SUBMISSIONS_SEARCH_ONLY) == null || state.getAttribute(SHOW_TAGS_STUDENT) == null) {
            String propValueSubmissions = null;
            String propValueTags = null;
            // save the option into tool configuration
            try {
                Site site = siteService.getSite(siteId);
                ToolConfiguration tc = site.getToolForCommonId(AssignmentConstants.TOOL_ID);
                propValueSubmissions = tc.getPlacementConfig().getProperty(SUBMISSIONS_SEARCH_ONLY);
                propValueTags = tc.getPlacementConfig().getProperty(SHOW_TAGS_STUDENT);
            } catch (IdUnusedException e) {
                log.warn(this + ":init()  Cannot find site with id " + siteId);
            }
            state.setAttribute(SUBMISSIONS_SEARCH_ONLY, propValueSubmissions == null ? Boolean.FALSE : Boolean.valueOf(propValueSubmissions));
            state.setAttribute(SHOW_TAGS_STUDENT, propValueTags == null ? Boolean.FALSE : Boolean.valueOf(propValueTags));
        }

        /** The calendar tool  */
        if (state.getAttribute(CALENDAR_TOOL_EXIST) == null) {
            if (!siteHasTool(siteId, calendarService.getToolId())) {
                state.setAttribute(CALENDAR_TOOL_EXIST, Boolean.FALSE);
                state.removeAttribute(CALENDAR);
            } else {
                state.setAttribute(CALENDAR_TOOL_EXIST, Boolean.TRUE);
                if (state.getAttribute(CALENDAR) == null) {
                    state.setAttribute(CALENDAR_TOOL_EXIST, Boolean.TRUE);

                    String calendarId = serverConfigurationService.getString("calendar", null);
                    if (calendarId == null) {
                        calendarId = calendarService.calendarReference(siteId, SiteService.MAIN_CONTAINER);
                        try {
                            state.setAttribute(CALENDAR, calendarService.getCalendar(calendarId));
                        } catch (IdUnusedException e) {
                            state.removeAttribute(CALENDAR);
                            log.info(this + ":initState No calendar found for site " + siteId + " " + e.getMessage());
                        } catch (PermissionException e) {
                            state.removeAttribute(CALENDAR);
                            log.info(this + ":initState No permission to get the calender. " + e.getMessage());
                        } catch (Exception ex) {
                            state.removeAttribute(CALENDAR);
                            log.info(this + ":initState Assignment : Action : init state : calendar exception : " + ex.getMessage());

                        }
                    }
                }
            }
        }

        /** Additional Calendar tool */
        // Setting this attribute to true or false currently makes no difference as it is never checked for true or false.
        // true: means the additional calendar is ready to be used with assignments.
        // false: means the tool may not be deployed at all or may be at the site but not ready to be used.
        if (state.getAttribute(ADDITIONAL_CALENDAR_TOOL_READY) == null) {
            // Get a handle to the Google calendar service class from the Component Manager. It will be null if not deployed.
            CalendarService additionalCalendarService = (CalendarService) ComponentManager.get(CalendarService.ADDITIONAL_CALENDAR);
            if (additionalCalendarService != null) {
                // If tool is not used/used on this site, we set the appropriate flag in the state.
                if (!siteHasTool(siteId, additionalCalendarService.getToolId())) {
                    state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.FALSE);
                    state.removeAttribute(ADDITIONAL_CALENDAR);
                } else {    // Also check that this calendar has been fully created (initialized) in the additional calendar service.
                    if (additionalCalendarService.isCalendarToolInitialized(siteId)) {
                        state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.TRUE); // Alternate calendar ready for events.
                        if (state.getAttribute(ADDITIONAL_CALENDAR) == null) {
                            try {
                                state.setAttribute(ADDITIONAL_CALENDAR, additionalCalendarService.getCalendar(null));
                            } catch (IdUnusedException e) {
                                log.info(this + ":initState No calendar found for site " + siteId + " " + e.getMessage());
                            } catch (PermissionException e) {
                                log.info(this + ":initState No permission to get the calendar. " + e.getMessage());
                            }
                        }
                    } else {
                        state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.FALSE); // Tool on site but alternate calendar not yet created.
                    }

                }
            } else {
                state.setAttribute(ADDITIONAL_CALENDAR_TOOL_READY, Boolean.FALSE); // Tool not deployed on the server.
            }
        }

        /** The Announcement tool  */
        if (state.getAttribute(ANNOUNCEMENT_TOOL_EXIST) == null) {
            if (!siteHasTool(siteId, "sakai.announcements")) {
                state.setAttribute(ANNOUNCEMENT_TOOL_EXIST, Boolean.FALSE);
                state.removeAttribute(ANNOUNCEMENT_CHANNEL);
            } else {
                state.setAttribute(ANNOUNCEMENT_TOOL_EXIST, Boolean.TRUE);
                if (state.getAttribute(ANNOUNCEMENT_CHANNEL) == null) {
                    String channelId = serverConfigurationService.getString("channel", null);
                    if (channelId == null) {
                        channelId = announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
                        try {
                            state.setAttribute(ANNOUNCEMENT_CHANNEL, announcementService.getAnnouncementChannel(channelId));
                        } catch (IdUnusedException e) {
                            log.warn(this + ":initState No announcement channel found. " + e.getMessage());
                            state.removeAttribute(ANNOUNCEMENT_CHANNEL);
                        } catch (PermissionException e) {
                            log.warn(this + ":initState No permission to annoucement channel. " + e.getMessage());
                        } catch (Exception ex) {
                            log.warn(this + ":initState Assignment : Action : init state : calendar exception : " + ex.getMessage());
                        }
                    }
                }
            }
        } // if

        if (state.getAttribute(STATE_CONTEXT_STRING) == null || ((String) state.getAttribute(STATE_CONTEXT_STRING)).length() == 0) {
            state.setAttribute(STATE_CONTEXT_STRING, siteId);
        } // if context string is null

        if (state.getAttribute(SORTED_BY) == null) {
            setDefaultSort(state);
        }

        if (state.getAttribute(FILTER_OPTION) == null) {
            state.setAttribute(FILTER_OPTION, AssignmentFilter.ALL);
        }

        if (state.getAttribute(SORTED_GRADE_SUBMISSION_BY) == null) {
            state.setAttribute(SORTED_GRADE_SUBMISSION_BY, SORTED_GRADE_SUBMISSION_BY_LASTNAME);
        }

        if (state.getAttribute(SORTED_GRADE_SUBMISSION_ASC) == null) {
            state.setAttribute(SORTED_GRADE_SUBMISSION_ASC, Boolean.TRUE.toString());
        }

        if (state.getAttribute(SORTED_SUBMISSION_BY) == null) {
            state.setAttribute(SORTED_SUBMISSION_BY, SORTED_SUBMISSION_BY_LASTNAME);
        }

        if (state.getAttribute(SORTED_SUBMISSION_ASC) == null) {
            state.setAttribute(SORTED_SUBMISSION_ASC, Boolean.TRUE.toString());
        }

        if (state.getAttribute(STUDENT_LIST_SHOW_TABLE) == null) {
            state.setAttribute(STUDENT_LIST_SHOW_TABLE, new ConcurrentSkipListSet());
        }

        if (state.getAttribute(ATTACHMENTS_MODIFIED) == null) {
            state.setAttribute(ATTACHMENTS_MODIFIED, Boolean.FALSE);
        }

        // SECTION MOD
        if (state.getAttribute(STATE_SECTION_STRING) == null) {

            state.setAttribute(STATE_SECTION_STRING, "001");
        }

        if (state.getAttribute(STATE_MODE) == null) {
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        }

        if (state.getAttribute(STATE_TOP_PAGE_MESSAGE) == null) {
            state.setAttribute(STATE_TOP_PAGE_MESSAGE, 0);
        }

        // whether to display the number of submission/ungraded submission column
        // default to show
        if (state.getAttribute(SHOW_NUMBER_SUBMISSION_COLUMN) == null) {
            PortletConfig config = portlet.getPortletConfig();
            String value = StringUtils.trimToNull(config.getInitParameter(SHOW_NUMBER_SUBMISSION_COLUMN));
            if (value == null) {
                value = Boolean.TRUE.toString();
            }
            state.setAttribute(SHOW_NUMBER_SUBMISSION_COLUMN, Boolean.valueOf(value));
        }

        if (state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM) == null) {
            state.setAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM, GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) - 4);
        }

        if (state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO) == null) {
            state.setAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO, GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) + 4);
        }
    } // initState

    /**
     * whether the site has the specified tool
     *
     * @param siteId
     * @return
     */
    private boolean siteHasTool(String siteId, String toolId) {
        boolean rv = false;
        try {
            Site s = siteService.getSite(siteId);
            if (s.getToolForCommonId(toolId) != null) {
                rv = true;
            }
        } catch (Exception e) {
            log.warn(this + "siteHasTool" + e.getMessage() + siteId);
        }
        return rv;
    }

    /**
     * reset the attributes for view submission
     */
    private void resetViewSubmission(SessionState state) {
        state.removeAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
        state.removeAttribute(VIEW_SUBMISSION_TEXT);
        state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
        state.removeAttribute(VIEW_SUBMISSION_ASSIGNMENT_INSTRUCTOR);
        state.removeAttribute(PREVIEW_SUBMISSION_TEXT);
        state.removeAttribute(CONTENT_REVIEW_EULA_REQUIRED_ALERTED);
        state.removeAttribute(ResourceProperties.ASSIGNMENT_INPUT_ADD_TIME_SPENT);
    } // resetViewSubmission

    /**
     * initialize assignment attributes
     *
     * @param state
     */
    private void initializeAssignment(SessionState state) {
        // put the input value into the state attributes
        state.setAttribute(NEW_ASSIGNMENT_TITLE, "");

        // get the configured date offsets in seconds  
        // defaults: visible date - now, open date - now, due date - seven days later,   
        //           accept until date - eight days after now, peer eval date - fifteen days after now.  
        // note that front-end javascript code rounds the time back to the nearest five minutes.

        int visibleDateOffset = serverConfigurationService.getInt("assignment.visibledate", 0);
        int openDateOffset = serverConfigurationService.getInt("assignment.opendate", 0);
        int dueDateOffset = serverConfigurationService.getInt("assignment.duedate", 604800);
        int acceptUntilDateOffset = serverConfigurationService.getInt("assignment.acceptuntildate", 691200);
        int peerEvaluationDateOffset = serverConfigurationService.getInt("assignment.peerevaluationdate", 1296000);

        // get current time
        Instant t = Instant.now();
        int minute;
        int hour;
        int month;
        int day;
        int year;

        // visible date is shifted forward by the offset
        Instant tVisible = t.plusSeconds(visibleDateOffset);
        LocalDateTime ldtVisible = LocalDateTime.ofInstant(tVisible, userTimeService.getLocalTimeZone().toZoneId());
        minute = ldtVisible.getMinute();
        hour = ldtVisible.getHour();
        month = ldtVisible.getMonthValue();
        day = ldtVisible.getDayOfMonth();
        year = ldtVisible.getYear();

        state.setAttribute(NEW_ASSIGNMENT_VISIBLE_MONTH, month);
        state.setAttribute(NEW_ASSIGNMENT_VISIBLE_DAY, day);
        state.setAttribute(NEW_ASSIGNMENT_VISIBLE_YEAR, year);
        state.setAttribute(NEW_ASSIGNMENT_VISIBLE_HOUR, hour);
        state.setAttribute(NEW_ASSIGNMENT_VISIBLE_MIN, minute);

        // open date is shifted forward by the offset
        Instant tOpen = t.plusSeconds(openDateOffset);
        LocalDateTime ldtOpen = LocalDateTime.ofInstant(tOpen, userTimeService.getLocalTimeZone().toZoneId());
        minute = ldtOpen.getMinute();
        hour = ldtOpen.getHour();
        month = ldtOpen.getMonthValue();
        day = ldtOpen.getDayOfMonth();
        year = ldtOpen.getYear();

        state.setAttribute(NEW_ASSIGNMENT_OPEN_MONTH, month);
        state.setAttribute(NEW_ASSIGNMENT_OPEN_DAY, day);
        state.setAttribute(NEW_ASSIGNMENT_OPEN_YEAR, year);
        state.setAttribute(NEW_ASSIGNMENT_OPEN_HOUR, hour);
        state.setAttribute(NEW_ASSIGNMENT_OPEN_MIN, minute);

        // set the all purpose item release time
        state.setAttribute(ALLPURPOSE_RELEASE_MONTH, month);
        state.setAttribute(ALLPURPOSE_RELEASE_DAY, day);
        state.setAttribute(ALLPURPOSE_RELEASE_YEAR, year);
        state.setAttribute(ALLPURPOSE_RELEASE_HOUR, hour);
        state.setAttribute(ALLPURPOSE_RELEASE_MIN, minute);

        // due date is shifted forward by the offset
        Instant tDue = t.plusSeconds(dueDateOffset);
        LocalDateTime ldtDue = LocalDateTime.ofInstant(tDue, userTimeService.getLocalTimeZone().toZoneId());
        minute = ldtDue.getMinute();
        hour = ldtDue.getHour();
        month = ldtDue.getMonthValue();
        day = ldtDue.getDayOfMonth();
        year = ldtDue.getYear();

        state.setAttribute(NEW_ASSIGNMENT_DUE_MONTH, month);
        state.setAttribute(NEW_ASSIGNMENT_DUE_DAY, day);
        state.setAttribute(NEW_ASSIGNMENT_DUE_YEAR, year);
        state.setAttribute(NEW_ASSIGNMENT_DUE_HOUR, hour);
        state.setAttribute(NEW_ASSIGNMENT_DUE_MIN, minute);

        // enable the close date by default
        state.setAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE, Boolean.TRUE);
        state.setAttribute(NEW_ASSIGNMENT_REMINDER_EMAIL, serverConfigurationService.getBoolean("asn.reminder.email.default", true));

        // Accept until date is shifted forward by the offset
        Instant tAccept = t.plusSeconds(acceptUntilDateOffset);
        LocalDateTime ldtAccept = LocalDateTime.ofInstant(tAccept, userTimeService.getLocalTimeZone().toZoneId());
        minute = ldtAccept.getMinute();
        hour = ldtAccept.getHour();
        month = ldtAccept.getMonthValue();
        day = ldtAccept.getDayOfMonth();
        year = ldtAccept.getYear();

        // Set the close date (accept until)
        state.setAttribute(NEW_ASSIGNMENT_CLOSE_MONTH, month);
        state.setAttribute(NEW_ASSIGNMENT_CLOSE_DAY, day);
        state.setAttribute(NEW_ASSIGNMENT_CLOSE_YEAR, year);
        state.setAttribute(NEW_ASSIGNMENT_CLOSE_HOUR, hour);
        state.setAttribute(NEW_ASSIGNMENT_CLOSE_MIN, minute);

        // set the resubmit time to be the same as close date
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_MONTH, month);
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_DAY, day);
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_YEAR, year);
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_HOUR, hour);
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_MIN, minute);
        state.setAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, 1);

        // set extension date to default to close date
        state.setAttribute(ALLOW_EXTENSION_CLOSE_MONTH, month);
        state.setAttribute(ALLOW_EXTENSION_CLOSE_DAY, day);
        state.setAttribute(ALLOW_RESUBMIT_CLOSE_YEAR, year);
        state.setAttribute(ALLOW_EXTENSION_CLOSE_HOUR, hour);
        state.setAttribute(ALLOW_EXTENSION_CLOSE_MIN, minute);

        // set the all purpose retract time
        state.setAttribute(ALLPURPOSE_RETRACT_MONTH, month);
        state.setAttribute(ALLPURPOSE_RETRACT_DAY, day);
        state.setAttribute(ALLPURPOSE_RETRACT_YEAR, year);
        state.setAttribute(ALLPURPOSE_RETRACT_HOUR, hour);
        state.setAttribute(ALLPURPOSE_RETRACT_MIN, minute);

        // Peer evaluation date is shifted forward by the offset
        Instant tPeer = t.plusSeconds(peerEvaluationDateOffset);
        LocalDateTime ldtPeer = LocalDateTime.ofInstant(tPeer, userTimeService.getLocalTimeZone().toZoneId());
        minute = ldtPeer.getMinute();
        hour = ldtPeer.getHour();
        month = ldtPeer.getMonthValue();
        day = ldtPeer.getDayOfMonth();
        year = ldtPeer.getYear();

        state.setAttribute(NEW_ASSIGNMENT_PEERPERIOD_MONTH, month);
        state.setAttribute(NEW_ASSIGNMENT_PEERPERIOD_DAY, day);
        state.setAttribute(NEW_ASSIGNMENT_PEERPERIOD_YEAR, year);
        state.setAttribute(NEW_ASSIGNMENT_PEERPERIOD_HOUR, hour);
        state.setAttribute(NEW_ASSIGNMENT_PEERPERIOD_MIN, minute);

        state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL, Boolean.TRUE);
        state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS, Boolean.TRUE);
        state.setAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS, 1);

        state.setAttribute(NEW_ASSIGNMENT_SECTION, "001");
        state.setAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE, Assignment.SubmissionType.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION.ordinal());
        Boolean withGradesConfig = serverConfigurationService.getBoolean("assignment.grade.default", Boolean.TRUE);
        if (withGradesConfig) {
            state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, Assignment.GradeType.SCORE_GRADE_TYPE.ordinal());
            String defaultPointsConfig = serverConfigurationService.getString("assignment.points.default", "");
            if (NumberUtils.isParsable(defaultPointsConfig)) {
                float defaultPoints = NumberUtils.createFloat(defaultPointsConfig);
                state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, String.valueOf(defaultPoints));
            } else {
                state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, "");
            }
        } else {
            state.setAttribute(NEW_ASSIGNMENT_GRADE_TYPE, Assignment.GradeType.UNGRADED_GRADE_TYPE.ordinal());
            state.setAttribute(NEW_ASSIGNMENT_GRADE_POINTS, "");
        }
        state.setAttribute(NEW_ASSIGNMENT_CONTENT_ID, null);
        state.setAttribute(NEW_ASSIGNMENT_CONTENT_TITLE, null);
        state.setAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW, null);
        state.setAttribute(NEW_ASSIGNMENT_CONTENT_TOOL_NEWPAGE, null);
        state.setAttribute(NEW_ASSIGNMENT_CONTENT_DELETED, Boolean.FALSE);
        state.setAttribute(NEW_ASSIGNMENT_DESCRIPTION, "");
        state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.FALSE.toString());
        boolean checkAddDueDate = (state.getAttribute(CALENDAR) != null || state.getAttribute(ADDITIONAL_CALENDAR) != null) && serverConfigurationService.getBoolean(AssignmentConstants.SAK_PROP_DUE_DATE_TO_CALENDAR_DEFAULT, DUE_DATE_TO_CALENDAR_DEFAULT);
        state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE, Boolean.toString(checkAddDueDate));
        state.setAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());

        String defaultNotification = serverConfigurationService.getString("announcement.default.notification", "n");
        if (defaultNotification.equalsIgnoreCase("r")) {
            state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_HIGH);
        } else if (defaultNotification.equalsIgnoreCase("o")) {
            state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_LOW);
        } else {
            state.setAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION, AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE);
        }
        // make the honor pledge not include as the default
        state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE, Boolean.FALSE);

        state.setAttribute(NEW_ASSIGNMENT_CHECK_AUTO_SUBMIT, serverConfigurationService.getBoolean("assignment.autoSubmit.defaultCheckbox", false));

        state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS, Boolean.FALSE);
        state.setAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS, Boolean.FALSE);

        state.setAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK, GRADEBOOK_INTEGRATION_NO);

        state.setAttribute(NEW_ASSIGNMENT_ATTACHMENT, entityManager.newReferenceList());

        state.setAttribute(NEW_ASSIGNMENT_FOCUS, NEW_ASSIGNMENT_TITLE);

        state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);

        // reset the global navigaion alert flag
        if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null) {
            state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
        }

        rangeAndGroups.initializeAssignment(state);

        // remove the edit assignment id if any
        state.removeAttribute(EDIT_ASSIGNMENT_ID);

        // remove the resubmit number
        state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
        state.removeAttribute((AssignmentConstants.ALLOW_EXTENSION_CLOSETIME)); //remove Extension date

        // remove the supplement attributes
        state.removeAttribute(MODELANSWER);
        state.removeAttribute(MODELANSWER_TEXT);
        state.removeAttribute(MODELANSWER_SHOWTO);
        state.removeAttribute(MODELANSWER_ATTACHMENTS);
        state.removeAttribute(NOTE);
        state.removeAttribute(NOTE_TEXT);
        state.removeAttribute(NOTE_SHAREWITH);
        state.removeAttribute(ALLPURPOSE);
        state.removeAttribute(ALLPURPOSE_TITLE);
        state.removeAttribute(ALLPURPOSE_TEXT);
        state.removeAttribute(ALLPURPOSE_HIDE);
        state.removeAttribute(ALLPURPOSE_SHOW_FROM);
        state.removeAttribute(ALLPURPOSE_SHOW_TO);
        state.removeAttribute(ALLPURPOSE_RELEASE_DATE);
        state.removeAttribute(ALLPURPOSE_RETRACT_DATE);
        state.removeAttribute(ALLPURPOSE_ACCESS);
        state.removeAttribute(ALLPURPOSE_ATTACHMENTS);

        // SAK-17606
        state.removeAttribute(NEW_ASSIGNMENT_CHECK_ANONYMOUS_GRADING);
    } // initializeAssignment

    /**
     * reset the attributes for assignment
     */
    private void resetAssignment(SessionState state) {
        state.removeAttribute(NEW_ASSIGNMENT_TITLE);
        state.removeAttribute(NEW_ASSIGNMENT_OPEN_MONTH);
        state.removeAttribute(NEW_ASSIGNMENT_OPEN_DAY);
        state.removeAttribute(NEW_ASSIGNMENT_OPEN_YEAR);
        state.removeAttribute(NEW_ASSIGNMENT_OPEN_HOUR);
        state.removeAttribute(NEW_ASSIGNMENT_OPEN_MIN);
        state.removeAttribute(NEW_ASSIGNMENT_CONTENT_ID);
        state.removeAttribute(NEW_ASSIGNMENT_CONTENT_TITLE);
        state.removeAttribute(NEW_ASSIGNMENT_CONTENT_LAUNCH_NEW_WINDOW);
        state.removeAttribute(NEW_ASSIGNMENT_CONTENT_TOOL_NEWPAGE);
        state.removeAttribute(NEW_ASSIGNMENT_CONTENT_DELETED);
        state.removeAttribute(ALLPURPOSE_RELEASE_MONTH);
        state.removeAttribute(ALLPURPOSE_RELEASE_DAY);
        state.removeAttribute(ALLPURPOSE_RELEASE_YEAR);
        state.removeAttribute(ALLPURPOSE_RELEASE_HOUR);
        state.removeAttribute(ALLPURPOSE_RELEASE_MIN);

        state.removeAttribute(NEW_ASSIGNMENT_DUE_MONTH);
        state.removeAttribute(NEW_ASSIGNMENT_DUE_DAY);
        state.removeAttribute(NEW_ASSIGNMENT_DUE_YEAR);
        state.removeAttribute(NEW_ASSIGNMENT_DUE_HOUR);
        state.removeAttribute(NEW_ASSIGNMENT_DUE_MIN);
        state.removeAttribute(NEW_ASSIGNMENT_PAST_DUE_DATE);

        state.removeAttribute(NEW_ASSIGNMENT_VISIBLE_MONTH);
        state.removeAttribute(NEW_ASSIGNMENT_VISIBLE_DAY);
        state.removeAttribute(NEW_ASSIGNMENT_VISIBLE_YEAR);
        state.removeAttribute(NEW_ASSIGNMENT_VISIBLE_HOUR);
        state.removeAttribute(NEW_ASSIGNMENT_VISIBLE_MIN);
        state.removeAttribute(NEW_ASSIGNMENT_VISIBLETOGGLE);

        state.removeAttribute(NEW_ASSIGNMENT_ENABLECLOSEDATE);
        state.removeAttribute(NEW_ASSIGNMENT_REMINDER_EMAIL);
        state.removeAttribute(NEW_ASSIGNMENT_CLOSE_MONTH);
        state.removeAttribute(NEW_ASSIGNMENT_CLOSE_DAY);
        state.removeAttribute(NEW_ASSIGNMENT_CLOSE_YEAR);
        state.removeAttribute(NEW_ASSIGNMENT_CLOSE_HOUR);
        state.removeAttribute(NEW_ASSIGNMENT_CLOSE_MIN);

        // set the all purpose retract time
        state.removeAttribute(ALLPURPOSE_RETRACT_MONTH);
        state.removeAttribute(ALLPURPOSE_RETRACT_DAY);
        state.removeAttribute(ALLPURPOSE_RETRACT_YEAR);
        state.removeAttribute(ALLPURPOSE_RETRACT_HOUR);
        state.removeAttribute(ALLPURPOSE_RETRACT_MIN);

        state.removeAttribute(NEW_ASSIGNMENT_SECTION);
        state.removeAttribute(NEW_ASSIGNMENT_SUBMISSION_TYPE);
        state.removeAttribute(NEW_ASSIGNMENT_GRADE_TYPE);
        state.removeAttribute(NEW_ASSIGNMENT_GRADE_TYPE_SWITCHING);
        state.removeAttribute(NEW_ASSIGNMENT_GRADE_TYPE_SWITCH_CONFIRM);
        state.removeAttribute(NEW_ASSIGNMENT_GRADE_POINTS);
        state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION);
        state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE);
        state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE);
        state.removeAttribute(AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION);
        state.removeAttribute(NEW_ASSIGNMENT_CHECK_ADD_HONOR_PLEDGE);
        state.removeAttribute(NEW_ASSIGNMENT_CHECK_ADD_INSTRUCTOR_TAGS);
        state.removeAttribute(NEW_ASSIGNMENT_CHECK_ADD_GROUP_TAGS);
        state.removeAttribute(NEW_ASSIGNMENT_CHECK_HIDE_DUE_DATE);
        state.removeAttribute(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
        state.removeAttribute(NEW_ASSIGNMENT_ATTACHMENT);
        state.removeAttribute(NEW_ASSIGNMENT_FOCUS);
        state.removeAttribute(NEW_ASSIGNMENT_DESCRIPTION_EMPTY);
        state.removeAttribute(AssignmentConstants.ASSIGNMENT_RELEASEGRADE_NOTIFICATION_VALUE);

        state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE);
        state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_CHECK_ADD_ESTIMATE_REQUIRED);
        state.removeAttribute(ResourceProperties.NEW_ASSIGNMENT_INPUT_ADD_TIME_ESTIMATE);

        // reset the global navigaion alert flag
        if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null) {
            state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
        }

        rangeAndGroups.resetAssignment(state);

        // remove the edit assignment id if any
        state.removeAttribute(EDIT_ASSIGNMENT_ID);

        // remove the resubmit number
        state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
        state.removeAttribute((AssignmentConstants.ALLOW_EXTENSION_CLOSETIME));

        // remove the supplement attributes
        state.removeAttribute(MODELANSWER);
        state.removeAttribute(MODELANSWER_TEXT);
        state.removeAttribute(MODELANSWER_SHOWTO);
        state.removeAttribute(MODELANSWER_ATTACHMENTS);
        state.removeAttribute(NOTE);
        state.removeAttribute(NOTE_TEXT);
        state.removeAttribute(NOTE_SHAREWITH);
        state.removeAttribute(ALLPURPOSE);
        state.removeAttribute(ALLPURPOSE_TITLE);
        state.removeAttribute(ALLPURPOSE_TEXT);
        state.removeAttribute(ALLPURPOSE_HIDE);
        state.removeAttribute(ALLPURPOSE_SHOW_FROM);
        state.removeAttribute(ALLPURPOSE_SHOW_TO);
        state.removeAttribute(ALLPURPOSE_RELEASE_DATE);
        state.removeAttribute(ALLPURPOSE_RETRACT_DATE);
        state.removeAttribute(ALLPURPOSE_ACCESS);
        state.removeAttribute(ALLPURPOSE_ATTACHMENTS);

        //revmoew peer assessment settings
        state.removeAttribute(NEW_ASSIGNMENT_USE_PEER_ASSESSMENT);
        state.removeAttribute(NEW_ASSIGNMENT_PEERPERIOD_MONTH);
        state.removeAttribute(NEW_ASSIGNMENT_PEERPERIOD_DAY);
        state.removeAttribute(NEW_ASSIGNMENT_PEERPERIOD_YEAR);
        state.removeAttribute(NEW_ASSIGNMENT_PEERPERIOD_HOUR);
        state.removeAttribute(NEW_ASSIGNMENT_PEERPERIOD_MIN);
        state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_ANON_EVAL);
        state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_STUDENT_VIEW_REVIEWS);
        state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_NUM_REVIEWS);
        state.removeAttribute(NEW_ASSIGNMENT_PEER_ASSESSMENT_INSTRUCTIONS);

        // remove content-review settings
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_USE_REVIEW_SERVICE);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_ALLOW_STUDENT_VIEW);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_CHECK_INSTITUTION);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_BIBLIOGRAPHIC);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_QUOTED);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SELF_PLAG);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STORE_INST_INDEX);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_REPORT_RADIO);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_STUDENT_PREVIEW);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_SMALL_MATCHES);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_TYPE);
        state.removeAttribute(AssignmentConstants.NEW_ASSIGNMENT_REVIEW_SERVICE_EXCLUDE_VALUE);

        state.removeAttribute(AssignmentConstants.ASSIGNMENT_RELEASERESUBMISSION_NOTIFICATION_VALUE);

        state.removeAttribute(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);

        state.removeAttribute(NEW_ASSIGNMENT_PREVIOUSLY_ASSOCIATED);

        state.removeAttribute(RUBRIC_ASSOCIATION);

        state.removeAttribute(TAG_SELECTOR);
        state.removeAttribute(GB_SELECTOR);

        if (serverConfigurationService.getBoolean("gradebookng.category.reset", false)) {
            state.removeAttribute(NEW_ASSIGNMENT_CATEGORY);
        }

        state.removeAttribute(AssignmentConstants.ASSIGNMENT_AUTO_SUBMIT_ENABLED);
    } // resetAssignment

    /**
     * construct a HashMap using integer as the key and three character string of the month as the value
     */
    private Map<Integer, String> monthTable() {
        Map<Integer, String> n = new HashMap<>();
        n.put(1, rb.getString("jan"));
        n.put(2, rb.getString("feb"));
        n.put(3, rb.getString("mar"));
        n.put(4, rb.getString("apr"));
        n.put(5, rb.getString("may"));
        n.put(6, rb.getString("jun"));
        n.put(7, rb.getString("jul"));
        n.put(8, rb.getString("aug"));
        n.put(9, rb.getString("sep"));
        n.put(10, rb.getString("oct"));
        n.put(11, rb.getString("nov"));
        n.put(12, rb.getString("dec"));
        return n;

    } // monthTable

    /**
     * construct a HashMap using the integer as the key and grade type String as the value
     */
    private Map<Integer, String> gradeTypeTable() {

        Map<Integer, String> gradeTypeTable = new HashMap<>();
        gradeTypeTable.put(1, rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_NOGRADE_PROP));
        gradeTypeTable.put(2, rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_LETTER_PROP));
        gradeTypeTable.put(3, rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_POINTS_PROP));
        gradeTypeTable.put(4, rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_PASS_FAIL_PROP));
        gradeTypeTable.put(5, rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_CHECK_PROP));

        return gradeTypeTable;
    } // gradeTypeTable

    /**
     * construct a HashMap using the integer as the key and submission type String as the value
     */
    private Map<Integer, String> submissionTypeTable() {

        Map<Integer, String> submissionTypeTable = new HashMap<>();
        submissionTypeTable.put(1, rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_INLINE_PROP));
        submissionTypeTable.put(2, rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_ATTACHMENTS_ONLY_PROP));
        submissionTypeTable.put(3, rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_INLINE_AND_ATTACHMENTS_PROP));
        submissionTypeTable.put(4, rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_NON_ELECTRONIC_PROP));
        submissionTypeTable.put(5, rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_SINGLE_ATTACHMENT_PROP));
        submissionTypeTable.put(6, rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_EXTERNAL_TOOL));
        submissionTypeTable.put(7, rb.getString(AssignmentConstants.ASSN_SUBMISSION_TYPE_VIDEO_PROP));

        return submissionTypeTable;
    } // submissionTypeTable

    /**
     * Add the list of categories from the gradebook tool
     * construct a HashMap using the integer as the key and category String as the value
     *
     * @return
     */
    private Map<Long, String> getCategoryTable() {
        // Preserve ordering: first "Unassigned", then categories in the Gradebook Settings order
        Map<Long, String> catTable = new LinkedHashMap<>();
        String gradebookUid = toolManager.getCurrentPlacement().getContext();

        if (canGrade() && gradingService.isCategoriesEnabled(gradebookUid)) {
            // Unassigned option (-1) shown first
            catTable.put(-1L, rb.getString("grading.unassigned"));

            List<CategoryDefinition> categoryDefinitions = gradingService.getCategoryDefinitions(gradebookUid, gradebookUid);
            if (categoryDefinitions != null) {
                for (CategoryDefinition c : categoryDefinitions) {
                    catTable.put(c.getId(), c.getName());
                }
            }
        }
        return catTable;
    }

    /**
     * Load the current category for an existing assignment from assignment properties (for draft assignments)
     */
    private void loadCurrentAssignmentCategory(SessionState state, Assignment assignment, String siteId) {
        boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(siteId);

        try {
            Map<String, String> properties = assignment.getProperties();
            String storedCategory = properties.get(NEW_ASSIGNMENT_CATEGORY);
            if (storedCategory != null) storedCategory = storedCategory.trim();

            if (storedCategory != null && !storedCategory.isEmpty()) {
                if (isGradebookGroupEnabled) {
                    state.setAttribute(NEW_ASSIGNMENT_CATEGORY, storedCategory);
                } else {
                    String normalized = storedCategory.trim();
                    try {
                        Long.parseLong(normalized);
                        state.setAttribute(NEW_ASSIGNMENT_CATEGORY, normalized);
                    } catch (NumberFormatException e) {
                        state.setAttribute(NEW_ASSIGNMENT_CATEGORY, "-1");
                    }
                }
            }
            else if (isGradebookGroupEnabled) {
                state.setAttribute(NEW_ASSIGNMENT_CATEGORY, "-1");
            } else {
                state.setAttribute(NEW_ASSIGNMENT_CATEGORY, -1L);
            }
        } catch (Exception e) {
            log.warn("Error loading category for assignment {} in site {}", assignment.getId(), siteId, e);
            if (isGradebookGroupEnabled) {
                state.setAttribute(NEW_ASSIGNMENT_CATEGORY, "-1");
            } else {
                state.setAttribute(NEW_ASSIGNMENT_CATEGORY, -1L);
            }
        }
    }

    /**
     * Sort based on the given property
     */
    public void doSort(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // we are changing the sort, so start from the first page again
        resetPaging(state);

        setupSort(data, data.getParameters().getString("criteria"));
    }

    /**
     * setup sorting parameters
     *
     * @param criteria String for sortedBy
     */
    private void setupSort(RunData data, String criteria) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // current sorting sequence
        String asc = "";
        if (!criteria.equals(state.getAttribute(SORTED_BY))) {
            state.setAttribute(SORTED_BY, criteria);
            asc = Boolean.TRUE.toString();
            state.setAttribute(SORTED_ASC, asc);
        } else {
            // current sorting sequence
            asc = (String) state.getAttribute(SORTED_ASC);

            // toggle between the ascending and descending sequence
            if (asc.equals(Boolean.TRUE.toString())) {
                asc = Boolean.FALSE.toString();
            } else {
                asc = Boolean.TRUE.toString();
            }
            state.setAttribute(SORTED_ASC, asc);
        }

    } // doSort

    /**
     * Do sort by group title
     */
    public void doSortbygrouptitle(RunData data) {
        setupSort(data, SORTED_BY_GROUP_TITLE);

    } // doSortbygrouptitle

    /**
     * Do sort by group description
     */
    public void doSortbygroupdescription(RunData data) {
        setupSort(data, SORTED_BY_GROUP_DESCRIPTION);

    } // doSortbygroupdescription

    /**
     * Sort submission based on the given property
     */
    public void doSort_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // we are changing the sort, so start from the first page again
        resetPaging(state);

        // get the ParameterParser from RunData
        ParameterParser params = data.getParameters();

        String criteria = params.getString("criteria");

        // current sorting sequence
        String asc = "";

        if (!criteria.equals(state.getAttribute(SORTED_SUBMISSION_BY))) {
            state.setAttribute(SORTED_SUBMISSION_BY, criteria);
            asc = Boolean.TRUE.toString();
            state.setAttribute(SORTED_SUBMISSION_ASC, asc);
        } else {
            // current sorting sequence
            state.setAttribute(SORTED_SUBMISSION_BY, criteria);
            asc = (String) state.getAttribute(SORTED_SUBMISSION_ASC);

            // toggle between the ascending and descending sequence
            if (asc.equals(Boolean.TRUE.toString())) {
                asc = Boolean.FALSE.toString();
            } else {
                asc = Boolean.TRUE.toString();
            }
            state.setAttribute(SORTED_SUBMISSION_ASC, asc);
        }
    } // doSort_submission

    /**
     * Sort submission based on the given property in instructor grade view
     */
    public void doSort_grade_submission(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        // we are changing the sort, so start from the first page again
        resetPaging(state);

        // get the ParameterParser from RunData
        ParameterParser params = data.getParameters();

        String criteria = params.getString("criteria");

        // current sorting sequence
        String asc = "";

        if (!criteria.equals(state.getAttribute(SORTED_GRADE_SUBMISSION_BY))) {
            state.setAttribute(SORTED_GRADE_SUBMISSION_BY, criteria);
            //for content review default is desc
            if (criteria.equals(SORTED_GRADE_SUBMISSION_CONTENTREVIEW))
                asc = Boolean.FALSE.toString();
            else
                asc = Boolean.TRUE.toString();

            state.setAttribute(SORTED_GRADE_SUBMISSION_ASC, asc);
        } else {
            // current sorting sequence
            state.setAttribute(SORTED_GRADE_SUBMISSION_BY, criteria);
            asc = (String) state.getAttribute(SORTED_GRADE_SUBMISSION_ASC);

            // toggle between the ascending and descending sequence
            if (asc.equals(Boolean.TRUE.toString())) {
                asc = Boolean.FALSE.toString();
            } else {
                asc = Boolean.TRUE.toString();
            }
            state.setAttribute(SORTED_GRADE_SUBMISSION_ASC, asc);
        }
    } // doSort_grade_submission

    public void doSort_tags(RunData data) {
        SessionState state = ((JetspeedRunData) data)
                .getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();

        String criteria = params.getString("criteria");
        String providerId = params.getString(PROVIDER_ID);

        String savedText = params.getString("savedText");
        state.setAttribute(VIEW_SUBMISSION_TEXT, savedText);

        String mode = (String) state.getAttribute(STATE_MODE);

        List<DecoratedTaggingProvider> providers = (List<DecoratedTaggingProvider>) state.getAttribute(mode + PROVIDER_LIST);

        for (DecoratedTaggingProvider dtp : providers) {
            if (dtp.getProvider().getId().equals(providerId)) {
                Sort sort = dtp.getSort();
                if (sort.getSort().equals(criteria)) {
                    sort.setAscending(!sort.isAscending());
                } else {
                    sort.setSort(criteria);
                    sort.setAscending(true);
                }
                break;
            }
        }
    }

    public void doPage_tags(RunData data) {
        SessionState state = ((JetspeedRunData) data)
                .getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        ParameterParser params = data.getParameters();

        String page = params.getString("page");
        String pageSize = params.getString("pageSize");
        String providerId = params.getString(PROVIDER_ID);

        String savedText = params.getString("savedText");
        state.setAttribute(VIEW_SUBMISSION_TEXT, savedText);

        String mode = (String) state.getAttribute(STATE_MODE);

        List<DecoratedTaggingProvider> providers = (List<DecoratedTaggingProvider>) state.getAttribute(mode + PROVIDER_LIST);

        for (DecoratedTaggingProvider dtp : providers) {
            if (dtp.getProvider().getId().equals(providerId)) {
                Pager pager = dtp.getPager();
                pager.setPageSize(Integer.valueOf(pageSize));
                if (Pager.FIRST.equals(page)) {
                    pager.setFirstItem(0);
                } else if (Pager.PREVIOUS.equals(page)) {
                    pager.setFirstItem(pager.getFirstItem()
                            - pager.getPageSize());
                } else if (Pager.NEXT.equals(page)) {
                    pager.setFirstItem(pager.getFirstItem()
                            + pager.getPageSize());
                } else if (Pager.LAST.equals(page)) {
                    pager.setFirstItem((pager.getTotalItems() / pager
                            .getPageSize())
                            * pager.getPageSize());
                }
                break;
            }
        }
    }

    public void doPermissions(RunData runData, Context context) {
        SessionState state = ((JetspeedRunData) runData).getPortletSessionState(((JetspeedRunData) runData).getJs_peid());
        state.setAttribute(STATE_MODE, "permissions");
        state.setAttribute(STATE_TOOL_KEY, "asn");
    }

    /**
     * transforms the Iterator to List
     */
    private List iterator_to_list(Iterator l) {
        List v = new ArrayList();
        while (l.hasNext()) {
            v.add(l.next());
        }
        return v;
    } // iterator_to_list

    /**
     * Implement this to return alist of all the resources that there are to page. Sort them as appropriate.
     */
    protected List readResourcesPage(SessionState state, int first, int last) {

        List returnResources = (List) state.getAttribute(STATE_PAGEING_TOTAL_ITEMS);

        PagingPosition page = new PagingPosition(first, last);
        page.validate(returnResources.size());
        returnResources = returnResources.subList(page.getFirst() - 1, page.getLast());

        return returnResources;

    } // readAllResources

    /*
	 * (non-Javadoc)
	 *
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#sizeResources(org.sakaiproject.service.framework.session.SessionState)
	 */
    protected int sizeResources(SessionState state) {
        String mode = (String) state.getAttribute(STATE_MODE);
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        // all the resources for paging
        List returnResources = new ArrayList();
        boolean hasOneAnon = false;

        boolean allowAddAssignment = assignmentService.allowAddAssignment(contextString);
        switch (mode) {
            case MODE_LIST_ASSIGNMENTS:
                String view = (String) state.getAttribute(STATE_SELECTED_VIEW);
                String selectedGroup = (String) state.getAttribute(FILTER_BY_GROUP);

                if (allowAddAssignment && (MODE_LIST_ASSIGNMENTS).equals(view)) {
                    // read all Assignments
                    returnResources.addAll(assignmentService.getAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING)));
                } else if (allowAddAssignment && MODE_STUDENT_VIEW.equals(view)
                        || (!allowAddAssignment && assignmentService.allowAddSubmission((String) state.getAttribute(STATE_CONTEXT_STRING)))) {
                    // in the student list view of assignments
                    Instant currentTime = Instant.now();
                    for (Assignment a : assignmentService.getAssignmentsForContext(contextString)) {
                        if (!a.getDeleted()) {
                            // show not deleted assignments
                            Instant openTime = a.getOpenDate();
                            Instant visibleTime = a.getVisibleDate();
                            if (!a.getDraft()
                                    && ((openTime != null && currentTime.isAfter(openTime))
                                        || (visibleTime != null && currentTime.isAfter(visibleTime)))) {
                                returnResources.add(a);
                            }
                        } else if (a.getDeleted() &&
                                   a.getTypeOfSubmission() != Assignment.SubmissionType.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION &&
                                   getSubmission(a.getId(), (User) state.getAttribute(STATE_USER), "sizeResources", state) != null) {
                            // and those deleted but not non-electronic assignments but the user has made submissions to them
                            returnResources.add(a);
                        }
                    }
                } else {
                    // read all Assignments
                    returnResources.addAll(assignmentService.getAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING)));
                }

                //Filter assignments by group
                if(StringUtils.isNotEmpty(selectedGroup) && !"all".equals(selectedGroup)){
                    returnResources = ((List<Assignment>)returnResources).stream().filter(a -> a.getGroups().stream().anyMatch(g -> g.endsWith(selectedGroup))).collect(Collectors.toList());
                }

                returnResources = filterAssignments(returnResources, (AssignmentFilter) state.getAttribute(FILTER_OPTION)); 

                //Filter assignments by tags
                List<String> selectedTags = state.getAttribute(TAG_SELECTOR) != null ? Arrays.asList(((String) state.getAttribute(TAG_SELECTOR)).split(",")) : new ArrayList<>();
                if (CollectionUtils.isNotEmpty(selectedTags)) {
                    returnResources = ((List<Assignment>)returnResources).stream().filter(a -> {
                        List<String> tagIds = tagService.getTagAssociationIds(a.getContext(), a.getId());
                        tagIds.addAll(addInstructorAndGroupTags(a));
                        return (tagIds.containsAll(selectedTags));
                    }).collect(Collectors.toList());
                }
                
                //Search assignments by title
                String searchTerm = (String) state.getAttribute(SEARCH_ASSIGNMENTS);
                if (searchTerm != null && !searchTerm.isEmpty()) {
                    String normalizedSearchTerm = normalizeStringForSearch(searchTerm);
                    returnResources = ((List<Assignment>)returnResources).stream()
                        .filter(a -> normalizeStringForSearch(a.getTitle()).contains(normalizedSearchTerm))
                        .collect(Collectors.toList());
                }

                state.setAttribute(HAS_MULTIPLE_ASSIGNMENTS, returnResources.size() > 1);
                break;
            case MODE_INSTRUCTOR_REORDER_ASSIGNMENT:
                returnResources.addAll(assignmentService.getAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING)));
                break;
            case MODE_INSTRUCTOR_REPORT_SUBMISSIONS: {
                initViewSubmissionListOption(state);
                String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
                String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
                Boolean searchFilterOnly = Boolean.TRUE.equals(state.getAttribute(SUBMISSIONS_SEARCH_ONLY));

                Boolean has_multiple_groups_for_user = false;
                List<SubmitterSubmission> submissions = new ArrayList<>();

                try {
                    Site site = siteService.getSite(contextString);

                    for (Assignment a : assignmentService.getAssignmentsForContext(contextString)) {
                        String aRef = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
                        List<String> submitterIds = assignmentService.getSubmitterIdList(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);
                        Collection<String> dupUsers = new ArrayList<>();
                        if (a.getIsGroup()) {
                            Collection<Group> submitterGroups = assignmentService.getSubmitterGroupList(searchFilterOnly.toString(), allOrOneGroup, "", a.getId(), contextString);
                            for (Group group : submitterGroups) {
                                submitterIds.add(group.getId());
                            }
                            dupUsers = rangeAndGroups.defaultMultipleGroupCheck(a).stream().map(mgr -> mgr.user.getId()).collect(Collectors.toList());
                        }
                        // Have we found an anonymous assignment in the list.
                        hasOneAnon = hasOneAnon || assignmentService.assignmentUsesAnonymousGrading(a);
                        //get the list of users which are allowed to grade this assignment
                        List allowGradeAssignmentUsers = assignmentService.allowGradeAssignmentUsers(aRef);

                        if (!a.getDeleted() && (!a.getDraft()) && assignmentService.allowGradeSubmission(aRef)) {
                            Set<AssignmentSubmission> ss = assignmentService.getSubmissions(a);
                            for (AssignmentSubmission s : ss) {
                                if (s.getSubmitted() || (s.getReturned() && (s.getDateModified().isBefore(s.getDateReturned())))) {
                                    // if the group search is null or if it contains the group
                                    Set<AssignmentSubmissionSubmitter> submitters = s.getSubmitters();
                                    if (a.getIsGroup()) {
                                        if (submitterIds.contains(s.getGroupId())) {
                                            for (AssignmentSubmissionSubmitter submitter : submitters) {
                                                if (submitterIds.contains(submitter.getSubmitter())) {
                                                    Member member = site.getMember(submitter.getSubmitter());
                                                    if (member != null && member.isActive()) {
                                                        // only include the active student submission
                                                        // conder TODO create temporary submissions
                                                        try {
                                                            User user = userDirectoryService.getUser(submitter.getSubmitter());
                                                            SubmitterSubmission subsub = new SubmitterSubmission(user, s);
                                                            subsub.setGroup(site.getGroup(s.getGroupId()));
                                                            if (dupUsers.contains(submitter.getSubmitter())) {
                                                                subsub.setMultiGroup(true);
                                                                has_multiple_groups_for_user = true;
                                                            }
                                                            submissions.add(subsub);
                                                        } catch (UserNotDefinedException e) {
                                                            log.warn("Cannot find user id={}, {}", submitter.getSubmitter(), e.getMessage());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        submitters.stream().filter(submitter -> submitterIds.contains(submitter.getSubmitter())).forEach(submitter -> {
                                            if (!allowGradeAssignmentUsers.contains(submitter.getSubmitter())) {
                                                // find whether the submitter is still an active member of the site
                                                Member member = site.getMember(submitter.getSubmitter());
                                                if (member != null && member.isActive()) {
                                                    // only include the active student submission
                                                    try {
                                                        User user = userDirectoryService.getUser(submitter.getSubmitter());
                                                        SubmitterSubmission subsub = new SubmitterSubmission(user, s);
                                                        submissions.add(subsub);
                                                    } catch (UserNotDefinedException e) {
                                                        log.warn("Cannot find user id={}, {}", submitter.getSubmitter(), e.getMessage());
                                                    }
                                                }
                                            }
                                        });
                                    }
                                } // if-else
                            }
                        }
                    }
                } catch (IdUnusedException e) {
                    log.warn("Could not retrieve site: {}, {}", contextString, e.getMessage());
                }

                if (has_multiple_groups_for_user) {
                    addAlert(state, rb.getString("group.user.multiple.error"));
                }

                returnResources = submissions;
                break;
            }
            case MODE_INSTRUCTOR_GRADE_ASSIGNMENT:
            case MODE_INSTRUCTOR_GRADE_SUBMISSION: {
                initViewSubmissionListOption(state);
                String allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
                String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);
                String aRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
                Boolean searchFilterOnly = Boolean.TRUE.equals(state.getAttribute(SUBMISSIONS_SEARCH_ONLY));

                Assignment assignment = null;
                assignment = getAssignment(aRef, "sizeResources", state);

                if (assignment != null) {
                    if (assignmentService.assignmentUsesAnonymousGrading(assignment)) {
                        allOrOneGroup = "all";
                    }
                    // TODO this could be optimized as we already have the assignment.
                    if (assignment.getIsGroup()) {
                        allOrOneGroup = MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode) ? "all" : allOrOneGroup;
                        Collection<Group> submitterGroups = assignmentService.getSubmitterGroupList("false", allOrOneGroup, "", assignment.getId(), contextString);

                        // construct the group-submission list
                        if (submitterGroups != null) {
                            for (Group gId : submitterGroups) {
                                AssignmentSubmission sub = getSubmission(aRef, gId.getId(), "sizeResources", state);
                                if (sub != null) {
                                    returnResources.add(new SubmitterSubmission(gId, sub));  // UserSubmission accepts either User or Group
                                } else {
                                    log.warn("Cannot find submission with reference = {}, group = {}", aRef, gId.getId());
                                }
                            }
                        }
                    } else {
                        Map<User, AssignmentSubmission> submitters = assignmentService.getSubmitterMap(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);
                        // construct the user-submission list
                        for (User u : submitters.keySet()) {
                            String uId = u.getId();

                            AssignmentSubmission sub = submitters.get(u);
                            SubmitterSubmission us = new SubmitterSubmission(u, sub);
                            String submittedById = sub.getProperties().get(AssignmentConstants.SUBMITTER_USER_ID);
                            if (submittedById != null) {
                                try {
                                    us.setSubmittedBy(userDirectoryService.getUser(submittedById));
                                } catch (UserNotDefinedException ex1) {
                                    log.warn(this + ":sizeResources cannot find submitter id=" + uId + ex1.getMessage());
                                }
                            }
                            returnResources.add(us);
                        }
                    }
                    // Have we found an anonymous assignment in the list.
                    hasOneAnon = hasOneAnon || assignmentService.assignmentUsesAnonymousGrading(assignment);
                }
                break;
            }
            case MODE_LIST_DELETED_ASSIGNMENTS:
                returnResources.addAll(assignmentService.getDeletedAssignmentsForContext((String) state.getAttribute(STATE_CONTEXT_STRING)));
                break;
        }

        // sort them all
        String ascending = "true";
        String sort = "";
        ascending = (String) state.getAttribute(SORTED_ASC);
        sort = (String) state.getAttribute(SORTED_BY);

        if (MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode)
                && (sort == null || !sort.startsWith("sorted_grade_submission_by"))) {
            ascending = (String) state.getAttribute(SORTED_GRADE_SUBMISSION_ASC);
            sort = (String) state.getAttribute(SORTED_GRADE_SUBMISSION_BY);
        } else if (MODE_INSTRUCTOR_REPORT_SUBMISSIONS.equals(mode) && (sort == null || sort.startsWith("sorted_submission_by"))) {
            ascending = (String) state.getAttribute(SORTED_SUBMISSION_ASC);
            sort = (String) state.getAttribute(SORTED_SUBMISSION_BY);
        } else {
            ascending = (String) state.getAttribute(SORTED_ASC);
            sort = (String) state.getAttribute(SORTED_BY);
        }

        if ((returnResources.size() > 1) && !MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(mode)) {
            AssignmentComparator ac = new AssignmentComparator(state, sort, ascending);

            // figure out if we have to sort by anonymous id
            if (SORTED_GRADE_SUBMISSION_BY_LASTNAME.equals(sort) &&
                    (MODE_INSTRUCTOR_GRADE_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode))) {
                String aRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
                Assignment assignment = getAssignment(aRef, "sizeResources", state);
                if (assignment != null) {
                    ac.setAnon(assignmentService.assignmentUsesAnonymousGrading(assignment));
                }
            } else if (hasOneAnon) {
                ac.setAnon(true);
            }

            try {
                Collections.sort(returnResources, ac);
            } catch (Exception e) {
                log.warn("sorting mode = {}, sort = {}, ascending = {}, {}", mode, sort, ascending, e.getMessage());
            }
        }

        // record the total item number
        state.setAttribute(STATE_PAGEING_TOTAL_ITEMS, returnResources);

        return returnResources.size();
    }

    public void doView(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        if (!alertGlobalNavigation(state, data)) {
            // we are changing the view, so start with first page again.
            resetPaging(state);

            // clear search form
            doSearch_clear(data, null);
            
            state.removeAttribute(TAG_SELECTOR);

            String viewMode = data.getParameters().getString("view");
            state.setAttribute(STATE_SELECTED_VIEW, viewMode);

            if (MODE_LIST_ASSIGNMENTS.equals(viewMode)) {
                doList_assignments(data);
            } else if (MODE_INSTRUCTOR_VIEW_STUDENTS_ASSIGNMENT.equals(viewMode)) {
                doView_students_assignment(data);
            } else if (MODE_INSTRUCTOR_REPORT_SUBMISSIONS.equals(viewMode)) {
                doReport_submissions(data);
            } else if (MODE_STUDENT_VIEW.equals(viewMode)) {
                doView_student(data);
            } if (MODE_LIST_DELETED_ASSIGNMENTS.equals(viewMode)) {
                doView_deletedAssignments(data);
            }

            // reset the global navigaion alert flag
            if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null) {
                state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
            }
        }

    } // doView

    /**
     * put those variables related to 2ndToolbar into context
     */
    private void add2ndToolbarFields(RunData data, Context context) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

        context.put("totalPageNumber", Integer.valueOf(totalPageNumber(state)));
        context.put("form_search", FORM_SEARCH);
        context.put("formPageNumber", FORM_PAGE_NUMBER);
        context.put("prev_page_exists", state.getAttribute(STATE_PREV_PAGE_EXISTS));
        context.put("next_page_exists", state.getAttribute(STATE_NEXT_PAGE_EXISTS));
        context.put("current_page", state.getAttribute(STATE_CURRENT_PAGE));
        context.put("selectedView", state.getAttribute(STATE_MODE));

    } // add2ndToolbarFields

    /**
     * Tests the format of the supplied grade and sets alert messages in the
     * state as required.
     */
    private void validPointGrade(SessionState state, String grade, int factor) {
        assignmentToolUtils.validPointGrade(grade, factor).forEach(a -> addAlert(state, a));
    }

    /**
     * valid grade for point based type
     */
    private void validLetterGrade(SessionState state, String grade) {
        String VALID_CHARS_FOR_LETTER_GRADE = " ABCDEFGHIJKLMNOPQRSTUVWXYZ+-";
        boolean invalid = false;
        if (grade != null) {
            grade = grade.toUpperCase();
            for (int i = 0; i < grade.length() && !invalid; i++) {
                char c = grade.charAt(i);
                if (VALID_CHARS_FOR_LETTER_GRADE.indexOf(c) == -1) {
                    invalid = true;
                }
            }
            if (invalid) {
                // -------- SAK-24199 (SAKU-274) by Shoji Kajita
                addAlert(state, rb.getFormattedMessage("plesuse0", grade));
                // --------
            }
        }
    }

    private void alertInvalidPoint(SessionState state, String grade, int factor) {
        assignmentToolUtils.alertInvalidPoint(grade, factor).forEach(a -> addAlert(state, a));
    }

    private String displayGrade(SessionState state, String grade, Integer factor) {
        String currentStateMessage = (String) state.getAttribute(STATE_MESSAGE);
        if (currentStateMessage == null || currentStateMessage.startsWith(rb.getString("pleasee6"))) {
            if (StringUtils.isNotBlank(grade)) {
                grade = assignmentService.getGradeDisplay(grade, Assignment.GradeType.SCORE_GRADE_TYPE, factor);
            } else {
                grade = "";
            }
        }
        return grade;
    }

    /**
     * scale the point value by "factor" if there is a valid point grade
     */
    protected String scalePointGrade(SessionState state, String point, int factor) {

        List<String> alerts = new ArrayList<>();
        point = assignmentToolUtils.scalePointGrade(point, factor, alerts);
        alerts.forEach(a -> addAlert(state, a));
        return point;
    } // scalePointGrade

    /**
     * Processes formatted text that is coming back from the browser (from the formatted text editing widget).
     *
     * @param state                    Used to pass in any user-visible alerts or errors when processing the text
     * @param strFromBrowser           The string from the browser
     * @param checkForFormattingErrors Whether to check for formatted text errors - if true, look for errors in the formatted text. If false, accept the formatted text without looking for errors.
     * @return The formatted text
     */
    private String processFormattedTextFromBrowser(SessionState state, String strFromBrowser, boolean checkForFormattingErrors) {
        return formattedText.processFormattedText(strFromBrowser, null, checkForFormattingErrors, true);
    }

    /**
     * Processes the given assignmnent feedback text, as returned from the user's browser. Makes sure that the Chef-style markup {{like this}} is properly balanced.
     */
    private String processAssignmentFeedbackFromBrowser(SessionState state, String strFromBrowser) {
        if (strFromBrowser == null || strFromBrowser.length() == 0) return strFromBrowser;

        StringBuilder buf = new StringBuilder(strFromBrowser);
        int pos = -1;
        int numopentags = 0;

        while ((pos = buf.indexOf("{{")) != -1) {
            buf.replace(pos, pos + "{{".length(), "<ins>");
            numopentags++;
        }

        while ((pos = buf.indexOf("}}")) != -1) {
            buf.replace(pos, pos + "}}".length(), "</ins>");
            numopentags--;
        }

        while (numopentags > 0) {
            buf.append("</ins>");
            numopentags--;
        }

        boolean checkForFormattingErrors = true; // so that grading isn't held up by formatting errors
        buf = new StringBuilder(processFormattedTextFromBrowser(state, buf.toString(), checkForFormattingErrors));

        while ((pos = buf.indexOf("<ins>")) != -1) {
            buf.replace(pos, pos + "<ins>".length(), "{{");
        }

        while ((pos = buf.indexOf("</ins>")) != -1) {
            buf.replace(pos, pos + "</ins>".length(), "}}");
        }

        return buf.toString();
    }

    private boolean alertGlobalNavigation(SessionState state, RunData data) {
        String mode = (String) state.getAttribute(STATE_MODE);
        ParameterParser params = data.getParameters();

        if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode) || MODE_STUDENT_PREVIEW_SUBMISSION.equals(mode) || MODE_STUDENT_CONFIRM_SUBMISSION.equals(mode)
                || MODE_STUDENT_VIEW_GRADE.equals(mode) || MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode)
                || MODE_INSTRUCTOR_DELETE_ASSIGNMENT.equals(mode) || (MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode) && !useSakaiGrader())
                || MODE_INSTRUCTOR_PREVIEW_GRADE_SUBMISSION.equals(mode) || MODE_INSTRUCTOR_PREVIEW_ASSIGNMENT.equals(mode)
                || MODE_INSTRUCTOR_VIEW_ASSIGNMENT.equals(mode) || MODE_INSTRUCTOR_REORDER_ASSIGNMENT.equals(mode)) {
            if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) == null) {
                addAlert(state, rb.getString("alert.globalNavi"));
                state.setAttribute(ALERT_GLOBAL_NAVIGATION, Boolean.TRUE);

                if (MODE_STUDENT_VIEW_SUBMISSION.equals(mode)) {
                    // save submit inputs
                    saveSubmitInputs(state, params);
                    state.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, rb.getString("gen.addatt"));

                    // TODO: file picker to save in dropbox? -ggolden
                    // User[] users = { userDirectoryService.getCurrentUser() };
                    // state.setAttribute(ResourcesAction.STATE_SAVE_ATTACHMENT_IN_DROPBOX, users);
                } else if (MODE_INSTRUCTOR_NEW_EDIT_ASSIGNMENT.equals(mode)) {
                    setNewAssignmentParameters(data, false);
                } else if (MODE_INSTRUCTOR_GRADE_SUBMISSION.equals(mode)) {
                    readGradeForm(data, state, "read");
                }

                return true;
            }
        }

        return false;

    } // alertGlobalNavigation

    /**
     * Dispatch function inside add submission page
     */
    public void doRead_add_submission_form(RunData data) {
        String option = data.getParameters().getString("option");
        if ("cancel".equals(option)) {
            // cancel
            doCancel_show_submission(data);
        } else if ("confirm".equals(option)) {
            if (StringUtils.isNotBlank(data.getParameters().get("submit_on_behalf_of"))) {
                // instructor submitting on behalf of a student go straight to posting submission
                doPost_submission(data);
            } else {
                // students go to confirmation screen
                doConfirm_submission(data);
            }
        } else if ("preview".equals(option)) {
            // preview
            doPreview_submission(data);
        } else if ("save".equals(option)) {
            // save draft
            doSave_submission(data);
        } else if ("post".equals(option)) {
            // post
            doPost_submission(data);
        } else if ("revise".equals(option)) {
            // done preview
            doDone_preview_submission(data);
        } else if ("attach".equals(option)) {
            // attach
            ToolSession toolSession = sessionManager.getCurrentToolSession();
            String userId = sessionManager.getCurrentSessionUserId();
            String siteId = siteService.getUserSiteId(userId);
            String collectionId = contentHostingService.getSiteCollection(siteId);
            toolSession.setAttribute(FilePickerHelper.DEFAULT_COLLECTION_ID, collectionId);
            doAttachments(data);
        } else if ("removeAttachment".equals(option)) {
            // remove selected attachment
            doRemove_attachment(data);
        } else if ("removeNewSingleUploadedFile".equals(option)) {
            doRemove_newSingleUploadedFile(data);
        } else if ("upload".equals(option)) {
            // upload local file
            doAttachUpload(data, true);
        } else if ("uploadSingleFile".equals(option)) {
            // upload single local file
            doAttachUpload(data, false);
        }
    }

    public void doRemove_attachment(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        // save submit inputs before refresh the page
        saveSubmitInputs(state, params);

        String mode = (String) state.getAttribute(STATE_MODE);

        String removeAttachmentId = params.getString("currentAttachment");
        List<Reference> attachments;
        if (MODE_STUDENT_REVIEW_EDIT.equals(mode)) {
            attachments = state.getAttribute(PEER_ATTACHMENTS) == null ? null : ((List<Reference>) state.getAttribute(PEER_ATTACHMENTS)).isEmpty() ? null : (List<Reference>) state.getAttribute(PEER_ATTACHMENTS);
        } else {
            attachments = state.getAttribute(ATTACHMENTS) == null ? null : ((List<Reference>) state.getAttribute(ATTACHMENTS)).isEmpty() ? null : (List<Reference>) state.getAttribute(ATTACHMENTS);
        }
        if (attachments != null) {
            for (Reference attachment : attachments) {
                if (attachment.getId().equals(removeAttachmentId)) {
                    attachments.remove(attachment);
                    // refresh state variable
                    if (MODE_STUDENT_REVIEW_EDIT.equals(mode)) {
                        state.setAttribute(PEER_ATTACHMENTS, attachments);
                    } else {
                        state.setAttribute(ATTACHMENTS, attachments);
                    }
                    break;
                }
            }
        }

    }

    public void doRemove_newSingleUploadedFile(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.removeAttribute("newSingleUploadedFile");
        state.removeAttribute("newSingleAttachmentList");
    }

    /**
     * return returns all groups in a site
     *
     * @param contextString
     * @return
     */
    private Collection<Group> getAllGroupsInSite(String contextString) {
        Collection<Group> groups = new ArrayList<>();
        try {
            Site site = siteService.getSite(contextString);
            // any group in the site?
            groups = site.getGroups();
        } catch (IdUnusedException e) {
            log.warn("Problem getting groups for site: {}, {}", contextString, e.getMessage());
        }
        return groups;
    }

    /**
     * return groups in a site where current user is member
     *
     * @param contextString
     * @return
     */
    private Collection<Group> getCurrentUserGroupsInSite(String contextString) {
        Collection<Group> groups = new ArrayList<>();
        try {
            Site site = siteService.getSite(contextString);
            groups = (securityService.unlock(userDirectoryService.getCurrentUser(), SECURE_ALL_GROUPS, siteService.siteReference(site.getId())))?site.getGroups():site.getGroupsWithMember(userDirectoryService.getCurrentUser().getId());
        } catch (IdUnusedException e) {
            log.warn("Could not fetch site: {}, {}", contextString, e.toString());
        }
        return groups;
    }

    /**
     * return list of submission object based on the group filter/search result
     *
     * @param state
     * @param aRef
     * @return
     */
    protected List<AssignmentSubmission> getFilteredSubmitters(SessionState state, String aRef) {
        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);
        String search = (String) state.getAttribute(VIEW_SUBMISSION_SEARCH);

        Assignment assignment = getAssignment(aRef, "getFilteredSubmitters", state);
        Boolean searchFilterOnly;
        String allOrOneGroup;
        if (assignment != null && assignment.getIsGroup()) {
            // For group assignments, ignore SUBMISSIONS_SEARCH_ONLY filter since group filtering/search is not available
            // Also ensure allOrOneGroup is "all"
            searchFilterOnly = Boolean.FALSE;
            allOrOneGroup = AssignmentConstants.ALL;
        } else {
            searchFilterOnly = Boolean.TRUE.equals(state.getAttribute(SUBMISSIONS_SEARCH_ONLY));
            allOrOneGroup = (String) state.getAttribute(VIEW_SUBMISSION_LIST_OPTION);
        }

        Map<User, AssignmentSubmission> submitters = assignmentService.getSubmitterMap(searchFilterOnly.toString(), allOrOneGroup, search, aRef, contextString);

        return new ArrayList<>(submitters.values());
    }

    /**
     * Set default score for all ungraded non electronic submissions
     *
     * @param data
     */
    public void doSet_defaultNotGradedNonElectronicScore(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String grade = StringUtils.trimToNull(params.getString(DEFAULT_GRADE));
        if (grade == null) {
            addAlert(state, rb.getString("plespethe2"));
        }

        String assignmentId = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
        Assignment a = getAssignment(assignmentId, "doSet_defaultNotGradedNonElectronicScore", state);
        if (a != null) {
            if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                //for point-based grades
                if (a.getScaleFactor() == null) a.setScaleFactor(assignmentService.getScaleFactor());
                validPointGrade(state, grade, a.getScaleFactor());

                if (state.getAttribute(STATE_MESSAGE) == null) {
                    int maxGrade = a.getMaxGradePoint();
                    try {
                        if (Integer.parseInt(scalePointGrade(state, grade, a.getScaleFactor())) > maxGrade) {
                            if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null) {
                                // alert user first when he enters grade bigger than max scale
                                addAlert(state, rb.getFormattedMessage("grad2", grade, displayGrade(state, String.valueOf(maxGrade), a.getScaleFactor())));
                                state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
                                state.setAttribute(DEFAULT_GRADE, grade);
                            } else {
                                // remove the alert once user confirms he wants to give student higher grade
                                state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.warn(this + ":setDefaultNotGradedNonElectronicScore " + e.getMessage());
                        alertInvalidPoint(state, grade, a.getScaleFactor());
                    }
                }

                // Only record the default grade setting for no-submission if there were no errors produced
                if (state.getAttribute(STATE_MESSAGE) == null) {

                    try {
                        assignmentService.updateAssignment(a);
                        state.setAttribute(DEFAULT_GRADE, StringUtils.EMPTY);
                    } catch (PermissionException e) {
                        log.warn("Could not update assignment: {}, {}", a.getId(), e.getMessage());
                    }
                }
            }


            if (grade != null && state.getAttribute(STATE_MESSAGE) == null) {
                if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                    grade = scalePointGrade(state, grade, a.getScaleFactor());
                }

                // get the user list
                String aRef = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
                List<AssignmentSubmission> submissions = getFilteredSubmitters(state, aRef);

                for (AssignmentSubmission submission : submissions) {
                    // get the submission object
                    if (submission.getSubmitted() && !submission.getGraded()) {
                        // update the grades for those existing non-submissions
                        submission.setGrade(grade);
                        submission.setGraded(true);
                        submission.setGradedBy(userDirectoryService.getCurrentUser() == null ? null : userDirectoryService.getCurrentUser().getId());
                        try {
                            assignmentService.updateSubmission(submission);
                        } catch (PermissionException e) {
                            log.warn("Could not update submission: {}, {}", submission.getId(), e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    public void doSet_defaultNoSubmissionScore(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String grade = StringUtils.trimToNull(params.getString(DEFAULT_GRADE));
        boolean checkNotGrade = params.getBoolean("notGrade");
        boolean checkNotSubmit = params.getBoolean("notSubmit");
        boolean checkNotPeerReview = params.getBoolean("notPeerReview");
        if (grade == null) {
            addAlert(state, rb.getString("plespethe2"));
        }

        String assignmentId = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
        Assignment a = getAssignment(assignmentId, "doSet_defaultNoSubmissionScore", state);
        if (a != null) {
            if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                //for point-based grades
                if (a.getScaleFactor() == null) a.setScaleFactor(assignmentService.getScaleFactor());
                validPointGrade(state, grade, a.getScaleFactor());

                if (state.getAttribute(STATE_MESSAGE) == null) {
                    int maxGrade = a.getMaxGradePoint();
                    try {
                        if (Integer.parseInt(scalePointGrade(state, grade, a.getScaleFactor())) > maxGrade) {
                            if (state.getAttribute(GRADE_GREATER_THAN_MAX_ALERT) == null) {
                                // alert user first when he enters grade bigger than max scale
                                addAlert(state, rb.getFormattedMessage("grad2", grade, displayGrade(state, String.valueOf(maxGrade), a.getScaleFactor())));
                                state.setAttribute(GRADE_GREATER_THAN_MAX_ALERT, Boolean.TRUE);
                                state.setAttribute(DEFAULT_GRADE, grade);
                            } else {
                                // remove the alert once user confirms he wants to give student higher grade
                                state.removeAttribute(GRADE_GREATER_THAN_MAX_ALERT);
                            }
                        }
                    } catch (NumberFormatException e) {
                        alertInvalidPoint(state, grade, a.getScaleFactor());
                        log.warn(this + ":setDefaultNoSubmissionScore " + e.getMessage());
                    }
                }

                // Only record the default grade setting for no-submission if there were no errors produced
                if (state.getAttribute(STATE_MESSAGE) == null) {
                    try {
                        assignmentService.updateAssignment(a);
                        state.setAttribute(DEFAULT_GRADE, StringUtils.EMPTY);
                    } catch (PermissionException e) {
                        log.warn("Could not update assignment: {}, {}", a.getId(), e.getMessage());
                    }
                }
            }


            if (grade != null && state.getAttribute(STATE_MESSAGE) == null) {
                if (a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
                    grade = scalePointGrade(state, grade, a.getScaleFactor());
                }

                // get the submission list
                String aRef = AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference();
                List<AssignmentSubmission> submissions = getFilteredSubmitters(state, aRef);
                List<String> draftSkipped = new ArrayList<>();

                boolean isPeerReviewAssignment = a.getAllowPeerAssessment()
                    && assignmentService.isPeerAssessmentClosed(a);

                for (AssignmentSubmission submission : submissions) {
                    String sGrade = StringUtils.trimToNull(submission.getGrade());

                    boolean applyDefaultGrade = false;
                    if (checkNotGrade || checkNotSubmit) {
                        if (sGrade == null || !submission.getGraded()) {
                            // update the grades for those existing non-submissions
                            applyDefaultGrade = (checkNotGrade && sGrade == null && submission.getDateSubmitted() != null)
                                    || (checkNotSubmit && submission.getDateSubmitted() == null);
                        }
                    }

                    boolean applyPeerReviewGrade = false;
                    boolean skipPeerReviewDraft = false;
                    if (checkNotPeerReview && !checkNotSubmit && isPeerReviewAssignment) {
                        String assessorId = null;
                        if (a.getIsGroup()) {
                            assessorId = submission.getGroupId();
                        } else if (submission.getSubmitters() != null && !submission.getSubmitters().isEmpty()) {
                            Optional<AssignmentSubmissionSubmitter> submitter = submission.getSubmitters().stream()
                                    .filter(AssignmentSubmissionSubmitter::getSubmittee)
                                    .findFirst();
                            if (submitter.isEmpty()) {
                                submitter = submission.getSubmitters().stream().findAny();
                            }
                            if (submitter.isPresent()) {
                                assessorId = submitter.get().getSubmitter();
                            }
                        }

                        if (StringUtils.isNotBlank(assessorId)) {
                            boolean hasDraft = false;
                            boolean hasNotStarted = false;
                            List<PeerAssessmentItem> peerItems = assignmentPeerAssessmentService.getPeerAssessmentItems(a.getId(), assessorId, a.getScaleFactor());
                            if (peerItems != null && !peerItems.isEmpty()) {
                                for (PeerAssessmentItem item : peerItems) {
                                    if (Boolean.TRUE.equals(item.getRemoved()) || Boolean.TRUE.equals(item.getSubmitted())) {
                                        continue;
                                    }
                                    String comment = StringUtils.trimToNull(item.getComment());
                                    if (item.getScore() != null || comment != null) {
                                        hasDraft = true;
                                    } else {
                                        hasNotStarted = true;
                                    }
                                }
                            }

                            if (hasNotStarted) {
                                applyPeerReviewGrade = true;
                            } else if (hasDraft) {
                                skipPeerReviewDraft = true;
                            }
                        }
                    }

                    boolean applyGrade = applyDefaultGrade || (applyPeerReviewGrade && !skipPeerReviewDraft);

                    if (skipPeerReviewDraft && !applyDefaultGrade) {
                        String displayName = null;
                        if (a.getIsGroup()) {
                            String siteId = toolManager.getCurrentPlacement().getContext();
                            try {
                                Site site = siteService.getSite(siteId);
                                Group group = site.getGroup(submission.getGroupId());
                                if (group != null) {
                                    displayName = group.getTitle();
                                }
                            } catch (IdUnusedException e) {
                                log.warn("Could not resolve site for group submission: {}", e.getMessage());
                            }
                        } else {
                            Optional<User> submitter = assignmentToolUtils.getSubmitters(submission).findFirst();
                            if (submitter.isPresent()) {
                                displayName = submitter.get().getDisplayName();
                            }
                        }
                        if (StringUtils.isNotBlank(displayName) && !draftSkipped.contains(displayName)) {
                            draftSkipped.add(displayName);
                        }
                    }

                    if (applyGrade) {
                        submission.setGrade(grade);
                        submission.setSubmitted(true);
                        submission.setGraded(true);
                        submission.setGradedBy(userDirectoryService.getCurrentUser() == null ? null : userDirectoryService.getCurrentUser().getId());
                        try {
                            assignmentService.updateSubmission(submission);
                        } catch (PermissionException e) {
                            log.warn("Could not update submission: {}, {}", submission.getId(), e.getMessage());
                        }
                    }
                }

                if (!draftSkipped.isEmpty()) {
                    String message = rb.getFormattedMessage("peerassessment.defaultgrade.draft.skipped", String.join(", ", draftSkipped));
                    state.setAttribute(STATE_PEER_REVIEW_DRAFT_SKIPPED_MESSAGE, message);
                }
            }
        }
    }

    public void doDownload_all(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
        ParameterParser params = data.getParameters();
        String downloadUrl = params.getString("downloadUrl");
        state.setAttribute(STATE_DOWNLOAD_URL, downloadUrl);
    }

    public void doUpload_all(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();
        // see if the user uploaded a file
        FileItem fileFromUpload = null;
        String fileName = null;
        fileFromUpload = params.getFileItem("file");
        String max_file_size_mb = serverConfigurationService.getString("content.upload.max", "1");

        if (fileFromUpload == null) {
            // "The user submitted a file to upload but it was too big!"
            addAlert(state, rb.getFormattedMessage("size.exceeded", max_file_size_mb));
        } else {
            String fname = StringUtils.lowerCase(fileFromUpload.getFileName());

            if (!StringUtils.endsWithAny(fname, new String[]{".zip", ".sit"})) {
                // no file
                addAlert(state, rb.getString("uploadall.alert.zipFile"));
            } else {
                String contextString = toolManager.getCurrentPlacement().getContext();
                String toolTitle = toolManager.getTool(AssignmentConstants.TOOL_ID).getTitle();
                String aReference = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
                String associateGradebookAssignment = null;

                List<String> choices = params.getStrings("choices") != null ? new ArrayList<>(Arrays.asList(params.getStrings("choices"))) : new ArrayList<>();

                if (choices.isEmpty()) {
                    // has to choose one upload feature
                    addAlert(state, rb.getString("uploadall.alert.choose.element"));
                    state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT);
                    state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT);
                    state.removeAttribute(UPLOAD_ALL_HAS_GRADEFILE);
                    state.removeAttribute(UPLOAD_ALL_GRADEFILE_FORMAT);
                    state.removeAttribute(UPLOAD_ALL_HAS_COMMENTS);
                    state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT);
                    state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT);
                    state.removeAttribute(UPLOAD_ALL_WITHOUT_FOLDERS);
                    state.removeAttribute(UPLOAD_ALL_RELEASE_GRADES);
                } else {
                    // should contain student submission text information
                    boolean hasSubmissionText = uploadAll_readChoice(choices, "studentSubmissionText");
                    // should contain student submission attachment information
                    boolean hasSubmissionAttachment = uploadAll_readChoice(choices, "studentSubmissionAttachment");
                    // should contain grade file
                    boolean hasGradeFile = uploadAll_readChoice(choices, "gradeFile");
                    String gradeFileFormat = params.getString("gradeFileFormat");
                    if ("excel".equals(gradeFileFormat)) {
                        gradeFileFormat = "excel";
                    } else {
                        gradeFileFormat = "csv";
                    }
                    ;
                    // inline text
                    boolean hasFeedbackText = uploadAll_readChoice(choices, "feedbackTexts");
                    // comments.txt should be available
                    boolean hasComment = uploadAll_readChoice(choices, "feedbackComments");
                    // feedback attachment
                    boolean hasFeedbackAttachment = uploadAll_readChoice(choices, "feedbackAttachments");
                    // folders
                    //boolean withoutFolders = params.getString("withoutFolders") != null ? params.getBoolean("withoutFolders") : false;
                    boolean withoutFolders = uploadAll_readChoice(choices, "withoutFolders"); // SAK-19147
                    // release
                    boolean releaseGrades = params.getString("release") != null && params.getBoolean("release");

                    state.setAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT, Boolean.valueOf(hasSubmissionText));
                    state.setAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT, Boolean.valueOf(hasSubmissionAttachment));
                    state.setAttribute(UPLOAD_ALL_HAS_GRADEFILE, Boolean.valueOf(hasGradeFile));
                    state.setAttribute(UPLOAD_ALL_GRADEFILE_FORMAT, gradeFileFormat);
                    state.setAttribute(UPLOAD_ALL_HAS_COMMENTS, Boolean.valueOf(hasComment));
                    state.setAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT, Boolean.valueOf(hasFeedbackText));
                    state.setAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT, Boolean.valueOf(hasFeedbackAttachment));
                    state.setAttribute(UPLOAD_ALL_WITHOUT_FOLDERS, Boolean.valueOf(withoutFolders));
                    state.setAttribute(UPLOAD_ALL_RELEASE_GRADES, Boolean.valueOf(releaseGrades));

                    // SAK-17606
                    Map<String, String> anonymousSubmissionAndEidTable = new HashMap<>();
                    String anonTitle = rb.getString("grading.anonymous.title");
                    // constructor the hashmap for all submission objects
                    Map<String, UploadGradeWrapper> submissionTable = new HashMap<>();
                    Set<AssignmentSubmission> submissions = null;
                    Assignment assignment = getAssignment(aReference, "doUpload_all", state);
                    if (assignment != null) {
                        associateGradebookAssignment = assignment.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
                        submissions = assignmentService.getSubmissions(assignment);
                        for (AssignmentSubmission s : submissions) {
                            String eid = null;
                            if (assignment.getIsGroup()) {
                                eid = s.getGroupId();
                            } else {
                                Optional<AssignmentSubmissionSubmitter> submitter = s.getSubmitters().stream().findAny();
                                if (submitter.isPresent()) {
                                    eid = submitter.get().getSubmitter();
                                }
                            }
                            if (eid != null) {
                                List<Reference> attachments = entityManager.newReferenceList();
                                attachments.addAll(s.getAttachments().stream().map(entityManager::newReference).collect(Collectors.toList()));
                                List<Reference> feedbackAttachments = entityManager.newReferenceList();
                                feedbackAttachments.addAll(s.getFeedbackAttachments().stream().map(entityManager::newReference).collect(Collectors.toList()));
                                submissionTable.put(eid, new UploadGradeWrapper(s.getGrade(), s.getSubmittedText(), s.getFeedbackComment(), hasSubmissionAttachment ? new ArrayList() : attachments, hasFeedbackAttachment ? new ArrayList() : feedbackAttachments, (s.getSubmitted() && s.getDateSubmitted() != null) ? Long.toString(s.getDateSubmitted().toEpochMilli()) : "", s.getFeedbackText()));
                                anonymousSubmissionAndEidTable.put(s.getId() + " " + anonTitle, eid);
                            } else {
                                log.warn("Upload missing submitter for submission {}", s.getId());
                            }
                        }
                    }
                    InputStream fileContentStream = fileFromUpload.getInputStream();
                    if (fileContentStream != null) {
                        submissionTable = uploadAll_parseZipFile(state,
                                hasSubmissionText, hasSubmissionAttachment,
                                hasGradeFile, hasFeedbackText, hasComment,
                                hasFeedbackAttachment, submissionTable,
                                assignment, fileContentStream, gradeFileFormat, anonymousSubmissionAndEidTable);
                    }

                    if (state.getAttribute(STATE_MESSAGE) == null) {
                        // update related submissions
                        uploadAll_updateSubmissions(state, aReference,
                                associateGradebookAssignment,
                                hasSubmissionText, hasSubmissionAttachment,
                                hasGradeFile, hasFeedbackText, hasComment,
                                hasFeedbackAttachment, releaseGrades,
                                submissionTable, submissions, assignment);
                    }
                }
            }
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            // go back to the list of submissions view
            cleanUploadAllContext(state);
            state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
        }
    }

    private boolean uploadAll_readChoice(List<String> choices, String text) {
        return choices != null && text != null && choices.contains(text);
    }

    /**
     * parse content inside uploaded zip file
     *
     * @param state
     * @param hasSubmissionText
     * @param hasSubmissionAttachment
     * @param hasGradeFile
     * @param hasFeedbackText
     * @param hasComment
     * @param hasFeedbackAttachment
     * @param submissionTable
     * @param assignment
     * @param fileContentStream
     * @return
     */
    private Map<String, UploadGradeWrapper> uploadAll_parseZipFile(SessionState state,
                                                                   boolean hasSubmissionText, boolean hasSubmissionAttachment,
                                                                   boolean hasGradeFile, boolean hasFeedbackText, boolean hasComment,
                                                                   boolean hasFeedbackAttachment, Map<String, UploadGradeWrapper> submissionTable,
                                                                   Assignment assignment, InputStream fileContentStream, String gradeFileFormat, Map<String, String> anonymousSubmissionAndEidTable) {
        // a flag value for checking whether the zip file is of proper format:
        // should have a grades.csv file or grades.xls if there is no user folders
        boolean zipHasGradeFile = false;
        // and if have any folder structures, those folders should be named after at least one site user (zip file could contain user names who is no longer inside the site)
        boolean zipHasFolder = false;
        boolean zipHasFolderValidUserId = false;

        FileOutputStream tmpFileOut = null;
        File tempFile = null;

        // as stated from UI, we expected the zip file to have structure as follows
        //       assignment_name/user_eid/files
        // or assignment_name/grades.csv or assignment_name/grades.xls
        boolean validZipFormat = true;

        try {
            tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), "");

            final Path destination = Paths.get(tempFile.getCanonicalPath());
            Files.copy(fileContentStream, destination, StandardCopyOption.REPLACE_EXISTING);
            final Charset tempFileCharset = getZipFileCharset(tempFile);

            ZipFile zipFile = new ZipFile(tempFile, tempFileCharset);
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            ZipEntry entry;
            // SAK-17606
            String anonTitle = rb.getString("grading.anonymous.title");
            boolean isAnon = assignmentService.assignmentUsesAnonymousGrading(assignment);
            while (zipEntries.hasMoreElements() && validZipFormat) {
                entry = zipEntries.nextElement();
                String entryName = entry.getName();
                if (!entry.isDirectory() && !entryName.contains("/.")) {
                    if (entryName.endsWith("grades.csv") || entryName.endsWith("grades.xls")) {
                        if (hasGradeFile && entryName.endsWith("grades.csv") && "csv".equals(gradeFileFormat)) {
                            // at least the zip file has a grade.csv
                            zipHasGradeFile = true;

                            // read grades.cvs from zip
                            String csvSep = assignmentService.getCsvSeparator();
                            CSVParser parser = new CSVParserBuilder().withSeparator(csvSep.charAt(0)).build();
                            CSVReader reader = new CSVReaderBuilder(new InputStreamReader(zipFile.getInputStream(entry))).withCSVParser(parser).build();

                            List<String[]> lines = reader.readAll();

                            if (lines != null) {
                                for (int i = 3; i < lines.size(); i++) {
                                    String[] items = lines.get(i);
                                    if ((assignment.getIsGroup() && items.length > 3) || items.length > 4) {
                                        // has grade information
                                        try {
                                            String eid = items[1];
                                            if (!assignment.getIsGroup()) {

                                                // SAK-17606
                                                User u = null;
                                                // check for anonymous grading
                                                if (!isAnon) {
                                                    u = userDirectoryService.getUserByEid(items[IDX_GRADES_CSV_EID]);
                                                } else { // anonymous so pull the real eid out of our hash table
                                                    String anonId = items[IDX_GRADES_CSV_EID];
                                                    String id = (String) anonymousSubmissionAndEidTable.get(anonId);
                                                    u = userDirectoryService.getUser(id);
                                                }

                                                if (u == null) throw new Exception("User not found!");
                                                eid = u.getId();
                                            }

                                            UploadGradeWrapper w = submissionTable.get(eid);
                                            if (w != null) {
                                                String itemString = assignment.getIsGroup() ? items[3] : items[4];
                                                Assignment.GradeType gradeType = assignment.getTypeOfGrade();
                                                if (gradeType == Assignment.GradeType.SCORE_GRADE_TYPE) {
                                                    validPointGrade(state, itemString, assignment.getScaleFactor());
                                                } // SAK-24199 - Applied patch provided with a few additional modifications.
                                                else if (gradeType == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
                                                    itemString = validatePassFailGradeValue(state, itemString);
                                                } else {
                                                    validLetterGrade(state, itemString);
                                                }
                                                if (state.getAttribute(STATE_MESSAGE) == null) {
                                                    w.setGrade(gradeType == Assignment.GradeType.SCORE_GRADE_TYPE ? scalePointGrade(state, itemString, assignment.getScaleFactor()) : itemString);
                                                    submissionTable.put(eid, w);
                                                }
                                            }

                                        } catch (Exception e) {
                                            log.warn(this + ":uploadAll_parseZipFile " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        } //end of csv grades import

                        //Excel file import
                        if (hasGradeFile && entryName.endsWith("grades.xls") && "excel".equals(gradeFileFormat)) {
                            // at least the zip file has a grade.csv
                            zipHasGradeFile = true;

                            // read grades.xls from zip
                            POIFSFileSystem fsFileSystem = new POIFSFileSystem(zipFile.getInputStream(entry));
                            HSSFWorkbook workBook = new HSSFWorkbook(fsFileSystem);
                            HSSFSheet hssfSheet = workBook.getSheetAt(0);
                            //Iterate the rows
                            Iterator rowIterator = hssfSheet.rowIterator();
                            int count = 0;
                            while (rowIterator.hasNext()) {
                                HSSFRow hssfRow = (HSSFRow) rowIterator.next();
                                //We skip first row (= header row)
                                if (count > 0) {
                                    double gradeXls = -1;
                                    String itemString = null;
                                    // has grade information
                                    try {
                                        String eid = hssfRow.getCell(1).getStringCellValue();
                                        if (!assignment.getIsGroup()) {
                                            if (!isAnon) {
                                                User u = userDirectoryService.getUserByEid(hssfRow.getCell(1).getStringCellValue()/*user eid*/);
                                                if (u == null) throw new Exception("User not found!");
                                                eid = u.getId();
                                            } else {
                                                eid = anonymousSubmissionAndEidTable.get(eid);
                                            }
                                        }
                                        UploadGradeWrapper w = submissionTable.get(eid);
                                        if (w != null) {
                                            itemString = assignment.getIsGroup() ? hssfRow.getCell(3).toString() : hssfRow.getCell(4).toString();
                                            Assignment.GradeType gradeType = assignment.getTypeOfGrade();
                                            if (gradeType == Assignment.GradeType.SCORE_GRADE_TYPE) {
                                                //Parse the string to double using the locale format
                                                try {
                                                    itemString = assignment.getIsGroup() ? hssfRow.getCell(3).getStringCellValue() : hssfRow.getCell(4).getStringCellValue();
                                                    if ((itemString != null) && (itemString.trim().length() > 0)) {
                                                        NumberFormat nbFormat = formattedText.getNumberFormat();
                                                        gradeXls = nbFormat.parse(itemString).doubleValue();
                                                    }
                                                } catch (Exception e) {
                                                    try {
                                                        gradeXls = assignment.getIsGroup() ? hssfRow.getCell(3).getNumericCellValue() : hssfRow.getCell(4).getNumericCellValue();
                                                    } catch (Exception e2) {
                                                        gradeXls = -1;
                                                    }
                                                }
                                                if (gradeXls != -1) {
                                                    // get localized number format
                                                    NumberFormat nbFormat = formattedText.getNumberFormat();
                                                    itemString = nbFormat.format(gradeXls);
                                                } else {
                                                    itemString = "";
                                                }

                                                validPointGrade(state, itemString, assignment.getScaleFactor());
                                            } else if (gradeType == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
                                                itemString = validatePassFailGradeValue(state, itemString);
                                            } else {
                                                validLetterGrade(state, itemString);
                                            }
                                            if (state.getAttribute(STATE_MESSAGE) == null) {
                                                w.setGrade(gradeType == Assignment.GradeType.SCORE_GRADE_TYPE ? scalePointGrade(state, itemString, assignment.getScaleFactor()) : itemString);
                                                submissionTable.put(eid, w);
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.warn(this + ":uploadAll_parseZipFile " + e.getMessage());
                                    }
                                }
                                count++;
                            }
                        } //end of Excel grades import

                    } else {
                        String[] pathParts = entryName.split("/");
                        if (pathParts.length <= 2) {
                            validZipFormat = false;
                        } else {
                            // get user eid part
                            String userEid = "";
                            if (entryName.contains("/")) {
                                // there is folder structure inside zip
                                if (!zipHasFolder) zipHasFolder = true;

                                // remove the part of zip name
                                userEid = entryName.substring(entryName.indexOf("/") + 1);
                                // get out the user name part
                                if (userEid.contains("/")) {
                                    userEid = userEid.substring(0, userEid.indexOf("/"));
                                }
                                // SAK-17606 - get the eid part
                                if (userEid.contains("(") && userEid.endsWith(")") && !userEid.contains(anonTitle)) {
                                    //The user will be at the end, grab this last set of parenthesis
                                    userEid = userEid.substring(userEid.lastIndexOf("(") + 1, userEid.lastIndexOf(")"));
                                }
                                if (userEid.contains(anonTitle)) { // anonymous grading so we have to figure out the eid
                                    //get eid out of this slick table we made earlier
                                    userEid = (String) anonymousSubmissionAndEidTable.get(userEid);
                                }

                                userEid = StringUtils.trimToNull(userEid);
                                if (!assignment.getIsGroup() && !isAnon) {
                                    try {
                                        User u = userDirectoryService.getUserByEid(userEid);
                                        if (u != null) userEid = u.getId();
                                    } catch (UserNotDefinedException unde) {
                                        log.warn("User not found: {}", userEid);
                                    }
                                }
                            }

                            if (submissionTable.containsKey(userEid)) {
                                if (!zipHasFolderValidUserId) zipHasFolderValidUserId = true;
                                if (hasComment && entryName.contains("comments")) {
                                    // read the comments file
                                    String comment = getBodyTextFromZipHtml(zipFile.getInputStream(entry), true);
                                    if (comment != null) {
                                        UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
                                        r.setComment(comment);
                                        submissionTable.put(userEid, r);
                                    }
                                }
                                if (hasFeedbackText && entryName.contains("feedbackText")) {
                                    // upload the feedback text
                                    String text = getBodyTextFromZipHtml(zipFile.getInputStream(entry), false);
                                    if (text != null) {
                                        UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
                                        r.setFeedbackText(text);
                                        submissionTable.put(userEid, r);
                                    }
                                }
                                if (hasSubmissionText && entryName.contains("_submissionText")) {
                                    // upload the student submission text
                                    String text = getBodyTextFromZipHtml(zipFile.getInputStream(entry), false);
                                    if (text != null) {
                                        UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
                                        r.setText(text);
                                        submissionTable.put(userEid, r);
                                    }
                                }
                                if (hasSubmissionAttachment) {
                                    // upload the submission attachment
                                    String submissionFolder = "/" + rb.getString("stuviewsubm.submissatt") + "/";
                                    if (entryName.contains(submissionFolder)) {
                                        // clear the submission attachment first
                                        UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
                                        submissionTable.put(userEid, r);
                                        submissionTable = uploadZipAttachments(state, submissionTable, zipFile.getInputStream(entry), entry, entryName, userEid, "submission");
                                    }
                                }
                                if (hasFeedbackAttachment) {
                                    // upload the feedback attachment
                                    String submissionFolder = "/" + rb.getString("download.feedback.attachment") + "/";
                                    if (entryName.contains(submissionFolder)) {
                                        // clear the feedback attachment first
                                        UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
                                        submissionTable.put(userEid, r);
                                        submissionTable = uploadZipAttachments(state, submissionTable, zipFile.getInputStream(entry), entry, entryName, userEid, "feedback");
                                    }
                                }

                                // if this is a timestamp file
                                if (entryName.contains("timestamp")) {
                                    byte[] timeStamp = readIntoBytes(zipFile.getInputStream(entry), entryName, entry.getSize());
                                    UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
                                    r.setSubmissionTimestamp(StringUtils.stripToEmpty(StringUtils.chomp(new String(timeStamp))));
                                    submissionTable.put(userEid, r);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // uploaded file is not a valid archive
            addAlert(state, rb.getString("uploadall.alert.zipFile"));
            log.warn(this + ":uploadAll_parseZipFile " + e.getMessage());
        } catch (CsvException e) {
            // uploaded file is not a valid archive
            addAlert(state, rb.getString("uploadall.alert.zipFile"));
            log.warn(this + ":uploadAll_parseZipFile " + e.getMessage());
        } finally {
            if (tmpFileOut != null) {
                try {
                    tmpFileOut.close();
                } catch (IOException e) {
                    log.warn(this + ":uploadAll_parseZipFile: Error closing temp file output stream: " + e.toString());
                }
            }

            if (fileContentStream != null) {
                try {
                    fileContentStream.close();
                } catch (IOException e) {
                    log.warn(this + ":uploadAll_parseZipFile: Error closing file upload stream: " + e.toString());
                }
            }

            //clean up the zip file
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to clean up temp file");
                }
            }

        }

        //This is used so that the "Zip Error" message is only printed once

        boolean zipError = false;

        // generate error when there is no grade file yet it was selected
        if (!zipHasGradeFile && hasGradeFile) {
            if ("csv".equals(gradeFileFormat)) {
                addAlert(state, rb.getString("uploadall.alert.nocsv"));
            } else {
                addAlert(state, rb.getString("uploadall.alert.noexcel"));
            }
            zipError = true;
        }

        // generate error when there is no grade file and no folder structure
        if (!zipHasGradeFile && !zipHasFolder) {
            addAlert(state, rb.getString("uploadall.alert.incorrectFormat"));
            addAlert(state, rb.getString("uploadall.alert.noGradeFile"));
            zipError = true;
        }
        // generate error when there is folder structure but not matching one user id
        if (zipHasFolder && !zipHasFolderValidUserId) {
            if (zipError == false)
                addAlert(state, rb.getString("uploadall.alert.incorrectFormat"));
            addAlert(state, rb.getString("uploadall.alert.invalidUserId"));
            zipError = true;
        }
        // should have right structure of zip file
        if (!validZipFormat) {
            if (zipError == false)
                addAlert(state, rb.getString("uploadall.alert.incorrectFormat"));

            // alert if the zip is of wrong format
            addAlert(state, rb.getString("uploadall.alert.wrongZipFormat"));
            zipError = true;

        }
        return submissionTable;
    }

    private Charset getZipFileCharset(File tempFile) {
        final Charset[] possibleCharSets = new Charset[]{
                StandardCharsets.UTF_8,
                Charset.forName("IBM437"),
                Charset.forName("windows-1252"),
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII,
                Charset.forName("Cp437")
        };

        for (Charset c : possibleCharSets) {
            try {
                ZipFile zf = new ZipFile(tempFile, c);
                Enumeration<? extends ZipEntry> zipEntries = zf.entries();
                // Try to provoke a MalformedNameException
                while (zipEntries.hasMoreElements()) {
                    zipEntries.nextElement();
                }
                return c;
            } catch (Exception e) {
                log.debug("ZIP encoding detection was incorrect for charset=", c.toString());
            }
        }

        return StandardCharsets.UTF_8;
    }

    /**
     * Update all submission objects based on uploaded zip file
     *
     * @param state
     * @param aReference
     * @param associateGradebookAssignment
     * @param hasSubmissionText
     * @param hasSubmissionAttachment
     * @param hasGradeFile
     * @param hasFeedbackText
     * @param hasComment
     * @param hasFeedbackAttachment
     * @param releaseGrades
     * @param submissionTable
     * @param submissions
     * @param assignment
     */
    private void uploadAll_updateSubmissions(SessionState state,
                                             String aReference, String associateGradebookAssignment,
                                             boolean hasSubmissionText, boolean hasSubmissionAttachment,
                                             boolean hasGradeFile, boolean hasFeedbackText, boolean hasComment,
                                             boolean hasFeedbackAttachment, boolean releaseGrades,
                                             Map<String, UploadGradeWrapper> submissionTable, Set<AssignmentSubmission> submissions, Assignment assignment) {
        if (assignment != null && submissions != null) {
            for (AssignmentSubmission submission : submissions) {
                String eid;
                if (assignment.getIsGroup()) {
                    eid = submission.getGroupId();
                } else {
                    Optional<AssignmentSubmissionSubmitter> submitter = submission.getSubmitters().stream().findAny();
                    if (submitter.isPresent()) {
                        eid = submitter.get().getSubmitter();
                    } else {
                        log.warn("Upload while updating submission missing submitter for submission {}", submission.getId());
                        continue;
                    }
                }
                User contentReviewSubmitter = null;
                if (assignment.getContentReview()) {
                    // Identify the user submitting to the content review service
                    if (assignment.getIsGroup()) {
                        // Same behaviour as submit_on_behalf_of in group assignments - use the current user
                        contentReviewSubmitter = userDirectoryService.getCurrentUser();
                    } else {
                        try {
                            contentReviewSubmitter = userDirectoryService.getUser(eid);
                        }
                        catch (UserNotDefinedException e) {
                            log.warn("Cannot find user {} for submission {}; skipping", eid, submission.getId());
                            continue;
                        }
                    }
                }

                UploadGradeWrapper w = submissionTable.get(eid);
                if (w != null) {
                    // the feedback text
                    if (hasFeedbackText) {
                        submission.setFeedbackText(w.getFeedbackText());
                    }

                    // the submission attachment
                    if (hasSubmissionAttachment) {
                        // update the submission attachments with newly added ones from the zip file
                        List<Reference> newAttachments = w.getSubmissionAttachments();
                        if (!newAttachments.isEmpty()) {
                            Set<String> submittedAttachments = submission.getAttachments();
                            // To eliminate duplication, it would be good to clear existing attachments here; but only once we have submission histories (would introduce data loss atm)
                            newAttachments.stream().forEach(ref -> {
                                if (!submittedAttachments.contains(ref.getReference())) {
                                    submittedAttachments.add(ref.getReference());
                                }
                            });
                        }
                    }

                    // the submission text
                    if (hasSubmissionText) {
                        String submissionText = w.getText();
                        if (assignment.getContentReview()) {
                            if (!isHtmlEmpty(submissionText)) {
                                prepareInlineForContentReview(submissionText, submission, state, contentReviewSubmitter);
                            }
                        }
                        submission.setSubmittedText(submissionText);
                    }

                    if (assignment.getContentReview() && !submission.getAttachments().isEmpty()) {
                        assignmentService.postReviewableSubmissionAttachments(submission);
                    }

                    // the feedback attachment
                    if (hasFeedbackAttachment) {
                        Set<String> feedbackAttachments = submission.getFeedbackAttachments();
                        for (Object o : w.getFeedbackAttachments()) {
                            // update the feedback attachments with newly added ones from zip file
                            Reference a = (Reference) o;
                            if (!feedbackAttachments.contains(a.getReference())) {
                                feedbackAttachments.add(a.getReference());
                            }
                        }
                    }

                    // the feedback comment
                    if (hasComment && StringUtils.isNotBlank(w.getComment())) {
                        submission.setFeedbackComment(w.getComment());
                        // ungraded assignments are considered graded when a comment is entered
                        if (assignment.getTypeOfGrade() == Assignment.GradeType.UNGRADED_GRADE_TYPE) {
                            submission.setGrade(null);
                            submission.setGraded(true);
                        }
                    }

                    // the grade file
                    if (hasGradeFile && assignment.getTypeOfGrade() != Assignment.GradeType.UNGRADED_GRADE_TYPE) {
                        // set grade
                        String grade = StringUtils.trimToNull(w.getGrade());
                        submission.setGrade(grade);
                        if (grade != null && !grade.equals(rb.getString("gen.nograd")) && !UNGRADED_GRADE_STRING.equals(grade)) {
                            submission.setGraded(true);
                            submission.setGradedBy(userDirectoryService.getCurrentUser() == null ? null : userDirectoryService.getCurrentUser().getId());
                        }
                    }

                    // release or not depending on if it's graded
                    if (submission.getGraded()) {
                        submission.setGradeReleased(releaseGrades);
                        submission.setReturned(releaseGrades);
                    } else {
                        submission.setGradeReleased(false);
                        submission.setReturned(false);
                    }

                    if (releaseGrades && submission.getGraded()) {
                        submission.setDateReturned(Instant.now());
                    }

                    // if the current submission lacks timestamp while the timestamp exists inside the zip file
                    if (StringUtils.trimToNull(w.getSubmissionTimeStamp()) != null && submission.getDateSubmitted() == null) {
                        Instant timestamp = Instant.now(); // default is now
                        if (StringUtils.isNumeric(w.getSubmissionTimeStamp())) {
                            // if timestamp in file is a numeric
                            timestamp = Instant.ofEpochMilli(Long.parseLong(w.getSubmissionTimeStamp()));
                        } else {
                            // otherwise should be 2020-03-06T16:10:21Z
                            try {
                                timestamp = Instant.parse(w.getSubmissionTimeStamp());
                            } catch (DateTimeParseException dtpe) {
                                log.warn("Could not parse timestamp {} for submission {}, {}", w.getSubmissionTimeStamp(), submission.getId(), dtpe.toString());
                            }
                        }
                        submission.setDateSubmitted(timestamp);
                        submission.setSubmitted(true);
                    }
                    else if (submission.getDateSubmitted() == null && (submission.getSubmittedText() != null || !submission.getAttachments().isEmpty()))
                    {
                        // Timestamp isn't present; use the current time
                        submission.setDateSubmitted(Instant.now());
                    }

                    // for further information
                    boolean graded = submission.getGraded();
                    String sReference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();

                    // commit
                    try {
                        assignmentService.updateSubmission(submission);
                        if (releaseGrades && graded) {
                            // update grade in gradebook
                            if (associateGradebookAssignment != null) {
                                String siteId = assignment.getContext();
                                updateGradebookAssociation(state, siteId, aReference, associateGradebookAssignment, sReference, "update");
                            }
                        }
                    } catch (PermissionException e) {
                        log.warn("Could not update submission: {}, {}", submission.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * A helper method for updating relevant gradebook associations.
     */
    private void updateGradebookAssociation(SessionState state, String siteId, String aReference, String associateGradebookAssignment, String submissionId, String updateRemoveSubmission) {
        if (StringUtils.isBlank(associateGradebookAssignment)) {
            return;
        }

        String update = StringUtils.equals("update", updateRemoveSubmission) ? updateRemoveSubmission : null;
        String remove = StringUtils.equals("remove", updateRemoveSubmission) ? updateRemoveSubmission : null;

        if (!gradingService.isGradebookGroupEnabled(siteId)) {
            addAlerts(state, assignmentToolUtils.integrateGradebook(stateToMap(state), siteId, aReference, associateGradebookAssignment, remove, null, -1, null, submissionId, update, -1));
        } else {
            List<String> gradebookUids = gradingService.getGradebookUidByExternalId(associateGradebookAssignment);
            if (CollectionUtils.isNotEmpty(gradebookUids)) {
                for (String gradebookUid : gradebookUids) {
                    addAlerts(state, assignmentToolUtils.integrateGradebook(stateToMap(state), gradebookUid, aReference, associateGradebookAssignment, remove, null, -1, null, submissionId, update, -1));
                }
            } else {
                log.warn("Cannot update gradebook. No gradebookUids found for associateGradebookAssignment: {}", associateGradebookAssignment);
            }
        }
    }
        
    private void addAlerts(SessionState state, List<String> alerts) {
        for (String alert : alerts) {
            addAlert(state, alert);
        }
    }

    /**
     * This is to get the submission or feedback attachment from the upload zip file into the submission object
     *
     * @param state
     * @param submissionTable
     * @param zin
     * @param entry
     * @param entryName
     * @param userEid
     * @param submissionOrFeedback
     */
    private Map<String, UploadGradeWrapper> uploadZipAttachments(SessionState state, Map<String, UploadGradeWrapper> submissionTable, InputStream zin, ZipEntry entry, String entryName, String userEid, String submissionOrFeedback) {
        // upload all the files as instructor attachments to the submission for grading purpose
        String fName = entryName.substring(entryName.lastIndexOf("/") + 1, entryName.length());
        ContentTypeImageService iService = contentTypeImageService;
        try {
            // get file extension for detecting content type
            // ignore those hidden files
            String extension = "";
            if (!fName.contains(".") || (fName.contains(".") && fName.indexOf(".") != 0)) {
                // add the file as attachment
                ResourceProperties properties = contentHostingService.newResourceProperties();
                properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fName);

                String[] parts = fName.split("\\.");
                if (parts.length > 1) {
                    extension = parts[parts.length - 1];
                }

                try {
                    String contentType = iService.getContentType(extension);
                    ContentResourceEdit attachment = contentHostingService.addAttachmentResource(fName);
                    attachment.setContent(zin);
                    attachment.setContentType(contentType);
                    attachment.getPropertiesEdit().addAll(properties);
                    contentHostingService.commitResource(attachment);

                    UploadGradeWrapper r = (UploadGradeWrapper) submissionTable.get(userEid);
                    List attachments = "submission".equals(submissionOrFeedback) ? r.getSubmissionAttachments() : r.getFeedbackAttachments();
                    attachments.add(entityManager.newReference(attachment.getReference()));
                    if ("submission".equals(submissionOrFeedback)) {
                        r.setSubmissionAttachments(attachments);
                    } else {
                        r.setFeedbackAttachments(attachments);
                    }
                    submissionTable.put(userEid, r);
                } catch (Exception e) {
                    log.warn(this + ":doUploadZipAttachments problem commit resource " + e.getMessage());
                }
            }
        } catch (Exception ee) {
            log.warn(this + ":doUploadZipAttachments " + ee.getMessage());
        }

        return submissionTable;
    }

    private String getBodyTextFromZipHtml(InputStream zin, boolean convertNewLines) {
        String rv = "";
        try {
            rv = StringUtils.trimToNull(readIntoString(zin));
        } catch (IOException e) {
            log.warn(this + ":getBodyTextFromZipHtml " + e.getMessage());
        }
        if (rv != null) {
            //SAK-28045 - Pre-process newlines
            if (convertNewLines == true) {
                rv = rv.replaceAll("\\r\\n|\\r|\\n", "<br>");
            }
            //Escape the html from malicious tags.
            rv = formattedText.processEscapedHtml(rv);

            int start = rv.indexOf("<body>");
            int end = rv.indexOf("</body>");
            if (start != -1 && end != -1) {
                // get the text in between
                rv = rv.substring(start + 6, end);
            }
        }
        return rv;
    }

    private byte[] readIntoBytes(InputStream zin, String fName, long length) throws IOException {

        byte[] buffer = new byte[4096];

        File f = File.createTempFile("asgnup", "tmp");

        FileOutputStream fout = new FileOutputStream(f);
        try {
            int len;
            while ((len = zin.read(buffer)) > 0) {
                fout.write(buffer, 0, len);
            }
            zin.close();
        } finally {
            try {
                fout.close(); // The file channel needs to be closed before the deletion.
            } catch (IOException ioException) {
                log.warn(this + "readIntoBytes: problem closing FileOutputStream " + ioException.getMessage());
            }
        }

        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        byte[] data = null;
        try {
            data = new byte[(int) (fc.size())];   // fc.size returns the size of the file which backs the channel
            ByteBuffer bb = ByteBuffer.wrap(data);
            fc.read(bb);
        } finally {
            try {
                fc.close(); // The file channel needs to be closed before the deletion.
            } catch (IOException ioException) {
                log.warn(this + "readIntoBytes: problem closing FileChannel " + ioException.getMessage());
            }

            try {
                fis.close(); // The file inputstream needs to be closed before the deletion.
            } catch (IOException ioException) {
                log.warn(this + "readIntoBytes: problem closing FileInputStream " + ioException.getMessage());
            }
        }

        //remove the file
        f.delete();

        return data;
    }

    private String readIntoString(InputStream zin) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int size = 2048;
        byte[] data = new byte[2048];
        while (true) {
            try {
                size = zin.read(data, 0, data.length);
                if (size > 0) {
                    buffer.append(new String(data, 0, size));
                } else {
                    break;
                }
            } catch (IOException e) {
                log.warn(this + ":readIntoString " + e.getMessage());
            }
        }
        return buffer.toString();
    }

    /**
     * @return
     */
    public void doCancel_download_upload_all(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_GRADE_ASSIGNMENT);
        cleanUploadAllContext(state);
    }

    /**
     * clean the state variabled used by upload all process
     */
    private void cleanUploadAllContext(SessionState state) {
        state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_TEXT);
        state.removeAttribute(UPLOAD_ALL_HAS_SUBMISSION_ATTACHMENT);
        state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_ATTACHMENT);
        state.removeAttribute(UPLOAD_ALL_HAS_FEEDBACK_TEXT);
        state.removeAttribute(UPLOAD_ALL_HAS_GRADEFILE);
        state.removeAttribute(UPLOAD_ALL_GRADEFILE_FORMAT);
        state.removeAttribute(UPLOAD_ALL_HAS_COMMENTS);
        state.removeAttribute(UPLOAD_ALL_WITHOUT_FOLDERS);
        state.removeAttribute(UPLOAD_ALL_RELEASE_GRADES);

    }

    /**
     * Action is to preparing to go to the download all file
     */
    public void doPrep_download_all(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();
        String view = params.getString("view");
        state.setAttribute(VIEW_SUBMISSION_LIST_OPTION, view);
        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_DOWNLOAD_ALL);

    } // doPrep_download_all

    /**
     * Action is to preparing to go to the upload files
     */
    public void doPrep_upload_all(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();
        state.setAttribute(STATE_MODE, MODE_INSTRUCTOR_UPLOAD_ALL);

    } // doPrep_upload_all

    private List<DecoratedTaggingProvider> initDecoratedProviders() {
        List<DecoratedTaggingProvider> providers = new ArrayList<DecoratedTaggingProvider>();
        for (TaggingProvider provider : taggingManager.getProviders()) {
            providers.add(new DecoratedTaggingProvider(provider));
        }
        return providers;
    }

    private List<DecoratedTaggingProvider> addProviders(Context context, SessionState state) {
        String mode = (String) state.getAttribute(STATE_MODE);
        List<DecoratedTaggingProvider> providers = (List) state.getAttribute(mode + PROVIDER_LIST);
        if (providers == null) {
            providers = initDecoratedProviders();
            state.setAttribute(mode + PROVIDER_LIST, providers);
        }
        context.put("providers", providers);
        return providers;
    }

    private void addActivity(Context context, Assignment assignment) {
        context.put("activity", assignmentActivityProducer.getActivity(assignment));

        String placement = toolManager.getCurrentPlacement().getId();
        context.put("iframeId", formattedText.escapeJavascript("Main" + placement));
    }

    private void addItem(Context context, AssignmentSubmission submission, String userId) {
        context.put("item", assignmentActivityProducer.getItem(submission, userId));
    }

    /**
     * add model answer input into state variables
     */
    public void doModel_answer(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String text = StringUtils.trimToNull(params.get("modelanswer_text"));

        int showTo = params.getInt("modelanswer_showto");
        if (showTo == 0) {
            // no show to criteria specifided for model answer
            addAlert(state, rb.getString("modelAnswer.show_to_student.alert.noShowTo"));
        }

        if (state.getAttribute(STATE_MESSAGE) == null) {
            state.setAttribute(NEW_ASSIGNMENT_MODEL_ANSWER, Boolean.TRUE);
            state.setAttribute(NEW_ASSIGNMENT_MODEL_ANSWER_TEXT, text);
            state.setAttribute(NEW_ASSIGNMENT_MODEL_SHOW_TO_STUDENT, showTo);
            //state.setAttribute(NEW_ASSIGNMENT_MODEL_ANSWER_ATTACHMENT);
        }
    }

    private void assignment_extension_option_into_context(Context context, SessionState state){
        String allowExtensionTimeString = null;
        if (state.getAttribute(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME) != null){
            allowExtensionTimeString = (String) state.getAttribute(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME);
            Instant extensionCloseTime = Instant.ofEpochMilli(Long.parseLong(allowExtensionTimeString));
            context.put("extensionCloseTime", assignmentService.getUsersLocalDateTimeString(extensionCloseTime));
        }
        putTimePropertiesInContext(context, state, "Extension", ALLOW_EXTENSION_CLOSE_MONTH, ALLOW_EXTENSION_CLOSE_DAY, ALLOW_EXTENSION_CLOSE_YEAR, ALLOW_EXTENSION_CLOSE_HOUR, ALLOW_EXTENSION_CLOSE_MIN);
    }

    private void assignment_extension_option_into_state(Assignment a, AssignmentSubmission s, SessionState state){
        String allowExtensionTimeString = null;
        if (s != null) {    // if submission is present, get the resubmission values from submission object first
            Map<String, String> sProperties = s.getProperties();
            allowExtensionTimeString = sProperties.get(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME);
        }
        Instant allowExtensionTime = null;
        if (allowExtensionTimeString != null) {
            state.setAttribute(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME, allowExtensionTimeString);
            allowExtensionTime = Instant.ofEpochMilli(Long.parseLong(allowExtensionTimeString));
        } else {
            state.removeAttribute(AssignmentConstants.ALLOW_EXTENSION_CLOSETIME);
        }
        if (allowExtensionTime != null) {   // set up related state variables
            putTimePropertiesInState(state, allowExtensionTime, ALLOW_EXTENSION_CLOSE_MONTH, ALLOW_EXTENSION_CLOSE_DAY, ALLOW_EXTENSION_CLOSE_YEAR, ALLOW_EXTENSION_CLOSE_HOUR, ALLOW_EXTENSION_CLOSE_MIN);
        }
    }

    private void assignment_resubmission_option_into_context(Context context, SessionState state) {
        context.put("name_allowResubmitNumber", AssignmentConstants.ALLOW_RESUBMIT_NUMBER);

        String allowResubmitNumber = state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) != null ? (String) state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) : null;
        String allowResubmitTimeString = state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME) != null ? (String) state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME) : null;

        // the resubmit number
        if (allowResubmitNumber != null && !"0".equals(allowResubmitNumber)) {
            context.put("value_allowResubmitNumber", Integer.valueOf(allowResubmitNumber));
            context.put("resubmitNumber", "-1".equals(allowResubmitNumber) ? rb.getString("allow.resubmit.number.unlimited") : allowResubmitNumber);

            // put allow resubmit time information into context
            putTimePropertiesInContext(context, state, "Resubmit", ALLOW_RESUBMIT_CLOSE_MONTH, ALLOW_RESUBMIT_CLOSE_DAY, ALLOW_RESUBMIT_CLOSE_YEAR, ALLOW_RESUBMIT_CLOSE_HOUR, ALLOW_RESUBMIT_CLOSE_MIN);
            // resubmit close time
            Instant resubmitCloseTime = null;
            if (allowResubmitTimeString != null) {
                resubmitCloseTime = Instant.ofEpochMilli(Long.parseLong(allowResubmitTimeString));
            }
            // put into context
            if (resubmitCloseTime != null) {
                context.put("resubmitCloseTime", assignmentService.getUsersLocalDateTimeString(resubmitCloseTime));
            }
        }

        context.put("value_year_from", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_FROM));
        context.put("value_year_to", state.getAttribute(NEW_ASSIGNMENT_YEAR_RANGE_TO));

    }

    private void assignment_resubmission_option_into_state(Assignment a, AssignmentSubmission s, SessionState state) {

        String allowResubmitNumber = null;
        String allowResubmitTimeString = null;

        if (s != null) {
            // if submission is present, get the resubmission values from submission object first
            Map<String, String> sProperties = s.getProperties();
            allowResubmitNumber = sProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
            allowResubmitTimeString = sProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
        } else if (a != null) {
            // otherwise, if assignment is present, get the resubmission values from assignment object next
            Map<String, String> aProperties = a.getProperties();
            allowResubmitNumber = aProperties.get(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
            allowResubmitTimeString = aProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
        }
        if (StringUtils.trimToNull(allowResubmitNumber) != null) {
            state.setAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, allowResubmitNumber);
        } else {
            state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER);
        }

        if (allowResubmitTimeString == null) {
            // default setting
            allowResubmitTimeString = String.valueOf(a.getCloseDate().toEpochMilli());
        }

        Instant allowResubmitTime = null;
        if (allowResubmitTimeString != null) {
            state.setAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, allowResubmitTimeString);

            // get time object
            allowResubmitTime = Instant.ofEpochMilli(Long.parseLong(allowResubmitTimeString));
        } else {
            state.removeAttribute(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
        }

        if (allowResubmitTime != null) {
            // set up related state variables
            putTimePropertiesInState(state, allowResubmitTime, ALLOW_RESUBMIT_CLOSE_MONTH, ALLOW_RESUBMIT_CLOSE_DAY, ALLOW_RESUBMIT_CLOSE_YEAR, ALLOW_RESUBMIT_CLOSE_HOUR, ALLOW_RESUBMIT_CLOSE_MIN);
        }
    }

    /******************* model answer *********/

    /**
     * save the resubmit option for selected users
     *
     * @param data
     */
    public void doSave_resubmission_option(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        // read in user input into state variable
        if (StringUtils.trimToNull(params.getString("allowResToggle")) != null) {
            if (params.getString(AssignmentConstants.ALLOW_RESUBMIT_NUMBER) != null) {
                // read in allowResubmit params
                readAllowResubmitParams(params, state, null);
            }
        } else {
            resetAllowResubmitParams(state);
        }

        String[] userIds = params.getStrings("selectedAllowResubmit");

        if (userIds == null || userIds.length == 0) {
            addAlert(state, rb.getString("allowResubmission.nouser"));
        } else {
            for (String userId : userIds) {
                try {
                    String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
                    Assignment a = getAssignment(assignmentRef, " resubmit update ", state);
                    AssignmentSubmission submission = null;
                    if (a.getIsGroup()) {
                        submission = getSubmission(assignmentRef, userId, "doSave_resubmission_option", state);
                    } else {
                        User u = userDirectoryService.getUser(userId);
                        submission = getSubmission(assignmentRef, u, "doSave_resubmission_option", state);
                    }
                    if (submission != null) {
                        // get resubmit number
                        Map<String, String> properties = submission.getProperties();
                        properties.put(AssignmentConstants.ALLOW_RESUBMIT_NUMBER, (String) state.getAttribute(AssignmentConstants.ALLOW_RESUBMIT_NUMBER));

                        if (state.getAttribute(ALLOW_RESUBMIT_CLOSE_YEAR) != null) {
                            // get resubmit time
                            Instant closeTime = getTimeFromState(state, ALLOW_RESUBMIT_CLOSE_MONTH, ALLOW_RESUBMIT_CLOSE_DAY, ALLOW_RESUBMIT_CLOSE_YEAR, ALLOW_RESUBMIT_CLOSE_HOUR, ALLOW_RESUBMIT_CLOSE_MIN);
                            properties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(closeTime.toEpochMilli()));
                        } else {
                            properties.remove(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
                        }

                        // save
                        assignmentService.updateSubmission(submission);
                    }
                } catch (Exception userException) {
                    log.warn(this + ":doSave_resubmission_option error getting user with id " + userId + " " + userException.getMessage());
                }
            }
        }

    }

    public void doSave_send_feedback(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();

        String[] userIds = params.getStrings("selectedAllowResubmit");
        String comment = processFormattedTextFromBrowser(state, params.getCleanString("commentFeedback"), true);
        String overwrite = params.getString("overWrite");

        if (userIds == null || userIds.length == 0) {
            addAlert(state, rb.getString("sendFeedback.nouser"));
        } else {
            if (comment.equals("")) {
                addAlert(state, rb.getString("sendFeedback.nocomment"));
            } else {
                int errorUsers = 0;
                for (String userId : userIds) {
                    try {
                        String assignmentRef = (String) state.getAttribute(EXPORT_ASSIGNMENT_REF);
                        Assignment assignment = getAssignment(assignmentRef, "doSave_send_feedback", state);
                        AssignmentSubmission submission = assignmentService.getSubmission(assignment.getId(), userId);
                        if (submission != null) {
                            String newFeedbackComment = "";
                            if (overwrite != null) {
                                newFeedbackComment = comment + "<br/>";
                                state.setAttribute(OW_FEEDBACK, Boolean.TRUE);
                            } else {
                                newFeedbackComment = ( submission.getFeedbackComment() == null ? "" : submission.getFeedbackComment() ) + comment + "<br/>";
                            }
                            submission.setFeedbackComment(newFeedbackComment);
                            if (assignment.getTypeOfGrade() == Assignment.GradeType.UNGRADED_GRADE_TYPE) {
                                submission.setGrade(null);
                                submission.setGraded(true);
                            }
                            assignmentService.updateSubmission(submission);
                            state.setAttribute(SAVED_FEEDBACK, Boolean.TRUE);
                        }
                    } catch (Exception userException) {
                        log.warn(this + ":doSave_send_feedback error getting user with id " + userId + " " + userException.getMessage());
                        errorUsers++;
                    }
                }
                if (errorUsers > 0) {
                    addAlert(state, rb.getFormattedMessage("sendFeedback.error", errorUsers));
                }
            }
        }
    }

    /**
     * multiple file upload
     *
     * @param data
     */
    public void doAttachUpload(RunData data) {
        // save the current input before leaving the page
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        saveSubmitInputs(state, data.getParameters());
        doAttachUpload(data, false);
        if (MODE_STUDENT_REVIEW_EDIT.equals(state.getAttribute(STATE_MODE))) {
            saveReviewGradeForm(data, state, "save");
        }
    }

    /**
     * single file upload
     *
     * @param data
     */
    public void doAttachUploadSingle(RunData data) {
        doAttachUpload(data, true);
    }

    /**
     * upload local file for attachment
     *
     * @param data
     * @param singleFileUpload
     */
    public void doAttachUpload(RunData data, boolean singleFileUpload) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ToolSession toolSession = sessionManager.getCurrentToolSession();
        ParameterParser params = data.getParameters();

        String max_file_size_mb = serverConfigurationService.getString("content.upload.max", "1");

        String mode = (String) state.getAttribute(STATE_MODE);
        List<Reference> attachments;

        boolean inPeerReviewMode = MODE_STUDENT_REVIEW_EDIT.equals(mode);
        if (inPeerReviewMode) {
            // construct the state variable for peer attachment list
            attachments = state.getAttribute(PEER_ATTACHMENTS) != null ? (List<Reference>) state.getAttribute(PEER_ATTACHMENTS) : entityManager.newReferenceList();
        } else {

            // construct the state variable for attachment list
            attachments = state.getAttribute(ATTACHMENTS) != null ? (List<Reference>) state.getAttribute(ATTACHMENTS) : entityManager.newReferenceList();
        }

        FileItem fileitem = null;
        try {
            fileitem = params.getFileItem("upload");
        } catch (Exception e) {
            // other exceptions should be caught earlier
            log.debug(this + ".doAttachupload ***** Unknown Exception ***** " + e.getMessage());
            addAlert(state, rb.getString("failed.upload"));
        }
        if (fileitem == null) {
            // "The user submitted a file to upload but it was too big!"
            addAlert(state, rb.getFormattedMessage("size.exceeded", max_file_size_mb));
            //addAlert(state, hrb.getString("size") + " " + max_file_size_mb + "MB " + hrb.getString("exceeded2"));
        } else if (singleFileUpload && (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)) {
            // only if in the single file upload case, need to warn user to upload a local file
            addAlert(state, rb.getString("choosefile7"));
        } else if (fileitem.getFileName().length() > 0) {
            String filename = FilenameUtils.getName(fileitem.getFileName());
            InputStream fileContentStream = fileitem.getInputStream();
            String contentType = fileitem.getContentType();

            InputStreamReader reader = new InputStreamReader(fileContentStream);

            try {
                //check the InputStreamReader to see if the file is 0kb aka empty
                if (!reader.ready()) {
                    addAlert(state, rb.getFormattedMessage("attempty", filename));
                } else {
                    // we just want the file name part - strip off any drive and path stuff
                    String name = FilenameUtils.getName(filename);
                    String resourceId = Validator.escapeResourceName(name);

                    // make a set of properties to add for the new resource
                    ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
                    props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
                    props.addProperty(ResourceProperties.PROP_DESCRIPTION, filename);

                    // make an attachment resource for this URL
                    SecurityAdvisor sa = createSubmissionSecurityAdvisor();
                    try {
                        String siteId = toolManager.getCurrentPlacement().getContext();

                        // add attachment
                        // put in a security advisor so we can create citationAdmin site without need
                        // of further permissions
                        securityService.pushAdvisor(sa);
                        ContentResource attachment = contentHostingService.addAttachmentResource(resourceId, siteId, "Assignments", contentType, fileContentStream, props);

                        if (mode.equals(MODE_STUDENT_VIEW_SUBMISSION)) {
                            if (fileConversionService.canConvert(attachment.getContentType())) {
                                fileConversionService.submit(attachment.getId());
                            }
                        }

                        Site s = null;
                        try {
                            s = siteService.getSite(siteId);
                        } catch (IdUnusedException iue) {
                            log.warn(this + ":doAttachUpload: Site not found!" + iue.getMessage());
                        }

                        // Check if the file is acceptable with the ContentReviewService
                        boolean blockedByCRS = false;
                        if (!inPeerReviewMode && assignmentService.allowReviewService(s)) {
                            String assignmentReference = (String) state.getAttribute(VIEW_SUBMISSION_ASSIGNMENT_REFERENCE);
                            Assignment a = getAssignment(assignmentReference, "doAttachUpload", state);
                            if (a.getContentReview()) {
                                if (!contentReviewService.isAcceptableContent(attachment)) {
                                    List<String> parameters = new ArrayList<>();
                                    parameters.add(contentReviewService.getServiceName());
                                    parameters.add(getContentReviewAcceptedFileTypesMessage());

                                    // Specific message for different providers
                                    String reviewServiceName = contentReviewService.getServiceName();
                                    switch(reviewServiceName) {
                                        case CONTENT_PROVIDER_TURNITIN:
                                            parameters.add(rb.getString("review.file.not.accepted." + reviewServiceName.toLowerCase()));
                                            break;
                                        default:
                                            parameters.add(StringUtils.EMPTY);
                                            break;
                                    }

                                    addAlert(state, rb.getFormattedMessage("review.file.not.accepted", parameters.stream().toArray(Object[]::new)));
                                    blockedByCRS = true;
                                    // TODO: delete the file? Could we have done this check without creating it in the first place?
                                }
                            }
                        }

                        if (!blockedByCRS) {
                            try {
                                Reference ref = entityManager.newReference(contentHostingService.getReference(attachment.getId()));
                                if (singleFileUpload && attachments.size() > 1) {
                                    //SAK-26319	- the assignment type is 'single file upload' and the user has existing attachments, so they must be uploading a 'newSingleUploadedFile'	--bbailla2
                                    state.setAttribute("newSingleUploadedFile", ref);
                                } else {
                                    attachments.add(ref);
                                }
                            } catch (Exception ee) {
                                log.warn(this + "doAttachUpload cannot find reference for " + attachment.getId() + ee.getMessage());
                            }
                        }

                        if (inPeerReviewMode) {
                            state.setAttribute(PEER_ATTACHMENTS, attachments);
                        } else {
                            state.setAttribute(ATTACHMENTS, attachments);
                        }
                    } catch (PermissionException e) {
                        addAlert(state, rb.getString("notpermis4"));
                    } catch (RuntimeException e) {
                        if (contentHostingService.ID_LENGTH_EXCEPTION.equals(e.getMessage())) {
                            // couldn't we just truncate the resource-id instead of rejecting the upload?
                            addAlert(state, rb.getFormattedMessage("alert.toolong", new String[]{name}));
                        } else {
                            log.debug(this + ".doAttachupload ***** Runtime Exception ***** " + e.getMessage());
                            addAlert(state, rb.getString("failed"));
                        }
                    } catch (ServerOverloadException e) {
                        // disk full or no writing permission to disk
                        log.debug(this + ".doAttachupload ***** Disk IO Exception ***** " + e.getMessage());
                        addAlert(state, rb.getString("failed.diskio"));
                    } catch (Exception ignore) {
                        // other exceptions should be caught earlier
                        log.debug(this + ".doAttachupload ***** Unknown Exception ***** " + ignore.getMessage());
                        addAlert(state, rb.getString("failed"));
                    } finally {
                        securityService.popAdvisor(sa);
                    }
                }
            } catch (IOException e) {
                log.debug(this + ".doAttachupload ***** IOException ***** " + e.getMessage());
                addAlert(state, rb.getString("failed"));
            }
        }
    }    // doAttachupload

    /**
     * This security advisor is used when making an assignment submission so that attachments can be added.
     *
     * @return The security advisor.
     */
    private SecurityAdvisor createSubmissionSecurityAdvisor() {
        return (userId, function, reference) -> {
            //Needed to be able to add or modify their own
            if (function.equals(contentHostingService.AUTH_RESOURCE_ADD) ||
                    function.equals(contentHostingService.AUTH_RESOURCE_WRITE_OWN) ||
                    function.equals(contentHostingService.AUTH_RESOURCE_HIDDEN)
                    ) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else if (function.equals(contentHostingService.AUTH_RESOURCE_WRITE_ANY)) {
                log.info(userId + " requested ability to write to any content on " + reference +
                        " which we didn't expect, this should be investigated");
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            }
            return SecurityAdvisor.SecurityAdvice.PASS;
        };
    }

    public void doOptions(RunData data, Context context) {
        doOptions(data);
    } // doOptions

    protected void doOptions(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String siteId = toolManager.getCurrentPlacement().getContext();
        try {
            Site site = siteService.getSite(siteId);
            ToolConfiguration tc = site.getToolForCommonId(AssignmentConstants.TOOL_ID);
            String optionValue = tc.getPlacementConfig().getProperty(SUBMISSIONS_SEARCH_ONLY);
            state.setAttribute(SUBMISSIONS_SEARCH_ONLY, optionValue == null ? Boolean.FALSE : Boolean.valueOf(optionValue));
            String optionTagsValue = tc.getPlacementConfig().getProperty(SHOW_TAGS_STUDENT);
            state.setAttribute(SHOW_TAGS_STUDENT, optionTagsValue == null ? Boolean.FALSE : Boolean.valueOf(optionTagsValue));
        } catch (IdUnusedException e) {
            log.warn(this + ":doOptions  Cannot find site with id " + siteId);
        }

        if (!alertGlobalNavigation(state, data)) {
            if (siteService.allowUpdateSite((String) state.getAttribute(STATE_CONTEXT_STRING))) {
                state.setAttribute(STATE_MODE, MODE_OPTIONS);
            } else {
                addAlert(state, rb.getString("youarenot_options"));
            }

            // reset the global navigaion alert flag
            if (state.getAttribute(ALERT_GLOBAL_NAVIGATION) != null) {
                state.removeAttribute(ALERT_GLOBAL_NAVIGATION);
            }
        }
    }

    /**
     * build the options
     */
    protected String build_options_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

        String siteId = (String) state.getAttribute(STATE_CONTEXT_STRING);
        context.put("context", siteId);

        Boolean submissionsSearchOnly = (Boolean) state.getAttribute(SUBMISSIONS_SEARCH_ONLY);
        Boolean showTagsStudents = (Boolean) state.getAttribute(SHOW_TAGS_STUDENT);

        if (submissionsSearchOnly == null || showTagsStudents == null) {
            try {
                Site site = siteService.getSite(siteId);
                ToolConfiguration tc = site.getToolForCommonId(AssignmentConstants.TOOL_ID);
                submissionsSearchOnly = BooleanUtils.toBoolean(tc.getPlacementConfig().getProperty(SUBMISSIONS_SEARCH_ONLY));
                showTagsStudents = BooleanUtils.toBoolean(tc.getPlacementConfig().getProperty(SHOW_TAGS_STUDENT));
            } catch (Exception e) {
            }
        }

        context.put(SUBMISSIONS_SEARCH_ONLY, submissionsSearchOnly);
        context.put(SHOW_TAGS_STUDENT, showTagsStudents);

        String template = (String) getContext(data).get("template");
        return template + TEMPLATE_OPTIONS;

    } // build_options_context

    /**
     * save the option edits
     *
     * @param data
     */
    public void doUpdate_options(RunData data) {
        if (!"POST".equals(data.getRequest().getMethod())) {
            return;
        }

        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        String siteId = toolManager.getCurrentPlacement().getContext();
        ParameterParser params = data.getParameters();

        // only show those submissions matching search criteria
        boolean submissionsSearchOnly = params.getBoolean(SUBMISSIONS_SEARCH_ONLY);
        state.setAttribute(SUBMISSIONS_SEARCH_ONLY, submissionsSearchOnly);
        boolean showTagsStudents = params.getBoolean(SHOW_TAGS_STUDENT);
        state.setAttribute(SHOW_TAGS_STUDENT, showTagsStudents);

        // save the option into tool configuration
        try {
            boolean changed = false;
            Site site = siteService.getSite(siteId);
            ToolConfiguration tc = site.getToolForCommonId(AssignmentConstants.TOOL_ID);
            String currentSetting = tc.getPlacementConfig().getProperty(SUBMISSIONS_SEARCH_ONLY);
            String currentSettingTags = tc.getPlacementConfig().getProperty(SHOW_TAGS_STUDENT);
            if (currentSetting == null || !currentSetting.equals(Boolean.toString(submissionsSearchOnly))) {
                changed = true;
                // save the change
                tc.getPlacementConfig().setProperty(SUBMISSIONS_SEARCH_ONLY, Boolean.toString(submissionsSearchOnly));
            }
            if (currentSettingTags == null || !currentSettingTags.equals(Boolean.toString(showTagsStudents))) {
                changed = true;
                tc.getPlacementConfig().setProperty(SHOW_TAGS_STUDENT, Boolean.toString(showTagsStudents));
            }
            if (changed) {
                siteService.save(site);
            }
        } catch (IdUnusedException e) {
            log.warn(this + ":doUpdate_options  Cannot find site with id " + siteId);
            addAlert(state, rb.getFormattedMessage("options_cannotFindSite", siteId));
        } catch (PermissionException e) {
            log.warn(this + ":doUpdate_options Do not have permission to edit site with id " + siteId);
            addAlert(state, rb.getFormattedMessage("options_cannotEditSite", siteId));
        }
        if (state.getAttribute(STATE_MESSAGE) == null) {
            // back to list view
            state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
        }
    } // doUpdate_options

    /**
     * cancel the option edits
     *
     * @param data
     * @param context
     */
    public void doCancel_options(RunData data, Context context) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        state.setAttribute(STATE_MODE, MODE_LIST_ASSIGNMENTS);
    } // doCancel_options

    protected void letterGradeOptionsIntoContext(Context context) {
        String lOptions = serverConfigurationService.getString("assignment.letterGradeOptions", "A+,A,A-,B+,B,B-,C+,C,C-,D+,D,D-,E,F");
        context.put("letterGradeOptions", StringUtils.split(lOptions, ","));
    }

    private LRS_Statement getStatementForViewSubmittedAssignment(String reference, String assignmentName) {
    	LRS_Actor actor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        String url = serverConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + reference, "view-submitted-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User reviewed a submitted assignment");
        lrsObject.setActivityName(nameMap);
        // Add description
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User reviewed a submitted assignment: " + assignmentName);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(actor, verb, lrsObject);
    }

    private LRS_Statement getStatementForViewAssignment(String reference, String assignmentName) {
    	LRS_Actor actor = learningResourceStoreService.getActor(sessionManager.getCurrentSessionUserId());
        String url = serverConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
        LRS_Object lrsObject = new LRS_Object(url + reference, "view-assignment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User viewed an assignment");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User viewed assignment: " + assignmentName);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(actor, verb, lrsObject);
    }

    /**
     * Validates the ungraded/pass/fail grade values provided in the upload file are valid.
     * Values must be present in the appropriate language property file.
     *
     * @param state
     * @param itemString
     * @return one of the valid values or the original value entered by the user
     */
    private String validatePassFailGradeValue(SessionState state, String itemString) {
        // -------- SAK-24199 (SAKU-274) by Shoji Kajita
        if (itemString.equalsIgnoreCase(rb.getString("pass"))) {
            itemString = "Pass";
        } else if (itemString.equalsIgnoreCase(rb.getString("fail"))) {
            itemString = "Fail";
        } else if (itemString.equalsIgnoreCase(rb.getString("ungra")) || itemString.isEmpty()) {
            itemString = UNGRADED_GRADE_TYPE_STRING;
        } else { // Not one of the expected values. Display error message.
            addAlert(state, rb.getFormattedMessage("plesuse0", itemString));
        }
        // --------

        return itemString;
    }

    /**
     * Action to determine which view do present to user.
     * This method is currently called from calendar events in the alternate calendar tool.
     */
    public void doCheck_view(RunData data) {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
        ParameterParser params = data.getParameters();
        String assignmentId = params.getString("assignmentId");

        String contextString = (String) state.getAttribute(STATE_CONTEXT_STRING);

        boolean allowReadAssignment = assignmentService.allowGetAssignment(contextString);
        boolean allowSubmitAssignment = assignmentService.allowAddSubmission(contextString);
        boolean allowAddAssignment = assignmentService.allowAddAssignment(contextString);

        // Retrieve the status of the assignment
        String assignStatus = getAssignmentStatus(assignmentId, state);
        if (assignStatus != null && !assignStatus.equals(rb.getString("gen.open")) && !allowAddAssignment) {
            addAlert(state, rb.getFormattedMessage("gen.notavail", assignStatus));
        }
        // Check the permission and call the appropriate view method.
        if (allowAddAssignment) {
            doView_assignment(data);
        } else if (allowSubmitAssignment) {
            doView_submission(data);
        } else if (allowReadAssignment) {
            doView_assignment_as_student(data);
        } else {
            addAlert(state, rb.getFormattedMessage("youarenot_viewAssignment", assignmentId));
        }
    } // doCheck_view

    /**
     * Retrieves the status of a given assignment.
     *
     * @param assignmentId
     * @param state
     * @return
     */
    private String getAssignmentStatus(String assignmentId, SessionState state) {
        String rv = null;
        try {
            Session session = sessionManager.getCurrentSession();
            rv = assignmentService.getAssignmentCannonicalStatus(assignmentId).toString();
        } catch (IdUnusedException e) {
            log.warn(this + " " + e.getMessage() + " " + assignmentId);
        } catch (PermissionException e) {
            log.warn(this + e.getMessage() + " " + assignmentId);
        }

        return rv;
    }

    /**
     * Set properties related to grading via an external scoring service. This service may be enabled for the
     * associated gradebook item.
     */
    protected void setScoringAgentProperties(Context context, Assignment assignment, AssignmentSubmission submission, boolean gradeView) {
        String associatedGbItem = assignment.getProperties().get(PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
        if (!assignment.getIsGroup() && submission != null && StringUtils.isNotBlank(associatedGbItem) && assignment.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE) {
            ScoringService scoringService = (ScoringService) ComponentManager.get("org.sakaiproject.scoringservice.api.ScoringService");
            ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();

            String gradebookUid = toolManager.getCurrentPlacement().getContext();
            boolean scoringAgentEnabled = scoringAgent != null && scoringAgent.isEnabled(gradebookUid, null);
            Set<AssignmentSubmissionSubmitter> submitters = submission.getSubmitters();
            String currentUser = sessionManager.getCurrentSessionUserId();
            AssignmentSubmissionSubmitter submitter = submitters.stream()
                    .filter(s -> s.getSubmitter().equals(currentUser)).findAny() // If in student context, get student's specific submission
                    .orElseGet(() -> submitters.stream().findAny() // If in instructor context, just get the first available submission (group assignments don't show the icon anyway)
                            .orElse(null));

            if (scoringAgentEnabled && submitter != null) {
                String gbItemName;
                String studentId = submitter.getSubmitter();
                String aRef = AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference();
                if (aRef.equals(associatedGbItem)) {
                    // this gb item is controlled by this tool
                    gbItemName = assignment.getTitle();
                } else {
                    // this assignment was associated with an existing gb item
                    gbItemName = associatedGbItem;
                }

                org.sakaiproject.grading.api.Assignment gbItem = null;
                try {
                    gbItem = gradingService.getAssignment(gradebookUid, gradebookUid, gbItemName);
                } catch (SecurityException se) {
                    // the gradebook method above is overzealous about security when retrieving the gb item by name. It doesn't
                    // allow student-role users to access the assignment via this method. So we
                    // have to retrieve all viewable gb items and filter to get the one we want, unfortunately, if we hit an exception.
                    // If gb item isn't released in the gb, scoring agent info will not be available.
                    List<org.sakaiproject.grading.api.Assignment> viewableGbItems = gradingService.getViewableAssignmentsForCurrentUser(gradebookUid, gradebookUid, SortType.SORT_BY_NONE);
                    if (viewableGbItems != null && !viewableGbItems.isEmpty()) {
                        for (org.sakaiproject.grading.api.Assignment viewableGbItem : viewableGbItems) {
                            if (gbItemName.equals(viewableGbItem.getName())) {
                                gbItem = viewableGbItem;
                                break;
                            }
                        }
                    }
                }

                if (gbItem != null) {
                    String gbItemId = Long.toString(gbItem.getId());

                    // Determine if a scoring component (like a rubric) has been associated with this gradebook item
                    ScoringComponent component = scoringService.getScoringComponent(
                            scoringAgent.getAgentId(), gradebookUid, gbItemId);
                    boolean scoringComponentEnabled = component != null;

                    context.put("scoringComponentEnabled", scoringComponentEnabled);

                    if (scoringComponentEnabled) {
                        context.put("scoringAgentImage", scoringAgent.getImageReference());
                        context.put("scoringAgentName", scoringAgent.getName());

                        // retrieve the appropriate url
                        if (gradeView) {
                            context.put("scoreUrl", scoringAgent.getScoreLaunchUrl(gradebookUid, gbItemId, studentId));
                            context.put("refreshScoreUrl", scoringService.getDefaultScoringAgent().getScoreUrl(gradebookUid, gbItemId, studentId) + "&t=gb");
                            context.put("scoreText", rb.getFormattedMessage("scoringAgent.grade", scoringAgent.getName()));
                        } else {
                            // only retrieve the graded rubric if grade has been released. otherwise, keep it generic
                            String scoreStudent = null;
                            if (submission.getGradeReleased()) {
                                scoreStudent = studentId;
                            }
                            context.put("scoreUrl", scoringAgent.getViewScoreLaunchUrl(gradebookUid, gbItemId, scoreStudent));
                            context.put("scoreText", rb.getFormattedMessage("scoringAgent.view", scoringAgent.getName()));
                        }
                    }
                }
            }
        }
    }

    private void addAdditionalNotesToContext(Object o, Context context, SessionState state) {

        try {
            Site st = siteService.getSite((String) state.getAttribute(STATE_CONTEXT_STRING));
            boolean notesEnabled = candidateDetailProvider != null && candidateDetailProvider.isAdditionalNotesEnabled(st);
            context.put("isAdditionalNotesEnabled", notesEnabled);
            Map<String, List<String>> notesMap = new HashMap<>();
            if (notesEnabled) {
                if (o instanceof List) {
                    List l = (List) o;
                    for (Object obj : l) {
                        User u = null;
                        if (obj instanceof SubmitterSubmission) {
                            u = ((SubmitterSubmission) obj).getUser();
                        }
                        if (u != null) {
                            if (notesMap.get(u.getId()) == null) {
                                List<String> notes = candidateDetailProvider.getAdditionalNotes(u, st).orElse(new ArrayList<>());
                                notesMap.put(u.getId(), notes);
                            }
                        }
                    }
                } else if (o instanceof User) {
                    User u = (User) o;
                    notesMap.put(u.getId(), candidateDetailProvider.getAdditionalNotes(u, st).orElse(new ArrayList<>()));
                }
            }
            context.put("notesMap", notesMap);
        } catch (IdUnusedException iue) {
            log.warn(this + ":addAdditionalNotesToContext: Site not found!" + iue.getMessage());
            context.put("isAdditionalNotesEnabled", false);
        }
    }

    public String getTypeOfGradeString(Assignment.GradeType type) {
        switch (type) {
            case UNGRADED_GRADE_TYPE:
                return rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_NOGRADE_PROP);
            case LETTER_GRADE_TYPE:
                return rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_LETTER_PROP);
            case SCORE_GRADE_TYPE:
                return rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_POINTS_PROP);
            case PASS_FAIL_GRADE_TYPE:
                return rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_PASS_FAIL_PROP);
            case CHECK_GRADE_TYPE:
                return rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_CHECK_PROP);
            default:
                return rb.getString(AssignmentConstants.ASSN_GRADE_TYPE_UNKNOWN_PROP);
        }
    }

    void sortGroupList(List<Group> groups, SessionState state) {
        String sort = (String) state.getAttribute(SORTED_BY);
        String asc = (String) state.getAttribute(SORTED_ASC);
        if (sort == null || (!sort.equals(SORTED_BY_GROUP_TITLE) && !sort.equals(SORTED_BY_GROUP_DESCRIPTION))) {
            sort = SORTED_BY_GROUP_TITLE;
            asc = Boolean.TRUE.toString();
            state.setAttribute(SORTED_BY, sort);
            state.setAttribute(SORTED_ASC, asc);
        }
        AssignmentComparator comp = new AssignmentComparator(state, sort, asc);
        Collections.sort(groups, comp);
    }

    /**
     * This is a type of backing bean for the ui it is used by sizeResources
     */
    @Data
    public class SubmitterSubmission {
        User user;
        Group group;
        String reference;
        User submittedBy;
        Boolean multiGroup = false;
        AssignmentSubmission submission;
        Boolean hasVisibleAttachments = false;

        SubmitterSubmission(User user, AssignmentSubmission submission) {
            this(user, null, submission);
        }

        SubmitterSubmission(Group group, AssignmentSubmission submission) {
            this(null, group, submission);
        }

        private SubmitterSubmission(User user, Group group, AssignmentSubmission submission) {
            this.user = user;
            this.group = group;
            this.submission = submission;
            reference = AssignmentReferenceReckoner.reckoner().submission(submission).reckon().getReference();
            hasVisibleAttachments = CollectionUtils.emptyIfNull(submission.getAttachments()).stream()
                    .map(r -> entityManager.newReference(r)).anyMatch(ref -> isVisibleAttachment(ref));
        }

        public String getGradeForUser(String id) {
            return assignmentService.getGradeForSubmitter(submission, id);
        }

        public String getTimeSpent() {
            return assignmentService.getTimeSpent(submission);
        }
    }

    /**
     * the AssignmentComparator clas
     */
    private class AssignmentComparator implements Comparator {
        Collator collator = null;
        Map<String, Integer> crSubmissionScoreMap = new HashMap<>();

        /**
         * the SessionState object
         */
        SessionState m_state = null;

        /**
         * the criteria
         */
        String m_criteria = null;

        /**
         * the criteria
         */
        String m_asc = null;

        /**
         * the user
         */
        User m_user = null;

        // true if users should be compared by anonymous submitter id rather than other identifiers
        boolean m_anon = false;

        /**
         * constructor
         *
         * @param state    The state object
         * @param criteria The sort criteria string
         * @param asc      The sort order string. TRUE_STRING if ascending; "false" otherwise.
         */
        public AssignmentComparator(SessionState state, String criteria, String asc) {
            this(state, criteria, asc, null);

        } // constructor

        /**
         * constructor
         *
         * @param state    The state object
         * @param criteria The sort criteria string
         * @param asc      The sort order string. TRUE_STRING if ascending; "false" otherwise.
         * @param user     The user object
         */
        public AssignmentComparator(SessionState state, String criteria, String asc, User user) {
            m_state = state;
            m_criteria = criteria;
            m_asc = asc;
            m_user = user;
            try {
                collator = new RuleBasedCollator(((RuleBasedCollator) Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
            } catch (ParseException e) {
                // error with init RuleBasedCollator with rules
                // use the default Collator
                collator = Collator.getInstance();
                log.warn(this + " AssignmentComparator cannot init RuleBasedCollator. Will use the default Collator instead. " + e);
            }
        } // constructor

        /**
         * caculate the range string for an assignment
         */
        private String getAssignmentRange(Assignment a) {
            String rv = "";
            if (a.getTypeOfAccess().equals(Assignment.Access.SITE)) {
                // site assignment
                rv = rb.getString("range.allgroups");
            } else {
                try {
                    Site site = siteService.getSite(a.getContext());
                    for (String s : a.getGroups()) {
                        // announcement by group
                        Group group = site.getGroup(s);
                        if (group != null)
                            rv = rv.concat(group.getTitle());
                    }
                } catch (IdUnusedException iue) {
                    log.warn("Could not get site: {}, {}", a.getContext(), iue.getMessage());
                }
            }

            return rv;

        } // getAssignmentRange

        public void setAnon(boolean value) {
            m_anon = value;
        }

        /**
         * implementing the compare function
         *
         * @param o1 The first object
         * @param o2 The second object
         * @return The compare result. 1 is o1 < o2; -1 otherwise
         */
        public int compare(Object o1, Object o2) {
            int result = -1;

            if (m_criteria == null) {
                m_criteria = SORTED_BY_DEFAULT;
            }

            /** *********** for sorting assignments ****************** */
            if (m_criteria.equals(SORTED_BY_DEFAULT)) {
                int s1 = ((Assignment) o1).getPosition();
                int s2 = ((Assignment) o2).getPosition();

                if (s1 == s2) // we either have 2 assignments with no existing postion_order or a numbering error, so sort by duedate
                {
                    // sorted by the assignment due date
                    Instant t1 = ((Assignment) o1).getDueDate();
                    Instant t2 = ((Assignment) o2).getDueDate();

                    if (t1 == null) {
                        result = -1;
                    } else if (t2 == null) {
                        result = 1;
                    } else {
                        if (t1.equals(t2)) {
                            t1 = ((Assignment) o1).getDateCreated();
                            t2 = ((Assignment) o2).getDateCreated();
                        }
                        if (t1.isBefore(t2)) {
                            result = 1;
                        } else {
                            result = -1;
                        }
                    }
                } else if (s1 == 0 && s2 > 0) // order has not been set on this object, so put it at the bottom of the list
                {
                    result = 1;
                } else if (s2 == 0 && s1 > 0) // making sure assignments with no position_order stay at the bottom
                {
                    result = -1;
                } else // 2 legitimate postion orders
                {
                    result = (s1 < s2) ? -1 : 1;
                }
            }
            if (m_criteria.equals(SORTED_BY_TITLE)) {
                // sorted by the assignment title
                String s1 = ((Assignment) o1).getTitle();
                String s2 = ((Assignment) o2).getTitle();
                result = compareString(s1, s2);
            } else if (m_criteria.equals(SORTED_BY_SECTION)) {
                // sorted by the assignment section
                String s1 = ((Assignment) o1).getSection();
                String s2 = ((Assignment) o2).getSection();
                result = compareString(s1, s2);
            } else if (m_criteria.equals(SORTED_BY_DUEDATE)) {
                // sorted by the assignment due date
                Instant t1 = ((Assignment) o1).getDueDate();
                Instant t2 = ((Assignment) o2).getDueDate();
                result = compareInstant(t1, t2);
            } else if (m_criteria.equals(SORTED_BY_SOFT_REMOVED_DATE)) {
                // sorted by the assignment due date
                Instant t1 = ((Assignment) o1).getSoftRemovedDate();
                Instant t2 = ((Assignment) o2).getSoftRemovedDate();
                result = compareInstant(t1, t2);
            } else if (m_criteria.equals(SORTED_BY_MODIFIEDDATE)) {
                Instant t1 = ((Assignment) o1).getDateModified();
                Instant t2 = ((Assignment) o2).getDateModified();
                result = compareInstant(t1, t2);
            } else if (m_criteria.equals(SORTED_BY_MODIFIEDUSER)) {
                try {
                    User u1 = userDirectoryService.getUser(((Assignment) o1).getModifier());
                    User u2 = userDirectoryService.getUser(((Assignment) o2).getModifier());
                    result = new UserSortNameComparator().compare(u1, u2);
                } catch (UserNotDefinedException e) {
                    log.error("Could not get user {} or {}: {}", ((Assignment) o1).getModifier(), ((Assignment) o2).getModifier(), e.getMessage());
                }
            } else if (m_criteria.equals(SORTED_BY_OPENDATE)) {
                // sorted by the assignment open
                Instant t1 = ((Assignment) o1).getOpenDate();
                Instant t2 = ((Assignment) o2).getOpenDate();
                result = compareInstant(t1, t2);
            } else if (m_criteria.equals(SORTED_BY_ASSIGNMENT_STATUS)) {

                if (assignmentService.allowAddAssignment(((Assignment) o1).getContext())) {
                    // comparing assignment status
                    String s1 = assignmentService.getAssignmentStatus(((Assignment) o1).getId());
                    String s2 = assignmentService.getAssignmentStatus(((Assignment) o2).getId());
                    result = compareString(s1, s2);
                } else {
                    // comparing submission status
                    AssignmentSubmission as1 = findAssignmentSubmission((Assignment) o1);
                    AssignmentSubmission as2 = findAssignmentSubmission((Assignment) o2);
                    String s1 = assignmentService.getSubmissionStatus(as1.getId(), false);
                    String s2 = assignmentService.getSubmissionStatus(as2.getId(), false);
                    result = as1 == null ? 1 : as2 == null ? -1 : compareString(s1, s2);
                }
            } else if (m_criteria.equals(SORTED_BY_NUM_SUBMISSIONS)) {
                // sort by numbers of submissions
                Assignment assignment1 = (Assignment) o1;
                String assignment1reference = AssignmentReferenceReckoner.reckoner().assignment(assignment1).reckon().getReference();
                int subNum1 = assignment1.getDraft() ? -1 : assignmentService.countSubmissions(assignment1reference, null);

                Assignment assignment2 = (Assignment) o2;
                String assignment2reference = AssignmentReferenceReckoner.reckoner().assignment(assignment2).reckon().getReference();
                int subNum2 = assignment2.getDraft() ? -1 : assignmentService.countSubmissions(assignment2reference, null);

                result = (subNum1 > subNum2) ? 1 : -1;

            } else if (m_criteria.equals(SORTED_BY_NUM_UNGRADED)) {
                // sort by numbers of ungraded submissions

                // initialize
                int ungraded1 = 0;
                int ungraded2 = 0;
                Instant t1, t2;

                Iterator submissions1 = assignmentService.getSubmissions((Assignment) o1).iterator();
                while (submissions1.hasNext()) {
                    AssignmentSubmission submission1 = (AssignmentSubmission) submissions1.next();
                    t1 = submission1.getDateSubmitted();

                    if (t1 != null && !submission1.getGraded()) ungraded1++;
                }

                Iterator submissions2 = assignmentService.getSubmissions((Assignment) o2).iterator();
                while (submissions2.hasNext()) {
                    AssignmentSubmission submission2 = (AssignmentSubmission) submissions2.next();
                    t2 = submission2.getDateSubmitted();

                    if (t2 != null && !submission2.getGraded()) ungraded2++;
                }

                result = (ungraded1 > ungraded2) ? 1 : -1;

            } else if (m_criteria.equals(SORTED_BY_GRADE) || m_criteria.equals(SORTED_BY_SUBMISSION_STATUS)) {
                AssignmentSubmission submission1 = getSubmission(((Assignment) o1).getId(), m_user, "compare", null);
                String grade1 = " ";
                if (submission1 != null && submission1.getGraded() && submission1.getGradeReleased()) {
                    grade1 = submission1.getGrade();
                }

                AssignmentSubmission submission2 = getSubmission(((Assignment) o2).getId(), m_user, "compare", null);
                String grade2 = " ";
                if (submission2 != null && submission2.getGraded() && submission2.getGradeReleased()) {
                    grade2 = submission2.getGrade();
                }

                result = compareString(grade1, grade2);
            } else if (m_criteria.equals(SORTED_BY_MAX_GRADE)) {
                String maxGrade1 = maxGrade(((Assignment) o1).getTypeOfGrade(), (Assignment) o1);
                String maxGrade2 = maxGrade(((Assignment) o2).getTypeOfGrade(), (Assignment) o2);

                try {
                    // do integer comparation inside point grade type
                    int max1 = Integer.parseInt(maxGrade1);
                    int max2 = Integer.parseInt(maxGrade2);
                    result = (max1 < max2) ? -1 : 1;
                } catch (NumberFormatException e) {
                    // otherwise do an alpha-compare
                    result = compareString(maxGrade1, maxGrade2);
                }
            }
            // group related sorting
            else if (m_criteria.equals(SORTED_BY_FOR)) {
                // sorted by the public view attribute
                String factor1 = getAssignmentRange((Assignment) o1);
                String factor2 = getAssignmentRange((Assignment) o2);
                result = compareString(factor1, factor2);
            } else if (m_criteria.equals(SORTED_BY_GROUP_TITLE)) {
                // sorted by the group title
                String factor1 = ((Group) o1).getTitle();
                String factor2 = ((Group) o2).getTitle();
                result = new AlphaNumericComparator().compare(factor1, factor2);
            } else if (m_criteria.equals(SORTED_BY_GROUP_DESCRIPTION)) {
                // sorted by the group description
                String factor1 = ((Group) o1).getDescription();
                String factor2 = ((Group) o2).getDescription();
                if (factor1 == null) {
                    factor1 = "";
                }
                if (factor2 == null) {
                    factor2 = "";
                }
                result = compareString(factor1, factor2);
            }
            /** ***************** for sorting submissions in instructor grade assignment view ************* */
            else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_CONTENTREVIEW)) {
                SubmitterSubmission u1 = (SubmitterSubmission) o1;
                SubmitterSubmission u2 = (SubmitterSubmission) o2;
                if (u1 == null || u2 == null) {
                    result = 1;
                } else {
                    AssignmentSubmission s1 = u1.getSubmission();
                    AssignmentSubmission s2 = u2.getSubmission();

                    if (s1 == null) {
                        result = -1;
                    } else if (s2 == null) {
                        result = 1;
                    } else {
                        // Avoid expensive calls below if possible
                        Integer score1 = crSubmissionScoreMap.get(s1.getId());
                        Integer score2 = crSubmissionScoreMap.get(s2.getId());

                        if (score1 == null) {
                          score1 = getContentReviewResultScore(assignmentService.getContentReviewResults(s1));
                          crSubmissionScoreMap.put(s1.getId(), score1);
                        }
                        if (score2 == null) {
                          score2 = getContentReviewResultScore(assignmentService.getContentReviewResults(s2));
                          crSubmissionScoreMap.put(s2.getId(), score2);
                        }

                        if (score1 == null && score2 == null) {
                            result = 0;
                        } else if (score1 == null) {
                            result = -1;
                        } else if (score2 == null) {
                            result = 1;
                        } else {
                            result = score1 == score2 ? 0 : (score1 > score2 ? 1 : -1);
                        }
                    }
                }

            } else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_LASTNAME)) {
                // sorted by the submitters sort name
                SubmitterSubmission u1 = (SubmitterSubmission) o1;
                SubmitterSubmission u2 = (SubmitterSubmission) o2;

                if (u1 == null || u2 == null || (u1.getUser() == null && u1.getGroup() == null) || (u2.getUser() == null && u2.getGroup() == null)) {
                    result = 1;
                } else if (m_anon) {
                    String anon1 = u1.getSubmission().getId();
                    String anon2 = u2.getSubmission().getId();
                    result = compareString(anon1, anon2);
                } else if (u1.getUser() != null && u2.getUser() != null) {
                    result = new UserSortNameComparator().compare(u1.getUser(), u2.getUser());
                } else {
                    String lName1 = u1.getUser() == null ? u1.getGroup().getTitle() : u1.getUser().getSortName();
                    String lName2 = u2.getUser() == null ? u2.getGroup().getTitle() : u2.getUser().getSortName();
                    result = compareString(lName1, lName2);
                }
            } else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_SUBMIT_TIME)) {
                // sorted by submission time
                SubmitterSubmission u1 = (SubmitterSubmission) o1;
                SubmitterSubmission u2 = (SubmitterSubmission) o2;

                if (u1 == null && u2 == null) result = 0;
                else if (u1 == null) result = -1;
                else if (u2 == null) result = 1;

                AssignmentSubmission s1 = u1.getSubmission();
                AssignmentSubmission s2 = u2.getSubmission();
                Instant t1 = (s1 == null ? null : s1.getDateSubmitted());
                Instant t2 = (s2 == null ? null : s2.getDateSubmitted());

                if (t1 == null && t2 == null) result = 0;
                else if (t1 == null) result = -1;
                else if (t2 == null) result = 1;
                else result = t1.compareTo(t2);
            } else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_STATUS)) {
                // sort by submission status
                SubmitterSubmission u1 = (SubmitterSubmission) o1;
                SubmitterSubmission u2 = (SubmitterSubmission) o2;

                String status1 = "";
                String status2 = "";

                if (u1 == null) {
                    status1 = rb.getString("listsub.nosub");
                } else {
                    AssignmentSubmission s1 = u1.getSubmission();
                    if (s1 == null) {
                        status1 = rb.getString("listsub.nosub");
                    } else {
                        status1 = assignmentService.getSubmissionStatus(s1.getId(), false);
                    }
                }

                if (u2 == null) {
                    status2 = rb.getString("listsub.nosub");
                } else {
                    AssignmentSubmission s2 = u2.getSubmission();
                    if (s2 == null) {
                        status2 = rb.getString("listsub.nosub");
                    } else {
                        status2 = assignmentService.getSubmissionStatus(s2.getId(), false);
                    }
                }

                result = compareString(status1, status2);
            } else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_GRADE)) {
                // sort by submission status
                SubmitterSubmission u1 = (SubmitterSubmission) o1;
                SubmitterSubmission u2 = (SubmitterSubmission) o2;

                if (u1 == null || u2 == null) {
                    result = -1;
                } else {
                    AssignmentSubmission s1 = u1.getSubmission();
                    AssignmentSubmission s2 = u2.getSubmission();

                    //sort by submission grade
                    if (s1 == null) {
                        result = -1;
                    } else if (s2 == null) {
                        result = 1;
                    } else {
                        String grade1 = s1.getGrade();
                        String grade2 = s2.getGrade();
                        if (grade1 == null) {
                            grade1 = "";
                        }
                        if (grade2 == null) {
                            grade2 = "";
                        }

                        // if scale is points
                        if ((s1.getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE)
                                && ((s2.getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE))) {
                            if ("".equals(grade1)) {
                                result = -1;
                            } else if ("".equals(grade2)) {
                                result = 1;
                            } else {
                                result = compareDouble(grade1, grade2);
                            }
                        } else {
                            result = compareString(grade1, grade2);
                        }
                    }
                }
            } else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_RELEASED)) {
                // sort by submission status
                SubmitterSubmission u1 = (SubmitterSubmission) o1;
                SubmitterSubmission u2 = (SubmitterSubmission) o2;

                if (u1 == null || u2 == null) {
                    result = -1;
                } else {
                    AssignmentSubmission s1 = u1.getSubmission();
                    AssignmentSubmission s2 = u2.getSubmission();

                    if (s1 == null) {
                        result = -1;
                    } else if (s2 == null) {
                        result = 1;
                    } else {
                        // sort by submission released
                        String released1 = (s1.getGradeReleased()).toString();
                        String released2 = (s2.getGradeReleased()).toString();

                        result = compareString(released1, released2);
                    }
                }
            }
            /****** for other sort on submissions **/
            else if (m_criteria.equals(SORTED_SUBMISSION_BY_LASTNAME)) {
                // sorted by the submitters sort name
                AssignmentSubmission a1 = (AssignmentSubmission) o1;
                AssignmentSubmission a2 = (AssignmentSubmission) o2;
                String s1 = "";
                String s2 = "";

                if (a1.getAssignment().getIsGroup()) {
                    try {
                        Site site = siteService.getSite(a1.getAssignment().getContext());
                        s1 = site.getGroup(a1.getGroupId()).getTitle();
                    } catch (Throwable _dfef) {
                    }
                } else {
                    try {
                        s1 = userDirectoryService.getUser(a1.getSubmitters().toArray(new AssignmentSubmissionSubmitter[0])[0].getSubmitter()).getSortName();
                    } catch (UserNotDefinedException e) {
                        log.warn("Cannot find user id while sorting by last name for submission: {}, {}", a1.getId(), e.getMessage());
                    }
                }
                if (a2.getAssignment().getIsGroup()) {
                    try {
                        Site site = siteService.getSite(a2.getAssignment().getContext());
                        s2 = site.getGroup(a2.getGroupId()).getTitle();
                    } catch (Throwable _dfef) { // TODO empty exception block
                    }
                } else {
                    try {
                        s2 = userDirectoryService.getUser(a2.getSubmitters().toArray(new AssignmentSubmissionSubmitter[0])[0].getSubmitter()).getSortName();
                    } catch (UserNotDefinedException e) {
                        log.warn("Cannot find user id while sorting by last name for submission: {}, {}", a2.getId(), e.getMessage());
                    }
                }

                result = s1.compareTo(s2); //compareString(submitters1, submitters2);
            } else if (m_criteria.equals(SORTED_SUBMISSION_BY_SUBMIT_TIME)) {
                // sorted by submission time
                Instant t1 = ((AssignmentSubmission) o1).getDateSubmitted();
                Instant t2 = ((AssignmentSubmission) o2).getDateSubmitted();

                if (t1 == null) {
                    result = -1;
                } else if (t2 == null) {
                    result = 1;
                } else if (t1.isBefore(t2)) {
                    result = -1;
                } else {
                    result = 1;
                }
            } else if (m_criteria.equals(SORTED_SUBMISSION_BY_STATUS)) {
                // sort by submission status
                String s1 = assignmentService.getSubmissionStatus(((AssignmentSubmission) o1).getId(), false);
                String s2 = assignmentService.getSubmissionStatus(((AssignmentSubmission) o2).getId(), false);
                result = compareString(s1, s2);
            } else if (m_criteria.equals(SORTED_SUBMISSION_BY_GRADE)) {
                // sort by submission grade
                String grade1 = ((AssignmentSubmission) o1).getGrade();
                String grade2 = ((AssignmentSubmission) o2).getGrade();
                if (grade1 == null) {
                    grade1 = "";
                }
                if (grade2 == null) {
                    grade2 = "";
                }

                // if scale is points
                if ((((AssignmentSubmission) o1).getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE)
                        && ((((AssignmentSubmission) o2).getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE))) {
                    if ("".equals(grade1)) {
                        result = -1;
                    } else if ("".equals(grade2)) {
                        result = 1;
                    } else {
                        result = compareDouble(grade1, grade2);
                    }
                } else {
                    result = compareString(grade1, grade2);
                }
            } else if (m_criteria.equals(SORTED_SUBMISSION_BY_GRADE)) {
                // sort by submission grade
                String grade1 = ((AssignmentSubmission) o1).getGrade();
                String grade2 = ((AssignmentSubmission) o2).getGrade();
                if (grade1 == null) {
                    grade1 = "";
                }
                if (grade2 == null) {
                    grade2 = "";
                }

                // if scale is points
                if ((((AssignmentSubmission) o1).getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE)
                        && ((((AssignmentSubmission) o2).getAssignment().getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE))) {
                    if ("".equals(grade1)) {
                        result = -1;
                    } else if ("".equals(grade2)) {
                        result = 1;
                    } else {
                        result = compareDouble(grade1, grade2);
                    }
                } else {
                    result = compareString(grade1, grade2);
                }
            } else if (m_criteria.equals(SORTED_SUBMISSION_BY_MAX_GRADE)) {
                Assignment a1 = ((AssignmentSubmission) o1).getAssignment();
                Assignment a2 = ((AssignmentSubmission) o2).getAssignment();
                String maxGrade1 = maxGrade(a1.getTypeOfGrade(), a1);
                String maxGrade2 = maxGrade(a2.getTypeOfGrade(), a2);

                try {
                    // do integer comparation inside point grade type
                    int max1 = Integer.parseInt(maxGrade1);
                    int max2 = Integer.parseInt(maxGrade2);
                    result = (max1 < max2) ? -1 : 1;
                } catch (NumberFormatException e) {
                    log.warn(this + ":AssignmentComparator compare" + e.getMessage());
                    // otherwise do an alpha-compare
                    result = maxGrade1.compareTo(maxGrade2);
                }
            } else if (m_criteria.equals(SORTED_SUBMISSION_BY_RELEASED)) {
                // sort by submission released
                String released1 = (Boolean.valueOf(((AssignmentSubmission) o1).getGradeReleased())).toString();
                String released2 = (Boolean.valueOf(((AssignmentSubmission) o2).getGradeReleased())).toString();

                result = compareString(released1, released2);
            } else if (m_criteria.equals(SORTED_SUBMISSION_BY_ASSIGNMENT)) {
                // sort by submission's assignment
                String title1 = ((AssignmentSubmission) o1).getAssignment().getTitle();
                String title2 = ((AssignmentSubmission) o2).getAssignment().getTitle();

                result = compareString(title1, title2);
            }
            /*************** sort user by sort name ***************/
            else if (m_criteria.equals(SORTED_USER_BY_SORTNAME)) {
                // sort by user's sort name
                String name1 = ((User) o1).getSortName();
                String name2 = ((User) o2).getSortName();

                result = compareString(name1, name2);

            } else if (m_criteria.equals(SORTED_BY_ESTIMATE)) {
                // sorted by the assignment estimate
                String t1 = ((Assignment) o1).getEstimate();
                String t2 = ((Assignment) o2).getEstimate();
                result = compareEstimate(t1, t2);

            }else if (m_criteria.equals(SORTED_GRADE_SUBMISSION_BY_ESTIMATE)) {
                // sorted by the submission time spent
                SubmitterSubmission u1 = (SubmitterSubmission) o1;
                SubmitterSubmission u2 = (SubmitterSubmission) o2;
                String t1 = u1.getTimeSpent();
                String t2 = u2.getTimeSpent();
                result = compareEstimate(t1, t2);
            }

            // sort ascending or descending
            if (!Boolean.valueOf(m_asc)) {
                result = -result;
            }
            return result;
        }

        private int compareEstimate(String t1, String t2) {
            int result;
            if (StringUtils.isBlank(t1)  && StringUtils.isBlank(t2)) {
                result = 0;
            } else if (StringUtils.isBlank(t2)) {
                result = 1;
            } else if (StringUtils.isBlank(t1)) {
                result = -1;
            } else {
                int i1, i2;
                i1 = assignmentService.timeToInt(t1);
                i2 = assignmentService.timeToInt(t2);
                result = (i1 < i2) ? -1 : 1;
            }
            return result;
        }

        private int getContentReviewResultScore(List<ContentReviewResult> resultList) {
            if (CollectionUtils.isEmpty(resultList)) {
                return -1;
            }

            // Find the highest score in all of the possible submissions
            int score = -99;

            for (ContentReviewResult crr : resultList) {
                if (score <= -2 && crr.isPending()) {
                    score = -2;
                } else if (score <= -1 && StringUtils.equals(crr.getReviewReport(), "Error")) {
                    // Yes, "Error" appears to be magic throughout the review code
                    // Error should appear before pending
                    score = -1;
                } else if (crr.getReviewScore() > score) {
                    score = crr.getReviewScore();
                }
            }

            return score;
        }

        /**
         * returns AssignmentSubmission object for given assignment by current user
         *
         * @param a
         * @return
         */
        protected AssignmentSubmission findAssignmentSubmission(Assignment a) {
            User user = userDirectoryService.getCurrentUser();
            try {
                return assignmentService.getSubmission(a.getId(), user);
            } catch (PermissionException e) {
                log.warn("Could not access submission for user: {}, {}", user.getId(), e.getMessage());
            }
            return null;
        }

        /**
         * Compare two strings as double values. Deal with the case when either of the strings cannot be parsed as double value.
         *
         * @param grade1
         * @param grade2
         * @return
         */
        private int compareDouble(String grade1, String grade2) {
            int result;
            try {
                result = Double.valueOf(grade1) > Double.valueOf(grade2) ? 1 : -1;
            } catch (Exception formatException) {
                // in case either grade1 or grade2 cannot be parsed as Double
                result = compareString(grade1, grade2);
                log.warn(this + ":AssignmentComparator compareDouble " + formatException.getMessage());
            }
            return result;
        } // compareDouble

        private int compareString(String s1, String s2) {
            int result;
            if (s1 == null && s2 == null) {
                result = 0;
            } else if (s2 == null) {
                result = 1;
            } else if (s1 == null) {
                result = -1;
            } else {
                result = collator.compare(s1.toLowerCase(), s2.toLowerCase());
            }
            return result;
        }

        private int compareInstant(Instant t1, Instant t2) {
            if (t1 == null) {
                return -1;
            } else if (t2 == null) {
                return 1;
            } else if (t1.isBefore(t2)) {
                return -1;
            } else {
                return 1;
            }
        }

        /**
         * get assignment maximum grade available based on the assignment grade type
         *
         * @param gradeType The int value of grade type
         * @param a         The assignment object
         * @return The max grade String
         */
        private String maxGrade(Assignment.GradeType gradeType, Assignment a) {
            String maxGrade = "";

            if (gradeType == Assignment.GradeType.GRADE_TYPE_NONE) {
                // Grade type not set
                maxGrade = rb.getString("granotset");
            } else if (gradeType == Assignment.GradeType.UNGRADED_GRADE_TYPE) {
                // Ungraded grade type
                maxGrade = rb.getString("gen.nograd");
            } else if (gradeType == Assignment.GradeType.LETTER_GRADE_TYPE) {
                // Letter grade type
                maxGrade = "A";
            } else if (gradeType == Assignment.GradeType.SCORE_GRADE_TYPE) {
                // Score based grade type
                maxGrade = Integer.toString(a.getMaxGradePoint());
            } else if (gradeType == Assignment.GradeType.PASS_FAIL_GRADE_TYPE) {
                // Pass/fail grade type
                maxGrade = rb.getString("pass");
            } else if (gradeType == Assignment.GradeType.CHECK_GRADE_TYPE) {
                // Grade type that only requires a check
                maxGrade = rb.getString("check");
            }

            return maxGrade;

        } // maxGrade

    } // DiscussionComparator

    /**
     * the UploadGradeWrapper class to be used for the "upload all" feature
     */
    public class UploadGradeWrapper {
        /**
         * the grade
         */
        String m_grade = null;

        /**
         * the text
         */
        String m_text = null;

        /**
         * the submission attachment list
         */
        List<Reference> m_submissionAttachments = entityManager.newReferenceList();

        /**
         * the comment
         */
        String m_comment = "";

        /**
         * the timestamp
         */
        String m_timeStamp = "";

        /**
         * the feedback text
         */
        String m_feedbackText = "";

        /**
         * the feedback attachment list
         */
        List<Reference> m_feedbackAttachments = entityManager.newReferenceList();

        public UploadGradeWrapper(String grade, String text, String comment, List<Reference> submissionAttachments, List<Reference> feedbackAttachments, String timeStamp, String feedbackText) {
            m_grade = grade;
            m_text = text;
            m_comment = comment;
            m_submissionAttachments = submissionAttachments;
            m_feedbackAttachments = feedbackAttachments;
            m_feedbackText = feedbackText;
            m_timeStamp = timeStamp;
        }

        /**
         * Returns grade string
         */
        public String getGrade() {
            return m_grade;
        }

        /**
         * set the grade string
         */
        public void setGrade(String grade) {
            m_grade = grade;
        }

        /**
         * Returns the text
         */
        public String getText() {
            return m_text;
        }

        /**
         * set the text
         */
        public void setText(String text) {
            m_text = text;
        }

        /**
         * Returns the comment string
         */
        public String getComment() {
            return m_comment;
        }

        /**
         * set the comment string
         */
        public void setComment(String comment) {
            m_comment = comment;
        }

        /**
         * Returns the submission attachment list
         */
        public List<Reference> getSubmissionAttachments() {
            return m_submissionAttachments;
        }

        /**
         * set the submission attachment list
         */
        public void setSubmissionAttachments(List<Reference> attachments) {
            m_submissionAttachments = attachments;
        }

        /**
         * Returns the feedback attachment list
         */
        public List<Reference> getFeedbackAttachments() {
            return m_feedbackAttachments;
        }

        /**
         * set the attachment list
         */
        public void setFeedbackAttachments(List<Reference> attachments) {
            m_feedbackAttachments = attachments;
        }

        /**
         * submission timestamp
         *
         * @return
         */
        public String getSubmissionTimeStamp() {
            return m_timeStamp;
        }

        /**
         * feedback text/incline comment
         *
         * @return
         */
        public String getFeedbackText() {
            return m_feedbackText;
        }

        /**
         * set the feedback text
         */
        public void setFeedbackText(String feedbackText) {
            m_feedbackText = feedbackText;
        }

        /**
         * set the submission timestamp
         */
        public void setSubmissionTimestamp(String timeStamp) {
            m_timeStamp = timeStamp;
        }
    }

    /**
     * Used when students are uploading a video object
     */
    private List<Reference> getVideoAttachments(RunData data, SessionState state, Assignment assignment) {
        List<Reference> attachments = entityManager.newReferenceList();
        //Get the video object in base64 from the form post
        String videoResponseBase64 = data.getParameters().get(VIDEO_SUBMISSION_PARAM);
        if (StringUtils.isBlank(videoResponseBase64) || videoResponseBase64.split(",").length < 2) {
            return attachments;
        }
        videoResponseBase64 = videoResponseBase64.split(",")[1];
        // Use the sessionId for the video reference.
        String resourceId = sessionManager.getCurrentSession().getId();
        String contentType = data.getParameters().get(VIDEO_SUBMISSION_MIMETYPE_PARAM);
        // The blob is encoded Base64 in the client side and sent via POST.
        InputStream contentStream = new ByteArrayInputStream(Base64.getDecoder().decode(videoResponseBase64.getBytes(StandardCharsets.UTF_8)));
        ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
        props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, resourceId);
        props.addProperty(ResourceProperties.PROP_DESCRIPTION, resourceId);
        SecurityAdvisor sa = createSubmissionSecurityAdvisor();
        try {
            securityService.pushAdvisor(sa);
            String siteId = toolManager.getCurrentPlacement().getContext();
            ContentResource attachment = contentHostingService.addAttachmentResource(resourceId, siteId, "Assignments", contentType, contentStream, props);
            try {
                Reference ref = entityManager.newReference(contentHostingService.getReference(attachment.getId()));
                attachments.add(ref);
                state.setAttribute(ATTACHMENTS, attachments);
            } catch (Exception e) {
                log.warn("Cannot find reference for {}: {}", attachment.getId(), e.getMessage());
            }
        } catch (PermissionException e) {
            addAlert(state, rb.getString("notpermis4"));
        } catch (RuntimeException e) {
            if (contentHostingService.ID_LENGTH_EXCEPTION.equals(e.getMessage())) {
                addAlert(state, rb.getFormattedMessage("alert.toolong", resourceId));
            } else {
                throw e;
            }
        } catch (ServerOverloadException e) {
            log.debug("***** DISK IO Exception ***** {}", e.getMessage());
            addAlert(state, rb.getString("failed.diskio"));
        } catch (Exception ignore) {
            log.debug("***** Unknown Exception ***** {}", ignore.getMessage());
            addAlert(state, rb.getString("failed"));
        } finally {
            securityService.popAdvisor(sa);
        }
        return attachments;
    }

    private String getRateTimeSpent(Set<AssignmentSubmission> as) {
        int totalTime = 0;
        int nSubmision = 0;
        int spentSubmision = 0;
        int assigmentRateSpent = 0;
        for (AssignmentSubmission submission : as) {
            for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                if(submitter.getTimeSpent()!=null) {
                    spentSubmision = assignmentService.timeToInt(submitter.getTimeSpent());
                    if(spentSubmision > 0) {
                        nSubmision++;
                        assigmentRateSpent = assigmentRateSpent + spentSubmision;
                    }
                }
            }

        }
        if(assigmentRateSpent > 0) {
            return assignmentService.intToTime(assigmentRateSpent/nSubmision);
        }
        return assignmentService.intToTime(assigmentRateSpent);
    }
    private String getRateSubmissionTimeSpent(AssignmentSubmission submission) {
        int totalTime = 0;
        int spentSubmision = 0;
        int assigmentRateSpent = 0;

        if(submission!=null) {
            for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
                if(submitter.getTimeSpent()!=null) {
                    spentSubmision = assignmentService.timeToInt(submitter.getTimeSpent());
                    if(spentSubmision > 0) {
                        assigmentRateSpent = assigmentRateSpent + spentSubmision;
                    }
                }
            }
        }
        if(assigmentRateSpent > 0) {
            return assignmentService.intToTime(assigmentRateSpent);
        }
        return assignmentService.intToTime(assigmentRateSpent);
    }
}
