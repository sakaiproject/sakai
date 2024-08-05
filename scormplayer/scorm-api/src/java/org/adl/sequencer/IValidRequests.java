package org.adl.sequencer;

import java.util.Map;

import javax.swing.tree.TreeModel;

public interface IValidRequests {

	public Map<String, ActivityNode> getChoice();

	public TreeModel getTreeModel();

	public boolean isContinueEnabled();

	public boolean isContinueExitEnabled();

	public boolean isPreviousEnabled();

	public boolean isResumeEnabled();

	public boolean isStartEnabled();

	public boolean isSuspendEnabled();

}