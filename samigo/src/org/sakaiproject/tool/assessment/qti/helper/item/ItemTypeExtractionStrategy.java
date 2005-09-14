/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/ExtractionHelper.java $
 * $Id: ExtractionHelper.java 426 2005-07-07 17:57:41Z esmiley@stanford.edu $
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
package org.sakaiproject.tool.assessment.qti.helper.item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.facade.ItemFacade;

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
public class ItemTypeExtractionStrategy
{
  private static Log log = LogFactory.getLog(ItemTypeExtractionStrategy.class);
  private static final Long DEFAULT_TYPE = new Long(2);


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

  /**
   * simple unit test
   *
   * @param args not used
   */
  public static void main(String[] args)
  {
    ItemFacade item = new ItemFacade();
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
    log.info("qmdItemType: " + qmdItemType);
    log.info("titleItemType: " + titleItemType);
    log.info("itemIntrospectItemType: " + itemIntrospectItemType);

    // if we can't find any other approach
    String itemType = itemIntrospectItemType;

    // start with item title
    if (titleItemType != null)
    {
      if (isExactType(titleItemType)) itemType = titleItemType;
      titleItemType = guessType(titleItemType);
      if (titleItemType != null) itemType = titleItemType;
    }

    // next try to figure out from qmd_itemtype metadata
    if (qmdItemType != null)
    {
      if (isExactType(qmdItemType)) itemType = qmdItemType;
      qmdItemType = guessType(qmdItemType);
      if (qmdItemType != null) itemType = qmdItemType;
    }

    log.info("returning itemType: " + itemType);
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
    else if (toGuess.indexOf("survey") != -1)
    {
      itemType = AuthoringConstantStrings.SURVEY;
    }
    else if (toGuess.indexOf("single") != -1 &&
             toGuess.indexOf("correct") != -1)
    {
      itemType = AuthoringConstantStrings.MCSC;
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
        type = new Long(i);
      }
    }
    return type;
  }


}