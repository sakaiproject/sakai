/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/ExtractionHelper.java $
 * $Id: ExtractionHelper.java 747 2005-07-23 00:40:34Z esmiley@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.business.entity.helper;

import java.util.List;
import java.util.StringTokenizer;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;

/**
 * Contract: use List of special "|" delimited "KEY|VALUE" Strings!
 * @author Ed Smiley esmiley@stanford.edu
 */
public class MetaDataList
{
  /**
   * list of editable settings
   */
  private static final String[] editableKeys =
    {
    "assessmentAuthor_isInstructorEditable",
    "assessmentCreator_isInstructorEditable",
    "description_isInstructorEditable",
    "dueDate_isInstructorEditable",
    "retractDate_isInstructorEditable",
    "anonymousRelease_isInstructorEditable",
    "authenticatedRelease_isInstructorEditable",
    "ipAccessType_isInstructorEditable",
    "passwordRequired_isInstructorEditable",
    "timedAssessment_isInstructorEditable",
    "timedAssessmentAutoSubmit_isInstructorEditable",
    "itemAccessType_isInstructorEditable",
    "displayChunking_isInstructorEditable",
    "displayNumbering_isInstructorEditable",
    "submissionModel_isInstructorEditable",
    "lateHandling_isInstructorEditable",
    "autoSave_isInstructorEditable",
    "submissionMessage_isInstructorEditable",
    "finalPageURL_isInstructorEditable",
    "feedbackType_isInstructorEditable",
    "feedbackComponents_isInstructorEditable",
    "testeeIdentity_isInstructorEditable",
    "toGradebook_isInstructorEditable",
    "recordedScore_isInstructorEditable",
    "bgColor_isInstructorEditable",
    "bgImage_isInstructorEditable",
    "metadataAssess_isInstructorEditable",
    "metadataParts_isInstructorEditable",
    "metadataQuestions_isInstructorEditable",
  };

  private List metadataList;

  /**
   * Contract: use List of special "|" delimited "KEY|VALUE" Strings!
   * Uses special "|" delimited "KEY|VALUE" strings
   * @param metadataList
   */
  public MetaDataList(List metadataList)
  {
    this.setMetadataList(metadataList);
  }

  /**
   * Adds extraction-created list of "|" key value pairs
   * to item metadata map, if there are any.
   * Example:<br/>
   * <p>
     * &lt; metadata type =" list " &gt; TEXT_FORMAT| HTML &lt;/ metadata &gt;<br/> á
   * &lt; metadata type =" list " &gt; ITEM_OBJECTIVE| &lt/ metadata &gt;<br/>
   * Becomes:<br/>
   * TEXT_FORMAT=>HTML etc.
   * </p>
   * @param metadataList extraction-created list of "|" key value pairs
   * @param item the item
   */
  private void addTo(ItemDataIfc item)
  {
    if (metadataList == null)
    {
      return; // no metadata found
    }

    for (int i = 0; i < metadataList.size(); i++)
    {
      String meta = (String) metadataList.get(i);
      StringTokenizer st = new StringTokenizer(meta, "|");
      String key = null;
      String value = null;
      if (st.hasMoreTokens())
      {
        key = st.nextToken().trim();
      }
      if (st.hasMoreTokens())
      {
        value = st.nextToken().trim();
        item.addItemMetaData(key, value);
      }
    }
  }

  /**
   * Adds extraction-created list of "|" key value pairs
   * to assessment metadata map, if there are any.
   * Example:<br/>
   * <p>á
   *
   * &lt; metadata type =" list " &gt; FEEDBACK_SHOW_CORRECT_RESPONSE|True &lt;/ metadata &gt;<br/> á
   * &lt; metadata type =" list " &gt; FEEDBACK_SHOW_STUDENT_SCORE|True &lt/ metadata &gt;<br/>
   * Becomes:<br/>
   * TEXT_FORMAT=>HTML etc.
   * </p>
   * @param metadataList extraction-created list of "|" key value pairs
   * @param assessment the assessment
   */
  private void addTo(AssessmentFacade assessment)
  {
    if (metadataList == null)
    {
      return; // no metadata found
    }

    for (int i = 0; i < metadataList.size(); i++)
    {
      String meta = (String) metadataList.get(i);
      StringTokenizer st = new StringTokenizer(meta, "|");
      String key = null;
      String value = null;
      if (st.hasMoreTokens())
      {
        key = st.nextToken().trim();
      }

      // translate XML metadata strings to assessment metadata strings here
      // key to patch up the difference between Daisy's and earlier labels
      // that are compatible with the earlier beta version of Samigo
      if ("AUTHORS".equals(key))
      {
        key = AssessmentMetaDataIfc.AUTHORS;
      }
      if ("ASSESSMENT_KEYWORDS".equals(key))
      {
        key = AssessmentMetaDataIfc.KEYWORDS;
      }
      if ("ASSESSMENT_OBJECTIVES".equals(key))
      {
        key = AssessmentMetaDataIfc.OBJECTIVES;
      }
      if ("ASSESSMENT_RUBRICS".equals(key))
      {
        key = AssessmentMetaDataIfc.RUBRICS;
      }
      if ("BGCOLOR".equals(key))
      {
        key = AssessmentMetaDataIfc.BGCOLOR;
      }
      if ("BGIMG".equals(key))
      {
        key = AssessmentMetaDataIfc.BGIMAGE;
      }
      if ("COLLECT_ITEM_METADATA".equals(key))
      {
        key = "hasMetaDataForQuestions";

      }
      if (st.hasMoreTokens())
      {
        value = st.nextToken().trim();
        assessment.addAssessmentMetaData(key, value);
      }
    }
  }

  /**
   * Turns on editability for everything (ecept template info),
   * since we don't know if this  metadata is in the assessment or not,
   * or may not want to follow it, even if it is.
   *
   * The importer of the assesment may also be different than the
   * exporter, and may be on a different system or have different
   * templates, or policies, even if using this softwware.
   *
   * @param assessment
   */
  private void setDefaults(AssessmentFacade assessment)
  {
    // turn this off specially, as template settings are meaningless on import
    assessment.addAssessmentMetaData("templateInfo_isInstructorEditable",
                                     "false");

    for (int i = 0; i < editableKeys.length; i++)
    {
      assessment.addAssessmentMetaData(editableKeys[i], "true");
    }

  }

  public List getMetadataList()
  {
    return metadataList;
  }

  public void setMetadataList(List metadataList)
  {
    this.metadataList = metadataList;
  }

}