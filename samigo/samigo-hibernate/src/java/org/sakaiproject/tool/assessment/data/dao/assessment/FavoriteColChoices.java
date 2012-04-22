/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;

public class FavoriteColChoices implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4610240359260147946L;
	private Long favoriteId;
	private String favoriteName;
	private String ownerStringId;
	private Set favoriteItems = new HashSet();
	
	public FavoriteColChoices() {}
	public FavoriteColChoices(String name, String agentId){
		
		this.favoriteName = name;
		this.ownerStringId = agentId;
	}
	public FavoriteColChoices(String name, String agentId, Set favoriteItems){
		this.favoriteName = name;
		this.ownerStringId = agentId;
		this.favoriteItems = favoriteItems;
	}
	public Long getFavoriteId(){
		return favoriteId;
	}
	public void setFavoriteId(Long id){
		this.favoriteId = id;
	}
	public String getFavoriteName(){
		return favoriteName;
	}
	public void setFavoriteName(String name){
		this.favoriteName = name;
	}
	public String getOwnerStringId(){
		return ownerStringId;
	}
	public void setOwnerStringId(String id){
		this.ownerStringId = id;
	}
	public Set getFavoriteItems(){
		return favoriteItems;
	}
	public void setFavoriteItems(Set set) {
		this.favoriteItems = set;
		
	}
}
