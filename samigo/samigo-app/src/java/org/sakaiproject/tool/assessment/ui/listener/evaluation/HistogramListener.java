/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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


package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingComparatorByScoreAndUniqueIdentifier;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc.TypeId;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService;
import org.sakaiproject.tool.assessment.services.assessment.StatisticsService.SubmissionOutcome;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.Phase;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PhaseStatus;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.ExportResponsesBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramBarBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramQuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.ItemBarBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;

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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @version $Id$
 */
@Slf4j
public class HistogramListener
  implements ActionListener, ValueChangeListener
{
	private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.EvaluationMessages");
	private static final ResourceLoader rc = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.CommonMessages");

  private GradingService delegate;
  private StatisticsService statisticsService;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("HistogramListener.processAction()");

    TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean(
                                "totalScores");
    HistogramScoresBean bean = (HistogramScoresBean) ContextUtil.lookupBean(
                               "histogramScores");
    
    // Set published assessmentId for histogramScores
    bean.setAssessmentId(totalBean.getPublishedId());
    if (bean.getAssessmentId().equals("0"))
    {
        bean.setAssessmentId((String) ContextUtil.lookupParam("publishedAssessmentId"));
    }

    if (!histogramScores(bean, totalBean))
    {
	String publishedId = totalBean.getPublishedId();
        if (publishedId.equals("0"))
        {
                publishedId = (String) ContextUtil.lookupParam("publishedAssessmentId");
        }
        log.error("Error getting statistics for assessment with published id = " + publishedId);
    	FacesContext context = FacesContext.getCurrentInstance();
	// reset histogramScoresBean, otherwise the previous assessment viewed is displayed. 
	// note that createValueBinding seems to be deprecated and replaced by a new method in 1.2.  Might need to modify this later
	FacesContext.getCurrentInstance().getApplication().createValueBinding("#{histogramScores}").setValue(FacesContext.getCurrentInstance(), null );
        return ;

    }
  }

  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {
    if(!HtmlSelectOneMenu.class.isInstance(event.getSource()) ||
            event.getNewValue() == null || event.getNewValue().equals(event.getOldValue())){
        return;
    }
    HtmlSelectOneMenu selectOneMenu = HtmlSelectOneMenu.class.cast(event.getSource());
    if(selectOneMenu.getId() != null && selectOneMenu.getId().startsWith("allSubmissions")){
        processAllSubmissionsChange(event);
    }
  }

  public void processAllSubmissionsChange(ValueChangeEvent event)
  {
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

    if (!histogramScores(bean, totalBean))
    {
        String publishedId = totalBean.getPublishedId();
        if (publishedId.equals("0"))
        {
                publishedId = (String) ContextUtil.lookupParam("publishedAssessmentId");
        }
        log.error("Error getting statistics for assessment with published id = " + publishedId);
        FacesContext context = FacesContext.getCurrentInstance();
        FacesContext.getCurrentInstance().getApplication().createValueBinding( "#{histogramScores}").setValue(FacesContext.getCurrentInstance(), null );
        return ;
    }
  }

  /**
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
    	DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    	String publishedId = totalScores.getPublishedId();
        if (publishedId.equals("0"))
        {
        	publishedId = (String) ContextUtil.lookupParam("publishedAssessmentId");
        }

        PublishedAssessmentService pubService = new PublishedAssessmentService();
        PublishedAssessmentFacade pub = pubService.getPublishedAssessment(publishedId, false);
        // Get the siteId from the assessment, as it also works in an no-site context
        String siteId = pub.getOwnerSiteId();

        String actionString = ContextUtil.lookupParam("actionString");
        // See if this can fix SAK-16437
        if (actionString != null && !actionString.equals("reviewAssessment")){
        	// Shouldn't come to here. The action should either be null or reviewAssessment.
        	// If we can confirm this is where causes SAK-16437, ask UX for a new screen with warning message.  
        	log.error("SAK-16437 happens!! publishedId = " + publishedId + ", agentId = " + AgentFacade.getAgentString());
        }
        
        String assessmentName = "";

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

		  delegate = new GradingService();
		  List<AssessmentGradingData> allscores = delegate.getTotalScores(publishedId, which);
          //set the ItemGradingData manually here. or we cannot
          //retrieve it later.
          for(AssessmentGradingData agd: allscores){
          	agd.setItemGradingSet(delegate.getItemGradingSet(String.valueOf(agd.getAssessmentGradingId())));
          }
          if (allscores.isEmpty()) {
			// Similar case in Bug 1537, but clicking Statistics link instead of assignment title.
			// Therefore, redirect the the same page.
			delivery.setOutcome("reviewAssessmentError");
			delivery.setActionString(actionString);
			return true;
		  }
		  
		  histogramScores.setPublishedId(publishedId);
		  int callerName = TotalScoresBean.CALLED_FROM_HISTOGRAM_LISTENER;
		  String isFromStudent = ContextUtil.lookupParam("isFromStudent");
		  if ("true".equals(isFromStudent)) {
			  callerName = TotalScoresBean.CALLED_FROM_HISTOGRAM_LISTENER_STUDENT;
		  }
		  
 		  // get the Map of all users(keyed on userid) belong to the selected sections 
		  // now we only include scores of users belong to the selected sections
		  Map<String, EnrollmentRecord> useridMap = null;
		  List<AssessmentGradingData> scores = new ArrayList<>();
		  // only do section filter if it's published to authenticated users
		  if (totalScores.getReleaseToAnonymous()) {
			  scores.addAll(allscores);
		  }
		  else {
			  useridMap = totalScores.getUserIdMap(callerName, siteId);
              for (AssessmentGradingData data : allscores) {
                  String agentid = data.getAgentId();
                  if (useridMap.containsKey(agentid)) {
                      scores.add(data);
                  }
              }
		  }

		  if (scores.isEmpty()) {
			  log.info("Students who have submitted may have been removed from this site");
			  return false;
		  }
		  
		  // here scores contain AssessmentGradingData 
		  Map assessmentMap = getAssessmentStatisticsMap(scores);

		  /*
		   * find students in upper and lower quartiles 
		   * of assessment scores
		   */ 
		  List<AssessmentGradingData> submissionsSortedForDiscrim = new ArrayList<>(scores);
		  boolean anonymous = Boolean.parseBoolean(totalScores.getAnonymous());
		  submissionsSortedForDiscrim.sort(new AssessmentGradingComparatorByScoreAndUniqueIdentifier(anonymous));
		  int numSubmissions = scores.size();
		  //int percent27 = ((numSubmissions*10*27/100)+5)/10; // rounded
		  int percent27 = numSubmissions*27/100; // rounded down
		  if (percent27 == 0) percent27 = 1;
		  for (int i=0; i<percent27; i++) {
			  histogramScores.addToLowerQuartileStudents(((AssessmentGradingData)
					  submissionsSortedForDiscrim.get(i)).getAgentId());
			  histogramScores.addToUpperQuartileStudents(((AssessmentGradingData)
					  submissionsSortedForDiscrim.get(numSubmissions-1-i)).getAgentId());
		  }
		  
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
						   delivery.setSecureDeliveryHTMLFragment( "" );
						   delivery.setBlockDelivery( false );
						   SecureDeliveryServiceAPI secureDelivery = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
						   if ( secureDelivery.isSecureDeliveryAvaliable() ) {
				            	  				            	 
							   String moduleId = pub.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.MODULE_KEY );
							   if ( moduleId != null && ! SecureDeliveryServiceAPI.NONE_ID.equals( moduleId ) ) {
				              		  
								   HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
								   PhaseStatus status = secureDelivery.validatePhase(moduleId, Phase.ASSESSMENT_REVIEW, pub, request );
								   delivery.setSecureDeliveryHTMLFragment( 
										   secureDelivery.getHTMLFragment(moduleId, pub, request, Phase.ASSESSMENT_REVIEW, status, new ResourceLoader().getLocale()));
								   if ( PhaseStatus.FAILURE == status )  {           			 
									   delivery.setOutcome( "secureDeliveryError" );
									   delivery.setActionString(actionString);
									   delivery.setBlockDelivery( true );
									   return true;
								   }
							   }                 	  
						   }
				   }
			}

			  boolean showObjectivesColumn = Boolean.parseBoolean(pub.getAssessmentMetaDataByLabel(AssessmentBaseIfc.HASMETADATAFORQUESTIONS));
			  Map<String, Double> objectivesCorrect = new HashMap<String, Double>();
			  Map<String, Integer> objectivesCounter = new HashMap<String, Integer>();
			  Map<String, Double> keywordsCorrect = new HashMap<String, Double>();
			  Map<String, Integer> keywordsCounter = new HashMap<String, Integer>();
			  
			  assessmentName = pub.getTitle();

			  List<? extends SectionDataIfc> parts = pub.getSectionArraySorted();
                          histogramScores.setAssesmentParts((List<PublishedSectionData>)parts);
			  List info = new ArrayList();
			  Iterator partsIter = parts.iterator();
			  int secseq = 1;
			  double totalpossible = 0;
			  boolean hasRandompart = false;
			  boolean isRandompart = false;
			  String poolName = null;
			  String poolNameFixed = null;
			  
			  Map itemScoresMap = delegate.getItemScores(Long.valueOf(publishedId), Long.valueOf(0), which);
			  Map itemScores = new HashMap();
			  			  
			  if (totalScores.getReleaseToAnonymous()) {
				  // skip section filter if it's published to anonymous users
				  itemScores.putAll(itemScoresMap);
			  }
			  else {
				  if (useridMap == null) {
					  useridMap = totalScores.getUserIdMap(callerName, siteId);
				  }

				  for (Iterator it = itemScoresMap.entrySet().iterator(); it.hasNext();) {
					  Map.Entry entry = (Map.Entry) it.next();
					  Long itemId = (Long) entry.getKey();
					  List itemScoresList = (List) entry.getValue();

					  List filteredItemScoresList = new ArrayList();
					  Iterator itemScoresIter = itemScoresList.iterator();
					  // get the Map of all users(keyed on userid) belong to the
					  // selected sections

					  while (itemScoresIter.hasNext()) {
						  ItemGradingData idata = (ItemGradingData) itemScoresIter.next();
						  String agentid = idata.getAgentId();
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
				  try{
					  if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL
							  .equals(Integer.valueOf(authortype)) || SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.equals(Integer.valueOf(authortype))) {
						  hasRandompart = true;
						  isRandompart = true;
						  poolName = section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW);
						  if (section.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT) != null) {
						    Integer count = Integer.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT));
						    for (int i = 1; i < count; i++) {
						      poolName += SectionDataIfc.SEPARATOR_COMMA + section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW + SectionDataIfc.SEPARATOR_MULTI + i);
						    }
						  }
					  } else if (SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL
							  .equals(Integer.valueOf(authortype))) {
						  hasRandompart = true;
						  isRandompart = true;
						  poolName = section
						  .getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW);
						  poolNameFixed = section
						  .getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_FIXED_AND_RANDOM_DRAW);
					  } else {
						  isRandompart = false;
						  poolName = null;
					  }
				  }catch(NumberFormatException e){
					  isRandompart = false;
					  poolName = null;
				  }
				  if (section.getSequence() == null)
					  section.setSequence(Integer.valueOf(secseq++));
				  String title = rb.getString("part") + " "
				  + section.getSequence().toString();
				  title += ", " + rb.getString("question") + " ";

				  List<ItemDataIfc> itemlist = section.getItemArraySortedForGrading();

				  Set<ItemDataIfc> sortedSet = itemlist.stream()
						  .filter(item -> ((PublishedItemData) item).getIsFixed())
						  .collect(Collectors.toSet());

				  if (!sortedSet.isEmpty()) {
					// getting all hashes from the sortedSet
					List<String> distinctHashValues = sortedSet.stream()
						.filter(item -> item instanceof PublishedItemData)
						.map(item -> ((PublishedItemData) item).getHash())
						.distinct()
						.collect(Collectors.toList());

					// removing from itemSet if there are hashes repeated and getFixed false -> itemSet with only fixed and not repeated fixed on the randow draw
					itemlist.removeIf(item -> item instanceof PublishedItemData &&
											!item.getIsFixed() &&
											distinctHashValues.stream().anyMatch(hash -> hash.equals(item.getHash())));

					section.setItemSet(new HashSet<>(itemlist));
				  }

				  int seq = 1;
				  Iterator<ItemDataIfc> itemsIter = itemlist.iterator();

				  // Iterate through the assessment questions (items)
				  while (itemsIter.hasNext()) {
					  HistogramQuestionScoresBean questionScores = new HistogramQuestionScoresBean();
					  questionScores.setNumberOfParts(parts.size());
					  //if this part is a randompart , then set randompart = true
					  questionScores.setRandomType(isRandompart);
					  ItemDataIfc item = itemsIter.next();
					  questionScores.setPoolName(item.getIsFixed() ? poolNameFixed : poolName);

					  TotalScoresBean totalBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
					  HistogramScoresBean histogramBean = (HistogramScoresBean) ContextUtil.lookupBean("histogramScores");
					  questionScores.setPublishedId(histogramBean.getPublishedId());
					  questionScores.setItemDataId(item.getItemId());
					  questionScores.setAgents(totalBean.getAgents());
					  
					  if (showObjectivesColumn) {
						  String obj = item.getItemMetaDataByLabel(ItemMetaDataIfc.OBJECTIVE);
						  questionScores.setObjectives(obj);
						  String key = item.getItemMetaDataByLabel(ItemMetaDataIfc.KEYWORD);
						  questionScores.setKeywords(key);
					  }

					  //String type = delegate.getTextForId(item.getTypeId());
					  String type = getType(item.getTypeId().intValue());
					  if (item.getSequence() == null)
						  item.setSequence(Integer.valueOf(seq++));

					  questionScores.setPartNumber( section.getSequence().toString());
                      //set the question label depending on random pools and parts
                      if(questionScores.getRandomType() && poolName != null){
                      	if(questionScores.getNumberOfParts() > 1){
                        	questionScores.setQuestionLabelFormat(rb.getString("label_question_part_pool", null));
                        }else{
                      		questionScores.setQuestionLabelFormat(rb.getString("label_question_pool", null));
                        }
                      }else{
                      	if(questionScores.getNumberOfParts() > 1){
                        	questionScores.setQuestionLabelFormat(rb.getString("label_question_part", null));
                        }else{
                            questionScores.setQuestionLabelFormat(rb.getString("label_question", null));
                        }
                      }
					  questionScores.setQuestionNumber( item.getSequence().toString());
                      questionScores.setItemId(item.getItemId());
					  questionScores.setTitle(title + item.getSequence().toString()
							  + " (" + type + ")");
					  
					  if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) { // emi question
						  questionScores.setQuestionText(item.getLeadInText());
					  }
					  else {
						  questionScores.setQuestionText(item.getText());
					  }
					  
					  questionScores.setQuestionType(item.getTypeId().toString());
					  //totalpossible = totalpossible + item.getScore().doubleValue();
					  //ArrayList responses = null;

					  // Should be set before determineResults
					  questionScores.setN(String.valueOf(numSubmissions));
					  if (item.getScore() != null) {
						  questionScores.setTotalScore(item.getScore().toString());
					  }

					  //for each question (item) in the published assessment's current part/section
					  determineResults(pub, questionScores, (List) itemScores.get(item.getItemId()));

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
                          int percent27ForThisQuestion = percent27;
                          Set<String> upperQuartileStudents = histogramScores.getUpperQuartileStudents().keySet();
                          Set<String> lowerQuartileStudents = histogramScores.getLowerQuartileStudents().keySet();
                          if(isRandompart){
                          	//we need to calculate the 27% upper and lower
                            //per question for the people that actually answered
                            //this question.
                            upperQuartileStudents = new HashSet<String>();
                            lowerQuartileStudents = new HashSet<String>();
                            percent27ForThisQuestion = questionScores.getNumResponses()*27/100;
                            if (percent27ForThisQuestion == 0) percent27ForThisQuestion = 1;
                            if(questionScores.getNumResponses() != 0){
                                //need to only get gradings for students that answered this question
                                List<AssessmentGradingData> filteredGradings =
                            		filterGradingData(submissionsSortedForDiscrim, questionScores.getItemId());
                                
                                // SAM-2228: loop control issues because of unsynchronized collection access
                                int filteredGradingsSize = filteredGradings.size();
                                percent27ForThisQuestion = filteredGradingsSize*27/100;
                                
                                for (int i = 0; i < percent27ForThisQuestion; i++) {
                                    lowerQuartileStudents.add(((AssessmentGradingData)
                                	filteredGradings.get(i)).getAgentId());
                                    //
                                    upperQuartileStudents.add(((AssessmentGradingData)
                                    filteredGradings.get(filteredGradingsSize-1-i)).getAgentId());
                                }
                             }
                          }
						  if(questionScores.getNumResponses() != 0){
                              int numStudentsWithAllCorrectFromUpperQuartile = 0;
                              int numStudentsWithAllCorrectFromLowerQuartile = 0;
                              Iterator studentsIter = studentsWithAllCorrect.iterator();
                              while (studentsIter.hasNext()) {
							  String agentId = (String) studentsIter.next();
							  if (upperQuartileStudents.contains(agentId)) {
								  numStudentsWithAllCorrectFromUpperQuartile++;
							  }
							  if (lowerQuartileStudents.contains(agentId)) {
								  numStudentsWithAllCorrectFromLowerQuartile++;
							  }
                              }
						  
                              double percentCorrectFromUpperQuartileStudents =
							  ((double) numStudentsWithAllCorrectFromUpperQuartile / 
									  (double) percent27ForThisQuestion) * 100d;

                              double percentCorrectFromLowerQuartileStudents =
							  ((double) numStudentsWithAllCorrectFromLowerQuartile / 
									  (double) percent27ForThisQuestion) * 100d;

                              questionScores.setPercentCorrectFromUpperQuartileStudents(
								  Integer.toString((int) percentCorrectFromUpperQuartileStudents));
                              questionScores.setPercentCorrectFromLowerQuartileStudents(
								  Integer.toString((int) percentCorrectFromLowerQuartileStudents));
                                                  
                                                    double discrimination = ((double)numStudentsWithAllCorrectFromUpperQuartile -
								  (double)numStudentsWithAllCorrectFromLowerQuartile)/(double)percent27ForThisQuestion ;

                              // round to 2 decimals
                              if (discrimination > 999999 || discrimination < -999999) {
							  questionScores.setDiscrimination("NaN");
                              }
                              else {
							  discrimination = ((int) (discrimination*100.00d)) / 100.00d;
							  questionScores.setDiscrimination(Double.toString(discrimination));
                              }
                          }else{
                              questionScores.setPercentCorrectFromUpperQuartileStudents(rb.getString("na"));
                              questionScores.setPercentCorrectFromLowerQuartileStudents(rb.getString("na"));
                              questionScores.setDiscrimination(rb.getString("na"));
                          }
					  }

					  info.add(questionScores);
				  } // end-while - items


				  totalpossible = pub.getTotalScore().doubleValue();

			  } // end-while - parts
			  histogramScores.setInfo(info);
			  histogramScores.setRandomType(hasRandompart);

			  int maxNumOfAnswers = 0;
			  List<HistogramQuestionScoresBean> detailedStatistics = new ArrayList<HistogramQuestionScoresBean>();
			  Iterator infoIter = info.iterator();
				  while (infoIter.hasNext()) {
					  HistogramQuestionScoresBean questionScores = (HistogramQuestionScoresBean)infoIter.next();
					  boolean includeInDetailedStatistics = isDetailedStatisticsQuestionType(questionScores.getQuestionType());
					  if (includeInDetailedStatistics) {
						  boolean showIndividualAnswers = showsIndividualAnswersInDetailedStatistics(questionScores.getQuestionType());
						  questionScores.setShowIndividualAnswersInDetailedStatistics(showIndividualAnswers);
						  detailedStatistics.add(questionScores);
						  if (showIndividualAnswers && questionScores.getHistogramBars() != null) {
							maxNumOfAnswers = questionScores.getHistogramBars().length > maxNumOfAnswers ? questionScores.getHistogramBars().length : maxNumOfAnswers;
						  }
					  }
				  
				  if (showObjectivesColumn) {
					  // Get the percentage correct by objective
					  String obj = questionScores.getObjectives();
					  if (StringUtils.isNotBlank(obj)) {
						  String[] objs = obj.split(",");
						  for (int i = 0; i < objs.length; i++) {
							  String objective = StringUtils.trimToNull(objs[i]);
							  if (objective == null) {
								  continue;
							  }

							  Double pctCorrect = resolveMetadataPercentCorrect(questionScores, "objectives");
							  Double newAvg = 0.0d;
							  int divisor = 1;

							  if (objectivesCorrect.get(objective) != null) {
								  Double objCorrect = objectivesCorrect.get(objective);
								  divisor = objCorrect.intValue() + 1;
								  newAvg = objCorrect + ((pctCorrect - objCorrect) / divisor);
								  newAvg = new BigDecimal(newAvg).setScale(2, RoundingMode.HALF_UP).doubleValue();
							  } else {
								  newAvg = new BigDecimal(pctCorrect).setScale(2, RoundingMode.HALF_UP).doubleValue();
							  }

							  objectivesCounter.put(objective, divisor);
							  objectivesCorrect.put(objective, newAvg);
						  }
					  }

					  // Get the percentage correct by keyword
					  String key = questionScores.getKeywords();
					  if (StringUtils.isNotBlank(key)) {
						  String [] keys = key.split(",");
						  for (int i = 0; i < keys.length; i++) {
							  String keyword = StringUtils.trimToNull(keys[i]);
							  if (keyword == null) {
								  continue;
							  }

							  // Keep metadata rows for non-answer-statistics questions (e.g. essay/file/audio)
							  // by treating missing/invalid percent-correct values as 0.
							  Double pctCorrect = resolveMetadataPercentCorrect(questionScores, "keywords");

							  if (keywordsCorrect.get(keyword) != null) {
								  int divisor = keywordsCounter.get(keyword) + 1;
								  Double newAvg = keywordsCorrect.get(keyword)
										  + ((pctCorrect - keywordsCorrect.get(keyword)) / divisor);
								  newAvg = new BigDecimal(newAvg).setScale(2, RoundingMode.HALF_UP).doubleValue();

								  keywordsCounter.put(keyword, divisor);
								  keywordsCorrect.put(keyword, newAvg);
							  } else {
								  Double newAvg = new BigDecimal(pctCorrect).setScale(2, RoundingMode.HALF_UP).doubleValue();
								  keywordsCounter.put(keyword, 1);
								  keywordsCorrect.put(keyword, newAvg);
							  }
						  }
					  }
				  }
				  //i.e. for EMI questions we add detailed stats for the whole
				  //question as well as for the sub-questions
				  if (questionScores.getQuestionType().equals(TypeIfc.EXTENDED_MATCHING_ITEMS.toString()) 
				  ) {
					  questionScores.setShowIndividualAnswersInDetailedStatistics(false);
					  detailedStatistics.addAll(questionScores.getInfo());
					  
					  Iterator subInfoIter = questionScores.getInfo().iterator();
					  while (subInfoIter.hasNext()) {
						  HistogramQuestionScoresBean subQuestionScores = (HistogramQuestionScoresBean) subInfoIter.next();
						  if (subQuestionScores.getHistogramBars() != null) {
							  subQuestionScores.setN(questionScores.getN());
							  maxNumOfAnswers = subQuestionScores.getHistogramBars().length >maxNumOfAnswers ? subQuestionScores.getHistogramBars().length : maxNumOfAnswers;
						  }
					  }
/*					  
					  Object numberOfStudentsWithZeroAnswers = numberOfStudentsWithZeroAnswersForQuestion.get(questionScores.getItemId());
					  if (numberOfStudentsWithZeroAnswers == null) {
						  questionScores.setNumberOfStudentsWithZeroAnswers(0);
					  }
					  else {
						  questionScores.setNumberOfStudentsWithZeroAnswers( ((Integer) numberOfStudentsWithZeroAnswersForQuestion.get(questionScores.getItemId())).intValue() );
					  }
*/					  
				  }
				  
				  
			  }
		      //VULA-1948: sort the detailedStatistics list by Question Label
			  sortQuestionScoresByLabel(detailedStatistics);
			  histogramScores.setDetailedStatistics(detailedStatistics);
			  histogramScores.setMaxNumberOfAnswers(maxNumOfAnswers);
			  histogramScores.setShowObjectivesColumn(showObjectivesColumn);
			  
			  if (showObjectivesColumn) {
				  List<Entry<String, Double>> objectivesList = new ArrayList<Entry<String, Double>>(objectivesCorrect.entrySet());
				  Collections.sort(objectivesList, new Comparator<Entry<String, Double>>() {
					  public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
						  return e1.getKey().compareTo(e2.getKey());
					  }
				  });
				  histogramScores.setObjectives(objectivesList);
				  
				  List<Entry<String, Double>> keywordsList = new ArrayList<Entry<String, Double>>(keywordsCorrect.entrySet());
				  
				  Collections.sort(keywordsList, new Comparator<Entry<String, Double>>() {
					  public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
						  return e1.getKey().compareTo(e2.getKey());
					  }
				  });
				  histogramScores.setKeywords(keywordsList);
			  }

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
				  }
				  histogramScores.setHistogramBars(bars);
			  } catch (IllegalAccessException e) {
				  log.warn("IllegalAccessException:  unable to populate bean" + e);
			  } catch (InvocationTargetException e) {
				  log.warn("InvocationTargetException: unable to populate bean" + e);
			  }

			  histogramScores.setAssessmentName(assessmentName);
	  return true;
  }

  private double resolveMetadataPercentCorrect(HistogramQuestionScoresBean questionScores, String metadataType) {
	  if (questionScores == null) {
		  return 0.0d;
	  }

	  if (isScoreStatisticsQuestionType(questionScores.getQuestionType())) {
		  return resolveScoreStatisticsPercentCorrect(questionScores);
	  }

	  return parseMetadataPercentCorrect(questionScores.getPercentCorrect(), metadataType);
  }

  private double parseMetadataPercentCorrect(String percentCorrect, String metadataType) {
	  if (StringUtils.isBlank(percentCorrect) || "N/A".equalsIgnoreCase(percentCorrect)) {
		  return 0.0d;
	  }

	  try {
		  return Double.parseDouble(percentCorrect);
	  } catch (NumberFormatException nfe) {
		  log.debug("Ignoring non-numeric percentCorrect [{}] for metadata type [{}]", percentCorrect, metadataType);
		  return 0.0d;
	  }
  }

  /**
   * For each question (item) in the published assessment's current part/section
   * determine the results by calculating statistics for whole question or
   * individual answers depending on the question type
   * @param pub
   * @param qbean
   * @param itemScores
   */
  private void determineResults(PublishedAssessmentIfc pub, HistogramQuestionScoresBean qbean, List<ItemGradingData> itemScores)
  {
    if (itemScores == null)
      itemScores = new ArrayList<ItemGradingData>();

      Map<Long, Boolean> hasAnswersByAssessmentGradingId = new HashMap<>();
      for (ItemGradingData itemGradingData : itemScores) {
          Long assessmentGradingId = itemGradingData.getAssessmentGradingId();
          if (assessmentGradingId == null) {
              continue;
          }

          boolean hasAnswer = hasAnswerForItemType(qbean.getQuestionType(), itemGradingData);
          hasAnswersByAssessmentGradingId.merge(assessmentGradingId, hasAnswer, Boolean::logicalOr);
      }

      int responses = 0;
      int numStudentsWithZeroAnswers = 0;
      for (Boolean hasAnswer : hasAnswersByAssessmentGradingId.values()) {
          if (Boolean.TRUE.equals(hasAnswer)) {
              responses++;
          } else {
              numStudentsWithZeroAnswers++;
          }
      }

      qbean.setNumResponses(responses);
      qbean.setNumberOfStudentsWithZeroAnswers(numStudentsWithZeroAnswers);

    if (isAnswerStatisticsQuestionType(qbean.getQuestionType()))
      doAnswerStatistics(pub, qbean, itemScores);
    if (isScoreStatisticsQuestionType(qbean.getQuestionType()))
      doScoreStatistics(qbean, itemScores);

  }

  private boolean isDetailedStatisticsQuestionType(String questionType) {
    return StatisticsService.includesInDetailedStatistics(questionType);
  }

  private boolean showsIndividualAnswersInDetailedStatistics(String questionType) {
    return StatisticsService.showsIndividualAnswersInDetailedStatistics(questionType);
  }

  private boolean isAnswerStatisticsQuestionType(String questionType) {
    return StatisticsService.supportsAnswerStatistics(questionType);
  }

  private boolean isScoreStatisticsQuestionType(String questionType) {
    return StatisticsService.supportsScoreStatistics(questionType);
  }

  private TypeId resolveQuestionTypeId(String questionType) {
    if (StringUtils.isBlank(questionType)) {
      return null;
    }

    long parsedQuestionTypeId;
    try {
      parsedQuestionTypeId = Long.parseLong(questionType);
    } catch (NumberFormatException e) {
      return null;
    }

    if (!TypeId.isValidId(parsedQuestionTypeId)) {
      return null;
    }
    return TypeId.getInstance(parsedQuestionTypeId);
  }

  private void dispatchAnswerStatistics(TypeId questionTypeId, Map publishedItemHash, Map publishedItemTextHash,
      Map publishedAnswerHash, List<ItemGradingData> scores, HistogramQuestionScoresBean qbean, ItemDataIfc item, List text,
      List answers, Map emiRequiredCorrectAnswersCount) {
    if (questionTypeId == null) {
      return;
    }

    switch (questionTypeId) {
      case MULTIPLE_CHOICE_ID:
      case MULTIPLE_CORRECT_SINGLE_SELECTION_ID:
      case MULTIPLE_CHOICE_SURVEY_ID:
      case TRUE_FALSE_ID:
        getTFMCScores(publishedAnswerHash, scores, qbean, answers);
        break;
      case MULTIPLE_CORRECT_ID:
      case FILL_IN_BLANK_ID:
      case FILL_IN_NUMERIC_ID:
        getFIBMCMCScores(publishedItemHash, publishedAnswerHash, scores, qbean, answers, item);
        break;
      case MATCHING_ID:
        getMatchingScores(publishedItemTextHash, publishedAnswerHash, scores, qbean, text);
        break;
      case EXTENDED_MATCHING_ITEMS_ID:
        getEMIScores(publishedItemHash, publishedAnswerHash, emiRequiredCorrectAnswersCount, scores, qbean, answers);
        break;
      case MATRIX_CHOICES_SURVEY_ID:
        getMatrixSurveyScores(publishedItemTextHash, publishedAnswerHash, scores, qbean, text);
        break;
      case CALCULATED_QUESTION_ID:
        getCalculatedQuestionScores(scores, qbean, item);
        break;
      case IMAGEMAP_QUESTION_ID:
        getImageMapQuestionScores(publishedItemTextHash, publishedAnswerHash, (List) scores, qbean, (List) text);
        break;
      default:
        log.warn("No answer-statistics dispatcher for question type [{}] (parsed id: {}). "
                + "Question type supports answer statistics but is not handled in dispatchAnswerStatistics.",
                qbean.getQuestionType(), questionTypeId);
        break;
    }
  }

  private boolean isSurveyLikeAnswerKeyCandidate(TypeId questionTypeId) {
    return questionTypeId == TypeId.MULTIPLE_CHOICE_ID
            || questionTypeId == TypeId.MULTIPLE_CORRECT_ID
            || questionTypeId == TypeId.MULTIPLE_CORRECT_SINGLE_SELECTION_ID
            || questionTypeId == TypeId.TRUE_FALSE_ID;
  }

  private boolean hasAnswerForItemType(String questionType, ItemGradingData itemGradingData) {
    Long answerId = itemGradingData.getPublishedAnswerId();
    String answerText = itemGradingData.getAnswerText();

    if (StringUtils.equalsAny(questionType,
            TypeIfc.FILL_IN_BLANK.toString(),
            TypeIfc.FILL_IN_NUMERIC.toString(),
            TypeIfc.CALCULATED_QUESTION.toString())) {
      return answerId != null && StringUtils.isNotBlank(answerText);
    }

    if (StringUtils.equals(questionType, TypeIfc.IMAGEMAP_QUESTION.toString())) {
      String normalizedAnswerText = StringUtils.trimToNull(answerText);
      return answerId != null
              && normalizedAnswerText != null
              && !StringUtils.equalsIgnoreCase(normalizedAnswerText, "undefined");
    }

    if (StringUtils.equals(questionType, TypeIfc.MATCHING.toString())) {
      return answerId != null || StringUtils.isNotBlank(answerText);
    }

    return answerId != null;
  }

  /**
   * For each question where statistics are required for seperate answers, 
   * this method calculates the answer statistics by calling a different
   * getXXXScores() method for each question type.
   * @param pub
   * @param qbean
   * @param scores
   */
  private void doAnswerStatistics(PublishedAssessmentIfc pub, HistogramQuestionScoresBean qbean,
    List<ItemGradingData> scores)
  {
	
//    Don't return here. This will cause questions to be displayed inconsistently on the stats page
//    if (scores.isEmpty())
//    {
//      qbean.setHistogramBars(new HistogramBarBean[0]);
//      qbean.setNumResponses(0);
//      qbean.setPercentCorrect(rb.getString("no_responses"));
//      return;
//    }

    PublishedAssessmentService pubService  =  new PublishedAssessmentService();
    PublishedItemService pubItemService = new PublishedItemService();
    
    //build a hashMap (publishedItemId, publishedItem)
    Map publishedItemHash = pubService.preparePublishedItemHash(pub);
    Map publishedItemTextHash = pubService.preparePublishedItemTextHash(pub);
    Map publishedAnswerHash = pubService.preparePublishedAnswerHash(pub);
    
 // re-attach session and load all lazy loaded parent/child stuff
       
//        Set<Long> publishedAnswerHashKeySet = publishedAnswerHash.keySet();
//    	   
//     	    for(Long key : publishedAnswerHashKeySet) {
//              AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(key);
//             
//     	          if (! Hibernate.isInitialized(answer.getChildAnswerSet())) {
//                 pubItemService.eagerFetchAnswer(answer);
//    	          }
//    	    }
    	

    //int numAnswers = 0;
    TypeId questionTypeId = resolveQuestionTypeId(qbean.getQuestionType());
    ItemDataIfc item = (ItemDataIfc) publishedItemHash.get(qbean.getItemId());
    List text = item.getItemTextArraySorted();
    List answers = null;
    
		//keys number of correct answers required by sub-question (ItemText)
	Map emiRequiredCorrectAnswersCount = null;
    if (questionTypeId == TypeId.EXTENDED_MATCHING_ITEMS_ID) { //EMI
    	emiRequiredCorrectAnswersCount = new HashMap();
    	answers = new ArrayList();
    	for (int i=0; i<text.size(); i++) { 
    	    ItemTextIfc iText = (ItemTextIfc) publishedItemTextHash.get(((ItemTextIfc) text.toArray()[i]).getId());
        	if (iText.isEmiQuestionItemText()) {
        		boolean requireAllCorrectAnswers = true;
        		int numCorrectAnswersRequired = 0;
        		if (iText.getRequiredOptionsCount()!=null && iText.getRequiredOptionsCount().intValue()>0) {
        			requireAllCorrectAnswers = false;
        			numCorrectAnswersRequired = iText.getRequiredOptionsCount().intValue();
        		}
        		if (iText.getAnswerArraySorted() == null) continue;
        		Iterator ansIter = iText.getAnswerArraySorted().iterator();
        		while (ansIter.hasNext()) {
        			AnswerIfc answer = (AnswerIfc)ansIter.next();
        			answers.add(answer);
        			if (requireAllCorrectAnswers && Boolean.TRUE.equals(answer.getIsCorrect())) {
        				numCorrectAnswersRequired++;
        			}
        		}
    			emiRequiredCorrectAnswersCount.put(iText.getId(), Integer.valueOf(numCorrectAnswersRequired));
    	    }
    	}
    }
    else if (questionTypeId != TypeId.MATCHING_ID) // matching
    {
      if (text.size() > 0) {
        ItemTextIfc firstText = (ItemTextIfc) publishedItemTextHash.get(((ItemTextIfc) text.toArray()[0]).getId());
        answers = firstText.getAnswerArraySorted();
      }
    }

    dispatchAnswerStatistics(questionTypeId, publishedItemHash, publishedItemTextHash, publishedAnswerHash, scores, qbean,
            item, text, answers, emiRequiredCorrectAnswersCount);

    boolean isSurveyType = StatisticsService.isSurveyQuestionType(qbean.getQuestionType())
            || isSurveyLikeQuestionWithoutAnswerKey(qbean.getQuestionType(), answers);

    if (!isSurveyType) {
      Map<Long, AnswerIfc> answersById = new HashMap<>();
      for (Object answerObject : publishedAnswerHash.values()) {
        AnswerIfc answer = (AnswerIfc) answerObject;
        if (answer != null && answer.getId() != null) {
          answersById.put(answer.getId(), answer);
        }
      }
      applyCanonicalSubmissionTallies(qbean, item, scores, answersById);
    }

    long respondedCount = qbean.getNumResponses();
    long correctCount = Optional.ofNullable(qbean.getStudentsWithAllCorrect()).map(Set::size).orElse(0);
    long blankCount = Optional.ofNullable(qbean.getNumberOfStudentsWithZeroAnswers()).orElse(0);
    long totalCount = respondedCount + blankCount;
    long incorrectCount = respondedCount - correctCount;
    if (incorrectCount < 0) {
      incorrectCount = 0;
    }

    if (!isSurveyType) {
      qbean.setPercentCorrect(Integer.toString(calculatePercentCorrect(correctCount, respondedCount)));
      // Ideally totalCount should not be 0, if it happens we should handle it to avoid division by 0
      if (totalCount > 0) {
        int difficulty = calcDifficulty(totalCount, incorrectCount, blankCount);
        qbean.setDifficulty(difficulty);
      } else {
        log.warn("respondedCount is 0 for item with id=[{}], title=[{}], type=[{}]",
                qbean.getItemId(), qbean.getTitle(), qbean.getQuestionType());
      }

      qbean.setNumberOfStudentsWithCorrectAnswers(correctCount);
      qbean.setNumberOfStudentsWithIncorrectAnswers(incorrectCount);
    } else {
      qbean.setDifficulty(null);
      qbean.setNumberOfStudentsWithCorrectAnswers(null);
      qbean.setNumberOfStudentsWithIncorrectAnswers(null);
    }
  }

  private boolean isSurveyLikeQuestionWithoutAnswerKey(String questionType, List answers) {
    TypeId questionTypeId = resolveQuestionTypeId(questionType);
    if (!isSurveyLikeAnswerKeyCandidate(questionTypeId)) {
      return false;
    }

    if (answers == null || answers.isEmpty()) {
      return false;
    }

    boolean hasAnswersWithNullCorrectness = false;
    for (Object answerObj : answers) {
      AnswerIfc answer = (AnswerIfc) answerObj;
      if (answer.getIsCorrect() != null) {
        return false;
      }
      hasAnswersWithNullCorrectness = true;
    }

    return hasAnswersWithNullCorrectness;
  }

  private void applyCanonicalSubmissionTallies(HistogramQuestionScoresBean qbean, ItemDataIfc item,
      List<ItemGradingData> scores, Map<Long, AnswerIfc> answersById) {
    Map<Long, List<ItemGradingData>> scoresByAssessment = new HashMap<>();
    for (ItemGradingData score : scores) {
      if (score == null || score.getAssessmentGradingId() == null) {
        continue;
      }
      scoresByAssessment.computeIfAbsent(score.getAssessmentGradingId(), key -> new ArrayList<>()).add(score);
    }

    int respondedCount = 0;
    int blankCount = 0;
    Set<String> studentsResponded = new TreeSet<>();
    Set<String> studentsWithAllCorrect = new TreeSet<>();
    for (List<ItemGradingData> submissionScores : scoresByAssessment.values()) {
      SubmissionOutcome submissionOutcome = getStatisticsService().classifySubmission(item, submissionScores, answersById);
      String agentId = getSubmissionAgentId(submissionScores);
      if (submissionOutcome == SubmissionOutcome.CORRECT) {
        respondedCount++;
        if (agentId != null) {
          studentsResponded.add(agentId);
          studentsWithAllCorrect.add(agentId);
        }
      } else if (submissionOutcome == SubmissionOutcome.INCORRECT) {
        respondedCount++;
        if (agentId != null) {
          studentsResponded.add(agentId);
        }
      } else if (submissionOutcome == SubmissionOutcome.BLANK) {
        blankCount++;
      }
    }

    qbean.setNumResponses(respondedCount);
    qbean.setNumberOfStudentsWithZeroAnswers(blankCount);
    qbean.setStudentsResponded(studentsResponded);
    qbean.setStudentsWithAllCorrect(studentsWithAllCorrect);
  }

  private StatisticsService getStatisticsService() {
    if (statisticsService == null) {
      statisticsService = new StatisticsService();
    }
    return statisticsService;
  }

  void setStatisticsService(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

  private String getSubmissionAgentId(List<ItemGradingData> submissionScores) {
    for (ItemGradingData score : submissionScores) {
      String agentId = score.getAgentId();
      if (StringUtils.isNotBlank(agentId)) {
        return agentId;
      }
    }
    return null;
  }

  /**
   * calculates statistics for EMI questions
   */
  private void getEMIScores(Map publishedItemHash,
			Map publishedAnswerHash, Map emiRequiredCorrectAnswersCount, List scores,
			HistogramQuestionScoresBean qbean, List answers) {

		// Answers keyed by answer-id
		Map answersById = new HashMap();
		
		//keys the number of student responses selecting a particular answer
		//by the Answer ID
		Map results = new HashMap();
		
		//keys Answer-IDs by subQuestion/ItemTextSequence-answerSequence (concatenated)
		Map sequenceMap = new HashMap();
		
		//list of answers for each sub-question/ItemText
		List subQuestionAnswers = null;
		
		//Map which keys above lists by the sub-question/ItemText sequence
		Map subQuestionAnswerMap = new HashMap();
		
		//Create a Map where each Sub-Question's Answers-ArrayList 
		//is keyed by sub-question and answer sequence 
		Iterator iter = answers.iterator();
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			answersById.put(answer.getId(), answer);
			results.put(answer.getId(), Integer.valueOf(0));
			sequenceMap.put(answer.getItemText().getSequence() + "-" + answer.getSequence(), answer.getId());
			Long subQuestionSequence = answer.getItemText().getSequence();
			Object subSeqAns = subQuestionAnswerMap.get(subQuestionSequence);
			if (subSeqAns == null) {
				subQuestionAnswers = new ArrayList();
				subQuestionAnswerMap.put(subQuestionSequence, subQuestionAnswers);
			}
			else {
				subQuestionAnswers = (ArrayList) subSeqAns;
			}
			subQuestionAnswers.add(answer);
		}

		//Iterate through the student answers (ItemGradingData)
		iter = scores.iterator();
		//Create a map that keys all the responses/answers (ItemGradingData) 
		//for this question from a specific student (assessment)
		//by the id of that assessment (AssessmentGradingData)
		Map responsesPerStudentPerQuestionMap = new HashMap();
		//and do the same for seperate sub-questions
		Map responsesPerStudentPerSubQuestionMap = new HashMap();
		while (iter.hasNext()) {
			ItemGradingData data = (ItemGradingData) iter.next();
			//Get the published answer that corresponds to the student's reponse
			AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data
					.getPublishedAnswerId());
			//This should always be the case as only valid responses 
			//from the list of available options are allowed 
			if (answer != null) {
				// found a response
				Integer num = null;
				// num is a counter for the number of responses that select this published answer
				try {
					// we found a response, now get existing count from the
					// hashmap
					num = (Integer) results.get(answer.getId());

				} catch (Exception e) {
					log.warn("No results for " + answer.getId());
				}
				
				//If this published answer has not been selected before
				if (num == null)
					num = Integer.valueOf(0);

				//Now create a map that keys all the responses (ItemGradingData) 
				//for this question from a specific student (or assessment)
				//by the id of that assessment (AssessmentGradingData)
				List studentResponseList = (List) responsesPerStudentPerQuestionMap.get(data.getAssessmentGradingId());
				if (studentResponseList == null) {
					studentResponseList = new ArrayList();
				}
				studentResponseList.add(data);
				responsesPerStudentPerQuestionMap.put(data.getAssessmentGradingId(),
						studentResponseList);
				
				//Do the same for the sub-questions
				String key = data.getAssessmentGradingId() + "-" + answer.getItemText().getId();
				List studentResponseListForSubQuestion = (List) responsesPerStudentPerSubQuestionMap.get(key);
				if (studentResponseListForSubQuestion == null) {
					studentResponseListForSubQuestion = new ArrayList();
				}
				studentResponseListForSubQuestion.add(data);
				responsesPerStudentPerSubQuestionMap.put(key, studentResponseListForSubQuestion);
				
				
				results.put(answer.getId(), Integer.valueOf(
							num.intValue() + 1));
			}
		}
		
		HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
		int[] numarray = new int[results.keySet().size()];
		
		//List of "ItemText.sequence-Answer.sequence"
		List<String> sequenceList = new ArrayList<String>();
		iter = answers.iterator();
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			sequenceList.add(answer.getItemText().getSequence() + "-" + answer.getSequence());
		}
                // sort the sequence
		Collections.sort(sequenceList, new Comparator<String>(){

                    public int compare(String o1, String o2) {
                        Integer a1 = Integer.valueOf(o1.substring(0, o1.indexOf("-")));
                        Integer a2 = Integer.valueOf(o2.substring(0, o2.indexOf("-")));
                        int val = a1.compareTo(a2);
                        if(val != 0){
                            return val;
                        }
                        a1 = Integer.valueOf(o1.substring(o1.indexOf("-")+1));
                        a2 = Integer.valueOf(o2.substring(o2.indexOf("-")+1));
                        return a1.compareTo(a2);
                    }
                });
		// iter = results.keySet().iterator();
		iter = sequenceList.iterator();
		int i = 0;
		int responses = 0;
		int correctresponses = 0;
		while (iter.hasNext()) {
			String sequenceId = (String) iter.next();
			Long answerId = (Long) sequenceMap.get(sequenceId);
			AnswerIfc answer = (AnswerIfc) answersById.get(answerId);
			int num = ((Integer) results.get(answerId)).intValue();
			numarray[i] = num;
			bars[i] = new HistogramBarBean();
			if (answer != null) {
				bars[i].setSubQuestionSequence(answer.getItemText().getSequence());
				if (answer.getItem().getIsAnswerOptionsSimple()) {
					bars[i].setLabel(answer.getItemText().getSequence() + ". " + answer.getLabel() + "  " + answer.getText());
				}
				else { //rich text or attachment options
					bars[i].setLabel(answer.getItemText().getSequence() + ". " + answer.getLabel());
				}

				if (answer.getLabel().equals("A")) {
					String title = rb.getString("item") + " " + answer.getItemText().getSequence();
					String text = answer.getItemText().getText();
					if (text != null && !text.equals(null)) {
						title += " : " + text;
						bars[i].setTitle(title);
					}
				}
				bars[i].setIsCorrect(answer.getIsCorrect());
			}
			
			bars[i].setNumStudentsText(String.valueOf(num));
			bars[i].setNumStudents(num);
			i++;
		}// end while

		responses = responsesPerStudentPerQuestionMap.size();
		
		//Determine the number of students with all correct responses for the whole question
		for (Iterator it = responsesPerStudentPerQuestionMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			List resultsForOneStudent = (List) entry.getValue();

			boolean hasIncorrect = false;
			Iterator listiter = resultsForOneStudent.iterator();

			// iterate through the results for one student
			// for this question (qbean)
			while (listiter.hasNext()) {
				ItemGradingData item = (ItemGradingData) listiter.next();
				
				// only answered choices are created in the
				// ItemGradingData_T, so we need to check
				// if # of checkboxes the student checked is == the number
				// of correct answers
				// otherwise if a student only checked one of the multiple
				// correct answers,
				// it would count as a correct response
				try {
					int corranswers = 0;
					Iterator answeriter = answers.iterator();
					while (answeriter.hasNext()) {
						AnswerIfc answerchoice = (AnswerIfc) answeriter
								.next();
						if (Boolean.TRUE.equals(answerchoice.getIsCorrect())) {
							corranswers++;
						}
					}
					if (resultsForOneStudent.size() != corranswers) {
						hasIncorrect = true;
						break;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(
							"error calculating emi question.");
				}

				// now check each answer
				AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(item
						.getPublishedAnswerId());
				if (answer == null || !Boolean.TRUE.equals(answer.getIsCorrect())) {
					hasIncorrect = true;
					break;
				}
			}

			if (!hasIncorrect) {
				correctresponses = correctresponses + 1;
				qbean.addStudentWithAllCorrect(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId());
			}
			qbean.addStudentResponded(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId());
		} // end for - number of students with all correct responses for the whole question
		
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
			qbean.setPercentCorrect(Integer.toString((int) (((double) correctresponses / (double) responses) * 100)));
		
		Map numStudentsWithAllCorrectPerSubQuestion = new HashMap();
		Map studentsWithAllCorrectPerSubQuestion = new HashMap();
		Map studentsRespondedPerSubQuestion = new HashMap();
		Iterator studentSubquestionResponseKeyIter = responsesPerStudentPerSubQuestionMap.keySet().iterator();
		while (studentSubquestionResponseKeyIter.hasNext()) {
			String key = (String)studentSubquestionResponseKeyIter.next();
			List studentResponseListForSubQuestion = (List) responsesPerStudentPerSubQuestionMap
			.get(key);
			if (studentResponseListForSubQuestion != null && !studentResponseListForSubQuestion.isEmpty()) {
				ItemGradingData response1 = (ItemGradingData)studentResponseListForSubQuestion.get(0);
				AnswerIfc firstResponseAnswer = (AnswerIfc) publishedAnswerHash.get(response1.getPublishedAnswerId());
				if (firstResponseAnswer == null || firstResponseAnswer.getItemText() == null) {
					log.warn("Could not determine EMI sub-question for ItemGradingData with id {}",
							response1.getItemGradingId());
					continue;
				}
				Long subQuestionId = firstResponseAnswer.getItemText().getId();
				
				Set studentsResponded = (Set)studentsRespondedPerSubQuestion.get(subQuestionId);
				if (studentsResponded == null) studentsResponded = new TreeSet();
				studentsResponded.add(response1.getAgentId());
				studentsRespondedPerSubQuestion.put(subQuestionId, studentsResponded);
				
				boolean hasIncorrect = false;
				//numCorrectSubQuestionAnswers = (Integer) correctAnswersPerSubQuestion.get(subQuestionId);
				Integer numCorrectSubQuestionAnswers = (Integer) emiRequiredCorrectAnswersCount.get(subQuestionId);
				if (numCorrectSubQuestionAnswers == null) {
					log.warn("No required correct answer count found for EMI sub-question id {}", subQuestionId);
					continue;
				}
				
				if (studentResponseListForSubQuestion.size() < numCorrectSubQuestionAnswers.intValue()) {
					hasIncorrect = true;
					continue;
				}
				//now check each answer
				Iterator studentResponseIter = studentResponseListForSubQuestion.iterator();
				while (studentResponseIter.hasNext()) {
					ItemGradingData response = (ItemGradingData)studentResponseIter.next();
					AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(response
							.getPublishedAnswerId());
					if (answer == null || !Boolean.TRUE.equals(answer.getIsCorrect())) {
						hasIncorrect = true;
						break;
					}
				}
				if (hasIncorrect) continue;
				Integer numWithAllCorrect = (Integer)numStudentsWithAllCorrectPerSubQuestion.get(subQuestionId);
				if (numWithAllCorrect == null) {
					numWithAllCorrect = Integer.valueOf(0);
				}
				numStudentsWithAllCorrectPerSubQuestion.put(subQuestionId, Integer.valueOf(numWithAllCorrect.intValue() + 1));
				
				Set studentsWithAllCorrect = (Set)studentsWithAllCorrectPerSubQuestion.get(subQuestionId);
				if (studentsWithAllCorrect == null) studentsWithAllCorrect = new TreeSet();
				studentsWithAllCorrect.add(response1.getAgentId());
				studentsWithAllCorrectPerSubQuestion.put(subQuestionId, studentsWithAllCorrect);

			}
			
		}
		
		//Map ItemText sequences to Ids
		Map itemTextSequenceIdMap = new HashMap();
		Iterator answersIter = answers.iterator();
		while (answersIter.hasNext()) {
			AnswerIfc answer = (AnswerIfc)answersIter.next();
			itemTextSequenceIdMap.put(answer.getItemText().getSequence(), answer.getItemText().getId());
		}
		
		//Now select the the bars for each sub-questions	
		Set subQuestionKeySet = subQuestionAnswerMap.keySet();
		List subQuestionKeyList = new ArrayList();
		subQuestionKeyList.addAll(subQuestionKeySet);
		Collections.sort(subQuestionKeyList);
		Iterator subQuestionIter = subQuestionKeyList.iterator();
		List subQuestionInfo = new ArrayList(); //List of sub-question HistogramQuestionScoresBeans - for EMI sub-questions
		  // Iterate through the assessment questions (items)
		  while (subQuestionIter.hasNext()) {
			  Long subQuestionSequence = (Long)subQuestionIter.next();
			  
			  //While qbean is the HistogramQuestionScoresBean for the entire question
			  //questionScores are the HistogramQuestionScoresBeans for each sub-question
			  HistogramQuestionScoresBean questionScores = new HistogramQuestionScoresBean();
			  questionScores.setSubQuestionSequence(subQuestionSequence);
			  
			  // Determine the number of bars (possible answers) for this sub-question
			  int numBars = 0;
			  for (int j=0; j<bars.length; j++) {
				  if (bars[j].getSubQuestionSequence().equals(subQuestionSequence)) {
					  numBars++;
				  }
			  }
			  //Now create an array of that size
			  //and populate it with the bars for this sub-question
			  HistogramBarBean[] subQuestionBars = new HistogramBarBean[numBars];
			  int subBar = 0;
			  for (int j=0; j<bars.length; j++) {
				  if (bars[j].getSubQuestionSequence().equals(subQuestionSequence)) {
					  subQuestionBars[subBar++]=bars[j];
				  }
			  }
			  
			  questionScores.setShowIndividualAnswersInDetailedStatistics(true);
			  questionScores.setHistogramBars(subQuestionBars);
			  questionScores.setNumberOfParts(qbean.getNumberOfParts());
			  //if this part is a randompart , then set randompart = true
			  questionScores.setRandomType(qbean.getRandomType());

			  questionScores.setPartNumber(qbean.getPartNumber());
			  questionScores.setQuestionNumber(qbean.getQuestionNumber()+"-"+subQuestionSequence);
			  
			  questionScores.setQuestionText(qbean.getQuestionText());
			  
			  questionScores.setQuestionType(qbean.getQuestionType());
			  
			  questionScores.setN(qbean.getN());
			  questionScores.setItemId(qbean.getItemId());
			 
			  //This boild down to the number of AssessmentGradingData
			  //So should be the same for whole and sub questions
			  questionScores.setNumResponses(qbean.getNumResponses());

			  Long subQuestionId = (Long)itemTextSequenceIdMap.get(subQuestionSequence);
			  if (questionScores.getNumResponses() > 0) { 
 				  Integer numWithAllCorrect = (Integer)numStudentsWithAllCorrectPerSubQuestion.get(subQuestionId);
 				  if (numWithAllCorrect != null) correctresponses = numWithAllCorrect.intValue();
				  questionScores.setPercentCorrect(Integer.toString((int) (((double) correctresponses / (double) responses) * 100)));
			  }
			  Set studentsWithAllCorrect = (Set)studentsWithAllCorrectPerSubQuestion.get(subQuestionId);
			  questionScores.setStudentsWithAllCorrect(studentsWithAllCorrect);
			  
			  Set studentsResponded = (Set)studentsRespondedPerSubQuestion.get(subQuestionId);
			  questionScores.setStudentsResponded(studentsResponded);
			    
			  subQuestionAnswers = (List) subQuestionAnswerMap.get(subQuestionSequence);
			  Iterator answerIter = subQuestionAnswers.iterator();
			  Double totalScore = new Double(0);
			  while (answerIter.hasNext()) {
				  AnswerIfc subQuestionAnswer = (AnswerIfc) answerIter.next();
				  totalScore += (subQuestionAnswer==null||subQuestionAnswer.getScore()==null?0.0:subQuestionAnswer.getScore());
				  
			  }
			  questionScores.setTotalScore(totalScore.toString());
			  
			  HistogramScoresBean histogramScores = (HistogramScoresBean) ContextUtil.lookupBean("histogramScores");
			    
			  Iterator keys = responsesPerStudentPerSubQuestionMap.keySet().iterator();
			  int numSubmissions = 0;
			  while (keys.hasNext()) {
				  String assessmentAndSubquestionId = (String)keys.next();
				  if (assessmentAndSubquestionId.endsWith("-"+subQuestionId)) numSubmissions++;
			  }
			  int percent27 = numSubmissions*27/100; // rounded down
			  if (percent27 == 0) percent27 = 1;
			  
			  studentsWithAllCorrect = questionScores.getStudentsWithAllCorrect();
			  studentsResponded = questionScores.getStudentsResponded();
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
				  
				  double percentCorrectFromUpperQuartileStudents = 
					  ((double) numStudentsWithAllCorrectFromUpperQuartile / 
							  (double) percent27) * 100d;

				  double percentCorrectFromLowerQuartileStudents = 
					  ((double) numStudentsWithAllCorrectFromLowerQuartile / 
							  (double) percent27) * 100d;

				  questionScores.setPercentCorrectFromUpperQuartileStudents(
						  Integer.toString((int) percentCorrectFromUpperQuartileStudents));
				  questionScores.setPercentCorrectFromLowerQuartileStudents(
						  Integer.toString((int) percentCorrectFromLowerQuartileStudents));

				  double numResponses = (double)questionScores.getNumResponses();

				  double discrimination = ((double)numStudentsWithAllCorrectFromUpperQuartile -								  
						  (double)numStudentsWithAllCorrectFromLowerQuartile)/(double)percent27 ;
				  
				  // round to 2 decimals
				  if (discrimination > 999999 || discrimination < -999999) {
					  questionScores.setDiscrimination("NaN");
				  }
				  else {
					  discrimination = ((int) (discrimination*100.00)) / 100.00;
					  questionScores.setDiscrimination(Double.toString(discrimination));
				  }
			  }

			  subQuestionInfo.add(questionScores);
		  } // end-while - items
		qbean.setInfo(subQuestionInfo);
		
	}
  
  private void getFIBMCMCScores(Map publishedItemHash, Map publishedAnswerHash, List scores, HistogramQuestionScoresBean qbean, List answers, ItemDataIfc itemData) {
		Map texts = new HashMap();
		Iterator iter = answers.iterator();
		Map<Long, Integer> results = new HashMap();
		Map numStudentRespondedMap = new HashMap();
		Map sequenceMap = new HashMap();
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			texts.put(answer.getId(), answer);
			results.put(answer.getId(), Integer.valueOf(0));
			sequenceMap.put(answer.getSequence(), answer.getId());
		}
		iter = scores.iterator();
		boolean isFIB = qbean.getQuestionType().equals(TypeIfc.FILL_IN_BLANK.toString());
		boolean isFIN = qbean.getQuestionType().equals(TypeIfc.FILL_IN_NUMERIC.toString());
		while (iter.hasNext()) {
			ItemGradingData data = (ItemGradingData) iter.next();
			AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data
					.getPublishedAnswerId());
			if (answer != null) {
				boolean hasInput = !isFIB && !isFIN || StringUtils.isNotBlank(data.getAnswerText());
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

				if (hasInput) {
					List studentResponseList = (List) numStudentRespondedMap
							.get(data.getAssessmentGradingId());
					if (studentResponseList == null) {
						studentResponseList = new ArrayList();
					}
					studentResponseList.add(data);
					numStudentRespondedMap.put(data.getAssessmentGradingId(),
							studentResponseList);
				}
				// we found a response, and got the existing num , now update
				// one
				if (isFIB) {
					// for fib we only count the number of correct responses
					if (hasInput && delegate.getFIBResult(data, new HashMap<Long, Set<String>>(), itemData, publishedAnswerHash)) {
						results.merge(answer.getId(), 1, Integer::sum);
					}
				} else if (isFIN) {
					if (hasInput && delegate.getFINResult(data, itemData, publishedAnswerHash)) {
						results.merge(answer.getId(), 1, Integer::sum);
					}
				} else {
					// for mc, we count the number of all responses
					results.merge(answer.getId(), 1, Integer::sum);
				}
			}
		}
		HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
		int[] numarray = new int[results.keySet().size()];
		List sequenceList = new ArrayList();
		iter = answers.iterator();
		while (iter.hasNext()) {
			AnswerIfc answer = (AnswerIfc) iter.next();
			sequenceList.add(answer.getSequence());
		}

		Collections.sort(sequenceList);
		iter = sequenceList.iterator();
		int i = 0;
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
			bars[i].setNumStudentsText(String.valueOf(num));
		        bars[i].setNumStudents(num);
			i++;
		}
		
		for (Iterator it = numStudentRespondedMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			List resultsForOneStudent = (List) entry.getValue();

			boolean hasIncorrect = false;
			Iterator listiter = resultsForOneStudent.iterator();

			// iterate through the results for one student
			// for this question (qbean)
			while (listiter.hasNext()) {
				ItemGradingData item = (ItemGradingData) listiter.next();
				
				if (qbean.getQuestionType().equals("8") && !delegate.getFIBResult(item, new HashMap<Long, Set<String>>(), itemData, publishedAnswerHash)) {
					hasIncorrect = true;
					break;
				} else if (qbean.getQuestionType().equals("11") && !delegate.getFINResult(item, itemData, publishedAnswerHash)) {
					hasIncorrect = true;
					break;
				} else if (qbean.getQuestionType().equals("2")) { // mcmc

					// only answered choices are created in the
					// ItemGradingData_T, so we need to check
					// if # of checkboxes the student checked is == the number
					// of correct answers
					// otherwise if a student only checked one of the multiple
					// correct answers,
					// it would count as a correct response

					try {
						List itemTextArray = ((ItemDataIfc) publishedItemHash
								.get(item.getPublishedItemId()))
								.getItemTextArraySorted();
						List answerArray = ((ItemTextIfc) itemTextArray
								.get(0)).getAnswerArraySorted();

						int corranswers = 0;
						Iterator answeriter = answerArray.iterator();
						while (answeriter.hasNext()) {
							AnswerIfc answerchoice = (AnswerIfc) answeriter
									.next();
							if (Boolean.TRUE.equals(answerchoice.getIsCorrect())) {
								corranswers++;
							}
						}
						if (resultsForOneStudent.size() != corranswers) {
							hasIncorrect = true;
							break;
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						throw new RuntimeException(
								"error calculating mcmc question.");
					}

					// now check each answer in MCMC

					AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(item
							.getPublishedAnswerId());
					if (answer == null || !Boolean.TRUE.equals(answer.getIsCorrect())) {
						hasIncorrect = true;
						break;
					}
				}
			}

			
			if (!hasIncorrect) {
				correctresponses = correctresponses + 1;
				
				qbean.addStudentWithAllCorrect(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId()); 
			}
			qbean.addStudentResponded(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId()); 
		}
		// NEW
		int[] heights = calColumnHeight(numarray, qbean.getNumResponses());
		// int[] heights = calColumnHeight(numarray);
		for (i = 0; i < bars.length; i++)
		{
			try
			{
				bars[i].setColumnHeight(Integer.toString(heights[i]));
			}
			catch(NullPointerException npe)
			{
				log.warn("bars[" + i + "] is null. " + npe);
			}
		}

		qbean.setHistogramBars(bars);
		if (qbean.getNumResponses() > 0)
			qbean
					.setPercentCorrect(Integer
							.toString((int) (((double) correctresponses / (double) qbean.getNumResponses()) * 100)));
	}

  private void getTFMCScores(Map publishedAnswerHash, List scores, HistogramQuestionScoresBean qbean, List answers) {
		Map<Long, AnswerIfc> texts = new HashMap<>();
		Map<Long, Integer> results = new HashMap<>();
		Map<Long, Long> sequenceMap = new HashMap<>();

		if (answers != null) {
			Iterator<AnswerIfc> iter = answers.iterator();
		
			// create the lookup maps
			while (iter.hasNext()) {
				AnswerIfc answer = iter.next();
				texts.put(answer.getId(), answer);
				results.put(answer.getId(), 0);
				sequenceMap.put(answer.getSequence(), answer.getId());
			}
	
			// find the number of responses (ItemGradingData) for each answer
            for (ItemGradingData data : (Iterable<ItemGradingData>) scores) {
                AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data
                        .getPublishedAnswerId());

                if (answer != null) {
                    // found a response
                    Integer num = null;
                    // num is a counter
                    try {
                        // we found a response, now get existing count from the
                        // hashmap
                        num = results.get(answer.getId());

                    } catch (Exception e) {
                        log.warn("No results for answer id: {}", answer.getId());
                        log.error(e.getMessage(), e);
                    }
                    if (num == null)
                        num = 0;

                    // we found a response, and got the existing num , now update
                    // one
                    // check here for the other bug about non-autograded items
                    // having 1 even with no responses
                    results.put(answer.getId(), num + 1);


                    // this should work because for tf/mc(single)
                    // questions, there should be at most
                    // one submitted answer per student/assessment
                    if (answer.getIsCorrect() != null && answer.getIsCorrect()) {
                        qbean.addStudentWithAllCorrect(data.getAgentId());
                    }
                    qbean.addStudentResponded(data.getAgentId());

                }
            }
			
			List<Long> sequenceList = new ArrayList<>();
			
			// get an arraylist of answer sequences
			iter = answers.iterator();
			while (iter.hasNext()) {
				AnswerIfc answer = iter.next();
				sequenceList.add(answer.getSequence());
			}
	
			// sort the sequences
			Collections.sort(sequenceList);
			Iterator<Long> sequenceIter = sequenceList.iterator();
			int i = 0;
			int correctresponses = 0;
			List<HistogramBarBean> barList = new ArrayList<>();
			List<Integer> numList = new ArrayList<>();
	
			// find answers sorted by sequence
			while (sequenceIter.hasNext()) {
				Long sequenceId = sequenceIter.next();
				Long answerId = sequenceMap.get(sequenceId);
				AnswerIfc answer = texts.get(answerId);

				if (StringUtils.isBlank(answer.getText())) continue;
				
				int num = results.get(answerId);
				HistogramBarBean bar = new HistogramBarBean();
				if (qbean.getQuestionType().equals("4")) { // true-false
					String origText = answer.getText();
					String text = "";
					if ("true".equals(origText)) {
						text = rb.getString("true_msg");
					} else {
						text = rb.getString("false_msg");
					}
					bar.setLabel(text);
				} else {
					bar.setLabel(answer.getText());
				}
				bar.setIsCorrect(answer.getIsCorrect());
				bar.setNumStudentsText(String.valueOf(num));
				bar.setNumStudents(num);
				if (answer.getIsCorrect() != null && answer.getIsCorrect()) {
					correctresponses += num;
				}
				barList.add(bar);
				numList.add(num);
			}
			int[] numarray = new int[numList.size()];
			HistogramBarBean[] bars = new HistogramBarBean[barList.size()];
			for (i = 0; i < numList.size(); i++) {
				numarray[i] = numList.get(i);
				bars[i] = barList.get(i);
			}
			int[] heights = calColumnHeight(numarray, qbean.getNumResponses());
			for (i = 0; i < bars.length; i++) {
				bars[i].setColumnHeight(Integer.toString(heights[i]));
				}
			qbean.setHistogramBars(bars);
			if (qbean.getNumResponses() > 0)
				qbean
						.setPercentCorrect(Integer
								.toString((int) (((double) correctresponses / (double) qbean.getNumResponses()) * 100)));
		
		}
	}

	private void getCalculatedQuestionScores(List<ItemGradingData> scores, HistogramQuestionScoresBean qbean, ItemDataIfc item) {
		final String CORRECT = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","correct");
		final String INCORRECT = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","incorrect");
		final int COLUMN_MAX_HEIGHT = 100;

		Map<String, Integer> results = new LinkedHashMap<>();
		results.put(CORRECT, Integer.valueOf(0));
		results.put(INCORRECT, Integer.valueOf(0));

		if (!scores.isEmpty()) { // not every question may have an answer i.e. randomly drawn questions
			Map<Long, List<ItemGradingData>> scoresByAssessment = new HashMap<>();
			for (ItemGradingData score : scores) {
				Long assessmentGradingId = score.getAssessmentGradingId();
				List<ItemGradingData> list = scoresByAssessment.get(assessmentGradingId);
				if (list == null) {
					list = new ArrayList<>();
					scoresByAssessment.put(assessmentGradingId, list);
				}
				list.add(score);
			}

			for (Map.Entry<Long, List<ItemGradingData>> entry : scoresByAssessment.entrySet()) {
				List<ItemGradingData> submissionScores = new ArrayList<>(entry.getValue());
				if (submissionScores.isEmpty()) {
					continue;
				}
				submissionScores.sort(Comparator.comparing(ItemGradingData::getPublishedAnswerId,
					Comparator.nullsLast(Long::compareTo)));
				int totalParts = submissionScores.size();
				int correctParts = 0;
				int blankParts = 0;
				int answerSequence = 0;
				Long previousAnswerId = null;
				Map<Integer, String> answersMap = new HashMap<>();
				LinkedHashMap<String, String> answersMapValues = new LinkedHashMap<>();
				LinkedHashMap<String, String> globalanswersMapValues = new LinkedHashMap<>();
				LinkedHashMap<String, String> mainvariablesWithValues = new LinkedHashMap<>();

				for (ItemGradingData score : submissionScores) {
					Long currentAnswerId = score.getPublishedAnswerId();
					if (currentAnswerId == null || StringUtils.isBlank(score.getAnswerText())) {
						blankParts++;
						continue;
					}
					if (!Objects.equals(previousAnswerId, currentAnswerId)) {
						answerSequence++;
						previousAnswerId = currentAnswerId;
					}
					if (isCalculatedPartCorrect(score, item, answerSequence, answersMap, answersMapValues, globalanswersMapValues, mainvariablesWithValues)) {
						correctParts++;
					}
				}

				int attemptedParts = totalParts - blankParts;
				if (attemptedParts <= 0) {
					continue;
				}

				String agentId = submissionScores.get(0).getAgentId();
				qbean.addStudentResponded(agentId);
				if (correctParts == attemptedParts) {
					results.merge(CORRECT, 1, Integer::sum);
					qbean.addStudentWithAllCorrect(agentId);
				} else {
					results.merge(INCORRECT, 1, Integer::sum);
				}
			}
		}

		// build the histogram bar for correct/incorrect answers
		int totalResponses = results.getOrDefault(CORRECT, 0) + results.getOrDefault(INCORRECT, 0);
		List<HistogramBarBean> barList = new ArrayList<>();
		for (Map.Entry<String, Integer> resultEntry : results.entrySet()) {
			HistogramBarBean bar = new HistogramBarBean();
			bar.setLabel(resultEntry.getKey());
			bar.setNumStudents(resultEntry.getValue());
			bar.setNumStudentsText(resultEntry.getValue() + " " + resultEntry.getKey());
			bar.setIsCorrect(resultEntry.getKey().equals(CORRECT));
			int height = 0;
			if (totalResponses > 0) {
				height = COLUMN_MAX_HEIGHT * resultEntry.getValue() / totalResponses;
			}
			bar.setColumnHeight(Integer.toString(height));
			barList.add(bar);
		}

		HistogramBarBean[] bars = new HistogramBarBean[barList.size()];
		bars = barList.toArray(bars);
		qbean.setHistogramBars(bars);

		if (totalResponses > 0) {
			double percentCorrect = ((double) results.getOrDefault(CORRECT, 0) / (double) totalResponses) * 100;
			String percentCorrectStr = Integer.toString((int) percentCorrect);
			qbean.setPercentCorrect(percentCorrectStr);
		}
	}

	private boolean isCalculatedPartCorrect(ItemGradingData score, ItemDataIfc item, int answerSequence,
			Map<Integer, String> answersMap, LinkedHashMap<String, String> answersMapValues,
			LinkedHashMap<String, String> globalanswersMapValues, LinkedHashMap<String, String> mainvariablesWithValues) {
		if (score.getIsCorrect() != null) {
			return score.getIsCorrect();
		}

		Double autoScore = score.getAutoScore();
		if (autoScore != null) {
			return autoScore > 0;
		}

		// Legacy safety fallback when no persisted correctness is available.
		delegate.extractCalcQAnswersArray(answersMap, answersMapValues, globalanswersMapValues, mainvariablesWithValues,
				item, score.getAssessmentGradingId(), score.getAgentId());
		return delegate.getCalcQResult(score, item, answersMap, answerSequence);
	}

	private void getImageMapQuestionScores(Map publishedItemTextHash, Map publishedAnswerHash,
	    List scores, HistogramQuestionScoresBean qbean, List labels)
	  {
		Map texts = new HashMap();
	    Iterator iter = labels.iterator();
	    Map results = new HashMap();
	    Map numStudentRespondedMap= new HashMap();
	    Map sequenceMap = new HashMap();
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
	       
	      if (text != null)
	      {
	        Integer num = (Integer) results.get(text.getId());
	        if (num == null)
	          num = Integer.valueOf(0);

	        List studentResponseList = (List)numStudentRespondedMap.get(data.getAssessmentGradingId());
	        if (studentResponseList==null) {
	            studentResponseList = new ArrayList();
	        }
	        studentResponseList.add(data);
	        numStudentRespondedMap.put(data.getAssessmentGradingId(), studentResponseList);
	        //if (answer.getIsCorrect() != null && answer.getIsCorrect().booleanValue())
	        if (data.getIsCorrect() != null && data.getIsCorrect().booleanValue())
	        // only store correct responses in the results
	        {
	          results.put(text.getId(), Integer.valueOf(num.intValue() + 1));
	        }
	      }
	    }

	    HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
	    int[] numarray = new int[results.keySet().size()];
	    List sequenceList = new ArrayList();
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
	      bars[i].setIsCorrect(((Integer) results.get(textId)) >= 1);
	      bars[i].setNumStudents(num);
	      bars[i].setNumStudentsText(String.valueOf(num));

	      i++;
	    }

	    // now calculate correctresponses
	    // correctresponses = # of students who got all answers correct, 
	    
	    for (Iterator it = numStudentRespondedMap.entrySet().iterator(); it.hasNext();) {
	    	Map.Entry entry = (Map.Entry) it.next();
	     	List resultsForOneStudent = (List) entry.getValue();
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
	          //AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(item.getPublishedAnswerId());
	          if (item.getIsCorrect() == null || (!item.getIsCorrect().booleanValue()))
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
	    int[] heights = calColumnHeight(numarray, qbean.getNumResponses());
	    //  int[] heights = calColumnHeight(numarray);
	    
	    for (i=0; i<bars.length; i++) {
	    	try {
	    		bars[i].setColumnHeight(Integer.toString(heights[i]));
	    	}
	    	catch (NullPointerException npe) {
	    		log.warn("bars[" + i + "] is null. " + npe);
	    	}
	    }	
	    
	    qbean.setHistogramBars(bars);
	    if (qbean.getNumResponses() > 0)
	      qbean.setPercentCorrect(Integer.toString((int)(((double) correctresponses/(double) qbean.getNumResponses()) * 100)));
	  }

  private void getMatchingScores(Map publishedItemTextHash, Map publishedAnswerHash,
		  List scores, HistogramQuestionScoresBean qbean, List labels)
  {
    Map texts = new HashMap();
    Iterator iter = labels.iterator();
    Map<Long, Integer> results = new HashMap();
    Map numStudentRespondedMap= new HashMap();
    Map sequenceMap = new HashMap();
    List<Long> distractors = new ArrayList<>();
    
    while (iter.hasNext())
    {
      ItemTextIfc label = (ItemTextIfc) iter.next();
      texts.put(label.getId(), label);
      results.put(label.getId(), Integer.valueOf(0));
      sequenceMap.put(label.getSequence(), label.getId());
      if ( delegate.isDistractor(label)){
          distractors.add(label.getId());
      }
    }
    iter = scores.iterator();

    while (iter.hasNext())
    {
      ItemGradingData data = (ItemGradingData) iter.next();
      ItemTextIfc text = (ItemTextIfc) publishedItemTextHash.get(data.getPublishedItemTextId());
      AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
      if (answer != null)
      {
        List studentResponseList = (List) numStudentRespondedMap.get(data.getAssessmentGradingId());
        if (studentResponseList==null) {
            studentResponseList = new ArrayList();
        }
        studentResponseList.add(data);
        numStudentRespondedMap.put(data.getAssessmentGradingId(), studentResponseList);

        if (answer.getIsCorrect() != null && answer.getIsCorrect().booleanValue())
        // only store correct responses in the results
        {
          results.merge(text.getId(), 1, Integer::sum);
        }
      } else if(data.getPublishedAnswerId() != null && data.getPublishedAnswerId() < 0 && distractors.contains(text.getId())) {
        results.merge(text.getId(), 1, Integer::sum);
      }
    }

    HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
    int[] numarray = new int[results.keySet().size()];
    List sequenceList = new ArrayList();
    iter = labels.iterator();
    while (iter.hasNext())
    {
      ItemTextIfc label = (ItemTextIfc) iter.next();
      sequenceList.add(label.getSequence());
    }
     
    Collections.sort(sequenceList);
    iter = sequenceList.iterator();
    int i = 0;
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
      bars[i].setNumStudentsText(String.valueOf(num));

      i++;
    }

    // now calculate correctresponses
    // correctresponses = # of students who got all answers correct,
    int numberOfRealChoices = labels.size() - distractors.size();
    for (Iterator it = numStudentRespondedMap.entrySet().iterator(); it.hasNext();) {
    	Map.Entry entry = (Map.Entry) it.next();
     	List resultsForOneStudent = (List) entry.getValue();
    	boolean hasIncorrectMatches = false;
    	Iterator listiter = resultsForOneStudent.iterator();
    	int correctMatchesCount = 0;

	      while (listiter.hasNext()){
	          ItemGradingData item = (ItemGradingData)listiter.next();
	          
	          if (!delegate.isDistractor((ItemTextIfc) publishedItemTextHash.get(item.getPublishedItemTextId()))){
	              // now check each answer in Matching 
	              AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(item.getPublishedAnswerId());
	              if (answer == null || !Boolean.TRUE.equals(answer.getIsCorrect())){
	                  hasIncorrectMatches = true;
	                  break;
	              }else{
	                  correctMatchesCount++;
	              }
          }
      }
      
      if (!hasIncorrectMatches && correctMatchesCount ==  numberOfRealChoices) {
        correctresponses = correctresponses + 1;
		qbean.addStudentWithAllCorrect(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId());
	  }
	  qbean.addStudentResponded(((ItemGradingData)resultsForOneStudent.get(0)).getAgentId());
    }

    //NEW
    int[] heights = calColumnHeight(numarray, qbean.getNumResponses());
    //  int[] heights = calColumnHeight(numarray);
    
    for (i=0; i<bars.length; i++) {
    	try {
    		bars[i].setColumnHeight(Integer.toString(heights[i]));
    	}
    	catch (NullPointerException npe) {
    		log.warn("bars[" + i + "] is null. " + npe);
    	}
    }	
    
    qbean.setHistogramBars(bars);
    if (qbean.getNumResponses() > 0)
      qbean.setPercentCorrect(Integer.toString((int)(((double) correctresponses/(double) qbean.getNumResponses()) * 100)));
  }

  private void getMatrixSurveyScores(Map publishedItemTextHash, Map publishedAnswerHash,
		  List scores, HistogramQuestionScoresBean qbean, List labels)
  {
	  Map<Long, ItemTextIfc> texts = new LinkedHashMap<>();
	  Map rows = new HashMap();
	  Map answers = new HashMap();
	  Map numStudentRespondedMap = new HashMap();


	  Iterator iter = labels.iterator();
	  // create labels(rows) and HashMap , rows has the total count of response for that row
	  while (iter.hasNext()) {
		  ItemTextIfc label = (ItemTextIfc) iter.next();
		  texts.put(label.getId(), label);
		  rows.put(label.getId(), Integer.valueOf(0));
		  // sequenceMap.put(label.getSequence(), label.getId());
	  }
	  // result only contains the row information, I should have another HashMap to store the Answer results
	  // find the number of responses (ItemGradingData) for each answer
	  iter = scores.iterator();
	  while (iter.hasNext()) {
		  ItemGradingData data = (ItemGradingData) iter.next();

		  AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data
				  .getPublishedAnswerId());

		  if (answer != null) {
			  Integer num = null;
			  // num is a counter
			  try {
				  // we found a response, now get existing count from the hashmap
				  // num = (Integer) results.get(answer.getId());
				  num = (Integer) answers.get(answer.getId());

			  } catch (Exception e) {
				  log.warn("No results for " + answer.getId());
				  log.error(e.getMessage());
			  }
			  if (num == null)
				  num = Integer.valueOf(0);
			  answers.put(answer.getId(), Integer.valueOf(num.intValue() + 1));
			  Long id = ((ItemTextIfc)answer.getItemText()).getId();
			  Integer rCount = null;
			  try {
				  rCount = (Integer)rows.get(id);
			  } catch (Exception e) {
				  log.warn("No results for " + id);
				  log.error(e.getMessage());
			  }
			  
			  if(rCount != null)
				  rows.put(id, Integer.valueOf(rCount.intValue()+1));
		  }
		  List studentResponseList = (List)numStudentRespondedMap.get(data.getAssessmentGradingId());
		  if (studentResponseList==null) {
			  studentResponseList = new ArrayList();
		  }
		  studentResponseList.add(data);
		  numStudentRespondedMap.put(data.getAssessmentGradingId(), studentResponseList);
		  qbean.addStudentResponded(data.getAgentId());
	  }

	  //create the arraylist for answer text
	  List answerTextList = new ArrayList<String>();
	  iter = publishedAnswerHash.keySet().iterator();
	  boolean isIn = false;
	  while(iter.hasNext()){
		  Long id = (Long)iter.next();
		  AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(id);
		  if (!qbean.getItemId().equals(answer.getItem().getItemId())) {
			  continue;
		  }
		  isIn = false;
		  for(int i=0; i< answerTextList.size(); i++){
			  if((((String)answer.getText()).trim()).equals(((String)answerTextList.get(i)).trim())){
				  isIn = true;
				  break;
			  }			
		  }
		  if(!isIn){
			  String ansStr = answer.getText().trim();
			  answerTextList.add(ansStr);
		  }
	  }
	Collections.sort(answerTextList);


	  //create the HistogramBarBean
	  List<HistogramBarBean> histogramBarList = new ArrayList<HistogramBarBean>();
	  iter = texts.keySet().iterator();
	  while (iter.hasNext()){
		  Long id = (Long)iter.next();
		  HistogramBarBean gramBar = new HistogramBarBean();
		  ItemTextIfc ifc = (ItemTextIfc)texts.get(id);
		  Integer totalCount = (Integer)rows.get(id);
		  gramBar.setLabel(ifc.getText());
		  //add each small beans
		  List<ItemBarBean> itemBars = new ArrayList<ItemBarBean>();

		  for(int i=0; i< answerTextList.size(); i++){
			  ItemBarBean barBean = new ItemBarBean();
			  int count = 0;
			  //get the count of checked
			  //1. first get the answerId for ItemText+AnswerText combination
			  Iterator iter1 = publishedAnswerHash.keySet().iterator();
			  while(iter1.hasNext()){
				  Long id1 = (Long)iter1.next();
				  AnswerIfc answer1 = (AnswerIfc)publishedAnswerHash.get(id1);
				  //answer1.getText() + answer1.getItemText().getText() + ifc.getText() + answer1.getId());
				  
				  // bjones86 - SAM-2232 - null checks
				  if( answer1 == null ) {
					  continue;
				  }
				  ItemTextIfc answer1ItemText = answer1.getItemText();
				  if( answer1ItemText == null ) {
					  continue;
				  }
				  String answer1Text = answer1.getText();
				  String answer1ItemTextText = answer1ItemText.getText();
				  if( answer1Text == null || answer1ItemTextText == null ) {
					  continue;
				  }
				  
				  String answerText = (String) answerTextList.get( i );
 				  String ifcText = ifc.getText();
 				  if(answer1Text.equals(answerText) && answer1ItemTextText.equals(ifcText)) {
					  //2. then get the count from HashMap
					  if(answers.containsKey(answer1.getId())) {
						  count =((Integer) answers.get(answer1.getId())).intValue();
						  break;
					  }
				  }
			  }

			  barBean.setNumStudentsText(String.valueOf(count));
			  //2. get the answer text
			  barBean.setItemText((String)answerTextList.get(i));

			  //3. set the columnHeight
			  int height= 0;
			  if (totalCount.intValue() != 0)
				  height = 300 * count/totalCount.intValue();
			  barBean.setColumnHeight(Integer.toString(height));
			  itemBars.add(barBean);
		  }
		  gramBar.setItemBars(itemBars);
		  histogramBarList.add(gramBar);
	  }

	  qbean.setHistogramBars(histogramBarList.toArray(new HistogramBarBean[histogramBarList.size()]));
	  qbean.setNumResponses(numStudentRespondedMap.size());
  }	

  private void doScoreStatistics(HistogramQuestionScoresBean qbean, List scores)
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
	bars[i].setNumStudentsText("" + qbean.getNumStudentCollection()[i]);
        bars[i].setRangeInfo(qbean.getRangeCollection()[i]);
        bars[i].setLabel(qbean.getRangeCollection()[i]);
      }
    }
      qbean.setHistogramBars(bars);
      qbean.setPercentCorrect(Integer.toString(resolveScoreStatisticsPercentCorrect(qbean)));
    }
      catch (IllegalAccessException e) {
		log.error(e.getMessage(), e);
	} catch (InvocationTargetException e) {
		log.error(e.getMessage(), e);
	}
  }

  private int resolveScoreStatisticsPercentCorrect(HistogramQuestionScoresBean qbean) {
    if (qbean == null || qbean.getNumResponses() <= 0) {
      return 0;
    }
    double meanScore = NumberUtils.toDouble(qbean.getMean(), 0d);
    double maxScoreForQuestion = NumberUtils.toDouble(qbean.getTotalScore(), 0d);
    return calculatePercentCorrect(meanScore, maxScoreForQuestion);
  }

  private int calculatePercentCorrect(double numerator, double denominator) {
    if (denominator <= 0d) {
      return 0;
    }
    int percentCorrect = (int) ((numerator / denominator) * 100d);
    if (percentCorrect < 0) {
      return 0;
    }
    if (percentCorrect > 100) {
      return 100;
    }
    return percentCorrect;
  }

  private Map getAssessmentStatisticsMap(List scoreList)
  {
    // this function is used to calculate stats for an entire assessment
    // or for a non-autograded question
    // depending on data's instanceof 

    Iterator iter = scoreList.iterator();
    List<Double> doubles = new ArrayList<>();
    while (iter.hasNext())
    {
      Object data = iter.next();
      if (data instanceof AssessmentGradingData) {
    	  Double finalScore = ((AssessmentGradingData) data).getFinalScore();
    	  if (finalScore == null) {
    		  finalScore = Double.valueOf("0");
    	  }
        doubles.add(finalScore);
      }
      else
      {
        double autoScore = (double) 0.0;
        if (((ItemGradingData) data).getAutoScore() != null)
          autoScore = ((ItemGradingData) data).getAutoScore().doubleValue();
        double overrideScore = (double) 0.0;
        if (((ItemGradingData) data).getOverrideScore() != null)
          overrideScore =
            ((ItemGradingData) data).getOverrideScore().doubleValue();
        doubles.add(Double.valueOf(autoScore + overrideScore));
      }
    }

    if (doubles.isEmpty())
      doubles.add(new Double(0.0));

    doubles.sort(Comparator.naturalOrder());

    double[] scores = new double[doubles.size()];
    int i = 0;
    for (Double d : doubles) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        scores[i++] = bd.doubleValue();
    }

    Map statMap = new HashMap();

    double min = scores[0];
    double max = scores[scores.length - 1];
    double total = calTotal(scores);
    double mean = calMean(scores, total);
    int interval = 0;
    interval = calInterval(min, max); // SAM-2409
    int[] numStudents = calNumStudents(scores, min, max, interval);
   
    statMap.put("maxScore", castingNum(max,2));
    statMap.put("interval", Integer.valueOf(interval));
    statMap.put("numResponses", Integer.valueOf(scoreList.size()));
    // statMap.put("numResponses", Integer.valueOf(scores.length));

    statMap.put("totalScore",castingNum(total,2));
    statMap.put("mean", castingNum(mean,2));
    double median = calMedian(scores);
    statMap.put("median", castingNum(median, 2));
    statMap.put("mode", castingNumForMode(calMode(scores)));

    statMap.put("numStudentCollection", numStudents);
    statMap.put(
      "rangeCollection", calRange(numStudents, min, max, interval));
    double standardDeviation = calStandDev(scores, mean);
    statMap.put("standDev", castingNum(standardDeviation, 2));
    statMap.put("skewnessCoefficient", castingNum(calcSkewnessCoefficient(mean, median, standardDeviation), 2));
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

  private static double calcSkewnessCoefficient(double mean, double median, double standardDeviation) {
    return (3 * (mean - median)) / standardDeviation;
  }

  //               incorrect + blank
  // difficulty = ------------------- * 100
  //                       total
  private int calcDifficulty(long totalCount, long incorrectCount, long blankCount) {
    // Can not calculate difficulty with totalCount smaller than 1
    if (totalCount <= 0) {
        return -1;
    }

    return BigDecimal.valueOf(incorrectCount + blankCount)
      .multiply(BigDecimal.valueOf(100L))
      .divide(BigDecimal.valueOf(totalCount), 0, RoundingMode.HALF_UP)
      .intValue();
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
  private static int calInterval(double min, double max) // SAM-2409
  {
    int interval;

    if((max - min) < 10)
    {
      interval = 1;
    }
    else
    {
      interval = (int) Math.ceil((Math.ceil(max) - Math.floor(min)) / 10); // SAM-2409
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
    double[] scores, double min, double max, int interval){

    if(min > max){
        max = min;
    }

    min = Math.floor(min); // SAM-2409
    max = Math.ceil(max); // SAM-2409

    int[] numStudents = new int[(int) Math.ceil((max - min) / interval)];

    // this handles a case where there are no num students, treats as if
    // a single value of 0
    if(numStudents.length == 0){
        numStudents = new int[1];
        numStudents[0] = 0;
    }

    for(int i = 0; i < scores.length; i++){
        if(scores[i] < (min + interval)){
            numStudents[0]++;
        }else{
            for(int j = 1; j < (numStudents.length); j++){

                double lowerEndpoint = min + (j * interval);
                double uppperEndpoint =  min + ((j + 1) * interval);

                if ( j < (numStudents.length -1) ){
                    if( (scores[i] >= lowerEndpoint ) && (scores[i] < uppperEndpoint) ){
                        numStudents[j]++;
                        break;
                    }
                }else{
                    if( (scores[i] >= lowerEndpoint ) && (scores[i] <= uppperEndpoint) ){
                        numStudents[j]++;
                        break;
                    }
                }
            }
        }
    }

    return numStudents;
  }

  /**
   * Calculate range strings for each interval.
   *
   * @param numStudents number of students for each interval
   * @param min the minimum
   * @param max the maximum
   * @param interval the number of intervals
   *
   * @return array of range strings for each interval.
   */
  private static String[] calRange(int[] numStudents, double min, double max, int interval)
  {
    String[] ranges = new String[numStudents.length];

    if(Double.compare(max, min) == 0){
      String num = castingNum(min,2);
      ranges[0] = "[ " + num + " , " + num + " ]";
    }
    else
    {	
      ranges[0] = "[ " +(int) min + " , " + (int) (min + interval) + " )";

      Integer nextVal = null;
      for(int i=1; i < ranges.length; i++)
      {
        nextVal = new Integer((int) (((i + 1) * interval) + min));

        if(nextVal.doubleValue() < max)
        {
          ranges[i] = "[ " + (int) ((i * interval) + min) + " , " + nextVal + " )";
        }
        else
        {
          ranges[i] = "[ " + (int) ((i * interval) + min) + " , " + castingNum(max,2) + " ]";
        }

      }
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
	    height[index] = (int)((100*numStudents[index])/totalResponse);
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
    private static String castingNum(double number,int decimal)
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
	  if (typeId == TypeIfc.MULTIPLE_CHOICE.intValue()) {
		  return rc.getString("multiple_choice_sin");
	  }
	  if (typeId == TypeIfc.MULTIPLE_CORRECT.intValue()) {
		  return rc.getString("multipl_mc_ms");
	  }
	  if (typeId == TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.intValue()) {
		  return rc.getString("multipl_mc_ss");
	  }
	  if (typeId == TypeIfc.MULTIPLE_CHOICE_SURVEY.intValue()) {
		  return rb.getString("q_mult_surv");
	  }
	  if (typeId == TypeIfc.TRUE_FALSE.intValue()) {
		  return rb.getString("q_tf");
	  }
	  if (typeId == TypeIfc.ESSAY_QUESTION.intValue()) {
		  return rb.getString("q_short_ess");
	  }
	  if (typeId == TypeIfc.FILE_UPLOAD.intValue()) {
		  return rb.getString("q_fu");
	  }
	  if (typeId == TypeIfc.AUDIO_RECORDING.intValue()) {
		  return rb.getString("q_aud");
	  }
	  if (typeId == TypeIfc.FILL_IN_BLANK.intValue()) {
		  return rb.getString("q_fib");
	  }
	  if (typeId == TypeIfc.MATCHING.intValue()) {
		  return rb.getString("q_match");
	  }
	  if (typeId == TypeIfc.FILL_IN_NUMERIC.intValue()) {
		  return rb.getString("q_fin");
	  }
	  if (typeId == TypeIfc.EXTENDED_MATCHING_ITEMS.intValue()) {
		  return rb.getString("q_emi");
	  }
	  if (typeId == TypeIfc.MATRIX_CHOICES_SURVEY.intValue()) {
		  return rb.getString("q_matrix_choices_surv");
	  }
	  if (typeId == TypeIfc.CALCULATED_QUESTION.intValue()) {
	      return rb.getString("q_cq");
	  }
	  
	  if (typeId == TypeIfc.IMAGEMAP_QUESTION.intValue()) {
	      return rb.getString("q_imq");
	  }
	  return "";
  }
  
  /**
   * Standard process action method.
   * @param publishedId published assessment id
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
    
    List spreadsheetRows = new ArrayList();
    List<HistogramQuestionScoresBean> detailedStatistics = bean.getDetailedStatistics();
    
    spreadsheetRows.add(bean.getShowPartAndTotalScoreSpreadsheetColumns());

      if (totalBean.getFirstItem().isEmpty()) {
        spreadsheetRows.add(false);
    	return spreadsheetRows;
    }
    else {
        spreadsheetRows.add(true);
    }
    
    if (detailedStatistics ==null || detailedStatistics.isEmpty()) {
    	return spreadsheetRows;
    }
    
    List<Object> headerList = new ArrayList<>();
    headerList.add(ExportResponsesBean.HEADER_MARKER);
    headerList.add(rb.getString("question"));
    if(bean.isRandomType()){
        headerList.add("N(" + bean.getNumResponses() + ")");
    }else{
        headerList.add("N");
    }
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

    // No Answer
    headerList.add(rb.getString("no_answer"));
    
    // Label the response options A, B, C, ...
      for (char colHeader=65; colHeader < 65+bean.getMaxNumberOfAnswers(); colHeader++) {
        headerList.add(String.valueOf(colHeader));
    }
    spreadsheetRows.add(headerList);
	//VULA-1948: sort the detailedStatistics list by Question Label
    sortQuestionScoresByLabel(detailedStatistics);
    List statsLine;
    for (HistogramQuestionScoresBean questionBean : detailedStatistics) {
    	statsLine = new ArrayList();
    	statsLine.add(questionBean.getQuestionLabel());
    	double dVal;
    	
    	statsLine.add(questionBean.getNumResponses());
    	
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
   			try {
   	   			if (questionBean.getQuestionType().equals(TypeIfc.EXTENDED_MATCHING_ITEMS.toString())
   	   					&& !questionBean.getQuestionLabel().contains("-")) {
   	   				statsLine.add(" ");
   	   			}
   				else {
   					if (questionBean.getHistogramBars()[i].getIsCorrect()) {
   						statsLine.add(ExportResponsesBean.FORMAT_BOLD);
   					}
   					dVal = Double.parseDouble("" + questionBean.getHistogramBars()[i].getNumStudents() );
   					statsLine.add(dVal);
   				}
   			}
   			catch (NullPointerException npe) {
   				log.warn("questionBean.getHistogramBars()[" + i + "] is null. " + npe);
   			}
   		}
    	
    	spreadsheetRows.add(statsLine);
    }
    
    return spreadsheetRows;
    
  }

  
  /**
   * This method sort the detailedStatistics List by Question Label value
   * 
   * @param detailedStatistics
   */
	private void sortQuestionScoresByLabel(List<HistogramQuestionScoresBean> detailedStatistics) {
		//VULA-1948: sort the detailedStatistics list by Question Label
		Collections.sort(detailedStatistics, new Comparator<HistogramQuestionScoresBean>() {

			@Override
			public int compare(HistogramQuestionScoresBean arg0, HistogramQuestionScoresBean arg1) {

				HistogramQuestionScoresBean bean1 = arg0;
				HistogramQuestionScoresBean bean2 = arg1;

                //first check the part number
				int compare = Integer.parseInt(bean1.getPartNumber()) - Integer.parseInt(bean2.getPartNumber());
				if (compare != 0) {
                    return compare;
				}

                //now check the question number
                int number1 = 0;
                int number2 = 0;
                //check if the question has a sub-question number, only test the question number now
                if(!bean1.getQuestionNumber().contains("-")){
                    number1 = Integer.parseInt(bean1.getQuestionNumber());
                }else{
                    number1 = Integer.parseInt(bean1.getQuestionNumber().substring(0, bean1.getQuestionNumber().indexOf("-")));
                }
                if(!bean2.getQuestionNumber().contains("-")){
                    number2 = Integer.parseInt(bean2.getQuestionNumber());
                }else{
                    number2 = Integer.parseInt(bean2.getQuestionNumber().substring(0, bean2.getQuestionNumber().indexOf("-")));
                }
                compare = number1 - number2;
                if(compare != 0){
                    return compare;
                }
                //Now check the sub-question number. At this stage it will be from the same question
                number1 = Integer.parseInt(bean1.getQuestionNumber().substring(bean1.getQuestionNumber().indexOf("-")+1));
                number2 = Integer.parseInt(bean2.getQuestionNumber().substring(bean2.getQuestionNumber().indexOf("-")+1));
                return number1 - number2;
			}
		});
	}

	private List<AssessmentGradingData> filterGradingData(List<AssessmentGradingData> submissionsSortedForDiscrim, Long itemId) {
        List<AssessmentGradingData> submissionsForItemSortedForDiscrim = new ArrayList<AssessmentGradingData>();
        for(AssessmentGradingData agd: submissionsSortedForDiscrim){
            Set<ItemGradingData> itemGradings = agd.getItemGradingSet();
            for(ItemGradingData igd: itemGradings){
                if(igd.getPublishedItemId().equals(itemId)){
                    submissionsForItemSortedForDiscrim.add(agd);
                    break;
                }
            }
        }
        return submissionsForItemSortedForDiscrim;
    }

}
