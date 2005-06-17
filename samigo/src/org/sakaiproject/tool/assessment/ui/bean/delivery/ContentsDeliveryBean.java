/*
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
 */

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.io.Serializable;

/**
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> Table of Contents and Contents Data</p>
 * <p>This is a 'dual purpose' bean.  It can serve as a
 * representation of the entire (table of) contents for an
 * assessment, or the contents presented in a praticular page view.</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ContentsDeliveryBean
  implements Serializable
{
  private java.util.ArrayList partsContents;
  private float currentScore;
  private float maxScore; // SectionContentsBeans
  // for display/hide score
  private boolean showStudentScore;
  private String pointsDisplayString;

  /**
   * Current score for entire contents.
   * @return current score for entire contents
   */
  public float getCurrentScore()
  {
    return currentScore;
  }

  /**
   * Current score for entire contents
   * @param currentScore current score for entire contents
   */
  public void setCurrentScore(float currentScore)
  {
    this.currentScore = currentScore;
  }

  /**
   * Maximum score for entire contents.
   * @return maximum score for entire contents
   */
  public float getMaxScore()
  {
    return maxScore;
  }

  /**
   * Maximum score for entire contents.
   * @param maxScore maximum score for entire contents
   */
  public void setMaxScore(float maxScore)
  {
    this.maxScore = maxScore;
  }

  /**
   * List of parts (SectionContentsBeans) for entire contents.
   * @return parts for entire contents
   */
  public java.util.ArrayList getPartsContents()
  {
    return partsContents;
  }

  /**
   * Set parts (SectionContentsBeans) for entire contents
   * @param partsContents parts (SectionContentsBeans) for entire contents
   */
  public void setPartsContents(java.util.ArrayList partsContents)
  {
    this.partsContents = partsContents;
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
   * @return the score, return it, followed by a slash or ""
   */
  public String getPointsDisplayString()
  {
    String pointsDisplayString = "";
    if (showStudentScore)
    {
      pointsDisplayString = "/" + currentScore;
    }
    return pointsDisplayString;
  }

}
