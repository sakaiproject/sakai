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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>This bean represents a Part in an assessment </p>
 */

@Slf4j
public class SectionContentsBean
  implements Serializable
{
	private static final long serialVersionUID = 5959692528847396966L;
	private String text;
	private String nonDefaultText;
  private List itemContents;
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
    Iterator i = itemContents.iterator();
    int num = 0;
    while (i.hasNext())
    {
      ItemContentsBean next = (ItemContentsBean) i.next();
      if (next.isUnanswered())
      {
        num++;
      }
    }
    return num;
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
  public List getItemContents()
  {
    /*
        if( (sectionAuthorType!= null) && (sectionAuthorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL) ))
          return getItemContentsForRandomDraw();
        else if( (sectionAuthorType!= null) && (questionOrdering!=null ) && (sectionAuthorType.equals(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE)) && (questionOrdering.equals(SectionDataIfc.RANDOM_WITHIN_PART)))
          return getItemContentsForRandomQuestionOrdering();
        else
          return itemContents;
     */
    return itemContents;
  }

  public List getItemContentsForRandomDraw()
  {
    // same ordering for each student
    List randomsample = new ArrayList();
    long seed = (long) AgentFacade.getAgentString().hashCode();
    Collections.shuffle(itemContents, new Random(seed));
    int samplesize = numberToBeDrawn.intValue();
    for (int i = 0; i < samplesize; i++)
    {
      randomsample.add(itemContents.get(i));
    }
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
  public void setItemContents(List itemContents)
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
      this.itemContents = new ArrayList();
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
      Set itemSet = section.getItemSet();
      if (itemSet != null)
      {
        setQuestions(itemSet.size());
        Iterator i = itemSet.iterator();
        while (i.hasNext())
        {
          ItemDataIfc item = (ItemDataIfc) i.next();
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

      if (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(
        SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))
      {
        if (section.getSectionMetaDataByLabel(SectionDataIfc.
                                              NUM_QUESTIONS_DRAWN) != null)
        {
          Integer numberdrawn = new Integer(section.getSectionMetaDataByLabel(
            SectionDataIfc.NUM_QUESTIONS_DRAWN));
          setNumberToBeDrawn(numberdrawn);
        }

        if (section.getSectionMetaDataByLabel(SectionDataIfc.
                                              POOLID_FOR_RANDOM_DRAW) != null)
        {
          Long poolid = new Long(section.getSectionMetaDataByLabel(
            SectionDataIfc.POOLID_FOR_RANDOM_DRAW));
          setPoolIdToBeDrawn(poolid);
        }
        if (section.getSectionMetaDataByLabel(SectionDataIfc.
                                              POOLNAME_FOR_RANDOM_DRAW) != null)
        {
          String poolname = section.getSectionMetaDataByLabel(
            SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW);
          setPoolNameToBeDrawn(poolname);
          
          String randomDrawDate = section.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_RANDOM_DRAW_DATE);
          if(randomDrawDate != null && !"".equals(randomDrawDate)){

              try{

                // bjones86 - SAM-1604
                DateTime drawDate;
                DateTimeFormatter fmt = ISODateTimeFormat.dateTime();	//The Date Time is in ISO format
                try {
                    drawDate = fmt.parseDateTime(randomDrawDate);
                } catch(Exception ex) {
                    Date date = null;
                    try
                    {
                        // Old code produced dates that appeard like java.util.Date.toString() in the database
                        // This means it's possible that the database contains dates in multiple formats
                        // We'll try parsing Date.toString()'s format first.
                        // Date.toString is locale independent. So this SimpleDateFormat using Locale.US should guarantee that this works on all machines:
                        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                        // parse can either throw an exception or return null
                        date = df.parse(randomDrawDate);
                    }
                    catch (Exception e)
                    {
                        // failed to parse. Not worth logging yet because we will try again with another format
                    }

                    if (date == null)
                    {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
                        // If this throws an exception, it's caught below. This is appropriate.
                        date = df.parse(randomDrawDate);
                    }

                    if(date == null) 
                    {
                        // Nothing has worked
                        throw new IllegalArgumentException("Unable to parse date " + randomDrawDate);
                    }
                    else
                    {
                        drawDate = new DateTime(date);
                    }
                }

                    //We need the locale to localize the output string
                    Locale loc = new ResourceLoader().getLocale();
                    String drawDateString = DateTimeFormat.fullDate().withLocale(loc).print(drawDate);
                    String drawTimeString = DateTimeFormat.fullTime().withLocale(loc).print(drawDate);
                    setRandomQuestionsDrawDate(drawDateString);
                    setRandomQuestionsDrawTime(drawTimeString);

                }catch(Exception e){
                    log.error("Unable to parse date text: " + randomDrawDate, e);
                }
          }
        }
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

  public String getNumberToBeDrawnString()
  {
    return numberToBeDrawn.toString();
  }

  public void setNumberToBeDrawn(Integer param)
  {
    numberToBeDrawn = param;
  }

  public Long getPoolIdToBeDrawn()
  {
    return poolIdToBeDrawn;
  }

  public String getPoolIdToBeDrawnString()
  {
    return poolIdToBeDrawn.toString();
  }

  public void setPoolIdToBeDrawn(Long param)
  {
    poolIdToBeDrawn = param;
  }

  public void setPoolNameToBeDrawn(String param)
  {
    poolNameToBeDrawn = param;
  }

  public String getPoolNameToBeDrawn()
  {
    if ( (sectionAuthorType != null) &&
        (sectionAuthorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL)))
    {


      if ("".equals(poolNameToBeDrawn)) {

// after 2.2. poolNameToBeDrawn should not be an empty string
// This is for backward compatibility, for versions prior 2.2,  
// we didn't store poolname in metadata so when a user deletes a pool used for random draw assessment, exception occurs when viewing the assessment. The items are still there, but it still needs to the original pool object to get the poolname for JSF display.  
// After 2.2 we save this information in section metadata.  If users delete the pools we can now still retrive the random draw pool names used in an assessment.             

        QuestionPoolService qpservice = new QuestionPoolService();
        QuestionPoolFacade poolfacade = qpservice.getPool(poolIdToBeDrawn,
          AgentFacade.getAgentString());
        if (poolfacade!=null) {
          return poolfacade.getTitle();
        }
      // else the pool is no longer there
        return "";
      }
      else {
// get poolname from section metadata
        return poolNameToBeDrawn;
      }
      
    }
    return "";

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
      pointsDisplayString = Precision.round(points, 2) + "/";
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
}

