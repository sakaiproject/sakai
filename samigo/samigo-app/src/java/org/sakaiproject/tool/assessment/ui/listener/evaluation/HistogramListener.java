

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingComparatorByScoreAndUniqueIdentifier;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.ExportResponsesBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramBarBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramQuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;

/**
 * <p>
 * This handles the selection of the Histogram Aggregate Statistics.
 *  </p>
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Ed Smiley
 * @version $Id$
 */

public class HistogramListener
  implements ActionListener, ValueChangeListener
{
  private static Log log = LogFactory.getLog(HistogramListener.class);
  //private static BeanSort bs;
  //private static ContextUtil cu;
  //private static EvaluationListenerUtil util;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("HistogramAggregate Statistics LISTENER.");

    TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean(
                                "totalScores");
    HistogramScoresBean bean = (HistogramScoresBean) ContextUtil.lookupBean(
                               "histogramScores");
    
    if (!histogramScores(bean, totalBean))
    {
      throw new RuntimeException("failed to call histogramScores.");
    }
  }

  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {
    //log.info("HistogramAggregate Statistics Value Change LISTENER.");

    TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean(
                                "totalScores");
    HistogramScoresBean bean = (HistogramScoresBean) ContextUtil.lookupBean(
                               "histogramScores");
    QuestionScoresBean questionBean = (QuestionScoresBean)
    ContextUtil.lookupBean("questionScores");

    String selectedvalue= (String) event.getNewValue();
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
        log.debug("changed submission pulldown ");
        bean.setAllSubmissions(selectedvalue);    // changed for histogram score bean
        totalBean.setAllSubmissions(selectedvalue);    // changed for total score bean
        questionBean.setAllSubmissions(selectedvalue); // changed for Question score bean
    }

    log.info("Calling histogramScores.");
    if (!histogramScores(bean, totalBean))
    {
      throw new RuntimeException("failed to call histogramScores.");
    }
  }

  /**
   * modified by gopalrc Nov 2007
   * 
   * Calculate the detailed statistics
   * 
   * This will populate the HistogramScoresBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * Some of this code will change when we move this to Hibernate persistence.
   * @param publishedId String
   * @param histogramScores TotalScoresBean
   * @return boolean true if successful
   */
  public boolean histogramScores(HistogramScoresBean histogramScores, TotalScoresBean totalScores)
  {
    try {
    	DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    	String publishedId = totalScores.getPublishedId();
        if (publishedId.equals("0"))
        {
        	publishedId = (String) ContextUtil.lookupParam("publishedAssessmentId");
        }
        String actionString = ContextUtil.lookupParam("actionString");
        // See if this can fix SAK-16437
        if (actionString != null && !actionString.equals("reviewAssessment")){
        	// Shouldn't come to here. The action should either be null or reviewAssessment.
        	// If we can confirm this is where causes SAK-16437, ask UX for a new screen with warning message.  
        	log.error("SAK-16437 happens!! publishedId = " + publishedId + ", agentId = " + AgentFacade.getAgentString());
        }
        
    	ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
        String assessmentName = "";

		  // gopalrc Dec 2007
		  histogramScores.clearLowerQuartileStudents();
		  histogramScores.clearUpperQuartileStudents();

		  String which = histogramScores.getAllSubmissions();
		  if (which == null && totalScores.getAllSubmissions() != null) {
			  // use totalscore's selection
			  which = totalScores.getAllSubmissions();
			  histogramScores.setAllSubmissions(which); // changed submission pulldown
		  }

		  histogramScores.setItemId(ContextUtil.lookupParam("itemId"));
		  histogramScores.setHasNav(ContextUtil.lookupParam("hasNav"));

		  GradingService delegate = new GradingService();
		  PublishedAssessmentService pubService = new PublishedAssessmentService();
		  ArrayList allscores = delegate.getTotalScores(publishedId, which);
          if (allscores.size() == 0) {
			// Similar case in Bug 1537, but clicking Statistics link instead of assignment title.
			// Therefore, redirect the the same page.
			delivery.setOutcome("reviewAssessmentError");
			delivery.setActionString(actionString);
			return true;
		  }
		  
		  histogramScores.setPublishedId(publishedId);
		  int callerName = TotalScoresBean.CALLED_FROM_HISTOGRAM_LISTENER;
		  String isFromStudent = (String) ContextUtil.lookupParam("isFromStudent");
		  if (isFromStudent != null && "true".equals(isFromStudent)) {
			  callerName = TotalScoresBean.CALLED_FROM_HISTOGRAM_LISTENER_STUDENT;
		  }
		  
 		  // get the Map of all users(keyed on userid) belong to the selected sections 
		  // now we only include scores of users belong to the selected sections
		  Map useridMap = null; 
		  ArrayList scores = new ArrayList();
		  // only do section filter if it's published to authenticated users
		  if (totalScores.getReleaseToAnonymous()) {
			  scores.addAll(allscores);
		  }
		  else {
			  Iterator allscores_iter = allscores.iterator();
			  while (allscores_iter.hasNext())
			  {
				  AssessmentGradingData data = (AssessmentGradingData) allscores_iter.next();
				  String agentid =  data.getAgentId();
				  useridMap = totalScores.getUserIdMap(callerName); 
				  if (useridMap.containsKey(agentid)) {
					  scores.add(data);
				  }
			  }
		  }
		  Iterator iter = scores.iterator();
		  //log.info("Has this many agents: " + scores.size());
		  if (!iter.hasNext())
			  return false;
		  Object next = iter.next();
		  AssessmentGradingData data = (AssessmentGradingData) next;

		  /*
		   * gopalrc - moved up from (1)
		   */
		  // here scores contain AssessmentGradingData 
		  Map assessmentMap = getAssessmentStatisticsMap(scores);

		  /*
		   * gopalrc Nov 2007
		   * find students in upper and lower quartiles 
		   * of assessment scores
		   */ 
		  ArrayList submissionsSortedForDiscrim = new ArrayList(scores);
		  boolean anonymous = Boolean.valueOf(totalScores.getAnonymous()).booleanValue();
		  Collections.sort(submissionsSortedForDiscrim, new AssessmentGradingComparatorByScoreAndUniqueIdentifier(anonymous));
		  int numSubmissions = scores.size();
		  //int percent27 = ((numSubmissions*10*27/100)+5)/10; // rounded
		  int percent27 = numSubmissions*27/100; // rounded down
		  if (percent27 == 0) percent27 = 1; // gopalrc - check this - only relevant for very small samples
		  //double q1 = Double.valueOf((String) assessmentMap.get("q1")).doubleValue();
		  //double q3 = Double.valueOf((String) assessmentMap.get("q3")).doubleValue();
		  for (int i=0; i<percent27; i++) {
			  histogramScores.addToLowerQuartileStudents(((AssessmentGradingData)
					  submissionsSortedForDiscrim.get(i)).getAgentId());
			  histogramScores.addToUpperQuartileStudents(((AssessmentGradingData)
					  submissionsSortedForDiscrim.get(numSubmissions-1-i)).getAgentId());
		  }
		  
		  PublishedAssessmentIfc pub = (PublishedAssessmentIfc) pubService
		  .getPublishedAssessment(data.getPublishedAssessmentId()
				  .toString());
		  
		  if (pub != null) {
			if (actionString != null && actionString.equals("reviewAssessment")){
				   if (AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(pub.getStatus())) {
				   // Bug 1547: If this is during review and the assessment is retracted for edit now, 
				   // set the outcome to isRetractedForEdit2 error page.
				   delivery.setOutcome("isRetractedForEdit2");
				   delivery.setActionString(actionString);
				   return true;
				   }
				   else {
						   delivery.setOutcome("histogramScores");
				   }
			}

			  assessmentName = pub.getTitle();
			  //log.info("ASSESSMENT NAME= " + assessmentName);
			  // if section set is null, initialize it - daisyf , 01/31/05
			  HashSet sectionSet = PersistenceService.getInstance()
			  .getPublishedAssessmentFacadeQueries()
			  .getSectionSetForAssessment(pub);
			  pub.setSectionSet(sectionSet);

			  ArrayList parts = pub.getSectionArraySorted();
			  ArrayList info = new ArrayList();
			  Iterator partsIter = parts.iterator();
			  int secseq = 1;
			  double totalpossible = 0;
			  boolean hasRandompart = false;
			  boolean isRandompart = false;

			  HashMap itemScoresMap = delegate.getItemScores(Long.valueOf(publishedId), Long.valueOf(0), which);
			  HashMap itemScores = new HashMap();
			  			  
			  if (totalScores.getReleaseToAnonymous()) {
				  // skip section filter if it's published to anonymous users
				  itemScores.putAll(itemScoresMap);
			  }
			  else {
				  
				  for (Iterator it = itemScoresMap.entrySet().iterator(); it.hasNext();) {
					  Map.Entry entry = (Map.Entry) it.next();
					  Long itemId = (Long) entry.getKey();
					  ArrayList itemScoresList = (ArrayList) entry.getValue();

					  ArrayList filteredItemScoresList = new ArrayList();
					  Iterator itemScoresIter = itemScoresList.iterator();
					  // get the Map of all users(keyed on userid) belong to the
					  // selected sections
					  while (itemScoresIter.hasNext()) {
						  ItemGradingData idata = (ItemGradingData) itemScoresIter.next();
						  String agentid = idata.getAgentId();
						  if (useridMap == null) {
							  useridMap = totalScores.getUserIdMap(callerName); 
						  }
						  if (useridMap.containsKey(agentid)) {
							  filteredItemScoresList.add(idata);
						  }
					  }
					  itemScores.put(itemId, filteredItemScoresList);
				  }
			  }

			  // Iterate through the assessment parts
			  while (partsIter.hasNext()) {
				  SectionDataIfc section = (SectionDataIfc) partsIter.next();
				  String authortype = section
				  .getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
				  if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL
						  .equals(Integer.valueOf(authortype))) {
					  hasRandompart = true;
					  isRandompart = true;
				  } else {
					  isRandompart = false;
				  }
				  if (section.getSequence() == null)
					  section.setSequence(Integer.valueOf(secseq++));
				  String title = rb.getString("p") + " "
				  + section.getSequence().toString();
				  title += ", " + rb.getString("q") + " ";
				  ArrayList itemset = section.getItemArraySortedForGrading();
				  int seq = 1;
				  Iterator itemsIter = itemset.iterator();

				  // Iterate through the assessment questions (items)
				  while (itemsIter.hasNext()) {
					  HistogramQuestionScoresBean questionScores = new HistogramQuestionScoresBean();
					  questionScores.setNumberOfParts(parts.size()); // gopalrc
					  //if this part is a randompart , then set randompart = true
					  questionScores.setRandomType(isRandompart);
					  ItemDataIfc item = (ItemDataIfc) itemsIter.next();

					  //String type = delegate.getTextForId(item.getTypeId());
					  String type = getType(item.getTypeId().intValue());
					  if (item.getSequence() == null)
						  item.setSequence(Integer.valueOf(seq++));

					  questionScores.setPartNumber( section.getSequence().toString());
					  questionScores.setQuestionNumber( item.getSequence().toString());
					  questionScores.setTitle(title + item.getSequence().toString()
							  + " (" + type + ")");
					  questionScores.setQuestionText(item.getText());
					  questionScores.setQuestionType(item.getTypeId().toString());
					  //totalpossible = totalpossible + item.getScore().doubleValue();
					  //ArrayList responses = null;
					  
					  determineResults(pub, questionScores, (ArrayList) itemScores
							  .get(item.getItemId()));
					  questionScores.setTotalScore(item.getScore().toString());


					  // below - gopalrc Nov 2007
					  questionScores.setN(""+numSubmissions);
					  questionScores.setItemId(item.getItemId());
					  Set studentsWithAllCorrect = questionScores.getStudentsWithAllCorrect();
					  Set studentsResponded = questionScores.getStudentsResponded();
					  if (studentsWithAllCorrect == null || studentsResponded == null || 
							  studentsWithAllCorrect.isEmpty() || studentsResponded.isEmpty()) {
						  questionScores.setPercentCorrectFromUpperQuartileStudents("0");
						  questionScores.setPercentCorrectFromLowerQuartileStudents("0");
						  questionScores.setDiscrimination("0.0");
					  }
					  else {
						  int numStudentsWithAllCorrectFromUpperQuartile = 0;
						  int numStudentsWithAllCorrectFromLowerQuartile = 0;
						  Iterator studentsIter = studentsWithAllCorrect.iterator();
						  while (studentsIter.hasNext()) {
							  String agentId = (String) studentsIter.next();
							  if (histogramScores.isUpperQuartileStudent(agentId)) {
								  numStudentsWithAllCorrectFromUpperQuartile++;
							  }
							  if (histogramScores.isLowerQuartileStudent(agentId)) {
								  numStudentsWithAllCorrectFromLowerQuartile++;
							  }
						  }
						  int numStudentsRespondedFromUpperQuartile = 0;
						  int numStudentsRespondedFromLowerQuartile = 0;
						  studentsIter = studentsResponded.iterator();
						  while (studentsIter.hasNext()) {
							  String agentId = (String) studentsIter.next();
							  if (histogramScores.isUpperQuartileStudent(agentId)) {
								  numStudentsRespondedFromUpperQuartile++;
							  }
							  if (histogramScores.isLowerQuartileStudent(agentId)) {
								  numStudentsRespondedFromLowerQuartile++;
							  }
						  }
						  
						  float percentCorrectFromUpperQuartileStudents = 
							  ((float) numStudentsWithAllCorrectFromUpperQuartile / 
									  (float) percent27) * 100f;

						  float percentCorrectFromLowerQuartileStudents = 
							  ((float) numStudentsWithAllCorrectFromLowerQuartile / 
									  (float) percent27) * 100f;

						  questionScores.setPercentCorrectFromUpperQuartileStudents(
								  Integer.toString((int) percentCorrectFromUpperQuartileStudents));
						  questionScores.setPercentCorrectFromLowerQuartileStudents(
								  Integer.toString((int) percentCorrectFromLowerQuartileStudents));

						  float numResponses = (float)questionScores.getNumResponses();

						  float discrimination = ((float)numStudentsWithAllCorrectFromUpperQuartile -								  
								  (float)numStudentsWithAllCorrectFromLowerQuartile)/(float)percent27 ;
						  
						  

						  // round to 2 decimals
						  if (discrimination > 999999 || discrimination < -999999) {
							  questionScores.setDiscrimination("NaN");
						  }
						  else {
							  discrimination = ((int) (discrimination*100.00f)) / 100.00f;
							  questionScores.setDiscrimination(Float.toString(discrimination));
						  }

					  }
					  // above - gopalrc Nov 2007


					  info.add(questionScores);
				  } // end-while - items


				  totalpossible = pub.getTotalScore().doubleValue();

			  } // end-while - parts
			  histogramScores.setInfo(info);
			  histogramScores.setRandomType(hasRandompart);


			  // below - gopalrc Dec 2007
			  HashMap numberOfStudentsWithZeroAnswersForQuestion = new HashMap();
			  
			  for (Iterator it = itemScores.entrySet().iterator(); it.hasNext();) {
				  Map.Entry entry = (Map.Entry) it.next();
				  Long itemId = (Long) entry.getKey();
				  ArrayList scoresPerItem = (ArrayList) entry.getValue();

				  int numStudentsWithZeroAnswers = 0;
				  Iterator scoresPerItemIter = scoresPerItem.iterator();
				  while (scoresPerItemIter.hasNext()) {
					  ItemGradingData itemGradingData = (ItemGradingData) scoresPerItemIter.next();
					  if (itemGradingData.getSubmittedDate() == null) {
						  numStudentsWithZeroAnswers++;
					  }
				  }
				  numberOfStudentsWithZeroAnswersForQuestion.put(itemId, Integer.valueOf(numStudentsWithZeroAnswers));
			  }
			  int maxNumOfAnswers = 0;
			  ArrayList detailedStatistics = new ArrayList();
			  Iterator infoIter = info.iterator();
			  while (infoIter.hasNext()) {
				  HistogramQuestionScoresBean questionScores = (HistogramQuestionScoresBean)infoIter.next();
				  if (questionScores.getQuestionType().equals("1") 
						  || questionScores.getQuestionType().equals("2")
						  || questionScores.getQuestionType().equals("3")
						  || questionScores.getQuestionType().equals("4")) {
					  detailedStatistics.add(questionScores);
					  if (questionScores.getHistogramBars() != null) {
						  maxNumOfAnswers = questionScores.getHistogramBars().length >maxNumOfAnswers ? questionScores.getHistogramBars().length : maxNumOfAnswers;
					  }
					  
					  Object numberOfStudentsWithZeroAnswers = numberOfStudentsWithZeroAnswersForQuestion.get(questionScores.getItemId());
					  if (numberOfStudentsWithZeroAnswers == null) {
						  questionScores.setNumberOfStudentsWithZeroAnswers(0);
					  }
					  else {
						  questionScores.setNumberOfStudentsWithZeroAnswers( ((Integer) numberOfStudentsWithZeroAnswersForQuestion.get(questionScores.getItemId())).intValue() );
					  }
				  }
			  }
			  histogramScores.setDetailedStatistics(detailedStatistics);
			  histogramScores.setMaxNumberOfAnswers(maxNumOfAnswers);
			  // above - gopalrc Dec 2007


			  /*
			   * gopalrc - moved up (1)
				// here scores contain AssessmentGradingData 
				Map assessmentMap = getAssessmentStatisticsMap(scores);
			   */

			  // test to see if it gets back empty map
			  if (assessmentMap.isEmpty()) {
				  histogramScores.setNumResponses(0);
			  }

			  try {
				  BeanUtils.populate(histogramScores, assessmentMap);

				  // quartiles don't seem to be working, workaround
				  histogramScores.setQ1((String) assessmentMap.get("q1"));
				  histogramScores.setQ2((String) assessmentMap.get("q2"));
				  histogramScores.setQ3((String) assessmentMap.get("q3"));
				  histogramScores.setQ4((String) assessmentMap.get("q4"));
				  histogramScores.setTotalScore((String) assessmentMap
						  .get("totalScore"));
				  histogramScores.setTotalPossibleScore(Double
						  .toString(totalpossible));
				  HistogramBarBean[] bars = new HistogramBarBean[histogramScores
				                                                 .getColumnHeight().length];
				  for (int i = 0; i < histogramScores.getColumnHeight().length; i++) {
					  bars[i] = new HistogramBarBean();
					  bars[i]
					       .setColumnHeight(Integer
					    		   .toString(histogramScores
					    				   .getColumnHeight()[i]));
					  bars[i].setNumStudents(histogramScores
							  .getNumStudentCollection()[i]);
					  bars[i].setRangeInfo(histogramScores
							  .getRangeCollection()[i]);
					  //log.info("Set bar " + i + ": " + bean.getColumnHeight()[i] + ", " + bean.getNumStudentCollection()[i] + ", " + bean.getRangeCollection()[i]);
				  }
				  histogramScores.setHistogramBars(bars);



				  ///////////////////////////////////////////////////////////
				  // START DEBUGGING
				  /*
					 log.info("TESTING ASSESSMENT MAP");
					 log.info("assessmentMap: =>");
					 log.info(assessmentMap);
					 log.info("--------------------------------------------");
					 log.info("TESTING TOTALS HISTOGRAM FORM");
					 log.info(
					 "HistogramScoresForm Form: =>\n" + "bean.getMean()=" +
					 bean.getMean() + "\n" +
					 "bean.getColumnHeight()[0] (first elem)=" +
					 bean.getColumnHeight()[0] + "\n" + "bean.getInterval()=" +
					 bean.getInterval() + "\n" + "bean.getLowerQuartile()=" +
					 bean.getLowerQuartile() + "\n" + "bean.getMaxScore()=" +
					 bean.getMaxScore() + "\n" + "bean.getMean()=" + bean.getMean() +
					 "\n" + "bean.getMedian()=" + bean.getMedian() + "\n" +
					 "bean.getNumResponses()=" + bean.getNumResponses() + "\n" +
					 "bean.getNumStudentCollection()=" +
					 bean.getNumStudentCollection() +
					 "\n" + "bean.getQ1()=" + bean.getQ1() + "\n" + "bean.getQ2()=" +
					 bean.getQ2() + "\n" + "bean.getQ3()=" + bean.getQ3() + "\n" +
					 "bean.getQ4()=" + bean.getQ4());
					 log.info("--------------------------------------------");

				   */
				  // END DEBUGGING CODE
				  ///////////////////////////////////////////////////////////
			  } catch (IllegalAccessException e) {
				  e.printStackTrace();
				  log.warn("unable to populate bean" + e);
			  } catch (InvocationTargetException e) {
				  e.printStackTrace();
				  log.warn("unable to populate bean" + e);
			  }

			  histogramScores.setAssessmentName(assessmentName);
		  } else {
			  return false;
		  }

	  } catch (RuntimeException e) {
		  e.printStackTrace();
		  return false;
	  }

	  return true;

  }

  private void determineResults(PublishedAssessmentIfc pub, HistogramQuestionScoresBean qbean,
    ArrayList itemScores)
  {
    if (itemScores == null)
      itemScores = new ArrayList();
    if (qbean.getQuestionType().equals("1") ||  // mcsc
        qbean.getQuestionType().equals("2") ||  // mcmcms
        qbean.getQuestionType().equals("12") ||  // mcmcss
        qbean.getQuestionType().equals("3") ||  // mc survey
        qbean.getQuestionType().equals("4") || // tf
        qbean.getQuestionType().equals("9") || // matching
        qbean.getQuestionType().equals("8") || // Fill in the blank
    	qbean.getQuestionType().equals("11"))  //  Numeric Response
      doAnswerStatistics(pub, qbean, itemScores);
    if (qbean.getQuestionType().equals("5") || // essay
        qbean.getQuestionType().equals("6") || // file upload
        qbean.getQuestionType().equals("7")) // audio recording
      doScoreStatistics(qbean, itemScores);

  }

  private void doAnswerStatistics(PublishedAssessmentIfc pub, HistogramQuestionScoresBean qbean,
    ArrayList scores)
  {
	ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
    if (scores.isEmpty())
    {
      qbean.setHistogramBars(new HistogramBarBean[0]);
      qbean.setNumResponses(0);
      qbean.setPercentCorrect(rb.getString("no_responses"));
      return;
    }

    PublishedAssessmentService pubService  =  new PublishedAssessmentService();
    //build a hashMap (publishedItemId, publishedItem)
    HashMap publishedItemHash = pubService.preparePublishedItemHash(pub);
    HashMap publishedItemTextHash = pubService.preparePublishedItemTextHash(pub);
    HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(pub);

    //int numAnswers = 0;
    ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(((ItemGradingData) scores.toArray()[0]).getPublishedItemId());
    ArrayList text = item.getItemTextArraySorted();
    ArrayList answers = null;
    if (!qbean.getQuestionType().equals("9")) // matching
    {
      ItemTextIfc firstText = (ItemTextIfc) publishedItemTextHash.get(((ItemTextIfc) text.toArray()[0]).getId());
      answers = firstText.getAnswerArraySorted();
    }
   
    if (qbean.getQuestionType().equals("1")) // mcsc
      getTFMCScores(publishedAnswerHash, scores, qbean, answers);
    else if (qbean.getQuestionType().equals("2")) // mcmc
      getFIBMCMCScores(publishedItemHash, publishedAnswerHash, scores, qbean, answers);
    else if (qbean.getQuestionType().equals("12")) 
      getTFMCScores(publishedAnswerHash, scores, qbean, answers);
    else if (qbean.getQuestionType().equals("3"))
      getTFMCScores(publishedAnswerHash, scores, qbean, answers);
    else if (qbean.getQuestionType().equals("4")) // tf
      getTFMCScores(publishedAnswerHash, scores, qbean, answers);
    else if ((qbean.getQuestionType().equals("8"))||(qbean.getQuestionType().equals("11")) )
      getFIBMCMCScores(publishedItemHash, publishedAnswerHash, scores, qbean, answers);
    //else if (qbean.getQuestionType().equals("11"))
    //    getFINMCMCScores(publishedItemHash, publishedAnswerHash, scores, qbean, answers);
    else if (qbean.getQuestionType().equals("9"))
      getMatchingScores(publishedItemTextHash, publishedAnswerHash, scores, qbean, text);
  }


  private void getFIBMCMCScores(HashMap publishedItemHash,
			HashMap publishedAnswerHash, ArrayList scores,
			HistogramQuestionScoresBean qbean, ArrayList answers) {
		ResourceLoader rb = new ResourceLoader(
				"org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
		HashMap texts = new HashMap();
		Iterator iter = answers.iterator();
		HashMap results = new HashMap();
		HashMap numStudentRespondedMap = new HashMap();
		HashMap sequenceMap = new HashMap();
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			texts.put(answer.getId(), answer);
			results.put(answer.getId(), Integer.valueOf(0));
			sequenceMap.put(answer.getSequence(), answer.getId());
		}
		iter = scores.iterator();
		while (iter.hasNext()) {
			ItemGradingData data = (ItemGradingData) iter.next();
			AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data
					.getPublishedAnswerId());
			if (answer != null) {
				// log.info("Rachel: looking for " + answer.getId());
				// found a response
				Integer num = null;
				// num is a counter
				try {
					// we found a response, now get existing count from the
					// hashmap
					num = (Integer) results.get(answer.getId());

				} catch (Exception e) {
					log.warn("No results for " + answer.getId());
				}
				if (num == null)
					num = Integer.valueOf(0);

				ArrayList studentResponseList = (ArrayList) numStudentRespondedMap
						.get(data.getAssessmentGradingId());
				if (studentResponseList == null) {
					studentResponseList = new ArrayList();
				}
				studentResponseList.add(data);
				numStudentRespondedMap.put(data.getAssessmentGradingId(),
						studentResponseList);
				// we found a response, and got the existing num , now update
				// one
				if ((qbean.getQuestionType().equals("8"))
						|| (qbean.getQuestionType().equals("11"))) {
					// for fib we only count the number of correct responses
					Float autoscore = data.getAutoScore();
					if (!(Float.valueOf(0)).equals(autoscore)) {
						results.put(answer.getId(), Integer.valueOf(
								num.intValue() + 1));
					}
				} else {
					// for mc, we count the number of all responses
					results
							.put(answer.getId(),
									Integer.valueOf(num.intValue() + 1));
				}
			}
		}
		HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
		int[] numarray = new int[results.keySet().size()];
		ArrayList sequenceList = new ArrayList();
		iter = answers.iterator();
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			sequenceList.add(answer.getSequence());
		}

		Collections.sort(sequenceList);
		// iter = results.keySet().iterator();
		iter = sequenceList.iterator();
		int i = 0;
		int responses = 0;
		int correctresponses = 0;
		while (iter.hasNext()) {
			Long sequenceId = (Long) iter.next();
			Long answerId = (Long) sequenceMap.get(sequenceId);
			AnswerIfc answer = (AnswerIfc) texts.get(answerId);
			int num = ((Integer) results.get(answerId)).intValue();
			numarray[i] = num;
			bars[i] = new HistogramBarBean();
			if (answer != null)
				bars[i].setLabel(answer.getText());

			// this doens't not apply to fib , do not show checkmarks for FIB
			if (!(qbean.getQuestionType().equals("8"))
					&& !(qbean.getQuestionType().equals("11"))
					&& answer != null) {
				bars[i].setIsCorrect(answer.getIsCorrect());
			}

			if ((num > 1) || (num == 0)) {
				bars[i].setNumStudentsText(num + " "
						+ rb.getString("responses"));
			} else {
				bars[i]
						.setNumStudentsText(num + " "
								+ rb.getString("response"));

			}
			bars[i].setNumStudents(num);
			i++;
		}

		responses = numStudentRespondedMap.size();
		
		for (Iterator it = numStudentRespondedMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			ArrayList resultsForOneStudent = (ArrayList) entry.getValue();

			boolean hasIncorrect = false;
			Iterator listiter = resultsForOneStudent.iterator();

			// iterate through the results for one student
			// for this question (qbean)
			while (listiter.hasNext()) {
				ItemGradingData item = (ItemGradingData) listiter.next();
				
				if ((qbean.getQuestionType().equals("8"))
						|| (qbean.getQuestionType().equals("11"))) {
					// TODO: we are checking to see if the score is > 0, this
					// will not work if the question is worth 0 points.
					// will need to verify each answer individually.
					Float autoscore = item.getAutoScore();
					if ((Float.valueOf(0)).equals(autoscore)) {
						hasIncorrect = true;
						break;
					}
				} else if (qbean.getQuestionType().equals("2")) { // mcmc

					// only answered choices are created in the
					// ItemGradingData_T, so we need to check
					// if # of checkboxes the student checked is == the number
					// of correct answers
					// otherwise if a student only checked one of the multiple
					// correct answers,
					// it would count as a correct response

					try {
						ArrayList itemTextArray = ((ItemDataIfc) publishedItemHash
								.get(item.getPublishedItemId()))
								.getItemTextArraySorted();
						ArrayList answerArray = ((ItemTextIfc) itemTextArray
								.get(0)).getAnswerArraySorted();

						int corranswers = 0;
						Iterator answeriter = answerArray.iterator();
						while (answeriter.hasNext()) {
							AnswerIfc answerchoice = (AnswerIfc) answeriter
									.next();
							if (answerchoice.getIsCorrect().booleanValue()) {
								corranswers++;
							}
						}
						if (resultsForOneStudent.size() != corranswers) {
							hasIncorrect = true;
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(
								"error calculating mcmc question.");
					}

					// now check each answer in MCMC

					AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(item
							.getPublishedAnswerId());
					if (answer != null
							&& (answer.getIsCorrect() == null || (!answer
									.getIsCorrect().booleanValue()))) {
						hasIncorrect = true;
						break;
					}
				}
			}

			
			if (!hasIncorrect) {
				correctresponses = correctresponses + 1;
				
				// gopalrc - Nov 2007
				qbean.addStudentWithAllCorrect(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId()); 
			}
			// gopalrc - Dec 2007
			qbean.addStudentResponded(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId()); 
		}
		// NEW
		int[] heights = calColumnHeight(numarray, responses);
		// int[] heights = calColumnHeight(numarray);
		for (i = 0; i < bars.length; i++)
		{
			try
			{
				bars[i].setColumnHeight(Integer.toString(heights[i]));
			}
			catch(NullPointerException npe)
			{
				log.warn("null column height " + npe);
			}
		}

		qbean.setHistogramBars(bars);
		qbean.setNumResponses(responses);
		if (responses > 0)
			qbean
					.setPercentCorrect(Integer
							.toString((int) (((float) correctresponses / (float) responses) * 100)));
	}

  /*
	 * private void getFINMCMCScores(HashMap publishedItemHash, HashMap
	 * publishedAnswerHash, ArrayList scores, HistogramQuestionScoresBean qbean,
	 * ArrayList answers) { HashMap texts = new HashMap(); Iterator iter =
	 * answers.iterator(); HashMap results = new HashMap(); HashMap
	 * numStudentRespondedMap= new HashMap(); while (iter.hasNext()) { AnswerIfc
	 * answer = (AnswerIfc) iter.next(); texts.put(answer.getId(), answer);
	 * results.put(answer.getId(), Integer.valueOf(0)); } iter = scores.iterator();
	 * while (iter.hasNext()) { ItemGradingData data = (ItemGradingData)
	 * iter.next(); AnswerIfc answer = (AnswerIfc)
	 * publishedAnswerHash.get(data.getPublishedAnswerId()); if (answer != null) {
	 * //log.info("Rachel: looking for " + answer.getId()); // found a response
	 * Integer num = null; // num is a counter try { // we found a response, now
	 * get existing count from the hashmap num = (Integer)
	 * results.get(answer.getId());
	 * 
	 *  } catch (Exception e) { log.warn("No results for " + answer.getId()); }
	 * if (num == null) num = Integer.valueOf(0);
	 * 
	 * ArrayList studentResponseList =
	 * (ArrayList)numStudentRespondedMap.get(data.getAssessmentGradingId()); if
	 * (studentResponseList==null) { studentResponseList = new ArrayList(); }
	 * studentResponseList.add(data);
	 * numStudentRespondedMap.put(data.getAssessmentGradingId(),
	 * studentResponseList); // we found a response, and got the existing num ,
	 * now update one if (qbean.getQuestionType().equals("11")) { // for fib we
	 * only count the number of correct responses Float autoscore =
	 * data.getAutoScore(); if (!(new Float(0)).equals(autoscore)) {
	 * results.put(answer.getId(), Integer.valueOf(num.intValue() + 1)); } } else { //
	 * for mc, we count the number of all responses results.put(answer.getId(),
	 * Integer.valueOf(num.intValue() + 1)); } } } HistogramBarBean[] bars = new
	 * HistogramBarBean[results.keySet().size()]; int[] numarray = new
	 * int[results.keySet().size()]; iter = results.keySet().iterator(); int i =
	 * 0; int responses = 0; int correctresponses = 0; while (iter.hasNext()) {
	 * Long answerId = (Long) iter.next(); AnswerIfc answer = (AnswerIfc)
	 * texts.get(answerId); int num = ((Integer)
	 * results.get(answerId)).intValue(); numarray[i] = num; bars[i] = new
	 * HistogramBarBean(); if(answer != null)
	 * bars[i].setLabel(answer.getText());
	 *  // this doens't not apply to fib , do not show checkmarks for FIB if
	 * (!qbean.getQuestionType().equals("11") && answer != null) {
	 * bars[i].setIsCorrect(answer.getIsCorrect()); }
	 * 
	 * 
	 * if ((num>1)||(num==0)) { bars[i].setNumStudentsText(num + " Responses"); }
	 * else { bars[i].setNumStudentsText(num + " Response");
	 *  } bars[i].setNumStudents(num); i++; }
	 * 
	 * 
	 * responses = numStudentRespondedMap.size(); Iterator mapiter =
	 * numStudentRespondedMap.keySet().iterator(); while (mapiter.hasNext()) {
	 * Long assessmentGradingId= (Long)mapiter.next(); ArrayList
	 * resultsForOneStudent =
	 * (ArrayList)numStudentRespondedMap.get(assessmentGradingId); boolean
	 * hasIncorrect = false; Iterator listiter =
	 * resultsForOneStudent.iterator(); while (listiter.hasNext()) {
	 * ItemGradingData item = (ItemGradingData)listiter.next(); if
	 * (qbean.getQuestionType().equals("11")) { Float autoscore =
	 * item.getAutoScore(); if (!(new Float(0)).equals(autoscore)) {
	 * hasIncorrect = true; break; } } else if
	 * (qbean.getQuestionType().equals("2")) {
	 *  // only answered choices are created in the ItemGradingData_T, so we
	 * need to check // if # of checkboxes the student checked is == the number
	 * of correct answers // otherwise if a student only checked one of the
	 * multiple correct answers, // it would count as a correct response
	 * 
	 * try { ArrayList itemTextArray =
	 * ((ItemDataIfc)publishedItemHash.get(item.getPublishedItemId())).getItemTextArraySorted();
	 * ArrayList answerArray =
	 * ((ItemTextIfc)itemTextArray.get(0)).getAnswerArraySorted();
	 * 
	 * int corranswers = 0; Iterator answeriter = answerArray.iterator(); while
	 * (answeriter.hasNext()){ AnswerIfc answerchoice = (AnswerIfc)
	 * answeriter.next(); if (answerchoice.getIsCorrect().booleanValue()){
	 * corranswers++; } } if (resultsForOneStudent.size() != corranswers){
	 * hasIncorrect = true; break; } } catch (Exception e) {
	 * e.printStackTrace(); throw new RuntimeException("error calculating mcmc
	 * question."); }
	 *  // now check each answer in MCMC
	 * 
	 * AnswerIfc answer = (AnswerIfc)
	 * publishedAnswerHash.get(item.getPublishedAnswerId()); if ( answer != null &&
	 * (answer.getIsCorrect() == null ||
	 * (!answer.getIsCorrect().booleanValue()))) { hasIncorrect = true; break; } } }
	 * if (!hasIncorrect) { correctresponses = correctresponses + 1; } } //NEW
	 * int[] heights = calColumnHeight(numarray,responses); // int[] heights =
	 * calColumnHeight(numarray); for (i=0; i<bars.length; i++)
	 * bars[i].setColumnHeight(Integer.toString(heights[i]));
	 * qbean.setHistogramBars(bars); qbean.setNumResponses(responses); if
	 * (responses > 0) qbean.setPercentCorrect(Integer.toString((int)(((float)
	 * correctresponses/(float) responses) * 100))); }
	 */

  private void getTFMCScores(HashMap publishedAnswerHash, ArrayList scores,
			HistogramQuestionScoresBean qbean, ArrayList answers) {
		ResourceLoader rb = new ResourceLoader(
				"org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
		HashMap texts = new HashMap();
		Iterator iter = answers.iterator();
		HashMap results = new HashMap();
		HashMap sequenceMap = new HashMap();
		
		// create the lookup maps
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			texts.put(answer.getId(), answer);
			results.put(answer.getId(), Integer.valueOf(0));
			sequenceMap.put(answer.getSequence(), answer.getId());
		}

		// find the number of responses (ItemGradingData) for each answer
		iter = scores.iterator();
		while (iter.hasNext()) {
			ItemGradingData data = (ItemGradingData) iter.next();
			
			AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data
					.getPublishedAnswerId());

			if (answer != null) {
				// log.info("Rachel: looking for " + answer.getId());
				// found a response
				Integer num = null;
				// num is a counter
				try {
					// we found a response, now get existing count from the
					// hashmap
					num = (Integer) results.get(answer.getId());

				} catch (Exception e) {
					log.warn("No results for " + answer.getId());
					e.printStackTrace();
				}
				if (num == null)
					num = Integer.valueOf(0);

				// we found a response, and got the existing num , now update
				// one
				// check here for the other bug about non-autograded items
				// having 1 even with no responses
				results.put(answer.getId(), Integer.valueOf(num.intValue() + 1));
				
				
				// gopalrc - Nov 2007
				// this should work because for tf/mc(single)
				// questions, there should be at most 
				// one submitted answer per student/assessment
				if (answer.getIsCorrect() != null
						&& answer.getIsCorrect().booleanValue()) {
					qbean.addStudentWithAllCorrect(data.getAgentId()); 
				}
				// gopalrc - Nov 2007
				qbean.addStudentResponded(data.getAgentId()); 

			}
		}
		
		HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
		int[] numarray = new int[results.keySet().size()];
		ArrayList sequenceList = new ArrayList();
		
		// get an arraylist of answer sequences
		iter = answers.iterator();
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			sequenceList.add(answer.getSequence());
		}

		// sort the sequences
		Collections.sort(sequenceList);
		iter = sequenceList.iterator();
		// iter = results.keySet().iterator();
		int i = 0;
		int responses = 0;
		int correctresponses = 0;

		// find answers sorted by sequence
		while (iter.hasNext()) {
			Long sequenceId = (Long) iter.next();
			Long answerId = (Long) sequenceMap.get(sequenceId);
			AnswerIfc answer = (AnswerIfc) texts.get(answerId);
			
			int num = ((Integer) results.get(answerId)).intValue();
			// set i to be the sequence, so that the answer choices will be in
			// the right order on Statistics page , see Bug SAM-440
			i = answer.getSequence().intValue() - 1;

			numarray[i] = num;
			bars[i] = new HistogramBarBean();
			if (qbean.getQuestionType().equals("4")) { // true-false
				String origText = answer.getText();
				String text = "";
				if ("true".equals(origText)) {
					text = rb.getString("true_msg");
				} else {
					text = rb.getString("false_msg");
				}
				bars[i].setLabel(text);
			} else {
				bars[i].setLabel(answer.getText());
			}
			bars[i].setIsCorrect(answer.getIsCorrect());
			if ((num > 1) || (num == 0)) {
				bars[i].setNumStudentsText(num + " "
						+ rb.getString("responses"));
			} else {
				bars[i]
						.setNumStudentsText(num + " "
								+ rb.getString("response"));

			}
			bars[i].setNumStudents(num);
			responses += num;
			if (answer.getIsCorrect() != null
					&& answer.getIsCorrect().booleanValue()) {
				correctresponses += num;
			}
			// i++;
		}
		// NEW
		int[] heights = calColumnHeight(numarray, responses);
		// int[] heights = calColumnHeight(numarray);
		for (i = 0; i < bars.length; i++)
			bars[i].setColumnHeight(Integer.toString(heights[i]));
		qbean.setHistogramBars(bars);
		qbean.setNumResponses(responses);
		if (responses > 0)
			qbean
					.setPercentCorrect(Integer
							.toString((int) (((float) correctresponses / (float) responses) * 100)));
	}




  private void getMatchingScores(HashMap publishedItemTextHash, HashMap publishedAnswerHash, 
    ArrayList scores, HistogramQuestionScoresBean qbean, ArrayList labels)
  {
	ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
    HashMap texts = new HashMap();
    Iterator iter = labels.iterator();
    HashMap results = new HashMap();
    HashMap numStudentRespondedMap= new HashMap();
    HashMap sequenceMap = new HashMap();
    while (iter.hasNext())
    {
      ItemTextIfc label = (ItemTextIfc) iter.next();
      texts.put(label.getId(), label);
      results.put(label.getId(), Integer.valueOf(0));
      sequenceMap.put(label.getSequence(), label.getId());
    }
    iter = scores.iterator();

    while (iter.hasNext())
    {
      ItemGradingData data = (ItemGradingData) iter.next();
      ItemTextIfc text = (ItemTextIfc) publishedItemTextHash.get(data.getPublishedItemTextId());
      AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
      //    if (answer.getIsCorrect() != null && answer.getIsCorrect().booleanValue())
      if (answer != null)
      {
        Integer num = (Integer) results.get(text.getId());
        if (num == null)
          num = Integer.valueOf(0);


        ArrayList studentResponseList = (ArrayList)numStudentRespondedMap.get(data.getAssessmentGradingId());
        if (studentResponseList==null) {
            studentResponseList = new ArrayList();
        }
        studentResponseList.add(data);
        numStudentRespondedMap.put(data.getAssessmentGradingId(), studentResponseList);

        if (answer.getIsCorrect() != null && answer.getIsCorrect().booleanValue())
        // only store correct responses in the results
        {
          results.put(text.getId(), Integer.valueOf(num.intValue() + 1));
        }
      }
    }

    HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
    int[] numarray = new int[results.keySet().size()];
    ArrayList sequenceList = new ArrayList();
    iter = labels.iterator();
    while (iter.hasNext())
    {
      ItemTextIfc label = (ItemTextIfc) iter.next();
      sequenceList.add(label.getSequence());
    }
     
    Collections.sort(sequenceList);
    iter = sequenceList.iterator();
    //iter = results.keySet().iterator();
    int i = 0;
    int responses = 0;
    int correctresponses = 0;
    while (iter.hasNext())
    {
      Long sequenceId = (Long) iter.next();
      Long textId = (Long) sequenceMap.get(sequenceId);
      ItemTextIfc text = (ItemTextIfc) texts.get(textId);
      int num = ((Integer) results.get(textId)).intValue();
      numarray[i] = num;
      bars[i] = new HistogramBarBean();
      bars[i].setLabel(text.getText());
      bars[i].setNumStudents(num);
      if ((num>1)||(num==0))
	  {
    	  bars[i].setNumStudentsText(num + " " +rb.getString("correct_responses"));
	  }
      else
	  {
	      bars[i].setNumStudentsText(num + " " +rb.getString("correct_response"));

      }

      i++;
    }


    // now calculate responses and correctresponses
    // correctresponses = # of students who got all answers correct, 

    responses = numStudentRespondedMap.size();
    
    for (Iterator it = numStudentRespondedMap.entrySet().iterator(); it.hasNext();) {
    	Map.Entry entry = (Map.Entry) it.next();
     	ArrayList resultsForOneStudent = (ArrayList) entry.getValue();
    	boolean hasIncorrect = false;
    	Iterator listiter = resultsForOneStudent.iterator();

      // numStudentRespondedMap only stores correct answers, so now we need to 
      // check to see if # of  rows in itemgradingdata_t == labels.size() 
      // otherwise if a student only answered one correct answer and 
      // skipped the rest, it would count as a correct response

      while (listiter.hasNext())
      {
        ItemGradingData item = (ItemGradingData)listiter.next();
        if (resultsForOneStudent.size()!= labels.size()){
          hasIncorrect = true;
          break;
        }
          // now check each answer in Matching 
          AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(item.getPublishedAnswerId());
          if (answer.getIsCorrect() == null || (!answer.getIsCorrect().booleanValue()))
          {
            hasIncorrect = true;
            break;
          }
      }
      if (!hasIncorrect) {
        correctresponses = correctresponses + 1;

        // gopalrc - Nov 2007
		qbean.addStudentWithAllCorrect(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId()); 
	  }
	  // gopalrc - Dec 2007
	  qbean.addStudentResponded(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId()); 


    }




    //NEW
    int[] heights = calColumnHeight(numarray,responses);
    //  int[] heights = calColumnHeight(numarray);
    for (i=0; i<bars.length; i++)
      bars[i].setColumnHeight(Integer.toString(heights[i]));
    qbean.setHistogramBars(bars);
    qbean.setNumResponses(responses);
    if (responses > 0)
      qbean.setPercentCorrect(Integer.toString((int)(((float) correctresponses/(float) responses) * 100)));
  }




  private void doScoreStatistics(HistogramQuestionScoresBean qbean,
    ArrayList scores)
  {
    // here scores contain ItemGradingData
    Map assessmentMap = getAssessmentStatisticsMap(scores);

    // test to see if it gets back empty map
    if (assessmentMap.isEmpty())
    {
      qbean.setNumResponses(0);
    }

    try
    {
      BeanUtils.populate(qbean, assessmentMap);

      // quartiles don't seem to be working, workaround
      qbean.setQ1( (String) assessmentMap.get("q1"));
      qbean.setQ2( (String) assessmentMap.get("q2"));
      qbean.setQ3( (String) assessmentMap.get("q3"));
      qbean.setQ4( (String) assessmentMap.get("q4"));
      //qbean.setTotalScore( (String) assessmentMap.get("maxScore"));




      HistogramBarBean[] bars =
        new HistogramBarBean[qbean.getColumnHeight().length];
   

      // SAK-1933: if there is no response, do not show bars at all 
      // do not check if assessmentMap is empty, because it's never empty.
      if (scores.size() == 0) {
      bars = new HistogramBarBean[0];
    }
    else {
      for (int i=0; i<qbean.getColumnHeight().length; i++)
      {
        bars[i] = new HistogramBarBean();
        bars[i].setColumnHeight
          (Integer.toString(qbean.getColumnHeight()[i]));
        bars[i].setNumStudents(qbean.getNumStudentCollection()[i]);
        if (qbean.getNumStudentCollection()[i]>1)
	  {
	      bars[i].setNumStudentsText(qbean.getNumStudentCollection()[i] +
          " Responses");
	  }
      else
	  {
	     bars[i].setNumStudentsText(qbean.getNumStudentCollection()[i] +
          " Response");

      }
	//  bars[i].setNumStudentsText(qbean.getNumStudentCollection()[i] +
	// " Responses");
        bars[i].setRangeInfo(qbean.getRangeCollection()[i]);
        bars[i].setLabel(qbean.getRangeCollection()[i]);
      }
    }
      qbean.setHistogramBars(bars);
    }
      catch (IllegalAccessException e) {
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		e.printStackTrace();
	}
  }

  private Map getAssessmentStatisticsMap(ArrayList scoreList)
  {
    // this function is used to calculate stats for an entire assessment
    // or for a non-autograded question
    // depending on data's instanceof 

    Iterator iter = scoreList.iterator();
    ArrayList floats = new ArrayList();
    while (iter.hasNext())
    {
      Object data = iter.next();
      if (data instanceof AssessmentGradingData) {
    	  Float finalScore = ((AssessmentGradingData) data).getFinalScore();
    	  if (finalScore == null) {
    		  finalScore = Float.valueOf("0");
    	  }
        floats.add(finalScore);
      }
      else
      {
        float autoScore = (float) 0.0;
        if (((ItemGradingData) data).getAutoScore() != null)
          autoScore = ((ItemGradingData) data).getAutoScore().floatValue();
        float overrideScore = (float) 0.0;
        if (((ItemGradingData) data).getOverrideScore() != null)
          overrideScore =
            ((ItemGradingData) data).getOverrideScore().floatValue();
        floats.add(Float.valueOf(autoScore + overrideScore));
      }
    }

    if (floats.isEmpty())
      floats.add(new Float(0.0));
    Object[] array = floats.toArray();
    Arrays.sort(array);

    double[] scores = new double[array.length];
    for (int i=0; i<array.length; i++)
{
      scores[i] = ((Float) array[i]).doubleValue();
}

    HashMap statMap = new HashMap();

    double min = scores[0];
    double max = scores[scores.length - 1];
    double total = calTotal(scores);
    double mean = calMean(scores, total);
    int interval = 0;
    interval = calInterval(scores, min, max);
    int[] numStudents = calNumStudents(scores, min, max, interval);
   
    statMap.put("maxScore", castingNum(max,2));
    statMap.put("interval", Integer.valueOf(interval));
    statMap.put("numResponses", Integer.valueOf(scoreList.size()));
    // statMap.put("numResponses", Integer.valueOf(scores.length));

    statMap.put("totalScore",castingNum(total,2));
    statMap.put("mean", castingNum(mean,2));
    statMap.put("median", castingNum(calMedian(scores),2));
    statMap.put("mode", castingNumForMode(calMode(scores)));

    statMap.put("numStudentCollection", numStudents);
    statMap.put(
      "rangeCollection", calRange(scores, numStudents, min, max, interval));
    statMap.put("standDev", castingNum(calStandDev(scores, mean),2));
    //NEW
    //statMap.put("columnHeight", calColumnHeight(numStudents));
    statMap.put("columnHeight", calColumnHeight(numStudents,scoreList.size()));
  
    statMap.put("arrayLength", Integer.valueOf(numStudents.length));
    statMap.put(
      "range",
      castingNum(scores[0],2) + " - " +
        castingNum(scores[scores.length - 1],2));
    statMap.put("q1", castingNum(calQuartiles(scores, 0.25),2));
    statMap.put("q2", castingNum(calQuartiles(scores, 0.5),2));
    statMap.put("q3", castingNum(calQuartiles(scores, 0.75),2));
    statMap.put("q4", castingNum(max,2));

    return statMap;
  }

  /*** What follows is Huong Nguyen's statistics code. ***/
  /*** We love you Huong! --rmg                        ***/

  /**
   * Calculate the total score for all students
   * @param scores array of scores
   * @return the total
   */
  private static double calTotal(double[] scores)
  {
    double total = 0;
    for(int i = 0; i < scores.length; i++)
    {
      total = total + scores[i];
    }
    return total;
  }

  /**
   * Calculate mean.
   *
   * @param scores array of scores
   * @param total the total of all scores
   *
   * @return mean
   */
  private static double calMean(double[] scores, double total)
  {
    return total / scores.length;
  }

  /**
   * Calculate median.
   *
   * @param scores array of scores
   *
   * @return median
   */
  private static double calMedian(double[] scores)
  {
    double median;
    if(((scores.length) % 2) == 0)
    {
      median =
        (scores[(scores.length / 2)] + scores[(scores.length / 2) - 1]) / 2;
    }

    else
    {
      median = scores[(scores.length - 1) / 2];
    }

    return median;
  }

 /**
   * Calculate mode
   *
   * @param scores array of scores
   *
   * @return mode
   */

    private static String calMode(double[]scores){
	//	double[]scores={1,2,3,4,3,6,5,5,6};
	Arrays.sort(scores);
	String maxString=""+scores[0];
	int maxCount=1;
	int currentCount=1;
	for(int i=1;i<scores.length;i++){
	    if(!(""+scores[i]).equals(""+scores[i-1])){
		currentCount=1;
		if(maxCount==currentCount)
		    maxString=maxString+", "+scores[i];
	    }
	    else{
		currentCount++;
		if(maxCount==currentCount)
		    maxString=maxString+", "+scores[i];
		if(maxCount<currentCount){
		    maxString=""+scores[i];
                    maxCount=currentCount;
		}
		
	    }
	   
	}
	return maxString;
    }



  /**
   * Calculate standard Deviation
   *
   * @param scores array of scores
   * @param mean the mean
   * @param total the total
   *
   * @return the standard deviation
   */
  private static double calStandDev(double[] scores, double mean)
  {
    double total = 0;  
  
    for(int i = 0; i < scores.length; i++)
    {
      total = total + ((scores[i] - mean) * (scores[i] - mean));
    }

    return Math.sqrt(total / (scores.length - 1));

  }

  /**
   * Calculate the interval to use for histograms.
   *
   * @param scores array of scores
   * @param min the minimum score
   * @param max the maximum score
   *
   * @return the interval
   */
  private static int calInterval(double[] scores, double min, double max)
  {
    int interval;

    if((max - min) < 10)
    {
      interval = 1;
    }
    else
    {
      interval = (int) Math.ceil((max - min) / 10);
    }

    return interval;
  }

  /**
   * Calculate the number for each answer.
   *
   * @param answers array of answers
   *
   *
   * @return array of number giving each answer.
   */
  /*
  private static int[] calNum(String[] answers, String[] choices, String type)
  {
    int[] num = new int[choices.length];

    for(int i = 0; i < answers.length; i++)
    {
      for(int j = 0; j < choices.length; j++)
      {
        if(type.equals("Multiple Correct Answer"))
        {
          // TODO: using Tokenizer because split() doesn't seem to work.
          StringTokenizer st = new StringTokenizer(answers[i], "|");
          while(st.hasMoreTokens())
          {
            String nt = st.nextToken();

            if((nt.trim()).equals(choices[j].trim()))
            {
              num[j] = num[j] + 1;
            }
          }

        }
        else
        {
          if(answers[i].equals(choices[j]))
          {
            num[j] = num[j] + 1;
          }
        }
      }
    }

    return num;
  }
  */

  /**
   * Calculate the number correct answer
   *
   * @param answers array of answers
   * @param correct the correct answer
   *
   * @return the number correct
   */
  /*
  private int calCorrect(String[] answers, String correct)
  {
    int cal = 0;
    for(int i = 0; i < answers.length; i++)
    {
      if(answers[i].equals(correct))
      {
        cal++;
      }
    }

    return cal;
  }
  */

  /**
   * Calculate the number of students per interval for histograms.
   *
   * @param scores array of scores
   * @param min the minimum score
   * @param max the maximum score
   * @param interval the interval
   *
   * @return number of students per interval
   */
  private static int[] calNumStudents(
    double[] scores, double min, double max, int interval)
  {

    if(min > max)
    {
      //log.info("max(" + max + ") <min(" + min + ")");
      max = min;
    }

    int[] numStudents = new int[(int) Math.ceil((max - min) / interval)];

    // this handles a case where there are no num students, treats as if
    // a single value of 0
    if(numStudents.length == 0)
    {
      numStudents = new int[1];
      numStudents[0] = 0;
    }

    for(int i = 0; i < scores.length; i++)
    {
      if(scores[i] <= (min + interval))
      {
        numStudents[0]++;
      }
      else
      {
        for(int j = 1; j < (numStudents.length); j++)
        {
          if(
            ((scores[i] > (min + (j * interval))) &&
              (scores[i] <= (min + ((j + 1) * interval)))))
          {
            numStudents[j]++;

            break;
          }
        }
      }
    }

    return numStudents;
  }

  /**
   * Get range text for each interval
   *
   * @param answers array of ansers
   *
   *
   * @return array of range text strings for each interval
   */
  
  /*
  private static String[] calRange(String[] answers, String[] choices)
  {
    String[] range = new String[choices.length];
    int current = 0;

    // gracefully handle a condition where there are no answers
    if(answers.length == 0)
    {
      for(int i = 0; i < range.length; i++)
      {
        range[i] = "unknown";
      }

      return range;
    }

    choices[0] = answers[0];
    for(int a = 1; a < answers.length; a++)
    {
      if(! (answers[a].equals(choices[current])))
      {
        current++;
        choices[current] = answers[a];
      }
    }

    return range;
  }
  */

  /**
   * Calculate range strings for each interval.
   *
   * @param scores array of scores
   * @param numStudents number of students for each interval
   * @param min the minimium
   * @param max the maximum
   * @param interval the number of intervals
   *
   * @return array of range strings for each interval.
   */
  private static String[] calRange(
    double[] scores, int[] numStudents, double min, double max, int interval)
  {
    String[] ranges = new String[numStudents.length];

    ranges[0] = (int) min + " - " + (int) (min + interval);
    int i = 1;
    while(i < ranges.length)
    {
      if((((i + 1) * interval) + min) < max)
      {
        ranges[i] =
          ">" + (int) ((i * interval) + min) + " - " +
          (int) (((i + 1) * interval) + min);
      }
      else
      {
        ranges[i] = ">" + (int) ((i * interval) + min) + " - " + (int) max;
      }

      i++;
    }

    return ranges;
  }


  /**
   * Calculate the height of each histogram column.
   *
   * @param numStudents the number of students for each column
   *
   * @return array of column heights
   */
  /*
  private static int[] calColumnHeightold(int[] numStudents)
  {
    int length = numStudents.length;
    int[] temp = new int[length];

    int[] height = new int[length];
    int i = 0;
    while(i < length)
    {
      temp[i] = numStudents[i];

      i++;
    }

    Arrays.sort(temp);

    int num = 1;

    if((temp.length > 0) && (temp[temp.length - 1] > 0))
    {
      num = (int) (300 / temp[temp.length - 1]);
      int j = 0;
      while(j < length)
      {
        height[j] = num * numStudents[j];

        j++;
      }
    }

    return height;
  }
  */
  
    private static int[] calColumnHeight(int[] numStudents, int totalResponse)
  {
    int[] height = new int[numStudents.length];
   
    int index=0;
    while(index <numStudents.length){
	if(totalResponse>0)
	    height[index] = (int)((600*numStudents[index])/totalResponse);
        else 
	    height[index]=0;
        index++;
    }

    return height;
  }

  /**
   * Calculate quartiles.
   *
   * @param scores score array
   * @param r the quartile rank
   *
   * @return the quartile
   */
  private static double calQuartiles(double[] scores, double r)
  {
    int k;
    double f;
    k = (int) (Math.floor((r * (scores.length - 1)) + 1));
    f = (r * (scores.length - 1)) - Math.floor(r * (scores.length - 1));

    // special handling if insufficient data to calculate
    if(k < 2)
    {
        return scores[0];
    }

    return scores[k - 1] + (f * (scores[k] - scores[k - 1]));
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param n DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
    private String castingNum(double number,int decimal)
  {
      int indexOfDec=0;
      String n;
      int index;
    if(Math.ceil(number) == Math.floor(number))
    {
      return ("" + (int) number);
    }
    else
    {
        n=""+number;
        indexOfDec=n.indexOf(".");
        index=indexOfDec+decimal+1;
        //log.info("NUMBER : "+n);
        //log.info("NUMBER LENGTH : "+n.length());
        if(n.length()>index)
            {
        return n.substring(0,index);
            }
        else{
            return ""+number;
        }

    }
  }


  private String castingNumForMode(String oldmode)
  // only show 2 decimal points for Mode
  {

        String[] tokens = oldmode.split(",");
        String[] roundedtokens =  new String[tokens.length];
        
        StringBuilder newModebuf = new StringBuilder();
        for (int j = 0; j < tokens.length; j++) {
           roundedtokens[j] = castingNum(new Double(tokens[j]).doubleValue(), 2);
           newModebuf.append(", " + roundedtokens[j]);
        }
        String newMode = newModebuf.toString();
        newMode = newMode.substring(2, newMode.length());
        return newMode;
  }
  
  private String getType(int typeId) {
	  ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
	  if (typeId == 1) {
		  return rb.getString("q_mult_sing");
	  }
	  if (typeId == 2) {
		  return rb.getString("q_mult_mult_ms");
	  }
	  if (typeId == 12) {
		  return rb.getString("q_mult_mult_ss");
	  }
	  if (typeId == 3) {
		  return rb.getString("q_mult_surv");
	  }
	  if (typeId == 4) {
		  return rb.getString("q_tf");
	  }
	  if (typeId == 5) {
		  return rb.getString("q_short_ess");
	  }
	  if (typeId == 6) {
		  return rb.getString("q_fu");
	  }
	  if (typeId == 7) {
		  return rb.getString("q_aud");
	  }
	  if (typeId == 8) {
		  return rb.getString("q_fib");
	  }
	  if (typeId == 9) {
		  return rb.getString("q_match");
	  }
	  if (typeId == 11) {
		  return rb.getString("q_fin");
	  }
	  return "";
  }
  
  
  
  /**
   * added by gopalrc - Dec 2007
   * 
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public List getDetailedStatisticsSpreadsheetData(String publishedId) throws
    AbortProcessingException
  {
    log.debug("HistogramAggregate Statistics LISTENER.");

    TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean(
                                "totalScores");
    HistogramScoresBean bean = (HistogramScoresBean) ContextUtil.lookupBean(
                               "histogramScores");
    
    totalBean.setPublishedId(publishedId);
    //String publishedId = totalBean.getPublishedId();

    if (!histogramScores(bean, totalBean))
    {
      throw new RuntimeException("failed to call histogramScores.");
    }
    
    ArrayList spreadsheetRows = new ArrayList();
    Collection detailedStatistics = bean.getDetailedStatistics();
    spreadsheetRows.add(bean.getShowPartAndTotalScoreSpreadsheetColumns());
    //spreadsheetRows.add(bean.getShowDiscriminationColumn());
    
	boolean showDetailedStatisticsSheet;
    if (totalBean.getFirstItem().equals("") || totalBean.getHasRandomDrawPart()) {
    	showDetailedStatisticsSheet = false;
        spreadsheetRows.add(showDetailedStatisticsSheet);
    	return spreadsheetRows;
    }
    else {
    	showDetailedStatisticsSheet = true;
        spreadsheetRows.add(showDetailedStatisticsSheet);
    }
    
    if (detailedStatistics==null || detailedStatistics.size()==0) {
    	return spreadsheetRows;
    }
    
	ResourceLoader rb = new ResourceLoader(
			"org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
    
    ArrayList<Object> headerList = new ArrayList<Object>();
    
    headerList = new ArrayList<Object>();
    headerList.add(ExportResponsesBean.HEADER_MARKER); 
    headerList.add(rb.getString("question")); 
    headerList.add("N");
    headerList.add(rb.getString("pct_correct_of")); 
    if (bean.getShowDiscriminationColumn()) {
        headerList.add(rb.getString("pct_correct_of")); 
        headerList.add(rb.getString("pct_correct_of")); 
    	headerList.add(rb.getString("discrim_abbrev"));
    }
    headerList.add(rb.getString("frequency")); 
    spreadsheetRows.add(headerList);
    
    headerList = new ArrayList<Object>();
    headerList.add(ExportResponsesBean.HEADER_MARKER); 
    headerList.add(""); 
    headerList.add("");
    headerList.add(rb.getString("whole_group")); 
    if (bean.getShowDiscriminationColumn()) {
	    headerList.add(rb.getString("upper_pct")); 
	    headerList.add(rb.getString("lower_pct")); 
	    headerList.add(""); 
    }
    headerList.add("-");
    
    int aChar = 65;
    for (char colHeader=65; colHeader < 65+bean.getMaxNumberOfAnswers(); colHeader++) {
        headerList.add(String.valueOf(colHeader));
    }
    spreadsheetRows.add(headerList);
    
    
    Iterator detailedStatsIter = detailedStatistics.iterator();
    ArrayList statsLine = null;
    while (detailedStatsIter.hasNext()) {
    	HistogramQuestionScoresBean questionBean = (HistogramQuestionScoresBean)detailedStatsIter.next();
    	statsLine = new ArrayList();
    	statsLine.add(questionBean.getQuestionLabel());
    	Double dVal;
    	
    	try {
    		dVal = Double.parseDouble(questionBean.getN());
       		statsLine.add(dVal);
    	}
    	catch (NumberFormatException ex) {
       		statsLine.add(questionBean.getN());
    	}

    	try {
    		if (questionBean.getShowPercentageCorrectAndDiscriminationFigures()) {
    			dVal = Double.parseDouble(questionBean.getPercentCorrect());
    			statsLine.add(dVal);
    		}
    		else {
    			statsLine.add(" ");
    		}
    	}
    	catch (NumberFormatException ex) {
    		statsLine.add(questionBean.getPercentCorrect());
    	}
		
    	if (bean.getShowDiscriminationColumn()) {
    		try {
    			if (questionBean.getShowPercentageCorrectAndDiscriminationFigures()) {
    				dVal = Double.parseDouble(questionBean.getPercentCorrectFromUpperQuartileStudents());
    				statsLine.add(dVal);
    			}
    			else {
    				statsLine.add(" ");
    			}
    		}
    		catch (NumberFormatException ex) {
    			statsLine.add(questionBean.getPercentCorrectFromUpperQuartileStudents());
    		}

    		try {
    			if (questionBean.getShowPercentageCorrectAndDiscriminationFigures()) {
    				dVal = Double.parseDouble(questionBean.getPercentCorrectFromLowerQuartileStudents());
    				statsLine.add(dVal);
    			}
    			else {
    				statsLine.add(" ");
    			}
    		}
    		catch (NumberFormatException ex) {
    			statsLine.add(questionBean.getPercentCorrectFromLowerQuartileStudents());
    		}

    		try {
    			if (questionBean.getShowPercentageCorrectAndDiscriminationFigures()) {
    				dVal = Double.parseDouble(questionBean.getDiscrimination());
    				statsLine.add(dVal);
    			}
    			else {
    				statsLine.add(" ");
    			}
    		}
    		catch (NumberFormatException ex) {
    			statsLine.add(questionBean.getDiscrimination());
    		}
    	}
    	
   		dVal = Double.parseDouble("" + questionBean.getNumberOfStudentsWithZeroAnswers());
   		statsLine.add(dVal);
    	
    	
    	for (int i=0; i<questionBean.getHistogramBars().length; i++) {
    		if (questionBean.getHistogramBars()[i].getIsCorrect()) {
           		statsLine.add(ExportResponsesBean.FORMAT_BOLD);
    		}
       		dVal = Double.parseDouble("" + questionBean.getHistogramBars()[i].getNumStudents() );
       		statsLine.add(dVal);
    	}
    	
    	spreadsheetRows.add(statsLine);
    }
    
    return spreadsheetRows;
    
  }

}
