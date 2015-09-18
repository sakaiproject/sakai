/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package org.sakaiproject.rsf.helper;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;

import uk.org.ponder.arrayutil.ArrayUtil;
import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.beanutil.BeanModelAlterer;
import uk.org.ponder.mapping.ShellInfo;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.ParameterList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIParameter;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterpreter;
import uk.org.ponder.rsf.flow.support.ARI2Processor;
import uk.org.ponder.rsf.preservation.StatePreservationManager;
import uk.org.ponder.rsf.state.TokenStateHolder;
import uk.org.ponder.rsf.view.View;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.view.ViewResolver;
import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.rsf.viewstate.ViewParamUtil;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;
import uk.org.ponder.stringutil.URLUtil;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * 
 * @author Andrew Thornton
 */
public class HelperHandlerHookBean {

	private static final String TOKEN_STATE_PREFIX = "HelperHandlerHook";
	private static final String POST_HELPER_BINDING_PARAMLIST = HelperViewParameters.POST_HELPER_BINDING + "PARAMLIST";
	private static final String POST_HELPER_ARI2_VIEWPARAMS = HelperViewParameters.POST_HELPER_BINDING
			+ "ARI2RETPARAMS";
	private static final String HELPER_FINISHED_PATH = "/done";
	private static final String IN_HELPER_PATH = "/tool";

	/*
	 * This is currently being used to record the fact that we are in the
	 * Helper, for the sole sake of being able to decide whether or not to
	 * disable the MultipartResolver. It's horrible and needs to be replaced
	 * with something more robust. Actually, even the current setup has
	 * problems. If you go to another part of the tool with a URL, and go back
	 * to somewhere that launches the helper, the information is obviously still
	 * there. Actually, we could just camp out in the HelperHandlerHook and
	 * remove this anytime we return false from handled.
	 */
	public static final String IN_HELPER_INDICATOR = TOKEN_STATE_PREFIX + "in-helper"; // Hack

	private HttpServletResponse response;
	private HttpServletRequest request;
	private ViewParameters viewParameters;
	private ViewResolver viewResolver;
	private StatePreservationManager statePreservationManager;
	private TokenStateHolder tsh;
	private ViewStateHandler vsh;
	private BeanModelAlterer bma;
	private BeanLocator beanLocator;
	private ActionResultInterpreter ari;
	private ARI2Processor ariprocessor;
	private ActiveToolManager activeToolManager;
	private BaseURLProvider bup;
	private String[] pathInfo;

	public boolean handle() {
		String viewID = viewParameters.viewID;
		Logger.log.info("Handling view: " + viewID);

		String pathBeyondViewID = "";
		if (pathInfo.length > 1) {
			pathBeyondViewID = URLUtil.toPathInfo((String[]) ArrayUtil.subArray(pathInfo, 1, pathInfo.length));
		}
		if (Logger.log.isInfoEnabled()) {
			Logger.log.info("pathInfo: " + pathInfo + " pathBeyondViewID: " + pathBeyondViewID);
		}
		if (pathBeyondViewID.startsWith(HELPER_FINISHED_PATH)) {
			return handleHelperDone();
		}

		if (pathBeyondViewID.startsWith(IN_HELPER_PATH)) {
			return handleHelperHelper(pathBeyondViewID);
		}

		return handleHelperStart();
	}

