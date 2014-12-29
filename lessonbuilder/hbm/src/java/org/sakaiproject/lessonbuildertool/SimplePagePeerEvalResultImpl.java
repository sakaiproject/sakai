/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Kim Huang, kimhuang@rutgers.edu
 *
 * Copyright (c) 2013 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public class SimplePagePeerEvalResultImpl implements SimplePagePeerEvalResult {
	
	
	private long peerEvalResultId;
	private long pageId;
	private Date timePosted; 
	private String grader; 
	private String gradee; 
	private String rowText;
	private int columnValue;
	private boolean selected;

	
	public SimplePagePeerEvalResultImpl() {}
	
	public SimplePagePeerEvalResultImpl(String userId) { 
		this.grader = userId;
		this.timePosted = new Date();
		this.selected = false;
		
	}
	public SimplePagePeerEvalResultImpl(String userId, String gradee) { 
		this.grader = userId;
		this.timePosted = new Date();
		this.gradee = gradee;
		this.selected = false;

		
	}
	public SimplePagePeerEvalResultImpl(long pageId, String gradee, String grader, String rowText, int columnValue) {
		this.pageId = pageId;
		this.timePosted = new Date();
		this.gradee = gradee;
		this.grader = grader;
		this.rowText = rowText;
		this.columnValue = columnValue;
		this.selected = true;

	}
	
	public void setPeerEvalResultId(long id) {
		this.peerEvalResultId = id;
	}
	
	public long getPeerEvalResultId() {
		return peerEvalResultId;
	}
	
	public long getPageId() {
		return pageId;
	}
	public void setPageId(long id){
		this.pageId = id;
	}
	
	public void setTimePosted(Date date) {
		timePosted = date;
	}
	
	public Date getTimePosted() {
		return timePosted;
	}
	
	public String getGrader(){
		return grader;
	}
	public void setGrader(String author){
		this.grader = author;
	}
	
	public String getGradee(){
		return gradee;
	}
	public void setGradee(String author){
		this.gradee = author;
	}

	public String getRowText() {
		return rowText;
	}
	public void setRowText(String text){
		this.rowText = text;
	}
	
	public int getColumnValue(){
		return columnValue;
	}
	public void setColumnValue(int value){
		this.columnValue = value;
	}

	public boolean getSelected() {
		return selected;
	}
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
	public int compareTo(Object o) {
		if(!(o instanceof SimplePagePeerEvalResult)) {
			throw new ClassCastException("Expected SimplePagePeerEvalResult Object");
		}
		
		return timePosted.compareTo(((SimplePagePeerEvalResultImpl)o).getTimePosted());
	}

}
