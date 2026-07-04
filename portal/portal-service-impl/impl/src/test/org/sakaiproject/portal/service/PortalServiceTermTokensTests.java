/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.portal.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.PortalConstants;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.test.SakaiTests;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PortalTestConfiguration.class})
public class PortalServiceTermTokensTests extends SakaiTests {

    @Autowired private CourseManagementService courseManagementService;
    @Autowired private LocaleService localeService;
    @Autowired private PortalService portalService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private UserTimeService userTimeService;

    private static final TimeZone TZ = TimeZone.getTimeZone("US/Eastern");

    @Before
    public void setup() {
        super.setup();
        when(userTimeService.getLocalTimeZone()).thenReturn(TZ);
        when(serverConfigurationService.getString(PortalConstants.PROP_SERVICE_NAME, "Sakai")).thenReturn("Test University");
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(java.util.Locale.US);
    }

    private void setTermTokensEnabled(boolean enabled) {
        when(serverConfigurationService.getBoolean(PortalConstants.PROP_TERM_TOKENS_ENABLED, false)).thenReturn(enabled);
    }

    private Site siteWithTerm(String termEid) {
        Site site = mock(Site.class);
        ResourceProperties props = new BaseResourceProperties();
        if (termEid != null) props.addProperty(Site.PROP_SITE_TERM_EID, termEid);
        props.addProperty(Site.PROP_SITE_CONTACT_NAME, "Instructor Dave");
        props.addProperty(Site.PROP_SITE_CONTACT_EMAIL, "dave@example.edu");
        when(site.getProperties()).thenReturn(props);
        when(site.getId()).thenReturn("site-" + termEid);
        when(site.getTitle()).thenReturn("SMPL101");
        when(site.getUrl()).thenReturn("http://localhost/portal/site/site-" + termEid);
        return site;
    }

    private AcademicSession session(String eid, String title, Date start, Date end) {
        AcademicSession session = mock(AcademicSession.class);
        when(session.getEid()).thenReturn(eid);
        when(session.getTitle()).thenReturn(title);
        when(session.getStartDate()).thenReturn(start);
        when(session.getEndDate()).thenReturn(end);
        return session;
    }

    private Date date(int year, int month, int day) {
        Calendar cal = Calendar.getInstance(TZ);
        cal.clear();
        cal.set(year, month - 1, day);
        return cal.getTime();
    }

    @Test
    public void disabledReturnsEmpty() {
        setTermTokensEnabled(false);
        Assert.assertEquals("", portalService.getTermTokensScript(siteWithTerm("OFF1")));
    }

    @Test
    public void nullSiteReturnsEmpty() {
        setTermTokensEnabled(true);
        Assert.assertEquals("", portalService.getTermTokensScript(null));
    }

    @Test
    public void siteTermEmitsAllFields() {
        setTermTokensEnabled(true);
        Date start = date(2026, 9, 1);
        Date end = date(2027, 1, 1);
        AcademicSession fall = session("FA26", "Fall 2026", start, end);
        when(courseManagementService.getAcademicSession("FA26")).thenReturn(fall);
        when(courseManagementService.getCurrentAcademicSessions()).thenReturn(Collections.emptyList());
        when(userTimeService.dateFormat(any(Date.class), any(), anyInt())).thenAnswer(inv -> {
            Date d = inv.getArgument(0);
            int format = inv.getArgument(2);
            if (d.equals(start)) return format == java.text.DateFormat.SHORT ? "9/1/26" : "September 1, 2026";
            if (d.equals(end)) return format == java.text.DateFormat.SHORT ? "1/1/27" : "January 1, 2027";
            return "TODAY";
        });

        String script = portalService.getTermTokensScript(siteWithTerm("FA26"));

        Assert.assertTrue(script.contains("sakai.termsInfo = {"));
        Assert.assertTrue(script.contains("\"siteTitle\": \"SMPL101\""));
        Assert.assertTrue(script.contains("\"siteTerm\": \"Fall 2026\""));
        Assert.assertTrue(script.contains("\"siteTermShort\": \"FA26\""));
        Assert.assertTrue(script.contains("\"termStart\": \"September 1, 2026\""));
        // escapeEcmaScript escapes the slashes in short dates; the JS value is unchanged
        Assert.assertTrue(script.contains("\"termStartShort\": \"9\\/1\\/26\""));
        Assert.assertTrue(script.contains("\"termEnd\": \"January 1, 2027\""));
        Assert.assertTrue(script.contains("\"termEndShort\": \"1\\/1\\/27\""));
        Assert.assertTrue(script.contains("\"termStartIso\": \"2026-09-01\""));
        Assert.assertTrue(script.contains("\"termEndIso\": \"2027-01-01\""));
        Assert.assertTrue(script.contains("\"instructor\": \"Instructor Dave\""));
        Assert.assertTrue(script.contains("\"instructorEmail\": \"dave@example.edu\""));
        Assert.assertTrue(script.contains("\"institution\": \"Test University\""));
        Assert.assertTrue(script.contains("\"siteUrl\": "));
        Assert.assertTrue(script.contains("\"today\": \"TODAY\""));
        Assert.assertTrue(script.contains("sakai.editor.enableTermTokens = true;"));
        // end date is in the future relative to any plausible test run before 2027
        Assert.assertTrue(script.contains("\"daysLeftInTerm\": \""));
        Assert.assertTrue(script.contains("\"termYear\": \"2026\""));
        // 2026-09-01 .. 2027-01-01 is 123 calendar days -> 18 weeks
        Assert.assertTrue(script.contains("\"weeksInTerm\": \"18\""));
        Assert.assertTrue(script.contains("\"currentYear\": \""));
        Assert.assertTrue(script.contains("\"currentMonth\": \""));
        Assert.assertTrue(script.contains("\"dayOfWeek\": \""));
    }

