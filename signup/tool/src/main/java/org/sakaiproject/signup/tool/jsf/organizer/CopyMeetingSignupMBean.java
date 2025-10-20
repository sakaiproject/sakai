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

package org.sakaiproject.signup.tool.jsf.organizer;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.api.SignupUser;
import org.sakaiproject.signup.api.SignupUserActionException;
import org.sakaiproject.signup.api.model.SignupAttachment;
import org.sakaiproject.signup.api.model.SignupAttendee;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupSiteWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.TimeslotWrapper;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateMeetings;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.util.DateFormatterUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * copy meeting page:<b>copyMeeting.jsp</b> and backbone system.
 *
 * @author Peter Liu
 *
 * </P>
 */
@Slf4j
public class CopyMeetingSignupMBean extends SignupUIBaseBean {

    private static String HIDDEN_ISO_STARTTIME = "startTimeISO8601";
    private static String HIDDEN_ISO_ENDTIME = "endTimeISO8601";
    private static String HIDDEN_ISO_UNTILTIME = "untilISO8601";

    private boolean newMeetingSendEmail;
    @Setter @Getter private SignupMeeting signupMeeting;
    @Getter @Setter private boolean keepAttendees;
    @Getter @Setter private int maxNumOfAttendees;
    @Setter @Getter private boolean unlimited;
    @Setter @Getter private String signupBeginsType;
    @Setter @Getter private int signupBegins; // singup can start before this minutes/hours/days
    @Setter @Getter private String deadlineTimeType;
    @Setter @Getter private int deadlineTime; // singup deadline before this minutes/hours/days
    @Setter @Getter private String title;
    @Setter @Getter private String selectedLocation;
    @Setter @Getter private String selectedCategory;
    @Setter private String creatorUserId;
    @Setter @Getter private Date repeatUntil;
    @Setter @Getter private String repeatType;
    @Setter @Getter private String recurLengthChoice; // 0 for num of repeat, 1 for date choice
    @Setter @Getter private int occurrences;
    @Setter @Getter private int numberOfSlots;
    @Setter @Getter private SignupSiteWrapper currentSite;
    @Setter @Getter private List<SignupSiteWrapper> otherSites;
    @Getter private boolean missingSitGroupWarning;
    @Getter private List<String> missingSites;
    @Getter private List<String> missingGroups;
    @Setter @Getter private boolean assignParicitpantsToAllRecurEvents;
    @Getter @Setter private boolean repeatTypeUnknown = true;
    @Setter @Getter private UserDefineTimeslotBean userDefineTimeslotBean;
    @Setter @Getter private List<TimeslotWrapper> customTimeSlotWrpList; // discontinued time slots case
    @Setter @Getter private boolean userDefinedTS = false;
    @Getter private boolean mandatorySendEmail = newMeetingSendEmail;
    @Setter @Getter private String startTimeString;
    @Setter @Getter private String endTimeString;
    @Setter @Getter private String repeatUntilString;

    private List<SignupUser> allowedUserList;
    private boolean showAttendeeName;
    private boolean validationError;
    private List<SelectItem> categories = null;
    private List<SelectItem> locations = null;

    public CopyMeetingSignupMBean() {
        newMeetingSendEmail = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.email.notification.mandatory.for.newMeeting", "true"));
    }

