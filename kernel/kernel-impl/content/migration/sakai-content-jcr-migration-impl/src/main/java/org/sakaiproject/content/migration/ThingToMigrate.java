package org.sakaiproject.content.migration;

import java.util.Date;

/* 
 CREATE TABLE MIGRATE_CHS_CONTENT_TO_JCR (
 id INT NOT NULL AUTO_INCREMENT,
 PRIMARY KEY (id),
 CONTENT_ID varchar(255),
 STATUS int NOT NULL DEFAULT 0,
 TIME_ADDED_TO_QUEUE datetime,
 -- ORIGINAL_MIGRATION will be used for the 
 -- original copy. Things added to the queue 
 -- later will use their actual event code, ex
 -- content.new, content.delete, etc
 EVENT_TYPE varchar(32) DEFAULT 'ORIGINAL_MIGRATION'
 
 );
 */
public class ThingToMigrate
{
	public String contentId;

	public int status;

	public Date timeAdded;

	public String eventType;
}
