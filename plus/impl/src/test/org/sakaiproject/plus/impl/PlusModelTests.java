/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.impl;

import java.time.Instant;

import org.sakaiproject.plus.api.Launch;
import org.sakaiproject.plus.api.PlusService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import org.sakaiproject.lti13.util.SakaiLaunchJWT;

import org.tsugi.lti13.LTI13JwtUtil;
import org.tsugi.lti13.LTI13ConstantsUtil;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.ContextLog;
import org.sakaiproject.plus.api.model.Link;
import org.sakaiproject.plus.api.model.LineItem;
import org.sakaiproject.plus.api.model.Score;
import org.sakaiproject.plus.api.model.Membership;

import org.sakaiproject.plus.api.repository.TenantRepository;
import org.sakaiproject.plus.api.repository.SubjectRepository;
import org.sakaiproject.plus.api.repository.ContextRepository;
import org.sakaiproject.plus.api.repository.ContextLogRepository;
import org.sakaiproject.plus.api.repository.LinkRepository;
import org.sakaiproject.plus.api.repository.LineItemRepository;
import org.sakaiproject.plus.api.repository.ScoreRepository;
import org.sakaiproject.plus.api.repository.MembershipRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PlusTestConfiguration.class})
public class PlusModelTests extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired private SecurityService securityService;
	@Autowired private SessionManager sessionManager;
	@Autowired private UserDirectoryService userDirectoryService;
	@Autowired private TenantRepository tenantRepository;
	@Autowired private SubjectRepository subjectRepository;
	@Autowired private ContextRepository contextRepository;
	@Autowired private ContextLogRepository contextLogRepository;
	@Autowired private LinkRepository linkRepository;
	@Autowired private LineItemRepository lineItemRepository;
	@Autowired private ScoreRepository scoreRepository;
	@Autowired private MembershipRepository membershipRepository;
	@Autowired private PlusService plusService;

	User user1User = null;
	User user2User = null;

	@Before
	public void setup() {

		reset(sessionManager);
		reset(securityService);
		reset(userDirectoryService);
		user1User = mock(User.class);
		when(user1User.getDisplayName()).thenReturn("User 1");
		user2User = mock(User.class);
		when(user2User.getDisplayName()).thenReturn("User 2");
	}

	@Test
	public void testModelObjects() {

		Instant now = Instant.now();
		Tenant tenant = new Tenant();
		tenant.setCreatedAt(now);
		tenant.setTitle("Yada");
		tenant.setIssuer("https://www.example.com");
		tenant.setClientId("42");
		tenant.setOidcAuth("https://www.example.com/auth");
		tenant.setOidcKeySet("https://www.example.com/keyset");
		assertTrue(tenant.isDraft());
		tenant.setOidcToken("https://www.example.com/token");
		assertFalse(tenant.isDraft());

		// Lets mess with deployment id validation logic
		// Null should allow anything
		assertTrue(tenant.validateDeploymentId("42"));
		tenant.setDeploymentId("*");
		assertTrue(tenant.validateDeploymentId("hello"));
		assertTrue(tenant.validateDeploymentId("world"));
		tenant.setDeploymentId("1");
		assertTrue(tenant.validateDeploymentId("1"));
		assertFalse(tenant.validateDeploymentId("42"));
		tenant.setDeploymentId("hello:world:42:zap");
		assertFalse(tenant.validateDeploymentId("1"));
		assertTrue(tenant.validateDeploymentId("hello"));
		assertTrue(tenant.validateDeploymentId("world"));
		assertTrue(tenant.validateDeploymentId("42"));
		assertTrue(tenant.validateDeploymentId("zap"));
		tenant.setDeploymentId("1");

		// Map<String, String> settings = tenant.getSettings();
		// settings.put("secret", "42");
		tenantRepository.save(tenant);
		String tenantId = tenant.getId();

		Optional<Tenant> optTenant = tenantRepository.findById(tenantId);
		Tenant newTenant = null;
		if ( optTenant.isPresent() ) {
			newTenant = optTenant.get();
		}
		assertNotNull(newTenant);
		assertEquals(newTenant.getTitle(), "Yada");
		assertEquals(newTenant.getIssuer(), "https://www.example.com");
		assertEquals(newTenant.getClientId(), "42");
		assertEquals(newTenant.getDeploymentId(), "1");
		assertEquals(newTenant.getOidcAuth(), "https://www.example.com/auth");
		assertEquals(newTenant.getOidcKeySet(), "https://www.example.com/keyset");
		assertEquals(newTenant.getVerbose(), Boolean.FALSE);

		newTenant = tenantRepository.findByIssuerClientIdAndDeploymentId("https://www.example.com", "42", "1");
		assertNotNull(newTenant);
		assertEquals(newTenant.getTitle(), "Yada");
		assertEquals(newTenant.getIssuer(), "https://www.example.com");
		assertEquals(newTenant.getClientId(), "42");
		assertEquals(newTenant.getDeploymentId(), "1");
		assertEquals(newTenant.getOidcAuth(), "https://www.example.com/auth");
		assertEquals(newTenant.getOidcKeySet(), "https://www.example.com/keyset");

		newTenant = tenantRepository.findByIssuerClientIdAndDeploymentId("https://www.not-example.com", "42", "1");
		assertNull(newTenant);


		Subject subject = new Subject();
		subject.setSubject("Yada");
		subject.setTenant(tenant);
		subject.setEmail("hirouki@p.com");
		subject.setSakaiUserId("user-12345");
		subjectRepository.save(subject);
		assertNotNull(subject.getCreatedAt());
		assertNotNull(subject.getModifiedAt());

		Subject newSubject = subjectRepository.findBySubjectAndTenant("Yada", tenant);
		assertEquals(subject.getEmail(), newSubject.getEmail());
		assertEquals(subject.getSubject(), newSubject.getSubject());
		assertEquals(subject.getSakaiUserId(), newSubject.getSakaiUserId());

		newSubject = subjectRepository.findByEmailAndTenant("hirouki@p.com", tenant);
		assertEquals(subject.getEmail(), newSubject.getEmail());
		assertEquals(subject.getSubject(), newSubject.getSubject());
		assertEquals(subject.getSakaiUserId(), newSubject.getSakaiUserId());

		Context context = new Context();
		context.setContext("SI364");
		context.setTenant(tenant);
		context.setSakaiSiteId("site-123");
		context.setDeploymentId("42");
		contextRepository.save(context);

		Context newContext = contextRepository.findBySakaiSiteId("site-123-wrong");
		assertNull(newContext);
		newContext = contextRepository.findBySakaiSiteId("site-123");
		assertEquals(context.getContext(), newContext.getContext());
		assertEquals(context.getTenant(), newContext.getTenant());
		assertEquals(context.getSakaiSiteId(), newContext.getSakaiSiteId());

		newSubject = subjectRepository.findBySakaiUserIdAndSakaiSiteId("user-12345-wrong", "site-123");
		assertNull(newSubject);
		newSubject = subjectRepository.findBySakaiUserIdAndSakaiSiteId("user-12345", "site-123-wrong");
		assertNull(newSubject);

		newSubject = subjectRepository.findBySakaiUserIdAndSakaiSiteId("user-12345", "site-123");
		assertEquals(subject.getEmail(), newSubject.getEmail());
		assertEquals(subject.getSubject(), newSubject.getSubject());
		assertEquals(subject.getSakaiUserId(), newSubject.getSakaiUserId());

		Membership ms = new Membership();
		ms.setSubject(subject);
		ms.setContext(context);
		assertFalse(ms.isInstructor());
		ms.setLtiRoles(LTI13ConstantsUtil.ROLE_LEARNER);
		assertFalse(ms.isInstructor());
		ms.setLtiRolesOverride(LTI13ConstantsUtil.ROLE_INSTRUCTOR);
		assertTrue(ms.isInstructor());
		ms.setLtiRolesOverride(null);
		assertFalse(ms.isInstructor());
		ms.setLtiRoles(LTI13ConstantsUtil.ROLE_INSTRUCTOR);
		assertTrue(ms.isInstructor());

		ms = new Membership();
		ms.setSubject(subject);
		ms.setContext(context);
		ms.setLtiRoles(LTI13ConstantsUtil.ROLE_INSTRUCTOR);
		ms = membershipRepository.upsert(ms);
		// Save should do the same
		// ms = membershipRepository.save(ms);
		assertNotNull(ms.getId());

		// Upsert is in effect a SELECT when there are no changes
		Membership dms = new Membership();
		dms.setSubject(subject);
		dms.setContext(context);
		dms.setLtiRoles(LTI13ConstantsUtil.ROLE_INSTRUCTOR);
		dms = membershipRepository.upsert(dms);
		// Save should do the same - but lets test all upsert() use cases
		// ms = membershipRepository.save(ms);
		assertNotNull(dms.getId());
		assertEquals(ms.getId(), dms.getId());
		assertEquals(ms.getLtiRoles(),dms.getLtiRoles());
		assertEquals(ms.getLtiRolesOverride(), dms.getLtiRolesOverride());

		// Is this a save/update operation?
		// Does the unique contraint work at all?
		Membership nms = new Membership();
		nms.setSubject(subject);
		nms.setContext(context);
		nms.setLtiRoles(LTI13ConstantsUtil.ROLE_LEARNER);
		nms = membershipRepository.upsert(nms);
		assertNotNull(nms.getId());
		assertEquals(ms.getId(), nms.getId());

		Membership lms = membershipRepository.findBySubjectAndContext(subject, context);
		assertEquals(lms.getId(), nms.getId());
		assertEquals(lms.getLtiRoles(), nms.getLtiRoles());
		assertEquals(lms.getLtiRolesOverride(), nms.getLtiRolesOverride());

		int minutes = 5;
		List expired = membershipRepository.getEntriesMinutesOld(context, minutes);
		assertEquals(0,expired.size());

		newContext = contextRepository.findByContextAndTenant("SI364", tenant);

		LineItem lineItem = new LineItem();
		lineItem.setId(42l);
		lineItem.setResourceId("YADA");
		lineItem.setContext(context);
		lineItem.setUpdatedAt(Instant.now());
		lineItem.setSentAt(Instant.now());
		lineItem.setStatus("Test Status");
		lineItem.setDebugLog("Debug goes here");
		lineItem.setSuccess(Boolean.TRUE);
		lineItemRepository.save(lineItem);

		Link link = new Link();
		link.setLink("YADA");
		link.setContext(context);
		linkRepository.save(link);

		Score score = new Score();
		// Set the logical keys
		score.setGradeBookColumnId(42l);
		score.setSubject(subject);
		score.setComment("Yada");

		score.setActivityProgress(org.tsugi.ags2.objects.Score.ACTIVITY_INITIALIZED);
		score.setActivityProgress(org.tsugi.ags2.objects.Score.ACTIVITY_STARTED);
		score.setActivityProgress(org.tsugi.ags2.objects.Score.ACTIVITY_INPROGRESS);
		score.setActivityProgress(org.tsugi.ags2.objects.Score.ACTIVITY_SUBMITTED);
		score.setActivityProgress(org.tsugi.ags2.objects.Score.ACTIVITY_COMPLETED);
		Enum<Score.ACTIVITY_PROGRESS> ap = score.getActivityProgress();
		assertEquals(ap, Score.ACTIVITY_PROGRESS.Completed);
		assertEquals(ap.name(), org.tsugi.ags2.objects.Score.ACTIVITY_COMPLETED);
		try {
			score.setActivityProgress("Yada");
			fail("score.setActivityProgress(\"Yada\"); should fail with a RunTime exception");
		} catch (Exception e) { /* no Problem */ }


		score.setGradingProgress(org.tsugi.ags2.objects.Score.GRADING_FULLYGRADED);
		score.setGradingProgress(org.tsugi.ags2.objects.Score.GRADING_PENDING);
		score.setGradingProgress(org.tsugi.ags2.objects.Score.GRADING_PENDINGMANUAL);
		score.setGradingProgress(org.tsugi.ags2.objects.Score.GRADING_FAILED);
		Enum<Score.GRADING_PROGRESS> gp = score.getGradingProgress();
		assertEquals(gp, Score.GRADING_PROGRESS.Failed);
		assertEquals(gp.name(), org.tsugi.ags2.objects.Score.GRADING_FAILED);
		try {
			score.setGradingProgress("Yada");
			fail("score.setGradingProgress(\"Yada\"); should fail with a RunTime exception");
		} catch (Exception e) { /* no Problem */ }

		String scoreGuid = score.getId();
		assertNull(scoreGuid);

		scoreRepository.save(score);
		assertEquals(score.getComment(), "Yada");

		// See if JPA can do INSERT ON DUPLICATE KEY UPDATE?  NO.
		// https://stackoverflow.com/questions/48568921/how-to-do-on-duplicate-key-update-in-spring-data-jpa
		// https://stackoverflow.com/questions/913341/can-hibernate-work-with-mysqls-on-duplicate-key-update-syntax
		// https://stackoverflow.com/questions/69373529/spring-data-jpa-on-duplicate-key-update-amount-account-amount-somevalue
		scoreGuid = score.getId();
		assertNotNull(scoreGuid);

		// Change one thing and save
		score.setGradingProgress(org.tsugi.ags2.objects.Score.GRADING_PENDINGMANUAL);
		scoreRepository.save(score);
		String newGuid = score.getId();
		assertEquals(scoreGuid, newGuid);
		gp = score.getGradingProgress();
		assertEquals(gp, Score.GRADING_PROGRESS.PendingManual);

		// Load up the score and check
		Long gradeBookColumn = 42l;
		Score loadScore = scoreRepository.findBySubjectAndColumn(subject, gradeBookColumn);
		assertNotNull(loadScore);
		assertEquals(score.getId(), loadScore.getId());
		assertEquals(score.getGradingProgress(), loadScore.getGradingProgress());
		assertEquals(score.getComment(), loadScore.getComment());

		// Load something that is not there
		gradeBookColumn = 43l;
		loadScore = scoreRepository.findBySubjectAndColumn(subject, gradeBookColumn);
		assertNull(loadScore);

		// Make a fresh Score with duplicate logical keys and re-save
		gradeBookColumn = 42l;
		score = scoreRepository.findBySubjectAndColumn(subject, gradeBookColumn);
		assertNotNull(score);
		score.setActivityProgress(org.tsugi.ags2.objects.Score.ACTIVITY_INITIALIZED);

		newGuid = score.getId();
		assertNotNull(newGuid);
		assertEquals(scoreGuid, newGuid);

		scoreRepository.save(score);
		newGuid = score.getId();
		assertNotNull(newGuid);
		assertEquals(scoreGuid, newGuid);
		gp = score.getGradingProgress();
		assertEquals(gp, Score.GRADING_PROGRESS.PendingManual);
		assertEquals(score.getComment(), "Yada");

		ap = score.getActivityProgress();
		assertEquals(ap, Score.ACTIVITY_PROGRESS.Initialized);

		// Lets delete the score record
		Integer count = scoreRepository.deleteBySubjectAndColumn(subject, gradeBookColumn);
		assertEquals(count, new Integer(1));

		score = scoreRepository.findBySubjectAndColumn(subject, gradeBookColumn);
		assertNull(score);

		// Lets do a delete of non-existant score
		gradeBookColumn = 1000l;
		count = scoreRepository.deleteBySubjectAndColumn(subject, gradeBookColumn);
		assertEquals(count, new Integer(0));

		// Test ContextLog
		ContextLog cLog = new ContextLog();
		cLog.setContext(context);
		cLog.setType(ContextLog.LOG_TYPE.NRPS_TOKEN);
		cLog.setAction("cool action 1");
		cLog.setSuccess(Boolean.FALSE);
		cLog.setDebugLog("I am debug string");
		contextLogRepository.save(cLog);

		List<ContextLog> logList = contextLogRepository.getLogEntries(context, Boolean.FALSE, 5);
		assertEquals(logList.size(), 1);
		logList = contextLogRepository.getLogEntries(context, Boolean.TRUE, 5);
		assertEquals(logList.size(), 0);
		logList = contextLogRepository.getLogEntries(context, null, 5);
		assertEquals(logList.size(), 1);

		ContextLog cl0 = logList.get(0);
		assertNotNull(cl0.getCreatedAt());
		assertEquals(cl0.getType(), ContextLog.LOG_TYPE.NRPS_TOKEN);
		assertEquals(cl0.getAction(), "cool action 1");

		Instant reallyOld = Instant.ofEpochSecond(1234567);

		cLog = new ContextLog();
		cLog.setContext(context);
		cLog.setType(ContextLog.LOG_TYPE.NRPS_TOKEN);
		cLog.setAction("old action 1");
		cLog.setSuccess(Boolean.FALSE);
		cLog.setDebugLog("I am debug string");
		cLog.setCreatedAt(reallyOld);
		contextLogRepository.save(cLog);

		logList = contextLogRepository.getLogEntries(context, null, 5);
		assertEquals(logList.size(), 2);

		cl0 = logList.get(0);
		assertNotNull(cl0.getCreatedAt());
		assertEquals(cl0.getType(), ContextLog.LOG_TYPE.NRPS_TOKEN);
		assertEquals(cl0.getAction(), "cool action 1");

		ContextLog cl1 = logList.get(1);
		assertEquals(cl1.getCreatedAt(), reallyOld);
		assertEquals(cl1.getType(), ContextLog.LOG_TYPE.NRPS_TOKEN);
		assertEquals(cl1.getAction(), "old action 1");

		logList = contextLogRepository.getLogEntries(context, Boolean.TRUE, 5);
		assertEquals(logList.size(), 0);

		// Do some deleting
		int howMany = contextLogRepository.deleteOlderThanDays(5);
		assertEquals(howMany, 1);
		howMany = contextLogRepository.deleteOlderThanDays(5);
		assertEquals(howMany, 0);
	}

	@Test
	public void testReceiveJWT()
		throws com.fasterxml.jackson.core.JsonProcessingException, org.sakaiproject.lti.api.LTIException
	{
		String id_token = getIdToken();
		JSONObject header = LTI13JwtUtil.jsonJwtHeader(id_token);
		assertNotNull(header);
		String kid = (String) header.get("kid");
		assertNotNull(kid);
		JSONObject body = LTI13JwtUtil.jsonJwtBody(id_token);
		assertNotNull(body);
		String rawbody = LTI13JwtUtil.rawJwtBody(id_token);
		assertNotNull(rawbody);

		// https://www.baeldung.com/jackson-deserialize-json-unknown-properties
		ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SakaiLaunchJWT launchJWT = mapper.readValue(rawbody, SakaiLaunchJWT.class);
		assertNotNull(launchJWT);

		// Make sure funky stuff is ignored
		String funkybody = rawbody.replace("{\"iss\":", "{\"funky\":\"town\",\"iss\":");
		launchJWT = mapper.readValue(funkybody, SakaiLaunchJWT.class);
		assertNotNull(launchJWT);

		Instant now = Instant.now();
		Tenant tenant = null;
		Launch launch = null;
		try {
			launch = plusService.updateAll(launchJWT, tenant);
			fail("SakaiLaunchJWT and tenant both null should throw a runtime exception");
		} catch (Exception e) { /* no Problem */ }

		tenant = new Tenant();
		try {
			launch = plusService.updateAll(launchJWT, tenant);
			fail("Tenant without issuer should throw a runtime exception");
		} catch (Exception e) { /* no Problem */ }

		tenant.setTitle("Example Tenant");
		tenant.setCreatedAt(now);
		tenant.setOidcAuth("https://www.example.com/auth");
		tenant.setOidcKeySet("https://www.example.com/keyset");
		tenant.setOidcToken("https://www.example.com/token");
		tenant.setDeploymentId("1");

		tenant.setIssuer("wrong");
		tenant.setClientId("wrong");
		try {
			launch = plusService.updateAll(launchJWT, tenant);
			fail("Tenant / SakaiLaunchJWT issuer mismatch should throw a runtime exception");
		} catch (Exception e) { /* no Problem */ }

		tenant.setIssuer(launchJWT.issuer);
		tenant.setClientId(launchJWT.audience);
		tenant.setDeploymentId(launchJWT.deployment_id);
		try {
			launch = plusService.updateAll(launchJWT, tenant);
			fail("Valid tenant that is not persisted should throw a runtime exception");
		} catch (Exception e) { /* no Problem */ }

		tenantRepository.save(tenant);
		String tenantId = tenant.getId();
		assertNotNull(tenantId);

		launch = plusService.updateAll(launchJWT, tenant);
		assertNotNull(launch);
		assertEquals(launch.getSubject().getDisplayName(), "Chuck P");
		assertEquals(launch.getContext().getTitle(), "Yada");

		launch = plusService.updateAll(launchJWT, tenant);
		assertNotNull(launch);
		assertEquals(launch.getSubject().getDisplayName(), "Chuck P");
		assertEquals(launch.getContext().getTitle(), "Yada");

		launchJWT.name = "Sakaiger";
		launchJWT.context.title = "SI664";
		launch = plusService.updateAll(launchJWT, tenant);
		assertNotNull(launch);
		assertEquals(launch.getSubject().getDisplayName(), "Sakaiger");
		assertEquals(launch.getContext().getTitle(), "SI664");

		// Construct from first and last
		launchJWT.name = null;
		launch = plusService.updateAll(launchJWT, tenant);
		assertNotNull(launch);
		assertEquals(launch.getSubject().getDisplayName(), "Chuck P");
		assertEquals(launch.getContext().getTitle(), "SI664");

		// Lets load from database
		Context loadContext = contextRepository.findByContextAndTenant(launch.getContext().getContext(), tenant);
		assertEquals(launch.getContext().getTitle(), loadContext.getTitle());
		assertEquals(launch.getContext().getId(), loadContext.getId());

	}

	public String getIdToken()
	{
		return "eyJraWQiOiIxNzkzNTI2OTg4IiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJhdWQiOiJhMmUwZjU4Yi0xZWFkLTQ3MjAtYWY3MC05OTExOGQwNmI2OTMiLCJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvdXNlci80MmNjNTAxYi03ZWFiLTRhYmItOTQwZC1iMGUwZWRkZjUzMmIiLCJub25jZSI6IjYxY2RiMjU3YmNkNTgiLCJpYXQiOjE2NDA4NzA0ODcsImV4cCI6MTY0MDg3NDA4NywiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vZGVwbG95bWVudF9pZCI6IjEiLCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS90YXJnZXRfbGlua191cmkiOiJodHRwOi8vbG9jYWxob3N0Ojg4ODgvcHk0ZS9tb2QvbG1zdGVzdC8iLCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS9tZXNzYWdlX3R5cGUiOiJMdGlSZXNvdXJjZUxpbmtSZXF1ZXN0IiwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vdmVyc2lvbiI6IjEuMy4wIiwiZ2l2ZW5fbmFtZSI6IkNodWNrIiwiZmFtaWx5X25hbWUiOiJQIiwiZW1haWwiOiJwQHAuY29tIiwibmFtZSI6IkNodWNrIFAiLCJsb2NhbGUiOiJlbl9VUyIsImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpL2NsYWltL2N1c3RvbSI6eyJhdmFpbGFibGVlbmQiOiIkUmVzb3VyY2VMaW5rLmF2YWlsYWJsZS5lbmREYXRlVGltZSIsImF2YWlsYWJsZXN0YXJ0IjoiJFJlc291cmNlTGluay5hdmFpbGFibGUuc3RhcnREYXRlVGltZSIsImNhbnZhc19jYWxpcGVyX3VybCI6IiRDYWxpcGVyLnVybCIsImNvbnRleHRfaWRfaGlzdG9yeSI6IiRDb250ZXh0LmlkLmhpc3RvcnkiLCJyZXNvdXJjZWxpbmtfaWRfaGlzdG9yeSI6IiRSZXNvdXJjZUxpbmsuaWQuaGlzdG9yeSIsInN1Ym1pc3Npb25lbmQiOiIkUmVzb3VyY2VMaW5rLnN1Ym1pc3Npb24uZW5kRGF0ZVRpbWUiLCJzdWJtaXNzaW9uc3RhcnQiOiIkUmVzb3VyY2VMaW5rLnN1Ym1pc3Npb24uc3RhcnREYXRlVGltZSJ9LCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS9yb2xlcyI6WyJodHRwOi8vcHVybC5pbXNnbG9iYWwub3JnL3ZvY2FiL2xpcy92Mi9tZW1iZXJzaGlwI0luc3RydWN0b3IiXSwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vcm9sZV9zY29wZV9tZW50b3IiOltdLCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS9sYXVuY2hfcHJlc2VudGF0aW9uIjp7ImRvY3VtZW50X3RhcmdldCI6ImlmcmFtZSIsInJldHVybl91cmwiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvaW1zb2lkYy9sdGkxMS9yZXR1cm4tdXJsL3NpdGUvNjJjOWQ2NWItODk1OS00M2QyLWI2NjQtNWNmYmZjMjczMzgwIiwiY3NzX3VybCI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9saWJyYXJ5L3NraW4vdG9vbF9iYXNlLmNzcyJ9LCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS9yZXNvdXJjZV9saW5rIjp7ImlkIjoiY29udGVudDoxMCIsInRpdGxlIjoiTE1TIFRlc3QiLCJkZXNjcmlwdGlvbiI6IlRoaXMgdG9vbCBleGVyY2lzZXMgdmFyaW91cyBMTVMgQWN0aXZpdGllcy4ifSwiaHR0cHM6Ly9wdXJsLmltc2dsb2JhbC5vcmcvc3BlYy9sdGkvY2xhaW0vY29udGV4dCI6eyJpZCI6IjYyYzlkNjViLTg5NTktNDNkMi1iNjY0LTVjZmJmYzI3MzM4MCIsImxhYmVsIjoiWWFkYSIsInRpdGxlIjoiWWFkYSIsInR5cGUiOlsiaHR0cDovL3B1cmwuaW1zZ2xvYmFsLm9yZy92b2NhYi9saXMvdjIvY291cnNlI0NvdXJzZU9mZmVyaW5nIl19LCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS9jbGFpbS90b29sX3BsYXRmb3JtIjp7Im5hbWUiOiJTYWthaSIsImRlc2NyaXB0aW9uIjoibG9jYWxob3N0LnNha2FpbG1zIiwicHJvZHVjdF9mYW1pbHlfY29kZSI6InNha2FpIiwidmVyc2lvbiI6IjIzLVNOQVBTSE9UIn0sImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpL2NsYWltL2xpcyI6eyJwZXJzb25fc291cmNlZGlkIjoiY3NldiIsImNvdXJzZV9vZmZlcmluZ19zb3VyY2VkaWQiOiI2MmM5ZDY1Yi04OTU5LTQzZDItYjY2NC01Y2ZiZmMyNzMzODAiLCJjb3Vyc2Vfc2VjdGlvbl9zb3VyY2VkaWQiOiI2MmM5ZDY1Yi04OTU5LTQzZDItYjY2NC01Y2ZiZmMyNzMzODAiLCJ2ZXJzaW9uIjpbIjEuMC4wIiwiMS4xLjAiXX0sImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpLWFncy9jbGFpbS9lbmRwb2ludCI6eyJzY29wZSI6WyJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS1hZ3Mvc2NvcGUvbGluZWl0ZW0iXSwibGluZWl0ZW1zIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2ltc2JsaXMvbHRpMTMvbGluZWl0ZW1zLzhmZGQyNjcyYzk1MTM1Y2QwNjRkOGFjMjgwNzI3NWE0Njc0MDY4NmY4YjQ5MmYxNzk5NWVhMDAyYzA1YzM4YzA6Ojo2MmM5ZDY1Yi04OTU5LTQzZDItYjY2NC01Y2ZiZmMyNzMzODA6Ojpjb250ZW50OjEwIiwibGluZWl0ZW0iOiJodHRwOi8vbG9jYWxob3N0OjgwODAvaW1zYmxpcy9sdGkxMy9saW5laXRlbS84ZmRkMjY3MmM5NTEzNWNkMDY0ZDhhYzI4MDcyNzVhNDY3NDA2ODZmOGI0OTJmMTc5OTVlYTAwMmMwNWMzOGMwOjo6NjJjOWQ2NWItODk1OS00M2QyLWI2NjQtNWNmYmZjMjczMzgwOjo6Y29udGVudDoxMCJ9LCJodHRwczovL3B1cmwuaW1zZ2xvYmFsLm9yZy9zcGVjL2x0aS1ucnBzL2NsYWltL25hbWVzcm9sZXNlcnZpY2UiOnsiY29udGV4dF9tZW1iZXJzaGlwc191cmwiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvaW1zYmxpcy9sdGkxMy9uYW1lc2FuZHJvbGVzLzhmZGQyNjcyYzk1MTM1Y2QwNjRkOGFjMjgwNzI3NWE0Njc0MDY4NmY4YjQ5MmYxNzk5NWVhMDAyYzA1YzM4YzA6Ojo2MmM5ZDY1Yi04OTU5LTQzZDItYjY2NC01Y2ZiZmMyNzMzODA6Ojpjb250ZW50OjEwIiwic2VydmljZV92ZXJzaW9ucyI6WyIyLjAiXX0sImh0dHBzOi8vcHVybC5pbXNnbG9iYWwub3JnL3NwZWMvbHRpL2NsYWltL2x0aTFwMSI6eyJ1c2VyX2lkIjoiNDJjYzUwMWItN2VhYi00YWJiLTk0MGQtYjBlMGVkZGY1MzJiIiwib2F1dGhfY29uc3VtZXJfa2V5IjoiNTQzMjEiLCJvYXV0aF9jb25zdW1lcl9rZXlfc2lnbiI6IndtaVM2MWVnck5hQ3hPNHFHUTg1aXA0T3ZRb3pNVDBKcm12a0hDeUxrczA9In0sImh0dHBzOi8vd3d3LnNha2FpbG1zLm9yZy9zcGVjL2x0aS9jbGFpbS9leHRlbnNpb24iOnsic2FrYWlfbGF1bmNoX3ByZXNlbnRhdGlvbl9jc3NfdXJsX2xpc3QiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvbGlicmFyeS9za2luL3Rvb2xfYmFzZS5jc3MsaHR0cDovL2xvY2FsaG9zdDo4MDgwL2xpYnJhcnkvc2tpbi9tb3JwaGV1cy1kZWZhdWx0L3Rvb2wuY3NzP3ZlcnNpb249ZTIzMjhhNmMiLCJzYWthaV9hY2FkZW1pY19zZXNzaW9uIjoiT1RIRVIiLCJzYWthaV9yb2xlIjoibWFpbnRhaW4iLCJzYWthaV9zZXJ2ZXIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzYWthaV9zZXJ2ZXJpZCI6Ik1hY0Jvb2stUHJvLTEwNS5sb2NhbCJ9fQ.S5IHPoUKbsOnjhmYOUcJCCfnNsdJGfNeqMuWnDRTApb7b44m-K5PZe5L2AgQkuRHebeu8bqGDZQpWGcEeoXJuBwGUsrQkWR6a95NSGUCiijWe5PsfJ1sGtuS7MqS5QLU-yaW9_x2fChR04LznyVMhsAPf3LLsvyaZm_36S-SpvtJkIyTLUKP0GITRNjmuk325701QUq4iFg2_n8SGR7T8azQxC5TrrkuXVz2kCz_91UG0mOASrq0bJAb6YbWxeF_hK27wKlDGnCkGe3_icXi_GJ7Vh_vQIBwJqAsrzUESJcka73dWL5ZFkfCRdH_pal-nI1jvVNAw3ceUIcas8AyOg";
	}

}
