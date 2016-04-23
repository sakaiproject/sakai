package org.sakaiproject.tool.assessment.ui.listener.select;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectRecordedListener implements ValueChangeListener {

	private static Logger log = LoggerFactory.getLogger(SelectRecordedListener.class);

	public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
		log.debug("Processing ... ValueChangeListener");

		SelectActionListener action = new SelectActionListener();
		action.processAction(new ActionEvent(event.getComponent()));
	}

}
