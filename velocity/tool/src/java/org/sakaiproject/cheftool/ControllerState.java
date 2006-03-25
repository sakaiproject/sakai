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

// imports
import java.util.Observable;
import java.util.Observer;

/**
* <p>ControllerState is the core base class for the CHEF Tool's state objects.</p>
* <p>State objects are used to store controller state for a tool.  Specific state object implement this interface.</p>
* <p>To support creation of controller state objects, make sure to supply a void constructor.</p>
* <p>To support pooling of objects, implement the recycle() method to release any resources and
* restore the object to initial conditions before reuse.</p>
* @author University of Michigan, CHEF Software Development Team
* @version 1.0
* @see org.chefproject.service.ControllerStateService
*/
public abstract class ControllerState
	implements Observer
{
	/**
	* Init to startup values
	*/
	protected void init()
	{
	}	// init

	/**
	* Release any resources and restore the object to initial conditions to be reused.
	*/
	public void recycle()
	{
		m_id = "";
		m_setId = "";

		init();

	}	// recycle

	/** This state's unique id (unique within the set) */
	private String m_id = "";
	public String getId() { return m_id; }
	public void setId(String id) { m_id = id; }

	/** This state's set unique id */
	private String m_setId = "";
	public String getSetId() { return m_setId; }
	public void setSetId(String id) { m_setId = id; }

	/**
	* Access a unique key for this state, combining the set id and the state id.
	* @return A unique key for this state, combining the set id and the state id.
	*/
	public String getKey()
	{
		return m_setId + m_id;

	}	// getKey

	/*******************************************************************************
	* Observer implementation
	*******************************************************************************/

	/**
	* This method is called whenever the observed object is changed. An
	* application calls an <tt>Observable</tt> object's
	* <code>notifyObservers</code> method to have all the object's
	* observers notified of the change.
	*
	* default implementation is to cause the courier service to deliver to the
	* interface controlled by my controller.  Extensions can override.
	*
	* @param   o     the observable object.
	* @param   arg   an argument passed to the <code>notifyObservers</code>
	*                 method.
	*/
	public void update(Observable o, Object arg)
	{
//		Log.debug("chef", this + ".update: " + arg.toString());
//
//		CourierService.deliver(getSetId(), getId());

	}	// update

}	// ControllerState



