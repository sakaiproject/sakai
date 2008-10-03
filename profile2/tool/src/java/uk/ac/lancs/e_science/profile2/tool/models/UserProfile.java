package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;

public class UserProfile implements Serializable {

	//we need a var and getters/setters for every field thats in the profile.
	private String nickname;	
	
	
	public String getNickname()
	  {
	    return nickname;
	  }
	  public void setNickname(String nickname)
	  {
	    this.nickname = nickname;
	  }
	  
	 	
}
