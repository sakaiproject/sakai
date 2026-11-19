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
package org.sakaiproject.sitestats.api.view;

import java.util.List;

public final class SiteStatsInstitutionalEventDictionary {

	public static final List<String> LEARNER_EVENTS = List.of(
			"asn.submit.submission",
			"asn.save.submission",
			"asn.read.assignment",
			"sam.assessment.take",
			"sam.assessment.take.via_url",
			"sam.assessment.submit",
			"sam.assessment.submit.via_url",
			"sam.assessment.submit.auto",
			"sam.assessment.resume",
			"sam.assessment.review",
			"lessonbuilder.page.read",
			"lessonbuilder.comment.create",
			"forums.response",
			"forums.read",
			"conversations.post.created",
			"conversations.topic.created");

	public static final List<String> AUTHORING_EVENTS = List.of(
			"asn.new.assignment",
			"asn.revise.assignment",
			"asn.grade.submission",
			"sam.assessment.create",
			"sam.assessment.revise",
			"sam.assessment.publish",
			"lessonbuilder.page.create",
			"lessonbuilder.page.update",
			"lessonbuilder.item.create",
			"lessonbuilder.item.update",
			"content.new",
			"content.revise",
			"gradebook.newItem",
			"gradebook.updateAssignment",
			"gradebook.updateItemScore",
			"annc.new",
			"annc.revise.own",
			"annc.revise.any");

	private SiteStatsInstitutionalEventDictionary() {
	}
}
