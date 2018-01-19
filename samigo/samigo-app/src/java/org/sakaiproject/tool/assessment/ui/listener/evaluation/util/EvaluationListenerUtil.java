/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.evaluation.util;

import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


/**
 * <p>
 * Utility methods for Action Listeners Evaluation </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * <p>Much of this code was originally in EvaluationAction</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class EvaluationListenerUtil
{
  private static ContextUtil cu;

  /**
   * Looks for a command JSF id that has "sortBy" (e.g. "sortByAssessmentResultId")
   * and computes the sort property (e.g., as above, "assessmentResultId").
   *
   * @return String sort field
   */
  public static String getSortOrder(){
    String sortCommandId = cu.paramLike("sortBy") + "        ";
    String capitalizedField = sortCommandId.substring(6);
    String field = (capitalizedField.substring(0,1).toLowerCase() +
                   capitalizedField.substring(1)).trim();
    return field;
  }


  /**
   * Utility
   * @param correctAnswerVar
   * @param answerVar
   * @return true if correct
   */
  public static boolean answerRight(String correctAnswerVar, String answerVar)
  {
    StringTokenizer st = new StringTokenizer(correctAnswerVar, "|");
    while (st.hasMoreElements())
    {
      String correct = st.nextToken();
      if (correct.equals(answerVar))
      {
        return true; // match
      }
    }

    return false;
  }

  /**
   * Get the total points for the assessment
   *
   * @param the assessment id
   *
   * @return the points
   */
    /** I don't think we are using this method, coment out as part of 2.0 clean up - daisyf
  public static double getTotalPoints(String assessmentId, Calendar cal)
  {
    double points = 0;
    ArrayList sectionList = new ArrayList();
    ArrayList itemList = new ArrayList();
    AssessmentHelper assessmentHelper = new AssessmentHelper();
    sectionList = assessmentHelper.getSectionRefsByDate(assessmentId, cal);

    // we loop through each part and each question within each part
    for (int p = 0; p < sectionList.size(); p++)
    {
      String sectionId = (String) sectionList.get(p);
      SectionHelper sectionHelper = new SectionHelper();
      log.info("getSectionItems(  String" + sectionId + ", boolean true )");

      ArrayList items = sectionHelper.getSectionItems(sectionId, true);
      int questionCount = items.size();
      log.info("questions for " + p + ": " + questionCount);
      log.info("item " + items.get(0));

      // look up item in part
      for (int q = 0; q < questionCount; q++)
      {
        String itemId = items.get(q).toString();
        ItemHelper itemHelper = new ItemHelper();
        org.navigoproject.business.entity.Item item =
          itemHelper.getItemXml(itemId);
        double max = getMaxPoints(item);
        double maxVal = 0;
        try
        {
          maxVal = max;
        }
        catch (Exception ex)
        {
          // don't throw exception, just skip scores that can't be doubles
          // we are not supporting non-numeric socres at this time
          // this is for forward compatibility
        }

        points += maxVal;
      }
    }

    return points;
  }
    */
  /**
   * Get the points text from the Item XML
   *
   * @param itemXml  the Item XML
   *
   * @return a String containing the points
   */
    /**
  public static double getMaxPoints(org.navigoproject.business.entity.Item
    itemXml)
  {
    String baseXPath = "item/resprocessing/outcomes/decvar";
    double answerPoints = 0;
    int respSize = 0;

    List resp = itemXml.selectNodes(baseXPath);
    if ( (resp != null) && (resp.size() > 0))
    {
      respSize = resp.size();
    }
    else
    {
      return 0;
    }

    for (int i = 1; i <= respSize; i++)
    {
      String index = ("[" + i) + "]";
      String max =
        itemXml.selectSingleValue(
        baseXPath + index + "/@maxvalue", "attribute");
      double dmax = 0;
      try
      {
        dmax = Double.parseDouble(max);
      }
      catch (Exception ex)
      {
        // don't throw exception, just skip scores that can't be doubles
      }

      answerPoints += dmax;
    }

    return answerPoints;
  }
    */

  /**
   * utility
   *
   * @param n a double value
   *
   * @return a string value
   */
  public static String castingNum(double n)
  {
    if (Math.ceil(n) == Math.floor(n))
    {
      return ("" + (int) n);
    }
    else
    {
      return "" + n;
    }
  }

}
