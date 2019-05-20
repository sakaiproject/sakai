/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.assignments.AssignmentData;
import org.sakaiproject.sitestats.api.event.detailed.assignments.GroupSubmissionData;
import org.sakaiproject.sitestats.api.event.detailed.assignments.SubmissionData;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

/**
 * Resolves assignment references into meaningful details
 *
 * @author bbailla2
 * @author plukasew
 */
@Slf4j
public class AssignmentReferenceResolver
{
	public enum Type { ASN, SUB };

	public static final String TOOL_ID = "sakai.assignment.grades";

	/**
	 * Resolves an event reference into meaningful details
	 * @param ref the event reference
	 * @param asnServ the assignment service
	 * @param siteServ the site service
	 * @return one of the AssignmentsData variants, or ResolvedEventData.ERROR/PERM_ERROR
	 */
	public static ResolvedEventData resolveReference(String ref, AssignmentService asnServ, SiteService siteServ)
	{
		if (StringUtils.isBlank(ref) || asnServ == null || siteServ == null)
		{
			log.warn("Cannot resolve reference. Reference is null/empty or service(s) are not initialized.");
			return ResolvedEventData.ERROR;
		}

		AssignmentReferenceReckoner.AssignmentReference asnRef = AssignmentReferenceReckoner.reckoner().reference(ref).reckon();
		try
		{
			if (AssignmentServiceConstants.REF_TYPE_ASSIGNMENT.equals(asnRef.getSubtype()))
			{
				Assignment asn = asnServ.getAssignment(asnRef.getId());
				if (asn != null)
				{
					boolean anon = asnServ.assignmentUsesAnonymousGrading(asn);
					return new AssignmentData(asn.getTitle(), anon, asn.getDeleted());
				}
			}
			else if (AssignmentServiceConstants.REF_TYPE_SUBMISSION.equals(asnRef.getSubtype()))
			{
				AssignmentSubmission sub = asnServ.getSubmission(asnRef.getId());
				if (sub == null)
				{
					return ResolvedEventData.ERROR;
				}
				Assignment asn = sub.getAssignment();
				boolean anon = asnServ.assignmentUsesAnonymousGrading(asn);
				AssignmentData asnData = new AssignmentData(asn.getTitle(), anon, asn.getDeleted());
				Set<AssignmentSubmissionSubmitter> submitters = sub.getSubmitters();
				boolean byInstructor = false;
				if (submitters.isEmpty())
				{
					log.warn("No submitters found for submission id " + sub.getId());
					return ResolvedEventData.ERROR;
				}
				String submitter = submitters.stream().filter(s -> s.getSubmittee())
						.findFirst().map(s -> s.getSubmitter()).orElse("");
				if (submitter.isEmpty())
				{
					byInstructor = true;
					if (submitters.size() == 1)
					{
						submitter = submitters.stream().findFirst().map(s -> s.getSubmitter()).orElse("");
					}
				}
				String group = "";
				String groupId = StringUtils.trimToEmpty(sub.getGroupId());
				if (!groupId.isEmpty())
				{
					// get the group title
					Optional<Site> site = RefResolverUtils.getSiteByID(asnRef.getContext(), siteServ, log);
					group = site.map(s -> s.getGroup(groupId)).map(g -> g.getTitle()).orElse("");
				}

				if (group.isEmpty())
				{
					if (submitter.isEmpty())
					{
						log.warn("No submitter found for submission id " + sub.getId());
						return ResolvedEventData.ERROR;
					}

					return new SubmissionData(asnData, submitter, byInstructor);
				}
				else
				{
					return new GroupSubmissionData(asnData, group, submitter, byInstructor);
				}
			}
		}
		catch (IdUnusedException iue)
		{
			log.warn("Unable to retrieve assignment/submission.", iue);
		}
		catch (PermissionException pe)
		{
			log.warn("Permission exception trying to retrieve assignment/submssion.", pe);
			return ResolvedEventData.PERM_ERROR;
		}

		log.warn("Unable to retrieve data; ref = " + ref);
		return ResolvedEventData.ERROR;
	}
}
