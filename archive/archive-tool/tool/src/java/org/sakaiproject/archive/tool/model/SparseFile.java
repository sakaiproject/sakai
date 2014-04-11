package org.sakaiproject.archive.tool.model;

import java.util.Date;

import lombok.Data;

/**
 * Helper to store details about a file
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
public class SparseFile {

	private String filename; //used as key to download file
	private String absolutePath;
	private String siteId;
	private String siteTitle; //may not be able to be resolved
	private String size; //display formatted
	private String dateCreated;
	private String hash;
	
}