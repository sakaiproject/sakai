package org.sakaiproject.scorm.ui.player.behaviors;

import org.adl.api.ecmascript.APIErrorManager;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.sequencer.SeqNavRequests;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;

public abstract class AbstractSCORM13APBase extends
		AbstractAnnotationAwareTransactionalTests {
	protected SCORM13API scorm13api;

	protected ScormApplicationService scormApplicationService;

	protected ScormSequencingService scormSequencingService;

	protected SessionBean sessionBean;

	protected ScoBean scoBean;

	protected ScormContentService scormContentService;

	protected ScormResourceService scormResourceService;

	protected String resourceId;

	protected ContentPackage contentPackage;

	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"hibernate-test.xml",
				// "sakai-test.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-hibernate-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-scorm-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-adl-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-standalone-*.xml",
				"classpath*:org/sakaiproject/scorm/**/spring-mock-*.xml", };
	}

	@Override
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		scormApplicationService = (ScormApplicationService) getApplicationContext()
				.getBean(ScormApplicationService.class.getName());
		scormSequencingService = (ScormSequencingService) getApplicationContext()
				.getBean(ScormSequencingService.class.getName());
		scormContentService = (ScormContentService) getApplicationContext()
				.getBean(ScormContentService.class.getName());
		scormResourceService = (ScormResourceService) getApplicationContext()
				.getBean(ScormResourceService.class.getName());

		resourceId = scormResourceService.putArchive(getClass()
				.getResourceAsStream("SCORM2004.3.PITE.1.1.zip"),
				"SCORM2004.3.PITE.1.1.zip", "application/zip", true, 0);

		scormContentService.validate(resourceId, false, false, "UTF-8");
		contentPackage = scormContentService.getContentPackages().get(0);
		sessionBean = new SessionBean();
		sessionBean.setContentPackage(contentPackage);
		sessionBean.setErrorManager(new APIErrorManager(
				IErrorManager.SCORM_2004_API));
		// init the launch data.
		scormSequencingService.navigate(SeqNavRequests.NAV_START, sessionBean,
				null, new Object());
		scoBean = scormApplicationService.produceScoBean("undefined",
				sessionBean);
		scorm13api = new SCORM13API() {

			@Override
			public Object getTarget() {
				return null;
			}

			@Override
			public SessionBean getSessionBean() {
				return sessionBean;
			}

			@Override
			public ScormSequencingService getSequencingService() {
				return scormSequencingService;
			}

			@Override
			public ScoBean getScoBean() {
				return scoBean;
			}

			@Override
			public ScormApplicationService getApplicationService() {
				return scormApplicationService;
			}

			@Override
			public INavigable getAgent() {
				return null;
			}
		};
	}
}
