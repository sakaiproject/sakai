package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;

public class TestModel implements Serializable {

	private String name;	
	
	public TestModel() {
		super();
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	
}
