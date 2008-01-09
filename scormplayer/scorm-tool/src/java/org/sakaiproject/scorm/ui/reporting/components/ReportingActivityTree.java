package org.sakaiproject.scorm.ui.reporting.components;

import javax.servlet.http.HttpServletRequest;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ISeqActivity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.ui.UISynchronizer;
import org.sakaiproject.scorm.ui.player.components.ActivityTree;
import org.sakaiproject.scorm.ui.reporting.pages.AttemptGraphPage;

public class ReportingActivityTree extends ActivityTree {
	
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ReportingActivityTree.class);
	
	public ReportingActivityTree(String id, SessionBean sessionBean,
			UISynchronizer synchronizer) {
		super(id, sessionBean, synchronizer);
	}
	
	@Override
	protected void onNodeLinkClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target)
	{		
		log.debug("onNodeLinkClicked");

		ISeqActivity activity = (ISeqActivity)((DefaultMutableTreeNode)node).getUserObject();
		
		if (log.isDebugEnabled())
			log.debug("ID: " + activity.getID());

		String url = getAttemptGraphUrl(sessionBean, activity.getID());
		
		if (log.isDebugEnabled())
			log.debug("Going to " + url);
		
		target.appendJavascript("window.graphScreen.location.href='" + url + "'");
		
		selectNode(activity.getID());
		updateTree(target);
		
		/*sequencingService.navigateToActivity(activity.getID(), sessionBean, new ReportingResourceNavigator(), target);
		
		synchronizer.synchronizeState(sessionBean, target);
		
		// FIXME: Turning this off to see if its necessary -- probably needs some more intricate solution
		// FIXME: Turning back on for the moment.
		ReportingActivityTree.this.bindModel(sessionBean);
		*/
	}
	
	private String getAttemptGraphUrl(SessionBean sessionBean, String activityId) {
		RequestCycle cycle = getRequestCycle();
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		WebRequest webRequest = (WebRequest)getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();
		String toolUrl = servletRequest.getContextPath();
		
		Class<?> pageClass = AttemptGraphPage.class;
	
		PageParameters params = new PageParameters();
		
		params.put("contentPackageId", sessionBean.getContentPackageId());
		params.put("learnerId", sessionBean.getLearnerId());
		params.put("activityId", activityId);
		params.put("attemptNumber", sessionBean.getAttemptNumber());
		
		CharSequence completionUrl = encoder.encode(cycle, new BookmarkablePageRequestTarget(pageClass, params));
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/").append(completionUrl);
		
		return url.toString();
	}
}
