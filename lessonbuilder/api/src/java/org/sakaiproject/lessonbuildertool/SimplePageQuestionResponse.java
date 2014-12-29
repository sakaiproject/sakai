/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

public interface SimplePageQuestionResponse {

    public long getId();

    public void setId(long id);

    public Date getTimeAnswered();

    public void setTimeAnswered(Date lastViewed);

    public String getUserId();

    public void setUserId(String userId);

    public long getQuestionId();

    public void setQuestionId(long questionId);

    public boolean isCorrect();

    public void setCorrect(boolean c);
    
	public String getShortanswer();
	
	public void setShortanswer(String sa);
	
	public long getMultipleChoiceId();
	
	public void setMultipleChoiceId(long id);
	
	public Double getPoints();
	
	public void setPoints(Double points);
	
	public boolean isOverridden();
	
	public void setOverridden(boolean overridden);
	
	public String getOriginalText();
	
	public void setOriginalText(String originalText);

}
