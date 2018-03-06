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

public interface SimplePagePeerEval {
	
	public long getPeerEvalId();
	public void setPeerEvalId(long id);
	
	
	public String getRowText();
	public void setRowText(String text);
	
	public int getRowSequence();
	public void setRowSequence(int sequence);


}