    /**
     * this reset information which contains in this UIBean lived in a session
     * scope
     */
    public void reset() {
        unlimited = false;
        keepAttendees = false;
        assignParicitpantsToAllRecurEvents = false;
        sendEmail = DEFAULT_SEND_EMAIL;
        if (newMeetingSendEmail) {
            //mandatory send email out
            sendEmail = true;
        }

        //sendEmailAttendeeOnly = false;
        sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;
        publishToCalendar = DEFAULT_EXPORT_TO_CALENDAR_TOOL;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        repeatUntil = calendar.getTime();
        recurLengthChoice = "1";//0 for num of repeat, 1 for date choice
        occurrences = 0;
        repeatType = ONCE_ONLY;
        repeatTypeUnknown = true;
        showAttendeeName = false;
        missingSitGroupWarning = false;

        /*cleanup previously unused attachments in CHS*/
        if (this.signupMeeting != null) cleanUpUnusedAttachmentCopies(this.signupMeeting.getSignupAttachments());

        /*refresh copy of original*/
        this.signupMeeting = signupMeetingService.loadSignupMeeting(meetingWrapper.getMeeting().getId(), sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId());

        /*get meeting title*/
        title = this.signupMeeting.getTitle();

        /*prepare new attachments*/
        assignMainAttachmentsCopyToSignupMeeting();
        //TODO not consider copy time slot attachment yet

        List<SignupTimeslot> signupTimeSlots = signupMeeting.getSignupTimeSlots();

        if (signupTimeSlots != null && !signupTimeSlots.isEmpty()) {
            SignupTimeslot ts = (SignupTimeslot) signupTimeSlots.get(0);
            maxNumOfAttendees = ts.getMaxNoOfAttendees();
            this.unlimited = ts.isUnlimitedAttendee();
            showAttendeeName = ts.isDisplayAttendees();
            this.numberOfSlots = signupTimeSlots.size();

        } else {// announcement meeting type
            setNumberOfSlots(1);

        }

        //populate location and cateogry data for new meeting
        //since it's copymeeting, the dropdown selections should have it already there.
        this.selectedLocation = this.signupMeeting.getLocation();
        this.selectedCategory = this.signupMeeting.getCategory();
        this.customLocation = "";
        this.customCategory = "";
        this.categories = null;
        this.locations = null;

        populateDataForBeginDeadline(this.signupMeeting);

        /*Case: recurrence events*/
        prepareRecurredEvents();

        /* Initialize site/groups for current organizer */
        initializeSitesGroups();

        /* custom-ts case */
        this.customTimeSlotWrpList = null;
        this.userDefinedTS = false;
        /*populate timeslot data*/
        updateTimeSlotWrappers(this.meetingWrapper);
        if (CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType())) {
            this.userDefinedTS = true;
            this.customTimeSlotWrpList = getTimeslotWrappers();
            markerTimeslots(this.customTimeSlotWrpList);
        }

