/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import javax.faces.model.SelectItem;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;

/**
 * <p> </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SectionContentsBean
  implements Serializable
{
  private String text;
  private String nonDefaultText;
  private java.util.ArrayList itemContents;
  private String sectionId;
  private String number;
  private float maxPoints;
  private float points;
  private int questions;
  private int numbering;
  private String numParts;
  private String description;
  private int unansweredQuestions; // ItemContentsBeans
  private ArrayList questionNumbers = new ArrayList();

  // added section Type , question ordering
  private Integer sectionAuthorType;
  private Integer questionOrdering;
  private Integer numberToBeDrawn;
  private Long poolIdToBeDrawn;

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
  public float getPoints()
  {
    return points;
  }

  /**
   * Points earned thus far for part.
   * @param points
   */
  public void setPoints(float points)
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
  public float getMaxPoints()
  {
    return maxPoints;
  }

  public float getRoundedMaxPoints()
  {
    // only show 2 decimal places 
    
    return roundTo2Decimals(maxPoints);
  }

  /**
   * Total points the part is worth.
   * @param maxPoints points the part is worth.
   */
  public void setMaxPoints(float maxPoints)
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
  public java.util.ArrayList getItemContents()
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

  public java.util.ArrayList getItemContentsForRandomDraw()
  {
    // same ordering for each student
    ArrayList randomsample = new ArrayList();
    long seed = (long) AgentFacade.getAgentString().hashCode();
    Collections.shuffle(itemContents, new Random(seed));
    int samplesize = numberToBeDrawn.intValue();
    for (int i = 0; i < samplesize; i++)
    {
      randomsample.add(itemContents.get(i));
    }
    return randomsample;
  }

  public java.util.ArrayList getItemContentsForRandomQuestionOrdering()
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
  public void setItemContents(java.util.ArrayList itemContents)
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
    return new Integer(itemContents.size()).toString();
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
  private boolean showStudentScore;   // show student Assessment Score
  private String pointsDisplayString;

  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public ArrayList getQuestionNumbers()
  {
    return questionNumbers;
  }

  public void setQuestionNumbers()
  {
    this.questionNumbers = new ArrayList();
    for (int i = 1; i <= this.itemContents.size(); i++)
    {
      this.questionNumbers.add(new SelectItem(new Integer(i)));
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
    }
    catch (Exception e)
    {
      e.printStackTrace();
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
      }

    }
    else
    {

      setSectionAuthorType(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE);
    }
    if (section.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_ORDERING) != null)
    {
      Integer questionorder = new Integer(section.getSectionMetaDataByLabel(
        SectionDataIfc.QUESTIONS_ORDERING));
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

  public String getPoolNameToBeDrawn()
  {
    if ( (sectionAuthorType != null) &&
        (sectionAuthorType.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL)))
    {
      QuestionPoolService qpservice = new QuestionPoolService();
      QuestionPoolFacade poolfacade = qpservice.getPool(poolIdToBeDrawn,
        AgentFacade.getAgentString());
      return poolfacade.getTitle();
    }
    return "";

  }

  /**
   * Show the student score currently earned?
   * @return the score
   */
  public boolean isShowStudentScore()
  {
    return showStudentScore;
  }

  /**
   * Set the student score currently earned.
   * @param showStudentScore true/false Show the student score currently earned?
   */
  public void setShowStudentScore(boolean showStudentScore)
  {
    this.showStudentScore = showStudentScore;
  }

  /**
   * If we display the score, return it, followed by a slash.
   * @return either, a) the score followed by a slash, or, b) "" (empty string)
   */
  public String getPointsDisplayString()
  {
    String pointsDisplayString = "";
    if (showStudentScore)
    {
      pointsDisplayString = roundTo2Decimals(points) + "/";
    }
    return pointsDisplayString;
  }

  public static float roundTo2Decimals(float points)
  {
    int tmp = Math.round(points * 100.0f);
    points = (float) tmp / 100.0f;
    return points;
  }

}
