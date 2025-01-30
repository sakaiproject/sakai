/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.grading.SectionGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>This bean represents a Part in an assessment </p>
 */

@Slf4j
public class SectionContentsBean extends SpringBeanAutowiringSupport implements Serializable {
  private static final long serialVersionUID = 5959692528847396966L;
  
  private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");

  @Autowired
  @Qualifier("org.sakaiproject.time.api.UserTimeService")
  private UserTimeService userTimeService;
  private String text;
  private List<ItemContentsBean> itemContents;
  private String sectionId;
  private String number;
  private double maxPoints;
  private double points;
  private int questions;
  private int numbering;
  private String numParts;
  private String description;
  private int unansweredQuestions; // ItemContentsBeans
  private List questionNumbers = new ArrayList();

  // added section Type , question ordering
  private Integer sectionAuthorType;
  private Integer questionOrdering;
  private Integer numberToBeDrawn;
  private Long poolIdToBeDrawn;
  private String poolNameToBeDrawn;
  private String randomQuestionsDrawDate = "";
  private String randomQuestionsDrawTime = "";
  private List attachmentList;
  private boolean noQuestions;
  
  @Getter @Setter private SectionGradingData sectionGradingData;
  @Getter private String timeLimit;
  @Getter private boolean timedSection;

  @Setter private Integer numberToBeFixed;
  @Setter private Long poolIdToBeFixed;
  @Getter @Setter private String poolNameToBeFixed;
  @Getter @Setter private String fixedQuestionsDrawDate = "";
  @Getter @Setter private String fixedQuestionsDrawTime = "";
  @Setter private List<Long> poolIdsToBeDrawn;

  public SectionContentsBean()
  {
  }

  /**
   * Part description.
   * @return Part description.
   */

  public String getText()
  {
    return text;
  }

  public String getNonDefaultText()
  {

    if ("Default".equals(text) || "default".equals(text))
    {
      return "";
    }
    return text;
  }

  /**
   * Part description.
   * @param text Part description.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   *   Points earned thus far for part.
   *
   * @return the points
   */
  public double getPoints()
  {
    return points;
  }

  /**
   * Points earned thus far for part.
   * @param points
   */
  public void setPoints(double points)
  {
    this.points = points;
  }

  /**
   * Number unanswered.
   * @return total unanswered.
   */
  public int getUnansweredQuestions()
  {
    return (int) itemContents.stream().filter(ItemContentsBean::isUnanswered).count();
  }

  /**
   * Number unanswered.
   * @param unansweredQuestions
   */
  public void setUnansweredQuestions(int unansweredQuestions)
  {
    this.unansweredQuestions = unansweredQuestions;
  }

  /**
   * Total points the part is worth.
   * @return max total points for part
   */
  public double getMaxPoints()
  {
    return maxPoints;
  }

  public double getRoundedMaxPoints()
  {
    // only show 2 decimal places 
    
    return Precision.round(maxPoints, 2);
  }

  /**
   * Total points the part is worth.
   * @param maxPoints points the part is worth.
   */
  public void setMaxPoints(double maxPoints)
  {
    this.maxPoints = maxPoints;
  }

  /**
   * Total number of questions.
   * @return total number of questions
   */
  public int getQuestions()
  {
    return questions;
  }

  /**
   * Total number of questions.
   * @param questions number of questions
   */
  public void setQuestions(int questions)
  {
    this.questions = questions;
  }

  /**
   * Total number of questions to list, based on numbering scheme
   * @return total number of questions
   */
  public int getNumbering()
  {
    return numbering;
  }

  /**
   * Total number of questions to list, based on numbering scheme
   * @param questions number of questions
   */
  public void setNumbering(int newNumbering)
  {
    numbering = newNumbering;
  }

  /**
   * Contents of part.
   * @return item contents of part.
   */
  public List<ItemContentsBean> getItemContents()
  {
    return itemContents;
  }

  public List<ItemContentsBean> getItemContentsForRandomDraw()
  {
    // same ordering for each student
    List<ItemContentsBean> randomsample = new ArrayList<>();
    long seed = (long) AgentFacade.getAgentString().hashCode();
    Collections.shuffle(itemContents, new Random(seed));
    IntStream.range(0, numberToBeDrawn).forEach(n -> randomsample.add(itemContents.get(n)));
    return randomsample;
  }

