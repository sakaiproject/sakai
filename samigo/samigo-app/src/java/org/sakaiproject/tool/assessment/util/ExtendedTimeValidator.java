/**
 * Copyright (c) 2005-2020 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * Centralized extended time validator:
 * - name or group must be supplied
 * - due date can't be before now, or before start date
 * - retract date can't be before now, or before start date
 * - retract date can't be before due date; auto pushed to due date
 * - due date can't be the same as start date
 * - open window can't be less than time limit
 *
 * @author bjones86
 */
public class ExtendedTimeValidator
{
    public static final String ERROR_KEY_USER_OR_GROUP_NOT_SET        = "extended_time_user_and_group_set";
    public static final String ERROR_KEY_DUE_BEFORE_START             = "extended_time_due_earlier_than_available";
    public static final String ERROR_KEY_RETRACT_BEFORE_START         = "extended_time_retract_earlier_than_available";
    public static final String ERROR_KEY_DUE_SAME_AS_START            = "extended_time_due_same_as_available";
    public static final String ERROR_KEY_OPEN_WINDOW_LESS_THAN_LIMIT  = "extended_time_open_window_less_than_time_limit";
    public static final String ERROR_KEY_USER_SUBSTRING               = "extended_time_error_user";
    public static final String ERROR_KEY_GROUP_SUBSTRING              = "extended_time_error_group";

    public static final String MSG_KEY_AND            = "extended_time_and";
    public static final String MSG_KEY_DUP_USERS      = "extended_time_duplicate_users";
    public static final String MSG_KEY_DUP_GROUPS     = "extended_time_duplicate_groups";
    public static final String MSG_KEY_NAME_NOT_FOUND = "extended_time_name_not_found";

    public static final String ASSESSMENT_SETTINGS_BUNDLE = "org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages";

    private static Object settingsBean;

    /**
     * Validate a single {@link ExtendedTime} entry.
     * @param entry the {@link ExtendedTime} entry to validate
     * @param context The {@link FacesContext}, in case validation errors are generated
     * @param settings The SettingsBean, for quiz settings (either {@link AssessmentSettingsBean} or {@link PublishedAssessmentSettingsBean})
     * @return true if all entities are valid; false otherwise.
     */
    public static boolean validateEntry( ExtendedTime entry, FacesContext context, Object settings )
    {
        return validateEntries( Collections.singletonList( entry ), context , settings );
    }

    /**
     * Validate a list of {@link ExtendedTime} entries.
     * @param entries The {@link ExtendedTime} entries to validate
     * @param context The {@link FacesContext}, in case validation errors are generated
     * @param settings The SettingsBean, for quiz settings (either {@link AssessmentSettingsBean} or {@link PublishedAssessmentSettingsBean})
     * @return true if all entities are valid; false otherwise.
     */
    public static boolean validateEntries( List<ExtendedTime> entries, FacesContext context, Object settings )
    {
        boolean valid = true;
        settingsBean = settings;
        List<String> users = new ArrayList<>( entries.size() );
        List<String> groups = new ArrayList<>( entries.size() );
        for( ExtendedTime entry : entries )
        {
            String user = entry.getUser();
            String group = entry.getGroup();
            Date startDate = entry.getStartDate();
            Date dueDate = entry.getDueDate();
            Date retractDate = entry.getRetractDate();

            if( StringUtils.isNotEmpty( user ) )
            {
                users.add( user );
            }

            if( StringUtils.isNotEmpty( group ) )
            {
                groups.add( group );
            }

            // Name & group validation
            if( StringUtils.isBlank( user ) && StringUtils.isBlank( group ) )
            {
                String errorMsg = getError( ERROR_KEY_USER_OR_GROUP_NOT_SET, entry );
                context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorMsg, null ) );
                valid = false;
            }

