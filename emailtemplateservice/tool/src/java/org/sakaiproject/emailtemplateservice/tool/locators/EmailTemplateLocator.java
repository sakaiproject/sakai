package org.sakaiproject.emailtemplateservice.tool.locators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class EmailTemplateLocator implements WriteableBeanLocator {

   private static Logger log = LoggerFactory.getLogger(EmailTemplateLocator.class);

   public static final String NEW_PREFIX = "new ";
   public static final String NEW_1 = NEW_PREFIX + "1";
   private Map delivered = new HashMap();

   private EmailTemplateService emailTemplateService;
   public void setEmailTemplateService(EmailTemplateService ets) {
      emailTemplateService = ets;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }

   public Object locateBean(String name) {
      Object togo=delivered.get(name);
      if (togo == null){
         if(name.startsWith(NEW_PREFIX)){
            togo = new EmailTemplate();
         }
         else { 
            log.info("looking for template: " + name);
            Long emailTemplateId = Long.valueOf(name);
            togo = emailTemplateService.getEmailTemplateById(emailTemplateId);
         }
         delivered.put(name, togo);
      }
      return togo;
   }


   public boolean remove(String beanname) {
      throw new UnsupportedOperationException("Not implemented");

   }
   public void set(String beanname, Object toset) {
      throw new UnsupportedOperationException("Not implemented");

   }


   public String saveAll() {
	   log.debug("Saving all!");
	   for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
         String key = it.next();
         log.debug("got key: " + key);
         
         EmailTemplate emailTemplate = (EmailTemplate) delivered.get(key);

         if (StringUtils.isBlank(emailTemplate.getLocale())) {
        	 emailTemplate.setLocale(EmailTemplate.DEFAULT_LOCALE);
         }

         // check to see if this template already exists
         Locale loc = null;
         if (!StringUtils.equals(emailTemplate.getLocale(), EmailTemplate.DEFAULT_LOCALE)) {
             try {
                     loc = LocaleUtils.toLocale(emailTemplate.getLocale());
             }
             catch (IllegalArgumentException ie) {
        	         messages.addMessage(new TargettedMessage("error.invalidlocale", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
             }
         }

         //key can't be null
         if (StringUtils.isBlank(emailTemplate.getKey())) {
        	 messages.addMessage(new TargettedMessage("error.nokey", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
         }
         else if (StringUtils.startsWith(key, NEW_PREFIX) && emailTemplateService.templateExists(emailTemplate.getKey(), loc)) {
             messages.addMessage(new TargettedMessage("error.duplicatekey", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
         }
         
         if (StringUtils.isBlank(emailTemplate.getSubject())) {
        	 messages.addMessage(new TargettedMessage("error.nosubject", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
         }
         
         if (StringUtils.isBlank(emailTemplate.getMessage())) {
        	 messages.addMessage(new TargettedMessage("error.nomessage", new Object[]{}, TargettedMessage.SEVERITY_ERROR));
         }
         
         if (messages.isError()) {
        	 return "failure";
         }
         
         emailTemplateService.saveTemplate(emailTemplate);
         messages.addMessage( new TargettedMessage("template.saved.message",
               new Object[] { emailTemplate.getKey(), emailTemplate.getLocale() }, 
               TargettedMessage.SEVERITY_INFO));
      }
	   return "success";
   }

}
