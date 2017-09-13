/**
 * Copyright (c) 2003-2013 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public class SimplePagePeerEvalImpl implements SimplePagePeerEval {
		private long peerEvalId;
		private String rowText; // Text of the row
		private int rowSequence;
		
		public SimplePagePeerEvalImpl(String peerEvalName, String rowText, int rowSequence) {
			
			this.rowText = rowText;
			this.rowSequence = rowSequence;
		}
	
	public SimplePagePeerEvalImpl() { }
	
	public long getPeerEvalId(){
		return peerEvalId;
	}
	public void setPeerEvalId(long id){
		this.peerEvalId = id;
	}
	
	public void setRowText(String text) {
		this.rowText = text;
	}
	public String getRowText() {
		return rowText;
	}
	
	public void setRowSequence(int seq) {
		this.rowSequence = seq;
	}
	
	public int getRowSequence() {
		return rowSequence;
	}
}
