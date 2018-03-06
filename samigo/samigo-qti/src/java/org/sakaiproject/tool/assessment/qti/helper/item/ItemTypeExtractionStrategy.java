/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.qti.helper.item;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;

/**
 * Encapsulates the work of figuring out what type the item is.
 * Right now, uses static methods, later, we might want to change, add to factory.
 *
 * We use the QTI qmd_itemtype in itemmetadata as the preferred way to ascertain type.
 * We fall back on title and then item structure, also attempring keyword matching.
 * Note, this may be deprecated in favor of a vocabulary based approach.
 * Need to investigate.  Anyway, this is the form that is backwardly compatible.
 *
 * @author Ed Smiley
 */
@Slf4j
 public class ItemTypeExtractionStrategy
{
  private static final Long DEFAULT_TYPE =  Long.valueOf(2);


  /**
   * Obtain Long item type id from strings extracted from item.
   * @param title the item title
   * @param itemIntrospect hte structure based guess from XSL
   * @param qmdItemtype hte item type meta information
   * @return Long item type id
   */  
  public static Long calculate(String title, String itemIntrospect, String qmdItemtype)
  {
	  String itemType = obtainTypeString(title, itemIntrospect, qmdItemtype);
	  Long typeId = getType(itemType);
	  return typeId;
  }

  public static Long calculate(Map itemMap)
  {
	  String itemType = null;
	  if (itemMap.get("type") != null && !itemMap.get("type").equals("")) {
		  if (itemMap.get("type").equals("FIB")) {
			  itemType = AuthoringConstantStrings.FIB;
		  }
		  else if (itemMap.get("type").equals("Matching"))
		  itemType = AuthoringConstantStrings.MATCHING;
	  }
	  else if (itemMap.get("itemRcardinality") != null && !itemMap.get("itemRcardinality").equals("")) {
		  String itemRcardinality = (String) itemMap.get("itemRcardinality");
		  if ("Single".equalsIgnoreCase(itemRcardinality)) {
			  List answerList = (List) itemMap.get("itemAnswer");
			  if (answerList.size() == 2) {
				  String firstAnswer = ((String) answerList.get(0)).split(":::")[1];
				  String secondAnswer = ((String) answerList.get(1)).split(":::")[1];
				  if ((firstAnswer.equalsIgnoreCase("true") && secondAnswer.equalsIgnoreCase("false"))
						  || (firstAnswer.equalsIgnoreCase("false") && secondAnswer.equalsIgnoreCase("true"))) {
					  itemType = AuthoringConstantStrings.TF;
				  }
				  else {
					  itemType = AuthoringConstantStrings.MCSC;
				  }
			  }
			  else {
				  itemType = AuthoringConstantStrings.MCSC;
			  }
		  }
		  else {
			  itemType = AuthoringConstantStrings.MCMC;
		  }
	  }
	  else {
		  itemType = AuthoringConstantStrings.ESSAY;
	  }
	  Long typeId = getType(itemType);
	  return typeId;
  }

  /**
   * simple unit test
   *
   * @param args not used
   */
  public static void main(String[] args)
  {
 //   ItemFacade item = new ItemFacade();
    String title;
    String intro;
    String qmd;
    String[] typeArray = AuthoringConstantStrings.itemTypes;
    for (int i = 0; i < typeArray.length; i++)
    {
      qmd = typeArray[i];
      title = "title";
      intro = "introspect";
      Long typeId = calculate(title, intro, qmd);
    }
    for (int i = 0; i < typeArray.length; i++)
    {
      title = typeArray[i];
      intro = "introspect";
      qmd = "qmd";
      Long typeId = calculate(title, intro, qmd);
    }
    for (int i = 0; i < typeArray.length; i++)
    {
      title = "title";
      intro = typeArray[i];
      qmd = "qmd";
      Long typeId = calculate(title, intro, qmd);
    }


  }

  /**
   * Figure out the best string describing type to use.
   * @param titleItemType the item's title
   * @param itemIntrospectItemType best guess based on structure
   * @param qmdItemType the type declared in metadata, if any
   * @return the string describing the item.
   */
  private static String obtainTypeString( String titleItemType,
                               String itemIntrospectItemType,
                               String qmdItemType)
  {
    log.debug("qmdItemType: " + qmdItemType);
    log.debug("titleItemType: " + titleItemType);
    log.debug("itemIntrospectItemType: " + itemIntrospectItemType);

    // if we can't find any other approach
    String itemType = itemIntrospectItemType;

    // start with item title
    if (titleItemType != null)
    {
      if (isExactType(titleItemType)){
    	  return titleItemType;
      }
      titleItemType = guessType(titleItemType);
      if (titleItemType != null) itemType = titleItemType;
    }

    // next try to figure out from qmd_itemtype metadata
    if (qmdItemType != null)
    {
      if (isExactType(qmdItemType)){
    	  return qmdItemType;
      }
      qmdItemType = guessType(qmdItemType);
      if (qmdItemType != null) itemType = qmdItemType;
    }

    log.debug("returning itemType: " + itemType);
    return itemType;
  }

