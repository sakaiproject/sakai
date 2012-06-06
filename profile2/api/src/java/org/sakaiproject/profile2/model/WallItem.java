/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WallItem represents an item posted on a profile wall. Items can be different
 * types e.g. events, posts, status updates, and can have comments attached.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Data
@NoArgsConstructor
public class WallItem implements Serializable, Comparable<WallItem> {

	private static final long serialVersionUID = 1L;

	private long id;
	// the id of the user whose wall the item is posted on
	private String userUuid;
	// the id of the user who created this wall item
	private String creatorUuid;
	private String text;
	private Date date;
	private int type;
	
	// any comments that may be attached to the wall item
	private List<WallItemComment> comments = new ArrayList<WallItemComment>();
	
	// add comment (have purposefully omitted ability to remove comment)
	public void addComment(WallItemComment comment) {
		comment.setWallItem(this);
		comments.add(comment);
	}
	
	@Override
	public int compareTo(WallItem wallItem) {
		// TODO might instead create Comparators so we can ascend or descend as needed
		if (date.getTime() > wallItem.getDate().getTime()) {
			return -1;
		} else if (date.getTime() < wallItem.getDate().getTime()) {
			return 1;
		} else {
			return 0;
		}
	}
	
}
