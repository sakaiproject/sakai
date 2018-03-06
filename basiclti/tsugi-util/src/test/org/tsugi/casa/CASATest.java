package org.tsugi.casa;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.casa.objects.Launch;
import org.tsugi.casa.objects.Use;
import org.tsugi.casa.objects.Contact;
import org.tsugi.casa.objects.Original;
import org.tsugi.casa.objects.Identity;
import org.tsugi.casa.objects.Application;

import org.tsugi.jackson.JacksonUtil;

@Slf4j
public class CASATest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testServiceCompare() {
		Launch launch = new Launch();
		launch.setLaunch_url("https://online.dr-chuck.com/sakai-api-test/tool.php");
		launch.setRegistration_url("https://online.dr-chuck.com/sakai-api-test/tp.php");

		String output = launch.prettyPrintLog();
		assertNotNull(output);

		assertTrue(output.contains("launch_url"));
		assertTrue(output.contains("registration_url"));
		assertTrue(output.contains("sakai-api-test"));

		Use use = new Use(launch);
		use.setIcon_url("http://www.dr-chuck.com/csev.jpg");
		use.setTitle("A title");
		use.setText("Some description");
		use.addContact(new Contact("Dr. Chuck","@drchuck"));

		output = use.prettyPrintLog();
		assertNotNull(output);

		Original orig = new Original(use);
		orig.setUri("http://www.dr-chuck.com");
		orig.setPropagate(Boolean.FALSE);
		orig.setTimestamp("2015-01-02T22:17:00.371Z");
		output = orig.prettyPrintLog();
		assertNotNull(output);

		Identity identity = new Identity("a9a860ae-7c0f-4c12-a1cf-9fe490ee1f49","local-lti-provider");
		output = identity.prettyPrintLog();
		assertNotNull(output);

		Application app = new Application(identity,orig);
		output = app.prettyPrintLog();
		assertNotNull(output);

		ArrayList<Application> apps = new ArrayList<Application>();
		apps.add(app);

		output = JacksonUtil.prettyPrintLog(apps);
		assertNotNull(output);

		log.debug("output={}", output);
	}

}

