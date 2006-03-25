/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

// package
package org.sakaiproject.cheftool;

import org.sakaiproject.service.framework.session.SessionState;

// imports

/**
* <p>VelocityPortletStateAction is an extension of VelocityPortletAction which provides a way
* to associate Controller state with each instances of the portlet using this action.</p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version 1.0
* @see org.chefproject.service.ControllerStateService
* @see org.chefproject.core.ControllerState
*/

public abstract class VelocityPortletStateAction
	extends VelocityPortletPaneledAction
{
	/**
	* Get the proper state for this instance (if portlet is not known, only context).
	* @param context The Template Context (it contains a reference to the portlet).
	* @param rundata The Jetspeed (Turbine) rundata associated with the request.
	* @param stateClass The Class of the ControllerState to find / create.
	* @return The proper state object for this instance.
	*/
	protected ControllerState getState(Context context, RunData rundata, Class stateClass)
	{
		return getState(((JetspeedRunData)rundata).getJs_peid(), rundata, stateClass);

	}   // getState

	/**
	* Get the proper state for this instance (if portlet is known).
	* @param portlet The portlet being rendered.
	* @param rundata The Jetspeed (Turbine) rundata associated with the request.
	* @param stateClass The Class of the ControllerState to find / create.
	* @return The proper state object for this instance.
	*/
	protected ControllerState getState(VelocityPortlet portlet, RunData rundata, Class stateClass)
	{
		if (portlet == null)
		{
			Log.warn("chef", this + ".getState(): portlet null");
			return null;
		}

		return getState(portlet.getID(), rundata, stateClass);

	}   // getState

	/**
	* Get the proper state for this instance (if portlet id is known).
	* @param peid The portlet id.
	* @param rundata The Jetspeed (Turbine) rundata associated with the request.
	* @param stateClass The Class of the ControllerState to find / create.
	* @return The proper state object for this instance.
	*/
	protected ControllerState getState(String peid, RunData rundata, Class stateClass)
	{
		if (peid == null)
		{
			Log.warn("chef", this + ".getState(): peid null");
			return null;
		}

		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData)rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");
			
			if (state != null) return state;

			// if there's no "state" object in there, make one
			state = (ControllerState)stateClass.newInstance();
			state.setId(peid);
// TODO: this does not seem used -ggolden
//			state.setSetId(((JetspeedRunData)rundata).getPageSessionId());
			
			// remember it!
			ss.setAttribute("state", state);

			return state;
		}
		catch (Exception e)
		{
			Log.warn("chef", "", e);
		}

		return null;

	}   // getState

	/**
	* Release the proper state for this instance (if portlet is not known, only context).
	* @param context The Template Context (it contains a reference to the portlet).
	* @param rundata The Jetspeed (Turbine) rundata associated with the request.
	*/
	protected void releaseState(Context context, RunData rundata)
	{
		releaseState(((JetspeedRunData)rundata).getJs_peid(), rundata);

	}   // releaseState

	/**
	* Release the proper state for this instance (if portlet is known).
	* @param portlet The portlet being rendered.
	* @param rundata The Jetspeed (Turbine) rundata associated with the request.
	*/
	protected void releaseState(VelocityPortlet portlet, RunData rundata)
	{
		releaseState(portlet.getID(), rundata);

	}   // releaseState

	/**
	* Release the proper state for this instance (if portlet id is known).
	* @param peid The portlet id being rendered.
	* @param rundata The Jetspeed (Turbine) rundata associated with the request.
	*/
	protected void releaseState(String peid, RunData rundata)
	{
		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData)rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");
			
			// recycle the state object
			state.recycle();
			
			// clear out the SessionState for this Portlet
			ss.removeAttribute("state");
			
            ss.clear();

		}
		catch (Exception e)
		{
			Log.warn("chef", "", e);
		}

	}   // releaseState

}   // VelocityPortletStateAction



