/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.adl.validator.contentpackage;

import java.io.Serializable;

public interface ILaunchData extends Serializable {

	/** 
	 * Assigns the given value to the mOrganizationIdentifier attribute.
	 * 
	 * @param iOrganizationIdentifier The organization identitifier value to 
	 * be assigned.
	 */
	public void setOrganizationIdentifier(String iOrganizationIdentifier);

	/**
	 * Assigns the given value to the mItemIdentifier attribute.
	 *
	 * @param iItemIdentifier The item identifier value to be assigned.
	 */
	public void setItemIdentifier(String iItemIdentifier);

	/**
	 * Assigns the given value to the mResourceIdentifier attribute.
	 *
	 * @param iResourceIdentifier The resource identifier value to be assigned.
	 */
	public void setResourceIdentifier(String iResourceIdentifier);

	/**
	 * Assigns the given value to the mManifestXMLBase attribute.
	 *
	 * @param iManifestXMLBase The manifest xml:base value to be assigned.
	 */
	public void setManifestXMLBase(String iManifestXMLBase);

	/**
	 * Assigns the given value to the mResourcesXMLBase attribute.
	 *
	 * @param iResourcesXMLBase The resources xml:base value to be assigned.
	 */
	public void setResourcesXMLBase(String iResourcesXMLBase);

	/**
	 * Assigns the given value to the mResourceXMLBase attribute.
	 *
	 * @param iResourceXMLBase The resource xml:base value to be assigned.
	 */
	public void setResourceXMLBase(String iResourceXMLBase);

	/**
	 * Assigns the given value to the mParameters attribute.
	 *
	 * @param iParameters The parameters value to be assigned.
	 */
	public void setParameters(String iParameters);

	/**
	 * Assigns the given value to the mPersistState attribute of an item.
	 * 
	 * @param iPersistState The persistState value to be assigned.
	 */
	public void setPersistState(String iPersistState);

	/**
	 * Assigns the given value to the mLocation attribute.
	 *
	 * @param iLocation The location value to be assigned.
	 */
	public void setLocation(String iLocation);

	/**
	 * Assigns the given value to the mSCORMType attribute.
	 *
	 * @param iSCORMType The scormtype value to be assigned.
	 */
	public void setSCORMType(String iSCORMType);

	/**
	 * Assigns the given value to the mItemTitle attribute.
	 *
	 * @param iItemTitle The item value to be assigned.
	 */
	public void setItemTitle(String iItemTitle);

	/**
	 * Assigns the given value to the mDataFromLMS attribute.
	 *
	 * @param iDataFromLMS The datafromlms value to be assigned.
	 */
	public void setDataFromLMS(String iDataFromLMS);

	/**
	 * Assigns the given value to the mTimeLimitAction attribute.
	 *
	 * @param iTimeLimitAction The timelimitaction value to be assigned.
	 */
	public void setTimeLimitAction(String iTimeLimitAction);

	/**
	 * Assigns the given value to the minNormalizedMeasure attribute.
	 *
	 * @param iMinNormalizedMeasure The minnormalizedmeasure value to be
	 * assigned.
	 */
	public void setMinNormalizedMeasure(String iMinNormalizedMeasure);

	/**
	 * Assigns the given value to the attemptAbsoluteDurationLimit attribute.
	 *
	 * @param iAttemptAbsoluteDurationLimit The attemptabsolutedurationlimit
	 * value to be assigned.
	 */
	public void setAttemptAbsoluteDurationLimit(
			String iAttemptAbsoluteDurationLimit);

	/**
	 * Assigns the given value to the mCompletionThreshold attribute.
	 * 
	 * @param iCompletionThreshold The completionThreshold value to be 
	 * assigned.
	 */
	public void setCompletionThreshold(String iCompletionThreshold);

	/**
	 * Assigns the given value to the mObjectivesList attribute.
	 * 
	 * @param iObjectivesList The objectives to be assigned.
	 */
	public void setObjectivesList(String iObjectivesList);

	/**
	 * Assigns the given value to the mPrevious attribute.
	 *
	 * @param iPrevious The previous value to be assigned.
	 */
	public void setPrevious(boolean iPrevious);

	/**
	 * Assigns the given value to the mContinue attribute.
	 *
	 * @param iContinue The continue value to be assigned.
	 */
	public void setContinue(boolean iContinue);

	/**
	 * Assigns the given value to the mExit attribute.
	 *
	 * @param iExit The exit value to be assigned.
	 */
	public void setExit(boolean iExit);

	/**
	 * Assigns the given value to the mExitAll attribute.
	 * @param iExitAll the exitAll value to be assigned.
	 */
	public void setExitAll(boolean iExitAll);

	/**
	 * Assigns the given value to the mAbandon attribute.
	 *
	 * @param iAbandon The abandon value to be assigned.
	 */
	public void setAbandon(boolean iAbandon);

	/**
	 * Assigns the given value to the mSuspendAll attribute.
	 *
	 * @param iSuspendAll The suspendAll value to be assigned.
	 */
	public void setSuspendAll(boolean iSuspendAll);

	/**
	 * Gives access to the indentifier value of the 
	 * <code>&lt;organization&gt;</code> element.
	 * 
	 * @return The identifier value of the <code>&lt;organization&gt;</code> 
	 * element.
	 */
	public String getOrganizationIdentifier();

