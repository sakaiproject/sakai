/**********************************************************************************
 * $URL: 
 * $Id: 
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.tool.assessment.ui.bean.author;

/**
 * CalculatedQuestionIfc is an interface that covers the common functionality
 * for variables and formulas that are part of a CalculatedQuestion. Variables
 * and Formulas are both stored in the sam_itemtext_t and sam_answer_t tables
 * and on occasion need to be used similarly.
 * 
 * @author mgillian
 *
 */
public interface ImageMapQuestionAnswerIfc {

	/**
	 * getName() returns the name of the object. Variable and Formula names both
	 * begin with an alpha character and can then hae any number of
	 * alpha-numeric characters.
	 * 
	 * @return
	 */
	public String getName();

	public void setName(String name);

	/**
	 * getSequence() returns the sequence number stored with the variable or
	 * formula in the sam_answer_t table
	 * 
	 * @return
	 */
	public Long getSequence();

	public void setSequence(Long sequence);

	/**
	 * getMatch() returns the encoded data stored in the sam_answer_t table,
	 * "text" field. Formulas and variables are both encoded differently, but
	 * they are stored in the same place.
	 * 
	 * @return
	 */
	public String getMatch();

}