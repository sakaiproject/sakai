/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.cheftool;

import java.util.Observable;
import java.util.Observer;

/**
 * <p>
 * ControllerState is the core base class for the CHEF Tool's state objects.
 * </p>
 * <p>
 * State objects are used to store controller state for a tool. Specific state object implement this interface.
 * </p>
 * <p>
 * To support creation of controller state objects, make sure to supply a void constructor.
 * </p>
 * <p>
 * To support pooling of objects, implement the recycle() method to release any resources and restore the object to initial conditions before reuse.
 * </p>
 */
public abstract class ControllerState implements Observer
{
	/**
	 * Init to startup values
	 */
	protected void init()
	{
	} // init

	/**
	 * Release any resources and restore the object to initial conditions to be reused.
	 */
	public void recycle()
	{
		m_id = "";
		m_setId = "";

		init();

	} // recycle

	/** This state's unique id (unique within the set) */
	private String m_id = "";

	public String getId()
	{
		return m_id;
	}

	public void setId(String id)
	{
		m_id = id;
	}

	/** This state's set unique id */
	private String m_setId = "";

	public String getSetId()
	{
		return m_setId;
	}

	public void setSetId(String id)
	{
		m_setId = id;
	}

	/**
	 * Access a unique key for this state, combining the set id and the state id.
	 * 
	 * @return A unique key for this state, combining the set id and the state id.
	 */
	public String getKey()
	{
		return m_setId + m_id;

	} // getKey

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Observer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * This method is called whenever the observed object is changed. An application calls an <tt>Observable</tt> object's <code>notifyObservers</code> method to have all the object's observers notified of the change. default implementation is to
	 * cause the courier service to deliver to the interface controlled by my controller. Extensions can override.
	 * 
	 * @param o
	 *        the observable object.
	 * @param arg
	 *        an argument passed to the <code>notifyObservers</code> method.
	 */
	public void update(Observable o, Object arg)
	{
		// Logger.debug("chef", this + ".update: " + arg.toString());
		//
		// CourierService.deliver(getSetId(), getId());

	} // update

} // ControllerState