	/**
	 * Gives access to the identifier value of the <code>&lt;item&gt;</code> 
	 * element.
	 *
	 * @return The identifier value of the <code>&lt;item&gt;</code> element.
	 */
	public String getItemIdentifier();

	/**
	 * Gives access to the identifier value of the <code>&lt;resource&gt;</code>
	 * element.
	 *
	 * @return The identifier value of the <code>&lt;resource&gt;</code> 
	 * element.
	 */
	public String getResourceIdentifier();

	/**
	 * Gives access to the xml:base value of the <code>&lt;manifest&gt;</code> 
	 * element.
	 *
	 * @return The xml:base value of the <code>&lt;manifest&gt;</code> element.
	 */
	public String getManifestXMLBase();

	/**
	 * Gives access to the xml:base value of the <code>&lt;resources&gt;</code> 
	 * element.
	 *
	 * @return The xml:base value of the <code>&lt;resources&gt;</code> 
	 * element.
	 */
	public String getResourcesXMLBase();

	/**
	 * Gives access to the xml:base value of the <code>&lt;resource&gt;</code>
	 * element.
	 *
	 * @return The xml:base value of the <code>&lt;resource&gt;</code> element.
	 */
	public String getResourceXMLBase();

	/**
	 * Gives access to the full <code>xml:base</code> value.
	 *
	 * @return The full <code>xml:base</code> value as determined in the 
	 * manifest.
	 */
	public String getXMLBase();

	/**
	 * Gives access to the parameters of the <code>&lt;item&gt;</code> element.
	 *
	 * @return - The parameter value of the <code>&lt;item&gt;</code> element.
	 */
	public String getParameters();

	/**
	 * TODO:  This attribute no longer exists, should we remove it
	 * Gives access to the persistState attribute of the item.
	 * 
	 * @return The value of the persistState attribute of the item.
	 */
	public String getPersistState();

	/**
	 * Gives access to the location of the item.
	 *
	 * @return The location value of the item.
	 */
	public String getLocation();

	/**
	 * Gives access to the SCORM type value of the item.
	 *
	 * @return The SCORM type value of the item.
	 */
	public String getSCORMType();

	/**
	 * Gives access to the title value of the item.
	 *
	 * @return The title value of the item.
	 */
	public String getItemTitle();

	/**
	 * Gives access to the datafromlms element value of the item.
	 *
	 * @return The value of the datafromlms element.
	 */
	public String getDataFromLMS();

	/**
	 * Gives access to the timelimitaction element value of the item.
	 *
	 * @return The value of the timelimitaction element.
	 */
	public String getTimeLimitAction();

	/**
	 * Gives access to the minNormalizedMeasure element value.
	 *
	 * @return The value of the minNormalizedMeasure element.
	 */
	public String getMinNormalizedMeasure();

	/**
	 * Gives access to the attemptAbsoluteDurationLimit element value.
	 *
	 * @return The value of the attemptAbsoluteDurationLimit element.
	 */
	public String getAttemptAbsoluteDurationLimit();

	/**
	 * Gives access to the completionThreshold element value.
	 * 
	 * @return The value of the completionThreshold element.
	 */
	public String getCompletionThreshold();

	/**
	 * Gives access to the objectiveslist element value.
	 * 
	 * @return The value of the objectiveslist element.
	 */
	public String getObjectivesList();

	/**
	 * Gives access to the value of mPrevious, which is a boolean
	 * representing whether a hideLMSUI element for the item had the value of
	 * "previous".
	 *
	 * @return The value of the mPrevious.
	 */
	public boolean getPrevious();

	/**
	 * Gives access to the value of mContinue, which is a boolean
	 * representing whether a hideLMSUI element for the item had the value of
	 * "continue".
	 *
	 * @return The value of the mContinue.
	 */
	public boolean getContinue();

	/**
	 * Gives access to the value of mExit, which is a boolean
	 * representing whether a hideLMSUI element for the item had the value of
	 * "exit".
	 *
	 * @return The value of the mExit.
	 */
	public boolean getExit();

	/**
	 * Gives access ot the value of mExitAll, which is a boolean representation
	 * of whether a hideLMSUI element for the item had the value of "exitAll".
	 * 
	 * @return The value of mExitAll.
	 */
	public boolean getExitAll();

	/**
	 * Gives access to the value of mAbandon, which is a boolean
	 * representing whether a hideLMSUI element for the item had the value of
	 * "abandon".
	 *
	 * @return The value of the mAbandon.
	 */
	public boolean getAbandon();

	/**
	 * Gives access to the value of mSuspendAll, which is a boolean
	 * representing whether a hideLMSUI element for the item had the value of
	 * "suspendAll".
	 *
	 * @return The value of the mSuspendAll.
	 */
	public boolean getSuspendAll();

	/**
	 * Gives access to the full launch line of the item including the full
	 * xml:base of the item.
	 *
	 * @return The full launch location of the item.
	 */
	public String getLaunchLine();

	/**
	 * Displays a string representation of the data structure for the SCO
	 * Integration to the Java logger. 
	 */
	public void print();

	/**
	 * Displays a string representation of the data structure for Integration to
	 * the Java Console.
	 */
	public void printToConsole();

}