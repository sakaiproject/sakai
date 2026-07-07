/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.tool.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswer;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseTotals;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.tool.cover.SessionManager;

import uk.org.ponder.messageutil.MessageLocator;

/**
 * Request-scoped helper bean that records a student's answer to an inline Lessons
 * question over AJAX (via the RSF UVB mechanism), so the page does not have to
 * reload. Mirrors {@link GradingBean}: the {@link #getResults()} getter performs
 * the mutation as a side effect and returns a payload the browser renders from.
 *
 * The single result string is a JSON object the client parses to paint the
 * feedback (status icon, correct/incorrect text) and, for polls, the results
 * graph. Keeping the grading/persistence logic in {@link SimplePageBean} means
 * the AJAX path and the legacy full-page submit share exactly one implementation.
 */
@Slf4j
public class QuestionResponseBean {

	// Written by the UVB request
	public String questionId;
	public String questionResponse;
	public String csrfToken;

	private SimplePageToolDao simplePageToolDao;
	private SimplePageBean simplePageBean;
	private MessageLocator messageLocator;

	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public boolean checkCsrf() {
		Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
		return sessionToken != null && sessionToken.toString().equals(csrfToken);
	}

	/**
	 * Records and grades the answer, then returns a one-element array whose single
	 * string is a JSON payload for the browser. The array shape matches the other
	 * UVB getters in this tool (e.g. {@code GradingBean.getResults()}).
	 */
	public String[] getResults() {
		if (!checkCsrf()) {
			return payload(errorResult("permission-failed"));
		}

		Long questionItemId;
		try {
			questionItemId = Long.valueOf(questionId);
		} catch (NumberFormatException e) {
			return payload(errorResult("permission-failed"));
		}

		if (!simplePageBean.itemOk(questionItemId) || !simplePageBean.canReadPage()) {
			return payload(errorResult("permission-failed"));
		}

		String userId = simplePageBean.getCurrentUserId();
		SimplePageQuestionResponse response = simplePageBean.submitQuestionResponse(questionItemId, questionResponse, userId);
		if (response == null) {
			// Already answered and not allowed to re-answer.
			return payload(errorResult("failure"));
		}

		SimplePageItem question = simplePageBean.findItem(questionItemId);
		String status = classifyStatus(question, response);

		JSONObject result = new JSONObject();
		result.put("result", "success");
		result.put("status", status);
		result.put("statusNote", statusNote(status, questionHasAnswerKey(question)));

		String feedbackText = null;
		if ("COMPLETED".equals(status)) {
			feedbackText = question.getAttribute("questionCorrectText");
		} else if ("FAILED".equals(status)) {
			feedbackText = question.getAttribute("questionIncorrectText");
		}
		if (feedbackText != null && !feedbackText.trim().isEmpty()) {
			result.put("feedbackText", feedbackText);
		}

		addPollData(question, status, result);

		return payload(result);
	}

	/**
	 * Classifies the graded response the same way {@code ShowPageProducer.getQuestionStatus}
	 * does for an answered question: manually graded questions await grading, otherwise
	 * the response's correctness drives completed vs. failed.
	 */
	private String classifyStatus(SimplePageItem question, SimplePageQuestionResponse response) {
		String questionType = question.getAttribute("questionType");
		boolean noSpecifiedAnswers = false;
		boolean manuallyGraded = false;

		if ("multipleChoice".equals(questionType) && !simplePageToolDao.hasCorrectAnswer(question)) {
			noSpecifiedAnswers = true;
		} else if ("shortanswer".equals(questionType) && "".equals(question.getAttribute("questionAnswer"))) {
			noSpecifiedAnswers = true;
		}

		if (noSpecifiedAnswers && "true".equals(question.getAttribute("questionGraded"))) {
			manuallyGraded = true;
		}

		if (manuallyGraded && !response.isOverridden()) {
			return "NEEDSGRADING";
		} else if (response.isCorrect()) {
			return "COMPLETED";
		} else {
			return "FAILED";
		}
	}

	/**
	 * For multiple choice questions, adds the per-answer response totals to the payload
	 * when the poll graph should be shown (instructors always; students only when the
	 * question is configured to show results and has been answered). Mirrors the poll
	 * data {@code ShowPageProducer} emits into the {@code questionPollData} region.
	 */
	@SuppressWarnings("unchecked")
	private void addPollData(SimplePageItem question, String status, JSONObject result) {
		boolean answered = "COMPLETED".equals(status) || "FAILED".equals(status) || "NEEDSGRADING".equals(status);
		boolean show = "multipleChoice".equals(question.getAttribute("questionType"))
				&& (simplePageBean.canEditPage()
					|| ("true".equals(question.getAttribute("questionShowPoll")) && answered));
		if (!show) {
			return;
		}

		List<SimplePageQuestionAnswer> answers = simplePageToolDao.findAnswerChoices(question);
		List<SimplePageQuestionResponseTotals> totals = simplePageToolDao.findQRTotals(question.getId());

		Map<Long, Long> responseCounts = new HashMap<>();
		for (SimplePageQuestionAnswer answer : answers) {
			responseCounts.put(answer.getId(), 0L);
		}
		for (SimplePageQuestionResponseTotals total : totals) {
			responseCounts.put(total.getResponseId(), total.getCount());
		}

		JSONArray poll = new JSONArray();
		for (int j = 0; j < answers.size(); j++) {
			char letter = (char) ('A' + j);
			JSONObject entry = new JSONObject();
			entry.put("letter", String.valueOf(letter));
			entry.put("legend", letter + ":" + answers.get(j).getText());
			entry.put("count", responseCounts.get(answers.get(j).getId()));
			poll.add(entry);
		}

		result.put("showPoll", Boolean.TRUE);
		result.put("poll", poll);
	}

	private String statusNote(String status, boolean questionHasAnswerKey) {
		if ("COMPLETED".equals(status)) {
			// A completed question with an answer key is "Correct"; polls/participation stay "Completed"
			return messageLocator.getMessage(questionHasAnswerKey ? "simplepage.status.correct" : "simplepage.status.completed");
		} else if ("FAILED".equals(status)) {
			// FAILED here means an incorrectly answered question; show "Incorrect"
			return messageLocator.getMessage("simplepage.status.incorrect");
		} else if ("NEEDSGRADING".equals(status)) {
			return messageLocator.getMessage("simplepage.status.needsgrading");
		}
		return "";
	}

	// A question can be graded "Correct"/"Incorrect" only when it defines an answer key;
	// polls and participation-only questions have none and stay "Completed".
	private boolean questionHasAnswerKey(SimplePageItem question) {
		String questionType = question.getAttribute("questionType");
		if ("multipleChoice".equals(questionType)) {
			return simplePageToolDao.hasCorrectAnswer(question);
		}
		if ("shortanswer".equals(questionType)) {
			return !"".equals(question.getAttribute("questionAnswer"));
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private JSONObject errorResult(String result) {
		JSONObject o = new JSONObject();
		o.put("result", result);
		if ("failure".equals(result)) {
			o.put("message", messageLocator.getMessage("simplepage.permissions-question"));
		}
		return o;
	}

	private String[] payload(JSONObject result) {
		return new String[] { result.toJSONString() };
	}
}
