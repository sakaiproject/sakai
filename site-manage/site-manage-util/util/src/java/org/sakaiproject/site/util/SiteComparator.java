/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
 
package org.sakaiproject.site.util;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * The comparator to be used in Worksite Setup/Site Info tool
 */
@Slf4j
public class SiteComparator implements Comparator {

	Collator collator = Collator.getInstance();
	Collator localeCollator = null;
	
	/**
	 * the criteria
	 */
	String m_criterion = null;
	Locale m_loc = null;
	String m_asc = null;

	/**
	 * constructor
	 * 
	 * @param criteria
	 *            The sort criteria string
	 * @param asc
	 *            The sort order string. TRUE_STRING if ascending; "false"
	 *            otherwise.
	 */
	public SiteComparator(String criterion, String asc) {
		m_criterion = criterion;
		m_asc = asc;

	} // constructor

	

        // create a locale-sensitive comparator; on error keep localeCollator set to null so it's not used 
	
        public SiteComparator(String criterion, String asc, Locale locale) {
	
                this(criterion, asc);
	
                m_loc = locale;
	
                try {
	
        	RuleBasedCollator defaultCollator = (RuleBasedCollator) Collator.getInstance(locale); 
	
               String rules = defaultCollator.getRules();
	
               localeCollator = new RuleBasedCollator(rules.replaceAll("<'\u005f'", "<' '<'\u005f'"));
	
               localeCollator.setStrength(Collator.TERTIARY);
	
                } catch (Exception e) {
	
                	log.warn("SiteComparator failed to create RuleBasedCollator for locale " + locale.toString(), e);
                	localeCollator = null;
	
                }
	
        }	
	
	
	/**
	 * implementing the Comparator compare function
	 * 
	 * @param o1
	 *            The first object
	 * @param o2
	 *            The second object
	 * @return The compare result. 1 is o1 < o2; -1 otherwise
	 */
	public int compare(Object o1, Object o2) {
		int result = -1;

		if (m_criterion == null) {
			m_criterion = SiteConstants.SORTED_BY_TITLE;
		}

		/** *********** for sorting site list ****************** */
		if (m_criterion.equals(SiteConstants.SORTED_BY_TITLE)) {
			// sorted by the worksite title
			String s1 = ((Site) o1).getTitle();
			String s2 = ((Site) o2).getTitle();
			result = compareString(s1, s2);
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_DESCRIPTION)) {

			// sorted by the site short description
			String s1 = ((Site) o1).getShortDescription();
			String s2 = ((Site) o2).getShortDescription();
			result = compareString(s1, s2);
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_TYPE)) {
			// sorted by the site type
			String s1 = ((Site) o1).getType();
			String s2 = ((Site) o2).getType();
			result = compareString(s1, s2);
		} else if (m_criterion.equals(SortType.CREATED_BY_ASC.toString())) {
			// sorted by the site creator
			String s1 = ((Site) o1).getProperties().getProperty(
					"CHEF:creator");
			String s2 = ((Site) o2).getProperties().getProperty(
					"CHEF:creator");
			result = compareString(s1, s2);
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_STATUS)) {
			// sort by the status, published or unpublished
			int i1 = ((Site) o1).isPublished() ? 1 : 0;
			int i2 = ((Site) o2).isPublished() ? 1 : 0;
			if (i1 > i2) {
				result = 1;
			} else {
				result = -1;
			}
			
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_JOINABLE)) {
			// sort by whether the site is joinable or not
			boolean b1 = ((Site) o1).isJoinable();
			boolean b2 = ((Site) o2).isJoinable();
			result = compareBoolean(b1, b2);
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_PARTICIPANT_NAME)) {
			// sort by whether the site is joinable or not
			String s1 = null;
			if (o1.getClass().equals(Participant.class)) {
				s1 = ((Participant) o1).getName();
			}

			String s2 = null;
			if (o2.getClass().equals(Participant.class)) {
				s2 = ((Participant) o2).getName();
			}
			
			result = compareString(s1, s2);

		} else if (m_criterion.equals(SiteConstants.SORTED_BY_PARTICIPANT_UNIQNAME)) {
			// sort by whether the site is joinable or not
			String s1 = null;
			if (o1.getClass().equals(Participant.class)) {
				s1 = ((Participant) o1).getUniqname();
			}

			String s2 = null;
			if (o2.getClass().equals(Participant.class)) {
				s2 = ((Participant) o2).getUniqname();
			}

			result = compareString(s1, s2);

			// secondary sort based on user name if necessary
			if (result == 0)
				result = compareParticipantName((Participant) o1, (Participant) o2);
			
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_PARTICIPANT_ROLE)) {
			String s1 = "";
			if (o1.getClass().equals(Participant.class)) {
				s1 = ((Participant) o1).getRole();
			}

			String s2 = "";
			if (o2.getClass().equals(Participant.class)) {
				s2 = ((Participant) o2).getRole();
			}

			result = compareString(s1, s2);

			// secondary sort based on user name if necessary
			if (result == 0)
				result = compareParticipantName((Participant) o1, (Participant) o2);
			
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_PARTICIPANT_COURSE)) {
			// sort by whether the site is joinable or not
			String s1 = null;
			if (o1.getClass().equals(Participant.class)) {
				s1 = ((Participant) o1).getSection();
			}

			String s2 = null;
			if (o2.getClass().equals(Participant.class)) {
				s2 = ((Participant) o2).getSection();
			}

			result = compareString(s1, s2);
			
			// secondary sort based on user name if necessary
			if (result == 0)
				result = compareParticipantName((Participant) o1, (Participant) o2);
			
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_PARTICIPANT_ID)) {
			String s1 = null;
			if (o1.getClass().equals(Participant.class)) {
				s1 = ((Participant) o1).getRegId();
			}

			String s2 = null;
			if (o2.getClass().equals(Participant.class)) {
				s2 = ((Participant) o2).getRegId();
			}

			result = compareString(s1, s2);

			// secondary sort based on user name if necessary
			if (result == 0)
				result = compareParticipantName((Participant) o1, (Participant) o2);
			
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_PARTICIPANT_CREDITS)) {
			String s1 = null;
			if (o1.getClass().equals(Participant.class)) {
				s1 = ((Participant) o1).getCredits();
			}

			String s2 = null;
			if (o2.getClass().equals(Participant.class)) {
				s2 = ((Participant) o2).getCredits();
			}

			result = compareString(s1, s2);

			// secondary sort based on user name if necessary
			if (result == 0)
				result = compareParticipantName((Participant) o1, (Participant) o2);
			
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_PARTICIPANT_STATUS)) {
			boolean a1 = true;
			if (o1.getClass().equals(Participant.class)) {
				a1 = ((Participant) o1).isActive();
			}

			boolean a2 = true;
			if (o2.getClass().equals(Participant.class)) {
				a2 = ((Participant) o2).isActive();
			}
			// let the active users show first when sort ascendingly
			result = -compareBoolean(a1, a2);
			
			// secondary sort based on user name if necessary
			if (result == 0)
				result = compareParticipantName((Participant) o1, (Participant) o2);
			
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_CREATION_DATE)) {
			// sort by the site's creation date
			Time t1 = null;
			Time t2 = null;

			// get the times
			try {
				t1 = ((Site) o1).getProperties().getTimeProperty(
						ResourceProperties.PROP_CREATION_DATE);
			} catch (EntityPropertyNotDefinedException e) {
			} catch (EntityPropertyTypeException e) {
			}

			try {
				t2 = ((Site) o2).getProperties().getTimeProperty(
						ResourceProperties.PROP_CREATION_DATE);
			} catch (EntityPropertyNotDefinedException e) {
			} catch (EntityPropertyTypeException e) {
			}
			if (t1 == null) {
				result = -1;
			} else if (t2 == null) {
				result = 1;
			} else if (t1.before(t2)) {
				result = -1;
			} else {
				result = 1;
			}
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_GROUP_TITLE)){
			// sorted by the group title
			String s1 = ((Group) o1).getTitle();
			String s2 = ((Group) o2).getTitle();
			result = compareString(s1, s2);
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_GROUP_SIZE)) {
			// sorted by the group title
			int n1 = ((Group) o1).getMembers().size();
			int n2 = ((Group) o2).getMembers().size();
			result = (n1 > n2) ? 1 : -1;
		} else if (m_criterion.equals(SiteConstants.SORTED_BY_MEMBER_NAME)) {
			// sorted by the member name
			String s1 = null;
			String s2 = null;

			try {
				s1 = UserDirectoryService
						.getUser(((Member) o1).getUserId()).getSortName();
			} catch (Exception ignore) {

			}

			try {
				s2 = UserDirectoryService
						.getUser(((Member) o2).getUserId()).getSortName();
			} catch (Exception ignore) {

			}
			result = compareString(s1, s2);
		}

		if (m_asc == null)
			m_asc = Boolean.TRUE.toString();

		// sort ascending or descending
		if (m_asc.equals(Boolean.FALSE.toString())) {
			result = -result;
		}

		return result;

	} // compare

	/**
	 * Serves as secondary sort by participant name if other criteria returns equal value
	 * @param o1
	 * @param o2
	 * @return
	 */
	private int compareParticipantName(Participant  o1, Participant o2) {
		return compareString(o1.getName(), o2.getName());
	}

	private int compareBoolean(boolean b1, boolean b2) {
		int result;
		if (b1 == b2) {
			result = 0;
		} else if (b1 == true) {
			result = 1;
		} else {
			result = -1;
		}
		return result;
	}

	private int compareString(String s1, String s2) {
		int result;
		if (s1 == null && s2 == null) {
			result = 0;
		} else if (s2 == null) {
			result = 1;
		} else if (s1 == null) {
			result = -1;
		} else {
			
            if (localeCollator != null) {
                    result = localeCollator.compare(s1, s2);
            } else {
        			result = collator.compare(s1, s2);
            }						
		}
		return result;
	}

} // SiteComparator
