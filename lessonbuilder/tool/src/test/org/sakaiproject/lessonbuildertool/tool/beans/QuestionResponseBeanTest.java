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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

import uk.org.ponder.messageutil.MessageLocator;

/**
 * Covers the status classification and labelling that drives the inline-question
 * feedback icon: whether an answered question reads Correct / Incorrect (it defines
 * an answer key) or the neutral Completed (a survey / participation question), and
 * that a manually graded question awaits grading.
 */
public class QuestionResponseBeanTest {

	@Mock private SimplePageToolDao simplePageToolDao;
	@Mock private MessageLocator messageLocator;
	@Mock private SimplePageItem question;
	@Mock private SimplePageQuestionResponse response;

	private QuestionResponseBean bean;
	private AutoCloseable mocks;

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		bean = new QuestionResponseBean();
		bean.setSimplePageToolDao(simplePageToolDao);
		bean.setMessageLocator(messageLocator);
		// Echo the key back so a test can assert which message the note asks for.
		lenient().when(messageLocator.getMessage(anyString())).thenAnswer(i -> i.getArgument(0));
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	// --- questionHasAnswerKey: a defined key means it can be graded Correct/Incorrect ---

	@Test
	public void multipleChoiceWithACorrectOptionHasAnswerKey() {
		when(question.getAttribute("questionType")).thenReturn("multipleChoice");
		when(simplePageToolDao.hasCorrectAnswer(question)).thenReturn(true);
		assertTrue(bean.questionHasAnswerKey(question));
	}

	@Test
	public void multipleChoiceWithNoCorrectOptionIsASurvey() {
		when(question.getAttribute("questionType")).thenReturn("multipleChoice");
		when(simplePageToolDao.hasCorrectAnswer(question)).thenReturn(false);
		assertFalse(bean.questionHasAnswerKey(question));
	}

	@Test
	public void shortAnswerWithAModelAnswerHasAnswerKey() {
		when(question.getAttribute("questionType")).thenReturn("shortanswer");
		when(question.getAttribute("questionAnswer")).thenReturn("photosynthesis");
		assertTrue(bean.questionHasAnswerKey(question));
	}

	@Test
	public void shortAnswerWithNoModelAnswerIsASurvey() {
		when(question.getAttribute("questionType")).thenReturn("shortanswer");
		when(question.getAttribute("questionAnswer")).thenReturn("");
		assertFalse(bean.questionHasAnswerKey(question));
	}

	// --- classifyStatus: correctness (or pending manual grade) drives the status ---

	@Test
	public void gradedCorrectAnswerIsCompleted() {
		when(question.getAttribute("questionType")).thenReturn("multipleChoice");
		when(simplePageToolDao.hasCorrectAnswer(question)).thenReturn(true);
		when(response.isCorrect()).thenReturn(true);
		assertEquals("COMPLETED", bean.classifyStatus(question, response));
	}

	@Test
	public void gradedWrongAnswerIsFailed() {
		when(question.getAttribute("questionType")).thenReturn("multipleChoice");
		when(simplePageToolDao.hasCorrectAnswer(question)).thenReturn(true);
		when(response.isCorrect()).thenReturn(false);
		assertEquals("FAILED", bean.classifyStatus(question, response));
	}

	@Test
	public void manuallyGradedNotYetOverriddenNeedsGrading() {
		// no answer key + marked graded + instructor has not overridden yet
		when(question.getAttribute("questionType")).thenReturn("multipleChoice");
		when(simplePageToolDao.hasCorrectAnswer(question)).thenReturn(false);
		when(question.getAttribute("questionGraded")).thenReturn("true");
		when(response.isOverridden()).thenReturn(false);
		assertEquals("NEEDSGRADING", bean.classifyStatus(question, response));
	}

	// --- statusNote: label the client tooltip shows for each status ---

	@Test
	public void completedWithAnswerKeyReadsCorrect() {
		assertEquals("simplepage.status.correct", bean.statusNote("COMPLETED", true));
	}

	@Test
	public void completedSurveyReadsCompleted() {
		assertEquals("simplepage.status.completed", bean.statusNote("COMPLETED", false));
	}

	@Test
	public void failedReadsIncorrect() {
		assertEquals("simplepage.status.incorrect", bean.statusNote("FAILED", true));
	}

	@Test
	public void needsGradingReadsNeedsGrading() {
		assertEquals("simplepage.status.needsgrading", bean.statusNote("NEEDSGRADING", false));
	}
}
