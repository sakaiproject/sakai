package org.sakaiproject.emailtemplateservice.logic.test;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.emailtemplateservice.dao.impl.EmailTemplateServiceDao;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.service.impl.EmailTemplateServiceImpl;
import org.springframework.test.AbstractTransactionalSpringContextTests;




public class TestEmailTemplateService extends AbstractTransactionalSpringContextTests {

	private static final String KEY_1 = "key1";
	private static final String KEY_2 = "key2";
	
	private static final String US_LOCALE = "en_us";
	private static final String ZA_LOCALE = "en_ZA";

	private static final String ADMIN_USER = "admin";


	EmailTemplateServiceDao dao;
	EmailTemplateServiceImpl emailTemplateService;


	EmailTemplate template1 = new EmailTemplate();
	EmailTemplate template2 = new EmailTemplate();
	EmailTemplate template3 = new EmailTemplate();
	
	Long template1Id = null;

	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] { "hibernate-test.xml",
				"classpath:org/sakaiproject/emailtemplateservice/spring-hibernate.xml"};
	}
	// run this before each test starts
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		
		dao = (EmailTemplateServiceDao)applicationContext.getBean("org.sakaiproject.emailtemplateservice.dao.EmailTemplateServiceDao");
		if (dao == null) {
			throw new NullPointerException("DAO could not be retrieved from spring context");
		}

		emailTemplateService = new EmailTemplateServiceImpl();
		emailTemplateService.setDao(dao);
		
		emailTemplateService.deleteAllTemplates();
		populateData();

	}

	private void populateData() {
		template1.setKey(KEY_1);
		template1.setLocale(EmailTemplate.DEFAULT_LOCALE);
		template1.setLastModified(new Date());
		template1.setOwner(ADMIN_USER);
		template1.setSubject("Subject 1");
		template1.setMessage("message 1");
		emailTemplateService.saveTemplate(template1);

		
		template1Id = template1.getId();
		
		template2.setKey(KEY_2);
		template2.setLocale(EmailTemplate.DEFAULT_LOCALE);
		template2.setLastModified(new Date());
		template2.setOwner(ADMIN_USER);
		template2.setSubject("Subject 2");
		template2.setMessage("message 2");
		
		
		
		template3.setKey(KEY_1);
		template3.setLocale(ZA_LOCALE);
		template3.setLastModified(new Date());
		template3.setOwner(ADMIN_USER);
		template3.setSubject("Subject 1");
		template3.setMessage("message 1");
		emailTemplateService.saveTemplate(template3);
	}
	

	public void testSaveTemplate() {

		
		
		//saving should set the ID
		assertNotNull(template1.getId());

		emailTemplateService.saveTemplate(template2);
		assertNotNull(template2.getId());
		//if these are the same there is something very wrong
		assertNotSame(template2.getId(), template1.getId());
		
		
		//we should not be able to save a new template in the same locale/key
		EmailTemplate template3 = new EmailTemplate();
		template3.setKey(KEY_1);
		template3.setLocale(US_LOCALE);
		template3.setLastModified(new Date());
		template3.setOwner(ADMIN_USER);
		template3.setSubject("Subject 1");
		template3.setMessage("message 1");
		
		EmailTemplate template4 = new EmailTemplate();
		template4.setKey(KEY_1);
		template4.setLocale(ZA_LOCALE);
		template4.setLastModified(new Date());
		template4.setOwner(ADMIN_USER);
		template4.setSubject("Subject 1");
		template4.setMessage("message 1");
		
		try {
			emailTemplateService.saveTemplate(template3);
			emailTemplateService.saveTemplate(template4);
			fail();
		}
		catch (Exception e) {
			//e.printStackTrace();
		}
		
	}


	public void testGetTemplatebyId() {
		EmailTemplate t1 =this.emailTemplateService.getEmailTemplateById(template1Id);
		assertNotNull(t1);
		assertEquals(t1.getKey(), KEY_1);
		
	}


	public void testGetEmailTemplate() {
		EmailTemplate ti = emailTemplateService.getEmailTemplate(KEY_1, null);
		assertNotNull(ti);
		
		
		//There is no specific template for en_us we still expect the default
		EmailTemplate t2 = emailTemplateService.getEmailTemplate(KEY_1, new Locale("en", "us"));
		assertNotNull(t2);
		assertEquals(EmailTemplate.DEFAULT_LOCALE, t2.getLocale());
		
		
	}
	
	public void testEmptyTemplate() {
		
		EmailTemplate badTemplate = new EmailTemplate();
		try {
		emailTemplateService.saveTemplate(badTemplate);
		fail();
		}
		catch (IllegalArgumentException e) {
			//we expect this
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		badTemplate.setKey("someKey");
		try {
			emailTemplateService.saveTemplate(badTemplate);
			fail();
		}
		catch (IllegalArgumentException e) {
			//we expect this
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		
		badTemplate.setOwner("admin");
		try {
			emailTemplateService.saveTemplate(badTemplate);
			fail();
		}
		catch (IllegalArgumentException e) {
			//we expect this
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		
		badTemplate.setSubject("something");
		try {
			emailTemplateService.saveTemplate(badTemplate);
			fail();
		}
		catch (IllegalArgumentException e) {
			//we expect this
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		badTemplate.setMessage("something");
		badTemplate.setLocale(" ");
		try {
			emailTemplateService.saveTemplate(badTemplate);
			assertEquals(EmailTemplate.DEFAULT_LOCALE, badTemplate.getLocale());
			assertNotSame("Template Locale can't be empty", "", badTemplate.getLocale());
			
		}
		catch (IllegalArgumentException e) {
			//we expect this
			fail();
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		badTemplate.setLocale("some_Locale");
		//this should now work
		try {
			emailTemplateService.saveTemplate(badTemplate);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}


}