	private boolean handleHelperDone() {
		Logger.log.info("Done handling helper in view: " + viewParameters.viewID);

		// Removing hack
		tsh.clearTokenState(IN_HELPER_INDICATOR);

		ELReference elref = (ELReference) tsh
				.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.POST_HELPER_BINDING);
		ParameterList paramlist = (ParameterList) tsh
				.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + POST_HELPER_BINDING_PARAMLIST);
		statePreservationManager.scopeRestore();
		if (paramlist != null) {
			for (int i = 0; i < paramlist.size(); i++) {
				UIParameter param = (UIParameter) paramlist.get(i);
				if (param instanceof UIELBinding) {
					bma.setBeanValue(((UIELBinding) param).valuebinding.value, beanLocator,
							((UIELBinding) param).rvalue, null, false);
				}
			}
		}
		String methodBinding = elref == null ? null : elref.value;
		Object beanReturn = null;
		if (methodBinding != null) {
			ShellInfo shells = bma.fetchShells(methodBinding, beanLocator, true);
			beanReturn = bma.invokeBeanMethod(shells, null);
		}

		ViewParameters originParams = (ViewParameters) tsh
				.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + POST_HELPER_ARI2_VIEWPARAMS);

		ARIResult ariresult = ari.interpretActionResult(viewParameters, beanReturn);
		ariprocessor.interceptActionResult(ariresult, originParams, ariresult);
		String urlToRedirectTo = ViewParamUtil.getAnyFullURL(ariresult.resultingView, vsh);
		try {
			response.sendRedirect(urlToRedirectTo);
		} catch (IOException e) {
			throw UniversalRuntimeException.accumulate(e, "Error redirecting to url: " + urlToRedirectTo);
		}
		return true;
	}

	private boolean handleHelperHelper(final String pathBeyondViewID) {
		Logger.log.info("Handling helper in view: " + viewParameters.viewID + " pathBeyondViewID: " + pathBeyondViewID);

		String helperId = (String) tsh
				.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.HELPER_ID);

		if (pathBeyondViewID.endsWith(".helper")) {
			int i = pathBeyondViewID.lastIndexOf('/');
			if (i >= 0) {
				String helperName = pathBeyondViewID.substring(i + 1);
				String toolName = pathBeyondViewID.substring(0, i);
				i = helperName.lastIndexOf('.');
				helperName = helperName.substring(0, i);
				Logger.log.debug("new helper name" + helperName);
				Logger.log.debug("tool name" + toolName);
				tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + "orig-helper-id", helperId);
				tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + "helper-tool-name", toolName);
				helperId = helperName;
				tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.HELPER_ID,
						helperId);
			}
		}

		String origToolName = (String) tsh
				.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + "helper-tool-name");
		if (origToolName != null && pathBeyondViewID.endsWith(origToolName)) {
			helperId = (String) tsh.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + "orig-helper-id");
			Logger.log.debug("returning to " + helperId);
			tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.HELPER_ID, helperId);
			tsh.clearTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + "helper-tool-name");
			tsh.clearTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + "orig-helper-id");
		}

		ActiveTool helperTool = activeToolManager.getActiveTool(helperId);

		String baseUrl = bup.getBaseURL();
		int hostStart = baseUrl.indexOf("://") + 3;
		int hostEnd = baseUrl.indexOf('/', hostStart);

		String contextPath = baseUrl.substring(hostEnd);
		contextPath += viewParameters.viewID;
		contextPath += IN_HELPER_PATH;
		String helperPathInfo = pathInfo[0] + "/" + pathInfo[1];

		request.removeAttribute(Tool.NATIVE_URL);

		// this is the forward call
		try {
			helperTool.help(request, response, contextPath, helperPathInfo);
		} catch (ToolException e) {
			throw UniversalRuntimeException.accumulate(e, "ToolException when trying to call help. HelperId: "
					+ helperId + " contextPath: " + contextPath + " pathInfo: " + pathInfo);
		}

		return true;
	}

	private boolean handleHelperStart() {
		Logger.log.info("Handling helper start in view: " + viewParameters.viewID);
		View view = new View();
		List producersList = viewResolver.getProducers(viewParameters.viewID);
		if (producersList.size() != 1) {
			throw new IllegalArgumentException(
					"There is not exactly one view producer for the view: " + viewParameters.viewID);
		}
		ViewComponentProducer vp = (ViewComponentProducer) producersList.get(0);

		statePreservationManager.scopeRestore();
		vp.fillComponents(view.viewroot, viewParameters, null);
		statePreservationManager.scopePreserve();
		UIOutput helperId = (UIOutput) view.viewroot.getComponent(HelperViewParameters.HELPER_ID);
		UICommand helperBinding = (UICommand) view.viewroot.getComponent(HelperViewParameters.POST_HELPER_BINDING);
		tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.HELPER_ID,
				helperId.getValue());
		if (helperBinding != null) {
			tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.POST_HELPER_BINDING,
					helperBinding.methodbinding);
			// Support for a ParameterList on the UICommand
			if (helperBinding.parameters != null) {
				tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + POST_HELPER_BINDING_PARAMLIST,
						helperBinding.parameters);
			}
		}
		// We need to save these ViewParameters for ActionResultInterceptors
		tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + POST_HELPER_ARI2_VIEWPARAMS, viewParameters);

		// Hack to know if we're in the helper
		tsh.putTokenState(IN_HELPER_INDICATOR, IN_HELPER_INDICATOR);

		String helperToolPath = bup.getBaseURL() + viewParameters.viewID + IN_HELPER_PATH;
		tsh.putTokenState(helperId.getValue() + Tool.HELPER_DONE_URL,
				bup.getBaseURL() + viewParameters.viewID + HELPER_FINISHED_PATH);

		try {
			response.sendRedirect(helperToolPath);
		} catch (IOException e) {
			throw UniversalRuntimeException.accumulate(e, "IOException when trying to redirect to helper tool");
		}

		return true;
	}

	public void setActiveToolManager(ActiveToolManager activeToolManager) {
		this.activeToolManager = activeToolManager;
	}

	public void setActionResultInterpreter(ActionResultInterpreter ari) {
		this.ari = ari;
	}

	public void setBeanLocator(BeanLocator beanLocator) {
		this.beanLocator = beanLocator;
	}

	public void setBeanModelAlterer(BeanModelAlterer bma) {
		this.bma = bma;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.request = httpServletRequest;
	}

	public void setHttpServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setStatePreservationManager(StatePreservationManager statePreservationManager) {
		this.statePreservationManager = statePreservationManager;
	}

	public void setTokenStateHolder(TokenStateHolder tsh) {
		this.tsh = tsh;
	}

	public void setViewParameters(ViewParameters viewParameters) {
		this.viewParameters = viewParameters;
	}

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public void setViewStateHandler(ViewStateHandler vsh) {
		this.vsh = vsh;
	}

	public void setBaseURLProvider(BaseURLProvider bup) {
		this.bup = bup;
	}

	public void setPathInfo(String[] pathInfo) {
		this.pathInfo = pathInfo;
	}

	public void setAriProcessor(ARI2Processor ari2p) {
		this.ariprocessor = ari2p;
	}
}
