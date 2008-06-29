/**
 * $Id$
 * $URL$
 * EntityUser.java - entity-broker - Jun 28, 2008 5:24:57 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.providers;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * This class is needed to allow input and output since the User/UserEdit classes are too hard to work with,
 * it is disappointing that this is needed, very disappointing indeed<br/>
 * They seem to already be wrapped in a proxy as well for some reason based on the failure from xstream when
 * it tries to work with them
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityUser implements User {

   private String id;
   private String eid;
   private String password;
   private String email;
   private String firstName;
   private String lastName;
   private String displayName;
   private String type;
   public Map<String, String> props;

   private transient User user;

   public EntityUser() {}

   /**
    * Construct an Entityuser from a legacy user object
    * @param user a legacy user or user edit
    */
   @SuppressWarnings("unchecked")
   public EntityUser(User user) {
      this.user = user;
      this.eid = user.getEid();
      this.email = user.getEmail();
      this.firstName = user.getFirstName();
      this.lastName = user.getLastName();
      this.displayName = user.getDisplayName();
      this.type = user.getType();
      ResourceProperties rp = user.getProperties();
      for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
         String name = iterator.next();
         String value = rp.getProperty(name);
         this.setProperty(name, value);
      }
   }

   public EntityUser(String eid, String email, String firstName, String lastName,
         String displayName, String password, String type) {
      this.eid = eid;
      this.password = password;
      this.email = email;
      this.firstName = firstName;
      this.lastName = lastName;
      this.displayName = displayName;
      this.type = type;
   }

   public void setProperty(String key, String value) {
      props.put(key, value);
   }

   public String getProperty(String key) {
      return props.get(key);
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getEid() {
      return eid;
   }

   public void setEid(String eid) {
      this.eid = eid;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getDisplayName() {
      return displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public Map<String, String> getProps() {
      return props;
   }

   public void setProps(Map<String, String> props) {
      this.props = props;
   }

   // extra User junk below here
   // TODO set these so they are ignored by reflection

   public boolean checkPassword(String arg0) {
      if (user != null) {
         return user.checkPassword(arg0);
      }
      return false;
   }

   public User getCreatedBy() {
      if (user != null) {
         return user.getCreatedBy();
      }
      throw new NotImplementedException();
   }

   public Time getCreatedTime() {
      if (user != null) {
         return user.getCreatedTime();
      }
      throw new NotImplementedException();
   }

   public String getDisplayId() {
      if (user != null) {
         return user.getDisplayId();
      }
      throw new NotImplementedException();
   }

   public User getModifiedBy() {
      if (user != null) {
         return user.getModifiedBy();
      }
      throw new NotImplementedException();
   }

   public Time getModifiedTime() {
      if (user != null) {
         return user.getModifiedTime();
      }
      throw new NotImplementedException();
   }

   public String getSortName() {
      if (user != null) {
         return user.getSortName();
      }
      return lastName;
   }

   public ResourceProperties getProperties() {
      if (user != null) {
         return user.getProperties();
      }
      throw new NotImplementedException();
   }

   public String getReference() {
      return "/user/" + id;
   }

   public String getReference(String arg0) {
      return getReference();
   }

   public String getUrl() {
      if (user != null) {
         return user.getUrl();
      }
      throw new NotImplementedException();
   }

   public String getUrl(String arg0) {
      if (user != null) {
         return user.getUrl(arg0);
      }
      throw new NotImplementedException();
   }

   @SuppressWarnings("unchecked")
   public Element toXml(Document arg0, Stack arg1) {
      if (user != null) {
         return user.toXml(arg0, arg1);
      }
      throw new NotImplementedException();
   }

   @SuppressWarnings("unchecked")
   public int compareTo(Object o) {
      if (user != null) {
         return user.compareTo(o);
      }
      throw new NotImplementedException();
   }
}
