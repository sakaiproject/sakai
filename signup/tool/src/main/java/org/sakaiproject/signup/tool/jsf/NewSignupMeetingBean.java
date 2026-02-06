/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at:
 *
 * http://opensource.org/licenses/ecl2.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.signup.tool.jsf;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupMeetingService;
import org.sakaiproject.signup.api.SignupMessageTypes;
import org.sakaiproject.signup.api.SignupUser;
import org.sakaiproject.signup.api.model.MeetingTypes;
import org.sakaiproject.signup.api.model.SignupAttachment;
import org.sakaiproject.signup.api.model.SignupAttendee;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupSite;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.attachment.AttachmentHandler;
import org.sakaiproject.signup.tool.jsf.organizer.UserDefineTimeslotBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateMeetings;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.DateFormatterUtil;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * This JSF UIBean class will handle the creation of different types of
 * event/meeting by Organizer It provides all the necessary business logic for
 * this process.
 *
 * @author Peter Liu
 *
 * </P>
 */
@Data
@Slf4j
public class NewSignupMeetingBean implements MeetingTypes, SignupMessageTypes, SignupBeanConstants {

    public static final boolean DEFAULT_SEND_EMAIL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.email.notification", "true"));
    public static final boolean DEFAULT_ALLOW_WAITLIST = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.allow.waitlist", "true"));
    public static final boolean DEFAULT_ALLOW_COMMENT = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.allow.comment", "true"));
    public static final boolean DEFAULT_AUTO_RIMINDER = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.event.default.auto.reminder", "true"));
    public static final boolean DEFAULT_AUTO_RMINDER_OPTION_CHOICE = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.autoRiminder.option.choice.setting", "true"));
    public static final boolean DEFAULT_USERID_INPUT_MODE_OPTION_CHOICE = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.userId.inputMode.choiceOption.setting", "true"));
    public static final boolean DEFAULT_EXPORT_TO_CALENDAR_TOOL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.export.to.calendar.setting", "true"));
    public static final boolean DEFAULT_CREATE_GROUPS = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.create.groups.setting", "true"));
    public static final String DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY = Utilities.getSignupConfigParamVal("signup.default.email.selected", SEND_EMAIL_ALL_PARTICIPANTS, VALID_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY);
    public static final boolean NEW_MEETING_SEND_EMAIL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.email.notification.mandatory.for.newMeeting", "true"));
    public static final String HIDDEN_ISO_STARTTIME = "startTimeISO8601";
    public static final String HIDDEN_ISO_ENDTIME = "endTimeISO8601";
    public static final String HIDDEN_ISO_UNTILTIME = "untilISO8601";
    public static final String PARAM_NAME_FOR_ATTENDEE_USERID = "attendeeUserId";

    private List<SelectItem> allAttendees;
    private List<SignupUser> allPossibleCoordinators;
    private List<SignupUser> allSignupUsers;
    private boolean allowComment = DEFAULT_ALLOW_COMMENT;
    private boolean allowWaitList = DEFAULT_ALLOW_WAITLIST;
    private boolean assignParicitpantsToAllRecurEvents = false;
    private AttachmentHandler attachmentHandler;
    private List<SignupAttachment> attachments;
    private boolean autoReminder = DEFAULT_AUTO_RIMINDER;
    private boolean autoReminderOptionChoice = DEFAULT_AUTO_RMINDER_OPTION_CHOICE;
    private List<SelectItem> categories = null;
    private boolean createGroups = DEFAULT_CREATE_GROUPS;
    private String creatorUserId;
    private SignupSiteWrapper currentSite;
    private HtmlInputHidden currentStepHiddenInfo;
    private String customCategory; // New Category added in the editable field
    private String customLocation; // New Location added in the editable field
    private List<TimeslotWrapper> customTimeSlotWrpList; // discontinued time slots case
    private int deadlineTime; // sign up deadline before this minutes/hours/days
    private String deadlineTimeType;
    private boolean eidInputMode = false;
    private String eidOrEmailInputByUser;
    private boolean endTimeAutoAdjusted = false;
    private String endTimeString;
    private String eventFreqType = "";
    private String iframeId = "";
    private List<SelectItem> locations = null;
    private boolean mandatorySendEmail = NEW_MEETING_SEND_EMAIL;
    private int maxAttendeesPerSlot;
    private int maxNumOfSlots; // to remember the number of the meeting slots
    private int maxOfAttendees;
    private int maxSlots;
    private List<SelectItem> meetingTypeRadioBttns;
    private UIInput newAttendeeInput;
    private int numberOfAttendees;
    private int numberOfSlots;
    private int occurrences;
    private List<SignupSiteWrapper> otherSites;
    private boolean otherSitesAvailability;
    private boolean publishToCalendar = DEFAULT_EXPORT_TO_CALENDAR_TOOL;
    private Boolean publishedSite;
    private boolean receiveEmail;
    private String recurLengthChoice; // 0 for num of repeat, 1 for date choice
    private boolean recurrence;
    private String repeatType;
    private Date repeatUntil;
    private String repeatUntilString;
    private SakaiFacade sakaiFacade;
    private String selectedCategory; // Category selected from the dropdown
    private String selectedLocation; // Location selected from the dropdown
    private boolean sendEmail = DEFAULT_SEND_EMAIL;
    private boolean sendEmailByOwner;
    private String sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;
    private boolean showParticipants;
    private int signupBegins; // sign up can start before this minutes/hours/days
    private String signupBeginsType;
    private SignupMeeting signupMeeting;
    private SignupMeetingService signupMeetingService;
    @Setter(AccessLevel.NONE) private List<SelectItem> slots; // Used for populate the drop down box for the allowed number of meeting slots
    private String startTimeString;
    private int timeSlotDuration;
    private List<TimeslotWrapper> timeSlotWrappers;
    private UIData timeslotWrapperTable;
    private String title = StringUtils.EMPTY;
    private boolean unlimited;
    private UserDefineTimeslotBean userDefineTimeslotBean;
    private boolean userDefinedTS = false;
    private boolean userIdInputModeOptionChoice = DEFAULT_USERID_INPUT_MODE_OPTION_CHOICE;
    private boolean validationError;

