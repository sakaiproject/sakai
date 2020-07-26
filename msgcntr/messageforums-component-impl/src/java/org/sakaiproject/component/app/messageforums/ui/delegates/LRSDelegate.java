/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.ui.delegates;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Result;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class LRSDelegate
{
	public static Optional<LRS_Statement> getStatementForUserPosted(LearningResourceStoreService lrs, String userId,
			String subject, SAKAI_VERB sakaiVerb)
	{
		if (lrs == null)
		{
			return Optional.empty();
		}
		LRS_Actor student = lrs.getActor(userId);
		String url = ServerConfigurationService.getPortalUrl();
		LRS_Verb verb = new LRS_Verb(sakaiVerb);
		LRS_Object lrsObject = new LRS_Object(url + "/forums", sakaiVerb == SAKAI_VERB.responded ? "post-to-thread" : "created-topic");
		lrsObject.setActivityName(enMap(sakaiVerb == SAKAI_VERB.responded ? "User responded to a thread" : "User created a new topic"));
		String desc = (sakaiVerb == SAKAI_VERB.responded ? "User responded to a thread with subject: " : "User created a new topic with subject: ") + subject;
		lrsObject.setDescription(enMap(desc));
		return Optional.of(new LRS_Statement(student, verb, lrsObject));
	}

	public static Optional<LRS_Statement> getStatementForUserReadViewed(LearningResourceStoreService lrs, String userId,
			String subject, String target)
	{
		if (lrs == null)
		{
			return Optional.empty();
		}
		LRS_Actor student = lrs.getActor(userId);
		String url = ServerConfigurationService.getPortalUrl();
		LRS_Verb verb = new LRS_Verb(SAKAI_VERB.interacted);
		LRS_Object lrsObject = new LRS_Object(url + "/forums", "viewed-" + target);
		lrsObject.setActivityName(enMap("User viewed " + target));
		lrsObject.setDescription(enMap("User viewed " + target + " with subject: " + subject));
		return Optional.of(new LRS_Statement(student, verb, lrsObject));
	}

	public static Optional<LRS_Statement> getStatementForGrade(LearningResourceStoreService lrs, UserDirectoryService uds,
			String studentUid, String forumTitle, double score)
	{
		if (lrs == null)
		{
			return Optional.empty();
		}
		LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
		LRS_Object lrsObject = new LRS_Object(ServerConfigurationService.getPortalUrl() + "/forums", "received-grade-forum");
		lrsObject.setActivityName(enMap("User received a grade"));
		lrsObject.setDescription(enMap("User received a grade for their forum post: " + forumTitle));
		try
		{
			User studentUser = uds.getUser(studentUid);
			LRS_Actor student = lrs.getActor(studentUser.getId());
			student.setName(studentUser.getDisplayName());
			LRS_Statement statement = new LRS_Statement(student, verb, lrsObject, getLRS_Result(score), null);
			return Optional.of(statement);
		}
		catch (UserNotDefinedException e)
		{
			log.warn("Student not found for LRS object: " + lrsObject.toString(), e);
			return Optional.empty();
		}
	}

	private static LRS_Result getLRS_Result(double score)
	{
		// the Sakai gradebook allows scores greater than the points possible,
		// so pass null for the max
		LRS_Result result = new LRS_Result(new Float(score), new Float(0.0), null, null);
		result.setCompletion(true);
		return result;
	}

	private static Map<String, String> enMap(String value)
	{
		return Collections.singletonMap("en-US", value);
	}
}
