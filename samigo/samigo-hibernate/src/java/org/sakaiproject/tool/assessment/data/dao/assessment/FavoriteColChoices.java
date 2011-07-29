package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.util.Set;
import java.util.HashSet;

public class FavoriteColChoices {
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
