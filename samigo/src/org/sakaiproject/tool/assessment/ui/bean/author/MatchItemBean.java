/**********************************************************************************
* $HeadURL$
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
package org.sakaiproject.tool.assessment.ui.bean.author;


import java.io.Serializable;
public class MatchItemBean implements Serializable {

  private static final long serialVersionUID = 7526471155622776147L;

//  private String text;
  private Long sequence;
//  private String corrfeedback;
//  private String incorrfeedback;
  private Boolean isCorrect;
  private String choice;  //
  private String match;
  private String corrMatchFeedback;
  private String incorrMatchFeedback;
  private String sequenceStr;


  public MatchItemBean() {
	// sequence = -1 for new items
	sequence = new Long(-1);
	sequenceStr = "-1";
  }
/*
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
*/

  public Long getSequence() {
    return sequence;
  }

  public void setSequence(Long sequence) {
    this.sequence = sequence;
    this.sequenceStr = sequence.toString();
  }

// used by jsf to check if the current pair is for editing
  public String getSequenceStr() {
    return sequenceStr;
  }

  public void setSequenceStr(String param) {
    this.sequenceStr= param;
  }

/*
  public String getCorrfeedback() {
    return corrfeedback;
  }

  public void setCorrfeedback(String feedback) {
    this.corrfeedback= feedback;
  }

  public String getIncorrfeedback() {
    return incorrfeedback;
  }

  public void setIncorrfeedback(String feedback) {
    this.incorrfeedback= feedback;
  }

*/

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public String getChoice() {
    return choice;
  }
  public void setChoice(String param) {
    this.choice= param;
  }

  public String getMatch() {
    return match ;
  }
  public void setMatch(String param) {
    this.match= param;
  }

  public String getCorrMatchFeedback() {
    return corrMatchFeedback;
  }
  public void setCorrMatchFeedback(String param) {
    this.corrMatchFeedback= param;
  }

  public String getIncorrMatchFeedback() {
    return incorrMatchFeedback;
  }
  public void setIncorrMatchFeedback(String param) {
    this.incorrMatchFeedback= param;
  }



}
