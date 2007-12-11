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
package org.sakaiproject.scorm.service.api;

import javax.swing.tree.TreeModel;

import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.sakaiproject.scorm.model.api.SessionBean;

public interface ScormSequencingService {

	public String navigate(int request, SessionBean sessionBean, INavigable agent, Object target);
	
	public void navigate(String choiceRequest, SessionBean sessionBean, INavigable agent, Object target);
	
	public void navigateToActivity(String activityId, SessionBean sessionBean, INavigable agent, Object target);
	
	public SessionBean newSessionBean(String courseId);
	
	public boolean isContinueEnabled(SessionBean sessionBean);
	
	public boolean isContinueExitEnabled(SessionBean sessionBean);
	
	public boolean isPreviousEnabled(SessionBean sessionBean);

	public boolean isResumeEnabled(SessionBean sessionBean);
	
	public boolean isStartEnabled(SessionBean sessionBean);
	
	public boolean isSuspendEnabled(SessionBean sessionBean);
	
	public boolean isControlModeFlow(SessionBean sessionBean);
	
	public boolean isControlModeChoice(SessionBean sessionBean);
	
	public String getCurrentUrl(SessionBean sessionBean);
	
	public TreeModel getTreeModel(SessionBean sessionBean);
	
	// TODO: These methods don't need to be made visible to the tool and probably shouldn't be included
	// in the interface

	public ISeqActivityTree getActivityTree(SessionBean sessionBean);
	
	public IDataManager getDataManager(SessionBean sessionBean, ScoBean scoBean);

	public ISequencer getSequencer(ISeqActivityTree tree);
	
}