    public NewSignupMeetingBean() {
        init();
    }

    public List<SelectItem> getSlots() {
        slots = IntStream.range(1, numberOfSlots + 1).mapToObj(i -> new SelectItem(i, String.valueOf(i))).collect(Collectors.toList());
        return slots;
    }

    public String getCreatorUserId() {
        if (creatorUserId == null) {
            // set current user as default meeting organizer if case people forget to select one
            creatorUserId = sakaiFacade.getCurrentUserId();
        }
        return creatorUserId;
    }

    public String getCurrentUserDisplayName() {
        return sakaiFacade.getUserDisplayName(sakaiFacade.getCurrentUserId());
    }

    public boolean isOtherSitesAvailability() {
        // checking for tool property, if it doesn't exist,take value as default
        String toolProperty = sakaiFacade.getToolManager().getCurrentPlacement().getConfig().getProperty("signup.other.sites.availability", "default");
        if ("default".equals(toolProperty)) {
            // if tool property is not set, then consider sakai property
            toolProperty = String.valueOf(sakaiFacade.getServerConfigurationService().getBoolean("signup.otherSitesAvailability", true));
        }
        // tool property would take precedence over sakai property
        otherSitesAvailability = !"false".equalsIgnoreCase(toolProperty);
        return otherSitesAvailability;
    }

    public int getMaxSlots() {
        String maxSlotsStringVal = Utilities.getSignupConfigParamVal("signup.maxSlots", "500");
        try {
            maxSlots = Integer.parseInt(maxSlotsStringVal);
        } catch (Exception e) {
            maxSlots = 500;
        }
        return maxSlots;
    }

    public int getMaxAttendeesPerSlot() {
        String maxAttendeesStringVal = Utilities.getSignupConfigParamVal("signup.maxAttendeesPerSlot", "500");
        try {
            maxAttendeesPerSlot = Integer.parseInt(maxAttendeesStringVal);
        } catch (Exception e) {
            maxAttendeesPerSlot = 500;
        }
        return maxAttendeesPerSlot;
    }

    /**
     * Initialize all the default setting for creating new events.
     */
    private void init() {

        signupMeeting = new SignupMeeting();
        signupMeeting.setMeetingType(INDIVIDUAL);

        Date date = new Date();
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(date);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        signupMeeting.setStartTime(startCal.getTime());

        startCal.add(Calendar.HOUR, 1);
        signupMeeting.setEndTime(startCal.getTime());

        unlimited = false;
        recurrence = false;
        assignParicitpantsToAllRecurEvents = false;
        numberOfSlots = 4;
        maxNumOfSlots = 1;
        numberOfAttendees = 1;
        maxOfAttendees = 10;
        timeSlotDuration = 0; // minutes
        signupBegins = 6;
        deadlineTime = 1;
        signupBeginsType = Utilities.DAYS;
        deadlineTimeType = Utilities.HOURS;
        validationError = false;
        sendEmail = DEFAULT_SEND_EMAIL;
        if (NEW_MEETING_SEND_EMAIL) {
            sendEmail = true;
        }
        sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;
        receiveEmail = false;
        sendEmailByOwner = DEFAULT_SEND_EMAIL; /*will be inherited per meeting basis*/
        allowComment = DEFAULT_ALLOW_COMMENT;
        allowWaitList = DEFAULT_ALLOW_WAITLIST;
        autoReminder = DEFAULT_AUTO_RIMINDER;
        publishToCalendar = DEFAULT_EXPORT_TO_CALENDAR_TOOL;
        createGroups = DEFAULT_CREATE_GROUPS;
        currentStepHiddenInfo = null;
        eidInputMode = false;
        repeatType = ONCE_ONLY;
        repeatUntil = startCal.getTime();
        recurLengthChoice = "1";//0 for num of repeat, 1 for date choice
        occurrences = 0;
        publishedSite = null;
        // custom defined time slots allocation
        userDefinedTS = false;
        customTimeSlotWrpList = null;
        otherSitesAvailability = true;
        creatorUserId = null;

        // cleanup unused attachments in CHS
        if (attachments != null && !attachments.isEmpty()) {
            for (SignupAttachment attach : attachments) {
                getAttachmentHandler().removeAttachmentInContentHost(attach);
            }
            attachments.clear();
        } else {
            attachments = new ArrayList<>();
        }
    }

