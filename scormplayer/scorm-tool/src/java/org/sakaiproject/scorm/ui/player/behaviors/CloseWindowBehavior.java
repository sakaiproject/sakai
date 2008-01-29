package org.sakaiproject.scorm.ui.player.behaviors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.sakaiproject.scorm.model.api.SessionBean;

public class CloseWindowBehavior extends ActivityAjaxEventBehavior {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(CloseWindowBehavior.class);
	
	private SessionBean sessionBean;
	
	public CloseWindowBehavior(SessionBean sessionBean, boolean isRelativeUrl) {
		super("closeWindow", isRelativeUrl);
		this.sessionBean = sessionBean;
	}
	
	@Override
	protected void onEvent(AjaxRequestTarget target) {
		log.debug("closeWindowBehavior onEvent()");
		if (sessionBean != null && sessionBean.isStarted() && !sessionBean.isEnded()) {
			log.debug("----> Going to exit on next terminate request");
			sessionBean.setCloseOnNextTerminate(true);
		}
	}
	
}
