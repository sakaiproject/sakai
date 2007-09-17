package org.dojo.moxie;

import java.util.*;
import java.util.regex.*;

/**
	@author Brad Neuberg, bkn3@columbia.edu
*/
public class Document{
	protected Integer id = null;
	protected Integer origId = null;
	protected String fileName = null;
	protected long createdOn;
	protected long lastUpdated;
	protected String content = null;
	
	/**
		@param id The ID of this document; this can either be a positive
		number if this document exists in the database; it can also be null
		or negative to indicate that no database-assigned id exists yet.
		@throws IllegalArgumentException Thrown if fileName,
		createdOn, or lastUpdated are null or if fileName is invalid.
	*/
	public Document(Integer id, String fileName, long createdOn,
					long lastUpdated, String content)
								throws MoxieException{
		if(validFileName(fileName) == false){
			throw new MoxieException("Invalid file name");
		}
		
		this.id = id;
		this.fileName = fileName;
		this.createdOn = createdOn;
		this.lastUpdated = lastUpdated;
		this.content = content;
	}
	
	public void setId(Integer Id){
		this.id = id;
	}
	
	public Integer getId(){
		return this.id;
	}
	
	public void setOrigId(Integer origId){
		this.origId = origId;
	}
	
	public Integer getOrigId(){
		return this.origId;
	}
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public void setCreatedOn(long createdOn){
		this.createdOn = createdOn;
	}
	
	public long getCreatedOn(){
		return this.createdOn;
	}
	
	public void setLastUpdated(long lastUpdated){
		this.lastUpdated = lastUpdated;
	}
	
	public long getLastUpdated(){
		return this.lastUpdated;
	}
	
	public void setContent(String content){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}
	
	public String toString(){
		StringBuffer results = new StringBuffer();
		results.append("{");
		results.append("id: " + this.id + ", ");
		if(this.origId != null){
			results.append("origID: " + this.origId + ", ");
		}
		results.append("fileName: '" + this.fileName + "', ");
		results.append("createdOn: " + this.createdOn + ", ");
		results.append("lastUpdated: " + this.lastUpdated + ", ");
		results.append("content: '" + this.content + "'");
		results.append("}");
		
		return results.toString();
	}
	
	public static boolean validFileName(String fileName){
		if(fileName == null || fileName.trim().equals("")){
			return false;
		}
		
		return Pattern.matches("^[0-9A-Za-z_]*$", fileName); 
	}
}
