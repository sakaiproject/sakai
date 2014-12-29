/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.poll.tool.params;

import java.io.Serializable;


import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.VoteCollection;

public class VoteBean implements Serializable {

	public transient VoteCollection voteCollection;
	public transient Poll poll;
	
	public VoteCollection getVoteCollection(){
		return voteCollection;
	}
	
	public Poll getPoll() {
		return poll;
	}
	public void setPoll(Poll p){
		this.poll = p;
	}
}
