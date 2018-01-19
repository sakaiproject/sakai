/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.qti.helper;

import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tags.api.*;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;

/**
 * Contract: use List of special "|" delimited "KEY|VALUE" Strings!
 * @author Ed Smiley esmiley@stanford.edu
 */
@Slf4j
 public class MetaDataList
{
  private static final TagService tagService= (TagService) ComponentManager.get( TagService.class );

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
    "lockedBrowser_isInstructorEditable",
    "timedAssessment_isInstructorEditable",
    "timedAssessmentAutoSubmit_isInstructorEditable",
    "itemAccessType_isInstructorEditable",
    "displayChunking_isInstructorEditable",
    "displayNumbering_isInstructorEditable",
    "displayScores_isInstructorEditable",
    "submissionModel_isInstructorEditable",
    "lateHandling_isInstructorEditable",
    "autoSave_isInstructorEditable",
    "submissionMessage_isInstructorEditable",
    "finalPageURL_isInstructorEditable",
    "feedbackType_isInstructorEditable",
    "feedbackAuthoring_isInstructorEditable",
    "feedbackComponents_isInstructorEditable",
    "testeeIdentity_isInstructorEditable",
    "toGradebook_isInstructorEditable",
    "recordedScore_isInstructorEditable",
    "bgColor_isInstructorEditable",
    "bgImage_isInstructorEditable",
    "metadataAssess_isInstructorEditable",
    "metadataParts_isInstructorEditable",
    "metadataQuestions_isInstructorEditable",
    "honorpledge_isInstructorEditable"
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
   * Example:<metadata type =" list " > TEXT_FORMAT| HTML </metadata > 
   * <metadata type =" list " > ITEM_OBJECTIVE| </ metadata > 
   * Becomes:
   * TEXT_FORMAT=>HTML 
   * @param metadataList extraction-created list of "|" key value pairs
   * @param item the item
   */
  public void addTo(ItemFacade item)
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
        if (key.equalsIgnoreCase("TIMEALLOWED")){
        	item.setDuration(new Integer(value));
        }
        else if (key.equalsIgnoreCase("NUM_OF_ATTEMPTS")){
        	item.setTriesAllowed(new Integer(value));
        	
        }
  /*
  // these metadata names are different in QTI and Authoring, 
  public static final String OBJECTIVE = "OBJECTIVE";
  public static final String KEYWORD= "KEYWORD";
  public static final String RUBRIC= "RUBRIC";
  */
        else if (key.equalsIgnoreCase("ITEM_KEYWORD")){
        	item.addItemMetaData("KEYWORD", value);
        }
        else if (key.equalsIgnoreCase("ITEM_OBJECTIVE")){
        	item.addItemMetaData("OBJECTIVE", value);
        }
        else if (key.equalsIgnoreCase("ITEM_RUBRIC")){
        	item.addItemMetaData("RUBRIC", value);
        }
        else if (key.equalsIgnoreCase("ATTACHMENT")) {
      	  value = meta.substring(meta.indexOf("|") + 1);
      	  item.addItemAttachmentMetaData(value);
        }else if (key.equalsIgnoreCase("ITEM_TAGS")) {

            String[] tagList = value.split("\\),");
            for (String tagString: tagList){
                 String tagLabel =tagString.trim(); //We add the last ) to the tag string
                 if (!(tagLabel.substring(tagLabel.length()-1).equals(")"))) {
                    tagLabel = tagLabel + ")";
                }
                String tagCollectionName;
                try {
                     tagCollectionName = tagLabel.substring(tagLabel.lastIndexOf("(")+1, tagLabel.lastIndexOf(")"));
                }catch (Exception e){
                    tagCollectionName = "Not assigned collection";
                    tagLabel = "No label needed";
                }
                try {
                    tagLabel = tagLabel.substring(0,tagLabel.lastIndexOf("(")-1).trim();
                }catch (Exception ex) {
                    //Nothing to do if this happens...
                }

                if (!(tagCollectionName.equals("Not assigned collection"))){ //check if the collection is in our system and add the tag
                    Optional collection = tagService.getTagCollections().getForExternalSourceName(tagCollectionName);
                    if (collection.isPresent()) {
                        TagCollection tagCollection = (TagCollection) collection.get();
                        List<Tag> potentialTags = tagService.getTags().getTagsByExactLabel(tagLabel.trim());
                        potentialTags.stream().filter(t -> t.getCollectionName().equals(tagCollection.getName())).forEach(t -> {
                                item.addItemTag(t.getTagId(), t.getTagLabel(), t.getTagCollectionId(), t.getCollectionName());
                        });

                    }
                }

            }

        }
        else {
        	log.debug("key now is " + key);
        item.addItemMetaData(key, value);
        }
      }
    }
  }

  public String getSubmissionMessage()
	{
		String submissionMsg = null;
		if (metadataList == null)
		{
			return null; // no metadata found
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

			// SAK-6831: if it's submissionMessage, do not store in sam_assessmentmetadata_t, because the value is 255 char.
			if ("SUBMISSION_MESSAGE".equalsIgnoreCase(key))
			{

				if (st.hasMoreTokens())
				{
					value = st.nextToken().trim();
					submissionMsg = value;

				}
			}
		}
		return submissionMsg;
	}

  /**
	 * Adds extraction-created list of "|" key value pairs to assessment metadata map, if there are any. Example:
	 * <metadata type =" list " > FEEDBACK_SHOW_CORRECT_RESPONSE|True </ metadata >
	 * <metadata type =" list " > FEEDBACK_SHOW_STUDENT_SCORE|True </ metadata > 
	 * Becomes:TEXT_FORMAT=>HTML etc.
	 * 
	 * @param metadataList
	 *        extraction-created list of "|" key value pairs
	 * @param assessment
	 *        the assessment
	 */
  public void addTo(AssessmentFacade assessment)
  {
    if (metadataList == null)
    {
      return; // no metadata found
    }

    for (int i = 0; i < metadataList.size(); i++)
    {
      String meta = (String) metadataList.get(i);
      StringTokenizer st = new StringTokenizer(meta, "|");
      String key = "";
      String value = "";
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
        value = meta.substring(meta.indexOf("|") + 1);
        assessment.addAssessmentMetaData(key, value); 
      }
      else if ("ASSESSMENT_KEYWORDS".equals(key))
      {
        key = AssessmentMetaDataIfc.KEYWORDS;
        value = meta.substring(meta.indexOf("|") + 1);
        assessment.addAssessmentMetaData(key, value); 
      }
      else if ("ASSESSMENT_OBJECTIVES".equals(key))
      {
        key = AssessmentMetaDataIfc.OBJECTIVES;
        value = meta.substring(meta.indexOf("|") + 1);
        assessment.addAssessmentMetaData(key, value); 
      }
      else if ("ASSESSMENT_RUBRICS".equals(key))
      {
        key = AssessmentMetaDataIfc.RUBRICS;
        value = meta.substring(meta.indexOf("|") + 1);
        assessment.addAssessmentMetaData(key, value); 
      }
      else if ("BGCOLOR".equals(key))
      {
        key = AssessmentMetaDataIfc.BGCOLOR;
        value = meta.substring(meta.indexOf("|") + 1);
        assessment.addAssessmentMetaData(key, value); 
      }
      else if ("BGIMG".equals(key))
      {
        key = AssessmentMetaDataIfc.BGIMAGE;
        value = meta.substring(meta.indexOf("|") + 1);
        assessment.addAssessmentMetaData(key, value); 
      }
      else if ("COLLECT_ITEM_METADATA".equals(key))
      {
        key = "hasMetaDataForQuestions";
        value = meta.substring(meta.indexOf("|") + 1);
        assessment.addAssessmentMetaData(key, value); 
      }

      // for backwards compatibility with version 1.5 exports.
      else if ("ASSESSMENT_RELEASED_TO".equals(key) &&
          value != null && value.indexOf("Authenticated Users") > -1)
      {
        log.debug(
          "Fixing obsolete reference to 'Authenticated Users', setting released to 'Anonymous Users'.");
        value = AuthoringConstantStrings.ANONYMOUS;
      }
       
      else if ("SUBMISSION_MESSAGE".equalsIgnoreCase(key)){
    	  // skip
      }

      else if ("ATTACHMENT".equalsIgnoreCase(key)) {
    	  value = meta.substring(meta.indexOf("|") + 1);
    	  assessment.addAssessmentAttachmentMetaData(value);
      }

      else if (st.hasMoreTokens())
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
  public void setDefaults(AssessmentFacade assessment)
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
