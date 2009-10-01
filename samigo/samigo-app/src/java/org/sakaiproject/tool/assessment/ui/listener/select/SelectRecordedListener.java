package org.sakaiproject.tool.assessment.ui.listener.select;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SelectRecordedListener implements ValueChangeListener {

	private static Log log = LogFactory.getLog(SelectRecordedListener.class);

	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
		log.debug("Processing ... ValueChangeListener");

		SelectActionListener action = new SelectActionListener();
		action.processAction(new ActionEvent(event.getComponent()));
	}

}