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

import java.util.HashMap;
import java.util.Map;

/**
 * This is a single item on a simple page.
 * 
 * WARNING: this code should execute under oracle and mysql. Oracle treats "" as null, 
 * and apparently stores null as "". To produce predictable results for the rest of the
 * code, we normalize some of the fields to "" if they are null. The problem is if we
 * write "", Oracle will read it as null, but our code expects it to come back as "".
 * Note that this code is called both to construct new items in our code and by hibernate
 * to build an object when reading from the database.
 * 
 * NOTE: Please don't add any more fields to this class.  Instead, take a look at
 * the SimplePageItemAttribute system we have set up.
 *
 * @author jeney
 * 
 */
public class SimplePageQuestionResponseTotalsImpl implements SimplePageQuestionResponseTotals {

    private long id;
    private long questionid;
    private long responseid;
    private long count;

    public SimplePageQuestionResponseTotalsImpl() {
    }

    public SimplePageQuestionResponseTotalsImpl(long qid, long rid) {
	questionid = qid;
	responseid = rid;
	count = 0L;
    }

    public long getId() {
	return id;
    }

    public void setId(long i) {
	id = i;
    }

    public long getQuestionId() {
	return questionid;
    }

    public void setQuestionId(long qid) {
	questionid = qid;
    }

    public long getResponseId() {
	return responseid;
    }

    public void setResponseId(long rid) {
	responseid = rid;
    }

    public long getCount() {
	return count;
    }

    public void setCount(long c) {
	count = c;
    }

}

