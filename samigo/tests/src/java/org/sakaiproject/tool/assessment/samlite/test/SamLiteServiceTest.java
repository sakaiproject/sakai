package org.sakaiproject.tool.assessment.samlite.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Iterator;

import org.sakaiproject.tool.assessment.qti.util.XmlStringBuffer;
import org.sakaiproject.tool.assessment.samlite.api.Answer;
import org.sakaiproject.tool.assessment.samlite.api.Question;
import org.sakaiproject.tool.assessment.samlite.api.QuestionGroup;
import org.sakaiproject.tool.assessment.samlite.api.SamLiteService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.w3c.dom.Document;

public class SamLiteServiceTest extends AbstractDependencyInjectionSpringContextTests {
	
	protected String[] getConfigLocations() {
		return new String[] { "classpath*:**/testbeans.xml" };
	}

	private SamLiteService samLiteService;
	
	public void setSamLiteService(SamLiteService samLiteService) {
		this.samLiteService = samLiteService;
	}
	
	public void testCreateQuiz() throws IOException {
		String quizName = "Test Quiz";
		String quizDescription = "Test Description";
		//String quizData = "Question 1 (10 points)\r\nIs this right?\r\n*a. Yes\r\nb. No\r\nSave answer\r\n";
		
		InputStream is = this.getClass().getResourceAsStream("TestQuiz.txt");
		
		String line = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		while ((line = in.readLine()) != null) {
			buffer.append(line);
			buffer.append('\r');
			buffer.append('\n');
		}
		
		QuestionGroup questionGroup = samLiteService.parse(quizName, quizDescription, buffer.toString());
		
		List questions = questionGroup.getQuestions();
		
		//printQuestions(questions);
		
		// There should be 9 questions in the test quiz
		assertEquals(questions.size(), 9);
		
		Question q1 = (Question) questions.get(0);
		assertEquals(q1.getQuestionType(), Question.MULTIPLE_CHOICE_QUESTION);
		
		Question q2 = (Question) questions.get(1);
		assertEquals(q2.getQuestionType(), Question.MULTIPLE_CHOICE_MULTIPLE_ANSWER_QUESTION);
		assertEquals(q2.getQuestionPoints(), "15");
		
		Question q3 = (Question) questions.get(2);
		assertEquals(q3.getQuestionType(), Question.FILL_IN_THE_BLANK_QUESTION);
		
		Question q4 = (Question) questions.get(3);
		assertEquals(q4.getQuestionType(), Question.FILL_IN_THE_BLANK_QUESTION);
		assertEquals(q4.getCorrectAnswer(), "Blank");
		
		Question q5 = (Question) questions.get(4);
		assertEquals(q5.getQuestionType(), Question.SHORT_ESSAY_QUESTION);
		
		Question q6 = (Question) questions.get(5);
		assertEquals(q6.getQuestionType(), Question.MULTIPLE_CHOICE_QUESTION);
		assertEquals(q6.getAnswers().size(), 3);
		
		Question q7 = (Question) questions.get(6);
		assertEquals(q7.getQuestionType(), Question.TRUE_FALSE_QUESTION);
		assertEquals(q7.getCorrectAnswer(), "True");
		
		Question q8 = (Question) questions.get(7);
		assertEquals(q8.getQuestionType(), Question.SHORT_ESSAY_QUESTION);
		
		Question q9 = (Question) questions.get(8);
		assertEquals(q9.getQuestionType(), Question.MULTIPLE_CHOICE_QUESTION);
		assertEquals(q9.getAnswers().size(), 3);
		
		Document doc = samLiteService.createDocument(questionGroup);
			
		//printDocument(doc);
		
	}
	
	private void printDocument(Document doc) {
		XmlStringBuffer xmlBuffer = new XmlStringBuffer(doc);
		
		System.out.println(xmlBuffer.stringValue());
	}
	
	private void printQuestions(List questions) {
		if (null != questions) {
			for (Iterator it = questions.iterator();it.hasNext();) {
				Question question = (Question)it.next();
				System.out.println("Question " + question.getQuestionNumber() + " (" + question.getQuestionPoints() + " points)");
				System.out.println();
				System.out.println(question.getQuestion());
				System.out.println();
				if (null != question.getAnswers()) {
					for (Iterator ait = question.getAnswers().iterator();ait.hasNext();) {
						Answer answer = (Answer)ait.next();
						if (answer.isCorrect())
							System.out.print("*");
						System.out.println(answer.getId() + ". " + answer.getText());
					}
				}
				System.out.println();
				if (question.getQuestionType() == Question.FILL_IN_THE_BLANK_QUESTION)
					System.out.println("> " + question.getCorrectAnswer());
				System.out.println();
				System.out.println();
			}
		}
	}
	
	
}