    @Test
    public void nextTermAndViewerFieldsEmitted() {
        setTermTokensEnabled(true);
        Date futureStart = new Date(new Date().getTime() + 100L * 24 * 60 * 60 * 1000);
        AcademicSession next = session("NEXT1", "Winter 2027", futureStart, null);
        when(courseManagementService.getAcademicSession("NX1")).thenThrow(new IdNotFoundException("NX1", "AcademicSession"));
        when(courseManagementService.getAcademicSessions()).thenReturn(Collections.singletonList(next));
        when(courseManagementService.getCurrentAcademicSessions()).thenReturn(Collections.emptyList());
        when(userTimeService.dateFormat(any(Date.class), any(), anyInt())).thenReturn("A Date");
        org.sakaiproject.user.api.User viewer = mock(org.sakaiproject.user.api.User.class);
        when(viewer.getFirstName()).thenReturn("Ada");
        when(viewer.getLastName()).thenReturn("Lovelace");
        when(viewer.getDisplayName()).thenReturn("Ada Lovelace");
        when(viewer.getEmail()).thenReturn("ada@example.edu");
        when(userDirectoryService.getCurrentUser()).thenReturn(viewer);

        String script = portalService.getTermTokensScript(siteWithTerm("NX1"));

        Assert.assertTrue(script.contains("\"nextTerm\": \"Winter 2027\""));
        Assert.assertTrue(script.contains("\"nextTermStart\": \"A Date\""));
        Assert.assertTrue(script.contains("\"firstName\": \"Ada\""));
        Assert.assertTrue(script.contains("\"lastName\": \"Lovelace\""));
        Assert.assertTrue(script.contains("\"fullName\": \"Ada Lovelace\""));
        Assert.assertTrue(script.contains("\"userEmail\": \"ada@example.edu\""));
    }

    @Test
    public void valuesAreEcmaScriptEscaped() {
        setTermTokensEnabled(true);
        AcademicSession tricky = session("ESC1", "Fall \"26\" </script>", null, null);
        when(courseManagementService.getAcademicSession("ESC1")).thenReturn(tricky);
        when(courseManagementService.getCurrentAcademicSessions()).thenReturn(Collections.emptyList());

        String script = portalService.getTermTokensScript(siteWithTerm("ESC1"));

        Assert.assertFalse(script.contains("Fall \"26\""));
        Assert.assertFalse(script.contains("</script>"));
        Assert.assertTrue(script.contains("Fall \\\"26\\\""));
    }

    @Test
    public void unknownTermFallsBackToCurrentSession() {
        setTermTokensEnabled(true);
        AcademicSession summer = session("NOW1", "Summer 2026", date(2026, 6, 1), date(2026, 9, 1));
        when(courseManagementService.getAcademicSession("GONE1")).thenThrow(new IdNotFoundException("GONE1", "AcademicSession"));
        when(courseManagementService.getCurrentAcademicSessions()).thenReturn(Collections.singletonList(summer));

        String script = portalService.getTermTokensScript(siteWithTerm("GONE1"));

        Assert.assertFalse(script.contains("siteTerm"));
        Assert.assertTrue(script.contains("\"currentTerm\": \"Summer 2026\""));
    }

    @Test
    public void sessionEndingTodayIsStillCurrent() {
        setTermTokensEnabled(true);
        Calendar cal = Calendar.getInstance(TZ);
        Date endOfTermMidnight = date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        long day = 24L * 60 * 60 * 1000;
        AcademicSession endingToday = session("LAST1", "Ending Term", new Date(new Date().getTime() - 100L * day), endOfTermMidnight);
        // newer-started but already over; the flagged list is ordered newest first, so the
        // get(0) fallback would wrongly pick this one if the date-covering check misfires
        AcademicSession alreadyOver = session("MINI1", "Short Term", new Date(new Date().getTime() - 10L * day), new Date(new Date().getTime() - 2L * day));
        when(courseManagementService.getCurrentAcademicSessions()).thenReturn(java.util.Arrays.asList(alreadyOver, endingToday));

        String script = portalService.getTermTokensScript(siteWithTerm(null));

        // a date-only end date is midnight; the term is still current for its whole final day
        Assert.assertTrue(script.contains("\"currentTerm\": \"Ending Term\""));
    }

    @Test
    public void currentTermPrefersSessionCoveringToday() {
        setTermTokensEnabled(true);
        Date now = new Date();
        Date past = new Date(now.getTime() - 200L * 24 * 60 * 60 * 1000);
        Date future = new Date(now.getTime() + 200L * 24 * 60 * 60 * 1000);
        AcademicSession ended = session("OLD1", "Old Term", past, new Date(now.getTime() - 100L * 24 * 60 * 60 * 1000));
        AcademicSession active = session("CUR1", "Active Term", past, future);
        // flagged list is ordered newest first; the ended term sorts first here
        when(courseManagementService.getCurrentAcademicSessions()).thenReturn(java.util.Arrays.asList(ended, active));

        String script = portalService.getTermTokensScript(siteWithTerm(null));

        Assert.assertTrue(script.contains("\"currentTerm\": \"Active Term\""));
    }
}
