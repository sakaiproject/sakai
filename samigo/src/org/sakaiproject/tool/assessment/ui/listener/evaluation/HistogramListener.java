/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/


package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramBarBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramQuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * <p>
 * This handles the selection of the Histogram Aggregate Statistics.
 *  </p>
 * <p>Description: Action Listener for Evaluation Histogram Aggregate Statistics.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class HistogramListener
  implements ActionListener, ValueChangeListener
{
  private static Log log = LogFactory.getLog(HistogramListener.class);
  private static BeanSort bs;
  private static ContextUtil cu;
  private static EvaluationListenerUtil util;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.info("HistogramAggregate Statistics LISTENER.");

    TotalScoresBean totalBean = (TotalScoresBean) cu.lookupBean(
                                "totalScores");
    HistogramScoresBean bean = (HistogramScoresBean) cu.lookupBean(
                               "histogramScores");
    String publishedId = totalBean.getPublishedId();

    if (publishedId == "0")
    {
	publishedId = (String) cu.lookupParam("publishedId");
    }

    log.info("Calling histogramScores.");
    if (!histogramScores(publishedId, bean, totalBean))
    {
      throw new RuntimeException("failed to call questionScores.");
    }
  }

  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {
    log.info("HistogramAggregate Statistics Value Change LISTENER.");

    TotalScoresBean totalBean = (TotalScoresBean) cu.lookupBean(
                                "totalScores");
    HistogramScoresBean bean = (HistogramScoresBean) cu.lookupBean(
                               "histogramScores");
    String publishedId = totalBean.getPublishedId();

    if (publishedId == "0")
    {
	publishedId = (String) cu.lookupParam("publishedId");
    }

    log.info("Calling histogramScores.");
    if (!histogramScores(publishedId, bean, totalBean))
    {
      throw new RuntimeException("failed to call questionScores.");
    }
  }

  /**
   * This will populate the HistogramScoresBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * Some of this code will change when we move this to Hibernate persistence.
   * @param publishedId String
   * @param bean TotalScoresBean
   * @return boolean true if successful
   */
  public boolean histogramScores(
    String publishedId, HistogramScoresBean bean, TotalScoresBean totalBean)
  {
    try {
      String assessmentName = "";

      // Get all submissions, or just the last?
      String which = cu.lookupParam("allSubmissions");
      //System.out.println("Rachel: allSubmissions = " + which);
      if (which == null)
        which = "false";
      bean.setAllSubmissions(which.equals("true")?true:false);
      bean.setItemId(cu.lookupParam("itemId"));
      bean.setHasNav(cu.lookupParam("hasNav"));

      GradingService delegate = new GradingService();
      ArrayList scores = delegate.getTotalScores(publishedId, which);
      HashMap itemscores = delegate.getItemScores(new Long(publishedId),
        new Long(0), which);
      //System.out.println("ItemScores size = " + itemscores.keySet().size());
      bean.setPublishedId(publishedId);

      Iterator iter = scores.iterator();
      //System.out.println("Has this many agents: " + scores.size());
      if (!iter.hasNext())
        return false;
      Object next = iter.next();
      AssessmentGradingData data = (AssessmentGradingData) next;

      if (data.getPublishedAssessment() != null)
      {
        assessmentName = data.getPublishedAssessment().getTitle();
        log.info("ASSESSMENT NAME= " + assessmentName);
        // if section set is null, initialize it - daisyf , 01/31/05
        PublishedAssessmentData pub = (PublishedAssessmentData)data.getPublishedAssessment();
        HashSet sectionSet = PersistenceService.getInstance().
            getPublishedAssessmentFacadeQueries().getSectionSetForAssessment(pub);
        data.getPublishedAssessment().setSectionSet(sectionSet);

        ArrayList parts =
          data.getPublishedAssessment().getSectionArraySorted();
        ArrayList info = new ArrayList();
        Iterator piter = parts.iterator();
        int secseq = 1;
        double totalpossible = 0;
        boolean hasRandompart = false;
        boolean isRandompart = false;
        while (piter.hasNext()) {


// todo: i can iterate through the parts and see if any is random drawn type, and set historgramScore bean's property randomtype = true,  and in jsf, check this property, if it's true, hide histogram

          SectionDataIfc section = (SectionDataIfc) piter.next();


 	  String authortype = section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
          if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.equals(authortype)){
            hasRandompart = true;
            isRandompart = true;
          }
          else {
            isRandompart = false;
  	  }


          if (section.getSequence() == null)
            section.setSequence(new Integer(secseq++));
          String title = "Part " + section.getSequence().toString();
          title += ", Question ";
          ArrayList itemset = section.getItemArraySortedForGrading();
          int seq = 1;
          Iterator iter2 = itemset.iterator();

          while (iter2.hasNext())
          {
            HistogramQuestionScoresBean qbean =
              new HistogramQuestionScoresBean();

            // if this part is a randompart , then set randompart = true
	    qbean.setRandomType(isRandompart);
	    ItemDataIfc item = (ItemDataIfc) iter2.next();
            String type = delegate.getTextForId(item.getTypeId());
            if (item.getSequence() == null)
              item.setSequence(new Integer(seq++));
            qbean.setTitle(title + item.getSequence().toString() +
              " (" + type + ")");
            qbean.setQuestionText(item.getText());
            qbean.setQuestionType(item.getTypeId().toString());
            totalpossible = totalpossible + item.getScore().doubleValue();
            ArrayList responses = null;
            determineResults(qbean, (ArrayList) itemscores.get
              (item.getItemId()));
            qbean.setTotalScore(item.getScore().toString());
            info.add(qbean);
          }
        }
        bean.setInfo(info);
        bean.setRandomType(hasRandompart);

        Map assessmentMap = getAssessmentStatisticsMap(scores);

        // test to see if it gets back empty map
        if (assessmentMap.isEmpty())
        {
          bean.setNumResponses(0);
        }

        try
        {
          BeanUtils.populate(bean, assessmentMap);

          // quartiles don't seem to be working, workaround
          bean.setQ1( (String) assessmentMap.get("q1"));
          bean.setQ2( (String) assessmentMap.get("q2"));
          bean.setQ3( (String) assessmentMap.get("q3"));
          bean.setQ4( (String) assessmentMap.get("q4"));
          bean.setTotalScore( (String) assessmentMap.get("totalScore"));
          bean.setTotalPossibleScore(new Double(totalpossible).toString());
          HistogramBarBean[] bars =
            new HistogramBarBean[bean.getColumnHeight().length];
          for (int i=0; i<bean.getColumnHeight().length; i++)
          {
            bars[i] = new HistogramBarBean();
            bars[i].setColumnHeight
              (new Integer(bean.getColumnHeight()[i]).toString());
            bars[i].setNumStudents(bean.getNumStudentCollection()[i]);
            bars[i].setRangeInfo(bean.getRangeCollection()[i]);
            //System.out.println("Set bar " + i + ": " + bean.getColumnHeight()[i] + ", " + bean.getNumStudentCollection()[i] + ", " + bean.getRangeCollection()[i]);
          }
          bean.setHistogramBars(bars);

          ///////////////////////////////////////////////////////////
          // START DEBUGGING
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

          // END DEBUGGING CODE
          ///////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
          log.info("unable to populate bean" + e);
        }

        bean.setAssessmentName(assessmentName);
      }
      else
      {
        return false;
      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    return true;

  }

  public void determineResults(HistogramQuestionScoresBean qbean,
    ArrayList itemScores)
  {
    if (itemScores == null)
      itemScores = new ArrayList();
    if (qbean.getQuestionType().equals("1") ||
        qbean.getQuestionType().equals("2") ||  // mcmc
        qbean.getQuestionType().equals("3") ||  // mcsc
        qbean.getQuestionType().equals("4") || // tf
        qbean.getQuestionType().equals("9") || // matching
        qbean.getQuestionType().equals("8")) // Fill in the blank
      doAnswerStatistics(qbean, itemScores);
    if (qbean.getQuestionType().equals("5") || // essay
        qbean.getQuestionType().equals("6") || // file upload
        qbean.getQuestionType().equals("7")) // audio recording
        // qbean.getQuestionType().equals("8")) // Fill in the blank
      doScoreStatistics(qbean, itemScores);

  }

  public void doAnswerStatistics(HistogramQuestionScoresBean qbean,
    ArrayList scores)
  {
    if (scores.isEmpty())
    {
      qbean.setHistogramBars(new HistogramBarBean[0]);
      qbean.setNumResponses(0);
      qbean.setPercentCorrect("No responses");
      return;
    }

    int numAnswers = 0;
    ItemDataIfc item = ((ItemGradingData) scores.toArray()[0])
      .getPublishedItem();
    ArrayList text = item.getItemTextArraySorted();
    ArrayList answers = null;
    if (!qbean.getQuestionType().equals("9"))
    {
      ItemTextIfc firstText = (ItemTextIfc) text.toArray()[0];
      answers = firstText.getAnswerArraySorted();
    }
    if (qbean.getQuestionType().equals("1") ||
        qbean.getQuestionType().equals("2"))
      getTFMCScores(scores, qbean, answers);
    else if (qbean.getQuestionType().equals("3"))
      getTFMCScores(scores, qbean, answers);
    else if (qbean.getQuestionType().equals("4"))
      getTFMCScores(scores, qbean, answers);
    else if (qbean.getQuestionType().equals("8"))
      getTFMCScores(scores, qbean, answers);
    else if (qbean.getQuestionType().equals("9"))
      getMatchingScores(scores, qbean, text);
  }

  public void getTFMCScores(ArrayList scores,
    HistogramQuestionScoresBean qbean, ArrayList answers)
  {
    HashMap texts = new HashMap();
    Iterator iter = answers.iterator();
    HashMap results = new HashMap();
    while (iter.hasNext())
    {
      AnswerIfc answer = (AnswerIfc) iter.next();
      texts.put(answer.getId(), answer);
      results.put(answer.getId(), new Integer(0));
    }
    iter = scores.iterator();
    while (iter.hasNext())
    {
      ItemGradingData data = (ItemGradingData) iter.next();
      AnswerIfc answer = data.getPublishedAnswer();
      if (answer != null)
      {
        //System.out.println("Rachel: looking for " + answer.getId());
        Integer num = null;
        try {
          num = (Integer) results.get(answer.getId());
        } catch (Exception e) {
          System.out.println("No results for " + answer.getId());
        }
        if (num == null)
          num = new Integer(0);
        results.put(answer.getId(), new Integer(num.intValue() + 1));
      }
    }
    HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
    int[] numarray = new int[results.keySet().size()];
    iter = results.keySet().iterator();
    int i = 0;
    int responses = 0;
    int correctresponses = 0;
    while (iter.hasNext())
    {
      Long answerId = (Long) iter.next();
      AnswerIfc answer = (AnswerIfc) texts.get(answerId);
      int num =	((Integer) results.get(answerId)).intValue();
      numarray[i] = num;
      bars[i] = new HistogramBarBean();
      if (!qbean.getQuestionType().equals("8"))
      {
          bars[i].setLabel(answer.getText());
      }
      else
      {
          ItemDataIfc item = ((ItemGradingData) scores.toArray()[0])
          .getPublishedItem();
          bars[i].setLabel(item.getCorrectItemFeedback());
      }

      bars[i].setIsCorrect(answer.getIsCorrect());
      if (num>1)
	  {
	      bars[i].setNumStudentsText(num + " Responses");
	  }
      else
	  {
	      bars[i].setNumStudentsText(num + " Response");

      }
      bars[i].setNumStudents(num);
      responses += num;
      if (answer.getIsCorrect() != null && answer.getIsCorrect().booleanValue())
        correctresponses += num;
      i++;
    }
    int[] heights = calColumnHeight(numarray);
    for (i=0; i<bars.length; i++)
      bars[i].setColumnHeight(new Integer(heights[i]).toString());
    qbean.setHistogramBars(bars);
    qbean.setNumResponses(responses);
    if (responses > 0)
      qbean.setPercentCorrect(new Integer((int)(((float) correctresponses/(float) responses) * 100)).toString());
  }

  public void getMatchingScores(ArrayList scores,
    HistogramQuestionScoresBean qbean, ArrayList labels)
  {
    HashMap texts = new HashMap();
    Iterator iter = labels.iterator();
    HashMap results = new HashMap();
    while (iter.hasNext())
    {
      ItemTextIfc label = (ItemTextIfc) iter.next();
      texts.put(label.getId(), label);
      results.put(label.getId(), new Integer(0));
    }
    iter = scores.iterator();
    int responses = 0;
    while (iter.hasNext())
    {
      ItemGradingData data = (ItemGradingData) iter.next();
      ItemTextIfc text = data.getPublishedItemText();
      AnswerIfc answer = data.getPublishedAnswer();
      if (answer.getIsCorrect() != null && answer.getIsCorrect().booleanValue())
      {
        Integer num = (Integer) results.get(text.getId());
        if (num == null)
          num = new Integer(0);
        results.put(text.getId(), new Integer(num.intValue() + 1));
      }
      else
        responses += 1;
    }
    HistogramBarBean[] bars = new HistogramBarBean[results.keySet().size()];
    int[] numarray = new int[results.keySet().size()];
    iter = results.keySet().iterator();
    int i = 0;
    int correctresponses = 0;
    while (iter.hasNext())
    {
      Long textId = (Long) iter.next();
      ItemTextIfc text = (ItemTextIfc) texts.get(textId);
      int num = ((Integer) results.get(textId)).intValue();
      numarray[i] = num;
      bars[i] = new HistogramBarBean();
      bars[i].setLabel(text.getText());
      bars[i].setNumStudents(num);
      if (num>1)
	  {
	      bars[i].setNumStudentsText(num + " Correct Responses");
	  }
      else
	  {
	      bars[i].setNumStudentsText(num + " Correct Response");

      }
      // bars[i].setNumStudentsText(num + " Correct Responses");
      correctresponses += num;
      responses += num;
      i++;
    }
    int[] heights = calColumnHeight(numarray);
    for (i=0; i<bars.length; i++)
      bars[i].setColumnHeight(new Integer(heights[i]).toString());
    qbean.setHistogramBars(bars);
    qbean.setNumResponses(responses);
    if (responses > 0)
      qbean.setPercentCorrect(new Integer((int)(((float) correctresponses/(float) responses) * 100)).toString());
  }

  public void doScoreStatistics(HistogramQuestionScoresBean qbean,
    ArrayList scores)
  {
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
      for (int i=0; i<qbean.getColumnHeight().length; i++)
      {
        bars[i] = new HistogramBarBean();
        bars[i].setColumnHeight
          (new Integer(qbean.getColumnHeight()[i]).toString());
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
      qbean.setHistogramBars(bars);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Map getAssessmentStatisticsMap(ArrayList scoreList)
  {
    Iterator iter = scoreList.iterator();
    ArrayList floats = new ArrayList();
    while (iter.hasNext())
    {
      Object data = iter.next();
      if (data instanceof AssessmentGradingData)
        floats.add((Float)
          ((AssessmentGradingData) data).getFinalScore());
      else
      {
        float autoScore = (float) 0.0;
        if (((ItemGradingData) data).getAutoScore() != null)
          autoScore = ((ItemGradingData) data).getAutoScore().floatValue();
        float overrideScore = (float) 0.0;
        if (((ItemGradingData) data).getOverrideScore() != null)
          overrideScore =
            ((ItemGradingData) data).getOverrideScore().floatValue();
        floats.add(new Float(autoScore + overrideScore));
      }
    }
    if (floats.isEmpty())
      floats.add(new Float(0.0));
    Object[] array = floats.toArray();
    Arrays.sort(array);

    double[] scores = new double[array.length];
    for (int i=0; i<array.length; i++)
      scores[i] = ((Float) array[i]).doubleValue();

    HashMap statMap = new HashMap();

    double min = scores[0];
    double max = scores[scores.length - 1];
    double total = calTotal(scores);
    double mean = calMean(scores, total);
    int interval = 0;
    interval = calInterval(scores, min, max);
    int[] numStudents = calNumStudents(scores, min, max, interval);

    statMap.put("maxScore", castingNum(max,2));
    statMap.put("interval", new Integer(interval));
    statMap.put("numResponses", new Integer(scores.length));
    statMap.put("totalScore",castingNum(total,2));
    statMap.put("mean", castingNum(mean,2));
    statMap.put("median", castingNum(calMedian(scores),2));
    statMap.put("mode", (new String(calMode(scores))));
    statMap.put("numStudentCollection", numStudents);
    statMap.put(
      "rangeCollection", calRange(scores, numStudents, min, max, interval));
    statMap.put("standDev", castingNum(calStandDev(scores, mean, total),2));
    statMap.put("columnHeight", calColumnHeight(numStudents));
    statMap.put("arrayLength", new Integer(numStudents.length));
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
  private static String calMode(double[] scores)
  {
    String max = ", " + scores[0];
    String temp = ", " + scores[0];
    int nmax = 1;
    int ntemp = 1;
    String scoreString;
    for(int i = 1; i < scores.length; i++)
    {
      if(Math.ceil(scores[i]) == Math.floor(scores[i]))
      {
        scoreString = "" + (int) scores[i];
      }
      else
      {
        scoreString = "" + scores[i];
      }

      if((", " + scoreString).equals(temp))
      {
        ntemp++;
        if(nmax < ntemp)
        {
          max = temp;
          nmax = ntemp;
        }
        else if(nmax == ntemp)
        {
          max = max + temp;
          nmax = ntemp;
        }
        else
        {
          // nmax>ntemp do nothing
        }
      }
      else
      {
        temp = ", " + scoreString;
        ntemp = 1;
      }
    }

    max = max.substring(2, max.length());

    return max;
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
  private static double calStandDev(double[] scores, double mean, double total)
  {
    for(int i = 0; i < scores.length; i++)
    {
      total = total + ((scores[i] - mean) * (scores[i] - mean));
    }

    return Math.sqrt(total / scores.length);
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

          /*
             String[] answerArray = answers[i].split("|");
             for(int k=0;i<answerArray.length ; k++)  {
               if ( (answerArray[k].trim()).equals(choices[j].trim())) {
                 num[j] = num[j] + 1;
               }
             }
           */
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

  /**
   * Calculate the number correct answer
   *
   * @param answers array of answers
   * @param correct the correct answer
   *
   * @return the number correct
   */
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
      log.info("max(" + max + ") <min(" + min + ")");
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
  private static int[] calColumnHeight(int[] numStudents)
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
      num = (int) (600 / temp[temp.length - 1]);
      int j = 0;
      while(j < length)
      {
        height[j] = num * numStudents[j];

        j++;
      }
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
      if(scores != null)
      {
        return scores[0];
      }
      else
      {
        return 0;
      }
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
        //System.out.println("NUMBER : "+n);
        //System.out.println("NUMBER LENGTH : "+n.length());
        if(n.length()>index)
            {
        return n.substring(0,index);
            }
        else{
            return ""+number;
        }

    }
  }
}
