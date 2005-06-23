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

package org.sakaiproject.tool.assessment.devtools;
import java.util.StringTokenizer;

/**
 * <p>Title: ManagedBeanUtil</p>
 * <p>Description: Programmer's utility to make a whole bunch of managed bean
 * entries for faces-config.xml</p>
 * <p>Copyright:  Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,                   Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation   Licensed under the Educational Community License Version 1.0 (the "License");  By obtaining, using and/or copying this Original Work, you agree that you have read,  understand, and will comply with the terms and conditions of the Educational Community License.  You may obtain a copy of the License at:        http://cvs.sakaiproject.org/licenses/license_1_0.html   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE  AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.</p>
 * <p>Sakai Project:: A collaboration of The Regents of the University of Michigan, Trustees of Indiana University,  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation</p>
 * @author Ed Smiley
 * @version $Id$
 *
 * Example output:
 *
 * <managed-bean>
 *   <description>Template editor backing bean</description>
 *   <managed-bean-name>template</managed-bean-name>
 *   <managed-bean-class>org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean</managed-bean-class>
 *   <managed-bean-scope>session</managed-bean-scope>
 * </managed-bean>
 *
 */

public class ManagedBeanUtil
{
  //layout for managed bean entry
  private static final String MAN_TAG = "<managed-bean>";
  private static final String MAN_TAG_END = "</managed-bean>";
  private static final String DESC_TAG = "  <description>";
  private static final String DESC_TAG_END = "</description>";
  private static final String NAME_TAG = "  <managed-bean-name>";
  private static final String NAME_TAG_END = "</managed-bean-name>";
  private static final String CLASS_TAG = "  <managed-bean-class>";
  private static final String CLASS_TAG_END = "</managed-bean-class>";
  private static final String SCOPE =
    "  <managed-bean-scope>session</managed-bean-scope>";


  // quick & dirty, rewrite to use your bean classes & generate array to std out
  // hint: use shell/sed/script to get list from directory and paste in here
  private static String[] beanz = {
    "org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean",
    "org.sakaiproject.tool.assessment.ui.bean.author.DeleteConfirmBean",
    "org.sakaiproject.tool.assessment.ui.bean.author.FileUploadBean",
    "org.sakaiproject.tool.assessment.ui.bean.author.IndexBean",
    "org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean",
    "org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentBean",
    "org.sakaiproject.tool.assessment.ui.bean.author.SectionBean",
    "org.sakaiproject.tool.assessment.ui.bean.delivery.DisplayAssetsBean",
    "org.sakaiproject.tool.assessment.ui.bean.delivery.XmlDeliveryBean",
    "org.sakaiproject.tool.assessment.ui.bean.evaluation.EvaluationResultBean",
    "org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramQuestionScoresBean",
    "org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean",
    "org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean",
    "org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean",
    "org.sakaiproject.tool.assessment.ui.bean.misc.DefaultLoginBean",
    "org.sakaiproject.tool.assessment.ui.bean.misc.DisplayMessageBean",
    "org.sakaiproject.tool.assessment.ui.bean.misc.NavigationBean",
    "org.sakaiproject.tool.assessment.ui.bean.misc.SimpleFormLoginBean",
    "org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean",
    "org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolData",
    "org.sakaiproject.tool.assessment.ui.bean.select.SelectAssessmentBean"
};

  /**
   * main util method
   * @param args arguments are fully qualified bean class names
   */
  public static void main(String[] args)
  {
    if (args.length>0) beanz = args;
    for (int i = 0; i < beanz.length; i++) {
      //System.out.println(makeManagedBeanEntry(beanz[i]));
    }
  }

  /**
   * create managed bean entry
   * @param beanClass
   * @return
   */
public static String makeManagedBeanEntry(String beanClass){
  String manBean = "  " + MAN_TAG + "\n";
  manBean += "  " + DESC_TAG + makeDescription(beanClass) + DESC_TAG_END + "\n";
  manBean += "  " + NAME_TAG + makeName(beanClass) + NAME_TAG_END + "\n";
  manBean += "  " + CLASS_TAG + beanClass + CLASS_TAG_END + "\n";
  manBean += "  " + SCOPE + "\n";
  manBean += "  " + MAN_TAG_END + "\n";

  return manBean;
}

private static String makeName(String beanClass){
  StringTokenizer st = new StringTokenizer(beanClass, ".");
  String name = beanClass;

  while (st.hasMoreElements())
  {
    name = st.nextToken();
  }

  // don't name your bean "Bean"! :)
  return name.toLowerCase().replaceAll("bean","");
}

private static String makeDescription(String beanClass){
  StringTokenizer st = new StringTokenizer(beanClass, ".");
  String info = beanClass;
  String pack = beanClass;
  String desc = "";

  while (st.hasMoreElements())
  {
    pack = info;
    info = st.nextToken();
  }

  info = info.replaceAll("bean","");
  info = info.replaceAll("Bean","");

  if (pack.equalsIgnoreCase(info))
   {
     pack = "";
   }
   else
   {
     pack = "For " + pack + ":";
   }

  char ca[] = info.toCharArray();

  for (int i = 0; i < ca.length; i++) {
    String ch = "" + ca[i];
    if (i==0)
    {
      ch  = ch.toUpperCase();
    }
    if (Character.isUpperCase(ca[i]))
    {
      desc += " ";
    }

    desc += ch;
  }

  return pack + desc + " backing bean.";
}

}