            // Due date can't be before now, or before start date
            if( (startDate != null && dueDate != null && dueDate.before( startDate ))
                || (startDate == null && dueDate != null && dueDate.before( new Date() )) )
            {
                String errorMsg = getError( ERROR_KEY_DUE_BEFORE_START, entry );
                context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorMsg, null ) );
                entry.setStartDate( new Date() );
                valid = false;
            }

            boolean isEntryRetractEarlierThanAvailable = false;
            if( StringUtils.equals( getLateHandling(), AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString() ) )
            {
                // Retract date can't be before now, or before start date
                if( (retractDate != null && startDate != null && retractDate.before( startDate ))
                    || (retractDate != null && startDate == null && retractDate.before( new Date() )) )
                {
                    String errorMsg = getError( ERROR_KEY_RETRACT_BEFORE_START, entry );
                    context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorMsg, null ) );
                    entry.setStartDate( new Date() );
                    isEntryRetractEarlierThanAvailable = true;
                    valid = false;
                }

                // Retract date can't be before due date; push it to the due date
                if( !isEntryRetractEarlierThanAvailable && (retractDate != null && dueDate != null && retractDate.before( dueDate )) )
                {
                    entry.setRetractDate( dueDate );
                }
            }

            // Due date can't be the same as start date
            if( dueDate != null && startDate != null && dueDate.equals( startDate ) )
            {
                String errorMsg = getError( ERROR_KEY_DUE_SAME_AS_START, entry );
                context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorMsg, null ) );
                valid = false;
            }

            // If time limit is set, ensure open window is not less than the time limit
            boolean hasTimer = (entry.getTimeHours() != null && entry.getTimeHours() > 0) || (entry.getTimeMinutes() != null && entry.getTimeMinutes() > 0);
            if (hasTimer)
            {
                Date due = entry.getRetractDate() != null ? entry.getRetractDate() : entry.getDueDate();
                long timerMinutes = 0;
                if( entry.getTimeHours() != null )
                {
                    timerMinutes += entry.getTimeHours().longValue() * 60;
                }
                if( entry.getTimeMinutes() != null )
                {
                    timerMinutes += entry.getTimeMinutes().longValue();
                }

                long openWindowMinutes = (due.getTime() - entry.getStartDate().getTime()) / 1000 / 60;
                if( openWindowMinutes < timerMinutes )
                {
                    String errorString = getError( ERROR_KEY_OPEN_WINDOW_LESS_THAN_LIMIT, entry );
                    context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorString, null ) );
                    valid = false;
                }
            }
        }

        // Check for duplicate users
        Set<String> duplicateUsers = findDuplicates(users);
        if( !duplicateUsers.isEmpty() )
        {
            String dupUsers = "";
            int count = 0;
            int end = users.size();
            for( String entry : duplicateUsers )
            {
                if( count == 0 )
                {
                    dupUsers = "'" + getUserName( entry ) + "'";
                }
                else if( count < (end - 1) )
                {
                    dupUsers = dupUsers + ", '" + getUserName( entry ) + "'";
                }
                else
                {
                    String and = ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, MSG_KEY_AND );
                    dupUsers = dupUsers + ", " + and + " '" + getUserName( entry );
                }

                count++;
            }

            String errorMsg = ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, MSG_KEY_DUP_USERS );
            errorMsg = MessageFormat.format( errorMsg, new Object[] { dupUsers } );
            context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorMsg, null ) );
            valid = false;
        }

        // Check for duplicate groups
        Set<String> duplicateGroups = findDuplicates(groups);
        if( !duplicateGroups.isEmpty() )
        {
            String dupGroups = "";
            int count = 0;
            int end = groups.size();
            for( String entry : duplicateGroups )
            {
                if( count == 0 )
                {
                    dupGroups = "'" + getGroupName( entry ) + "'";
                }
                else if( count < (end - 1) )
                {
                    dupGroups = dupGroups + ", '" + getGroupName( entry ) + "'";
                }
                else
                {
                    String and = ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, MSG_KEY_AND );
                    dupGroups = dupGroups + ", " + and + " '" + getGroupName( entry );
                }

                count++;
            }

            String errorMsg = ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, MSG_KEY_DUP_GROUPS );
            errorMsg = MessageFormat.format( errorMsg, new Object[] { dupGroups } );
            context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorMsg, null ) );
            valid = false;
        }

        return valid;
    }

    /**
     * Get an extended time error message for the UI.
     * @param key the key of the extended time error message requested
     * @param entry the {@link ExtendedTime} entry which generated the error
     * @return the error message requested with parameter substitution
     */
    private static String getError( String key, ExtendedTime entry )
    {
        String errorString = ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, key );
        String replacement = "";
        String user = getUserName( entry.getUser() );
        String group = getGroupName( entry.getGroup() );

        boolean hasUser = false;
        if( StringUtils.isNotBlank( user ) )
        {
            replacement += ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, ERROR_KEY_USER_SUBSTRING );
            replacement = MessageFormat.format( replacement, new Object[] { user } );
            hasUser = true;
        }
        if( StringUtils.isNotBlank( group ) )
        {
            if( hasUser )
            {
                replacement += " " + ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, MSG_KEY_AND ) + " ";
            }

            replacement += ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, ERROR_KEY_GROUP_SUBSTRING );
            replacement = MessageFormat.format( replacement, new Object[] { group } );
        }

        errorString = MessageFormat.format( errorString, new Object[] { replacement } );
        return errorString;
    }

    /**
     * Gets a user name from a user ID and a SettingsBean.
     * @param userId the user ID in question
     * @return the name of the user ID supplied
     * @author Leonardo Canessa<masterbob+github@gmail.com>
     */
    private static String getUserName( String userId )
    {
        return getName( userId, getUsersInSite() );
    }

    /**
     * Gets a group name from a group ID and a SettingsBean.
     * @param groupId the group ID in question
     * @return the name of the group ID supplied
     * @author Leonardo Canessa<masterbob+github@gmail.com>
     */
    private static String getGroupName( String groupId )
    {
        return getName( groupId, getGroupsForSite() );
    }

    /**
     * Utility function for {@link #getGroupName(String)} and {@link #getUserName(String)}
     * @param parameter the param to search for in the array of entries
     * @param entries the array of entries to search
     * @return the entry from the array with matches the parameter passed
     * @author Leonardo Canessa<masterbob+github@gmail.com>
     */
    private static String getName( String parameter, SelectItem[] entries )
    {
        if( parameter == null || parameter.isEmpty() )
        {
            return "";
        }

        for( SelectItem item : entries )
        {
            if( item.getValue().equals( parameter ) )
            {
                return item.getLabel();
            }
        }

        return ContextUtil.getLocalizedString( ASSESSMENT_SETTINGS_BUNDLE, MSG_KEY_NAME_NOT_FOUND );
    }

    /**
     * Find and return a {@link Set} of duplicates from the provided {@link List}
     * @param list the {@link List} to scan for duplicates
     * @return a {@link Set} containing any duplicates from the provided {@link List}
     * @author Leonardo Canessa<masterbob+github@gmail.com>
     */
    private static Set<String> findDuplicates( List<String> list )
    {
        final Set<String> setToReturn = new HashSet<>();
        final Set<String> set1 = new HashSet<>();

        for( String value : list )
        {
            if( !set1.add( value ) )
            {
                setToReturn.add( value );
            }
        }

        return setToReturn;
    }

    /**
     * Get the quiz's late handling setting from the appropriate SettingsBean.
     * @return the quiz's late handling setting
     */
    private static String getLateHandling()
    {
        if( settingsBean instanceof PublishedAssessmentSettingsBean )
        {
            return ((PublishedAssessmentSettingsBean) settingsBean).getLateHandling();
        }
        else if( settingsBean instanceof AssessmentSettingsBean )
        {
            return ((AssessmentSettingsBean) settingsBean).getLateHandling();
        }

        return "";
    }

    /**
     * Get the groups in the site from the appropriate SettingsBean.
     * @return an array of {@link SelectItem} objects which contain the groups in the site
     */
    private static SelectItem[] getGroupsForSite()
    {
        if( settingsBean instanceof PublishedAssessmentSettingsBean )
        {
            return ((PublishedAssessmentSettingsBean) settingsBean).getGroupsForSite();
        }
        else if( settingsBean instanceof AssessmentSettingsBean )
        {
            return ((AssessmentSettingsBean) settingsBean).getGroupsForSite();
        }

        return new SelectItem[]{};
    }

    /**
     * Get the users in the site from the appropriate SettingsBean.
     * @return an array of {@link SelectItem} objects which contain the users in the site
     */
    private static SelectItem[] getUsersInSite()
    {
        if( settingsBean instanceof PublishedAssessmentSettingsBean )
        {
            return ((PublishedAssessmentSettingsBean) settingsBean).getUsersInSite();
        }
        else if( settingsBean instanceof AssessmentSettingsBean )
        {
            return ((AssessmentSettingsBean) settingsBean).getUsersInSite();
        }

        return new SelectItem[]{};
    }
}
