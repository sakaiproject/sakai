/**********************************************************************************
* $URL$
* $Id$
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

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;

import org.sakaiproject.tool.assessment.ui.bean.util.Validator;

/**
 * @version $Id$
 * @author Ed Smiley
 */
public class HistogramBarBean
  implements Serializable
{
  private int numStudents;
  private String numStudentsText;
  private String columnHeight;
  private String rangeInfo;
  private String label;
  private Boolean isCorrect;

  /**
    *
    * @param numStudents int
    */
   public void setNumStudents(int numStudents)
   {
     this.numStudents = numStudents;
   }


   /**
    *
    * @return int
    */
   public int getNumStudents()
   {
     return this.numStudents;
   }

   /**
    *
    * @param pStudents String
    */
   public void setNumStudentsText(String pStudents)
   {
     numStudentsText = pStudents;
   }

   /**
    *
    * @return String
    */
   public String getNumStudentsText()
   {
     return Validator.check(numStudentsText, "N/A");
   }

   /**
    *
    * @param columnHeight String
    */
   public void setColumnHeight(String columnHeight)
   {
     this.columnHeight = columnHeight;
   }

   /**
    *
    * @return String
    */
   public String getColumnHeight()
   {
     return Validator.check(this.columnHeight, "0");
   }

   /**
    *
    * @param range String
    */
   public void setRangeInfo(String range){
     this.rangeInfo = range;
   }

   /**
    *
    * @return String
    */
   public String getRangeInfo()
   {
     return Validator.check(this.rangeInfo, "N/A");
   }

   /**
    *
    * @param range String
    */
   public void setLabel(String plabel){
     label = plabel;
   }

   /**
    *
    * @return String
    */
   public String getLabel()
   {
     return Validator.check(label, "N/A");
   }

   /**
    * @param range String
    */

   public void setIsCorrect(Boolean pcorrect){
     isCorrect = pcorrect;
   }

   /**
    *
    * @return String
    */
   public Boolean getIsCorrect()
   {
     return Validator.bcheck(isCorrect, false);
   }


}
