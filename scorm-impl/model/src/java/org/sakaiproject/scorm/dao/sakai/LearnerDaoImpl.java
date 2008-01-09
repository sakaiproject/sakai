package org.sakaiproject.scorm.dao.sakai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StringUtil;

public abstract class LearnerDaoImpl implements LearnerDao {

	private static Log log = LogFactory.getLog(LearnerDaoImpl.class);
	
	protected abstract CourseManagementService cms();
	protected abstract GroupProvider groupProvider();
	protected abstract SiteService siteService();
	protected abstract UserDirectoryService userDirectoryService();
	
	public List<Learner> find(String context) {
		String realmId = siteService().siteReference(context);
		
		Map learnerMap = new ConcurrentHashMap();
		try {
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			String providerGroupId = realm.getProviderGroupId();
			
			List providerCourseList = getProviderCourseList(StringUtil
					.trimToNull(providerGroupId));
			
			// iterate through the provider list first
			for (Iterator i=providerCourseList.iterator(); i.hasNext();)
			{
				String providerCourseEid = (String) i.next();
				if (cms().isSectionDefined(providerCourseEid))
				{
					// in case of Section eid
					EnrollmentSet enrollmentSet = cms().getSection(providerCourseEid).getEnrollmentSet();
					addLearnersFromEnrollmentSet(learnerMap, realm, providerCourseEid, enrollmentSet);
					// add memberships
					Set memberships = cms().getSectionMemberships(providerCourseEid);
					addLearnersFromMemberships(learnerMap, realm, providerCourseEid, memberships);
				}
			}
			
			// now for those not provided users
			Set grants = realm.getMembers();
			for (Iterator i = grants.iterator(); i.hasNext();) {
				Member g = (Member) i.next();
				if (!g.isProvided())
				{
					try {
						User user = userDirectoryService().getUserByEid(g.getUserEid());
						String userId = user.getId();
						addLearner(userId, user, learnerMap);
						
					} catch (UserNotDefinedException e) {
						// deal with missing user quietly without throwing a
						// warning message
						log.warn(e.getMessage());
					}
				}
			}

		} catch (GroupNotDefinedException ee) {
			log.warn(this + "  IdUnusedException " + realmId);
		}
		
		return new ArrayList<Learner>(learnerMap.values());
	}

	public Learner load(String id) throws LearnerNotDefinedException {
		Learner learner = null;
		
		try {
			User user = userDirectoryService().getUser(id);
			
			learner = new Learner(id, user.getDisplayName(), user.getDisplayId());
			learner.setSortName(user.getSortName());
			
			ResourceProperties resprops = user.getProperties();
			Properties props = new Properties();
			
			for (Iterator<String> it = resprops.getPropertyNames();it.hasNext();) {
				String name = it.next();
				Object value = resprops.get(name);
				props.put(name, value);
			}

		} catch (UserNotDefinedException e) {
			throw new LearnerNotDefinedException("There is no learner in the lms with this id " + id);
		}
		
		return learner;
	}

	
	private void addLearnersFromMemberships(Map learnerMap, AuthzGroup realm, String providerCourseEid, Set memberships) {
		if (memberships != null)
		{
			String sectionTitle = cms().getSection(providerCourseEid).getTitle();
			for (Iterator mIterator = memberships.iterator();mIterator.hasNext();)
			{
				Membership m = (Membership) mIterator.next();
				try 
				{
					User user = userDirectoryService().getUserByEid(m.getUserId());
					String userId = user.getId();
					Member member = realm.getMember(userId);
					if (member != null && member.isProvided())
					{
						addLearner(userId, user, learnerMap);
					}
				} catch (UserNotDefinedException exception) {
					// deal with missing user quietly without throwing a
					// warning message
					log.warn("Failed to find user with id " + m.getUserId(), exception);
				}
			}
		}
	}

	private void addLearnersFromEnrollmentSet(Map learnerMap, AuthzGroup realm, String providerCourseEid, EnrollmentSet enrollmentSet) {
		if (enrollmentSet != null)
		{
			String sectionTitle = cms().getSection(providerCourseEid).getTitle();
			Set enrollments = cms().getEnrollments(enrollmentSet.getEid());
			for (Iterator eIterator = enrollments.iterator();eIterator.hasNext();)
			{
				Enrollment e = (Enrollment) eIterator.next();
				try 
				{
					User user = userDirectoryService().getUserByEid(e.getUserId());
					String userId = user.getId();
					Member member = realm.getMember(userId);
					if (member != null && member.isProvided())
					{
						try
						{
							addLearner(userId, user, learnerMap);
						} catch (Exception ee) {
							log.warn("Unable to add learner from enrollment " + userId, ee);
						}
					}
				} catch (UserNotDefinedException exception) {
					// deal with missing user quietly without throwing a
					// warning message
					log.warn("Failed to find user with id " + e.getUserId(), exception);
				}
			}
		}
	}
	
	
	private Learner addLearner(String userId, User user, Map learnerMap) {
		Learner learner;
		
		if (learnerMap.containsKey(userId))
		{
			learner = (Learner) learnerMap.get(userId);
		}
		else
		{
			learner = new Learner(userId);
		}
		
		learner.setDisplayId(user.getDisplayId());
		learner.setDisplayName(user.getDisplayName());
		learner.setSortName(user.getSortName());
		
		ResourceProperties resprops = user.getProperties();
		Properties props = new Properties();
		
		for (Iterator<String> it = resprops.getPropertyNames();it.hasNext();) {
			String name = it.next();
			Object value = resprops.get(name);
			props.put(name, value);
		}
		
		learnerMap.put(userId, learner);
		
		return learner;
	}
	
	private List getProviderCourseList(String id) {
		Vector rv = new Vector();
		if (id == null || id == "") {
			return rv;
		}
		// Break Provider Id into course id parts
		String[] courseIds = groupProvider().unpackId(id);
		
		// Iterate through course ids
		for (int i=0; i<courseIds.length; i++) {
			String courseId = (String) courseIds[i];

			rv.add(courseId);
		}
		return rv;
	}
}
