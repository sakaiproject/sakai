/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.emailtemplateservice.model;

import java.util.Date;

/**
 * EmailTemplate is an email template, though it could actually be used for anything,
 * identified by a unique key and set to be locale specific if desired
 */
public class EmailTemplate implements java.io.Serializable {

   // Fields

   private Long id;

   private Date lastModified;

   private String key;

   private String locale;

   private String owner;

   private String subject;

   private String message;

   private String defaultType;

   // Constructors

   /** default constructor */
   public EmailTemplate() {
   }

   /** minimal constructor 
    * @param key TODO*/
   public EmailTemplate(String key, String owner, String message) {
      if (this.lastModified == null) {
         this.lastModified = new Date();
      }
      this.owner = owner;
      this.message = message;
   }

   /** full constructor */
   public EmailTemplate(String key, String owner, String message, String defaultType, String locale) {
      if (this.lastModified == null) {
         this.lastModified = new Date();
      }
      this.key = key;
      this.locale = locale;
      this.owner = owner;
      this.message = message;
      this.defaultType = defaultType;
   }

   // Property accessors
   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Date getLastModified() {
      return this.lastModified;
   }

   public void setLastModified(Date lastModified) {
      this.lastModified = lastModified;
   }

   public String getOwner() {
      return this.owner;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getDefaultType() {
      return this.defaultType;
   }

   public void setDefaultType(String defaultType) {
      this.defaultType = defaultType;
   }

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getLocale() {
      return locale;
   }

   public void setLocale(String locale) {
      this.locale = locale;
   }

   public String getSubject() {
      return subject;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

}
