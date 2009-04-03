/**********************************************************************************
 * $URL: 
 * $Id: 
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.contentreview.exception;

public class SubmissionException extends Exception {
	
	public SubmissionException() {
		super();
	}

	public SubmissionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SubmissionException(Throwable arg0) {
		super(arg0);
	}

	public SubmissionException(String s) {
		super(s);
	}

}
