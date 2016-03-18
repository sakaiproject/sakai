/*
 * #%L
 * SCORM API
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
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
 * #L%
 */
package org.adl.sequencer;

import java.io.Serializable;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

public class SeqActivityComparator implements Comparator<ActivityNode>, Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6253881918230754073L;

	public SeqActivityComparator() {
		super();
	}

	public int compare(ActivityNode o1, ActivityNode o2) {
		DefaultMutableTreeNode n1 = o1;
		DefaultMutableTreeNode n2 = o2;

		SeqActivity a1 = (SeqActivity) n1.getUserObject();
		SeqActivity a2 = (SeqActivity) n2.getUserObject();

		int c1 = a1.getCount();
		int c2 = a2.getCount();

		return c1 - c2;
	}

}
