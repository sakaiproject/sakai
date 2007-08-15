package org.sakaiproject.scorm.client.api;

import java.io.Serializable;
import java.util.List;

import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.validator.contentpackage.ILaunchData;

public interface IRunState extends Serializable{

	public abstract String navigate(int navRequest, Object target);
	
	public abstract void navigate(String navRequest, Object target);
	
	public abstract ISeqActivityTree getActivityTree(boolean isFresh);

	public abstract ISequencer getSequencer();

	public abstract ISeqActivity getCurrentActivity();

	public abstract String getCurrentActivityId();

	public abstract String getCurrentHref();

	public abstract ILaunchData getCurrentLaunchData();

	public abstract IValidRequests getCurrentNavState();

	public abstract String getCurrentUserId();

	public abstract String getCurrentCourseId();
	
	public abstract List getCurrentObjStatusSet();

	public abstract String getCurrentSco();

	public abstract boolean isTreeVisible();

	public abstract boolean isNextVisible();

	public abstract boolean isContinueEnabled();

	public abstract boolean isContinueExitEnabled();

	public abstract boolean isPreviousEnabled();

	public abstract boolean isResumeEnabled();

	public abstract boolean isStartEnabled();

	public abstract boolean isSuspendEnabled();
	
	public abstract IDataManager getDataManager();
	
	public abstract void setDataManager(IDataManager dataManager);
	
	public abstract boolean isSuspended();
	
	public abstract void setSuspended(boolean isSuspended);

}