    public void reset() {
        init();
        signupBeginsType = Utilities.DAYS;
        deadlineTimeType = Utilities.HOURS;
        signupBegins = 6;
        deadlineTime = 1;
        timeSlotWrappers = null;
        currentSite = null;
        otherSites = null;
        // for main meeting page
        Utilities.resetMeetingList();
        eidOrEmailInputByUser = null;
        selectedLocation = null;
        customLocation = "";
        selectedCategory = null;
        customCategory = "";
        creatorUserId = null;
        locations = null;
        categories = null;
        // clean up everything in getUserDefineTimeslotBean
        getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.NEW_MEETING);
    }

    /**
     * This method is called to get all locations to populate the dropdown, for new signup creation.
     *
     * @return list of allLocations
     */
    public List<SelectItem> getAllLocations() {
        if (locations == null) {
            locations = new ArrayList<>(Utilities.getSignupMeetingsBean().getAllLocations());
            locations.add(0, new SelectItem(Utilities.rb.getString("select_location")));
        }
        return locations;
    }

    /**
     * This method is called to get all categories to populate the dropdown, for new signup creation.
     *
     * @return list of categories
     */
    public List<SelectItem> getAllCategories() {
        if (categories == null) {
            categories = new ArrayList<>(Utilities.getSignupMeetingsBean().getAllCategories());
            categories.remove(0); // remove the 'All' default value from the list
            categories.add(0, new SelectItem(Utilities.rb.getString("select_category")));
        }
        return categories;
    }

    /**
     * This is a JSF action call method by UI to navigate to the next page.
     *
     * @return an action outcome string.
     */
    public String goNext() {
        if (validationError) {
            validationError = false;
            return ADD_MEETING_STEP1_PAGE_URL;
        }

        String step = (String) currentStepHiddenInfo.getValue();
        if (step.equals("step1")) {
            // let recalculate the duration just in case of meeting endTime changes
            setTimeSlotDuration(0);
            if (isUserDefinedTS()) {
                // get the timeslots schedules for further process
                if (!Utilities.isDataIntegritySafe(isUserDefinedTS(), UserDefineTimeslotBean.NEW_MEETING, getUserDefineTimeslotBean())) {
                    return ADD_MEETING_STEP1_PAGE_URL;
                }
                customTimeSlotWrpList = getUserDefineTimeslotBean().getDestTSwrpList();
            }
            return ADD_MEETING_STEP2_PAGE_URL;
        }
        return ADD_MEETING_STEP1_PAGE_URL;
    }

    /**
     * This method is called by JSP page for adding/removing attachments action.
     *
     * @return null.
     */
    public String addRemoveAttachments() {
        this.setMeetingDates();
        getAttachmentHandler().processAddAttachRedirect(attachments, signupMeeting, true);
        return null;
    }

    /**
     * Create a new time slot blocks
     *
     * @return String object for the next page url
     */
    public String createUserDefTimeSlots() {
        /* initially get the data from automatic time-slot creation as default*/
        timeSlotDuration = 0;//reset the value
        customTimeSlotWrpList = timeSlotWrappers();
        getUserDefineTimeslotBean().init(signupMeeting, ADD_MEETING_STEP1_PAGE_URL, customTimeSlotWrpList, UserDefineTimeslotBean.NEW_MEETING);
        return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
    }

    /**
     * Modify the existing time slot blocks
     *
     * @return String object for next page url
     */
    public String editUserDefTimeSlots() {
        if (!Utilities.isDataIntegritySafe(isUserDefinedTS(), UserDefineTimeslotBean.NEW_MEETING, getUserDefineTimeslotBean())) {
            reset();
            return ADD_MEETING_STEP1_PAGE_URL;
        }

        customTimeSlotWrpList = getUserDefineTimeslotBean().getDestTSwrpList();
        getUserDefineTimeslotBean().init(signupMeeting, ADD_MEETING_STEP1_PAGE_URL, customTimeSlotWrpList, UserDefineTimeslotBean.NEW_MEETING);
        return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
    }

    /**
     * This is a validator to make sure that the event/meeting starting time is
     * before ending time.
     *
     * @param e an ActionEvent object.
     */

    public void validateNewMeeting(ActionEvent e) {
        if (currentStepHiddenInfo == null) return;

        String step = (String) currentStepHiddenInfo.getValue();

        if ("step1".equals(step)) {
            this.setMeetingDates();
            boolean locationSet = false;

            // Set Title
            if (StringUtils.isNotBlank(title)) {
                log.debug("title set: " + title);
                signupMeeting.setTitle(title);
            } else {
                validationError = true;
                Utilities.addErrorMessage(Utilities.rb.getString("event.title_cannot_be_blank"));
                return;
            }

            // Set Location
            if (StringUtils.isNotBlank(customLocation)) {
                log.debug("custom location set: " + customLocation);
                signupMeeting.setLocation(customLocation);
                locationSet = true;
            }

            if (!locationSet && StringUtils.isNotBlank(selectedLocation) && !StringUtils.equals(selectedLocation, Utilities.rb.getString("select_location"))) {
                signupMeeting.setLocation(selectedLocation);
                log.debug("chose a location: " + selectedLocation);
                locationSet = true;
            }

            if (!locationSet) {
                validationError = true;
                Utilities.addErrorMessage(Utilities.rb.getString("event.location_not_assigned"));
                return;
            }

            // Set Category
            if (StringUtils.isNotBlank(customCategory)) {
                signupMeeting.setCategory(customCategory);
            } else {
                // or from the dropdown, but if we don't choose one or left it as the 'choose category method' then don't set it
                if (!StringUtils.equals(selectedCategory, Utilities.rb.getString("select_category"))) {
                    signupMeeting.setCategory(selectedCategory);
                }
            }

            // Need to filter for bad HTML
            String filteredDescription = sakaiFacade.getFormattedText()
                    .processFormattedText(signupMeeting.getDescription(), null, true);
            signupMeeting.setDescription(filteredDescription);

            // set instructor
            signupMeeting.setCreatorUserId(creatorUserId);

            Date eventEndTime = signupMeeting.getEndTime();
            Date eventStartTime = signupMeeting.getStartTime();
            // user defined own TS case
            if (isUserDefinedTS()) {
                if (getUserDefineTimeslotBean().getDestTSwrpList() == null || getUserDefineTimeslotBean().getDestTSwrpList().isEmpty()) {
                    validationError = true;
                    Utilities.addErrorMessage(Utilities.rb.getString("event.create_custom_defined_TS_blocks"));
                    return;
                } else {
                    eventEndTime = getUserDefineTimeslotBean().getEventEndTime();
                    eventStartTime = getUserDefineTimeslotBean().getEventStartTime();
                    // pass the value since they are null
                    signupMeeting.setStartTime(eventStartTime);
                    signupMeeting.setEndTime(eventEndTime);
                }

            }

            if (eventEndTime.before(eventStartTime) || eventStartTime.equals(eventEndTime)) {
                validationError = true;
                Utilities.addErrorMessage(Utilities.rb.getString("event.endTime_should_after_startTime"));
                return;
            }

            setRecurrence(false);
            if (!(getRepeatType().equals(ONCE_ONLY))) {
                int repeatNum = getOccurrences();
                if ("1".equals(getRecurLengthChoice())) {
                    repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), eventStartTime,
                            getRepeatUntil());
                }

                if ((DAILY.equals(getRepeatType()) || WEEKDAYS.equals(getRepeatType())) && isMeetingOverRepeatPeriod(eventStartTime, eventEndTime, 1)) {
                    validationError = true;
                    Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.daily.problem"));
                    return;
                }

                if (WEEKLY.equals(getRepeatType()) && isMeetingOverRepeatPeriod(eventStartTime, eventEndTime, 7)) {
                    validationError = true;
                    Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.weekly.problem"));
                    return;
                }

                if (BIWEEKLY.equals(getRepeatType()) && isMeetingOverRepeatPeriod(eventStartTime, eventEndTime, 14)) {
                    validationError = true;
                    Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.biweekly.problem"));
                    return;
                }
                // TODO need to check for weekly too?

                if (repeatNum < 1) {
                    validationError = true;
                    if ("1".equals(getRecurLengthChoice()))
                        Utilities.addErrorMessage(Utilities.rb.getString("event.repeatbeforestart"));
                    else
                        Utilities.addErrorMessage(Utilities.rb.getString("event.repeatNnum.bigger.than.one"));

                    return;
                }
                setRecurrence(true);
            }

            warnMeetingAccrossTwoDates(eventEndTime, eventStartTime);

            if (!CreateSitesGroups.isAtleastASiteOrGroupSelected(getCurrentSite(), getOtherSites())) {
                validationError = true;
                Utilities.addErrorMessage(Utilities.rb.getString("select.atleast.oneGroup"));

            }

            if (signupMeeting.getMeetingType() == null) {
                validationError = true;
                Utilities.addErrorMessage(Utilities.rb.getString("signup.validator.selectMeetingType"));
            }

            /* give warning to user in the next page if the event ending time get auto adjusted due to not even-division
             * and it's not the case for custom defined time slot */
            setEndTimeAutoAdjusted(false);
            if (!isUserDefinedTS()) {
                if (isIndividualType() && getNumberOfSlots() != 0) {
                    double duration = (double) (getSignupMeeting().getEndTime().getTime() - getSignupMeeting().getStartTime().getTime()) / (double) (MINUTE_IN_MILLISEC * getNumberOfSlots());
                    if (duration != Math.floor(duration)) {
                        setEndTimeAutoAdjusted(true);
                        Utilities.addErrorMessage(Utilities.rb.getString("event_endtime_auto_adjusted_warning"));
                    }
                }
            }

            // for custom time slot case
            if (!validationError && isUserDefinedTS()) {
                signupMeeting.setStartTime(eventStartTime);
                signupMeeting.setEndTime(eventEndTime);
                signupMeeting.setMeetingType(CUSTOM_TIMESLOTS);
            }
            /*reset meetingType for step1 */
            if (!isUserDefinedTS() && CUSTOM_TIMESLOTS.equals(signupMeeting.getMeetingType())) {
                signupMeeting.setMeetingType(INDIVIDUAL);
            }

            // pre-load all possible coordinators for step2
            signupMeeting.setSignupSites(CreateSitesGroups.getSelectedSignupSites(getCurrentSite(), getOtherSites()));
            allPossibleCoordinators = sakaiFacade.getAllPossbileCoordinatorsOnFastTrack(signupMeeting);

            // tick the creator by default (SIGNUP-216)
            for (SignupUser u : allPossibleCoordinators) {
                if (StringUtils.equals(u.getInternalUserId(), creatorUserId)) {
                    u.setChecked(true);
                }
            }

        }
    }

    private void warnMeetingAccrossTwoDates(Date endTime, Date startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH);
        int startDay = calendar.get(Calendar.DATE);

        calendar.setTime(endTime);
        int endYear = calendar.get(Calendar.YEAR);
        int endMonth = calendar.get(Calendar.MONTH);
        int endDay = calendar.get(Calendar.DATE);
        if (startYear != endYear) {
            Utilities.addInfoMessage(Utilities.rb.getString("warning.event.crossed_twoYears"));
            return;
        }
        if (startMonth != endMonth) {
            Utilities.addInfoMessage(Utilities.rb.getString("warning.event.crossed_twoMonths"));
            return;
        }
        if (startDay != endDay) {
            Utilities.addInfoMessage(Utilities.rb.getString("warning.event.crossed_twoDays"));
        }
    }

    private boolean isMeetingOverRepeatPeriod(Date startTime, Date endTime, int repeatPeriodInDays) {
        long duration = endTime.getTime() - startTime.getTime();
        return ((24 * repeatPeriodInDays) - (duration / (MINUTE_IN_MILLISEC * Hour_In_MINUTES))) < 0;
    }

    /**
     * This is a JSF action call method by UI to let user navigate one page
     * back.
     *
     * @return an action outcome string.
     */
    public String goBack() {
        if (currentStepHiddenInfo == null) {
            // it is rarely happening if any
            Utilities.addErrorMessage(Utilities.rb.getString("publish.withAttendee.exception"));
            // recover this from "assignAttendee" step case too
            // reset to remove timeslots info with attendees
            timeSlotWrappers = null;
            assignParicitpantsToAllRecurEvents = false;
            // reset warning for ending time auto-adjustment
            setEndTimeAutoAdjusted(false);
            return ADD_MEETING_STEP1_PAGE_URL;
        }

        String step = (String) currentStepHiddenInfo.getValue();

        if ("step2".equals(step)) {
            return ADD_MEETING_STEP1_PAGE_URL;
        }
        if ("assignAttendee".equals(step)) {
            timeSlotWrappers = null; // reset to remove timeslots info with attendees
            assignParicitpantsToAllRecurEvents = false;
            // reset warning for ending time auto-adjustment
            setEndTimeAutoAdjusted(false);
            // reset who should receive emails
            // setSendEmailAttendeeOnly(false);
            sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;//reset

            return ADD_MEETING_STEP2_PAGE_URL;
        }

        return ADD_MEETING_STEP1_PAGE_URL;
    }

    /**
     * This is a JSF action call method by UI to let user cancel the action.
     *
     * @return an action outcome string.
     */
    public String processCancel() {
        reset();
        return CANCEL_ADD_MEETING_PAGE_URL;
    }

    /**
     * This is a ValueChange Listener to watch the meeting type selection by
     * user.
     *
     * @param vce a ValuechangeEvent object.
     * @return a outcome string.
     */
    public String processSelectedType(ValueChangeEvent vce) {
        String newMeetingType = (String) vce.getNewValue();
        signupMeeting.setMeetingType(newMeetingType);
        if (!INDIVIDUAL.equals(newMeetingType)) {
            setUserDefinedTS(false);
        }

        return "";

    }

    /**
     * This is a ValueChange Listener to watch changes on the selection of
     * 'unlimited attendee' choice by user.
     *
     * @param vce a ValuechangeEvent object.
     * @return a outcome string.
     */
    public String processGroup(ValueChangeEvent vce) {
        Boolean changeValue = (Boolean) vce.getNewValue();
        if (changeValue != null) {
            unlimited = changeValue;
            if (unlimited) maxOfAttendees = 10;
        }

        return "";
    }

    /**
     * This is a JSF action call method by UI to let user to save and create a
     * new event/meeting.
     *
     * @return an action outcome string.
     */
    public String processSave() {
        if (!Utilities.isDataIntegritySafe(isUserDefinedTS(), UserDefineTimeslotBean.NEW_MEETING, getUserDefineTimeslotBean())) {
            reset();
            return ADD_MEETING_STEP1_PAGE_URL;
        }

        preSaveAction();
        processSaveMeetings();
        reset();
        return MAIN_EVENTS_LIST_PAGE_URL;
    }

    // Prepare the data for saving action
    private void preSaveAction() {
        List<SignupTimeslot> slots = timeslots();
        signupMeeting.setSignupTimeSlots(slots);

        Date sBegin = Utilities.subTractTimeToDate(signupMeeting.getStartTime(), getSignupBegins(),
                getSignupBeginsType());
        Date sDeadline = Utilities.subTractTimeToDate(signupMeeting.getEndTime(), getDeadlineTime(),
                getDeadlineTimeType());

        signupMeeting.setSignupBegins(sBegin);
        signupMeeting.setSignupDeadline(sDeadline);
        // maybe duplicated, it is already set up after 'step1'
        signupMeeting.setSignupSites(CreateSitesGroups.getSelectedSignupSites(getCurrentSite(), getOtherSites()));

        signupMeeting.setCreatorUserId(creatorUserId);
        signupMeeting.setReceiveEmailByOwner(receiveEmail);
        signupMeeting.setSendEmailByOwner(sendEmailByOwner);
        signupMeeting.setAllowWaitList(allowWaitList);
        signupMeeting.setAllowComment(allowComment);
        signupMeeting.setAutoReminder(autoReminder);
        signupMeeting.setEidInputMode(eidInputMode);
        signupMeeting.setMaxNumOfSlots(maxNumOfSlots);
        signupMeeting.setCoordinatorIds(Utilities.getSelectedCoordinators(allPossibleCoordinators, creatorUserId));
        // add attachments
        signupMeeting.setSignupAttachments(attachments);

        signupMeeting.setCreateGroups(createGroups);

    }

    /**
     * This is a JSF action call method by UI to let user to go to next page,
     * which will allow user to pre-assign the attendees into the event/meeting.
     *
     * @return an action outcome string.
     */
    public String proceesPreAssignAttendee() {
        if (!Utilities.isDataIntegritySafe(isUserDefinedTS(), UserDefineTimeslotBean.NEW_MEETING, getUserDefineTimeslotBean())) {
            reset();
            return ADD_MEETING_STEP1_PAGE_URL;
        }

        preSaveAction();
        loadAllAttendees(getSignupMeeting());
        return PRE_ASSIGN_ATTENDEE_PAGE_URL;
    }

    /**
     * This is a JSF action call method by UI to let user to save and publish
     * the new event/meeting with pre-assigned attendees.
     *
     * @return an action outcome string.
     */
    public String processAssignStudentsAndPublish() {
        preSaveAction();
        processSaveMeetings();
        reset();
        return MAIN_EVENTS_LIST_PAGE_URL;
    }

    /**
     * This is a JSF action call method by UI to let user add an attendee into
     * the page.
     *
     * @return an action outcome string.
     */
    public String addAttendee() {
        TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

        String attendeeEidOrEmail;
        if (isEidInputMode()) {
            attendeeEidOrEmail = getEidOrEmailInputByUser();
        } else {
            attendeeEidOrEmail = (String) newAttendeeInput.getValue();
        }

        if (attendeeEidOrEmail == null || attendeeEidOrEmail.length() < 1)
            return PRE_ASSIGN_ATTENDEE_PAGE_URL;

        // check if there are multiple email addresses associated with input
        List<String> associatedEids = getEidsForEmail(attendeeEidOrEmail.trim());
        if (associatedEids.size() > 1) {
            Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("exception.multiple.eids"), attendeeEidOrEmail, StringUtils.join(associatedEids, ", ")));
            return PRE_ASSIGN_ATTENDEE_PAGE_URL;
        }

        String attendeeUserId = getUserIdForEidOrEmail(attendeeEidOrEmail.trim());
        if (StringUtils.isBlank(attendeeEidOrEmail)) {
            Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user") + attendeeEidOrEmail);
            return PRE_ASSIGN_ATTENDEE_PAGE_URL;
        }

        SignupUser attendeeSignUser = getSakaiFacade().getSignupUser(signupMeeting, attendeeUserId);
        if (attendeeSignUser == null) {
            Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("user.has.no.permission.attend"), attendeeEidOrEmail));
            return PRE_ASSIGN_ATTENDEE_PAGE_URL;
        }

        SignupAttendee attendee = new SignupAttendee(attendeeUserId, attendeeSignUser.getMainSiteId());

        if (isDuplicateAttendee(timeslotWrapper.getTimeSlot(), attendee)) {
            Utilities.addErrorMessage(Utilities.rb.getString("attendee.already.in.timeslot"));
        } else {
            timeslotWrapper.addAttendee(attendee, sakaiFacade.getUserDisplayLastFirstName(attendeeUserId));
        }

        return PRE_ASSIGN_ATTENDEE_PAGE_URL;
    }

    /**
     * This is a JSF action call method by UI to let user remove an attendee
     * from the page.
     *
     * @return an action outcome string.
     */
    public String removeAttendee() {
        TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
        String attendeeUserId = Utilities.getRequestParam(PARAM_NAME_FOR_ATTENDEE_USERID);

        timeslotWrapper.removeAttendee(attendeeUserId);

        return "";
    }

    private boolean isDuplicateAttendee(SignupTimeslot timeslot, SignupAttendee newAttendee) {
        List<SignupAttendee> attendees = timeslot.getAttendees();
        if (attendees != null && !attendees.isEmpty()) {
            for (SignupAttendee attendee : attendees) {
                if (attendee.getAttendeeUserId().equals(newAttendee.getAttendeeUserId()))
                    return true;
            }
        }
        return false;
    }

    private List<SignupTimeslot> timeslots() {
        List<SignupTimeslot> slots = new ArrayList<>();
        List<TimeslotWrapper> timeSlotWrappers = getTimeSlotWrappers();

        if (timeSlotWrappers == null) return null; // for Announcement type

        for (TimeslotWrapper wrapper : timeSlotWrappers) {
            SignupTimeslot slot = wrapper.getTimeSlot();
            slots.add(slot);
        }
        return slots;
    }

    /**
     * This is a getter method for UI.
     *
     * @return a list of TimeslotWrapper objects.
     */
    public List<TimeslotWrapper> getTimeSlotWrappers() {
        if (timeSlotWrappers == null)
            timeSlotWrappers = timeSlotWrappers();

        return timeSlotWrappers;

    }

    /**
     * construct the TimeslotWrapper list from the raw data
     * @return list of Time slots
     */
    private List<TimeslotWrapper> timeSlotWrappers() {
        String meetingType = signupMeeting.getMeetingType();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(signupMeeting.getStartTime());
        String isoStartTime = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(HIDDEN_ISO_STARTTIME);
        if (DateFormatterUtil.isValidISODate(isoStartTime)) {
            calendar.setTime(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoStartTime));
        }

        List<TimeslotWrapper> timeSlotWrappers = new ArrayList<>();
        if (meetingType.equals(INDIVIDUAL)) {
            for (int i = 0; i < numberOfSlots; i++) {
                SignupTimeslot slot = new SignupTimeslot();
                slot.setMaxNoOfAttendees(numberOfAttendees);
                slot.setStartTime(calendar.getTime());
                calendar.add(Calendar.MINUTE, getTimeSlotDuration());
                slot.setEndTime(calendar.getTime());
                slot.setDisplayAttendees(isShowParticipants());

                TimeslotWrapper wrapper = new TimeslotWrapper(slot);
                wrapper.setPositionInTSlist(i);
                timeSlotWrappers.add(wrapper);
            }
            // set endTime for meeting
            getMeetingEndTime();
            return timeSlotWrappers;
        }

        if (meetingType.equals(GROUP)) {
            SignupTimeslot slot = new SignupTimeslot();
            slot.setMaxNoOfAttendees(unlimited ? SignupTimeslot.UNLIMITED : maxOfAttendees);
            slot.setStartTime(signupMeeting.getStartTime());
            slot.setEndTime(signupMeeting.getEndTime());
            slot.setDisplayAttendees(isShowParticipants());

            TimeslotWrapper wrapper = new TimeslotWrapper(slot);
            timeSlotWrappers.add(wrapper);
            return timeSlotWrappers;
        }

        if (meetingType.equals(CUSTOM_TIMESLOTS)) {
            List<TimeslotWrapper> tmpTSList = new ArrayList<>(customTimeSlotWrpList);
            for (TimeslotWrapper tsWrp : tmpTSList) {
                tsWrp.getTimeSlot().setDisplayAttendees(isShowParticipants());
            }
            return tmpTSList;
        }
        return null;
    }

    /**
     * This is a getter method for UI and it provides the time for Signup-begin.
     *
     * @return a Date object.
     */
    public Date getSignupBeginInDate() {
        return Utilities.subTractTimeToDate(signupMeeting.getStartTime(), signupBegins, signupBeginsType);
    }

    /**
     * This is a getter method for UI and it provides the time for
     * Signup-Deadline.
     *
     * @return a Date object.
     */
    public Date getSignupDeadlineInDate() {
        return Utilities.subTractTimeToDate(signupMeeting.getEndTime(), deadlineTime, deadlineTimeType);
    }

    /**
     * This is a getter method for UI and it calculates the meeting ending time
     * according to meeting type
     *
     * @return a Date object.
     */
    /*  */
    public Date getMeetingEndTime() {
        if (signupMeeting.getMeetingType().equals(INDIVIDUAL)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(signupMeeting.getStartTime());
            int internval = getTimeSlotDuration() * numberOfSlots;
            calendar.add(Calendar.MINUTE, internval);
            signupMeeting.setEndTime(calendar.getTime());
            return calendar.getTime();
        }

        return signupMeeting.getEndTime();
    }



    public int getMaxOfAttendees() {
        if (unlimited) maxOfAttendees = SignupTimeslot.UNLIMITED;
        return maxOfAttendees;
    }

    public int getTimeSlotDuration() {
        if (timeSlotDuration == 0) { // first time
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            Date startTime = getSignupMeeting().getStartTime();
            String isoStartTime = params.get(HIDDEN_ISO_STARTTIME);
            if (DateFormatterUtil.isValidISODate(isoStartTime)) {
                startTime = sakaiFacade.getTimeService().parseISODateInUserTimezone(isoStartTime);
            }
            Date endTime = getSignupMeeting().getEndTime();
            String isoEndTime = params.get(HIDDEN_ISO_ENDTIME);
            if (DateFormatterUtil.isValidISODate(isoEndTime)) {
                endTime = sakaiFacade.getTimeService().parseISODateInUserTimezone(isoEndTime);
            }
            long duration = (endTime.getTime() - startTime.getTime()) / (MINUTE_IN_MILLISEC * getNumberOfSlots());
            setTimeSlotDuration((int) duration);
        }
        return timeSlotDuration;
    }


    public List<SignupSiteWrapper> getOtherSites() {
        if (currentSite == null) {
            getAvailableSiteGroups();
        }
        return otherSites;
    }

    public SignupSiteWrapper getCurrentSite() {
        if (currentSite == null) {
            getAvailableSiteGroups();
        }
        return currentSite;
    }

    /**
     * Due to Authz bug, we have to share the same CreateSitesGroups object. It
     * will much simple if Authz bug is fixed.
     */
    private void getAvailableSiteGroups() {
        Utilities.getSignupMeetingsBean().getCreateSitesGroups().resetSiteGroupCheckboxMark();
        currentSite = Utilities.getSignupMeetingsBean().getCreateSitesGroups().getCurrentSite();
        otherSites = Utilities.getSignupMeetingsBean().getCreateSitesGroups().getOtherSites();
    }

    public boolean getSendEmail() {
        if (!getPublishedSite()) sendEmail = false; // no email notification
        return sendEmail;
    }


    public String getIndividual() {
        return INDIVIDUAL;
    }

    public String getGroup() {
        return GROUP;
    }

    public String getAnnouncement() {
        return ANNOUNCEMENT;
    }

    private void loadAllAttendees(SignupMeeting meeting) {
        if (isEidInputMode())
            return;

        try {
            Site site = getSakaiFacade().getSiteService().getSite(getSakaiFacade().getCurrentLocationId());
            if (site != null) {
                int allMemeberSize = site.getMembers() != null ? site.getMembers().size() : 0;
                /*
                 * due to efficiency, user has to input EID instead of using dropdown
                 * user name list
                 * First check to avoid load all site member up if there is ten of thousends
                 */
                if (allMemeberSize > MAX_NUM_PARTICIPANTS_FOR_DROPDOWN_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE) {
                    setEidInputMode(true);
                    return;
                }
            }
        } catch (IdUnusedException e) {
            log.error(e.getMessage(), e);
        }

        allSignupUsers = sakaiFacade.getAllPossibleAttendees(meeting);

        if (allSignupUsers != null && allSignupUsers.size() > MAX_NUM_PARTICIPANTS_FOR_DROPDOWN_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE) {
            setEidInputMode(true);
            return;
        }

        setEidInputMode(false);
        allAttendees = new ArrayList<>();
        SelectItem sItem = new SelectItem("", " " + Utilities.rb.getString("label.select.attendee"));
        allAttendees.add(sItem);
        String previous_displayName = "";
        int index = 0;
        for (SignupUser user : allSignupUsers) {
            if (user.getDisplayName().equals(previous_displayName)) {
                allAttendees.add(new SelectItem(user.getEid(), user.getDisplayName() + "(" + user.getEid() + ")"));
                SelectItem prev_sItem = allAttendees.get(index);
                //checking: not already has eid for triple duplicates case
                if (!prev_sItem.getLabel().contains("(")) {
                    prev_sItem.setLabel(prev_sItem.getLabel() + " (" + prev_sItem.getValue() + ")");
                }

            } else {
                allAttendees.add(new SelectItem(user.getEid(), user.getDisplayName()));
            }

            previous_displayName = user.getDisplayName();
            index++;
        }
    }

    public String getAttendeeUserId() {
        return PARAM_NAME_FOR_ATTENDEE_USERID;
    }

    public boolean isAnnouncementType() {
        return ANNOUNCEMENT.equals(getSignupMeeting().getMeetingType());
    }

    public boolean isGroupType() {
        return GROUP.equals(getSignupMeeting().getMeetingType());
    }

    public boolean isIndividualType() {
        return INDIVIDUAL.equals(getSignupMeeting().getMeetingType());
    }

    public boolean isCustomTimeslotType() {
        return CUSTOM_TIMESLOTS.equals(getSignupMeeting().getMeetingType());
    }

    /**
     * This is for UI purpose and it displays the meeting type, which user can
     * redefine in message bundle
     *
     * @return a meeting display type
     */
    public String getDisplayCurrentMeetingType() {
        String mType = "";
        if (isIndividualType()) {
            mType = Utilities.rb.getString("label_individaul");
        } else if (isGroupType()) {
            mType = Utilities.rb.getString("label_group");
        } else if (isAnnouncementType()) {
            mType = Utilities.rb.getString("label_announcement");
        } else if (isUserDefinedTS()) {
            mType = Utilities.rb.getString("label_custom_timeslots");
        }
        return mType;
    }


    /**
     * This is for Javascript UI only.
     *
     * @return empty string.
     */
    public String getUserInputEidOrEmail() {
        return "";
    }

    /**
     * This is for Javascript UI only.
     *
     * @param value eid or email for the user
     */
    public void setUserInputEidOrEmail(String value) {
        if (StringUtils.isNotBlank(value)) {
            eidOrEmailInputByUser = value;
        }
    }

    private String getEidOrEmailInputByUser() {
        String eid = eidOrEmailInputByUser;
        eidOrEmailInputByUser = null; // reset for use once only
        return eid;
    }

    private void processSaveMeetings() {
        signupMeeting.setRepeatUntil(getRepeatUntil());
        int repeatNum = getOccurrences();
        if ("1".equals(getRecurLengthChoice())) {
            repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), signupMeeting.getStartTime(),
                    getRepeatUntil());
        }
        signupMeeting.setRepeatNum(repeatNum);
        signupMeeting.setRepeatType(getRepeatType());

        if (CUSTOM_TIMESLOTS.equals(signupMeeting.getMeetingType())) {
            boolean multipleCalBlocks = getUserDefineTimeslotBean().getPutInMultipleCalendarBlocks();
            signupMeeting.setInMultipleCalendarBlocks(multipleCalBlocks);
        }

        // pass who should receive the emails
        signupMeeting.setSendEmailToSelectedPeopleOnly(sendEmailToSelectedPeopleOnly);

        CreateMeetings createMeeting = new CreateMeetings(signupMeeting, sendEmail,
                !assignParicitpantsToAllRecurEvents, assignParicitpantsToAllRecurEvents, getSignupBegins(),
                getSignupBeginsType(), getDeadlineTime(), getDeadlineTimeType(), getRecurLengthChoice(), sakaiFacade, signupMeetingService,
                getAttachmentHandler(), sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), true);

        try {
            // need push to calendar tool
            createMeeting.setPublishToCalendar(publishToCalendar);

            // do we want to also create groups?
            createMeeting.setCreateGroups(createGroups);

            createMeeting.processSaveMeetings();

            // handle attachments and it should not be cleaned up in CHS
            attachments.clear();

        } catch (PermissionException e) {
            log.info("{} - {}", Utilities.rb.getString("no.permission_create_event"), e.getMessage());
        } catch (Exception e) {
            log.error("{} - {}", Utilities.rb.getString("error.occurred_try_again"), e.getMessage());
            Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
        }

    }

    /**
     * This is a getter method for UI
     *
     * @return a event frequency type string
     */
    public String getEventFreqType() {
        switch (getRepeatType()) {
            case DAILY:
                eventFreqType = Utilities.rb.getString("label_daily");
                break;
            case WEEKDAYS:
                eventFreqType = Utilities.rb.getString("label_weekdays");
                break;
            case WEEKLY:
                eventFreqType = Utilities.rb.getString("label_weekly");
                break;
            case BIWEEKLY:
                eventFreqType = Utilities.rb.getString("label_biweekly");
                break;
            default:
                eventFreqType = StringUtils.EMPTY;
                break;
        }
        return eventFreqType;
    }

    public List<SignupSite> getSelectedSignupSites() {
        return CreateSitesGroups.getSelectedSignupSites(currentSite, otherSites);
    }

    public List<SelectItem> getMeetingTypeRadioBttns() {
        meetingTypeRadioBttns = Utilities.getMeetingTypeSelectItems("", false);
        return meetingTypeRadioBttns;
    }

    public Boolean getPublishedSite() {
        if (publishedSite == null) {
            try {
                publishedSite = sakaiFacade.getSiteService().getSite(sakaiFacade.getCurrentLocationId()).isPublished();
            } catch (Exception e) {
                log.warn(e.getMessage());
                publishedSite = Boolean.FALSE;
            }
        }
        return publishedSite;
    }


    public boolean isAttachmentsEmpty() {
        return attachments == null || attachments.isEmpty();
    }

    public boolean isAllLocationsEmpty() {
        // this is safe to call often since we cache the locations
        // it already has one label item in
        return !(getAllLocations().size() > 1);

    }

    public boolean isCategoriesExist() {
        // this is safe to call often since we cache the categories
        return getAllCategories().size() > 1;
    }

    /**
     * @return true if sakai property signup.enableAttendance is true, else will return false
     */
    public boolean isAttendanceOn() {
        return Utilities.getSignupMeetingsBean().isAttendanceOn();
    }

    /**
     * Get a list of users that have permission, but format it as a SelectItem list for the dropdown.
     * Since this is a new item there will be no current instructor so it returns the current user at the top of the list
     * We send a null signup meeting param as this is a new meeting.
     */
    public List<SelectItem> getInstructors() {
        return Utilities.getSignupMeetingsBean().getInstructors(null);
    }

    /**
     * Get the instructor name attached to the value currently set for the instructor
     */
    public String getInstructorName() {
        return Utilities.getSignupMeetingsBean().getFormattedName(sakaiFacade.getUser(creatorUserId));
    }

    /**
     * Gets the userId for a user, given an eid or an email address.
     * We check if it matches the eid first, then if it matches an email address.
     * If nothing, return null.
     *
     * @param value the string to lookup, could be an eid or an email address
     * @return the userId or null if User cannot be found
     */
    public String getUserIdForEidOrEmail(String value) {
        User u = Optional.ofNullable(Optional.ofNullable(sakaiFacade.getUserByEid(value)).orElseGet(() -> sakaiFacade.getUserByEmail(value))).orElse(null);
        if (u != null) return u.getId();
        return null;
    }

    /**
     * Get the eids assocaited with an email address, ie there may be two or more users with the same email address.
     * We need to be able to handle this in the UI.
     *
     * @param email email address to lookup
     * @return List<String> of eids.
     */
    public List<String> getEidsForEmail(String email) {
        return sakaiFacade.getUsersByEmail(email).stream().map(User::getEid).collect(Collectors.toList());
    }

    /**
     * This is a getter method which provide current Iframe id for refresh
     * IFrame purpose.
     *
     * @return the current tool placement
     */
    public String getIframeId() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return (String) request.getAttribute("sakai.tool.placement.id");
    }

    /* Get the dates from the input fields and save them into the signupMetting object.*/
    private void setMeetingDates() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String isoStartTime = params.get(HIDDEN_ISO_STARTTIME);

        if(DateFormatterUtil.isValidISODate(isoStartTime)){
            this.signupMeeting.setStartTime(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoStartTime));
        }

        String isoEndTime = params.get(HIDDEN_ISO_ENDTIME);

        if(DateFormatterUtil.isValidISODate(isoEndTime)){
            this.signupMeeting.setEndTime(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoEndTime));
        }

        String isoUntilTime = params.get(HIDDEN_ISO_UNTILTIME);

        if(DateFormatterUtil.isValidISODate(isoUntilTime)){
            setRepeatUntil(DateFormatterUtil.parseISODate(isoUntilTime));
            this.signupMeeting.setRepeatUntil(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoUntilTime));
        }

    }
}