        getUserDefineTimeslotBean().init(this.signupMeeting, COPTY_MEETING_PAGE_URL, this.customTimeSlotWrpList, UserDefineTimeslotBean.COPY_MEETING);


    }

    /* process the relative time for Signup begin/deadline */
    private void populateDataForBeginDeadline(SignupMeeting sMeeting) {
        long signupBeginsTime = sMeeting.getSignupBegins() == null ? new Date().getTime() : sMeeting.getSignupBegins().getTime();
        long signupDeadline = sMeeting.getSignupDeadline() == null ? new Date().getTime() : sMeeting.getSignupDeadline().getTime();

        /* get signup begin & deadline relative time in minutes */
        long signupBeginBeforMeeting = (sMeeting.getStartTime().getTime() - signupBeginsTime) / MINUTE_IN_MILLISEC;
        long signupDeadLineBeforMeetingEnd = (sMeeting.getEndTime().getTime() - signupDeadline) / MINUTE_IN_MILLISEC;

        this.signupBeginsType = Utilities.getTimeScaleType(signupBeginBeforMeeting);
        this.signupBegins = Utilities.getRelativeTimeValue(signupBeginsType, signupBeginBeforMeeting);

        this.deadlineTimeType = Utilities.getTimeScaleType(signupDeadLineBeforMeetingEnd);
        this.deadlineTime = Utilities.getRelativeTimeValue(deadlineTimeType, signupDeadLineBeforMeetingEnd);

        /*user readability case for big numbers of minutes*/
        if (MINUTES.equals(this.signupBeginsType) && sMeeting.getSignupBegins().before(new Date()) && this.signupBegins > 500) {
            /*we assume it has to be 'start now' before and we convert it to round to days*/
            this.signupBeginsType = DAYS;
            this.signupBegins = Utilities.getRelativeTimeValue(DAYS, signupBeginBeforMeeting);
            if (this.signupBegins == 0) this.signupBegins = 1; //add a day					
        }

    }

    /**
     * Just to overwrite the parent one
     */
    public SignupMeetingWrapper getMeetingWrapper() {
        return meetingWrapper;
    }

    /**
     * This is a JSF action call method by UI to copy the event/meeting into a
     * new one
     *
     * @return an action outcome string
     */
    // TODO: what to do if timeslot is locked or canceled
    public String processSaveCopy() {
        if (validationError) {
            validationError = false;
            return COPTY_MEETING_PAGE_URL;
        }

        SignupMeeting sMeeting = getSignupMeeting();
        try {
            prepareCopy(sMeeting);

            sMeeting.setRepeatUntil(getRepeatUntil());
            int repeatNum = getOccurrences();
            if ("1".equals(getRecurLengthChoice())) {
                repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), sMeeting.getStartTime(), getRepeatUntil());
            }
            sMeeting.setRepeatNum(repeatNum);
            sMeeting.setRepeatType(getRepeatType());

            if (CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType())) {
                boolean multipleCalBlocks = getUserDefineTimeslotBean().getPutInMultipleCalendarBlocks();
                sMeeting.setInMultipleCalendarBlocks(multipleCalBlocks);
            }

            /*pass who are receiving emails*/
            sMeeting.setSendEmailToSelectedPeopleOnly(getSendEmailToSelectedPeopleOnly());

            CreateMeetings createMeeting = new CreateMeetings(sMeeting, sendEmail, keepAttendees && !assignParicitpantsToAllRecurEvents, keepAttendees && assignParicitpantsToAllRecurEvents, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(), getDeadlineTimeType(), getRecurLengthChoice(), sakaiFacade, signupMeetingService, getAttachmentHandler(), sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), true);

            createMeeting.setPublishToCalendar(isPublishToCalendar());
            createMeeting.processSaveMeetings();

            /*make sure that they don't get cleaned up in CHS when saved successfully*/
            this.signupMeeting.getSignupAttachments().clear();

        } catch (PermissionException e) {
            log.info(Utilities.rb.getString("no.permission_create_event") + " - " + e.getMessage());
            Utilities.addErrorMessage(Utilities.rb.getString("no.permission_create_event"));
            return ORGANIZER_MEETING_PAGE_URL;
        } catch (SignupUserActionException ue) {
            Utilities.addErrorMessage(ue.getMessage());
            return COPTY_MEETING_PAGE_URL;
        } catch (Exception e) {
            log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
            Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
            return ORGANIZER_MEETING_PAGE_URL;
        }

        getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.COPY_MEETING);

        return MAIN_EVENTS_LIST_PAGE_URL;
    }

    /**
     * This is a validator to make sure that the event/meeting starting time is
     * before ending time etc.
     *
     * @param e
     *            an ActionEvent object.
     */
    public void validateCopyMeeting(ActionEvent e) {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String isoStartTime = params.get(HIDDEN_ISO_STARTTIME);

        if (DateFormatterUtil.isValidISODate(isoStartTime)) {
            this.signupMeeting.setStartTime(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoStartTime));
        }

        String isoEndTime = params.get(HIDDEN_ISO_ENDTIME);

        if (DateFormatterUtil.isValidISODate(isoEndTime)) {
            this.signupMeeting.setEndTime(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoEndTime));
        }

        String isoUntilTime = params.get(HIDDEN_ISO_UNTILTIME);

        if (DateFormatterUtil.isValidISODate(isoUntilTime)) {
            setRepeatUntil(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoUntilTime));
        }
        Date eventEndTime = signupMeeting.getEndTime();
        Date eventStartTime = signupMeeting.getStartTime();

        //Set Title		
        if (StringUtils.isNotBlank(title)) {
            log.debug("title set: " + title);
            this.signupMeeting.setTitle(title);
        } else {
            validationError = true;
            Utilities.addErrorMessage(Utilities.rb.getString("event.title_cannot_be_blank"));
            return;
        }

        /*user defined own TS case*/
        if (isUserDefinedTS()) {
            if (getUserDefineTimeslotBean().getDestTSwrpList() == null || getUserDefineTimeslotBean().getDestTSwrpList().isEmpty()) {
                validationError = true;
                Utilities.addErrorMessage(Utilities.rb.getString("event.create_custom_defined_TS_blocks"));
                return;
            } else {
                eventEndTime = getUserDefineTimeslotBean().getEventEndTime();
                eventStartTime = getUserDefineTimeslotBean().getEventStartTime();
                /*pass the value since they may be null*/
                this.signupMeeting.setStartTime(eventStartTime);
                this.signupMeeting.setEndTime(eventEndTime);
            }

        }

        if (eventEndTime.before(eventStartTime) || eventStartTime.equals(eventEndTime)) {
            validationError = true;
            Utilities.addErrorMessage(Utilities.rb.getString("event.endTime_should_after_startTime"));
            return;
        }

        if (!(getRepeatType().equals(ONCE_ONLY))) {
            int repeatNum = getOccurrences();
            if ("1".equals(getRecurLengthChoice())) {
                repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), eventStartTime, getRepeatUntil());
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

            if (repeatNum < 1) {
                validationError = true;
                if ("1".equals(getRecurLengthChoice()))
                    Utilities.addErrorMessage(Utilities.rb.getString("event.repeatbeforestart"));
                else Utilities.addErrorMessage(Utilities.rb.getString("event.repeatNnum.bigger.than.one"));

                return;
            }
        }

        if (!CreateSitesGroups.isAtleastASiteOrGroupSelected(this.getCurrentSite(), this.getOtherSites())) {
            validationError = true;
            Utilities.addErrorMessage(Utilities.rb.getString("select.atleast.oneGroup.for.copyMeeting"));

        }

        /*for custom defined time slot case*/
        if (!validationError && isUserDefinedTS()) {
            this.signupMeeting.setStartTime(eventStartTime);
            this.signupMeeting.setEndTime(eventEndTime);
            this.signupMeeting.setMeetingType(CUSTOM_TIMESLOTS);
        }

        //Set Location		
        if (StringUtils.isBlank(getCustomLocation())) {
            if (StringUtils.isBlank(selectedLocation) || selectedLocation.equals(Utilities.rb.getString("select_location"))) {
                validationError = true;
                Utilities.addErrorMessage(Utilities.rb.getString("event.location_not_assigned"));
                return;
            }
            this.signupMeeting.setLocation(selectedLocation);

        } else {
            this.signupMeeting.setLocation(getCustomLocation());
        }
        //clear the location fields???
        this.selectedLocation = "";

        //Set Category
        //if textfield is blank, check the dropdown
        if (StringUtils.isBlank(getCustomCategory())) {
            //if dropdown is not the default, then use its value
            if (!StringUtils.equals(selectedCategory, Utilities.rb.getString("select_category"))) {
                this.signupMeeting.setCategory(selectedCategory);
            }
        } else {
            this.signupMeeting.setCategory(getCustomCategory());
        }
        //clear the category fields???
        this.selectedCategory = "";

        //set the creator/organiser
        this.signupMeeting.setCreatorUserId(creatorUserId);
        this.creatorUserId = "";

    }

    /**
     * This method is called by JSP page for adding/removing attachments action.
     * @return null.
     */
    public String addRemoveAttachments() {
        getAttachmentHandler().processAddAttachRedirect(this.signupMeeting.getSignupAttachments(), null, true);
        return null;
    }

    public String doCancelAction() {
        cleanUpUnusedAttachmentCopies(this.signupMeeting.getSignupAttachments());
        getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.COPY_MEETING);
        this.selectedLocation = null; //Reset selected option
        this.selectedCategory = null; //Reset selected option
        return ORGANIZER_MEETING_PAGE_URL;
    }

    /**
     * This is a ValueChange Listener to watch changes on the selection of
     * 'unlimited attendee' choice by user.
     *
     * @param vce
     *            a ValuechangeEvent object.
     * @return a outcome string.
     */
    public String processGroup(ValueChangeEvent vce) {
        Boolean changeValue = (Boolean) vce.getNewValue();
        if (changeValue != null) {
            unlimited = changeValue.booleanValue();
            if (unlimited) maxNumOfAttendees = 10;

        }

        return "";

    }

    /**
     * Modify the existing time slot blocks
     * @return String object for next page url
     */
    public String editUserDefTimeSlots() {
        if (this.customTimeSlotWrpList == null) {
            /*initialize when it comes from other meeting type*/
            this.customTimeSlotWrpList = getTimeslotWrappers();
            /*Mark the time slot sequence for recurring events changes issues*/
            markerTimeslots(this.customTimeSlotWrpList);

            getUserDefineTimeslotBean().init(this.signupMeeting, COPTY_MEETING_PAGE_URL, this.customTimeSlotWrpList, UserDefineTimeslotBean.COPY_MEETING);
        } else {
            if (!Utilities.isDataIntegritySafe(isUserDefinedTS(), UserDefineTimeslotBean.COPY_MEETING, getUserDefineTimeslotBean())) {
                return ORGANIZER_MEETING_PAGE_URL;
            }

            this.customTimeSlotWrpList = getUserDefineTimeslotBean().getDestTSwrpList();
            getUserDefineTimeslotBean().init(this.signupMeeting, COPTY_MEETING_PAGE_URL, this.customTimeSlotWrpList, UserDefineTimeslotBean.COPY_MEETING);
        }

        return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
    }

    private void prepareCopy(SignupMeeting meeting) throws Exception {

        meeting.setId(null);// to save as new meeting in db
        meeting.setRecurrenceId(null);

        meeting.getSignupSites().clear();
        meeting.getSignupSites().addAll(CreateSitesGroups.getSelectedSignupSites(currentSite, otherSites));

        this.allowedUserList = LoadAllowedUsers(meeting);

        List<SignupTimeslot> timeslots = meeting.getSignupTimeSlots();

        boolean lockOrCanceledTimeslot = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(meeting.getStartTime());

        /* Announcement type */
        if (getAnnouncementType() || timeslots == null || timeslots.isEmpty()) {
            calendar.add(Calendar.MINUTE, getTimeSlotDuration());
            meeting.setMeetingType(ANNOUNCEMENT);
            meeting.getSignupTimeSlots().clear();
        } else {
            List<SignupTimeslot> cpTimeslotList = new ArrayList<SignupTimeslot>();
            List<SignupTimeslot> origTsList = null;
            if (!isUserDefinedTS() && (meeting.getMeetingType().equals(INDIVIDUAL) || meeting.getMeetingType().equals(GROUP))) {
                origTsList = meeting.getSignupTimeSlots();

                SignupTimeslot origTs = null;

                for (int i = 0; i < getNumberOfSlots(); i++) {
                    SignupTimeslot cpTs = new SignupTimeslot();
                    int maxAttendees = (unlimited) ? SignupTimeslot.UNLIMITED : maxNumOfAttendees;
                    cpTs.setMaxNoOfAttendees(maxAttendees);
                    cpTs.setDisplayAttendees(showAttendeeName);
                    cpTs.setStartTime(calendar.getTime());
                    calendar.add(Calendar.MINUTE, getTimeSlotDuration());
                    cpTs.setEndTime(calendar.getTime());

                    /* pass attendees */
                    if (i < origTsList.size()) {
                        origTs = origTsList.get(i);
                        List<SignupAttendee> attList = origTs.getAttendees();
                        /* screening attendees */
                        removeNotAllowedAttedees(attList);

                        if (!unlimited && attList != null && attList.size() > maxAttendees) {
                            /* attendee may be truncated */
                            //this.truncateAttendee = true; validate by javaScript
                            for (int j = attList.size(); j > maxAttendees; j--)
                                attList.remove(j - 1);
                        }
                        cpTs.setAttendees(attList);
                        origTs.getAttendees().clear();
                        cpTs.setLocked(origTs.isLocked());
                        cpTs.setCanceled(origTs.isCanceled());
                        if (origTs.isCanceled() || origTs.isLocked()) lockOrCanceledTimeslot = true;

                    }
                    cpTimeslotList.add(cpTs);
                }
            }

            /*User defined time slots case*/
            if (meeting.getMeetingType().equals(CUSTOM_TIMESLOTS) || isUserDefinedTS()) {
                UserDefineTimeslotBean uBean = getUserDefineTimeslotBean();
                if (uBean == null || !uBean.COPY_MEETING.equals(uBean.getPlaceOrderBean())) {
                    throw new SignupUserActionException(MessageFormat.format(Utilities.rb.getString("you.have.multiple.tabs.in.browser"), new Object[]{getSakaiFacade().getServerConfigurationService().getServerName()}));
                }
                List<TimeslotWrapper> tsWrpList = uBean.getDestTSwrpList();
                if (tsWrpList != null) {
                    for (TimeslotWrapper wrapper : tsWrpList) {
                        SignupTimeslot slot = wrapper.getTimeSlot();

                        List<SignupAttendee> attList = slot.getAttendees();
                        /* screening attendees */
                        removeNotAllowedAttedees(attList);

                        if (attList != null && attList.size() > slot.getMaxNoOfAttendees()) {
                            /* attendee may be truncated */
                            for (int j = attList.size(); j > slot.getMaxNoOfAttendees(); j--)
                                attList.remove(j - 1);
                        }

                        if (slot.isLocked() || slot.isCanceled()) lockOrCanceledTimeslot = true;

                        cpTimeslotList.add(slot);
                    }
                }

                /*for end time purpose*/
                int duration = getUserDefineTimeslotBean().getEventDuration();
                calendar.add(Calendar.MINUTE, duration);
            }


            meeting.getSignupTimeSlots().clear();
            meeting.getSignupTimeSlots().addAll(cpTimeslotList);

            if (lockOrCanceledTimeslot)
                Utilities.addErrorMessage(Utilities.rb.getString("warning.some_timeslot_may_locked_canceled"));
        }

        meeting.setEndTime(calendar.getTime());

        /* setup signup begin / deadline */
        setSignupBeginDeadlineData(meeting, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(), getDeadlineTimeType());

        // copySites(meeting);

        /*Remove the coordinates who are not in the meeting any more due to the site group changes
         * we are simplify and just copy over coordinators over and user can change it via modify meeting page*/
        //TODO later we may add the coordinators ability in the copy page too and need ajax to the trick.
        meeting.setCoordinatorIds(getValidatedMeetingCoordinators(meeting));

    }

    /**
     * This method is called to get all locations to populate the dropdown
     *
     * @return list of allLocations
     */
    public List<SelectItem> getAllLocations() {
        if (locations == null) {
            locations = new ArrayList<SelectItem>();
            locations.addAll(Utilities.getSignupMeetingsBean().getAllLocations());
            locations.add(0, new SelectItem(Utilities.rb.getString("select_location")));
        }
        return locations;
    }

    /**
     * This method is called to get all categories to populate the dropdown
     *
     * @return list of categories
     */
    public List<SelectItem> getAllCategories() {
        if (categories == null) {
            categories = new ArrayList<SelectItem>();
            categories.addAll(Utilities.getSignupMeetingsBean().getAllCategories());
            //remove option 'All'
            categories.remove(0);
            categories.add(0, new SelectItem(Utilities.rb.getString("select_category")));
        }
        return categories;
    }

    /**
     * This is getter method for UI
     * @return a String for the meeting id
     */
    public String getSignupMeetingId() {
        String rv = "";
        if (signupMeeting != null) {
            Long id = signupMeeting.getId();
            if (id != null) {
                rv = id.toString();
            }
        }
        return rv;
    }


    public String getCreatorUserId() {
        if (this.creatorUserId == null) {
            //set current user as default meeting organizer if case people forget to select one
            return sakaiFacade.getCurrentUserId();
        }
        return creatorUserId;
    }

    /**
     * This is a getter method for UI.
     *
     * @return a HtmlInputHidden object.
     */
    public int getTimeSlotDuration() {
        long duration = (getSignupMeeting().getEndTime().getTime() - getSignupMeeting().getStartTime().getTime()) / (MINUTE_IN_MILLISEC * getNumberOfSlots());
        return (int) duration;
    }

	/*public void setTimeSlotDuration(int timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
	}*/

    private void initializeSitesGroups() {
        /*
         * Temporary bug fix for AuthZ code ( isAllowed(..) ), which gives wrong
         * permission for the first time at 'Create new or Copy meeting pages'.
         * The bug will be gone by second time go into it. Once it's fixed,
         * remove this below and other places and make it into a more clean way
         * by not sharing the same CreateSitesGroups Object. new
         * CreateSitesGroups(getSignupMeeting(),sakaiFacade,signupMeetingService);
         */
        CreateSitesGroups createSiteGroups = Utilities.getSignupMeetingsBean().getCreateSitesGroups();
        createSiteGroups.resetSiteGroupCheckboxMark();
        createSiteGroups.setSignupMeeting(this.getSignupMeeting());
        createSiteGroups.processSiteGroupSelectionMarks();
        setCurrentSite(createSiteGroups.getCurrentSite());
        setOtherSites(createSiteGroups.getOtherSites());
        setMissingSitGroupWarning(createSiteGroups.isSiteOrGroupTruncated());
        setMissingSites(createSiteGroups.getMissingSites());
        setMissingGroups(createSiteGroups.getMissingGroups());
    }

    private List<SignupUser> LoadAllowedUsers(SignupMeeting meeting) {
        return sakaiFacade.getAllUsers(getSignupMeeting());
    }

    private void removeNotAllowedAttedees(List<SignupAttendee> screenAttendeeList) {
        if (screenAttendeeList == null || screenAttendeeList.isEmpty()) return;

        boolean notFound = true;
        for (int i = screenAttendeeList.size(); i > 0; i--) {
            notFound = true;
            for (SignupUser allowedOne : allowedUserList) {
                if (allowedOne.getInternalUserId().equals(screenAttendeeList.get(i - 1).getAttendeeUserId())) {
                    notFound = false;
                    break;
                }
            }
            if (notFound) {
                screenAttendeeList.remove(i - 1);
            }
        }
    }

    private String getValidatedMeetingCoordinators(SignupMeeting meeting) {
        List<String> allCoordinatorIds = meeting.getCoordinatorIdsList();
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String couId : allCoordinatorIds) {
            if (this.sakaiFacade.hasPermissionToCreate(meeting, couId)) {
                if (isFirst) {
                    sb.append(couId);
                    isFirst = false;
                } else {
                    //safeguard -db column max size, hardly have over 10 coordinators per meeting
                    if (sb.length() < 1000) sb.append("|" + couId);
                }
            }
        }

        return sb.length() < 1 ? null : sb.toString();
    }

    private void setMissingSitGroupWarning(boolean missingSitGroupWarning) {
        this.missingSitGroupWarning = missingSitGroupWarning;
    }

    private void setMissingSites(List<String> missingSites) {
        this.missingSites = missingSites;
    }

    /**
     * It's a getter method for UI.
     *
     * @return a boolean value
     */
    public boolean isMissingSitesThere() {
        if (this.missingSites == null || this.missingSites.isEmpty()) return false;
        return true;
    }

    private void setMissingGroups(List<String> missingGroups) {
        this.missingGroups = missingGroups;
    }

    public boolean isMissingGroupsThere() {
        if (this.missingGroups == null || this.missingGroups.isEmpty()) return false;
        return true;
    }

    /**
     * It's a getter method for UI
     *
     * @return a list of SelectItem objects for radio buttons.
     */
    public List<SelectItem> getMeetingTypeRadioBttns() {
        return Utilities.getMeetingTypeSelectItems(getSignupMeeting().getMeetingType(), true);
    }

    private void prepareRecurredEvents() {
        Long recurrenceId = this.signupMeeting.getRecurrenceId();
        if (recurrenceId != null && recurrenceId.longValue() > 0) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(this.signupMeeting.getStartTime());
            /*backward to one month and make sure we could get some recurrence events
             * if it's not the only one existed
             * */
            cal.add(Calendar.HOUR, -24 * 31);
            List<SignupMeeting> recurredMeetings = signupMeetingService.getRecurringSignupMeetings(getSakaiFacade().getCurrentLocationId(), getSakaiFacade().getCurrentUserId(), recurrenceId, cal.getTime());
            retrieveRecurrenceData(recurredMeetings);
        }
    }

    /*This method only provide a most possible repeatType, not with 100% accuracy*/
    private void retrieveRecurrenceData(List<SignupMeeting> upTodateOrginMeetings) {
        /*to see if the recurring events have a 'Start_Now' type already*/
        if (Utilities.testSignupBeginStartNowType(upTodateOrginMeetings)) {
            setSignupBeginsType(START_NOW);//overwrite previous value
            setSignupBegins(6);//default value; not used
        }

        Date lastDate = new Date();
        if (upTodateOrginMeetings == null || upTodateOrginMeetings.isEmpty()) return;

        /*if this is the last one*/
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.signupMeeting.getStartTime());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        setRepeatUntil(cal.getTime());

        int listSize = upTodateOrginMeetings.size();
        if (listSize > 1) {
            /*set last recurred Date for recurrence events*/
            lastDate = upTodateOrginMeetings.get(listSize - 1).getStartTime();
            cal.setTime(lastDate);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            setRepeatUntil(cal.getTime());

            String repeatType = upTodateOrginMeetings.get(listSize - 1).getRepeatType();
            if (repeatType != null && !ONCE_ONLY.equals(repeatType)) {
                setRepeatType(repeatType);
                setRepeatTypeUnknown(false);
                return;
            }

            /*The following code is to make it old version backward compatible
             * It will be cleaned after a while.
             */
            Calendar calFirst = Calendar.getInstance();
            Calendar calSecond = Calendar.getInstance();
            /*The following code is to make it old version backward compatible*/

            /*
             * we can only get approximate estimation by assuming it's a
             * succession. take the last two which are more likely be in a sequence
             */
            calFirst.setTime(upTodateOrginMeetings.get(listSize - 2).getStartTime());
            calFirst.set(Calendar.SECOND, 0);
            calFirst.set(Calendar.MILLISECOND, 0);
            calSecond.setTime(upTodateOrginMeetings.get(listSize - 1).getStartTime());
            calSecond.set(Calendar.SECOND, 0);
            calSecond.set(Calendar.MILLISECOND, 0);
            int tmp = calSecond.get(Calendar.DATE);
            int daysDiff = (int) (calSecond.getTimeInMillis() - calFirst.getTimeInMillis()) / DAY_IN_MILLISEC;
            setRepeatTypeUnknown(false);
            if (daysDiff == perDay)//could have weekdays get into this one, not very accurate.
                setRepeatType(DAILY);
            else if (daysDiff == perWeek) setRepeatType(WEEKLY);
            else if (daysDiff == perBiweek) setRepeatType(BIWEEKLY);
            else if (daysDiff == 3 && calFirst.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) setRepeatType(WEEKDAYS);
            else {
                /*case:unknown repeatType*/
                setRepeatTypeUnknown(true);
            }
        }
    }

    private void assignMainAttachmentsCopyToSignupMeeting() {
        List<SignupAttachment> attachList = new ArrayList<SignupAttachment>();
        if (attachList != null) {
            for (SignupAttachment attach : this.signupMeeting.getSignupAttachments()) {
                if (attach.getTimeslotId() == null && attach.getViewByAll()) attachList.add(attach);

                //TODO Later: how about time slot attachment?.
            }
        }

        List<SignupAttachment> cpList = new ArrayList<SignupAttachment>();
        if (attachList.size() > 0) {
            for (SignupAttachment attach : attachList) {
                cpList.add(getAttachmentHandler().copySignupAttachment(this.signupMeeting, true, attach, ATTACH_COPY + this.signupMeeting.getId().toString()));
            }
        }

        this.signupMeeting.getSignupAttachments().clear();
        this.signupMeeting.getSignupAttachments().addAll(cpList);
    }

    /*Overwrite default one*/
    public boolean getSignupAttachmentEmpty() {
        if (this.signupMeeting == null) return true;

        if (this.signupMeeting.getSignupAttachments() == null || this.signupMeeting.getSignupAttachments().isEmpty())
            return true;
        else return false;
    }

    /**
     * This is only for UI purpose to check if the event/meeting is an
     * custom-ts style (manay time slots) and it requires signup.
     */
    public boolean getCustomTsType() {
        return CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType());
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
        return Utilities.getSignupMeetingsBean().getInstructors(signupMeeting);
    }

}