  /**
   * Try to infer the type of imported question from string, such as title.
   * @param candidate string to guess type from
   * @return AuthoringConstantStrings.{type}
   */
  private static String guessType(String candidate)
  {
    String itemType;
    String lower = candidate.toLowerCase();
    itemType = matchGuess(lower);

    return itemType;
  }

  /**
   * helper method for guessType() with its type guess strategy.
   * @param toGuess  string to test
   * @return the guessed canonical type
   */
  private static String matchGuess(String toGuess)
  {
	  // not sure how well this works for i18n.  
    String itemType = null;
    if (toGuess.indexOf("multiple") != -1 &&
        toGuess.indexOf("response") != -1)
    {
      itemType = AuthoringConstantStrings.MCMC;
    }
    else if (toGuess.indexOf("true") != -1 ||
             toGuess.indexOf("tf") != -1)
    {
      itemType = AuthoringConstantStrings.TF;
    }
    else if (toGuess.indexOf("matrix") != -1)
    {
    	itemType = AuthoringConstantStrings.MATRIX;
    }
    else if (toGuess.indexOf("survey") != -1)
    {
      itemType = AuthoringConstantStrings.SURVEY;
    }
    else if (toGuess.indexOf("single") != -1 &&
             toGuess.indexOf("correct") != -1)
    {
      if (toGuess.indexOf("selection") != -1) {
    	  itemType = AuthoringConstantStrings.MCMCSS;
      }
      else {
        itemType = AuthoringConstantStrings.MCSC;
      }
    }
    else if (toGuess.indexOf("multiple") != -1 &&
             toGuess.indexOf("correct") != -1)
    {
      itemType = AuthoringConstantStrings.MCMC;
    }
    else if (toGuess.indexOf("audio") != -1 ||
             toGuess.indexOf("recording") != -1)
    {
      itemType = AuthoringConstantStrings.AUDIO;
    }
    else if (toGuess.indexOf("file") != -1 ||
             toGuess.indexOf("upload") != -1)
    {
      itemType = AuthoringConstantStrings.FILE;
    }
    else if (toGuess.indexOf("extended") != -1 &&
    		toGuess.indexOf("matching") != -1)
    {
    	itemType = AuthoringConstantStrings.EMI;
    }
    else if (toGuess.indexOf("match") != -1)
    {
      itemType = AuthoringConstantStrings.MATCHING;
    }
    else if (toGuess.indexOf("fib") != -1 ||
             toGuess.indexOf("fill") != -1 ||
             toGuess.indexOf("f.i.b.") != -1
             )
    {
      itemType = AuthoringConstantStrings.FIB;
    }
    // place holder for numerical responses questions
    else if (toGuess.indexOf("numerical") != -1 ||
            (toGuess.indexOf("calculate") != -1 && toGuess.indexOf("question") == -1) ||
            toGuess.indexOf("math") != -1
            )
   {
     itemType = AuthoringConstantStrings.FIN;
   }
    // CALCULATED_QUESTION
    else if (toGuess.indexOf("calcq") != -1 ||
            toGuess.indexOf("c.q.") != -1 ||
            toGuess.indexOf("cq") != -1 ||
            (toGuess.indexOf("calculate") != -1 && toGuess.indexOf("question") != -1)
            )
    {
        itemType = AuthoringConstantStrings.CALCQ;
    }
    // IMAGEMAP_QUESTION
    else if (toGuess.indexOf("imagmq") != -1 ||
        toGuess.indexOf("im.q.") != -1 ||
        toGuess.indexOf("imq") != -1
    )
    {
        itemType = AuthoringConstantStrings.IMAGMQ;
    }
    else if (toGuess.indexOf("essay") != -1 ||
             toGuess.indexOf("short") != -1)
    {
      itemType = AuthoringConstantStrings.ESSAY;
    }
    return itemType;
  }

  /**
   *
   * @param typeString get type string
   * @return Long item type
   */
  private static Long getType(String typeString)
  {
    Long type = getValidType(typeString);
    if (type==null)
    {
      log.warn("Unable to set item type: '" + typeString + "'.");
      log.warn("guessing item type: '" + DEFAULT_TYPE + "'.");
      type =  DEFAULT_TYPE;
    }

    return type;
  }

  /**
   * Is this one of our exact type strings, or not?
   * Ignores case and extra space.
   * @param typeString the string
   * @return true if it is
   */
  private static boolean isExactType(String typeString)
  {
    return getValidType(typeString)!=null;
  }

  /**
   * Get valid type as Long matching typeString
   * Ignores case and extra space.
   * @param typeString the candidate string
   * @return valid type as Long matching typeString, or null, if no match.
   */
  private static Long getValidType(String typeString)
  {
    Long type = null;

    String[] typeArray = AuthoringConstantStrings.itemTypes;
    for (int i = 0; i < typeArray.length; i++)
    {
      if (typeString.trim().equalsIgnoreCase(typeArray[i]))
      {
        type =  Long.valueOf(i);
        break;
      }
    }
    return type;
  }


}