  public List getItemContentsForRandomQuestionOrdering()
  {
    // same ordering for each student
    long seed = (long) AgentFacade.getAgentString().hashCode();
    Collections.shuffle(itemContents, new Random(seed));
    return itemContents;
  }

  /**
   * Contents of part.
   * @param itemContents item contents of part.
   */
  public void setItemContents(List<ItemContentsBean> itemContents)
  {
    this.itemContents = itemContents;
  }

  /**
   * Get the size of the contents
   */
  public String getItemContentsSize()
  {
    if (itemContents == null)
    {
      return "0";
    }
    return Integer.toString(itemContents.size());
  }

  /**
   * Set the size of the contents
   */
  public void setItemContentsSize(String dummy)
  {
    // noop
  }

  /**
   * Display part number.
   * @return display numbering
   */
  public String getNumber()
  {
    return number;
  }

  /**
   * Display part number.
   * @param number display numbering
   */
  public void setNumber(String number)
  {
    this.number = number;
  }

  // added by daisyf on 11/22/04
  private String title;
  private String sequence;

  // for display/hide score
  // private boolean showStudentScore;   // show student Assessment Score
  // Chage showStudentScore to showStudentQuestionScore for SAK-7290
  // We consider the display/hide of part(section) score same as question score 
  // We used to consider them as assessment score as you can see above line and 
  // comment (private boolean showStudentScore;   // show student Assessment Score)
  private boolean showStudentQuestionScore;   // show student Assessment Score
  private String pointsDisplayString;

  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public List getQuestionNumbers()
  {
    return questionNumbers;
  }

  public void setQuestionNumbers()
  {
    this.questionNumbers = new ArrayList();
    for (int i = 1; i <= this.itemContents.size(); i++)
    {
      this.questionNumbers.add(new SelectItem( Integer.valueOf(i)));
    }
  }

