/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.postem;

/**
 * @author rduhon
 */

import java.util.List;

import org.sakaiproject.api.app.postem.data.Gradebook;

public class Column {

	public Gradebook gradebook;

	public int column;

	public Column(Gradebook gradebook, int column) {
		this.gradebook = gradebook;
		this.column = column;
	}

	public List getSummary() {
		try {
			return gradebook.getAggregateData(column);
		} catch (Exception exception) {
			return null;
		}
	}

	public List getRaw() {
		return gradebook.getRawData(column);
	}

	public boolean getHasName() {
		return gradebook.getHeadings().size() > 0;
	}

	public String getName() {
		try {
			return (String) gradebook.getHeadings().get(column + 1);
		} catch (Exception exception) {
			return "" + (column + 1);
		}
	}
}
