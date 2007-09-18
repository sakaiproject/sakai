/*
 * Created on 14 May 2007
 */

package org.sakaiproject.entitybroker.impl.oldentity;

import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.impl.EntityHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EntityBrokerEntity implements Entity {

   private EntityHandler entityHandler;

   public void setEntityHandler(EntityHandler entityHandler) {
      this.entityHandler = entityHandler;
   }

   private ResourceProperties properties;
   // we use only two fields in Reference - type -> parsed.prefix, id -> parsed.id
   private Reference reference;

   void setReference(Reference ref) {
      this.reference = ref;
   }

   public String getId() {
      return reference.getId();
   }

   public ResourceProperties getProperties() {
      return properties;
   }

   public String getReference() {
      return reference.getReference();
   }

   public String getReference(String rootProperty) {
      return getReference();
   }

   public String getUrl() {
      return entityHandler.getEntityURL(reference.getReference());
   }

   public String getUrl(String rootProperty) {
      return entityHandler.getEntityURL(reference.getReference());
   }

   public Element toXml(Document doc, Stack stack) {
      // TODO Auto-generated method stub
      return null;
   }

}