  public SectionContentsBean(SectionDataIfc section)
  {
    try
    {
      this.itemContents = new ArrayList<>();
      setSectionId(section.getSectionId().toString());
      setTitle(section.getTitle());
      setDescription(section.getDescription());
      Integer sequence = section.getSequence();
      if (sequence != null)
      {
        setNumber(sequence.toString());
      }
      else
      {
        setNumber("1");
      }
      setNumber(section.getSequence().toString());
      // do teh rest later
      Set<ItemDataIfc> itemSet = section.getItemSet();

      if (itemSet != null) {
        // adding fixed questions (could be empty if not fixed and draw part)
        Set<ItemDataIfc> sortedSet = itemSet.stream()
            .filter(item -> ((ItemDataIfc) item).getIsFixed())
            .collect(Collectors.toSet());

        if (!sortedSet.isEmpty()) {
             // getting all hashes from the sortedSet
             List<String> distinctHashValues = sortedSet.stream()
                 .map(item -> ((ItemDataIfc) item).getHash())
                 .distinct()
                 .collect(Collectors.toList());

              // removing from itemSet if there are hashes repeated and getFixed false -> itemSet with only fixed and not repeated fixed on the randow draw
              itemSet.removeIf(item -> !item.getIsFixed() &&
                                       distinctHashValues.stream().anyMatch(hash -> hash.equals(item.getHash())));

              section.setItemSet(itemSet);
        }

        setQuestions(itemSet.size());
        for (ItemDataIfc item : itemSet) {
          ItemContentsBean itemBean = new ItemContentsBean(item);
          this.itemContents.add(itemBean);
        }
      }
      // set questionNumbers now
      setQuestionNumbers();
      setMetaData(section);
      this.attachmentList = section.getSectionAttachmentList();
      if (this.attachmentList !=null && this.attachmentList.size() >0 )
        this.hasAttachment = true;
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public void setMetaData(SectionDataIfc section)
  {
    if (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE) != null)
    {
      Integer authortype = new Integer(section.getSectionMetaDataByLabel(
        SectionDataIfc.AUTHOR_TYPE));
      setSectionAuthorType(authortype);

      if (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()) || 
            section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString())) {
          setMetadataRandowDraw(section);
      } else if (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {
          setMetadataFixed(section);
          setMetadataRandowDraw(section);
        }
    }
    else
    {

      setSectionAuthorType(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE);
    }

    // SAM-2781 this was added in Sakai 11 so need to be sure this is a real numeric value
    String qorderString = section.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_ORDERING);
    if (StringUtils.isNotBlank(qorderString) && StringUtils.isNumeric(qorderString))
    {
      Integer questionorder = new Integer(section.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_ORDERING));
      setQuestionOrdering(questionorder);
    }
    else
    {
      setQuestionOrdering(SectionDataIfc.AS_LISTED_ON_ASSESSMENT_PAGE);
    }
    
    String value = section.getSectionMetaDataByLabel(SectionMetaDataIfc.TIMED);
    this.timedSection = (StringUtils.isNotBlank(value) && !StringUtils.equalsIgnoreCase(Boolean.FALSE.toString(), value));
    this.timeLimit = value;
  }

  public void setMetadataFixed(SectionDataIfc section) {

	if (section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_FIXED) != null){
		Integer numberfixed = new Integer(section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_FIXED));
		setNumberToBeFixed(numberfixed);
	}

	if (section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_FIXED_AND_RANDOM_DRAW) != null) {
		Long poolid = new Long(section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_FIXED_AND_RANDOM_DRAW));
		setPoolIdToBeFixed(poolid);
	}

	if (section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_FIXED_AND_RANDOM_DRAW) != null) {
		String poolname = section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_FIXED_AND_RANDOM_DRAW);
		setPoolNameToBeFixed(poolname);

		String randomFixedDate = section.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_FIXED_DRAW_DATE);
		if (StringUtils.isNotEmpty(randomFixedDate)) {
			try {
				Instant fixedDate = parseInstant(randomFixedDate);

				//We need the locale to localize the output string
				Locale loc = new ResourceLoader().getLocale();
				ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
				DateTimeFormatter dateF = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(loc);
				DateTimeFormatter timeF = DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).withLocale(loc).withZone(zone);
				String fixedDateString = LocalDateTime.ofInstant(fixedDate, zone).format(dateF);
				String fixedTimeString = LocalDateTime.ofInstant(fixedDate, zone).format(timeF);
				setFixedQuestionsDrawDate(fixedDateString);
				setFixedQuestionsDrawTime(fixedTimeString);

			} catch(Exception e){
				log.error("Unable to parse date text: " + randomFixedDate, e);
			}
		}
	}
  }

  public void setMetadataRandowDraw(SectionDataIfc section) {

	if (section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) != null){
		Integer numberdrawn = new Integer(section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
		setNumberToBeDrawn(numberdrawn);
	}

	if (section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW) != null) {
		Long poolid = new Long(section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW));
		setPoolIdToBeDrawn(poolid);
		if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString().equals(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE))) {
			Integer randomPools = Integer.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT));
			List<Long> poolIds = new ArrayList<>();
			poolIds.add(poolid);
			for (int i = 1; i < randomPools; i++) {
				poolIds.add(Long.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW + "_" + i)));
			}
			setPoolIdsToBeDrawn(poolIds);
		}
	}

	if (section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW) != null) {
		String poolname = section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW);
		if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.equals(Integer.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE))) && 
				section.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT) != null) {
			Integer count = Integer.valueOf(section.getSectionMetaDataByLabel(SectionDataIfc.RANDOM_POOL_COUNT));
			for (int i = 1; i < count; i++) {
				poolname += SectionDataIfc.SEPARATOR_COMMA + section.getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW + SectionDataIfc.SEPARATOR_MULTI + i);
			}
		}
		setPoolNameToBeDrawn(poolname);

		String randomDrawDate = section.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_RANDOM_DRAW_DATE);
		if (StringUtils.isNotEmpty(randomDrawDate)) {
			try {
				Instant drawDate = parseInstant(randomDrawDate);

				//We need the locale to localize the output string
				Locale loc = new ResourceLoader().getLocale();
				ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
				DateTimeFormatter dateF = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(loc);
				DateTimeFormatter timeF = DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).withLocale(loc).withZone(zone);
				String drawDateString = LocalDateTime.ofInstant(drawDate, zone).format(dateF);
				String drawTimeString = LocalDateTime.ofInstant(drawDate, zone).format(timeF);
				setRandomQuestionsDrawDate(drawDateString);
				setRandomQuestionsDrawTime(drawTimeString);

			} catch(Exception e){
				log.error("Unable to parse date text: " + randomDrawDate, e);
			}
		}
	}
  }

  private Instant parseInstant(String dateText) throws ParseException {
	try {
		return LocalDateTime.parse(dateText, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant(ZoneOffset.UTC);
	} catch (DateTimeParseException ex) {
		Date date = null;
		// Old code produced dates that appeard like java.util.Date.toString() in the database
		// This means it's possible that the database contains dates in multiple formats
		// We'll try parsing Date.toString()'s format first.
		// Date.toString is locale independent. So this SimpleDateFormat using Locale.US should guarantee that this works on all machines:
		try {
			DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			// parse can either throw an exception or return null
			date = df.parse(dateText);
		} catch (Exception e) {
			// failed to parse. Not worth logging yet because we will try again with another format
		}
		if (date == null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
			// If this throws an exception, it's caught. This is appropriate.
			date = df.parse(dateText);

			if (date == null) {
				// Nothing has worked
				throw new IllegalArgumentException("Unable to parse date " + dateText);
			}
		}
		return date.toInstant();
	}
  }

  public String getSectionId()
  {
    return sectionId;
  }

  /**
   * Part description.
   * @param text Part description.
   */
  public void setSectionId(String sectionId)
  {
    this.sectionId = sectionId;
  }

  public String getNumParts()
  {
    return numParts;
  }

  public void setNumParts(String newNum)
  {
    numParts = newNum;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String newDesc)
  {
    description = newDesc;
  }

  public Integer getSectionAuthorType()
  {
    return sectionAuthorType;
  }

  public String getSectionAuthorTypeString()
  {
    return sectionAuthorType.toString();
  }

  public void setSectionAuthorType(Integer param)
  {
    sectionAuthorType = param;
  }

  public Integer getQuestionOrdering()
  {
    return questionOrdering;
  }

  public String getQuestionOrderingString()
  {
    return questionOrdering.toString();
  }

  public void setQuestionOrdering(Integer param)
  {
    questionOrdering = param;
  }

  public Integer getNumberToBeDrawn()
  {
    return numberToBeDrawn;
  }

  public Integer getNumberToBeFixed()
  {
    return numberToBeFixed;
  }

  public String getNumberToBeDrawnString()
  {
    return numberToBeDrawn.toString();
  }

  public void setNumberToBeDrawn(Integer param)
  {
    numberToBeDrawn = param;
  }

  public String getNumberToBeFixedString()
  {
    return numberToBeFixed.toString();
  }

  public void setNumberToBeFixed(Integer param)
  {
    numberToBeFixed = param;
  }

  public Long getPoolIdToBeDrawn()
  {
    return poolIdToBeDrawn;
  }

  public Long getPoolIdToBeFixed()
  {
    return poolIdToBeFixed;
  }

  public void setPoolIdToBeDrawn(Long param)
  {
    poolIdToBeDrawn = param;
  }

  public List<Long> getPoolIdsToBeDrawn()
  {
    return poolIdsToBeDrawn;
  }

  public void setPoolIdsToBeDrawn(List<Long> param)
  {
    poolIdsToBeDrawn = param;
  }

  public void setPoolNameToBeDrawn(String param)
  {
    poolNameToBeDrawn = param;
  }

  public String getPoolNameToBeDrawn()
  {
    return poolNameToBeDrawn;
  }

  /**
   * Show the student score currently earned?
   * @return the score
   */
  public boolean isShowStudentQuestionScore()
  {
    return showStudentQuestionScore;
  }

  /**
   * Set the student score currently earned.
   * @param setShowStudentQuestionScore true/false Show the student score currently earned?
   */
  public void setShowStudentQuestionScore(boolean showStudentQuestionScore)
  {
    this.showStudentQuestionScore = showStudentQuestionScore;
  }

  /**
   * If we display the score, return it, followed by a slash.
   * @return either, a) the score followed by a slash, or, b) "" (empty string)
   */
  public String getPointsDisplayString()
  {
    String pointsDisplayString = "";
    if (showStudentQuestionScore)
    {
      pointsDisplayString = String.valueOf(Precision.round(points, 2));
    }
    return pointsDisplayString;
  }

  public List getAttachmentList() {
    return attachmentList;
  }

  public void setAttachmentList(List attachmentList)
  {
    this.attachmentList = attachmentList;
  }

  private boolean hasAttachment = false;
  public boolean getHasAttachment(){
    boolean hasAttachment = false;
    if (attachmentList!=null && attachmentList.size() >0){
        hasAttachment = true;
    }
    return hasAttachment;
  }

  public boolean getNoQuestions() {
	  return noQuestions;
  }

  public void setNoQuestions(boolean noQuestions)
  {
	  this.noQuestions = noQuestions;
  }
  public String getRandomQuestionsDrawDate() {
	  return randomQuestionsDrawDate;
  }

  public void setRandomQuestionsDrawDate(String randomQuestionsDrawDate) {
	  this.randomQuestionsDrawDate = randomQuestionsDrawDate;
  }

  public String getRandomQuestionsDrawTime() {
	  return randomQuestionsDrawTime;
  }

  public void setRandomQuestionsDrawTime(String randomQuestionsDrawTime) {
	  this.randomQuestionsDrawTime = randomQuestionsDrawTime;
  }

  public boolean isEmiItemPresent() {
    return this.itemContents != null
        ? this.itemContents.stream()
            .filter(item -> TypeIfc.EXTENDED_MATCHING_ITEMS.equals(item.getItemData().getTypeId()))
            .collect(Collectors.counting())
            .intValue() > 0
        : false;
  }

  public int getCancelledItemsCount() {
    return this.itemContents != null
        ? this.itemContents.stream()
            .filter(item -> !TypeIfc.EXTENDED_MATCHING_ITEMS.equals(item.getItemData().getTypeId()))
            .filter(item -> !item.isCancelled())
            .collect(Collectors.counting())
            .intValue()
        : 0;
  }

  public boolean isCancellationAllowed() {
    return getCancelledItemsCount() > 1;
  }

  public String getTimeLimitString() {
    int seconds = Integer.parseInt(getTimeLimit());
    int hour = 0;
    int minute = 0;
    if (seconds >= 3600) {
        hour = Math.abs(seconds/3600);
        minute = Math.abs((seconds-hour*3600)/60);
    }
    else {
        minute = Math.abs(seconds/60);
    }
    StringBuilder sb = new StringBuilder();
    if (hour > 1) {
        sb.append(hour).append(" ").append(rb.getString("time_limit_hours"));
    } else if (hour == 1) {
        sb.append(hour).append(" ").append(rb.getString("time_limit_hour"));
    }
    if(sb.length() > 0) {
        sb.append(" ");
    }
    if (minute > 1) {
        sb.append(minute).append(" ").append(rb.getString("time_limit_minutes"));
    } else if (minute == 1) {
        sb.append(minute).append(" ").append(rb.getString("time_limit_minute"));
    }
    return sb.toString();
  }

  public String getRealTimeLimit() {
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    return delivery.getTimeBeforeDueRetract(this.timeLimit, getAttemptDate());
  }

  public Date getAttemptDate() {
    return (this.sectionGradingData != null) ? this.sectionGradingData.getAttemptDate() : null;
  }

  /**
   * Check if current item is Enabled
   * -1: TimedSection, NOT started
   * 0: TimedSection, Time expired
   * 1: OK -> TimedSection, started and NOT expired, or NOT TimedQuestion
   * @return 
   */
  public int getEnabled() {
    if(!isTimedSection()) {
        return 1;
    }
    
    if(getAttemptDate() == null) {
        return -1;
    }
    
    String timeBeforeDueRetract = getRealTimeLimit();
    long adjustedTimedAssesmentDueDateLong  = getAttemptDate().getTime() + (Long.parseLong(timeBeforeDueRetract) * 1000);
    Date endDate = new Date(adjustedTimedAssesmentDueDateLong);
    
    Date now = new Date();
    return now.before(endDate) ? 1 : 0;
  }

  public String getTimeElapsed() {
    try {
        Date start = getAttemptDate();
        Date now = new Date();
        long ret = now.getTime() - start.getTime();
        return String.valueOf(ret/1000);
    }catch(Exception e) {
        return "0";
    }
  }

  public String startTimedSection() {
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    //we can start only if no attempt date is set yet
    if(getAttemptDate() == null) {
        SectionGradingData sectionGradingData = new SectionGradingData();
        sectionGradingData.setAssessmentGradingId(delivery.getAssessmentGradingId());
        sectionGradingData.setPublishedSectionId(Long.parseLong(sectionId));
        sectionGradingData.setAgentId(AgentFacade.getAgentString());
        sectionGradingData.setAttemptDate(new Date());
    
        GradingService gs = new GradingService();
        gs.saveSectionGrading(sectionGradingData);
    }
    
    return delivery.samePage();
  }
}

